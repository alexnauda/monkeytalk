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

#import <objc/runtime.h>
#import "UITableView+MTReady.h"
#import "MTCommandEvent.h"
#import "MonkeyTalk.h"
#import "UIView+MTReady.h"
#import "NSObject+MTReady.h"
#import "TouchSynthesis.h"
#import "NSString+MonkeyTalk.h"
#import "UIGestureRecognizer+MTReady.h"

// Note that the recording functionality is actually implemented in UIScrollView (MTReady). This is because we're not sure
// about overriding the setContentOffset method here, since we swizzle it in UIScrollView. The Objective-C doc is unclear
// on the safety of overriding a swizzled method (and it's not clear there's a prescribed order of class loading so 
// further swizzling the swizzled methods here seems dangerous. As a result, we do an explicit type check in 
// UIScrollView to see if we're actually a UITableView, and if we are, we record the (section,row) tuple rather than (x,y)

@interface UITableView (Intercept)
- (void)originalTableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)sourceIndexPath toIndexPath:(NSIndexPath *)destinationIndexPath;
- (void)origTableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath;
- (void) originalTableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath;
@end

@implementation UITableView (MTReady)

- (NSString *)mtComponent {
    return MTComponentTable;
}

+ (void)load {
    if (self == [UITableView class]) {
		
        Method originalMethod = class_getInstanceMethod(self, @selector(setDataSource:));
        Method replacedMethod = class_getInstanceMethod(self, @selector(mtSetDataSource:));
        method_exchangeImplementations(originalMethod, replacedMethod);		
    }
}

- (NSString *) valueForProperty:(NSString *)prop withArgs:(NSArray *)args {
    NSString *result;
    NSInteger arg1Int = 0;
    NSInteger arg2Int = 0;
    BOOL shouldReturnArray = NO;
    BOOL arrayHasSection = NO;
    
    // Return items in first row/component by default
    if ([prop isEqualToString:@"value" ignoreCase:YES]) {
        prop = @"item";
//        shouldReturnArray = YES;
    }
    
    if ([args count] > 0) {
        for (int i = 0; i < [args count]; i++) {
            NSString *arg = [args objectAtIndex:i];
            if (i == 0 && ([arg isEqualToString:@"[]"] ||
                           [arg isEqualToString:@"["])){
//                shouldReturnArray = YES;
            } else if (i == 1 && shouldReturnArray && [arg isEqualToString:@"]"]) {
                arrayHasSection = YES;
            }
            
            arg = [arg stringByReplacingOccurrencesOfString:@"[" withString:@""];
            arg = [arg stringByReplacingOccurrencesOfString:@"]" withString:@""];
            
            if (i == 0 && [arg intValue] > 0)
                arg1Int = [arg intValue] - 1;
            else if (i == 1 && [arg intValue] > 0)
                arg2Int = [arg intValue] - 1;
        }
    }
    
    if ([prop isEqualToString:@"selected" ignoreCase:YES]) { 
        if ([self indexPathForSelectedRow]) {
            UITableViewCell *cell = [self cellForRowAtIndexPath:[self indexPathForSelectedRow]];
            
            if ([cell.textLabel.text length] > 0) {
                result = cell.textLabel.text;
            }
        } else {
            result = @"No Row Selected";
        }
    } else if ([prop isEqualToString:@"selectedIndex" ignoreCase:YES]) {
        NSIndexPath *indexPath = [self indexPathForSelectedRow];
        if (indexPath) {
            if ([self numberOfSections] == 1) {
                result = [NSString stringWithFormat:@"%d", indexPath.row+1];
            } else {
                result = [NSString stringWithFormat:@"%d,%d", indexPath.row+1, indexPath.section+1];
            }
        } else {
            result = @"No Row Selected";
        }
    } else if ([prop isEqualToString:@"item" ignoreCase:YES] ||
               [prop isEqualToString:@"detail" ignoreCase:YES]) {
        NSInteger row = arg1Int;
        NSInteger section = arg2Int;
        
        NSIndexPath *startIndex = [self indexPathForCell:[[self visibleCells] objectAtIndex:0]];
        
        if (shouldReturnArray) {
            result = @"[";
            for (int j = 0; j < [self numberOfSections]; j++) {
                if (arrayHasSection)
                    j = section;
                
                result = [result stringByAppendingFormat:@"["];
                for (int i = 0; i < [self numberOfRowsInSection:j]; i++) {
                    [self scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:i inSection:j] atScrollPosition:UITableViewScrollPositionNone animated:NO];
                    UITableViewCell *temp = [self cellForRowAtIndexPath:[NSIndexPath indexPathForRow:i inSection:section]];
                    result = [result stringByAppendingFormat:@"%@",temp.textLabel.text];
                    if (i != [self numberOfRowsInSection:0]-1)
                        result = [result stringByAppendingFormat:@","];
                }
                
                result = [result stringByAppendingFormat:@"]"];
                
                if (j != [self numberOfSections]-1 && !arrayHasSection)
                    result = [result stringByAppendingFormat:@","];
                
                if (arrayHasSection)
                    j = [self numberOfSections];
            }
            result = [result stringByAppendingFormat:@"]"];
        } else {
            [self scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:row inSection:section] atScrollPosition:UITableViewScrollPositionNone animated:NO];
            
            UITableViewCell *cell = [self cellForRowAtIndexPath:[NSIndexPath indexPathForRow:row inSection:section]];
            
            if ([prop isEqualToString:@"item" ignoreCase:YES])
                result = cell.textLabel.text;
            else
                result = cell.detailTextLabel.text;
        }
        
        [self scrollToRowAtIndexPath:startIndex atScrollPosition:UITableViewScrollPositionNone animated:NO];
    } else if([prop isEqualToString:@"size" ignoreCase:YES]) {
        result = [NSString stringWithFormat:@"%i",[self numberOfRowsInSection:arg1Int]];
    } else
        result = [self valueForKeyPath:prop];
    
    return result;
}

- (void) mtSetDataSource:(NSObject <UITableViewDataSource>*) del {
	// The existence of this method triggers swipe-to-edit functionality in a UITableView (and who knows what else). 
	// If we add this method and it doesn't already exist, we inadvertently enable swipe-to-edit 
	// So, if this method doesn't already exist, we assume the table is not editable and therefore we don't have to record these delegate calls.
	if ([del mtHasMethod:@selector(tableView:commitEditingStyle:forRowAtIndexPath:)]) {
		[del  interceptMethod:@selector(tableView:commitEditingStyle:forRowAtIndexPath:) 
				   withMethod:@selector(mtTableView:commitEditingStyle:forRowAtIndexPath:) 
					  ofClass:[self class] 
				   renameOrig:@selector(origTableView:commitEditingStyle:forRowAtIndexPath:) 
						types:"v@:@i@"];
        
        [del  interceptMethod:@selector(tableView:didSelectRowAtIndexPath:) 
				   withMethod:@selector(mtTableView:didSelectRowAtIndexPath:) 
					  ofClass:[self class] 
				   renameOrig:@selector(originalTableView:didSelectRowAtIndexPath:) 
						types:"v@:@i@"];
        
        [del  interceptMethod:@selector(tableView:moveRowAtIndexPath:toIndexPath:) 
				   withMethod:@selector(mtTableView:moveRowAtIndexPath:toIndexPath:) 
					  ofClass:[self class] 
				   renameOrig:@selector(originalTableView:moveRowAtIndexPath:toIndexPath:) 
						types:"v@:@i@"];
        
        [del  interceptMethod:@selector(insertRowsAtIndexPaths:withRowAnimation:) 
				   withMethod:@selector(mtInsertRowsAtIndexPaths:withRowAnimation:) 
					  ofClass:[self class] 
				   renameOrig:@selector(originalInsertRowsAtIndexPaths:withRowAnimation:) 
						types:"v@:@i@"];
        
        // For now, do not record set editing
//        [del  interceptMethod:@selector(setEditing:animated:) 
//				   withMethod:@selector(mtSetEditing:animated:) 
//					  ofClass:[self class] 
//				   renameOrig:@selector(origSetEditing:animated:) 
//						types:"@:@"];
	}
	
//	Method originalMethod = class_getInstanceMethod([del class], @selector(tableView:commitEditingStyle:forRowAtIndexPath:));
//	if (originalMethod) {
//		IMP origImp = method_getImplementation(originalMethod);
//		Method replacedMethod = class_getInstanceMethod([self class], @selector(mtTableView:commitEditingStyle:forRowAtIndexPath:));
//		IMP replImp = method_getImplementation(replacedMethod);
//		
//		if (origImp != replImp) {
//			method_setImplementation(originalMethod, replImp);
//			class_addMethod([del class], @selector(origTableView:commitEditingStyle:forRowAtIndexPath:), origImp,"v@:@i@");
//		}
//	}
	[self mtSetDataSource:del];
	
	
}

// For now, do not record set editing
//- (void)mtSetEditing:(BOOL)editing animated:(BOOL)animated {
//    NSString *editingString;
//    if (editing)
//        editingString = @"true";
//    else
//        editingString = @"false";
//    
//    UIViewController *controller = (UIViewController *)self;
//    UITableView *firstTable;
//    
//    // May be an issue if there is more than 1 table in controller.view
//    for (UITableView *tableView in [controller.view subviews]) {
//        firstTable = tableView;
//        break;
//    }
//    
//    [MonkeyTalk recordFrom:firstTable command:MTCommandSetEditing args:[NSArray arrayWithObjects: editingString, nil]];
//
//    [self origSetEditing:editing animated:animated];
//}

- (void)mtTableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)sourceIndexPath toIndexPath:(NSIndexPath *)destinationIndexPath {
    
    if (sourceIndexPath != destinationIndexPath)
        [MonkeyTalk recordFrom:tableView command:MTCommandMove args:[NSArray arrayWithObjects: [NSString stringWithFormat: @"%d", sourceIndexPath.row+1], [NSString stringWithFormat:@"%d", destinationIndexPath.row+1], nil]];
    
    [self originalTableView:tableView moveRowAtIndexPath:sourceIndexPath toIndexPath:destinationIndexPath];
}

- (void)mtTableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
	if (editingStyle == UITableViewCellEditingStyleDelete) {
		[MonkeyTalk recordFrom:tableView command:MTCommandRemove args:[NSArray arrayWithObjects: [NSString stringWithFormat: @"%d", indexPath.row+1], [NSString stringWithFormat:@"%d", indexPath.section+1], nil]];
	} else if (editingStyle == UITableViewCellEditingStyleInsert) {
        [MonkeyTalk recordFrom:tableView command:MTCommandInsert args:[NSArray arrayWithObjects: [NSString stringWithFormat: @"%d", indexPath.row+1], [NSString stringWithFormat:@"%d", indexPath.section+1], nil]];
    }
	[self origTableView:tableView commitEditingStyle:editingStyle forRowAtIndexPath:indexPath];
}

- (void) playbackMonkeyEvent:(MTCommandEvent*)event {
    if ([event.command isEqualToString:MTCommandTap]) {
        [super playbackMonkeyEvent:event];
        return;
    }
    
    if ([[event args] count] == 0) {
        event.lastResult = [NSString stringWithFormat:@"Requires 1 or more arguments, but has %d", [event.args count]];
        return;
    }
    
	if ([event.command isEqualToString:MTCommandVScroll] || 
        [event.command isEqualToString:MTCommandScrollToRow ignoreCase:YES]) {
//		if ([event.args count] > 2) {
//			event.lastResult = @"Requires 0, 1, or 2 arguments, but has %d", [event.args count];
//		}
		NSInteger row = [event.args count] > 0 ? [[event.args objectAtIndex:0] intValue] : 0;
		NSInteger section = [event.args count] > 1 ? [[event.args objectAtIndex:1] intValue] : 0;
        
        if (row > 0)
            row -= 1;
        if (section > 0)
            section -= 1;

		NSIndexPath* path = [[self class] indexPathForCellTextLabel:self withTitle:[event.args objectAtIndex:0]];
        
        if (path == nil)
            path = [NSIndexPath indexPathForRow:row inSection:section];
        
        // Handle errors if we can't find cell
        if (path == nil) {
            event.lastResult = [NSString stringWithFormat:@"Could not find cell %@ in table with monkeyID %@",[event.args objectAtIndex:0],event.monkeyID];
            return;
        } else if (row+1 > [self numberOfRowsInSection:section]) {
            event.lastResult = [NSString stringWithFormat:@"Scroll out of bounds -- can't scroll to row %i, because section %i only has %i %@",
                                row+1, section+1, [self numberOfRowsInSection:section], 
                                [NSString pluralStringFor:@"row" withCount:[self numberOfRowsInSection:section]]];
            return;
        } else if (section+1 > [self numberOfSections]) {
            event.lastResult = [NSString stringWithFormat:@"Scroll out of bounds -- can't scroll to row %i in section %i, because table only has %i %@",
                                row+1, section+1, [self numberOfSections], [NSString pluralStringFor:@"section" withCount:[self numberOfSections]]];
            return;
        }
        // UITableViewScrollPositionNone changed from UITableViewScrollPositionTop to resolve two bugs --KplMax
		[self scrollToRowAtIndexPath:path atScrollPosition:UITableViewScrollPositionNone animated:YES];
	} else if ([event.command isEqualToString:MTCommandDelete] ||
               [event.command isEqualToString:MTCommandRemove ignoreCase:YES] ||
               [event.command isEqualToString:MTCommandInsert ignoreCase:YES]) {
//		if ([event.args count] > 2) {
//			event.lastResult = @"Requires 0, 1, or 2 arguments, but has %d", [event.args count];
//		}
		NSInteger row = [event.args count] > 0 ? [[event.args objectAtIndex:0] intValue] : 0;
		NSInteger section = [event.args count] > 1 ? [[event.args objectAtIndex:1] intValue] : 0;
        
        if (row > 0)
            row -= 1;
        if (section > 0)
            section -= 1;
		
		
		NSIndexPath* path = [NSIndexPath indexPathForRow:row inSection:section];
        
        UITableViewCellEditingStyle style = UITableViewCellEditingStyleDelete;
        
        if ([event.command isEqualToString:MTCommandInsert ignoreCase:YES])
            style = UITableViewCellEditingStyleInsert;
		
		[(UITableView *)self.dataSource origTableView:self commitEditingStyle:style forRowAtIndexPath:path];
	}  else if ([event.command isEqualToString:MTCommandSelectRow ignoreCase:YES] ||
                 [event.command isEqualToString:MTCommandSelectIndex ignoreCase:YES] ||
                [event.command isEqualToString:MTCommandSelect ignoreCase:YES] ||
                [event.command isEqualToString:MTCommandSelectIndicator ignoreCase:YES]) {
        NSInteger row = [event.args count] > 0 ? [[event.args objectAtIndex:0] intValue] : 0;
		NSInteger section = [event.args count] > 1 ? [[event.args objectAtIndex:1] intValue] : 0;
        
        if (row > 0)
            row -= 1;
        if (section > 0)
            section -= 1;

        NSIndexPath *indexPath = nil;
        
        if ([event.command isEqualToString:MTCommandSelect ignoreCase:YES]) {
            indexPath = [[self class] indexPathForCellTextLabel:self withTitle:[event.args objectAtIndex:0]];
        } else {
            indexPath = [NSIndexPath indexPathForRow:row inSection:section];
        }
        
        // Handle errors if we can't find cell
        if (indexPath == nil) {
            event.lastResult = [NSString stringWithFormat:@"Could not find cell %@ in table with monkeyID %@",[event.args objectAtIndex:0],event.monkeyID];
            return;
        } else if (row+1 > [self numberOfRowsInSection:section]) {
            event.lastResult = [NSString stringWithFormat:@"Selection out of bounds -- can't select row %i, because section %i only has %i %@",
                                row+1, section+1, [self numberOfRowsInSection:section], 
                                [NSString pluralStringFor:@"row" withCount:[self numberOfRowsInSection:section]]];
            return;
        } else if (section+1 > [self numberOfSections]) {
            event.lastResult = [NSString stringWithFormat:@"Selection out of bounds -- can't select row %i in section %i, because table only has %i %@",
                                row+1, section+1, [self numberOfSections], [NSString pluralStringFor:@"section" withCount:[self numberOfSections]]];
            return;
        }
        
        BOOL isCellVisible = NO;
        
//        for (UITableViewCell *cell in [self visibleCells]) {
//            if (cell == selectCell) {
//                isCellVisible = YES;
//            }
//        }
        
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            if (!isCellVisible) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self scrollToRowAtIndexPath:indexPath 
                                atScrollPosition:UITableViewScrollPositionNone animated:YES];
                });
                
                // Set isAnimating to wait to send response
                // Allows for scroll animation to be seen
                [MonkeyTalk sharedMonkey].isAnimating = YES;
                [NSThread sleepForTimeInterval:0.33];
                [MonkeyTalk sharedMonkey].isAnimating = NO;
            }
            
            
            //        if (!isCellVisible)
            //            [NSThread sleepForTimeInterval:0.83];
            
            dispatch_async(dispatch_get_main_queue(), ^{
//                if ([self respondsToSelector:@selector(delegate)] &&
//                    [self.delegate respondsToSelector:@selector(tableView:willSelectRowAtIndexPath:)])
//                    [self.delegate tableView:self willSelectRowAtIndexPath:indexPath];
//                
//                [self.delegate tableView:self didSelectRowAtIndexPath:indexPath];
                
                UITableViewCell *cell = [self cellForRowAtIndexPath:indexPath];
                
                if ([event.command isEqualToString:MTCommandSelectIndicator ignoreCase:YES]){
                    [self.delegate tableView:self accessoryButtonTappedForRowWithIndexPath:indexPath];
                    cell.selected = NO;
                } else {
                    // select the cell
                    if ([self.delegate respondsToSelector:@selector(tableView:didSelectRowAtIndexPath:)]) {
                        [self selectRowAtIndexPath:indexPath animated:NO scrollPosition:UITableViewScrollPositionNone];
                        [self.delegate tableView:self didSelectRowAtIndexPath:indexPath];
                    } else {
                        // support playback for apps using storyboards
                        [UIEvent performTouchInView:cell];
                    }
                }
            });
        });
        
    } else if([event.command isEqualToString:MTCommandMove ignoreCase:YES]) {
        if ([[event args] count] < 2) {
            event.lastResult = [NSString stringWithFormat:@"Requires 2 arguments, but has %d", [event.args count]];
            return;
        }
        
        NSInteger sourceRow = [event.args count] > 0 ? [[event.args objectAtIndex:0] intValue] : 0;
		NSInteger destinationRow = [event.args count] > 1 ? [[event.args objectAtIndex:1] intValue] : 0;
        
        if (sourceRow > 0
            )
            sourceRow -= 1;
        if (destinationRow > 0)
            destinationRow -= 1;
        
        // ToDo: Handle Move in sections other than 0
        NSIndexPath *sourceIndexPath = [NSIndexPath indexPathForRow:sourceRow inSection:0];
        NSIndexPath *destinationIndexPath = [NSIndexPath indexPathForRow:destinationRow inSection:0];
        
        [(UITableView *)self.delegate originalTableView:self moveRowAtIndexPath:sourceIndexPath toIndexPath:destinationIndexPath];
    } else if([event.command isEqualToString:MTCommandSetEditing ignoreCase:YES]) {
        // Default editing to YES
        BOOL editingBool = YES;
        
        if ([event.args count] > 0) {
            if ([[event.args objectAtIndex:0] isEqualToString:@"false"])
                editingBool = NO;
        }
        
        if ([self respondsToSelector:@selector(setEditing:animated:)]) {
            [self setEditing:editingBool animated:YES];
        } else {
            event.lastResult = @"An error occurred while trying to set editing.";
        }
    }
    else if ([event.command isEqualToString:MTCommandLongSelectIndex ignoreCase:YES])
    {
        [UIGestureRecognizer playbackMonkeyEvent:event];
        return;
    }
    else {
		[super playbackMonkeyEvent:event];
		return;
	}
}

+ (NSString*) uiAutomationCommand:(MTCommandEvent*)command {
	NSMutableString* string = [[NSMutableString alloc] init];
//	if (command.source && ![command.source accessibilityLabel]) {
//		[string appendFormat:@"// Accessibility label may need to be set to %@ for %@\n", command.monkeyID, command.className]; 
//		NSLog(@"Accessibility label may need to be set to %@ for %@\n", command.monkeyID, command.className);
//	}
	if ([command.command isEqualToString:MTCommandVScroll]) {
		NSString* section = [command.args count] < 2 ? @"0" : [command.args objectAtIndex:1];
		NSString* row = [command.args count] < 1 ? @"0" : [command.args objectAtIndex:0];
		[string appendFormat:@"MonkeyTalk.scrollTo(\"%@\", \"%@\", \"%@\")", command.monkeyID, section, row];
	} else {
		[string appendString:[super uiAutomationCommand:command]];
	}
	return string;
}

+ (NSIndexPath *) indexPathForCellTextLabel:(UITableView *)tableView withTitle:(NSString *)title {
    NSIndexPath *indexPath = nil;
    for (int i = 0; i < [tableView numberOfSections]; i++) {
        for (int j = 0; j < [tableView numberOfRowsInSection:i]; j++) {
            NSIndexPath *currentPath = [NSIndexPath indexPathForRow:j inSection:i];
            UITableViewCell *cell = nil;
            
            // UIMoreListController (more tab) does not return correct cell from tableView:cellForRowAtIndexPath:
            if ([tableView.dataSource respondsToSelector:@selector(tableView:cellForRowAtIndexPath:)] &&
                ![tableView.dataSource isKindOfClass:NSClassFromString(@"UIMoreListController")])
                cell = [tableView.dataSource tableView:tableView cellForRowAtIndexPath:currentPath];
            else
                cell = [tableView cellForRowAtIndexPath:currentPath];
            
            if (!cell)
                continue;
            
            if ([cell.textLabel.text isEqualToString:title]) {
                indexPath = currentPath;
                break;
            }
        }
        
        if (indexPath != nil)
            break;
    }
    return indexPath;
}

- (void) mtTableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    NSString *section = [NSString stringWithFormat:@"%i",indexPath.section+1];
    NSString *row = [NSString stringWithFormat:@"%i",indexPath.row+1];
    NSMutableArray *argsArray = [[NSMutableArray alloc] initWithObjects:row, nil];
    
    if (indexPath.section > 0)
        [argsArray addObject:section];
    
    [MonkeyTalk recordFrom:tableView
                   command:MTCommandSelectIndex
                      args:argsArray];
    
    if ([self respondsToSelector:@selector(originalTableView:didSelectRowAtIndexPath:)]) {
        [self originalTableView:tableView didSelectRowAtIndexPath:indexPath];
    }
}

@end
