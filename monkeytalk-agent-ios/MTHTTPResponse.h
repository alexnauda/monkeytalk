#import <Foundation/Foundation.h>


@protocol MTHTTPResponse

- (UInt64)contentLength;

- (UInt64)offset;
- (void)setOffset:(UInt64)offset;

- (NSData *)readDataOfLength:(unsigned int)length;

@end

@interface MTHTTPFileResponse : NSObject <MTHTTPResponse>
{
	NSString *filePath;
	NSFileHandle *fileHandle;
}

- (id)initWithFilePath:(NSString *)filePath;
- (NSString *)filePath;

@end

@interface MTHTTPDataResponse : NSObject <MTHTTPResponse>
{
	unsigned offset;
	NSData *data;
}

- (id)initWithData:(NSData *)data;

@end
