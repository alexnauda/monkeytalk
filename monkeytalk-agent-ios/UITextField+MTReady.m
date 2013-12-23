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

#import "UITextField+MTReady.h"

#import "MonkeyTalkAPI.h"
#import "MonkeyTalk.h"
#import "MTUtils.h"
#import <objc/runtime.h>
#import "NSObject+MTReady.h"
#import "NSString+MonkeyTalk.h"
#import "MTOrdinalView.h"
#import "MTCache.h"

#define MTTextFieldStateKey @"MTTextFieldState"

@interface UITextField (MT_INTERCEPTOR) 
// Stop the compiler from whining
- (BOOL) orig_textFieldShouldReturn:(UITextField*)textField;
- (BOOL) orig_textFieldShouldClear:(UITextField*)textField;
- (BOOL) orig_textFieldShouldEndEditing:(UITextField*)textField;
- (void) orig_textFieldDidEndEditing:(UITextField*)textField;
- (BOOL) orig_textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string;
@end

@interface MTDefaultTextFieldDelegate : NSObject <UITextFieldDelegate>
@end
@implementation MTDefaultTextFieldDelegate

- (void)dealloc {
    NSLog(@"deallocating %@", self);
}

@end


@implementation UITextField (MTReady)

typedef enum {
	MTTextFieldStateNormal,
	MTTextFieldStateEditing,
	MTTextFieldStateReturn
} MTTextFieldState;

- (NSString *)mtComponent {
    return MTComponentInput;
}

+ (void)load {
    if (self == [UITextField class]) {
        Method originalMethod = class_getInstanceMethod(self, @selector(setDelegate:));
        Method replacedMethod = class_getInstanceMethod(self, @selector(mtSetDelegate:));
        method_exchangeImplementations(originalMethod, replacedMethod);	
    }
}
		 
- (void) mtAssureAutomationInit {
	[super mtAssureAutomationInit];
    
    if (self.class != NSClassFromString(@"UIAlertSheetTextField")) {
        if (!self.delegate) {
            MTDefaultTextFieldDelegate *del = [[MTDefaultTextFieldDelegate alloc] init];
            self.delegate = del;
        }
    }
}

//- (UIControlEvents)monkeyEventsToHandle {
//	return UIControlEventEditingDidEnd | UIControlEventTouchDown;
//}	

- (void) mtSetDelegate:(NSObject <UITextFieldDelegate>*) del {  
	[del interceptMethod:@selector(textFieldShouldReturn:) withClass:[self class] types:"c@:@"];
	[del interceptMethod:@selector(textFieldShouldEndEditing:) withClass:[self class] types:"v@:@"];	
	[del interceptMethod:@selector(textFieldDidEndEditing:) withClass:[self class] types:"v@:@"];	
	[del interceptMethod:@selector(textField:shouldChangeCharactersInRange:replacementString:) withClass:[self class] types:"c@:@@@"];	
	[del interceptMethod:@selector(textFieldShouldClear:) withClass:[self class] types:"c@:@"];
    [MTCache add:del];
    
	[self mtSetDelegate:del];
	
}

- (NSString*) baseMonkeyID {
	if (self.placeholder && self.placeholder.length>0) {
	    return self.placeholder;
	}
    return [super baseMonkeyID];
}

//- (void) handleMonkeyEventFromSender:(id)sender forEvent:(UIEvent*)event {
//
//	if (event) {
//		
//		if (event.type == UIEventTypeTouches) {
//			[MonkeyTalk recordFrom:self command:MTCommandTouch];
//		}
//		
//		return;
//	} 
//	
////	if (!self.editing && self.text != nil) {
////		[MonkeyTalkAPI record:self command:MTCommandInputText args:[NSArray arrayWithObject:[self.text copy]]];
////	} else {
////		[MonkeyTalkAPI continueRecording];
////	}
//	
//}

+ (void) mtSetState:(MTTextFieldState)s {
	[[MonkeyTalk sharedMonkey].session setObject:[NSString stringWithFormat:@"%d",s] forKey:MTTextFieldStateKey];
}

+ (MTTextFieldState) mtState {
	MTTextFieldState s = [((NSString*)[[MonkeyTalk sharedMonkey].session objectForKey:MTTextFieldStateKey]) intValue];
	return s;
}

- (BOOL)mt_textFieldShouldReturn:(UITextField *)textField {
	if ([MonkeyTalk isRecording]) {
		[UITextField mtSetState:MTTextFieldStateReturn];
		MTCommandEvent* lastEvent = [[MonkeyTalk sharedMonkey] lastCommand];
		if (([lastEvent.command isEqualToString:MTCommandInputText] || [lastEvent.command isEqualToString:MTCommandEnterText ignoreCase:YES]) && [lastEvent.monkeyID isEqualToString:[textField monkeyID]]) {
			[[MonkeyTalk sharedMonkey] popCommand];
		}		
//		[ MonkeyTalk recordEvent:[[MTCommandEvent alloc]
//								  init:MTCommandReturn className:[NSString stringWithUTF8String:class_getName([textField class])]
//								  monkeyID:[textField monkeyID]
//								  args:[NSArray arrayWithObjects: textField.text, 
//										nil]]];
        [MonkeyTalk recordFrom:textField command:MTCommandEnterText args:[NSArray arrayWithObjects: textField.text, 
                                                                          @"enter", 
                                                                          nil]];
//		[ MonkeyTalk recordEvent:[[MTCommandEvent alloc]
//								  init:MTCommandEnterText className:[NSString stringWithUTF8String:class_getName([textField class])]
//								  monkeyID:[textField monkeyID]
//								  args:[NSArray arrayWithObjects: textField.text, 
//                                        @"enter", 
//										nil]]];	
	}
	if (class_getInstanceMethod([self class], @selector(orig_textFieldShouldReturn:))) {
		return ([self orig_textFieldShouldReturn:textField]);
	} else {
		return YES;
	}
}

- (BOOL)mt_textFieldShouldClear:(UITextField *)textField {
	if ([MonkeyTalk isRecording]) {
		[UITextField mtSetState:MTTextFieldStateEditing];
		[MonkeyTalk recordFrom:textField command:MTCommandClear];
	}
	if (class_getInstanceMethod([self class], @selector(orig_textFieldShouldClear:))) {
		return [self orig_textFieldShouldClear:textField];
	} else {
		return YES;
	}
}

- (BOOL)mt_textFieldShouldEndEditing:(UITextField *)textField {
	if ([MonkeyTalk isRecording]) {
//		NSInteger index = [[[MonkeyTalk sharedMonkey] commands] count] - 1;
//		if (index < 0 || ![[[MonkeyTalk sharedMonkey] commandAt:index].command isEqualToString:MTCommandReturn]) {
//			[ [MonkeyTalk sharedMonkey] recordFrom:textField command:MTCommandInputText 
//							   args:[NSArray arrayWithObjects: textField.text, nil]
//							   post:NO];
//			[[MonkeyTalk sharedMonkey] moveCommand:index + 1 to:index];
//		}
	}
	if (class_getInstanceMethod([self class], @selector(orig_textFieldShouldEndEditing:))) {
		return ([self orig_textFieldShouldEndEditing:textField]);
	} else {
		return YES;
	}
}

- (void)mt_textFieldDidEndEditing:(UITextField *)textField {
	if ([MonkeyTalk isRecording]) {
		[UITextField mtSetState:MTTextFieldStateNormal];
	}
	
	if (class_getInstanceMethod([self class], @selector(orig_textFieldDidEndEditing:))) {
		[self orig_textFieldDidEndEditing:textField];
	}
}

- (BOOL) mt_textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
	if ([MonkeyTalk isRecording]) {
//        NSLog(@"Special: %@",[NSString stringWithUTF8String:[string UTF8String]]);
		[UITextField mtSetState:MTTextFieldStateEditing];
		if ([MonkeyTalk isRecording]) {
			MTCommandEvent* lastEvent = [[MonkeyTalk sharedMonkey] lastCommand];
			if (([lastEvent.command isEqualToString:MTCommandInputText] || 
                 [lastEvent.command isEqualToString:MTCommandEnterText ignoreCase:YES]) && [lastEvent.monkeyID isEqualToString:[textField monkeyID]]) {
				[[MonkeyTalk sharedMonkey] popCommand];
			}
			NSString* newVal = [textField.text stringByReplacingCharactersInRange:range withString:string];
			[ [MonkeyTalk sharedMonkey] recordFrom:textField command:MTCommandEnterText 
											  args:[NSArray arrayWithObjects: newVal, nil]
											  post:NO];
		}		
	}
	if (class_getInstanceMethod([self class], @selector(orig_textField:shouldChangeCharactersInRange:replacementString:))) {
		return [self orig_textField:textField shouldChangeCharactersInRange:range replacementString:string];
	} else {
		return YES;
	}

}

- (void) playbackMonkeyEvent:(MTCommandEvent*)recevent {
	[self mtAssureAutomationInit];
	if ([recevent.command isEqualToString:MTCommandReturn ignoreCase:YES] || 
        [recevent.command isEqualToString:MTCommandInputText ignoreCase:YES] || [recevent.command isEqualToString:MTCommandEnterText ignoreCase:YES] ) {
		NSString* newText = [recevent.args count] < 1 ? @"" : [recevent.args objectAtIndex:0];
		NSRange range;
		range.location = 0;
		range.length = [self.text length];
		BOOL noInput = range.length == 0 && [newText length] == 0;
		if (noInput  || [self.delegate textField:self shouldChangeCharactersInRange:range replacementString:newText]) {
			if (!noInput) {
				self.text = newText;
                
                // fire notification
                [[NSNotificationCenter defaultCenter] postNotificationName:@"UITextFieldTextDidChangeNotification" object:self];
                
                // fire event
                [self sendActionsForControlEvents:UIControlEventEditingChanged];
			}
            
            // Input * EnterText "some text" enter (will trigger a return)
            BOOL enterTextReturn = [recevent.command isEqualToString:MTCommandEnterText ignoreCase:YES] && [recevent.args count] > 1 && [[recevent.args objectAtIndex:1] isEqualToString:@"enter"];
            
			if ([recevent.command isEqualToString:MTCommandReturn ignoreCase:YES] || enterTextReturn) {
				if ([self.delegate textFieldShouldReturn:self]) {
				// If text field should return, then autocorrected value would be accepted?
				// Autocorrected text handling goes here?
                     [self sendActionsForControlEvents:UIControlEventEditingDidEndOnExit];
				}
			} else {
				if ([self.delegate textFieldShouldEndEditing:self]) {
					[self resignFirstResponder];	
				}
			}
		} 
		return;
	}
	
	if ([recevent.command isEqualToString:MTCommandTouch] ||
        [recevent.command isEqualToString:MTCommandTap ignoreCase:YES]) {
		[self becomeFirstResponder];
		return;
	}
				 
	if ([recevent.command isEqualToString:MTCommandClear ignoreCase:YES]) {
		if ([self.delegate textFieldShouldClear:self]) {
			self.text = nil;
            
            // fire text change notification
            [[NSNotificationCenter defaultCenter] postNotificationName:@"UITextFieldTextDidChangeNotification" object:self];
            
            // fire text change event
            [self sendActionsForControlEvents:UIControlEventEditingChanged];
		}
		return;
	}
	
	[super playbackMonkeyEvent:recevent];
	
}

+ (NSString*) uiAutomationCommand:(MTCommandEvent*)command {
	NSMutableString* string = [[NSMutableString alloc] init];
	if ([command.command isEqualToString:MTCommandInputText]) {
		NSString* value = [command.args count] < 1 ? @"" : [command.args objectAtIndex:0];
		[string appendFormat:@"MonkeyTalk.elementNamed(\"%@\").setValue(\"%@\"); // UIATextField", 
						[MTUtils stringByJsEscapingQuotesAndNewlines:command.monkeyID], 
						[MTUtils stringByJsEscapingQuotesAndNewlines:value]];
	} else {
		[string appendString:[super uiAutomationCommand:command]];
	}
	return string;
}



@end
