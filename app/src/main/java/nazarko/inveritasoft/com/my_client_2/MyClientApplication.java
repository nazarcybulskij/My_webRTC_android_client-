package nazarko.inveritasoft.com.my_client_2;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import io.socket.client.IO;
import io.socket.client.Socket;


/**
 * Created by nazarko on 24.11.17.
 */

public class MyClientApplication extends Application {

    private Socket mSocket;
    private String UUID = "";
    SharedPreferences mSharedPreferences ;


    public Socket getSocket() {
        if (mSocket==null){
            try {
                TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                UUID = getUniqueID();
                mSocket = IO.socket(Constants.SERVER_URL+"/?"+"userid="+UUID);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return mSocket;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    public String getUniqueID() throws UnsupportedEncodingException {
        String email = mSharedPreferences.getString(Constants.EMAIL,null);
        return  URLEncoder.encode(email, "UTF-8");
    }

}
