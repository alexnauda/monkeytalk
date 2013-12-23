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

#import "MTButtonAutomator.h"

@implementation MTButtonAutomator

- (id) initWithMTCommandEvent:(MTCommandEvent *)event {
    self = [super initWithMTCommandEvent:event];
    
    return self;
}

- (NSString *) alternateID {
    return [NSString
            stringWithFormat:@"//input[%@]",[self finderExpression:mtEvent.monkeyID]];
}

- (void) valueForElement {
    [super valueForElement];
    if ([mtEvent.args count] == 1 && mtEvent.value.length == 0) {
        NSString *attribute = @"value";
        mtEvent.value = [self.element attribute:attribute];
    }
}

//- (NSString *) xPath {
//    return @"//*[translate(name(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') = 'button' or (translate(name(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') = 'input' and (translate(@type, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') = 'submit' or translate(@type, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') = 'password' or translate(@type, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') = 'reset'))]";
//}

@end
