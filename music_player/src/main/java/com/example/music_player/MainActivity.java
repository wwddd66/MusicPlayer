package com.example.music_player;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    protected Context context;
    private String MEDIA_PATH = "";//SD卡路径
    private ListView listView;
    private ArrayList<HashMap<String, Object>> listItems = new ArrayList<HashMap<String, Object>>();//存放歌曲相关信息
    private FileFilter fileFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return !pathname.isDirectory() && pathname.getName().matches("^.*?\\.(mp3|mid|wma)$");
        }
    };
    private SimpleAdapter simpleAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        listView = findViewById(R.id.lvNames);
        //1. 判断SD卡的playmusic目录及音乐文件功能
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            MEDIA_PATH = Environment.getExternalStorageDirectory().toString();
        } else {
            Toast.makeText(MainActivity.this, "对不起，SD卡不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        File folder = new File(MEDIA_PATH + "/playmusic/");
        if (!folder.exists()) {
            folder.mkdir();
            Toast.makeText(MainActivity.this, "对不起，暂时没有音乐文件", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //2. 定义音乐文件扩展名（定义一个FileFilter）
        File[] fileArr = folder.listFiles(fileFilter);

        //3. 给ListView装配数据
        //将满足扩展名的文件信息（图片，歌曲名，存放位置，播放时间等）存放在ArrayList中
        HashMap<String, Object> map;
        for (File file : fileArr) {
            map = new HashMap<String, Object>();
            map.put("icon", R.mipmap.ic_launcher);//图片
            map.put("fileName", file.getName());//歌曲名
            map.put("filePathName", file.getAbsolutePath());//歌曲存放路径
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(file.getAbsolutePath());
                mediaPlayer.prepare();
                int mTime = mediaPlayer.getDuration();
                long minutes = TimeUnit.MILLISECONDS.toMinutes(mTime);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(mTime) - minutes * 60;
                String sTime = String.format("%d:%d", minutes, seconds);
                map.put("fileTime", sTime);//音乐播放时间
            } catch (IOException e) {
                e.printStackTrace();
            }
            listItems.add(map);
        }

        //4. 将数据与ListView绑定（对于每一行的布局文件）
        String[] from = new String[]{"icon", "fileName", "filePathName", "fileTime"};
        int[] to = new int[]{R.id.iv_head, R.id.tvName, R.id.tvLocation, R.id.tvTime};
        simpleAdapter = new SimpleAdapter(MainActivity.this, listItems, R.layout.item_message, from, to);
        listView.setAdapter(simpleAdapter);

        //5. 单机歌曲名，会打开PlayActivity并将歌曲信息传入
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, PlayActivity.class);
                intent.putExtra("index", position);
                intent.putExtra("list", listItems);
                startActivity(intent);
            }
        });


    }
}
