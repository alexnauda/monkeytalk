//
//  UIPinchGestureRecognizerProxy.m
//  MonkeyTalk
//
//  Created by Kyle Balogh on 3/8/12.
//  Copyright 2012 Gorilla Logic, Inc. All rights reserved.
//

#import "UIPinchGestureRecognizerProxy.h"


@implementation UIPinchGestureRecognizerProxy
@synthesize scale, velocity;

- (id) initWithVelocity:(float)v forGesture:(UIGestureRecognizer *)gr {
    self = [self init];
    self.velocity = v;
    return self;
}

@end
