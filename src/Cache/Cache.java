package Cache;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

import FileFragments.FileFragments;


public class Cache extends Thread{

    private static final int SERVER_PORT = 8080;
    private static final int CACHE_PORT = 8081;

    private Logger log = Logger.getLogger(Cache.class.getName());
    private CacheGUI gui;

    private HashMap<String, File> cachedFilesMap = new HashMap<>();
    private Map<String, FileFragments> fileFragmentsMap = new HashMap<>();
    private Map<String, byte[]> digestToPartsMap = new HashMap<String, byte[]>();

    private String cacheLog = new String();



    @Override
    public void run(){
        this.load();
        this.deleteFiles();
        this.runCache();
    }

    public static void main(String[] args) {
        Cache cache = new Cache();
        cache.load();
        cache.deleteFiles();
        cache.runCache();
    }



    private void load(){
        cachedFilesMap = new HashMap<>();
    }

    public void runCache(){
        try {
            ServerSocket cacheSocket = new ServerSocket(CACHE_PORT);
            System.out.println("Start the cache，port："+ CACHE_PORT + "...");

            while (true){
                Socket socket = cacheSocket.accept();
                System.out.println("The client: "+socket.getInetAddress().getLocalHost()+" is connected to this cache.");
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
                    //sendFiles(commandFromClient);
                    transferFile(commandFromClient);
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
                fileFragmentsMap = (Map<String, FileFragments>) ois_fromServer.readObject();
                for (String fileName : files) {
                    log.info(fileName);
                }
                //发送给Client
                ObjectOutputStream oos_toClient = new ObjectOutputStream(os_toClient);
                oos_toClient.writeObject(files);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                dos_toServer.close();
                ois_fromServer.close();
                cacheSocket.close();
            }

        }

        private void sendFiles(String fileName) throws UnknownHostException, IOException{
            log.info("client requests transfer file:"+fileName);
            boolean cached = true;

            try {
                //如果没有缓存过
                if(!cachedFilesMap.containsKey(fileName)){
                    System.out.println("没有缓存过");
                    cached = false;

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

                        //开始接收文件
                        byte[] bytes = new byte[5000];
                        int length = 0;
                        long progress = 0;
                        while((length = dis_fromServer.read(bytes, 0, bytes.length)) != -1) {
                            fos.write(bytes, 0, length);
                            fos.flush();
                            progress += length;
                            System.out.print("| " + (100*progress/fileLength) + "% |");
                        }

                        cachedFilesMap.put(fileName, file);

                        gui.setCachedFiles((String[]) cachedFilesMap.keySet().toArray(new String[0]));


                    } catch (IOException e){
                        e.printStackTrace();
                    } finally {
                        dos_toServer.close();
                        dis_fromServer.close();
                        cacheSocket.close();
                    }


                }
                //上一阶段（接收）：        data-》file
                //下一阶段（发送给Client）  file-》data

                writeLog(fileName,cached);

                try {
                    File file = cachedFilesMap.get(fileName);
                    FileInputStream fis = new FileInputStream(file);

                    // 文件名和长度
                    //dos_toClient.writeLong(file.length());
                    //dos_toClient.flush();

                    // 开始传输文件
                    byte[] bytes = new byte[5000];
                    int length = 0;
                    long progress = 0;
                    while((length = fis.read(bytes, 0, bytes.length)) != -1) {
                        dos_toClient.write(bytes, 0, length);
                        dos_toClient.flush();
                        progress += length;
                        System.out.print("| " + (100*progress/file.length()) + "% |");
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
                        //TODO 有缓存
                        byte[] filePart = digestToPartsMap.get(digest);
                        totalSize = totalSize + filePart.length;
                        cachedSize = cachedSize + filePart.length;
                        bos.write(filePart);
                    } else{
                        //TODO 从Server读取
                        byte[] filePart = requestFilePartFromServer(digest);
                        totalSize = totalSize + filePart.length;
                        bos.write(filePart);
                    }
                }
                gui.setCachedFiles((String[]) digestToPartsMap.keySet().toArray(new String[0]));

            }

            System.out.println("total size: "+ totalSize+" ;cached size: "+cachedSize);

            try {
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
                writeLog(fileName, totalSize, cachedSize);


            } catch (IOException e){
                e.printStackTrace();
            } finally {
                dos_toClient.close();
            }

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


    public void deleteFiles(){
        File directory = new File("cacheFiles");
        File[] listOfFiles = directory.listFiles();
        for (File file : listOfFiles) {
            log.info("delete file: "+ file.getName());
            file.delete();
        }
    }

    public void clearCache() {
        //Part 1
        File directory = new File("cacheFiles");
        File[] listOfFiles = directory.listFiles();
        for (File file : listOfFiles) {
            log.info("delete file: "+ file.getName());
            file.delete();
        }
        cachedFilesMap.clear();

        //Part 2
        digestToPartsMap.clear();
        cacheLog = new String();
    }

    public String getFilePartString(String string) {
        if(digestToPartsMap.containsKey(string)){
            byte[] filePart = digestToPartsMap.get(string);

            StringBuffer sb = new StringBuffer();
            for(int i = 0; i < filePart.length; i++) {
                String hex = Integer.toHexString(filePart[i] & 0xFF);
                if(hex.length() < 2){
                    sb.append(0);
                }
                sb.append(hex);
            }
            return sb.toString();
        }
        return null;
    }


    public CacheGUI getGui() {
        return gui;
    }

    public void setGui(CacheGUI gui) {
        this.gui = gui;
    }

    private void writeLog(String fileName, boolean cached) {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd");
        cacheLog += "user request: file " + fileName + " at " + df.format(date) + "\n\n";
        if (cached) {
            cacheLog += "response: cached file " + fileName + "\n";
        } else {
            cacheLog += "response: file " + fileName + " downloaded from the server" + "\n\n";
        }
        gui.setLogText(cacheLog);
    }

    private void writeLog(String fileName, int totalSize, int cachedSize) {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd");
        cacheLog += "user request: file " + fileName + " at " + df.format(date) + "\n";
        String percentage = ((double) cachedSize / totalSize * 100 + "");
        percentage = percentage.substring(0, percentage.indexOf('.'));
        cacheLog += "response: " + percentage +"% of file " + fileName + " was constructed with the cached data\n\n";
        gui.setLogText(cacheLog);
    }


}
