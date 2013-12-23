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

#import "UITableViewCell+MTReady.h"
#import <objc/runtime.h>
#import "TouchSynthesis.h"
#import "UIView+MTReady.h"
#import "MTUtils.h"
#import "MonkeyTalk.h"
#import "MTCommandEvent.h"

@implementation UITableViewCell (MTReady)


- (BOOL) isMTEnabled {
	return YES;
}

- (UITableView *)parentTable {
    UITableView *tableView = (UITableView *)self.superview;
    
    while (![tableView isKindOfClass:[UITableView class]]) {
        if (!tableView.superview) {
            return nil;
        }
        tableView = (UITableView *)tableView.superview;
    }
    return tableView;;
}

//
//- (void) playbackMonkeyEvent:(id)event {
//	[UIEvent performTouchInView:self];
//}
- (void) handleMonkeyTouchEvent:(NSSet*)touches withEvent:(UIEvent*)event {
    UITouch* touch = [touches anyObject];
    UITableView *parentTable = [self parentTable];
    
    if (!parentTable) {
        return;
    }

	if (touch) {
		NSString* cname = [MTUtils className:touch.view];
        
		if (cname) {
			if ([cname isEqualToString:@"UITableViewCellDeleteConfirmationControl"]) {
				// [MonkeyTalk recordFrom:self command:MTCommandDelete]; //No-op for now
				return;
			}
			
			if ([cname isEqualToString:@"UITableViewCellEditControl"]) {
				//[MonkeyTalk recordFrom:self command:MTCommandEdit]; No-op for now
				return;
			}
            
            if ([cname isEqualToString:@"UITableViewCellContentView"]) {
                CGPoint location = [touch locationInView:parentTable];
                NSIndexPath *indexPath = [parentTable indexPathForRowAtPoint:location];
                NSString *section = [NSString stringWithFormat:@"%i",indexPath.section+1];
                NSString *row = [NSString stringWithFormat:@"%i",indexPath.row+1];
                
                // Ignore touches on UIPickerTableView
                if ([parentTable isKindOfClass:objc_getClass("UIPickerTableView")])
                    return;
                
                NSMutableArray *argsArray = [[NSMutableArray alloc] initWithObjects:row, nil];
                
                if ([section intValue] > 1)
                    [argsArray addObject:section];
                
                if (touch.phase == UITouchPhaseEnded) {
                    if ([self isKindOfClass:NSClassFromString(@"_UIModalItemTableViewCell")]) {
                        // record UIAlertView cells as Button.tap
                        MTCommandEvent *commandEvent = [MTCommandEvent command:MTCommandTap component:MTComponentButton className:MTComponentButton monkeyID:self.textLabel.text delay:nil timeout:nil args:nil modifiers:nil];
                        [MonkeyTalk recordEvent:commandEvent];
                    } else if ([self.textLabel.text length] > 0) {
                        [MonkeyTalk recordFrom:parentTable
                                       command:MTCommandSelect
                                         args:[NSArray arrayWithObject:self.textLabel.text]];
                    } else {
                        [MonkeyTalk recordFrom:parentTable 
                               command:MTCommandSelectIndex
                                  args:argsArray];
                    }
                }
                
				return;
			}
		}
	}
	[super handleMonkeyTouchEvent:touches withEvent:event];
					  
					  
}

- (NSString*) baseMonkeyID {
	NSString* label =  [[self textLabel] text];
	if (label && label.length>0) {
		return  label;
	}
	
	label =  [[self detailTextLabel] text];
	if (label && label.length>0) {
		return label;
	}
	
	label =[self text];
	if (label && label.length>0) {
		return label;
	}
	
	for (UIView* view in [[self contentView] subviews]) {
        UILabel *l = (UILabel *)view;
		if ([l respondsToSelector:@selector(text)] && [l text] && [[l text] length]>0) {
			return [l text];
		}
	}
	
	return [super baseMonkeyID];
}

//- (void) playbackMonkeyEvent:(id)event {
//	MTCommandEvent* command = event;
//	
//	if ([command.command  isEqualToString:MTCommandDelete]) {
//		UITableView* table = [self superview];
// 		[table deleteRowsAtIndexPaths:[NSArray arrayWithObject:[table indexPathForCell:self]] withRowAnimation:YES];
//	}
//}

@end
