//
//  UIGestureRecognizer.h
//  UIKit
//
//  Copyright 2008-2010 Apple Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreGraphics/CoreGraphics.h>
#import <UIKit/UIKitDefines.h>
#import <UIKit/UIKit.h>

@protocol UIGestureRecognizerDelegate;
@class UIView, UIEvent, UITouch;

@interface UIGestureRecognizerProxy : NSObject {
//NS_CLASS_AVAILABLE_IOS(3_2) @interface UIGestureRecognizerProxy : NSObject {
    @public
    NSMutableArray                   *_targets;
    NSMutableArray                   *_delayedTouches;
    UIView                           *__unsafe_unretained _view;
    UIEvent                          *_updateEvent;
    
    id <UIGestureRecognizerDelegate>  __unsafe_unretained _delegate;
    
    NSMutableSet                     *_failureRequirements;
    NSMutableSet                     *_failureDependents;
    NSMutableSet                     *_dynamicFailureRequirements;
    NSMutableSet                     *_dynamicFailureDependents;
    NSMutableSet                     *_unfailedGestures;
    NSMutableSet                     *_unfailedGesturesForReset;
    
    NSMutableSet                     *_friends;
    
    UIGestureRecognizerState          _state;
    
    struct {
        unsigned int delegateShouldBegin:1;
        unsigned int delegateCanPrevent:1;
        unsigned int delegateCanBePrevented:1;
        unsigned int delegateShouldRecognizeSimultaneously:1;
        unsigned int delegateShouldReceiveTouch:1;
        unsigned int delegateShouldRequireFailure:1;
        unsigned int delegateFailed:1;
        unsigned int privateDelegateShouldBegin:1;
        unsigned int privateDelegateShouldRecognizeSimultaneously:1;
        unsigned int privateDelegateShouldReceiveTouch:1;
        unsigned int subclassShouldRequireFailure:1;
        unsigned int cancelsTouchesInView:1;
        unsigned int delaysTouchesBegan:1;
        unsigned int delaysTouchesEnded:1;
        unsigned int notExclusive:1;
        unsigned int disabled:1;
        unsigned int dirty:1;
        unsigned int queriedFailureRequirements:1;
        unsigned int delivered:1;
        unsigned int continuous:1;
        unsigned int requiresDelayedBegan:1;
    } _gestureFlags;
}

// Valid action method signatures:
//     -(void)handleGesture;
//     -(void)handleGesture:(UIGestureRecognizer*)gestureRecognizer;
- (id)initWithTarget:(id)target action:(SEL)action; // default initializer

- (void)addTarget:(id)target action:(SEL)action;    // add a target/action pair. you can call this multiple times to specify multiple target/actions
- (void)removeTarget:(id)target action:(SEL)action; // remove the specified target/action pair. passing nil for target matches all targets, and the same for actions

@property(nonatomic,readonly) UIGestureRecognizerState state;  // the current state of the gesture recognizer

@property(nonatomic,unsafe_unretained) id <UIGestureRecognizerDelegate> delegate; // the gesture recognizer's delegate

@property(nonatomic, getter=isEnabled) BOOL enabled;  // default is YES. disabled gesture recognizers will not receive touches. when changed to NO the gesture recognizer will be cancelled if it's currently recognizing a gesture

// a UIGestureRecognizer receives touches hit-tested to its view and any of that view's subviews
@property(unsafe_unretained, nonatomic,readonly) UIView *view;           // the view the gesture is attached to. set by adding the recognizer to a UIView using the addGestureRecognizer: method

@property(nonatomic) BOOL cancelsTouchesInView;       // default is YES. causes touchesCancelled:withEvent: to be sent to the view for all touches recognized as part of this gesture immediately before the action method is called
@property(nonatomic) BOOL delaysTouchesBegan;         // default is NO.  causes all touch events to be delivered to the target view only after this gesture has failed recognition. set to YES to prevent views from processing any touches that may be recognized as part of this gesture
@property(nonatomic) BOOL delaysTouchesEnded;         // default is YES. causes touchesEnded events to be delivered to the target view only after this gesture has failed recognition. this ensures that a touch that is part of the gesture can be cancelled if the gesture is recognized

// create a relationship with another gesture recognizer that will prevent this gesture's actions from being called until otherGestureRecognizer transitions to UIGestureRecognizerStateFailed
// if otherGestureRecognizer transitions to UIGestureRecognizerStateRecognized or UIGestureRecognizerStateBegan then this recognizer will instead transition to UIGestureRecognizerStateFailed
// example usage: a single tap may require a double tap to fail
- (void)requireGestureRecognizerToFail:(UIGestureRecognizer *)otherGestureRecognizer;

// individual UIGestureRecognizer subclasses may provide subclass-specific location information. see individual subclasses for details
- (CGPoint)locationInView:(UIView*)view;                                // a generic single-point location for the gesture. usually the centroid of the touches involved

- (NSUInteger)numberOfTouches;                                          // number of touches involved for which locations can be queried
- (CGPoint)locationOfTouch:(NSUInteger)touchIndex inView:(UIView*)view; // the location of a particular touch

@end
