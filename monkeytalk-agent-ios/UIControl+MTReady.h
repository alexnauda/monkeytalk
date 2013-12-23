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
#import <UIKit/UIControl.h>

/**
 MonkeyTalk UIControl event handling extensions. Override to modify UIControl recording of UIControlEvents.
 */
@interface UIControl (MTReady)

/**
 The events to be recorded for this UIControl class. Defaults to none.
 */
- (UIControlEvents)monkeyEventsToHandle;

/**
 Prepare a UIControlEvent event for recording.
 */
- (void) handleMonkeyEventFromSender:(id)sender forEvent:(UIEvent*)event;

/**
 Register for events to be recorded
 */
- (void) subscribeToMonkeyEvents;

@end
