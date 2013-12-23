//
//  UIViewController+MTReady.m
//  MonkeyTalk
//
//  Created by Kyle Balogh on 10/14/13.
//  Copyright (c) 2013 Gorilla Logic, Inc. All rights reserved.
//

#import "UIViewController+MTReady.h"
#import "MonkeyTalk.h"
#import "NSObject+MTReady.h"

@implementation UIViewController (MTReady)
+ (void)load {
    if (self == [UIViewController class]) {
        [NSObject swizzle:@"viewDidAppear:" with:@"mtViewDidAppear:" for:self];
    }
}

- (void)mtViewDidAppear:(BOOL)animated {
    [self mtViewDidAppear:animated];
    [MonkeyTalk sharedMonkey].isPushingController = NO;
}
@end
