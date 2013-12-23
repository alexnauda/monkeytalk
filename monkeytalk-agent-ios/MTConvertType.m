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

#import "MTConvertType.h"
#import "MonkeyTalk.h"
#import "MTUtils.h"
#import "NSString+MonkeyTalk.h"

@interface MTConvertType()
+ (NSDictionary *) componentsDictionary;
+ (NSDictionary *) recordDictionary;
@end

@implementation MTConvertType

// Aliasing
+ (NSDictionary *) componentsDictionary {
    NSMutableDictionary *mutableComponents = [[NSMutableDictionary alloc] init];
    
    // Add components value:iOS key:wire(new)
    [mutableComponents setValue:@"UIView" forKey:[MTComponentView lowercaseString]];
    [mutableComponents setValue:@"UIButton" forKey:[MTComponentButton lowercaseString]];
    [mutableComponents setValue:@"UITextField" forKey:[MTComponentInput lowercaseString]];
    [mutableComponents setValue:@"UILabel" forKey:[MTComponentLabel lowercaseString]];
    [mutableComponents setValue:@"UITextView" forKey:[MTComponentTextArea lowercaseString]];
    [mutableComponents setValue:@"UITableView" forKey:[MTComponentTable lowercaseString]];
    [mutableComponents setValue:@"UIPickerView" forKey:[MTComponentSelector lowercaseString]];
    [mutableComponents setValue:@"UIDatePicker" forKey:[MTComponentDatePicker lowercaseString]];
    [mutableComponents setValue:@"UISegmentedControl" forKey:[MTComponentButtonSelector lowercaseString]];
    [mutableComponents setValue:@"UISlider" forKey:[MTComponentSlider lowercaseString]];
    [mutableComponents setValue:@"UIScrollView" forKey:[MTComponentScroller lowercaseString]];
    [mutableComponents setValue:@"UITabBar" forKey:[MTComponentTabBar lowercaseString]];
    [mutableComponents setValue:@"UIToolbar" forKey:[MTComponentToolBar lowercaseString]];
    [mutableComponents setValue:@"MPMovieView" forKey:[MTComponentVideoPlayer lowercaseString]];
    [mutableComponents setValue:@"UILabel" forKey:[MTComponentLink lowercaseString]];
    [mutableComponents setValue:@"UIStepper" forKey:[MTComponentStepper lowercaseString]];
    [mutableComponents setValue:@"UIWebView" forKey:[MTComponentBrowser lowercaseString]];
    [mutableComponents setValue:@"UIImageView" forKey:[MTComponentImage lowercaseString]];
    [mutableComponents setValue:@"UIWebView" forKey:[MTComponentWeb lowercaseString]];
    
    // iOS 5 records/plays back _UISwitchInternalView not UISwitch
//    if (![MTUtils isOs5Up])
        [mutableComponents setValue:@"UISwitch" forKey:[MTComponentToggle lowercaseString]];
//    else
//        [mutableComponents setValue:@"_UISwitchInternalView" forKey:@"toggle"];
    
    [mutableComponents setValue:@"UISegmentedControl" forKey:@"radiobuttons"];
    [mutableComponents setValue:@"UISlider" forKey:@"numericselector"];
    [mutableComponents setValue:@"UISlider" forKey:@"ratingbar"];
    [mutableComponents setValue:@"UITabBar" forKey:@"menu"];
    [mutableComponents setValue:@"UISwitch" forKey:@"checkbox"];
    [mutableComponents setValue:@"UISwitch" forKey:[MTComponentSwitch lowercaseString]];
    
    return mutableComponents;
}

// Mapping
+ (NSDictionary *) recordDictionary {
    NSMutableDictionary *mutableComponents = [NSMutableDictionary dictionaryWithObject:MTComponentView forKey:@"UIView"];
    
    // Add components value:iOS key:wire(new)
    [mutableComponents setValue:MTComponentButton forKey:@"UIButton"];
    [mutableComponents setValue:MTComponentButton forKey:@"UINavigationItemButtonView"];
    [mutableComponents setValue:MTComponentButton forKey:@"UINavigationButton"];
    [mutableComponents setValue:MTComponentButton forKey:@"UIThreePartButton"];
    [mutableComponents setValue:MTComponentButton forKey:@"UIRoundedRectButton"];
    [mutableComponents setValue:MTComponentButton forKey:@"UIToolbarTextButton"];
    [mutableComponents setValue:MTComponentButton forKey:@"UIAlertButton"];
    [mutableComponents setValue:MTComponentInput forKey:@"UITextField"];
    [mutableComponents setValue:MTComponentLabel forKey:@"UILabel"];
    [mutableComponents setValue:MTComponentTextArea forKey:@"UITextView"];
    [mutableComponents setValue:MTComponentTable forKey:@"UITableView"];
    [mutableComponents setValue:MTComponentSelector forKey:@"UIPickerView"];
    [mutableComponents setValue:MTComponentDatePicker forKey:@"UIDatePicker"];
    [mutableComponents setValue:MTComponentButtonSelector forKey:@"UISegmentedControl"];
    [mutableComponents setValue:MTComponentSlider forKey:@"UISlider"];
    [mutableComponents setValue:MTComponentScroller forKey:@"UIScrollView"];
    [mutableComponents setValue:MTComponentTabBar forKey:@"UITabBar"];
    [mutableComponents setValue:MTComponentToggle forKey:@"UISwitch"];
    [mutableComponents setValue:MTComponentToolBar forKey:@"UIToolbar"];
    [mutableComponents setValue:MTComponentVideoPlayer forKey:@"MPMovieView"];
    [mutableComponents setValue:MTComponentStepper forKey:@"UIStepper"];
    [mutableComponents setValue:MTComponentBrowser forKey:@"UIWebView"];
    [mutableComponents setValue:MTComponentImage forKey:@"UIImageView"];
    [mutableComponents setValue:MTComponentWeb forKey:@"UIWebView"];
    
    [mutableComponents setValue:MTComponentToggle forKey:@"_UISwitchInternalView"];
    [mutableComponents setValue:MTComponentScroller forKey:@"_UIWebViewScrollView"];
    
    // [mutableComponents setValue:MTComponentToolBar forKey:@"UIToolbar"];
    // [mutableComponents setValue:MTComponentButton forKey:@"UINavigationItemButtonView"];
    // [mutableComponents setValue:MTComponentButton forKey:@"UINavigationButton"];
    // [mutableComponents setValue:MTComponentButton forKey:@"UIThreePartButton"];
    // [mutableComponents setValue:MTComponentButton forKey:@"UIRoundedRectButton"];
    // [mutableComponents setValue:MTComponentButton forKey:@"UIToolbarTextButton"];
    // [mutableComponents setValue:MTComponentButton forKey:@"UIAlertButton"];
    // [mutableComponents setValue:MTComponentToggle forKey:@"_UISwitchInternalView"];
    // [mutableComponents setValue:MTComponentScroller forKey:@"_UIWebViewScrollView"];
    
    return mutableComponents;
}

+ (NSString *) convertedComponentFromString:(NSString *)originalComponent 
                                isRecording:(BOOL)isRecording {
    
    if (isRecording) {
        NSDictionary *recordDictionary = [self recordDictionary];
        if (![recordDictionary objectForKey:originalComponent]) {
            // It is a custom component — do not use . prefix after beta4
            NSString *prefix = @"";
            
            if ([originalComponent isEqualToString:MTComponentDevice ignoreCase:YES])
                prefix = @"";
            
            // Add . prefix and return original component
            return [NSString stringWithFormat:@"%@%@",prefix,originalComponent];
        }
        
        return [recordDictionary objectForKey:originalComponent];
    }
    
    if ([[originalComponent substringToIndex:1] isEqualToString:@"." ignoreCase:YES]) {
        // It is a custom component — return original component
        originalComponent = [originalComponent substringFromIndex:1];
        return originalComponent;
    } else if ([originalComponent isEqualToString:@"Device" ignoreCase:YES])
        return originalComponent;
    
    return [[self componentsDictionary] objectForKey:[originalComponent lowercaseString]];
}

@end
