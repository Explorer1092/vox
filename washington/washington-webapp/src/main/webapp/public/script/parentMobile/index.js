/* global define : true, PM : true, $:true */
/**
 *  @date 2015/9/10
 *  @auto liluwei
 *  @description 该模块主要负责家长端的 首页上的逻辑操作
 */

define(['slide', 'fullTemplate', 'versionCompare', 'sendFlower', 'jqPopup'], function(Slide, fullTemplate, versionCompare){

    'use strict';

    var WIN = window;

    //支持作业动态卡片定位至宝贝表现tab
    (function(){
        var getUrlParam=function(name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
            var r = window.location.search.substr(1).match(reg);  //匹配目标参数
            if (r != null) return unescape(r[2]); return null; //返回参数值
        }
        if(
            getUrlParam("source_type")==="homework_dynamic_new"
            ||getUrlParam("source_type")==="homework_dynamic_finish"
            ||getUrlParam("source_type")==="homework_dynamic_check"
        ){
            try{
                WIN.external.jumpMainTab(JSON.stringify({
                    "type": "baby_tab"
                }));

                WIN.external.backToHome("");
            }catch(e){

            }
        }
    })();


    $(WIN).scroll(function(){
        $(".parentApp-homeFixedBanner").css( "display", $(this).scrollTop() > 120 ? "block" : "none" );
    });

    // 暴露给客户端的获取最新作业数的方法
    WIN.updateLatestHomeWorkBarStatusByCount = function(count){

        count = +count;

        var $bar = $(".msgNew span");

        if(isNaN(count)){
            $bar.text("获取最新作业数失败");
            return ;
        }

        var statusObj = count === 0 ?
        {
            classAction : "remove",
            text : "查看作业动态"
        }:
        {
            classAction : "add",
            text : "查看" + count + "条新动态"
        };

        $bar[statusObj.classAction + "Class"]("new").text(statusObj.text);

    };

    // 首页调用客户端点击事件
    var NEED_SHARE_MARK = "17parent_share=1",
        isNotSupportJsonData = versionCompare((PM.app_version || '0') , "1.3.5") !== 1,
        externalOpenSecondWebview = function(type, isShare, openSecondWebviewUrl){
            var openSecondWebviewParam =
                isNotSupportJsonData ?
                    [
                        type,
                        openSecondWebviewUrl
                    ]
                    :
                    [
                        JSON.stringify(
                            {
                                shareType : isShare ? "SHARE_VIEW" : "NO_SHARE_VIEW",
                                shareContent : "",
                                shareUrl : "",
                                type : type,
                                url : openSecondWebviewUrl
                            }
                        )
                    ];

            PM.doExternal.apply(
                null,
                ["openSecondWebview"].concat(openSecondWebviewParam)
            );
        };

    $(document)
        .on('click', 'a', function(event){
            var $self = $(this);
            if($(this).hasClass("J-check-kids")){
                return false;
            }
            if(!PM.isWebview || $self.hasClass('do_ignore_href')){
                var operate=$(event.target).closest("a").data("operate"),
                    type=3;

                operate==="point_read"&&(type=1);
                operate==="book_listen"&&(type=2);

                try{
                    if(!operate){
                        operate="book_listen";
                    }
                    //兼容老版本,暂时策略
                    if(operate!="book_listen"&& versionCompare((PM.app_version || '0') , "1.5.5")<0){
                        //宝贝表现页_自学工具升级提示弹窗_被曝光打点
                        //1.点读机，2.随身听，3.语文朗读）
                        PM.doTrack("m_YLk8e7LP","o_c85O9DgA",type+"");
                        $.confirm("您的版本太旧了，暂不支持该功能。请升级后再使用~",function(){
                            //宝贝表现页_自学工具升级提示弹窗_去升级按钮_被点击
                            PM.doTrack("m_YLk8e7LP","o_w1F5Zzm8",type+"");
                            setTimeout(function() {
                                externalOpenSecondWebview(
                                    "",
                                    false,
                                    "http://wx.17zuoye.com/download/17parentapp?cid=100327"
                                );
                            },0);
                        },{"ok_text":"去升级"});
                        return false;
                    }
                    WIN.external.innerJump(JSON.stringify({"name":operate}));
                }catch(e){
                    //宝贝表现页_自学工具升级提示弹窗_被曝光打点
                    //1.点读机，2.随身听，3.语文朗读）
                    PM.doTrack("m_YLk8e7LP","o_c85O9DgA",type+"");
                    $.confirm("您的版本太旧了，暂不支持该功能。请升级后再使用~",function(){
                        //宝贝表现页_自学工具升级提示弹窗_去升级按钮_被点击
                        PM.doTrack("m_YLk8e7LP","o_w1F5Zzm8",type+"");
                        setTimeout(function(){
                            externalOpenSecondWebview(
                                "",
                                false,
                                "http://wx.17zuoye.com/download/17parentapp?cid=100327"
                            );
                        },0);
                    },{"ok_text":"去升级"});
                }
                return ;
            }

            event.preventDefault();

            var href = $.trim($self.attr("href") || ""),
                openSecondWebviewUrl = (href.search("javascript:") === 0) ? "" : href;

            externalOpenSecondWebview(
                $.trim( $self.data("type") || "" ),
                openSecondWebviewUrl.search(NEED_SHARE_MARK) > -1,
                openSecondWebviewUrl
            );

        });
    //添加孩子按钮点击
    $(document)
        .on('click', '.J-check-kids', function(event){
            var self=$(this),
                result=null,
                func_model_detail_type=self.data("from_type"),
                from=self.data("from"),
                open_add_child=function(loginSource) {
                    try {
                        result = JSON.parse(WIN.external.getAppIsLogin()).type;
                    } catch (e) {
                        result = null;
                    }

                    try {
                        if (result === "type_no_login") {
                            WIN.external.innerJump(JSON.stringify({
                                "name": "go_login",
                                "func_model": "baby_show",
                                "func_model_detail_type": func_model_detail_type,
                                "loginSource":loginSource
                            }));
                        } else if (result === "type_login_and_no_child") {
                            WIN.external.innerJump(JSON.stringify({
                                "name": "add_child",
                                "func_model": "baby_show",
                                "func_model_detail_type": func_model_detail_type,
                                "loginSource":2
                            }));
                        } else {
                            alert("error");
                        }
                    } catch (e) {
                        //宝贝表现页_自学工具升级提示弹窗_被曝光打点
                        //1.点读机，2.随身听，3.语文朗读）
                        var type=3;

                        type==="child_point_read"&&(type=1);
                        type==="child_book_listen"&&(type=2);
                        PM.doTrack("m_YLk8e7LP","o_c85O9DgA",type);
                        $.confirm("您的版本太旧了，暂不支持该功能。请升级后再使用~",
                            function () {
                                //宝贝表现页_自学工具升级提示弹窗_去升级按钮_被点击
                                PM.doTrack("m_YLk8e7LP","o_w1F5Zzm8",type+"");
                                setTimeout(function() {
                                    externalOpenSecondWebview(
                                        "",
                                        false,
                                        "http://wx.17zuoye.com/download/17parentapp?cid=100327"
                                    );
                                },0);
                            },
                            {"ok_text": "去升级"}
                        );
                    }
                };

                if(from==="learn-tools"){
                    $.confirm("添加孩子信息，我们会为您推荐专属的学习资源",
                        function(){
                            open_add_child(5);//宝贝表现添加孩子按钮点击：loginSource
                        },
                        {"ok_text":"添加"}
                    );
                    return ;
                }

                open_add_child(4);//宝贝表现自学工具按钮点击：loginSource
        });

    $(function(){

        // 填充模版
        var SID = PM.sid;

        var sidCondition = {
            sid : SID
        };

        var fullTemp = function(templateInfo, callback){

            fullTemplate(
                templateInfo,
                function(errors){
                    if(errors.length > 0){
                        // TODO 有错了 赶紧看啊
                    }

                    $.isFunction(callback) && callback(errors);

                }
            );
        };

        var fullIndexHomeWork = function(){
			$("#newHomeWork").length && fullTemp(
                ['/parentMobile/home/lastHomework.vpage', sidCondition, 'newHomeWork']
            );
        };

        // 暴露给客户端使用的WIN.fullIndexHomeWork
        WIN.fullIndexHomeWork = fullIndexHomeWork;

        fullIndexHomeWork();

        //header banner
		var $boxDom =  $("#headerBannerCrm");

        if($boxDom.length > 0 && WIN.YQ && $.isFunction(YQ.voxSpread)){

			YQ.voxSpread({
				boxId : $boxDom,
				keyId : 220101,
				userId : (SID ? SID : null),
				tabCss : "focusDot"
			}, function(data){
				if(data.success && data.data.length > 0){
					//外部 jQuery flexSlider 实现效果
					data.boxId.flexslider({
						animation: "slide",
						animationLoop : false,
						directionNav : false,
						touch: true //是否支持触屏滑动
					});

					$boxDom.find('a').addClass('do_not_add_client_params');
				}else{
					$("#headerDefaultBanner").show();
				}
			});
        }
    });

});

