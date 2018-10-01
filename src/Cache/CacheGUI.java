package Cache;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CacheGUI extends JFrame{

    private Cache cache;

    private JPanel rootPanel;
    private JList cachedFileList;
    private JButton btnClear;
    private JTextField fileContent;
    private JButton btnContent;
    private JTextArea logText;

    public CacheGUI(){

        this.cache = new Cache();


        add(rootPanel);
        setTitle("CACHE");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.cache.setGui(this);



        cachedFileList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(cachedFileList.getSelectedIndex() != -1){
                    btnContent.setEnabled(true);
                    fileContent.setText(cache.getFilePartString((String)cachedFileList.getSelectedValue()));
                }
            }
        });


        btnClear.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                cache.clearCache();
                cachedFileList.setListData(new String[0]);
                fileContent.setText("");
                logText.setText("");
            }
        });
    }





    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                CacheGUI cacheGUI = new CacheGUI();
                cacheGUI.setVisible(true);
                cacheGUI.cache.start();
            }
        });
    }

    public void setCachedFiles(String[] items){
        cachedFileList.setListData(items);
    }


    public void setLogText(String text) {
        logText.setText(text);
    };



}
