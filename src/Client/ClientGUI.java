package Client;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.UnknownHostException;

public class ClientGUI extends JFrame {

    private Client client;

    private JButton btnDownload;
    private JPanel rootPanel;
    private JList serverFileList;
    private JButton btnRefresh;
    private JList downloadedFileList;
    private JButton btnClear;
    private JTextField fileContent;


    //init
    public ClientGUI(){


        this.client = new Client();

        add(rootPanel);
        setTitle("CLIENT");
        setSize(800,500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);


        btnRefresh.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                try {
                    client.listFiles();
                    System.out.println("count      " + serverFileList.getComponentCount());
                    System.out.println("count1      " + serverFileList.getVisibleRowCount());

                    //System.out.println("count    client  " + client.getServerFileList().size());
                    System.out.println("***********");
                    serverFileList.setListData((String[]) client.getServerFileList().toArray(new String[0]));
                    System.out.println("count      " + serverFileList.getModel().getSize());
                    serverFileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

                    btnClear.setEnabled(true);



                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        //下载按钮

        btnDownload.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(serverFileList.getSelectedIndex() == -1){
                    //btnDownload.setEnabled(false);
                    System.out.println("No file selected");
                } else{
                    String fileName = (String)serverFileList.getSelectedValue();
                    System.out.println("selected file : " + fileName);
                    try {
                        client.requestFile(fileName);
                        downloadedFileList.setListData( client.getDownloadedFileList().toArray(new String[0]));
                    } catch (UnknownHostException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        btnDownload.setEnabled(false);
        btnClear.setEnabled(false);

        serverFileList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(serverFileList.getSelectedIndex() != -1){
                    btnDownload.setEnabled(true);
                }
            }
        });

        downloadedFileList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(downloadedFileList.getSelectedIndex() != -1){
                    //btnClear.setEnabled(true);
                    fileContent.setText(client.getFileContent((String)downloadedFileList.getSelectedValue()));
                }
            }
        });

        btnClear.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                client.clearClient();
                serverFileList.setListData(new String[0]);
                downloadedFileList.setListData(new String[0]);
                fileContent.setText("");
            }
        });
    }





    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ClientGUI clientGUI = new ClientGUI();
                clientGUI.setVisible(true);
            }
        });
    }
}
