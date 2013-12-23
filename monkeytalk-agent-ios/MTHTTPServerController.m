//
//  MTHTTPServerController.m
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

#import "MTHTTPServerController.h"
#import "MTWebDriverHTTPServer.h"
#import "MTWebDriverHTTPConnection.h"
#import "MTRESTServiceMapping.h"
#import "MTWebDriverPreferences.h"

#import <sys/types.h>
#import <sys/socket.h>
#import <ifaddrs.h>
#import <arpa/inet.h>

@implementation MTHTTPServerController

@synthesize status = status_;
@synthesize viewController = viewController_;
@synthesize serviceMapping = serviceMapping_;

-(NSString *)getAddress {
  
  struct ifaddrs *head;
  if (getifaddrs(&head))
    return @"unknown";
  
  // Default to return localhost.
  NSString *address = @"127.0.0.1";
  
  // |head| contains the first element in a linked list of interface addresses.
  // Iterate through the list.
  for (struct ifaddrs *ifaddr = head;
       ifaddr != NULL;
       ifaddr = ifaddr->ifa_next) {
    
    struct sockaddr *sock = ifaddr->ifa_addr;

    NSString *interfaceName = [NSString stringWithUTF8String:ifaddr->ifa_name];

    // Ignore localhost.
    if ([interfaceName isEqualToString:@"lo0"])
      continue;
  
    // Ignore IPv6 for now.
    if (sock->sa_family == AF_INET && [interfaceName isEqualToString:@"en0"]) {
      struct in_addr inaddr = ((struct sockaddr_in *)sock)->sin_addr;
      char *name = inet_ntoa(inaddr);
      address = [NSString stringWithUTF8String:name];
      break;
    }
  }
  
  freeifaddrs(head);
  
  return address;
}

-(id) init {
  if (![super init])
    return nil;
  UInt16 portNumber = [[MTWebDriverPreferences sharedInstance] serverPortNumber];

  server_ = [[MTWebDriverHTTPServer alloc] init];

  [server_ setType:@"_http._tcp."];
  [server_ setPort:portNumber];
  [server_ setDelegate:self];
  [server_ setConnectionClass:[MTWebDriverHTTPConnection class]];
  
  NSError *error;
  BOOL success = [server_ start:&error];
  
  if(!success) {
    NSLog(@"Error starting HTTP Server: %@", error);
  }

  NSLog(@"HTTP server started on addr %@ port %d",
        [self getAddress],
        [server_ port]);
  
  status_ = [[NSString alloc] initWithFormat:@"Started at http://%@:%d/hub/",
             [self getAddress],
             [server_ port]];

  serviceMapping_ = [[MTRESTServiceMapping alloc] init];

  return self;
}

// Singleton

static MTHTTPServerController *singleton = nil;

+ (void)kill {
    [singleton->server_ stop];
    singleton->server_ = nil;
    singleton->serviceMapping_ = nil;
    singleton->status_ = nil;
    singleton = nil;
}

+(MTHTTPServerController*) sharedInstance {
  if (singleton == nil) {
    singleton = [[MTHTTPServerController alloc] init];
  }
  
  return singleton;
}

- (NSObject<MTHTTPResponse> *)httpResponseForRequest:(CFHTTPMessageRef)request {
  return [serviceMapping_ httpResponseForRequest:request];
}

- (NSObject *)httpResponseForQuery:(NSString *)query
														method:(NSString *)method
													withData:(NSData *)theData {
	return [serviceMapping_.serverRoot httpResponseForQuery:query
																									 method:method
																								 withData:theData];
}

@end
