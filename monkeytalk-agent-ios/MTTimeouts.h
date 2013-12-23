//
//  MTTimeouts.h
//  iWebDriver
//
//  Copyright 2010 Google Inc.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

#import <Foundation/Foundation.h>
#import "MTHTTPVirtualDirectory.h"

@class MTSession;

// This |MTHTTPVirtualDirectory| matches the /hub/:session/timeouts
// directory in the WebDriver REST service.
@interface MTTimeouts : MTHTTPVirtualDirectory {
  MTSession* session_;
}

- (id)initWithSession:(MTSession*)session;

+ (MTTimeouts*)timeoutsForSession:(MTSession*)session;

@end

// This |MTHTTPVirtualDirectory| matches the /hub/:session/timeouts/implicit_wait
// directory in the WebDriver REST service.
@interface MTImplicitWait : MTHTTPVirtualDirectory {
  MTSession* session_;
}

- (id)initWithSession:(MTSession*)session;

+ (MTImplicitWait*)implicitWaitForSession:(MTSession*)session;

- (long)getImplicitWait;
- (void)setImplicitWait:(NSDictionary *)params;
- (void)clearImplicitWait;

@end

// This |MTHTTPVirtualDirectory| 
@interface MTScriptTimeout : MTHTTPVirtualDirectory {
  MTSession* session_;
}

- (id)initWithSession:(MTSession*)session;

+ (MTScriptTimeout*)scriptTimeoutForSession:(MTSession*)session;

- (long)getScriptTimeout;
- (void)setScriptTimeout:(NSDictionary *)params;
- (void)clearScriptTimeout;

@end