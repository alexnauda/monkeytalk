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

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "MonkeyTalk.h"
#import "MTEventViewCell.h"


@implementation MTEventViewCell
@synthesize commandNumber;

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event {
	if ([(UITouch*)[touches anyObject] tapCount] > 1 ){
		[[MonkeyTalk sharedMonkey] playFrom:commandNumber numberOfCommands:1];
	} 
//	else if (!selected) {
//		selected = YES;
//		self.backgroundColor = [UIColor blueColor];
//		self.textLabel.textColor = [UIColor whiteColor];
//	} else {
//		selected = NO;
//		self.backgroundColor = [UIColor whiteColor];
//		self.textLabel.textColor = [UIColor blackColor];		
//	}
	[super touchesEnded:touches withEvent:event];
}
@end
