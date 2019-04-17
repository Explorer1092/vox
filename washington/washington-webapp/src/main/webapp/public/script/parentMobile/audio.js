
/**
 * @authon luwei.li
 * @description   提供给网页播放音频
 * @createDate 2015/9/25 by luwei.li
 * @updateDate 2015/10/18 by xinqiang.wang
 *             2016/01/02 by luwei.li
 */

/* global define : true, window : true, $:true, vox : true, PM : true */
"use strict";
define(["build_map_params"], function(make_function_params_to_key_value){

	var WIN = window,
		noop = $.noop,
		global_log = WIN.log || noop,
		Log = function(errMsg, method){
			global_log({
				errMsg : errMsg,
				op    : "audioError"
			}, method);
		};

	var playlist = [],
		nextPlayIndex, // 当前播放的文件的数组index + 1
		prev_url,      // 上一个播放的音频url FIXME to fix Parent App 1.3.5 for Android 重复播放问题 1.3.5 强升后，可考虑干掉这个
		currentUrl,
		stopPlayClassName = "icon-n-stop";

	//播放
	var playAudio =  function(playlistId){
		currentUrl = playlist[playlistId];
		PM.doExternal( "playAudio", currentUrl);
	};

	//停止
	var stopAudio = function(url){

		url = (url || "").trim();

		url && PM.doExternal( "stopAudio", url);

		$(".doPlayAudio").removeClass(stopPlayClassName);

	};

	// 播放下一个 当该序列播放完毕，结束播放
	var play_next = function(url){

		if(prev_url === url){
			return ;
		}
		prev_url = url;

		if (nextPlayIndex < playlist.length) {
			playAudio(nextPlayIndex++);
			return ;
		}

		stopAudio();

	};

	WIN.vox = {
		task : {
			//app 播放回调调用接口 http://wiki.17zuoye.net/pages/viewpage.action?pageId=4980841#id-%E5%AE%B6%E9%95%BF%E9%80%9A%E6%8E%A5%E5%8F%A3-JS%E4%B8%8ENative%E9%80%9A%E4%BF%A1%E6%92%AD%E6%94%BE%E9%9F%B3%E9%A2%91%E6%8E%A5%E5%8F%A3
			playAudioProgress : function(){
				var play_action_by_state = {
					ended : function(url){
						play_next(url);
					},
					error : function(url){
						play_next(url);

						Log(
							"Audio vox.task.playAudioProgress <error> method Error: the arguments : " +
								make_function_params_to_key_value(vox.task.playAudioProgress)(arguments, true),
							"error"
						);

						PM.doTrack("hwdetail", "en_playfail");
					}
				};

				vox.task.playAudioProgress = function(url, state, currentTime, duration){
					(state in play_action_by_state ? play_action_by_state[state] : noop).apply(
						null,
						arguments
					);
				};

				vox.task.playAudioProgress.apply(null, arguments);

			},
			loadAudioProgress : noop
		}
	};

	var init = function(targetDom){
		nextPlayIndex = 1;  // TODO 播放时顺序无法控制  故不能放到 stopAudio 方法里
		prev_url = null;
		currentUrl = null;
		playlist = $(targetDom).data('audio_src').split('|');

		playAudio(0);

		$(targetDom).addClass(stopPlayClassName);
	};

	$.iosOnClick(".doPlayAudio", function () {
		stopAudio(currentUrl);

		//播放
		init(this);

	});

});

