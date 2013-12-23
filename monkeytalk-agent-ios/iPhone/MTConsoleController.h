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

#import <UIKit/UIKit.h>
#import "MTConnectWebView.h"
#import "MTQunitView.h"


@interface MTConsoleController : UIViewController<UITextFieldDelegate> {
	UIView* moreView;
	UITableView* eventView;
	UISegmentedControl* controlBar;
    UIToolbar* toolBar;
    UIBarButtonItem *scriptButton;
    MTConnectWebView *connectWebView;
    MTQunitView *qUnitView;
    UIView *tableHeaderView;
    
    UITextField *liveTextField;
    UITextField *fixedTextField;
    UITextField *timeoutTextField;
    UISwitch *liveSwitch;
    UIButton *optionsButton;
}

@property (nonatomic, strong) IBOutlet UIView* moreView;
@property (nonatomic, strong) IBOutlet UITableView* eventView;
@property (nonatomic, strong) IBOutlet UISegmentedControl* controlBar;
@property (nonatomic, strong) IBOutlet UIToolbar* toolBar;
@property (nonatomic, strong) IBOutlet UIBarButtonItem *scriptButton;
@property (nonatomic, strong) MTConnectWebView *connectWebView;
@property (nonatomic, strong) MTQunitView *qUnitView;
@property (nonatomic, strong) IBOutlet UIView *tableHeaderView;
@property (nonatomic, strong) IBOutlet UITextField *liveTextField;
@property (nonatomic, strong) IBOutlet UITextField *fixedTextField;
@property (nonatomic, strong) IBOutlet UITextField *timeoutTextField;
@property (nonatomic, strong) IBOutlet UISwitch *liveSwitch;
@property (nonatomic, strong) UIButton *optionsButton;
- (IBAction) doMonkeyAction:(id)sender;
- (IBAction) save:(id)sender;
- (IBAction) open:(id)sender;
- (IBAction) gorilla:(id)sender;
- (IBAction) clear:(id)sender;
- (IBAction) editCommands:(id) sender;
- (IBAction) insertCommands:(id) sender;
- (IBAction)showView:(id)sender;
- (IBAction)showOptions:(id)sender;
- (IBAction) textInput:(id)sender;
- (IBAction) switchChanged:(id)sender;

- (void) monkeySuspended:(NSNotification*)notification;
-(void) refresh;
- (void) hideConsole;
-(void) hideConsoleAndThen:(SEL)action;
- (void) showConsole;
- (void) hideConsoleQunit;
- (void) showConsoleQunit;

+ (MTConsoleController*) sharedInstance;
@end
