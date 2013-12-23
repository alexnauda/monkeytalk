/*  MonkeyTalk - a cross-platform functional testing tool
    Copyright (C) 2012 Gorilla Logic, Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */

#import "UIWebView+Selenium.h"
#import "MTWebViewController.h"
#import <objc/runtime.h>
#import "MonkeyTalk.h"
#import "NSString+MonkeyTalk.h"
#import "MTDefaultProperty.h"
#import "TouchSynthesis.h"
#import "MTWebTouchEventsGestureRecognizer.h"
#import "UIGestureRecognizerProxy.h"
#import "UIGestureRecognizerTargetProxy.h"
#import "UIView+MTReady.h"


@interface MTDefaultWebViewDelegate : NSObject <UIWebViewDelegate>
@end
@implementation MTDefaultWebViewDelegate
@end

@implementation UIWebView (Selenium)

- (NSString *)mtComponent {
    return MTComponentWeb;
}

+(void) load
{
    if (self == [UIWebView class]) {
        Method originalMethod = class_getInstanceMethod(self, @selector(setDelegate:));
        Method replacedMethod = class_getInstanceMethod(self, @selector(mtSetDelegate:));
        method_exchangeImplementations(originalMethod, replacedMethod);
        
        Method originalLoadRequestMethod = class_getInstanceMethod(self, @selector(loadRequest:));
        Method replacedLoadRequestMethod = class_getInstanceMethod(self, @selector(mtLoadRequest:));
        method_exchangeImplementations(originalLoadRequestMethod, replacedLoadRequestMethod);
        
        Method originalLoadHtmlMethod = class_getInstanceMethod(self, @selector(loadHTMLString:baseURL:));
        Method replacedLoadHtmlMethod = class_getInstanceMethod(self, @selector(mtLoadHTMLString:baseURL:));
        method_exchangeImplementations(originalLoadHtmlMethod, replacedLoadHtmlMethod);
        
        Method originalLoadDataMethod = class_getInstanceMethod(self, @selector(loadData:MIMEType:textEncodingName:baseURL:));
        Method replacedLoadDataMethod = class_getInstanceMethod(self, @selector(mtLoadData:MIMEType:textEncodingName:baseURL:));
        method_exchangeImplementations(originalLoadDataMethod, replacedLoadDataMethod);
    }
}

- (void) mtAssureAutomationInit {
	[super mtAssureAutomationInit];
	if (!self.delegate) {
        MTDefaultWebViewDelegate *del = [[MTDefaultWebViewDelegate alloc] init];
		self.delegate = del;
	}
}

- (void) mtSetDelegate:(NSObject <UIWebViewDelegate>*) del {
    // ignore hidden webviews
    if (self.frame.size.width == 0 || self.frame.size.height == 0)
        return [self mtSetDelegate:del];
    if ([self.delegate class] != [MTWebViewController class]) {
        MTWebViewController *webController = [[MTWebViewController alloc] init];
        
        // Set delController to call original delegate methods from webController
        webController.delController = del;
        
        // Set webView delegate to webController
        [self mtSetDelegate:webController];
        
//        self.accessibilityLabel = @"webView";
        
        // Set webController webView to user's webView
        webController.webView = self;
    } else {
        // phonegap iOS 5.1 loops forever when overriding delegate
        if ([del isKindOfClass:NSClassFromString(@"CDVLocalStorage")])
            return;
        
        MTWebViewController *webController = self.delegate;
        webController.delController = del;
    }
}

- (void)attachMonkeyTalk {
    if (CGRectEqualToRect(self.frame, CGRectZero)) {
        return;
    }
    
    if (!self.delegate || [self.delegate class] != [MTWebViewController class]) {
        MTWebViewController *webController = [[MTWebViewController alloc] init];
        NSObject <UIWebViewDelegate>*del = self.delegate;
        
        // Set delController to call original delegate methods from webController
        webController.delController = del;
        
        // Set webView delegate to webController
        [self mtSetDelegate:webController];
        webController.webView = self;
    }
}

- (void)mtLoadRequest:(NSURLRequest *)request {
    [self mtLoadRequest:request];
    [self attachMonkeyTalk];
}

- (void)mtLoadHTMLString:(NSString *)string baseURL:(NSURL *)baseURL {
    [self mtLoadHTMLString:string baseURL:baseURL];
    [self attachMonkeyTalk];
}

- (void)mtLoadData:(NSData *)data MIMEType:(NSString *)MIMEType textEncodingName:(NSString *)textEncodingName baseURL:(NSURL *)baseURL {
    [self mtLoadData:data MIMEType:MIMEType textEncodingName:textEncodingName baseURL:baseURL];
    [self attachMonkeyTalk];
}

+ (NSString *) formattedUrlString:(NSString *)url {
    if ([url rangeOfString:@"http://"].location != 0 &&
        [url rangeOfString:@"https://"].location != 0 &&
        [url rangeOfString:@"file://"].location != 0)
        url = [NSString stringWithFormat:@"http://%@",url];
    
    return url;
}

- (NSString *) valueForProperty:(NSString *)prop withArgs:(NSArray *)args {
    NSString* value;
    
    if ([prop isEqualToString:MTVerifyPropertyDefault ignoreCase:YES])
        value = [NSString stringWithFormat:@"%@",self.request.URL];
    else
        [NSException raise:@"Invalid keypath" format:@"invalid keypath"];
    
    return value;
}

- (void) playbackMonkeyEvent:(id)event {
    MTCommandEvent *commandEvent = (MTCommandEvent *)event;
    
    if ([commandEvent.command isEqualToString:MTCommandOpen ignoreCase:YES]) {
        if ([commandEvent.args count] != 1) {
            commandEvent.lastResult = [NSString stringWithFormat:@"Requires 1 argument, but has %d", [commandEvent.args count]];
            return;
        }
        
        NSString *formattedUrl = [[self class] formattedUrlString:
                                  [commandEvent.args objectAtIndex:0]];
        [self loadRequest:[NSURLRequest requestWithURL:
                           [NSURL URLWithString:formattedUrl]]];
    } else if ([commandEvent.command isEqualToString:MTCommandBack ignoreCase:YES]) {
        if (self.canGoBack)
            [self goBack];
        else
            commandEvent.lastResult = @"Browser cannot go back";
    } else if ([commandEvent.command isEqualToString:MTCommandForward ignoreCase:YES]) {
        if (self.canGoForward)
            [self goForward];
        else
            commandEvent.lastResult = @"Browser cannot go forward";
    } else if ([commandEvent.command isEqualToString:MTCommandExec ignoreCase:YES]) {
        [self stringByEvaluatingJavaScriptFromString:[commandEvent.args objectAtIndex:0]];
    } else if ([commandEvent.command isEqualToString:MTCommandExecAndReturn ignoreCase:YES]) {
        if (commandEvent.args.count != 2) {
            commandEvent.lastResult = @"ExecAndReturn requires 2 args.";
            return;
        }
        NSString *js = [commandEvent.args objectAtIndex:1];
        commandEvent.value = [self stringByEvaluatingJavaScriptFromString:js];
    } else
        [super playbackMonkeyEvent:event];
}

- (void) recordTap:(UIGestureRecognizer *)tapGesture {
    if (![MonkeyTalk sharedMonkey].isWireRecording)
        return;
    
    CGPoint location = [tapGesture locationInView:self];
    MTWebViewController *webViewController = (MTWebViewController *)self.delegate;
    
    if (![webViewController isKindOfClass:[MTWebViewController class]]) {
        NSLog(@"Unable to record.");
        return;
    }
    
    location = [webViewController translatePointToPage:location];
    
    NSString *js = [NSString stringWithFormat:@"'' + MonkeyTalk.recordTap(%f,%f); ",location.x, location.y];
    NSString *jsonString = [self stringByEvaluatingJavaScriptFromString:js];
    NSDictionary *json = [NSJSONSerialization JSONObjectWithData:[jsonString dataUsingEncoding:NSUTF8StringEncoding] options:0 error:nil];
    
    
    if (json) {
        NSString *command = [json objectForKey:@"action"] ? [json objectForKey:@"action"] : @"tap";
        NSString *component = [json objectForKey:@"component"] ? [json objectForKey:@"component"] : @"View";
        NSString *monkeyId = [json objectForKey:@"monkeyId"] ? [json objectForKey:@"monkeyId"] : @"*";
        NSString *args = [json objectForKey:@"args"] ? [json objectForKey:@"args"] : nil;
        MTCommandEvent *event = [MTCommandEvent command:command className:component monkeyID:monkeyId args:[NSArray arrayWithObject:args]];
        event.isWebRecording = YES;
        [MonkeyTalk recordEvent:event];
    }
}

- (void) playbackTapAtLocation:(CGPoint)location {
    [UIEvent performTouchInView:self at:location withCount:1];
}

@end
