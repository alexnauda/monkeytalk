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

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "UIView+MTFinder.h"
#import "MTConvertType.h"
@class MTCommandEvent;

/**
 MonkeyTalk UIView extensions provide recording and playback of Touch and Motion events. UIView subclasses can override one or more of these methods to cusotmize recording and playback logic for a class.
 */
@interface UIView (MTReady)
/** Interpret the command and generate the necessary UI events for the component.
 */
- (void) playbackMonkeyEvent:(MTCommandEvent*)event;
/**
 A string value uniquely identifying this instance of the component class
 */
- (NSString*) monkeyID;
- (NSString *) baseMonkeyID;
- (NSInteger) mtOrdinal;
- (NSArray *) rawMonkeyIDCandidates;

/**
 Returns YES if the supplied touch should be recorded. By default, returns YES if touch.phase == UITouchPhaseEnded and NO otherwise. Override this method to filter which touch events should be recorded for a class.
 */
- (BOOL) shouldRecordMonkeyTouch:(UITouch*)touch;

/**
 Evaluates touch events and records corresponding command.
 */
- (void) handleMonkeyTouchEvent:(NSSet*)touches withEvent:(UIEvent*)event;
/**
 Evaluates motion (shake) event and records corresponding command.
 */
- (void) handleMonkeyMotionEvent:(UIEvent*)event;

/**
 Returns NO if recording should be disabled for the component. By default, returns YES for UIView subclasses, but NO for UIView class instances (since these are component containers).
 */
- (BOOL) isMTEnabled;

/**
 Returns YES if this component might have been substituted for one with the supplied class name. The only known example
 of component swapping is UIToolbarTextButton which swaps with UINavigationButton on an iPad multiview controller.
 */
- (BOOL) swapsWith:(NSString*)className;

/**
 Return the corresponding UIAutomation command executable by Instruments
 */
+ (NSString*) uiAutomationCommand:(MTCommandEvent*)command;

/**
 Return the corresponding CommandEvent source executable in ObjC
 */
+ (NSString*) objcCommandEvent:(MTCommandEvent*)command;

/**
 Return the corresponding CommandEvent source executable in JS for QUnit
 */
+ (NSString*) qunitCommandEvent:(MTCommandEvent*)command;

/**
 Called to give an component's automation extension an opportunity to init itself.
 May (will) be called multiple (countless) times.
 */
- (void) mtAssureAutomationInit;

/**
 Returns YES if the view is found as a key in MonkeyTalk componentMonkeyIds dict
 so that monkeyID and ordinal are found once per view
 */
- (BOOL) hasMonkeyIdAssigned;

- (UIWebView *) currentWebView;

- (void) showTapAtLocation:(CGPoint)touchCenter;
- (void)highlightView;
- (NSString *) keyForMonkeyId;

@end
