/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    // Application Constructor
    initialize: function() {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },

    // deviceready Event Handler
    //
    // Bind any cordova events here. Common events are:
    // 'pause', 'resume', etc.
    onDeviceReady: function() {
		this.callUUID = '';
		this.callKit;

		function callChanged(data) {
			console.log("onCallChanged: "+JSON.stringify(data));
		}

		function audioSystem(data) {
			console.log("onAudioSystem: "+JSON.stringify(data));
		}

		this.callKit = new CallKit();
		this.callKit.register( callChanged, audioSystem );

		setTimeout( this.incomingCall.bind(this), 10000);
    },

	incomingCall : function() {
		this.callKit.reportIncomingCall('Incoming call', {
			supportsVideo : false,
			supportsGroup: false,
			supportsUngroup: false,
			supportsDTMF: false,
			supportsHold: false
		}, function(uuid) {
			this.callUUID = uuid;
			console.log('call reported. returned uuid: ' + uuid);
		});
	}
};

app.initialize();