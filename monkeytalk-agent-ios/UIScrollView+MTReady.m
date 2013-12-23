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

#import "UIScrollView+MTReady.h"
#import "MTUtils.h"
#import <objc/runtime.h>
#import "MonkeyTalk.h"
#import "MTCommandEvent.h"
#import "UIView+MTReady.h"
#import "UITableView+MTReady.h"
#import "NSString+MonkeyTalk.h"
#import "NSObject+MTReady.h"

@interface UIScrollView (MT_INTERCEPTOR)
-(void) orig_scrollViewDidScroll:(UIScrollView *) scrollView;
-(void) orig_scrollViewWillBeginDragging:(UIScrollView *) scrollView;
-(void) orig_scrollViewDidEndDecelerating:(UIScrollView *) scrollView;
-(void) orig_scrollViewDidEndScrollingAnimation:(UIScrollView *) scrollView;
- (void)orig_scrollViewDidEndDragging:(UIScrollView *)scrollView willDecelerate:(BOOL)decelerate;

@end

@implementation UIScrollView (MTReady)

- (NSString *)mtComponent {
    return MTComponentScroller;
}

- (BOOL) isMTEnabled {
	return YES;
}

+ (void)load {
    if (self == [UIScrollView class]) {
        Method originaMethod = class_getInstanceMethod(self, @selector(setDelegate:));
        Method replaceMethod = class_getInstanceMethod(self, @selector(mtSetDelegate:));
        method_exchangeImplementations(originaMethod, replaceMethod);
		
    }
}

- (void)mtTriggerRefreshControl {
    UITableViewController *del = self.delegate;
    
    if (del.refreshControl) {
        [del.refreshControl performSelector:@selector(beginRefreshing) withObject:nil afterDelay:0.3];
        [del.refreshControl performSelector:@selector(endRefreshing) withObject:nil afterDelay:0.3];
        
        [del.refreshControl sendActionsForControlEvents:UIControlEventValueChanged];
    }
}

- (void) playbackMonkeyEvent:(MTCommandEvent*)event {
	CGPoint offset = [self contentOffset];
	if ([event.command isEqualToString:MTCommandScroll ignoreCase:YES]) {
		if ([event.args count] < 2) {
			event.lastResult = [NSString stringWithFormat:@"Requires 2 coordinates, but has %d", [event.args count]];
            return;
		} else if ([event.args count] == 4) {
            // support commands that were recorded with beginning coordinates
            offset.x = [[[event args] objectAtIndex:2] floatValue];
            offset.y = [[[event args] objectAtIndex:3] floatValue];
        } else {
            // handle playback for 2 args
            offset.x = [[[event args] objectAtIndex:0] floatValue];
            offset.y = [[[event args] objectAtIndex:1] floatValue];
        }
	} else {
		[super playbackMonkeyEvent:event];
		return;
	}
    
    if ([self.delegate respondsToSelector:@selector(scrollViewWillBeginDragging:)])
        [self.delegate scrollViewWillBeginDragging:self];
    
    // trigger refresh control
    if ([self.delegate respondsToSelector:@selector(refreshControl)] && offset.y < 0) {
        [self mtTriggerRefreshControl];
    }
    
    [MonkeyTalk sharedMonkey].isAnimating = YES;
	[self setContentOffset:offset animated:YES];
    
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        // Sleep while setContentOffset is animating
        [NSThread sleepForTimeInterval:0.33];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            [MonkeyTalk sharedMonkey].isAnimating = NO;
            if ([self.delegate respondsToSelector:@selector(scrollViewDidEndDragging:willDecelerate:)])
                [self.delegate scrollViewDidEndDragging:self willDecelerate:NO];
            
            if ([self.delegate respondsToSelector:@selector(scrollViewDidEndDecelerating:)])
                [self.delegate scrollViewDidEndDecelerating:self];
            
            if ([self.delegate respondsToSelector:@selector(scrollViewDidScroll:)])
                [self.delegate scrollViewDidScroll:self];
            
            if ([self.delegate respondsToSelector:@selector(scrollViewDidEndScrollingAnimation:)]) {
                [self.delegate scrollViewDidEndScrollingAnimation:self];
            }
        });
    });
}

-(void) mtSetDelegate:(NSObject <UIScrollViewDelegate> *)del {
    [del interceptMethod:@selector(scrollViewDidScroll:) withClass:[self class] types:"c@:@"];
    [del interceptMethod:@selector(scrollViewWillBeginDragging:) withClass:[self class] types:"c@:@"];
    [del interceptMethod:@selector(scrollViewDidEndDecelerating:) withClass:[self class] types:"c@:@"];
    [del interceptMethod:@selector(scrollViewDidEndScrollingAnimation:) withClass:[self class] types:"c@:@"];
    [del interceptMethod:@selector(scrollViewDidEndDragging:willDecelerate:) withClass:[self class] types:"c@:@"];
    
    [self mtSetDelegate:del];
}

- (void)recordTableScrollToRow {
    UITableView* table = (UITableView*) self;
    NSArray* cells = [table visibleCells];
    
    // Handle pull to refresh behavior by recording scroll on table view
    // if the y offset is negative, otherwise record scroll to row
    if (self.contentOffset.y >= 0) {
        if ([cells count]) {
            UITableViewCell *topCell = (UITableViewCell *)[cells objectAtIndex:0];
            NSIndexPath* indexPath = [table indexPathForCell:topCell];
            NSArray *recordArray = [NSArray
                                    arrayWithObjects:
                                    [NSString stringWithFormat:@"%d", indexPath.row+1],
                                    indexPath.section == 0 ? nil : [NSString stringWithFormat:@"%d", indexPath.section], nil];
            
            // Record index path and not text label for now
            //                if ([topCell.textLabel.text length] > 0)
            //                    recordArray = [NSArray arrayWithObject:topCell.textLabel.text];
            //                else
            //                    recordArray = [NSArray
            //                                   arrayWithObjects:
            //                                   [NSString stringWithFormat:@"%d", indexPath.row+1],
            //                                   indexPath.section == 0 ? nil : [NSString stringWithFormat:@"%d", indexPath.section], nil];
            
            // Record table view scroll to row
            [[MonkeyTalk sharedMonkey] postCommandFrom:self
                                               command:MTCommandScrollToRow
                                                  args:recordArray];
        }
    }
}

- (void)recordScroll {
    // Do not record scroll on UIPickerTableView and _UIWebViewScrollView
    if ([self isKindOfClass:objc_getClass("UIPickerTableView")] ||
        [self.superview isKindOfClass:objc_getClass("_UIWebViewScrollView")]) {
        return;
    }
    
    // Since it's unclear exactly how to do this in a subclass (override a swapped method), we do it here instead (sorry)
	if ([self isKindOfClass:[UITableView class]] && self.contentOffset.y >= 0) {
		[self recordTableScrollToRow];
	} else {
        CGPoint offsetEndPoint;
        offsetEndPoint.x = self.contentOffset.x;
        offsetEndPoint.y = self.contentOffset.y;
        
        [MonkeyTalk recordEvent:[MTCommandEvent command:MTCommandScroll
                                              className:[NSString stringWithUTF8String:class_getName([self class])]
                                               monkeyID:self.monkeyID
                                                   args:[NSArray arrayWithObjects:
                                                         [NSString stringWithFormat:@"%1.0f",(float)offsetEndPoint.x],
                                                         [NSString stringWithFormat:@"%1.0f", (float)offsetEndPoint.y], nil]]];
    }
}

#pragma ScrollView Delegates

-(void) mt_scrollViewDidSroll:(UIScrollView *) scrollView {
    if ([self respondsToSelector:@selector(orig_scrollViewDidScroll:)])
        [self orig_scrollViewDidScroll:scrollView];
}

-(void) mt_scrollViewWillBeginDragging:(UIScrollView *) scrollView {
    if ([self respondsToSelector:@selector(orig_scrollViewWillBeginDragging:)])
        [self orig_scrollViewWillBeginDragging:scrollView];
}

-(void) mt_scrollViewDidEndDecelerating:(UIScrollView *) scrollView {
    // record content offset at the end of deceleration
    [scrollView recordScroll];
    
    if ([self respondsToSelector:@selector(orig_scrollViewDidEndDecelerating:)])
        [self orig_scrollViewDidEndDecelerating:scrollView];
}

- (void)mt_scrollViewDidEndDragging:(UIScrollView *)scrollView willDecelerate:(BOOL)decelerate {
    if (!decelerate) {
        // record content offset at end of drag
        [scrollView recordScroll];
    } else if ([scrollView isKindOfClass:[UITableView class]] && scrollView.contentOffset.y < 0) {
        [scrollView recordScroll];
    }
    
    if ([self respondsToSelector:@selector(orig_scrollViewDidEndDragging:willDecelerate:)])
        [self orig_scrollViewDidEndDragging:scrollView willDecelerate:decelerate];
}

-(void) mt_scrollViewDidEndScrollingAnimation:(UIScrollView *) scrollView {
    if ([self respondsToSelector:@selector(orig_scrollViewDidEndScrollingAnimation:)])
        [self orig_scrollViewDidEndScrollingAnimation:scrollView];
}

- (BOOL) shouldRecordMonkeyTouch:(UITouch*)touch {
	return NO;
}

@end
