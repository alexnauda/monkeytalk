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

#import "MTCommandEvent.h"
#import "MTUtils.h"
#import "MTWireKeys.h"
#import "MTOrdinalView.h"
#import "MTWebViewController.h"
#import "SBJsonWriterMT.h"
#import "MTConvertType.h"
#import "UIView+MTReady.h"
#import "UIView+MTFinder.h"

@implementation MTCommandEvent

//@synthesize source, command, className, monkeyID, args, dict;
@synthesize dict, found, didPlayInWeb, monkeyOrdinal, isWebRecording;

- (id) init {
	if (self = [super init]) {
		dict = [[NSMutableDictionary alloc] initWithCapacity:6];
		[self setCommand:@"Verify"];
		[self setMonkeyID:@""];
        [self setPlaybackDelay:@""];
        [self setPlaybackTimeout:@""];
		[self setClassName:@""];
		[self setArgs:[NSMutableArray arrayWithCapacity:1]];
        [self setModifiers:[NSMutableDictionary dictionaryWithCapacity:1]];
	}
	
	return self;
}


- (id) init:(NSString*)cmd className:(NSString*)name monkeyID:(NSString*)id args:(NSArray*)array {
	self = [self init];
	self.command = cmd;
	self.className = name;
	self.monkeyID = id;
    self.playbackDelay = [NSString stringWithFormat:@"%g",MT_DEFAULT_THINKTIME];
    self.playbackTimeout = [NSString stringWithFormat:@"%i",MT_DEFAULT_TIMEOUT];
	self.args = array;
	return self;
}

- (id) init:(NSString*)cmd className:(NSString*)name monkeyID:(NSString*)id delay:(NSString *)playback timeout:(NSString *)timeout args:(NSArray*)array modifiers:(NSDictionary *)theModifiers {
	self = [self init];
    
	self.command = cmd;
	self.className = name;
	self.monkeyID = id;
    self.value = nil;
    self.found = NO;
    self.didPlayInWeb = NO;
    self.isWebRecording = NO;
    
    if (playback)
        self.playbackDelay = playback;
    else
        self.playbackDelay = [NSString stringWithFormat:@"%i",MT_DEFAULT_THINKTIME];
    
    if (timeout)
        self.playbackTimeout = timeout;
    else
        self.playbackTimeout = [NSString stringWithFormat:@"%i",MT_DEFAULT_TIMEOUT];
    
	self.args = array;
    self.modifiers = theModifiers;
	return self;
}

- (id) init:(NSString*)cmd component:(NSString *)mtComponent className:(NSString*)name monkeyID:(NSString*)mid delay:(NSString *)cmdDelay timeout:(NSString *)timeout args:(NSArray*)cmdArgs modifiers:(NSDictionary *)mods {
	self = [self init:cmd className:name monkeyID:mid delay:cmdDelay timeout:timeout args:cmdArgs modifiers:mods];
	self.component = mtComponent;
	return self;
}

+ (MTCommandEvent*) command:(NSString*)cmd className:(NSString*)name monkeyID:(NSString*)id args:(NSArray*)array {
	return [[MTCommandEvent alloc] init:cmd className:name monkeyID:id args:array];
}

+ (MTCommandEvent*) command:(NSString*)cmd className:(NSString*)name monkeyID:(NSString*)id delay:(NSString *)playback timeout:(NSString *)timeout args:(NSArray*)array modifiers:(NSDictionary *)theModifiers {
	return [[MTCommandEvent alloc] init:cmd className:name monkeyID:id delay:playback timeout:timeout args:array modifiers:theModifiers];
}

+ (MTCommandEvent*) command:(NSString*)cmd component:(NSString *)mtComponent className:(NSString*)name monkeyID:(NSString*)mid delay:(NSString *)cmdDelay timeout:(NSString *)timeout args:(NSArray*)cmdArgs modifiers:(NSDictionary *)mods {
	return [[MTCommandEvent alloc] init:cmd component:mtComponent className:name monkeyID:mid delay:cmdDelay timeout:timeout args:cmdArgs modifiers:mods];
}

// protocl NSCopying
- (id) copyWithZone:(NSZone*)zone {
	return [MTCommandEvent command:self.command component:self.component className:self.className monkeyID:self.monkeyID delay:self.playbackDelay timeout:self.playbackTimeout args:self.args modifiers:self.modifiers];
}

- (id) initWithDict:(NSMutableDictionary*)dictionary {
	if (self = [super init]) {
		self.dict = dictionary;
	}

	return self;
}

- (UIView*) source {
    NSString *sourceClass = self.className;
    NSString *sourceId = self.monkeyID;
    
    if ([sourceClass isEqualToString:@"Script"]) {
        NSString *argsString = [self.args componentsJoinedByString:@" "];
        if ([argsString hasSuffix:@".html"])
            sourceClass = @"UIWebView";
    } 
    
	UIView* v = [UIView findViewFromEvent:self];
	if (v) {
		return v;
	}
    if (!self.skipWebView && ![MonkeyTalk sharedMonkey].isWireRecording) {
        UIWebView *webView = (UIWebView *)[MTOrdinalView currentWebView];
        
        if (webView != nil) {
            //NSLog(@"webview: %@",webView);
            if ([webView respondsToSelector:@selector(delegate)]) {
                MTWebViewController *webDriver = (MTWebViewController *)webView.delegate;
                
                if (webDriver && [webDriver respondsToSelector:@selector(playBackCommandInWebView:)]) {
                    if ([webDriver playBackCommandInWebView:self])
                        return [[MTFoundElement alloc] init];
                }
            }
        }
    }
	// Search again considering classes that can be swapped with the supplied class (ie, UIToolbarTextButton and UINavigationButton)
	return nil;
}

- (void) set:(NSString*)key value:(NSObject*)value {
	if (value == nil) {
		[dict removeObjectForKey:key];
		return;
	}
	[dict setObject:value forKey:key];
}

- (NSString*) command {
	return [dict objectForKey:@"command"];
}

- (void) setCommand:(NSString*)value {
	[self set:@"command" value:value];
}

- (NSString*) monkeyID {
	return [dict objectForKey:@"monkeyID"];

}
- (void) setMonkeyID:(NSString*)value {
	[self set:@"monkeyID" value:value];
}

- (NSString*) playbackDelay {
	return [dict objectForKey:@"playbackDelay"];
    
}
- (void) setPlaybackDelay:(NSString*)value {
	[self set:@"playbackDelay" value:value];
}

- (NSString*) playbackTimeout {
	return [dict objectForKey:@"playbackTimeout"];
    
}
- (void) setPlaybackTimeout:(NSString*)value {
	[self set:@"playbackTimeout" value:value];
}

- (NSString*) component {
	return [dict objectForKey:@"component"];
}

- (void) setComponent:(NSString*)value {
	[self set:@"component" value:value];
}

- (NSString*) className {
	return [dict objectForKey:@"className"];
}

- (void) setClassName:(NSString*)value {
	[self set:@"className" value:value];
}

- (NSArray*) args {
	return [dict objectForKey:@"args"];
}
	
- (void) setArgs:(NSArray*)value {
	[self set:@"args" value:value];
}

- (NSDictionary*) modifiers {
	return [dict objectForKey:@"modifiers"];
}

- (void) setModifiers:(NSDictionary *)value {
	[self set:@"modifiers" value:value];
}

- (NSString*) lastResult {
	return [dict objectForKey:@"lastResult"];
}

- (void) setLastResult:(NSString*)value {
	[self set:@"lastResult" value:value];
}

- (id) execute {
	return nil;
}

- (NSString*) value {
	return [dict objectForKey:@"value"];
}

- (void) setValue:(NSString*)value {
	[self set:@"value" value:value];
}

- (BOOL) screenshotOnError {
    NSString *val = [self.modifiers objectForKey:MTWireScreenshotOnError];
    return (val ? [val caseInsensitiveCompare:@"true"] == NSOrderedSame : NO);
}

- (BOOL) skipWebView {
    NSString *val = [self.modifiers objectForKey:MTWireSkipWebView];
    return (val ? [val caseInsensitiveCompare:@"true"] == NSOrderedSame : NO);
}

- (NSDictionary *) monkeyOrdinal {
    if (!monkeyOrdinal) {
        monkeyOrdinal = [[self class] monkeyOrdinalFromId:self.monkeyID];
    }
    
    return monkeyOrdinal;
}

+ (NSDictionary *) monkeyOrdinalFromId:(NSString *)monkeyId {
    NSDictionary *monkeyOrdinal = nil;
    NSString* regexString = @"\\(\\d+\\)$";
    NSError *error = nil;
    NSRegularExpression *regex = [[NSRegularExpression alloc]
                                  initWithPattern:regexString
                                  options:NSRegularExpressionCaseInsensitive error:&error];
    
    if (error) {
        NSLog(@"RegEx error.");
    }
    
    NSArray* regexResults = [regex matchesInString:monkeyId options:0
                                             range:NSMakeRange(0, [monkeyId length])];
    
    NSString* foundRegex = nil;
    
    for (NSTextCheckingResult* b in regexResults)
    {
        foundRegex = [monkeyId substringWithRange:b.range];
    }
    
    if (foundRegex) {
        NSString *mid = [monkeyId stringByReplacingOccurrencesOfString:foundRegex withString:@""];
        NSString *ordinal = [foundRegex stringByReplacingOccurrencesOfString:@"(" withString:@""];
        ordinal = [ordinal stringByReplacingOccurrencesOfString:@")" withString:@""];
        NSArray *objects = [NSArray arrayWithObjects:mid,ordinal, nil];
        NSArray *keys = [NSArray arrayWithObjects:@"mid",@"ordinal", nil];
        monkeyOrdinal = [[NSDictionary alloc] initWithObjects:objects forKeys:keys];
    }
    
    return monkeyOrdinal;
}

- (NSString *)printMonkeyId {
    if ([self.monkeyID rangeOfString:@" "].location == NSNotFound) {
        return self.monkeyID;
    } else {
        return [NSString stringWithFormat:@"\"%@\"", self.monkeyID];
    }
}

- (NSString *)printArgs {
    NSString *s = @"";
    
    for (NSString *arg in self.args) {
        if ([arg rangeOfString:@" "].location == NSNotFound) {
            s = [s stringByAppendingFormat:@" %@", arg];
        } else {
            s = [s stringByAppendingFormat:@" \"%@\"", arg];
        }
    }
    return s;
}

- (NSString *)printModifiers {
    NSString *s = @"";
    
    // we should only ignore default mods (ex: screenshotonerror=true, thinktime=500, timeout=2000)
    NSArray *ignore = [NSArray arrayWithObjects:MTWireTimeoutKey, MTWireThinkTimeKey, MTWireScreenshotOnError, nil];
    for (NSString *key in self.modifiers) {
        if (![ignore containsObject:key]) {
            NSString *val = [self.modifiers objectForKey:key];
            s = [s stringByAppendingFormat:@" %%%@=%@", key, val];
        }
    }
    return s;
}

- (NSString *)printCommand {
    return [NSString stringWithFormat:@"%@ %@ %@%@%@",
            self.className,
            self.printMonkeyId,
            self.command,
            self.printArgs,
            self.printModifiers];
}

- (NSString *) jsonEvent {
    SBJsonWriterMT *jsonWriter = [[SBJsonWriterMT alloc] init];
    NSMutableDictionary *jsonDict = [[NSMutableDictionary alloc] init];
    NSString *json = nil;
    
    NSDictionary *modifiers = [NSDictionary
                               dictionaryWithObject:self.playbackDelay
                               forKey:MTWireThinkTimeKey];
    
    // Convert component type to universal component
    NSString *componentType = [MTConvertType
                               convertedComponentFromString:self.className
                               isRecording:YES];
    
    //    NSString *componentType = self.className;
    
    // Replace monkeyID #1 with *
    // #-1 seems to be recorded on TabBar when one Tab lacks title
    // (may need to investigate further)
    if ([self.monkeyID isEqualToString:@"#1"] ||
        [self.monkeyID isEqualToString:@"#-1"])
        self.monkeyID = @"*";
    
    // Build json dict for command event
    [jsonDict setValue:MTWireVersionValue forKey:MTWireVersionKey];
    [jsonDict setValue:MTWireCommandRecord forKey:MTWireCommandKey];
    [jsonDict setValue:componentType forKey:MTWireComponentTypeKey];
    [jsonDict setValue:self.monkeyID forKey:MTWireMonkeyIdKey];
    [jsonDict setValue:self.command forKey:MTWireActionKey];
    [jsonDict setValue:self.args forKey:MTWireArgsKey];
    
    // Eventually send think time for recorded speed
    //    [jsonDict setValue:modifiers forKey:MTWireModifiersKey];
    
    
    json = [jsonWriter stringWithObject:jsonDict];
    
    return json;
}


@end
