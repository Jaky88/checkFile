package com.onyx.listfile.activity;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.onyx.listfile.R;
import com.onyx.listfile.request.AsyncRequest;
import com.onyx.listfile.request.RequestCallback;
import com.onyx.listfile.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String FILE_TYPE_FOLDER = "wFl2d";

    public static final String FILE_INFO_NAME = "fName";
    public static final String FILE_INFO_ISFOLDER = "fIsDir";
    public static final String FILE_INFO_TYPE = "fFileType";
    public static final String FILE_INFO_NUM_SONDIRS = "fSonDirs";
    public static final String FILE_INFO_NUM_SONFILES = "fSonFiles";
    public static final String FILE_INFO_PATH = "fPath";
    private String dictPath = "";
    private ListView folderLv;
    private TextView foldernowTv;
    private SimpleAdapter sAdapter;
    private List<Map<String, Object>> aList;
    private String baseFile;
    private int count = 0;
    private int fcount = 0;

    private TextView titleTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initFilePath();
        intView();
        initData();
        initEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(count <= 0 && fcount <= 0){
            showDialog("缺少 *.md5 文件和 文件夹内字典文件!");
        }else if(count <= 0){
            showDialog("缺少 *.md5 文件!");
        }else if(fcount <= 0){
            showDialog("缺少文件夹内 字典 文件!");
        }else{
            showDialog("OK!");
        }
    }

    private void initFilePath() {
        baseFile = FileUtils.getInstance().getBasePath();
        dictPath = baseFile + "/dictionary";
    }

    private void intView() {
        folderLv = (ListView) findViewById(R.id.folder_list);
        foldernowTv = (TextView) findViewById(R.id.folder_now);
        foldernowTv.setText(baseFile);
    }


    @SuppressWarnings("unchecked")
    private void initData() {
        aList = new ArrayList<Map<String, Object>>();
        sAdapter = new SimpleAdapter(this, aList, R.layout.item_list_folder, new String[]{"fImg", "fName", "fInfo"},
                new int[]{R.id.folder_img, R.id.folder_name, R.id.folder_info});
        folderLv.setAdapter(sAdapter);
        checkDir(dictPath);
        new AsyncRequest<>(AsyncRequest.DEFAULT_REQUEST, new Object(), new RequestCallback() {
            @Override
            public void onStart(Object o) {

            }

            @Override
            public Object onDoInBackground(Object o) {
                try {
                    loadFolderList(dictPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public void onResult(Object o) {
                sAdapter.notifyDataSetChanged();
                foldernowTv.setText(file);
            }
        });
    }

    private void initEvent() {
        foldernowTv.setOnClickListener(this);
        folderLv.setOnItemClickListener(this);
    }

    private boolean checkDir(String path) {
        if (null == path) return false;
        File file = new File(path);
        if (file.exists()) {
            return true;
        } else {
            Toast.makeText(this, path + " is not found!", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void loadFolderList(String file) throws IOException {
        count = 0;
        fcount =1;
        aList.clear();
        List<Map<String, Object>> list = FileUtils.getInstance().getSonNode(file);
        if (list != null) {
            Collections.sort(list, FileUtils.getInstance().defaultOrder());

            for (Map<String, Object> map : list) {

                Map<String, Object> gMap = new HashMap<String, Object>();
                String path = ((File) map.get(FileUtils.FILE_INFO_PATH)).getAbsolutePath();
                if(path.startsWith(dictPath)) {
                    listFile(map, gMap, path);
                }else{
                    listFile2(map, gMap, path);
                }
            }
        }
    }

    private void listFile2(Map<String, Object> map, Map<String, Object> gMap, String path) {
        if (map.get(FileUtils.FILE_INFO_ISFOLDER).equals(true)) {
            String msg = "共：";
            msg += map.get(FileUtils.FILE_INFO_NUM_SONDIRS) + "个文件夹,";
            msg += map.get(FileUtils.FILE_INFO_NUM_SONFILES) + "个文件";
            gMap.put("fIsDir", true);
            gMap.put("fImg", R.drawable.directory);
            gMap.put("fInfo", msg);
            gMap.put("fName", map.get(FileUtils.FILE_INFO_NAME));
            gMap.put("fPath", map.get(FileUtils.FILE_INFO_PATH));
            aList.add(gMap);
        }else{
            gMap.put("fImg", R.drawable.unknown_document);
            gMap.put("fImg", R.drawable.unknown_document);
            gMap.put("fInfo", "文件大小:" + FileUtils.getInstance().getFileSize(map.get(FileUtils.FILE_INFO_PATH).toString()));
            gMap.put("fName", map.get(FileUtils.FILE_INFO_NAME));
            gMap.put("fPath", map.get(FileUtils.FILE_INFO_PATH));
            aList.add(gMap);
        }
    }

    private void listFile(Map<String, Object> map, Map<String, Object> gMap, String path) {
        String fileType = (String) map.get(FileUtils.FILE_INFO_TYPE);
        if (map.get(FileUtils.FILE_INFO_ISFOLDER).equals(true)) {
            List<String> info = getAllFiles(path);
            if (info == null || info.size() != 4) {
                String msg = "共：";
                msg += map.get(FileUtils.FILE_INFO_NUM_SONDIRS) + "个文件夹,";
                msg += map.get(FileUtils.FILE_INFO_NUM_SONFILES) + "个文件";
                    msg += ", 缺少文件：";
                    if (info == null) {
                        msg += info.toString();
                    } else if (!info.contains("dz")) {
                        msg += " *.dict.dz ";
                    } else if (!info.contains("ifo")) {
                        msg += " *.ifo ";
                    } else if (!info.contains("idx")) {
                        msg += " *.idx ";
                    } else if (!info.contains("yaidx")) {
                        msg += " *.yaidx ";
                    }

                gMap.put("fIsDir", true);
                gMap.put("fImg", R.drawable.directory);
                gMap.put("fInfo", msg);
                gMap.put("fName", map.get(FileUtils.FILE_INFO_NAME));
                gMap.put("fPath", map.get(FileUtils.FILE_INFO_PATH));
                aList.add(gMap);
                fcount = 0;
            }

        } else {
            gMap.put("fIsDir", false);
            if (fileType.equals("md5")) {
                gMap.put("fImg", R.drawable.unknown_document);
                gMap.put("fImg", R.drawable.unknown_document);
                gMap.put("fInfo", "文件大小:" + FileUtils.getInstance().getFileSize(map.get(FileUtils.FILE_INFO_PATH).toString()));
                gMap.put("fName", map.get(FileUtils.FILE_INFO_NAME));
                gMap.put("fPath", map.get(FileUtils.FILE_INFO_PATH));
//                        aList.add(gMap);
                count++;
            }
        }
    }

    private List<String> getAllFiles(String file) {
        List<Map<String, Object>> list = FileUtils.getInstance().getSonNode(file);
        List<String> info = new ArrayList<String>();
        if (list != null) {
            Collections.sort(list, FileUtils.getInstance().defaultOrder());
            for (Map<String, Object> map : list) {
                String fileType = (String) map.get(FileUtils.FILE_INFO_TYPE);
                if (fileType.equals("dz")) {
                    info.add("dz");
                } else if (fileType.equals("idx")) {
                    info.add("idx");
                } else if (fileType.equals("ifo")) {
                    info.add("ifo");
                } else if (fileType.equals("yaidx")) {
                    info.add("yaidx");
                }
            }
        }
        return info;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            if (aList.get(position).get("fIsDir").equals(true)) {
                loadFolderList(aList.get(position).get("fPath").toString());
            } else {
                Toast.makeText(this, "这是文件，处理程序待添加", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.folder_now) {
            try {
                String folder = FileUtils.getInstance().getParentPath(foldernowTv.getText().toString());
                if (folder == null) {
                    Toast.makeText(this, "无父目录，待处理", Toast.LENGTH_SHORT).show();
                } else {
                    loadFolderList(folder);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void showDialog(String str){
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setIcon(R.drawable.directory)
                .setTitle("文件提示")
                .setMessage(str)
                .setPositiveButton("确定",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("返回",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                    })
                .show();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);//display = getWindowManager().getDefaultDisplay();display.getMetrics(dm)（把屏幕尺寸信息赋值给DisplayMetrics dm）;
        float xdpi = dm.widthPixels;
        float ydpi = dm.heightPixels;
        dialog.getWindow().setLayout((int) (xdpi*0.9), (int) (ydpi*0.3));
    }
}
