var exec = cordova.require('cordova/exec');

var CallKit = function() {
    console.log('CallKit instanced');
};

CallKit.prototype.register = function(callChanged,audioSystem) {
    var errorCallback = function() {};
    var successCallback = function(obj) {
        if (obj && obj.hasOwnProperty('callbackType')) {
            if (obj.callbackType == "callChanged") {
                /* this is a call changed callback! */
                callChanged(obj);
            } else if (obj.callbackType == "audioSystem") {
                /* this is an audio system callback! */
                audioSystem(obj.message);
            }
        } else {
        }
    };

    exec(successCallback, errorCallback, 'CallKit', 'register' );
};

CallKit.prototype.reportIncomingCall = function(name,params,onSuccess) {
    var supportsVideo = true;
    var supportsGroup = false;
    var supportsUngroup = false;
    var supportsDTMF = false;
    var supportsHold = false;

    if (typeof params === "boolean") {
        supportsVideo = params;
    } else if (typeof params === "object") {
        supportsVideo = (params.video === true);
        supportsGroup = (params.group === true);
        supportsUngroup = (params.ungroup === true);
        supportsDTMF = (params.dtmf === true);
        supportsHold = (params.hold === true);
    }

    var errorCallback = function() {};
    var successCallback = function(obj) {
        onSuccess(obj);
    };

    exec(successCallback, errorCallback, 'CallKit', 'reportIncomingCall', [name, supportsVideo, supportsGroup, supportsUngroup, supportsDTMF, supportsHold] );
};

CallKit.prototype.askNotificationPermission = function() {
    // TODO: allow user to pass a succes/error callback to know the user's answer
    var cb = function() {};
    exec(cb, cb, 'CallKit', 'askNotificationPermission', []);
};

CallKit.prototype.startCall = function(name,isVideo,onSuccess) {
    var errorCallback = function() {};
    var successCallback = function(obj) {
        onSuccess(obj);
    };

    exec(successCallback, errorCallback, 'CallKit', 'startCall', [name, isVideo] );
};

CallKit.prototype.callConnected = function(uuid) {
    var errorCallback = function() {};
    var successCallback = function() {};

    exec(successCallback, errorCallback, 'CallKit', 'callConnected', [uuid] );
};

CallKit.prototype.endCall = function(uuid, notify, contentTitle) {
    var errorCallback = function() {};
    var successCallback = function() {};

    exec(successCallback, errorCallback, 'CallKit', 'endCall', [uuid, notify, contentTitle] );
};

CallKit.prototype.finishRing = function(uuid) {
    var errorCallback = function() {};
    var successCallback = function() {};

    exec(successCallback, errorCallback, 'CallKit', 'finishRing', [uuid] );
};

if (typeof module != 'undefined' && module.exports) {
    module.exports = CallKit;
}