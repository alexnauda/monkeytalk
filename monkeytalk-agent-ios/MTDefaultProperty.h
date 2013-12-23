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

#import <Foundation/Foundation.h>

#define MTVerifyPropertyDefault @"value"
#define MTVerifyPropertyTextField @"text"
#define MTVerifyPropertyTextView @"text"
#define MTVerifyPropertyButton @"titleLabel.text"
#define MTVerifyPropertyNavBar @"topItem.title"
#define MTVerifyPropertyTableCell @"textLabel.text"
#define MTVerifyPropertySlider @"value"
#define MTVerifyPropertySwitch @"on"
#define MTVerifyPropertyDevice @"os"
#define MTVerifyPropertyLabel @"text"
#define MTVerifyPropertyDate @"date"
#define MTVerifyPropertyDateAndTime @"dateAndTime"
#define MTVerifyPropertyTime @"time"
#define MTVerifyPropertyCountDownTimer @"countDownTimer"
#define MTVerifyPropertyCustomePicker @"customPicker"
#define MTVerifyPropertyNavButton @"title"


#define MTVerifyPropertyMax @"max"
#define MTVerifyPropertyMin @"min"
#define MTVerifyPropertyStepSize @"stepsize"

@interface MTDefaultProperty : NSObject {
    
}

+ (NSString *) defaultPropertyForClass:(NSString *)classString;

@end
