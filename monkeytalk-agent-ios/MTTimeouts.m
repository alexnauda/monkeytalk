//
//  MTTimeouts.m
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


#import "MTTimeouts.h"
#import "MTSession.h"
#import "MTWebDriverResource.h"

@implementation MTTimeouts

- (id)initWithSession:(MTSession*)session {
  self = [super init];
  if (!self) {
    return nil;
  }
  [self setResource:[MTScriptTimeout scriptTimeoutForSession:session]
           withName:@"async_script"];
  [self setResource:[MTImplicitWait implicitWaitForSession:session]
           withName:@"implicit_wait"];
  return self;
}

+ (MTTimeouts*) timeoutsForSession:(MTSession*)session {
  return [[MTTimeouts alloc] initWithSession:session];
}

@end

@implementation MTImplicitWait

- (id)initWithSession:(MTSession*)session {
  self = [super init];
  if (!self) {
    return nil;
  }
  session_ = session;
  [self setIndex:
   [MTWebDriverResource resourceWithTarget:self
                               GETAction:@selector(getImplicitWait)
                              POSTAction:@selector(setImplicitWait:)
                               PUTAction:NULL
                            DELETEAction:@selector(clearImplicitWait)]];
  return self;
}



+ (MTImplicitWait*) implicitWaitForSession:(MTSession*)session {
  return [[MTImplicitWait alloc] initWithSession:session];
}

- (long) getImplicitWait {
  return [session_ implicitWait];
}

- (void) setImplicitWait:(NSDictionary *)params {
  NSNumber* number = (NSNumber*) [params objectForKey:@"ms"];
  NSLog(@"Setting implicit waits to %@ms", number);
  NSTimeInterval wait = [number doubleValue] / 1000.0;
  [session_ setImplicitWait:wait];
}

- (void) clearImplicitWait {
  [session_ setImplicitWait:0];
}

@end

@implementation MTScriptTimeout

- (id)initWithSession:(MTSession*)session {
  self = [super init];
  if (!self) {
    return nil;
  }
  session_ = session;
  [self setIndex:
   [MTWebDriverResource resourceWithTarget:self
                               GETAction:@selector(getScriptTimeout)
                              POSTAction:@selector(setScriptTimeout:)
                               PUTAction:NULL
                            DELETEAction:@selector(clearScriptTimeout)]];
  return self;
}



+ (MTScriptTimeout*) scriptTimeoutForSession:(MTSession*)session {
  return [[MTScriptTimeout alloc] initWithSession:session];
}

- (long) getScriptTimeout {
  return [session_ scriptTimeout];
}

- (void) setScriptTimeout:(NSDictionary *)params {
  NSNumber* number = (NSNumber*) [params objectForKey:@"ms"];
  NSLog(@"Setting script timeouts to %@ms", number);
  NSTimeInterval wait = [number doubleValue] / 1000.0;
  [session_ setScriptTimeout:wait];
}

- (void) clearScriptTimeout {
  [session_ setScriptTimeout:0];
}

@end
