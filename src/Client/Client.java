package Client;


import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;


public class Client {

    private static Logger log = Logger.getLogger(Client.class.getName());
    private static final String LIST_FILES_COMMAND = "list files";
    private Socket socket;

    private static final int SERVER_PORT = 8080;
    private static final int CACHE_PORT = 8081;


    private List<String> serverFileList;
    private String selectedFileName;
    private List<String> downloadedFileList;

    public static void main(String[] args) {

        Client client = new Client();
        try {
            client.listFiles();

            //client.requestFile("1.txt");


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listFiles() throws UnknownHostException, IOException{

        this.serverFileList = new ArrayList<String>();
        Socket clientSocket = new Socket("localhost",CACHE_PORT);
        DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
        dos.writeUTF("list files");
        ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
        System.out.println("Sending string: listFile");
        try {

            this.getServerFileList().clear();
            Set<String> files = (Set<String>) ois.readObject();
            for (String fileName : files) {
                log.info(fileName);
                this.getServerFileList().add(fileName);
            }
            for (String item : this.getServerFileList()) {
                log.info("printing " + item);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dos.close();
            ois.close();
            clientSocket.close();
        }
    }

    private void requestFile(String fileName) throws UnknownHostException, IOException{

        Socket clientSocket = new Socket("localhost",CACHE_PORT);
        DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
        DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
        FileOutputStream fos = new FileOutputStream(fileName);
        try {
            dos.writeUTF(fileName);


            String Name = dis.readUTF();
            long fileLength = dis.readLong();




            // 开始接收文件
            System.out.println("======== 开始接收文件 ========");
            byte[] bytes = new byte[5000];
            int length = 0;
            long progress = 0;
            while((length = dis.read(bytes, 0, bytes.length)) != -1) {
                fos.write(bytes, 0, length);
                fos.flush();
                progress += length;
                System.out.print("| " + (100*progress/fileLength) + "% |");
            }
            System.out.println();
            System.out.println("======== 文件传输成功 ========");



        } catch (IOException e){
            e.printStackTrace();
        }




    }

    public List<String> getServerFileList() {
        return serverFileList;
    }


}
