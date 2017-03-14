package self.wzy.random;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static public String TAG = "MainActivity";
    private List<MusicItem> mActivityMusicList;
    private ListView mMusicListView;
    private Button mPlayBtn;
    private Button mNextBtn;
    private AlwaysFocusedTextView mMusicTitle;
    private TextView mPlayedTime;
    private TextView mDurationTime;
    private SeekBar mMusicSeekBar;
    private MusicService.MusicServiceIBinder mMusicServiceBinder;
    private MusicItemAdapter adapter;
    private int goodPosition = 0;
    private int start_index, end_index;
    public static boolean isInitList = false;
    public static boolean isScrolling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        goodPosition = getWindowManager().getDefaultDisplay().getHeight()/4;
        Intent i = new Intent(this, MusicService.class);
        startService(i);
        initViews();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isInitList = false;
        mMusicServiceBinder.unregisterOnStateChangeListener(mStateChangeListenr);
        unbindService(mServiceConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent i = new Intent(this, MusicService.class);
        bindService(i, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityMusicList.clear();
    }

    private void initViews(){
        mActivityMusicList = new ArrayList<MusicItem>();
        mMusicListView = (ListView) findViewById(R.id.music_list);
        mMusicListView.setOnItemClickListener(mOnMusicItemClickListener);

        mMusicListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                isInitList = true;
                switch (scrollState){
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE: // stop scroll
                        isScrolling = false;
                        for (; start_index < end_index; start_index++) {
                            Bitmap bmp = Utils.createThumbFromUir(getContentResolver(), mActivityMusicList.get(start_index).thumbUri);
                            if (bmp != null) {
                                ((ImageView) mMusicListView.findViewWithTag(start_index)).setImageBitmap(bmp);
                            }
                        }
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING: // start scroll
                        isScrolling = true;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                start_index = firstVisibleItem;
                end_index = firstVisibleItem + visibleItemCount;

            }
        });


        mPlayBtn = (Button) findViewById(R.id.play_btn);
        mNextBtn = (Button) findViewById(R.id.next_btn);

        mMusicTitle = (AlwaysFocusedTextView) findViewById(R.id.music_title);
        mDurationTime = (TextView) findViewById(R.id.duration_time);
        mPlayedTime = (TextView) findViewById(R.id.played_time);
        mMusicSeekBar = (SeekBar) findViewById(R.id.seek_music);
        mMusicSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);

        Button more = (Button)findViewById(R.id.more_btn);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity.this).setTitle("EXIT?")
                        .setPositiveButton("YES",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mMusicServiceBinder.stop();
                                finish();
                            }
                        }).setNegativeButton("NO",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
            }
        });
    }

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if(mMusicServiceBinder != null) {
                mMusicServiceBinder.seekTo(seekBar.getProgress());
            }
        }
    };

    private AdapterView.OnItemClickListener mOnMusicItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(mMusicServiceBinder != null) {
                mMusicServiceBinder.PlayItem(mActivityMusicList.get(position));
            }
        }
    };

    private void enableControlPanel(boolean enabled) {
        mPlayBtn.setEnabled(enabled);
        mNextBtn.setEnabled(enabled);
        mMusicSeekBar.setEnabled(enabled);
    }

    private void updatePlayingInfo(MusicItem item) {
        String times = Utils.convertMSecendToTime(item.duration);
        mDurationTime.setText(times);

        times = Utils.convertMSecendToTime(item.playedTime);
        mPlayedTime.setText(times);

        mMusicSeekBar.setMax((int) item.duration);
        mMusicSeekBar.setProgress((int) item.playedTime);

        mMusicTitle.setText(item.name);
    }

    private MusicService.OnStateChangeListenr mStateChangeListenr = new MusicService.OnStateChangeListenr() {

        @Override
        public void onPlayProgressChange(MusicItem item) {
            String times = Utils.convertMSecendToTime(item.playedTime);
            mPlayedTime.setText(times);

            mMusicSeekBar.setMax((int) item.duration);
            mMusicSeekBar.setProgress((int) item.playedTime);
        }

        @Override
        public void onPlay(MusicItem item) {
            mPlayBtn.setBackgroundResource(R.drawable.ic_pause);
            updatePlayingInfo(item);
            enableControlPanel(true);
            adapter.setSelectItem(mActivityMusicList.indexOf(item));
            adapter.notifyDataSetInvalidated();
        }

        @Override
        public void onPause(MusicItem item) {
            mPlayBtn.setBackgroundResource(R.drawable.ic_play);
            enableControlPanel(true);
        }

        @Override
        public void onUpdateInfos(MusicItem item){
            if(mActivityMusicList != null && !mActivityMusicList.contains(item)){
                mActivityMusicList.add(item);
            }
            adapter.notifyDataSetChanged();
        }
    };


    private ServiceConnection mServiceConnection = new ServiceConnection()
    {

        @Override
        public void onServiceConnected(ComponentName name, IBinder mBinder) {

            mMusicServiceBinder = (MusicService.MusicServiceIBinder) mBinder;
            mMusicServiceBinder.registerOnStateChangeListener(mStateChangeListenr);

            mActivityMusicList.clear();
            mActivityMusicList.addAll(mMusicServiceBinder.getList());

            adapter = new MusicItemAdapter(MainActivity.this, R.layout.music_item, mActivityMusicList);
            mMusicListView.setAdapter(adapter);

            MusicItem currentItem = mMusicServiceBinder.getCurrentMusic();
            if(currentItem != null){
                int indexOfCurrent = mActivityMusicList.indexOf(currentItem);
                adapter.setSelectItem(indexOfCurrent);
                mMusicListView.setSelectionFromTop(indexOfCurrent, goodPosition);
            }

            MusicItem item = mMusicServiceBinder.getCurrentMusic();
            if(item == null) {
                enableControlPanel(false);
                return;
            }
            else {
                updatePlayingInfo(item);
            }
            if(mMusicServiceBinder.isPlaying()) {
                mPlayBtn.setBackgroundResource(R.drawable.ic_pause);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public void onClick(View view) {
        if(mMusicServiceBinder == null){
            return;
        }
        switch (view.getId()) {
            case R.id.play_btn: {
                if(mMusicServiceBinder != null) {
                    if (!mMusicServiceBinder.isPlaying()) {
                        mMusicServiceBinder.play();
                    } else {
                        mMusicServiceBinder.pause();
                    }
                }
            }
            break;
            case R.id.next_btn: {
                if(mMusicServiceBinder != null) {
                    mMusicServiceBinder.next();
                    mMusicListView.setSelectionFromTop(mActivityMusicList.indexOf(mMusicServiceBinder.getCurrentMusic()), goodPosition);
                }
            }
            break;

            case R.id.music_title: {
                if(mMusicServiceBinder != null) {
                    mMusicListView.setSelectionFromTop(mActivityMusicList.indexOf(mMusicServiceBinder.getCurrentMusic()), goodPosition);
                }
            }
        }
    }

}
