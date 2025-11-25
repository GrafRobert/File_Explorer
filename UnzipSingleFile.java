import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Versiune organizată a utilitarului de unzip care urmărește structura și stilul ZipSingleFile.
 * - BUFFER constant
 * - metodă publică unzipFile(...) care aruncă IOException (caller-ul tratează)
 * - overload unzipFile(zipPath) care folosește defaultDestDirFor(...)
 * - defaultDestDirFor(...) similar cu defaultZipPathFor(...) din ZipSingleFile
 *
 * Observații:
 * - Extrage toate intrările din arhivă (fișiere și directoare).
 * - Protecție împotriva "zip slip" prin verificarea căii canonice.
 * - Simplu, buffer 1024, try-with-resources.
 */
public class UnzipSingleFile {

    private static final int BUFFER = 1024;

    /**
     * Dezarchivează toată arhiva zipPath în directorul destDir.
     * Aruncă IOException pentru ca apelantul (UI) să trateze erorile.
     *
     * @param zipPath calea arhivei .zip
     * @param destDir directorul în care se vor extrage fișierele (se va crea dacă nu există)
     * @throws IOException la orice eroare I/O sau la detectarea unei intrări care iese din destDir
     */
    public static void unzipFile(String zipPath, String destDir) throws IOException {
        File zipFile = new File(zipPath);
        if (!zipFile.exists() || !zipFile.isFile()) {
            throw new IOException("Zip file does not exist: " + zipPath);
        }

        File dest = new File(destDir);
        if (!dest.exists()) {
            if (!dest.mkdirs()) {
                throw new IOException("Could not create destination directory: " + destDir);
            }
        }

        String destCanonical = dest.getCanonicalPath();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            byte[] buffer = new byte[BUFFER];
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                File outFile = new File(dest, entryName);

                // Protecție zip-slip: calea rezultată trebuie să fie în interiorul destDir
                String outCanonical = outFile.getCanonicalPath();
                if (!outCanonical.equals(destCanonical) && !outCanonical.startsWith(destCanonical + File.separator)) {
                    zis.closeEntry();
                    throw new IOException("Entry is outside of the target dir: " + entryName);
                }

                if (entry.isDirectory()) {
                    if (!outFile.exists() && !outFile.mkdirs()) {
                        zis.closeEntry();
                        throw new IOException("Could not create directory: " + outFile.getAbsolutePath());
                    }
                } else {
                    File parent = outFile.getParentFile();
                    if (parent != null && !parent.exists() && !parent.mkdirs()) {
                        zis.closeEntry();
                        throw new IOException("Could not create directory: " + parent.getAbsolutePath());
                    }

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

    /**
     * Overload: dezarchivează arhiva zipPath într-un director implicit (same-folder + nume fără .zip).
     *
     * @param zipPath calea arhivei .zip
     * @throws IOException
     */
    public static void unzipFile(String zipPath) throws IOException {
        String dest = defaultDestDirFor(zipPath);
        unzipFile(zipPath, dest);
    }

    /**
     * Director implicit pentru extragere: același folder ca arhiva și un subfolder cu numele arhivei (fără .zip)
     * Ex: /path/Folder.zip -> /path/Folder
     */
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