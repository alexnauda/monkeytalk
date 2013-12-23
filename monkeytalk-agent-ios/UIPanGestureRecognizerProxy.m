//
//  UIPanGestureRecognizerProxy.m
//  MonkeyTalk
//
//  Created by Kyle Balogh on 4/23/13.
//  Copyright (c) 2013 Gorilla Logic, Inc. All rights reserved.
//

#import "UIPanGestureRecognizerProxy.h"
#import "TouchSynthesis.h"

@implementation UIPanGestureRecognizerProxy
@synthesize location, mtView;

- (CGPoint)locationInView:(UIView *)view {
    CGPoint point = [mtView convertPoint:location toView:view];
    return point;
}

@end
