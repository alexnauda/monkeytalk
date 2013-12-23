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
#import "MTCommandEvent.h"
#import "NSRegularExpression+MonkeyTalk.h"

#define MTOptionsLiveSwitch @"MTLiveSwitch"
#define MTOptionsLive @"MTLive"
#define MTOptionsFixed @"MTFixed"
#define MTOptionsTimeout @"MTTimeout"

// Set default pause to 1/2 sec between commands
#define MT_DEFAULT_THINKTIME 500
// Set default timeout (before retry gives up)
#define MT_DEFAULT_TIMEOUT 1000
// 100ms default timeout in microseconds (before retry gives up)
#define MT_DEFAULT_RETRY_DELAY 100000

@interface MTUtils : NSObject {

}

+ (UIWindow*) rootWindow;
+ (UIView*) viewWithMonkeyID:(NSString*)mid startingFromView:(UIView*)current havingClass:(Class)class;
+ (UIView*) viewWithMonkeyID:(NSString*)mid startingFromView:(UIView*)current havingClass:(Class)class swapsOK:(BOOL)swapsOK;

+ (NSInteger) ordinalForView:(UIView*)view;
+ (UIView*) findFirstMonkeyView:(UIView*)current;
+ scriptPathForFilename:(NSString*) fileName;
+ (BOOL)writeApplicationData:(NSData *)data toFile:(NSString *)fileName;
+ (BOOL)writeString:(NSString *)string toFile:(NSString *)fileName;
+ (NSData *)applicationDataFromFile:(NSString *)fileName;
+ (NSString*) scriptsLocation;
+ (void) navLeft:(UIView*)view to:(UIView*)to;
+ (void) navRight:(UIView*)view from:(UIView*)from;
+ (void) slideOut:(UIView*) view;
+ (void) slideIn:(UIView*) view;
+ (void) dismissKeyboard;
+ (void) shake;
+ (BOOL) isKeyboard:(UIView*)view;
+ (NSString*) stringByJsEscapingQuotesAndNewlines:(NSString*) unescapedString;
+ (NSString*) stringByOcEscapingQuotesAndNewlines:(NSString*) unescapedString;
+ (NSString*) stringForQunitTest:(NSString*)testCase inModule:(NSString *)module withResult:(NSString *)resultMessage;
+ (void) writeQunitToXmlWithString:(NSString*)xmlString testCount:(NSString *)testCount failCount:(NSString *)failCount runTime:(NSString *)runTime fileName:(NSString *)fileName;
+ (void) setShouldRecordMonkeyTouch:(BOOL)shouldRecord forView:(UIView*)view;
+ (NSString*) className:(NSObject*)ob;
+ (void) rotate:(UIInterfaceOrientation)orientation;
+ (void) interfaceChanged:(UIView *)view;
+ (void)rotateInterface:(UIView *)view degrees:(NSInteger)degrees frame:(CGRect)frame;
+ (NSString *)timeStamp;
+ (NSArray *) replaceArgForCommand:(NSArray *)commandArgs variables:(NSDictionary *)jsVariable;
+ (NSString *) replaceMonkeyIdForCommand:(NSString *)monkeyId variables:(NSDictionary *)jsVariable;
+ (NSString *) replaceString:(NSString *)origString variables:(NSDictionary *)jsVariable;
+ (NSMutableDictionary *) addVarsFrom:(MTCommandEvent *)event to:(NSMutableDictionary *)dict;
+ (void) saveUserDefaults:(NSString*)value forKey:(NSString*)key;
+ (NSString *) retrieveUserDefaultsForKey:(NSString*)key;
+ (double) playbackSpeedForCommand:(MTCommandEvent *)nextCommandToRun isLive:(BOOL)isLive retryCount:(int)retryCount;
+ (double) timeoutForCommand:(MTCommandEvent *)nextCommandToRun;
+ (BOOL) isOs5Up;
+ (UIWebView *) currentWebView;

+ (BOOL) shouldRecord:(NSString *)classString view:(UIView *)current;
+ (UIView*) viewWithCommandEvent:(MTCommandEvent *)event;
+ (NSString *) buildWildCardRegularExpression:(NSString *)wildCardPattern;

+ (UIImage *)screenshot;
+ (NSString *) encodedScreenshot;
+ (NSString *) screenshotFileName:(MTCommandEvent*) command;
+ (NSString *) appDirectory;

+ (NSMutableArray *) componentsForWildCardMonkeyId:(NSString *)wildCardId className:(NSString *) className ;
+ (NSString *) getMatchedStringForRegEx:(NSRegularExpression *) regex inString:(NSString *) inString;
+ (void)saveScreenshot:(NSString *)filename;
@end
