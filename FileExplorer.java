
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class FileExplorer {

    private JFrame frame;
    private DefaultListModel<File> model;
    private JList<File> fileList;
    private JLabel pathLabel;
    private File current;


    public FileExplorer(File start)
    {
        current = start;
        createUI();
        refreshList();
    }

    private void createUI()
    {
        frame = new JFrame("FileExplorer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800,600);
        frame.setLocationRelativeTo(null);

        model = new DefaultListModel<>();
        fileList = new JList<>(model);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        fileList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel();
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            if (isSelected) {
                lbl.setBackground(list.getSelectionBackground());
                lbl.setForeground(list.getSelectionForeground());
            }
            else
            {
                lbl.setBackground(list.getBackground());
                lbl.setForeground(list.getForeground());
            }

            String name = value.getName();
            if (name.isEmpty()) name = value.getAbsolutePath();
            lbl.setText((value.isDirectory() ? "[DIR] " : "      ") + name);
            return lbl;
        });

        fileList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount()==2){
                    File f = fileList.getSelectedValue();
                    if(f != null && f.isDirectory()){
                        current=f;
                        refreshList();

                    }
                }
            }
        });

        JScrollPane listScroll = new JScrollPane(fileList);

        pathLabel = new JLabel();
       // pathLabel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));


       JButton upBtn = new JButton("UP");
       upBtn.addActionListener( e-> {
           File f = current.getParentFile();
           if(f != null){
               current  = f;
               refreshList();

           }

           else
           {
               JOptionPane.showMessageDialog(frame, "Esti la directory-ul principal");
           }
       });


       JPanel topPanel = new JPanel(new BorderLayout());
       topPanel.add(pathLabel, BorderLayout.CENTER);

       JPanel btnPanel = new JPanel();
        btnPanel.add(upBtn);
        topPanel.add(btnPanel, BorderLayout.EAST);

        frame.getContentPane().add(topPanel, BorderLayout.NORTH);
        frame.getContentPane().add(listScroll, BorderLayout.CENTER);

}

    private void refreshList() {
        pathLabel.setText(" Path: " + current.getAbsolutePath());
        model.clear();

        File[] files = current.listFiles();
        if (files == null) {

            JOptionPane.showMessageDialog(frame, "Nu se poate lista directorul");
            return;
        }

        //sortare alfabetica
        Arrays.sort(files, Comparator
                .comparing((File f) -> !f.isDirectory())
                .thenComparing(f -> f.getName().toLowerCase()));

        for (File f : files) {
            model.addElement(f);
        }
    }

    public void afisare()
    {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }

    public static void main(String[] args)
    {
        String startPath = System.getProperty("user.home");
        if(args.length>0)
        {
            File f = new File(args[0]);
            if(f.exists() &&  f.isDirectory())
            {
               startPath = f.getAbsolutePath();
            }
        }

        File start =  new File(startPath);
        FileExplorer fileExplorer = new FileExplorer(start);
        fileExplorer.afisare();
    }


}
