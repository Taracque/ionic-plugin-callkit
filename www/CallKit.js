var exec = cordova.require('cordova/exec');

var CallKit = function() {
	console.log('CallKit instanced');
};

CallKit.prototype.init = function() {
	var errorCallback = function() {};
	var successCallback = function(obj) {
		console.log(obj);
	};

	exec(successCallback, errorCallback, 'CallKit', 'init' );
};

CallKit.prototype.receiveCall = function(username,call_id,video) {
	var errorCallback = function() {};
	var successCallback = function(obj) {
		console.log(obj);
	};

	exec(successCallback, errorCallback, 'CallKit', 'receiveCall' );
}

if (typeof module != 'undefined' && module.exports) {
	module.exports = CallKit;
}