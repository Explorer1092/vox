<#import "../../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="认证老师专属礼包"
pageJs=['init']
pageJsFile={"init" : "public/script/project/ctepackage"}
pageCssFile={"css" : ["public/skin/project/ctepackage/app/css/packs"]}
>

<div style="padding: 150px 0 0; font-size: 16px; text-align: center;">活动已下线</div>

<#--<div class="autTeaPacks-box">
    <div class="autTea-banner"></div>
    <div class="aut-bg">
        <div class="autTea-main">
            <div class="aut-tag">发放奖品规则</div>
            <div class="aut-content">
                <p>礼包由系统随机抽取，获奖老师均有一次领取机会，分享活动页无效</p>
                <p>领取的截止日期为2016年10月20日</p>
                <p>活动期间，抽取到的未认证老师需要完成身份认证后方可领取奖品。<a href="javascript:;" class="JS-authIn">[查看如何认证]</a></p>
                <p>【认证专属礼包】由一起作业员工提供VIP专属配送，请保持电话畅通</p>
                <p>本活动最终解释权归一起作业所有</p>
            </div>
        </div>

        <#if isBooked!false>
            <div class="aut-tag">奖品配送中</div>
            <div class="autTea-container conMar">
                <div class="aut-con">
                    <div class="aut-title">我选择的奖品</div>
                    <div class="aut-column active"><!--active&ndash;&gt;
                        <div class="image">
                            <img src="<@app.link href="public/skin/project/ctepackage/gift/${(record.giftPicUrl)!}"/>">
                            <div class="tips">${(record.giftName)!'---'}</div>
                        </div>
                    </div>
                </div>
                <div class="aut-con ${((record.status == "ING")!false)?string("conYel", "")}">
                    <div class="aut-title">正在由${(record.agentName)!'---'}老师配送</div>
                    <div class="aut-column columnBg">
                        <div class="imageHead">
                            <img src="${(record.agentAvatar)!}">
                        </div>
                        <div class="tips">
                            <p>${(record.agentName)!'---'}  ${(record.agentMobile)!'---'}</p>
                            <p>请静待一起作业老师为您送货到学校</p>
                        </div>
                    </div>
                </div>
                <#if (record.status == "END")!false>
                    <div class="aut-con aut-con-end">
                        <div class="aut-title">完成配送</div>
                    </div>
                <#else>
                    <div class="aut-title titleGray">完成配送</div>
                </#if>
            </div>
        <#else>
            <div class="aut-tag">挑选你喜欢的奖品吧！</div>
            <div class="autTea-side ">方便收货时间：
                <input type="text" class="txt JS-datetimeInput" placeholder="例如：10月10日上午-10月31日下午"/>
                <span style="display: block; padding: 5px 0 0; "><i style="visibility: hidden;">-------------</i>（送货时间：10月10日-31日）</span>
            </div>
            <div class="autTea-container">
                <ul class="aut-list">
                    <#list giftList as gift>
                        <li class="JS-selectGift" data-gift_id="${(gift.giftId)!}">
                            <div class="aut-column"><!--active&ndash;&gt;
                                <div class="image"><img src="<@app.link href="public/skin/project/ctepackage/gift/${(gift.giftPicUrl)!}"/>"></div>
                                <div class="tips">${(gift.giftName)!'---'}</div>
                            </div>
                        </li>
                    </#list>
                </ul>
            </div>

            <div class="autTea-btn">
                <a href="javascript:void(0);" class="sure-btn JS-submitGift">确认领取</a>
            </div>
        </#if>
    </div>
</div>
<script type="text/javascript">
    /*去掉微信分享功能*/
    function onBridgeReady(){
        WeixinJSBridge.call('hideOptionMenu');
    }

    if (typeof WeixinJSBridge == "undefined"){
        if( document.addEventListener ){
            document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false);
        }else if (document.attachEvent){
            document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
            document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
        }
    }else{
        onBridgeReady();
    }
</script>-->
</@layout.page>