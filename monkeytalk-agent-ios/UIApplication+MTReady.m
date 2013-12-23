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

#import "UIApplication+MTReady.h"
#import <objc/runtime.h>
#import "UIView+MTReady.h"
#import "MonkeyTalk.h"
#import "MTUtils.h"
#import "NSString+MonkeyTalk.h"
#import "MTConvertType.h"
#import "MTHTTPServerController.h"


@implementation UIApplication (MTReady)

+ (void)load {
	if (self == [UIApplication class]) {
		NSLog(@"Loading MonkeyTalk...");
		
        Method originalMethod = class_getInstanceMethod(self, @selector(sendEvent:));
        Method replacedMethod = class_getInstanceMethod(self, @selector(mtSendEvent:));
        method_exchangeImplementations(originalMethod, replacedMethod);
		[[NSNotificationCenter defaultCenter] addObserver:self	
												 selector:@selector(initTheMonkey:)
													 name:UIApplicationDidFinishLaunchingNotification object:nil];
        
        Method originalBgMethod = class_getInstanceMethod(self, @selector(setDelegate:));
        Method replacedBgMethod = class_getInstanceMethod(self, @selector(mtSetDelegate:));
        method_exchangeImplementations(originalBgMethod, replacedBgMethod);
	
	}
}

- (void) mtSetDelegate:(id <UIApplicationDelegate>) del {
	Method originalMethod = class_getInstanceMethod([del class], @selector(applicationDidEnterBackground:));
	if (originalMethod) {
		IMP origImp = method_getImplementation(originalMethod);
		Method replacedMethod = class_getInstanceMethod([self class], @selector(mtApplicationDidEnterBackground:));
		IMP replImp = method_getImplementation(replacedMethod);
		
		if (origImp != replImp) {
			method_setImplementation(originalMethod, replImp);
			class_addMethod([del class], @selector(origApplicationDidEnterBackground:), origImp,"v@");
		}
	}
    
    Method originalForegroundMethod = class_getInstanceMethod([del class], @selector(applicationWillEnterForeground:));
	if (originalForegroundMethod) {
		IMP origImp = method_getImplementation(originalForegroundMethod);
		Method replacedForegroundMethod = class_getInstanceMethod([self class], @selector(mtApplicationWillEnterForeground:));
		IMP replImp = method_getImplementation(replacedForegroundMethod);
		
		if (origImp != replImp) {
			method_setImplementation(originalForegroundMethod, replImp);
			class_addMethod([del class], @selector(origApplicationWillEnterForeground:), origImp,"v@");
		}
	}
    
	[self mtSetDelegate:del];
    
}

- (void)mtApplicationWillEnterForeground:(UIApplication *)application {
    if ([self respondsToSelector:@selector(mtApplicationWillEnterForeground:)]) {
        [self mtApplicationWillEnterForeground:application];
    }
    
    [[MTHTTPServerController sharedInstance]
     httpResponseForQuery:@"/hub/session"
     method:@"POST"
     withData:[@"{\"browserName\":\"firefox\",\"platform\":\"ANY\","
               "\"javascriptEnabled\":false,\"version\":\"\"}"
               dataUsingEncoding:NSASCIIStringEncoding]];
}

- (void)mtApplicationDidEnterBackground:(UIApplication *)application {
    if ([self respondsToSelector:@selector(mtApplicationDidEnterBackground:)]) {
        [self mtApplicationDidEnterBackground:application];
    }
    
    //abort();
    [MTHTTPServerController kill];
}

+ (void) initTheMonkey:(NSNotification*)notification {
	[[MonkeyTalk sharedMonkey] open];
}



- (void)mtSendEvent:(UIEvent *)event {
	[[MonkeyTalk sharedMonkey] handleEvent:event];
	
	// Call the original
	[self mtSendEvent:event];

	
}

- (NSString *) playbackExecForClass:(NSString *)aClass method:(NSString *)method args:(NSArray *)args error:(NSError**)error {
    // Handle Exec method by calling performSelector:withObject: on source class
    
//    if (event.args.count == 0 && !shouldReturn) {
//        event.lastResult = [NSString stringWithFormat:@"Exec requires one or more args"];
//        return;
//    } else if (event.args.count < 2 && shouldReturn) {
//        event.lastResult = [NSString stringWithFormat:@"ExecAndReturn requires at least two args"];
//        return;
//    }
    NSString *value = nil;
    
    // Append : to method
    method = [method stringByAppendingString:@":"];
    
    SEL customSelector = NSSelectorFromString(method);
    Class customClass = NSClassFromString(aClass);
    
    if (!customClass) {
        *error = [NSError errorWithDomain:[NSString stringWithFormat:@"No class %@ found",aClass] code:0 userInfo:nil];
        return nil;
    }
    
    @try {
        if ([customClass respondsToSelector:customSelector]) {
            
            // Handling void class method. Performs only selection, It does not return any value - defaults to nil, if MonkeyTalk Command has ExecAndReturn though.
            
            char methodReturnType[10];
            Method customMethod = class_getClassMethod([customClass class], customSelector);
            method_getReturnType(customMethod, methodReturnType, sizeof(methodReturnType));
            

            if ([[NSString stringWithFormat:@"%s", methodReturnType] isEqualToString:@"v"])
                     [customClass performSelector:customSelector withObject:args];
            else {
                NSString *result = [customClass performSelector:customSelector withObject:args];
                if (result){
                   value = result;
                }
            }
        } else {
            *error = [NSError errorWithDomain:[NSString stringWithFormat:@"%@ does not respond to %@",aClass, method] code:0 userInfo:nil];
        }
    }
    @catch (NSException *exception) {
        // The class does not respond to selector
        *error = [NSError errorWithDomain:[NSString stringWithFormat:@"%@ does not respond to %@",aClass, method] code:0 userInfo:nil];
    }
    
    return value;
}

- (void) playbackMonkeyEvent:(MTCommandEvent *)event {
    // Implementation looks like: + (NSString *) method:(NSArray *)args { }
    
    NSError *error = nil;
    NSString *class = event.monkeyID;
    NSString *method = nil;
    NSMutableArray *args = [[NSMutableArray alloc] initWithArray:event.args];
    [args removeObjectAtIndex:0];
    
    if ([event.command isEqualToString:MTCommandExec ignoreCase:YES]) {
        // App <classname> Exec <method> <<args>>
        if (event.args.count == 0) {
            event.lastResult = [NSString stringWithFormat:@"Exec requires one or more args"];
            return;
        }

        method = [event.args objectAtIndex:0];
    } else if ([event.command isEqualToString:MTCommandExecAndReturn ignoreCase:YES]) {
        // App <classname> ExecAndReturn <variable> <method> <<args>>
        if (event.args.count < 2) {
            event.lastResult = [NSString stringWithFormat:@"ExecAndReturn requires at least two args"];
            return;
        }
        
        // remove the method arg from args passed to method
        [args removeObjectAtIndex:0];
        
        method = [event.args objectAtIndex:1];
    }
    
    event.value = [self playbackExecForClass:class method:method args:args error:&error];
    
    if (error)
        event.lastResult = error.domain;
}


@end
