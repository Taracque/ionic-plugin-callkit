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

@synthesize callObserver;

- (instancetype)init
{
    self = [super init];
    if (self) {
        
        // create the observer
        _callObserver = [CXCallObserver new];
        [_callObserver setDelegate:self queue:dispatch_get_main_queue()];
        
    }
    return self;
}

- (void)callstateNow {
    if ([self.callObserver.calls count] == 0) {
        [self callStateValue:nil];
    } else {
        for (CXCall *call in self.callObserver.calls) {
            [self callStateValue:call];
        }
    }
}

- (void)callObserver:(CXCallObserver *)callObserver callChanged:(CXCall *)call{
    [self callStateValue:call];
}

- (void)callStateValue:(CXCall *)call {
    
    NSLog(@"hasEnded     %@", call.hasEnded? @"YES":@"NO");
    NSLog(@"isOutgoing   %@", call.isOutgoing? @"YES":@"NO");
    NSLog(@"isOnHold     %@", call.isOnHold? @"YES":@"NO");
    NSLog(@"hasConnected %@", call.hasConnected? @"YES":@"NO");
    
    
    // Call ended
    if (call == nil || call.hasEnded == YES) {
        NSLog(@"CXCallState : Disconnected");
    }
    
    // Outgoing, not connected
    if (call.isOutgoing == YES && call.hasConnected == NO) {
        NSLog(@"CXCallState : Dialing");
    }
    
    // Incoming, not connected
    if (call.isOutgoing == NO  && call.hasConnected == NO && call.hasEnded == NO && call != nil) {
        NSLog(@"CXCallState : Incoming");
    }
    
    // Connected
    if (call.hasConnected == YES && call.hasEnded == NO) {
        NSLog(@"CXCallState : Connected");
    }
    
}

@end
