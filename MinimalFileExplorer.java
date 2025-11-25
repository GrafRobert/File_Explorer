import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class MinimalFileExplorer extends Frame {

    private java.awt.List dirList = new java.awt.List();
    private Label pathLabel = new Label();
    private Button upBtn = new Button("Up");
    private Button archiveBtn = new Button("Archive");
    private Button unzipBtn = new Button("Unzip");
    private File currentFile;

    public MinimalFileExplorer(File start) {
        super("File Explorer");
        this.currentFile = start;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        refreshList();
        setVisible(true);
    }

    private void jbInit() {
        this.setSize(700, 440);
        this.setResizable(false);
        this.setLayout(null);
        this.setBackground(new Color(240, 240, 240));
        this.setLocationRelativeTo(null);

        dirList.setMultipleMode(false);
        dirList.setBounds(20, 60, 660, 320);
        dirList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = dirList.getSelectedItem();
                if (name != null) {
                    String rawName = name.endsWith("/") ? name.substring(0, name.length() - 1) : name;
                    File f = new File(currentFile, rawName);
                    if (f.exists() && f.isDirectory()) {
                        currentFile = f;
                        refreshList();
                    }
                }
            }
        });

        pathLabel.setBounds(20, 30, 400, 24);

        upBtn.setBounds(430, 30, 80, 28);
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

        // Archive: acceptă fișier sau director
        archiveBtn.setBounds(520, 30, 80, 28);
        archiveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selected = dirList.getSelectedItem();
                if (selected == null) {
                    showMessage("Selectează mai întâi un fișier sau director din listă pentru arhivare.");
                    return;
                }
                String rawName = selected.endsWith("/") ? selected.substring(0, selected.length() - 1) : selected;
                File target = new File(currentFile, rawName);
                if (!target.exists()) {
                    showMessage("Elementul selectat nu există: " + target.getName());
                    return;
                }

                new Thread(() -> {
                    try {
                        String zipPath = ZipSingleFile.defaultZipPathFor(target.getAbsolutePath());
                        ZipSingleFile.zipFile(target.getAbsolutePath(), zipPath);
                        EventQueue.invokeLater(() -> {
                            showMessage("Arhivare finalizată: " + zipPath);
                            refreshList();
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        EventQueue.invokeLater(() -> showMessage("Eroare arhivare: " + ex.getMessage()));
                    }
                }).start();
            }
        });

        // Unzip: folosește UnzipSingleFile.unzipFile(...) (noua structură)
        unzipBtn.setBounds(610, 30, 80, 28);
        unzipBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selected = dirList.getSelectedItem();
                if (selected == null) {
                    showMessage("Selectează mai întâi un fișier .zip din listă pentru dezarhivare.");
                    return;
                }
                String rawName = selected.endsWith("/") ? selected.substring(0, selected.length() - 1) : selected;
                File target = new File(currentFile, rawName);
                if (!target.exists()) {
                    showMessage("Fișierul selectat nu există: " + target.getName());
                    return;
                }
                if (!target.isFile()) {
                    showMessage("Selectează un fișier .zip (nu un director) pentru dezarhivare.");
                    return;
                }
                if (!rawName.toLowerCase().endsWith(".zip")) {
                    showMessage("Fișierul selectat nu este .zip: " + rawName);
                    return;
                }

                new Thread(() -> {
                    try {
                        String destDir = UnzipSingleFile.defaultDestDirFor(target.getAbsolutePath());
                        // apelăm metoda care aruncă IOException; tratăm eroarea aici
                        UnzipSingleFile.unzipFile(target.getAbsolutePath(), destDir);
                        EventQueue.invokeLater(() -> {
                            showMessage("Dezarhivare finalizată în: " + destDir);
                            refreshList();
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        EventQueue.invokeLater(() -> showMessage("Eroare dezarhivare: " + ex.getMessage()));
                    }
                }).start();
            }
        });

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitwindow(e);
            }
        });

        this.add(pathLabel);
        this.add(upBtn);
        this.add(archiveBtn);
        this.add(unzipBtn);
        this.add(dirList);
    }

    private void exitwindow(WindowEvent e) {
        System.exit(0);
    }

    private void refreshList() {
        pathLabel.setText(" Path: " + currentFile.getAbsolutePath());
        dirList.removeAll();
        File[] files = currentFile.listFiles(f -> !f.isHidden());
        if (files == null) {
            showMessage("Nu se poate lista directorul");
            return;
        }
        Arrays.sort(files, Comparator
                .comparing((File f) -> !f.isDirectory())
                .thenComparing(f -> f.getName().toLowerCase()));

        for (File f : files) {
            String display = f.getName();
            if (f.isDirectory()) display += "/";
            dirList.add(display);
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