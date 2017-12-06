package nazarko.inveritasoft.com.my_client_2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.SessionDescription;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static nazarko.inveritasoft.com.my_client_2.Constants.ANSWER_CALL_PHONE;
import static nazarko.inveritasoft.com.my_client_2.Constants.HANG_UP_CALL;


/**
 * Created by nazarko on 28.11.17.
 */

public class InComingCallActivity extends AppCompatActivity implements CallFragment.OnFragmentCallListener {

    public Socket mSocket;

    private int CONNECTION_REQUEST =  110;

    View greenView;
    View redView;

    String mUserId;
    String mRoomId;

    boolean onback = false;

    FrameLayout frameCallLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out_going_call);
        initData();
        initViews();;
    }

    @Override
    protected void onDestroy() {
        mSocket.off(ANSWER_CALL_PHONE, onAnswerCall);
        mSocket.off(HANG_UP_CALL, onHangUpCall);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (onback==false)
            mSocket.emit(Constants.HANG_UP,mUserId,mRoomId);
        super.onBackPressed();
    }


    private void initData() {
        mSocket = ((MyClientApplication)getApplication()).getSocket();
        mSocket.on(ANSWER_CALL_PHONE, onAnswerCall);
        mSocket.on(HANG_UP_CALL, onHangUpCall);
        Intent intent = getIntent();
        if (intent !=null){
            Bundle bundle = intent.getExtras();
            if (bundle!=null){
                mUserId = bundle.getString(MainActivity.BUNDLE_USER_ID);
                mRoomId = bundle.getString(MainActivity.BUNDLE_ROOM_ID);
                Log.d(MainActivity.TAG+"room",mRoomId);
            }
        }
    }

    private void initViews() {
        greenView = findViewById(R.id.green);
        redView = findViewById(R.id.red);
        frameCallLayout = (FrameLayout) findViewById(R.id.frameLayout);
        frameCallLayout = findViewById(R.id.frameLayout);
        redView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSocket.emit(Constants.HANG_UP,mUserId,mRoomId);
            }
        });
        greenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSocket.emit(Constants.ANSWER_CALL,mUserId);
            }
        });
    }


    private Emitter.Listener onAnswerCall = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    greenView.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(),mRoomId+"",Toast.LENGTH_SHORT).show();

                    joinOrCreateRoom(mRoomId);

                }
            });
        }
    };


    private Emitter.Listener onHangUpCall = new Emitter.Listener() {
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onback = true;
                    onBackPressed();
                }
            });
        }
    };


    public void  joinOrCreateRoom(String userId){
        String roomUrl =  Constants.SERVER_URL;

        // Video call enabled flag.
        boolean videoCallEnabled =  Boolean.valueOf(getString(R.string.pref_videocall_default));

        // Use screencapture option.
        boolean useScreencapture = Boolean.valueOf(getString(R.string.pref_screencapture_default));

        // Use Camera2 option.
        boolean useCamera2 = Boolean.valueOf(getString(R.string.pref_camera2_default));

        // Get default codecs.
        String videoCodec = getString(R.string.pref_videocodec_default);

        String audioCodec = getString(R.string.pref_audiocodec_default);

        // Check HW codec flag.
        boolean hwCodec =  Boolean.valueOf(getString(R.string.pref_hwcodec_default));

        // Check Capture to texture.
        boolean captureToTexture =  Boolean.valueOf(getString(R.string.pref_capturetotexture_default));

        // Check FlexFEC.
        boolean flexfecEnabled = Boolean.valueOf(getString(R.string.pref_flexfec_default));

        // Check Disable Audio Processing flag.
        boolean noAudioProcessing = Boolean.valueOf(getString(R.string.pref_noaudioprocessing_default));

        // Check Disable Audio Processing flag.
        boolean aecDump = Boolean.valueOf(getString(R.string.pref_aecdump_default));

        // Check OpenSL ES enabled flag.
        boolean useOpenSLES = Boolean.valueOf(getString(R.string.pref_opensles_default));

        // Check Disable built-in AEC flag.
        boolean disableBuiltInAEC = Boolean.valueOf(getString(R.string.pref_disable_built_in_aec_default));

        // Check Disable built-in AGC flag.
        boolean disableBuiltInAGC = Boolean.valueOf(getString(R.string.pref_disable_built_in_agc_default));


        // Check Disable built-in NS flag.
        boolean disableBuiltInNS = Boolean.valueOf(getString(R.string.pref_disable_built_in_ns_default));

        // Check Enable level control.
        boolean enableLevelControl =  Boolean.valueOf(getString(R.string.pref_enable_level_control_default));

        // Check Disable gain control
        boolean disableWebRtcAGCAndHPF = Boolean.valueOf(getString(R.string.pref_disable_webrtc_agc_default));

        // Get video resolution from settings.
        int videoWidth = 0;
        int videoHeight = 0;
        if (videoWidth == 0 && videoHeight == 0) {
            String resolution = getString(R.string.pref_resolution_default);
            String[] dimensions = resolution.split("[ x]+");
            if (dimensions.length == 2) {
                try {
                    videoWidth = Integer.parseInt(dimensions[0]);
                    videoHeight = Integer.parseInt(dimensions[1]);
                } catch (NumberFormatException e) {
                    videoWidth = 0;
                    videoHeight = 0;
                }
            }
        }

        // Get camera fps from settings.
        int cameraFps = 0;
        if (cameraFps == 0) {
            String fps =  getString(R.string.pref_fps_default);
            String[] fpsValues = fps.split("[ x]+");
            if (fpsValues.length == 2) {
                try {
                    cameraFps = Integer.parseInt(fpsValues[0]);
                } catch (NumberFormatException e) {
                    cameraFps = 0;
                }
            }
        }

        // Check capture quality slider flag.
        boolean captureQualitySlider = Boolean.valueOf(getString(R.string.pref_capturequalityslider_default));

        // Get video and audio start bitrate.
        int videoStartBitrate = 0;
        if (videoStartBitrate == 0) {
            String bitrateTypeDefault = getString(R.string.pref_maxvideobitrate_default);
            String bitrateType =  "";
            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue =  getString(R.string.pref_maxvideobitratevalue_default);
                videoStartBitrate = Integer.parseInt(bitrateValue);
            }
        }

        int audioStartBitrate = 0;
        if (audioStartBitrate == 0) {
            String bitrateTypeDefault = getString(R.string.pref_startaudiobitrate_default);
            String bitrateType = "";
            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue =  getString(R.string.pref_startaudiobitratevalue_default);
                audioStartBitrate = Integer.parseInt(bitrateValue);
            }
        }

        // Check statistics display option.
        boolean displayHud = Boolean.valueOf(getString(R.string.pref_displayhud_default));

        boolean tracing = Boolean.valueOf(getString(R.string.pref_tracing_default));

        // Get datachannel options
        boolean dataChannelEnabled = Boolean.valueOf(getString(R.string.pref_enable_datachannel_default));

        boolean ordered = Boolean.valueOf(getString(R.string.pref_ordered_default));

        boolean negotiated = Boolean.valueOf(getString(R.string.pref_negotiated_default));

        int maxRetrMs =  Integer.valueOf(getString(R.string.pref_max_retransmit_time_ms_default));


        int maxRetr =  Integer.valueOf(getString(R.string.pref_max_retransmits_default));

        int id = Integer.valueOf(getString(R.string.pref_data_id_default));

        String protocol = getString(R.string.pref_data_protocol_default);

        Bundle intent = new Bundle();
        intent.putString(CallFragment.EXTRA_ROOM_URL,roomUrl);
        intent.putString(CallFragment.EXTRA_ROOMID, userId);
        intent.putBoolean(CallFragment.EXTRA_VIDEO_CALL, videoCallEnabled);
        intent.putInt(CallFragment.EXTRA_VIDEO_WIDTH, videoWidth);
        intent.putInt(CallFragment.EXTRA_VIDEO_HEIGHT, videoHeight);
        intent.putInt(CallFragment.EXTRA_VIDEO_FPS, cameraFps);
        intent.putInt(CallFragment.EXTRA_VIDEO_BITRATE, videoStartBitrate);
        intent.putString(CallFragment.EXTRA_VIDEOCODEC, videoCodec);
        intent.putBoolean(CallFragment.EXTRA_HWCODEC_ENABLED, hwCodec);
        intent.putBoolean(CallFragment.EXTRA_FLEXFEC_ENABLED, flexfecEnabled);
        intent.putBoolean(CallFragment.EXTRA_NOAUDIOPROCESSING_ENABLED, noAudioProcessing);
        intent.putBoolean(CallFragment.EXTRA_AECDUMP_ENABLED, aecDump);
        intent.putBoolean(CallFragment.EXTRA_OPENSLES_ENABLED, useOpenSLES);
        intent.putBoolean(CallFragment.EXTRA_DISABLE_BUILT_IN_AEC, disableBuiltInAEC);
        intent.putBoolean(CallFragment.EXTRA_DISABLE_BUILT_IN_AGC, disableBuiltInAGC);
        intent.putBoolean(CallFragment.EXTRA_DISABLE_BUILT_IN_NS, disableBuiltInNS);
        intent.putBoolean(CallFragment.EXTRA_ENABLE_LEVEL_CONTROL, enableLevelControl);
        intent.putBoolean(CallFragment.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, disableWebRtcAGCAndHPF);
        intent.putInt(CallFragment.EXTRA_AUDIO_BITRATE, audioStartBitrate);
        intent.putString(CallFragment.EXTRA_AUDIOCODEC, audioCodec);
        intent.putBoolean(CallFragment.EXTRA_DISPLAY_HUD, displayHud);
        intent.putBoolean(CallFragment.EXTRA_TRACING, tracing);
        intent.putInt(CallFragment.EXTRA_RUNTIME,100);

        intent.putBoolean(CallFragment.EXTRA_DATA_CHANNEL_ENABLED, dataChannelEnabled);

        if (dataChannelEnabled) {
            intent.putBoolean(CallFragment.EXTRA_ORDERED, ordered);
            intent.putInt(CallFragment.EXTRA_MAX_RETRANSMITS_MS, maxRetrMs);
            intent.putInt(CallFragment.EXTRA_MAX_RETRANSMITS, maxRetr);
            intent.putString(CallFragment.EXTRA_PROTOCOL, protocol);
            intent.putBoolean(CallFragment.EXTRA_NEGOTIATED, negotiated);
            intent.putInt(CallFragment.EXTRA_ID, id);
        }



        // Create a new Fragment to be placed in the activity layout
        CallFragment firstFragment = CallFragment.newInstance();
        // In case this activity was started with special instructions from an
        // Intent, pass the Intent's extras to the fragment as arguments
        firstFragment.setArguments(intent);
        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction().add(R.id.frameLayout, firstFragment).commit();
    }



}
