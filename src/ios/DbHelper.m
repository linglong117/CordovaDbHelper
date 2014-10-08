/*
 * Copyright (C) 2011-2014 ealing
 * Copyright (C) 2014 ealing
 *
 *
 *
 */

#import "DbHelper.h"
#include <regex.h>
#import "NSString+Extended.h"


//LIBB64
typedef enum
{
    step_A, step_B, step_C
} base64_encodestep;

typedef struct
{
    base64_encodestep step;
    char result;
    int stepcount;
} base64_encodestate;

static void base64_init_encodestate(base64_encodestate* state_in)
{
    state_in->step = step_A;
    state_in->result = 0;
    state_in->stepcount = 0;
}

static char base64_encode_value(char value_in)
{
    static const char* encoding = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    if (value_in > 63) return '=';
    return encoding[(int)value_in];
}

static int base64_encode_block(const char* plaintext_in,
                               int length_in,
                               char* code_out,
                               base64_encodestate* state_in,
                               int line_length)
{
    const char* plainchar = plaintext_in;
    const char* const plaintextend = plaintext_in + length_in;
    char* codechar = code_out;
    char result;
    char fragment;
    
    result = state_in->result;
    
    switch (state_in->step)
    {
            while (1)
            {
            case step_A:
                if (plainchar == plaintextend)
                {
                    state_in->result = result;
                    state_in->step = step_A;
                    return codechar - code_out;
                }
                fragment = *plainchar++;
                result = (fragment & 0x0fc) >> 2;
                *codechar++ = base64_encode_value(result);
                result = (fragment & 0x003) << 4;
            case step_B:
                if (plainchar == plaintextend)
                {
                    state_in->result = result;
                    state_in->step = step_B;
                    return codechar - code_out;
                }
                fragment = *plainchar++;
                result |= (fragment & 0x0f0) >> 4;
                *codechar++ = base64_encode_value(result);
                result = (fragment & 0x00f) << 2;
            case step_C:
                if (plainchar == plaintextend)
                {
                    state_in->result = result;
                    state_in->step = step_C;
                    return codechar - code_out;
                }
                fragment = *plainchar++;
                result |= (fragment & 0x0c0) >> 6;
                *codechar++ = base64_encode_value(result);
                result  = (fragment & 0x03f) >> 0;
                *codechar++ = base64_encode_value(result);
                
                if(line_length > 0)
                {
                    ++(state_in->stepcount);
                    if (state_in->stepcount == line_length/4)
                    {
                        *codechar++ = '\n';
                        state_in->stepcount = 0;
                    }
                }
            }
    }
    /* control should not reach here */
    return codechar - code_out;
}

static int base64_encode_blockend(char* code_out,
                                  base64_encodestate* state_in)
{
    char* codechar = code_out;
    
    switch (state_in->step)
    {
        case step_B:
            *codechar++ = base64_encode_value(state_in->result);
            *codechar++ = '=';
            *codechar++ = '=';
            break;
        case step_C:
            *codechar++ = base64_encode_value(state_in->result);
            *codechar++ = '=';
            break;
        case step_A:
            break;
    }
    *codechar++ = '\n';
    
    return codechar - code_out;
}

//LIBB64---END

static void sqlite_regexp(sqlite3_context* context, int argc, sqlite3_value** values) {
    int ret;
    regex_t regex;
    char* reg = (char*)sqlite3_value_text(values[0]);
    char* text = (char*)sqlite3_value_text(values[1]);
    
    if ( argc != 2 || reg == 0 || text == 0) {
        sqlite3_result_error(context, "SQL function regexp() called with invalid arguments.\n", -1);
        return;
    }
    
    ret = regcomp(&regex, reg, REG_EXTENDED | REG_NOSUB);
    if ( ret != 0 ) {
        sqlite3_result_error(context, "error compiling regular expression", -1);
        return;
    }
    
    ret = regexec(&regex, text , 0, NULL, 0);
    regfree(&regex);
    
    sqlite3_result_int(context, (ret != REG_NOMATCH));
}


@implementation DbHelper

@synthesize openDBs;
@synthesize appDocsPath;
@synthesize seDbPath;

-(CDVPlugin*) initWithWebView:(UIWebView*)theWebView
{
    self = (DbHelper*)[super initWithWebView:theWebView];
    if (self) {
        openDBs = [NSMutableDictionary dictionaryWithCapacity:0];
#if !__has_feature(objc_arc)
        [openDBs retain];
#endif
        
        NSString *docs = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex: 0];
        NSLog(@"Detected docs path: %@", docs);
        [self setAppDocsPath:docs];
    }
    return self;
}


//========================================
#pragma custom code

- (void) copyDatabase:(id)dbPath{
    
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSError *error;
    
    //NSString *dbPath = [self getDBPath];
    
    BOOL success = [fileManager fileExistsAtPath:dbPath];
    
    if(!success) {
        
        NSString *defaultDBPath = [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:@"www/"];
        
        defaultDBPath = [defaultDBPath stringByAppendingPathComponent:@"smartevent.db"];
        
        success = [fileManager copyItemAtPath:defaultDBPath toPath:dbPath error:&error];
        
        if (!success)
            NSAssert1(0, @"Failed to create writable database file with message '%@'.", [error localizedDescription]);
    }
}

- (Boolean) reCopyDatabase:(id)dbPath{
    
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSError *error;
    
    //NSString *dbPath = [self getDBPath];
    
    BOOL success = [fileManager fileExistsAtPath:dbPath];
    Boolean flag=false;
    
    if(!success) {
        NSString *defaultDBPath = [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:@"www/"];
        defaultDBPath = [defaultDBPath stringByAppendingPathComponent:@"smartevent.db"];
        success = [fileManager copyItemAtPath:defaultDBPath toPath:dbPath error:&error];
        if (!success)
        {
            flag=false;
            NSAssert1(0, @"Failed to create writable database file with message '%@'.", [error localizedDescription]);
        }else
        {
            flag=true;
        }
    }else{
        //先删除，再重新拷贝
        success= [fileManager removeItemAtPath:dbPath error:&error];
        
        NSString *defaultDBPath = [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:@"www/"];
        defaultDBPath = [defaultDBPath stringByAppendingPathComponent:@"smartevent.db"];
        success = [fileManager copyItemAtPath:defaultDBPath toPath:dbPath error:&error];
        if (!success)
        {
            flag=false;
            NSAssert1(0, @"Failed to create writable database file with message '%@'.", [error localizedDescription]);
        }else
        {
            flag=true;
        }
    }
    return flag;
}

/*
 //test
 - (CDVPluginResult*)get:(CDVInvokedUrlCommand*)command
 {
 CDVPluginResult *pluginResult = nil;
 [self open:command];
 
 
 //NSString *echo = [command.arguments objectAtIndex:0];
 DataManager *dm = [DataManager sharedManager];
 //database copy
 [dm open:command];
 NSMutableDictionary *options = [command.arguments objectAtIndex:0];
 pluginResult = [dm executeSqlWithDictSE: options];
 //pluginResult = [dm query:command];
 
 NSLog(@"here");
 NSMutableArray *array = [pluginResult.message objectForKey:@"rows"];
 NSString *strvalue = [[array objectAtIndex:0] objectForKey:@"UserId"];
 //return [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
 NSMutableDictionary *resultSet = [[NSMutableDictionary alloc] init];
 [resultSet setObject:@"userid" forKey:@"rows"];
 [resultSet setObject:@"1" forKey:@"rowsAffected"];
 
 return [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:resultSet];
 echo = @"调用原生方法返回数据成功";
 
 NSMutableArray *array = [[NSMutableArray alloc] init];
 [array addObject:echo];
 [array addObject:@"001"];
 [array addObject:@"007"];
 
 if (echo != nil && [echo length] > 0) {
 //pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:echo];
 pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:array];
 
 } else {
 pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
 }
 [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
 
 }
 */

- (void)put:(CDVInvokedUrlCommand*)command
{
    [self open:command];
    [self backgroundExecuteSqlBatch:command];
    
}

- (NSString*)insertSE:(NSMutableArray*)options
{
    //NSMutableString *sql = [NSMutableString string];
    NSString *tbName = [NSString string];
    NSString *dbName = [NSString string];
    NSMutableArray *columns = [NSMutableArray array];
    NSMutableArray *whereArgs = [NSMutableArray array];
    
    tbName = [options objectAtIndex:1];
    dbName = [options objectAtIndex:0];
    columns = [options objectAtIndex:2];
    //where = [options objectAtIndex:3];
    whereArgs = [options objectAtIndex:3];
    
    NSString *sql = [NSString string];
    sql = [@"INSERT INTO " stringByAppendingFormat:@" %@ (",tbName];
    
    if (columns && [columns count]>0) {
        for (int i=0;i<[columns count];i++) {
            NSString *strcol = [columns objectAtIndex:i];
            sql = [sql stringByAppendingFormat:@"%@,",strcol];
        }
        sql = [sql substringToIndex:[sql length]-1];
    }
    
    sql = [sql stringByAppendingString:@") VALUES ("];
    
    if (columns && [columns count]>0) {
        for (int i=0;i<[columns count];i++) {
            sql = [sql stringByAppendingFormat:@"%@,",@"?"];
        }
        sql = [sql substringToIndex:[sql length]-1];
    }
    sql = [sql stringByAppendingString:@")"];
    
    /*
     var aSql = [];
     var sql = "INSERT INTO " + table + " (";
     var sql1 = "(";
     for (var i in values){
     sql1 += " ?,";
     sql += i + ",";
     aSql.push( values[i] );
     }
     sql = sql.substring(0, sql.length - 1) + ") VALUES ";
     sql1 = sql1.substring(0, sql1.length - 1) + ")";
     sql += sql1;
     aSql.unshift(sql);
     if (compile == true){
     return aSql
     }
     else{
     this.executeSql(aSql, function(res){ if (success)success(res.insertId); }, error);
     }*/
    
    return sql;
}

- (NSString*)updateSE:(NSMutableArray*)options
{
    /*
     var sql = "UPDATE " + table + " SET ";
     var aSql = [];
     for (var i in values){
     sql += i + " = ? ,";
     aSql.push( values[i] );
     }
     sql = sql.substring(0, sql.length - 1) + " ";
     if (where){
     sql += " WHERE " +  where;
     }
     if (whereArgs instanceof Array){
     aSql = aSql.concat(whereArgs);
     }
     aSql.unshift(sql);
     if (compile == true){
     return aSql;
     }
     else{
     this.executeSql(aSql, function(res){if (success)success(res.rowsAffected); }, error);
     }
     */
    
    NSString *tbName = [NSString string];
    NSString *dbName = [NSString string];
    NSMutableArray *columnsValues = [NSMutableArray array];
    NSString *where = [NSString string];
    NSMutableArray *whereArgs = [NSMutableArray array];
    dbName = [options objectAtIndex:0];
    tbName = [options objectAtIndex:1];
    columnsValues = [options objectAtIndex:2];
    where = [options objectAtIndex:3];
    whereArgs = [options objectAtIndex:4];
    NSMutableArray *_whereArgs = [NSMutableArray array];
    //    NSLog(@"col value  %@ >>>",[columnsValues description]);
    NSString *sql = [NSString string];
    sql = [@"UPDATE " stringByAppendingFormat:@" %@  SET ",tbName];
    /*
     // NSMutableDictionary *columnsValues = [NSMutableDictionary dictionary];
     
     if (columnsValues && [columnsValues count]) {
     //得到词典中所有KEY值
     NSEnumerator * enumeratorKey = [columnsValues keyEnumerator];
     //快速枚举遍历所有KEY的值
     for (NSObject *object in enumeratorKey) {
     //NSLog(@"遍历KEY的值: %@",object);
     
     NSString *strcol = [NSString stringWithFormat:@"%@",object];
     sql = [sql stringByAppendingFormat:@"%@ =?,",strcol];
     [_whereArgs addObject:[columnsValues objectForKey:@"strcol"]];
     }
     
     //得到词典中所有Value值
     NSEnumerator * enumeratorValue = [columnsValues objectEnumerator];
     
     //快速枚举遍历所有Value的值
     for (NSObject *object in enumeratorValue) {
     NSLog(@"遍历Value的值: %@",object);
     [_whereArgs addObject:[NSString stringWithFormat:@"%@",object]];
     }
     //通过KEY找到value
     NSObject *object = [columnsValues objectForKey:@"name"];
     
     if (object != nil) {
     NSLog(@"通过KEY找到的value是: %@",object);
     }
     }*/
    
    if (columnsValues && [columnsValues count]>0) {
        for (int i=0;i<[columnsValues count];i++) {
            //id obj = [columnsValues objectAtIndex:i];
            //NSLog(@"col value  %@ >>>",[obj description]);
            NSString *strcol = [columnsValues objectAtIndex:i];
            sql = [sql stringByAppendingFormat:@"%@ =?,",strcol];
        }
        sql = [sql substringToIndex:[sql length]-1];
    }
    if (where) {
        if(whereArgs && [whereArgs count]>0)
        {
            sql =[sql stringByAppendingFormat:@"  WHERE  %@", where];
            
            for (int j=0; j<[whereArgs count]; j++) {
                [_whereArgs addObject:[whereArgs objectAtIndex:j]];
            }
        }
    }
    return sql;
}


- (NSString*)deleteSE:(NSMutableArray*)options
{
    /*
     var sql = "DELETE FROM " + table;
     if (where){
     sql += " WHERE " + where;
     }
     var aSql = [];
     aSql.push(sql);
     if (whereArgs){
     aSql = aSql.concat(whereArgs);
     }
     if (compile == true){
     return aSql;
     }
     else{
     this.executeSql(aSql, function(res){if (success)success(res.rowsAffected); }, error);
     }
     */
    
    NSString *tbName = [NSString string];
    NSString *dbName = [NSString string];
    //NSMutableArray *columns = [NSMutableArray array];
    NSString *where = [NSString string];
    NSMutableArray *whereArgs = [NSMutableArray array];
    dbName = [options objectAtIndex:0];
    
    tbName = [options objectAtIndex:1];
    //columns = [options objectAtIndex:2];
    where = [options objectAtIndex:2];
    whereArgs = [options objectAtIndex:3];
    
    NSString *sql = [NSString string];
    sql = [@"DELETE FROM " stringByAppendingFormat:@" %@  ",tbName];
    
    if (where) {
        if(whereArgs && [whereArgs count]>0)
        {
            sql =[sql stringByAppendingFormat:@" WHERE  %@", where];
        }
    }
    
    return sql;
}


- (NSString*)selectSE:(NSMutableArray*)options
{
    // NSMutableArray *options = [command.arguments objectAtIndex:0];
    NSString *dbName = [options objectAtIndex:0];
    NSString *tbName = [options objectAtIndex:1];
    NSMutableArray *columns = [options objectAtIndex:2];
    NSString *where = [options objectAtIndex:3];
    NSMutableArray *whereArgs = [options objectAtIndex:4];
    NSString *groupBy = [options objectAtIndex:5];
    NSString *having = [options objectAtIndex:6];
    NSString *orderBy = [options objectAtIndex:7];
    NSString *limit = [options objectAtIndex:8];
    /*
     (table, columns, where, whereArgs, groupBy, having, orderBy, limit, success, error, compile) {
     var sql = "SELECT ";
     var aSql = [];
     if (columns){
     for (var i in columns){
     sql += columns[i] + ",";
     }
     sql = sql.substring(0, sql.length - 1);
     }
     else {
     sql += " * ";
     }
     sql += " FROM " + table + " ";
     if (where){
     if (whereArgs instanceof Array){
     aSql = aSql.concat(whereArgs);
     }
     sql += " WHERE " + where;
     }
     if (groupBy){
     sql += " GROUP BY " + groupBy + " ";
     }
     if (having){
     sql += " HAVING " + having + " ";
     }
     if (orderBy){
     sql += " ORDER BY " + orderBy + " ";
     }
     if (limit){
     sql += " LIMIT " + limit + " ";
     }
     aSql.unshift(sql);
     if (compile == true){
     return aSql;
     }
     else{
     this.executeSql(aSql, success, error);
     }
     */
    
    NSString *sql = [NSString stringWithFormat:@"SELECT "];
    if (columns && [columns count]>0) {
        for (int i=0;i<[columns count];i++) {
            //sql = [sql stringByAppendingFormat:@""];
            //sql =[sql stringWithFormat:@"%@" ,[columns objectAtIndex:i]];
            NSString *strcol = [columns objectAtIndex:i];
            sql = [sql stringByAppendingFormat:@"%@,",strcol];
        }
        sql = [sql substringToIndex:[sql length]-1];
        
    }else
    {
        sql = [sql stringByAppendingFormat:@" %@ ",@"*"];
    }
    
    if (tbName && [tbName length]) {
        sql = [sql stringByAppendingFormat:@" FROM %@ ",tbName];
    }
    
    if (where){
        
        if(whereArgs && [whereArgs count]>0)
        {
            sql =[sql stringByAppendingFormat:@" WHERE  %@", where];
        }
    }
    if (groupBy && [groupBy length]>0){
        
        sql =[sql stringByAppendingFormat:@" GROUP BY  %@", groupBy];
    }
    if (having && [having length]>0){
        sql =[sql stringByAppendingFormat:@" HAVING  %@", having];
    }
    if (orderBy && [orderBy length]>0){
        sql =[sql stringByAppendingFormat:@" ORDER BY  %@", orderBy];
    }
    if (limit && [limit length]>0){
        sql =[sql stringByAppendingFormat:@" LIMIT  %@", limit];
    }
    return sql;
}

- (void)resetdb:(CDVInvokedUrlCommand*)command
{
    NSLog(@"resetdb");
    CDVPluginResult* pluginResult = nil;
    NSMutableArray *options = [command.arguments objectAtIndex:0];
    NSString *dbname = [options objectAtIndex:0];
    NSString *dbPath = [self databaseFullPath:dbname];
    databasePath = [NSString stringWithFormat:@"%@",dbPath];
    Boolean result = [self reCopyDatabase:dbPath];
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:result];
    
    [self closeSE:seDbPath];
    [self.commandDelegate sendPluginResult:pluginResult callbackId: command.callbackId];
}



#pragma custom code
//====================================================================

/*
 + (id)sharedManager{
 //
 static id sharedManager = nil;
 if(sharedManager == nil){
 sharedManager = [[self alloc] init];
 }
 return sharedManager;
 //
 }
 - (id)init{
 if (self = [super init]) {
 //opQueue = [[NSOperationQueue alloc] init];
 //[self prepareDatabase];
 
 //self = (DbHelper*)[super initWithWebView:theWebView];
 if (self) {
 openDBs = [NSMutableDictionary dictionaryWithCapacity:0];
 #if !__has_feature(objc_arc)
 [openDBs retain];
 #endif
 
 NSString *docs = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex: 0];
 NSLog(@"Detected docs path: %@", docs);
 [self setAppDocsPath:docs];
 }
 }
 return self;
 }
 */

+(NSMutableDictionary *) objectFromJSONString:(NSString *)jsonString
{
    NSMutableDictionary *jsonDic= [[NSMutableDictionary alloc] init];
    NSError *error = nil;
    NSData *jsonData = [jsonString dataUsingEncoding:NSUTF8StringEncoding allowLossyConversion:YES];
    
    if (jsonData) {
        jsonDic = [NSJSONSerialization JSONObjectWithData:jsonData options:NSJSONReadingMutableLeaves error:&error];
    }
    return jsonDic;
}


#pragma mark Database Method

/*
 - (void) copyDatabase:(id)dbPath{
 
 NSFileManager *fileManager = [NSFileManager defaultManager];
 NSError *error;
 
 //NSString *dbPath = [self getDBPath];
 
 BOOL success = [fileManager fileExistsAtPath:dbPath];
 
 if(!success) {
 
 NSString *defaultDBPath = [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:@"www/"];
 
 defaultDBPath = [defaultDBPath stringByAppendingPathComponent:@"smartevent.db"];
 
 success = [fileManager copyItemAtPath:defaultDBPath toPath:dbPath error:&error];
 
 if (!success)
 NSAssert1(0, @"Failed to create writable database file with message '%@'.", [error localizedDescription]);
 }
 }
 
 - (NSString *) getDBPath
 {
 
 NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory , NSUserDomainMask, YES);
 NSString *documentsDir = [paths objectAtIndex:0];
 return [documentsDir stringByAppendingPathComponent:@"DummyDB"];
 }
 */

- (NSString *)databaseFullPath:(id)dbFile{
    
    if (dbFile == NULL) {
        return NULL;
    }
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    NSString *_databaseFullPath = [documentsDirectory stringByAppendingPathComponent:dbFile];
    return _databaseFullPath;
}

#pragma custom code

- (void)get:(CDVInvokedUrlCommand*)command
{
    
    NSLog(@"get");
    [self open:command];
    [self backgroundExecuteSqlBatch:command];
}

- (void)post:(CDVInvokedUrlCommand*)command
{
    NSLog(@"post");
    [self open:command];
    [self backgroundExecuteSqlBatch:command];
}

- (void)postArray:(CDVInvokedUrlCommand*)command
{
    NSLog(@"postArray");
    
    [self openCustom:[[command.arguments objectAtIndex:0] objectAtIndex:0]];
    
    [self backgroundExecuteSqlBatch:command];
}

-(void) delete: (CDVInvokedUrlCommand*)command
{
    NSLog(@"delete");
    [self openCustom:[[command.arguments objectAtIndex:0] objectAtIndex:0]];
    
    //[self open:command];
    [self backgroundExecuteSqlBatch:command];
}

-(void) deleteArray: (CDVInvokedUrlCommand*)command
{
    NSLog(@"deleteArray");
    [self openCustom:[[command.arguments objectAtIndex:0] objectAtIndex:0]];
    [self backgroundExecuteSqlBatch:command];
}

#pragma custom code


-(id) getDBPath:(id)dbFile {
    if (dbFile == NULL) {
        return NULL;
    }
    NSString *dbPath = [NSString stringWithFormat:@"%@/%@", appDocsPath, dbFile];
    return dbPath;
}

-(void)open: (CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    
    NSMutableArray *options = [command.arguments objectAtIndex:0];
    
    //NSString *dbname = [self getDBPath:[options objectForKey:@"name"]];
    //========custom code===========================
    //NSString *dbname = [options objectForKey:@"dbName"];
    NSString *dbname = [options objectAtIndex:0];
    NSString *dbPath = [self databaseFullPath:dbname];
    databasePath = [NSString stringWithFormat:@"%@",dbPath];
    [self copyDatabase:dbPath];
    
    seDbPath = [NSString stringWithFormat:@"%@",dbPath];
    /*
     int n = sqlite3_open([databasePath UTF8String], &database);
     if (n!=SQLITE_OK) {
     NSLog(@"数据库打开出错...");
     return;
     }else
     {
     NSLog(@"数据库打开成功...");
     }*/
    
    NSValue *dbPointer;
    
    if (dbPath == NULL) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"You must specify database name"];
    }
    else {
        dbPointer = [openDBs objectForKey:dbPath];
        if (dbPointer != NULL) {
            // NSLog(@"Reusing existing database connection");
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Database opened"];
        }
        else {
            const char *name = [dbPath UTF8String];
            // NSLog(@"using db name: %@", dbname);
            sqlite3 *db;
            
            if (sqlite3_open(name, &db) != SQLITE_OK) {
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Unable to open DB"];
                return;
            }
            else {
                // Extra for SQLCipher:
                // const char *key = [@"your_key_here" UTF8String];
                // if(key != NULL) sqlite3_key(db, key, strlen(key));
                sqlite3_create_function(db, "regexp", 2, SQLITE_ANY, NULL, &sqlite_regexp, NULL, NULL);
                
                // Attempt to read the SQLite master table (test for SQLCipher version):
                if(sqlite3_exec(db, (const char*)"SELECT count(*) FROM sqlite_master;", NULL, NULL, NULL) == SQLITE_OK) {
                    dbPointer = [NSValue valueWithPointer:db];
                    [openDBs setObject: dbPointer forKey: dbPath];
                    
                    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Database opened"];
                    
                } else {
                    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Unable to encrypt DB"];
                }
            }
        }
    }
    if (sqlite3_threadsafe()) {
        NSLog(@"Good news: SQLite is thread safe!");
    }
    else {
        NSLog(@"Warning: SQLite is not thread safe.");
    }
    //[self.commandDelegate sendPluginResult:pluginResult callbackId: command.callbackId];
    // NSLog(@"open cb finished ok");
}


-(void)openCustom: (NSMutableArray*)options
{
    CDVPluginResult* pluginResult = nil;
    //NSMutableArray *options = [command objectAtIndex:0];
    //NSString *dbname = [self getDBPath:[options objectForKey:@"name"]];
    //========custom code===========================
    //NSString *dbname = [options objectForKey:@"dbName"];
    NSString *dbname = [options objectAtIndex:0];
    NSString *dbPath = [self databaseFullPath:dbname];
    databasePath = [NSString stringWithFormat:@"%@",dbPath];
    [self copyDatabase:dbPath];
    seDbPath = [NSString stringWithFormat:@"%@",dbPath];

    /*
     int n = sqlite3_open([databasePath UTF8String], &database);
     if (n!=SQLITE_OK) {
     NSLog(@"数据库打开出错...");
     return;
     }else
     {
     NSLog(@"数据库打开成功...");
     }*/
    
    NSValue *dbPointer;
    
    if (dbPath == NULL) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"You must specify database name"];
    }
    else {
        dbPointer = [openDBs objectForKey:dbPath];
        if (dbPointer != NULL) {
            // NSLog(@"Reusing existing database connection");
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Database opened"];
        }
        else {
            const char *name = [dbPath UTF8String];
            // NSLog(@"using db name: %@", dbname);
            sqlite3 *db;
            
            if (sqlite3_open(name, &db) != SQLITE_OK) {
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Unable to open DB"];
                return;
            }
            else {
                // Extra for SQLCipher:
                // const char *key = [@"your_key_here" UTF8String];
                // if(key != NULL) sqlite3_key(db, key, strlen(key));
                sqlite3_create_function(db, "regexp", 2, SQLITE_ANY, NULL, &sqlite_regexp, NULL, NULL);
                
                // Attempt to read the SQLite master table (test for SQLCipher version):
                if(sqlite3_exec(db, (const char*)"SELECT count(*) FROM sqlite_master;", NULL, NULL, NULL) == SQLITE_OK) {
                    dbPointer = [NSValue valueWithPointer:db];
                    [openDBs setObject: dbPointer forKey: dbPath];
                    
                    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Database opened"];
                    
                } else {
                    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Unable to encrypt DB"];
                }
            }
        }
    }
    if (sqlite3_threadsafe()) {
        NSLog(@"Good news: SQLite is thread safe!");
    }
    else {
        NSLog(@"Warning: SQLite is not thread safe.");
    }
    //[self.commandDelegate sendPluginResult:pluginResult callbackId: command.callbackId];
    // NSLog(@"open cb finished ok");
}

-(void) close: (CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSMutableDictionary *options = [command.arguments objectAtIndex:0];
    
    NSString *dbPath = [self getDBPath:[options objectForKey:@"path"]];
    if (dbPath == NULL) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"You must specify database path"];
    }
    else {
        NSValue *val = [openDBs objectForKey:dbPath];
        sqlite3 *db = [val pointerValue];
        if (db == NULL) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Specified db was not open"];
        }
        else {
            sqlite3_close (db);
            [openDBs removeObjectForKey:dbPath];
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"DB closed"];
        }
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId: command.callbackId];
}

-(void) closeSE: (NSString*)path
{
    NSString *dbPath = path;
    if (dbPath == NULL) {
        //pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"You must specify database path"];
        NSLog(@"You must specify database path");
    }
    else {
        NSValue *val = [openDBs objectForKey:dbPath];
        sqlite3 *db = [val pointerValue];
        if (db == NULL) {
            //pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Specified db was not open"];
            NSLog(@"Specified db was not open");
        }
        else {
            sqlite3_close (db);
            [openDBs removeObjectForKey:dbPath];
            //pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"DB closed"];
            NSLog(@"DB closed");
        }
    }
    //[self.commandDelegate sendPluginResult:pluginResult callbackId: command.callbackId];
}


/*
 -(void) delete: (CDVInvokedUrlCommand*)command
 {
 
 CDVPluginResult* pluginResult = nil;
 NSMutableDictionary *options = [command.arguments objectAtIndex:0];
 
 NSString *dbPath = [self getDBPath:[options objectForKey:@"path"]];
 if(dbPath==NULL) {
 pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"You must specify database path"];
 } else {
 if([[NSFileManager defaultManager]fileExistsAtPath:dbPath]) {
 [[NSFileManager defaultManager]removeItemAtPath:dbPath error:nil];
 [openDBs removeObjectForKey:dbPath];
 pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"DB deleted"];
 } else {
 pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"The database does not exist on that path"];
 }
 }
 [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
 
 }
 */

-(void) backgroundExecuteSqlBatch: (CDVInvokedUrlCommand*)command
{
    [self executeSqlBatchSE:command];
    //2014-10-08 13:54:10

//    [self.commandDelegate runInBackground:^{
//        //[self executeSqlBatch: command];
//        //NSString *strAction = command.methodName;
//        [self executeSqlBatchSE:command];
//        NSLog(@"======backgroundExecuteSqlBatch=========");
//    }];
}


-(NSMutableArray*)getOptionsArray:(CDVInvokedUrlCommand*)command
{
    
    NSMutableArray *array = [[NSMutableArray alloc] init];
    
    NSMutableArray *options = [command.arguments objectAtIndex:0];
    NSString *strAction = command.methodName;
    
    NSString *dbName = [NSString string];
    
    NSString *tbName = [NSString string];
    NSMutableArray *columns = [NSMutableArray array];
    NSString *where = [NSString string];
    //NSMutableArray *whereArgs = [NSMutableArray array];
    //NSMutableArray *_whereArgs = [NSMutableArray array];
    
    NSString *groupBy = [NSString string];
    NSString *having = [NSString string];
    NSString *orderBy = [NSString string];;
    NSString *limit = [NSString string];
    
    NSMutableArray *querys = [NSMutableArray array];
    NSMutableArray *_querys = [NSMutableArray array];
    
    
    if ([strAction isEqualToString:@"get"]) {
        //NSMutableArray *getArray = [[NSMutableArray alloc] init];
        
        dbName = [options objectAtIndex:0];
        tbName = [options objectAtIndex:1];
        columns = [options objectAtIndex:2];
        where = [options objectAtIndex:3];
        querys = [options objectAtIndex:4];
        groupBy = [options objectAtIndex:5];
        having = [options objectAtIndex:6];
        orderBy = [options objectAtIndex:7];
        limit = [options objectAtIndex:8];
        
        [array addObject:options];
    }else if ([strAction isEqualToString:@"put"])
    {
        dbName = [options objectAtIndex:0];
        tbName = [options objectAtIndex:1];
        columns = [options objectAtIndex:2];
        querys = [options objectAtIndex:3];
        
        if (querys && [querys count]>0) {
            for (int i=0; i<[querys count]; i++) {
                NSMutableArray *putArray = [[NSMutableArray alloc] init];
                [putArray addObject:dbName];
                [putArray addObject:tbName];
                [putArray addObject:columns];
                [putArray addObject:[querys objectAtIndex:i]];
                [array addObject:putArray];
            }
        }
    }else if ([strAction isEqualToString:@"post"])
    {
        NSMutableDictionary *columnsValues = [NSMutableDictionary dictionary];
        NSMutableArray *_options = [[NSMutableArray alloc] init];
        dbName = [options objectAtIndex:0];
        tbName = [options objectAtIndex:1];
        columnsValues = [options objectAtIndex:2];
        where = [options objectAtIndex:3];
        querys = [options objectAtIndex:4];
        
        if (columnsValues && [columnsValues count]) {
            //得到词典中所有KEY值
            NSEnumerator * enumeratorKey = [columnsValues keyEnumerator];
            //快速枚举遍历所有KEY的值
            for (NSObject *object in enumeratorKey) {
                //NSLog(@"遍历KEY的值: %@",object);
                NSString *strcol = [NSString stringWithFormat:@"%@",object];
                [columns addObject:strcol];
                NSString *strValue = [NSString stringWithFormat:@"%@",[columnsValues objectForKey:strcol]];
                [_querys addObject:strValue];
            }
        }
        
        if ((querys && [querys count])) {
            for (int i=0; i<[querys count]; i++) {
                [_querys addObject:[querys objectAtIndex:i]];
            }
        }
        [_options addObject:dbName];
        [_options addObject:tbName];
        [_options addObject:columns];
        [_options addObject:where];
        [_options addObject:_querys];
        
        [array addObject:_options];
        
    }else if ([strAction isEqualToString:@"postArray"])
    {
        for (int i=0; i<[options count]; i++) {
            NSMutableArray *arr  = [options objectAtIndex:i];
            
            
            NSMutableDictionary *columnsValues = [NSMutableDictionary dictionary];
            NSMutableArray *_columns = [NSMutableArray array];
            NSMutableArray *_querys_ = [NSMutableArray array];
            
            
            NSMutableArray *_options = [[NSMutableArray alloc] init];
            dbName = [arr objectAtIndex:0];
            tbName = [arr objectAtIndex:1];
            columnsValues = [arr objectAtIndex:2];
            where = [arr objectAtIndex:3];
            querys = [arr objectAtIndex:4];
            
            if (columnsValues && [columnsValues count]) {
                //得到词典中所有KEY值
                NSEnumerator * enumeratorKey = [columnsValues keyEnumerator];
                //快速枚举遍历所有KEY的值
                for (NSObject *object in enumeratorKey) {
                    //NSLog(@"遍历KEY的值: %@",object);
                    NSString *strcol = [NSString stringWithFormat:@"%@",object];
                    [_columns addObject:strcol];
                    NSString *strValue = [NSString stringWithFormat:@"%@",[columnsValues objectForKey:strcol]];
                    [_querys_ addObject:strValue];
                }
            }
            
            if ((querys && [querys count])) {
                for (int i=0; i<[querys count]; i++) {
                    [_querys_ addObject:[querys objectAtIndex:i]];
                }
            }
            [_options addObject:dbName];
            [_options addObject:tbName];
            [_options addObject:_columns];
            [_options addObject:where];
            [_options addObject:_querys_];
            [array addObject:_options];
        }
        
    }else if ([strAction isEqualToString:@"delete"])
    {
        dbName = [options objectAtIndex:0];
        tbName = [options objectAtIndex:1];
        where = [options objectAtIndex:2];
        querys = [options objectAtIndex:3];
        [array addObject:options];
    }else if ([strAction isEqualToString:@"deleteArray"])
    {
        for (int i=0; i<[options count]; i++) {
            
            NSMutableArray *_options = [[NSMutableArray alloc] init];
            
            NSMutableArray *arr = [options objectAtIndex:i];
            NSMutableArray *_querys_ = [NSMutableArray array];
            dbName = [arr objectAtIndex:0];
            tbName = [arr objectAtIndex:1];
            where = [arr objectAtIndex:2];
            _querys_ = [arr objectAtIndex:3];
            
            [_options addObject:dbName];
            [_options addObject:tbName];
            [_options addObject:where];
            [_options addObject:_querys_];
            
            [array addObject:_options];
        }
    }
    return array;
}

- (NSData *)toJSONData:(id)theData{
    
    NSError *error = nil;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:theData
                                                       options:NSJSONWritingPrettyPrinted
                                                         error:&error];
    if ([jsonData length] > 0 && error == nil){
        return jsonData;
    }else{
        return nil;
    }
}

-(NSMutableDictionary*)getSelectData:(NSMutableArray*)sourceData
{
    NSMutableDictionary *resultDic = [[NSMutableDictionary alloc] init];
    
    //NSString *resutValue = [NSString stringWithFormat:@"%@",resultArray];
    NSString *resutValue = [NSString string];
    //NSDictionary *myDictionary = [NSDictionary dictionaryWithObject:@&quot;Hello&quot; forKey:@&quot;World&quot;];
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:sourceData
                                                       options:0
                                                         error:&error];
    if (!jsonData) {
        NSLog(@"JSON error: %@", error);
    } else {
        NSString *JSONString = [[NSString alloc] initWithBytes:[jsonData bytes] length:[jsonData length] encoding:NSUTF8StringEncoding];
        NSLog(@"JSON OUTPUT: %@",JSONString);
        resutValue = JSONString;
    }
    NSData *jsonDataNew = [resutValue dataUsingEncoding:NSUTF8StringEncoding];
    //NSError *error = nil;
    resultDic = [NSJSONSerialization JSONObjectWithData:jsonDataNew options:NSJSONReadingMutableContainers error:&error];
    
    if(!resultDic) {
        NSLog(@"%@",error);
    }
    else {
        //Do Something
        NSLog(@"%@", resultDic);
    }
    return resultDic;
}

-(void) executeSqlBatchSE: (CDVInvokedUrlCommand*)command
{
    // NSMutableArray *options = [command.arguments objectAtIndex:0];
    NSString *strAction = command.methodName;
    NSMutableArray *options = [self getOptionsArray:command];
    NSMutableArray *results = [NSMutableArray arrayWithCapacity:0];
    CDVPluginResult* pluginResult;
    @synchronized(self) {
        for (int i=0;i<[options count];i++) {
            //pluginResult = [self executeSqlWithDictSE:command];
            pluginResult = [self executeSqlWithDictSE:[options objectAtIndex:i] action:strAction];
            
            if ([pluginResult.status intValue] == CDVCommandStatus_ERROR) {
                /* add error with result.message: */
                NSMutableDictionary *r = [NSMutableDictionary dictionaryWithCapacity:0];
                [r setObject:@"0" forKey:@"qid"];
                [r setObject:@"error" forKey:@"type"];
                [r setObject:pluginResult.message forKey:@"error"];
                [r setObject:pluginResult.message forKey:@"result"];
                [results addObject: r];
            } else {
                /* add result with result.message: */
                NSMutableDictionary *r = [NSMutableDictionary dictionaryWithCapacity:0];
                [r setObject:@"0" forKey:@"qid"];
                [r setObject:@"success" forKey:@"type"];
                [r setObject:pluginResult.message forKey:@"result"];
                [results addObject: r];
            }
        }
        //pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:results];
        if ([strAction isEqualToString:@"get"]) {
            //Select
            NSMutableArray *resultArray = pluginResult.message;
            NSMutableDictionary  *resultDic = [self getSelectData:resultArray];
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:resultDic];
            
        }else  if ([strAction isEqualToString:@"put"]) {
            //insert
            NSMutableDictionary *resultDic = pluginResult.message;
            NSInteger  code = [[resultDic objectForKey:@"code"] integerValue];
            if (code!=0) {
                [resultDic setObject:[NSNumber numberWithInteger:-1] forKey:@"insertId"];
            }
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:[[resultDic objectForKey:@"insertId"] stringValue]];
        }else if ([strAction isEqualToString:@"post"] ||[strAction isEqualToString:@"postArray"])
        {
            //update
            NSMutableDictionary *resultDic = pluginResult.message;
            NSInteger  code = [[resultDic objectForKey:@"code"] integerValue];
            if (code!=0) {
                //[resultDic setObject:[NSNumber numberWithInteger:-1] forKey:@"insertId"];
            }
            //result = new PluginResult(PluginResult.Status.OK, count);
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:resultDic];
        }else if ([strAction isEqualToString:@"delete"]||[strAction isEqualToString:@"deleteArray"])
        {
            //delete
            NSMutableDictionary *resultDic = pluginResult.message;
            NSInteger  code = [[resultDic objectForKey:@"code"] integerValue];
            if (code!=0) {
                //[resultDic setObject:[NSNumber numberWithInteger:-1] forKey:@"insertId"];
            }
            //result = new PluginResult(PluginResult.Status.OK, count);
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:resultDic];
        }else
        {
            //delete
            NSMutableDictionary *resultDic = pluginResult.message;
            //NSInteger  code = [[resultDic objectForKey:@"code"] integerValue];
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:resultDic];
        }
    }
    [self closeSE:seDbPath];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


-(void) executeSqlBatch: (CDVInvokedUrlCommand*)command
{
    NSMutableDictionary *options = [command.arguments objectAtIndex:0];
    
    NSMutableArray *results = [NSMutableArray arrayWithCapacity:0];
    NSMutableDictionary *dbargs = [options objectForKey:@"dbargs"];
    NSMutableArray *executes = [options objectForKey:@"executes"];
    CDVPluginResult* pluginResult;
    @synchronized(self) {
        for (NSMutableDictionary *dict in executes) {
            CDVPluginResult *result = [self executeSqlWithDict:dict andArgs:dbargs];
            if ([result.status intValue] == CDVCommandStatus_ERROR) {
                /* add error with result.message: */
                NSMutableDictionary *r = [NSMutableDictionary dictionaryWithCapacity:0];
                [r setObject:[dict objectForKey:@"qid"] forKey:@"qid"];
                [r setObject:@"error" forKey:@"type"];
                [r setObject:result.message forKey:@"error"];
                [r setObject:result.message forKey:@"result"];
                [results addObject: r];
            } else {
                /* add result with result.message: */
                NSMutableDictionary *r = [NSMutableDictionary dictionaryWithCapacity:0];
                [r setObject:[dict objectForKey:@"qid"] forKey:@"qid"];
                [r setObject:@"success" forKey:@"type"];
                [r setObject:result.message forKey:@"result"];
                [results addObject: r];
            }
        }
        
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:results];
    }
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

-(void) backgroundExecuteSql: (CDVInvokedUrlCommand*)command
{
    [self.commandDelegate runInBackground:^{
        [self executeSql:command];
    }];
}

-(void) executeSql: (CDVInvokedUrlCommand*)command
{
    NSMutableDictionary *options = [command.arguments objectAtIndex:0];
    NSMutableDictionary *dbargs = [options objectForKey:@"dbargs"];
    NSMutableDictionary *ex = [options objectForKey:@"ex"];
    
    CDVPluginResult* pluginResult;
    @synchronized (self) {
        pluginResult = [self executeSqlWithDict: ex andArgs: dbargs];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

//========
#pragma custom code   by xyl
-(CDVPluginResult*) executeSqlWithDictSE: (NSMutableArray*)options action:(NSString*)action
{
    // NSMutableArray *options = [command.arguments objectAtIndex:0];
    NSString *strAction = [NSString stringWithString:action];
    
    NSString *tbName = [NSString string];
    NSString *dbName = [NSString string];
    NSMutableArray *columns = [NSMutableArray array];
    NSString *where = [NSString string];
    NSMutableArray *whereArgs = [NSMutableArray array];
    NSMutableArray *querys = [NSMutableArray array];
    
    NSString *groupBy = [NSString string];
    NSString *having = [NSString string];
    NSString *orderBy = [NSString string];;
    NSString *limit = [NSString string];
    
    if ([strAction isEqualToString:@"get"]) {
        
        tbName = [options objectAtIndex:1];
        dbName = [options objectAtIndex:0];
        columns = [options objectAtIndex:2];
        where = [options objectAtIndex:3];
        querys = [options objectAtIndex:4];
        groupBy = [options objectAtIndex:5];
        having = [options objectAtIndex:6];
        orderBy = [options objectAtIndex:7];
        limit = [options objectAtIndex:8];
    }else if ([strAction isEqualToString:@"put"])
    {
        //NSLog(@"tbname %@ >>>>",[options objectAtIndex:1]);
        dbName = [options objectAtIndex:0];
        tbName = [options objectAtIndex:1];
        columns = [options objectAtIndex:2];
        querys = [options objectAtIndex:3];
    }else if ([strAction isEqualToString:@"post"])
    {
        //NSLog(@"tbname %@ >>>>",[options objectAtIndex:1]);
        dbName = [options objectAtIndex:0];
        tbName = [options objectAtIndex:1];
        columns = [options objectAtIndex:2];
        where = [options objectAtIndex:3];
        querys = [options objectAtIndex:4];
    }else if ([strAction isEqualToString:@"postArray"])
    {
        //NSLog(@"tbname %@ >>>>",[options objectAtIndex:1]);
        dbName = [options objectAtIndex:0];
        tbName = [options objectAtIndex:1];
        columns = [options objectAtIndex:2];
        where = [options objectAtIndex:3];
        querys = [options objectAtIndex:4];
    }else if ([strAction isEqualToString:@"delete"])
    {
        //NSLog(@"tbname %@ >>>>",[options objectAtIndex:1]);
        dbName = [options objectAtIndex:0];
        tbName = [options objectAtIndex:1];
        where = [options objectAtIndex:2];
        querys = [options objectAtIndex:3];
    }else if ([strAction isEqualToString:@"deleteArray"])
    {
        //NSLog(@"tbname %@ >>>>",[options objectAtIndex:1]);
        dbName = [options objectAtIndex:0];
        tbName = [options objectAtIndex:1];
        where = [options objectAtIndex:2];
        querys = [options objectAtIndex:3];
    }
    
    
    NSString *dbPath = [NSString stringWithFormat:@"%@",databasePath];
    NSString *query = [NSString stringWithFormat:@"BEGIN"];
    //NSString *sql = [NSString stringWithFormat:@"select UserId,UserName,Email from %@ where UserId='001'",tbName];
    NSString *sql = [NSString string];
    
    if ([strAction isEqualToString:@"get"]) {
        sql = [self selectSE:options];
    }else   if ([strAction isEqualToString:@"put"]) {
        sql = [self insertSE:options];
    }else   if ([strAction isEqualToString:@"post"]) {
        sql = [self updateSE:options];
    }else   if ([strAction isEqualToString:@"delete"]) {
        sql = [self deleteSE:options];
    }else   if ([strAction isEqualToString:@"postArray"]) {
        sql = [self updateSE:options];
    }else   if ([strAction isEqualToString:@"deleteArray"]) {
        sql = [self deleteSE:options];
    }
    else
    {
        return nil;
    }
    //sql = [NSString stringWithFormat:@"select UserId,UserName,Email from %@  ",tbName];
    query = sql;
    NSLog(@"sql info %@ >>>",query);
    
    if (dbPath == NULL) {
        return [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"You must specify database path"];
    }
    if (query == NULL) {
        return [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"You must specify a query to execute"];
    }
    NSValue *dbPointer = [openDBs objectForKey:dbPath];
    if (dbPointer == NULL) {
        return [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"No such database, you must open it first"];
    }
    sqlite3 *db = [dbPointer pointerValue];
    database = db;
    int n = sqlite3_open([databasePath UTF8String], &database);
    if (n!=SQLITE_OK) {
        NSLog(@"数据库打开出错...");
    }else
    {
        NSLog(@"数据库打开成功...");
    }
    const char *sql_stmt = [query UTF8String];
    NSDictionary *error = nil;
    sqlite3_stmt *statement;
    int result, i, column_type, count;
    int previousRowsAffected, nowRowsAffected, diffRowsAffected;
    long long previousInsertId, nowInsertId;
    BOOL keepGoing = YES;
    BOOL hasInsertId;
    NSMutableDictionary *resultSet = [NSMutableDictionary dictionaryWithCapacity:0];
    NSMutableArray *resultRows = [NSMutableArray arrayWithCapacity:0];
    NSMutableDictionary *entry;
    NSObject *columnValue;
    NSString *columnName;
    NSObject *insertId;
    NSObject *rowsAffected;
    
    hasInsertId = NO;
    previousRowsAffected = sqlite3_total_changes(db);
    previousInsertId = sqlite3_last_insert_rowid(db);
    
    if (sqlite3_prepare_v2(db, sql_stmt, -1, &statement, NULL) != SQLITE_OK) {
        error = [DbHelper captureSQLiteErrorFromDb:db];
        keepGoing = NO;
    } else {
        for (int b = 0; b < [querys count]; b++) {
            [self bindStatement:statement withArg:[querys objectAtIndex:b] atIndex:b+1];
        }
    }
    while (keepGoing) {
        result = sqlite3_step (statement);
        switch (result) {
            case SQLITE_ROW:
                i = 0;
                entry = [NSMutableDictionary dictionaryWithCapacity:0];
                count = sqlite3_column_count(statement);
                
                while (i < count) {
                    columnValue = nil;
                    columnName = [NSString stringWithFormat:@"%s", sqlite3_column_name(statement, i)];
                    
                    column_type = sqlite3_column_type(statement, i);
                    
                    switch (column_type) {
                        case SQLITE_INTEGER:
                            columnValue = [NSNumber numberWithDouble: sqlite3_column_double(statement, i)];
                            break;
                        case SQLITE_TEXT:
                            columnValue = [NSString stringWithUTF8String:(char *)sqlite3_column_text(statement, i)];
                            break;
                        case SQLITE_BLOB:
                            //LIBB64
                            columnValue = [DbHelper getBlobAsBase64String: sqlite3_column_blob(statement, i)
                                                               withlength: sqlite3_column_bytes(statement, i) ];
                            //LIBB64---END
                            break;
                        case SQLITE_FLOAT:
                            columnValue = [NSNumber numberWithFloat: sqlite3_column_double(statement, i)];
                            break;
                        case SQLITE_NULL:
                            columnValue = [NSNull null];
                            break;
                    }
                    
                    if (columnValue) {
                        [entry setObject:columnValue forKey:columnName];
                    }
                    i++;
                }
                [resultRows addObject:entry];
                break;
            case SQLITE_DONE:
            {
                nowRowsAffected = sqlite3_total_changes(db);
                diffRowsAffected = nowRowsAffected - previousRowsAffected;
                rowsAffected = [NSNumber numberWithInt:diffRowsAffected];
                nowInsertId = sqlite3_last_insert_rowid(db);
                if (nowRowsAffected > 0 && nowInsertId != 0) {
                    hasInsertId = YES;
                    insertId = [NSNumber numberWithLongLong:sqlite3_last_insert_rowid(db)];
                }
                keepGoing = NO;
            }
                break;
                default:
                error = [DbHelper captureSQLiteErrorFromDb:db];
                keepGoing = NO;
        }
    }
    sqlite3_finalize (statement);
    if (error) {
        return [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:error];
    }
    [resultSet setObject:resultRows forKey:@"rows"];
    [resultSet setObject:rowsAffected forKey:@"rowsAffected"];
    if (hasInsertId) {
        [resultSet setObject:insertId forKey:@"insertId"];
    }
    return [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:resultSet];
}


#pragma custom code

-(CDVPluginResult*) executeSqlWithDict: (NSMutableDictionary*)options andArgs: (NSMutableDictionary*)dbargs
{
    NSString *dbPath = [self getDBPath:[dbargs objectForKey:@"dbname"]];
    
    NSMutableArray *query_parts = [options objectForKey:@"query"];
    NSString *query = [query_parts objectAtIndex:0];
    
    if (dbPath == NULL) {
        return [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"You must specify database path"];
    }
    if (query == NULL) {
        return [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"You must specify a query to execute"];
    }
    
    NSValue *dbPointer = [openDBs objectForKey:dbPath];
    if (dbPointer == NULL) {
        return [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"No such database, you must open it first"];
    }
    sqlite3 *db = [dbPointer pointerValue];
    
    const char *sql_stmt = [query UTF8String];
    NSDictionary *error = nil;
    sqlite3_stmt *statement;
    int result, i, column_type, count;
    int previousRowsAffected, nowRowsAffected, diffRowsAffected;
    long long previousInsertId, nowInsertId;
    BOOL keepGoing = YES;
    BOOL hasInsertId;
    NSMutableDictionary *resultSet = [NSMutableDictionary dictionaryWithCapacity:0];
    NSMutableArray *resultRows = [NSMutableArray arrayWithCapacity:0];
    NSMutableDictionary *entry;
    NSObject *columnValue;
    NSString *columnName;
    NSObject *insertId;
    NSObject *rowsAffected;
    
    hasInsertId = NO;
    previousRowsAffected = sqlite3_total_changes(db);
    previousInsertId = sqlite3_last_insert_rowid(db);
    
    if (sqlite3_prepare_v2(db, sql_stmt, -1, &statement, NULL) != SQLITE_OK) {
        error = [DbHelper captureSQLiteErrorFromDb:db];
        keepGoing = NO;
    } else {
        for (int b = 1; b < query_parts.count; b++) {
            [self bindStatement:statement withArg:[query_parts objectAtIndex:b] atIndex:b];
        }
    }
    
    while (keepGoing) {
        result = sqlite3_step (statement);
        switch (result) {
                
            case SQLITE_ROW:
                i = 0;
                entry = [NSMutableDictionary dictionaryWithCapacity:0];
                count = sqlite3_column_count(statement);
                
                while (i < count) {
                    columnValue = nil;
                    columnName = [NSString stringWithFormat:@"%s", sqlite3_column_name(statement, i)];
                    
                    column_type = sqlite3_column_type(statement, i);
                    switch (column_type) {
                        case SQLITE_INTEGER:
                            columnValue = [NSNumber numberWithDouble: sqlite3_column_double(statement, i)];
                            break;
                        case SQLITE_TEXT:
                            columnValue = [NSString stringWithUTF8String:(char *)sqlite3_column_text(statement, i)];
                            break;
                        case SQLITE_BLOB:
                            //LIBB64
                            columnValue = [DbHelper getBlobAsBase64String: sqlite3_column_blob(statement, i)
                                                               withlength: sqlite3_column_bytes(statement, i) ];
                            //LIBB64---END
                            break;
                        case SQLITE_FLOAT:
                            columnValue = [NSNumber numberWithFloat: sqlite3_column_double(statement, i)];
                            break;
                        case SQLITE_NULL:
                            columnValue = [NSNull null];
                            break;
                    }
                    
                    if (columnValue) {
                        [entry setObject:columnValue forKey:columnName];
                    }
                    i++;
                }
                [resultRows addObject:entry];
                break;
                
            case SQLITE_DONE:
                nowRowsAffected = sqlite3_total_changes(db);
                diffRowsAffected = nowRowsAffected - previousRowsAffected;
                rowsAffected = [NSNumber numberWithInt:diffRowsAffected];
                nowInsertId = sqlite3_last_insert_rowid(db);
                if (nowRowsAffected > 0 && nowInsertId != 0) {
                    hasInsertId = YES;
                    insertId = [NSNumber numberWithLongLong:sqlite3_last_insert_rowid(db)];
                }
                keepGoing = NO;
                break;
                
                default:
                error = [DbHelper captureSQLiteErrorFromDb:db];
                keepGoing = NO;
        }
    }
    
    sqlite3_finalize (statement);
    
    if (error) {
        return [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:error];
    }
    
    [resultSet setObject:resultRows forKey:@"rows"];
    [resultSet setObject:rowsAffected forKey:@"rowsAffected"];
    if (hasInsertId) {
        [resultSet setObject:insertId forKey:@"insertId"];
    }
    return [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:resultSet];
}

-(void)bindStatement:(sqlite3_stmt *)statement withArg:(NSObject *)arg atIndex:(NSUInteger)argIndex
{
    if ([arg isEqual:[NSNull null]]) {
        sqlite3_bind_null(statement, argIndex);
    } else if ([arg isKindOfClass:[NSNumber class]]) {
        NSNumber *numberArg = (NSNumber *)arg;
        const char *numberType = [numberArg objCType];
        if (strcmp(numberType, @encode(int)) == 0) {
            sqlite3_bind_int(statement, argIndex, [numberArg integerValue]);
        } else if (strcmp(numberType, @encode(long long int)) == 0) {
            sqlite3_bind_int64(statement, argIndex, [numberArg longLongValue]);
        } else if (strcmp(numberType, @encode(double)) == 0) {
            sqlite3_bind_double(statement, argIndex, [numberArg doubleValue]);
        } else {
            sqlite3_bind_text(statement, argIndex, [[NSString stringWithFormat:@"%@", arg] UTF8String], -1, SQLITE_TRANSIENT);
        }
    } else { // NSString
        NSString *stringArg = (NSString *)arg;
        NSData *data = [stringArg dataUsingEncoding:NSUTF8StringEncoding];
        
        sqlite3_bind_text(statement, argIndex, data.bytes, data.length, SQLITE_TRANSIENT);
    }
}

-(void)dealloc
{
    int i;
    NSArray *keys = [openDBs allKeys];
    NSValue *pointer;
    NSString *key;
    sqlite3 *db;
    
    /* close db the user forgot */
    for (i=0; i<[keys count]; i++) {
        key = [keys objectAtIndex:i];
        pointer = [openDBs objectForKey:key];
        db = [pointer pointerValue];
        sqlite3_close (db);
    }
    
#if !__has_feature(objc_arc)
    [openDBs release];
    [appDocsPath release];
    [super dealloc];
#endif
}

+(NSDictionary *)captureSQLiteErrorFromDb:(sqlite3 *)db
{
    int code = sqlite3_errcode(db);
    int webSQLCode = [DbHelper mapSQLiteErrorCode:code];
#if INCLUDE_SQLITE_ERROR_INFO
    int extendedCode = sqlite3_extended_errcode(db);
#endif
    const char *message = sqlite3_errmsg(db);
    
    NSMutableDictionary *error = [NSMutableDictionary dictionaryWithCapacity:4];
    
    [error setObject:[NSNumber numberWithInt:webSQLCode] forKey:@"code"];
    [error setObject:[NSString stringWithUTF8String:message] forKey:@"message"];
    
#if INCLUDE_SQLITE_ERROR_INFO
    [error setObject:[NSNumber numberWithInt:code] forKey:@"sqliteCode"];
    [error setObject:[NSNumber numberWithInt:extendedCode] forKey:@"sqliteExtendedCode"];
    [error setObject:[NSString stringWithUTF8String:message] forKey:@"sqliteMessage"];
#endif
    
    return error;
}

+(int)mapSQLiteErrorCode:(int)code
{
    // map the sqlite error code to
    // the websql error code
    switch(code) {
        case SQLITE_ERROR:
            return SYNTAX_ERR;
        case SQLITE_FULL:
            return QUOTA_ERR;
        case SQLITE_CONSTRAINT:
            return CONSTRAINT_ERR;
            default:
            return UNKNOWN_ERR;
    }
}

+(id) getBlobAsBase64String:(const char*) blob_chars
                 withlength: (int) blob_length
{
    base64_encodestate b64state;
    
    base64_init_encodestate(&b64state);
    
    //2* ensures 3 bytes -> 4 Base64 characters + null for NSString init
    char* code = malloc (2*blob_length*sizeof(char));
    
    int codelength;
    int endlength;
    
    codelength = base64_encode_block(blob_chars,blob_length,code,&b64state,0);
    
    endlength = base64_encode_blockend(&code[codelength], &b64state);
    
    //Adding in a null in order to use initWithUTF8String, expecting null terminated char* string
    code[codelength+endlength] = '\0';
    
    NSString* result = [NSString stringWithUTF8String: code];
    
    free(code);
    
    return result;
}

@end
