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

#import "MTDefaultProperty.h"
#import "MTConvertType.h"
#import "NSString+MonkeyTalk.h"
#import <UIKit/UIDatePicker.h>

@implementation MTDefaultProperty
+ (NSString *) defaultPropertyForClass:(NSString *)classString {
    NSString *property = MTVerifyPropertyDefault;
    NSString *mtComponent = [MTConvertType convertedComponentFromString:classString isRecording:YES];
    
    if ([mtComponent isEqualToString:MTComponentInput ignoreCase:YES])
        property = MTVerifyPropertyTextField;
    else if ([mtComponent isEqualToString:MTComponentTextArea ignoreCase:YES])
        property = MTVerifyPropertyTextView;
    else if ([classString isEqualToString:@"UINavigationItemButtonView" ignoreCase:YES])
        property = MTVerifyPropertyNavButton;
    else if ([mtComponent isEqualToString:MTComponentButton ignoreCase:YES])
        property = MTVerifyPropertyButton;
    else if ([mtComponent isEqualToString:MTComponentSlider ignoreCase:YES])
        property = MTVerifyPropertySlider;
    else if ([mtComponent isEqualToString:MTComponentToggle ignoreCase:YES])
        property = MTVerifyPropertySwitch;
    else if ([mtComponent isEqualToString:MTComponentLabel ignoreCase:YES])
        property = MTVerifyPropertyLabel;
    else if ([mtComponent isEqualToString:MTComponentDevice ignoreCase:YES])
        property = MTVerifyPropertyDevice;
    else if ([mtComponent isEqualToString:@"UINavigationBar" ignoreCase:YES])
        property = MTVerifyPropertyNavBar;
    else if ([mtComponent isEqualToString:@"UITableViewCell" ignoreCase:YES])
        property = MTVerifyPropertyTableCell;
//    else if ([mtComponent isEqualToString:@"UIDatePickerModeDate" ignoreCase:YES])
//        property = MTVerifyPropertyDate;
//    else if ([mtComponent isEqualToString:@"UIDatePickerModeDateAndTime" ignoreCase:YES])
//        property = MTVerifyPropertyDateAndTime;
//    else if ([mtComponent isEqualToString:@"UIDatePickerModeTime" ignoreCase:YES])
//        property = MTVerifyPropertyTime;
//    else if ([mtComponent isEqualToString:@"UIDatePickerModeCountDownTimer" ignoreCase:YES])
//        property = MTVerifyPropertyCountDownTimer;
    
    return property;
}
@end
