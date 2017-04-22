# ionic-plugin-callkit

Ionic/Cordova plugin for CallKit.

On Android it mimics CallKit calls, but the callscreen itself should be displayed by the Ionic/Cordova app. On Android the main activity must be called as "MainActivity". You can check this in your AndroidManifest.xml file, the first activity tag under application tag should have android:name="MainActivity" attribute.

On iOS use resources/Ringtone.caf as ringtone (optional, uses system default ringtone if not found) automatically looped
On Android res/raw/ringtone.mp3 or res/raw/ringtone.ogg is used (filename is lowercase, if not found then plays the default system ring), use ANDROID_LOOP metadata to loop the ogg ringtone

## Install

```
cordova plugin add https://github.com/Taracque/ionic-plugin-callkit.git
```

## How to use

Exmaple (only one call tracked at a time, this code is just a hint, see [Call Flow](#call-flows) description below):

```javascript
var callKitService = new CallKitService();
function CallKitService() {
  var callKit;
  var callUUID;

  return {
    hasCallKit: function() {
      return typeof CallKit !== "undefined" && callKit;
    },
    register: function(callChanged, audioSystem) {
      if (typeof CallKit !== "undefined") {
        callKit = new CallKit();
        callKit.register(allChanged, audioSystem);
      }
    },
    reportIncomingCall: function(name, params) {
      if (this.hasCallKit()) {
        callKit.reportIncomingCall(name, params, function(uuid) {
          callUUID = uuid;
        });
      }
    },
    startCall: function(name, isVideo) {
      if (this.hasCallKit()) {
        callKit.startCall(name, isVideo, function(uuid) {
          callUUID = uuid;
        });
      }
    },
    callConnected: function(uuid) {
      if (this.hasCallKit()) {
        callKit.callConnected(callUUID);
      }
    },
    endCall: function(notify) {
      if (this.hasCallKit()) {
        callKit.endCall(callUUID, notify);
      }
    },
    finishRing: function() {
      if (this.hasCallKit()) {
        callKit.finishRing();
      }
    }
  };
}
```

## API

Plugin initialization:

```javascript
callKitService.register( callChanged, audioSystem )
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
* *hasEnded* - is it eneded? Boolean
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
callKitService.reportIncomingCall(name, params, onSuccess);
```

to activate the call screen.
* *name: String* - the caller name, which should displayed on the callscreen
* *params: Object* - with the following keys
  * `video` : set to true if this call can be a video call
  * `group` : set to true if call supports grouping (default: false)
  * `ungroup` : set to true if call supports ungrouping (default: false)
  * `dtmf` : set to true if call supports dtmf tones (default: false)
  * `hold` : set to true if call supports hold (default: false)
* *onSuccess: function(uuid)* - a function where the call's `uuid` will be provided. This `uuid` should be used when calling `endCall` function.

```javascript
callKitService.startCall(name, isVideo, onSuccess);
```

to report an initiated outgoing call to the system
* *name: String* - the callee name, which should displayed in the call history.
* *isVideo: boolean* - set to true if this call can be a video call.
* *onSuccess: function(uuid)* - a function where the call's `uuid` will be provided. This `uuid` should be used when calling `callConnected` and `endCall` functions.

Use

```javascript
callKitService.callConnected(uuid);
```

to report the system that the outgoing call is connected.
* *uuid: String* - Uniquie identifier of the call.

Use

```javascript
callKitService.endCall(uuid, notify);
```

to let the system know, the call is ended.

* *uuid: String* - Uniquie identifier of the call. In case of incoming call, it is provided by the `reportIncomingCall` `onSuccess` callback.
* *notify: Boolean* - If `true`, sends a local notification to the system about the missed call.

On android the callscreen should be displayed by the app. Use

```javascript
callKitService.finishRing(uuid, notify);
```

to stop the ringtone playing.

* *uuid: String* - Uniquie identifier of the call. In case of incoming call, it is provided by the `reportIncomingCall` `onSuccess` callback.

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
