package com.sample.sunyton.musicbox;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.sample.sunyton.musicbox.adapter.MyAdapter;
import com.sample.sunyton.musicbox.model.Audio;
import com.sample.sunyton.musicbox.service.MediaplayerService;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String BROADCAST_PLAY_NEW_AUDIO = "com.sample.sunyton.musicbox.PlayNewAudio";
    private ImageView collapsingImageView;
    private int imageIndex = 0;
    private ArrayList<Audio> mAudioList;
    private boolean serviceBound = false;
    private MediaplayerService player;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        collapsingImageView = findViewById(R.id.collapsingImageView);
//        设置图片
        loadCollapsingImage(imageIndex);

//        这里需要请求权限，后期使用自定义的工具类
        loadAudioList();


//        fab点击切换图片index
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageIndex == 4) {
                    imageIndex = 0;
                } else {
                    loadCollapsingImage(++imageIndex);
                }
            }
        });


    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean("serviceStatus", serviceBound);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("serviceStatus");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            player.stopSelf();
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaplayerService.LocalBinder binder = (MediaplayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };


    private void loadAudioList() {
        loadAudio();
        initRecyclerView();
    }

    private void initRecyclerView() {
        if (mAudioList != null && mAudioList.size() > 0) {
            RecyclerView recyclerView = findViewById(R.id.recyclerview);
            MyAdapter myAdapter = new MyAdapter(mAudioList, getApplicationContext());
            recyclerView.setAdapter(myAdapter);
            recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.addOnItemTouchListener(new CustomRouchListener(this, new OnItemClickListener() {
                @Override
                public void onClick(View view, int position) {
//                    获取点击位置，并播放
                    playAudio(position);
                }
            }));
        }
    }

    /**
     * 根据位置获取list中的音乐实体
     *
     * @param position
     */
    private void playAudio(int position) {
        if (!serviceBound) {
            Utils.with(getApplicationContext()).storeAudio(mAudioList).storeAudioIndex(position);
            Intent playerIntent = new Intent(this, MediaplayerService.class);

            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            Utils.with(getApplicationContext()).storeAudioIndex(position);

            Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);

        }
    }

    private void loadAudio() {
        ContentResolver resolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " !=0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = resolver.query(uri, null, selection, null, sortOrder);
        if (cursor != null && cursor.getCount() > 0) {
            mAudioList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                int art = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                mAudioList.add(new Audio(data, title, album, artist));

            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }


    /**
     * 根据index设置图片，index根据fab循环增加
     * @param imageIndex
     */
    private void loadCollapsingImage(int imageIndex) {
        TypedArray array = getResources().obtainTypedArray(R.array.images);
        collapsingImageView.setImageDrawable(array.getDrawable(imageIndex));

    }

}
