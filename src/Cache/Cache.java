package Cache;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.UnknownHostException;
import java.util.*;
import java.util.logging.Logger;

public class Cache {

    private Logger log = Logger.getLogger(Cache.class.getName());

    private static final int SERVER_PORT = 8080;
    private static final int CACHE_PORT = 8081;

    private HashMap<String, File> cachedFileList = new HashMap<>();

    //Part 2
    private Map<String, FileFragments> fileFragmentsMap = new HashMap<>();
    private Map<String, byte[]> digestToPartsMap = new HashMap<String, byte[]>();



    public static void main(String[] args) {
        Cache cache = new Cache();
        cache.load();
        cache.deleteFiles();
        cache.runCache();
    }

    private void load(){
        cachedFileList = new HashMap<>();
    }

    private void runCache(){
        try {
            ServerSocket cacheSocket = new ServerSocket(CACHE_PORT);
            System.out.println("启动缓存器，端口："+ CACHE_PORT + "...");

            while (true){
                Socket socket = cacheSocket.accept();
                System.out.println("客户端:"+socket.getInetAddress().getLocalHost()+"已连接到服务器");
                new Thread(new Task(socket)).start();
            }

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    class Task implements Runnable{

        private Socket socket;

        private OutputStream os_toClient;
        private DataInputStream dis_fromClient;
        private DataOutputStream dos_toClient;
        private DataInputStream dis_fromServer;
        private DataOutputStream dos_toServer;


        public Task(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                os_toClient = socket.getOutputStream();

                dis_fromClient = new DataInputStream(socket.getInputStream());
                dos_toClient = new DataOutputStream(socket.getOutputStream());

                String commandFromClient = dis_fromClient.readUTF();
                log.info("received command : [" + commandFromClient + "]");


                //判断命令
                if (commandFromClient.equals("list files")) {
                    listFiles();

                } else {
                    sendFiles(commandFromClient);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void listFiles() throws UnknownHostException, IOException{
            log.info("client requests file list");


            Socket cacheSocket = new Socket("localhost",SERVER_PORT);
            DataOutputStream dos_toServer = new DataOutputStream(cacheSocket.getOutputStream());
            dos_toServer.writeUTF("list files");
            ObjectInputStream ois_fromServer = new ObjectInputStream(cacheSocket.getInputStream());
            System.out.println("Sending listFile request to Server");
            try {
                //从Server接收
                Set<String> files = (Set<String>) ois_fromServer.readObject();
                for (String fileName : files) {
                    log.info(fileName);
                }
                //发送给Client
                ObjectOutputStream oos_toClient = new ObjectOutputStream(os_toClient);
                oos_toClient.writeObject(files);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }

        private void sendFiles(String fileName) throws UnknownHostException, IOException{
            log.info("client requests transfer file:"+fileName);

            try {
                //File file = cachedFileList.get(fileName);
                //如果没有缓存过
                if(!cachedFileList.containsKey(fileName)){
                    System.out.println("没有缓存过");

                    Socket cacheSocket = new Socket("localhost",SERVER_PORT);
                    dos_toServer = new DataOutputStream(cacheSocket.getOutputStream());
                    dis_fromServer = new DataInputStream(cacheSocket.getInputStream());
                    //FileOutputStream fos = new FileOutputStream(fileName);

                    File directory = new File("cacheFiles");
                    log.info("path:" + directory.getAbsolutePath());
                    if(!directory.exists()) {
                        directory.mkdir();
                    }

                    File file = new File(directory.getAbsolutePath() + File.separatorChar + fileName);
                    FileOutputStream fos = new FileOutputStream(file);

                    try {
                        dos_toServer.writeUTF(fileName);
                        dos_toServer.flush();
                        long fileLength = dis_fromServer.readLong();

                        //开始接受文件
                        System.out.println("======== 开始接收文件 ========");
                        byte[] bytes = new byte[5000];
                        int length = 0;
                        long progress = 0;
                        while((length = dis_fromServer.read(bytes, 0, bytes.length)) != -1) {
                            fos.write(bytes, 0, length);
                            fos.flush();
                            progress += length;
                            System.out.print("| " + (100*progress/fileLength) + "% |");
                        }
                        System.out.println();
                        System.out.println("======== 文件接收成功 ========");

                        System.out.println("*****"+file.length());
                        cachedFileList.put(fileName, file);


                    } catch (IOException e){
                        e.printStackTrace();
                    } finally {
                        dos_toServer.close();
                        dis_fromServer.close();
                        cacheSocket.close();
                    }


                }

                try {
                    File file = cachedFileList.get(fileName);
                    FileInputStream fis = new FileInputStream(file);

                    // 文件名和长度
                    dos_toClient.writeLong(file.length());
                    dos_toClient.flush();

                    // 开始传输文件
                    System.out.println("======== 开始向Client传输缓存文件 ========");
                    byte[] bytes = new byte[5000];
                    int length = 0;
                    long progress = 0;
                    while((length = fis.read(bytes, 0, bytes.length)) != -1) {
                        dos_toClient.write(bytes, 0, length);
                        dos_toClient.flush();
                        progress += length;
                        System.out.print("| " + (100*progress/file.length()) + "% |");
                        System.out.println();
                        System.out.println("======== 缓存文件传输成功 ========");
                    }
                } catch (IOException e){
                    e.printStackTrace();
                } finally {
                    dos_toClient.close();
                }

            } catch (Exception e){
                e.printStackTrace();
            }
        }

        private void transferFile(String fileName) throws FileNotFoundException, IOException{
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int totalSize = 0;
            int cachedSize = 0;
            if(!fileFragmentsMap.containsKey(fileName)){
                log.info("file not found");
                return;
            } else {
                FileFragments fragments = fileFragmentsMap.get(fileName);
                for (String digest: fragments.getFragmentDigestList()){
                    if(digestToPartsMap.containsKey(digest)){
                        byte[] filePart = digestToPartsMap.get(digest);
                        totalSize = totalSize + filePart.length;
                        cachedSize = cachedSize + filePart.length;
                        bos.write(filePart);
                    } else{
                        //TODO 从服务器读取
                        byte[] filePart = requestFilePartFromServer(digest);
                        totalSize = totalSize + filePart.length;
                        bos.write(filePart);
                    }
                }
            }

            byte[] tempByteArray = new byte[8132];
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            DataInputStream dis = new DataInputStream(bis);
            dos_toClient = new DataOutputStream(os_toClient);
            int read;
            while((read = dis.read(tempByteArray)) != -1){
                dos_toClient.write(tempByteArray, 0, read);
                dos_toClient.flush();
            }
            bis.close();
            dis.close();
        }

        private byte[] requestFilePartFromServer(String digest) throws IOException {
            Socket cacheClient = new Socket("localhost", SERVER_PORT);
            dos_toServer = new DataOutputStream(cacheClient.getOutputStream());
            dis_fromServer = new DataInputStream(cacheClient.getInputStream());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                dos_toServer.writeUTF(digest);

                byte[] buffer = new byte[2048];
                int read = 0;
                while ((read = dis_fromServer.read(buffer)) != -1){
                    bos.write(buffer, 0, read);
                }
                digestToPartsMap.put(digest, bos.toByteArray());
                return bos.toByteArray();
            } catch (IOException e){
                e.printStackTrace();
            } finally {
                dos_toServer.close();
                dis_fromServer.close();
                cacheClient.close();
            }

            return null;
        }
    }


    private void deleteFiles(){
        File directory = new File("cacheFiles");
        File[] listOfFiles = directory.listFiles();
        for (File file : listOfFiles) {
            log.info("delete file: "+ file.getName());
            file.delete();
        }
    }

    //内部类
    class FileFragments implements Serializable {
        private List<String> fragmentDigestList = new ArrayList<>();

        public List<String> getFragmentDigestList() {
            return fragmentDigestList;
        }

        public void setFragmentDigestList(List<String> fragmentDigestList) {
            this.fragmentDigestList = fragmentDigestList;
        }
    }


}
