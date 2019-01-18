package com.occamlab.te.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
    
  // Zips the directory and all of it's sub directories
  public static void zipDir(File zipFile, File dirObj) throws Exception {

    try {
      ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
      if (dirObj.isDirectory()) {
        for (String fileName : dirObj.list()) {
          addDir("", dirObj.getAbsolutePath() + "/" + fileName, out);
        }
      } else {
        addDir("", dirObj.getAbsolutePath(), out);
      }
      out.flush();
      out.close();

    } catch (IOException e) {
      throw new Exception(e.getMessage());
    }

  }
    
  // Add directory to zip file
  private static void addDir(String path, String dirObj, ZipOutputStream out)
      throws IOException {
    File srcFile = new File(dirObj);
    String filePath = "".equals(path) ? srcFile.getName() : path + "/" + srcFile.getName();
    if (srcFile.isDirectory()) {
      for (String fileName : srcFile.list()) {
        addDir(filePath, srcFile.getAbsolutePath() + "/" + fileName, out);
      }
    } else {
      out.putNextEntry(new ZipEntry(filePath));
      FileInputStream in = new FileInputStream(srcFile);

      byte[] buffer = new byte[1024];
      int len;
      while ((len = in.read(buffer)) != -1) {
        out.write(buffer, 0, len);
      }
      out.closeEntry();
      in.close();
    }
  }
}
