package com.scriptchess.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

public class FileWriter {
    public static void writeFiles(Map<String, String> contentMap) {
        File file = new File("");
        String targetDirectory = file.getAbsolutePath() + File.separator + "target";
        for(Map.Entry<String, String> entry : contentMap.entrySet()) {
            String path = entry.getKey();
            StringBuilder directoryPathBuilder = new StringBuilder();
            String[] split = path.split(Pattern.quote("."));
            for (int i= 0; i<split.length -1; i++) {
                directoryPathBuilder.append(split[i]).append(File.separator);
            }
            String directoryPath = directoryPathBuilder.toString();
            File directory = new File(targetDirectory + File.separator
                    + Constants.GENERATED_SOURCE_DIRECTORY_NAME + File.separator
                    + directoryPath);
            if(!directory.exists()) {
                boolean mkdirs = directory.mkdirs();
                if(!mkdirs) {
                    throw new IllegalStateException("Unable to create Directory at " + directory.getAbsolutePath());
                }
            }
            path = path.replace(".", File.separator);
            try {
                File classFile = new File(targetDirectory + File.separator
                        + Constants.GENERATED_SOURCE_DIRECTORY_NAME + File.separator + path + ".java");
                boolean newFile = classFile.createNewFile();
                if(!newFile)
                    throw new IllegalStateException("Unable to write class file on " + classFile.getAbsolutePath());
                FileUtils.writeStringToFile(classFile, entry.getValue(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
