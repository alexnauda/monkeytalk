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

#define MTComponentApp @"App"
#define MTComponentView @"View"
#define MTComponentButton @"Button"
#define MTComponentInput @"Input"
#define MTComponentTextArea @"TextArea"
#define MTComponentLabel @"Label"
#define MTComponentTable @"Table"
#define MTComponentSelector @"ItemSelector"
#define MTComponentDatePicker @"DatePicker"
#define MTComponentButtonSelector @"ButtonSelector"
#define MTComponentRadioButtons @"RadioButtons"
#define MTComponentSlider @"Slider"
#define MTComponentScroller @"Scroller"
#define MTComponentTabBar @"TabBar"
#define MTComponentToggle @"Toggle"
#define MTComponentCheckBox @"CheckBox"
#define MTComponentToolBar @"ToolBar"
#define MTComponentVideoPlayer @"VideoPlayer"
#define MTComponentDevice @"Device"
#define MTComponentLink @"Link"
#define MTComponentHTMLTag @"HTMLTag"
#define MTComponentStepper @"Stepper"
#define MTComponentBrowser @"Browser"
#define MTComponentSwitch @"Switch"
#define MTComponentImage @"Image"
#define MTComponentWeb @"Web"

@interface MTConvertType : NSObject {
    
}

+ (NSString *) convertedComponentFromString:(NSString *)originalComponent 
                                isRecording:(BOOL)isRecording;

@end
