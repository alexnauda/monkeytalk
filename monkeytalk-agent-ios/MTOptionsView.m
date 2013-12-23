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

#import "MTOptionsView.h"
#import "MTUtils.h"


@implementation MTOptionsView

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
    }
    return self;
}

- (id) initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self) {
        // Initialization code
        self.backgroundColor = [UIColor scrollViewTexturedBackgroundColor];
        
        CGPoint point1 = CGPointMake(8, 8);
        CGPoint point2 = CGPointMake((int)self.frame.size.width/2, 8);
        CGSize labelSize = CGSizeMake((int)self.frame.size.width/2-8, 18);
        CGSize textFieldSize = CGSizeMake((int)self.frame.size.width/2-8, 28);
        
        float textFieldY = labelSize.height + point1.y;
        
        UILabel *delayLabel = [[UILabel alloc] initWithFrame:CGRectMake(point1.x, point1.y, labelSize.width, labelSize.height)];
        delayLabel.font = [UIFont systemFontOfSize:14];
        delayLabel.text = @"Playback Delay";
        delayLabel.textColor = [UIColor whiteColor];
        delayLabel.shadowColor = [UIColor darkGrayColor];
        delayLabel.shadowOffset = CGSizeMake(1, 1);
        delayLabel.backgroundColor = [UIColor clearColor];
        
        UITextField * delayTextField = [[UITextField alloc] initWithFrame:CGRectMake(point1.x, textFieldY, textFieldSize.width, textFieldSize.height)];
        delayTextField.borderStyle = UITextBorderStyleLine;
        delayTextField.placeholder = @"seconds";
        delayTextField.backgroundColor = [UIColor whiteColor];
        delayTextField.text = [MTUtils retrieveUserDefaultsForKey:@"MTDelay"];
        delayTextField.tag = 0;
        delayTextField.returnKeyType = UIReturnKeyDone;
        delayTextField.clearButtonMode = UITextFieldViewModeWhileEditing;
        
        UITextField * retryTextField = [[UITextField alloc] initWithFrame:CGRectMake(point2.x, textFieldY, textFieldSize.width, textFieldSize.height)];
        retryTextField.borderStyle = delayTextField.borderStyle;
        retryTextField.placeholder = @"count";
        retryTextField.backgroundColor = delayTextField.backgroundColor;
        retryTextField.text = [MTUtils retrieveUserDefaultsForKey:@"MTRetry"];
        retryTextField.tag = 1;
        retryTextField.returnKeyType = delayTextField.returnKeyType;
        retryTextField.clearButtonMode = delayTextField.clearButtonMode;
        
        UILabel *retryLabel = [[UILabel alloc] initWithFrame:CGRectMake(point2.x, point2.y, labelSize.width, labelSize.height)];
        retryLabel.font = delayLabel.font;
        retryLabel.text = @"Retry Attempts";
        retryLabel.textColor = delayLabel.textColor;
        retryLabel.shadowColor = delayLabel.shadowColor;
        retryLabel.shadowOffset = delayLabel.shadowOffset;
        retryLabel.backgroundColor = delayLabel.backgroundColor;
        
        delayTextField.delegate = self;
        retryTextField.delegate = self;
        
        [delayTextField addTarget:self action:@selector(textInput:) forControlEvents:UIControlEventEditingChanged];
        [retryTextField addTarget:self action:@selector(textInput:) forControlEvents:UIControlEventEditingChanged];
        
//        [self addSubview:delayLabel];
//        [self addSubview:retryLabel];
//        [self addSubview:delayTextField];
//        [self addSubview:retryTextField];
        
        
        
//        if ([[UIApplication sharedApplication] statusBarOrientation] == UIDeviceOrientationLandscapeLeft ||
//            [[UIApplication sharedApplication] statusBarOrientation] == UIDeviceOrientationLandscapeRight)
//            self.frame = CGRectMake(self.frame.origin.x, self.frame.origin.y, [[UIScreen mainScreen] bounds].size.height, [[UIScreen mainScreen] bounds].size.width-20);
//        
//        self.backgroundColor = [UIColor blackColor];
//        
//        UIToolbar *qToolBar = [[UIToolbar alloc] initWithFrame:CGRectMake(0, 0, self.frame.size.width, 44)];
//        qToolBar.tintColor = [UIColor colorWithRed:.498 green:0 blue:.0 alpha:1];
//        
//        UIBarButtonItem *fixedSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFixedSpace target:self action:nil];
//        fixedSpace.width = 62.0;
//        UIBarButtonItem *flexSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:self action:nil];
//        UIBarButtonItem *gorillaButton = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"gorilla-32"] style:UIBarButtonItemStylePlain target:self action:nil];
//        UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithTitle:@"Done" style:UIBarButtonItemStyleDone target:self action:@selector(hideView)];
//        
//        doneButton.title = @"Done";
//        
//        qToolBar.items = [NSArray arrayWithObjects:fixedSpace, flexSpace, gorillaButton, flexSpace, doneButton, nil];
//        
//        [self addSubview:qToolBar];
//        
//        [fixedSpace release];
//        [flexSpace release];
//        [gorillaButton release];
//        [doneButton release];
    }
    return self;
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{
    // Drawing code
}
*/

- (void) textInput:(id)sender
{
    UITextField *textField = (UITextField *)sender;
    
    if (textField.tag == 0)
        [MTUtils saveUserDefaults:textField.text forKey:@"MTDelay"];
    else
        [MTUtils saveUserDefaults:textField.text forKey:@"MTRetry"];
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
	[textField resignFirstResponder];
	return YES;
}


@end
