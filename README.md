# ionic-plugin-callkit
Ionic/Cordova plugin for CallKit

Exmaple:

```javascript
.factory('$ionicCallKit', ['$q', function($q) {
	var callKit;

	return {
		register : function( callChanged ) {
			if (typeof CallKit !== "undefined") {
				var q = $q.defer();
				callKit = new CallKit();
				callKit.register( callChanged );
				q.resolve(callKit);
				return q.promise;
			} else {
				return this;
			}
		},
		reportIncomingCall : function(name,isVideo) {
			if ((typeof CallKit !== "undefined") && (callKit)) {
				callKit.reportIncomingCall(name,isVideo);
			}
		}
	};
}])
```

use

```javascript
$ionicCallKit.register( callChanged )
```

to register the plugin. Where

```javascript
callChanged = function(obj) {
< your code >
}
```

obj has the following properties:
* "handle" : The handle of the call (the string what is displayed on the phone) String
* "isOutgoing" : is the call outgoing? Boolean
* "isOnHold" : is the is on hold? Boolean
* "hasConnected" : is it connected? Boolean
* "hasEnded" : is it eneded? Boolean
* "hasStartedConnecting" : is it started connecting (i.e. user pressed the accept button)? Boolean
* "endDate" : when the call is ended? String (ISO8601 format date time)
* "connectDate" : when the call is connected? String (ISO8601 format date time)
* "connectingDate" : when the call started to connect? String (ISO8601 format date time)
* "duration" : call duration? Double
