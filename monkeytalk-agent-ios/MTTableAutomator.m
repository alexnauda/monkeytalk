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

#import "MTTableAutomator.h"
#import "MTConstants.h"
#import "NSString+MonkeyTalk.h"
#import "MTHTTPVirtualDirectory+FindElement.h"

@interface MTTableAutomator ()
@end

@implementation MTTableAutomator

- (BOOL) playBackOnElement {
    BOOL isGetOrVerify = [mtEvent.command isEqualToString:MTCommandGet ignoreCase:YES] || [mtEvent.command rangeOfString:MTCommandVerify].location != NSNotFound;
    if (isGetOrVerify) {
        [self valueForElement];
        return YES;
    }
    
    MTElement *cell = nil;
    
    if ([mtEvent.command isEqualToString:MTCommandSelect ignoreCase:YES]) {
        if (mtEvent.args.count == 0) {
            mtEvent.lastResult = [NSString stringWithFormat:@"%@ requires one arg.",mtEvent.command];
            mtEvent.didPlayInWeb = NO;
            return NO;
        }
        cell = [self elementWithText:[mtEvent.args objectAtIndex:0]];
    } else if ([mtEvent.command isEqualToString:MTCommandSelectIndex ignoreCase:YES] ||
               [mtEvent.command isEqualToString:MTCommandSelectRow ignoreCase:YES]) {
        if (mtEvent.args.count == 0) {
            mtEvent.lastResult = [NSString stringWithFormat:@"%@ requires one or more args.",mtEvent.command];
            mtEvent.didPlayInWeb = NO;
            return NO;
        }
        NSInteger row = mtEvent.args.count > 0 ? [[mtEvent.args objectAtIndex:0] integerValue] : -1;
        NSInteger column = mtEvent.args.count > 1 ? [[mtEvent.args objectAtIndex:1] integerValue] : -1;
        cell = [self elementFromRow:row column:column];
    } else {
        return [super playBackOnElement];
    }
    
    if (!cell) {
        mtEvent.lastResult = [NSString stringWithFormat:@"Failed to find html cell in %@ \"%@\".", mtEvent.component ,mtEvent.monkeyID];
        mtEvent.didPlayInWeb = NO;
        return NO;
    }
    
    [self tapElement:cell];
    return YES;
}

- (MTElement *)elementWithText:(NSString *)textValue {
    NSString *finder = [self finderExpression:textValue];
    NSString *xpath = [self.xPath stringByAppendingFormat:@"//tr[%@]", finder];
    MTElement *rowElement = [self elementFromXpath:xpath];
    
    if (rowElement) {
        return rowElement;
    }
    
    xpath = [self.xPath stringByAppendingFormat:@"//td[%@]", finder];
    return [self elementFromXpath:xpath];
}

- (MTElement *)elementFromRow:(NSInteger)row column:(NSInteger)column {
    if (row == -1 && column == -1) {
        // return the table element
        return self.element;
    }
    
    NSString *xpath = [self.xPath stringByAppendingFormat:@"/tr[%i]|%@/tbody/tr[%i]", row, self.xPath, row];
    
    if (column > 0) {
        xpath = [self.xPath stringByAppendingFormat:@"/tr[%i]/td[%i]|%@/tbody/tr[%i]/td[%i]", row, column, self.xPath, row, column];
    }

    return [self elementFromXpath:xpath];
}

- (NSInteger)rowCount {
    MTHTTPVirtualDirectory *virtualDirectory = [[MTHTTPVirtualDirectory alloc] init];
    NSDictionary *webElementDict = [[NSMutableDictionary alloc] init];
    NSString *using = @"xpath";
    // //table/tr[1]/td[1]|//table/tbody/tr[1]/td[1]
    NSString *value = [self.xPath stringByAppendingFormat:@"/tr|%@/tbody/tr", self.xPath];
    
    [webElementDict setValue:using forKey:@"using"];
    [webElementDict setValue:value forKey:@"value"];
    NSArray *elements = [virtualDirectory findElements:webElementDict root:nil implicitlyWait:0];
    
    return elements ? elements.count : 0;
}

- (void)valueForProperty:(NSString *)property indices:(NSArray *)indices {
    if ([property isEqualToString:@"item"]) {
        if (indices && indices.count > 0) {
            NSInteger row = [indices[0] integerValue];
            NSInteger column = indices.count > 1 ? [indices[1] integerValue] : -1;
            MTElement *item = [self elementFromRow:row column:column];
            mtEvent.value = item.text;
        }
    }
}

- (void)valueForElement {
    if ([mtEvent.args count] > 1) {
        NSString *attribute = [mtEvent.args objectAtIndex:1];
        NSArray *argItemsArray = [self argIndices:&attribute];
        if (argItemsArray && argItemsArray.count > 0) {
            return [self valueForProperty:attribute indices:argItemsArray];
        }
        
        if ([attribute isEqualToString:@"size"]) {
            mtEvent.value = [NSString stringWithFormat:@"%i", [self rowCount]];
        } else {
            mtEvent.value = [self.element attribute:attribute];
        }
    } else {
        mtEvent.value = [self.element text];
    }
}

@end
