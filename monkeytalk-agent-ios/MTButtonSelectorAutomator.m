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

#import "MTButtonSelectorAutomator.h"
#import "MTConstants.h"
#import "NSString+MonkeyTalk.h"

@implementation MTButtonSelectorAutomator

- (NSString *) xPath {
    // ButtonSelector creates xPath for radio group
    if ([mtEvent.monkeyID rangeOfString:@"xpath="].location != NSNotFound) {
        return [super xPath];
    }
    
    NSString *convertedCommand;
    NSString *path;
    BOOL isOrdinal = [self isOrdinal];
    
    if ([mtEvent.args count] > 0) {
        if ([mtEvent.command isEqualToString:MTCommandSelect ignoreCase:YES]) {
            // Select command uses value to select in a radio group
            path = [NSString stringWithFormat:@"[@type = 'radio' and @value = '%@']",[mtEvent.args objectAtIndex:0]];
            
            if (isOrdinal)
                path = [NSString stringWithFormat:@"[@type='radio' and @value='%@'][%i]",
                        [mtEvent.args objectAtIndex:0],
                        currentOrdinal];
            
            convertedCommand = [NSString
                                stringWithFormat:@"(//input%@)",path];
        } else if ([mtEvent.command isEqualToString:MTCommandSelectIndex ignoreCase:YES]) {
            path = [NSString stringWithFormat:@"[@type = 'radio' and @value = '%@']",[mtEvent.args objectAtIndex:0]];
            // SelectIndex uses value of arg as index in xPath
            if (isOrdinal)
                path = [NSString stringWithFormat:@"[@type='radio'][%i]",
                        currentOrdinal--];
            
            convertedCommand = [NSString stringWithFormat:@"(//input%@)[%@]",
                                path,[mtEvent.args objectAtIndex:0]];
        } else {
            // Verify/get
            path = [NSString stringWithFormat:@"[@type = 'radio' and @value = '%@']",[mtEvent.args objectAtIndex:0]];
            convertedCommand = [NSString stringWithFormat:@"//input%@",
                                path];
        }
    } else {
        convertedCommand = [NSString
                            stringWithFormat:@"//input[@type='radio']"];
    }

    convertedCommand = [convertedCommand stringByReplacingOccurrencesOfString:@"MTSeleniumCommand" withString:@""];
    
    return convertedCommand;
}

- (void)valueForElement {
    mtEvent.value = [self.element attribute:@"value"];
}

- (BOOL)playBackOnElement {
    BOOL didPlayBack = [super playBackOnElement];
    
    if (didPlayBack && mtEvent.args.count > 0) {
        if ([mtEvent.command isEqualToString:MTCommandVerify ignoreCase:YES]) {
            BOOL isChecked = [self.element isChecked].boolValue;
            if (!isChecked) {
                mtEvent.lastResult = [NSString stringWithFormat:@"%@ not selected in %@ with monkeyID \"%@\"",[mtEvent.args objectAtIndex:0], mtEvent.component, mtEvent.monkeyID];
                mtEvent.didPlayInWeb = NO;
                return NO;
            }
        }
    }
    return didPlayBack;
}

@end
