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

#import "MTOrdinalView.h"
#import "MTUtils.h"
#import "NSString+MonkeyTalk.h"
#import "MTWebViewController.h"
#import "UIView+MTReady.h"

@implementation MTOrdinalView

+ (NSMutableArray *) foundComponents {
    return [MonkeyTalk sharedMonkey].foundComponents;
}

+ (NSArray *) componentsArrayForClass:(NSString *)className withMonkeyId:(NSString *)monkeyID {
    NSMutableArray *temp = [[NSMutableArray alloc] init];
    
    [MTOrdinalView buildFoundComponentsStartingFromView:nil havingClass:[NSString stringWithFormat:@"%@",className] isOrdinalMid:YES];
    [MTOrdinalView sortFoundComponents:[MonkeyTalk sharedMonkey].monkeyComponents];
    
    for (int i = 0; i < [[MonkeyTalk sharedMonkey].monkeyComponents count]; i++) {
        UIView *found = (UIView *)[[MonkeyTalk sharedMonkey].monkeyComponents objectAtIndex:i];
        NSString *foundID = found.baseMonkeyID;
        if (!monkeyID || [monkeyID isEqualToString:foundID]) {
            [temp addObject:found];
        } else {
            // search other candidates
            NSArray* candidates=[found rawMonkeyIDCandidates];
            for (int i=0; i<candidates.count; i++) {
                NSString* candidate = candidates[i];
                if (candidate && candidate.length>0 && [candidate isEqualToString:monkeyID]) {
                    [temp addObject:found];
                }
            }
        }
    }
    
    [[MonkeyTalk sharedMonkey].monkeyComponents removeAllObjects];
    [MonkeyTalk sharedMonkey].monkeyComponents = nil;
    
    return [NSArray arrayWithArray:temp];
}

+ (NSInteger) componentOrdinalForView:(UIView *)view withMonkeyID:(NSString *)monkeyID {
    if ([view hasMonkeyIdAssigned]) {
        return view.mtOrdinal;
    } else if (!view.isMTEnabled) {
        return 1;
    }
    
    NSInteger ordinal = 1;
    [MTOrdinalView buildFoundComponentsStartingFromView:nil havingClass:[NSString stringWithFormat:@"%@",[view class]] isOrdinalMid:YES];
    [MTOrdinalView sortFoundComponents:[MonkeyTalk sharedMonkey].monkeyComponents];
    
    NSMutableArray *temp = [[NSMutableArray alloc] init];
    
    for (int i = 0; i < [[MonkeyTalk sharedMonkey].monkeyComponents count]; i++) {
        UIView *found = (UIView *)[[MonkeyTalk sharedMonkey].monkeyComponents objectAtIndex:i];
        NSString *foundID = found.baseMonkeyID;
        //NSLog(@"looking: %@: %@",view,foundID);
        if (!monkeyID || [monkeyID isEqualToString:foundID]) {
            [temp addObject:found];
        }
    }
    
    if ([temp count] > 1) {
        for (int i = 0; i < [temp count]; i++) {
            UIView *found = (UIView *)[temp objectAtIndex:i];
            if (found == view) {
                ordinal = i + 1;
                break;
            }
        }
    }
    
    NSString *ordinalString = [NSString stringWithFormat:@"%i",ordinal];
    NSString *mid = view.baseMonkeyID;
    if ([mid isEqualToString:@"#mt"])
        mid = [NSString stringWithFormat:@"#%@",ordinalString];
    
    NSArray *objects = [NSArray arrayWithObjects:mid, ordinalString, nil];
    NSArray *keys = [NSArray arrayWithObjects:@"monkeyID", @"ordinal", nil];
    NSDictionary *item = [NSDictionary dictionaryWithObjects:objects forKeys:keys];
    [[MonkeyTalk sharedMonkey].componentMonkeyIds setObject:item forKey:[view keyForMonkeyId]];
    
    [[MonkeyTalk sharedMonkey].monkeyComponents removeAllObjects];
    [MonkeyTalk sharedMonkey].monkeyComponents = nil;
    [MonkeyTalk sharedMonkey].monkeyComponents;
    
    return ordinal;
}

+ (UIView*) buildFoundComponentsStartingFromView:(UIView*)current havingClass:(NSString *)classString {
    return [self buildFoundComponentsStartingFromView:current havingClass:classString isOrdinalMid:NO skipWebView:NO];
}

+ (UIView*) buildFoundComponentsStartingFromView:(UIView*)current havingClass:(NSString *)classString isOrdinalMid:(BOOL)isOrdinal {
    return [self buildFoundComponentsStartingFromView:current havingClass:classString isOrdinalMid:isOrdinal skipWebView:NO];
}

+ (UIView*) buildFoundComponentsStartingFromView:(UIView*)current havingClass:(NSString *)classString isOrdinalMid:(BOOL)isOrdinal skipWebView:(BOOL)skipWebView {
    Class class = objc_getClass([classString UTF8String]);
    
    if (!current) {
		current =  [MTUtils rootWindow];
	}
    
    //NSString *currentClassString = NSStringFromClass([current class]);
    //NSLog(@"find class: %@",classString);
    
    if (classString == nil) { 
        return current;
    }
    
    BOOL isButton = [MTUtils shouldRecord:classString view:current];
    
    if ([classString.lowercaseString isEqualToString:@"mtcomponenttree"]) {
        
        if (isOrdinal) {
            if (![MonkeyTalk sharedMonkey].monkeyComponents) {
                [MonkeyTalk sharedMonkey].monkeyComponents = [[NSMutableArray alloc] init];
            }
            
            [[MonkeyTalk sharedMonkey].monkeyComponents addObject:current];
        } else {
            if (![MonkeyTalk sharedMonkey].foundComponents) {
                [MonkeyTalk sharedMonkey].foundComponents = [[NSMutableArray alloc] init];
            }

            [[MonkeyTalk sharedMonkey].foundComponents addObject:current];
        }
    } else if ( (classString != nil && ([current isKindOfClass:class] || isButton))) {
        // Build array with components of class looking for
        // UILabel must be exact class
        if ([current isKindOfClass:class] ||
            ![classString isEqualToString:@"UILabel"] || 
            [classString.lowercaseString isEqualToString:@"itemselector"] ||
            [classString.lowercaseString isEqualToString:@"indexedselector"]) {
            
            if (isOrdinal) {
                if (![MonkeyTalk sharedMonkey].monkeyComponents) {
                    [MonkeyTalk sharedMonkey].monkeyComponents = [[NSMutableArray alloc] init];
                }
                
                [[MonkeyTalk sharedMonkey].monkeyComponents addObject:current];
                //NSLog(@"count: %i",[MonkeyTalk sharedMonkey].monkeyComponents.count);
            } else {
                if (![MonkeyTalk sharedMonkey].foundComponents) {
                    [MonkeyTalk sharedMonkey].foundComponents = [[NSMutableArray alloc] init];
                }
                
                [[MonkeyTalk sharedMonkey].foundComponents addObject:current];
            }
        }
    }
	
	if (!current.subviews) {
		return nil;
	}
    
    NSArray *subviews = [[NSArray alloc] initWithArray:current.subviews];
	
    for (UIView* view in [[subviews reverseObjectEnumerator] allObjects]) {
		UIView* result = [self buildFoundComponentsStartingFromView:view havingClass:classString isOrdinalMid:isOrdinal skipWebView:skipWebView];
		if (result) {
			return result;
		}
	}
    
    return nil;
}

+ (void) sortFoundComponents {
    [[self class] sortFoundComponents:[MonkeyTalk sharedMonkey].foundComponents];
}

+ (void) sortFoundComponents:(NSMutableArray *)components {
    if ([components count] > 0) {
        [components sortUsingComparator:  
         ^NSComparisonResult (id obj1, id obj2) {  
             UIView *view1 = (UIView *)obj1;
             UIView *view2 = (UIView *)obj2;
             CGPoint point1 = [view1 convertPoint:view1.frame.origin toView:nil];
             CGPoint point2 = [view2 convertPoint:view2.frame.origin toView:nil];  
             
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
}

+ (UIWebView *) currentWebView {
    UIView *root =  [MTUtils rootWindow];
    
    return [root currentWebView];
}

+ (UIView *) viewWithOrdinal:(NSInteger)ordinal startingFromView:(UIView *)current havingClass:(NSString *)classString skipWebView:(BOOL)skipWebView {
    
    // Find all components of class
    [[self class] buildFoundComponentsStartingFromView:current 
                                           havingClass:classString
                                          isOrdinalMid:NO
                                           skipWebView:skipWebView];
    
    // Order components based on position on screen
    [[self class] sortFoundComponents];
    
    @try {
        for (int i = 0; i < [[MonkeyTalk sharedMonkey].foundComponents count]; i++) {
            if (ordinal > 0 && i == ordinal-1)
                return [[MonkeyTalk sharedMonkey].foundComponents objectAtIndex:i];
        }
    }
    @catch (NSException *exception) {
        // Handle error
    }
    
    return nil;
}

@end
