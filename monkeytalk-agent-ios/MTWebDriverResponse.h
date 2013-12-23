//
//  MTWebDriverResponse.h
//  iWebDriver
//
//  Copyright 2009 Google Inc.
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
#import "MTHTTPResponse.h"

@class MTHTTPJSONResponse;

// |MTWebDriverResponse| encapsulates the information for a response to a
// WebDriver RPC method, as defined by the WebDriver wire protocol.
//
// This is implemented as a proxy around an MTHTTPResponse. When the standard
// MTHTTPResponse methods are called, the data in MTWebDriverResponse's fields are
// baked into an MTHTTPJSONResponse and the data fields (status, value, sessionId)
// become immutable.
// 
// Don't confuse MTHTTPResponse (a response to an HTTP message) and a
// WebDriver |Response| (an MTHTTPResponse containing the return value of a
// method).
@interface MTWebDriverResponse : NSObject {
  // These fields mirror their equivalents in WebDriver.
  
  // The status code for this response. A non-zero value indicates some error
  // occurred.
  int status_;
  
  // The value the method returned or the exception generated
  id value_;
  
  // The active session
  NSString *sessionId_;

  // We're a proxy around this response.
  MTHTTPDataResponse *response_;
}

@property (nonatomic) BOOL isError;
@property (nonatomic) int status;
@property (nonatomic, strong) id value;
@property (nonatomic, copy) NSString *sessionId;

- (id)initWithValue:(id)value;
- (id)initWithError:(id)error;

+ (MTWebDriverResponse *)responseWithValue:(id)value;
+ (MTWebDriverResponse *)responseWithError:(id)error;

- (MTHTTPDataResponse *)response;

@end

// This is needed so the world knows that MTWebDriverResponse indirectly
// implements the |MTHTTPResponse| protocol. MTWebDriverResponse implements this
// through forwarding. Doing it this way supresses compiler warnings. 
@interface MTWebDriverResponse (ForwardedHTTPResponse) <MTHTTPResponse>

@end