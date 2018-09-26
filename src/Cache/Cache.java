package Cache;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class Cache {

    private Logger log = Logger.getLogger(Cache.class.getName());


    public static void main(String[] args) {
        Cache cache = new Cache();
        //cache.loadFiles();
        cache.runCache(8080);
    }

    private void runCache(int port){
        try {
            ServerSocket cacheSocket = new ServerSocket(port);
            System.out.println("启动缓存器，端口："+ port + "...");

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
                    //TODO 展示可用文件

                } else {
                    //TODO 下载文件
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
