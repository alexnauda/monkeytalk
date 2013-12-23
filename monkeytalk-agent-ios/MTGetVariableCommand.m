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

#define RETRIES 10
#define RETRY 500

#import "MTGetVariableCommand.h"
#import "MTDefaultProperty.h"
#import "MonkeyTalk.h"
#import "MTVerifyCommand.h"
#import "MTDevice.h"
#import "MTValueFromProperty.h"
#import "NSString+MonkeyTalk.h"
#import "MTConvertType.h"

@implementation MTGetVariableCommand

+ (NSString*) execute:(MTCommandEvent*) ev {
    
	UIView* source = nil;
	
    if (!ev.value)
        source = ev.source;
    
    NSString* value;
    NSString *prop = @"";
    NSString *internal = nil;
    @try {
        if ([ev.args count] == 1 || [[ev.args objectAtIndex:1] isEqualToString:@"value"]) {
            prop = @"value";
        } else {
            prop = [ev.args objectAtIndex:1];
            
            if ([[prop substringToIndex:1] isEqualToString:@"."]) {
                internal = [prop substringFromIndex:1];
            }
            
            // Backwards compatability
            if ([ev.command isEqualToString:MTCommandGetVariable])
                prop = [ev.args objectAtIndex:0];
            
//            if ([ev.args count] > 2)
//                errorMessage = [ev.args objectAtIndex:2];
        }
        
        if ([ev.className isEqualToString:@"device" ignoreCase:YES]) {
            if ([prop isEqualToString:@"value" ignoreCase:YES])
                prop = @"os";
            
            SEL selector = NSSelectorFromString(prop);
            return [MTDevice performSelector:selector];
        }
        
        if (ev.value)
            value = ev.value;
        else {
            if (internal) {
                if ([MTValueFromProperty shouldUseCustomValueForProp:internal onView:source])
                    value = [MTValueFromProperty valueFromProp:internal forView:source];
                else
                    value = [source valueForKeyPath:internal];
            } else
                value = [MTValueFromProperty valueFromProp:prop forView:source];
            
            ev.value = value;
        }

    } @catch (NSException* e)
    {
        // Handle exception
        
        if ([ev.className isEqualToString:@"device" ignoreCase:YES]) {
            if ([ev.args count] == 0)
                ev.lastResult = [NSString 
                             stringWithFormat: @"Get Device requires at least one arg (variable)"];
            else
                ev.lastResult = [NSString 
                                 stringWithFormat: @"Problem getting device info"];
        } else {
            if ([[e reason] isEqualToString:@"invalid property"])
                ev.lastResult = [NSString 
                                 stringWithFormat: @"\"%@\" is not a valid MonkeyTalk property for %@ (prefix property arg with . to use internal keypaths)", 
                                 prop, [MTConvertType convertedComponentFromString:ev.className isRecording:YES]];
            else
                ev.lastResult = [NSString 
                                 stringWithFormat: @"\"%@\" is not a valid keypath for %@\"", 
                                 internal, ev.className];
        }

        
        return ev.lastResult;
    }
    
    return value;
}

@end
