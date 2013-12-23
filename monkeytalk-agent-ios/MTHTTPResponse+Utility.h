//
//  MTHTTPDataResponse+Utility.h
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

// The |Utility| category adds some convenience methods to MTHTTPDataResponse
// allowing |MTHTTPDataResponse|s to be created from strings.
@interface MTHTTPDataResponse (Utility)

// Init an |MTHTTPDataResponse| containing the given string encoded in UTF8. 
- (id)initWithString:(NSString *)str;

// Create and return an |MTHTTPDataResponse| containing the given string encoded
// in UTF8. Returned object is autoreleased.
+ (MTHTTPDataResponse *)responseWithString:(NSString *)str;

- (NSString *)description;

@end
