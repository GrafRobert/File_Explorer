import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class MinimalFileExplorer extends Frame {

    private java.awt.List dirList = new java.awt.List();
    private Label pathLabel = new Label();
    private Button upBtn = new Button("Up");
    private File currentFile;

    public MinimalFileExplorer(File start) {
        super("File Explorer");
        this.currentFile = start;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();  //afiseaza toate functiile pana la cea care a aruncat exceptia
        }
        refreshList();
        setVisible(true);
    }

    private void jbInit() {
        this.setSize(700, 480);
        this.setResizable(false);
        this.setLayout(null);
        this.setBackground(new Color(240, 240, 240));

        dirList.setMultipleMode(false);
        dirList.setBounds(20, 60, 640, 320);
        dirList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = dirList.getSelectedItem();
                if (name != null) {
                    File f = new File(currentFile, name);
                    if (f.exists() && f.isDirectory()) {
                        currentFile = f;
                        refreshList();
                    }
                }
            }
        });

        pathLabel.setBounds(20, 30, 520, 24);

        upBtn.setBounds(550, 30, 110, 28);
        upBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File f = currentFile.getParentFile();
                if (f != null) {
                    currentFile = f;
                    refreshList();
                } else {
                    showMessage("Esti deja la directory-ul principal!");
                }
            }
        });

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitwindow(e);
            }
        });

        this.add(pathLabel);
        this.add(upBtn);
        this.add(dirList);
    }

    private void exitwindow(WindowEvent e) {
        System.exit(0);
    }

    private void refreshList() {
        pathLabel.setText(" Path: " + currentFile.getAbsolutePath());
        dirList.removeAll();
        File[] files = currentFile.listFiles(f -> f.isDirectory() && !f.isHidden());
        if (files == null) {
            showMessage("Nu se poate lista directorul");
            return;
        }
        Arrays.sort(files, Comparator.comparing(f -> f.getName().toLowerCase()));
        for (File f : files) {
            dirList.add(f.getName());
        }
    }

    private void showMessage(String msg) {
        Dialog diag = new Dialog(this, "Info", true);
        diag.setLayout(new BorderLayout());
        Label l = new Label(msg);
        Panel center = new Panel(new FlowLayout(FlowLayout.CENTER));
        center.add(l);
        Button exit = new Button("OK");
        exit.addActionListener(ev -> diag.setVisible(false));
        Panel btnp = new Panel();
        btnp.add(exit);
        diag.add(center, BorderLayout.CENTER);
        diag.add(btnp, BorderLayout.SOUTH);
        diag.setSize(360, 120);
        diag.setLocationRelativeTo(this);
        diag.setVisible(true);
    }

    public static void main(String[] args) {
        String startPath = System.getProperty("user.home");
        if (args.length > 0) {

            File f = new File(args[0]);
            if (f.exists() && f.isDirectory()) startPath = f.getAbsolutePath();

        }
        File start = new File(startPath);
        new MinimalFileExplorer(start);
    }
}