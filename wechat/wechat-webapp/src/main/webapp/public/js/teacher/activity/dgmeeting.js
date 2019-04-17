/**
 * Created by free 2016/5/3
 */
define(["$17","swiper","logger","wx","swiperAni","jbox"], function ($17,swiper,logger,wx) {
    "use strict";
    var mySwiper = new Swiper('.swiper-container',{
        mode : 'vertical',
        eventTarget : 'container',
        autoResize : true,
        resizeReInit : true,
        autoplayStopOnLast : true,
        onInit: function(swiper){
            swiperAnimateCache(swiper);
            swiperAnimate(swiper);
        },
        onSlideChangeEnd: function(swiper){
            swiperAnimate(swiper);
        }
    });


    $(document).ready(function() {
        var scaleW=window.innerWidth/320;
        var scaleH=window.innerHeight/480;
        var resizeArray = document.querySelectorAll('.resize');
        for (var j=0; j<resizeArray.length; j++) {
            resizeArray[j].style.width=parseInt(resizeArray[j].style.width)*scaleW+'px';
            resizeArray[j].style.height=parseInt(resizeArray[j].style.height)*scaleH+'px';
            resizeArray[j].style.top=parseInt(resizeArray[j].style.top)*scaleH+'px';
            resizeArray[j].style.left=parseInt(resizeArray[j].style.left)*scaleW+'px';
        }

        var bgmSource = document.getElementById("bgmSource");
        bgmSource.play();

        $("#bgmSource").on("canplay",function(){
            bgmSource.play();
        });

        $("#bgmSource").on("ended",function(){
            setTimeout(function () {
                bgmSource.play();
            }, 500);
        });

        //播放bgm
        $(document).on("click","#bgmBtn",function(){
            if($(this).hasClass("pause")){
                bgmSource.play();
                $(this).removeClass("pause");
            }else{
                bgmSource.pause();
                $(this).addClass("pause");
            }
        });

        //safari不支持音频自动播放
        $(document).on("touchstart",function(){
            bgmSource.play();
        });

        //分享静态页
        $(document).on("click",".js-shareBtn",function(){
            $(".js-guideContent").show();
            logger.log({
                app: "teacher",
                module: 'dgmeetingPage',
                op: 'shareStaticPage'
            });
        });

        $(document).on("click",".js-closeGuideDiv",function(){
            $(".js-guideContent").hide();
        });

    });
    logger.log({
        app: "teacher",
        module: 'dgmeetingPage',
        op: 'loadIndex'
    });

    /**************** weChat config***********/
    wx.config({
        debug: false,
        appId: wechatConfig.appid,
        timestamp: wechatConfig.timestamp,
        nonceStr: wechatConfig.noncestr,
        signature: wechatConfig.signature,
        jsApiList: ['onMenuShareTimeline','onMenuShareAppMessage','onMenuShareQQ']
    });
    //分享记录
    wx.ready(function(){
        var desc = "我的加入、使用、收获与成长";
        var iconUrl = wechatConfig.sharePic;
        //分享到朋友圈
        wx.onMenuShareTimeline({
            title: wechatConfig.title, // 分享标题
            link: wechatConfig.shareUrl, // 分享链接
            imgUrl: iconUrl, // 分享图标
            success: function () {
                // 用户确认分享后执行的回调函数
                logger.log({
                    app: "teacher",
                    module: 'dgmeetingPage',
                    op: 'shareFriendsCircle'
                });
            },
            cancel: function () {
                // 用户取消分享后执行的回调函数
            }
        });

        //分享给朋友
        wx.onMenuShareAppMessage({
            title: wechatConfig.title, // 分享标题
            desc: desc, // 分享描述
            link: wechatConfig.shareUrl, // 分享链接
            imgUrl: iconUrl, // 分享图标
            type: '', // 分享类型,music、video或link，不填默认为link
            dataUrl: '', // 如果type是music或video，则要提供数据链接，默认为空
            success: function () {
                // 用户确认分享后执行的回调函数
                logger.log({
                    app: "teacher",
                    module: 'dgmeetingPage',
                    op: 'shareToFriends'
                });
            },
            cancel: function () {
                // 用户取消分享后执行的回调函数
            }
        });

        //分享到QQ
        wx.onMenuShareQQ({

            title: wechatConfig.title, // 分享标题
            desc: desc, // 分享描述
            link: wechatConfig.shareUrl, // 分享链接
            imgUrl: iconUrl, // 分享图标
            success: function () {
                // 用户确认分享后执行的回调函数
                logger.log({
                    app: "teacher",
                    module: 'dgmeeting',
                    op: 'shareToQQ'
                });
            },
            cancel: function () {
                // 用户取消分享后执行的回调函数
            }
        });
    });

});