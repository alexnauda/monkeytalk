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

#import "MTLinkAutomator.h"

@implementation MTLinkAutomator
- (NSString *) xPath {
    // Return basic xpath expression — handles most components
    // Override in automators class to specify on per component basis
    if ([mtEvent.monkeyID rangeOfString:@"xpath="].location != NSNotFound) {
        return [super xPath];
    }
    
    NSString *convertedCommand;
    
    NSString *path = [self basePath];
    
    convertedCommand = [NSString
                        stringWithFormat:@"(//a)%@",path];
    
    if ([[mtEvent.monkeyID lowercaseString] rangeOfString:@"xpath="].location != NSNotFound) {
        // Use Monkey ID as xpath
        convertedCommand = [mtEvent.monkeyID stringByReplacingOccurrencesOfString:@"xpath=" withString:@""];
    }
    
    return convertedCommand;
}
@end
