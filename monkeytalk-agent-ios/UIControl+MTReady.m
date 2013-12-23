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

#import "UIControl+MTReady.h"
#import <UIKit/UIControl.h>
#import <objc/runtime.h>
#import "MonkeyTalk.h"


@implementation UIControl (MTReady)

+ (void)load {
    if (self == [UIControl class]) {
		// Hijack UIControl's initialializers so we can add MonkeyTalk stuff whenever a UIControl is created

        Method originalMethod = class_getInstanceMethod(self, @selector(initWithCoder:));
        Method replacedMethod = class_getInstanceMethod(self, @selector(mtInitWithCoder:));
        method_exchangeImplementations(originalMethod, replacedMethod);		
    }
}

- (void) subscribeToMonkeyEvents {
	if (self.monkeyEventsToHandle != 0) {
		[self addTarget:self action:@selector(handleMonkeyEventFromSender:forEvent:) forControlEvents:self.monkeyEventsToHandle];
	}
}

- (id)mtInitWithCoder:(NSCoder *)decoder {
	// Calls original initWithCoder (that we swapped in load method)
    [self mtInitWithCoder:decoder];
	if (self) {
		[self subscribeToMonkeyEvents];
	}
	return self;
}



- (UIControlEvents) monkeyEventsToHandle {
	// Default ignores all events
	return 0;
}
- (void) handleMonkeyEventFromSender:(id)sender forEvent:(UIEvent*)event {
	// Default is a no-op
}

@end
