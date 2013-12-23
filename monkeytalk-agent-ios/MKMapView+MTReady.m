//
//  MKMapView+MTReady.m
//  MonkeyTalk
//
//  Created by Kyle Balogh on 11/30/12.
//  Copyright (c) 2012 Gorilla Logic, Inc. All rights reserved.
//

#import "MKMapView+MTReady.h"
#import <objc/runtime.h>
#import "NSObject+MTReady.h"

@implementation MKMapView (MTReady)
+ (void)load {
    if (self == [MKMapView class]) {
        
        Method originalMethod = class_getInstanceMethod(self, @selector(setDelegate:));
        Method replacedMethod = class_getInstanceMethod(self, @selector(mtSetDelegate:));
        method_exchangeImplementations(originalMethod, replacedMethod);
    }
}


- (void) mtSetDelegate:(NSObject <MKMapViewDelegate>*) del {
    NSLog(@"swizzle maps");
    [del  interceptMethod:@selector(mapView:didUpdateUserLocation:)
               withMethod:@selector(mtMapView:didUpdateUserLocation:)
                  ofClass:[self class]
               renameOrig:@selector(originalMapView:didUpdateUserLocation:)
                    types:"v@:@i@"];
    
    [self mtSetDelegate:del];
}

- (void)mtMapView:(MKMapView *)mapView didUpdateUserLocation:(MKUserLocation *)userLocation {
    NSLog(@"swizzled user location");
    [self originalMapView:mapView didUpdateUserLocation:userLocation];
}

//- (void) setShowsUserLocation:(BOOL)showsUserLocation {
//    NSLog(@"showsUserLocation set to NO");
//}
@end
