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

#import "UITabBar+MTReady.h"
#import "MonkeyTalk.h"
#import "TouchSynthesis.h"
#import "UIView+MTReady.h"
#import "NSString+MonkeyTalk.h"

@implementation UITabBar (MTReady)

- (NSString *)mtComponent {
    return MTComponentTabBar;
}

- (NSString *) valueForProperty:(NSString *)prop withArgs:(NSArray *)args {
    NSString *result;
    NSInteger arg1Int = 0;
    BOOL shouldReturnArray = NO;
    BOOL arrayHasSection = NO;
    
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
        }
    }
    
    NSInteger index = arg1Int;
    
    if ([prop isEqualToString:@"value" ignoreCase:YES]) { 
        result = self.selectedItem.title;
    } else if ([prop isEqualToString:@"item" ignoreCase:YES]) {
        UITabBarItem *itemAtIndex = [self.items objectAtIndex:index];
        result = itemAtIndex.title;
    } else if([prop isEqualToString:@"size" ignoreCase:YES]) {
        result = [NSString stringWithFormat:@"%i",[self.items count]];
    } else
        result = [self valueForKeyPath:prop];
    
    return result;
}

- (void) handleTabBar:(UITabBar *)tabBar {
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        
        // Short delay to get the tab navigated to
        [NSThread sleepForTimeInterval:0.1];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            UITabBarController *tbController = (UITabBarController *)tabBar.delegate;
            
            NSString *monkeyID = tabBar.selectedItem.title;
            NSString *command = @"";
            
            if ([monkeyID length] == 0) {
                // New changes for handling 'More navigation controller' - Kapil
                
                NSUInteger selected = 0;
                
                for (UITabBarItem *item in tabBar.items) {
                    if (item == tabBar.selectedItem)
                        break;
                    selected++;
                }
                
                // monkeyID = [NSString stringWithFormat:@"%i",tbController.selectedIndex+1];
                NSUInteger selectedIndex = 0;
                
                if ([tbController respondsToSelector:@selector(selectedIndex)]) {
                    selectedIndex = tbController.selectedIndex;
                } else {
                    for (UITabBarItem *item in tabBar.items) {
                        if (item == tabBar.selectedItem)
                            break;
                        selectedIndex++;
                    }
                }
                
                if(selectedIndex == NSNotFound)
                {
                    if([tbController.selectedViewController isEqual:tbController.moreNavigationController] ) {
                        monkeyID = @"More";
                        command = MTCommandSelect;
                    }
                }
                else {
                    monkeyID = [NSString stringWithFormat:@"%i",selectedIndex+1];
                }
                
                if ([command length] == 0)
                    command = MTCommandSelectIndex;
            } else {
                command = MTCommandSelect;
            }
            
            NSArray *monkeyArg = [NSArray arrayWithObject:monkeyID];
            
            [MonkeyTalk recordFrom:tabBar command:command args:monkeyArg];
        }); 
    });
}

- (void) playbackTabBarEvent:(MTCommandEvent *)event {
	// We should actually call this on all components from up in the run loop
    //	[self mtAssureAutomationInit];
	
	// By default we generate a touch in the center of the view
	MTCommandEvent* ev = event;
    
    if ([[ev args] count] == 0) {
        ev.lastResult = [NSString stringWithFormat:@"Requires 1 argument, but has %d", [ev.args count]];
        return;
    }
    
    NSInteger selectIndex = -1;
    UITabBar *tabBar = (UITabBar *)self;
    UITabBarController *tbController = (UITabBarController *)tabBar.delegate;
    
    if ([[ev command] isEqualToString:MTCommandTouch] || 
        [[[ev command] lowercaseString] isEqualToString:[MTCommandSelect lowercaseString]]) {
        
        // New change regarding handling 'More navigation controller' -Kapil
        if ([[ev.args objectAtIndex:0] isEqualToString:@"More"]) {
            [tbController setSelectedViewController:tbController.moreNavigationController];
            return;
        }
        
        NSString *title = [ev.args objectAtIndex:0];
        
        
        for (int i = 0; i < [tabBar.items count]; i++) {
            UITabBarItem* item = [tabBar.items objectAtIndex:i];
            if ([title isEqualToString:item.title]) {
                selectIndex = i;
                //                [tbController setSelectedIndex:i];
            }
            //                [UIEvent performTouchInView:(UIView *)item]; 
        }
    } 
    else if ([[[ev command] lowercaseString] isEqualToString:[MTCommandSelectIndex lowercaseString]]) {
        selectIndex = [[ev.args objectAtIndex:0] integerValue]-1;
    }
    
    if (selectIndex >= 0) {
        if (selectIndex > [tabBar.items count] || selectIndex < 0) {
            ev.lastResult = [NSString stringWithFormat:@"%@ out of index range for UITabBar with monkeyID %@",[ev.args objectAtIndex:0],ev.monkeyID];
            return;
        }
        if ([tbController respondsToSelector:@selector(setSelectedIndex:)]) {
                 [tbController setSelectedIndex:selectIndex];
        }
        // Added below condition, if tabbar does not responds to selectIndex, get the location of selectedItem and perform touchInView:at point.
        else{
            CGPoint itemAtLocation = [self locationOfTabItemInTabBar:tabBar withIndex:selectIndex];
            [UIEvent performTouchInView:self at:itemAtLocation];
        }
        
        if (tbController.delegate) {
            if ([tbController.delegate respondsToSelector:@selector(tabBarController:shouldSelectViewController:)]) {
                [tbController.delegate tabBarController:tbController shouldSelectViewController:tbController.selectedViewController];
            }
            if ([tbController.delegate respondsToSelector:@selector(tabBarController:didSelectViewController:)]) {
                [tbController.delegate tabBarController:tbController didSelectViewController:tbController.selectedViewController];
            }
        }
    }
    else
        ev.lastResult = [NSString stringWithFormat:@"Could not find %@ tab UITabBar with monkeyID %@", [ev.args objectAtIndex:0], ev.monkeyID];
}

// Returns CGPoint for an tabbar item with respect to index position
-(CGPoint) locationOfTabItemInTabBar:(UITabBar *)tabBar withIndex:(NSInteger) index {
    
    NSUInteger currentIndex = 0;
    
    for (UIView *subView in tabBar.subviews) {
        if ([subView isKindOfClass:NSClassFromString(@"UITabBarButton")]) {
            if (currentIndex == index) {
                return subView.frame.origin;
            }
            else
                currentIndex++;
        }
    }
    NSAssert(NO, @"Index out of Bounds");
    return CGPointZero;
}

@end
