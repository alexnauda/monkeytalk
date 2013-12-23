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
#import <UIKit/UISwitch.h>

@interface _UISwitchInternalViewNeueStyle1: UIView

@end
@interface _UISwitchInternalViewNeueStyle1 (MTReady)

@end

@interface UISwitch (MTReady) 
- (NSString *) valueForProperty:(NSString *)prop withArgs:(NSArray *)args;
- (void) handleSwitchTouchEvent:(NSSet*)touches withEvent:(UIEvent*)event;
- (void) playbackSwitchEvent:(id)event;
- (void)recordSwitchTap;
- (void)handleSwitchGesture:(UIGestureRecognizer *)recognizer;
+ (UISwitch *)parentSwitchFromInternalView:(UIView *)view;
@end
