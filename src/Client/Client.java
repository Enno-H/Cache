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


    private List<String> serverFileList;
    private String selectedFileName;
    private List<String> downloadedFileList;

    public static void main(String[] args) {

        Client client = new Client();
        try {
            client.listFiles();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        /**
        try {
            Socket s = new Socket("127.0.0.1",8080);

            //构建IO
            InputStream is = s.getInputStream();
            OutputStream os = s.getOutputStream();

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
            //向服务器端发送一条消息
            bw.write("测试客户端和服务器通信，服务器接收到消息返回到客户端\n");
            bw.flush();

            //读取服务器返回的消息
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String mess = br.readLine();
            System.out.println("服务器："+mess);


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
         **/


    }

    private void listFiles() throws UnknownHostException, IOException{

        this.serverFileList = new ArrayList<String>();


        Socket clientSocket = new Socket("127.0.0.1",8080);
        DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
        dos.writeUTF(LIST_FILES_COMMAND);
        ObjectInputStream inputFromServer = new ObjectInputStream(clientSocket.getInputStream());
        System.out.print("Sending string: '" + LIST_FILES_COMMAND + "'\n");
        try {

            Set<String> fileList = null;
            this.getServerFileList().clear();
            fileList = (Set<String>) inputFromServer.readObject();
            for (String fileName : fileList) {
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
            inputFromServer.close();
            clientSocket.close();
        }
    }

    public List<String> getServerFileList() {
        return serverFileList;
    }


}
