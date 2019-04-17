<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="${(info.name)!''}"
pageJs=['init']
pageJsFile={"init" : "public/script/mobile/mizar/micropay"}
pageCssFile={"css" : ["public/skin/mobile/seattle/css/pay"]}
>
<div class="liveExperience-box">

    <div class="lex-main">
        <ul class="lex-list">
            <li>
                <div class="lex-right">${(info.name)!''}</div>
                课程明细
            </li>
            <li>
                <div class="lex-right">微信</div>
                支付方式
            </li>

        </ul>

        <#--<#if (period.tip)!false>-->
            <div class="lex-text">
            <#--<input id="remark" type="text" placeholder="可填写备注信息">-->
                <textarea  placeholder="${(info.tip)!'备注信息最多输入不超过200字'}" id="remark" maxlength="200"></textarea>
            </div>
        <#--</#if>-->
        <div class="lex-info" style="margin-top:0.6rem;">
            共${(info.cnt)!'1'}件商品<span class="num">合计：<i>${(info.price)!''}元</i></span>
        </div>
    </div>
    <div class="footer noFix">
        <div class="inner">
            <a href="javascript:void(0);" class="w-orderedBtn w-btn-green w-btnPer JS-submit" data-type="pay">立即支付</a>
        </div>
    </div>
</div>
<script type="text/javascript">
    var wechatHostHeader = function () {
        var hostHeader = "";
        <#if ProductDevelopment.isDevEnv()>
            hostHeader = '10.200.27.37:8180';
        <#elseif ProductDevelopment.isTestEnv()>
            hostHeader = "wechat.test.17zuoye.net";
        <#elseif ProductDevelopment.isStagingEnv()>
            hostHeader = "wechat.staging.17zuoye.net";
        <#elseif ProductDevelopment.isProductionEnv()>
            hostHeader = "xue.17xueba.com/p17wechat";
        </#if>
        return hostHeader;
    };
    var weChatLinkHost = wechatHostHeader();
</script>
<script type="text/javascript" src="https://res.wx.qq.com/open/js/jweixin-1.0.0.js"></script>
</@layout.page>