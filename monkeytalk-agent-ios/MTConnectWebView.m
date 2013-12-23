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

#import "MTConnectWebView.h"
#import "MTUtils.h"
#import "MonkeyTalkAPI.h"
#import "MonkeyTalk.h"

@interface MTConnectWebView(Private)
- (void) jsFunction:(NSString *)function args:(NSArray *)args;
- (void) playCommands:(NSArray *)args;
- (void) getValue:(NSArray *)args;
- (void) writeResultsToXml:(NSArray *)args;
- (void) finishWritingXml:(NSArray *)args;
- (void) playCommand:(NSArray *)args;
@end

@implementation MTConnectWebView

@synthesize xmlString,xmlFileName,jsVariables;

- (id)initWithFrame:(CGRect)frame 
{
    if (self = [super initWithFrame:frame]) {
        
        group = dispatch_group_create();
        
        self.delegate = self;
        sbJson = [ SBJSONMT new ];
        NSString *script = @"";
        
        // Set script string to MT_ENABLE_QUNIT environment var
        if (getenv("MT_ENABLE_QUNIT"))
            script = [NSString stringWithUTF8String:getenv("MT_ENABLE_QUNIT")];
        
        // Build html path
        NSString *htmlPath = [[NSBundle mainBundle] pathForResource:@"MTObjConnect" ofType:@"html"];
        NSString *apiPath = [[NSBundle mainBundle] pathForResource:@"MTQunitAPI" ofType:@"jslib"];
        NSString *dirPath = [htmlPath stringByDeletingLastPathComponent];
        
        htmlPath = [htmlPath stringByAppendingFormat:@"?jsfile=%@",
                    [[MTUtils scriptsLocation] stringByAppendingPathComponent:script]];
        
        htmlPath = [htmlPath stringByAppendingFormat:@"&connectfile=%@",
                    [[NSBundle mainBundle] pathForResource:@"MTObjConnect" ofType:@"jslib"]];
        
        htmlPath = [htmlPath stringByAppendingFormat:@"&dirpath=%@", 
                    dirPath];
        
        htmlPath = [htmlPath stringByAppendingFormat:@"&apifile=%@", 
                    apiPath];
        
        NSURL *url = [NSURL URLWithString: [htmlPath stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding]];
        
        // Load url with script paths
        [self loadRequest:[NSURLRequest requestWithURL:url]];
        
    }
    return self;
}

- (void)webViewDidFinishLoad:(UIWebView *)webView
{
    self.scalesPageToFit = YES;
}

- (BOOL) webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType
{
    NSString *req = [[request URL] absoluteString];
    
    if ([req hasPrefix:@"objconnect:"]) {
        NSArray *parameters = [req componentsSeparatedByString:@":"];
        
        NSString *function = [NSString stringWithFormat:@"%@",[parameters objectAtIndex:1]];
        NSString *args = [NSString stringWithFormat:@"%@",[[parameters objectAtIndex:2] stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
        
        NSArray *argsArray = [NSArray arrayWithArray:[sbJson objectWithString:args error:nil]];
        
        [self jsFunction:function args:argsArray];
        
        return NO;
    }
    return YES;
}

- (void)qResult:(NSString *)result event:(MTCommandEvent *)commandEvent function:(NSString *)function
{
    if ([function isEqualToString:MTCommandDataDrive]) {
        [self returnFunction:function args:result,nil];
    } else if (![function isEqualToString:@"MTPlayCommands"]) {
        
        if (commandEvent.args && [commandEvent.args count] > 1 && [jsVariables objectForKey:[NSString stringWithFormat:@"${%@}",[commandEvent.args objectAtIndex:1]]])
            [self performSelectorOnMainThread:@selector(returnValueForCommand:) withObject:commandEvent waitUntilDone:YES];
    } else
    {
        [self returnFunction:function args:result,nil];
    }
}

- (void)returnFunction:(NSString *)function args:(id)argument, ...;
{
    if ([function length] == 0) return;
    
    va_list args;
    NSMutableArray *results = [[NSMutableArray alloc] init];
    
    if(argument != nil){
        [results addObject:argument];
        va_start(args, argument);
        while((argument = va_arg(args, id)) != nil)
            [results addObject:argument];
        va_end(args);
    }
    
    NSString * js = [NSString stringWithFormat:@"MTObjConnect.result('%@',%@);",function,[sbJson stringWithObject:results allowScalar:YES error:nil]];
    
    [self performSelectorOnMainThread:@selector(stringByEvaluatingJavaScriptFromString:) withObject:js waitUntilDone:NO];
    
}

- (void) jsFunction:(NSString *)function args:(NSArray *)args
{
    // Call Obj-C function relating to js
    if ([function isEqualToString:@"MTPlayCommands"])
    {
        [self playCommands:args];
    }
    else if ([function isEqualToString:@"MTGetValue"])
    {
        [self getValue:args];
    }
    else if ([function isEqualToString:@"MTWriteResults"])
    {
        [self writeResultsToXml:args];
    }
    else if ([function isEqualToString:@"MTWriteResultsDone"])
    {
        [self finishWritingXml:args];
    }
    else if ([function isEqualToString:@"MTPlayCommand"])
    {
        [self playCommand:args];
    }
    else {        
        // Do something if function doesn't exist
        
        NSLog(@"Called: %@ %@",function, args);
    }
}

- (void)returnValueForCommand:(MTCommandEvent *)commandEvent
{
    NSString* value = [MTGetVariableCommand execute:commandEvent];
    NSString* jsVariable = [commandEvent.args objectAtIndex:1];
    NSString *objectVariable = [NSString stringWithFormat:@"${%@}",jsVariable];
    
    // Set custom variable to property value
    [jsVariables setValue:value forKey:objectVariable];
    
    // Set variable in js
    [self performSelectorOnMainThread:@selector(stringByEvaluatingJavaScriptFromString:) withObject:[NSString stringWithFormat:@"var %@ = \"%@\";",jsVariable,value] waitUntilDone:YES];
    
    [self returnFunction:commandEvent.monkeyID args:value,nil];
}

- (void) playCommand:(NSArray *)args
{
    NSString *commandString = [args objectAtIndex:0];
    NSString *monkeyString = [args objectAtIndex:1];
    
    NSString *delayString = [NSString stringWithFormat:@"%i",MT_DEFAULT_THINKTIME];
    NSString *retryString = [NSString stringWithFormat:@"%i",MT_DEFAULT_TIMEOUT];
    
    NSMutableArray *allArgs = (NSMutableArray *)[args objectAtIndex:2];
    NSMutableArray *commandArgs = [[NSMutableArray alloc] init];
    
    for (int i = 0; i < [allArgs count]; i++) {
        NSString *argString = [allArgs objectAtIndex:i];
        
        if ([argString rangeOfString:@"retryDelay="].location != NSNotFound)
            delayString = argString;
        else if ([argString rangeOfString:@"timeout="].location != NSNotFound)
            retryString = argString;
        else
            [commandArgs addObject:argString];
    }
    
    if ([commandArgs count] == 0)
        commandArgs = nil;
    
    dispatch_group_async(group, dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        @synchronized(self) {
            while ([[MonkeyTalk sharedMonkey] state] == MTStatePlaying) {
                usleep(500);
            }
            
            [[MonkeyTalk sharedMonkey] playCommandFromJs:[MTCommandEvent command:commandString className:@"" monkeyID:monkeyString delay:delayString timeout:retryString args:commandArgs modifiers:nil]];
        }
    });
    
    dispatch_group_notify(group, dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        dispatch_async(dispatch_get_main_queue(), ^{
            // Playback complete
            usleep(500000); // When playback is done, wait a sec before dropping the curtain
            [[MonkeyTalk sharedMonkey] playingDone];
        }); 
    });
    
    
    
    
    
//    NSString *commandString = [args objectAtIndex:0];
//    NSString *monkeyString = [args objectAtIndex:1];
//    NSString *delayString = [args objectAtIndex:2];
//    NSString *retryString = [args objectAtIndex:3];
//    NSString *xString = [args objectAtIndex:4];
//    NSString *yString = [args objectAtIndex:5];
//    
//    dispatch_group_async(group, dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
//        @synchronized(self) {
//            while ([[MonkeyTalk sharedMonkey] state] == MTStatePlaying) {
//                usleep(500);
//            }
//            
//            [[MonkeyTalk sharedMonkey] playCommandFromJs:[MTCommandEvent command:commandString className:@"" monkeyID:monkeyString delay:delayString timeout:retryString args:[NSArray arrayWithObjects:xString, yString, nil]]];
//        }
//    });
//    
//    dispatch_group_notify(group, dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
//        dispatch_async(dispatch_get_main_queue(), ^{
//            // Playback complete
//            usleep(500000); // When playback is done, wait a sec before dropping the curtain
//            [[MonkeyTalk sharedMonkey] playingDone];
//        }); 
//    });
    
//    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
//        @synchronized(self) {
//            while ([[MonkeyTalk sharedMonkey] state] == MTStatePlaying) {
//                usleep(500);
//            }
//            
//            [[MonkeyTalk sharedMonkey] playCommandFromJs:[MTCommandEvent command:commandString className:@"" monkeyID:monkeyString delay:delayString timeout:retryString args:[NSArray arrayWithObjects:xString, yString, nil]]];
//        }
//    });
    
//    NSArray *array = [NSArray arrayWithObject:[MTCommandEvent command:commandString className:@"" monkeyID:monkeyString delay:delayString timeout:retryString args:[NSArray arrayWithObjects:xString, yString, nil]]];
//    
//    [MonkeyTalkAPI playCommands:array];
}

- (void) playCommands:(NSArray *)args
{
    NSMutableArray *commands = (NSMutableArray *)args;
    NSMutableArray* array = [[NSMutableArray alloc] initWithCapacity:[commands count]];
    
    for (int i = 0; i < [commands count]; i++) {
        
        NSString *command = [[commands objectAtIndex:i] objectAtIndex:0];
        NSString *className = [[commands objectAtIndex:i] objectAtIndex:1];
        NSString *monkeyId = [[commands objectAtIndex:i] objectAtIndex:2];
        
        NSString *playback = nil;
        NSString *timeout = nil;
        
        NSMutableArray *args = nil;
        
        if ([[commands objectAtIndex:i] count] == 4)
        {
            args = (NSMutableArray *)[[commands objectAtIndex:i] objectAtIndex:3];
        } else if ([[commands objectAtIndex:i] count] == 6)
        {
            playback = [[commands objectAtIndex:i] objectAtIndex:3];
            timeout = [[commands objectAtIndex:i] objectAtIndex:4];
            args = (NSMutableArray *)[[commands objectAtIndex:i] objectAtIndex:5];
        }
        
        // If args do not have any values, set to nil
        if ([[NSString stringWithFormat:@"%@", args] rangeOfString:@"null"].location != NSNotFound)
            args = nil;
        
        if (args && [command isEqualToString:MTCommandGetVariable])
            [self getValue:args];
        
        [array addObject:[MTCommandEvent command:command className:className monkeyID:monkeyId delay:playback timeout:timeout args:args modifiers:nil]];
    }
    
    [MonkeyTalkAPI playCommands:array];
    
}

- (void) getValue:(NSArray *)args
{
    NSString *variable = [NSString stringWithFormat:@"${%@}", [args objectAtIndex:1]];
    
    if (!jsVariables)
        jsVariables = [[NSMutableDictionary alloc] init];
    
    [jsVariables setValue:@"" forKey:variable];
}

- (void) writeResultsToXml:(NSArray *)args
{ 
    // While tests are running, populate xml string
    NSString *module = (NSString *)[args objectAtIndex:0];
    NSString *testCase = (NSString *)[args objectAtIndex:1];
    NSString *resultMessage = (NSString *)[args objectAtIndex:2];
    
    if ([self.xmlString length] == 0)
        self.xmlString = [NSString stringWithFormat:@"<?xml version=\"1.0\"?>%@",[MTUtils stringForQunitTest:testCase inModule:module withResult:resultMessage]];
    else
    {
        self.xmlString = [self.xmlString stringByAppendingString:[MTUtils stringForQunitTest:testCase inModule:module withResult:resultMessage]];
    }
}

- (void) finishWritingXml:(NSArray *)args
{ 
    // Write xml to file when test is completed
    NSString *testCount = (NSString *)[args objectAtIndex:0];
    NSString *failCount = (NSString *)[args objectAtIndex:1];
    NSString *runTime = (NSString *)[args objectAtIndex:2];
    
    if ([self.xmlFileName length] == 0)
        self.xmlFileName = [NSString stringWithFormat:@"MT_QUNIT_LOG-%@.xml", [MTUtils timeStamp]];
    
    [MTUtils writeQunitToXmlWithString:self.xmlString testCount:testCount failCount:failCount runTime:runTime fileName:self.xmlFileName];
}



@end
