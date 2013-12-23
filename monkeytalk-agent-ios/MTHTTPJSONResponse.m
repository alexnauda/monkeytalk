//
//  MTHTTPJSONResponse.m
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

#import "MTHTTPJSONResponse.h"
#import "NSObject+SBJSONMT.h"

@implementation MTHTTPJSONResponse

- (id)initWithData:(NSData *)data {
  NSLog(@"Use initWithObject for MTHTTPJSONResponse");
  return nil;
}

// Designated initaliser
- (id)initWithObject:(id)object {
  NSString *contents = [object JSONFragment];
  NSLog(@"Sending JSON: %@", contents);
  NSData *theData = [contents dataUsingEncoding:NSUTF8StringEncoding];
  return [super initWithData:theData];
}

+ (MTHTTPJSONResponse *)responseWithObject:(id)object {
  if (object == nil)
    return nil;
  else
    return [[self alloc] initWithObject:object];
}

- (NSString *)contentType {
  return @"application/json; charset=UTF-8";
}

@end
