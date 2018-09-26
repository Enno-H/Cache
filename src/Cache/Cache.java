package Cache;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.UnknownHostException;
import java.util.Set;
import java.util.logging.Logger;

public class Cache {

    private Logger log = Logger.getLogger(Cache.class.getName());

    private static final int SERVER_PORT = 8080;
    private static final int CACHE_PORT = 8081;



    public static void main(String[] args) {
        Cache cache = new Cache();
        //cache.loadFiles();
        cache.runCache();
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

        private DataInputStream dis_fromClient;
        private DataOutputStream dos_toClient;

        private FileInputStream fis;

        public Task(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                dis_fromClient = new DataInputStream(socket.getInputStream());
                dos_toClient = new DataOutputStream(socket.getOutputStream());
                OutputStream os_toClient = socket.getOutputStream();

                String commandFromClient = dis_fromClient.readUTF();
                log.info("received command : [" + commandFromClient + "]");


                //判断命令
                if (commandFromClient.equals("list files")) {
                    //TODO 展示可用文件
                    listFiles(os_toClient);

                } else {
                    //TODO 下载文件
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void listFiles(OutputStream os_toClient) throws UnknownHostException, IOException{
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


    }


}
