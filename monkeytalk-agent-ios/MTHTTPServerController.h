//
//  MTHTTPServerController.h
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
#import <UIKit/UIKit.h>

@class MTHTTPServer;
@class MTWebViewController;
@class MTRESTServiceMapping;

// |MTHTTPServerController| creates, configures and controls the web server.
@interface MTHTTPServerController : NSObject {
  MTHTTPServer *server_;
	
  MTWebViewController *viewController_;

  // The current status of the server for users.
  NSString *status_;
	
  MTRESTServiceMapping *serviceMapping_;
}

@property (retain, nonatomic) MTWebViewController *viewController;
@property (readonly, nonatomic) MTRESTServiceMapping *serviceMapping;
@property (readonly, nonatomic, copy) NSString *status;

+(MTHTTPServerController *)sharedInstance;
+ (void)kill;

-(NSObject *)httpResponseForQuery:(NSString *)query
                           method:(NSString *)method
                         withData:(NSData *)theData;
-(NSString *)getAddress;
@end
