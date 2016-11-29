package hu.taracque.ionic.plugin;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;

import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;

import android.view.Window;
import android.view.WindowManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.UUID;

public class CallKit extends CordovaPlugin {
    public static final String TAG = "CallKit";
    public static PowerManager powerManager;
    public static PowerManager.WakeLock wakeLock;
    private static MediaPlayer ringtone;
    private static Vibrator vibrator;
    
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        
        powerManager = (PowerManager) cordova.getActivity().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock((PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE), "TAG");
        
        Log.v(TAG, "Init CallKit");
    }
    
    @Override
    public synchronized boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        
        if (action == null) {
            return false;
        }
        
        if (action.equals("register")) {
            try {
                this.register(args, callbackContext);
            }
            catch (Exception exception) {
                callbackContext.error("CallKit uncaught exception: " + exception.getMessage());
            }
            
            return true;
        }
        else if (action.equals("reportIncomingCall")) {
            try {
                this.reportIncomingCall(args, callbackContext);
            }
            catch (Exception exception) {
                callbackContext.error("CallKit uncaught exception: " + exception.getMessage());
            }
            
            return true;
        }
        else if (action.equals("endCall")) {
            try {
                this.endCall(args, callbackContext);
            }
            catch (Exception exception) {
                callbackContext.error("CallKit uncaught exception: " + exception.getMessage());
            }
            
            return true;
        }
        else if (action.equals("finishRing")) {
            try {
                this.finishRing(args, callbackContext);
            }
            catch (Exception exception) {
                callbackContext.error("CallKit uncaught exception: " + exception.getMessage());
            }
            
            return true;
        }

        return false;
        
    }
    
    private synchronized void register(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        /* Placeholder */
        callbackContext.success();
    }
    
    private synchronized void reportIncomingCall(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        String name = args.getString(0);
        boolean hasVideo = args.getBoolean(1);
        
        final String uuid = UUID.randomUUID().toString();
        
        Window window = cordova.getActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        
        if(wakeLock.isHeld()) {
            wakeLock.release();
        }
        wakeLock.acquire();
        try {
            boolean vibrate = false;
            
            AudioManager audioManager = (AudioManager) cordova.getActivity().getApplication().getSystemService(Context.AUDIO_SERVICE);
            
            ringtone = new MediaPlayer();
            Context ctx = cordova.getActivity().getApplicationContext();
            AssetManager am = ctx.getResources().getAssets();
            
            AssetFileDescriptor afd = am.openFd("www/media/Ringtone.mp3");
            
            ringtone.setDataSource( afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            ringtone.setLooping(true);
            ringtone.setAudioStreamType(AudioManager.STREAM_RING);
            ringtone.setVolume( (float) (audioManager.getStreamVolume(AudioManager.STREAM_RING) / 7.0), (float) (audioManager.getStreamVolume(AudioManager.STREAM_RING) / 7.0));
            ringtone.prepare();
            ringtone.start();
            
            if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE){
                vibrate = true;
            } else if (1 == Settings.System.getInt(ctx.getContentResolver(), "vibrate_when_ringing", 0)) //vibrate on
                vibrate = true;
            vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrate) {
                vibrator.vibrate(new long[] {0, 1000, 1000}, 0);
            }
            
        } catch (Exception e) {
            Log.v(TAG, "CallKit asset management error: " + e.getMessage());
        }
        
        callbackContext.success(uuid);
    }

    private synchronized void finishRing(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if(ringtone.isPlaying()) {
            ringtone.stop();
        }
        vibrator.cancel();
        
        callbackContext.success();
    }

    private synchronized void endCall(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        String uuid = args.getString(0);
        
        Window window = cordova.getActivity().getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if(wakeLock.isHeld()) {
            wakeLock.release();
        }
        
        finishRing(args,callbackContext);
        
        callbackContext.success();
    }
    
}
