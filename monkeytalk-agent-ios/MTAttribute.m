//
//  MTAttribute.m
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

#import "MTAttribute.h"
#import "MTWebDriverResource.h"
#import "MTElement.h"
#import "MTWebDriverResponse.h"
#import "MTHTTPVirtualDirectory+ExecuteScript.h"
#import "MTHTTPStaticResource.h"
#import "MTWebViewController.h"

@implementation MTAttribute

- (id)initForElement:(MTElement *)element {
  if (![super init])
    return nil;
  
  // Not retained as per delegate pattern - avoids circular dependancies.
  element_ = element;
  
  return self;
}

+ (MTAttribute *)attributeDirectoryForElement:(MTElement *)element {
  return [[MTAttribute alloc] initForElement:element];
}

- (id<MTHTTPResource>)elementWithQuery:(NSString *)query {
  if ([query length] > 0) {
    NSString *queriedAttribute = [query substringFromIndex:1];
    id<MTHTTPResource> resource = [contents objectForKey:queriedAttribute];
    if (resource == nil) {
      resource = [MTNamedAttribute
                  namedAttributeDirectoryForElement:element_
                  andName:queriedAttribute];
      [self setResource:resource withName:queriedAttribute];
    }
  }
  // Need to delegate back to |super| so |MTSession| can set the session ID on
  // the response.
  return [super elementWithQuery:query];
}

@end

@implementation MTNamedAttribute

- (id) initForElement:(MTElement *)element
              andName:(NSString *)name {
  if (![super init]) {
    return nil;
  }
  // Not retained as per delegate pattern - avoids circular dependancies.
  element_ = element;
  name_ = name;
  
  [self setIndex:
   [MTWebDriverResource resourceWithTarget:self
                               GETAction:@selector(getAttribute)
                              POSTAction:NULL
                               PUTAction:NULL
                            DELETEAction:NULL]];
  return self;
}

+ (MTNamedAttribute *)namedAttributeDirectoryForElement:(MTElement *)element
                                              andName:(NSString *)name {
  return [[MTNamedAttribute alloc] initForElement:element andName:name];
}

- (id)getAttribute {
  return [element_ attribute:name_];
}

@end


