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

#import "UIStepper+MTReady.h"
#import "MonkeyTalk.h"
#import "NSString+MonkeyTalk.h"
#import "UIView+MTReady.h"
#import "MTConvertType.h"
#import "MTCommandEvent.h"
#import "MTDefaultProperty.h"

@implementation UIStepper (MTReady)

- (NSString *)mtComponent {
    return MTComponentStepper;
}

- (NSString *) valueForProperty:(NSString *)prop withArgs:(NSArray *)args {
    NSString* value;
    
    if ([prop isEqualToString:MTVerifyPropertyDefault])
        value = [NSString stringWithFormat:@"%0.2f",self.value];
    else if ([prop isEqualToString:MTVerifyPropertyMax])
        value = [NSString stringWithFormat:@"%0.2f",self.maximumValue];
    else if ([prop isEqualToString:MTVerifyPropertyMin])
        value = [NSString stringWithFormat:@"%0.2f",self.minimumValue];
    else if ([prop isEqualToString:MTVerifyPropertyStepSize])
        value = [NSString stringWithFormat:@"%0.2f",self.stepValue];
    else
        value = @"No value for property";
    
    return value;
}

- (void) handleMonkeyTouchEvent:(NSSet*)touches withEvent:(UIEvent*)event {
    UITouch* touch = [touches anyObject];
    CGPoint location = [touch locationInView:self];
    
    // Record on touch phase ended only
    if (touch.phase == UITouchPhaseEnded) {
        // Find which button is being tapped based on tap location
        for (UIButton *button in self.subviews) {
            CGRect buttonRect = button.frame;
            
            if (CGRectContainsPoint(button.frame, location)) {
                NSString *command;
                
                // Set command based on default accessibility label of buttons (increment/decrement)
                if ([[button monkeyID] isEqualToString:MTCommandIncrement ignoreCase:YES]) {
                    
                    command = MTCommandIncrement;
                } else if ([[button monkeyID] isEqualToString:MTCommandDecrement ignoreCase:YES]) {
                    command = MTCommandDecrement;
                }
                
                if (button.enabled)
                    // Record command for stepper
                    [MonkeyTalk recordFrom:self command:command];
            }
        }
    }
}

- (void) playbackMonkeyEvent:(id)event {
    MTCommandEvent *commandEvent = (MTCommandEvent *)event;
    BOOL changed = NO;
    
    if ([commandEvent.command isEqualToString:MTCommandIncrement ignoreCase:YES]) {
        changed = YES;
        self.value += self.stepValue;
    } else if ([commandEvent.command isEqualToString:MTCommandDecrement ignoreCase:YES]) {
        changed = YES;
        self.value -= self.stepValue;
    } else if ([commandEvent.command isEqualToString:MTCommandSelect ignoreCase:YES]) {
        if ([commandEvent.args count] != 1) {
            commandEvent.lastResult = [NSString stringWithFormat:@"Requires 1 argument, but has %d", [commandEvent.args count]];
            return;
        }
        
        self.value = [[commandEvent.args objectAtIndex:0] floatValue];
    }
    
    if (changed)
        // Send value changed
        [self sendActionsForControlEvents:UIControlEventValueChanged];
    else
        [super playbackMonkeyEvent:event];
}
@end
