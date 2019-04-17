 define ['jquery'],($)->
 	###日志白名单###
 	errWhiteList= ['WeixinJSBridge is not defined']
 	###判断当前错误是否在白名单中###
 	isInWhiteList=(errMsg)->
 		result=false
 		result=true for err in errWhiteList when errMsg.indexOf(err) isnt -1
 		result
 	class Logger
 		constructor:->
 			@s0 = !!LoggerProxy && LoggerProxy.openId
 			@userId = @getCookie('uid')
 			(=>
 				window.onerror = (errMsg,file,line)=>
 					if !isInWhiteList(errMsg)
	 					userId = @getCookie('ssid')
	 					useragent = if navigator and navigator.userAgent then navigator.userAgent else "No browser information";
	 					encodeURI(useragent);
	 					url = 'http://101.251.192.236/log?_c=vox_logs:wechat_parent_js_errors_logs&_l=3&_log={"userId":"' + userId + '","errMsg":"' + errMsg + '","file":"' + file + '","line":"' + line + '","useragent":"' + useragent + '"}';
	 					$('<img />').attr('src', url).css('display', 'none').appendTo($('body'));
	 					#ga('send','event','jQueryError_parent',errMsg,useragent,userId.toString());
 					return
 				return
 			)()
 			# ga('send','event','parentWhiteScreenTime',location.href,(pf_white_screen_time_end-pf_time_start)+'ms');
 		log:(msg)->
 			def={
 				sys: 'wechat'
 				type: 'log'
 				app: 'parent'
 				code: 1
 				s0: @s0
 				userId: @userId
 			}
 			$.extend def,msg
 			# console.log def
 			@voxLogger(def)
 		voxLogger:(msg)->
 			if $.type(msg) =='string' and msg[0] == '{'
 				msg=$.parseJSON(msg)
 			(->
 				url = 'http://101.251.192.236/log?_c=vox_logs:wechat_logs&_l=info&' + $.param(msg);
 				$('<img />').attr('src', url).css('display', 'none').appendTo($('body'));
 			)()
 			return

 		voxTimerLogger:(msg)->
 			if $.type(msg) =='string' and msg[0] == '{'
 				msg=$.parseJSON(msg)
 			(->
 				url = 'http://101.251.192.236/log?_c=vox_logs:wechat_parent_logs&_l=info&' + $.param(msg);
 				$('<img />').attr('src', url).css('display', 'none').appendTo($('body'));
 			)()
 			return
 		getCookie:(name)->
 			pattern = RegExp(name + "=.[^;]*");
 			matched = document.cookie.match(pattern);
 			if matched
 				cookie = matched[0].split('=');
 				return cookie[1]
 			''
 	new Logger