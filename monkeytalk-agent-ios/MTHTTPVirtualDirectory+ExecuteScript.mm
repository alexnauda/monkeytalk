//
//  MTHTTPVirtualDirectory+ExecuteScript.m
//  iWebDriver
//
//  Copyright 2010 WebDriver committers
//  Copyright 2010 Google Inc.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

#import "MTHTTPVirtualDirectory+ExecuteScript.h"

#include <string>
#import "MTHTTPVirtualDirectory+AccessViewController.h"
#import "NSException+MTWebDriver.h"
#import "NSObject+SBJSONMT.h"
#import "NSString+SBJSONMT.h"
#import "MTWebViewController.h"
#include "MTatoms.h"
#include "MTerrorcodes.h"


@interface MTSimpleObserver : NSObject {
    NSDictionary* data_;
}

+(MTSimpleObserver*) simpleObserverForAction:(NSString*) action
                                   andSender:(id)notificationSender;

-(id) initObserverForAction:(NSString*)action
                  andSender:(id)notificationSender;

-(void) onNotification:(NSNotification*)notification;
-(NSDictionary*) waitForData;

@end

@implementation MTSimpleObserver

-(id) initObserverForAction:(NSString*)action
                  andSender:(id)notificationSender {
    if (![super init]) {
        return nil;
    }
    data_ = nil;
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(onNotification:)
                                                 name:action
                                               object:notificationSender];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(onPageLoad:)
                                                 name:@"webdriver:pageLoad"
                                               object:notificationSender];
    return self;
}



+(MTSimpleObserver*) simpleObserverForAction:(NSString*) action
                                   andSender:(id)sender {
    return [[MTSimpleObserver alloc] initObserverForAction:action
                                                  andSender:sender];
}

-(void) onPageLoad:(NSNotification*)notification {
    @synchronized(self) {
        [[NSNotificationCenter defaultCenter] removeObserver:self];
        NSLog(@"[MTSimpleObserver onPageLoad:]");
        NSDictionary* value = [NSDictionary
                               dictionaryWithObject:@"Page load detected; async scripts did not work "
                               "across page loads"
                               forKey:@"message"];
        data_ = [NSDictionary dictionaryWithObjectsAndKeys:
                 [NSNumber numberWithInt:EUNHANDLEDERROR], @"status",
                 value, @"value",
                 nil];
    }
}

-(void) onNotification:(NSNotification*)notification {
    @synchronized(self) {
        NSLog(@"[MTSimpleObserver onNotification:]");
        [[NSNotificationCenter defaultCenter] removeObserver:self];
        data_ = [notification userInfo];
    }
}

-(NSDictionary*) waitForData {
    while (true) {
        @synchronized(self) {
            if (data_ != nil) {
                // If data_ is not nil, then we've already removed ourselves as an
                // observer.
                return data_;
            }
        }
        [NSThread sleepForTimeInterval:0.25];
    }
}

@end


@implementation MTHTTPVirtualDirectory (ExecuteScript)

-(id) executeAtom:(const char* const[])atom
         withArgs:(NSArray*) args {
    std::string compiled("");
    for (size_t i = 0; atom[i] != NULL; i++) {
        compiled.append(atom[i]);
    }
    
    BOOL searching = NO;
    
    if (MTwebdriver::MTatoms::asString(atom) == MTwebdriver::MTatoms::asString(MTwebdriver::MTatoms::FIND_ELEMENT)) {
        searching = YES;
    }
    
    __block id executed = nil;
    
    //dispatch_async(dispatch_get_main_queue(), ^{
        executed = [self executeJsFunction:[NSString stringWithCString:compiled.c_str() encoding:NSUTF8StringEncoding]
                                  withArgs:args isFinding:searching];
    //});
    
    //    if (!executed)
    //    {
    //        MTWebViewController *webViewController = (MTWebViewController *)[self viewController];
    //        [webViewController runAgain];
    //    }
    
	return executed;
}

-(id) verifyResult:(NSDictionary*)resultDict {
    int status = [(NSNumber*) [resultDict objectForKey:@"status"] intValue];
    if (status != SUCCESS) {
        //    NSDictionary* value = (NSDictionary*) [resultDict objectForKey:@"value"];
        //    NSString* message = (NSString*) [value objectForKey:@"message"];
        //    @throw [NSException webDriverExceptionWithMessage:message
        //                                        andStatusCode:status];
        //      NSLog(@"error: %@",resultDict);
        return nil;
    } else {
        //MTWebViewController *MTWebViewController = (MTWebViewController *)[self viewController];
        //[[self viewController] runNextCommand];
    }
    return [resultDict objectForKey:@"value"];
}

-(id) executeScript:(NSString*)script
           withArgs:(NSArray*)args isFinding:(BOOL)finding {
    std::string compiled("");
    for (size_t i = 0; MTwebdriver::MTatoms::EXECUTE_SCRIPT[i] != NULL; i++) {
        compiled.append(MTwebdriver::MTatoms::EXECUTE_SCRIPT[i]);
    }
    
    MTWebViewController *webViewController = (MTWebViewController *)[self viewController];
	
    NSString* result = [webViewController jsEval:@"(%@)(%@,%@,true)",
                        [NSString stringWithCString:compiled.c_str() encoding:NSUTF8StringEncoding],
                        script,
                        [args JSONRepresentation]];
    
    NSDictionary* resultDict = (NSDictionary*) [result JSONValue];

    BOOL notFound = (finding && [[resultDict objectForKey:@"value"] isKindOfClass:[NSNull class]]);
    BOOL notInFrame = (!finding && [[resultDict objectForKey:@"status"] integerValue] == 10);
    
    if (notFound || notInFrame) {
        NSInteger iframeCount = [[webViewController.webView stringByEvaluatingJavaScriptFromString:@"document.getElementsByTagName('iframe').length"] integerValue];
        
        if (iframeCount > 0) {
            for (int i = 0; i < iframeCount; i++) {
                result = [webViewController evalFor:i jsEval:@"(%@)(%@,%@,true)",
                          [NSString stringWithCString:compiled.c_str() encoding:NSUTF8StringEncoding],
                          script,
                          [args JSONRepresentation]];
                
                resultDict = (NSDictionary*) [result JSONValue];
                notFound = ([[resultDict objectForKey:@"value"] isKindOfClass:[NSNull class]]);
                notInFrame = ([[resultDict objectForKey:@"status"] integerValue] == 10);
                
                if (finding && !notFound)
                    break;
                else if (!finding && !notInFrame)
                    break;
            }
        }
    }
    
    return [self verifyResult:resultDict];
}

-(id) executeScript:(NSString*)script
           withArgs:(NSArray*)args {
    return [self executeScript:script withArgs:args isFinding:NO];
}

-(id) executeJsFunction:(NSString*)script
               withArgs:(NSArray*)args isFinding:(BOOL)finding {
    return [self executeScript:script withArgs:args isFinding:finding];
}

-(id) executeJsFunction:(NSString*)script
               withArgs:(NSArray*)args {
    return [self executeJsFunction:script withArgs:args isFinding:NO];
}

-(id) executeAsyncJsFunction:(NSString*)script
                    withArgs:(NSArray*)args
                 withTimeout:(NSTimeInterval)timeout {
    // The |MTWebViewController| will broadcast a |webdriver:executeAsyncScript|
    // notification when the web view tries to load a URL of the form:
    // webdriver://executeAsyncScript?query.
    // The |EXECUTE_ASYNC_SCRIPT| loads this URL to notify that it has finished,
    // encoding its response in the query string.
    MTSimpleObserver* observer =
    [MTSimpleObserver simpleObserverForAction:@"webdriver:executeAsyncScript"
                                    andSender:[self viewController]];
    
    std::string compiled("");
    for (size_t i = 0; MTwebdriver::MTatoms::EXECUTE_ASYNC_SCRIPT[i] != NULL; i++) {
        compiled.append(MTwebdriver::MTatoms::EXECUTE_ASYNC_SCRIPT[i]);
    }
	
    [[self viewController] jsEval:@"(%@)(function(){%@\n},%@,%@)",
     [NSString stringWithCString:compiled.c_str() encoding:NSUTF8StringEncoding],
     script,
     [args JSONRepresentation],
     [NSNumber numberWithDouble:timeout * 1000]];
    
    NSDictionary* resultDict = [observer waitForData];
    
    //  NSLog(@"Got result: %@", [resultDict JSONRepresentation]);
    return [self verifyResult:resultDict];
}

@end
