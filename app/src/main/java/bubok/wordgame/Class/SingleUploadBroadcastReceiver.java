package bubok.wordgame.Class;

import net.gotev.uploadservice.UploadServiceBroadcastReceiver;

/**
 * Created by bubok on 29.03.2016.
 */
public class SingleUploadBroadcastReceiver extends UploadServiceBroadcastReceiver {
    public interface Delegate {
        void onCompleted(int serverResponseCode, byte[] serverResponseBody);
    }

    private String mUploadID;
    private Delegate mDelegate;

    public void setUploadID(String uploadID) {
        mUploadID = uploadID;
    }

    public void setDelegate(Delegate delegate) {
        mDelegate = delegate;
    }

    @Override
    public void onCompleted(String uploadId, int serverResponseCode, byte[] serverResponseBody) {
        if (uploadId.equals(mUploadID) && mDelegate != null) {
            mDelegate.onCompleted(serverResponseCode, serverResponseBody);
        }
    }

}
