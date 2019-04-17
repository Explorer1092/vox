<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title="${(activity.title)!''}"
pageJs=['init']
pageJsFile={"init" : "public/script/mobile/seattle/main"}
pageCssFile={"css" : ["public/skin/mobile/seattle/css/pay"]}
>

<div class="liveExperience-box">
    <div class="tma-head ">
        <div class="lex-head">当前选择的孩子：</div>
        <ul>
            <li class="active">
                <div class="tma-image">
                    <img src="${(studentInfo.img)!}" style="border-radius: 50%; overflow: hidden;" width="100%">
                    <em class="tma-mask"></em>
                </div>
                <div class="tma-name">${(studentInfo.name)!'---'}</div>
            </li>
        </ul>
    </div>
    <div class="lex-main">
        <ul class="lex-list">
            <li>
                <div class="lex-right">${(activity.productName)!}</div>
                课程明细
            </li>
        </ul>
        <div class="lex-text">
            <input type="text" id="mobile" placeholder="请输入报名手机号" maxlength="11"/>
        </div>
        <div class="lex-text">
            <a href="javascript:void(0);" class="w-orderedBtn w-btn-green w-btnPer JS-sendCode" style="float: right; width: 26%; border-radius: 8px; padding: 0.45rem;">获取验证码</a>
            <input type="text" id="code" placeholder="请输入手机短信验证码" style="width: 64%;" maxlength="6"/>
        </div>
    </div>
    <div class="footer noFix">
        <div class="inner">
            <a href="javascript:void(0);" class="w-orderedBtn w-btn-green w-btnPer JS-submit" data-type="reserve" data-id="${(activity.id)!}">提交报名</a>
        </div>
    </div>
</div>
</@layout.page>