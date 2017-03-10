package self.wzy.random;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class MusicItemAdapter extends BaseAdapter {

    private List<MusicItem> mData;
    private final LayoutInflater mInflater;
    private final int mResource;
    private Context mContext;
    private int selectItem = -1;

    public MusicItemAdapter(Context context, int resId, List<MusicItem> data)
    {
        mContext = context;
        mData = data;
        mInflater = LayoutInflater.from(context);
        mResource = resId;
    }

    @Override
    public int getCount() {
        return mData != null ? mData.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mData != null ? mData.get(position): null ;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        MyHolder myHolder;

        if (convertView == null) {
            view = mInflater.inflate(mResource, parent, false);
            myHolder = new MyHolder();
            myHolder.hTitle = (TextView) view.findViewById(R.id.music_title);
            myHolder.hSinger = (TextView) view.findViewById(R.id.music_singer);
            myHolder.hTime = (TextView) view.findViewById(R.id.music_duration);
            myHolder.hThumb = (ImageView) view.findViewById(R.id.music_thumb);
            view.setTag(myHolder);
        }else {
            view = convertView;
            myHolder = (MyHolder) view.getTag();
        }

        MusicItem item = mData.get(position);

        myHolder.hTitle.setText(item.name);
        myHolder.hSinger.setText(item.singer);

        String times = Utils.convertMSecendToTime(item.duration);
        times = String.format(mContext.getString(R.string.duration), times);
        myHolder.hTime.setText(times);

        myHolder.hThumb.setTag(position);
        if(!Utils.isInitList) {
            Bitmap bmp = Utils.createThumbFromUir(mContext.getContentResolver(), item.thumbUri);
            if (bmp != null) {
                myHolder.hThumb.setImageBitmap(bmp);
            }
        }else {
            myHolder.hThumb.setImageResource(R.drawable.default_cover);
        }

        if (position == selectItem) {
            view.setBackgroundResource(R.color.colorAccent);
        }else {
            view.setBackground(null);
        }

        return view;
    }

    public  void setSelectItem(int selectItem) {
        this.selectItem = selectItem;
    }

    static class MyHolder{
        TextView hTitle;
        TextView hSinger;
        TextView hTime;
        ImageView hThumb;
    }
}
