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

#import "UISlider+MTReady.h"
#import "MonkeyTalk.h"
#import "MTCommandEvent.h"
#import "UIView+MTReady.h"
#import "MTUtils.h"
#import "NSString+MonkeyTalk.h"

@implementation UISlider (MTReady)
- (NSString *)mtComponent {
    return MTComponentSlider;
}

- (NSString *) valueForProperty:(NSString *)prop withArgs:(NSArray *)args {
    NSString* value;
    
    if ([prop isEqualToString:@"value" ignoreCase:YES])
        value = [NSString stringWithFormat:@"%.2f",self.value];
    else if ([prop isEqualToString:@"max" ignoreCase:YES])
        value = [NSString stringWithFormat:@"%.2f",self.maximumValue];
    else if ([prop isEqualToString:@"min" ignoreCase:YES])
        value = [NSString stringWithFormat:@"%.2f",self.minimumValue];
    else
        value = [self valueForKeyPath:prop];
    
    return value;
}

- (BOOL) isMTEnabled {
	return YES;
}

/**
 The events to be recorded for the class of control. Defaults to none.
 */
- (UIControlEvents)monkeyEventsToHandle {
		return UIControlEventValueChanged;
}


- (void) handleMonkeyEventFromSender:(id)sender forEvent:(UIEvent*)event {

	if (event && [event isKindOfClass:[UIEvent class]]) {
		if (event.type == UIEventTypeTouches) {
			UITouch* touch = [[event allTouches] anyObject];
			// Inexplicably, UISwitches sometimes get touch events without any touches in them
			if (touch == nil || touch.phase == UITouchPhaseEnded) {
				
                // forward acessibililty if it is not already set
                if ([self respondsToSelector:@selector(accessibilityIdentifier)] && [self accessibilityIdentifier] == nil) {
                    [self setAccessibilityIdentifier:[sender accessibilityIdentifier]];
                } else if ([self accessibilityLabel] == nil) {
					[self setAccessibilityLabel:[sender accessibilityLabel]];
				}

				if ([self isMemberOfClass:[UISlider class]] || [self isKindOfClass:[UISlider class]]) {
					[MonkeyTalk recordFrom:self command:MTCommandSelect args:[NSArray arrayWithObject:[[NSString alloc] initWithFormat:@"%.2f",self.value]]];
				} else {
					// It's a UISwitch (probably)
					[MonkeyTalk recordFrom:sender command:MTCommandSwitch];
				}
		
			}
		}
		
		return;
	} 
	
}	

- (void) playbackMonkeyEvent:(MTCommandEvent*)event {
	if ([event.command isEqual:MTCommandSlide] ||
        [event.command isEqualToString:MTCommandSelect ignoreCase:YES]) {
		if ([[event args] count] == 0) {
			event.lastResult = @"Requires 1 argument, but has %d", [event.args count];
			return;
		}
		self.value = [[[event args] objectAtIndex:0] floatValue];
		[self sendActionsForControlEvents:UIControlEventValueChanged];
	} else if ([event.command isEqualToString:MTCommandTap ignoreCase:YES]) {
        // Send touchdown/touchupinside for slider tap playback
        [self sendActionsForControlEvents:UIControlEventTouchDown];
        [self sendActionsForControlEvents:UIControlEventTouchUpInside];
    } else {
		[super playbackMonkeyEvent:event];
	}
}

- (BOOL) shouldRecordMonkeyTouch:(UITouch*)phase {
	return NO;
}

+ (NSString*) uiAutomationCommand:(MTCommandEvent*)command {
	if ([command.command isEqualToString:MTCommandSlide]) {
		NSString* value = [[command args] count] ? [command.args objectAtIndex:0] : @"0";
		return [NSString stringWithFormat:@"MonkeyTalk.elementNamed(\"%@\").dragToValue(%@); // UIASlider", 
				[MTUtils stringByJsEscapingQuotesAndNewlines:command.monkeyID], 
				[MTUtils stringByJsEscapingQuotesAndNewlines:value]];
	}
	
	return [super uiAutomationCommand:command];
	
}


@end
