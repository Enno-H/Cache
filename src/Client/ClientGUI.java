package Client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientGUI extends JFrame {
    private JButton myButton;
    private JTextArea myTextArea;
    private JPanel rootPanel;


    public ClientGUI(){
        add(rootPanel);
        setTitle("GUI");
        setSize(400,500);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        myButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(rootPanel,"HAHAHA");
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
