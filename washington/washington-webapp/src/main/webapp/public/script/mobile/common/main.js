(function(root){

	"use strict";

	/*
	var isFunction = function(fn){
		// 只因为不用Object.prototype.toString, 是因为客户端有时候返回的函数并不是 [object Function]
		return typeof fn === "function" && fn.constructor === Function;
	};
	*/

	var noop = function(){};

	var log = root.log || noop;

	var toStringTrim = function(str){
		return String(str).trim();
	};

	var doTrack = function(moduleName, trackName, appName){
			log(
				{
					app      : appName || root.trackApp,
					"module" : moduleName,
					op       : trackName
				},
				"point"
			);
		},
		trackListener = function(event){
			var targetDom = event.target;

			if(targetDom && targetDom.classList.contains("doTrack")){
				var trackData = toStringTrim(targetDom.dataset.track || "").split("|");

				doTrack.apply(null, trackData);

			}
		};

	document.addEventListener("click", trackListener);

	var viewportWidth = +root.app.viewportWidth,
		adaptUILayout = root.adaptUILayout || noop;

	viewportWidth && adaptUILayout(viewportWidth);


	// 检查external是否已经加载了某个方法
	var checkExternalMethodIsExist = function(methodName){
		var external = "external" in  window && window.external;

		if(!external){
			return false;
		}

		var method = external[methodName];

		return (typeof method === "function") && function(){ method.apply(external, arguments);};

	};

	root.app.doExternal = (function(){

		var externalErrorLog = function(errorMsg, logOp){
			log(
				{
					errMsg: [errorMsg],
					op    : logOp
				},
				"error"
			);
		};

		return function(methodName){

			var method = checkExternalMethodIsExist(methodName),
				logOp = "external_" + methodName,
				errorMsg;

			if(!method){

				errorMsg = '未找到原生提供的 '+ methodName +' 方法';

				externalErrorLog(errorMsg, logOp);
				return false;
			}

			var methodArgs = Array.prototype.slice.call(arguments, 1),
				result = true;

			try{
				method.apply(null, methodArgs);
			}catch(err){
				result = false;

				errorMsg = '调用原生 ' + methodName+ ' 方法失败';
				externalErrorLog(errorMsg, logOp);
			}

			return result;
		};

	})();


})(this);
