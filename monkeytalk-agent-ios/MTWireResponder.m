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

#import "MTWireResponder.h"
#import "MTBuildStamp.h"
#import "MonkeyTalk.h"
#import "MTCommandEvent.h"
#import "MTConvertType.h"
#import "SBJSONMT.h"
#import "SBJsonWriterMT.h"
#import "NSString+SBJSONMT.h"
#import "MTWireKeys.h"
#import "MTUtils.h"
#import "MTComponentTree.h"
#import "MTWireSender.h"

@implementation MTWireResponder
MonkeyTalk* theMonkey;

+ (NSDictionary *) replaceMTOrdinals:(NSDictionary *)dict {
    
    Boolean isVerifyAction = [self isMTCommandHasVerifyAction:dict];
    
    for (NSString *key in [dict allKeys]) {
        NSString *value = [dict objectForKey:key];
        
        Boolean isFirstMonkeyOrdinal = NO;
        NSRange monkeyOrdinalRange;
        
        if ([value respondsToSelector:@selector(rangeOfString:)]) {
            monkeyOrdinalRange = [value rangeOfString:@"(1)"];
            isFirstMonkeyOrdinal = [value respondsToSelector:@selector(isEqualToString:)] &&
            monkeyOrdinalRange.location != NSNotFound &&
            ![key isEqualToString:MTWireArgsKey] && (monkeyOrdinalRange.location + monkeyOrdinalRange.length == value.length);
        }
        
        Boolean isFirstIndex = [value respondsToSelector:@selector(isEqualToString:)] &&
        [value isEqualToString:@"*"] && !isVerifyAction &&
        ![key isEqualToString:MTWireArgsKey];
        
        if (isFirstIndex)
            [dict setValue:@"#1" forKey:key];
        if (isFirstMonkeyOrdinal)
            [dict setValue:[value stringByReplacingCharactersInRange:monkeyOrdinalRange withString:@""] forKey:key];
    }
    
    return dict;
}

// Boolean method which returns YES if MTCommand has verify action or else returns NO
// Argument - NSDictionary

+(Boolean) isMTCommandHasVerifyAction :(NSDictionary *) dict {
    
    NSString *value = [[dict objectForKey:@"action"] lowercaseString];
    if ([value isEqualToString:@"verify"] || [value isEqualToString:@"verifynot"]
        || [value isEqualToString:@"verifyregex"] || [value isEqualToString:@"verifynotregex"] || [value isEqualToString:@"verifywildcard"] || [value isEqualToString:@"verifynotwildcard"]) {
        return  YES;
    }
    else
        return NO;
}

#pragma mark - Handle/Respond to JSON
+ (NSObject<MTHTTPResponse> *) wireResponseFromQuery:(NSString *)query
                                            withData:(NSData *)data {
    if ([query isEqualToString:@"/screenshot"]) {
        return [self screenshotResponse];
    }
    
    NSString *jsonString = [[NSString alloc]
                             initWithData:data
                             encoding:NSUTF8StringEncoding];
    
    NSDictionary *jsonDictionary = [jsonString JSONValue];
    NSString *command = [jsonDictionary objectForKey:MTWireCommandKey];
    NSDictionary *modifiers = (NSDictionary *)[jsonDictionary objectForKey:MTWireModifiersKey];
    
    id<MTHTTPResponse,NSObject> response = nil;
    NSString *postString = nil;
    NSData *postData = nil;
    
    // Log string coming across wire
    //    NSLog(@"data: '%@'", dataString);
    
    if ([command isEqualToString:MTWireCommandDumpTree]) {
        SBJsonWriterMT *jsonWriter = [[SBJsonWriterMT alloc] init];
        NSMutableDictionary *jsonDict = [[NSMutableDictionary alloc] init];
        [jsonDict setValue:@"OK" forKey:@"result"];
        
        NSString *val = [modifiers objectForKey:MTWireSkipWebView];
        BOOL skip = (val ? [val caseInsensitiveCompare:@"true"] == NSOrderedSame : NO);
        __block BOOL foundComponents = NO;
        
        // do tree dump on main thread
        dispatch_async(dispatch_get_main_queue(), ^{
            NSArray *tree = [[NSArray alloc] initWithArray:[MTComponentTree componentTree:skip]];
            [jsonDict setValue:[tree objectAtIndex:0] forKey:@"message"];
            foundComponents = YES;
        });
        
        // wait until the component tree has been added to message
        while (!foundComponents)
            usleep(500);
        
        postString = [jsonWriter stringWithObject:jsonDict];
        
        postData = [postString
                    dataUsingEncoding:NSUTF8StringEncoding
                    allowLossyConversion:YES];
        
        response = [[MTHTTPDataResponse alloc] initWithData:postData];
    } else if ([command isEqualToString:MTWireCommandPlay]) {
        SBJsonWriterMT *jsonWriter = [[SBJsonWriterMT alloc] init];
        NSMutableDictionary *jsonDict = [[NSMutableDictionary alloc] init];
        jsonDictionary = [[self class] replaceMTOrdinals:jsonDictionary];
        NSString *action = [jsonDictionary objectForKey:MTWireActionKey];
        NSString *type = [jsonDictionary objectForKey:MTWireComponentTypeKey];
        NSString *monkeyID = [jsonDictionary objectForKey:MTWireMonkeyIdKey];
        NSArray *args = (NSArray *)[jsonDictionary objectForKey:MTWireArgsKey];
        
        NSString *timeout = nil;
        NSString *thinkTime = nil;
        
        if (modifiers) {
            timeout = [modifiers objectForKey:MTWireTimeoutKey];
            thinkTime = [modifiers objectForKey:MTWireThinkTimeKey];
        }
        
        if (thinkTime == nil)
            thinkTime = [NSString stringWithFormat:@"%i",MT_DEFAULT_THINKTIME];
        if (timeout == nil)
            timeout = [NSString stringWithFormat:@"%i",MT_DEFAULT_TIMEOUT];
        
        // Set args to nil if there is no args coming across wire
        if ([args count] == 0)
            args = nil;
        
        // Convert the action/component to ObjC
        NSString *originalType = [NSString stringWithFormat:@"%@",type];
        type = [MTConvertType convertedComponentFromString:type isRecording:NO];
        
        // Play the event
        if (type) {
            postString = [[MonkeyTalk sharedMonkey] playAndRespond:[MTCommandEvent command:action component:originalType className:type monkeyID:monkeyID delay:thinkTime timeout:timeout args:args modifiers:modifiers]];
        } else if ([originalType isEqualToString:@"(null)"]) {
            // It's a comment — send back OK
            [jsonDict setValue:@"OK" forKey:@"result"];
            
            postString = [jsonWriter stringWithObject:jsonDict];
        } else {
            // It is a custom component
            postString = [[MonkeyTalk sharedMonkey] playAndRespond:[MTCommandEvent command:action component:originalType className:originalType monkeyID:monkeyID delay:thinkTime timeout:timeout args:args modifiers:modifiers]];
            
            // No longer fail if custom component is not prefixed with "."
            //            NSString *failure = [NSString stringWithFormat:@"%@ is not a MonkeyTalk component — prefix it with a \".\" to use as custom component (.%@)",
            //                                 originalType,originalType];
            //            SBJsonWriterMT *jsonWriter = [[SBJsonWriterMT alloc] init];
            //            NSMutableDictionary *jsonDict = [[NSMutableDictionary alloc] init];
            //            // Command failed
            //            [jsonDict setValue:@"FAILURE" forKey:@"result"];
            //            [jsonDict setValue:failure forKey:@"message"];
            //
            //            postString = [jsonWriter stringWithObject:jsonDict];
            //            NSLog(@"MonkeyTalk Script Failure: %@\n", failure);
        }
        
        
        postData = [postString
                    dataUsingEncoding:NSUTF8StringEncoding
                    allowLossyConversion:YES];
        
        response = [[MTHTTPDataResponse alloc] initWithData:postData];
    } else if ([command isEqualToString:MTWireCommandPing]) {
        
        SBJsonWriterMT *jsonWriter = [[SBJsonWriterMT alloc] init];
        NSMutableDictionary *jsonDict = [[NSMutableDictionary alloc] init];
        NSMutableDictionary *metadata = [[NSMutableDictionary alloc] init];
        
        // Add os to metadata
        [metadata setValue:MTWireMetadataOsValue forKey:MTWireMetadataOsKey];
        
        // Add pathToApp to metadata
        [metadata setValue:[MTUtils appDirectory] forKey:MTWireMetadataAppDirKey];
        
        // Add mtversion to metadata
        [metadata setValue:[MTBuildStamp versionInfo] forKey:MTWireMetadataVersionKey];
        
        NSString *recordString = [jsonDictionary objectForKey:[MTWireCommandRecord lowercaseString]];
        
        // Set isWireRecording flag based on record key
        if ([recordString isEqualToString:@"ON"]) {
            [MonkeyTalk sharedMonkey].recordHost = [jsonDictionary objectForKey:[MTWireRecordHost lowercaseString]];
            [MonkeyTalk sharedMonkey].recordPort = [jsonDictionary objectForKey:[MTWireRecordPort lowercaseString]];
            
            [MonkeyTalk sharedMonkey].isWireRecording = YES;
            [theMonkey record];
        } else {
            [MonkeyTalk sharedMonkey].isWireRecording = NO;
            [theMonkey pause];
        }
        
        // Add to response dictionary
        [jsonDict setValue:MTWireSuccessValue forKey:MTWireResultKey];
        [jsonDict setValue:metadata forKey:MTWireMessageKey];
        //        [jsonDict setValue:metadata forKey:MTWireMetadataKey];
        //        [jsonDict setValue:MTWireMetadataOsValue forKey:MTWireMetadataOsKey];
        
        // handle polled recording via ping
        if ([MonkeyTalk sharedMonkey].commandQueue != nil) {
            [jsonDict setObject:[MonkeyTalk sharedMonkey].commandQueue forKey:MTWireMessageKey];
            postString = [jsonWriter stringWithObject:jsonDict];
            [[MonkeyTalk sharedMonkey] emptyRecordQueue];
        } else {
            postString = [jsonWriter stringWithObject:jsonDict];
        }
        
        postData = [postString
                    dataUsingEncoding:NSUTF8StringEncoding
                    allowLossyConversion:YES];
        
        response = [[MTHTTPDataResponse alloc] initWithData:postData];
        
    }
    
    return response;
    //    return nil;
}

+ (NSObject<MTHTTPResponse> *)screenshotResponse {
    NSString *filePath = [NSHomeDirectory() stringByAppendingPathComponent:@"Documents/screenshot.png"];
    CGImageRef imageRef = UIGetScreenImage();
    UIImage *screenshot = [UIImage imageWithCGImage:imageRef];
    CFRelease(imageRef);
    NSData *imgData = UIImagePNGRepresentation(screenshot);
    
    // Write the file.  Choose YES atomically to enforce an all or none write. Use the NO flag if partially written files are okay which can occur in cases of corruption
    if (![imgData writeToFile:filePath atomically:YES]) {
        // something went wrong
    }
    
    BOOL isDir = NO;
    
    if (filePath && [[NSFileManager defaultManager] fileExistsAtPath:filePath isDirectory:&isDir] && !isDir) {
        return [[MTHTTPFileResponse alloc] initWithFilePath:filePath];
        
        // Use me instead for asynchronous file IO.
        // Generally better for larger files.
        
        //	return [[[HTTPAsyncFileResponse alloc] initWithFilePath:filePath forConnection:self] autorelease];
    }
    
    return nil;
}

@end
