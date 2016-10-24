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

#import "CDVCallKit.h"
#import <CallKit/CallKit.h>

@implementation CDVCallKit

@synthesize callProvider;
@synthesize callbackId;

static CXProviderConfiguration * providerConfiguration;

- (void)init:(CDVInvokedUrlCommand*)command
{
    [self.commandDelegate runInBackground:^{
        providerConfiguration = [[CXProviderConfiguration alloc] initWithLocalizedName:@"Talkivo"];
        providerConfiguration.maximumCallGroups = 1;
        providerConfiguration.ringtoneSound = @"video-call-incoming.mp3";
        providerConfiguration.maximumCallsPerCallGroup = 1;
        
        // create the provider
        callProvider = [callProvider initWithConfiguration:providerConfiguration];
        [callProvider setDelegate:self queue:dispatch_get_main_queue()];
    }];
    
}

- (void)receiveCall:(CDVInvokedUrlCommand*)command
{
    NSString *userId = @"Test User";
    NSUUID * uuid = [NSUUID UUID];
    BOOL hasVideo = YES;
    
    CXCallUpdate * update = [CXCallUpdate new];
    update.localizedCallerName = userId;
    update.hasVideo = hasVideo;
    
    [callProvider reportNewIncomingCallWithUUID:uuid update:update completion:^(NSError * error){
        if (error == nil) {
            
        }
    }];
}

- (void) provider:(CXProvider *)provider performAnswerCallAction:(CXAnswerCallAction *) action
{
    [action fulfill];
}


- (void) provider:(CXProvider *)provider performEndCallAction:(CXEndCallAction *) action
{
    
    [action fulfill];
    
}

- (void) providerDidReset:(CXProvider *)provider {
    
}

@end
