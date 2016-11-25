package hu.taracque.ionic.plugin;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.PowerManager;

import android.util.Log;

import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.app.KeyguardManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class CallKit extends CordovaPlugin {
    public static final String TAG = "CallKit";
    public static PowerManager powerManager;
    public static PowerManager.WakeLock wakeLock;

	/**
	 * Constructor
	 */
	public CallKit() {
	}

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);

        powerManager = (PowerManager) cordova.getActivity().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock((PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE), "TAG");

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

	}
	
    private synchronized void register(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
		/* Placeholder */
    	callbackContext.success();
	}

    private synchronized void reportIncomingCall(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
    	String name = args.getString(0);
    	boolean hasVideo = args.getBoolean(1);

    	String uuid = UUID.randomUUID().toString();

		cordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Window window = cordova.getActivity().getWindow();
				window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
				window.clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
				window.clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

				if(wakeLock.isHeld()) {
					wakeLock.release();
				}

				callbackContext.success(uuid);
			}
		});
	}

    private synchronized void endCall(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
    	String uuid = args.getString(0);

		cordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Window window = cordova.getActivity().getWindow();
				window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
				window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
				window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

				if(wakeLock.isHeld()) {
					wakeLock.release();
				}
				wakeLock.acquire();
				callbackContext.success();
			}
		});

	}

}