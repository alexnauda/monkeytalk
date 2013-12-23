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

#import "MTParseCsv.h"


@implementation MTParseCsv

+ (NSArray *)readFile:(NSString *)filePath error:(NSError **)error 
{
    if ( nil == filePath ) return nil;
    
     NSString *contentString = [NSString stringWithContentsOfFile:filePath encoding:NSUTF8StringEncoding error:nil];
     
     NSArray *csvLines = [contentString componentsSeparatedByString:@"\n"];
     NSArray *keysArr = nil;
    NSMutableArray *rowsArray = [[NSMutableArray alloc] initWithCapacity:[csvLines count]-1];
     
     for (int i = 0; i < [csvLines count]; i++) {
         //NSLog(@"String: %@",[csvLines objectAtIndex:i]);
         NSArray *tempArr = [[csvLines objectAtIndex:i] componentsSeparatedByString:@","];
     
     
     //NSCharacterSet quotes = [NSCharacterSet characterSetWithCharactersInString:@"\""];
     //[tempArr stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
     
     
         //NSLog(@"tempArr: %@",tempArr);
          NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
     
         if (keysArr == nil) {
                keysArr = [[csvLines objectAtIndex:i] componentsSeparatedByString:@","];
         } else{
                for (int j = 0; j < [keysArr count]; j++) {
                    NSString *keyString = [keysArr objectAtIndex:j];
                    NSString *valueString = [NSString stringWithFormat:@"%@", [tempArr objectAtIndex:j]];
     
                    keyString = [keyString stringByTrimmingCharactersInSet:[NSCharacterSet newlineCharacterSet]];
                    valueString = [valueString stringByTrimmingCharactersInSet:[NSCharacterSet newlineCharacterSet]];
                    
                    keyString = [NSString stringWithFormat:@"${%@}",keyString];
     
                    [dict setValue:valueString forKey:keyString];
                }
             [rowsArray addObject:dict];
         }
     
         //NSLog(@"Temp: %@ %i",dict,[csvLines count]);
     }
     
     //NSLog(@"Temp: %@ %i",rowsArray,[csvLines count]); 
     
    return rowsArray;
}

@end
