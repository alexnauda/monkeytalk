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

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "MonkeyTalk.h"

@interface MTOrdinalView : NSObject {
    
}

+ (NSMutableArray *) foundComponents;
+ (NSInteger) componentOrdinalForView:(UIView *)view withMonkeyID:(NSString *)monkeyID;
+ (void) sortFoundComponents;

+ (UIView*) buildFoundComponentsStartingFromView:(UIView*)current havingClass:(NSString *)classString;
+ (UIView*) buildFoundComponentsStartingFromView:(UIView*)current havingClass:(NSString *)classString isOrdinalMid:(BOOL)ordinal;
+ (UIView*) buildFoundComponentsStartingFromView:(UIView*)current havingClass:(NSString *)classString isOrdinalMid:(BOOL)ordinal skipWebView:(BOOL)skipWebView;
+ (NSArray *) componentsArrayForClass:(NSString *)className withMonkeyId:(NSString *)monkeyID;
+ (UIView *) viewWithOrdinal:(NSInteger)ordinal startingFromView:(UIView *)current havingClass:(NSString *)classString skipWebView:(BOOL)skipWebView;

+ (UIWebView *) currentWebView;

@end
