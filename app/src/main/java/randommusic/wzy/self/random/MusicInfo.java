package randommusic.wzy.self.random;

/**
 * Created by Ziyue.Wang on 2017/2/24.
 */
public class MusicInfo {
    private Long id = null;
    private String title = null;
    private String artist = null;
    private Long duration = null;
    private Long size = null;
    private String url = null;

    public MusicInfo() {
    }

    public MusicInfo(String title, String artist, Long duration, Long id, Long size, String url) {
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.id = id;
        this.size = size;
        this.url = url;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
