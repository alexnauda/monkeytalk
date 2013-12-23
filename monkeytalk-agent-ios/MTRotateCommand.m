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

#import "MTRotateCommand.h"
#import "MonkeyTalk.h"
#import "MTUtils.h"
#import "NSString+MonkeyTalk.h"

@interface  MTRotateCommand() 
+ (UIDeviceOrientation) rotationOrientationFrom:(UIDeviceOrientation)startOrientation withDirection:(NSString *)direction;
+ (NSString *) rotateDirectionFrom:(UIDeviceOrientation)startOrientation to:(UIDeviceOrientation)endOrientation;
@end

@implementation MTRotateCommand

static NSString *leftArg = @"Left";
static NSString *rightArg = @"Right";

+ (void) rotate:(MTCommandEvent*)command {
    MonkeyTalk *monkey = [MonkeyTalk sharedMonkey];
    UIInterfaceOrientation orientation = monkey.currentOrientation;
    NSArray *availableActions = [NSArray arrayWithObjects:leftArg, rightArg, nil];
//    NSString *rotationDirection = nil;
    
    if ([command.args count] != 1) {
        command.lastResult = [NSString stringWithFormat:@"Requires 1 argument, but has %d", [command.args count]];
        return;
    } else if (![[command.args objectAtIndex:0] isEqualToString:leftArg] &&
               ![[command.args objectAtIndex:0] isEqualToString:rightArg]) {
        command.lastResult = [NSString stringWithFormat:@"%@ is not an available argument for Device Rotate — use %@ or %@", [command.args objectAtIndex:0], leftArg, rightArg];
        return;
    }
    
//	orientation = [((NSString*)[command.args objectAtIndex:0]) intValue];
    orientation = [[self class] rotationOrientationFrom:orientation withDirection:(NSString*)[command.args objectAtIndex:0]];
	
//    if (rotationDirection == nil)
//        return;
		
	[MTUtils rotate:orientation];
}

+ (void) recordRotation:(NSNotification *)notification {
    MonkeyTalk *monkey = [MonkeyTalk sharedMonkey];
    UIDeviceOrientation orientation = [[UIDevice currentDevice] orientation];
    if (orientation == 0) {
        return;
    }
    
    if (orientation == monkey.currentOrientation) {
        return;
    }
    
    NSString *rotationDirection = [[self class] rotateDirectionFrom:monkey.currentOrientation to:orientation];
    
    monkey.currentOrientation = orientation;
    
    if (rotationDirection == nil)
        return;
    
    //	[MonkeyTalk recordFrom:nil command:MTCommandRotate args:[NSArray arrayWithObject:[NSString stringWithFormat:@"%d", orientation]]];
    [MonkeyTalk recordFrom:nil command:MTCommandRotate args:[NSArray arrayWithObject:rotationDirection]];
}

+ (UIDeviceOrientation) rotationOrientationFrom:(UIDeviceOrientation)startOrientation withDirection:(NSString *)direction {
    UIDeviceOrientation endOrientation = startOrientation;
    BOOL isLeft = [direction isEqualToString:leftArg ignoreCase:YES];
    BOOL isRight = [direction isEqualToString:rightArg ignoreCase:YES];
    
    if (startOrientation == UIDeviceOrientationPortrait) {
        if (isLeft)
            endOrientation = UIDeviceOrientationLandscapeLeft;
        else if (isRight)
            endOrientation = UIDeviceOrientationLandscapeRight;
    } else if (startOrientation == UIDeviceOrientationPortraitUpsideDown) {
        if (isLeft)
            endOrientation = UIDeviceOrientationLandscapeRight;
        else if (isRight)
            endOrientation = UIDeviceOrientationLandscapeLeft;
    } else if (startOrientation == UIDeviceOrientationLandscapeRight) {
        if (isLeft)
            endOrientation = UIDeviceOrientationPortrait;
        else if (isRight)
            endOrientation = UIDeviceOrientationPortraitUpsideDown;
    } else if (startOrientation == UIDeviceOrientationLandscapeLeft) {
        if (isLeft)
            endOrientation = UIDeviceOrientationPortraitUpsideDown;
        else if (isRight)
            endOrientation = UIDeviceOrientationPortrait;
    }
    
    return endOrientation;
}

+ (NSString *) rotateDirectionFrom:(UIDeviceOrientation)startOrientation to:(UIDeviceOrientation)endOrientation {
    NSString *rotationDirection = nil;
    NSString *leftString = leftArg;
    NSString *rightString = rightArg;
    
    if (startOrientation == UIDeviceOrientationPortrait) {
        if (endOrientation == UIDeviceOrientationLandscapeLeft)
            rotationDirection = leftString;
        else if (endOrientation == UIDeviceOrientationLandscapeRight)
            rotationDirection = rightString;
    } else if (startOrientation == UIDeviceOrientationPortraitUpsideDown) {
        if (endOrientation == UIDeviceOrientationLandscapeRight)
            rotationDirection = leftString;
        else if (endOrientation == UIDeviceOrientationLandscapeLeft)
            rotationDirection = rightString;
    } else if (startOrientation == UIDeviceOrientationLandscapeRight) {
        if (endOrientation == UIDeviceOrientationPortrait)
            rotationDirection = leftString;
        else if (endOrientation == UIDeviceOrientationPortraitUpsideDown)
            rotationDirection = rightString;
    } else if (startOrientation == UIDeviceOrientationLandscapeLeft) {
        if (endOrientation == UIDeviceOrientationPortraitUpsideDown)
            rotationDirection = leftString;
        else if (endOrientation == UIDeviceOrientationPortrait)
            rotationDirection = rightString;
    }
    
    return rotationDirection;
}
@end
