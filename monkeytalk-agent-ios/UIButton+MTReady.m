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

#import "MonkeyTalk.h"
#import "MTCommandEvent.h"
#import "UIButton+MTReady.h"
#import "UIView+MTReady.h"
#import "MTOrdinalView.h"

#import <UIKit/UIEvent.h>


@implementation UIButton (MTReady)

- (NSString *)mtComponent {
    return MTComponentButton;
}

- (NSString *) baseMonkeyID {
	if (self.currentTitle && self.currentTitle.length>0) {
		return self.currentTitle;
	}
    return [super baseMonkeyID];
}

+ (NSArray *)aliased {
    if (self == [UIButton class]) {
        return [NSArray arrayWithObjects:@"UINavigationItemButtonView", @"UINavigationButton", @"UIThreePartButton", @"UIRoundedRectButton", @"UIToolbarTextButton", @"UIAlertButton", @"_UIModalItemTableViewCell", nil];
    }
    return nil;
}

- (BOOL) shouldRecordMonkeyTouch:(UITouch *)touch {
	if ([self.superview isKindOfClass:[UITextField class]]) {
		// It's a clear button?
        UITextField *textField = (UITextField *)self.superview;
        UITextFieldViewMode tfClearMode = [textField clearButtonMode];
        
        // Return no only if right view clear button visible
        if ([textField.text length] > 0 && 
            ((!textField.isEditing && tfClearMode == UITextFieldViewModeUnlessEditing) || 
            (textField.isEditing && tfClearMode == UITextFieldViewModeWhileEditing) ||
            tfClearMode == UITextFieldViewModeAlways))
            return NO;
	}
	return [super shouldRecordMonkeyTouch:touch];
}


@end
