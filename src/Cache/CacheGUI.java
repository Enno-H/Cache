package Cache;

import javax.swing.*;

public class CacheGUI extends JFrame{

    private Cache cache;

    private JPanel rootPanel;
    private JList cachedFileList;
    private JTextField logText;
    private JButton btnClear;
    private JButton btnLog;

    public CacheGUI(){

        this.cache = new Cache();


        add(rootPanel);
        setTitle("CACHE");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.cache.setGui(this);

        //cache.deleteFiles();
        //cache.runCache();

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

    public void setCachedFiles(String[] fileNames){

        /***
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                if (!list.isDisposed())
                    list.setItems(fileNames);
            }
        });
         ***/
        System.out.println("触发了");
        cachedFileList.setListData(fileNames);

    }



}
