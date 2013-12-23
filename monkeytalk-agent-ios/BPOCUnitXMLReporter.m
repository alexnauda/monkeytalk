//
//  BPOCUnitXMLReporter.m
//
//  Created by Jason Foreman on 10/24/09.
//  Modified by Kyle Balogh on 7/21/11
//  Changes: Use GDataXML for iOS compatibility.
//
//  Copyright 2009 Jason Foreman. Some rights reserved.
//  This code is released under a Creative Commons license:
//  http://creativecommons.org/licenses/by-sa/3.0/
//


#import <Foundation/Foundation.h>
#import <SenTestingKit/SenTestingKit.h>
#import "GDataXMLNode.h"
#import "MTUtils.h"


@interface BPTestXunitXmlListener : NSObject
{
@private
    GDataXMLDocument *document;
    GDataXMLElement *suitesElement;
    GDataXMLElement *currentSuiteElement;
    GDataXMLElement *currentCaseElement;
    NSAutoreleasePool *pool;
    NSString* fileName;
}

@property (retain) GDataXMLDocument *document;
@property (retain) GDataXMLElement *suitesElement;
@property (retain) GDataXMLElement *currentSuiteElement;
@property (retain) GDataXMLElement *currentCaseElement;

- (void)writeResultFile;

@end


static BPTestXunitXmlListener *instance = nil;

static void __attribute__ ((constructor)) BPTestXunitXmlListenerStart(void)
{
    instance = [BPTestXunitXmlListener new];
}

static void __attribute__ ((destructor)) BPTestXunitXmlListenerStop(void)
{
    [instance writeResultFile];
}


@implementation BPTestXunitXmlListener

@synthesize document;
@synthesize suitesElement;
@synthesize currentSuiteElement;
@synthesize currentCaseElement;


- (id)init;
{
    if ((self = [super init]))
    {
        pool = [[NSAutoreleasePool alloc] init];
        NSNotificationCenter *center = [NSNotificationCenter defaultCenter];
        [center addObserver:self selector:@selector(testSuiteStarted:) name:SenTestSuiteDidStartNotification object:nil];
        [center addObserver:self selector:@selector(testSuiteStopped:) name:SenTestSuiteDidStopNotification object:nil];
        [center addObserver:self selector:@selector(testCaseStarted:) name:SenTestCaseDidStartNotification object:nil];
        [center addObserver:self selector:@selector(testCaseStopped:) name:SenTestCaseDidStopNotification object:nil];
        [center addObserver:self selector:@selector(testCaseFailed:) name:SenTestCaseDidFailNotification object:nil];

        document = [[GDataXMLDocument alloc] init];
		[document initWithRootElement:[GDataXMLElement elementWithName:@"testsuites"]];
		self.suitesElement = [document rootElement];
        
        fileName = [NSString stringWithFormat:@"MT_LOG-%@.xml",[MTUtils timeStamp]];
    }
    return self;
}

- (void)dealloc;
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    self.document = nil;
    self.suitesElement = nil;
    self.currentSuiteElement = nil;
    self.currentCaseElement = nil;
    [super dealloc];
}

- (void)writeResultFile;
{
    if (self.document && (getenv("MT_ENABLE_XML_REPORT") && strcmp(getenv("MT_ENABLE_XML_REPORT"),"NO") && !getenv("MT_ENABLE_QUNIT")))
    {
        NSLog(@"XML Output: %@",[MTUtils scriptPathForFilename:fileName]);
        
        [[self.document XMLData] writeToFile:[MTUtils scriptPathForFilename:fileName] atomically:NO];
    }
}


#pragma mark Notification Callbacks

- (void)testSuiteStarted:(NSNotification*)notification;
{
    SenTest *test = [notification test];
    self.currentSuiteElement = [GDataXMLElement elementWithName:@"testsuite"];
    [self.currentSuiteElement addAttribute:[GDataXMLNode attributeWithName:@"name" stringValue:[test name]]];
}

- (void)testSuiteStopped:(NSNotification*)notification;
{
    /* Original implementation
    if (self.currentSuiteElement)
    {
        [self.suitesElement addChild:self.currentSuiteElement];
        self.currentSuiteElement = nil;
    }
     */
    
    SenTestSuiteRun *testSuiteRun = (SenTestSuiteRun *)[notification object];
	
    if (currentSuiteElement)
    {
        [currentSuiteElement addAttribute:[GDataXMLNode attributeWithName:@"name" stringValue:[[testSuiteRun test] name]]];
		[currentSuiteElement addAttribute:[GDataXMLNode attributeWithName:@"tests" stringValue:[NSString stringWithFormat:@"%d",[testSuiteRun testCaseCount]]]];
		[currentSuiteElement addAttribute:[GDataXMLNode attributeWithName:@"errors" stringValue:[NSString stringWithFormat:@"%d",[testSuiteRun unexpectedExceptionCount]]]];
		[currentSuiteElement addAttribute:[GDataXMLNode attributeWithName:@"failures" stringValue:[NSString stringWithFormat:@"%d", [testSuiteRun failureCount]]]];
		[currentSuiteElement addAttribute:[GDataXMLNode attributeWithName:@"time" stringValue:[NSString stringWithFormat:@"%f",[testSuiteRun testDuration]]]];
		
        [suitesElement addChild:self.currentSuiteElement];
        
		self.currentSuiteElement = nil;
    }

    //[instance writeResultFile];
}

- (void)testCaseStarted:(NSNotification*)notification;
{
    SenTest *test = [notification test];
    self.currentCaseElement = [GDataXMLElement elementWithName:@"testcase"];
    [self.currentCaseElement addAttribute:[GDataXMLNode attributeWithName:@"name" stringValue:[test name]]];
}

- (void)testCaseStopped:(NSNotification*)notification;
{
    /* Original implementation
    [self.currentSuiteElement addChild:self.currentCaseElement];
    self.currentCaseElement = nil;
    */
    
    SenTestCase *testCase = (SenTestCase *)[notification test];
    SenTestCaseRun *testCaseRun = (SenTestCaseRun *)[notification object];
	
	[currentCaseElement addAttribute:[GDataXMLNode attributeWithName:@"name" stringValue:NSStringFromSelector([testCase selector])]];
	[currentCaseElement addAttribute:[GDataXMLNode attributeWithName:@"classname" stringValue:NSStringFromClass([testCase class])]];
	[currentCaseElement addAttribute:[GDataXMLNode attributeWithName:@"time" stringValue:[NSString stringWithFormat:@"%f",[testCaseRun testDuration]]]];
	
    [currentSuiteElement addChild:self.currentCaseElement];
    self.currentCaseElement = nil;
}

- (void)testCaseFailed:(NSNotification*)notification;
{
    GDataXMLElement *failureElement = [GDataXMLElement elementWithName:@"failure"];
    [failureElement setStringValue:[[notification exception] description]];
    [self.currentCaseElement addChild:failureElement];
}

@end
