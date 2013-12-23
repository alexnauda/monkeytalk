//
//  MTElement.m
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


#import "MTElement.h"
#import "MTJSONRESTResource.h"
#import "MTHTTPRedirectResponse.h"
#import "MTElementStore.h"
#import "MTWebDriverResource.h"
#import "MTHTTPVirtualDirectory+ExecuteScript.h"
#import "MTHTTPVirtualDirectory+FindElement.h"
#import "MTAttribute.h"
#import "MTCss.h"
//#import "MainViewController.h"
#import "NSString+SBJSONMT.h"
#import "NSException+MTWebDriver.h"
#import "MTSession.h"
#include "MTatoms.h"
#import "MTerrorcodes.h"

static NSString* const kElementIdKey = @"ELEMENT";

@implementation MTElement

@synthesize elementId = elementId_;
@synthesize commandId = commandId_;
@synthesize session = session_;
@synthesize MTWebViewController = MTWebViewController_;

- (id)initWithId:(NSString *)elementId
      andSession:(MTSession*)session
        andMTWebViewController:(MTWebViewController *)webVc
andCommandId:(NSString *)commandId{
  if (![super init]) {
    return nil;
  }
  
  elementId_ = [elementId copy];
    commandId_ = [commandId copy];
  session_ = session;
  MTWebViewController_ = webVc;
  
  [self setResource:[MTWebDriverResource
                        resourceWithTarget:self
                                 GETAction:NULL
                                POSTAction:@selector(findElement:)]
              withName:@"element"];

  [self setResource:[MTWebDriverResource
                        resourceWithTarget:self
                                 GETAction:NULL
                                POSTAction:@selector(findElements:)]
              withName:@"elements"];

  [self setMyWebDriverHandlerWithGETAction:NULL
                                POSTAction:@selector(click:)
                                  withName:@"click"];
//  [self setMyWebDriverHandlerWithGETAction:NULL
//                                POSTAction:@selector(clickSimulate:)
//                                  withName:@"click"];
  
  [self setMyWebDriverHandlerWithGETAction:NULL
                                POSTAction:@selector(clear:)
                                  withName:@"clear"];
  
  [self setMyWebDriverHandlerWithGETAction:NULL
                                POSTAction:@selector(submit:)
                                  withName:@"submit"];
  
  [self setMyWebDriverHandlerWithGETAction:@selector(text)
                                POSTAction:NULL
                                  withName:@"text"];
  
  [self setMyWebDriverHandlerWithGETAction:@selector(value)
                                POSTAction:@selector(sendKeys:)
                                  withName:@"value"];
  
  [self setMyWebDriverHandlerWithGETAction:@selector(isChecked)
                                POSTAction:@selector(setChecked:)
                                  withName:@"selected"];
  
  [self setMyWebDriverHandlerWithGETAction:@selector(isEnabled)
                                POSTAction:NULL
                                  withName:@"enabled"];
  
  [self setMyWebDriverHandlerWithGETAction:@selector(isDisplayed)
                                POSTAction:NULL
                                  withName:@"displayed"];
  
  [self setMyWebDriverHandlerWithGETAction:@selector(locationAsDictionary)
                                POSTAction:NULL
                                  withName:@"location"];

  [self setMyWebDriverHandlerWithGETAction:@selector(sizeAsDictionary)
                                POSTAction:NULL
                                  withName:@"size"];
  
  [self setMyWebDriverHandlerWithGETAction:@selector(name) 
                                POSTAction:NULL
                                  withName:@"name"];
    
  [self setResource:[MTAttribute attributeDirectoryForElement:self]
           withName:@"attribute"];

  [self setResource:[MTCss cssDirectoryForElement:self]
           withName:@"css"];

  [self setResource:[MTElementComparatorBridge comparatorBridgeFor:self]
           withName:@"equals"];
  
  return self;
}


+ (MTElement *)elementWithId:(NSString *)elementId
                andSession:(MTSession*)session
andMTWebViewController:(MTWebViewController *)webVc
andCommandId:(NSString *)commandId{
  return [[MTElement alloc] initWithId:elementId
                           andSession:session
           andMTWebViewController:webVc andCommandId:commandId];
}

- (NSDictionary *)idDictionary {
  return [NSDictionary dictionaryWithObjectsAndKeys:
          [self elementId], kElementIdKey,
          nil];
}

#pragma mark Webdriver methods

-(NSDictionary*) findElement:(NSDictionary*)query {
  return [self findElement:query
                      root:[self idDictionary]
            implicitlyWait:[session_ implicitWait]];
}

-(NSArray*) findElements:(NSDictionary*)query {
  return [self findElements:query
                       root:[self idDictionary]
             implicitlyWait:[session_ implicitWait]];
}

- (void)click:(NSDictionary*)ignored {
   [self executeAtom:MTwebdriver::MTatoms::CLICK
           withArgs:[NSArray arrayWithObject:[self idDictionary]]];
    
//    if ([commandId_ isEqualToString:@"xpath"])
//        [MTWebViewController_ runNextCommand];
}

- (void)clickAndWait:(NSDictionary*)ignored
{
    [self click:ignored];
}

// This returns the pixel position of the element on the page in page
// coordinates.
- (CGPoint)location {
  NSDictionary* result = (NSDictionary*)
      [self executeAtom:MTwebdriver::MTatoms::GET_LOCATION
               withArgs:[NSArray arrayWithObject:[self idDictionary]]];

  NSNumber* x = [result objectForKey:@"x"];
  NSNumber* y = [result objectForKey:@"y"];

  return CGPointMake([x floatValue], [y floatValue]);
}

// Fetches the size of the element in page coordinates.
- (CGSize)size {
  NSDictionary* result = (NSDictionary*) [self
      executeJsFunction:@"function(){return {width:arguments[0].offsetWidth,"
                         "height:arguments[0].offsetHeight};}"
               withArgs:[NSArray arrayWithObject:[self idDictionary]]];

  NSNumber* width = [result objectForKey:@"width"];
  NSNumber* height = [result objectForKey:@"height"];

  return CGSizeMake([width floatValue], [height floatValue]);
}

// Fetches the bounds of the element in page coordinates. This is built from
// |size| and |location|.
- (CGRect)bounds {
  CGRect bounds;
  bounds.origin = [self location];
  bounds.size = [self size];
  return bounds;
}

- (void)clickSimulate:(NSDictionary *)dict {
//  CGRect currentPosition = [self bounds];
//  CGPoint midpoint = CGPointMake(CGRectGetMidX(currentPosition),
//                                 CGRectGetMidY(currentPosition));
    //[[MTWebViewController sharedInstance] selenium];
//   clickOnPageElementAt:midpoint];
}

- (void)clear:(NSDictionary*)ignored {
  [self executeAtom:MTwebdriver::MTatoms::CLEAR
           withArgs:[NSArray arrayWithObject:[self idDictionary]]];
}

- (void)submit:(NSDictionary*)ignored {
  [self executeAtom:MTwebdriver::MTatoms::SUBMIT
           withArgs:[NSArray arrayWithObject:[self idDictionary]]];
}

- (NSString *)text {
  return (NSString*) [self
      executeAtom:MTwebdriver::MTatoms::GET_TEXT
         withArgs:[NSArray arrayWithObject:[self idDictionary]]];
}

- (void)sendKeys:(NSDictionary *)dict {
//  NSString* stringToType =
//      [[dict objectForKey:@"value"] componentsJoinedByString:@""];
    
    NSString* stringToType = [dict objectForKey:@"value"];
    
  [self executeAtom:MTwebdriver::MTatoms::TYPE
           withArgs:[NSArray arrayWithObjects:[self idDictionary],
                                              stringToType, nil]];
    
    //[MTWebViewController_ runNextCommand];
}

- (void) type:(NSDictionary *)dict {
    // Clear field
    [self clear:nil];
    NSString *type = [self attribute:@"type"];
    
    // Enter text
    if (type && [type.lowercaseString isEqualToString:@"number"]) {
        // fix for number input playing back in incorrect order
        [self setValue:[dict objectForKey:@"value"]];
    } else {
        [self sendKeys:dict];
    }
}

- (void)setValue:(NSString *)val {
    [self
                        executeJsFunction:[NSString stringWithFormat:@"function(){arguments[0].value = %@}",val]
                        withArgs:[NSArray arrayWithObject:[self idDictionary]]];
}

- (NSString *)value {
  return (NSString*) [self
      executeJsFunction:@"function(){return arguments[0].value;}"
               withArgs:[NSArray arrayWithObject:[self idDictionary]]];
}

// This method is only valid on option elements, checkboxes and radio buttons.
- (NSNumber *)isChecked {
  return (NSNumber*) [self
      executeAtom:MTwebdriver::MTatoms::IS_SELECTED
         withArgs:[NSArray arrayWithObject:[self idDictionary]]];
}

// This method is only valid on option elements, checkboxes and radio buttons.
- (void)setChecked:(NSDictionary*)ignored {
  [self executeAtom:MTwebdriver::MTatoms::SET_SELECTED
           withArgs:[NSArray arrayWithObjects:
               [self idDictionary], [NSNumber numberWithBool:YES], nil]];
}

// Like |checked| above, we should check that the element is valid.
- (void)toggleSelected {
  [self executeAtom:MTwebdriver::MTatoms::TOGGLE
           withArgs:[NSArray arrayWithObject:[self idDictionary]]];
}

- (NSNumber *)isEnabled {
  return (NSNumber*) [self
      executeAtom:MTwebdriver::MTatoms::IS_ENABLED
         withArgs:[NSArray arrayWithObject:[self idDictionary]]];
}

- (NSNumber *)isDisplayed {
  return (NSNumber*) [self
      executeAtom:MTwebdriver::MTatoms::IS_DISPLAYED
         withArgs:[NSArray arrayWithObject:[self idDictionary]]];
}

- (NSDictionary *)locationAsDictionary {
  CGPoint location = [self location];
  return [NSDictionary dictionaryWithObjectsAndKeys:
          [NSNumber numberWithFloat:location.x], @"x",
          [NSNumber numberWithFloat:location.y], @"y",
          nil];
}

- (NSDictionary *)sizeAsDictionary {
  CGSize size = [self size];
  return [NSDictionary dictionaryWithObjectsAndKeys:
          [NSNumber numberWithFloat:size.width], @"width",
          [NSNumber numberWithFloat:size.height], @"height",
          nil];  
}

// Get an attribute with the given name.
-(id) attribute:(NSString *)name {
  return [self executeAtom:MTwebdriver::MTatoms::GET_ATTRIBUTE
                  withArgs:[NSArray arrayWithObjects:
                      [self idDictionary], name, nil]];
}

-(NSString *) css:(NSString *)property {
  return [self executeAtom:MTwebdriver::MTatoms::GET_EFFECTIVE_STYLE
                  withArgs:[NSArray arrayWithObjects:
                      [self idDictionary], property, nil]];
}

// Get the tag name of this element, not the value of the name attribute:
// will return "input" for the element <input name="foo">
- (NSString *)name {
  NSString* name = [self
      executeJsFunction:@"function(){return arguments[0].tagName;}"
               withArgs:[NSArray arrayWithObject:[self idDictionary]]];
  return [name lowercaseString];
}

@end

@implementation MTElementComparatorBridge

@synthesize element = element_;

- (id) initFor:(MTElement*)element {
  if (![super init]) {
    return nil;
  }
  element_ = element;
  return self;
}

+ (MTElementComparatorBridge*) comparatorBridgeFor:(MTElement*)element {
  return [[MTElementComparatorBridge alloc] initFor:element];
}

// Configures a temporary directory to handle /element/:elementId/equals/:other.
// The directory will remove itself after a singel request.
- (id<MTHTTPResource>)elementWithQuery:(NSString *)query {
  if ([query length] > 0) {
    NSString* otherId = [query substringFromIndex:1];
    id<MTHTTPResource> resource = [contents objectForKey:otherId];
    if (resource == nil) {
      NSLog(@"Adding comparator for element %@", otherId);
      NSDictionary* idDict = [NSDictionary dictionaryWithObject:otherId
                                                         forKey:kElementIdKey];
      resource = [[MTElementComparator alloc] initFor:self
                                        compareWith:idDict];
    }
  }
  return [super elementWithQuery:query];
}

@end

@implementation MTElementComparator

- (id) initFor:(MTElementComparatorBridge*)parentDirectory
   compareWith:(NSDictionary*)otherElementId {
  if (![super init]) {
    return nil;
  }
  parentDirectory_ = parentDirectory;
  otherElementId_ = otherElementId;
  
  [parentDirectory_ setResource:[MTWebDriverResource
                                 resourceWithTarget:self
                                          GETAction:@selector(compareElements)
                                         POSTAction:NULL]
                       withName:[otherElementId objectForKey:kElementIdKey]];
  return self;
}


- (id) compareElements {
  id result;
  @try {
    NSArray* args = [NSArray arrayWithObjects:
        [[parentDirectory_ element] idDictionary], otherElementId_, nil];
    result = [self executeJsFunction:
        @"function(){return arguments[0]==arguments[1];}"
                          withArgs:args];
  } @finally {
    // This is a one shot directory; remove ourselves from the parent directory.
    NSLog(@"Removing comparator tmp directory");
    [parentDirectory_ removeResourceWithName:
        [otherElementId_ objectForKey:kElementIdKey]];
  }
  return result;
}

@end

