/* global define : true, PM : true, $:true, location : true */
/**
 *  @date  2016-05-30 15:02:39
 *  @auto liluwei
 *  @description 该模块主要负责一键重做按钮的逻辑
 */

"use strict";

define(["ajax", "jqPopup"], function(promise){

	$(function(){

		var redoSelector = ".doRedo",
			sid = PM.sid;

		var doRedoFn = function(){

			var self = this;

			var alertTextObj = {
				1 : {
					content : "错题本已导入阿分提错题工厂，请到一起作业学生APP或电脑上重练",
					ok_text : "知道了"
				},
				0:{
					content : "错题本已导入阿分提错题工厂，开通即可在一起作业学生APP或电脑上练习",
					ok_text : "去开通",
					href : "/parentMobile/ucenter/shoppinginfo.vpage?sid=" + sid + "&productType=__productType__"
				}
			};

			var _doRedoFn = function(){
				var $self = $(this),
					isVip = +$self.data('is_vip'),
					popupInfo = isVip in alertTextObj && alertTextObj[isVip];

				if(!popupInfo){
					$.alert("未找到对应文案");
					return;
				}

				$.extend(popupInfo, {type:"alert"});
				$.popup(popupInfo).done(function(){
					var product_type = ($self.data('product_type') || '');

					//打点
					if(/math$/i.test(product_type)){  // 因为传过来的是 AfentiMath
						PM.doTrack("faultnotes","faultdetail_math_afenti_gopay");
					}else{
						PM.doTrack("faultnotes","faultdetail_en_afenti_gopay");
					}
					setTimeout(function(){
						popupInfo.href && (location.href = popupInfo.href.replace("__productType__", product_type));
					});
				});

			};

			doRedoFn = _doRedoFn;

			_doRedoFn.call(self);
		};


		$.iosOnClick(
			redoSelector,
			doRedoFn
		);

		//显示 错题重做 按钮逻辑
		var mockEventName = "checkCanShowRedoBtn";  // 因为homeworkReport 也在使用

		$(document).on(mockEventName, function(){
			var $redo = $(redoSelector);

			if( $redo.length === 0 ){
				return ;
			}

			promise(
				"/parentMobile/ucenter/isShowRedo.vpage",
				{sid : sid}
			)
			.done(function(res){
				if(!res.success){
					return $.alert(res.info);
				}

				!res.isShowRedo && $redo.closest(".doRedoParent").add($redo).remove();

			});

		})
		.trigger(mockEventName);

	});


});

