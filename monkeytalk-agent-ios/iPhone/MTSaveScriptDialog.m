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

#import "MTSaveScriptDialog.h"
#import "MonkeyTalk.h"
#import <QuartzCore/QuartzCore.h>


@implementation MTSaveScriptDialog
UITextField* filenameField = nil;
@synthesize table;

/*
 // The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if (self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil]) {
        // Custom initialization
    }
    return self;
}
*/

- (void) show {
    if ([[UIApplication sharedApplication] statusBarOrientation] == UIDeviceOrientationLandscapeLeft ||
        [[UIApplication sharedApplication] statusBarOrientation] == UIDeviceOrientationLandscapeRight)
        self.view.frame = CGRectMake(self.view.frame.origin.x, self.view.frame.origin.y, [[UIScreen mainScreen] bounds].size.height, self.view.frame.size.height);
    
	CATransition *transition = [CATransition animation];
    transition.duration = 0.5;
    transition.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseInEaseOut];
	transition.type =  kCATransitionMoveIn;
	transition.subtype  = kCATransitionFromBottom;
    transition.delegate = self;
    [self.view.layer addAnimation:transition forKey:nil];
	self.view.alpha = 1.0;
	//[[MTUtils rootWindow] bringSubviewToFront:[self view]];
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Rotation implementation
    [[UIDevice currentDevice] beginGeneratingDeviceOrientationNotifications];   
    [[NSNotificationCenter defaultCenter] 
     addObserver:self 
     selector:@selector(interfaceChanged) 
     name:@"UIDeviceOrientationDidChangeNotification" 
     object:nil];
    
    if ([[UIApplication sharedApplication] statusBarOrientation] == UIDeviceOrientationLandscapeLeft ||
        [[UIApplication sharedApplication] statusBarOrientation] == UIDeviceOrientationLandscapeRight)
        self.view.frame = CGRectMake(self.view.frame.origin.x, self.view.frame.origin.y, [[UIScreen mainScreen] bounds].size.height, [[UIScreen mainScreen] bounds].size.width);
    
	[self show];
}


- (void)didReceiveMemoryWarning {
	// Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
	
	// Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
	// Release any retained subviews of the main view.
	// e.g. self.myOutlet = nil;
}
- (UITextField *)fileNameField {
	if (filenameField == nil) {
		
		filenameField = [[UITextField alloc] initWithFrame:CGRectMake(10, 10, 285.0, 25.0)];
		
		filenameField.borderStyle = UITextBorderStyleNone;
		filenameField.textColor = [UIColor blackColor];
		filenameField.font = [UIFont systemFontOfSize:17.0];
		//filenameField.placeholder = @"<enter normal text>";
		//filenameField.backgroundColor = [UIColor whiteColor];
		filenameField.autocorrectionType = UITextAutocorrectionTypeNo;	// no auto correction support
		
		
		filenameField.keyboardType = UIKeyboardTypeDefault;	// use the default type input method (entire keyboard)
		filenameField.returnKeyType = UIReturnKeyDone;
		
		filenameField.clearButtonMode = UITextFieldViewModeWhileEditing;	// has a clear 'x' button to the right
		
		filenameField.tag = 0;		// tag this control so we can remove it later for recycled cells
		
		filenameField.delegate = self;	// let us be the delegate so we know when the keyboard's "Done" button is pressed
        
        [filenameField addTarget:self action:@selector(done:) forControlEvents:UIControlEventEditingDidEndOnExit];
		
		// Add an accessibility label that describes what the text field is for.
		[filenameField setAccessibilityLabel:NSLocalizedString(@"NormalTextField", @"")];
	}	
	return filenameField;
}



- (void) hide {
	[[self fileNameField] resignFirstResponder];
	CATransition *transition = [CATransition animation];
    transition.duration = 0.5;
    transition.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseInEaseOut];
	transition.type =  kCATransitionReveal;
	transition.subtype  = kCATransitionFromTop;
    transition.delegate = self;
    [self.view.layer addAnimation:transition forKey:nil];
	self.view.alpha = 0;
	//[[MTUtils rootWindow] bringSubviewToFront:[self view]];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
	return 1;
}



- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
	static NSString *CellIdentifier = @"MTCell";
	
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
	
    if (cell == nil) {
		
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle
				 
									   reuseIdentifier:CellIdentifier];
		
    }
	

	UITextField* file = [self fileNameField];
	[cell.contentView addSubview:file];
//	cell.textLabel.text = [NSString stringWithFormat:@"%@ %@ \"%@\"", recevent.command, recevent.className, recevent.monkeyID];
//	cell.textLabel.font = [cell.textLabel.font fontWithSize:[UIFont smallSystemFontSize]+2];
//    cell.detailTextLabel.text = [NSString stringWithFormat:@"%@", arg0 == nil ? @"" : arg0];
//	cell.detailTextLabel.textColor = [UIColor blackColor];
// 	cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
	cell.selectionStyle = UITableViewCellSelectionStyleNone;
	[file becomeFirstResponder];
    return cell;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
	return @"Save script as...";
}


- (IBAction) save:(id) sender {
	NSString* text = [self fileNameField].text;


	[[MonkeyTalk sharedMonkey] save:text];
	[self hide];
}

- (IBAction) done:(id) sender {
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone)
        [self save:nil];
    else
        [[self fileNameField] resignFirstResponder];
}

- (IBAction) cancel:(id) sender {
	//[[MonkeyTalk sharedMonkey] save:[[self fileNameField] text]];
	[self hide];
}

- (void) interfaceChanged
{
    if ([[self fileNameField] isFirstResponder]){
        [[self fileNameField] resignFirstResponder];
        [[self fileNameField] becomeFirstResponder];
    }
}

// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    return YES;
}




@end
