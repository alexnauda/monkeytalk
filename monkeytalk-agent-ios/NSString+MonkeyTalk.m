//
//  NSString+MonkeyTalk.m
//  MonkeyTalk
//
//  Created by Kyle Balogh on 3/27/12.
//  Copyright 2012 Gorilla Logic, Inc. All rights reserved.
//

#import "NSString+MonkeyTalk.h"


@implementation NSString (MonkeyTalk)
- (BOOL)isEqualToString:(NSString *)aString ignoreCase:(BOOL)ignore {
    if (ignore)
        return [[self lowercaseString] isEqualToString:[aString lowercaseString]];
    
    return [self isEqualToString:aString];
}

+ (NSString *) pluralStringFor:(NSString *)string withCount:(NSInteger)value {
    NSString *pluralString = string;
    
    if (value > 1)
        pluralString = [pluralString stringByAppendingFormat:@"s"];
    
    return pluralString;
}
@end
