package nazarko.inveritasoft.com.my_client_2;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.List;
import android.os.Handler;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import nazarko.inveritasoft.com.my_client_2.persistence.AppDataBase;
import nazarko.inveritasoft.com.my_client_2.persistence.User;

import static nazarko.inveritasoft.com.my_client_2.Constants.CALL;
import static nazarko.inveritasoft.com.my_client_2.Constants.GET_USERS_IDS;
import static nazarko.inveritasoft.com.my_client_2.Constants.INCOMING_CALL;
import static nazarko.inveritasoft.com.my_client_2.Constants.OUTGOING_CALL;
import static nazarko.inveritasoft.com.my_client_2.Constants.USERS_IDS;

public class MainActivity extends AppCompatActivity implements  OnCallClickListener, DialogInterface.OnDismissListener {

    public  static String TAG = "MainActivity";
    public static String BUNDLE_USER_ID ="bundle:user_id";
    public static String BUNDLE_ROOM_ID ="bundle:room_id";

    private SharedPreferences mSharedPreferences;
    private RecyclerView mRecyclerView;
    private CallAdapter  mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Socket mSocket;

//    private Handler mHandler ;
//
//    private Runnable mRunnable= new Runnable() {
//        @Override
//        public void run() {
//            if(mSocket!=null)
//                if(mSocket.connected())
//                    mSocket.emit(GET_USERS_IDS);
//           // mHandler.postDelayed(this, 5000);
//        }
//    };

    private AppDataBase dataBase ;


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
        loadUsers();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void initData() {
        mSocket = ((MyClientApplication)getApplication()).getSocket();
        if (!mSocket.connected()){
            mSocket.connect();
        }
        dataBase = Room.databaseBuilder(this,AppDataBase.class,AppDataBase.DB_NAME).build();
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(OUTGOING_CALL, onOutGoing);
        mSocket.on(INCOMING_CALL, onIncoming);
        mSocket.on(USERS_IDS, onUserIds);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String email = mSharedPreferences.getString(Constants.EMAIL,null);
        if (email!=null){
            try {
                String topiks = URLEncoder.encode(email, "UTF-8");
                FirebaseMessaging.getInstance().subscribeToTopic("/topics/"+topiks);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

    }

    private void initViews() {
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initiateRefresh();
            }
        });
        findViewById(R.id.add_user_btn).setOnClickListener(view -> {
            showEnterEmailDialog();
        });
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new CallAdapter();
        mAdapter.addCallClickListener(MainActivity.this);
        mRecyclerView.setAdapter(mAdapter);
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
//                    mAdapter = new CallAdapter(userids);
//                    adapter.addCallClickListener(MainActivity.this);
//                    mRecyclerView.setAdapter(adapter);
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    };


    private void showEnterEmailDialog(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(EnterEmailNewUserFragment.class.getName());
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        EnterEmailNewUserFragment newFragment = EnterEmailNewUserFragment.newInstance();
        newFragment.show(ft, EnterEmailNewUserFragment.class.getName());
    }


    @Override
    public void call(String userId) {
        mSocket.emit(CALL,userId);
    }


    private void loadUsers() {
        new AsyncTask<Void, Void, List<User>>() {
            @Override
            protected List<User> doInBackground(Void... params) {
                return dataBase.getUserDao().getAll();
            }

            @Override
            protected void onPostExecute(List<User> users) {
                mAdapter.setUsers(users);
            }
        }.execute();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        loadUsers();
    }
}
