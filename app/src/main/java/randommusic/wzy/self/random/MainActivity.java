package randommusic.wzy.self.random;

import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static int PLAY_MSG = 0;
    public static int PAUSE_MSG = 1;
    public static int STOP_MSG = 2;
    private ListView mMusiclist = null;
    private List<MusicInfo> infos = null;
    private SimpleAdapter mAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        infos = getAllMusicInfos();
        mMusiclist = (ListView)findViewById(R.id.list_layout);
        setListAdpter(infos);
        mMusiclist.setOnItemClickListener(new MusicListItemClickListener());

        Button btn1 = (Button)findViewById(R.id.ctrl_pause);
        Button btn2 = (Button)findViewById(R.id.ctrl_stop);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("MSG", PAUSE_MSG);
                intent.setClass(MainActivity.this, MyService.class);
                startService(intent);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("MSG", STOP_MSG);
                intent.setClass(MainActivity.this, MyService.class);
                startService(intent);
            }
        });

    }


    public List<MusicInfo> getAllMusicInfos() {
        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        List<MusicInfo> MusicInfos = new ArrayList<MusicInfo>();
        for (int i = 0; i < cursor.getCount(); i++) {
            MusicInfo MusicInfo = new MusicInfo();
            cursor.moveToNext();
            long id = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media._ID));
            String title = cursor.getString((cursor
                    .getColumnIndex(MediaStore.Audio.Media.TITLE)));
            String artist = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ARTIST));
            long duration = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DURATION));
            long size = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media.SIZE));
            String url = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DATA));
            int isMusic = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
            if (isMusic != 0) {
                MusicInfo.setId(id);
                MusicInfo.setTitle(title);
                MusicInfo.setArtist(artist);
                MusicInfo.setDuration(duration);
                MusicInfo.setSize(size);
                MusicInfo.setUrl(url);
                MusicInfos.add(MusicInfo);
            }
        }
        return MusicInfos;
    }


    public void setListAdpter(List<MusicInfo> allInfos) {
        List<HashMap<String, String>> mp3list = new ArrayList<HashMap<String, String>>();
        for (Iterator iterator = allInfos.iterator(); iterator.hasNext();) {
            MusicInfo info = (MusicInfo) iterator.next();
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("title", info.getTitle());
            map.put("Artist", info.getArtist());
            map.put("duration", String.valueOf(info.getDuration()));
            map.put("size", String.valueOf(info.getSize()));
            map.put("url", info.getUrl());
            mp3list.add(map);
        }
        mAdapter = new SimpleAdapter(this, mp3list,
                R.layout.list_item, new String[] { "title", "Artist", "duration" },
                new int[] { R.id.music_title, R.id.music_artist, R.id.music_duration });
        mMusiclist.setAdapter(mAdapter);
    }


    private class MusicListItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            if(infos != null) {
                MusicInfo info = infos.get(position);
                Intent intent = new Intent();
                intent.putExtra("url", info.getUrl());
                android.util.Log.w("----------",info.getTitle());
                intent.putExtra("MSG", PLAY_MSG);
                intent.setClass(MainActivity.this, MyService.class);
                startService(intent);
            }
        }
    }

}
