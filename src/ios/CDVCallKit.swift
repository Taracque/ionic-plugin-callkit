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

@available(iOS 10.0, *)
@objc(CDVCallKit) class CDVCallKit : CDVPlugin {
    var callManager: CDVCallManager?
    var providerDelegate: CDVProviderDelegate?
    var callbackId: String?

    func register(_ command:CDVInvokedUrlCommand) {
        self.commandDelegate.run(inBackground: {
            var pluginResult = CDVPluginResult(
                status : CDVCommandStatus_ERROR
            )
            
            self.callManager = CDVCallManager()

            self.providerDelegate = CDVProviderDelegate(callManager: self.callManager!)
            
            self.callbackId = command.callbackId

            NotificationCenter.default.addObserver(self, selector: #selector(self.handle(withNotification:)), name: Notification.Name("CDVCallManagerCallsChangedNotification"), object: nil)

            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK
            )
            pluginResult?.setKeepCallbackAs(true)
            
            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            )
        });
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
    }

    func reportIncomingCall(_ command:CDVInvokedUrlCommand) {
        var pluginResult = CDVPluginResult(
            status : CDVCommandStatus_ERROR
        )
        
        let uuid = UUID()
        let name = command.arguments[0] as? String ?? ""
        let hasVideo = ((command.arguments[1] as? String ?? "false") == "true")

        providerDelegate?.reportIncomingCall(uuid,handle: name,hasVideo: hasVideo)
        
        pluginResult = CDVPluginResult(
            status: CDVCommandStatus_OK
        )
        pluginResult?.setKeepCallbackAs(false)

        self.callbackId = command.callbackId
        
        self.commandDelegate!.send(
            pluginResult,
            callbackId: command.callbackId
        )
    }
    
    @objc func handle(withNotification notification : NSNotification) {
        if (notification.name == Notification.Name("CDVCallManagerCallsChangedNotification")) {
            let notificationObject = notification.object as? CDVCallManager
            var resultMessage = [String: Any]()
            
            if (((notificationObject?.calls) != nil) && (notificationObject!.calls.count>0)) {
                let call = (notificationObject?.calls[0])! as CDVCall
                
                resultMessage = [
                    "handle" : call.handle as String? ?? "",
                    "isOutgoing" : call.isOutgoing as Bool,
                    "isOnHold" : call.isOnHold as Bool,
                    "hasConnected" : call.hasConnected as Bool,
                    "hasEnded" : call.hasEnded as Bool,
                    "hasStartedConnecting" : call.hasStartedConnecting as Bool,
                    "endDate" : call.endDate?.string(format: "yyyy-MM-dd'T'HH:mm:ssZ") as String? ?? "",
                    "connectDate" : call.connectDate?.string(format: "yyyy-MM-dd'T'HH:mm:ssZ") as String? ?? "",
                    "connectingDate" : call.connectingDate?.string(format: "yyyy-MM-dd'T'HH:mm:ssZ") as String? ?? "",
                    "duration" : call.duration as Double
                ]
            }
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: resultMessage)
            pluginResult?.setKeepCallbackAs(true)

            print("RECEIVED SPECIFIC NOTIFICATION: \(notification)")
            
            self.commandDelegate!.send(
                pluginResult, callbackId: self.callbackId
            )
        } else {
            print("INVALID NOTIFICATION RECEIVED: \(notification)")
        }
    }
}
