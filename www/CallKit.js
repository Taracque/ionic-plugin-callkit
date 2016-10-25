var exec = cordova.require('cordova/exec');

var CallKit = function() {
	console.log('CallKit instanced');
};

CallKit.prototype.register = function() {
	var errorCallback = function() {};
	var successCallback = function(obj) {
		console.log(obj);
	};

	exec(successCallback, errorCallback, 'CallKit', 'register' );
};

if (typeof module != 'undefined' && module.exports) {
	module.exports = CallKit;
}