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

#import "UINavigationBar+MTReady.h"
#import "TouchSynthesis.h"
#import "MonkeyTalk.h"
#import "MTCommandEvent.h"
#import "UIView+MTReady.h"
#import <objc/runtime.h>
#import "MTUtils.h"


@implementation UINavigationBar (MTReady)

+ (void)load {
    if (self == [UINavigationBar class]) {
        Method originalMethod = class_getInstanceMethod(self, @selector(popNavigationItemAnimated:));
        Method replacedMethod = class_getInstanceMethod(self, @selector(mtpopNavigationItemAnimated:));
        method_exchangeImplementations(originalMethod, replacedMethod);
    }
}

- (NSString*) baseMonkeyID {
	// Default identifier is the component tag
	NSString* title = [[self topItem] title];
	if (title && title.length>0) {
		return [title copy];
	}
	return [super baseMonkeyID];
}

- (BOOL) shouldRecordMonkeyTouch:(UITouch*)touch {	
	return (touch.phase == UITouchPhaseBegan);
}

- (void) handleMonkeyTouchEvent:(NSSet*)touches withEvent:(UIEvent*)event {
	UITouch* touch = [touches anyObject];
	CGPoint loc = [touch locationInView:self];
//	CGFloat width = [[touch view] bounds].size.width;
    
    NSLog(@"Subviews: %@",self.subviews);
    
    for (int i = 0; i < [self.subviews count]; i++) {
        UIView *view = [self.subviews objectAtIndex:i];
        if (CGRectContainsPoint(view.frame, loc) && 
            [view isKindOfClass:objc_getClass("UINavigationItemButtonView")])
            
            [MonkeyTalk recordFrom:view command:MTCommandTap args:
             [NSArray arrayWithObjects:[NSString stringWithFormat:@"%1.0f", loc.x],
                                       [NSString stringWithFormat:@"%1.0f",loc.y],
                                        nil]];
//        NSLog(@"Back Title: %f",view.frame.origin.x);
    }
	
//	if (loc.x/width < 0.33) {
//		[MonkeyTalk recordFrom:self command:MTCommandTouchLeft];
//		return;
//	}
//	
//	if (loc.x/width > 0.66) {
//		[MonkeyTalk recordFrom:self command:MTCommandTouchRight];
//		return;
//	}
}

- (void) playbackMonkeyEvent:(id)event {
	NSString* command = ((MTCommandEvent*)event).command;
	if ([command isEqual:MTCommandTouchLeft]) {
		[UIEvent performTouchLeftInView:self ];
		return;
	}
	if ([command isEqual:MTCommandTouchRight]) {
		[UIEvent performTouchRightInView:self ];
		return;
	}
	
	[super playbackMonkeyEvent:event];
	return;
}

+ (NSString*) uiAutomationCommand:(MTCommandEvent*)command {
	NSString* string;
	if ([command.command isEqualToString:MTCommandTouchLeft]) {
		string = [NSString stringWithFormat:@"MonkeyTalk.elementNamed(\"%@\").leftButton().tap()", command.monkeyID];
	} else if ([command.command isEqualToString:MTCommandTouchRight]) {
		string = [NSString stringWithFormat:@"MonkeyTalk.elementNamed(\"%@\").rightButton().tap()", command.monkeyID];
	} else {
		string = [super uiAutomationCommand:command];
	}
	return string;
}

- (UINavigationItem *) mtpopNavigationItemAnimated:(BOOL)animated
{
    if ([MonkeyTalk sharedMonkey].commandSpeed != nil && [[MonkeyTalk sharedMonkey].commandSpeed doubleValue] < 333333)
        animated = NO;
    
    [self mtpopNavigationItemAnimated:animated];
    
    return nil;
}

@end
