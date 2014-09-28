var root, dbHelper, DbHelper;
root = this;
DbHelper = function() {

	this.Query = function(types, success, fail) {
		cordova.exec(success, fail, "DbHelper", "query", types);
		return Cordova.exec(success, fail, "DbHelper", "query", types);
	}

	this.Put = function(params, success, fail) {
		cordova.exec(success, fail, "DbHelper", "put", [params]);
	}

	this.Get = function(params, success, fail) {
		//cordova.exec(callback, null, "DbHelper", "get", [params]);
		cordova.exec(success, fail, "DbHelper", "get", [params]);
	}

	this.Post = function(params, callback, fail) {
		cordova.exec(callback, fail, "DbHelper", "post", [params]);
	}

	this.PostArray = function(params, callback, fail) {
		cordova.exec(callback, fail, "DbHelper", "postArray", [params]);
	}

	this.Delete = function(params, callback) {
		cordova.exec(callback, null, "DbHelper", "delete", [params]);
	}

	this.DeleteArray = function(params, callback) {
		cordova.exec(callback, null, "DbHelper", "deleteArray", [params]);
	}
};

var instance = new DbHelper();

root.dbHelper = {
	put : instance.Put,
	get : instance.Get,
	post : instance.Post,
	postArray : instance.PostArray,
	deleteArray : instance.DeleteArray,
	query : instance.Query
};
