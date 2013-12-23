//
//  MTWebViewController.m
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

#import "MTWebViewController.h"
#import "MTHTTPServerController.h"
#import "NSException+MTWebDriver.h"
#import "NSURLRequest+MTIgnoreSSL.h"
#import "UIResponder+MTSimulateTouch.h"
#import "MTWebDriverPreferences.h"
#import "MTWebDriverRequestFetcher.h"
#import "MTWebDriverUtilities.h"
#import "NSString+SBJSONMT.h"
#import <objc/runtime.h>
#import <QuartzCore/QuartzCore.h>
#import <QuartzCore/CATransaction.h>
#import "MTerrorcodes.h"
#import "MTElement.h"
#import "MTSession.h"
#import "MTHTTPVirtualDirectory.h"
#import "MTHTTPVirtualDirectory+FindElement.h"
#import "MTUtils.h"
#import "MTSeleniumCommand.h"
#import "MTVerifyCommand.h"
#import "MTConvertType.h"
#import "NSString+MonkeyTalk.h"
#import "MonkeyTalk.h"
#import "MTCommandEvent.h"
#import "MTWebRecorder.h"
#import "UIView+MTReady.h"
#import "MTWebDriverResponse.h"
#import "MTHTTPServerController.h"
#import "NSObject+SBJSONMT.h"
#import "WebView.h"
#import "UIWebDocumentView.h"
#import "MTWebDriverRequestFetcher.h"
#import "UIWebView+Selenium.h"


@implementation MTFoundElement
// Dummy to handle errors with webview
@end

//static const NSString* kGeoLocationKey = @"location";
//static const NSString* kGeoLongitudeKey = @"longitude";
//static const NSString* kGeoLatitudeKey = @"latitude";
//static const NSString* kGeoAltitudeKey = @"altitude";
const int COLUMN_COUNT = 3;

MTWebRecorder *webRecorder;
@implementation MTWebViewController

typedef enum {
	COMMAND,
	ID,
    FIELD,
    ARGS
} fields;

@synthesize webView, delController, testComplete, errorIndex, currentOrdinal, webComponentTree;

- (id) init {
    
    if ((self = [super init]))
    {
        [self setUp];
    }
    
    return self;
}

// Executed after the nib loads the interface.
// Configure the webview to match the mobile safari app.
- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self setUp];
}

- (void) setUp
{
    [[self webView] setScalesPageToFit:NO];
    [[self webView] setDelegate:self];
    
    if ([[UIDevice currentDevice] respondsToSelector:@selector(setMediaPlaybackRequiresUserAction)]) {
        [[self webView] setMediaPlaybackRequiresUserAction:NO];
    } else {
        //Too bad, though it seems iOS 3 supported this by default
    }
    
    lastJSResult_ = nil;
    
    // Creating a new session if auto-create is enabled
    //  if ([[RootViewController sharedInstance] isAutoCreateSession]) {
    [[MTHTTPServerController sharedInstance]
     httpResponseForQuery:@"/hub/session"
     method:@"POST"
     withData:[@"{\"browserName\":\"firefox\",\"platform\":\"ANY\","
               "\"javascriptEnabled\":false,\"version\":\"\"}"
               dataUsingEncoding:NSASCIIStringEncoding]];
    //  }
    
    MTWebDriverPreferences *preferences = [MTWebDriverPreferences sharedInstance];
    
    cachePolicy_ = [preferences cache_policy];
    NSURLCache *sharedCache = [NSURLCache sharedURLCache];
    [sharedCache setDiskCapacity:[preferences diskCacheCapacity]];
    [sharedCache setMemoryCapacity:[preferences memoryCacheCapacity]];
    
    if ([[preferences mode] isEqualToString: @"Server"]) {
        MTHTTPServerController* serverController = [MTHTTPServerController sharedInstance];
        [serverController setViewController:self];
        [self describeLastAction:[serverController status]];
    } else {
        MTWebDriverRequestFetcher* fetcher = [MTWebDriverRequestFetcher sharedInstance];
        [fetcher setViewController:self];
        [self describeLastAction:[fetcher status]];
    }
}

- (void)didReceiveMemoryWarning {
    NSLog(@"Memory warning recieved.");
    // TODO(josephg): How can we send this warning to the user? Maybe set the
    // displayed text; though that could be overwritten basically straight away.
    [super didReceiveMemoryWarning];
}

- (void)dealloc {
    [[self webView] setDelegate:nil];
    //  [lastJSResult_ release];
}

//- (UIWebView *)webView {
//  if (![[self view] isKindOfClass:[UIWebView class]]) {
//    NSLog(@"NIB error: MTWebViewController's view is not a UIWebView.");
//    return nil;
//  }
//  return (UIWebView *)[self view];
//}

- (BOOL)webView:(UIWebView *)webView2
shouldStartLoadWithRequest:(NSURLRequest *)request
 navigationType:(UIWebViewNavigationType)navigationType {
    // Handling Record event for web components; receives key and values from java script, parse the values according to MonkeyTalk API
    BOOL shouldStart = YES;
    NSString *requestString = [[[request URL] absoluteString] stringByReplacingPercentEscapesUsingEncoding: NSUTF8StringEncoding];
    NSArray *requestArray = [requestString componentsSeparatedByString:@":monkeytalk"];
    
    // Call original delegate webView:shouldStartLoadWithRequest:navigationType:
    if ([delController respondsToSelector:@selector(webView:shouldStartLoadWithRequest:navigationType:)])
        shouldStart = [delController webView:webView2 shouldStartLoadWithRequest:request navigationType:navigationType];
    
    if (!shouldStart)
        return NO;
    
    if ([requestArray count] > 1){
        NSString *msgFromJS = ([requestArray count] > 0) ? [requestArray objectAtIndex:1] : @"";
        NSLog(@"msg: %@",msgFromJS);
        
        if ([[MonkeyTalk sharedMonkey] state] == MTStateRecording) {
            [webRecorder jsMessageKey:msgFromJS];
        }
        return NO;
    } else {
        // Call original delegate webView:shouldStartLoadWithRequest:navigationType:
        if ([delController respondsToSelector:@selector(webView:shouldStartLoadWithRequest:navigationType:)]) {
            return [delController webView:webView2 shouldStartLoadWithRequest:request navigationType:navigationType];
        }
    }
    /*
     // Uncomment to return from javascript
     NSString *req = [[request URL] absoluteString];
     
     if ([req hasPrefix:@"monkeytalk:"]) {
     NSArray *parameters = [req componentsSeparatedByString:@":"];
     
     NSString *function = [NSString stringWithFormat:@"%@",[parameters objectAtIndex:1]];
     NSString *args = [NSString stringWithFormat:@"%@",[[parameters objectAtIndex:2] stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
     
     NSLog(@"fromJS: %@",function);
     
     //        NSArray *argsArray = [NSArray arrayWithArray:[sbJson objectWithString:args error:nil]];
     
     //        [self jsFunction:function args:argsArray];
     
     webComponentTree = function;
     isComponentLoaded = YES;
     
     return NO;
     }
     
     */
    
    //  NSURL* url = [request URL];
    //  if ([[url scheme] isEqualToString:@"webdriver"]) {
    //
    //    NSString* action = [url host];
    //    if (action == nil) {
    //        // No action specified; ignoring webdriver://
    //        return NO;
    //    }
    //
    //    NSString* jsonData = @"{}";
    //    if ([url query] != nil) {
    //      jsonData = [[url query]
    //          stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    //    }
    //
    //    // TODO: catch malformed query data and broadcast an appropriate error.
    //    NSDictionary* data = (NSDictionary*) [jsonData JSONValue];
    //    [[NSNotificationCenter defaultCenter]
    //        postNotificationName:[NSString stringWithFormat:@"webdriver:%@", action]
    //                      object:self
    //                    userInfo:data];
    //    return NO;
    //  }
    
    return shouldStart;
}

- (void)webViewDidStartLoad:(UIWebView *)webView2 {
    // Call original delegate webViewDidStartLoad:
    if ([delController respondsToSelector:@selector(webViewDidStartLoad:)])
        [delController webViewDidStartLoad:webView2];
    
    [[NSNotificationCenter defaultCenter]
     postNotificationName:@"webdriver:pageLoad"
     object:self];
    @synchronized(self) {
        numPendingPageLoads_ += 1;
    }
}

- (void)webViewDidFinishLoad:(UIWebView *)webView2 {
    // Code for setting listeners on web elements from objc
    //    [webView stringByEvaluatingJavaScriptFromString:@"function recordClick () {var iFrame = document.createElement(\"iframe\");iFrame.setAttribute(\"src\", 'monkeytalk://' + this.tagName + '/' + this.getAttribute('id') +  '/recordTap:param1:param2:param3');document.documentElement.appendChild(iFrame);iFrame.parentNode.removeChild(iFrame);iFrame = null;}var elements = document.getElementsByTagName('*');for (var i = 0; i < elements.length; i++) {var element = elements[i];if(element.getElementsByTagName('*').length == 0)element.addEventListener (\"click\", recordClick, false);}"];
    
    // Call original delegate webViewDidFinishLoad:
    if ([delController respondsToSelector:@selector(webViewDidFinishLoad:)])
        [delController webViewDidFinishLoad:webView2];
    
    webRecorder = [[MTWebRecorder alloc] init];
    
    // NSString *jsFilePath = [[NSBundle mainBundle] pathForResource:@"WebviewRecorder" ofType:@"js"];
    
    //   NSString *jsContent = [NSString stringWithContentsOfFile:jsFilePath encoding:NSUTF8StringEncoding error:nil];
    
    NSString *jsContent = [webRecorder webRecorderJSScript];
    
    [self.webView stringByEvaluatingJavaScriptFromString:jsContent];
    
    @synchronized(self) {
        numPendingPageLoads_ -= 1;
    }
}

- (void)webView:(UIWebView *)webView2 didFailLoadWithError:(NSError *)error {
    // This is a very troubled method. It can be called multiple times (for each
    // frame of webpage). It is sometimes called even when the page seems to have
    // loaded correctly.
    
    // Page loading errors are ignored because that's what WebDriver expects.
    
    // Call original delegate webView:didFailLoadWithError:
    if ([delController respondsToSelector:@selector(webView:didFailLoadWithError:)]) {
        if (!([error.domain isEqualToString:@"WebKitErrorDomain"] &&
              [error.description rangeOfString:@"fbconnect://"].location != NSNotFound))
            [delController webView:webView2 didFailLoadWithError:error];
        else
            // FBConnect failure
            return;
    }
    
    if ([error code] == 101) {
        NSString *failingURLString = [[error userInfo] objectForKey:
                                      @"NSErrorFailingURLStringKey"];
        // This is an issue only with simulator due to lack of support for tel: url.
        if ([[failingURLString substringToIndex:4] isEqualToString:@"tel:"]) {
            @throw [NSException webDriverExceptionWithMessage:
                    [NSString stringWithFormat:
                     @"tel: url isn't supported in simulator"]
                                                andStatusCode:EUNHANDLEDERROR];
        }
    } else if (error.code == NSURLErrorCancelled)
        return;
    
    @synchronized(self) {
        numPendingPageLoads_ -= 1;
    }
}

- (BOOL)shouldOpenLinksExternally {
    return YES;
}

#pragma mark Web view controls

- (void)performSelectorOnWebView:(SEL)selector withObject:(id)obj {
    [[self webView] performSelector:selector withObject:obj];
}

- (void)waitForLoad {
    // TODO(josephg): Test sleep intervals on the device.
    // This delay should be long enough that the webview has isLoading
    // set correctly (but as short as possible - these delays slow down testing.)
    
    // - The problem with [view isLoading] is that it gets set in a separate
    // worker thread. So, right after asking the webpage to load a URL we need to
    // wait an unspecified amount of time before isLoading will correctly tell us
    // whether the page is loading content.
    
    [NSThread sleepForTimeInterval:0.2f];
    
    while ([[self webView] isLoading]) {
        // Yield.
        [NSThread sleepForTimeInterval:0.01f];
    }
    
    // The main view may be loaded, but there may be frames that are still
    // loading.
    while (true) {
        @synchronized(self) {
            if (numPendingPageLoads_ == 0) {
                break;
            }
        }
    }
}

// All method calls on the view need to be done from the main thread to avoid
// synchronization errors. This method calls a given selector in this class
// optionally with an argument.
//
// If called with waitUntilLoad:YES, we wait for a web page to be loaded in the
// view before returning.
- (void)performSelectorOnView:(SEL)selector
                   withObject:(id)value
                waitUntilLoad:(BOOL)wait {
    
    /* The problem with this method is that the UIWebView never gives us any clear
     * indication of whether or not it's loading and if so, when its done. Asking
     * it to load causes it to begin loading sometime later (isLoading returns NO
     * for awhile.) Even the |webViewDidFinishLoad:| method isn't a sure sign of
     * anything - it will be called multiple times, once for each frame of the
     * loaded page.
     *
     * The result: The only effective method I can think of is nasty polling.
     */
    
    while ([[self webView] isLoading])
        [NSThread sleepForTimeInterval:0.01f];
    
    [[self webView] performSelectorOnMainThread:selector
                                     withObject:value
                                  waitUntilDone:YES];
    
    //  NSLog(@"loading %d", [[self webView] isLoading]);
    
    if (wait)
        [self waitForLoad];
}

// Get the specified URL and block until it's finished loading.
- (void)setURL:(NSDictionary *)urlMap {
    NSString *urlString = (NSString*) [urlMap objectForKey:@"url"];
    NSURLRequest *url = [NSURLRequest requestWithURL:[NSURL URLWithString:urlString]
                                         cachePolicy:cachePolicy_
                                     timeoutInterval:60];
    
    [self performSelectorOnView:@selector(loadRequest:)
                     withObject:url
                  waitUntilLoad:YES];
}

- (void)back:(NSDictionary*)ignored {
    [self describeLastAction:@"back"];
    [self performSelectorOnView:@selector(goBack)
                     withObject:nil
                  waitUntilLoad:YES];
}

- (void)forward:(NSDictionary*)ignored {
    [self describeLastAction:@"forward"];
    [self performSelectorOnView:@selector(goForward)
                     withObject:nil
                  waitUntilLoad:YES];
}

- (void)refresh:(NSDictionary*)ignored {
    [self describeLastAction:@"refresh"];
    [self performSelectorOnView:@selector(reload)
                     withObject:nil
                  waitUntilLoad:YES];
}

- (id)visible {
    // The WebView is always visible.
    return [NSNumber numberWithBool:YES];
}

// Ignored.
- (void)setVisible:(NSNumber *)target {
}

// Execute js in the main thread and set lastJSResult_ appropriately.
// This function must be executed on the main thread. Its designed to be called
// using performSelectorOnMainThread:... which doesn't return a value - so
// the return value is passed back through a class parameter.
- (void)jsEvalInternal:(NSString *)script {
    // We wrap the eval command in a CATransaction so that we can explicitly
    // force any UI updates that might occur as a side effect of executing the
    // javascript to finish rendering before we return control back to the HTTP
    // server thread. We actually found some cases where the rendering was
    // finishing before control returned and so the core animation framework would
    // defer committing its implicit transaction until the next iteration of the
    // HTTP server thread's run loop. However, because you're only allowed to
    // update the UI on the main application thread, committing it on the HTTP
    // server thread would cause the whole application to crash.
    // This feels like it shouldn't be necessary but it was the only way we could
    // find to avoid the problem.
    [CATransaction begin];
    //  [lastJSResult_ release];
    lastJSResult_ = nil;
    lastJSResult_ = [[self webView]
                      stringByEvaluatingJavaScriptFromString:script];
    [CATransaction commit];
    
    //NSLog(@"jsEval: %@ -> %@", script, lastJSResult_);
}

// Evaluate the given JS format string & arguments. Argument list is the same
// as [NSString stringWithFormat:...].
- (NSString *)jsEval:(NSString *)format, ... {
    if (format == nil) {
        [NSException raise:@"invalidArguments" format:@"Invalid arguments for jsEval"];
    }
    
    va_list argList;
    va_start(argList, format);
    NSString *script = [[NSString alloc] initWithFormat:format
                                               arguments:argList];
    va_end(argList);
    
    [self performSelectorOnMainThread:@selector(jsEvalInternal:)
                           withObject:script
                        waitUntilDone:YES];
    
    return [lastJSResult_ copy];
}

- (NSString *)evalFor:(int)frame jsEval:(NSString *)format, ... {
    if (format == nil) {
        [NSException raise:@"invalidArguments" format:@"Invalid arguments for jsEval"];
    }
    
    va_list argList;
    va_start(argList, format);
    NSString *script = [[NSString alloc] initWithFormat:format
                                               arguments:argList];
    va_end(argList);
    
    // check first is the frames still exist, if any of them are gone
    // automatically reset to default content
    [self performSelectorOnMainThread:@selector(jsEvalInternal:)
                           withObject:[NSString stringWithFormat:@"(function(){var w=window;var frameIndexes=[\"%i\"];for(var i=0;i<frameIndexes.length;i++){if(!(w=w.frames[frameIndexes[i]]))return true;};return false;})()",frame]
                        waitUntilDone:YES];
    if (![[lastJSResult_ copy] isEqualToString:@"true"]) {
        script = [[NSString alloc]
                  initWithFormat:@"(function(){var win=(function(){var w=window;var frameIndexes=[\"%i\"];for(var i=0;i<frameIndexes.length;i++){if(!(w=w.frames[frameIndexes[i]]))return window;};return w;})(); return win.eval('%@');})()",
                  frame,
                  // [script JSONRepresentation]
                  // It would have been so nice to just use the JSON serializer
                  // but as luck would have it, it fails to convert most of our
                  // atomized javascript code.
                  // So, resorting to a string replacement to escape certain characters
                  // to coerce into a javascript string for eval.
                  [[[[script stringByReplacingOccurrencesOfString:@"\\"    withString:@"\\\\"]
                     stringByReplacingOccurrencesOfString:@"'"     withString:@"\\'"]
                    stringByReplacingOccurrencesOfString:@"\\\\x" withString:@"\\x"]
                   stringByReplacingOccurrencesOfString:@"\n"    withString:@""]
                  ];
    }
    
    [self performSelectorOnMainThread:@selector(jsEvalInternal:)
                           withObject:script
                        waitUntilDone:YES];
    
    return [lastJSResult_ copy];
}

- (NSString *)currentTitle {
    return [self jsEval:@"document.title"];
}

- (NSString *)source {
    return [self jsEval:@"new XMLSerializer().serializeToString(document);"];
}

// Takes a screenshot.
- (UIImage *)screenshot {
    UIGraphicsBeginImageContext([[self webView] bounds].size);
    [[self webView].layer renderInContext:UIGraphicsGetCurrentContext()];
    UIImage *viewImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    // dump the screenshot into a file for debugging
    //NSString *path = [[[NSSearchPathForDirectoriesInDomains
    //   (NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0]
    //  stringByAppendingPathComponent:@"screenshot.png"] retain];
    //[UIImagePNGRepresentation(viewImage) writeToFile:path atomically:YES];
    
    return viewImage;
}

- (NSString *)URL {
    return [self jsEval:@"window.location.href"];
}

- (void)describeLastAction:(NSString *)status {
    [statusLabel_ setText:status];
}

- (CGRect)viewableArea {
    CGRect area;
    area.origin.x = [[self jsEval:@"window.pageXOffset"] intValue];
    area.origin.y = [[self jsEval:@"window.pageYOffset"] intValue];
    area.size.width = [[self jsEval:@"window.innerWidth"] intValue];
    area.size.height = [[self jsEval:@"window.innerHeight"] intValue];
    return area;
}

- (BOOL)pointIsViewable:(CGPoint)point {
    return CGRectContainsPoint([self viewableArea], point);
}

// Scroll to make the given point centered on the screen (if possible).
- (void)scrollIntoView:(CGPoint)point {
    // Webkit will clip the given point if it lies outside the window.
    // It may be necessary at some stage to do this using touches.
    [self jsEval:@"window.scroll(%f - window.innerWidth / 2, %f - window.innerHeight / 2);", point.x, point.y];
}

// Translate pixels in webpage-space to pixels in view space.
- (CGPoint)translatePageCoordinateToView:(CGPoint)point {
    CGRect viewBounds = [[self webView] bounds];
    CGRect pageBounds = [self viewableArea];
    
    // ... And then its just a linear transformation.
    float scale = viewBounds.size.width / pageBounds.size.width;
    CGPoint transformedPoint;
    transformedPoint.x = (point.x - pageBounds.origin.x) * scale;
    transformedPoint.y = (point.y - pageBounds.origin.y) * scale;
    
    NSLog(@"%@ -> %@",
          NSStringFromCGPoint(point),
          NSStringFromCGPoint(transformedPoint));
    
    return transformedPoint;
}

- (CGSize)translateSizeWithZoom:(CGSize)size {
    CGSize result = size;
    
    UIWebDocumentView *webDocument = [self.webView _documentView];
    
    result.width = size.width * [webDocument _documentScale];
    result.height = size.height * [webDocument _documentScale];
    
    return result;
}

- (void)clickOnPageElementAt:(CGPoint)point {
    if (![self pointIsViewable:point]) {
        [self scrollIntoView:point];
    }
    
    CGPoint pointInViewSpace = [self translatePageCoordinateToView:point];
    
    NSLog(@"simulating a click at %@", NSStringFromCGPoint(pointInViewSpace));
    [[self webView] simulateTapAt:pointInViewSpace];
}

// Gets the location
- (NSDictionary *)location {
    // ToDo: Handle Location
    //  MTGeoLocation *locStorage = [MTGeoLocation sharedManager];
    //  CLLocationCoordinate2D coordinate = [locStorage getCoordinate];
    //  CLLocationDistance altitude = [locStorage getAltitude];
    //
    //  return [NSDictionary dictionaryWithObjectsAndKeys:
    //          [NSDecimalNumber numberWithDouble:coordinate.longitude], kGeoLongitudeKey,
    //          [NSDecimalNumber numberWithDouble:coordinate.latitude], kGeoLatitudeKey,
    //          [NSDecimalNumber numberWithFloat:altitude], kGeoAltitudeKey, nil];
    
    return nil;
}

// Sets the location
- (void)setLocation:(NSDictionary *)dict {
    // ToDo: Handle Location
    //  NSDictionary *values = [dict objectForKey:kGeoLocationKey];
    //  NSDecimalNumber *altitude = [values objectForKey:kGeoAltitudeKey];
    //  NSDecimalNumber *longitude = [values objectForKey:kGeoLongitudeKey];
    //  NSDecimalNumber *latitude = [values objectForKey:kGeoLatitudeKey];
    //
    //  MTGeoLocation *locStorage = [MTGeoLocation sharedManager];
    //  [locStorage setCoordinate:[longitude doubleValue]
    //                   latitude:[latitude doubleValue]];
    //  [locStorage setAltitude:[altitude doubleValue]];
}

// Finds out if browser connection is alive
- (NSNumber *)isBrowserOnline {
    BOOL onlineState = [[self jsEval:@"navigator.onLine"] isEqualToString:@"true"];
    return [NSNumber numberWithBool:onlineState];
}

#pragma mark - HTML Selenium

- (void)performSelectorOnElement:(MTElement *)element selector:(SEL)selector
                      withObject:(id)value
                   waitUntilLoad:(BOOL)wait {
    
    /* The problem with this method is that the UIWebView never gives us any clear
     * indication of whether or not it's loading and if so, when its done. Asking
     * it to load causes it to begin loading sometime later (isLoading returns NO
     * for awhile.) Even the |webViewDidFinishLoad:| method isn't a sure sign of
     * anything - it will be called multiple times, once for each frame of the
     * loaded page.
     *
     * The result: The only effective method I can think of is nasty polling.
     */
    
    while ([[self webView] isLoading])
        [NSThread sleepForTimeInterval:0.01f];
    
    [element performSelectorOnMainThread:selector
                              withObject:value
                           waitUntilDone:YES];
    
    if (wait)
        [self waitForLoad];
}

//- (NSArray *) parseHtmlAtPath:(NSString *)path
//{
//    NSData *data = [[NSData alloc] initWithContentsOfFile:path];
//
//    // Create parser
//    MTTFHpple *xpathParser = [[MTTFHpple alloc] initWithHTMLData:data];
//
//    //Get all the cells of the 2nd row of the 3rd table
//    NSArray *elements  = [xpathParser searchWithXPathQuery:@"//table[1]/tbody/tr"];
//    int rowCount = [elements count];
//
//    NSMutableArray *array = [[NSMutableArray alloc] init];
//
//    for (int i = 1; i <= rowCount; i++) {
//        NSString *query = [NSString stringWithFormat:@"//table[1]/tbody/tr[%i]/td",i];
//        elements = [xpathParser searchWithXPathQuery:query];
//        NSMutableArray *rowArray = [[NSMutableArray alloc] init];
//
//        for (int j = 0; j < COLUMN_COUNT; j++) {
//            MTTFHppleElement *element = [elements objectAtIndex:j];
//
//            // Get the text within the cell tag
//            NSString *content = [element content];
//
//            if (j == ID && content) {
//                if ([content rangeOfString:@"//"].location != NSNotFound) {
//                    [rowArray addObject:@"xpath"];
//                    [rowArray addObject:content];
//                } else if ([content rangeOfString:@"link="].location != NSNotFound) {
//                    content = [content stringByReplacingOccurrencesOfString:@"link=" withString:@""];
//                    [rowArray addObject:@"linkText"];
//                    [rowArray addObject:content];
//                } else if ([content rangeOfString:@"css="].location != NSNotFound) {
//                    content = [content stringByReplacingOccurrencesOfString:@"css=" withString:@""];
//                    [rowArray addObject:@"css"];
//                    [rowArray addObject:content];
//                }
//                else if ([content rangeOfString:@"id="].location != NSNotFound) {
//                    content = [content stringByReplacingOccurrencesOfString:@"id=" withString:@""];
//                    [rowArray addObject:@"id"];
//                    [rowArray addObject:content];
//                } else {
//                    [rowArray addObject:@"id"];
//                    [rowArray addObject:content];
//                }
//            } else if (content)
//                [rowArray addObject:content];
//        }
//
//        [array addObject:rowArray];
//        [rowArray release];
//    }
//
//    return array;
//
//    [xpathParser release];
//    [data release];
//}

- (void) seleniumFile:(NSString *)fileName speed:(double)speed timeout:(double)timeout
{
    if (speed < 1000000)
        speed = 1000000;
    if (timeout < 1000000)
        timeout = 1000000;
    
    playbackSpeed = speed;
    playbackTimeout = timeout;
    
    //    commandList = [self parseHtmlAtPath:tempSt];
    commandIndex = 0;
    testComplete = nil;
    
    [self runCommandAtIndex:0];
}

- (void) performInBack:(NSString *)iString
{
    @autoreleasepool {
    
        if (playbackSpeed > 0)
            [NSThread sleepForTimeInterval:playbackSpeed/1000000];
        
        int i = [iString intValue];
        MTSession *session = [[MTSession alloc] init];
        MTHTTPVirtualDirectory *virtualDirectory = [[MTHTTPVirtualDirectory alloc] init];
        NSDictionary *response;
        MTElement *element;
        
        NSArray *commands = (NSArray *)[commandList objectAtIndex:i];
        
        NSDictionary *webElementDict = [[NSMutableDictionary alloc] init];
        NSString *command = [commands objectAtIndex:COMMAND];
        NSString *commandId = [commands objectAtIndex:ID];
        NSString *field = [commands objectAtIndex:FIELD];
        NSString *args = nil;
        NSMutableDictionary *argsDict = nil;
        SEL selector = NSSelectorFromString([NSString stringWithFormat:@"%@:",command]);
        
        if ([commands count] == 4) {
            args = [commands objectAtIndex:ARGS];
            
            if ([command isEqualToString:@"type"])
                argsDict = [NSMutableDictionary dictionaryWithObject:args forKey:@"value"];
        }
        
        [webElementDict setValue:commandId forKey:@"using"];
        [webElementDict setValue:field forKey:@"value"];
        
        
        NSDate* startTime = [NSDate dateWithTimeIntervalSinceNow:0];
        NSTimeInterval implicitWait = playbackTimeout/1000;
        response = [virtualDirectory findElement:webElementDict root:nil implicitlyWait:0];
        
        while (![response objectForKey:@"ELEMENT"]) {
            response = [virtualDirectory findElement:webElementDict root:nil implicitlyWait:0];
            
            NSDate* now = [NSDate dateWithTimeIntervalSinceNow:0];
            NSTimeInterval elapsedTime = [now timeIntervalSinceDate:startTime];
            
            if (elapsedTime > implicitWait) {
                [self throwScriptFailed:[field copy]];
                return;
            }
        }
        
        element = [MTElement elementWithId:[response objectForKey:@"ELEMENT"] andSession:session andMTWebViewController:self andCommandId:commandId];
        
        [element performSelectorOnMainThread:selector withObject:argsDict waitUntilDone:NO];
        
        commandIndex = i + 1;
        
    }
}

- (void) runCommandAtIndex:(int)i
{
    [self performSelectorInBackground:@selector(performInBack:) withObject:[NSString stringWithFormat:@"%i",i]];
}

- (void) runNextCommand
{
    if (commandIndex == [commandList count])
        testComplete = @"YES";
    else
        testComplete = nil;
    
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        //        while ([webView isLoading]) {
        //            usleep(5000);
        //        }
        
        dispatch_async(dispatch_get_main_queue(), ^{
            if (commandIndex < [commandList count])
                [self runCommandAtIndex:commandIndex];
        });
    });
}

- (void) runAgain
{
    commandIndex -= 1;
    if (commandIndex < [commandList count])
        [self runCommandAtIndex:commandIndex];
}

- (void) throwScriptFailed:(NSString *)failureString
{
    errorIndex = commandIndex;
    testComplete = failureString;
    commandIndex = [commandList count];
}

- (void) verifyWebElement:(MTElement *)element forCommand:(MTCommandEvent *)event {
    NSString *mtComponent = [MTConvertType convertedComponentFromString:event.className isRecording:YES];
    
    // Usually want to verify text value
    event.value = [element text];
    
    if ([mtComponent isEqualToString:MTComponentToggle ignoreCase:YES]) {
        // Default value for toggle (checkbox)
        if ([[element isChecked] boolValue])
            event.value = @"on";
        else
            event.value = @"off";
    }
    
    if ([event.args count] > 1) {
        NSString *attribute = [event.args objectAtIndex:1];
        
        event.value = [element attribute:attribute];
    }
    
    [MTVerifyCommand handleVerify:event];
}

- (void) getWebElement:(MTElement *)element forCommand:(MTCommandEvent *)event {
    NSString *mtComponent = [MTConvertType convertedComponentFromString:event.className isRecording:YES];
    
    // Usually want to verify text value
    event.value = [element text];
    
    if ([mtComponent isEqualToString:MTComponentToggle ignoreCase:YES]) {
        // Default value for toggle (checkbox)
        if ([[element isChecked] boolValue])
            event.value = @"on";
        else
            event.value = @"off";
    }
    
    if ([event.args count] > 1) {
        NSString *attribute = [event.args objectAtIndex:1];
        
        event.value = [element attribute:attribute];
    }
    
    [MTVerifyCommand handleVerify:event];
}

- (void) setValueForElement:(MTElement *)element forCommand:(MTCommandEvent *)event {
    NSString *mtComponent = [MTConvertType convertedComponentFromString:event.className isRecording:YES];
    
    // Usually want to verify text value
    event.value = [element text];
    
    if ([mtComponent isEqualToString:MTComponentToggle ignoreCase:YES]) {
        // Default value for toggle (checkbox)
        if ([[element isChecked] boolValue])
            event.value = @"on";
        else
            event.value = @"off";
    }
    
    if ([event.args count] > 1) {
        NSString *attribute = [event.args objectAtIndex:1];
        attribute = [attribute substringFromIndex:1];
        
        event.value = [element attribute:attribute];
    }
}

- (NSInteger) updateOrdinalForTag:(NSString *)htmlTag {
    NSInteger count = 0;
    
    count += [[webView stringByEvaluatingJavaScriptFromString:
               [NSString stringWithFormat:@"document.getElementsByTagName('%@').length",
                htmlTag]] intValue];
    
    if ([htmlTag isEqualToString:MTComponentButton ignoreCase:YES]) {
        count += [[webView stringByEvaluatingJavaScriptFromString:[NSString stringWithFormat:@"var count = 0; var inputs = document.getElementsByTagName('input'); for (var i = 0; i < inputs.length; i++) {var input = inputs[i]; if (input.getAttribute('type') == 'submit' || input.getAttribute('type') == 'reset') {count++;} return count.toString();}",htmlTag]] intValue];
    }
    
    return count;
}

- (BOOL) playBackCommandInWebView:(MTCommandEvent *)event {
    if ([MonkeyTalk sharedMonkey].currentCommand.found ||
        event.didPlayInWeb)
        return nil;
    
    BOOL isVerifyNotExistance = [event.command.lowercaseString rangeOfString:@"verifynot"].location != NSNotFound && event.args.count == 0;
    MTSeleniumCommand *selCommand = [MTSeleniumCommand initWithMTCommandEvent:event];
    selCommand.currentOrdinal = currentOrdinal;
    
    BOOL didPlay = NO;
    
    // make sure we play back to correct webview
    [MTHTTPServerController sharedInstance].viewController = self;
    
    if (selCommand.element) {
        selCommand.delegate = self;
        didPlay = YES;
        
        if ([[event.command lowercaseString] isEqualToString:@"off"]) {
            NSNumber *isChecked = [selCommand.element isChecked];
            
            // Checkbox is already off
            if (![isChecked boolValue])
                return didPlay;
        }
        
        didPlay = [selCommand playBackOnElement];
        
        if (selCommand.mtEvent.value && !event.lastResult) {
            if ([selCommand.mtEvent.command.lowercaseString rangeOfString:MTCommandVerify.lowercaseString].location != NSNotFound) {
                [MTVerifyCommand handleVerify:selCommand.mtEvent];
                
                if (selCommand.mtEvent.lastResult) {
                    selCommand.mtEvent.value = nil;
                    didPlay = NO;
                }
            }
        }
    } else if (!isVerifyNotExistance) {
        // handle in MonkeyTalk.m
    }
    
    if (!didPlay) {
        currentOrdinal = [self updateOrdinalForTag:selCommand.htmlTag];
    }
    
    event.didPlayInWeb = didPlay;
    
    return didPlay;
}

- (void) showTapAtPoint:(CGPoint)touchCenter {
    //    CGSize size = [element size];
    //    CGPoint touchCenter = [element location];
    //    size = [self translateSizeWithZoom:size];
    touchCenter = [self translatePageCoordinateToView:touchCenter];
    
    //    touchCenter.x = touchCenter.x + size.width/2;
    //    touchCenter.y = touchCenter.y + size.height/2;
    
    [self.webView showTapAtLocation:touchCenter];
}

- (CGPoint) translatePointToPage:(CGPoint)point {
    UIWebDocumentView *webDocument = [self.webView _documentView];
    float scale = [webDocument _documentScale];
    CGPoint transformedPoint;
    
    transformedPoint.x = point.x / scale;
    transformedPoint.y = point.y / scale;
    
    return transformedPoint;
}

- (void) showTapOnElement:(MTElement *)element {
    CGSize size = [element size];
    CGPoint touchCenter = [element location];
    size = [self translateSizeWithZoom:size];
    touchCenter = [self translatePageCoordinateToView:touchCenter];
    
    touchCenter.x = touchCenter.x + size.width/2;
    touchCenter.y = touchCenter.y + size.height/2;
    
    [self.webView showTapAtLocation:touchCenter];
}

- (void) highlightElement:(MTElement *)element {
    CGSize size = [element size];
    CGPoint location = [element location];
    
    size = [self translateSizeWithZoom:size];
    location = [self translatePageCoordinateToView:location];
    
    UIView *touchView = [[UIView alloc] initWithFrame:CGRectMake(location.x, location.y, size.width, size.height)];
    touchView.backgroundColor = [UIColor clearColor];
    touchView.layer.borderColor = [UIColor colorWithRed:6/255.0 green:102/255.0 blue:214/255.0 alpha:1].CGColor;
    touchView.layer.borderWidth = 4;
    
    [self.webView addSubview:touchView];
    
    [self.webView performSelector:@selector(removeView:) withObject:touchView afterDelay:1];
}

- (CGFloat) scale {
    UIWebDocumentView *webDocument = [self.webView _documentView];
    CGFloat scale = [webDocument _documentScale];
    
    return scale;
}

- (Boolean) isElementOffScreen:(MTElement *)element {
    CGRect frame = CGRectZero;
    CGSize size = [element size];
    CGPoint location = [element location];
    size = [self translateSizeWithZoom:size];
    location = [self translatePageCoordinateToView:location];
    frame.origin = location;
    frame.size = size;
    
    if (!CGRectIntersectsRect(frame, self.webView.bounds))
        return YES;
    
    return NO;
}

- (NSString *) webComponentTree {
    if (!webComponentTree) {
        isComponentLoaded = NO;
        [webView stringByEvaluatingJavaScriptFromString:@"var iframe = document.createElement(\"IFRAME\"); iframe.setAttribute(\"src\", \"monkeytalk:loadNext\"); document.documentElement.appendChild(iframe); iframe.parentNode.removeChild(iframe); iframe = null;"];
        
        //        while (!isComponentLoaded) {
        //            [NSThread sleepForTimeInterval:0.01];
        //        }
        
        NSLog(@"loaded componentTree");
    }
    
    return webComponentTree;
}

@end
