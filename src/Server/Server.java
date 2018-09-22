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
        try {
            Server server = new Server();
            server.loadFiles();


            ServerSocket ss = new ServerSocket(8080);
            System.out.println("启动服务器....");
            Socket s = ss.accept();
            System.out.println("客户端:"+s.getInetAddress().getLocalHost()+"已连接到服务器");
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            //读取客户端发送来的消息
            String mess = br.readLine();
            System.out.println("客户端："+mess);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            bw.write(mess+"\n");
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private  void loadFiles() {

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
