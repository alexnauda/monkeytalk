//
//  RESTServiceHandler.m
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

#import "MTJSONRESTResource.h"
#import "NSString+SBJSONMT.h"
#import "MTHTTPJSONResponse.h"

@implementation MTJSONRESTResource

@synthesize target = target_, action = action_;

- (id)initWithTarget:(id)theTarget action:(SEL)theAction {
  if (![super init])
    return nil;
  
  [self setTarget:theTarget];
  [self setAction:theAction];
  
  return self;
}

- (void)dealloc {
  
  // TODO: do I need to do this?
  [self setAction:nil];
  
}

+ (MTJSONRESTResource *)JSONResourceWithTarget:(id)theTarget
                                      action:(SEL)theAction {
  return [[self alloc] initWithTarget:theTarget action:theAction];
}

// Get the HTTP response to this request
- (id<MTHTTPResponse,NSObject>)httpResponseForQuery:(NSString *)query
                                           method:(NSString *)method
                                         withData:(NSData *)theData {
  id requestData = nil;
  
  if ([theData length] > 0) {
    NSString *dataString =
      [[NSString alloc] initWithData:theData
                            encoding:NSUTF8StringEncoding];
    
    requestData = [dataString JSONFragmentValue];
  }
  
  id<MTHTTPResponse,NSObject> response = [target_ performSelector:action_
                                                     withObject:requestData
                                                     withObject:method];

  return response;
}

// I will take things from here.
- (id<MTHTTPResource>)elementWithQuery:(NSString *)query {
  return self;
}

@end

@implementation MTHTTPVirtualDirectory (JSONResource)

// Helper method to set JSON resources
- (void)setMyJSONHandler:(SEL)selector withName:(NSString *)name {
  [self setResource:[MTJSONRESTResource JSONResourceWithTarget:self
                                                      action:selector]
           withName:name];
}

@end
