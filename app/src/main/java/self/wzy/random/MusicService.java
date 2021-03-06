package self.wzy.random;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicService extends Service {

    private List<MusicItem> mServiceMusicList;
    private MusicUpdateTask mMusicUpdateTask;
    private List<OnStateChangeListenr> mListenerList = new ArrayList<OnStateChangeListenr>();
    private MusicItem mCurrentMusicItem;
    private MediaPlayer mMusicPlayer;
    private boolean mPaused;

    public interface OnStateChangeListenr {

        void onPlayProgressChange(MusicItem item);
        void onPlay(MusicItem item);
        void onPause(MusicItem item);
        void onUpdateInfos(MusicItem item);
    }

    private BroadcastReceiver headsetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getIntExtra("state", 0) == 0){
                if(mMusicPlayer.isPlaying())
                    pauseInner();
            }
        }
    };

    private final int MSG_PROGRESS_UPDATE = 0;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PROGRESS_UPDATE: {
                    mCurrentMusicItem.playedTime = mMusicPlayer.getCurrentPosition();
                    mCurrentMusicItem.duration = mMusicPlayer.getDuration();
                    for(OnStateChangeListenr l : mListenerList) {
                        l.onPlayProgressChange(mCurrentMusicItem);
                    }
                    sendEmptyMessageDelayed(MSG_PROGRESS_UPDATE, 1000);
                }
                break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headsetReceiver, filter);

        mServiceMusicList = new ArrayList<MusicItem>();
        mMusicUpdateTask = new MusicUpdateTask();
        mMusicUpdateTask.execute();

        mMusicPlayer = new MediaPlayer();
        mPaused = false;
        mMusicPlayer.setOnCompletionListener(mOnCompletionListener);


        if(mCurrentMusicItem != null) {
            prepareToPlay(mCurrentMusicItem);
        }
    }

    private void prepareToPlay(MusicItem item) {
        try {
            mMusicPlayer.reset();
            mMusicPlayer.setDataSource(MusicService.this, item.songUri);
            mMusicPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(headsetReceiver);

        if(mMusicPlayer.isPlaying()) {
            mMusicPlayer.stop();
        }
        mMusicPlayer.release();

        mHandler.removeMessages(MSG_PROGRESS_UPDATE);
        mListenerList.clear();
    }

    public class MusicServiceIBinder extends Binder {
        public void stop() {
            stopInner();
        }

        public void PlayItem(MusicItem item) {
            PlayItemInner(item);
        }

        public void next() {
            nextInner();
        }

        public void play() {
            playInner();
        }

        public void pause() {
            pauseInner();
        }

        public void seekTo(int pos) {
            seekToInner(pos);
        }

        public void registerOnStateChangeListener(OnStateChangeListenr l) {
            registerOnStateChangeListenerInner(l);
        }

        public void unregisterOnStateChangeListener(OnStateChangeListenr l) {
            unregisterOnStateChangeListenerInner(l);
        }

        public MusicItem getCurrentMusic() {
            return getCurrentMusicInner();
        }

        public boolean isPlaying() {
            return isPlayingInner();
        }

        public List getList(){
            return mServiceMusicList;
        }

    }

    private final IBinder mBinder = new MusicServiceIBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void PlayItemInner(MusicItem item) {
        mCurrentMusicItem = item;
        playInner();
    }

    private void stopInner() {
        stopSelf();
    }

    private void nextInner() {
        Random random = new Random();
        int num = random.nextInt(mServiceMusicList.size());
        PlayItemInner(mServiceMusicList.get(num));
    }

    private void playInner() {
        if(mPaused) {
            playMusicItem(mCurrentMusicItem, false);
        }
        else {
            playMusicItem(mCurrentMusicItem, true);
        }
    }

    private void pauseInner() {

        mPaused = true;
        mMusicPlayer.pause();
        for(OnStateChangeListenr l : mListenerList) {
            l.onPause(mCurrentMusicItem);
        }
        mHandler.removeMessages(MSG_PROGRESS_UPDATE);
    }

    private void seekToInner(int pos) {
        mMusicPlayer.seekTo(pos);
    }

    private void registerOnStateChangeListenerInner(OnStateChangeListenr l) {
        mListenerList.add(l);
    }

    private void unregisterOnStateChangeListenerInner(OnStateChangeListenr l) {
        mListenerList.remove(l);
    }

    private MusicItem getCurrentMusicInner() {
        return mCurrentMusicItem;
    }

    private boolean isPlayingInner() {
        return mMusicPlayer.isPlaying();
    }

    private void playMusicItem(MusicItem item, boolean reload) {
            if(item == null) {
                return;
            }

            if(reload) {
                prepareToPlay(item);
                item.playedTime=0;
            }

            mMusicPlayer.start();
            seekToInner((int)item.playedTime);
            for(OnStateChangeListenr l : mListenerList) {
                l.onPlay(item);
            }
            mPaused = false;

            mHandler.removeMessages(MSG_PROGRESS_UPDATE);
            mHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
    }

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {

            mCurrentMusicItem.playedTime = 0;
            nextInner();

        }
    };

    private class MusicUpdateTask extends AsyncTask<Object, MusicItem, Void> {

        @Override
        protected Void doInBackground(Object... params) {

            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String[] searchKey = new String[] {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Albums.ALBUM_ID,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DURATION
            };

            String [] keywords = null;
            String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;

            ContentResolver resolver = getContentResolver();
            Cursor cursor = resolver.query(uri, searchKey, null, keywords, sortOrder);

            if(cursor != null)
            {
                while(cursor.moveToNext() && ! isCancelled())
                {
                    String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                    Uri musicUri = Uri.withAppendedPath(uri, id);

                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

                    int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ID));
                    Uri thumbUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
                    MusicItem data = new MusicItem(musicUri, thumbUri, name, singer, duration, 0);
                    publishProgress(data);
                }
                cursor.close();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(MusicItem... values) {
            MusicItem data = values[0];
            mServiceMusicList.add(data);
            for(OnStateChangeListenr l : mListenerList) {
                l.onUpdateInfos(data);
            }
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

}
