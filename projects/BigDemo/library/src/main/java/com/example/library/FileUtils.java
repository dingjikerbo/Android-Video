package com.example.library;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

public class FileUtils {

    public static void writeFile(File file, List<String> lines, boolean append) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(file, append));
        for (String line : lines) {
            pw.println(line);
        }
        pw.close();
    }

    public static void writeFile2(File file, List<String> lines, boolean append) throws IOException {
        FileOutputStream fos = new FileOutputStream(file, append);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        for (String line : lines) {
            bw.write(line);
            bw.newLine();
        }

        bw.close();
    }


}
