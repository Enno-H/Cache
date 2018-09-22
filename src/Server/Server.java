package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
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

                DataInputStream input = new DataInputStream(socket.getInputStream());
                OutputStream outputStream = socket.getOutputStream();
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

    private void list(OutputStream outputStream){
        System.out.println("list request received");
    }

    private void transfer(OutputStream outputStream, String fileName){
        System.out.println("transfer request received");
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
