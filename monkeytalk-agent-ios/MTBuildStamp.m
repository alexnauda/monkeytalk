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

#import "MTBuildStamp.h"
#import "MTBuildStampDefines.h"
#import "MTPorts.h"

@implementation MTBuildStamp

+ (NSString*) buildStamp {
    return [NSString stringWithFormat:@"MonkeyTalk v%@%@%@ - %@ - Copyright 2012-2013 Gorilla Logic, Inc. - www.gorillalogic.com", MT_VERSION, (MT_BUILD_NUMBER.length > 0 ? @"_" : @""), MT_BUILD_NUMBER, MT_BUILD_DATE];
}

+ (NSString*) buildDate {
	return MT_BUILD_DATE;
}

+ (NSString*) buildNumber {
	return MT_BUILD_NUMBER;
}

+ (NSString*) version {
	return MT_VERSION;
}

+ (NSString*) versionInfo {
    return [NSString stringWithFormat:@"%@%@%@ - %@", MT_VERSION, (MT_BUILD_NUMBER.length > 0 ? @"_" : @""), MT_BUILD_NUMBER, MT_BUILD_DATE];
}

+ (NSString *) serverTestPage {
    return [NSString stringWithFormat:@"<!DOCTYPE html>\n \
            <html>\n \
            <head>\n \
            <title>MonkeyTalk</title>\n \
            </head>\n \
            <body>\n \
            <h1>OK</h1>\n \
            <p>iOS server running on port %@</p>\n \
            <p>%@</p>\n \
            </body>\n \
            </html>", MT_PLAYBACK_PORT, [MTBuildStamp buildStamp]];
}

@end
