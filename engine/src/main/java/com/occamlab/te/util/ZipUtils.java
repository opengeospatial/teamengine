package com.occamlab.te.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
        // Zips the directory and all of it's sub directories
    public static void zipDir(File zipFile, File dirObj) throws Exception
    {
        //File dirObj = new File(dir);
        if(!dirObj.isDirectory())
        {
            System.err.println(dirObj.getName() + " is not a directory");
            System.exit(1);
        }

        try
        {

            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

            System.out.println("Creating : " + zipFile);

            addDir(dirObj, out);
            // Complete the ZIP file
            out.close();


        }
        catch (IOException e)
        {
                throw new Exception(e.getMessage());
        }

    }

    // Add directory to zip file
    private static void addDir(File dirObj, ZipOutputStream out)  throws IOException
    {
        File[] dirList = dirObj.listFiles();
        byte[] tmpBuf = new byte[1024];

        for (int i=0; i<dirList.length; i++)
        {
            if(dirList[i].isDirectory())
            {
                addDir(dirList[i], out);
                continue;
            }

            FileInputStream in = new FileInputStream(dirList[i].getAbsolutePath());
            System.out.println(" Adding: " + dirList[i].getAbsolutePath());

            out.putNextEntry(new ZipEntry(dirList[i].getAbsolutePath()));

            // Transfer from the file to the ZIP file
            int len;
            while((len = in.read(tmpBuf)) > 0)
            {
                out.write(tmpBuf, 0, len);
            }

            // Complete the entry
            out.closeEntry();
            in.close();
        }
    }
}
