package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class Server {

    private HashMap<String, File> fileList = new HashMap<>();
    private Logger log = Logger.getLogger(Server.class.getName());


    public static void main(String[] args) {
            Server server = new Server();
            server.loadFiles();
            server.runServer(8080);
    }


    private void runServer(int port){
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("启动服务器，端口："+ port + "...");


            while (true){
                Socket socket = serverSocket.accept();
                System.out.println("客户端:"+socket.getInetAddress().getLocalHost()+"已连接到服务器");
                new Thread(new Task(socket)).start();




                DataInputStream input = new DataInputStream(socket.getInputStream());
                OutputStream outputStream = socket.getOutputStream();


                //debug
                String commandFromClient = input.readUTF();
                log.info("received command : [" + commandFromClient + "]");

                //判断命令
                if (commandFromClient.equals("list")) {
                    list(outputStream);

                } else {
                    transfer(outputStream, commandFromClient);
                }


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
                if (commandFromClient.equals("list")) {
                    //TODO 展示可用文件

                } else {
                    //TODO 下载文件
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
    }




    private void list(OutputStream outputStream){
        System.out.println("list request received");
    }

    private void transfer(OutputStream outputStream, String fileName){
        System.out.println("transfer request received");
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
                byte[] bytes = new byte[1024];
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
        oos.writeObject(fileList.keySet());
    }


    private void loadFiles() {
        File folder = new File("files");
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

}
