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

#import <Foundation/Foundation.h>
#import <MediaPlayer/MediaPlayer.h>
@class MPMoviePlayerController;

@protocol MPMovieViewDelegate;

@interface MPMovieView : UIView {
@private
	id<MPMovieViewDelegate> _delegate;
}
@property(assign, nonatomic) id<MPMovieViewDelegate> delegate;
-(void)willMoveToWindow:(id)window;
-(void)didMoveToWindow;
@end
@interface MPMovieView (MTReady)

- (NSString *) valueForProperty:(NSString *)prop withArgs:(NSArray *)args;

@end

@interface MPFullScreenTransportControls
@end
@interface MPFullScreenTransportControls (MTDisable)

@end

@interface MPFullScreenVideoOverlay
@end
@interface MPFullScreenVideoOverlay (MTDisable)

@end

@interface MPVideoBackgroundView
@end
@interface MPVideoBackgroundView (MTDisable)

@end

@interface MPSwipableView
@end
@interface MPSwipableView (MTDisable)

@end

@interface MPTransportButton
@end
@interface MPTransportButton (MTDisable)

@end