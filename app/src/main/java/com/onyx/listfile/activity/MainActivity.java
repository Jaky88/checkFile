package com.onyx.listfile.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private List<String> tempList;
    private List<String> checkList;
    private List<String> lostList;
    private String baseFile;
    private TextView titleTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intView();
        initData();
        initEvent();
    }

    private void intView() {
        folderLv = (ListView) findViewById(R.id.folder_list);
        foldernowTv = (TextView) findViewById(R.id.folder_now);
    }

    private void initEvent() {
        foldernowTv.setOnClickListener(this);
        folderLv.setOnItemClickListener(this);
    }

    private void initFilePath() {
        baseFile = FileUtils.getInstance().getBasePath();
        dictPath = baseFile + "/dictionary";
    }

    @SuppressWarnings("unchecked")
    private void initData() {
        initFilePath();
        initList();
        new AsyncRequest<List<Map<String, Object>>>(AsyncRequest.DEFAULT_REQUEST, new RequestCallback<List<Map<String, Object>>>() {
            @Override
            public void onStart() {
                tempList.clear();
                lostList.clear();
                aList.clear();
                sAdapter = new SimpleAdapter(MainActivity.this, aList, R.layout.item_list_folder,
                        new String[]{"fImg", "fName", "fInfo"},
                        new int[]{R.id.folder_img, R.id.folder_name, R.id.folder_info});
                folderLv.setAdapter(sAdapter);
                foldernowTv.setText(dictPath);
            }

            @Override
            public List<Map<String, Object>> onDoInBackground() {
                try {
                    loadFolderList(dictPath);
                } catch (IOException e) {
                }
                return null;
            }

            @Override
            public void onResult(List<Map<String, Object>> o) {
                sAdapter.notifyDataSetChanged();
                foldernowTv.setText(dictPath);
                reportFilesDetails();
            }
        });
    }

    private void initList() {
        aList = new ArrayList<Map<String, Object>>();
        tempList = new ArrayList<String>();
        lostList = new ArrayList<String>();
        checkList = new ArrayList<String>();
        checkList.add("dz");
        checkList.add("ifo");
        checkList.add("idx");
        checkList.add("yaidx");
        checkList.add("md5");
    }

    private void loadFolderList(String file) throws IOException {
        List<Map<String, Object>> list = FileUtils.getInstance().getSonNode(file);
        if (list != null) {
            Collections.sort(list, FileUtils.getInstance().defaultOrder());
            for (Map<String, Object> map : list) {
                Map<String, Object> gMap = new HashMap<String, Object>();
                String path = ((File) map.get(FileUtils.FILE_INFO_PATH)).getAbsolutePath();
                listFile(map, gMap, path);
            }
        }
    }

    private void listFile(Map<String, Object> map, Map<String, Object> gMap, String path) {
        if (map.get(FileUtils.FILE_INFO_ISFOLDER).equals(true)) {
            String msg = "共：";
            msg += map.get(FileUtils.FILE_INFO_NUM_SONDIRS) + "个文件夹,";
            msg += map.get(FileUtils.FILE_INFO_NUM_SONFILES) + "个文件";
            if (path.startsWith(dictPath)) {
                msg = setMsg(path, msg);
            }
            gMap.put("fIsDir", true);
            gMap.put("fImg", R.drawable.directory);
            gMap.put("fInfo", msg);
            gMap.put("fName", map.get(FileUtils.FILE_INFO_NAME));
            gMap.put("fPath", map.get(FileUtils.FILE_INFO_PATH));
            aList.add(gMap);
        } else {
            matchFile(path);
            gMap.put("fIsDir", false);
            gMap.put("fImg", R.drawable.unknown_document);
            gMap.put("fImg", R.drawable.unknown_document);
            gMap.put("fInfo", "文件大小:" + FileUtils.getInstance().getFileSize(map.get(FileUtils.FILE_INFO_PATH).toString()));
            gMap.put("fName", map.get(FileUtils.FILE_INFO_NAME));
            gMap.put("fPath", map.get(FileUtils.FILE_INFO_PATH));
            aList.add(gMap);
        }
    }

    private void reportFilesDetails() {
        removeDuplicate(tempList);
        lostList.addAll(checkList);
        lostList.removeAll(tempList);
        if (lostList.size() == 0) {
            showDialog("OK!");
        } else {
            showDialog("缺少 " + lostList.toString() + " 文件!");
        }
    }

    private String setMsg(String path, String msg) {
        List<String> info = getAllFiles(path);
        if (info == null || info.size() != 4) {
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
        }
        return msg;
    }

    private void matchFile(String patch) {
        String suffix = getFileSuffix(patch);
        String dir1 = getSubStr(patch, 1);
        String dir2 = getSubStr(patch, 2);
        if ((dictPath.equals(dir1) && suffix.equals("md5")) || (dictPath.equals(dir2) && checkList.contains(suffix))) {
            tempList.add(suffix);
        }
    }

    private String getFileSuffix(String patch) {
        int start = patch.lastIndexOf(".");
        int end = patch.length();

        if (start != -1 && end != -1) {
            return patch.substring(start + 1, end);
        } else {
            return null;
        }
    }

    private String getSubStr(String str, int num) {
        String result = "";
        int i = 0;
        while (i < num) {
            int lastFirst = str.lastIndexOf('/');
            result = str.substring(lastFirst) + result;
            str = str.substring(0, lastFirst);
            i++;
        }
        return str;
    }

    private List<String> removeDuplicate(List<String> list)
    {
        Set set = new LinkedHashSet<String>();
        set.addAll(list);
        list.clear();
        list.addAll(set);
        return list;
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

    private void showDialog(String str) {
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setIcon(R.drawable.directory)
                .setTitle("文件提示")
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
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        float xPx = dm.widthPixels;
        float yPx = dm.heightPixels;
        dialog.getWindow().setLayout((int) (xPx * 0.9), (int) (yPx * 0.3));
    }
}
