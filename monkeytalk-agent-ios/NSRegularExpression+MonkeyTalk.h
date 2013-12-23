//
//  NSRegularExpression+MonkeyTalk.h
//  MonkeyTalk
//
//  Created by Kyle Balogh on 9/9/13.
//  Copyright (c) 2013 Gorilla Logic, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSRegularExpression (MonkeyTalk)
- (BOOL)foundMatchInString:(NSString *)matchString;
@end
