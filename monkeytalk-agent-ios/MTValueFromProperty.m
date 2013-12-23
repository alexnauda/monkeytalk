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

#import "MTValueFromProperty.h"
#import "MTDefaultProperty.h"
#import "NSString+MonkeyTalk.h"
#import "UIPickerView+MTReady.h"
#import "UIDatePicker+MTReady.h"
#import "UITabBar+MTReady.h"
#import "MPMovieView+MTReady.h"
#import "UIPageControl+MTReady.h"

@implementation MTValueFromProperty

+ (BOOL) shouldUseCustomValueForProp:(NSString *)prop onView:(UIView *)view {
    // Determine whether or not the internal prop is one we need to handle
    // in a way other than using valueForKeyPath (e.g. CGRects and CGPoints)
    NSArray *propPath = [prop componentsSeparatedByString:@"."];

    @try {
        if ([propPath count] > 1) {
            NSString *valueString = [NSString stringWithFormat:@"%@",[view valueForKeyPath:[propPath objectAtIndex:0]]];
            
            // Rects and Points need to be handled in custom fashion
            if ([valueString rangeOfString:@"NSPoint"].location != NSNotFound || [valueString rangeOfString:@"NSRect"].location != NSNotFound) {
                return YES;
            }
        }
    }
    @catch (NSException *exception) {
        // Could log the exception here
    }
    
    return NO;
}

+ (NSString *) valueForRect:(CGRect)rect forProp:(NSString *)prop {
    // Custom handling of CGRect
    NSArray *propPath = [prop componentsSeparatedByString:@"."];
    
    if ([propPath count] > 1 && [[propPath objectAtIndex:1] isEqualToString:@"size"]) {
        CGSize size = rect.size;
        if ([propPath count] == 2)
            return NSStringFromCGSize(size);
        
        if ([[propPath objectAtIndex:2] isEqualToString:@"width"])
            return [NSString stringWithFormat:@"%0.2f",size.width];
        else if ([[propPath objectAtIndex:2] isEqualToString:@"height"])
            return [NSString stringWithFormat:@"%0.2f",size.height];
    } else if ([propPath count] > 1 && [[propPath objectAtIndex:1] isEqualToString:@"origin"]) {
        CGPoint point = rect.origin;
        if ([propPath count] == 2)
            return NSStringFromCGPoint(point);
        
        if ([[propPath objectAtIndex:2] isEqualToString:@"x"])
            return [NSString stringWithFormat:@"%0.2f",point.x];
        else if ([[propPath objectAtIndex:2] isEqualToString:@"y"])
            return [NSString stringWithFormat:@"%0.2f",point.y];
    }
    
    // If not size or origin and prop count is greater than 1, we do not handle keypath
    if ([propPath count] > 1)
        [NSException raise:@"Invalid keypath" format:@"invalid keypath"];
    
    return NSStringFromCGRect(rect);
}

+ (NSString *) valueForPoint:(CGPoint)point forProp:(NSString *)prop {
    // Custom handling of CGPoint
    NSArray *propPath = [prop componentsSeparatedByString:@"."];
    
    if (([propPath count] > 1 && [[propPath objectAtIndex:1] isEqualToString:@"x"]))
        return [NSString stringWithFormat:@"%0.2f",point.x];
    else if (([propPath count] > 1 && [[propPath objectAtIndex:1] isEqualToString:@"y"]))
        return [NSString stringWithFormat:@"%0.2f",point.y];
    
    // If not x or y and prop count is greater than 1, we do not handle keypath
    if ([propPath count] > 1)
        [NSException raise:@"Invalid keypath" format:@"invalid keypath"];
    
    return NSStringFromCGPoint(point);
}

+ (NSString *) handleInternalPropPath:(NSString *)prop forView:(UIView *)view {
    NSArray *propPath = [prop componentsSeparatedByString:@"."];
    NSString *base = [propPath objectAtIndex:0];
    
    // Handle frame, bounds, contentStretch, and center appropriately
    if ([base rangeOfString:@"frame"].location != NSNotFound) {
        return [[self class] valueForRect:view.frame forProp:prop];
    } else if ([base rangeOfString:@"bounds"].location != NSNotFound) {
        return [[self class] valueForRect:view.bounds forProp:prop];
    } else if ([base rangeOfString:@"contentStretch"].location != NSNotFound) {
        return [[self class] valueForRect:view.contentStretch forProp:prop];
    } else if ([base rangeOfString:@"center"].location != NSNotFound) {
        return [[self class] valueForPoint:view.center forProp:prop];
    }
    
    // Raise an exception for keypaths we do not handle
    [NSException raise:@"Invalid keypath" format:@"invalid keypath"];

    return nil;
}

- (NSString *) valueForProperty:(NSString *)prop withArgs:(NSArray *)args {
    return @"";
}

+ (NSString *) valueFromProp:(NSString *)prop forView:(UIView *)source {
    NSString *property = prop;
    NSArray *args = nil;
    NSArray *mtProperties = [NSArray arrayWithObjects:@"item",@"size",@"detail",@"value",@"max",@"min", nil];

    if ([[self class] shouldUseCustomValueForProp:prop onView:source]) {
        // Internal property path for a struct we need to handle in a custom fashion
        return [[self class] handleInternalPropPath:prop forView:source];
    } else if ([source isKindOfClass:[UITableView class]] ||
        [source isKindOfClass:[UIPickerView class]] ||
               [source isKindOfClass:[UISegmentedControl class]] ||
               [source isKindOfClass:[UITabBar class]]) {
        if (([prop length] >= 5 && [[prop substringToIndex:5] isEqualToString:@"item("]) || [prop isEqualToString:@"item" ignoreCase:YES]) {
            prop = [prop substringFromIndex:4];
            prop = [prop stringByReplacingOccurrencesOfString:@"(" withString:@""];
            prop = [prop stringByReplacingOccurrencesOfString:@")" withString:@""];
            args = [prop componentsSeparatedByString:@","];
            property = @"item";
        } else if (([prop length] >= 5 && [[prop substringToIndex:5] isEqualToString:@"size("]) ||
                   [prop isEqualToString:@"size" ignoreCase:YES]) {
            prop = [prop substringFromIndex:4];
            prop = [prop stringByReplacingOccurrencesOfString:@"(" withString:@""];
            prop = [prop stringByReplacingOccurrencesOfString:@")" withString:@""];
            args = [prop componentsSeparatedByString:@","];
            property = @"size";
        } else if (([prop length] >= 7 && [[prop substringToIndex:7] isEqualToString:@"detail("]) ||
                   [prop isEqualToString:@"detail" ignoreCase:YES]) {
            prop = [prop substringFromIndex:6];
            prop = [prop stringByReplacingOccurrencesOfString:@"(" withString:@""];
            prop = [prop stringByReplacingOccurrencesOfString:@")" withString:@""];
            args = [prop componentsSeparatedByString:@","];
            property = @"detail";
        }
    } else if ([prop isEqualToString:@"value" ignoreCase:YES]) {
        if ([source respondsToSelector:@selector(mtDefaultValueKeyPath)]) {
            property = [source performSelector:@selector(mtDefaultValueKeyPath)];
        } else {
            property = [MTDefaultProperty defaultPropertyForClass:[NSString stringWithFormat:@"%@",[source class]]];
        }
    } else {
        BOOL isMTComponent = NO;
        
        for (NSString *found in mtProperties) {
            if ([prop isEqualToString:found ignoreCase:NO]) {
                isMTComponent = YES;
                break;
            }
        }
        
        if (!isMTComponent)
            [NSException raise:@"Invalid MT property" format:@"invalid property"];
    }
    
    if ([source respondsToSelector:@selector(valueForProperty:withArgs:)])
        return [(MTValueFromProperty *)source valueForProperty:property withArgs:args];
    
    return [source valueForKeyPath:property];
}
@end
