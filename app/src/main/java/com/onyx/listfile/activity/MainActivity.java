package com.onyx.listfile.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.widget.TextView;

import com.onyx.listfile.R;
import com.onyx.listfile.request.AsyncRequest;
import com.onyx.listfile.request.RequestCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private String dictPath = "";
    private List<String> checkList;
    private List<String> errInfo;
    private String baseFile;
    private TextView titleTv;
    private Object mScreenInfo;
    private float mScreenWidthPx;
    private float mScreenHeightPx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startCheckFile();
    }

    private void initFilePath() {
        baseFile = getBasePath();
        dictPath = baseFile + "/dictionary";
    }

    private void initList() {
        errInfo = new ArrayList<String>();
        checkList = new ArrayList<String>();
        checkList.add("dz");
        checkList.add("ifo");
        checkList.add("idx");
        checkList.add("yaidx");
    }

    public void getScreenInfo() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenWidthPx = dm.widthPixels;
        mScreenHeightPx = dm.heightPixels;
    }


    @SuppressWarnings("unchecked")
    private void startCheckFile() {
        getScreenInfo();
        initFilePath();
        initList();
        new AsyncRequest<List<String>>(AsyncRequest.DEFAULT_REQUEST, new RequestCallback<List<String>>() {
            @Override
            public List<String> onDoInBackground() {
                checkFileImpl(dictPath);
                return null;
            }

            @Override
            public void onResult(List<String> list) {
                showResult();
            }
        });
    }

    private void checkFileImpl(String filePath) {
        List<File> dictSubFileList = getSubFileList(new File(filePath));
        if (dictSubFileList != null) {
            if(dictSubFileList.size() < 4){
                errInfo.add(dictPath+"下的字典文件夹数量少于4个！");
            }
            boolean hasMD5File = false;
            for (File file : dictSubFileList) {
                if(file.isDirectory()){
                    List<File> subFileList = getSubFileList(file);
                    if (subFileList != null) {
                        List<String> subfixList = new ArrayList<String>();
                        for (File sub : subFileList) {
                            if(!file.isDirectory()){
                                subfixList.add(getFileType(file.getName()));
                            }
                        }
                        if(!subfixList.containsAll(checkList)){
                            errInfo.add(file.getName()+"下缺少文件！");
                        }
                    }else{
                        errInfo.add(file.getName()+"下缺少"+checkList.toString()+"文件！");
                    }

                }else {
                    if("md5".equals(getFileType(file.getName()))){
                        hasMD5File = true;
                    }
                }
            }
            if(!hasMD5File){
                errInfo.add(dictPath+"下缺少 *.md5 文件！");
            }
        }
        errInfo.add(filePath+"下没有任何文件！");
    }

    private void showResult() {
        if (errInfo.size() == 0) {
            showDialog("OK!");
        } else {
            showDialog(errInfo.toString());
        }
    }

    private void showDialog(String str) {
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setIcon(R.drawable.directory)
                .setTitle("字典文件丢失检测结果")
                .setMessage(str)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
        dialog.getWindow().setLayout((int) (mScreenWidthPx * 0.9), (int) (mScreenHeightPx * 0.3));
    }

    public List<File> getSubFileList(File path){
        if(!path.isDirectory()){
            return null;
        }
        List<File> subFileList=new ArrayList<File>();
        File[] files=path.listFiles();
        if(files==null){
            return null;
        }
        for(int i=0;i<files.length;i++){
            subFileList.add(files[i]);
        }
        return subFileList;
    }

    public String getFileType(String fileName){
        if(fileName!=""&&fileName.length()>3){
            int dot=fileName.lastIndexOf(".");
            if(dot>0){
                return fileName.substring(dot+1);
            }
        }
        return "";
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
}
