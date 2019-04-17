"use strict";

(function(){
	var getParamNames = function(){

		var STRIP_COMMENTS = /((\/\/.*$)|(\/\*[\s\S]*?\*\/))/mg,
			ARGUMENT_NAMES = /([^\s,]+)/g;

		getParamNames = function(func){
			var fnStr = func.toString().replace(STRIP_COMMENTS, '');
			var result = fnStr.slice(fnStr.indexOf('(')+1, fnStr.indexOf(')')).match(ARGUMENT_NAMES);
			if(result === null){
				result = [];
			}
			return result;
		};

		return getParamNames(arguments[0]);
	};


	var make_function_params_to_key_value = function(func){
		var parameter = getParamNames(func);

		return function(args, stringify){
			var param_map = {};
			parameter.forEach(function(param, index){
				param_map[param] = args[index];
			});

			return stringify === true ? JSON.stringify(param_map) : param_map;
		};
	};

	var packaged_module = function(){
		return make_function_params_to_key_value;
	};

	if (typeof define === 'function' && define.amd) {
		// AMD
		define(['jquery'], packaged_module);
	} else if (typeof exports === 'object') {
		// CMD, CommonJS之类的
		module.exports = packaged_module();
	}else{
		window.build_map_params = packaged_module();
	}

})();
