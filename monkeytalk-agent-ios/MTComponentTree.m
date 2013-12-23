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

#import "MTComponentTree.h"
#import "MTOrdinalView.h"
#import "MonkeyTalk.h"
#import "UIView+MTReady.h"
#import "MTConvertType.h"
#import "MTCommandEvent.h"
#import "MTGetVariableCommand.h"
#import "MTWebViewController.h"
#import "MTWebJs.h"
#import "UIView+MTFinder.h"

@implementation MTComponentTree

+ (NSArray *) componentTree:(BOOL)skipWebView {
    NSMutableArray *tree = [NSMutableArray array];
    NSArray *all = [MTComponentTree componentTree:skipWebView withTree:tree];
    return all;
}

+ (NSArray *) componentTree:(BOOL)skipWebView withTree:(NSMutableArray*)tree {
    NSArray* roots=[MTComponentTree viewRoots:skipWebView];
    NSMutableDictionary *componentsDictionary = [[NSMutableDictionary alloc] init];
    for (UIView *root in roots) {
        NSDictionary *componentDictionary = [root componentTreeDictionary:componentsDictionary];
        [tree addObject:componentDictionary];
    }
    return tree;
}

+ (NSArray *) viewRoots:(BOOL)skipWebView {
    NSArray *found = [UIView orderedViews];
    //[MTOrdinalView buildFoundComponentsStartingFromView:nil havingClass:@"MTComponentTree" isOrdinalMid:NO skipWebView:skipWebView];
    //NSArray *found = [[NSArray alloc] initWithArray:[MonkeyTalk sharedMonkey].foundComponents];
    NSMutableArray *roots = [[NSMutableArray alloc] initWithCapacity:5];
    for (NSValue *value in found) {
        UIView *view = [value nonretainedObjectValue];
        if (![view superview]) {
            [roots addObject:view];
        }
    }
    //[found release];
    return roots;
}

+ (NSDictionary *) componentTreeForView:(UIView*)view skipWebView:(BOOL)skipWebView {
    NSString *component = [MTConvertType convertedComponentFromString:[NSString stringWithFormat:@"%@",view.class] isRecording:YES];
    NSString *className = [NSString stringWithFormat:@"%@",[view class]];
    
    NSString *visible = @"true";
    if (view.hidden) {
        visible = @"false";
    }
    
    NSString* value = [MTComponentTree valueForView:view asComponentType:component];
    
    NSString* mid = view.monkeyID;
    if (!mid) {
        mid = @"";
    }
    
    NSArray* identifiers = [view rawMonkeyIDCandidates];
    
    NSNumber* ordinal = [MTComponentTree ordinalForView:view];
    
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
    [dict setObject:component forKey:@"ComponentType"];
    [dict setObject:mid forKey:@"monkeyId"];
    [dict setObject:className forKey:@"className"];
    [dict setObject:visible forKey:@"visible"];
    [dict setObject:identifiers forKey:@"identifiers"];
    [dict setObject:ordinal forKey:@"ordinal"];
    [dict setObject:value forKey:@"value"];
    
    NSMutableArray *childArray = [[NSMutableArray alloc] init];
    [dict setObject:childArray forKey:@"children"];
    
    if ([view isKindOfClass:[UIWebView class]] && !skipWebView) {        
        NSDictionary *webComponentTree = [MTComponentTree componentTreeForWebView:(UIWebView*)view];
        if (webComponentTree) {
            [childArray addObject:webComponentTree];
        }
    } else {
        
        for (int i=0; i<[[view subviews] count]; i++) {
            UIView* subview=[[view subviews] objectAtIndex:i];
            [childArray addObject:[MTComponentTree componentTreeForView:subview skipWebView:skipWebView]];
        }
    }

    return dict;
}
    
+ (NSDictionary *) componentTreeForWebView: (UIWebView*)view {

    // assure we have MonkeyTalk support
    [view stringByEvaluatingJavaScriptFromString:MTWebJsString];
    
    // get the JSON array of components
    NSString* webComponentTreeJson = [(UIWebView*)view stringByEvaluatingJavaScriptFromString:@"MonkeyTalk.getComponentTreeJson();"];
    //NSLog(@"webComponentTreeJson:%@",webComponentTreeJson);
    
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

+ (NSNumber*) ordinalForView: (UIView*)view {
    NSInteger ordinal = [view mtOrdinal];
    if (!ordinal || ordinal<0) {
        ordinal=1;
    }
    return [NSNumber numberWithInteger:ordinal];
}

+ (NSString*) valueForView:(UIView*)view asComponentType:(NSString*)componentType {
    MTCommandEvent *event = [[MTCommandEvent alloc] init:MTCommandGet
                                               className:componentType
                                                monkeyID:view.monkeyID
                                                    args:[NSArray arrayWithObject:@"value"]];
    NSString* value = [MTGetVariableCommand execute:event];
    
    if (!value
            || ![value respondsToSelector:@selector(rangeOfString:)]
            || ([value rangeOfString:@"is not a valid keypath"].location != NSNotFound)) {
        value = @"";
    }
    
    return value;
}

@end
