import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class UnzipSingleFile {

    private static final int BUFFER = 1024;

    // Metoda principală pentru dezarhivare
    public static void unzipFile(String zipPath, String destDir) throws IOException {
        File zipFile = new File(zipPath);
        if (!zipFile.exists() || !zipFile.isFile()) {
            throw new IOException("Zip file does not exist: " + zipPath);
        }

        // Crearea directorului destinație
        File dest = new File(destDir);
        if (!dest.exists()) {
            if (!dest.mkdirs()) {
                throw new IOException("Could not create destination directory: " + destDir);
            }
        }

        // Obține calea canonică pentru destinație
        String destCanonical = dest.getCanonicalPath();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            byte[] buffer = new byte[BUFFER];
            while ((entry = zis.getNextEntry()) != null) {
                // Obține numele intrării curente din arhivă
                String entryName = entry.getName();

                // Creăm direct intrarea în structura destinației fără directoare redundante
                File outFile = new File(dest, entryName);

                // Protecție zip-slip
                String outCanonical = outFile.getCanonicalPath();
                if (!outCanonical.startsWith(destCanonical + File.separator)) {
                    zis.closeEntry();
                    throw new IOException("Entry is outside of the target dir: " + entryName);
                }

                if (entry.isDirectory()) {
                    // Creează directorul doar o singură dată, fără redundanțe
                    if (!outFile.exists() && !outFile.mkdirs()) {
                        zis.closeEntry();
                        throw new IOException("Could not create directory: " + outFile.getAbsolutePath());
                    }
                } else {
                    // Creează părintele fișierului dacă nu există și evită directoare redundante
                    File parent = outFile.getParentFile();
                    if (parent != null && !parent.exists() && !parent.mkdirs()) {
                        zis.closeEntry();
                        throw new IOException("Could not create directory: " + parent.getAbsolutePath());
                    }

                    // Scrie conținutul fișierului
                    try (FileOutputStream fos = new FileOutputStream(outFile);
                         BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER)) {
                        int len;
                        while ((len = zis.read(buffer)) != -1) {
                            bos.write(buffer, 0, len);
                        }
                    }
                }

                zis.closeEntry();
            }
        }
    }

    // Versiune alternativă care determină o destinație implicită
    public static void unzipFile(String zipPath) throws IOException {
        String dest = defaultDestDirFor(zipPath);
        unzipFile(zipPath, dest);
    }

    // Creează un director implicit bazat pe numele arhivei
    public static String defaultDestDirFor(String zipFilePath) {
        File z = new File(zipFilePath);
        String name = z.getName();
        int dot = name.lastIndexOf('.');
        String base = (dot > 0) ? name.substring(0, dot) : name;
        String parent = z.getParent();
        if (parent == null) parent = ".";
        return parent + File.separator + base;
    }
}