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

#import "UINavigationController+MTReady.h"
#import <objc/runtime.h>
#import "MonkeyTalk.h"
#import "NSObject+MTReady.h"

@implementation UINavigationController (MTReady)

+ (void)load {
    if (self == [UINavigationController class]) {
		
        Method originalMethod = class_getInstanceMethod(self, @selector(pushViewController:animated:));
        Method replacedMethod = class_getInstanceMethod(self, @selector(mtpushViewController:animated:));
        method_exchangeImplementations(originalMethod, replacedMethod);
        
        Method originalMethod2 = class_getInstanceMethod(self, @selector(popViewControllerAnimated:));
        Method replacedMethod2 = class_getInstanceMethod(self, @selector(mtpopViewControllerAnimated:));
        method_exchangeImplementations(originalMethod2, replacedMethod2);
    }
}

- (void) mtpushViewController:(UIViewController *)viewController animated:(BOOL)animated
{
    if ([[MonkeyTalk sharedMonkey].commandSpeed doubleValue] < 333333 && [MonkeyTalk sharedMonkey].commandSpeed)
        animated = NO;
    
    [self mtpushViewController:viewController animated:animated];
    [MonkeyTalk sharedMonkey].isPushingController = YES
    ;
}


- (UIViewController *) mtpopViewControllerAnimated:(BOOL)animated
{
    if ([MonkeyTalk sharedMonkey].commandSpeed && [[MonkeyTalk sharedMonkey].commandSpeed doubleValue] < 333333)
        animated = NO;

    [self mtpopViewControllerAnimated:animated];
    
    return self;
}

@end
