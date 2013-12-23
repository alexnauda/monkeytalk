//
//  UIPanGestureRecognizer.h
//  UIKit
//
//  Copyright (c) 2008-2012, Apple Inc. All rights reserved.
//
#import <UIKit/UIKit.h>
#import "UIGestureRecognizerProxy.h"
#import <UIKit/UIGestureRecognizerSubclass.h>

@interface UIPanGestureRecognizerProxy : UIPanGestureRecognizer {
    CGPoint location;
}

@property (nonatomic, readwrite) CGPoint location;
@property (unsafe_unretained, nonatomic, readwrite) UIView *mtView;

@end