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

#import "UIPageControl+MTReady.h"
#import "MonkeyTalk.h"
#import "MTCommandEvent.h"
#import "UIView+MTReady.h"
#import "MTUtils.h"
#import "NSString+MonkeyTalk.h"

@implementation UIPageControl (MTReady)


-(NSString *) valueForProperty:(NSString *)prop withArgs:(NSArray *)args
{
    // Added code for verifying the default value and total pages.
    NSString *value;
    if ([prop isEqualToString:@"value" ignoreCase:YES])
        value = [NSString stringWithFormat:@"%i",self.currentPage + 1];
    else if ([prop isEqualToString:@"size" ignoreCase:YES])
        value = [NSString stringWithFormat:@"%i", self.numberOfPages];
    
    return value;
}

-(BOOL) isMTEnabled {
    return YES;
}

- (UIControlEvents)monkeyEventsToHandle {
    return UIControlEventValueChanged;
}

- (BOOL) shouldRecordMonkeyTouch:(UITouch*)phase {
	return NO;
}

- (void) handleMonkeyEventFromSender:(id)sender forEvent:(UIEvent*)event {
    
	if (event) {
		if (event.type == UIEventTypeTouches) {
			UITouch* touch = [[event allTouches] anyObject];

			if (touch == nil || touch.phase == UITouchPhaseEnded) {
                
               // NSString *currentViewPage = [NSString stringWithFormat:@"%d", self.currentPage + 1];
//                NSString *totalPages = [NSString stringWithFormat:@"%d",self.numberOfPages];
                
//                NSMutableString *args = [NSMutableString string];
//                [args appendString:currentViewPage];
//                [args appendString:@"of"];
//                [args appendString:totalPages];
                
                // Recording only current page transist
                [MonkeyTalk recordFrom:self command:MTCommandSelectPage
                                  args:[NSArray arrayWithObject:[NSString stringWithFormat:@"%d", self.currentPage + 1]]];
            }
        }
        return;
    }
}

-(void) playbackMonkeyEvent:(MTCommandEvent *)event
{
    if ([event.command isEqualToString:MTCommandSelectPage]) {
        if ([[event args] count] == 0) {
            event.lastResult = @"Requires 1 argument, but has %d", [[event args] count];
            return;
        }

//        NSString *args = [[event args] objectAtIndex:0];
//        NSString *expressionString = @"(\\d+)(\\w+)(\\d+)";        
//        NSRegularExpression *expression = [NSRegularExpression regularExpressionWithPattern:expressionString options:0 error:nil];
//        
//        NSString *transistionPage = [args stringByReplacingOccurrencesOfString:expressionString withString:@"$1" options:NSRegularExpressionSearch range:NSMakeRange(0, [args length] )];
        
        self.currentPage = [[[event args] objectAtIndex:0] integerValue] -1;
        
        if ([self respondsToSelector:@selector(type)]) {
            [self sendActionsForControlEvents:UIControlEventValueChanged];
        }
    }
   else
        [super playbackMonkeyEvent:event];
}
@end
