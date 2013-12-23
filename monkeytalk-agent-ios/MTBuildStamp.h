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

#define STARTUP_MESSAGE @"\n\n \
								   .\"`\". \n \
							  .-./ _=_ \\.-. \n \
							 {  (,(oYo),) }}       ___________________________ \n \
							 {{ |   \"   |} }-.   /                             \\ \n \
							 { { \\(-^-)/  }}  *.|    Need help? Go gorilla!     | \n \
							 { { }._:_.{  }}     \\ ___________________________ / \n \
							 {{  } -:- { } } \n \
							 {_{ }`===`{  _} \n \
							((((\\)     (/)))) \n \
#########################################################################################################################\n \
#                                                                                                                       #\n \
# %@\n \
#                                                                                                                       #\n \
# Gorilla Logic can help you create complex applications for iOS, Android, Adobe Flex and Java platforms.               #\n \
# To learn more about our development, training, and testing services, visit us at www.gorillalogic.com.                #\n \
#                                                                                                                       #\n \
#########################################################################################################################\n\n" \
, [MTBuildStamp buildStamp]


@interface MTBuildStamp : NSObject

+ (NSString*) buildStamp;
+ (NSString*) buildDate;
+ (NSString*) buildNumber;
+ (NSString*) version;
+ (NSString*) versionInfo;
+ (NSString *) serverTestPage;

@end
