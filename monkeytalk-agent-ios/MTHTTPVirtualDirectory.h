//
//  MTVirtualDirectory.h
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
#import "MTHTTPResource.h"

// A MTVirtualDirectory represents a particular directory in the URL heirachy.
// It does not need to be mapped by actual files.
@interface MTHTTPVirtualDirectory : NSObject<MTHTTPResource> {
	// This maps virtual subdirectory -> MTHTTPResource element
	NSMutableDictionary *contents;
	
	// The index (or root) element of the directory
	id<MTHTTPResource> index;
	
	// If this is set, it redirects /foo/self to /foo/self/
	// If this is not set, /foo/self returns index.
	BOOL redirectBaseToIndex;
}

// The index of the vdir. This will be returnd on .../vdir/ and .../vdir
// You can make this a redirect to index.html or something if you want.
@property (nonatomic, strong) id<MTHTTPResource> index;

// Do we redirect /foo/self to /foo/self/ or just return index?
@property (nonatomic) BOOL redirectBaseToIndex;

// Make and return an autoreleased MTVirtualDirectory
+ (MTHTTPVirtualDirectory *)virtualDirectory;

// Set a virtual file in the MTVirtualDirectory. The resource is retained.
// If resource is nil, this method removes the named resource from the
// MTVirtualDirectory. 
- (void)setResource:(id<MTHTTPResource>)resource withName:(NSString *)name;

// Remove a resource from the MTVirtualDirectory.
- (void)removeResourceWithName:(NSString *)name;

@end
