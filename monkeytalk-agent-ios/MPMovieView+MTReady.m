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

#import "MPMovieView+MTReady.h"
#import "MonkeyTalk.h"
#import "MTCommandEvent.h"
#import "NSString+MonkeyTalk.h"
#import "UIView+MTReady.h"

@implementation MPMovieView (MTReady)

- (NSString *)mtComponent {
    return MTComponentVideoPlayer;
}

NSString *previousSeekDirection;
NSTimeInterval seekMovieTime, movieDuration, currentPlayTime;


+ (void) load {
    [[NSNotificationCenter defaultCenter] addObserver:self 
                                             selector:@selector(changed:)
                                                 name:MPMoviePlayerPlaybackStateDidChangeNotification object:nil];
    
//    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(SetMovieDuration:) name:MPMovieDurationAvailableNotification object:nil];
    
}

-(NSString *) valueForProperty:(NSString *)prop withArgs:(NSArray *)args
{
    NSString *value;
    // Returns duration of a movie played
    // Get and Verify is put to current play time of a movie played.
    
    if ([prop isEqualToString:@"value" ignoreCase:YES]) {
        value = [NSString stringWithFormat:@"%1.3f", currentPlayTime];
    }
    
    return value;
}

-(BOOL) shouldRecordMonkeyTouch:(UITouch *)touch
{
    return NO;
}

+ (void) changed:(NSNotification *)notification{
    MPMoviePlayerController* controller = (MPMoviePlayerController *)[notification object];
    MPMovieView *mpView = (MPMovieView *)controller.view;

//    movieDuration = controller.playableDuration;
    
    if ([controller playbackState] == MPMoviePlaybackStatePlaying){
        // Record the seek movement after the touch end event
        
        currentPlayTime = controller.currentPlaybackTime;
        if (seekMovieTime != 0) {
            
            if (currentPlayTime > seekMovieTime) {
            [MonkeyTalk recordFrom:mpView command:MTCommandForwardMovie args:[NSArray arrayWithObject:[NSString stringWithFormat:@"%1.3f", controller.currentPlaybackTime]]];
            }
        
            else if (currentPlayTime < seekMovieTime)
            {
            [MonkeyTalk recordFrom:mpView command:MTCommandBackwardMovie args:[NSArray arrayWithObject:[NSString stringWithFormat:@"%1.3f", controller.currentPlaybackTime]]];
            }
        }
        else
         [MonkeyTalk recordFrom:mpView command:MTCommandPlayMovie args:nil];
    }
    else if ( [controller playbackState] == MPMoviePlaybackStatePaused)
    {
         [MonkeyTalk recordFrom:mpView command:MTCommandPauseMovie args:nil];
        currentPlayTime = 0;
        seekMovieTime = 0;
    }
    
    else if ([controller playbackState] == MPMoviePlaybackStateSeekingForward)
         seekMovieTime = controller.currentPlaybackTime;
    else if ( [controller playbackState] == MPMoviePlaybackStateSeekingBackward)
        seekMovieTime = controller.currentPlaybackTime;
}

- (void) playbackMonkeyEvent:(MTCommandEvent*)event {
    
    MPMoviePlayerController *player = (MPMoviePlayerController *)self.delegate;
    
	if ([event.command isEqualToString:MTCommandPlayMovie ignoreCase:YES] || 
        [event.command isEqualToString:MTCommandPauseMovie ignoreCase:YES]) {
        
        if ([event.command isEqualToString:MTCommandPlayMovie ignoreCase:YES])
            [player play];
        else if ([event.command isEqualToString:MTCommandPauseMovie ignoreCase:YES])
            [player pause];
	}
    else if ([event.command isEqualToString:MTCommandForwardMovie ignoreCase:YES] ||
             [event.command isEqualToString:MTCommandBackwardMovie ignoreCase:YES])
    {
        if ([event.args count] == 0) {
            event.lastResult = @"Requires 1 argument, but has %d", [event.args count];
            return;
        }
        
        //Setting current playback time;
        NSTimeInterval playbackTime = [[event.args objectAtIndex:0] floatValue];
        [player setCurrentPlaybackTime:playbackTime];
    }
    else
		[super playbackMonkeyEvent:event];
    
}

@end

@implementation MPFullScreenTransportControls (MTDisable)

- (BOOL) isMTEnabled {
	return NO;
}

@end

@implementation MPFullScreenVideoOverlay (MTDisable)

- (BOOL) isMTEnabled {
	return NO;
}

@end

@implementation MPVideoBackgroundView (MTDisable)

- (BOOL) isMTEnabled {
	return NO;
}

@end

@implementation MPSwipableView (MTDisable)

- (BOOL) isMTEnabled {
	return NO;
}

@end

@implementation MPTransportButton (MTDisable)

- (BOOL) isMTEnabled {
	return NO;
}

@end