# ionic-plugin-callkit

Ionic/Cordova plugin for [CallKit](https://developer.apple.com/reference/callkit).

On Android and iOS versions prior to 10, it mimics CallKit calls, but the callscreen itself should be displayed by the Ionic/Cordova app.
On Android the main activity must be called as "MainActivity". You can check this in your AndroidManifest.xml file, the first activity tag under application tag should have android:name="MainActivity" attribute.

On iOS 10+, use resources/Ringtone.caf as ringtone (optional, uses system default ringtone if not found) automatically looped
On iOS versions prior to 10, a local notification is displayed if the app is in background.
On Android res/raw/ringtone.mp3 or res/raw/ringtone.ogg is used (filename is lowercase, if not found then plays the default system ring), use ANDROID_LOOP metadata to loop the ogg ringtone.

## Install

```
cordova plugin add https://github.com/taracque/ionic-plugin-callkit.git
```

## How to use

Example (only one call tracked at a time, this code is just a hint, see [Call Flow](#call-flows) description below):

```javascript
var callKitService = new CallKitService();
function CallKitService() {
  var callKit;
  var callUUID;

  /**
   * Determine whether the plugin is available.
   *
   * @return {boolean} `true` if the plugin is available.
   */
  function hasCallKit() {
    return typeof CallKit !== "undefined" && callKit;
  }

  /**
   * Wrapper for functions which cannot be executed without the plugin.
   *
   * @param {Function} fn Function to be called only if plugin is available.
   *
   * @return {Function} A function running `fn` (with its arguments), if plugin is available.
   */
  function execWithPlugin(fn) {
    return function() {
      if (!hasCallKit()) {
        console.error('callkit plugin not available');
        return;
      }

      fn.apply(this, Array.prototype.slice.call(arguments));
    };
  }

  return {
    register: function(callChanged, audioSystem) {
      if (typeof CallKit !== "undefined") {
        callKit = new CallKit();
        callKit.register(callChanged, audioSystem);
      }
    },
    reportIncomingCall: execWithPlugin(function(name, params) {
      callKit.reportIncomingCall(name, params, function(uuid) {
        callUUID = uuid;
      });
    }),
    askNotificationPermission: execWithPlugin(function() {
      // only useful on iOS 9, as we use local notifications to report incoming calls
      callKit.askNotificationPermission();
    }),
    startCall: execWithPlugin(function(name, isVideo) {
      callKit.startCall(name, isVideo, function(uuid) {
        callUUID = uuid;
      });
    }),
    callConnected: execWithPlugin(function(uuid) {
      callKit.callConnected(callUUID);
    }),
    endCall: execWithPlugin(function(notify, contentTitle) {
        callKit.endCall(callUUID, notify, contentTitle);
    }),
    finishRing: execWithPlugin(function() {
        callKit.finishRing();
    })
  };
}
```

## API

Plugin initialization:

```javascript
callKitService.register(callChanged, audioSystem)
```

For iOS versions prior to 10, as CallKit is not available, this plugin displays
a local notification (when app is in background) to report an incoming call.
Permission to display notifications should be asked this way:

```javascript
callKitService.askNotificationPermission();
```

Initializes the plugin a register callback codes.

```javascript
callChanged = function(obj) {
  < your code >
}
```

callback is called with an object, which contains the following properties:
* *uuid* - The UUID of the call
* *handle* - The handle of the call (the string what is displayed on the phone) String
* *isOutgoing* - is the call outgoing? Boolean
* *isOnHold* - is the is on hold? Boolean
* *hasConnected* - is it connected? Boolean
* *hasEnded* - is it ended? Boolean
* *hasStartedConnecting* - is it started connecting (i.e. user pressed the accept button)? Boolean
* *endDate* - when the call is ended? String (ISO8601 format date time)
* *connectDate* - when the call is connected? String (ISO8601 format date time)
* *connectingDate* - when the call started to connect? String (ISO8601 format date time)
* *duration* - call duration Double

```javascript
audioSystem = function(message) {
  < your code >
}
```
* *message: String* - can be `startAudio`, `stopAudio`, `configureAudio`

Use

```javascript
callKitService.reportIncomingCall(name, params);
```

to activate the call screen.
* *name: String* - the caller name, which should displayed on the callscreen
* *params: Object* - with the following keys
  * `video` : set to true if this call can be a video call
  * `group` : set to true if call supports grouping (default: false)
  * `ungroup` : set to true if call supports ungrouping (default: false)
  * `dtmf` : set to true if call supports dtmf tones (default: false)
  * `hold` : set to true if call supports hold (default: false)

```javascript
callKitService.startCall(name, isVideo);
```

to report an initiated outgoing call to the system
* *name: String* - the callee name, which should displayed in the call history.
* *isVideo: boolean* - set to true if this call can be a video call.

Use

```javascript
callKitService.callConnected(uuid);
```

to report the system that the outgoing call is connected.
* *uuid: String* - Unique identifier of the call.

Use

```javascript
callKitService.endCall(notify, contentTitle);
```

to let the system know, the call is ended.

* *notify: boolean* - If `true`, sends a local notification to the system about the missed call.
* *contentTitle: String* - Title of the notification (will default to `{appName} call missed`).

On android the callscreen should be displayed by the app. Use

```javascript
callKitService.finishRing(uuid, notify);
```

to stop the ringtone playing.

* *uuid: String* - Unique identifier of the call. In case of incoming call, it is provided by the `reportIncomingCall` `onSuccess` callback.

## Call Flows:

Incoming:

1. call `reportIncomingCall`
2. `callChanged` gets called with `hasStartedConnecting=true`
3. use `finishRing` to finish the ring (only needed on android)
4. connect the call, and once connected user `callConnected` call
5. once the call is finished user `endCall`

Outgoing:

1. call `startCall`
2. initiate call
3. once the call is connected use `callConnected`
4. once the call is finished use `endCall`


## iOS Quirks

CallKit adds a "quick launch" button on the call screen, for your application. The icon file (white mask) `callkit-icon.png` is read from the XCode `Resources` project.
> The icon image should be a square with side length of 40 points. The alpha channel of the image is used to create a white image mask, which is used in the system native in-call UI for the button which takes the user from this system UI to the 3rd-party app.

You can use the tag `<resource-file>` in config.xml (*since cordova-ios 4.4.0*) to copy the file in the app bundle:

```xml
<platform name="ios">
    <resource-file src="resources/ios/icon/callkit-icon.png" target="callkit-icon.png" />
    [...]
</platform>
```
