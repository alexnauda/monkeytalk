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

#import "MTTextAreaAutomator.h"

@implementation MTTextAreaAutomator

- (void) valueForElement {
    if ([self useDefaultProperty]) {
        // If there is no text, but the value attribute is set
        // use value attribute as default
        if ([self.element.text length] == 0 &&
            ![self.element attribute:@"value"])
            mtEvent.value = self.element.text;
        else
            mtEvent.value = [self.element attribute:@"value"];
    } else
        [super valueForElement];
}

@end
