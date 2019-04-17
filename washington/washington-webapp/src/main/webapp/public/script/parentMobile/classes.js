/* global define : true, $:true, PM : true */

/**
 *  @date 2016-03-03 10:25:14
 *  @auto liluwei
 *  @description 该模块主要奖励榜
 */

"use strict";

define(['ajax', 'jqPopup', 'tab'], function(promise){

	var doTrack = PM.doTrack || $.noop,
		sid = PM.sid,
		$do_tab_control = $(".do_tab_control"),
		doTabClassName = "doTab",
		base_ajax_url = "/parentMobile/ucenter/__.vpage";

	var trigger_index = +(window.tab_index || 0);


	var check_is_bind_clazz = function(){

		var $no_bind_clazz = $('#no_bind_clazz'),
			$tab_content = $('.tab_content');

		check_is_bind_clazz = function(sid, success){
			promise(base_ajax_url.replace('__', 'isBindClazz') + "?sid=" + sid)
			.done(function(res){

				if(res.success && res.isBindClazz){

					$no_bind_clazz.hide();

					success();

					return ;

				}
				$tab_content.html('');
				$no_bind_clazz.show();
				$do_tab_control.removeClass(doTabClassName);
			});
		};

		check_is_bind_clazz.apply(null, arguments);

	},
	getSignDetai = function(){
		promise('/parentMobile/parent/getParentSignInfo.vpage?sid='+sid,{},'GET')
		.done(function(res){
			if(res.success){

				var list_tpl=res.list.map(function(detail){
					return [
						'<li v-if="detail.info.length">',
						'<div class="title">'+detail.month+'月</div>',
						detail.info.map(function(_detail){
							return [
								'<div class="main">',
								'<div class="info-tit">'+_detail.name+'</div>',
								'<div class="info-tel">'+_detail.mobile+'</div>',
								'<div class="info-state">'+(_detail.is_sign ? "已" : "未")+'签到</div>',
								'</div>'
							].join('')
						}).join(''),
						'</li>'
					].join('')
				});
				$("#sign_in_detail").html(list_tpl);
			}
		});
	};


	// 家长动态榜
	$.iosOnClick(
		'.doReceiveParentReward',
		function(){
			var $self = $(this);

			promise(
				'/parentMobile/rank/receiveloginreward.vpage',
				{
					sid : sid
				},
				'POST'
			)
			.done(function(res){
				if(res.success){
					doTrack("classrank", "jzlogin_bean_success");
					$self
						.text("已领取")
						.removeClass("doReceiveParentReward").addClass("notLogged")
						.removeData("track");

					return ;
				}

				$.alert("领取失败 : " + res.info);
				doTrack("classrank", "jzlogin_bean_fail");

			});
		}
	);

	//家长动态榜 点我签到
	$.iosOnClick(
		'.doSign',
		function(){
			var $self = $(this);

			$self.removeClass('doSign');

			promise(
				'/parentMobile/parent/sign.vpage',
				{
				},
				'POST'
			)
			.done(function(res){
				if(res.success){
					$self.text('本月已签到');
					$self.addClass('notLogged');
					getSignDetai();
					return ;
				}

				$self.addClass('doSign');
				$.alert(res.info);
			})
			.fail(function(){
				$self.addClass('doSign');
			});
		}
	)
	.iosOnClick('.doSignDetail', function(){
		this.href = "/view/mobile/parent/sign_in/detail?sid=" + sid+"&app_version="+((PM.client_params&&PM.client_params.app_version)||PM.app_version||'0.0');
	});

	$.iosOnClick('#J-do-show-pop', function(){
		$("#J-get-help-box").stop().show(0,function(){
			$("#J-get-help-box-inner").stop().slideDown(100);
		});
	});

	$.iosOnClick('#J-do-hide-pop', function(){
		$("#J-get-help-box-inner").stop().slideUp(100,function(){
			$("#J-get-help-box").stop().hide();
		});
	});

	if(window.navigator.userAgent.toLowerCase().search('17parent') > -1){

		check_is_bind_clazz(sid, function(){
			$do_tab_control.addClass(doTabClassName).eq(trigger_index).click();

			//签到详情
			setTimeout(function(){
				getSignDetai();
			},500);

		});
		return ;
	}

	promise(
		base_ajax_url.replace('__', 'getKids')
	)
	.done(function(res){
		var selectClassName = "select";

		$('<ul>', {
			// 预防iosClick
			html : (res.users || []).reduce(function(prev, current){
				// jscs:disable maximumLineLength
				return prev += '<li data-sid="'+ current.student_id +'" class="'+ (current.student_id === PM.sid ? "select" : "") +'">' +
									'<img src="'+ (current.img_url || PM.default_user_image) +'">' +
									'<i class="icon"></i><p class="name textOverflow">'+ current.real_name +'</p>' +
								'</li>';
				// jscs:enable maximumLineLength
			}, '')
		})
		.on('click', 'li', function(){
			sid = $(this).closest('ul')
				.find('.' + selectClassName).removeClass(selectClassName)
				.end()
				.end()
				.addClass(selectClassName)
				.data('sid');

			check_is_bind_clazz(sid, function(){
				$do_tab_control.each(function(index, tab_control){
					$(tab_control).addClass(doTabClassName).data(
						"tab_ajax_url",
						$(tab_control).data("tab_ajax_url").replace(/sid=(\d*)/, "sid=" + sid)
					);
				})
				.eq(trigger_index).click();
			});

		})
		.appendTo(

			$('<div class="mc-childList">')
			.insertBefore('.doTabBlock')
		)
		.find('li').eq(0).click();

		$(".parentApp-topBar").remove();

	});
});
