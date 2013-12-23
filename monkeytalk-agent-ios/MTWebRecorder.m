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

#import "MTWebRecorder.h"
#import "MonkeyTalk.h"
#import "MTCommandEvent.h"
#import "MTWebJs.h"

@implementation MTWebRecorder

-(NSString *) webRecorderJSScript
{
    NSString *jsScript = MTWebJsString;
    
    return jsScript;
}



-(void) jsMessageKey:(NSString *)val
{
    NSArray *splitValues = [val componentsSeparatedByString:@";"];
    
    elementPair = [[NSMutableDictionary alloc]init];
    
    for (NSString *element in splitValues) {
        NSArray *tempArray = [element componentsSeparatedByString:@"*"];
        [elementPair setObject:[tempArray objectAtIndex:1] forKey:[tempArray objectAtIndex:0]];
    }
    
    if ([[elementPair objectForKey:@"Args"] length] == 0) {
        [MonkeyTalk recordWebComponents:[elementPair objectForKey:@"ComponentType"]
                               monkeyID:[elementPair objectForKey:@"MonkeyId"]
                                command:[elementPair objectForKey:@"Action"]
                                   args:nil];
    }
    else
    {
        NSLog(@"Inside else condition");
        [MonkeyTalk recordWebComponents:[elementPair objectForKey:@"ComponentType"]
                           monkeyID:[elementPair objectForKey:@"MonkeyId"]
                            command:[elementPair objectForKey:@"Action"]
                               args:[NSArray arrayWithObject:[NSString stringWithFormat:@"%@", [elementPair objectForKey:@"Args" ]]]];
    }
}


@end