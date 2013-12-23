//
//  MTWebDriverRequestFetcher.h
//  iWebDriver
//
//  Created by Yu Chen on 4/16/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//
#import <Foundation/Foundation.h>
#import "MTHTTPResponse.h"
@class MTRESTServiceMapping;
@class MTWebViewController;

// This class fetches webdriver requests from a server (connector) that connects 
// webdriver requesters and iWebdrivers; the targeted requester is identified by
// requesterId.
@interface MTWebDriverRequestFetcher : NSObject {
  NSString* connectorAddr;
  NSString* requesterId;
	
  MTWebViewController *viewController_;
  MTRESTServiceMapping *serviceMapping_;
	
  NSString *status_;
}

@property (retain, nonatomic) MTWebViewController *viewController;
@property (retain, nonatomic) MTRESTServiceMapping* serviceMapping;
@property (readonly, nonatomic, copy) NSString *status;

// Singleton
+ (MTWebDriverRequestFetcher *)sharedInstance;
@end
