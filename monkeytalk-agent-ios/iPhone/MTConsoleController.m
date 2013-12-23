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

#import "MTConsoleController.h"
#import "MonkeyTalk.h"
#import "MTCommandEvent.h"
#import "MTUtils.h"
#import "MTSaveScriptDialog.h"
#import "MTOpenScriptDialog.h"
#import "MTCommandEditViewController.h"
#import "MTGorillaView.h"
#import "MTEventViewCell.h"
#import <QuartzCore/QuartzCore.h>

const int LIVE_OPTIONS_HEIGHT = 48;
const int FULL_OPTIONS_HEIGHT = 104;

@implementation MTConsoleController

@synthesize moreView, eventView, controlBar, toolBar, connectWebView, qUnitView,scriptButton, tableHeaderView, liveTextField, fixedTextField, timeoutTextField, liveSwitch, optionsButton;

MonkeyTalk* theMonkey;
MTConsoleController* _sharedInstance;
MTCommandEditViewController* _editor = nil;
NSIndexPath* _indexPath;
NSMutableDictionary* _cmdIcons;
BOOL _more = NO;
UIWindow* _appWindow;

typedef enum {
	PAUSE,
	RECORD,
	PLAY,
	MORE
	
	
} consoleCommands;
typedef enum {
	DELETE,
	INSERT
	
} EditMode;

EditMode _editMode;

 // The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if (self = [super initWithNibName:nibNameOrNil bundle:nil]) {
		theMonkey = [MonkeyTalk sharedMonkey];
    }

	_sharedInstance = self;
    return self;
}



+ sharedInstance {
	return _sharedInstance;
}



// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
	moreView.alpha = 0.0;
	[[MTUtils rootWindow] sendSubviewToBack:[self view]];
	
	
	// MonkeyTalk sends notifications whenever it times out
	[[NSNotificationCenter defaultCenter] addObserver:self
											 selector:@selector(monkeySuspended:)
												 name:MTNotificationMonkeySuspended object:nil];	
	[[NSNotificationCenter defaultCenter] addObserver:self
											 selector:@selector(scriptOpened:)
												 name:MTNotificationScriptOpened object:nil];
	[[NSNotificationCenter defaultCenter] addObserver:self
											 selector:@selector(playingDone:)
												 name:MTNotificationPlayingDone object:nil];	
	// OCUnit may have already started up some tests
	if ([MonkeyTalk sharedMonkey].state != MTStatePlaying) {
		// Show the console
		[theMonkey suspend];
	}
    
    if (!getenv("MT_ENABLE_QUNIT"))
    {
        scriptButton.enabled = NO;
        scriptButton.image = nil;
    } else{
        connectWebView = [[MTConnectWebView alloc] initWithFrame:CGRectMake(0, 0, 320, 480)];
    }
    
    // Rotation implementation
    [[UIDevice currentDevice] beginGeneratingDeviceOrientationNotifications];   
    [[NSNotificationCenter defaultCenter] 
     addObserver:self 
     selector:@selector(interfaceChanged) 
     name:@"UIDeviceOrientationDidChangeNotification" 
     object:nil];
	//eventView.allowsSelection = NO;
    
    liveTextField.returnKeyType = UIReturnKeyDone;
    fixedTextField.returnKeyType = liveTextField.returnKeyType;
    timeoutTextField.returnKeyType = liveTextField.returnKeyType;
    
    [liveTextField setValue:[UIColor darkGrayColor] 
                    forKeyPath:@"_placeholderLabel.textColor"];
    [fixedTextField setValue:[UIColor darkGrayColor] 
                 forKeyPath:@"_placeholderLabel.textColor"];
    [timeoutTextField setValue:[UIColor darkGrayColor] 
                  forKeyPath:@"_placeholderLabel.textColor"];
    
    // Get and set the live switch state
    NSString *switchStateString = [MTUtils retrieveUserDefaultsForKey:MTOptionsLiveSwitch];
    if (!switchStateString)
        [MTUtils saveUserDefaults:@"NO" forKey:MTOptionsLiveSwitch];
    
    if ([switchStateString isEqualToString:@"YES"]) {
        liveSwitch.on = YES;
        liveTextField.alpha = 1;
    } else {
        liveSwitch.on = NO;
        liveTextField.alpha = 0;
    }
    
    liveTextField.text = [MTUtils retrieveUserDefaultsForKey:MTOptionsLive];
    fixedTextField.text = [MTUtils retrieveUserDefaultsForKey:MTOptionsFixed];
    timeoutTextField.text = [MTUtils retrieveUserDefaultsForKey:MTOptionsTimeout];
    
    
}


- (void) dismissKeyboard {
	[MTUtils dismissKeyboard];
}

-(void) refresh {	
	[self  dismissKeyboard];	
	[eventView reloadData];

	
}

- (void) scriptOpened:(NSNotification*)notification {
	[self refresh];
}


-(void)showMore
{
	_more = YES;
    CATransition *transition = [CATransition animation];
    transition.duration = 0.5;
    // using the ease in/out timing function
    transition.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseInEaseOut];
	transition.type =  kCATransitionMoveIn;
	transition.subtype  = kCATransitionFromTop;
	
    
    // Tto avoid overlapping transitions we assign ourselves as the delegate for the animation and wait for the
    // -animationDidStop:finished: message. When it comes in, we will flag that we are no longer transitioning.
	// transitioning = YES;
    transition.delegate = self;
    
    // Next add it to the containerView's layer. This will perform the transition based on how we change its contents.
	[moreView.layer addAnimation:transition forKey:nil];
    
	moreView.alpha = 1.0;
	//[[MTUtils rootWindow] bringSubviewToFront:[self view]];
}
- (void) showConsole:(BOOL)more {
	((UIWindow*)(self.view)).windowLevel = UIWindowLevelAlert;
	_appWindow = [MTUtils rootWindow];

	//[((UIWindow*)(self.view)) makeKeyAndVisible];
	[UIView beginAnimations:nil context:nil];	

	
	if (!more) {

		moreView.alpha = 0.0;
	}
	
	else {
		[self showMore];
	}
	//[[MTUtils rootWindow] bringSubviewToFront:[self view]];
	//_appWindow = [MTUtils rootWindow];
	[UIView setAnimationDuration:0.5];
	[UIView setAnimationTransition:UIViewAnimationTransitionCurlDown forView:[self view] cache:NO];
	[self view].alpha = 1.0;	
	[UIView setAnimationDelegate:self];
	[UIView setAnimationDidStopSelector:@selector(refresh)];	
	NSInteger firstError = [[MonkeyTalk sharedMonkey] firstErrorIndex];
	if (firstError > -1) {
		if (more) {
			[eventView scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:firstError inSection:0] atScrollPosition:UITableViewScrollPositionTop animated:YES];
		}
	}
		
	[UIView commitAnimations];	
}


- (void) playingDone:(NSNotification*)notification {
	
	[self showConsole:YES];
}

- (void) showConsole {
    // MT6: Hide Console
//	[self showConsole:NO]; 
}


-(void)showLess
{
    CATransition *transition = [CATransition animation];
    transition.duration = 0.5;
    // using the ease in/out timing function
    transition.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseInEaseOut];
	transition.type =  kCATransitionReveal;
	transition.subtype  = kCATransitionFromBottom;
	
    
    // Tto avoid overlapping transitions we assign ourselves as the delegate for the animation and wait for the
    // -animationDidStop:finished: message. When it comes in, we will flag that we are no longer transitioning.
	// transitioning = YES;
    transition.delegate = self;
    
    // Next add it to the containerView's layer. This will perform the transition based on how we change its contents.
    [moreView.layer addAnimation:transition forKey:nil];
    
	moreView.alpha = 0.0;
	//[[MTUtils rootWindow] bringSubviewToFront:[self view]];
}

- (void) monkeySuspended:(NSNotification*) notification {
	if ([MonkeyTalk sharedMonkey].state != MTStatePlaying) {
		[self showConsole];
	}
}

- (void) interfaceChanged
{
    [MTUtils interfaceChanged:self.view];
    
    if (qUnitView)
    {
        if ([[UIApplication sharedApplication] statusBarOrientation] == UIDeviceOrientationLandscapeLeft ||
            [[UIApplication sharedApplication] statusBarOrientation] == UIDeviceOrientationLandscapeRight)
        {
            qUnitView.frame = CGRectMake(qUnitView.frame.origin.x, qUnitView.frame.origin.y, [[UIScreen mainScreen] bounds].size.height, [[UIScreen mainScreen] bounds].size.width-20);
            
            for (UIView* subView in qUnitView.subviews)
            {
                if ([subView class] == [MTConnectWebView class])
                    subView.frame = CGRectMake(subView.frame.origin.x, subView.frame.origin.y, [[UIScreen mainScreen] bounds].size.height, [[UIScreen mainScreen] bounds].size.width-67);
                else if ([subView class] == [UIToolbar class])
                    subView.frame = CGRectMake(subView.frame.origin.x, subView.frame.origin.y, [[UIScreen mainScreen] bounds].size.height, subView.frame.size.height);
            }
        } else {
            qUnitView.frame = CGRectMake(0, 20, self.view.frame.size.width, self.view.frame.size.height-20);
            
            for (UIView* subView in qUnitView.subviews)
            {
                if ([subView class] == [MTConnectWebView class])
                    subView.frame = CGRectMake(subView.frame.origin.x, subView.frame.origin.y, self.view.frame.size.width, self.view.frame.size.height-67);
                else if ([subView class] == [UIToolbar class])
                    subView.frame = CGRectMake(subView.frame.origin.x, subView.frame.origin.y, self.view.frame.size.width, subView.frame.size.height);
            }
        }
        //[MTUtils interfaceChanged:qUnitView];
    }
        //[MTUtils interfaceChanged:qUnitView];
}

// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    return YES;
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


// Event table datasource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
	return [theMonkey commandCount];
}

- (UIImage*) getImage:(NSString*)cmd {
	NSString* imgname = [NSString stringWithFormat:@"MT%@",cmd];
	NSString *path = [[NSBundle mainBundle] pathForResource:imgname ofType:@"png"];
	if (!path) {
		// Try it without the leading "MT"
		path = [[NSBundle mainBundle] pathForResource:cmd ofType:@"png"];
	}
	if (!path) {
		path = [[NSBundle mainBundle] pathForResource:@"MTCommand" ofType:@"png"];
	}
	
	return path ? [UIImage imageWithContentsOfFile:path] : nil;
}

- (UIImage*) iconForCommand:(NSString*) cmd {
	UIImage* image = (UIImage*)[_cmdIcons objectForKey:cmd];
	if (image == nil) {
		image = [self getImage:cmd];
		[_cmdIcons setObject:image forKey:cmd];			
	}
	return image;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
	static NSString *CellIdentifier = @"MTCell";
	
    MTEventViewCell *cell = (MTEventViewCell*) [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
	
    if (cell == nil) {
		
        cell = [[MTEventViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle
				 
									   reuseIdentifier:CellIdentifier];
		
    }
	

    MTCommandEvent* command = [theMonkey commandAt:indexPath.row];
	NSString* className;
	if (command.className) {
		if ([command.className hasPrefix:@"UI"]) {
			className = [command.className substringFromIndex:2];
		} else {
			className = command.className;
		}
	} else {
		className = @"";
	}
	
	NSString* monkeyID = (command.monkeyID) ? command.monkeyID : @"";
	
	NSMutableString* args = [[NSMutableString alloc] init];
	for (NSString* arg in command.args) {
		[args appendFormat:@"%@ ",arg];
	}	
	cell.textLabel.text = [NSString stringWithFormat:@"%@ %@", className, monkeyID];
	cell.textLabel.font = [cell.textLabel.font fontWithSize:[UIFont smallSystemFontSize]+2];
    cell.detailTextLabel.text = args;
	cell.detailTextLabel.textColor = [UIColor blackColor];
 	cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
	cell.editingAccessoryType = UITableViewCellAccessoryDetailDisclosureButton;
    if ([command.command isEqualToString:MTCommandRun])
        cell.imageView.image = [self iconForCommand:MTCommandWebDriver];
    else
        cell.imageView.image = [self iconForCommand:command.command];
	if (command.lastResult) {
		cell.textLabel.textColor = [UIColor redColor];
		cell.detailTextLabel.text = command.lastResult;
		cell.detailTextLabel.textColor = [UIColor redColor];
	} else {
		cell.textLabel.textColor = [UIColor blackColor];
		cell.detailTextLabel.textColor = [UIColor blackColor];		
	}
    
    if ([className length] == 0 && [monkeyID length] == 0)
        cell.textLabel.text = command.command;
    
	cell.commandNumber = indexPath.row;
    return cell;
}

- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
	if (editingStyle == UITableViewCellEditingStyleDelete) {
		[[MonkeyTalk sharedMonkey] deleteCommand:indexPath.row];
		[tableView deleteRowsAtIndexPaths:[NSArray arrayWithObjects:indexPath, nil]  withRowAnimation:UITableViewRowAnimationFade];
	} else {
		indexPath = [NSIndexPath indexPathForRow:indexPath.row+1 inSection:0];
		[[MonkeyTalk sharedMonkey] insertCommand:indexPath.row];
		[UIView beginAnimations:nil context:nil];
		[UIView setAnimationDuration:0.75];
		[UIView setAnimationTransition:UIViewAnimationTransitionNone forView:[self view] cache:NO];
		[tableView insertRowsAtIndexPaths:[NSArray arrayWithObjects:indexPath, nil]  withRowAnimation:UITableViewRowAnimationFade];	
		
		[UIView setAnimationDelegate:self];
		_indexPath = indexPath;
		//[UIView setAnimationDidStopSelector:@selector(editNewCommand)];
		[UIView commitAnimations];

		
	}
}

- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath {
	[[MonkeyTalk sharedMonkey] moveCommand:fromIndexPath.row to:toIndexPath.row];
}

- (BOOL) showsReorderControl {
	return YES;

}


- (void)tableView:(UITableView *)tableView accessoryButtonTappedForRowWithIndexPath:(NSIndexPath *)indexPath {
	if (!_editor) {
		if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone)
		{
			_editor = [[MTCommandEditViewController alloc] initWithNibName:@"MTCommandEditViewController_iPhone" bundle:nil];
		}
		else
		{
			_editor = [[MTCommandEditViewController alloc] initWithNibName:@"MTCommandEditViewController_iPad" bundle:nil];
		}
		[self.view addSubview:_editor.view];
	}
	_editor.commandNumber = indexPath.row;
	[MTUtils navRight:_editor.view from:self.view];
}

- (void) editNewCommand {
		[self tableView:nil accessoryButtonTappedForRowWithIndexPath:_indexPath];	
		//[_indexPath release];
}


- (UITableViewCellEditingStyle)tableView:(UITableView *)tableView editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath {
	
	return _editMode == INSERT ? UITableViewCellEditingStyleInsert : UITableViewCellEditingStyleDelete;
}


-(void)animationDidStop:(CAAnimation *)theAnimation finished:(BOOL)flag
{
	// transitioning = NO;
}



-(void) hideConsoleAndThen:(SEL)action{
    // MT6: Hide Console
//	[UIView beginAnimations:nil context:nil];
//	[UIView setAnimationDuration:0.5];
//	[UIView setAnimationTransition:UIViewAnimationTransitionCurlUp forView:[self view] cache:NO];
//	[self view].alpha = 0.0;
//	[UIView setAnimationDelegate:self];
//	[UIView setAnimationDidStopSelector:action];
//	[UIView commitAnimations];
//	((UIWindow*)(self.view)).windowLevel = UIWindowLevelStatusBar;
//	[[[UIApplication sharedApplication] keyWindow] makeKeyAndVisible];
}

- (void) pause {
	[theMonkey pause];
}


- (void) record {
	[theMonkey record];
}

- (void) play {
	NSIndexPath* path = [eventView indexPathForSelectedRow];
	if (path) {
		[theMonkey playFrom:[path row]];
	} else {
		[theMonkey play];
	}
}

- (void) hideConsole {
	[self doMonkeyAction:controlBar];	
}

- (IBAction) doMonkeyAction:(id)sender {
	UISegmentedControl* seg = ((UISegmentedControl*)sender);	
	NSInteger index = seg.selectedSegmentIndex;
    
    if (index != RECORD)
        [MonkeyTalk sharedMonkey].previousTime = nil;
	
	switch (index) {
		case MORE:
			_more = !_more;
			if (_more) {
				[self showMore];
			} else {
				[self showLess];
			}
			seg.selectedSegmentIndex = UISegmentedControlNoSegment;
			return;				
		case RECORD:
			_more = NO;
			[self hideConsoleAndThen:@selector(record)];
			break;
		case PLAY:
			seg.selectedSegmentIndex = UISegmentedControlNoSegment;
			[self hideConsoleAndThen:@selector(play)];
			break;
		case PAUSE:
			_more = NO;
			[self hideConsoleAndThen:@selector(pause)];
			break;
			
	}
	
}


- (IBAction) timeoutDidChange:(id)sender {
	[MonkeyTalk sharedMonkey].runTimeout = [(UISlider*)sender value];
}


- (IBAction) open:(id)sender {
	MTSaveScriptDialog* dialog;
	if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone)
	{
		dialog =[[MTOpenScriptDialog alloc] initWithNibName:@"MTOpenScriptDialog_iPhone" bundle:nil];
	}
	else
	{
		dialog =[[MTOpenScriptDialog alloc] initWithNibName:@"MTOpenScriptDialog_iPad" bundle:nil];
	}
	[self.view addSubview:dialog.view];
}


- (IBAction) gorilla:(id)sender {
	MTSaveScriptDialog* dialog;
	if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone)
	{
		dialog =[[MTGorillaView alloc] initWithNibName:@"MTGorillaView_iPhone" bundle:nil];
	}
	else
	{
		dialog =[[MTGorillaView alloc] initWithNibName:@"MTGorillaView_iPad" bundle:nil];
	}
	[self.view addSubview:dialog.view];
}

- (IBAction) save:(id)sender {
	MTSaveScriptDialog* dialog;
	if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone)
	{
		dialog =[[MTSaveScriptDialog alloc] initWithNibName:@"MTSaveScriptDialog_iPhone" bundle:nil];
	}
	else
	{
		dialog =[[MTSaveScriptDialog alloc] initWithNibName:@"MTSaveScriptDialog_iPad" bundle:nil];
	}
	[self.view addSubview:dialog.view];
}

- (IBAction) clear:(id)sender {
	[[MonkeyTalk sharedMonkey] clear];
	[self refresh];
}

- (void) updateButtons:(BOOL)editing
{
    NSMutableArray *updatedItems = [toolBar.items mutableCopy];

	if (editing) {
        UIBarButtonItem *fixedSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFixedSpace target:nil action:nil];        
        UIBarButtonItem* doneButton = [[UIBarButtonItem alloc] initWithTitle:@"Done" style:UIBarButtonItemStyleDone target:self action:@selector(editCommands:)];
        
        // Set fix space width to keep gorilla center alligned
        fixedSpace.width = 29.0;
        
        // Remove add/delete items
        for (int i = 0; i < 2; i++)
            [updatedItems removeObjectAtIndex:[updatedItems count] - 1];
        
        [updatedItems addObject:fixedSpace];
        [updatedItems addObject:doneButton];
    }
    else
    {
        UIBarButtonItem* editButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(insertCommands:)];
        
        UIBarButtonItem* deleteButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemTrash target:self action:@selector(editCommands:)];
        
        // Remove fixed space and done item
        for (int i = 0; i < 2; i++)
            [updatedItems removeObjectAtIndex:[updatedItems count] - 1];
        
        // Set button style to bordered for add/delete items
        deleteButton.style = UIBarButtonItemStyleBordered;
        editButton.style = UIBarButtonItemStyleBordered;
        
        // Add buttons to tool bar
        [updatedItems addObject:editButton];
        [updatedItems addObject:deleteButton];
        
    }
    toolBar.items = updatedItems;
}

- (IBAction) editCommands:(id) sender {
	_editMode = DELETE;
	//NSLog(@"Edit!");
	BOOL editing = !(eventView.editing);
    
    // Update buttons based on editing state
    [self updateButtons:editing];
	
	[eventView setEditing:editing animated:true];
}

- (IBAction) insertCommands:(id) sender {
	_editMode = INSERT;
	BOOL editing = !(eventView.editing);
	
    // Update buttons based on editing state
    [self updateButtons:editing];
	
	[eventView setEditing:editing animated:true];
}

- (void)tabBarController:(UITabBarController *)tabBarController didSelectViewController:(UIViewController *)viewController {
	//[self refresh];
}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event {
	[super touchesEnded:touches withEvent:event];
	if (controlBar.selectedSegmentIndex == UISegmentedControlNoSegment) {
		controlBar.selectedSegmentIndex = PAUSE;
        [[MTConsoleController sharedInstance] hideConsole];
	} else {
		[[MTConsoleController sharedInstance] hideConsole];
	}
}

- (void)showView:(id)sender
{
    if (!qUnitView) {
        qUnitView = [[MTQunitView alloc] initWithFrame:CGRectMake(0, 20, self.view.frame.size.width, self.view.frame.size.height-20)];
        
        connectWebView.frame = CGRectMake(0, 47, self.view.frame.size.width, self.view.frame.size.height-67);
        
        [qUnitView addSubview:connectWebView];
        [self.view addSubview:qUnitView];
    } else {
        [UIView beginAnimations:nil context:nil];
        [UIView setAnimationDuration:0.5];
        qUnitView.frame = CGRectMake(qUnitView.frame.origin.x, 20, qUnitView.frame.size.width, qUnitView.frame.size.height);
        qUnitView.alpha = 1.0;
        [UIView setAnimationDelegate:self];
        [UIView commitAnimations];
    }
}

- (void) hideConsoleQunit {
    self.view.hidden = YES;
}

- (void) showConsoleQunit {
    self.view.hidden = NO;
}

- (void) animationComplete
{
    [eventView setTableHeaderView:tableHeaderView];
}

- (void) animateOptions:(int)height
{
    [UIView beginAnimations:nil context:nil];
    [UIView setAnimationDuration:0.3];
    tableHeaderView.frame = CGRectMake(tableHeaderView.frame.origin.x, tableHeaderView.frame.origin.y, tableHeaderView.frame.size.width, height);
    eventView.tableHeaderView.frame = CGRectMake(eventView.tableHeaderView.frame.origin.x, eventView.tableHeaderView.frame.origin.y, eventView.tableHeaderView.frame.size.width, height);
    
    if (liveSwitch.on)
        liveTextField.alpha = 1;
    else
        liveTextField.alpha = 0;
    
    [UIView setAnimationDelegate:self];
    if ([theMonkey commandCount] == 0 && 
        height == optionsButton.frame.size.height)
        [UIView setAnimationDidStopSelector:@selector(animationComplete)];
    else
        [eventView setTableHeaderView:tableHeaderView];
    [UIView commitAnimations];
}

- (void) showOptions:(id)sender
{
    UIButton *button = (UIButton *)sender;
    optionsButton = button;
    int height = 0;
    
    if (tableHeaderView.frame.size.height > button.frame.size.height)
        height = button.frame.size.height;
    else
    {
        if (liveSwitch.on == YES)
            height = button.frame.size.height + LIVE_OPTIONS_HEIGHT;
        else
            height = button.frame.size.height + FULL_OPTIONS_HEIGHT;
    }
    
    [self animateOptions:height];
}

- (void) textInput:(id)sender
{
    UITextField *textField = (UITextField *)sender;
    
    if (textField.tag == 0)
    {
        [MTUtils saveUserDefaults:textField.text forKey:MTOptionsLive];
    }
    else if (textField.tag == 1)
        [MTUtils saveUserDefaults:textField.text forKey:MTOptionsFixed];
    else if (textField.tag == 2)
        [MTUtils saveUserDefaults:textField.text forKey:MTOptionsTimeout];
}

- (void) switchChanged:(id)sender
{
    UISwitch *senderSwitch = (UISwitch *)sender;
    NSString *stateString = nil;
    int height = 0;
    
    if (senderSwitch.on == YES)
    {
        stateString = @"YES";
        height = optionsButton.frame.size.height + LIVE_OPTIONS_HEIGHT;
    }
    else
    {
        stateString = @"NO";
        height = optionsButton.frame.size.height + FULL_OPTIONS_HEIGHT;
    }
    
    [self animateOptions:height];
    
    [MTUtils saveUserDefaults:stateString forKey:MTOptionsLiveSwitch];
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    if (textField.tag == 0 && [textField.text length] > 0)
    {
        textField.text = [textField.text stringByReplacingOccurrencesOfString:@"%" withString:@""];
        textField.text = [NSString stringWithFormat:@"%@%%",textField.text];
    }
	[textField resignFirstResponder];
	return YES;
}



@end
