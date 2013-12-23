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

#import "UIPIckerView+MTReady.h"
#import "MonkeyTalk.h"
#import "MTCommandEvent.h"
#import "MTUtils.h"
#import <objc/runtime.h>
#import "NSString+MonkeyTalk.h"
#import "UIView+MTReady.h"

@interface UIPickerView (Intercept)
- (void)origPickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component;
- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component;
@end

@implementation UIPickerView (MTReady)

- (NSString *)mtComponent {
    return MTComponentSelector;
}

+ (void)load {
    if (self == [UIPickerView class]) {
		
        Method originalMethod = class_getInstanceMethod(self, @selector(setDelegate:));
        Method replacedMethod = class_getInstanceMethod(self, @selector(mtSetDelegate:));
        method_exchangeImplementations(originalMethod, replacedMethod);		
    }
}

- (NSString *) valueForProperty:(NSString *)prop withArgs:(NSArray *)args {
    
    NSString *result;
    NSInteger arg1Int = 0;
    NSInteger arg2Int = 0;
    BOOL shouldReturnArray = NO;
    BOOL arrayHasSection = NO;
    
    // Return items in first row/component by default
    if ([prop isEqualToString:@"value" ignoreCase:YES]) {
        prop = @"selected";
        //        shouldReturnArray = YES;
    }
    
    if ([args count] > 0) {
        for (int i = 0; i < [args count]; i++) {
            NSString *arg = [args objectAtIndex:i];
            if (i == 0 && ([arg isEqualToString:@"[]"] ||
                           [arg isEqualToString:@"["])){
                //                shouldReturnArray = YES;
            } else if (i == 1 && shouldReturnArray && [arg isEqualToString:@"]"]) {
                arrayHasSection = YES;
            }
            
            arg = [arg stringByReplacingOccurrencesOfString:@"[" withString:@""];
            arg = [arg stringByReplacingOccurrencesOfString:@"]" withString:@""];
            
            if (i == 0 && [arg intValue] > 0)
                arg1Int = [arg intValue] - 1;
            else if (i == 1 && [arg intValue] > 0)
                arg2Int = [arg intValue] - 1;
        }
    }
    
    NSInteger row = arg1Int;
    NSInteger component = arg2Int;
    
    if ([prop isEqualToString:@"selected" ignoreCase:YES]) { 
        NSInteger selected = [self selectedRowInComponent:component];
        result = [self.delegate pickerView:self titleForRow:selected forComponent:component];
    } else if ([prop isEqualToString:@"item" ignoreCase:YES]) {
        result = [self.delegate pickerView:self titleForRow:row forComponent:component];
    } else if([prop isEqualToString:@"size" ignoreCase:YES]) {
        result = [NSString stringWithFormat:@"%i",[self numberOfRowsInComponent:arg1Int]];
    } else
        result = [self valueForKeyPath:prop];
    return result;
}

- (void) mtSetDelegate:(id <UIPickerViewDelegate>) del {
	Method originalMethod = class_getInstanceMethod([del class], @selector(pickerView:didSelectRow:inComponent:));
	if (originalMethod) {
		IMP origImp = method_getImplementation(originalMethod);
		Method replacedMethod = class_getInstanceMethod([self class], @selector(mtPickerView:didSelectRow:inComponent:));
		IMP replImp = method_getImplementation(replacedMethod);
		
		if (origImp != replImp) {
			method_setImplementation(originalMethod, replImp);
			class_addMethod([del class], @selector(origPickerView:didSelectRow:inComponent:), origImp,"v@:@ii");
		}
	}
	[self mtSetDelegate:del];

}
- (void)mtPickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component
{
    NSString *rowString = [NSString stringWithFormat:@"%i",row+1];
    NSString *componentString = [NSString stringWithFormat:@"%i",component+1];
    
    NSString *selectedTitle = @"";
    
    if (![self isKindOfClass:objc_getClass("_UIDatePickerView")] && ![self isKindOfClass:objc_getClass("UIDatePickerView")]) {

        if ([self respondsToSelector:@selector(pickerView:titleForRow:forComponent:)])
        {
            selectedTitle = [self pickerView:self titleForRow:row forComponent:component];
        }
        
        NSMutableArray *argsArray = [[NSMutableArray alloc] initWithObjects:rowString, nil];
        
        if ([componentString intValue] > 1)
            [argsArray addObject:componentString];
        
        if ([selectedTitle length] > 0)
            [ MonkeyTalk recordEvent:[[MTCommandEvent alloc]
                                      init:MTCommandSelect className:@"UIPickerView" monkeyID:[pickerView monkeyID] args:[NSArray arrayWithObject:selectedTitle]]];
        else
            [ MonkeyTalk recordEvent:[[MTCommandEvent alloc]
                                      init:MTCommandSelectIndex className:@"UIPickerView" monkeyID:[pickerView monkeyID] args:argsArray]];
        
    }
    
	[self origPickerView:pickerView didSelectRow:row inComponent:component];
}	

- (void) playbackMonkeyEvent:(MTCommandEvent*)event {
    // If it is a DatePicker ignore playback here
    if (![self isKindOfClass:objc_getClass("UIDatePickerView")] &&
        [self.delegate respondsToSelector:@selector(pickerView:titleForRow:forComponent:)]) {
        NSInteger row = -1;
        NSInteger component = -1;
        
        if ([event.command isEqualToString:MTCommandSelectIndex ignoreCase:YES]) {
            if ([event.args count] == 0) {
                event.lastResult = @"Requires 1 or 2 arguments (row #, component #)";
                return;
            }
            
            row = [event.args count] > 0 ? [[event.args objectAtIndex:0] intValue] : 0;
            component = [event.args count] > 1 ? [[event.args objectAtIndex:1] intValue] : 0;
            
            if (row > 0)
                row -= 1;
            if (component > 0)
                component -= 1;
        } else if ([event.command isEqualToString:MTCommandSelect ignoreCase:YES]) {
            if ([event.args count] == 0) {
                event.lastResult = @"Requires 1 arguments (title)";
                return;
            }
            
            for (int i = 0; i < [self numberOfComponents]; i++) {
                for (int j = 0; j < [self numberOfRowsInComponent:i]; j++) {
                    NSString *currentTitle = @"";
                    
                    if ([self.delegate respondsToSelector:@selector(pickerView:titleForRow:forComponent:)])
                        currentTitle = [self.delegate pickerView:self titleForRow:j forComponent:i];
                    
                    //                NSLog(@"Current: %@ Find: %@",currentTitle,[event.args objectAtIndex:0]);
                    
                    if ([currentTitle isEqualToString:[event.args objectAtIndex:0]]) {
                        row = j;
                        component = i;
                        break;
                    }
                }
                
                if (row >= 0)
                    break;
            }
        } else {
            [super playbackMonkeyEvent:event];
            return;
        }
        
        if (row < 0 || component < 0) {
            event.lastResult = [NSString stringWithFormat:@"Could not find row in UIPickerView"];
            return;
        }
        
        [self selectRow:row inComponent:component animated:NO];
        [(UIPickerView *)self.delegate origPickerView:self didSelectRow:row inComponent:component];
    }
}


- (BOOL) shouldRecordMonkeyTouch:(UITouch*)touch {
	return NO;
}

+ (NSString*) uiAutomationCommand:(MTCommandEvent*)command {
	NSMutableString* string = [[NSMutableString alloc] init];
	if ([command.command isEqualToString:MTCommandSelectIndex ignoreCase:YES]) {
		NSString* row = [command.args count] < 1 ? @"0" : [command.args objectAtIndex:0];
		NSString* component = [command.args count] < 2 ? @"0" : [command.args objectAtIndex:1];
        
		[string appendFormat:@"MonkeyTalk.selectPickerValue(\"%@\",\"%@\",\"%@\"); // UIAPicker", 
							[MTUtils stringByJsEscapingQuotesAndNewlines:command.monkeyID], 
							[MTUtils stringByJsEscapingQuotesAndNewlines:row],
							[MTUtils stringByJsEscapingQuotesAndNewlines:component]
						];
	} else {
		[string appendString:[super uiAutomationCommand:command]];
	}
	return string;
}

@end
