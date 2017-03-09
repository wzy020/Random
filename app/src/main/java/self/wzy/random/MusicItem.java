package self.wzy.random;

import android.net.Uri;

public class MusicItem {

    String name;
    String singer;
    Uri songUri;
    Uri thumbUri;
    long duration;
    long playedTime;

    MusicItem(Uri songUri, Uri thumbUri, String strName,String strSinger, long duration, long playedTime) {
        this.name = strName;
        this.singer = strSinger;
        this.songUri = songUri;
        this.duration = duration;
        this.playedTime = playedTime;
        this.thumbUri = thumbUri;
    }

    @Override
    public boolean equals(Object o) {
        MusicItem another = (MusicItem) o;

        return another.songUri.equals(this.songUri);
    }
}