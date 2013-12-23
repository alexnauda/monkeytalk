//
//  MTHTTPStaticResource.m
//  iWebDriver
//
//  Copyright 2009 Google Inc.
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

#import "MTHTTPStaticResource.h"
#import "MTHTTPRedirectResponse.h"

@implementation MTHTTPStaticResource

@synthesize response;

- (id)initWithResponse:(id<MTHTTPResponse,NSObject>)theResponse
{
	if (![super init])
		return nil;
	
	[self setResponse:theResponse];
	
	return self;
}


+ (MTHTTPStaticResource *)resourceWithResponse:(id<MTHTTPResponse,NSObject>)theResponse
{
	return [[self alloc] initWithResponse:theResponse];
}

+ (MTHTTPStaticResource *)redirectWithURL:(NSString *)url
{
	return [self resourceWithResponse:[MTHTTPRedirectResponse redirectToURL:url]];
}

// Get the HTTP response to this request
- (id<MTHTTPResponse,NSObject>)httpResponseForQuery:(NSString *)query
										  method:(NSString *)method
										withData:(NSData *)theData
{
	return response;
}

- (id<MTHTTPResource>)elementWithQuery:(NSString *)query
{
	return self;
}

@end
