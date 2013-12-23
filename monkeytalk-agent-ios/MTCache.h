#import <Foundation/Foundation.h>

@interface MTCache : NSObject
+ (id) sharedInstance;
+ (void)add:(id)object;
@property (strong, atomic) NSCache *cache;
@end
