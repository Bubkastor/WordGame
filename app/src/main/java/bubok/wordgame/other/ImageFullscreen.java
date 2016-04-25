package bubok.wordgame.other;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class ImageFullscreen extends ImageView {
    private static String TAG = "ImageFullscreen";
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i(TAG, "onTouchEvent");
        return super.onTouchEvent(event);
    }

    public ImageFullscreen(Context context) {
        super(context);
    }
}
