import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class ZipSingleFile {

    private static final int BUFFER = 1024;


    public static void zipFile(String inputPath, String outputZipPath) throws IOException {
        File input = new File(inputPath);
        if (!input.exists()) {
            throw new IOException("Input does not exist: " + inputPath);
        }

        try (FileOutputStream fileOutput = new FileOutputStream(outputZipPath);
             ZipOutputStream zipOutput = new ZipOutputStream(fileOutput)) {

            if (input.isDirectory()) {
                // dacă este director, îi includem numele ca root în arhivă
                String baseName = input.getName();
                // apelăm recursiv cu parent = baseName + "/"
                addFileToZip(input, baseName + "/", zipOutput);
            } else {
                // fișier singur: entry simplu cu numele fișierului
                addFileToZip(input, "", zipOutput);
            }
        }
    }

    // helper recursiv: parent este calea relativă în arhivă pentru acest file (ex: "folder/sub/")
    private static void addFileToZip(File file, String parent, ZipOutputStream zos) throws IOException {
        String entryName = parent + file.getName();

        if (file.isDirectory()) {
            // adăugăm entry pentru director (utile pentru directoare goale)
            if (!entryName.endsWith("/")) {
                entryName = entryName + "/";
            }
            zos.putNextEntry(new ZipEntry(entryName));
            zos.closeEntry();

            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    addFileToZip(child, entryName, zos);
                }
            }
        } else {
            try (FileInputStream fis = new FileInputStream(file)) {
                ZipEntry ze = new ZipEntry(entryName);
                zos.putNextEntry(ze);

                byte[] buffer = new byte[BUFFER];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    zos.write(buffer, 0, len);
                }
                zos.closeEntry();
            }
        }
    }


    public static String defaultZipPathFor(String inputFilePath) {
        File in = new File(inputFilePath);
        String name = in.getName();
        int dot = name.lastIndexOf('.');
        String base = (dot > 0) ? name.substring(0, dot) : name;
        String parent = in.getParent();
        if (parent == null) parent = ".";
        return parent + File.separator + base + ".zip";
    }
}