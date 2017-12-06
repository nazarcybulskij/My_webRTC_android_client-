package nazarko.inveritasoft.com.my_client_2;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import android.os.Handler;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static nazarko.inveritasoft.com.my_client_2.Constants.CALL;
import static nazarko.inveritasoft.com.my_client_2.Constants.GET_USERS_IDS;
import static nazarko.inveritasoft.com.my_client_2.Constants.INCOMING_CALL;
import static nazarko.inveritasoft.com.my_client_2.Constants.OUTGOING_CALL;
import static nazarko.inveritasoft.com.my_client_2.Constants.USERS_IDS;

public class MainActivity extends AppCompatActivity implements  OnCallClickListener {

    public  static String TAG = "MainActivity";
    public static String BUNDLE_USER_ID ="bundle:user_id";
    public static String BUNDLE_ROOM_ID ="bundle:room_id";


    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Socket mSocket;
    private Handler mHandler ;
    private Runnable mRunnable= new Runnable() {
        @Override
        public void run() {
            if(mSocket!=null)
                if(mSocket.connected())
                    mSocket.emit(GET_USERS_IDS);
            mHandler.postDelayed(this, 5000);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initViews();
        initiateRefresh();

    }

    @Override
    protected void onStart() {
        super.onStart();
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable,5000);
    }

    @Override
    protected void onStop() {
        mHandler.removeCallbacks(mRunnable);
        mHandler = null;
        super.onStop();
    }

    private void initData() {
        mSocket = ((MyClientApplication)getApplication()).getSocket();
        if (!mSocket.connected()){
            mSocket.connect();
        }
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(OUTGOING_CALL, onOutGoing);
        mSocket.on(INCOMING_CALL, onIncoming);
        mSocket.on(USERS_IDS, onUserIds);
    }

    private void initViews() {
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initiateRefresh();
            }
        });
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setHasFixedSize(true);
    }

    private void initiateRefresh() {
        mSocket.emit(GET_USERS_IDS);
    }


    @Override
    public void onDestroy() {
        if (mSocket.connected()){
            mSocket.disconnect();
        }
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(OUTGOING_CALL, onOutGoing);
        mSocket.off(INCOMING_CALL, onIncoming);
        mSocket.off(USERS_IDS, onUserIds);
        super.onDestroy();
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), R.string.connect, Toast.LENGTH_LONG).show();

                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), R.string.disconnect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onOutGoing = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(MainActivity.this, OutGoingCallActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(MainActivity.BUNDLE_USER_ID,(String)args[0]);
                    bundle.putString(MainActivity.BUNDLE_ROOM_ID,(String)args[1]);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        }
    };


    private Emitter.Listener onIncoming = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(MainActivity.this, InComingCallActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(MainActivity.BUNDLE_USER_ID,(String)args[0]);
                    bundle.putString(MainActivity.BUNDLE_ROOM_ID,(String)args[1]);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });

        }
    };

    private Emitter.Listener onUserIds =  new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String json = String.valueOf(args[0]);
                    Type listType = new TypeToken<List<String>>() {}.getType();
                    List<String> userids = new Gson().fromJson(json, listType);
                    CallAdapter  adapter = new CallAdapter(userids);
                    adapter.addCallClickListener(MainActivity.this);
                    mRecyclerView.setAdapter(adapter);
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    };


    @Override
    public void call(String userId) {
        mSocket.emit(CALL,userId);
    }
}
