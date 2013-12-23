//
//  UIImagePickerController+MTReady.m
//  MonkeyTalk
//
//  Created by Kyle Balogh on 12/7/12.
//  Copyright (c) 2012 Gorilla Logic, Inc. All rights reserved.
//

#import "UIImagePickerController+MTReady.h"
#import "MonkeyTalk.h"
#import "NSObject+MTReady.h"
#import "MTCommandEvent.h"
#import "MTUtils.h"

@implementation UIImagePickerController (MTReady)
+ (void) load {
    if (self == [UIImagePickerController class]) {
        [NSObject swizzle:@"setDelegate:" with:@"mtSetDelegate:" for:self];
    }
}

- (void) mtSetDelegate:(NSObject <UIImagePickerControllerDelegate>*) del {
    [MonkeyTalk sharedMonkey].currentImagePickerDelegate = del;
    [MonkeyTalk sharedMonkey].currentImagePicker = self;
    [del  interceptMethod:@selector(imagePickerController: didFinishPickingMediaWithInfo:)
               withMethod:@selector(mtImagePickerController: didFinishPickingMediaWithInfo:)
                  ofClass:[self class]
               renameOrig:@selector(originalImagePickerController: didFinishPickingMediaWithInfo:)
                    types:"v@:@i@"];
    
    if ([self respondsToSelector:@selector(mtSetDelegate:)])
        [self mtSetDelegate:del];
}

- (void)mtImagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info {
    NSString *mediaType = [NSString stringWithFormat:@"%@",[info objectForKey:UIImagePickerControllerMediaType]];
    NSString *documentsDirectory = [MTUtils scriptsLocation];
    
    NSArray *args = [NSArray arrayWithObjects:mediaType, nil];
    MTCommandEvent *event = [[MTCommandEvent alloc]
                             init:@"Choose" className:@"UIImagePickerController"
                             monkeyID:@"*"
                             args:args];
    
    [MonkeyTalk sendRecordEvent:event];
    
    [self originalImagePickerController:picker didFinishPickingMediaWithInfo:info];
}

+ (void) playbackMonkeyEvent:(id)event {
    MonkeyTalk *mt = [MonkeyTalk sharedMonkey];
    MTCommandEvent *c = (MTCommandEvent *)event;
    
    NSString *mediaType = [c.args objectAtIndex:0];
    NSString *referenceURLString = @"";
    NSString *mediaUrlString = [MTUtils sampleVideo];
    
    NSURL *mediaUrl = [NSURL URLWithString:mediaUrlString];
    NSURL *referenceURL = [NSURL URLWithString:referenceURLString];
    
    NSArray *keys = [NSArray arrayWithObjects:UIImagePickerControllerMediaType, UIImagePickerControllerMediaURL, UIImagePickerControllerReferenceURL, nil];
    NSArray *objects = [NSArray arrayWithObjects:mediaType, mediaUrl, referenceURL, nil];
    NSDictionary *info = [NSDictionary dictionaryWithObjects:objects forKeys:keys];
    [mt.currentImagePickerDelegate imagePickerController:mt.currentImagePicker didFinishPickingMediaWithInfo:info];
}
@end
