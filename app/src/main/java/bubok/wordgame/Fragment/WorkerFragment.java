package bubok.wordgame.Fragment;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by bubok on 16.03.2016.
 */
public class WorkerFragment  extends Fragment{

    private final SocketModel socketModel;

    public WorkerFragment(){
        socketModel = new SocketModel();
    }
    public WorkerFragment(String url){
        socketModel = new SocketModel(url);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
    @Override
    public void finalize(){

    }

    public SocketModel getSocketModel(){
        return socketModel;
    }
}
