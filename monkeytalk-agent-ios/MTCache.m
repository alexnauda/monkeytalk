#import "MTCache.h"

@implementation MTCache
+ (id)sharedInstance {
    static dispatch_once_t pred = 0;
    __strong static id _sharedObject = nil;
    dispatch_once(&pred, ^{
        _sharedObject = [[self alloc] init];
    });
    return _sharedObject;
}

- (id)init {
    self = [super init];
    if (self) {
        self.cache = [[NSCache alloc] init];
        [self.cache setCountLimit:1000];
    }
    return self;
}

+ (void)add:(id)object {
    MTCache *me = [MTCache sharedInstance];
    [me.cache setObject:object forKey:[NSNumber numberWithLongLong:&object]];
}
@end
