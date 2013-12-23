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

#import "UIResponder+MTReady.h"
#import "MonkeyTalk.h"
#import "MTCommandEvent.h"
#import "MTUtils.h"
#import <objc/runtime.h>
#import "UIView+MTReady.h"


@implementation UIResponder (MTReady)

+ (void)load {
    if (self == [UIResponder class]) {
		
        Method originalMethod = class_getInstanceMethod(self, @selector(becomeFirstResponder));
        Method replacedMethod = class_getInstanceMethod(self, @selector(mtbecomeFirstResponder));
        method_exchangeImplementations(originalMethod, replacedMethod);
    }
}

- (BOOL)mtbecomeFirstResponder
{
    if ([self class] == [UITextField class])
    {
        UITextField *view = (UITextField *)self;
        
        if (view != nil)
            [view mtAssureAutomationInit];
    } else if ([self class] == [UITextView class])
    {
        UITextView *view = (UITextView *)self;
        
        if (view != nil)
            [view mtAssureAutomationInit];
    }
    
    [self mtbecomeFirstResponder];
    return YES;
}

@end
