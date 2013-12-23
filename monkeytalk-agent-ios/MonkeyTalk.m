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

#import <unistd.h>
#import <Foundation/Foundation.h>
#import "MonkeyTalk.h"
#import "MTCommandEvent.h"
#import "UIView+MTReady.h"
#import <UIKit/UIView.h>
#import "UIView+FullDescription.h"
#import "MTUtils.h"
#import <objc/runtime.h>
#import "MTCommandEvent.h"
#import "MTConsoleController.h"
#import "MTParseCsv.h"
#import <QuartzCore/QuartzCore.h>
#import "MTWebViewController.h"
#import "MTHTTPServerController.h"
#import "MTWireSender.h"
#import "MTConvertType.h"
#import "MTRotateCommand.h"
#import "NSString+MonkeyTalk.h"
#import "MTComponentTree.h"
#import "MTDevice.h"
#import "UIApplication+MTReady.h"
#import "MTOrdinalView.h"
#import "MTWireKeys.h"
#import "MTBuildStamp.h"

// Pause 1/2 sec between commands. This needs to be a setting!
#define THINK_TIME 500000
//#define UIAUTOMATION_PATH @"uiautomation"
#define UIAUTOMATION_PATH @""
//#define OCUNIT_PATH @"ocunit"
#define OCUNIT_PATH @""
//#define QUNIT_PATH @"qunit"
#define QUNIT_PATH @""

@implementation MonkeyTalk
static MonkeyTalk* _sharedMonkey = nil;

char* _enableScreenshot = nil;

MTCommandEvent* lastCommandPosted;
BOOL _lastCommandRecorded = YES;
MTCommandEvent* nextCommandToRun;

@synthesize commandQueue, recordOperationQueue;
@synthesize commands, runTimeout, state, session, csvData, previousTime, commandSpeed, isWireRecording, recordHost, recordPort, foundComponents, monkeyComponents, currentOrientation, isAnimating, currentCommand, componentMonkeyIds, currentTapCommand;

NSMutableDictionary* _monkeyIDs;

MTConsoleController* _console;
//UIDeviceOrientation _currentOrientation;

NSArray* emptyArray;

+(MonkeyTalk*)sharedMonkey
{
	@synchronized([MonkeyTalk class])
	{
		if (!_sharedMonkey) {
			// Weird objective-c singleton code idiom. The alloc/init creates the singleton instance and resets self so that rest of method refers to instance variables (not static) variables.
			[[self alloc] init];
			// After executing the above alloc/init, we are no longer in a static method. We are now in the singleton instance!
			_monkeyIDs = [[NSMutableDictionary alloc] init];
            
			
			if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone)
			{
                NSLog(@"Running on iPhone");
				// load the content controller object for Phone-based devices
                //				_console = [[MTConsoleController alloc] initWithNibName:@"MTConsoleController_iPhone" bundle:nil];
                _console = [[MTConsoleController alloc] init];
			}
			else
			{
                NSLog(@"Running on iPad");
				// load the content controller object for Pad-based devices
                //				_console = [[MTConsoleController alloc] initWithNibName:@"MTConsoleController_iPad" bundle:nil];
                _console = [[MTConsoleController alloc] init];
			}
			NSLog(@"Documents folder located at: %@",[MTUtils scriptsLocation]);
            
            // If we're running headless we might have to force creation of Documents directory
            [[NSFileManager defaultManager] createDirectoryAtPath:[MTUtils scriptsLocation]
                                      withIntermediateDirectories:YES
                                                       attributes:nil error:nil];
			emptyArray = [[NSArray alloc] init];
		}
		return _sharedMonkey;
	}
	
	return nil;
}


+(id)alloc
{
	@synchronized([MonkeyTalk class])
	{
		NSAssert(_sharedMonkey == nil, @"Attempted to allocate a second instance of MonkeyTalk.");
		NSLog(STARTUP_MESSAGE);
		_sharedMonkey = [super alloc];
		_sharedMonkey.session = [NSMutableDictionary dictionary];
		return _sharedMonkey;
	}
	
	return nil;
}


// Part of code to handle orientation events.
// This method is called by NSNotificationCenter when the device is rotated.
-(void) receivedRotate: (NSNotification*) notification
{
	UIWindow* _appWindow;
	UIDeviceOrientation interfaceOrientation = [[UIDevice currentDevice] orientation];
	
	if ((interfaceOrientation = UIDeviceOrientationLandscapeLeft) ||
		(interfaceOrientation = UIDeviceOrientationLandscapeRight)) {
		/*_landscape = YES;
         [_console hideConsole];
         _console = _console_landscape;
         [_console showConsole];
		 */
		// Rotates the view.
		CGAffineTransform transform = CGAffineTransformMakeRotation(-3.14159/2);
		_console.view.transform = transform;
		// Repositions and resizes the view.
		_appWindow = [MTUtils rootWindow];
		
		
		CGRect contentRect = [_appWindow bounds];
		if (contentRect.size.height > contentRect.size.width) {
			CGFloat temp = contentRect.size.height;
			contentRect.size.height = contentRect.size.width;
			contentRect.size.width = temp;
		}
		_console.view.bounds = contentRect;
		
	} else {
		CGAffineTransform transform = CGAffineTransformMakeRotation(3.14159/2);
		_console.view.transform = transform;
		// Repositions and resizes the view.
		_appWindow = [MTUtils rootWindow];
		
		
		CGRect contentRect = [_appWindow bounds];
		if (contentRect.size.height < contentRect.size.width) {
			CGFloat temp = contentRect.size.height;
			contentRect.size.height = contentRect.size.width;
			contentRect.size.width = temp;
		}
		_console.view.bounds = contentRect;
	}
	
}

- (NSString *) liveDelay
{
    NSDate *currentTime = [NSDate date];
    if (!previousTime) {
        previousTime = [currentTime copy];
        return [NSString stringWithFormat:@"%i",MT_DEFAULT_THINKTIME];
    }
    
    NSTimeInterval delay = [currentTime timeIntervalSinceDate:previousTime];
    delay = delay * 1000;
    
    previousTime = [currentTime copy];
    
    return [NSString stringWithFormat:@"%g",delay];
}

+ (void) sendRecordEvent:(MTCommandEvent *)event {
    [MTWireSender sendRecordEvent:event];
}

- (void) recordEvent:(MTCommandEvent*)event {
    UIView *view = event.source;
    [self recordEvent:event withSource:view];
}

- (void) recordEvent:(MTCommandEvent*)event withSource:(UIView *)view {
	if (state != MTStateRecording && !isWireRecording) {
		return;
	}
    
    // Ignore web components that are not set as recording
    if (!event.isWebRecording && (!view ||
                                  [view isKindOfClass:objc_getClass("UITextEffectsWindow")] ||
                                  [view isKindOfClass:objc_getClass("UITextEffectsWindow")])) {
        return;
    }
	
    // for iOS "raw" monkeyIDs are used to override what comes in on the event
    if (![view isKindOfClass:objc_getClass("UITabBarButton")]) {
        NSArray* candidates=[view rawMonkeyIDCandidates];
        for (int cndx=0; cndx<candidates.count; cndx++) {
            NSString* candidate = candidates[cndx];
            if (candidate!=nil && [candidate length]>0) {
                event.monkeyID = candidate;
                break;
            }
        }
    }
    
    NSInteger ordinal = [MTOrdinalView componentOrdinalForView:view withMonkeyID:view.baseMonkeyID];
    if ([self shouldAppendOrdinal:ordinal ToMonkeyID:event.monkeyID]) {
        event.monkeyID = [event.monkeyID stringByAppendingFormat:@"(%i)",ordinal];
    }
    
    // Record Device as classname for shake/rotate
    if ([event.command isEqualToString:MTCommandShake] ||
        [event.command isEqualToString:MTCommandRotate]) {
        event.className = MTComponentDevice;
    }
    
    event.playbackDelay = [self liveDelay];
    event.playbackTimeout = [NSString stringWithFormat:@"%i",MT_DEFAULT_TIMEOUT];
	
	//NSLog(@"\n\n< < < RECORDING > > > - source:%@\n%@\n%@ %@ %@\n\n",event.source, event.source.fullDescription, event.command, [event.source monkeyID], [event.args count] > 0 ? [event.args objectAtIndex:0] : @"");
	//NSLog(@"\n\n< < < RECORDING > > > - source:%@\n%@ %@ %@\n\n",[event.source class], event.command, [event.source monkeyID], event.args);
    //	NSLog(@"\n\n< < < RECORDING > > > - source:%@\n%@ %@ %@\n\n",[[event source] class], [event command], event.monkeyID, [event args]);
    
    NSString *recordString = [NSString stringWithFormat:@"\n\n< < < RECORDING > > > - source:%@\n%@ %@",event.className, [event command], event.monkeyID];
    
    for (NSString *arg in [event args])
        recordString = [recordString stringByAppendingFormat:@" %@\n",[NSString stringWithUTF8String:[arg UTF8String]]];
    
    //Added conditional statement to avoid sending empty command to IDE
    if ([event.command length] >0) {
        if (isWireRecording) {
            NSLog(@"%@\n",recordString);
            [MTWireSender sendRecordEvent:event];
        }  else {
            [commands addObject:[NSMutableDictionary dictionaryWithDictionary:event.dict]];
        }
    }
}

- (BOOL) shouldAppendOrdinal:(NSInteger)ordinal ToMonkeyID:(NSString*)monkeyID {
    NSRegularExpression *regEx =
    [NSRegularExpression regularExpressionWithPattern:[NSString stringWithFormat:@"(%i)",ordinal] options:NSRegularExpressionCaseInsensitive error:nil];
    NSInteger matchCount = [regEx numberOfMatchesInString:monkeyID options:0 range:NSMakeRange(0, [monkeyID length])];
    
    if (ordinal > 1 && [monkeyID rangeOfString:@"#"].location != 0 && matchCount == 0) {
        return TRUE;
    }
    return FALSE;
}

+ (void) buildCommand:(MTCommandEvent*)event {
    [[self sharedMonkey].commands addObject:[NSMutableDictionary dictionaryWithDictionary:event.dict]];
    lastCommandPosted = event;
}

//- (void) reportLastResult:(NSString*)result forCommandNumber:(NSInteger)index {
//
//}

+ (void) recordEvent:(MTCommandEvent*)event {
	[[self sharedMonkey] recordEvent:event];
}

+ (void) recordEvent:(MTCommandEvent*)event withSource:(UIView *)source {
	[[self sharedMonkey] recordEvent:event withSource:source];
}

- (void) recordLastCommand:(NSNotification*) notification {
	[self recordEvent:lastCommandPosted];
	_lastCommandRecorded = YES;
}

- (id)init {
	if ((self = [super init])) {
		self.commands = [NSMutableArray arrayWithCapacity:12];
		self.session = [NSMutableDictionary dictionary];
		lastCommandPosted = [[MTCommandEvent alloc] init];
		nextCommandToRun = [[MTCommandEvent alloc] init];
		[[NSNotificationCenter defaultCenter] addObserver:self
												 selector:@selector(recordLastCommand:)
													 name:MTNotificationCommandPosted object:nil];
		runTimeout = 2.5; // Should remember last user setting
	}
	
	UIDevice* dev = [UIDevice currentDevice];
	[[NSNotificationCenter defaultCenter] addObserver:self
											 selector:@selector(recordRotation:)
												 name:@"UIDeviceOrientationDidChangeNotification" object:nil];
    
    currentOrientation = [dev orientation];
    
    csvIndex = 0;
    
	
	[dev beginGeneratingDeviceOrientationNotifications];
    
    // Register for UITextField and UITextView DidBeginEditing
    // Fixes issue when delegate is not set for text field/view
    //    [[NSNotificationCenter defaultCenter] addObserver:self
    //                                             selector:@selector(textEditingBegan:)
    //                                                 name:UITextFieldTextDidBeginEditingNotification object:nil];
    //    [[NSNotificationCenter defaultCenter] addObserver:self
    //                                             selector:@selector(textEditingBegan:)
    //                                                 name:UITextViewTextDidBeginEditingNotification object:nil];
    
	
	//This is code to register for orientation events and then handle them.
	// CODE TO HANDLE DEVICE ROTATE - NEXT VERSION
	//[[NSNotificationCenter defaultCenter] addObserver: self selector: @selector(receivedRotate:) name: UIDeviceOrientationDidChangeNotification object: nil];
	
    [[MTHTTPServerController sharedInstance]
     httpResponseForQuery:@"/hub/session"
     method:@"POST"
     withData:[@"{\"browserName\":\"firefox\",\"platform\":\"ANY\","
               "\"javascriptEnabled\":false,\"version\":\"\"}"
               dataUsingEncoding:NSASCIIStringEncoding]];
    
	return self;
}


- (void) recordRotation:(NSNotification *)notification
{
    [MTRotateCommand recordRotation:notification];
}

-(void)textEditingBegan:(NSNotification *)notification
{
    UIView* view = (UIView *)[notification object];
    view = [MTUtils findFirstMonkeyView:view];
    
    if (view != nil) {
        [view mtAssureAutomationInit];
    }
}



- (void) sendNotification:(NSString*) notificationName object:sender {
	NSNotification *myNotification =
    [NSNotification notificationWithName:notificationName object:sender];
	[[NSNotificationQueue defaultQueue]
	 enqueueNotification:myNotification
	 postingStyle:NSPostWhenIdle
	 coalesceMask:NSNotificationCoalescingOnName
	 forModes:nil];
}


+ (void) recordFrom:(UIView*)source command:(NSString*)command {
	[[self sharedMonkey] postCommandFrom:source command:command args:nil];
}

- (BOOL) isGestureCommand:(NSString *)command {
    return ([command isEqualToString:MTCommandSwipe ignoreCase:YES] ||
            [command isEqualToString:MTCommandPinch ignoreCase:YES] ||
            [command isEqualToString:MTCommandLongPress ignoreCase:YES] ||
            [command isEqualToString:MTCommandRotate ignoreCase:YES] );
}


- (void) recordFrom:(UIView*)source command:(NSString*)command args:(NSArray*)args post:(BOOL)post {
    BOOL isGesture = [self isGestureCommand:command];
    if (![source isMTEnabled] && !isGesture) {
        return;
    }
    
    // ToDo: Better fix for Play Notification on Done
    if ([lastCommandPosted.monkeyID isEqualToString:@"Done"] &&
        [command  isEqualToString:MTCommandPlayMovie ignoreCase:YES])
        return;
    
    if (state != MTStateRecording && !isWireRecording) {
		return;
	} else if (post) {
		[self postCommandFrom:source command:command args:args];
	} else {
		[ MonkeyTalk recordEvent:[[MTCommandEvent alloc]
								  init:command className:[NSString stringWithUTF8String:class_getName([source class])]
								  monkeyID:[source monkeyID]
								  args:args]];
	}
}
- (void) recordFrom:(UIView*)source command:(NSString*)command monkeyID:(NSString*)monkeyID  args:(NSArray*)args post:(BOOL)post {
    
    if (![source isMTEnabled])
        return;
    
    // ToDo: Better fix for Play Notification on Done
    if ([lastCommandPosted.monkeyID isEqualToString:@"Done"] &&
        [command  isEqualToString:MTCommandPlayMovie ignoreCase:YES])
        return;
    
    if (state != MTStateRecording && !isWireRecording) {
		return;
	} else if (post) {
		[self postCommandFrom:source command:command args:args];
	} else {
		[ MonkeyTalk recordEvent:[[MTCommandEvent alloc]
								  init:command className:[NSString stringWithUTF8String:class_getName([source class])]
								  monkeyID:monkeyID
								  args:args]];
	}
}
+ (void) recordFrom:(UIView*)source command:(NSString*)command args:(NSArray*)args {
	[[MonkeyTalk sharedMonkey] recordFrom:source command:command args:args post:YES];
}
+ (void) recordFrom:(UIView*)source command:(NSString*)command monkeyID:(NSString *)monkeyID args:(NSArray*)args {
	[[MonkeyTalk sharedMonkey] recordFrom:source command:command args:args post:YES];
}
- (void) recordFrom:(UIView*)source command:(NSString*)command  {
    
    if (![source isMTEnabled])
        return;
    
	[self postCommandFrom:source command:command args:nil];
}
// Code to record web components

+(void) recordWebComponents:(NSString *)source monkeyID:(NSString *)monkeyID command:(NSString *)command args:(NSArray *)args
{
    [MonkeyTalk sendRecordEvent:[[MTCommandEvent alloc] init:command className:source monkeyID:monkeyID args:args]];
}
//- (BOOL)sendAction:(SEL)action to:(id)target from:(id)sender forEvent:(UIEvent *)event {
//	NSLog(@"Got an action from %@ for %@ ", [sender description], [event description]);
//	return [super sendAction:action to:target from:sender forEvent:event];
//}

- (BOOL) isRecording {
	return (state == MTStateRecording);
}

+ (BOOL) isRecording {
	return [[self sharedMonkey] isRecording];
}

- (void) continueMonitoring {
	if (state == MTStateSuspended) {
		return;
	}
	[NSObject cancelPreviousPerformRequestsWithTarget:self];
    
    // MT6: Hide Console
    //	[self performSelector:@selector(suspend) withObject:nil afterDelay:runTimeout];
}

- (void) pause {
	state = MTStatePaused;
	[self continueMonitoring];
}



- (NSString*) lastResult {
	return [self firstErrorIndex] == -1 ? nil : [[self commandAt:[self firstErrorIndex]] lastResult];
}

- (void) play:(BOOL)waitUntilDone {
	state = MTStatePlaying;
	NSThread* thread = [[NSThread alloc] initWithTarget:self selector:@selector(runCommands) object:nil];
	[thread start];
	if (waitUntilDone) {
        if (!getenv("MT_ENABLE_QUNIT")) { // Skip loop if JS test running, otherwise it loops forever
            while (![thread isFinished]) {
            	usleep(100000);
            }
            
            while (isDriving) {
                usleep(100000);
            }
        } else {
            [_console hideConsoleQunit];
        }
	}
    
	return;
}

- (void) playFrom:(NSUInteger)index {
	state = MTStatePlaying;
	NSThread* thread = [[NSThread alloc] initWithTarget:self selector:@selector(runCommandsStartingFrom:) object:[NSNumber numberWithInteger:index]];
	[thread start];
	
}

- (void) playFrom:(NSUInteger)index numberOfCommands:(NSUInteger)count{
	state = MTStatePlaying;
	NSThread* thread = [[NSThread alloc] initWithTarget:self selector:@selector(runCommandRange:) object:[NSArray arrayWithObjects:[NSNumber numberWithInteger:index],[NSNumber numberWithInteger:count],nil]];
	[thread start];
	
}

- (NSString*) playAndWait {
	state = MTStatePlaying;
	[_console performSelectorOnMainThread:@selector(hideConsoleAndThen:) withObject:nil waitUntilDone:YES];
	[self play:YES];
	return [self lastResult];
}

- (NSString*) runScript:(NSString*)script {
	[self open:script];
	return [self playAndWait];
}

- (void) play {
	[self play:NO];
}

- (void) record {
	state = MTStateRecording;
	[self continueMonitoring];
}

- (void) suspend {
	state = MTStateSuspended;
	[self sendNotification:MTNotificationMonkeySuspended object:self];
}


- (void) playingDone {
    if (_console.view.alpha == 0) {
        state = MTStateSuspended;
        isDriving = NO;
        
        if (getenv("MT_ENABLE_QUNIT")) {
            [_console showConsoleQunit];
            [_console.connectWebView qResult:@"success" event:nextCommandToRun function:@"MTPlayCommands"];
            
            [_console performSelectorOnMainThread:@selector(showView:) withObject:nil waitUntilDone:NO];
        }
        
        [self sendNotification:MTNotificationPlayingDone object:self];
    }
}

- (void) clear {
	[self sendNotification:MTNotificationScriptOpened object:self];
    
	[commands removeAllObjects];
}

- (void) handleEvent:(UIEvent*)event {
	if (state == MTStateSuspended || state == MTStatePlaying) {
		return;
	}
	
	BOOL eventHandled = NO;
    
	if (event.type == UIEventTypeTouches) {
		NSSet* touches = [event allTouches];
		UITouch* touch = [touches anyObject];
		UIView* view = touch.view;
		view = [MTUtils findFirstMonkeyView:view];
        
		if (view != nil) {
			[view mtAssureAutomationInit];
			if ([view shouldRecordMonkeyTouch:touch]) {
				[view handleMonkeyTouchEvent:touches withEvent:event];
				NSLog(@"MonkeyTalk(state: %d) got an event\n%@", state, event);
				eventHandled = YES;
                
			}
		}
        //		else {
        //						NSLog(@"Nil view");
        //			UIWindow* window = [touch window];
        //			view = [window hitTest:[touch locationInView:window] withEvent:event];
        //			view = [MTUtils findFirstMonkeyView:view];
        //		}
	} else if (event.type == UIEventTypeMotion) {
		[[[UIApplication sharedApplication] keyWindow] handleMonkeyMotionEvent:event];
		eventHandled = YES;
	}
	else {
		NSLog(@"Event has invalid type. Keyboard input?");
	}
    
	if (!eventHandled) {
		NSLog(@"No MTReady view to handle this event\n%@", event);
		
	}
	[self continueMonitoring];
	return;
    
}

- (void) addCommand:(MTCommandEvent*)cmd {
	[commands addObject:[cmd dict]];
}

- (void) loadCommands:(NSArray*) cmds {
	[commands removeAllObjects];
	int i;
	for (i = 0; i < [cmds count]; i++) {
		[self addCommand:[cmds objectAtIndex:i]];
	}
}

- (void) saveScreenshot:(MTCommandEvent*) command {
    NSString* fileName = [MTUtils screenshotFileName:command];
    
    NSLog(@"Screen image saved to %@",fileName);
    [MTUtils saveScreenshot:fileName];
}


- (char*) isScreenshotOn {
    if (!_enableScreenshot) {
        _enableScreenshot = getenv("MT_ENABLE_SCREENSHOT");
        if (!_enableScreenshot) {
            _enableScreenshot = "NONE";
        }
    }
    return _enableScreenshot;
}

- (BOOL) isScreenshotOnFailure {
    return !strcmp([self isScreenshotOn],"FAILURE");
}

- (BOOL) isScreenshotOnAll {
    return !strcmp([self isScreenshotOn],"ALL");
}

- (void) runCommandsStartingFrom:(NSInteger)start numberOfCommands:(NSInteger)count{
	// We're a thread
	@autoreleasepool {
	
		BOOL failure = NO;
		int i;
		for (i = start; i < start + count; i++) {
			MTCommandEvent* nextCommandToRun = [self commandAt:i];
			nextCommandToRun.lastResult = nil;
        
        double commandPlaybackSpeed = [MTUtils playbackSpeedForCommand:nextCommandToRun isLive:_console.liveSwitch.on retryCount:retryCount];
        double commandTimeout = [MTUtils timeoutForCommand:nextCommandToRun];
        
        // If user is using recorded speed, set timeout to 0
        if (_console.liveSwitch.on)
            commandTimeout = 0;
        
        // Set 10 millisecond playback if speed is 0
        // Fixes issue causeing component not found when running script
        if (commandPlaybackSpeed == 0)
            commandPlaybackSpeed = 10000;
        
        if ([self commandCount] > i+1)
            commandSpeed = [NSString stringWithFormat:@"%f",[MTUtils playbackSpeedForCommand:nextCommandToRun isLive:_console.liveSwitch.on retryCount:retryCount]];
        
			if (failure) {
				continue;
			}
        if ([nextCommandToRun.command isEqualToString:MTCommandDataDrive]) {
            NSLog(@"Data driving with %@.csv at row %i",[nextCommandToRun.args objectAtIndex:0],csvIndex+1);
            isDriving = YES;
        }
        else
            NSLog(@"\n\n< < <Playing> > > %@ %@ \"%@\" %@\n\n", nextCommandToRun.command,
                  nextCommandToRun.className != nil ? nextCommandToRun.className : @"", nextCommandToRun.monkeyID,
                  nextCommandToRun.args);
        
		if ([nextCommandToRun.command isEqualToString:MTCommandPause]) {
			if ([nextCommandToRun.args count] < 1) {
				nextCommandToRun.lastResult = [NSString stringWithFormat:@"Requires 1 argument, but has %d", [nextCommandToRun.args count]];
			} else {
				NSInteger msecs = [((NSString*)[nextCommandToRun.args objectAtIndex:0]) intValue];
				usleep(msecs * 1000);
			}
		} else if ([nextCommandToRun.command  isEqualToString:MTCommandShake ignoreCase:YES]) {
			usleep(commandPlaybackSpeed);
			[MTUtils shake];
		} else if ([nextCommandToRun.command  isEqualToString:MTCommandRotate ignoreCase:YES]) {
			usleep(commandPlaybackSpeed);
			[self performSelectorOnMainThread:@selector(rotate:) withObject:nextCommandToRun waitUntilDone:YES];
		} else if ([nextCommandToRun.command  isEqualToString:MTCommandGetVariable ignoreCase:YES]) {
            if (!replaceVars)
                replaceVars = [[NSMutableDictionary alloc] init];
            
            replaceVars = [MTUtils addVarsFrom:nextCommandToRun to:replaceVars];
            
            if (getenv("MT_ENABLE_QUNIT"))
                [_console.connectWebView qResult:@"success" event:nextCommandToRun function:nextCommandToRun.command];
        } else if ([nextCommandToRun.command isEqualToString:MTCommandDataDrive]) {
            NSString *tempSt = [NSString stringWithFormat:@"%@/%@.csv",[MTUtils scriptsLocation],[nextCommandToRun.args objectAtIndex:0]];
            
            csvData = [MTParseCsv readFile:tempSt error:nil];
            csvIndex++;
            
            [_console.connectWebView qResult:[NSString stringWithFormat:@"%i", [csvData count]] event:nextCommandToRun function:MTCommandDataDrive];
        } else if ([nextCommandToRun.command isEqualToString:MTCommandRun]) {
            
            UIWebView *webView = (UIWebView *)nextCommandToRun.source;
            MTWebViewController *webDriver = (MTWebViewController *)webView.delegate;
            NSString *htmlFile = [nextCommandToRun.args objectAtIndex:0];
            
            while (!webDriver) {
                usleep(5000);
                webView = (UIWebView *)nextCommandToRun.source;
                webDriver = (MTWebViewController *)webView.delegate;
            }
            
            dispatch_async(dispatch_get_main_queue(), ^{
                [webDriver seleniumFile:htmlFile speed:commandPlaybackSpeed timeout:commandTimeout];
            });
            webDriver.testComplete = nil;
            
            //[webDriver performSelectorOnMainThread:@selector(selenium) withObject:nil waitUntilDone:YES];
            
            while (!webDriver.testComplete)
                usleep(5000);
            
            if (![webDriver.testComplete isEqualToString:@"YES"])
            {
                nextCommandToRun.lastResult = [NSString stringWithFormat:@"%@ not found. Row: %i",webDriver.testComplete, webDriver.errorIndex+1];
            }
            
            // Reset test for next run
            webDriver.testComplete = nil;
        } else {
				usleep(commandPlaybackSpeed);
				//UIView* source = (UIView *)nextCommandToRun.source;
				//if (source != nil) {
            
            NSArray *origArgs = [NSArray arrayWithArray:nextCommandToRun.args];
            NSString *origID = nextCommandToRun.monkeyID;
            
            // Check if variables within command
            if (replaceVars)
            {
                nextCommandToRun.args = [MTUtils replaceArgForCommand:nextCommandToRun.args variables:replaceVars];
                nextCommandToRun.monkeyID = [MTUtils replaceMonkeyIdForCommand:nextCommandToRun.monkeyID variables:replaceVars];
            }
            
            if (csvData) {
                //NSMutableDictionary *tempDict = [csvData objectAtIndex:0];
                
                if (csvIndex-1 < [csvData count]) {
                    nextCommandToRun.args = [MTUtils replaceArgForCommand:nextCommandToRun.args variables:[csvData objectAtIndex:csvIndex-1]];
                }
                //[tempDict release];
            }
            [self performSelectorOnMainThread:@selector(playbackMonkeyEvent:) withObject:nextCommandToRun waitUntilDone:YES];
            
            nextCommandToRun.args = origArgs;
            nextCommandToRun.monkeyID = origID;
            
				//} else {
				//	nextCommandToRun.lastResult = [NSString stringWithFormat:@"No %@ found with monkeyID \"%@\"", nextCommandToRun.className, nextCommandToRun.monkeyID];
				//}
			}
        
			if ([nextCommandToRun lastResult]) {
            // Retry command based on values for command
            if (retryCount < commandTimeout/10 && ![nextCommandToRun.command isEqualToString:MTCommandWebDriver])
            {
                //NSLog(@"time: %@",[MTUtils timeStamp]);
                retryCount++;
                [self playFrom:i];
                return;
            }
            
            NSLog(@"MonkeyTalk Script Failure: %@\n", nextCommandToRun.lastResult);
            if (getenv("MT_ENABLE_QUNIT"))
                [_console.connectWebView qResult:nextCommandToRun.lastResult event:nextCommandToRun function:@"MTPlayCommands"];
            if ([self isScreenshotOnFailure] || [self isScreenshotOnAll]) {
                [self saveScreenshot:nextCommandToRun];
            }
				failure = YES;
			} else {
            if ([self isScreenshotOnAll]) {
                [self saveScreenshot:nextCommandToRun];
            }
        }
        
        // Reset retry count for next command
        retryCount = 0;
		}
    
    previousTime = nil;
    commandSpeed = nil;
		usleep(500000); // When playback is done, wait a sec before dropping the curtain
    
    if (csvIndex < [csvData count] && !getenv("MT_ENABLE_QUNIT"))
        [self performSelectorOnMainThread:@selector(playFrom:) withObject:0 waitUntilDone:YES];
    else
        [self performSelectorOnMainThread:@selector(playingDone) withObject:nil waitUntilDone:YES];
    
    if (csvIndex == [csvData count] && csvIndex != 0)
        csvIndex = 0;
    
	//[self performSelectorOnMainThread:@selector(playingDone) withObject:nil waitUntilDone:YES];
	
    //if (csvIndex == [csvData count])
    //csvIndex = 0;
	
	}
	
}

- (void) playCommandFromJs:(MTCommandEvent*)command {
    state = MTStatePlaying;
    
    if (_console.view.alpha > 0)
        [_console performSelectorOnMainThread:@selector(hideConsoleAndThen:) withObject:nil waitUntilDone:YES];
    MTCommandEvent* nextCommandToRun = command;
    nextCommandToRun.lastResult = nil;
    
    if ([nextCommandToRun.command isEqualToString:MTCommandDataDrive]) {
        NSLog(@"Data driving with %@.csv at row %i",[nextCommandToRun.args objectAtIndex:0],csvIndex+1);
        isDriving = YES;
    }
    else
        NSLog(@"\n\n< < <Playing> > > %@ %@ \"%@\" %@\n\n", nextCommandToRun.command,
              nextCommandToRun.className != nil ? nextCommandToRun.className : @"", nextCommandToRun.monkeyID,
              nextCommandToRun.args);
    
    NSLog(@"Source: %@",nextCommandToRun.source);
    
    double commandPlaybackSpeed = [MTUtils playbackSpeedForCommand:nextCommandToRun isLive:_console.liveSwitch.on retryCount:retryCount];
    double commandTimeout = [MTUtils timeoutForCommand:nextCommandToRun];
    
    // If user is using recorded speed, set timeout to 0
    if (_console.liveSwitch.on)
        commandTimeout = 0;
    
    // Set 10 millisecond playback if speed is 0
    // Fixes issue causeing component not found when running script
    if (commandPlaybackSpeed == 0)
        commandPlaybackSpeed = 10000;
    
    // Handle GetVariable command
    if ([nextCommandToRun.command isEqualToString:MTCommandGetVariable]) {
        if (!replaceVars) {
            replaceVars = [[NSMutableDictionary alloc] init];
        }
        
        replaceVars = [MTUtils addVarsFrom:nextCommandToRun to:replaceVars];
    }
    
    usleep(commandPlaybackSpeed);
    //UIView* source = (UIView *)nextCommandToRun.source;
    //if (source != nil) {
    
    NSArray *origArgs = [NSArray arrayWithArray:nextCommandToRun.args];
    NSString *origID = nextCommandToRun.monkeyID;
    
    // Check if variables within command
    if (replaceVars)
    {
        nextCommandToRun.args = [MTUtils replaceArgForCommand:nextCommandToRun.args variables:replaceVars];
        nextCommandToRun.monkeyID = [MTUtils replaceMonkeyIdForCommand:nextCommandToRun.monkeyID variables:replaceVars];
    }
    
    if (csvData) {
        //NSMutableDictionary *tempDict = [csvData objectAtIndex:0];
        
        if (csvIndex-1 < [csvData count]) {
            nextCommandToRun.args = [MTUtils replaceArgForCommand:nextCommandToRun.args variables:[csvData objectAtIndex:csvIndex-1]];
        }
        //[tempDict release];
    }
    
    [self performSelectorOnMainThread:@selector(playbackMonkeyEvent:) withObject:nextCommandToRun waitUntilDone:YES];
    
    [_console.connectWebView qResult:@"success" event:nextCommandToRun function:@"MTPlayCommand"];
    
    nextCommandToRun.args = origArgs;
    nextCommandToRun.monkeyID = origID;
    
    if ([nextCommandToRun lastResult]) {
        
        NSLog(@"MonkeyTalk Script Failure: %@\n", nextCommandToRun.lastResult);
        if (getenv("MT_ENABLE_QUNIT"))
            [_console.connectWebView qResult:nextCommandToRun.lastResult event:nextCommandToRun function:@"MTPlayCommands"];
        if ([self isScreenshotOnFailure] || [self isScreenshotOnAll]) {
            [self saveScreenshot:nextCommandToRun];
        }
    } else {
        
        if ([self isScreenshotOnAll]) {
            [self saveScreenshot:nextCommandToRun];
        }
    }
    
    state = MTStateSuspended;
}

- (void) playbackExecMethod:(MTCommandEvent *)event {
    NSError *error = nil;
    event.value = [[UIApplication sharedApplication] playbackExecForClass:event.className method:event.command args:event.args error:&error];
    
    if (error) {
        NSString *component = [MTConvertType convertedComponentFromString:event.className isRecording:YES];
        
        if (!event.lastResult && event.lastResult.length == 0) {
            event.lastResult = [NSString stringWithFormat:@"No %@ found with monkeyID \"%@\"", component, event.monkeyID];
        }
    }
}

- (void) playbackMonkeyEvent:(MTCommandEvent*)command {
    UIView* source = command.source;
    
    if([source isKindOfClass:objc_getClass("MTFoundElement")]) {
        // Played in web view
    } else if (source != nil) {
        if (source.layer.animationKeys) {
            command.lastResult = [NSString stringWithFormat:@"%@ with monkeyID \"%@\" is animating", command.component, command.monkeyID];
            return;
        }
        
        [source playbackMonkeyEvent:command];
    } else if (!command.lastResult && !command.didPlayInWeb &&
               ![command.command isEqualToString:MTCommandVerifyNot ignoreCase:YES]) {
        // Starting in beta5 we try to execute command on the class before failure
        [self playbackExecMethod:command];
    } else if(![command.command isEqualToString:MTCommandVerifyNot ignoreCase:YES]) {
        NSString *component = [MTConvertType convertedComponentFromString:command.className isRecording:YES];
        command.lastResult = [NSString stringWithFormat:@"No %@ found with monkeyID \"%@\"", component, command.monkeyID];
    }
}

#pragma mark - Playback for Wire Protocol

NSTimeInterval startInterval;
float retryTime;

- (NSString *) playAndRespond:(MTCommandEvent*)nextCommandToRun {
    currentCommand = nextCommandToRun;
    // Play command for wire protocol
    if (startInterval == 0) {
        startInterval = [[NSDate date] timeIntervalSince1970];
        // NSLog(@"Start Interval: %f",startInterval);
    }
    
    state = MTStatePlaying;
    if ([nextCommandToRun.command isEqualToString:MTCommandDataDrive]) {
        NSLog(@"Data driving with %@.csv at row %i",
              [nextCommandToRun.args objectAtIndex:0],
              csvIndex+1);
        isDriving = YES;
    } else {
        NSLog(@"\n\nPLAYING - %@\n\n",
              nextCommandToRun.printCommand);
    }
    
    SBJsonWriterMT *jsonWriter = [[SBJsonWriterMT alloc] init];
    NSMutableDictionary *jsonDict = [[NSMutableDictionary alloc] init];
    NSString *postString = nil;
    NSArray *origArgs = nil;
    NSString *origID = nil;
    nextCommandToRun.lastResult = nil;
    
    // Convert strings to double values
    double commandPlaybackSpeed = [nextCommandToRun.playbackDelay doubleValue]*1000;
    double commandTimeout = [nextCommandToRun.playbackTimeout doubleValue];
    
    /* MT6: Hide Console
     // Hide console if not already hidden
     if (_console.view.alpha > 0)
     [_console performSelectorOnMainThread:@selector(hideConsoleAndThen:)
     withObject:nil waitUntilDone:YES];
     
     // If user is using recorded speed, set timeout to 0
     if (_console.liveSwitch.on)
     commandTimeout = 0;
     */
    
    // Set 10 millisecond playback if speed is 0
    // Fixes issue causing component not found when running script
    if (commandPlaybackSpeed == 0)
        commandPlaybackSpeed = 10000;
    
    // The command is retrying — set to default retry delay
    if (retryTime > 0)
        commandPlaybackSpeed = MT_DEFAULT_RETRY_DELAY;
    
    if ([nextCommandToRun.command isEqualToString:MTCommandVerifyImage]) {
        // perform agent-side bit of this command
        // same for all components
        usleep(commandPlaybackSpeed);
        jsonDict = [MonkeyTalk verifyImage:nextCommandToRun andDict:jsonDict];
        if([[jsonDict objectForKey:@"result"] isEqualToString:(@"FAILURE")])
            nextCommandToRun.lastResult = [NSString stringWithFormat:@"The component \"%@\" with monkeyID \"%@\" was not found! You can't verify the image of non-existant components.", nextCommandToRun.className, nextCommandToRun.monkeyID];
    } else if ([nextCommandToRun.className isEqualToString:MTComponentDevice ignoreCase:YES]) {
        // Handle device commands
        usleep(commandPlaybackSpeed);
        
        // Set jsonDict based on device result
        jsonDict = [MTDevice postStringForCommand:nextCommandToRun andDict:jsonDict];
    } else {
        usleep(commandPlaybackSpeed);
        
        origArgs = [NSArray arrayWithArray:nextCommandToRun.args];
        origID = nextCommandToRun.monkeyID;
        
        // Check if variables within command
        if (replaceVars) {
            nextCommandToRun.args = [MTUtils
                                     replaceArgForCommand:nextCommandToRun.args
                                     variables:replaceVars];
            nextCommandToRun.monkeyID = [MTUtils
                                         replaceMonkeyIdForCommand:nextCommandToRun.monkeyID
                                         variables:replaceVars];
        }
        
        if (csvData) {
            if (csvIndex-1 < [csvData count])
                nextCommandToRun.args = [MTUtils
                                         replaceArgForCommand:nextCommandToRun.args
                                         variables:[csvData objectAtIndex:csvIndex-1]];
        }
        
        if (self.isPushingController) {
            nextCommandToRun.lastResult = [NSString stringWithFormat:@"No %@ found with monkeyID \"%@\"", nextCommandToRun.component, nextCommandToRun.monkeyID];
        }
        
        // Play command on main thread
        if ([nextCommandToRun.className isEqualToString:MTComponentApp ignoreCase:YES])
            [[UIApplication sharedApplication] performSelectorOnMainThread:@selector(playbackMonkeyEvent:)
                                                                withObject:nextCommandToRun waitUntilDone:YES];
        else if (!nextCommandToRun.lastResult)
            [self performSelectorOnMainThread:@selector(playbackMonkeyEvent:)
                                   withObject:nextCommandToRun waitUntilDone:YES];
        
        // Reset original args/monkeyID
        nextCommandToRun.args = origArgs;
        nextCommandToRun.monkeyID = origID;
        
        // Get command successfully returned value
        // Set postString with found value
        if (nextCommandToRun.value) {
            [jsonDict setValue:@"OK" forKey:@"result"];
            [jsonDict setValue:nextCommandToRun.value forKey:@"message"];
            
            postString = [jsonWriter stringWithObject:jsonDict];
            
            if (![nextCommandToRun lastResult])
                return postString;
        }
    }
    
    
    
    if ([nextCommandToRun lastResult]) {
        NSTimeInterval currentInterval = [[NSDate date] timeIntervalSince1970];
        
        // Get time diff between now and start and convert to milliseconds
        retryTime = (currentInterval-startInterval) * 1000;
        
        // Subtract the thinktime from the time diff
        retryTime -= [nextCommandToRun.playbackDelay doubleValue];
        
        // Retry command until timout
        if (retryTime < commandTimeout)
            return [self playAndRespond:nextCommandToRun];
        
        // Set up message dict with screenshot and message
        // If screenshotonerror is set to true, we generate screenshot or else only failure message is sent to IDE.
        // screenshotonerror modifier is set to false by IDE by default.
        
        NSArray *messageKeys, *messageObjects;
        NSDictionary *messageDict;
        NSString *screenshotOnErrorValue = [nextCommandToRun.modifiers objectForKey:MTWireScreenshotOnError];
        Boolean shouldTakeScreenshotOnError = (!screenshotOnErrorValue ||
                                               [screenshotOnErrorValue isEqualToString:@"true" ignoreCase:YES]);
        
        if (shouldTakeScreenshotOnError) {
            messageKeys = [NSArray arrayWithObjects:@"screenshot", @"message", nil];
            messageObjects = [NSArray arrayWithObjects:[MTUtils encodedScreenshot], nextCommandToRun.lastResult, nil];
            messageDict = [NSDictionary dictionaryWithObjects:messageObjects forKeys:messageKeys];
            [jsonDict setValue:messageDict forKey:@"message"];
        }
        else {
            [jsonDict setValue:nextCommandToRun.lastResult forKey:@"message"];
        }
        
        // Command failed
        [jsonDict setValue:@"FAILURE" forKey:@"result"];
        
        postString = [jsonWriter stringWithObject:jsonDict];
        
        NSLog(@"MonkeyTalk Script Failure: %@\n", nextCommandToRun.lastResult);
        
        //        if ([self isScreenshotOnFailure] || [self isScreenshotOnAll])
        //            [self saveScreenshot:nextCommandToRun];
    } else {
        // Command succeeded
        [jsonDict setValue:@"OK" forKey:@"result"];
        postString = [jsonWriter stringWithObject:jsonDict];
        
        //        if ([self isScreenshotOnAll])
        //            [self saveScreenshot:nextCommandToRun];
    }
    
    state = MTStateSuspended;
    startInterval = 0;
    retryTime = 0;
    
    
    while (isAnimating) {
        [NSThread sleepForTimeInterval:0.1];
    }
    
    return postString;
}

#pragma mark -


- (void) runCommandRange:(NSArray*)array {
	[self runCommandsStartingFrom:[[array objectAtIndex:0] intValue] numberOfCommands:[[array objectAtIndex:1] intValue]];
}


- (void) runCommands {
	[self runCommandsStartingFrom:0 numberOfCommands:[commands count]];
}

- (void) runCommandsStartingFrom:(NSNumber*)start {
	[self runCommandsStartingFrom:[start intValue] numberOfCommands:([commands count] - [start intValue])];
}



- (IBAction) clear:(id)sender {
	[commands removeAllObjects];
}

- (void)rotate:(MTCommandEvent*)command {
    //	UIInterfaceOrientation orientation = 0;
    //	if ([command.args count] > 0) {
    //		orientation = [((NSString*)[command.args objectAtIndex:0]) intValue];
    //	}
    //	[MTUtils rotate:orientation];
    
    [MTRotateCommand rotate:command];
}

- (void) save:(NSString*)file {
	NSLog(@"saving script \"%@\" to %@",file,[MTUtils scriptsLocation]);
	NSString* error;
	NSData* pList = [NSPropertyListSerialization dataFromPropertyList:commands format:NSPropertyListXMLFormat_v1_0 errorDescription:&error];
	if (error) {
		NSLog(@"%@", error);
	}
	//[self assureScriptsLocation];
	if (![file hasSuffix:@".mt"]) {
		file = [file stringByAppendingString:@".mt"];
	}
	[MTUtils writeApplicationData:pList toFile:file];
	if ([file hasSuffix:@".mt"]) {
		file = [file stringByDeletingPathExtension];
	}
	NSString* ocunitPath = file;
	//NSString* ocunitPath = [[NSString stringWithString:OCUNIT_PATH] stringByAppendingPathComponent:file];
	[self saveOCScript:ocunitPath];
    
    if (getenv("MT_ENABLE_UIAUTOMATION")) {
        NSString* uiautomationPath = file;
        //NSString* uiautomationPath = [[NSString stringWithString:UIAUTOMATION_PATH] stringByAppendingPathComponent:file];
        [self saveUIAutomationScript:uiautomationPath];
    } else {
        NSString* qunitPath = file;
        //NSString* qunitPath = [[NSString stringWithString:QUNIT_PATH] stringByAppendingPathComponent:file];
        [self saveQUnitScript:qunitPath];
    }
}

- (void) open:(NSString*)file {
	NSData* data = [MTUtils applicationDataFromFile:file];
	NSString* errorString = [NSString string];
	NSArray* array = [NSPropertyListSerialization propertyListFromData:data mutabilityOption:0 format:nil errorDescription:&errorString];
	NSLog(@"%@", errorString);
	if (array) {
		NSMutableArray* mutie = [[NSMutableArray alloc] initWithCapacity:[array count]];
		[mutie addObjectsFromArray:array];
		self.commands = mutie;
	}
	
	[self sendNotification:MTNotificationScriptOpened object:nil];
    
	
}

- (void) postCommandFrom:(UIView*)sender command:(NSString*)command args:(NSArray*)args {
    BOOL isGesture = [self isGestureCommand:command];
    
    // Added below lines of code to send device orientation recording to IDE
    if (!sender && [command isEqualToString:MTCommandRotate]) {
        lastCommandPosted.command = command;
        lastCommandPosted.args = args;
        lastCommandPosted.monkeyID = @"#1";
        [self sendNotification:MTNotificationCommandPosted object:sender];
        return;
    }
    if (![sender isMTEnabled] && !isGesture)
        return;
    
	if (self.state == MTStateSuspended) {
		return;
	}
    
    if (!_lastCommandRecorded && lastCommandPosted.monkeyID && lastCommandPosted.monkeyID != [sender monkeyID]) {
        [self recordEvent:lastCommandPosted];
    } else if (([lastCommandPosted.command isEqualToString:MTCommandDrag] && !_lastCommandRecorded) ||
               [lastCommandPosted.command isEqualToString:MTCommandTouchUp])
        [self recordEvent:lastCommandPosted];
    
	_lastCommandRecorded = NO;
	lastCommandPosted.command = command;
	if (sender) {
        if ([sender isKindOfClass:objc_getClass("UINavigationItemButtonView")]) {
            UIAlertView *b = (UIAlertView *)sender;
            lastCommandPosted.monkeyID = [b title];
        } else
        {
            lastCommandPosted.monkeyID = [sender monkeyID];
        }
        
        // Workaround to get monkey ID of tab bar
        //        if ([sender isKindOfClass:objc_getClass("UITabBar")])
        //            lastCommandPosted.monkeyID = [args objectAtIndex:0];
        
        lastCommandPosted.className = [NSString stringWithUTF8String:class_getName([sender class])];
        
        // If subclass of MTReady class, save inherited class (ignoring UITableView)
        for (NSString *classString in MTObjCComponentsArray) {
            if ([[sender class] isSubclassOfClass:objc_getClass([classString cStringUsingEncoding:NSStringEncodingConversionAllowLossy])] &&
                ![sender isKindOfClass:[UITableView class]])
                lastCommandPosted.className = classString;
            
        }
	} else {
		lastCommandPosted.monkeyID = nil;
		lastCommandPosted.className = nil;
	}
    
    //    // Workaround to get monkey ID of tab bar
    //    if ([sender isKindOfClass:objc_getClass("UITabBar")])
    //        lastCommandPosted.args = nil;
    //    else
    lastCommandPosted.args = args;
    [self sendNotification:MTNotificationCommandPosted object:sender];
}

- (NSUInteger) commandCount {
	return [self.commands count];
}

- (NSArray*) scripts {
    NSString *scriptsLocation = [MTUtils scriptsLocation];
	NSError* errorString = nil;
	NSArray* paths;
	NSFileManager* fileManager = [NSFileManager defaultManager];
	if (![[NSFileManager defaultManager] fileExistsAtPath:scriptsLocation]) {
		return emptyArray;
	}
	paths = [fileManager contentsOfDirectoryAtPath:scriptsLocation error:&errorString];
	if (errorString) {
		NSLog(@"%@",errorString);
	}
	
	// filter out directories
	if (paths) {
		NSMutableArray* filtered = [[NSMutableArray alloc] init];
		BOOL isDirectory;
		NSString* scriptsLocation = [MTUtils scriptsLocation];
		for (int i=0; i<[paths count]; i++) {
			NSString* path = [paths objectAtIndex:i];
			if ([path hasPrefix:@"."]) {
				continue;
			}
			NSString* fullPath = [scriptsLocation stringByAppendingPathComponent:path];
			if ([fileManager fileExistsAtPath:fullPath isDirectory:&isDirectory]) {
				if (!isDirectory && [fullPath hasSuffix:@".mt"]) {
					[filtered addObject:path];
				}
			}
		}
		paths = filtered;
	}
	
	return paths;
    
}

- (void) delete:(NSString*)file {
    NSString *documentsDirectory = [MTUtils scriptsLocation];
    NSString *appFile = [documentsDirectory stringByAppendingPathComponent:file];
	NSError* errorString = nil;
	[[NSFileManager defaultManager] removeItemAtPath:appFile error:&errorString];
	NSLog(@"%@", errorString);
	
}

- (MTCommandEvent*)commandAt:(NSInteger)index {
	NSMutableDictionary* dict = [commands objectAtIndex:index];
	return [[MTCommandEvent alloc] initWithDict:dict];
	
}

- (void) deleteCommand:(NSInteger) index {
    if (index >= commands.count)
        return;
	[commands removeObjectAtIndex:index];
}


- (void) insertCommand:(NSInteger) index {
	MTCommandEvent* command = [[MTCommandEvent alloc] init];
	MTCommandEvent* prev = [self commandAt:index - 1];
	command.command = @"Verify";
	command.className = prev.className;
	command.monkeyID = prev.monkeyID;
	[commands insertObject:[command dict] atIndex:index];
}

- (MTCommandEvent*) lastCommand {
	NSInteger index = [commands count] - 1;
	
	NSMutableDictionary* dict = nil;
	if (index > -1) {
		dict = [commands objectAtIndex:index];
		return [[MTCommandEvent alloc] initWithDict:dict];
	}
	
	return nil;
}

- (MTCommandEvent*) popCommand {
	NSInteger index = [commands count] - 1;
	
	NSMutableDictionary* dict = nil;
	if (index > -1) {
		dict = [commands objectAtIndex:index];
		[self deleteCommand:index];
		return [[MTCommandEvent alloc] initWithDict:dict];
	}
	
	return nil;
}

- (void) moveCommand:(NSInteger)from to:(NSInteger)to {
	NSDictionary* mover = [commands objectAtIndex:from];
	if (to > from) {
		to--;
	}
    
    if (from >= commands.count)
        return;
    
	[commands removeObjectAtIndex:from];
	[commands insertObject:mover atIndex:to];
}

- (NSInteger) firstErrorIndex {
	int i;
	for (i = 0; i < [commands count]; i++) {
		if ([self commandAt:i].lastResult) {
			return i;
		}
	}
	return -1;
}

- (MTCommandEvent*) lastCommandPosted {
	return lastCommandPosted;
}

- (NSString*) monkeyIDfor:(UIView*)view {
	NSString* value;
	NSValue* key;
	key = [NSValue valueWithPointer:(__bridge const void *)(view)];
	if ((value = [_monkeyIDs objectForKey:key])) {
		return value;
	}
    
    // #mt is replaced with ordinal in UIView+MTReady in monkeyID
	value = [NSString stringWithFormat:@"#mt"];
	[_monkeyIDs setValue:value forKey:(id)key];
	return value;
}

- (BOOL) isOCnitRunnerPresent {
    return [[MonkeyTalk sharedMonkey] respondsToSelector:@selector(runAllTests)];
}

- (void) open {
    // MT6: Hide Console
	[_console showConsole];
    
    if ([self isOCnitRunnerPresent]) {
        //[[MonkeyTalk sharedMonkey] performSelector:@selector(setupObserver)];
        if (!getenv("MT_DISABLE_AUTOSTART")) {
            [[MonkeyTalk sharedMonkey] performSelector:@selector(runAllTests)];
        }
    }
}

- (void) openConsole {
	[_console showConsole];
}

- (void) hideConsole {
	[_console hideConsole];
}

- (void) closeConsole {
	[_console hideConsoleAndThen:nil];
}

- (BOOL) assureScriptsLocation {
	NSString *dataPath = [MTUtils scriptsLocation];
	if (![[NSFileManager defaultManager] fileExistsAtPath:dataPath]) {
		return [[NSFileManager defaultManager] createDirectoryAtPath:dataPath
										 withIntermediateDirectories:YES
														  attributes:nil error:nil]; //Create folder
	}
	return YES;
}

- (BOOL) assureOCUnitScriptDirectory {
	NSString *dataPath = [[MTUtils scriptsLocation] stringByAppendingPathComponent:OCUNIT_PATH];
	if (![[NSFileManager defaultManager] fileExistsAtPath:dataPath]) {
		return [[NSFileManager defaultManager] createDirectoryAtPath:dataPath
										 withIntermediateDirectories:YES
														  attributes:nil error:nil]; //Create folder
	}
	return YES;
}

- (void) saveOCScript:(NSString* ) filename {
	[self assureOCUnitScriptDirectory];
	NSString *path = [[NSBundle mainBundle] pathForResource:
					  @"objc" ofType:@"template"];
	NSError* error;
	NSString* s = [NSString stringWithContentsOfFile:path encoding:NSUTF8StringEncoding error:&error];
	if (!s) {
		NSLog(@"Unable to create objective-c file: Unable to read objc.template: %@", [error description]);
		return;
	}
	int i;
	NSMutableString* code = [[NSMutableString alloc] init];
	for (i = 0; i < [commands count]; i++) {
		MTCommandEvent* command = [self commandAt:i];
		Class c = NSClassFromString(command.className);
		NSString* occmd;
		if (c) {
			occmd = [c objcCommandEvent:command];
		} else {
			occmd = [UIView objcCommandEvent:command];
		}
		if ([occmd hasPrefix:@"//"]) {
			[code appendFormat:@"\t%@\n", occmd];
		} else {
			[code appendFormat:@"\t[array addObject:%@];\n", occmd];
		}
	}
	s = [s stringByReplacingOccurrencesOfString:@"${TESTNAME}" withString:filename];
	s = [s stringByReplacingOccurrencesOfString:@"${CODE}" withString:code];
    
	[MTUtils writeString:s toFile:[filename stringByAppendingString:@".m"]];
	
}

- (BOOL) assureUIAutomationScriptDirectory {
	NSString *dataPath = [[MTUtils scriptsLocation] stringByAppendingPathComponent:UIAUTOMATION_PATH];
	if (![[NSFileManager defaultManager] fileExistsAtPath:dataPath]) {
		return [[NSFileManager defaultManager] createDirectoryAtPath:dataPath
										 withIntermediateDirectories:YES
														  attributes:nil error:nil]; //Create folder
	}
	return YES;
}

- (BOOL) assureUIAutomationScriptSupport {
	NSString *dataPath = UIAUTOMATION_PATH;
	NSString* supportScriptFile = [dataPath stringByAppendingPathComponent:@"MonkeyTalk.js"];
	//NSData* jsLib = [MTUtils applicationDataFromFile:supportScriptFile];
	//if (jsLib==nil || [jsLib length]<1) {
    [self assureUIAutomationScriptDirectory];
    NSString *path = [[NSBundle mainBundle] pathForResource:
                      @"MonkeyTalk" ofType:@"jslib"];
    NSError* error;
    NSString* s = [NSString stringWithContentsOfFile:path encoding:NSUTF8StringEncoding error:&error];
    if (!s) {
        NSLog(@"Unable to create uiautomation file: Unable to read MonkeyTalk.js resource: %@", [error description]);
        return false;
    }
    [MTUtils writeString:s toFile:supportScriptFile];
	//}
	return true;
}

- (void) saveUIAutomationScript:(NSString* ) filename {
	if (! [self assureUIAutomationScriptSupport]) {
		return;
	}
	
	NSString *path = [[NSBundle mainBundle] pathForResource:
					  @"uiautomation" ofType:@"template"];
	NSError* error;
	NSString* s = [NSString stringWithContentsOfFile:path encoding:NSUTF8StringEncoding error:&error];
	if (!s) {
		NSLog(@"Unable to create UIAutomation js file: Unable to read uiautomation.template: %@", [error description]);
		return;
	}
	int i;
	NSMutableString* code = [[NSMutableString alloc] init];
	for (i = 0; i < [commands count]; i++) {
		MTCommandEvent* command = [self commandAt:i];
		NSString* jscmd;
		Class c = NSClassFromString(command.className);
		if (c) {
			jscmd = [c uiAutomationCommand:command];
		} else {
			jscmd = [UIView uiAutomationCommand:command];
		}
		[code appendFormat:@"%@\n", jscmd];
	}
	s = [s stringByReplacingOccurrencesOfString:@"${TESTNAME}" withString:filename];
	s = [s stringByReplacingOccurrencesOfString:@"${CODE}" withString:code];
    
	[MTUtils writeString:s toFile:[filename stringByAppendingString:@".js"]];
	
}

- (BOOL) assureQUnitScriptDirectory {
	NSString *dataPath = [[MTUtils scriptsLocation] stringByAppendingPathComponent:QUNIT_PATH];
	if (![[NSFileManager defaultManager] fileExistsAtPath:dataPath]) {
		return [[NSFileManager defaultManager] createDirectoryAtPath:dataPath
										 withIntermediateDirectories:YES
														  attributes:nil error:nil]; //Create folder
	}
	return YES;
}

- (void) saveQUnitScript:(NSString* ) filename {
	[self assureQUnitScriptDirectory];
	NSString *path = [[NSBundle mainBundle] pathForResource:
					  @"qunit" ofType:@"template"];
	NSError* error;
	NSString* s = [NSString stringWithContentsOfFile:path encoding:NSUTF8StringEncoding error:&error];
	if (!s) {
		NSLog(@"Unable to create QUnit file: Unable to read qunit.template: %@", [error description]);
		return;
	}
	int i;
	NSMutableString* code = [[NSMutableString alloc] init];
	for (i = 0; i < [commands count]; i++) {
		MTCommandEvent* command = [self commandAt:i];
		Class c = NSClassFromString(command.className);
		NSString* occmd;
		if (c) {
			occmd = [c qunitCommandEvent:command];
		} else {
			occmd = [UIView qunitCommandEvent:command];
		}
		if ([occmd hasPrefix:@"//"]) {
			[code appendFormat:@"\t%@\n", occmd];
		} else {
			[code appendFormat:@"\t\tMT.commandList.addRetry(%@);\n", occmd];
		}
	}
	s = [s stringByReplacingOccurrencesOfString:@"${TESTNAME}" withString:filename];
	s = [s stringByReplacingOccurrencesOfString:@"${CODE}" withString:code];
    
	[MTUtils writeString:s toFile:[filename stringByAppendingString:@".js"]];
	
}

- (NSMutableDictionary *) componentMonkeyIds {
    if (!componentMonkeyIds)
        componentMonkeyIds = [[NSMutableDictionary alloc] init];
    
    return componentMonkeyIds;
}

- (NSOperationQueue *) recordOperationQueue {
    if (!recordOperationQueue) {
        recordOperationQueue = [[NSOperationQueue alloc] init];
    }
    
    return recordOperationQueue;
}

- (void) emptyRecordQueue {
    [self.commandQueue removeAllObjects];
    self.commandQueue = nil;
    
    lastCommandPosted.args = [[NSArray alloc] init];
}

// just the agent-side
// take a screenshot and send the bounding box to verify as the message
+ (NSMutableDictionary *) verifyImage:(MTCommandEvent*) ev andDict:(NSMutableDictionary*) jsonDict {
    
    // grab a screenshot
    // Handle screenshot command in MonkeyTalk
    
    // if there is a UIView available, use its dimensions
    CGRect bounds;
    if (ev.source!=nil) {
        bounds=ev.source.frame;
        UIView* parent = ev.source.superview;
        while (parent!=nil) {
            bounds.origin.x += parent.frame.origin.x;
            bounds.origin.y += parent.frame.origin.y;
            parent = parent.superview;
        }
        //bounds.origin.x += [UIScreen mainScreen].applicationFrame.origin.x;
        //bounds.origin.y += [UIScreen mainScreen].applicationFrame.origin.y;
    } else {
        NSString *errorMessage = [NSString stringWithFormat:@"The component \"%@\" with monkeyID \"%@\" was not found! You can't verify the image of non-existant components.", nextCommandToRun.className, nextCommandToRun.monkeyID];
        [jsonDict setValue:errorMessage forKey:@"message"];
        [jsonDict setValue:@"FAILURE" forKey:@"result"];
        return jsonDict;
    }
    
    BOOL *isRetina = [[UIScreen mainScreen] respondsToSelector:@selector(displayLinkWithTarget:selector:)] && ([UIScreen mainScreen].scale == 2.0);
    NSString *boundsMessage = [NSString stringWithFormat:@"%d %d %d %d",
                               ((int)bounds.origin.x * (isRetina ? 2 : 1)),
                               ((int)bounds.origin.y * (isRetina ? 2 : 1)),
                               ((int)bounds.size.width * (isRetina ? 2 : 1)),
                               ((int)bounds.size.height * (isRetina ? 2 : 1))];
    
    [jsonDict setValue:[MTUtils encodedScreenshot] forKey:@"screenshot"];
    [jsonDict setValue:boundsMessage forKey:@"message"];
    [jsonDict setValue:@"OK" forKey:@"result"];
    // [screenshot release];
    
    return jsonDict;
}

+ (NSArray *)allComponents {
    [MTOrdinalView buildFoundComponentsStartingFromView:nil havingClass:@"MTComponentTree" isOrdinalMid:NO skipWebView:NO];
    
    NSArray *found = [[NSArray alloc] initWithArray:[self sharedMonkey].foundComponents];
    
    [[self sharedMonkey].foundComponents removeAllObjects];
    [self sharedMonkey].foundComponents = nil;
    
    return found;
}

@end
