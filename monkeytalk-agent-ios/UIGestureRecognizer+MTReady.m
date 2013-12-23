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

#import "UIGestureRecognizer+MTReady.h"
#import "MonkeyTalk.h"
#import "MTCommandEvent.h"
#import "MTUtils.h"
#import <objc/runtime.h>
#import "MTUtils.h"
#import "UIView+MTReady.h"
#import "UIGestureRecognizerProxy.h"
#import "NSString+MonkeyTalk.h"
#import "MTConvertType.h"
#import "UIGestureRecognizerProxy.h"
#import "UIGestureRecognizerTargetProxy.h"
#import "UITableView+MTReady.h"
#import "UIWebView+Selenium.h"
#import "MTUtils.h"
#import "MTWebTouchEventsGestureRecognizer.h"
#import "UIPanGestureRecognizerProxy.h"
#import "UILongPressGestureRecognizerProxy.h"
#import "UISwitch+MTReady.h"

@interface MTDefaultGestureRecognizerDelegate : NSObject <UIGestureRecognizerDelegate>
@end
@implementation MTDefaultGestureRecognizerDelegate
@end

@implementation UIGestureRecognizer (MTReady)
NSMutableArray *panArgs;

+ (void)load {
    if (self == [UIGestureRecognizer class]) {
        Method originalMethod = class_getInstanceMethod(self, @selector(initWithTarget:action:));
        Method replacedMethod = class_getInstanceMethod(self, @selector(mtinitWithTarget:action:));
        method_exchangeImplementations(originalMethod, replacedMethod);
        panArgs = [[NSMutableArray alloc] init];
    }
}

+ (NSString *) directionForInt:(NSInteger)direction {
    NSString *directionString;
    if (direction == UISwipeGestureRecognizerDirectionUp)
        directionString = MTSwipeDirectionUp;
    else if (direction == UISwipeGestureRecognizerDirectionDown)
        directionString = MTSwipeDirectionDown;
    else if (direction == UISwipeGestureRecognizerDirectionLeft)
        directionString = MTSwipeDirectionLeft;
    else
        directionString = MTSwipeDirectionRight;
    
    return directionString;
}

+(void) playbackMonkeyEvent:(id)event {
    
    MTCommandEvent *ev = (MTCommandEvent *)event;
    UIView *gestureView = ev.source;
    NSString *component = [MTConvertType convertedComponentFromString:ev.className isRecording:YES];
    
    if ([gestureView.gestureRecognizers count] == 0) {
        ev.lastResult = [NSString stringWithFormat:@"%@ with monkeyID %@ does not have any gesture recognizers (make sure you are using the correct monkeyID).",component,ev.monkeyID];
        return;
    }
    
    if ([ev.command isEqualToString:MTCommandLongPress ignoreCase:YES] ||
        [ev.command isEqualToString:MTCommandLongSelectIndex ignoreCase:YES]) {
        NSArray *playbackStates = [NSArray arrayWithObjects:[NSNumber numberWithInteger:UIGestureRecognizerStateBegan], [NSNumber numberWithInteger:UIGestureRecognizerStateEnded], nil];
        NSInteger pressRecognizerCount = 0;
        for (UIGestureRecognizer* gr in gestureView.gestureRecognizers) {
            if ([gr isKindOfClass:[UILongPressGestureRecognizer class]]) {
                pressRecognizerCount ++;
                
                for (NSNumber *state in playbackStates) {
                    UIGestureRecognizerProxy *grProxy = (UIGestureRecognizerProxy *)gr;
                    UILongPressGestureRecognizerProxy *longPress = [[UILongPressGestureRecognizerProxy alloc] init];
                    longPress.state = [state integerValue];
                    
                    for (int i = 0; i < [grProxy->_targets count]; i++) {
                        UIGestureRecognizerTargetProxy *targetProxy = [grProxy->_targets objectAtIndex:i];
                        
                        id target = targetProxy->_target;
                        SEL selector = targetProxy->_action;
                        
                        [target performSelectorOnMainThread:selector withObject:longPress waitUntilDone:NO];
                        
                    }
                }
            }
        }
        
        if (pressRecognizerCount == 0)
            ev.lastResult = [NSString stringWithFormat:@"%@ with monkeyID %@ does not have any long press gesture recognizers (make sure you are using the correct monkeyID).",component,ev.monkeyID];
    }
    
    else if ([ev.command isEqualToString: MTCommandDoubleTap ignoreCase:YES])
    {
        NSUInteger doubleTapGestureCount =0;
        for (UIGestureRecognizer *gr in gestureView.gestureRecognizers) {
            if ([gr isKindOfClass:[UITapGestureRecognizer class ]]) {
                doubleTapGestureCount++;
                UIGestureRecognizerProxy *grProxy = (UIGestureRecognizerProxy *)gr;
                UITapGestureRecognizer *tapGesture = (UITapGestureRecognizer *) gr;
                for(int i=0; i < [grProxy->_targets count];i++)
                {
                    UIGestureRecognizerTargetProxy *targetProxy = [grProxy->_targets objectAtIndex:i];
                    
                    id target  = targetProxy->_target;
                    SEL selector = targetProxy->_action;
                    
                    [target performSelectorOnMainThread:selector withObject:tapGesture waitUntilDone:NO];
                }
            }
        }
        if (doubleTapGestureCount == 0)
            ev.lastResult = [NSString stringWithFormat:@"%@ with monkeyID %@ does not have any double tap gesture recognizers (make sure you are using the correct monkeyID).",component,ev.monkeyID];
    }
    else if ([ev.command isEqualToString: MTCommandDrag ignoreCase:YES])
    {
        for (UIGestureRecognizer *gr in gestureView.gestureRecognizers) {
            if ([gr isKindOfClass:[UIPanGestureRecognizer class ]]) {
                
                int startX1 = [[ev.args objectAtIndex:0] integerValue];
                int startY1 = [[ev.args objectAtIndex:1] integerValue];
                int stopX2 = [[ev.args objectAtIndex:2] integerValue];
                int stopY2 = [[ev.args objectAtIndex:3] integerValue];
                
                UIGestureRecognizerProxy *grProxy = (UIGestureRecognizerProxy *)gr;
                UIPanGestureRecognizerProxy *panProxy = [[UIPanGestureRecognizerProxy alloc] init];
                panProxy.state = UIGestureRecognizerStateBegan;
                panProxy.location = CGPointMake(startX1, startY1);
                panProxy.mtView = grProxy.view;
                for(int i=0; i < [grProxy->_targets count];i++)
                {
                    UIGestureRecognizerTargetProxy *targetProxy = [grProxy->_targets objectAtIndex:i];
                    
                    id target  = targetProxy->_target;
                    SEL selector = targetProxy->_action;
                    if (![target isKindOfClass:[UIScrollView class]]) {
                        [target performSelectorOnMainThread:selector withObject:panProxy waitUntilDone:YES];
                        
                        int distX1 = startX1;
                        int distY1 = startY1;
                        
                        if (distY1 < stopY2) {
                            while(distY1 < stopY2) {
                                if (distX1 < stopX2) {
                                    distX1++;
                                }
                                else if (distX1 > stopX2 ) {
                                    distX1--;
                                }
                                distY1++;
                                panProxy.state = UIGestureRecognizerStateChanged;
                                panProxy.location = CGPointMake(distX1, distY1);
                                [target performSelectorOnMainThread:selector withObject:panProxy waitUntilDone:YES];
                                continue;
                            }
                        }
                        
                        else if (distY1 > stopY2) {
                            while(distY1 > stopY2) {
                                if (distX1 < stopX2) {
                                    distX1++;
                                }
                                else if (distX1 > stopX2 ) {
                                    distX1--;
                                }
                                distY1--;
                                panProxy.state = UIGestureRecognizerStateChanged;
                                panProxy.location = CGPointMake(distX1, distY1);
                                [target performSelectorOnMainThread:selector withObject:panProxy waitUntilDone:YES];
                                continue;
                            }
                        }
                        
                        // Invoking Gesutre End state.
                        panProxy.state = UIGestureRecognizerStateEnded;
                        panProxy.location = CGPointMake(stopX2, stopY2);
                        [target performSelectorOnMainThread:selector withObject:panProxy waitUntilDone:YES];
                    }
                }
            }
        }
    }
    
}

- (void) mtreplaceAction:(UIGestureRecognizer *)recognizer {
    // Ignore web components
    BOOL isWebTouch = [recognizer isKindOfClass:objc_getClass("UIWebTouchEventsGestureRecognizer")];
    BOOL isTapInWebView = [recognizer.view isKindOfClass:objc_getClass("UIWebBrowserView")] && [recognizer isKindOfClass:[UITapGestureRecognizer class]];
    BOOL isLongPressInWebView = [recognizer.view isKindOfClass:objc_getClass("UIWebBrowserView")] && [recognizer isKindOfClass:[UILongPressGestureRecognizer class]];
    
    if ([recognizer.view isKindOfClass:objc_getClass("_UISwitchInternalViewNeueStyle1")]) {
        UISwitch *parentSwitch = [UISwitch parentSwitchFromInternalView:recognizer.view];
        
        if (parentSwitch) {
            return [parentSwitch handleSwitchGesture:recognizer];
        }
    }
    
    if (isWebTouch || isTapInWebView || isLongPressInWebView) {
        BOOL shouldRecord = NO;
        
        if (isWebTouch) {
            MTWebTouchEventsGestureRecognizer *webRecognizer = (MTWebTouchEventsGestureRecognizer *)recognizer;
            
            NSString *type = [NSString stringWithFormat:@"%@",webRecognizer._typeDescription];
            if ([type isEqualToString:@"WebEventTouchEnd"]) {
                shouldRecord = YES;
                
                for (UITapGestureRecognizer *tap in self.view.gestureRecognizers) {
                    if ([tap isKindOfClass:[UITapGestureRecognizer class]]) {
                        if (tap.state != UIGestureRecognizerStatePossible)
                            shouldRecord = NO;
                    }
                }
            }
        } else if (isTapInWebView) {
            // handle in UITapGestureRecognizer+MTReady category
            //            UITapGestureRecognizer *tapGesture = (UITapGestureRecognizer *)recognizer;
            //
            //            if (tapGesture.numberOfTapsRequired == 1)
            //                shouldRecord = YES;
        } else if (isLongPressInWebView) {
            if (recognizer.state == UIGestureRecognizerStateEnded) {
                shouldRecord = YES;
            }
        }
        
        UIWebView *webView = [MTUtils currentWebView];
        
        if (webView && shouldRecord) {
            [webView recordTap:recognizer];
        }
        
        return;
    }
    
    NSString *objectString = [NSString stringWithFormat:@"%@",recognizer];
    NSArray *replaceStrings = [NSArray arrayWithObjects:@"\n",@"\"",@" ",@"(",@")", nil];
    NSArray *args = nil;
    NSString *command = nil;
    
    for (NSString *string in replaceStrings)
        objectString = [objectString stringByReplacingOccurrencesOfString:string withString:@""];
    
    
    NSArray *array = [objectString componentsSeparatedByString:@";"];
    
    for (__strong NSString *string in array) {
        if ([string rangeOfString:@"target="].location != NSNotFound) {
            string = [string stringByReplacingOccurrencesOfString:@"=<" withString:@","];
        }
    }
    
    if ([self isKindOfClass:[UISwipeGestureRecognizer class]]) {
        UISwipeGestureRecognizer *swipeRecognizer = (UISwipeGestureRecognizer *)recognizer;
        
        NSString *directionString = [[self class] directionForInt:swipeRecognizer.direction];
        args = [NSArray arrayWithObject:directionString];
        command = MTCommandSwipe;
        [MonkeyTalk recordFrom:recognizer.view
                       command:command args:args];
        return;
        
    } else if ([self isKindOfClass:[UIPinchGestureRecognizer class]]) {
        UIPinchGestureRecognizer *pinchRecognizer = (UIPinchGestureRecognizer *)recognizer;
        
        NSString *scaleString = [NSString stringWithFormat:@"%0.2f",pinchRecognizer.scale];
        NSString *velocityString = [NSString stringWithFormat:@"%0.2f",pinchRecognizer.velocity];
        args = [NSArray arrayWithObjects:scaleString, velocityString, nil];
        command = MTCommandPinch;
        [MonkeyTalk recordFrom:recognizer.view
                       command:command args:args];
        return;
        
    } else if ([self isKindOfClass:[UILongPressGestureRecognizer class]]) {
        UILongPressGestureRecognizer *longGesure = (UILongPressGestureRecognizer *) recognizer;
        CGPoint touchPoint;
        if ([self.view isKindOfClass:[UITableView class]])
        {
            UITableView *mtTableView = self.view;
            touchPoint = [longGesure locationInView:mtTableView];
            NSIndexPath *indexPath =  [mtTableView indexPathForRowAtPoint:touchPoint];
            NSString *selectedRowId = [NSString stringWithFormat:@"%d",[indexPath row]+1];
            args = [NSArray arrayWithObject:selectedRowId];
            command = MTCommandLongSelectIndex;
        }
        else if ([self.view isKindOfClass:[UIPickerView class]]) {
            //Logical Code goes here
        }
        else
            command = MTCommandLongPress;
        
        [MonkeyTalk recordFrom:recognizer.view
                       command:command args:args];
        return;
        
    } else if ([self isKindOfClass:[UIPanGestureRecognizer class]]) {
        if ([self shouldIgnoreGesture])
            return;
        
        UIPanGestureRecognizer *panGesture = (UIPanGestureRecognizer *)recognizer;
        CGPoint translatePoint = [panGesture locationInView:self.view.superview];
        
        if (recognizer.state == UIGestureRecognizerStateBegan) {
            [panArgs addObjectsFromArray:[NSArray arrayWithObjects:[NSString stringWithFormat:@"%1.0f", translatePoint.x],
                                          [NSString stringWithFormat:@"%1.0f",translatePoint.y], nil]];
        }  else if (recognizer.state == UIGestureRecognizerStateEnded) {
            [panArgs addObjectsFromArray:[NSArray arrayWithObjects:[NSString stringWithFormat:@"%1.0f", translatePoint.x],
                                          [NSString stringWithFormat:@"%1.0f",translatePoint.y], nil]];
            
            [ MonkeyTalk recordEvent:[[MTCommandEvent alloc]
                                      init:MTCommandDrag className:[NSString stringWithUTF8String:class_getName([recognizer.view class])]
                                      monkeyID:[recognizer.view monkeyID]
                                      args:[NSArray arrayWithArray:panArgs]]];
            [panArgs removeAllObjects];
            return;
        }
    }
    //    [MonkeyTalk recordFrom:recognizer.view
    //                   command:command args:args];
    //    [self mtreplaceAction];
}

- (BOOL)shouldIgnoreGesture {
    // ignore scrollview pan gestures
    BOOL isScrollViewPan = [self isKindOfClass:objc_getClass("UIScrollViewPanGestureRecognizer")];
    BOOL isSwitch = [self.view isKindOfClass:[UISwitch class]] || [self.view isKindOfClass:objc_getClass("_UISwitchInternalView")];
    
    if (isScrollViewPan || isSwitch)
        return YES;

    return NO;
}

- (id) mtinitWithTarget:(id)target action:(SEL)action {
    [self mtinitWithTarget:target action:action];
    
    //    NSLog(@"GestureClasses: %@",[self class]);
    
    if ([self isKindOfClass:[UISwipeGestureRecognizer class]] ||
        [self isKindOfClass:[UIPinchGestureRecognizer class]] ||
        [self isKindOfClass:[UILongPressGestureRecognizer class]] ||
        [self isKindOfClass: [UITapGestureRecognizer class]] ||
        [self isKindOfClass:[UIPanGestureRecognizer class]]  ||
        [self isKindOfClass:objc_getClass("UIWebTouchEventsGestureRecognizer")]){
        [self addTarget:self action:@selector(mtreplaceAction:)];
    }
    
    return self;
}
@end
