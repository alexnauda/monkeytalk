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

#import "MTRESTServiceMapping+MTService.h"
#import "MTHTTPVirtualDirectory.h"
#import "MTHTTPRedirectResponse.h"
#import "SBJSONMT.h"
#import "SBJsonWriterMT.h"
#import "NSString+SBJSONMT.h"
#import "MonkeyTalk.h"
#import "MTCommandEvent.h"
#import "MTHTTPServerController.h"
#import "MTWireResponder.h"
#import "NSObject+MTReady.h"


@implementation MTRESTServiceMapping (MTService)

+ (void)load {
    if (self == [MTRESTServiceMapping class]) {
        [NSObject swizzle:@"httpResponseForRequest:" with:@"mthttpResponseForRequest:" for:self];
    }
}

// Send the request to the right MTHTTPResource and return its response.
- (NSObject<MTHTTPResponse> *)mthttpResponseForRequest:(CFHTTPMessageRef)request {
    
    NSString *query;
    NSString *method;
    NSData *data;
    
    [MTRESTServiceMapping propertiesOfHTTPMessage:request
                                        toQuery:&query
                                         method:&method
                                           data:&data];
    
    if (data) {
        return [MTWireResponder wireResponseFromQuery:query withData:data];
    }
    
    return nil;
}

@end
