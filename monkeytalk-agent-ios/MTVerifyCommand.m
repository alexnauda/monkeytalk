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

#import "MTVerifyCommand.h"
#import "MonkeyTalk.h"
#import "MTDefaultProperty.h"
#import "MTValueFromProperty.h"
#import "NSString+MonkeyTalk.h"
#import "MTConvertType.h"
#import "MTUtils.h"
#import "UIView+MTFinder.h"

@implementation MTVerifyCommand

NSString *verifyResult;
NSString *expectedString;
NSString *prop;
NSString *internal;
NSString *errorMessage = nil;


+ (NSDictionary *) verifyArgs:(MTCommandEvent *)ev {
    NSString* arg1 = nil;
    NSString* arg2 = nil;
    
    if ([ev.args count] == 1 || [[ev.args objectAtIndex:1] isEqualToString:@"value"]) {
        arg2 = @"value";
        arg1 = [ev.args objectAtIndex:0];
    } else {
        arg1 = [ev.args objectAtIndex:0];
        arg2 = [ev.args objectAtIndex:1];
    }
    
    if ([[arg2 substringToIndex:1] isEqualToString:@"."]) {
        arg2 = [arg2 substringFromIndex:1];
        return [NSDictionary dictionaryWithObjects:[NSArray arrayWithObjects:arg1, arg2, nil] forKeys:[NSArray arrayWithObjects:@"Arg1",@"InternalProperty", nil]];
    }
    
    return [NSDictionary dictionaryWithObjects:[NSArray arrayWithObjects:arg1, arg2, nil] forKeys:[NSArray arrayWithObjects:@"Arg1",@"Arg2", nil]];
}

+ (NSString *) valueFromProp:(NSString *)prop forView:(UIView *)source {
    NSString* value;
    
    if ([prop isEqualToString:MTVerifyPropertySwitch]) {
        BOOL isOn = [[source valueForKeyPath:prop] boolValue];
        
        if (isOn)
            value = @"on";
        else
            value = @"off";
    } else
        value = [source valueForKeyPath:prop];
    return value;
}

+ (void) handleVerify:(MTCommandEvent*) ev {
    if ([ev.command isEqualToString:MTCommandVerify ignoreCase:YES]) {
        [MTVerifyCommand execute:ev isVerifyNot:NO isRegEx:NO isWildCard:NO];
        return;
    } else if ([ev.command isEqualToString:MTCommandVerifyNot ignoreCase:YES]) {
        [MTVerifyCommand execute:ev isVerifyNot:YES isRegEx:NO isWildCard:NO];
        return;
    } else if ([ev.command isEqualToString:MTCommandVerifyRegex ignoreCase:YES]) {
        [MTVerifyCommand execute:ev isVerifyNot:NO isRegEx:YES isWildCard:NO];
        return;
    } else if ([ev.command isEqualToString:MTCommandVerifyNotRegex ignoreCase:YES]) {
        [MTVerifyCommand execute:ev isVerifyNot:YES isRegEx:YES isWildCard:NO];
        return;
    } else if ([ev.command isEqualToString:MTCommandVerifyWildcard ignoreCase:YES]) {
        [MTVerifyCommand execute:ev isVerifyNot:NO isRegEx:NO isWildCard:YES];
        return;
    } else if ([ev.command isEqualToString:MTCommandVerifyNotWildcard ignoreCase:YES]) {
        [MTVerifyCommand execute:ev isVerifyNot:YES isRegEx:NO isWildCard:YES];
        return;
    }
}



+ (NSString*) execute:(MTCommandEvent*) ev isVerifyNot:(BOOL)isVerifyNot isRegEx:(BOOL)isRegEx isWildCard:(BOOL)isWildCard {
    
    ev.lastResult = nil;
	UIView* source = nil;
    NSString *propValue;
    BOOL requiresArg = (isRegEx || isWildCard);
    
    if (ev.args.count == 0 && requiresArg) {
        NSString *finder = [ev.command.lowercaseString rangeOfString:@"regex"].location != NSNotFound ? @"regex" : @"wildcard";
        ev.lastResult = [NSString stringWithFormat:@"Missing required argument specifying %@ value for verification", finder];
        return ev.lastResult;
    }
    
    if (!ev.value)
        source = ev.source;
	if (source == nil && !ev.value) {
		ev.lastResult = [NSString
                         stringWithFormat:@"Unable to find %@ with monkeyID %@",
                         ev.className, ev.monkeyID];
		return ev.lastResult;
	} else if ((isVerifyNot && [ev.args count] == 0) && (source || ev.value)) {
        ev.lastResult = [NSString
                         stringWithFormat:@"Found %@ with monkeyID %@",
                         ev.className, ev.monkeyID];
		return ev.lastResult;
    }
	if ([ev.args count] > 0) {
        
        NSRegularExpression *regex;
        NSError *error = nil;
        
        [self setMonkeyArgumentsForVerify:ev isWildCard:isWildCard];
        
        if ([ev.args count] > 2)
            errorMessage = [ev.args objectAtIndex:2];
        
        if (isRegEx) {
            if ([expectedString length] == 0) {
                // NSRegularExpression throws error with empty pattern
                // Playback as verify to match android
                isRegEx = NO;
            } else {
                regex = [[NSRegularExpression alloc] initWithPattern:expectedString
                                                             options:NSRegularExpressionDotMatchesLineSeparators error:&error];
            }
            
            if (error)
            {
                ev.lastResult = [NSString
                                 stringWithFormat: @"Error with regular expression syntax \"%@\".",
                                 expectedString];
                return ev.lastResult;
            }
        }
        if (isWildCard) {
            if ([expectedString length] == 0) {
                // NSRegularExpression throws error with empty pattern
                // Playback as verify to match android
                isWildCard = NO;
            } else {
                regex = [[NSRegularExpression alloc] initWithPattern:[MTUtils buildWildCardRegularExpression:expectedString]
                                                         options:NSRegularExpressionDotMatchesLineSeparators
                                                           error:&error];
            }
            if (error)
            {
                ev.lastResult = [NSString
                                 stringWithFormat: @"Error with regular expression syntax \"%@\".",
                                 expectedString];
                return ev.lastResult;
            }
        }
        
        // WildCard MonkeyID
        // 1. check for occurenece of * and ? in MonkeyID
        // 2. if found build wildcard monkeyid pattern using buildWildCardRegularExpression method
        // 3. Get list of components that matches wildcard pattern for a component
        // 4. Iterate through list of component to match expected value
        
        if ([ev.monkeyID rangeOfString:@"*"].location != NSNotFound ||
            [ev.monkeyID rangeOfString:@"?"].location != NSNotFound ) {
            
            //NSString *wildCardMonkeyIDPattern = [MTUtils buildWildCardRegularExpression:ev.monkeyID];
            NSArray *components = [NSClassFromString(ev.className) orderedViewsWithWildcardMonkeyId:ev.monkeyID];
            //NSMutableArray *components =[MTUtils componentsForWildCardMonkeyId:wildCardMonkeyIDPattern className:ev.className];
            
            for (NSValue *value in components) {
                UIView *tempView = [value nonretainedObjectValue];
                source = tempView;
                propValue = [self getPropertyValueForComponent:ev
                                                   forInternal:internal forProp:prop
                                                       forView:source];
                if (isRegEx) {
                    [self verifyRegexWithPropValue:regex
                                         propValue:propValue
                                       isVerifyNot:isVerifyNot
                                             event:ev];
                }
                if (isWildCard) {
                    [self verifyWildCardWithPropValue:regex
                                            propValue:propValue
                                          isVerifyNot:isVerifyNot
                                                event:ev];
                }
                if (!isRegEx && !isWildCard ){
                    [self verificationOfPropValues:expectedString
                                         propValue:propValue
                                       isVerifyNot:isVerifyNot
                                             event:ev];
                }
                
                if (ev.lastResult && isVerifyNot) {
                    break;
                } else if (ev.lastResult || (!ev.lastResult && isVerifyNot)) {
                    continue;
                } else {
                    return nil;
                    break;
                }
            }
        }
        // 5. Conditonal else - verification of component prop values for not a wild card monkey id
        else {
            
            propValue = [self getPropertyValueForComponent:ev
                                               forInternal:internal forProp:prop
                                                   forView:source];
            
            if (isRegEx) {
                [self verifyRegexWithPropValue:regex
                                     propValue:propValue
                                   isVerifyNot:isVerifyNot
                                         event:ev];
            }
            if (isWildCard) {
                [self verifyWildCardWithPropValue:regex
                                        propValue:propValue
                                      isVerifyNot:isVerifyNot
                                            event:ev];
            }
            if (!isRegEx && !isWildCard ){
                [self verificationOfPropValues:expectedString
                                     propValue:propValue
                                   isVerifyNot:isVerifyNot
                                         event:ev];
            }
            if (ev.lastResult)
                return  ev.lastResult;
            else
                return nil;
        }
    }
    return ev.lastResult;
}

// Method to set monkey arguments for verification command

+(void) setMonkeyArgumentsForVerify:(MTCommandEvent *)event isWildCard:(BOOL) isWildCard {
    NSDictionary *verifyArgs = [[self class] verifyArgs:event];
    
    prop = [verifyArgs objectForKey:@"Arg2"];
    internal = [verifyArgs objectForKey:@"InternalProperty"];
    expectedString = [verifyArgs objectForKey:@"Arg1"];
}

// Method returns a property value for component

+(NSString *) getPropertyValueForComponent:(MTCommandEvent *)event forInternal:(NSString *)internal forProp:(NSString *)prop forView:(UIView *)source {
    NSString* value;
    @try {
        if (event.value) {
            // value found from element in webview
            value = event.value;
        }
        else {
            if (internal) {
                if ([MTValueFromProperty shouldUseCustomValueForProp:internal onView:source])
                    value = [MTValueFromProperty valueFromProp:internal forView:source];
                else
                    value = [source valueForKeyPath:internal];
            } else
                value = [MTValueFromProperty valueFromProp:prop forView:source];
            
        }
    } @catch (NSException* e)
    {
        if ([[e reason] isEqualToString:@"invalid property"])
            event.lastResult = [NSString
                                stringWithFormat: @"\"%@\" is not a valid MonkeyTalk property for %@ (prefix property arg with . to use internal keypaths)",
                                prop, [MTConvertType convertedComponentFromString:event.className isRecording:YES]];
        else
            event.lastResult = [NSString
                                stringWithFormat: @"\"%@\" is not a valid keypath for %@\"",
                                internal, event.className];
        return event.lastResult;
    }
    
    value = [NSString stringWithFormat:@"%@", value];
    
    return value;
}

+(void)verificationOfPropValues:(NSString *)expected propValue:(NSString *)propValue isVerifyNot:(BOOL)isVerifyNot event:(MTCommandEvent *)event {
    
    if ([expected isEqualToString:propValue]) {
        if (isVerifyNot) {
            event.lastResult = [NSString
                                stringWithFormat: @"Expected not \"%@\" and found \"%@\"",
                                expected, propValue];
            
            if (errorMessage) {
                // Use provided error message
                event.lastResult = [event.lastResult stringByAppendingFormat:@": %@",errorMessage];
            }
        } else {
            event.lastResult = nil;
        }
        
    } else {
        if (isVerifyNot) {
            event.lastResult = nil;
        }
        else {
            NSString *result = [NSString
                                stringWithFormat: @"String \"%@\" does not match \"%@\"",
                                expected, propValue];
            event.lastResult = event.lastResult ? [event.lastResult stringByAppendingFormat:@" or \"%@\"", propValue] : result;
            
            if (errorMessage)
                // Use provided error message
                event.lastResult = [event.lastResult stringByAppendingFormat:@": %@",errorMessage];
        }
    }
    
}

+(void) verifyRegexWithPropValue:(NSRegularExpression *)regex propValue:(NSString *)propValue isVerifyNot:(BOOL)isVerifyNot event:(MTCommandEvent *)event {
    if ([regex foundMatchInString:propValue]) {
        if (isVerifyNot) {
            event.lastResult = [NSString
                                stringWithFormat: @"Regex \"%@\" found in \"%@\"",
                                expectedString, propValue];
            
            if (errorMessage)
                // Use provided error message
                event.lastResult = [event.lastResult stringByAppendingFormat:@": %@",errorMessage];
        } else
            event.lastResult = nil;
    }
    else {
        if (isVerifyNot)
            event.lastResult = nil;
        else {
            NSString *result = [NSString
                                stringWithFormat: @"Regex \"%@\" not found in \"%@\"",
                                expectedString, propValue];
            event.lastResult = event.lastResult ? [event.lastResult stringByAppendingFormat:@" or \"%@\"", propValue] : result;
            
            if (errorMessage)
                // Use provided error message
                event.lastResult = [event.lastResult stringByAppendingFormat:@": %@",errorMessage];
        }
    }
    
}

+(void) verifyWildCardWithPropValue:(NSRegularExpression *)regex propValue:(NSString *)propValue isVerifyNot:(BOOL)isVerifyNot event:(MTCommandEvent *)event {
    if([regex foundMatchInString:propValue]) {
        if (isVerifyNot) {
            event.lastResult = [NSString
                                stringWithFormat: @"Wildcard string \"%@\" found in \"%@\"",
                                expectedString, propValue];
            
            if (errorMessage)
                // Use provided error message
                event.lastResult = [event.lastResult stringByAppendingFormat:@": %@",errorMessage];
        } else
            event.lastResult = nil;
    }
    else {
        if (isVerifyNot)
            event.lastResult = nil;
        else {
            NSString *result = [NSString
                                stringWithFormat: @"Wildcard string \"%@\" not found in \"%@\"",
                                expectedString, propValue];
            event.lastResult = event.lastResult ? [event.lastResult stringByAppendingFormat:@" or \"%@\"", propValue] : result;
            
            if (errorMessage)
                // Use provided error message
                event.lastResult = [event.lastResult stringByAppendingFormat:@": %@",errorMessage];
        }
    }
    
}


@end