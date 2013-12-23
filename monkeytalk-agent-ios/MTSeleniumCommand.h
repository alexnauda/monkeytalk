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
#import "MTCommandEvent.h"
#import "MTElement.h"
#import "UIWebView+Selenium.h"


@interface MTSeleniumCommand : NSObject {
    MTCommandEvent *mtEvent;
    NSString *xPath;
    NSString *alternateID;
    NSString *command;
    NSDictionary *args;
    NSInteger currentOrdinal;
    NSString *htmlTag;
    MTElement *element;
}

@property (nonatomic, strong) MTCommandEvent *mtEvent;
@property (nonatomic, strong) NSString *xPath;
@property (nonatomic, strong) NSString *alternateID;
@property (nonatomic, strong) NSString *command;
@property (nonatomic, strong) NSDictionary *args;
@property (nonatomic, readwrite) NSInteger currentOrdinal;
@property (nonatomic, strong) NSString *htmlTag;
@property (nonatomic, strong) MTElement *element;
@property (nonatomic, unsafe_unretained)id delegate;

+ (id) initWithMTCommandEvent:(MTCommandEvent *)event;
- (id) initWithMTCommandEvent:(MTCommandEvent *)event;
+ (NSString *)convertedFromCommand:(MTCommandEvent *)event;
- (BOOL) playBackOnElement;
- (NSString *) basePath;
- (NSString *) finderExpression:(NSString *)selection;
- (MTElement *) elementFromXpath:(NSString *)xp;
- (NSArray *)argIndices:(NSString **)attribute;
- (void)tapElement;
- (void)tapElement:(MTElement *)e;
- (void)scrollToElement:(MTElement *)e touchPoint:(CGPoint)touchCenter;
- (void) valueForElement;
- (Boolean) useDefaultProperty;
- (Boolean) isOrdinal;
- (NSString *) attribute;
- (Boolean) hasAttribute;

@end
