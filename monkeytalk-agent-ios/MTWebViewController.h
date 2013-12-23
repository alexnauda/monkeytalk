//
//  MTWebViewController.h
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

#import <sqlite3.h>
#import <UIKit/UIKit.h>
#import "MTCommandEvent.h"

@interface MTFoundElement : UIView
@end

// The MTWebViewController manages the iWebDriver's WebView.
@interface MTWebViewController : UIViewController<UIWebViewDelegate>
{
@private
  // Used to track the number of page loads.  The view is considered loaded
  // when there are no pending page loads.
  int numPendingPageLoads_;

  NSString *lastJSResult_;
 
  NSURLRequestCachePolicy cachePolicy_;
  
  // Pointer to the status / activity label.
  IBOutlet UILabel *statusLabel_;
  
  // This is nil if the last operation succeeded.
  NSError *lastError_;
    
    NSArray *commandList;
    int commandIndex;
    NSString *testComplete;
    double playbackSpeed;
    double playbackTimeout;
    BOOL isCommandLoading;
    NSInteger errorIndex;
    NSInteger currentOrdinal;
    NSString *webComponentTree;
    BOOL isComponentLoaded;
}

@property (nonatomic, strong) UIWebView *webView;
@property (nonatomic, strong) id delController;
@property (nonatomic, strong) NSString *testComplete;
@property (nonatomic, readwrite) NSInteger errorIndex;
@property (nonatomic, readwrite) NSInteger currentOrdinal;

- (void)waitForLoad;

- (CGRect)viewableArea;
- (BOOL)pointIsViewable:(CGPoint)point;

// Some webdriver stuff.
- (id)visible;
- (void)setVisible:(NSNumber *)target;

// Get the current page title
- (NSString *)currentTitle;

// Get the URL of the page we're looking at
- (NSString *)URL;

// Navigate to a URL.
// The URL should be specified by the |url| key in the |urlMap|.
- (void)setURL:(NSDictionary *)urlMap;

- (void)forward:(NSDictionary*)ignored;
- (void)back:(NSDictionary*)ignored;
- (void)refresh:(NSDictionary*)ignored;
-(BOOL) shouldOpenLinksExternally;


// Evaluate a javascript string and return the result.
// Arguments can be passed in in NSFormatter (printf) style.
//
// Variables declared with var are kept between script calls. However, they are
// lost when the page reloads. Check before using any variables which were
// defined during previous events.
- (NSString *)jsEval:(NSString *)format, ...;
- (NSString *)evalFor:(int)frame jsEval:(NSString *)format, ...;

// Get the HTML source of the page we've loaded
- (NSString *)source;

// Get a screenshot of the page we've loaded
- (UIImage *)screenshot;


- (void)clickOnPageElementAt:(CGPoint)point;

// Calls the same on the main view controller.
- (void)describeLastAction:(NSString *)status;

// Get geolocation
- (id)location;

- (void) showTapAtPoint:(CGPoint)touchCenter;
- (CGPoint) translatePointToPage:(CGPoint)point;

// Set geolocation
- (void)setLocation:(NSDictionary *)dict;

// Check if browser connection is alive
- (NSNumber *)isBrowserOnline;

- (void) setUp;

- (void) seleniumFile:(NSString *)fileName speed:(double)speed timeout:(double)timeout;
- (void) runCommandAtIndex:(int)i;
- (void) runNextCommand;
- (void) runAgain;
- (void) throwScriptFailed:(NSString *)testComplete;

- (Boolean) isElementOffScreen:(id)element;
- (CGPoint)translatePageCoordinateToView:(CGPoint)point;
- (CGSize)translateSizeWithZoom:(CGSize)size;
- (void) showTapOnElement:(id)element;
- (void) showTapAtPoint:(CGPoint)touchCenter;
- (void) highlightElement:(id)element;

- (BOOL) playBackCommandInWebView:(MTCommandEvent *)event;
@property (nonatomic, strong) NSString *webComponentTree;

@end
