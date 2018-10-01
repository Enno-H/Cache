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
    private JTextField logText;
    private JButton btnClear;
    private JButton btnLog;
    private JTextField fileContent;
    private JButton btnContent;

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

        /***
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                if (!list.isDisposed())
                    list.setItems(fileNames);
            }
        });
         ***/
        System.out.println("触发了");
        cachedFileList.setListData(items);

    }



}
