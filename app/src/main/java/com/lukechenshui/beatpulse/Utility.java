package com.lukechenshui.beatpulse;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by luke on 12/10/16.
 */

public class Utility {
    private final static String TAG = "Utility";
    public static String getMD5OfFile(File file){
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Exception while getting digest", e);
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception on closing MD5 input stream", e);
            }
        }
    }

    public static ArrayList<File> getListOfAudioFilesInDirectory(Context context){
        ArrayList<File> fileList = new ArrayList<>();
        File location = new File(Config.getLastFolderLocation(context));
        File[] files = location.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (file.isDirectory() || file.getName().endsWith(".mp3")
                        || file.getName().endsWith(".flac") || file.getName().endsWith(".ogg")
                        || file.getName().endsWith(".wav"));
            }
        });
        File parent = location.getParentFile();
        if(parent !=  null){
            fileList.add(location.getParentFile());
        }


        if(files != null && files.length > 0){
            fileList.addAll(Arrays.asList(files));
        }
        if(!fileList.isEmpty()){
            Collections.sort(fileList);
        }

        return fileList;
    }
}
