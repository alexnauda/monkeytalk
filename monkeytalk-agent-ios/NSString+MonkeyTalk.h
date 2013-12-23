//
//  NSString+MonkeyTalk.h
//  MonkeyTalk
//
//  Created by Kyle Balogh on 3/27/12.
//  Copyright 2012 Gorilla Logic, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface NSString (MonkeyTalk)
- (BOOL)isEqualToString:(NSString *)aString ignoreCase:(BOOL)ignore;
+ (NSString *) pluralStringFor:(NSString *)string withCount:(NSInteger)value;
@end
