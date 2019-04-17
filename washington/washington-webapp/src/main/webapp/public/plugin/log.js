/* global define : true, location:true */

/**
 * 统一记录前端web Log日志
 * author: luwei.li
 * date: 2015/11/2
 * update : 2016-04-24
 * update : 2016-06-01 10:43:09
 */

"use strict";

(function (root, factory) {
	if (typeof define === 'function' && define.amd) {
		// AMD
		define(['jquery'], factory);
	} else if (typeof exports === 'object') {
		// CMD, CommonJS之类的
		module.exports = factory(require('jquery'));
	} else {
		// 浏览器全局变量(root 即 window)
		root.log = factory();
	}
})( window, function (){
	var WIN = window,
		extend = function(out) {
			out = out || {};

			for (var i = 1; i < arguments.length; i++) {
				if (!arguments[i]){
					continue;
				}

				for (var key in arguments[i]) {
					if (arguments[i].hasOwnProperty(key)){
						out[key] = arguments[i][key];
					}
				}
			}

			return out;
		},
		globalLog = function(){},
		do_external = function(method_name){
			var external = WIN.external || {};

			if(!(method_name in external)){
				return -1;
			}

			try {
				external[method_name].apply(external, Array.prototype.slice.call(arguments, 1));
			} catch (error) {
				globalLog("调用客户端 "+ method_name + " 方法失败  " + error, "error");
			}
		},
		get_env = function(){
			var env_reg = /\.(\w+)\.17zuoye.net/;

			get_env = function(){
				var host = location.host;

				if(host.indexOf('17zuoye.com') === -1){
					var env = host.match(env_reg);

					return env ? env[1] : host;
				}

				return "prod";
			};

			return get_env();
		},
		_console = function(){
			var console = WIN.console;

			_console = function(method, logs){
				(method in console) && Function.prototype.apply.call(console[method], console, logs);
			};

			_console.apply(null, arguments);
		},
		get_cookie = function(name){

			var pattern = RegExp(name + "=.[^;]*"),
				matched = document.cookie.match(pattern);

			return matched ? matched[0].split('=')[1] : "";
		};

	var send_img = document.createElement('img');

	send_img.id = 'doSendLog';
	//send_img.style = "display : none;";  // TODO 这种直接写的 容易出兼容问题 mobile上遇到
	send_img.style.display = "none";
	document.body.appendChild(send_img);

	var LOG_BASE_HREF = "http://log.17zuoye.cn/log",  // 对应的ip是 101.251.192.236/log
		USERAGENT = (WIN.navigator || {}).userAgent || "No browser information",
		obj2string = WIN.JSON.stringify,

		logDefaultLogInfo = {
			useragent : USERAGENT,
			uid       : get_cookie('uid')
		},
		build_json_log_info = function(logInfo){
			return extend({}, logDefaultLogInfo, logInfo);
		},

		build_log_params = function(where, json_log_info){
			// 参照见: http://wiki.17zuoye.net/pages/viewpage.action?pageId=12092226
			return {
				_c   : 'vox_logs:' + where,
				_log : obj2string(json_log_info)
			};
		},

		build_log_url = function(params){
			var url = LOG_BASE_HREF + "?";

			Object.keys(params).forEach(function(key){
				url += key + '=' + params[key] + '&';
			});

			return url.slice(0, -1);
		},
		send_log = function(src){
			if(arguments.length > 1){
				send_log(
					build_log_url(
						build_log_params.apply(null, arguments)
					)
				);
				return ;
			}
			send_img.src = src;
		};

	var log_actions = {
		error : function(where, json_log_info){
			json_log_info._l = "error";

			send_log(where, json_log_info);
		},
		point : function(where, json_log_info, app){
			if(do_external('log_b', app, obj2string(json_log_info) ) === -1){
				send_log(where, json_log_info);
			}
		},
		debug : function(role_type, debugInfo){
			do_external('showlog', debugInfo.logs[0]);
		}
	};

	/**
	* @param logInfo Object  详细配置 见logDefaultLogInfo
	* @param role_type String
	* @param method String
	*/
	var get_default_value = function(vars, name){
		return (vars || WIN[name] || "").trim();
	};

	globalLog  = function(logInfo, method, logApp, logWhere){

		// app 默认值
		var app = get_default_value(logApp, 'logApp'),
			where = get_default_value(logWhere, 'logWhere');

		method || (method = "log");

		// logs 默认类型判断 TODO 暂且保持这样, 只因为写成这样 1:console 可读性 2: 有些是函数报的错，需要记录其参数
		if(typeof logInfo === "string"){
			logInfo = {
				logs : [logInfo]
			};
		}else if(logInfo.logs === "string"){
			logInfo.logs = [logInfo];
		}

		if( where.length === 0 || app.length === 0){
			return ;
		}

		extend(
			logInfo,
			{
				app     : app,
				href    : encodeURIComponent(location.href),
				referre : encodeURIComponent(document.referrer),
				env     : get_env()
			}
		);

		var buildedLogInfo = build_json_log_info(logInfo);

		_console(method, buildedLogInfo.logs);

		(typeof log_actions[method] === 'function') && log_actions[method]( where, buildedLogInfo, app);

	};

	WIN.onerror = function(logs, file, line){
		globalLog(
			{
				logs : [logs],
				file : file,
				line : line,
				op    : "windowOnError"
			},
			'error'
		);
	};

	return globalLog;
});
