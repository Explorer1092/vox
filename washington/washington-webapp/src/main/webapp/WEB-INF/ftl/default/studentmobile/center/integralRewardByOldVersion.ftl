<#include "../../parentmobile/constants.ftl">
<#assign studentappImagesBasePath = "public/skin/mobile/pc/images/studentapp/">

<a href="javascript:;" class="btn-default btn-blue doClickOpenParentByOld" data-module="user" data-op="extra_studies">立即领取</a>

<div id="base_error_popup_alert" class="w-error-popup-white" style="position: fixed;width: 100%; top: 0; left: 0; z-index: 1000; display: none;">
    <div class="w-error-back"></div>
    <div class="w-error-inner">
        <div class="box">
            <div class="face-1">领取方式</div>
            <div class="face-text">需下载并使用家长通领取奖励<br/>若已下载，请直接登录</div>
            <div class="btn-box btn-box-sig">
                <span id="base_error_popup_alert_cancel" class="w-btn"><div class="w-btn-inner">暂不下载</div></span>
                <span id="base_error_popup_alert_submit" class="w-btn w-btn-green"><div class="w-btn-inner">立即下载</div></span>
            </div>
        </div>
    </div>
</div>
<style>
    .w-error-popup-white { position: fixed; width: 100%; height: 100%; z-index: 11; }
    .w-error-popup-white .w-error-back { width: 100%; height: 100%; background-color: rgba(0,0,0,.8); }
    .w-error-popup-white .w-error-inner { position: fixed; top: 15%; left: 50%; width: 90%; margin-left: -47%; padding: 6px; border: 6px #c60 solid; border-radius: 65px; background-color: #ffe28d; z-index: 11; }
    .w-error-popup-white .w-error-inner:after { position: absolute; bottom: -57px; left: 50%; content: ""; margin: 0 0 0 -31px; width: 63px; height: 66px; background: url(${buildStaticFilePath("", "img", studentappImagesBasePath + "w-error-popup-white-ico.png")}) no-repeat; background-size: 100% 100%; }
    .w-error-popup-white .w-error-inner .box { background-color: #fffddd; border-radius: 53px; overflow: hidden; }
    .w-error-popup-white .w-error-inner h4 { padding: 13px 0 10px; text-align: center; color: #f60; font-size: 48px; line-height: 72px; font-weight: normal; }
    .w-error-popup-white .w-error-inner .btn-box { padding: 23px 0 34px; text-align: center; overflow: hidden; }
    .w-error-popup-white .w-error-inner .btn-box-sig .w-btn { display: inline-block; margin: 0 12px; width: 40%; height: 90px; border: 6px #0097ce solid; background: url(${buildStaticFilePath("", "img", studentappImagesBasePath + "public-btn-round-white.png")}) no-repeat 18px 8px #5dcdf6; background-size: auto auto; box-shadow: 0 -6px 0 #43bcf5 inset; color: #fff; font-size: 40px; }
    .w-error-popup-white .w-error-inner .btn-box-sig .w-btn .w-btn-inner { margin: 0; padding: 0; height: 90px; background: none; }
    .w-error-popup-white .w-error-inner .btn-box-sig .w-btn-green { border-color: #189300; background-color: #73e000; box-shadow: 0 -6px 0 #54c000 inset; }
    .w-error-popup-white .w-error-inner .face-1 { padding: 80px 40px 40px; text-align: center; color: #f60; font-size: 34px; line-height: 54px; }
    .w-error-popup-white .w-error-inner .face-text { padding: 0 0 20px; text-align: center; color: #c60; font-size: 34px; line-height: 40px; }
    .w-btn { display: block; width: 100%; height: 93px; text-align: center; color: #9b6c05; font-size: 36px; line-height: 90px; cursor: pointer; border-radius: 100px; }
</style>
<script>
    $(function(){
        var $popup = $("#base_error_popup_alert");

        $(document)
            .on("click", ".doClickOpenParentByOld",function(){

                $popup.show();

                var $self = $(this),
                    trackInfo = {
                        app : "openParent",
                        module : $self.data('module') || "",
                        op : $self.data('op') || ""
                    };

                $M.appLog('reward',trackInfo);

            })
            .on("click", "#base_error_popup_alert_submit", function(){
                var UA = window.navigator.userAgent.toLowerCase(),
                    isAndroid =  /android/.test(UA),
                    href  = isAndroid ? "/parentMobile/home/dimensionCodeIndex.vpage?cid=100312" : "https://itunes.apple.com/cn/app/jia-zhang-tong-yi-qi-zuo-ye/id913817574?l=zh&ls=1&mt=8";

                location.href = href;
            })
            .on("click", "#base_error_popup_alert_cancel", function(){
                $popup.hide();
            });

    });
</script>
