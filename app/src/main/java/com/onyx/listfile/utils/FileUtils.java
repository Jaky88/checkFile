package com.onyx.listfile.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 12345 on 2017/4/7.
 */

public class FileUtils {
    public static final String FILE_TYPE_FOLDER="wFl2d";

    public static final String FILE_INFO_NAME="fName";
    public static final String FILE_INFO_ISFOLDER="fIsDir";
    public static final String FILE_INFO_TYPE="fFileType";
    public static final String FILE_INFO_NUM_SONDIRS="fSonDirs";
    public static final String FILE_INFO_NUM_SONFILES="fSonFiles";
    public static final String FILE_INFO_PATH="fPath";

    private static FileUtils gfu;

    private FileUtils(){

    }

    public static synchronized FileUtils getInstance(){
        if(gfu==null){
            gfu=new FileUtils();
        }
        return gfu;
    }

    public List<Map<String, Object>> getSonNode(File path){
        if(path.isDirectory()){
            List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
            File[] files=path.listFiles();
            if(files!=null){

                for(int i=0;i<files.length;i++){
                    Map<String, Object> fileInfo=new HashMap<String, Object>();
                    fileInfo.put(FILE_INFO_NAME, files[i].getName());
                    if(files[i].isDirectory()){
                        fileInfo.put(FILE_INFO_ISFOLDER, true);
                        File[] bFiles=files[i].listFiles();
                        if(bFiles==null){
                            fileInfo.put(FILE_INFO_NUM_SONDIRS, 0);
                            fileInfo.put(FILE_INFO_NUM_SONFILES, 0);
                        }else{
                            int getNumOfDir=0;
                            for(int j=0;j<bFiles.length;j++){
                                if(bFiles[j].isDirectory()){
                                    getNumOfDir++;
                                }
                            }
                            fileInfo.put(FILE_INFO_NUM_SONDIRS, getNumOfDir);
                            fileInfo.put(FILE_INFO_NUM_SONFILES, bFiles.length-getNumOfDir);
                        }
                        fileInfo.put(FILE_INFO_TYPE, FILE_TYPE_FOLDER);
                    }else{
                        fileInfo.put(FILE_INFO_ISFOLDER, false);
                        fileInfo.put(FILE_INFO_NUM_SONDIRS, 0);
                        fileInfo.put(FILE_INFO_NUM_SONFILES, 0);
                        fileInfo.put(FILE_INFO_TYPE, getFileType(files[i].getName()));
                    }
                    fileInfo.put(FILE_INFO_PATH, files[i].getAbsoluteFile());
                    list.add(fileInfo);
                }
                return list;
            }else{
                return null;
            }
        }else{
            return null;
        }
    }

    public List<Map<String, Object>> getSonNode(String pathStr){
        File path=new File(pathStr);
        return getSonNode(path);
    }

    public List<Map<String, Object>> getBrotherNode(File path){
        if(path.getParentFile()!=null){
            return getSonNode(path.getParentFile());
        }else{
            return null;
        }
    }

    public List<Map<String, Object>> getBrotherNode(String pathStr){
        File path=new File(pathStr);
        return getBrotherNode(path);
    }

    public String getParentPath(File path){
        if(path.getParentFile()==null){
            return null;
        }else{
            return path.getParent();
        }
    }

    public String getParentPath(String pathStr){
        File path=new File(pathStr);
        if(path.getParentFile()==null){
            return null;
        }else{
            return path.getParent();
        }
    }

    public String getSDPath(){
        String sdcard= Environment.getExternalStorageState();
        if(sdcard.equals(Environment.MEDIA_MOUNTED)){
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }else{
            return null;
        }
    }

    public String getBasePath(){
        String basePath=getSDPath();
        if(basePath==null){
            return Environment.getDataDirectory().getAbsolutePath();
        }else{
            return basePath;
        }
    }

    public String getFileSize(File path) throws IOException {
        if(path.exists()){
            DecimalFormat df = new DecimalFormat("#.00");
            String sizeStr="";
            FileInputStream fis=new FileInputStream(path);
            long size=fis.available();
            fis.close();
            if(size<1024){
                sizeStr=size+"B";
            }else if(size<1048576){
                sizeStr=df.format(size/(double)1024)+"KB";
            }else if(size<1073741824){
                sizeStr=df.format(size/(double)1048576)+"MB";
            }else{
                sizeStr=df.format(size/(double)1073741824)+"GB";
            }
            return sizeStr;
        }else{
            return null;
        }
    }

    public String getFileSize(String fpath){
        File path=new File(fpath);
        if(path.exists()){
            DecimalFormat df = new DecimalFormat("#.00");
            String sizeStr="";
            long size=0;
            try {
                FileInputStream fis = new FileInputStream(path);
                size=fis.available();
                fis.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "鏈煡澶у皬";
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "鏈煡澶у皬";
            }
            if(size<1024){
                sizeStr=size+"B";
            }else if(size<1048576){
                sizeStr=df.format(size/(double)1024)+"KB";
            }else if(size<1073741824){
                sizeStr=df.format(size/(double)1048576)+"MB";
            }else{
                sizeStr=df.format(size/(double)1073741824)+"GB";
            }
            return sizeStr;
        }else{
            return "鏈煡澶у皬";
        }
    }

    public String getFileType(String fileName){
        if(fileName!=""&&fileName.length()>3){
            int dot=fileName.lastIndexOf(".");
            if(dot>0){
                return fileName.substring(dot+1);
            }else{
                return "";
            }
        }
        return "";
    }

    public Comparator<Map<String, Object>> defaultOrder() {

        final String orderBy0=FILE_INFO_ISFOLDER;
        final String orderBy1=FILE_INFO_TYPE;
        final String orderBy2=FILE_INFO_NAME;

        Comparator<Map<String, Object>> order=new Comparator<Map<String,Object>>() {

            @Override
            public int compare(Map<String, Object> lhs, Map<String, Object> rhs) {
                // TODO Auto-generated method stub
                int left0=lhs.get(orderBy0).equals(true)?0:1;
                int right0=rhs.get(orderBy0).equals(true)?0:1;
                if(left0==right0){
                    String left1=lhs.get(orderBy1).toString();
                    String right1=rhs.get(orderBy1).toString();
                    if(left1.compareTo(right1)==0){
                        String left2=lhs.get(orderBy2).toString();
                        String right2=rhs.get(orderBy2).toString();
                        return left2.compareTo(right2);
                    }else{
                        return left1.compareTo(right1);
                    }
                }else{
                    return left0-right0;
                }
            }
        };

        return order;
    }

}
