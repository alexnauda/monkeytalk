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

#import "UITextView+MTReady.h"
#import "MTUtils.h"
#import <objc/runtime.h>
#import "MonkeyTalk.h"
#import "MTCommandEvent.h"
#import "UIView+MTReady.h"
#import "NSObject+MTReady.h"
#import "NSString+MonkeyTalk.h"

@interface UITextView (MT_INTERCEPTOR)  
// Stop the compiler from whining
- (BOOL)orig_textViewShouldEndEditing:(UITextView *)textView;
- (BOOL) orig_textView:(UITextView *)textView shouldChangeTextInRange:(NSRange)range replacementText:(NSString *)string;
- (void) orig_setDelegate:(NSObject <UITextViewDelegate>*) del;
@end

@interface MTDefaultTextViewDelegate : NSObject <UITextViewDelegate>
@end
@implementation MTDefaultTextViewDelegate
@end

@implementation UITextView (MTReady)

- (NSString *)mtComponent {
    return MTComponentTextArea;
}

+ (void)load {
    if (self == [UITextView class]) {
		
        //[self mtSwapImplementation:@selector(setDelegate:)];
		[UIScrollView interceptMethod:@selector(setDelegate:) withClass:[UITextView class] types:"v@:@"];
    }
}

- (void) mtAssureAutomationInit {
	[super mtAssureAutomationInit];
	if (!self.delegate) {
        MTDefaultTextViewDelegate *del = [[MTDefaultTextViewDelegate alloc] init];
		self.delegate = del;
	}
}

- (void) mt_setDelegate:(NSObject <UITextViewDelegate>*) del {	
	//if ([self class] == [UITextView class]) {
		//	[del interceptMethod:@selector(textViewDidChangeSelection:) withClass:[self class] types:"v@:@"];
		[del interceptMethod:@selector(textViewShouldEndEditing:) withClass:[self class] types:"c@:@"];	
		[del interceptMethod:@selector(textViewShouldBeginEditing:) withClass:[self class] types:"c@:@"];			
		//	[del interceptMethod:@selector(textViewDidEndEditing:) withClass:[self class] types:"v@:@"];	
		[del interceptMethod:@selector(textView:shouldChangeTextInRange:replacementText:) withClass:[self class] types:"c@:@@@"];
	
//    if (del && [self.delegate class] == [MTDefaultTextViewDelegate class])
//        [self.delegate autorelease];
    
		//	[del interceptMethod:@selector(textViewDidChange:) withClass:[self class] types:"v@:@"];
	//}
	[self orig_setDelegate:del];

	
}


- (BOOL)mt_textViewShouldEndEditing:(UITextView *)textView {
	if ([MonkeyTalk isRecording]) {
//        NSLog(@"isRecording");
		MTCommandEvent* lastEvent = [[MonkeyTalk sharedMonkey] lastCommand];
		if (!([lastEvent.command isEqualToString:MTCommandInputText] || 
              [lastEvent.command isEqualToString:MTCommandEnterText ignoreCase:YES])) {
			[[MonkeyTalk sharedMonkey] recordFrom:textView command:MTCommandEnterText 
										  args:[NSArray arrayWithObjects: textView.text, @"true", nil]
										  post:NO];
//			NSInteger index = [[MonkeyTalk sharedMonkey].commands count] - 1;
//			MTCommandEvent* prevCommand = [[MonkeyTalk sharedMonkey] commandAt:index - 2];
										   
//			if (([prevCommand.command isEqualToString:MTCommandInputText] || 
//                 [[prevCommand.command isEqualToString:MTCommandEnterText ignoreCase:YES]) && 
//                [prevCommand.monkeyID isEqualToString:textView.monkeyID]) {
//				[[MonkeyTalk sharedMonkey] deleteCommand:index -2 ];
//				index--;
//			}
            
//			[[MonkeyTalk sharedMonkey] moveCommand:index to:index - 1];
		} else {
			if (([lastEvent.command isEqualToString:MTCommandInputText] || 
                 [lastEvent.command isEqualToString:MTCommandEnterText ignoreCase:YES]) && 
                [lastEvent.monkeyID isEqualToString:[textView monkeyID]]) {
				[[MonkeyTalk sharedMonkey] popCommand];
			}
			[ [MonkeyTalk sharedMonkey] recordFrom:textView command:MTCommandEnterText 
											  args:[NSArray arrayWithObjects: textView.text, @"true", nil]
											  post:NO];
		}
	}
	if (class_getInstanceMethod([self class], @selector(mt_textViewShouldEndEditing:))) {
		return ([(UITextView *)self.delegate mt_textViewShouldEndEditing:textView]);
	} else {
		return YES;
	}
}

- (BOOL) mt_textView:(UITextView *)textView shouldChangeTextInRange:(NSRange)range replacementText:(NSString *)string {
	if ([MonkeyTalk isRecording]) {
		MTCommandEvent* lastEvent = [[MonkeyTalk sharedMonkey] lastCommand];
		if (([lastEvent.command isEqualToString:MTCommandInputText] || 
             [lastEvent.command isEqualToString:MTCommandEnterText ignoreCase:YES]) 
            && [lastEvent.monkeyID isEqualToString:[textView monkeyID]]) {
			[[MonkeyTalk sharedMonkey] popCommand];
		}
		NSString* newVal = [textView.text stringByReplacingCharactersInRange:range withString:string];
		[ [MonkeyTalk sharedMonkey] recordFrom:textView command:MTCommandEnterText 
										  args:[NSArray arrayWithObjects: newVal, nil]
										  post:NO];
	}
	if (class_getInstanceMethod([self class], @selector(mt_textView:shouldChangeTextInRange:replacementText:))) {
		return [(UITextView *)self.delegate mt_textView:textView shouldChangeTextInRange:range replacementText:string];
	} else {
		return YES;
	}
	
}
//- (void) handleMonkeyTouchEvent:(NSSet*)touches withEvent:(UIEvent*)event {	
//	// [MonkeyTalk suspend];
//	//[super handleMonkeyTouchEvent:touches withEvent:event];
////	[[NSNotificationCenter defaultCenter] addObserver:self
////											 selector:@selector(keyBoardDismissed:)
////												 name:UIKeyboardWillHideNotification object:nil];
//	[[MonkeyTalk sharedMonkey] postCommandFrom:self command:MTCommandTouch args:nil];	
//	[[NSNotificationCenter defaultCenter] addObserver:self
//											 selector:@selector(keyBoardDismissed:)
//												 name:UITextViewTextDidEndEditingNotification object:nil];	
//}

//- (void) keyBoardDismissed:(NSNotification*) notification {
////	[[NSNotificationCenter defaultCenter] removeObserver:self
////												 name:UIKeyboardWillHideNotification object:nil];	
//	[[NSNotificationCenter defaultCenter] removeObserver:self
//												name:UITextViewTextDidEndEditingNotification object:nil];	
//	[[MonkeyTalk sharedMonkey] postCommandFrom:self command:MTCommandInputText args:[NSArray arrayWithObject:self.text]];
//}
//
//- (BOOL) shouldRecordMonkeyTouch:(UITouch*)touch {	
//	return (touch.phase == UITouchPhaseBegan);
//}

- (void) playbackMonkeyEvent:(MTCommandEvent*)event {
	if ([event.command isEqualToString:MTCommandInputText] || 
        [event.command isEqualToString:MTCommandEnterText ignoreCase:YES]) {
		NSString* newText = [event.args count] < 1 ? @"" : [event.args objectAtIndex:0];
		NSRange range;
		range.location = 0;
		range.length = [self.text length];
		//BOOL noInput = range.length == 0 && [newText length] == 0;
		if ([self.delegate textView:self shouldChangeTextInRange:range replacementText:newText]) {
			self.text = newText;
            
            // fire text change notification
            [[NSNotificationCenter defaultCenter] postNotificationName:@"UITextViewTextDidChangeNotification" object:self];
		}
        
        if ([event.args count] > 1 && [[event.args objectAtIndex:1] isEqualToString:@"true"] && [self isFirstResponder]) {
            [self resignFirstResponder];
        }

		return;
	}
	
	if ([event.command isEqualToString:MTCommandTouch] ||
        [event.command isEqualToString:MTCommandTap ignoreCase:YES]) {
		[self becomeFirstResponder];
		return;
	}
    
    if ([event.command isEqualToString:MTCommandClear ignoreCase:YES]) {
		self.text = nil;
            
        // fire text change notification
        [[NSNotificationCenter defaultCenter] postNotificationName:@"UITextViewTextDidChangeNotification" object:self];

		return;
	}
	
	[super playbackMonkeyEvent:event];
}

+ (NSString*) uiAutomationCommand:(MTCommandEvent*)command {
	NSMutableString* string = [[NSMutableString alloc] init];
	if ([command.command isEqualToString:MTCommandInputText]) {
		NSString* textValue = [command.args count] < 1 ? @"" : [command.args objectAtIndex:0];
		[string appendFormat:@"MonkeyTalk.elementNamed(\"%@\").setValue(\"%@\"); // UIATextView", 
		 [MTUtils stringByJsEscapingQuotesAndNewlines:command.monkeyID], 
		 [MTUtils stringByJsEscapingQuotesAndNewlines:textValue]];
	} else {
		[string appendString:[super uiAutomationCommand:command]];
	}
	return string;
}

- (BOOL) shouldRecordMonkeyTouch:(UITouch*)touch {
	return (touch.phase == UITouchPhaseEnded);
}


@end
