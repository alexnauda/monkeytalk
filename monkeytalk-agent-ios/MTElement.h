//
//  MTElement.h
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

#import <Foundation/Foundation.h>
#import "MTHTTPVirtualDirectory.h"
#import "MTWebViewController.h"

@class MTSession;

// This represents a web element accessible via :session/element/X where X is an
// opaque ID assigned by the server when the element is first located on the
// page.
@interface MTElement : MTHTTPVirtualDirectory {
 @private
  // The opaque ID assigned by the server.
  NSString* elementId_;
    NSString *commandId_;
  MTSession* session_;
    MTWebViewController *MTWebViewController_;
    
}

@property (nonatomic, readonly, copy) NSString *elementId;
@property (nonatomic, readonly, copy) NSString *commandId;
@property (nonatomic, readonly, strong) MTSession *session;
@property (nonatomic, readonly, strong) MTWebViewController *MTWebViewController;

// Designated initializer. Don't call this directly - instead
// use |elementWithId|.
- (id)initWithId:(NSString *)elementId
      andSession:(MTSession*)session
andMTWebViewController:(MTWebViewController *)webVc
    andCommandId:(NSString *)commandId;

// Create a new element.
+ (MTElement *)elementWithId:(NSString *)elementId
                andSession:(MTSession*)session
      andMTWebViewController:(MTWebViewController *)webVc
              andCommandId:(NSString *)commandId;

// Get the JSON dictionary with this element's ID for transmission
// over the wire: |{"ELEMENT": "elementId"}|.
- (NSDictionary *)idDictionary;

// Locates the first element under this element that matches the given |query|.
// The |query| must have two keys:
// @li "using" - The locator strategy to use.
// @li "value" - The value to search for using the strategy.
// Returns the JSON representation of the located element.
-(NSDictionary*) findElement:(NSDictionary*)query;

// Locates every element on under this element matching the given |query|.
// The |query| must have two keys:
// @li "using" - The locator strategy to use.
// @li "value" - The value to search for using the strategy.
// Returns an array of elements in their JSON representation.
-(NSArray*) findElements:(NSDictionary*)query;

// Simulate a click on the element.
// Dictionary parameters are passed in by REST service, but are redundant
// with directory ID and are thus ignored.
- (void)click:(NSDictionary*)ignored;
- (void)clickAndWait:(NSDictionary*)ignored;

// Clear the contents of this input field.
// Dictionary parameters are passed in by REST service, but are redundant
// with directory ID and are thus ignored.
- (void)clear:(NSDictionary*)ignored;

// Submit this form, or the form containing this element.
// Dictionary parameters are passed in by REST service, but are redundant
// with directory ID and are thus ignored.
- (void)submit:(NSDictionary*)ignored;

// The text contained in the element.
- (NSString *)text;

// Type these keys into the element.
// Dictionary parameters are passed in by REST service, but are redundant
// with directory ID and are thus ignored.
- (void)sendKeys:(NSDictionary *)dict;
- (void)type:(NSDictionary *)dict;

// Is the element checked?
// This method is only valid on checkboxes and radio buttons.
- (NSNumber *)isChecked;

// Set the element's checked property.
// This method is only valid on checkboxes and radio buttons.
- (void)setChecked:(NSDictionary*)ignored;

// Toggle the element's checked property.
// This method is only valid on checkboxes and radio buttons.
// Dictionary parameters are passed in by REST service, but are redundant
// with directory ID and are thus ignored.
- (void)toggleSelected;

// Is the element enabled?
- (NSNumber *)isEnabled;

// Is the element displayed on the screen?
- (NSNumber *)isDisplayed;

// Get the attribute with the given name.
- (id)attribute:(NSString *)attributeName;

// Get the effective CSS property with the given name.
- (NSString*)css:(NSString*)property;

// Get the tag name of this element, not the value of the name attribute:
// will return "input" for the element <input name="foo">
- (NSString *)name;

- (CGPoint)location;
- (CGSize)size;
- (void)setValue:(NSString *)val;

@end


// Directory acts as a bridge, creating subdirectories on demand to handle
// requests to /session/:id/element/:elementId/equals/:other.
@interface MTElementComparatorBridge : MTHTTPVirtualDirectory {
 @private
  MTElement* element_;
}

@property (nonatomic, readonly, strong) MTElement* element;

+ (MTElementComparatorBridge*) comparatorBridgeFor:(MTElement*)element;
- (id) initFor:(MTElement*)element;

@end

// Temporary directory that handles element equality comparisons.
@interface MTElementComparator : MTHTTPVirtualDirectory {
 @private
  MTElementComparatorBridge* parentDirectory_;
  NSDictionary* otherElementId_;
}

- (id) initFor:(MTElementComparatorBridge*)parentDirectory
   compareWith:(NSDictionary*)otherElementId;

- (id) compareElements;

@end

