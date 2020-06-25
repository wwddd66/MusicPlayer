package com.example.music_player;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class PlayActivity extends AppCompatActivity {
    private TextView tvMusic, tvProgressLeft, tvProgressRight;
    private Button btnLast, btnNext, btnPlay, btnStopTemp, btnStop, btnMode1, btnMode2, btnMode3, btnTurnDown, btnTurnUp;
    private SeekBar seekbar;

    private AudioManager audioManager;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private ArrayList<HashMap<String, Object>> musicList;
    private int index;
    private int playMode = 0;//播放模式：0-顺序播放，1-随机播放，2-单曲循环
    private Random random = new Random();//随机播放歌曲的序号

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 9999:
                    seekbar.setProgress(msg.arg2);
                    seekbar.setMax(msg.arg1);

                    int leftTime = msg.arg2 / 1000;
                    int leftMinute = leftTime / 60;
                    int leftSecond = leftTime % 60;
                    tvProgressLeft.setText(leftMinute + ":" + leftSecond);

                    int rightTime = (msg.arg1 - msg.arg2) / 1000;
                    int rightMinute = rightTime / 60;
                    int rightSecond = rightTime % 60;
                    tvProgressRight.setText(rightMinute + ":" + rightSecond);
                    break;
            }
        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                while (true) {
                    Thread.sleep(200);
                    Message message = new Message();
                    message.what = 9999;
                    message.arg1 = mediaPlayer.getDuration();//歌曲的总进度
                    message.arg2 = mediaPlayer.getCurrentPosition();//歌曲当前进度
                    handler.sendMessage(message);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play);
        initView();

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        final Intent intent = this.getIntent();
        index = intent.getIntExtra("index", 0);
        musicList = (ArrayList<HashMap<String, Object>>) intent.getSerializableExtra("list");

        //播放歌曲及显示相关信息
        //1. 显示歌曲名
        tvMusic.setText((CharSequence) musicList.get(index).get("fileName"));

        //2. 播放歌曲
        playMusic(index);

        btnLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index--;
                if (index < 0) {
                    Toast.makeText(PlayActivity.this, "没有更多歌曲了", Toast.LENGTH_SHORT).show();
                    return;
                }
                playMusic(index);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index++;
                if (index >= musicList.size()) {
                    Toast.makeText(PlayActivity.this, "没有更多歌曲了", Toast.LENGTH_SHORT).show();
                    return;
                }
                playMusic(index);
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }

            }
        });

        btnStopTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
            }
        });

        btnTurnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
            }
        });

        btnTurnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
            }
        });

        btnMode1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMode = 0;//顺序播放
            }
        });

        btnMode2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMode = 1;//随机播放
            }
        });

        btnMode3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMode = 2;//单曲循环
            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {//当进度改变时的操作

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {//当拖动滑块开始执行的操作

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {//当停止拖动滑块开始进行的操作
                mediaPlayer.seekTo(seekBar.getProgress());

            }
        });
    }

    void playMusic(int index) {
        tvMusic.setText(musicList.get(index).get("fileName").toString());//显示歌曲名
        String path = musicList.get(index).get("filePathName").toString();//取出歌曲保存位置
        if (TextUtils.isEmpty(path)) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.release();
        mediaPlayer = null;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.reset();

        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            //mediaPlayer.start();
            mediaPlayer.setOnPreparedListener(new SetPreparedListener());//设置当歌曲准备完毕后的监听事件
            mediaPlayer.setOnCompletionListener(new SetCompletionListener());//设置一首歌播放完的监听事件

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void initView() {
        tvMusic = findViewById(R.id.tvMusic);
        tvProgressLeft = findViewById(R.id.tvProgressLeft);
        tvProgressRight = findViewById(R.id.tvProgressRight);
        btnLast = findViewById(R.id.btnLast);
        btnNext = findViewById(R.id.btnNext);
        btnPlay = findViewById(R.id.btnPlay);
        btnStopTemp = findViewById(R.id.btnStopTemp);
        btnStop = findViewById(R.id.btnStop);
        btnMode1 = findViewById(R.id.btnMode1);
        btnMode2 = findViewById(R.id.btnMode2);
        btnMode3 = findViewById(R.id.btnMode3);
        btnTurnDown = findViewById(R.id.btnTurnDown);
        btnTurnUp = findViewById(R.id.btnTurnUp);
        seekbar = findViewById(R.id.seekbar);
    }

    class SetPreparedListener implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            //1. 开始播放
            mediaPlayer.start();

            //2. 更新seekbar的进度，需要使用handler，runnable配合
            new Thread(runnable).start();

        }
    }

    class SetCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            switch (playMode) {
                case 0://顺序播放
                    index++;
                    if (index >= musicList.size()) {
                        Toast.makeText(PlayActivity.this, "没有更多歌曲了", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    playMusic(index);
                    break;
                case 1://随机播放
                    if (!musicList.isEmpty()) {
                        index = random.nextInt(musicList.size());
                        System.out.println(index);
                        playMusic(index);
                    }
                    break;
                case 2://单曲循环
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                    break;
            }
        }
    }
}
