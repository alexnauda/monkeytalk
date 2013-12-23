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

//
//  TouchSynthesis.m
//  SelfTesting
//
//  Created by Matt Gallagher on 23/11/08.
//  Copyright 2008 Matt Gallagher. All rights reserved.
//
//  Permission is given to use this source code file, free of charge, in any
//  project, commercial or otherwise, entirely at your risk, with the condition
//  that any redistribution (in part or whole) of source code must retain
//  this copyright and permission notice. Attribution in compiled projects is
//  appreciated but not required.
//
//
// Modified extensively for MonkeyTalk by Stu Stern, Gorilla Logic, November, 2009
//

//#ifdef SCRIPT_DRIVEN_TEST_MODE_ENABLED

#import "TouchSynthesis.h"
#import "MTWebTouchEventsGestureRecognizer.h"
#import <UIKit/UIGestureRecognizerSubclass.h>

@implementation UITouch (Synthesize)

- (id)initInView:(UIView *)view at:(CGPoint)point withCount:(NSInteger)count {
	self = [super init];
	if (self != nil)
	{
		
		if (![view isKindOfClass:[UIWindow class]])
		{
			point =	[view.window convertPoint:point fromView:view];
		}		
		
		_tapCount = count;
		_locationInWindow =point;
		_previousLocationInWindow = _locationInWindow;
        
        UIView *target = nil;
        
        if ([view isKindOfClass:[UITableViewCell class]]) {
            target = view;
        } else {
            target = [view.window hitTest:_locationInWindow withEvent:nil];
        }

		_window = view.window;
		_view = target;
		_phase = UITouchPhaseBegan;
		_touchFlags._firstTouchForView = 1;
		_touchFlags._isTap = 1;
		_timestamp = [NSDate timeIntervalSinceReferenceDate];
	}
	return self;
}

- (id)initInView:(UIView *)view at:(CGPoint)point {
	return [self initInView:view at:point withCount:1];
}

- (id)initInView:(UIView *)view pctRight:(CGFloat)pctRight {

	
	return [self initInView:view at:CGPointMake(
												pctRight * view.frame.size.width,
												0.5 * view.frame.size.height)];												
//										  frameInWindow.origin.x + pctRight * frameInWindow.size.width,
//										  frameInWindow.origin.y + 0.5 * frameInWindow.size.height)];
}

- (id)initInView:(UIView *)view {
	return [self initInView:view pctRight:0.5];
}


//
// setPhase:
//
// Setter to allow access to the _phase member.
//
- (void)setThePhase:(UITouchPhase)phase
{
	_phase = phase;
	_timestamp = [NSDate timeIntervalSinceReferenceDate];
}

//
// setPhase:
//
// Setter to allow access to the _locationInWindow member.
//
- (void)setLocationInWindow:(CGPoint)location
{
	_previousLocationInWindow = _locationInWindow;
	_locationInWindow = location;
	_timestamp = [NSDate timeIntervalSinceReferenceDate];
}

@end

//
// GSEvent is an undeclared object. We don't need to use it ourselves but some
// Apple APIs (UIScrollView in particular) require the x and y fields to be present.
//
@interface GSEventProxy : NSObject
{
@public
	unsigned int flags;
	unsigned int type;
	unsigned int ignored1;
	float x1;
	float y1;
	float x2;
	float y2;
	unsigned int ignored2[10];
	unsigned int ignored3[7];
	float sizeX;
	float sizeY;
	float x3;
	float y3;
	unsigned int ignored4[3];
}
@end
@implementation GSEventProxy
@end

//
// PublicEvent
//
// A dummy class used to gain access to UIEvent's private member variables.
// If UIEvent changes at all, this will break.
//
@interface PublicEvent : NSObject
{
@public
    GSEventProxy           *_event;
    NSTimeInterval          _timestamp;
    NSMutableSet           *_touches;
    CFMutableDictionaryRef  _keyedTouches;
}
@end

@implementation PublicEvent
@end

@interface UIEvent (Creation)

- (id)_initWithEvent:(GSEventProxy *)fp8 touches:(id)fp12;

@end

//
// UIEvent (Synthesize)
//
// A category to allow creation of a touch event.
//
@implementation UIEvent (Synthesize)

- (id)initWithTouch:(UITouch *)touch
{
	CGPoint location = [touch locationInView:touch.window];
	GSEventProxy *gsEventProxy = [[GSEventProxy alloc] init];
	gsEventProxy->x1 = location.x;
	gsEventProxy->y1 = location.y;
	gsEventProxy->x2 = location.x;
	gsEventProxy->y2 = location.y;
	gsEventProxy->x3 = location.x;
	gsEventProxy->y3 = location.y;
	gsEventProxy->sizeX = 1.0;
	gsEventProxy->sizeY = 1.0;
	gsEventProxy->flags = ([touch phase] == UITouchPhaseEnded) ? 0x1010180 : 0x3010180;
	gsEventProxy->type = 3001;	
	
	//
	// On SDK versions 3.0 and greater, we need to reallocate as a
	// UITouchesEvent.
	//
	Class touchesEventClass = objc_getClass("UITouchesEvent");
	if (touchesEventClass && ![[self class] isEqual:touchesEventClass])
	{
		self = [touchesEventClass alloc];
	}
	
	self = [self _initWithEvent:gsEventProxy touches:[NSSet setWithObject:touch]];
	if (self != nil)
	{
	}
	return self;
}
+ (void)performTouch:(UITouch*)touch {
	UIEvent *eventDown = [[UIEvent alloc] initWithTouch:touch];
    BOOL didPlay = NO;

    for (UIGestureRecognizer* gr in touch.view.gestureRecognizers) {
        // This is just a first quick hack at handling gesture recognizers
        // It only handles a single UITapGestureRecognizer
        if ([gr isKindOfClass:objc_getClass("UIWebTouchEventsGestureRecognizer")]) {
            MTWebTouchEventsGestureRecognizer *webRecognizer = (MTWebTouchEventsGestureRecognizer *)gr;
            [webRecognizer _processTouches:[eventDown allTouches] withEvent:eventDown type:6];
            [touch setThePhase:UITouchPhaseEnded];
            UIEvent *eventUp = [[UIEvent alloc] initWithTouch:touch];
            [webRecognizer _processTouches:[eventUp allTouches] withEvent:eventUp type:8];
        }
        
        if ([gr isKindOfClass:[UITapGestureRecognizer class]]) {
            [gr touchesBegan:[eventDown allTouches] withEvent:eventDown];
            [touch setThePhase:UITouchPhaseEnded];
            UIEvent *eventUp = [[UIEvent alloc] initWithTouch:touch]; 
            [gr touchesEnded:[eventUp allTouches] withEvent:eventUp];
            
            didPlay = YES;
            if (gr.cancelsTouchesInView) {
                //[eventDown release];
                return;
            }
            
        }
    }
    

	[touch.view touchesBegan:[eventDown allTouches] withEvent:eventDown];
	[[UIApplication sharedApplication] sendEvent:eventDown];
	[touch setThePhase:UITouchPhaseEnded];
	UIEvent *eventUp = [[UIEvent alloc] initWithTouch:touch];
	[touch.view touchesEnded:[eventUp allTouches] withEvent:eventUp];
	[[UIApplication sharedApplication] sendEvent:eventUp];
	

}

+ (void)performTouchInView:(UIView *)view pctRight:(CGFloat)pctRight {
	UITouch *touch = [[UITouch alloc] initInView:view pctRight:pctRight];
	[self performTouch:touch];
}
	
+ (void)performTouchInView:(UIView *)view {
	[self performTouchInView:view pctRight:0.5];
}


+ (void)performTouchLeftInView:(UIView *)view {
	[self performTouchInView:view pctRight:0.16];
}


+ (void)performTouchRightInView:(UIView *)view {
	[self performTouchInView:view pctRight:0.84];
}

+ (void)performTouchInView:(UIView *)view at:(CGPoint)point {
	[self performTouchInView:view at:point withCount:1];
}
+ (void)performTouchInView:(UIView *)view at:(CGPoint)point withCount:(NSInteger)count {
	UITouch *touch = [[UITouch alloc] initInView:view at:point withCount:count];
	[self performTouch:touch];
}

+ (void)performTouchInView:(UIView *)view at:(CGPoint)point forPhase:(UITouchPhase)phase {
	UITouch *touch = [[UITouch alloc] initInView:view at:point];
	UIEvent *event = [[UIEvent alloc] initWithTouch:touch];
    [touch setThePhase:UITouchPhaseEnded];
	[[UIApplication sharedApplication] sendEvent:event];
}


+ (void)performMoveInView:(UIView *)view from:(CGPoint)from to:(CGPoint)to {
//	UITouch *touch = [[UITouch alloc] initInView:view at:from];
//	to = [view convertPoint:to toView:nil];	
//	[touch setLocationInWindow:to]; // sets previous location to from
//	UIEvent *event = [[UIEvent alloc] initWithTouch:touch];
//    [touch setThePhase:UITouchPhaseEnded];
//	
//	[[UIApplication sharedApplication] sendEvent:event];
//	[touch release];
//	[event release];
//    
//    NSLog(@"Simulate Swipe");
//     NSLog(@"FoundView: %@",view);
//    
    
    // Do not call touch down when move is called
    UITouch *touch = [[UITouch alloc] initInView:view at:from];
    UIEvent *eventDown = [[UIEvent alloc] initWithTouch:touch];
//    [touch.view touchesBegan:[eventDown allTouches] withEvent:eventDown];
//    //	[[UIApplication sharedApplication] sendEvent:eventDown];
//	[touch setThePhase:UITouchPhaseEnded];
    
    UITouch *touchEnd = [[UITouch alloc] initInView:view at:to];
    UIEvent *eventMove = [[UIEvent alloc] initWithTouch:touchEnd];
    [touchEnd.view touchesMoved:[eventDown allTouches] withEvent:eventMove];
    //	[[UIApplication sharedApplication] sendEvent:eventMove];
    
    //	UIEvent *eventUp = [[UIEvent alloc] initWithTouch:touchEnd];
    //	[touchEnd.view touchesCancelled:[eventUp allTouches] withEvent:eventUp];
    //	//[[UIApplication sharedApplication] sendEvent:eventUp];
    //    [touchEnd setThePhase:UITouchPhaseEnded];
	
    //	[eventUp release];
	
}

+ (void) performSwipeInView:(UIView *)view from:(CGPoint)from to:(CGPoint)to {
    UITouch *touchEnd = [[UITouch alloc] initInView:view at:to];
    UIEvent *eventMove = [[UIEvent alloc] initWithTouch:touchEnd];
    [touchEnd.view touchesMoved:[eventMove allTouches] withEvent:eventMove];
}

+ (void) performTouchBeganInView:(UIView *)view atPoint:(CGPoint)point {
    UITouch *touch = [[UITouch alloc] initInView:view at:point];
    UIEvent *eventDown = [[UIEvent alloc] initWithTouch:touch];
    [touch.view touchesBegan:[eventDown allTouches] withEvent:eventDown];
}

+ (void) performTouchCancelledInView:(UIView *)view atPoint:(CGPoint)point {
    UITouch *touch = [[UITouch alloc] initInView:view at:point];
    UIEvent *eventDown = [[UIEvent alloc] initWithTouch:touch];
    [touch.view touchesCancelled:[eventDown allTouches] withEvent:eventDown];
}


+ (void)performTouchDownInView:(UIView *)view at:(CGPoint)point {
	[self performTouchInView:view at:point forPhase:UITouchPhaseBegan];
	
}


+ (void)performTouchUpInView:(UIView *)view at:(CGPoint)point {
	[self performTouchInView:view at:point forPhase:UITouchPhaseEnded];
	
}

@end

//#endif // SCRIPT_DRIVEN_TEST_MODE_ENABLED

