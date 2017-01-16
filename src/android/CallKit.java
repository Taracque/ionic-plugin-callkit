package hu.taracque.ionic.plugin;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.os.Build;

import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import android.view.WindowManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.UUID;

public class CallKit extends CordovaPlugin {
    public static final String TAG = "CallKit";

    public static PowerManager powerManager;
    public static PowerManager.WakeLock wakeLock;
    private static Ringtone ringtone;
    private static Vibrator vibrator;
    private static String callName;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        powerManager = (PowerManager) cordova.getActivity().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock((PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE), TAG);

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
        /* initialize the ringtone */
        Context ctx = cordova.getActivity().getBaseContext();
        Uri ringtoneUri;

        int ringtoneID = ctx.getResources().getIdentifier("ringtone","raw", ctx.getPackageName());
        if (ringtoneID != 0 ) {
            ringtoneUri = Uri.parse("android.resource://" + ctx.getPackageName() + "/" + ringtoneID);
        } else {
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        }

        ringtone = RingtoneManager.getRingtone(ctx, ringtoneUri);
        if (Build.VERSION.SDK_INT >= 21) {
            AudioAttributes aa = new AudioAttributes.Builder()
                    .setFlags(AudioAttributes.USAGE_NOTIFICATION_RINGTONE | AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_REQUEST)
                    .build();
            ringtone.setAudioAttributes(aa);
        } else {
            ringtone.setStreamType(RingtoneManager.TYPE_RINGTONE);
        }
        ringtone.stop();

        callbackContext.success();
    }

    private synchronized void reportIncomingCall(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        callName = args.getString(0);
        boolean hasVideo = args.getBoolean(1);

        final String uuid = UUID.randomUUID().toString();

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String packageName = cordova.getActivity().getApplicationContext().getPackageName();

                    Intent intent = new Intent("android.intent.action.MAIN");
                    intent.setComponent(new ComponentName(packageName, packageName + ".MainActivity"));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    cordova.getActivity().getApplicationContext().startActivity(intent);
                } catch (Exception e)  {
                    Log.v(TAG, "CallKit error: " + e.getMessage());
                }
                cordova.getActivity().getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                );
            }
        });

        if(wakeLock.isHeld()) {
            wakeLock.release();
        }
        wakeLock.acquire();
        try {
            boolean vibrate = false;
            Uri ringtoneUri;

            AudioManager audioManager = (AudioManager) cordova.getActivity().getApplication().getSystemService(Context.AUDIO_SERVICE);

            Context ctx = cordova.getActivity().getBaseContext();

            ringtone.play();

            if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE){
                vibrate = true;
            } else if (1 == Settings.System.getInt(ctx.getContentResolver(), "vibrate_when_ringing", 0)) //vibrate on
                vibrate = true;

            vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrate) {
                vibrator.vibrate(new long[] {0, 1000, 1000}, 0);
            }

        } catch (Exception e) {
            Log.v(TAG, "CallKit error: " + e.getMessage());
        }

        callbackContext.success(uuid);
    }

    private void notifyUser(String uuid) {
        String appName;
        ApplicationInfo app = null;

        Context context = cordova.getActivity().getApplicationContext();
        PackageManager packageManager = cordova.getActivity().getPackageManager();

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        try {
            app = packageManager.getApplicationInfo(cordova.getActivity().getPackageName(), 0);
            appName = (String)packageManager.getApplicationLabel(app);
        } catch (PackageManager.NameNotFoundException e) {
            appName = "Incoming";
            e.printStackTrace();
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setContentTitle( appName + " call missed" )
                .setContentText( callName )
                .setSound( defaultSoundUri );

        int resID = context.getResources().getIdentifier("callkit_missed_call", "drawable", cordova.getActivity().getPackageName());
        if (resID != 0) {
            notificationBuilder.setSmallIcon(resID);
        } else {
            notificationBuilder.setSmallIcon(app.icon);
        }
        notificationBuilder.setLargeIcon( BitmapFactory.decodeResource( context.getResources(), app.icon ) );

        PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, CallKitReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(contentIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(uuid.hashCode(), notificationBuilder.build());
    }

    private synchronized void finishRing(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        String uuid = args.getString(0);

        if(ringtone.isPlaying()) {
            ringtone.stop();
        }
        vibrator.cancel();

        callbackContext.success();
    }

    private synchronized void endCall(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        String uuid = args.getString(0);
        boolean notify = args.getBoolean(1);

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cordova.getActivity().getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });

        if(wakeLock.isHeld()) {
            wakeLock.release();
        }

        finishRing(args,callbackContext);

        if (notify) {
            this.notifyUser(uuid);
        }

        callbackContext.success();
    }

}
