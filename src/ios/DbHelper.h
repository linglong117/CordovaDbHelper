/*
 *
 */
#import <Foundation/Foundation.h>


#import "sqlite3.h"
#import <Cordova/CDVPlugin.h>
#import <Cordova/CDVJSON.h>
#import "AppDelegate.h"
#import "DbHelper.h"


enum WebSQLError {
    UNKNOWN_ERR = 0,
    DATABASE_ERR = 1,
    VERSION_ERR = 2,
    TOO_LARGE_ERR = 3,
    QUOTA_ERR = 4,
    SYNTAX_ERR = 5,
    CONSTRAINT_ERR = 6,
    TIMEOUT_ERR = 7
};


typedef int WebSQLError;


@interface DbHelper : CDVPlugin {
    NSMutableDictionary *openDBs;
    
    sqlite3		*database;
    NSString	*databasePath;
    NSOperationQueue	*opQueue;

}

@property (nonatomic, copy) NSMutableDictionary *openDBs;
@property (nonatomic, retain) NSString *appDocsPath;



- (NSString *)databaseFullPath;
- (NSArray *)getMainMenuData;
- (NSArray *)productDataBySNo:(NSInteger)sno;
- (BOOL)prepareDatabase;

+(NSMutableDictionary *) objectFromJSONString:(NSString *)jsonString;


// Open / Close
-(void) open: (CDVInvokedUrlCommand*)command;
-(void) close: (CDVInvokedUrlCommand*)command;
//-(void) delete: (CDVInvokedUrlCommand*)command;

// Batch processing interface
-(void) backgroundExecuteSqlBatch: (CDVInvokedUrlCommand*)command;
-(void) executeSqlBatch: (CDVInvokedUrlCommand*)command;

// Single requests interface
-(void) backgroundExecuteSql:(CDVInvokedUrlCommand*)command;
-(void) executeSql:(CDVInvokedUrlCommand*)command;

// Perform the SQL request
-(CDVPluginResult*) executeSqlWithDict: (NSMutableDictionary*)dict andArgs: (NSMutableDictionary*)dbargs;

-(CDVPluginResult*) executeSqlWithDictSE: (NSMutableArray*)options action:(NSString*)action;


-(id) getDBPath:(id)dbFile;

+(NSDictionary *)captureSQLiteErrorFromDb:(sqlite3 *)db;

+(int)mapSQLiteErrorCode:(int)code;

// LIBB64
+(id) getBlobAsBase64String:(const char*) blob_chars
                 withlength: (int) blob_length;
// LIBB64---END

#pragma custom code
- (void)doParserjson:(CDVInvokedUrlCommand*)command;

- (void)query:(CDVInvokedUrlCommand*)command;
- (void)put:(CDVInvokedUrlCommand*)command;
- (void)get:(CDVInvokedUrlCommand*)command;
- (void)post:(CDVInvokedUrlCommand*)command;
- (void)delete:(CDVInvokedUrlCommand*)command;



@end
