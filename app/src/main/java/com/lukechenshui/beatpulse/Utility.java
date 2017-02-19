package com.lukechenshui.beatpulse;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ListIterator;

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
                return (file.getName().endsWith(".mp3")
                        || file.getName().endsWith(".flac") || file.getName().endsWith(".ogg")
                        || file.getName().endsWith(".wav") || file.getName().endsWith(".m4a"));
            }
        });

        if(files != null && files.length > 0){
            fileList.addAll(Arrays.asList(files));
        }
        if(!fileList.isEmpty()){
            Collections.sort(fileList);
        }
        return fileList;
    }

    public static ArrayList<File> getListOfAudioFilesInDirectoryExcept(Context context, final File exceptFile){
        ArrayList<File> fileList = new ArrayList<>();
        File location = new File(Config.getLastFolderLocation(context));
        File[] files = location.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return !file.getAbsolutePath().equals(exceptFile.getAbsolutePath())
                        &&  isMusicFileSupported(file);
            }
        });

        if(files != null && files.length > 0){
            fileList.addAll(Arrays.asList(files));
        }
        if(!fileList.isEmpty()){
            Collections.sort(fileList);
        }
        return fileList;
    }

    public static ArrayList<File> getListOfFoldersAndAudioFilesInDirectoryWithParent(Context context){
        ArrayList<File> fileList = new ArrayList<>();
        File location = new File(Config.getLastFolderLocation(context));
        File[] files = location.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (file.isDirectory()  || isMusicFileSupported(file));
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

    public static ArrayList<File> getListOfFoldersAndAudioFilesInDirectory(Context context, File directory){
        ArrayList<File> fileList = new ArrayList<>();
        ArrayList<File> toDelete = new ArrayList<>();
        fileList.addAll(Arrays.asList(directory.listFiles()));

        ListIterator<File> iterator = fileList.listIterator();

        while (iterator.hasNext()) {
            File file = iterator.next();
            if (file.isDirectory()) {
                File[] subFiles = file.listFiles();
                if (subFiles != null) {
                    for (File subFile : subFiles) {
                        if (isMusicFileSupported(subFile) || subFile.isDirectory()) {
                            iterator.add(subFile);
                        }

                    }
                }
                toDelete.add(file);
            }
        }

        for (File fileToDelete : toDelete) {
            fileList.remove(fileToDelete);
        }

        /*
        try{
            directory = directory.getCanonicalFile();
        }
        catch (IOException exc){
            Log.d(TAG, "Exception occured while getting canonical file of " + directory.getAbsolutePath(), exc);
        }
        File location = directory;
        File[] files = location.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (file.isDirectory() || isMusicFileSupported(file));
            }
        });
        File parent = location.getParentFile();

        if(files != null && files.length > 0){
            fileList.addAll(Arrays.asList(files));
        }
        files = null;
        ArrayList<File> secondaryList = new ArrayList<>();
        ArrayList<File> unnecessaryFolders = new ArrayList<>();
        try {
            ListIterator iterator = fileList.listIterator();
            for (File file : fileList) {
                if (file.isDirectory()) {
                    secondaryList.addAll(getListOfFoldersAndAudioFilesInDirectory(context, file));
                    unnecessaryFolders.add(file);
                }
            }
        } catch (StackOverflowError exc) {
            Log.d(TAG, "StackOverflow occurred!", exc);
        }
        for(int counter = 0; counter < secondaryList.size(); counter++){
            File file = secondaryList.get(counter);
            if(file.isDirectory()){
                secondaryList.remove(counter);
                counter--;
                continue;
            }
        }

        for(int counter = 0; counter < fileList.size(); counter++){
            File file = fileList.get(counter);
            if(file.isDirectory()){
                fileList.remove(counter);
                counter--;
                continue;
            }
        }
        fileList.addAll(secondaryList);
        if(!fileList.isEmpty()){
            Collections.sort(fileList);
        }
        */
        return fileList;
    }

    public static boolean isMusicFileSupported(File file){
        return (file.getName().endsWith(".mp3")
                || file.getName().endsWith(".flac") || file.getName().endsWith(".ogg")
                || file.getName().endsWith(".wav") || file.getName().endsWith(".m4a"));
    }
}
