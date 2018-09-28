package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;

public class Server {

    private HashMap<String, File> fileList = new HashMap<>();
    private Logger log = Logger.getLogger(Server.class.getName());

    private FileFragmenter fragmenter = new FileFragmenter();

    //Part 2
    private Map<String, byte[]> digestToPartsMap = new HashMap<String, byte[]>();
    private HashMap<String, FileFragments> fileFragmentsMap = new HashMap<String, FileFragments>();

    private static final int SERVER_PORT = 8080;
    private static final int CACHE_PORT = 8081;


    public static void main(String[] args) {
            Server server = new Server();
            server.loadFiles2();
            server.runServer();
    }


    private void runServer(){
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("启动服务器，端口："+ SERVER_PORT + "...");


            while (true){
                Socket socket = serverSocket.accept();
                System.out.println("客户端:"+socket.getInetAddress().getLocalHost()+"已连接到服务器");
                new Thread(new Task(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    class Task implements Runnable{

        private Socket socket;

        private DataInputStream dis;
        private DataOutputStream dos;

        private FileInputStream fis;

        public Task(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
                OutputStream os = socket.getOutputStream();

                String commandFromClient = dis.readUTF();
                log.info("received command : [" + commandFromClient + "]");


                //判断命令
                if (commandFromClient.equals("list files")) {
                    listFile(os);

                } else {
                    //sendFile(os, commandFromClient);
                    transferFile(os,commandFromClient);
                }

            } catch (SocketTimeoutException s) {
                log.info("Socket timed out!");

            } catch (IOException e){
                e.printStackTrace();

            } catch (Exception e) {
                e.printStackTrace();

            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        private void sendFile(OutputStream outputStream, String fileName) throws Exception {
            try {
                File file = fileList.get(fileName);
                if(file.exists()) {
                    FileInputStream fis = new FileInputStream(file);
                    DataOutputStream dos = new DataOutputStream(outputStream);

                    // 文件名和长度
                    dos.writeUTF(file.getName());
                    dos.flush();
                    dos.writeLong(file.length());
                    dos.flush();

                    // 开始传输文件
                    System.out.println("======== 开始传输文件 ========");
                    byte[] bytes = new byte[5000];
                    int length = 0;
                    long progress = 0;
                    while((length = fis.read(bytes, 0, bytes.length)) != -1) {
                        dos.write(bytes, 0, length);
                        dos.flush();
                        progress += length;
                        System.out.print("| " + (100*progress/file.length()) + "% |");
                    }
                    System.out.println();
                    System.out.println("======== 文件传输成功 ========");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void listFile(OutputStream outputStream) throws IOException{
            log.info("client requests file list");



            ObjectOutputStream oos = new ObjectOutputStream(outputStream);
            Set<String> files = new HashSet<String>(fileList.keySet());
            oos.writeObject(files);
            oos.writeObject(fileFragmentsMap);

        }

        private void transferFile(OutputStream outputStream, String digestString)
                throws FileNotFoundException, IOException {
            byte[]  filePart= digestToPartsMap.get(digestString);
            byte[] tempByteArray = new byte[2048];
            ByteArrayInputStream bis = new ByteArrayInputStream(filePart);
            DataInputStream dis = new DataInputStream(bis);
            DataOutputStream outToClient = new DataOutputStream(outputStream);
            int read;
            while ((read = dis.read(tempByteArray)) != -1) {
                outToClient.write(tempByteArray, 0, read);
                outToClient.flush();
            }

            bis.close();
            dis.close();
        }

    }



    private void loadFiles() {
        File folder = new File("serverFiles");
        log.info("path:" + folder.getAbsolutePath());
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null) {
            log.info("invalid directory");
            return;
        }
        for (File file : listOfFiles) {
            log.info("add file: "+ file.getName());
            fileList.put(file.getName(), file);
        }
    }

    private void loadFiles2(){
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("MD5");
            File folder = new File("serverFiles");
            log.info(folder.getAbsolutePath());
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles == null) {

                log.info("wrong directory, no files loaded");
            }
            for (File file : listOfFiles) {
                log.info(file.getName());
                fileList.put(file.getName(), file);
                List<byte[]> fragmentList = fragmenter.fragment(file.getAbsolutePath());
                FileFragments fileFragments = new FileFragments();
                for (byte[] filePart : fragmentList) {

                    byte[] thedigest = md.digest(filePart);
                    byte[] encoded = Base64.getEncoder().encode(thedigest);
                    String base64Digest = new String(encoded);
                    digestToPartsMap.put(base64Digest, filePart);
                    fileFragments.getFragmentDigestList().add(base64Digest);
                    log.info("digestToPartsMap map size : " +digestToPartsMap.size());
                }
                fileFragmentsMap.put(file.getName(), fileFragments);
            }

            log.info("digestToPartsMap map size : " +digestToPartsMap.size() +"");
            //System.out.println("fileFragmentsMap size: "+fileFragmentsMap.size());
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //内部类
    class FileFragmenter{

        private  Logger log = Logger.getLogger(FileFragmenter.class.getName());
        //private Integer prime = 10000169;
        private Integer prime = 1009;
        private Integer windowSize = 48;
        private Integer hashFactor = 256;

        public List<byte[]> fragment(String fileName) {
            List<byte[]> resultList = new ArrayList<byte[]>();
            int eliminationMultiplier = generateEliminationMultiplier(hashFactor, windowSize, prime);

            try {
                Path requestedFile = Paths.get(fileName);
                byte[] data = Files.readAllBytes(requestedFile);
                if(data.length <= 2048){
                    resultList.add(data);
                    return resultList;
                }
                int rollingHash = 0;
                int boundary = 0;
                int count = 1;
                int lastBoundaryIndex = 0;
                Queue<Integer> window = new LinkedList<Integer>();

                log.info("total bytes : " + data.length);
                for (int i = 0; i < windowSize; i++) {
                    int value = data[i];
                    window.add(value);
                    rollingHash = (rollingHash * hashFactor + value  + prime) % prime;
                }

                for (int j = windowSize;  j < data.length; j++)
                {
                    int nextValue = data[j];
                    int firstValueInWindow = window.poll();
                    window.add(nextValue);
                    rollingHash = (rollingHash  + prime -  (firstValueInWindow) * (eliminationMultiplier ) % prime) % prime ;
                    rollingHash = (rollingHash * hashFactor + nextValue ) % prime;
                    if (rollingHash == boundary){
                        //log.info("boundary found " + j);
                        if( j - lastBoundaryIndex >= 2048){
                            resultList.add(Arrays.copyOfRange(data,lastBoundaryIndex, j));
                            lastBoundaryIndex = j;

                            count ++;
                        }

                    }

                }
                resultList.add(Arrays.copyOfRange(data,lastBoundaryIndex, data.length));
                log.info("average chunk size " + (data.length / count));

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }catch(Exception e){
                e.printStackTrace();
            }
            log.info(resultList.size()+"");
            return resultList;

        }


        private int generateEliminationMultiplier(Integer hashFactor, Integer windowSize, Integer prime) {
            int multiplier =1;
            for (int i = 0; i < windowSize -1; i++){
                multiplier = (multiplier  * hashFactor )% prime;
            }
            return multiplier;
        }



    }

    static class FileFragments implements Serializable {
        private List<String> fragmentDigestList = new ArrayList<>();

        public List<String> getFragmentDigestList() {
            return fragmentDigestList;
        }

        public void setFragmentDigestList(List<String> fragmentDigestList) {
            this.fragmentDigestList = fragmentDigestList;
        }
    }


}
