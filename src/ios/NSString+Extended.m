//
//  NSString+Extended.m
//  MCS
//
//  Created by yilong xie on 13-11-27.
//  Copyright (c) 2013年 yilong xie. All rights reserved.
//

#import "NSString+Extended.h"

@implementation NSString(Extended)


- (NSString *)StringTrimAll{
    //NSString* str = @" [d da fa]     ";
    NSString *res = [self stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
    return res;
}


//NSString *trimmedString = [string stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
//
//
//NSString* result = [yourString stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];

-(id)JSONValue
{
    NSData* data = [self dataUsingEncoding:NSUTF8StringEncoding];
    __autoreleasing NSError* error = nil;
    id result = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:&error];
    if (error != nil) return nil;
    return result;
}


-(NSData*)JSONString
{
    NSError* error = nil;
    id result = [NSJSONSerialization dataWithJSONObject:self
                                                options:kNilOptions error:&error];
    if (error != nil) return nil;
    return result;
}

//NSDictionary to JSON NSString
//This same technique can be applied for NSArray .
-(NSString*)JSONValueFromObj:(id)obj
{
    //NSDictionary *myDictionary = [NSDictionary dictionaryWithObject:@&quot;Hello&quot; forKey:@&quot;World&quot;];
    NSError *error;
    NSString *strValue = [NSString string];
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:obj
                                                   options:0
                                                     error:&error];
    if (!jsonData) {
        NSLog(@"JSON error: %@", error);
    } else {
    
        NSString *JSONString = [[NSString alloc] initWithBytes:[jsonData bytes] length:[jsonData length] encoding:NSUTF8StringEncoding];
        NSLog(@"JSON OUTPUT: %@",JSONString);
        strValue = JSONString;
    }
    return strValue;
}


//JSON NSData to NSDictionary
//This same technique can be applied for NSArray .
-(NSData*)JSONStringFromStr:(NSString*)jsonString
{
    //NSString *jsonString = @&quot;{ \&quot;World\&quot; : \&quot;Hello\&quot; }&quot;;

    NSData *jsonData = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    NSError *error = nil;
    
   // NSDictionary *myDictionary = [NSJSONSerialization JSONObjectWithData:jsonData options:NSJSONReadingMutableContainers error:&amp;error];
    id obj = [NSJSONSerialization JSONObjectWithData:jsonData options:NSJSONReadingMutableContainers error:&error];
    
    if(!obj) {
        NSLog(@"%@",error);
    }
    else {
        //Do Something
        NSLog(@"%@", obj);
    }
    if (error != nil) return nil;
    return obj;
}



@end
