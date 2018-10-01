package Client;


import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;


public class Client {

    private static Logger log = Logger.getLogger(Client.class.getName());
    private static final String LIST_FILES_COMMAND = "list files";

    private static final int SERVER_PORT = 8080;
    private static final int CACHE_PORT = 8081;


    private List<String> serverFileList;
    private List<String> downloadedFileList;

    public static void main(String[] args) {

        Client client = new Client();

        try {
            //client.deleteFiles();


            client.listFiles();

            //client.requestFile("1.pdf");

            //client.requestFile("2.pdf");

            System.out.println(client.getFileContent("1.txt"));


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Client(){
        this.serverFileList = new ArrayList<String>();
        this.downloadedFileList = new ArrayList<>();

    }

    public void listFiles() throws UnknownHostException, IOException{

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

    public List<String> getDownloadedFileList() {
        return downloadedFileList;
    }


    public void requestFile(String fileName) throws UnknownHostException, IOException{

        Socket clientSocket = new Socket("localhost",CACHE_PORT);
        DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
        DataInputStream dis = new DataInputStream(clientSocket.getInputStream());

        File directory = new File("clientFiles");
        log.info("path:" + directory.getAbsolutePath());
        if(!directory.exists()) {
            directory.mkdir();
        }

        File file = new File(directory.getAbsolutePath() + File.separatorChar + fileName);
        FileOutputStream fos = new FileOutputStream(file);


        try {
            dos.writeUTF(fileName);
            //long fileLength = dis.readLong();
            //System.out.println("文件长度为："+ fileLength);




            // 开始接收文件
            System.out.println("======== 开始接收文件 ========");
            byte[] bytes = new byte[8132];
            int length = 0;
            long progress = 0;
            while((length = dis.read(bytes, 0, bytes.length)) != -1) {
                fos.write(bytes, 0, length);
                fos.flush();
                progress += length;
                //System.out.print("| " + (100*progress/fileLength) + "% |");
            }
            System.out.println();
            System.out.println("======== 文件传输成功 ========");

            if (!downloadedFileList.contains(fileName)) {
                downloadedFileList.add(fileName);
            }



        } catch (IOException e){
            e.printStackTrace();
        } finally {
            dis.close();
            dos.close();
            fos.close();
            clientSocket.close();
        }




    }

    public List<String> getServerFileList() {
        return serverFileList;
    }

    private void deleteFiles(){
        File directory = new File("clientFiles");
        File[] listOfFiles = directory.listFiles();
        for (File file : listOfFiles) {
            log.info("delete file: "+ file.getName());
            file.delete();
        }
    }

    public String getFileContent(String fileName) {
        byte[] encoded;
        try {
            encoded = Files.readAllBytes(Paths.get("clientFiles",fileName));
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();

        }
        return null;
    }

}
