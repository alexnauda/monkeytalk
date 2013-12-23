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

#import "MonkeyTalk+OCUnit.h"

@implementation MonkeyTalk (OCUnit)

- (void) runTests:(SenTestSuite*) suite {
	// We're a thread
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init]; 
	SenTestRun* run = [suite run];
	if (getenv("MT_ENABLE_AUTOEXIT") && strcmp(getenv("MT_ENABLE_AUTOEXIT"),"NO")) {
		int exitStatus = (([run totalFailureCount] == 0U) ? 0 : 1);
		exit(exitStatus);
	}
	[pool release];	
}	

- (void) runTestSuite:(SenTestSuite*)suite {
	[NSThread detachNewThreadSelector:@selector(runTests:) toTarget:self withObject:suite];
	
}

- (void) runAllTests {
	[self runTestSuite:[SenTestSuite defaultTestSuite]];
}

- (void) setupObserver {
    // We need this because if the user specifies which suites he wants to run (rather than the default of all suites)
    //    then SenTest doesn't set the observer itself
    // This might be a problem if user actually wants to override the observer themselves since we might be clobbering it here....
    [SenTestObserver setCurrentObserver:[SenTestLog class]];
}


@end
