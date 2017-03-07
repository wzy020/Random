package self.wzy.random;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    static public String TAG = "MainActivity";
    private List<MusicItem> mActivityMusicList;
    private ListView mMusicListView;
    private Button mPlayBtn;
    private Button mNextBtn;
    private TextView mMusicTitle;
    private TextView mPlayedTime;
    private TextView mDurationTime;
    private SeekBar mMusicSeekBar;
    private MusicService.MusicServiceIBinder mMusicServiceBinder;
    private MusicItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }

        Intent i = new Intent(this, MusicService.class);
        startService(i);
        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMusicServiceBinder.unregisterOnStateChangeListener(mStateChangeListenr);
        unbindService(mServiceConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent i = new Intent(this, MusicService.class);
        bindService(i, mServiceConnection, BIND_AUTO_CREATE);

        adapter = new MusicItemAdapter(getApplicationContext(), R.layout.music_item, mActivityMusicList);
        mMusicListView.setAdapter(adapter);
    }

    private void initViews(){
        mActivityMusicList = new ArrayList<MusicItem>();
        mMusicListView = (ListView) findViewById(R.id.music_list);
        mMusicListView.setOnItemClickListener(mOnMusicItemClickListener);

        mPlayBtn = (Button) findViewById(R.id.play_btn);
        mNextBtn = (Button) findViewById(R.id.next_btn);

        mMusicTitle = (TextView) findViewById(R.id.music_title);
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
            updatePlayingInfo(item);
        }

        @Override
        public void onPlay(MusicItem item) {
            mPlayBtn.setBackgroundResource(R.drawable.ic_pause);
            updatePlayingInfo(item);
            enableControlPanel(true);
        }

        @Override
        public void onPause(MusicItem item) {
            mPlayBtn.setBackgroundResource(R.drawable.ic_play);
            enableControlPanel(true);
        }

        @Override
        public void onPalyComplet(MusicItem item) {
            mNextBtn.callOnClick();
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
        switch (view.getId()) {

            case R.id.play_btn: {
                if(mMusicServiceBinder != null) {
                    if(!mMusicServiceBinder.isPlaying()) {
                        mMusicServiceBinder.play();
                    }
                    else {
                        mMusicServiceBinder.pause();
                    }
                }
            }
            break;

            case R.id.next_btn: {
                if(mMusicServiceBinder != null) {
                    Random random = new Random();
                    int num = random.nextInt(mActivityMusicList.size());
                    mMusicServiceBinder.PlayItem(mActivityMusicList.get(num));
                }
            }
            break;
        }
    }
}
