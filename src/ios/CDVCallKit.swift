/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

import PushKit

@objc(CDVCallKit) class CDVCallKit : CDVPlugin {
    let callManager = CDVCallManager()
    var providerDelegate: CDVProviderDelegate?
    
    func register(_ command:CDVInvokedUrlCommand) {
        var pluginResult = CDVPluginResult(
            status : CDVCommandStatus_ERROR
        )
        
        providerDelegate = CDVProviderDelegate(callManager: callManager)
        
        pluginResult = CDVPluginResult(
            status: CDVCommandStatus_OK
        )
        
        self.commandDelegate!.send(
            pluginResult,
            callbackId: command.callbackId
        )
    }

    func reportIncomingCall(_ command:CDVInvokedUrlCommand) {
        var pluginResult = CDVPluginResult(
            status : CDVCommandStatus_ERROR
        )
        
        let uuid = UUID(); /* command.arguments[0] as? String ?? "" */
        let name = command.arguments[1] as? String ?? ""
        let hasVideo = ((command.arguments[2] as? String ?? "false") == "true")

        providerDelegate?.reportIncomingCall(uuid,handle: name,hasVideo: hasVideo)
        
        pluginResult = CDVPluginResult(
            status: CDVCommandStatus_OK
        )
        
        self.commandDelegate!.send(
            pluginResult,
            callbackId: command.callbackId
        )
    }
}
