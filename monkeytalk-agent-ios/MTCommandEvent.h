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
#import <Foundation/NSArray.h>

/**
 The MonkeyTalk command object used for recording and playback.
 */
@interface MTCommandEvent : NSObject <NSCopying> {
	UIView* source;
	NSString* command;
    NSString* component;
	NSString* className;
	NSString* monkeyID;
	NSString* playbackDelay;
    NSString* playbackTimeout;
	NSArray* args;
	NSMutableDictionary* dict;
	NSString* lastResult;
    NSString* value;
    Boolean found;
    Boolean didPlayInWeb;
    Boolean isWebRecording;
    NSDictionary *monkeyOrdinal;
}

+ (MTCommandEvent*) command:(NSString*)cmd className:(NSString*)name monkeyID:(NSString*)id args:(NSArray*)array;
+ (MTCommandEvent*) command:(NSString*)cmd className:(NSString*)name monkeyID:(NSString*)id delay:(NSString *)playback timeout:(NSString *)timeout args:(NSArray*)array modifiers:(NSDictionary *)theModifiers;
+ (MTCommandEvent*) command:(NSString*)cmd component:(NSString *)mtComponent className:(NSString*)name monkeyID:(NSString*)mid delay:(NSString *)cmdDelay timeout:(NSString *)timeout args:(NSArray*)cmdArgs modifiers:(NSDictionary *)mods;
- (id) init:(NSString*)cmd className:(NSString*)className monkeyID:(NSString*)monkeyID args:(NSArray*)args;
- (id) init:(NSString*)cmd className:(NSString*)name monkeyID:(NSString*)id delay:(NSString *)playback timeout:(NSString *)timeout args:(NSArray*)array modifiers:(NSDictionary *)theModifiers;
- (id) initWithDict:(NSMutableDictionary*)dict;
- (id) execute;
+ (NSDictionary *) monkeyOrdinalFromId:(NSString *)monkeyId;
- (NSString *)printMonkeyId;
- (NSString *)printArgs;
- (NSString *)printModifiers;
- (NSString *)printCommand;
- (NSString *) jsonEvent;

@property (unsafe_unretained, readonly) UIView* source;
@property (nonatomic, strong) NSString* command;
@property (nonatomic, strong) NSString* component;
@property (nonatomic, strong) NSString* className;
@property (nonatomic, strong) NSString* monkeyID;
@property (nonatomic, strong) NSString* playbackDelay;
@property (nonatomic, strong) NSString* playbackTimeout;
@property (nonatomic, strong) NSString* lastResult;
@property (nonatomic, strong) NSArray* args;
@property (nonatomic, strong) NSMutableDictionary* dict;
@property (nonatomic, strong) NSMutableDictionary* modifiers;
@property (nonatomic, strong) NSString* value;
@property (nonatomic, readwrite) Boolean found;
@property (nonatomic, readwrite) Boolean didPlayInWeb;
@property (nonatomic, readwrite) Boolean isWebRecording;
@property (nonatomic, strong) NSDictionary *monkeyOrdinal;
@property (nonatomic, readonly) BOOL screenshotOnError;
@property (nonatomic, readonly) BOOL skipWebView;

@end
