//
//  MTCookie.m
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

#import "MTCookie.h"
#import "MTHTTPStaticResource.h"
#import "MTHTTPVirtualDirectory+AccessViewController.h"
#import "NSException+MTWebDriver.h"
#import "NSString+SBJSONMT.h"
#import "MTWebDriverResource.h"
#import "MTWebDriverResponse.h"
#import "MTWebViewController.h"
#import "MTerrorcodes.h"

@implementation MTCookie

- (id) initWithSessionId:(int)sessionId {
  self = [super init];
  if (!self) {
    return nil;
  }
  sessionId_ = sessionId;
  
  [self setIndex:
   [MTWebDriverResource resourceWithTarget:self
                               GETAction:@selector(getCookies)
                              POSTAction:@selector(addCookie:)
                               PUTAction:NULL
                            DELETEAction:@selector(deleteAllCookies)]];
  
  return self;
}

+ (MTCookie*) cookieWithSessionId:(int)sessionId {
  return [[MTCookie alloc] initWithSessionId:sessionId];
}

- (NSURL *)currentUrl {
  return [NSURL URLWithString:[[self viewController] URL]];
}

- (void) deleteAllCookies {
  NSHTTPCookieStorage* cookieStorage =
      [NSHTTPCookieStorage sharedHTTPCookieStorage];
  NSArray* theCookies = [cookieStorage cookiesForURL:[self currentUrl]];
  for (NSHTTPCookie *cookie in theCookies) {
    [cookieStorage deleteCookie:cookie];
  }
}

- (NSArray*) getCookies {
  NSHTTPCookieStorage* cookieStorage =
      [NSHTTPCookieStorage sharedHTTPCookieStorage];
  NSArray* theCookies = [cookieStorage cookiesForURL:[self currentUrl]];
  NSMutableArray* toReturn = [NSMutableArray arrayWithCapacity:
                              [theCookies count]];
  for (NSHTTPCookie *cookie in theCookies) {
    NSMutableDictionary* cookieDict =
        [NSMutableDictionary dictionaryWithObjectsAndKeys:
         [cookie name], @"name",
         [cookie value], @"value",
         [cookie domain], @"domain",
         [cookie path], @"path",
         [NSNumber numberWithBool:[cookie isSecure]], @"secure",
         nil];
    
    NSDate* expires = [cookie expiresDate];
    if ([expires isKindOfClass:[NSDate class]]) {
      NSNumber* expiry =
          [NSNumber numberWithLong:[expires timeIntervalSince1970]];
      [cookieDict setObject:expiry forKey:@"expiry"];
    }
    [toReturn addObject:cookieDict];
  }
  return toReturn;
}

- (void)addCookie:(NSDictionary *)cookie {
  cookie = [cookie objectForKey:@"cookie"];
  NSURL* currentUrl = [self currentUrl];
  NSString* domain = [cookie objectForKey:@"domain"];
  if (domain == (id)[NSNull null] || domain.length == 0) {
    domain = [currentUrl host];
  } else {
    // Strip off the port if the domain has one.
    domain = [[domain componentsSeparatedByString:@":"] objectAtIndex:0];
    if (![[currentUrl host] hasSuffix:domain]) {
      @throw [NSException webDriverExceptionWithMessage:
              [NSString stringWithFormat:
               @"You may only set cookies for the current domain:"
               " Expected <%@>, but was <%@>", [currentUrl host], domain]
                                          andStatusCode:EINVALIDCOOKIEDOMAIN];
    }
  }
  
  NSString* path = [cookie objectForKey:@"path"];
  if (path == (id)[NSNull null] || path.length == 0) {
    path = @"/";
  }
  // We need to convert the cookie data from the format used by WebDriver to one
  // recognized by the NSHTTPCookie class.
  NSMutableDictionary* cookieProperties =
      [NSMutableDictionary dictionaryWithObjectsAndKeys:
       [cookie objectForKey:@"name"], NSHTTPCookieName,
       [cookie objectForKey:@"value"], NSHTTPCookieValue,
       domain, NSHTTPCookieDomain,
       path, NSHTTPCookiePath,
       nil];
  
  NSNumber* expires = [cookie objectForKey:@"expiry"];
  if ([expires isKindOfClass:[NSNumber class]]) {
    NSDate* expiresDate =
        [NSDate dateWithTimeIntervalSince1970:[expires doubleValue]];
    [cookieProperties setObject:expiresDate forKey:NSHTTPCookieExpires];
  }
  
  NSNumber* secure = [cookie objectForKey:@"secure"];
  if ([secure boolValue] == YES) {
    [cookieProperties setObject:@"true" forKey:NSHTTPCookieSecure];
  }
  
  NSArray *cookieToAdd = [NSArray arrayWithObject:
                          [NSHTTPCookie cookieWithProperties:cookieProperties]];
  [[NSHTTPCookieStorage sharedHTTPCookieStorage]
   setCookies:cookieToAdd forURL:currentUrl mainDocumentURL:nil];
}

- (id<MTHTTPResource>)elementWithQuery:(NSString *)query {
  if ([query length] > 0) {
    NSString *cookieName = [query substringFromIndex:1];
    id<MTHTTPResource> resource = [contents objectForKey:cookieName];
    if (resource == nil) {
      [self setResource:[MTNamedCookie namedCookie:cookieName]
               withName:cookieName];
    }
  }
  return [super elementWithQuery:query];
}

@end

@implementation MTNamedCookie

- (id)initWithName:(NSString *)name {
  if (![super init]) {
    return nil;
  }
  name_ = name;
  [self setIndex:
   [MTWebDriverResource resourceWithTarget:self
                               GETAction:@selector(getCookie)
                              POSTAction:NULL
                               PUTAction:NULL
                            DELETEAction:@selector(deleteCookie)]];
  return self;
}

+ (MTNamedCookie *)namedCookie:(NSString *)name {
  return [[MTNamedCookie alloc] initWithName:name];
}

- (NSDictionary *)getCookie {
  NSHTTPCookieStorage* cookieStorage =
      [NSHTTPCookieStorage sharedHTTPCookieStorage];
  NSURL* currentUrl = [NSURL URLWithString:[[self viewController] URL]];
  NSArray* theCookies = [cookieStorage cookiesForURL:currentUrl];
  for (NSHTTPCookie *cookie in theCookies) {
    if ([[cookie name] isEqualToString:name_]) {
      NSMutableDictionary* cookieDict =
          [NSMutableDictionary dictionaryWithObjectsAndKeys:
           [cookie name], @"name",
           [cookie value], @"value",
           [cookie domain], @"domain",
           [cookie path], @"path",
           [NSNumber numberWithBool:[cookie isSecure]], @"secure",
           nil];
      NSDate* expires = [cookie expiresDate];
      if ([expires isKindOfClass:[NSDate class]]) {
        [cookieDict setObject:[expires description] forKey:@"expires"];
      }
      return cookieDict;
    }
  }
  return nil;
}

- (void)deleteCookie {
  NSHTTPCookieStorage* cookieStorage =
      [NSHTTPCookieStorage sharedHTTPCookieStorage];
  NSURL* currentUrl = [NSURL URLWithString:[[self viewController] URL]];
  NSArray* theCookies = [cookieStorage cookiesForURL:currentUrl];
  for (NSHTTPCookie *cookie in theCookies) {
    if ([[cookie name] isEqualToString:name_]) {
      [cookieStorage deleteCookie:cookie];
      break;
    }
  }
}

@end
