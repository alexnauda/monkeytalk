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

/** \mainpage MonkeyTalk API Documentation
 * <div style="border:1px solid black; font-size:12pt;width:50%; margin:5%;padding:5%;text-align:center">
 * <div stle="margin-below:24pt"><div>MonkeyTalk was created and donated to the iPhone community by Gorilla Logic, an enterprise application consulting firm</div> specializing in iPhone, Flex, and Java.</div>
 * To to learn more about our development, training, and testing services at http://www.gorillalogic.com.
 * </div>
 */

#import "TouchSynthesis.h"
#import "MTCommandEvent.h"
#import "UIView+MTReady.h"
#import "UIControl+MTready.h"

/** The public API for the MonkeyTalk framework.
 */
@interface MonkeyTalkAPI : NSObject
/** Appends the command to the end of the currently recording script.
 @param sender the component for which this command is to be recorded 
 @param command the name of the command.
 @param args an array of the command's arguments
 */
+ (void) record:(UIView*)sender command:(NSString*)command args:(NSArray*)args;

/**
 When handling an event, a component MUST call either record:command:args or continueRecording before returning. Neglecting to do so will permanently suspend the MonkeyTalk console.
 */
+ (void) continueRecording;

/**
 Play the supplied array of MTCommandEvents.
 @return nil if all comamnds run successfully and an error or failure message otherwise.
 */
+ (NSString*) playCommands:(NSArray*)commands;

/**
 Play the commands from the named script file.
 @return nil if all comamnds run successfully and an error or failure message otherwise.
 */
+ (NSString*) playFile:(NSString*)file;

/**
 Find the view with the specified monkeyID and class.
 @param monkeyID the id to search for
 @param className the string name of the class
 @return the view or nil if no view is found.
 */
+ (UIView*) viewWithMonkeyID:(NSString*)monkeyID havingClass:(NSString*)className;

@end
