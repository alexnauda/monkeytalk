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

#import "UISegmentedControl+MTReady.h"
#import "MonkeyTalk.h"
#import "MTCommandEvent.h"
#import "MTUtils.h"
#import "UISegmentedControlProxy.h"
#import "NSString+MonkeyTalk.h"
#import "UIView+MTReady.h"
#import "TouchSynthesis.h"
#import <objc/runtime.h>
#import "MTUtils.h"
#import "NSObject+MTReady.h"

@interface UISegmentedControl (MTDummy)
- (NSString *)infoName;
@end

@implementation UISegmentedControl (MTReady)
- (NSString *)mtComponent {
    return MTComponentButtonSelector;
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
    
    NSInteger row = arg1Int;
    
    UISegmentedControlProxy *tmp = (UISegmentedControlProxy *)self;
    
    if ([prop isEqualToString:@"value" ignoreCase:YES]) { 
        result = [tmp titleForSegmentAtIndex:self.selectedSegmentIndex];
    } else if ([prop isEqualToString:@"item" ignoreCase:YES]) {
        result = [tmp titleForSegmentAtIndex:row];
    } else if([prop isEqualToString:@"size" ignoreCase:YES]) {
        result = [NSString stringWithFormat:@"%i",self.numberOfSegments];
    } else
        result = [self valueForKeyPath:prop];
    
    return result;
}

+ (void)load {
    if (self == [UISegmentedControl class]) {
        [NSObject swizzle:@"initWithItems:" with:@"mtinitWithItems:" for:self];
        [NSObject swizzle:@"initWithFrame:" with:@"mtinitWithFrame:" for:self];
        [NSObject swizzle:@"initWithCoder:" with:@"mtinitWithCoder:" for:self];
    }
}

- (id)mtinitWithItems:(NSArray *)items {
    [self mtinitWithItems:items];
    
    if (self) {
        [self addTarget:self action:@selector(mtSegmentedControlDidChange) forControlEvents:UIControlEventValueChanged];
    }
    
    return self;
}

- (id)mtinitWithFrame:(CGRect)frame {
    [self mtinitWithFrame:frame];
    
    if (self) {
        [self addTarget:self action:@selector(mtSegmentedControlDidChange) forControlEvents:UIControlEventValueChanged];
    }
    
    return self;
}

- (id)mtinitWithCoder:(NSCoder *)aDecoder {
    [self mtinitWithCoder:aDecoder];
    
    if (self) {
        [self addTarget:self action:@selector(mtSegmentedControlDidChange) forControlEvents:UIControlEventValueChanged];
    }
    
    return self;
}

- (void) playbackMonkeyEvent:(id)event {
    MTCommandEvent* ev = event;
    if (([[ev command] isEqualToString:MTCommandTouch] || 
         [[ev command] isEqualToString:MTCommandSelectIndex ignoreCase:YES] ||
         [[ev command] isEqualToString:MTCommandSelect ignoreCase:YES])) {
            UISegmentedControlProxy *tmp = (UISegmentedControlProxy *)self;
            if ([[ev args] count] == 0) {
                ev.lastResult = @"Requires 1 argument, but has %d", [ev.args count];
                return;
            }	
            int index;
            int i;
            NSString* title =(NSString*) [ev.args objectAtIndex:0];
            for (i = 0; i < [tmp numberOfSegments]; i++) {
                
                NSString* t = [tmp titleForSegmentAtIndex:i];
                
                if (t == nil)  {
                    // MT6: ToDo Fix to handle with no title (iOS5)
                    index = [title intValue]-1;
                    // Need to use undocumented property that contains array of "segments" (subviews that are the buttons)	
                    
                    
                    if ([MTUtils isOs5Up]) {
                        NSMutableArray *segmentsArray = [NSMutableArray arrayWithArray:[tmp subviews]];
                        if ([segmentsArray count] > 0) {
                            [segmentsArray sortUsingComparator:  
                             ^NSComparisonResult (id obj1, id obj2) {  
                                 UIView *view1 = (UIView *)obj1;
                                 UIView *view2 = (UIView *)obj2;
                                 CGPoint point1 = view1.frame.origin;
                                 CGPoint point2 = view2.frame.origin;  
                                 
                                 if (point1.y > point2.y)  
                                     return NSOrderedDescending;  
                                 else if (point1.y < point2.y)  
                                     return NSOrderedAscending;
                                 else if (point1.x > point2.x)  
                                     return NSOrderedDescending;  
                                 else if (point1.x < point2.x)  
                                     return NSOrderedAscending; 
                                 else  
                                     return NSOrderedSame;  
                             }  
                             ];
                        }
                        
                        for (int i = 0; i < [segmentsArray count]; i++) {
                            id segment = [segmentsArray objectAtIndex:i];
                            if (i == index) {
                                [UIEvent performTouchInView:(UIView *)segment];
                                return;
                            }
                        }
                    } else {
                        [UIEvent performTouchInView:(UIView*) [tmp->_segments objectAtIndex:index]];
                        
                        return;
                    }
                } else {
                    if ([t isEqualToString:title]) {
                        // Need to use undocumented property that contains array of "segments" (subviews that are the buttons)
                        
                        if ([MTUtils isOs5Up]) {
                            for (id segment in [tmp subviews]) {
                                NSString *foundTitle = (NSString *)[(UISegmentedControl *)segment infoName];
                                
                                if ([title isEqualToString:foundTitle])
                                    [UIEvent performTouchInView:(UIView *)segment];
                            }
                        } else
                            [UIEvent performTouchInView:(UIView*) [tmp->_segments objectAtIndex:i]];
                        
                        return;
                    }
                }
            }
        
        ev.lastResult = [NSString stringWithFormat:@"Unable to find %@ in ButtonSelector %@", title, ev.monkeyID];
    } else {
        [super playbackMonkeyEvent:event];
    }
}

- (void)mtSegmentedControlDidChange {
    UISegmentedControlProxy *tmp = (UISegmentedControlProxy *)self;
    int index = tmp.selectedSegmentIndex;
    if (index < 0) {
        return;
    }
    NSString* title = [tmp titleForSegmentAtIndex:index];
    NSString* command = MTCommandSelect;
    
    if (title == nil) {
        title = [NSString stringWithFormat:@"%d", index+1];
        command = MTCommandSelectIndex;
    }
    
    [MonkeyTalk recordFrom:self command:command args:[NSArray arrayWithObject:title]];
}

- (void) handleMonkeyTouchEvent:(NSSet*)touches withEvent:(UIEvent*)event {
    // moved recording to mtSegmentedControlDidChange
}
@end
