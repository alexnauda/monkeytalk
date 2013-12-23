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

#import "SBJSONMT.h"
#import <UIKit/UIWebView.h>
#import "MTCommandEvent.h"
#import "MTGetVariableCommand.h"

@interface MTConnectWebView : UIWebView <UIWebViewDelegate> {
    SBJSONMT *sbJson;
    
    NSString *xmlString;
    NSString *xmlFileName;
    NSMutableDictionary *jsVariables;
    dispatch_group_t group;
}

@property(nonatomic,strong)NSString *xmlString;
@property(nonatomic,strong)NSString *xmlFileName;
@property(nonatomic,strong)NSMutableDictionary *jsVariables;

- (void)qResult:(NSString *)result event:(MTCommandEvent *)commandEvent function:(NSString *)callBack;
- (void)returnFunction:(NSString *)function args:(id)argument, ...;
- (void)returnValueForCommand:(MTCommandEvent *)commandEvent;

@end
