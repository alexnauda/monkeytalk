/**
 * MTDDRange is the functional equivalent of a 64 bit NSRange.
 * The HTTP Server is designed to support very large files.
 * On 32 bit architectures (ppc, i386) NSRange uses unsigned 32 bit integers.
 * This only supports a range of up to 4 gigabytes.
 * By defining our own variant, we can support a range up to 16 exabytes.
 * 
 * All effort is given such that MTDDRange functions EXACTLY the same as NSRange.
**/

#import <Foundation/NSValue.h>
#import <Foundation/NSObjCRuntime.h>

@class NSString;

typedef struct _DDRange {
    UInt64 location;
    UInt64 length;
} MTDDRange;

typedef MTDDRange *DDRangePointer;

NS_INLINE MTDDRange DDMakeRange(UInt64 loc, UInt64 len) {
    MTDDRange r;
    r.location = loc;
    r.length = len;
    return r;
}

NS_INLINE UInt64 DDMaxRange(MTDDRange range) {
    return (range.location + range.length);
}

NS_INLINE BOOL DDLocationInRange(UInt64 loc, MTDDRange range) {
    return (loc - range.location < range.length);
}

NS_INLINE BOOL DDEqualRanges(MTDDRange range1, MTDDRange range2) {
    return ((range1.location == range2.location) && (range1.length == range2.length));
}

FOUNDATION_EXPORT MTDDRange MTDDUnionRange(MTDDRange range1, MTDDRange range2);
FOUNDATION_EXPORT MTDDRange MTDDIntersectionRange(MTDDRange range1, MTDDRange range2);
FOUNDATION_EXPORT NSString *MTDDStringFromRange(MTDDRange range);
FOUNDATION_EXPORT MTDDRange MTDDRangeFromString(NSString *aString);

@interface NSValue (NSValueDDRangeExtensions)

+ (NSValue *)valueWithDDRange:(MTDDRange)range;
- (MTDDRange)ddrangeValue;

@end
