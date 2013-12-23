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

#import <objc/runtime.h>

#import "MTUtils.h"
#import "UIView+MTReady.h"
#import <QuartzCore/QuartzCore.h>
#import "TouchSynthesis.h"
#import <objc/runtime.h>
#import "MTConsoleController.h"
#import "UIMotionEventProxy.h"
#import "MTWindow.h"
#import "MTGetVariableCommand.h"
#import "MonkeyTalk.h"
#import <dlfcn.h>
#import "MTOrdinalView.h"
#import "MTWebViewController.h"
#import "MTConvertType.h"
#import "NSString+MonkeyTalk.h"
#import "NSData+MTBase64.h"
#import "MTComponentTree.h"
#import "UIDevice+Hardware.h"

@interface MTUtils ()
+ (void)enableAccessibility:(CFBooleanRef)enabled;
@end

@implementation MTUtils
+ (void)load {
    [[self class] enableAccessibility:kCFBooleanTrue];
}

+ (UIWindow *)rootWindow {
    UIWindow *keyWindow = [[UIApplication sharedApplication] keyWindow];
    return keyWindow != nil ? keyWindow : [[[UIApplication sharedApplication] windows] objectAtIndex:0];
}

+ (UIView *)viewWithMonkeyID:(NSString *)mid startingFromView:(UIView *)current havingClass:(Class)class {
    return [self viewWithMonkeyID:mid startingFromView:current havingClass:class swapsOK:NO];
}

+ (UIView *)viewWithMonkeyID:(NSString *)mid startingFromView:(UIView *)current havingClass:(Class)class swapsOK:(BOOL)swapsOK {
    NSString *classString = NSStringFromClass(class);
    
    if (!current) {
        current =  [MTUtils rootWindow];
    }
    
    // we can prune the search here
    // of the view is hidden
    if (current.hidden == YES) {
        return nil;
    }
    
    // if the current view has no monkeyID
    // we can prune the search here
    if ([current monkeyID] == nil) {
        return nil;
    }
    
    BOOL match = false;
    if (mid == nil || [mid length] == 0 || [[current monkeyID] isEqualToString:mid]) {
        match = true; // monkeyID match
    } else {
        // search other candidates
        NSArray* candidates=[current rawMonkeyIDCandidates];
        for (int i=0; i<candidates.count; i++) {
            NSString* candidate = candidates[i];
            if (candidate && candidate.length>0 && [candidate isEqualToString:mid]) {
                match=true;
                break;
            }
        }
    }
    
    if (match) {
        if (class == nil) {
            return nil;
        }
        
        BOOL isButton = [[self class] shouldRecord:classString view:current];
        
        if ( (class != nil && ([current isKindOfClass:class] || isButton)) || (swapsOK && [current swapsWith:NSStringFromClass(class)]) ) {
            NSString *matchId = mid;
            NSDictionary *monkeyOrdinal = [MTCommandEvent monkeyOrdinalFromId:mid];
            NSInteger findMonkeyOrdinal = 1;
            
            if (monkeyOrdinal && [monkeyOrdinal respondsToSelector:@selector(objectForKey:)]) {
                findMonkeyOrdinal = [[monkeyOrdinal objectForKey:@"ordinal"] integerValue];
                matchId = [monkeyOrdinal objectForKey:@"mid"];
            }
            
            NSArray *components = [MTOrdinalView componentsArrayForClass:classString withMonkeyId:matchId];
            int index = findMonkeyOrdinal-1;
            
            if (components.count > index) {
                return [components objectAtIndex:index];
            }
        }
    }
    
    if (!current.subviews) {
        return nil;
    }
    
    for (UIView *view in current.subviews) {
        UIView *result;
        if ((result = [self viewWithMonkeyID:mid startingFromView:view havingClass:class swapsOK:swapsOK])) {
            return result;
        }
    }
    
    return nil;
}


+ (UIView *)viewWithCommandEvent:(MTCommandEvent *)event {
    NSString *mid = event.monkeyID;
    NSString *className = event.className;
    NSString *command = event.command;
    Class class = objc_getClass([className UTF8String]);
    
    // Check if the className is a MonkeyTalk component
    BOOL isMTComponent = [className isEqualToString:[MTConvertType convertedComponentFromString:className isRecording:YES]];
    
    if (!class && !isMTComponent) {
        if ([className length] && ![command isEqualToString:@"GetVariable"]) {
        }
    }
    
    // use [MTUtils rootWindow] instead of [[UIApplication sharedApplication] windows]
    // to find alert dialogs in ios7
    for (UIView *view in [MTUtils rootWindow].subviews) {
        if ([view isKindOfClass:[MTWindow class]]) {
            continue;
        }
        
        if ([mid rangeOfString:@"#"].location == 0) {
            NSInteger findViewInt = [[mid stringByReplacingOccurrencesOfString:@"#" withString:@""] intValue];
            UIView *first = [MTOrdinalView viewWithOrdinal:findViewInt startingFromView:view havingClass:className skipWebView:event.skipWebView];
            
            [MonkeyTalk sharedMonkey].foundComponents = nil;
            
            if (first != nil) {
                return first;
            }
        }
        // Check for wild card pattern "*" and "?" in MonkeyId - If found at index 0, it returns
        // first view of component type or else it looks for components that matches the wild card pattern.
        // WildCardMonkeyIDComponents holds list of components that matches monkeyids of WildCardPattern
        
        else if ([mid rangeOfString:@"*"].location != NSNotFound ||
                 [mid rangeOfString:@"?"].location != NSNotFound)
        {
            
            if ([mid rangeOfString:@"*"].location == 0 ||
                [mid rangeOfString:@"?"].location == 0) {
                
                UIView *first = [MTOrdinalView viewWithOrdinal:1 startingFromView:view havingClass:className skipWebView:event.skipWebView];
                [MonkeyTalk sharedMonkey].foundComponents = nil;
                
                if (first != nil) {
                    return first;
                }
            }
            else {
                // Logic for Regular expression monkey id goes here.
                // Try to build a finder based on MonkeyID
                // 1. Get the wild card expression string from buildWildCardRegularExpression
                // 2. Look for matched monkey ids and store it in WildCardMonkeyIDComponents
                // 3. Returns first of its view
                
                NSString *wildCardExpression = [self buildWildCardRegularExpression:mid];
                NSMutableArray *wildCardMonkeyIDComponents = [self componentsForWildCardMonkeyId:wildCardExpression className:event.className];
                
                if ([wildCardMonkeyIDComponents count] != 0) {
                    UIView *first = (UIView *)[wildCardMonkeyIDComponents objectAtIndex:0];
                    return first;
                }
            }
        }
        else {
            UIView *result = [self viewWithMonkeyID:mid startingFromView:view havingClass:class];
            BOOL foundItemIndexed = ([result isKindOfClass:[UIWindow class]] &&
                                     ([className isEqualToString:@"itemselector" ignoreCase:YES] ||
                                      [className isEqualToString:@"indexedselector" ignoreCase:YES]));
            
            if (result != nil && !foundItemIndexed) {
                if ([MonkeyTalk sharedMonkey].currentCommand != nil &&
                    [[MonkeyTalk sharedMonkey].currentCommand respondsToSelector:@selector(found)]) {
                    [MonkeyTalk sharedMonkey].currentCommand.found = YES;
                }
                
                return result;
            }
        }
    }
    
    return nil;
}
// Build components that matches with wildcard monkeyids.
// Returns list of array that contains matched monkeyid components

+(NSMutableArray *) componentsForWildCardMonkeyId:(NSString *)wildCardId className:(NSString *)className{
    NSError *error = nil;
    NSMutableArray *monkeyIDComponents = [[NSMutableArray alloc] init];
    
    [MTOrdinalView buildFoundComponentsStartingFromView:nil havingClass:className isOrdinalMid:NO skipWebView:NO];
    NSArray *components = [[NSArray alloc] initWithArray:[MonkeyTalk sharedMonkey].foundComponents];
    NSRegularExpression *regex = [[NSRegularExpression alloc] initWithPattern:wildCardId
                                                                      options:0
                                                                        error:&error];
    for (UIView *temp in components) {
        NSString *mid = temp.monkeyID;
        NSTextCheckingResult *b = [regex firstMatchInString:mid
                                                    options:NSRegularExpressionDotMatchesLineSeparators
                                                      range:NSMakeRange(0, [mid length])];
        NSString *foundComponent = [mid substringWithRange:b.range];
        if ([foundComponent isEqualToString:mid]) {
            [monkeyIDComponents addObject:temp];
        }
    }
    [MonkeyTalk sharedMonkey].foundComponents = nil;
    return monkeyIDComponents;
}

// MT: No Longer In Use
+ (UIView *)viewWithMonkeyID:(NSString *)mid havingClass:(NSString *)className withCommand:(NSString *)command {
    Class class = objc_getClass([className UTF8String]);
    
    // Check if the className is a MonkeyTalk component
    BOOL isMTComponent = [className isEqualToString:[MTConvertType convertedComponentFromString:className isRecording:YES]];
    
    if (!class && !isMTComponent) {
        if ([className length] && ![command isEqualToString:@"GetVariable"]) {
            // NSLog(@"Warning: %@ is not a valid classname.", className);
        }
    }
    for (UIView *view in [[UIApplication sharedApplication] windows]) {
        if ([view isKindOfClass:[MTWindow class]]) {
            continue;
        }
        
        UIView *result = [self viewWithMonkeyID:mid startingFromView:view havingClass:class];
        BOOL foundItemIndexed = ([result isKindOfClass:[UIWindow class]] &&
                                 ([className isEqualToString:@"itemselector" ignoreCase:YES] ||
                                  [className isEqualToString:@"indexedselector" ignoreCase:YES]));
        if (result != nil && !foundItemIndexed) {
            return result;
        }
        
        if ([mid rangeOfString:@"#"].location != NSNotFound) {
            NSInteger findViewInt = [[mid stringByReplacingOccurrencesOfString:@"#" withString:@""] intValue];
            UIView *first = [MTOrdinalView viewWithOrdinal:findViewInt startingFromView:view havingClass:className skipWebView:NO];
            
            [MonkeyTalk sharedMonkey].foundComponents = nil;
            
            if (first != nil) {
                return first;
            }
        }
    }
    
    return nil;
}

static NSInteger foundSoFar;
NSMutableArray *foundArray;
+ (NSInteger)ordinalForView:(UIView *)view startingFromView:(UIView *)current {
    if (!current) {
        current =  [MTUtils rootWindow];
    }
    
    if ([current isMemberOfClass:[view class]]) {
        if (current == view) {
            return foundSoFar;
        }
        
        foundSoFar++;
    }
    
    if (!current.subviews) {
        return -1;
    }
    
    for (UIView *kid in current.subviews) {
        NSInteger result;
        if ((result = [self ordinalForView:view startingFromView:kid]) > -1) {
            return result;
        }
    }
    
    
    
    return -1;
}

// Not Re-entrant!
+ (NSInteger)ordinalForView:(UIView *)view {
    foundSoFar = 1;
    return [self ordinalForView:view startingFromView:nil];
}

+ (UIView *)findFirstMonkeyView:(UIView *)current {
    if (current == nil) {
        return nil;
    }
    
    if ([current isMTEnabled]) {
        return current;
    }
    
    return [MTUtils findFirstMonkeyView:[current superview]];
}

+ (NSString *)scriptPathForFilename:(NSString *)fileName {
    NSString *documentsDirectory = [MTUtils scriptsLocation];
    if (!documentsDirectory) {
        NSLog(@"Documents directory not found!");
        return nil;
    }
    return [documentsDirectory stringByAppendingPathComponent:fileName];
}

+ (BOOL)writeString:(NSString *)string toFile:(NSString *)fileName {
    NSString *path = [self scriptPathForFilename:fileName];
    if (!path) {
        return NO;
    }

    NSError *error;
    if (string != nil) {
        BOOL result = [string writeToFile:path atomically:YES encoding:NSUTF8StringEncoding error:&error];
        if (!result) {
            NSLog(@"Error writing to file %@: %@", fileName, [error localizedFailureReason]);
        }
        return result;
    }
    return NO;
}

+ (BOOL)writeApplicationData:(NSData *)data toFile:(NSString *)fileName {
    NSString *path = [self scriptPathForFilename:fileName];
    if (!path) {
        return NO;
    }
    return ([data writeToFile:path atomically:YES]);
}

+ (NSString *)appDirectory {
    NSString *appDirectory = [MTUtils scriptsLocation];
    
    if (!appDirectory) {
        NSLog(@"App directory not found!");
    }
    
    // Remove Documents from path
    appDirectory = [appDirectory stringByReplacingOccurrencesOfString:@"/Documents" withString:@""];
    
    return appDirectory;
}

+ (NSString *)documentsLocation {
    NSString *documentsDirectory = [MTUtils scriptsLocation];
    if (!documentsDirectory) {
        NSLog(@"Documents directory not found!");
    }
    return documentsDirectory;
}

+ (NSData *)applicationDataFromFile:(NSString *)fileName {
    NSString *documentsDirectory = [MTUtils scriptsLocation];
    NSString *appFile = [documentsDirectory stringByAppendingPathComponent:fileName];
    NSLog(@"Reading %@", appFile);
    NSData *myData = [[NSData alloc] initWithContentsOfFile:appFile];
    return myData;
}

+ (void)slideIn:(UIView *)view {
    CATransition *transition = [CATransition animation];
    transition.duration = 0.5;
    transition.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseInEaseOut];
    transition.type =  kCATransitionPush;
    transition.subtype  = kCATransitionFromBottom;
    // transition.delegate = self;
    [view.layer addAnimation:transition forKey:nil];
    view.alpha = 1.0;
    [[MTUtils rootWindow] bringSubviewToFront:view];
}

+ (void)slideOut:(UIView *)view {
    CATransition *transition = [CATransition animation];
    transition.duration = 0.5;
    transition.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseInEaseOut];
    transition.type =  kCATransitionPush;
    transition.subtype  = kCATransitionFromTop;
    //transition.delegate = self;
    [view.layer addAnimation:transition forKey:nil];
    view.alpha = 0;
    [[MTUtils rootWindow] bringSubviewToFront:view];
}

+ (void)navRight:(UIView *)view from:(UIView *)from {
    CATransition *transition = [CATransition animation];
    transition.duration = .25;
    transition.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionLinear];
    transition.type =  kCATransitionPush;
    transition.subtype  = kCATransitionFromRight;
    // transition.delegate = self;
    [view.layer addAnimation:transition forKey:nil];
    view.alpha = 1.0;
    //	[from.layer addAnimation:transition  forKey:nil];
    //	from.alpha = 0.0;
    [[MTUtils rootWindow] bringSubviewToFront:view];
}

+ (void)navLeft:(UIView *)view to:(UIView *)to {
    CATransition *transition = [CATransition animation];
    transition.duration = .25;
    transition.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionLinear];
    transition.type =  kCATransitionPush;
    transition.subtype  = kCATransitionFromLeft;
    // transition.delegate = self;
    [view.layer addAnimation:transition forKey:nil];
    view.alpha = 0.0;
    //	[to.layer addAnimation:transition  forKey:nil];
    //	to.alpha = 1.0;
    //	[[MTUtils rootWindow] bringSubviewToFront:view];
}

+ (void)dismissKeyboard {
    UIWindow *keyWindow = (UIWindow *)[MTConsoleController sharedInstance].view;
    UIView *firstResponder = [keyWindow performSelector:@selector(firstResponder)];
    [firstResponder resignFirstResponder];
}

+ (NSString *)scriptsLocation {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *loc = [paths objectAtIndex:0];
    //loc = [loc stringByAppendingPathComponent:@"monkeytalk"];
    // NSLog(@"Scripts location: %@", loc);
    return loc;
}

+ (void)shake {
    // Although such legal wrangling is not necessary in the simulator, it is necessary to prevent dynamic linker errors on the actual iPhone
    UIMotionEventProxy *m = [[NSClassFromString(@"UIMotionEvent") alloc] _init];
    
    m->_subtype = UIEventSubtypeMotionShake;
    m->_shakeState = 1;
    //[[UIApplication sharedApplication] sendEvent:m]; // Works in simulator but not on device
    [[[UIApplication sharedApplication] keyWindow] motionBegan:UIEventSubtypeMotionShake withEvent:m];
    [[[UIApplication sharedApplication] keyWindow] motionEnded:UIEventSubtypeMotionShake withEvent:m];
}

+ (void)rotate:(UIInterfaceOrientation)orientation {
    [[UIDevice currentDevice] setOrientation:orientation];
}

+ (BOOL)isKeyboard:(UIView *)view {
    // It should go without saying that this is a hack
    Class cls = view.window ? [view.window class] : [view class];
    return [[NSString stringWithUTF8String:class_getName(cls)] isEqualToString:@"UITextEffectsWindow"];
}

+ (NSString *)stringByJsEscapingQuotesAndNewlines:(NSString *)unescapedString {
    return [MTUtils stringByOcEscapingQuotesAndNewlines:unescapedString];
}

+ (NSString *)stringByOcEscapingQuotesAndNewlines:(NSString *)unescapedString {
    NSString *escapedString = [unescapedString stringByReplacingOccurrencesOfString:@"\"" withString:@"\\\""];
    escapedString = [escapedString stringByReplacingOccurrencesOfString:@"\n" withString:@"\\n"];
    return escapedString;
}

+ (NSString *)stringForQunitTest:(NSString *)testCase inModule:(NSString *)module withResult:(NSString *)resultMessage {
    NSString *xmlString = nil;
    
    if ([resultMessage rangeOfString:@"Test Successful"].location == NSNotFound) { // If result of testCase failed add failure detail to xml
        resultMessage = [resultMessage stringByReplacingOccurrencesOfString:@"\\" withString:@""];
        xmlString = [NSString stringWithFormat:@"<testcase name=\"-[%@ %@]\" modulename=\"%@\"><failure>%@</failure></testcase>", module, testCase, module, resultMessage];
    } else {
        xmlString = [NSString stringWithFormat:@"<testcase name=\"-[%@ %@]\" modulename=\"%@\"/>", module, testCase, module];
    }
    
    return xmlString;
}

+ (void)writeQunitToXmlWithString:(NSString *)xmlString testCount:(NSString *)testCount failCount:(NSString *)failCount runTime:(NSString *)runTime fileName:(NSString *)fileName {
    NSString *testSuite = [NSString stringWithFormat:@"<?xml version=\"1.0\"?><testsuite tests=\"%@\" failures=\"%@\" time=\"%@\">", testCount, failCount, runTime];
    NSString *finalXmlString = [xmlString stringByReplacingOccurrencesOfString:@"<?xml version=\"1.0\"?>" withString:testSuite];
    
    finalXmlString = [finalXmlString stringByAppendingString:@"</testsuite>"];
    
    if (getenv("MT_ENABLE_XML_REPORT") && strcmp(getenv("MT_ENABLE_XML_REPORT"), "NO") && getenv("MT_ENABLE_QUNIT")) {
        [self writeString:finalXmlString toFile:fileName];
    }
}

- (BOOL)recordMonkeyTouchesYes:(UITouch *)touch {
    return YES;
}

- (BOOL)recordMonkeyTouchesNo:(UITouch *)touch {
    return NO;
}

+ (void)setShouldRecordMonkeyTouch:(BOOL)shouldRecord forView:(UIView *)view {
    Class viewClass = [view class];
    Method currentMethod = class_getInstanceMethod(viewClass, @selector(shouldRecordMonkeyTouch:));
    Method replaceMethod = class_getInstanceMethod([MTUtils class],
                                                   shouldRecord ? @selector(recordMonkeyTouchesYes:)
                                                   : @selector(recordMonkeyTouchesNo:));
    method_setImplementation(currentMethod, method_getImplementation(replaceMethod));
}

+ (NSString *)className:(NSObject *)ob {
    return [NSString stringWithUTF8String:class_getName([ob class])];
}

+ (NSString *)screenshotFileName:(MTCommandEvent *)command {
    NSString *fileName = [NSString stringWithFormat:@"MT_SCREENSHOT-%@-%@-%@-%@.png",
                          [MTUtils timeStamp],
                          command.command,
                          command.className,
                          command.monkeyID
                          ];
    
    return fileName;
}

+ (void)saveScreenshot:(NSString *)filename {
    //    UIImageWriteToSavedPhotosAlbum (
    //                                    [MTUtils screenshot], nil, nil, nil
    //);
    [MTUtils writeApplicationData:UIImagePNGRepresentation([MTUtils screenshot])  toFile:filename];
}

+ (UIImage *)screenshot {
    extern CGImageRef UIGetScreenImage(void);
    CGImageRef imgRef = UIGetScreenImage();
    UIImage *image = [UIImage imageWithCGImage:imgRef];
    CFRelease(imgRef);
    
    return image;
}

+ (NSString *)encodedScreenshot {
    UIImage *image = [[self class] screenshot];
    NSData *imageData = UIImagePNGRepresentation(image);
    NSString *encodedImageString = [imageData MTbase64EncodedString];
    
    return encodedImageString;
}

+ (void)interfaceChanged:(UIView *)view {
    UIInterfaceOrientation interfaceOrientation = [[UIApplication sharedApplication] statusBarOrientation];
    CGRect portraitFrame = CGRectMake(0, 0, [[UIScreen mainScreen] bounds].size.width, [[UIScreen mainScreen] bounds].size.height);
    CGRect landscapeFrame = CGRectMake(0, 0, [[UIScreen mainScreen] bounds].size.height, [[UIScreen mainScreen] bounds].size.width);
    
    // Rotate and scale interface based on status bar orientation
    if (interfaceOrientation == UIDeviceOrientationPortrait) {
        [self rotateInterface:view degrees:0 frame:portraitFrame];
    } else if (interfaceOrientation == UIDeviceOrientationPortraitUpsideDown) {
        [self rotateInterface:view degrees:180 frame:portraitFrame];
    } else if (interfaceOrientation == UIDeviceOrientationLandscapeLeft) {
        [self rotateInterface:view degrees:90 frame:landscapeFrame];
    } else {
        [self rotateInterface:view degrees:-90 frame:landscapeFrame];
    }
}

+ (void)rotateInterface:(UIView *)view degrees:(NSInteger)degrees frame:(CGRect)frame {
    //[UIView beginAnimations:nil context:nil];
    //[UIView setAnimationDuration:[[UIApplication sharedApplication] statusBarOrientationAnimationDuration]];
    //[UIView setAnimationTransition:UIViewAnimationTransitionNone forView:view cache:YES];
    //[UIView setAnimationDelegate:view];
    view.transform = CGAffineTransformIdentity;
    view.transform = CGAffineTransformMakeRotation(degrees * M_PI / 180);
    view.bounds = frame;
    //[UIView commitAnimations];
}

+ (NSString *)timeStamp {
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"MM-dd-HH-mm-ss-SSS"];
    NSString *dateString = [dateFormatter stringFromDate:[NSDate date]];
    return dateString;
}

+ (NSArray *)replaceArgForCommand:(NSArray *)commandArgs variables:(NSDictionary *)jsVariable {
    if ([[commandArgs componentsJoinedByString:@","] rangeOfString:@"${"].location != NSNotFound) {
        NSMutableArray *mutableArgs = [NSMutableArray arrayWithArray:commandArgs];
        for (int i = 0; i < [commandArgs count]; i++) {
            NSString *argString = (NSString *)[commandArgs objectAtIndex:i];
            argString = [[self class] replaceString:argString variables:jsVariable];
            [mutableArgs replaceObjectAtIndex:i withObject:argString];
            commandArgs = mutableArgs;
        }
    }
    
    return commandArgs;
}

+ (NSString *)replaceMonkeyIdForCommand:(NSString *)monkeyId variables:(NSDictionary *)jsVariable {
    monkeyId = [[self class] replaceString:monkeyId variables:jsVariable];
    
    return monkeyId;
}

+ (NSString *)replaceString:(NSString *)origString variables:(NSDictionary *)jsVariable {
    NSString *newString = origString;
    if ([origString rangeOfString:@"${"].location != NSNotFound && [origString rangeOfString:@"}"].location != NSNotFound) {
        NSUInteger count = 0, length = [origString length];
        NSRange range = NSMakeRange(0, length);
        NSRange endRange = NSMakeRange(0, length);
        while (range.location != NSNotFound) {
            range = [origString rangeOfString:@"${" options:0 range:range];
            endRange = [origString rangeOfString:@"}" options:0 range:endRange];
            
            if (range.location <= [origString length]) {
                NSString *replaceString = [origString substringWithRange:NSMakeRange(range.location, endRange.location - range.location + 1)];
                
                if ([jsVariable objectForKey:replaceString]) {
                    newString = [newString stringByReplacingOccurrencesOfString:replaceString withString:[jsVariable objectForKey:replaceString]];
                    NSLog(@"Replacing %@ with %@", origString, newString);
                }
            }
            
            if (range.location != NSNotFound) {
                range = NSMakeRange(range.location + range.length, length - (range.location + range.length));
                endRange = NSMakeRange(endRange.location + endRange.length, length - (endRange.location + endRange.length));
                count++;
            }
        }
    }
    
    return newString;
}

+ (NSMutableDictionary *)addVarsFrom:(MTCommandEvent *)event to:(NSMutableDictionary *)dict {
    NSString *variable = [NSString stringWithFormat:@"${%@}", [event.args objectAtIndex:1]];
    NSString *value = [MTGetVariableCommand execute:event];
    
    [dict setValue:value forKey:variable];
    
    return dict;
}

+ (void)saveUserDefaults:(NSString *)value forKey:(NSString *)key {
    NSUserDefaults *standardUserDefaults = [NSUserDefaults standardUserDefaults];
    
    if (standardUserDefaults) {
        [standardUserDefaults setObject:value forKey:key];
        [standardUserDefaults synchronize];
    }
}

+ (NSString *)retrieveUserDefaultsForKey:(NSString *)key {
    NSUserDefaults *standardUserDefaults = [NSUserDefaults standardUserDefaults];
    NSString *value = nil;
    
    if (standardUserDefaults) {
        value = [standardUserDefaults objectForKey:key];
    }
    
    return value;
}

+ (double)playbackSpeedForCommand:(MTCommandEvent *)nextCommandToRun isLive:(BOOL)isLive retryCount:(int)retryCount {
    if ([nextCommandToRun.playbackDelay length] == 0) {
        nextCommandToRun.playbackDelay = [NSString stringWithFormat:@"%i", MT_DEFAULT_THINKTIME];
    }
    
    double commandPlaybackSpeed = 0;
    double perCommandPlayback = [nextCommandToRun.playbackDelay doubleValue] * 1000;
    double globalLivePercent = [[MTUtils retrieveUserDefaultsForKey:MTOptionsLive] doubleValue] / 100;
    double globalCommandPlayback = [[MTUtils retrieveUserDefaultsForKey:MTOptionsFixed] doubleValue] * 1000;
    
    if (retryCount > 0) {
        commandPlaybackSpeed = 0;
    } else if (isLive) {
        if ([[MTUtils retrieveUserDefaultsForKey:MTOptionsLive] length] > 0) {
            commandPlaybackSpeed = perCommandPlayback * globalLivePercent;
        } else {
            commandPlaybackSpeed = perCommandPlayback;
        }
    } else
        if ([[MTUtils retrieveUserDefaultsForKey:MTOptionsFixed] length] > 0) {
            commandPlaybackSpeed = globalCommandPlayback;
        } else {
            commandPlaybackSpeed = MT_DEFAULT_THINKTIME * 1000;
        }
    
    return commandPlaybackSpeed;
}

+ (double)timeoutForCommand:(MTCommandEvent *)nextCommandToRun {
    int commandTimeout = 0;
    
    double perCommandTimeout = [nextCommandToRun.playbackTimeout intValue];
    double globalCommandTimeout = [[MTUtils retrieveUserDefaultsForKey:MTOptionsTimeout] intValue];
    
    if (globalCommandTimeout > 0) {
        commandTimeout = globalCommandTimeout;
    } else if ([nextCommandToRun.playbackDelay length] > 0) {
        commandTimeout = perCommandTimeout;
    } else {
        commandTimeout = MT_DEFAULT_TIMEOUT;
    }
    
    return commandTimeout;
}

+ (BOOL)isOs5Up {
    NSString *osVersion = [[UIDevice currentDevice] systemVersion];
    if ([[osVersion substringToIndex:1] intValue] >= 5) {
        return YES;
    }
    
    return NO;
}

+ (UIWebView *)currentWebView {
    return [MTOrdinalView currentWebView];
}

+ (BOOL)shouldRecord:(NSString *)classString view:(UIView *)current {
    //    NSLog(@"FindClass: %@",classString);
    
    NSString *currentString = NSStringFromClass([current class]);
    
    BOOL isButton = (([current isKindOfClass:objc_getClass("UIButton")] ||
                      [current isKindOfClass:objc_getClass("UINavigationButton")] ||
                      [current isKindOfClass:objc_getClass("UINavigationItemButtonView")] ||
                      [current isKindOfClass:objc_getClass("UIThreePartButton")] ||
                      [current isKindOfClass:objc_getClass("UIRoundedRectButton")] ||
                      [current isKindOfClass:objc_getClass("UIToolbarTextButton")]) &&
                     ([currentString isEqualToString:@"UIButton"] ||
                      [currentString isEqualToString:@"UINavigationButton"] ||
                      [currentString isEqualToString:@"UINavigationItemButtonView"] ||
                      [currentString isEqualToString:@"UIThreePartButton"] ||
                      [currentString isEqualToString:@"UIRoundedRectButton"] ||
                      [currentString isEqualToString:@"UIToolbarTextButton"]));
    
    isButton = isButton && [classString isEqualToString:@"UIButton"];
    
    BOOL isIndexedSelector = (([classString isEqualToString:@"indexedselector" ignoreCase:YES]) &&
                              ([current isKindOfClass:objc_getClass("UIToolBar")] ||
                               [current isKindOfClass:objc_getClass("UITabBar")] ||
                               [current isKindOfClass:objc_getClass("UITableView")]));
    
    BOOL isItemSelector = (([classString isEqualToString:@"itemselector" ignoreCase:YES]) &&
                           ([current isKindOfClass:objc_getClass("UITabBar")] ||
                            [current isKindOfClass:objc_getClass("UITableView")]));
    
    return isButton || isIndexedSelector || isItemSelector;
}

+ (void)enableAccessibility:(CFBooleanRef)enabled {
    @autoreleasepool {
        NSString *appSupportLocation = @"/System/Library/PrivateFrameworks/AppSupport.framework/AppSupport";
        
        NSDictionary *environment = [[NSProcessInfo processInfo] environment];
        NSString *simulatorRoot = [environment objectForKey:@"IPHONE_SIMULATOR_ROOT"];
        if (simulatorRoot) {
            appSupportLocation = [simulatorRoot stringByAppendingString:appSupportLocation];
        }
        
        void *appSupportLibrary = dlopen([appSupportLocation fileSystemRepresentation], RTLD_LAZY);
        
        CFStringRef (*copySharedResourcesPreferencesDomainForDomain)(CFStringRef domain) = dlsym(appSupportLibrary, "CPCopySharedResourcesPreferencesDomainForDomain");
        
        if (copySharedResourcesPreferencesDomainForDomain) {
            CFStringRef accessibilityDomain = copySharedResourcesPreferencesDomainForDomain(CFSTR("com.apple.Accessibility"));
            
            if (accessibilityDomain) {
                CFPreferencesSetValue(CFSTR("ApplicationAccessibilityEnabled"), enabled, accessibilityDomain, kCFPreferencesAnyUser, kCFPreferencesAnyHost);
                CFRelease(accessibilityDomain);
            }
        }
    
    }
}

// Method used to build a regular expression string for wild card pattern
// 1. Iterate through a character in a string and
// 2. if a character is not a wildcard, prefix and suffix it with \\Q and \\E and append it to a string
// 3. else append it to string
// 4. Replace all "*"'s with ".*"
// 5. Replace all "?" with "."

+ (NSString *) buildWildCardRegularExpression:(NSString *)wildCardPattern{
    
    NSCharacterSet *wildCardSet = [NSCharacterSet characterSetWithCharactersInString:@"*?"];
    
    NSString *buildWildCardRegEx = [[NSString alloc] init];
    
    for (int i=0; i<[wildCardPattern length]; i++) {
        unichar c = [wildCardPattern characterAtIndex:i];
        if ([wildCardSet characterIsMember:c] ) {
            buildWildCardRegEx = [buildWildCardRegEx stringByAppendingString:[NSString stringWithCharacters:&c  length:1]];
        }
        else {
            buildWildCardRegEx = [[[buildWildCardRegEx stringByAppendingString:@"\\Q"] stringByAppendingString:[NSString stringWithCharacters:&c length:1]] stringByAppendingString:@"\\E"];
        }
    }
    buildWildCardRegEx = [buildWildCardRegEx stringByReplacingOccurrencesOfString:@"*" withString:@".*"];
    buildWildCardRegEx = [buildWildCardRegEx stringByReplacingOccurrencesOfString:@"?" withString:@"."];
    
    return buildWildCardRegEx;
}

// Method returns matched string for a given regex
+ (NSString *) getMatchedStringForRegEx:(NSRegularExpression *) regex inString:(NSString *) inString {
    NSTextCheckingResult *b = [regex firstMatchInString:inString
                                                options:NSRegularExpressionDotMatchesLineSeparators range:NSMakeRange(0, [inString length])];
    NSString *foundRegex = [inString substringWithRange:b.range];
    
    return foundRegex;
    
}


@end
