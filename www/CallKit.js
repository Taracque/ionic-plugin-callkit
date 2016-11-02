var exec = cordova.require('cordova/exec');

var CallKit = function() {
	console.log('CallKit instanced');
};

CallKit.prototype.register = function(callChanged) {
	var errorCallback = function() {};
	var successCallback = function(obj) {
		if (obj && obj.hasOwnProperty('connectDate')) {
			/* this is a call changed callback! */
			callChanged(obj);
		} else {
		}
	};

	exec(successCallback, errorCallback, 'CallKit', 'register' );
};

CallKit.prototype.reportIncomingCall = function(name,isVideo,onSuccess) {
	var errorCallback = function() {};
	var successCallback = function(obj) {
		onSuccess(obj);
	};

	exec(successCallback, errorCallback, 'CallKit', 'reportIncomingCall', [name, isVideo] );
};

CallKit.prototype.endCall = function(uuid) {
	var errorCallback = function() {};
	var successCallback = function() {};

	exec(successCallback, errorCallback, 'CallKit', 'endCall', [uuid] );
};

if (typeof module != 'undefined' && module.exports) {
	module.exports = CallKit;
}