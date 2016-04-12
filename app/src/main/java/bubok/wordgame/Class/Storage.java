/**
 * Created by bubok on 4/12/2016.
 */
package bubok.wordgame.Class;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class Storage {
    private static final String TAG = "Storage";
    private Context context;
    private Map<String, String> map;

    public Storage(Context context){
        map = new HashMap<>();
        this.context = context;
    }

    public void put(String key, Bitmap bitmap){
        try {
            String f = addBitmap(bitmap, key);
            map.put(key, f);
        }catch (Exception ex){
            Log.i(TAG, ex.getMessage());
        }
    }

    public String get(String key){
        return map.get(key);
    }

    private String addBitmap(Bitmap bitmap, String key) throws Exception{
        String tempKey = key.replace("https://graph.facebook.com/", " ");
        String filePath = tempKey.split("/")[0];
        File f = new File(context.getFilesDir(), filePath);
        f.createNewFile();
        FileOutputStream fos = new FileOutputStream(f);
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, fos);
        fos.flush();
        fos.close();
        return f.getAbsolutePath();
    }
}
