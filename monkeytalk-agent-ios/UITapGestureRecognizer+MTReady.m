//
//  UITapGestureRecognizer+MTReady.m
//  MonkeyTalk
//
//  Created by Kyle Balogh on 3/21/13.
//  Copyright (c) 2013 Gorilla Logic, Inc. All rights reserved.
//

#import "UITapGestureRecognizer+MTReady.h"
#import <objc/runtime.h>
#import "MonkeyTalk.h"
#import "MTUtils.h"
#import "UIWebView+Selenium.h"

@implementation UITapGestureRecognizer (MTReady)
+ (void)load {
    if (self == [UITapGestureRecognizer class]) {
        Method originalMethod = class_getInstanceMethod(self, @selector(touchesEnded:withEvent:));
        Method replacedMethod = class_getInstanceMethod(self, @selector(mttouchesEnded:withEvent:));
        method_exchangeImplementations(originalMethod, replacedMethod);
    }
}

- (void) mttouchesEnded:(NSSet *)touches withEvent:(UIEvent *)event {
    // need to record tap gesture here to capture recording for jquery (before elementAtPoint changes)
    if ([self.view isKindOfClass:objc_getClass("UIWebBrowserView")] && [self class] == [UITapGestureRecognizer class]) {
        BOOL shouldRecord = NO;
        for (UIGestureRecognizer *recognizer in self.view.gestureRecognizers) {
            if ([recognizer isKindOfClass:objc_getClass("UIWebTouchEventsGestureRecognizer")]) {
                if (recognizer.state != UIGestureRecognizerStateEnded)
                    shouldRecord = YES;
            }
        }
        
        if (shouldRecord && self.numberOfTapsRequired == 1) {
            UIWebView *webView = [MTUtils currentWebView];

            if (webView)
                [webView recordTap:self];
        }
    }
    [self mttouchesEnded:touches withEvent:event];
}

@end
