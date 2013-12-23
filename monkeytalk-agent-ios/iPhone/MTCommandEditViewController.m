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

#import "MTCommandEditViewController.h"
#import "MTUtils.h"
#import "MonkeyTalk.h"
#import "MTCommandEvent.h"
#import "MTConsoleController.h"
#import <UIKit/UIKit.h>


#define kTextFieldWidth	260.0



#define NUMFIELDS 7

@interface MTCommandEditViewController()
    - (void) changeTimeout;
    - (void) textChanged:(id)sender;
@end

@implementation MTCommandEditViewController
typedef enum {
	COMMAND,
	CLASS_NAME,
	MONKEY_ID,
    PLAYBACK_TIMEOUT,
	ARG1,
	ARG2,
	ARG3
} fields;

@synthesize layoutTable;
@dynamic commandNumber;

UITextField* _monkeyIDField;
UITextField* _commandField;
UIView* _back;
NSInteger _index;
MTCommandEvent* _command;
NSMutableArray* _fields;
BOOL _keyboardShown;
int _kbAdjustment;
int _commandNumber;




-(void)registerForKeyboardNotifications
{
    [[NSNotificationCenter defaultCenter] addObserver:self
											 selector:@selector(keyboardWasShown:)
												 name:UIKeyboardDidShowNotification object:nil];
	
    [[NSNotificationCenter defaultCenter] addObserver:self
											 selector:@selector(keyboardWasHidden:)
												 name:UIKeyboardDidHideNotification object:nil];
}



- (UITextField *)textField:(BOOL)indent{
	
	int x = indent ? 80 : 10;	
	UITextField* field = [[UITextField alloc] initWithFrame:CGRectMake(x, 10, 285.0 - x, 25.0)];
	field.borderStyle = UITextBorderStyleNone;
	field.textColor = [UIColor blackColor];
	field.font = [UIFont systemFontOfSize:17.0];
	field.adjustsFontSizeToFitWidth = YES;
	field.minimumFontSize = 8.0;
	//_monkeyIDField.placeholder = @"<enter normal text>";
	//_monkeyIDField.backgroundColor = [UIColor whiteColor];
	field.autocorrectionType = UITextAutocorrectionTypeNo;	// no auto correction support
	
	field.keyboardType = UIKeyboardTypeDefault;	// use the default type input method (entire keyboard)
	field.returnKeyType = UIReturnKeyDone;
	
	field.clearButtonMode = UITextFieldViewModeWhileEditing;	// has a clear 'x' button to the right
	
	//field.tag = 0;		// tag this control so we can remove it later for recycled cells
	
	field.delegate = self;	// let us be the delegate so we know when the keyboard's "Done" button is pressed
	
	
	return field;
}

- (id)initWithNibName:(NSString *)nibName bundle:(NSBundle *)nibBundle {
	if (self = [super initWithNibName:nibName bundle:nibBundle]) {
		[self registerForKeyboardNotifications];
		
		_fields = [[NSMutableArray alloc] initWithCapacity:8];
		
		self.title = @"Edit Command";
		[self.view setAccessibilityLabel:@"Edit Command View"];
		int i;
		for (i = 0; i < NUMFIELDS; i++) {
			[_fields addObject:[self textField:(i < ARG1)]];
		}
		
	}
	return self;
}
- (void)viewDidLoad
{
	[super viewDidLoad];
    // Rotation implementation
    if ([[UIApplication sharedApplication] statusBarOrientation] == UIDeviceOrientationLandscapeLeft ||
        [[UIApplication sharedApplication] statusBarOrientation] == UIDeviceOrientationLandscapeRight)
        self.view.frame = CGRectMake(self.view.frame.origin.x, self.view.frame.origin.y, [[UIScreen mainScreen] bounds].size.height, [[UIScreen mainScreen] bounds].size.width);
}

- (void) reset {
	for (UITextField* field in _fields) {
		field.text = nil;
	}
}
- (void) setCommandNumber:(NSInteger)num {
	[self reset];
	_commandNumber = num;
	//[_command release];
	NSMutableDictionary* dic = [[[MonkeyTalk sharedMonkey] commands] objectAtIndex:num];
	_command = [[MTCommandEvent alloc] initWithDict:dic];
	[layoutTable reloadData];
}

- (NSInteger) commandNumber {
	return _commandNumber;
}

#pragma mark -
#pragma mark UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return 2;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
	if (section == 0) {
		return nil;
	}
	
	return @"Arguments";
	
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if (section == 0 &&
        ![[MTUtils retrieveUserDefaultsForKey:MTOptionsLiveSwitch] isEqualToString:@"YES"])
        return 4;
    
    return 3;
}


//
- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	return ([indexPath row] == 0) ? 50.0 : 22.0;
}

// to determine which UITableViewCell to be used on a given row.
//
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	UITableViewCell *cell = nil;
	
	NSString *kSourceCell_ID = [NSString stringWithFormat:@"%d:%d", indexPath.section, indexPath.row];
	cell = [tableView dequeueReusableCellWithIdentifier:kSourceCell_ID];
	if (cell == nil)
	{
		cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:kSourceCell_ID];
		cell.selectionStyle = UITableViewCellSelectionStyleNone;
		
		cell.textLabel.textAlignment = UITextAlignmentLeft;
		cell.textLabel.textColor = [UIColor grayColor];
		cell.textLabel.font = [UIFont systemFontOfSize:10];
		
	} 
	
	//cell.textLabel.text = @"Target ID!";
	
	UITextField* field;
	
	if (indexPath.section == 0) {
		switch (indexPath.row) {
			case COMMAND:
				field = [_fields objectAtIndex:COMMAND];
				field.text = _command.command;
				cell.textLabel.text = @"Command";
				break;
			case CLASS_NAME:
				field = [_fields objectAtIndex:CLASS_NAME];
				field.text = _command.className;
				cell.textLabel.text = @"Class Name";
				break;
			case MONKEY_ID:
				field = [_fields objectAtIndex:MONKEY_ID];
				field.text = _command.monkeyID;
				cell.textLabel.text = @"Monkey ID";	
				break;
            case PLAYBACK_TIMEOUT:
				field = [_fields objectAtIndex:PLAYBACK_TIMEOUT];
				field.text = _command.playbackTimeout;
				cell.textLabel.text = @"Timeout";	
				break;
		}
        
        field.tag = indexPath.row;
//        [field addTarget:self action:@selector(textChanged:) forControlEvents:UIControlEventEditingChanged];
	} else {
		int index = ARG1 + indexPath.row;
		field = [_fields objectAtIndex:index];
		if (indexPath.row < [_command.args count]) {
			field.text = [_command.args objectAtIndex:indexPath.row]; 
		}
	}
	if (field.superview == nil) {
		[cell.contentView addSubview:field];
	}
	
    return cell;
	
}



- (void) done:(id)sender {
	_command.command = [[_fields objectAtIndex:COMMAND] text];
	_command.className = [[_fields objectAtIndex:CLASS_NAME] text];
	_command.monkeyID = [[_fields objectAtIndex:MONKEY_ID] text];
    _command.playbackTimeout = [[_fields objectAtIndex:PLAYBACK_TIMEOUT] text];
	_command.args = [NSArray arrayWithObjects: 
					 [[_fields objectAtIndex:ARG1] text],
					 [[_fields objectAtIndex:ARG2] text],	
					 [[_fields objectAtIndex:ARG3] text],
					 nil];
	[MTUtils dismissKeyboard];
	[[MTConsoleController sharedInstance] refresh];
	[MTUtils navLeft:self.view to:_back];
}


#pragma mark -
#pragma mark UITextFieldDelegate

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
	// the user pressed the "Done" button, so dismiss the keyboard
	[textField resignFirstResponder];
	return YES;
}



// Called when the UIKeyboardDidShowNotification is sent.
- (void)keyboardWasShown:(NSNotification*)aNotification
{
    if (_keyboardShown)
        return;
	
    NSDictionary* info = [aNotification userInfo];
	
	
    // Get the size of the keyboard.
    NSValue* aValue = [info objectForKey:UIKeyboardBoundsUserInfoKey];
    CGSize keyboardSize = [aValue CGRectValue].size;
	UITableView* table = layoutTable;
    CGRect viewFrame = [table frame];
	int screenheight = [[UIScreen mainScreen] applicationFrame].size.height;
	_kbAdjustment =  ((viewFrame.origin.y + viewFrame.size.height) -  (screenheight - keyboardSize.height)) - 20;
    viewFrame.size.height -= _kbAdjustment;
	
    // Scroll the active text field into view.
	
    CGRect textFieldRect = [[_fields objectAtIndex:ARG3] frame];
	//UIWindow *keyWindow = [[UIApplication sharedApplication] keyWindow];
	UIWindow* keyWindow = (UIWindow*)[MTConsoleController sharedInstance].view;
	UIView   *firstResponder = [keyWindow performSelector:@selector(firstResponder)];
	
	textFieldRect = [table convertRect:textFieldRect fromView:[firstResponder  superview]];
	CGPoint bottom;
	bottom.x = 0;
	bottom.y = textFieldRect.origin.y + textFieldRect.size.height;
	
	//	if ((viewFrame.origin.y + viewFrame.size.height) > bottom.y) {
	//		_kbAdjustment = 0;
	//		// Field is visible so no need to scroll
	//		return;
	//	}
	NSLog(@"Resizing from %f", [layoutTable frame].size.height);
	
    table.frame = viewFrame;
	
    [table scrollRectToVisible:textFieldRect animated:YES];
	
	
    _keyboardShown = YES;
}


// Called when the UIKeyboardDidHideNotification is sent
- (void)keyboardWasHidden:(NSNotification*)aNotification
{
	[UIView beginAnimations:nil context:nil];
	[UIView setAnimationDuration:0.5];
	[UIView setAnimationTransition:UIViewAnimationTransitionNone forView:[self view] cache:YES];
	UITableView* table = layoutTable;
	//[table scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:0 inSection:0] atScrollPosition:UITableViewScrollPositionTop animated:YES];
	
	// Resize to original
    CGRect viewFrame = [table frame];
    viewFrame.size.height += _kbAdjustment;
    table.frame = viewFrame;
	//	[UIView setAnimationDelegate:self];
	//	[UIView setAnimationDidStopSelector:action];
	[UIView commitAnimations];		
    _keyboardShown = NO;
}

- (void)textFieldDidEndEditing:(UITextField *)textField {
	_command.command = [[_fields objectAtIndex:COMMAND] text];
	_command.className = [[_fields objectAtIndex:CLASS_NAME] text];
	_command.monkeyID = [[_fields objectAtIndex:MONKEY_ID] text];	
	_command.args = [NSArray arrayWithObjects: 
					 [[_fields objectAtIndex:ARG1] text],
					 [[_fields objectAtIndex:ARG2] text],	
					 [[_fields objectAtIndex:ARG3] text],
					 nil];
}

@end

