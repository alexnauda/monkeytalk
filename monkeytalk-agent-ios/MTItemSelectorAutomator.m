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

#import "MTItemSelectorAutomator.h"
#import "MTConstants.h"
#import "NSString+MonkeyTalk.h"

@implementation MTItemSelectorAutomator

- (NSString *) xPath {
    // Selector finds select web element
    if ([mtEvent.monkeyID rangeOfString:@"xpath="].location != NSNotFound) {
        return [super xPath];
    }
    
    NSString *convertedCommand = [super xPath];
    NSString *path = [self basePath];
    
    if (mtEvent.args && [mtEvent.args count] > 0) {
        // If there are args, add option to xPath
        
        if ([mtEvent.command isEqualToString:MTCommandSelectIndex ignoreCase:YES])
            convertedCommand = [convertedCommand stringByAppendingFormat:@"/option[%@]",
                                [mtEvent.args objectAtIndex:0]];
        else if ([mtEvent.command isEqualToString:MTCommandSelect ignoreCase:YES])
            convertedCommand = [convertedCommand stringByAppendingFormat:@"/option[@value='%@' or ./text()=%@]", [mtEvent.args objectAtIndex:0], [mtEvent.args objectAtIndex:0]];
    }
    
    return convertedCommand;
}

- (MTElement *) selectElement {
    return [self elementFromXpath:[super xPath]];
}

- (MTElement *) optionAtIndex:(NSInteger)index {
    NSString *path = [NSString stringWithFormat:@"%@/option[%i]",[super xPath],index];
    return [self elementFromXpath:path];
}

- (void) deselectAll {
    MTElement *selectElement = [self selectElement];
    NSInteger size = [[selectElement attribute:@"length"] integerValue];
    
    for (int i = 0; i < size; i++) {
        MTElement *option = [self optionAtIndex:i];
        
        if ([option.isChecked integerValue] == 1) {
            [option toggleSelected];
        }
        
    }
    
}

- (void) selectItems {
    if (mtEvent.args && [mtEvent.args count] > 0) {
        // If there are args, add option to xPath
        
        for (NSString *arg in mtEvent.args) {
            MTElement *option;
            NSString *path = [super xPath];
            
            if ([mtEvent.command isEqualToString:MTCommandSelectIndex ignoreCase:YES])
                path = [path stringByAppendingFormat:@"/option[%@]",
                                    arg];
            else if ([mtEvent.command isEqualToString:MTCommandSelect ignoreCase:YES])
                path = [path stringByAppendingFormat:@"/option[@value='%@' or ./text()=%@]", arg, arg];
            
            option = [self elementFromXpath:path];
            
            [option toggleSelected];
            
        }
    }
}

- (BOOL) playBackOnElement {
    // Find size based on element attribute size of parent
    // Iterate through select element
    // Check isChecked on each option
    // Toggle checked if checked
    if ([mtEvent.command isEqualToString:MTCommandGet ignoreCase:YES] ||
        [mtEvent.command rangeOfString:[MTCommandVerify lowercaseString]].location != NSNotFound)
        [super playBackOnElement];
    
    MTElement *selectElement = [self selectElement];
    
    if ([selectElement attribute:@"multiple"])
        [self deselectAll];
    
    if ([mtEvent.command isEqualToString:MTCommandClear ignoreCase:YES])
        return YES;
    
    [self selectItems];
    
    return YES;
}

- (void) valueForElement {
    if ([self useDefaultProperty]) {
        NSInteger *selectedIndex = [[self.element attribute:@"selectedIndex"] integerValue] + 1;
        
        MTElement *option = [self optionAtIndex:selectedIndex];
        
        mtEvent.value = option.text;
    } else if ([self hasAttribute]) {
        if ([[self attribute] isEqualToString:@"size"])
            mtEvent.value = [self.element attribute:@"length"];
        else if ([[self attribute] isEqualToString:@"item"])
            mtEvent.value = self.element.text;
        else
            [super valueForElement];
    }
}

@end
