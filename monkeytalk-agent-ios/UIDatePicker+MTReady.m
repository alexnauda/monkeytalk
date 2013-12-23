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

#import "UIDatePicker+MTReady.h"
#import "MTCommandEvent.h"
#import "MTDefaultProperty.h"
#import "MTUtils.h"
#import <objc/runtime.h>
#import "MonkeyTalk.h"
#import "NSString+MonkeyTalk.h"
#import "UIView+MTReady.h"

@implementation UIDatePicker (MTReady)

- (NSString *)mtComponent {
    return MTComponentDatePicker;
}

- (NSString *) valueForProperty:(NSString *)prop withArgs:(NSArray *)args {
    NSString* value;
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    // For now only record date picker with Month/Day/Year
    if ([prop isEqualToString:MTVerifyPropertyDefault] && (self.datePickerMode == UIDatePickerModeDate))
    {
        [dateFormatter setDateFormat:@"yyyy-MM-dd"];
        value = [dateFormatter stringFromDate:self.date];
    }
    else if ([prop isEqualToString:MTVerifyPropertyDefault] && (self.datePickerMode == UIDatePickerModeDateAndTime))
    {
        [dateFormatter setDateFormat:@"yyyy-MM-dd hh:mm a"];
        value = [dateFormatter stringFromDate:self.date];
    }
    else if ([prop isEqualToString:MTVerifyPropertyDefault] && (self.datePickerMode == UIDatePickerModeTime))
    {
        [dateFormatter setDateFormat:@"hh:mm a"];
        value = [dateFormatter stringFromDate:self.date];
        //value = [NSString stringWithFormat:@"%0.2f",self.minimumValue];
    }
    else if ([prop isEqualToString:MTVerifyPropertyDefault] && (self.datePickerMode == UIDatePickerModeCountDownTimer))
    {
        [dateFormatter setDateFormat:@"HH:mm"];
        value = [dateFormatter stringFromDate:self.date];
    }
    else
        value = @"No value for the property";
    
    return value;
}

+ (void)load {
    if (self == [UIDatePicker class]) {
        
        Method originalInitFrame = class_getInstanceMethod(self, @selector(initWithFrame:));
        Method replacedInitFrame = class_getInstanceMethod(self, @selector(mtinitWithFrame:));
        Method originalInitCoder = class_getInstanceMethod(self, @selector(initWithCoder:));
        Method replacedInitCoder = class_getInstanceMethod(self, @selector(mtinitWithCoder:));
        
        method_exchangeImplementations(originalInitFrame, replacedInitFrame);
        method_exchangeImplementations(originalInitCoder, replacedInitCoder);
    }
}

- (void) playbackMonkeyEvent:(MTCommandEvent*)event {
    
    if ([event.command isEqualToString:MTCommandEnterDate ignoreCase:YES]) {
        if ([event.args count] == 0) {
            event.lastResult = @"EnterDate requires 1 arg (\"YYYY-MM-DD\")";
            return;
        }
        
        @try {
            NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
            [dateFormatter setDateFormat:@"yyyy-MM-dd"];
            //            [dateFormatter setDateStyle:NSDateFormatterMediumStyle];
            NSDate *date = [dateFormatter dateFromString:[event.args objectAtIndex:0]];
            [self setDate:date animated:YES];
        }
        @catch (NSException *exception) {
            event.lastResult = @"Error entering date (check the format: \"YYYY-MM-DD\")";
            return;
        }
    }
    // New code added for all the modes of UIDatePicker
    else if ([event.command isEqualToString:MTCommandEnterDateAndTime ignoreCase:YES]) {
        if ([event.args count] == 0) {
            event.lastResult = @"EnterDateAndTime requires 3 arg (\"yyyy-MM-dd hh:mm am/pm\")";
            return;
        }
        
        @try {
            NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
            [dateFormatter setDateFormat:@"yyyy-MM-dd hh:mm a"];
            //            [dateFormatter setDateStyle:NSDateFormatterMediumStyle];
            NSDate *date = [dateFormatter dateFromString:[event.args objectAtIndex:0]];
            [self setDate:date animated:YES];
        }
        @catch (NSException *exception) {
            event.lastResult = @"Error entering date and time(check the format: \"yyyy-MM-dd hh:mm am/pm\")";
            return;
        }
    }
    else if ([event.command isEqualToString:MTCommandEnterTime ignoreCase:YES]) {
        if ([event.args count] == 0) {
            event.lastResult = @"EnterTime requires 2 arg (\"hh:mm am/pm\")";
            return;
        }
        
        @try {
            NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
            [dateFormatter setDateFormat:@"hh:mm a"];
            //            [dateFormatter setDateStyle:NSDateFormatterMediumStyle];
            NSDate *date = [dateFormatter dateFromString:[event.args objectAtIndex:0]];
            [self setDate:date animated:YES];
        }
        @catch (NSException *exception) {
            event.lastResult = @"Error entering time (check the format: \"hh:mm am/pm\")";
            return;
        }
    }
    else if ([event.command isEqualToString:MTCommandEnterCountDownTimer ignoreCase:YES]) {
        if ([event.args count] == 0) {
            event.lastResult = @"EnterCountDownTimer requires 1 arg (\"HH:mm\")";
            return;
        }
        
        @try {
            NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
            [dateFormatter setDateFormat:@"HH:mm"];
            //            [dateFormatter setDateStyle:NSDateFormatterMediumStyle];
            NSDate *date = [dateFormatter dateFromString:[event.args objectAtIndex:0]];
            [self setDate:date animated:YES];
        }
        @catch (NSException *exception) {
            event.lastResult = @"Error entering countdown time (check the format: \"HH:mm\")";
            return;
        }
    }
    else {
        [super playbackMonkeyEvent:event];
        return;
    }
    
}

- (void) mtDateChanged {
    //    NSLog(@"Date: %@\nCountDownDuration: %f\n MinInterval: %i\nMode: %i", self.date, 
    //          self.countDownDuration, self.minuteInterval,self.datePickerMode);
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    
    //    [dateFormatter setDateStyle:NSDateFormatterMediumStyle];
    
    // For now only record date picker with Month/Day/Year
    if (self.datePickerMode == UIDatePickerModeDate)
    {
        [dateFormatter setDateFormat:@"yyyy-MM-dd"];
        [MonkeyTalk recordEvent:[[MTCommandEvent alloc] init:MTCommandEnterDate
                                                   className: [NSString stringWithUTF8String:class_getName(self.class)]
                                                    monkeyID:[self monkeyID]
                                                        args:[NSArray arrayWithObject:[dateFormatter stringFromDate:self.date]]]];
//        [[MonkeyTalk sharedMonkey] postCommandFrom:self
//                                           command:MTCommandEnterDate 
//                                              args:[NSArray arrayWithObject:[dateFormatter stringFromDate:self.date]]];
    }
    // New code for other UIDatePicker options-- Kapil
    else if(self.datePickerMode == UIDatePickerModeDateAndTime)
    {
        [dateFormatter setDateFormat:@"yyyy-MM-dd hh:mm a"];
        [MonkeyTalk recordEvent:[[MTCommandEvent alloc] init:MTCommandEnterDateAndTime
                                                   className:[NSString stringWithUTF8String:class_getName(self.class)]
                                                    monkeyID:[self monkeyID]
                                                        args:[NSArray arrayWithObject:[dateFormatter stringFromDate:self.date]]]];
//        [[MonkeyTalk sharedMonkey] postCommandFrom:self
//                                           command:MTCommandEnterDateAndTime 
//                                              args:[NSArray arrayWithObject:[dateFormatter stringFromDate:self.date]]];
    }
    else if(self.datePickerMode == UIDatePickerModeTime)
    {
        [dateFormatter setDateFormat:@"hh:mm a"];
        [MonkeyTalk recordEvent:[[MTCommandEvent alloc] init:MTCommandEnterTime
                                                   className:[NSString stringWithUTF8String:class_getName(self.class)]
                                                    monkeyID:[self monkeyID]
                                                        args:[NSArray arrayWithObject:[dateFormatter stringFromDate:self.date]]]];

        
//        [[MonkeyTalk sharedMonkey] postCommandFrom:self
//                                           command:MTCommandEnterTime
//                                              args:[NSArray arrayWithObject:[dateFormatter stringFromDate:self.date]]];
    }
    else if (self.datePickerMode == UIDatePickerModeCountDownTimer) {
        [dateFormatter setDateFormat:@"HH:mm"];
//        [[MonkeyTalk sharedMonkey] postCommandFrom:self 
//                                           command:MTCommandEnterCountDownTimer
//                                              args:[NSArray arrayWithObject:[dateFormatter stringFromDate:self.date]]];
        [MonkeyTalk recordEvent:[[MTCommandEvent alloc] init:MTCommandEnterCountDownTimer
                                                   className:[NSString stringWithUTF8String:class_getName(self.class)]
                                                    monkeyID:[self monkeyID]
                                                        args:[NSArray arrayWithObject:[dateFormatter stringFromDate:self.date]]]];
    }
    
}

- (void) mtsetDate:(NSDate *)date animated:(BOOL)animated {
    [self mtsetDate:date animated:animated];
}

- (id) mtinitWithFrame:(CGRect)frame {
    [self mtinitWithFrame:frame];
    [self addTarget:self action:@selector(mtDateChanged) forControlEvents:UIControlEventValueChanged];
    return self;
}

- (id) mtinitWithCoder:(NSCoder *)aDecoder {
    [self mtinitWithCoder:aDecoder];
    [self addTarget:self action:@selector(mtDateChanged) forControlEvents:UIControlEventValueChanged];
    return self;
}

@end
