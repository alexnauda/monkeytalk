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

#import "MTSeleniumCommand.h"
#import "MTConvertType.h"
#import "MTConstants.h"
#import "NSString+MonkeyTalk.h"
#import "MTHTTPVirtualDirectory+FindElement.h"
#import "WebView.h"
#import "UIWebDocumentView.h"
#import "MTSession.h"

@implementation MTSeleniumCommand
@synthesize mtEvent, xPath, alternateID, command, args, currentOrdinal, htmlTag, element;

+ (NSDictionary *) webAutomators {
    NSArray *objects = [NSArray arrayWithObjects:
                        @"MTButtonAutomator",
                        @"MTCheckBoxAutomator",
                        @"MTCheckBoxAutomator",
                        @"MTImageAutomator",
                        @"MTInputAutomator",
                        @"MTItemSelectorAutomator",
                        @"MTLabelAutomator",
                        @"MTLinkAutomator",
                        @"MTButtonSelectorAutomator",
                        @"MTTableAutomator",
                        @"MTTextAreaAutomator",
                        @"MTButtonSelectorAutomator",
                        nil];
    
    NSArray *keys = [NSArray arrayWithObjects:
                        MTComponentButton,
                        MTComponentToggle,
                        MTComponentCheckBox,
                        MTComponentImage,
                        MTComponentInput,
                        MTComponentSelector,
                        MTComponentLabel,
                        MTComponentLink,
                        MTComponentButtonSelector,
                        MTComponentTable,
                        MTComponentTextArea,
                        MTComponentRadioButtons,
                        nil];
    
    return [[NSDictionary alloc] initWithObjects:objects forKeys:keys];
}

+ (id) initWithMTCommandEvent:(MTCommandEvent *)event {
    NSString *mtComponent = event.component;
    NSString *automator = [[[self class] webAutomators] objectForKey:mtComponent];
    
    // If automator is not found and the monkeyID is not an xpath expression
    // override the xpath in automator class (if necissary)
    if (automator) {
        return [[NSClassFromString(automator) alloc] initWithMTCommandEvent:event];
    }
    
    return [[MTSeleniumCommand alloc] initWithMTCommandEvent:event];
}

- (id) initWithMTCommandEvent:(MTCommandEvent *)event {
    self = [super init];
    if (self) {
        mtEvent = event;
    }
    
    return self;
}

- (NSString *) htmlTag {
    NSString *mtComponent = [MTConvertType convertedComponentFromString:mtEvent.className isRecording:YES];
    NSString *tag = [NSString stringWithFormat:mtComponent];
    
    if ([mtComponent isEqualToString:MTComponentButtonSelector ignoreCase:YES])
        tag = @"input";
    else if ([mtComponent isEqualToString:MTComponentSelector ignoreCase:YES])
        tag = @"select";
    else if ([mtComponent isEqualToString:MTComponentLabel ignoreCase:YES])
        tag = @"a";
    else if ([mtComponent isEqualToString:MTComponentImage ignoreCase:YES])
        tag = @"img";
    else if ([mtComponent isEqualToString:MTComponentView ignoreCase:YES] ||
             [mtComponent isEqualToString:MTComponentHTMLTag ignoreCase:YES])
        tag = @"*";
    
    return tag;
}

- (Boolean) isOrdinal {
    if ([mtEvent.monkeyID rangeOfString:@"#"].location == 0) {
        return YES;
    }
    
    return NO;
}

- (NSString *) basePath {
    NSString *mid = mtEvent.monkeyID;
    NSLog(@"midordinal: %@",mtEvent.monkeyOrdinal);
    
    if (mtEvent.monkeyOrdinal)
        mid = [mtEvent.monkeyOrdinal objectForKey:@"mid"];
    
    NSString *path = [NSString stringWithFormat:@"[%@]",
                      [self finderExpression:mid]];
    
    NSInteger find = 0;
    if ([self isOrdinal]) {
        find = [[mtEvent.monkeyID substringFromIndex:1] integerValue];
        //find -= currentOrdinal;
        path = [NSString stringWithFormat:@"[%i]",find];
    }
    
    return path;
}

- (NSString *) finderExpression:(NSString *)selection {
    return [NSString stringWithFormat:@"@id='%@' or @name='%@' or @value='%@' or text()='%@' or @title='%@' or @class='%@' or @alt='%@' or @src='%@' or @href='%@'",
            selection, selection, selection, selection, selection, selection, selection, selection, selection];
}

- (NSString *) xPath {
    // Return basic xpath expression — handles most components
    // Override in automators class to specify on per component basis
    NSString *convertedCommand;
    
    NSString *mtComponent = [MTConvertType convertedComponentFromString:mtEvent.className isRecording:YES];
    NSString *path = [self basePath];
    BOOL isOrdinal = [self isOrdinal];

    convertedCommand = [NSString 
                        stringWithFormat:@"//%@%@",self.htmlTag,path];
    
    if ([[mtEvent.monkeyID lowercaseString] rangeOfString:@"xpath="].location != NSNotFound) {
        // Use Monkey ID as xpath
        convertedCommand = [mtEvent.monkeyID stringByReplacingOccurrencesOfString:@"xpath=" withString:@""];
    }
    
    return convertedCommand;
}

- (NSString *) command {
    NSString *seleniumCommand;
    NSString *mtComponent = [MTConvertType convertedComponentFromString:mtEvent.className isRecording:YES];
    
    if ([mtEvent.command isEqualToString:MTCommandEnterText ignoreCase:YES])
        seleniumCommand = @"type:";
    else if ([mtEvent.command isEqualToString:MTCommandClear ignoreCase:YES])
        seleniumCommand = @"clear:";
    else if (!([mtComponent isEqualToString:MTComponentTable ignoreCase:YES] ||
               [mtComponent isEqualToString:MTComponentButtonSelector ignoreCase:YES]) && 
             ([mtEvent.command isEqualToString:MTCommandOn ignoreCase:YES]))
        seleniumCommand = @"setChecked:";
    else if ([mtEvent.command isEqualToString:MTCommandOff ignoreCase:YES] ||
             ([mtEvent.command isEqualToString:MTCommandSelect ignoreCase:YES] && !([mtComponent isEqualToString:MTComponentTable ignoreCase:YES] || [mtComponent isEqualToString:MTComponentButtonSelector ignoreCase:YES])))
        seleniumCommand = @"toggleSelected";
    else
        seleniumCommand = @"click:";
    
    return seleniumCommand;
}

- (NSDictionary *) args {
    if (mtEvent.args != nil && [mtEvent.args count] > 0) {
        NSDictionary *argsDict = [NSDictionary dictionaryWithObject:[mtEvent.args objectAtIndex:0] forKey:@"value"];
        
        return argsDict;
    }
    
    return nil;
}

- (MTElement *) elementFromXpath:(NSString *)xp {
    MTSession *session = [[MTSession alloc] init];
    MTHTTPVirtualDirectory *virtualDirectory = [[MTHTTPVirtualDirectory alloc] init];
    NSDictionary *webElementDict = [[NSMutableDictionary alloc] init];
    NSDictionary *response;
    
    NSString *using = @"xpath";
    NSString *value = xp;
    
    [webElementDict setValue:using forKey:@"using"];
    [webElementDict setValue:value forKey:@"value"];
    
    if (mtEvent.monkeyOrdinal) {
        NSArray *elements = [virtualDirectory findElements:webElementDict root:nil implicitlyWait:0];
        NSInteger midOrdinal = [[mtEvent.monkeyOrdinal objectForKey:@"ordinal"] integerValue];
        
        if (midOrdinal > 0 && midOrdinal <= elements.count) {
            midOrdinal--;
            response = [elements objectAtIndex:midOrdinal];
        } else
            return nil;
    } else
        response = [virtualDirectory findElement:webElementDict root:nil implicitlyWait:0];
    
    
    if ([response objectForKey:@"ELEMENT"]) {
        return [MTElement elementWithId:[response objectForKey:@"ELEMENT"]
                                andSession:session andMTWebViewController:self andCommandId:using];
    }
    
    return nil;
}

- (MTElement *) element {
    if (!element) {
        element = [self elementFromXpath:self.xPath];
        
        if (!element && self.alternateID != nil)
            element = [self elementFromXpath:self.alternateID];
        
//        MTSession *session = [[[MTSession alloc] init] autorelease];
//        MTHTTPVirtualDirectory *virtualDirectory = [[MTHTTPVirtualDirectory alloc] init];
//        NSDictionary *webElementDict = [[NSMutableDictionary alloc] init];
//        NSDictionary *response;
//        
//        NSString *using = @"xpath";
//        NSString *value = self.xPath;
//        NSDictionary *argsDict = self.args;
//        SEL selector = NSSelectorFromString(self.command);
//        
//        [webElementDict setValue:using forKey:@"using"];
//        [webElementDict setValue:value forKey:@"value"];
//        
//        response = [virtualDirectory findElement:webElementDict root:nil implicitlyWait:0];
//        
//        if (![response objectForKey:@"ELEMENT"] && self.alternateID != nil) {
//            // If there is an alternate xpath for event — try again
//            [webElementDict setValue:self.alternateID forKey:@"value"];
//            response = [virtualDirectory findElement:webElementDict root:nil implicitlyWait:0];
//        }
//        
//        if ([response objectForKey:@"ELEMENT"]) {
//            element = [MTElement elementWithId:[response objectForKey:@"ELEMENT"]
//                                    andSession:session andMTWebViewController:self andCommandId:using];
//        }
    }
    
    return element;
}

- (void)scrollToElement:(MTElement *)e touchPoint:(CGPoint)touchCenter {
    MTWebViewController *webController = (MTWebViewController *)self.delegate;
    BOOL isOffScreen = [webController isElementOffScreen:e] || !CGRectContainsPoint(webController.webView.bounds, touchCenter);
    if (isOffScreen) {
        NSLog(@"Scrolling webview to element");
        UIScrollView *scrollView = (UIScrollView *)webController.webView.scrollView;
        CGPoint origin = e.location;
        if (scrollView.contentSize.width - origin.x < scrollView.frame.size.width) {
            origin.x = origin.x - (scrollView.frame.size.width - (scrollView.contentSize.width - origin.x));
        }
        if (scrollView.contentSize.height - origin.y < scrollView.frame.size.height) {
            origin.y = origin.y - (scrollView.frame.size.height - (scrollView.contentSize.height - origin.y));
        }
        
        // we can alternatively use scrollRectToVisible: but causes playback issues if element is clipped
        //[scrollView scrollRectToVisible:CGRectMake(origin.x, origin.y, size.width, size.height) animated:YES];
        [scrollView setContentOffset:origin animated:NO];
    }
}

- (BOOL) playBackOnElement {
    MTWebViewController *webController = self.delegate;
    CGSize size = [self.element size];
    CGPoint initialTouchCenter = [self.element location];
    initialTouchCenter = [webController translatePageCoordinateToView:initialTouchCenter];
    size = [webController translateSizeWithZoom:size];
    
    initialTouchCenter.x = initialTouchCenter.x + size.width/2;
    initialTouchCenter.y = initialTouchCenter.y + size.height/2;
    
    // make sure element is on screen
    [self scrollToElement:self.element touchPoint:initialTouchCenter];
    
    // we need to wait until the webview has completed loading before playing back
    WebView *coreWebView = [[webController.webView _documentView] webView];
    if (coreWebView.estimatedProgress > 0) {
        mtEvent.didPlayInWeb = NO;
        return NO;
    }
    
    if ([mtEvent.command isEqualToString:@"find" ignoreCase:YES]) {
        [webController highlightElement:self.element];
    } else if ([mtEvent.command isEqualToString:@"tap" ignoreCase:YES] ||
               [mtEvent.command isEqualToString:@"click" ignoreCase:YES]) {
        [self tapElement];
    } else if ([mtEvent.command isEqualToString:MTCommandGet ignoreCase:YES] ||
        [mtEvent.command.lowercaseString rangeOfString:[MTCommandVerify lowercaseString]].location != NSNotFound) {
        [self valueForElement];
    } else {
        SEL selector = NSSelectorFromString(self.command);
        
        //dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            [self.element performSelectorOnMainThread:selector withObject:self.args waitUntilDone:YES];
        //});
    }
    return YES;
}

- (NSString *) attribute {
    return [mtEvent.args objectAtIndex:1];
}

- (Boolean) hasAttribute {
    return ([mtEvent.args count] > 1);
}

- (Boolean) useDefaultProperty {
    return ([self hasAttribute] && [[self attribute] isEqualToString:@"value"] ||
            [mtEvent.args count] == 1);
}

- (void) valueForElement {
    if ([mtEvent.args count] > 1) {
        NSString *attribute = [mtEvent.args objectAtIndex:1];
        
        if ([attribute rangeOfString:@"."].location == 0)
            attribute = [attribute substringFromIndex:1];
        
        mtEvent.value = [self.element attribute:attribute];
    } else {
        mtEvent.value = [self.element text];
    }
}

- (void)tapElement {
    [self tapElement:self.element];
}

- (void)tapElement:(MTElement *)e {
    CGSize size = CGSizeZero;
    MTWebViewController *webController = (MTWebViewController *)self.delegate;
    
    size = [e size];
    CGPoint touchCenter = [e location];
    touchCenter = [webController translatePageCoordinateToView:touchCenter];
    size = [webController translateSizeWithZoom:size];
    
    touchCenter.x = touchCenter.x + size.width/2;
    touchCenter.y = touchCenter.y + size.height/2;
    
    // make sure element is on screen
    [self scrollToElement:e touchPoint:touchCenter];
    
    // update cell touch center
    touchCenter = [e location];
    touchCenter.x = touchCenter.x + size.width/2;
    touchCenter.y = touchCenter.y + size.height/2;
    [webController showTapOnElement:e];
    
    // translate touch location for synthesis
    touchCenter = [webController translatePageCoordinateToView:touchCenter];
    [webController.webView playbackTapAtLocation:touchCenter];
}

- (NSArray *)argIndices:(NSString **)attribute {
    NSMutableArray *argItemsArray= nil;
    NSRegularExpression *regex = [[NSRegularExpression alloc] initWithPattern:@"\\((\\d+)[,\\)]" options:NSRegularExpressionCaseInsensitive error:nil];
    NSTextCheckingResult *r = [regex firstMatchInString:*attribute options:0 range:NSMakeRange(0, (*attribute).length)];
    NSString *argItems = nil;
    
    if (r) {
        argItems = [*attribute substringWithRange:NSMakeRange(r.range.location, (*attribute).length - r.range.location)];
        *attribute = [*attribute substringToIndex:r.range.location];
    }
    
    if (argItems) {
        argItemsArray = [[NSMutableArray alloc] init];
        NSRegularExpression *regex = [[NSRegularExpression alloc] initWithPattern:@"\\d" options:NSRegularExpressionCaseInsensitive error:nil];
        [regex enumerateMatchesInString:argItems options:0 range:NSMakeRange(0, argItems.length) usingBlock:^(NSTextCheckingResult *result, NSMatchingFlags flags, BOOL *stop){
            [argItemsArray addObject:[argItems substringWithRange:result.range]];
        }];
    }
    
    if ([*attribute rangeOfString:@"."].location == 0)
        *attribute = [*attribute substringFromIndex:1];
    return argItemsArray ? [[NSArray alloc] initWithArray:argItemsArray] : nil;
}

//- (void) dealloc {
//    [super dealloc];
//    [xPath release];
//    [command release];
//    [args release];
//}

@end
