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

#import "MonkeyTalkAPI.h"
#import "MonkeyTalk.h"
#import "MonkeyTalk+OCUnit.h"
#import "MTUtils.h"

@implementation MonkeyTalkAPI
+ (void) record:(UIView*)sender command:(NSString*)command args:(NSArray*)args {
	[[MonkeyTalk sharedMonkey] postCommandFrom:sender command:command args:args];
}

+ (void) continueRecording {
	[[MonkeyTalk sharedMonkey] continueMonitoring];
}

+ (NSString*) playCommands:(NSArray*)commands {
	[[MonkeyTalk sharedMonkey] loadCommands:commands];
	return [[MonkeyTalk sharedMonkey] playAndWait];
}

+ (NSString*) playFile:(NSString*)file {
	[[MonkeyTalk sharedMonkey] open:file];
	return [[MonkeyTalk sharedMonkey] playAndWait];
}

+ (UIView*) viewWithMonkeyID:(NSString*)monkeyID havingClass:(NSString*)className{
	Class class = NSClassFromString(className);
	return [MTUtils viewWithMonkeyID:monkeyID startingFromView:[MTUtils rootWindow] havingClass:class];
}

+ (void) runTestSuite:(SenTestSuite*)suite {
	[[MonkeyTalk sharedMonkey] runTestSuite:suite];
}

@end
