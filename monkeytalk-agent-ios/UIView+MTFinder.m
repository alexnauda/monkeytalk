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

#import "UIView+MTFinder.h"
#import "UIView+MTReady.h"
#import "MonkeyTalk.h"
#import "MTConvertType.h"
#import "MTUtils.h"
#import "MTWebJs.h"
#import "MTValueFromProperty.h"
#import "MTComponentTree.h"

@implementation UIView (MTFinder)
- (NSString *)mtComponent {
    if (self.class == [UIView class]) {
        return MTComponentView;
    } else if (self.class == [UIToolbar class]) {
        return MTComponentToolBar;
    } else if (self.class == NSClassFromString(@"UINavigationItemButtonView")) {
        return MTComponentButton;
    } else if (self.class == NSClassFromString(@"UINavigationButton")) {
        return MTComponentButton;
    } else if (self.class == NSClassFromString(@"UIThreePartButton")) {
        return MTComponentButton;
    } else if (self.class == NSClassFromString(@"UIRoundedRectButton")) {
        return MTComponentButton;
    } else if (self.class == NSClassFromString(@"UIToolbarTextButton")) {
        return MTComponentButton;
    } else if (self.class == NSClassFromString(@"UIAlertButton")) {
        return MTComponentButton;
    } else if (self.class == NSClassFromString(@"_UISwitchInternalView")) {
        return MTComponentToggle;
    } else if (self.class == NSClassFromString(@"_UIWebViewScrollView")) {
        return MTComponentScroller;
    }
    
    return NSStringFromClass(self.class);
}

- (NSDictionary *)componentTreeForWebView {
    
    // assure we have MonkeyTalk support
    [(UIWebView*)self stringByEvaluatingJavaScriptFromString:MTWebJsString];
    
    // get the JSON array of components
    NSString* webComponentTreeJson = [(UIWebView*)self stringByEvaluatingJavaScriptFromString:@"MonkeyTalk.getComponentTreeJson();"];
    webComponentTreeJson = [webComponentTreeJson stringByReplacingOccurrencesOfString:@"\\n" withString:@""];
    
    if (webComponentTreeJson.length > 0) {
        NSError *error=nil;
        
        // convert to dictionary format for eventual re-serialization to JSON
        NSDictionary *webComponentTree = [NSJSONSerialization JSONObjectWithData:[webComponentTreeJson dataUsingEncoding:NSUTF8StringEncoding] options:kNilOptions error:&error];
        if (!error) {
            return webComponentTree;
        } else {
            NSLog(@"ERROR parsing web component tree JSON: %@", error.description);
        }
    }
    
    return nil;
}

- (NSDictionary *) componentTreeDictionary:(NSMutableDictionary *)allComponents {
    NSString *component = [self mtComponent];
    NSString *className = NSStringFromClass(self.class);
    
    NSArray *components = [allComponents objectForKey:className];
    if (!components) {
        components = [self.class orderedViews];
        [allComponents setValue:components forKey:className];
    }
    
    NSValue *selfValue = [NSValue valueWithNonretainedObject:self];
    NSInteger indexMonkeyId = 0;
    for (NSValue *value in components) {
        UIView *view = [value nonretainedObjectValue];
        
        if (![view.baseMonkeyID isEqualToString:@"#mt"] && [view.baseMonkeyID isEqualToString:self.baseMonkeyID]) {
            indexMonkeyId++;
        }
        
        if ([value isEqualToValue:selfValue]) {
            break;
        }
    }
    
    NSString *visible = @"true";
    if (self.hidden || (CGRectEqualToRect(self.frame, CGRectZero) && self.clipsToBounds) || self.alpha == 0) {
        visible = @"false";
        return nil;
    }

    NSString* value = [self valueAsComponentType:component];
    NSString* mid = self.baseMonkeyID;
    if (!mid) {
        mid = @"";
    }
    
    if (!value) {
        value = @"";
    }
    
    NSArray* identifiers = [[NSArray alloc] initWithArray:[self rawMonkeyIDCandidates]];
    NSInteger ordinalInteger = [components indexOfObject:[NSValue valueWithNonretainedObject:self]] + 1;
    NSNumber* ordinal = [NSNumber numberWithInteger:ordinalInteger];
    
    if ([mid isEqualToString:@"#mt"]) {
        mid = [NSString stringWithFormat:@"#%i",ordinalInteger];
    } else if (indexMonkeyId > 1) {
        mid = [mid stringByAppendingFormat:@"(%i)",indexMonkeyId];
    }
    
    NSMutableDictionary *dict = [NSMutableDictionary dictionaryWithObject:component forKey:@"ComponentType"];
    [dict setObject:mid forKey:@"monkeyId"];
    [dict setObject:className forKey:@"className"];
    [dict setObject:visible forKey:@"visible"];
    [dict setObject:identifiers forKey:@"identifiers"];
    [dict setObject:ordinal forKey:@"ordinal"];
    [dict setObject:value forKey:@"value"];
    
    NSMutableArray *childArray = [NSMutableArray array];
    [dict setObject:childArray forKey:@"children"];
    
    if ([self isKindOfClass:[UIWebView class]]) {
        NSDictionary *webComponentTree = [self componentTreeForWebView];
        if (webComponentTree) {
            [childArray addObject:webComponentTree];
        }
    } else {
        for (int i=0; i<[[self subviews] count]; i++) {
            UIView* subview=[[self subviews] objectAtIndex:i];
            NSDictionary *componentDictionary = [subview componentTreeDictionary:allComponents];
            
            if (componentDictionary) {
                [childArray addObject:componentDictionary];
            }
        }
    }
    
    className = nil;
    
    return dict;
}

- (void)constructComponentTree:(NSMutableArray *)tree {
    // skip hidden views and their subviews
    if (self.hidden || self.alpha == 0) {
        return;
    }
    [tree addObject:[NSValue valueWithNonretainedObject:self]];
    for (UIView *subview in self.subviews) {
        [subview constructComponentTree:tree];
    }
}

+ (NSArray *)unorderedViews {
    NSMutableArray *tree = [[NSMutableArray alloc] init];
    [[MTUtils rootWindow] constructComponentTree:tree];
    NSMutableArray *unorderedViews = [[NSMutableArray alloc] initWithArray:tree];
    
    int i = 0;
    for (NSValue *value in unorderedViews) {
        UIView *view = [value nonretainedObjectValue];
        
        if (view.subviews.count > 0) {
            i += view.subviews.count;
        } else {
            i++;
        }
    }
    
    return unorderedViews;
}

+ (NSArray *)orderedViews {
    NSMutableArray *tree = [NSMutableArray array];
    UIView *root = [MTUtils rootWindow];
    [root constructComponentTree:tree];
    
    for (UIView *view in [UIApplication sharedApplication].windows) {
        if (![[NSValue valueWithNonretainedObject:root] isEqualToValue:[NSValue valueWithNonretainedObject:view]]) {
            [view constructComponentTree:tree];
        }
    }
    
    NSMutableArray *orderedViews = [NSMutableArray array];
    NSArray *aliased = [self aliased];
    
    if (aliased) {
        for (NSString *classString in aliased) {
            Class class = NSClassFromString(classString);
            [orderedViews addObjectsFromArray:[class orderedViews]];
        }
    }
    
    if ([tree count] > 0) {
        [tree sortUsingComparator:^NSComparisonResult (NSValue *obj1, NSValue *obj2) {
            UIView *view1 = (UIView *)[obj1 nonretainedObjectValue];
            UIView *view2 = (UIView *)[obj2 nonretainedObjectValue];
            CGPoint point1 = [view1 convertPoint:view1.frame.origin toView:[MTUtils rootWindow]];
            CGPoint point2 = [view2 convertPoint:view2.frame.origin toView:[MTUtils rootWindow]];
            
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
        }];
    }
    
    for (NSValue *value in tree) {
        UIView *view = [value nonretainedObjectValue];
        BOOL shouldAddToPossibleComponents = [view isKindOfClass:self];
        
        if (shouldAddToPossibleComponents) {
            [orderedViews addObject:value];
        }
    }
    
    [tree removeAllObjects];
    //[tree release];
    return orderedViews;
}

+ (NSArray *)aliased {
    return nil;
}

+ (NSArray *)orderedViewsWithMonkeyId:(NSString *)monkeyId {
    if ([monkeyId rangeOfString:@"*"].location != NSNotFound ||
        [monkeyId rangeOfString:@"?"].location != NSNotFound) {
        return [self orderedViewsWithWildcardMonkeyId:monkeyId];
    }
    
    NSMutableArray *orderedViewsWithMonkeyId = [[NSMutableArray alloc] init];
    NSArray *orderedViews = [self orderedViews];
    
    for (NSValue *value in orderedViews) {
        UIView *view = [value nonretainedObjectValue];
        BOOL monkeyIdMatches = [view.baseMonkeyID isEqualToString:monkeyId] || [view.rawMonkeyIDCandidates containsObject:monkeyId];
        
        if (monkeyIdMatches) {
            [orderedViewsWithMonkeyId addObject:value];
        }
    }
    return orderedViewsWithMonkeyId;
}

+ (NSMutableArray *)orderedViewsWithWildcardMonkeyId:(NSString *)monkeyId{
    NSError *error = nil;
    NSMutableArray *orderedViewsWithMonkeyId = [[NSMutableArray alloc] init];
    NSArray *orderedViews = [self orderedViews];
    NSString *monkeyIdRegex = [MTUtils buildWildCardRegularExpression:monkeyId];
    NSRegularExpression *regex = [[NSRegularExpression alloc] initWithPattern:monkeyIdRegex
                                                                      options:0
                                                                        error:&error];
    
    for (NSValue *value in orderedViews) {
        UIView *view = [value nonretainedObjectValue];
        NSString *mid = view.monkeyID;
        NSTextCheckingResult *b = [regex firstMatchInString:mid
                                                    options:NSRegularExpressionDotMatchesLineSeparators
                                                      range:NSMakeRange(0, [mid length])];
        NSString *foundComponent = [mid substringWithRange:b.range];
        BOOL monkeyIdMatches = [foundComponent isEqualToString:mid];
        if (monkeyIdMatches) {
            [orderedViewsWithMonkeyId addObject:value];
        }
    }
    
    return orderedViewsWithMonkeyId;
}

+ (UIView *)findViewFromEvent:(MTCommandEvent *)event {
    NSString *classString = [MTConvertType convertedComponentFromString:event.className isRecording:NO] ? [MTConvertType convertedComponentFromString:event.className isRecording:NO] : event.className;
    Class findClass = NSClassFromString(classString);
    
    // no classes with classString found
    if (![findClass respondsToSelector:@selector(orderedViews)]) {
        return nil;
    }
    
    UIView *foundView = nil;
    NSRegularExpression *ordinalRegex = [NSRegularExpression regularExpressionWithPattern:@"^\\#(\\d+)$" options:NSRegularExpressionDotMatchesLineSeparators error:nil];
    NSRegularExpression *indexRegex = [NSRegularExpression regularExpressionWithPattern:@"^(.+?)\\((\\d+)\\)$" options:NSRegularExpressionDotMatchesLineSeparators error:nil];
    BOOL lookingForOrdinal = [ordinalRegex foundMatchInString:event.monkeyID];
    __block NSString *monkeyId = event.monkeyID;
    __block NSInteger matchIndex = lookingForOrdinal ? [[event.monkeyID stringByReplacingOccurrencesOfString:@"#" withString:@""] integerValue]-1 : 0;
    
    // find the first view for monkeyId * and ?
    if ([monkeyId isEqualToString:@"*"] || [monkeyId isEqualToString:@"?"])
    lookingForOrdinal = YES;
    
    [indexRegex enumerateMatchesInString:monkeyId options:NSMatchingReportProgress range:NSMakeRange(0, [monkeyId length]) usingBlock:^(NSTextCheckingResult *result, NSMatchingFlags flags, BOOL *stop){
        NSInteger monkeyIdRange = 1;
        NSInteger indexRange = 2;
        matchIndex = [[monkeyId substringWithRange:[result rangeAtIndex:indexRange]] integerValue]-1;
        monkeyId = [monkeyId substringWithRange:[result rangeAtIndex:monkeyIdRange]];
    }];
    
    NSArray *possibleComponents = lookingForOrdinal ? [findClass orderedViews] : [findClass orderedViewsWithMonkeyId:monkeyId];
    
    if (possibleComponents.count > 0 && possibleComponents.count > matchIndex) {
        NSValue *value = [possibleComponents objectAtIndex:matchIndex];
        foundView = [value nonretainedObjectValue];
        
        // ignore UITextFieldLabel as labels
        if ([event.component.lowercaseString isEqualToString:MTComponentLabel.lowercaseString] &&
            foundView.class == NSClassFromString(@"UITextFieldLabel")) {
            while (foundView) {
                if (foundView.class == [UILabel class]) {
                    break;
                }
                
                if (possibleComponents.count > matchIndex+1) {
                    value = [possibleComponents objectAtIndex:matchIndex++];
                    foundView = [value nonretainedObjectValue];
                } else {
                    foundView = nil;
                }
            }
        }
    }
    
    return foundView;
}

- (NSString*) valueAsComponentType:(NSString*)componentType {
    NSString *value = @"";
    @try {
        value = [MTValueFromProperty valueFromProp:@"value" forView:self];
    }
    @catch (NSException *exception) {
        // failed
    }
    return value;
}

@end