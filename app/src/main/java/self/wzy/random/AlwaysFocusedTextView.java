package self.wzy.random;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Ziyue.Wang on 2017/3/14.
 */
public class AlwaysFocusedTextView extends TextView {

    public AlwaysFocusedTextView(Context context) {
        super(context);
    }

    public AlwaysFocusedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean isFocused() {
        return true;
    }

}
