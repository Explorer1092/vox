<#macro page tagindex=0>
<!DOCTYPE html>
<html>
<head>
    <#include "../../nuwa/meta.ftl" />
    <title>一起作业，一起作业网，一起作业学生</title>
    <@sugar.capsule js=["jquery", "core", "alert", "template", "ZeroClipboard"] css=["plugin.alert", "rstaff.main"] />
    <style type="text/css">
        div.jqi .jqimessage{
            padding: 10px;
            line-height: 20px;
            color: #444444;
            font-size: 13px;
        }
    </style>
    <@sugar.site_traffic_analyzer_begin />
</head>
<body>
<div id="back_top_but" style="display: none;" class="backTop"><a href="javascript:void (0)"></a></div>
<div class="header">
    <div class="head_inline">
        <p><a href="/" title="一起作业" class="logo"></a></p>
        <div class="aside">
            <a style="margin: 8px 0 0;" class="btn_mark row_vox_right" href="/rstaff/index.vpage"><strong>返回首页</strong></a>
        </div>
    </div>
</div>
<div class="rstaffInvite">
    <h1 class="inviteHeader"></h1>
    <div class="inviteTab">
        <a href="index.vpage" <#if tagindex == 0 >class="active"</#if>>邀请注册</a>
        <a href="list.vpage" <#if tagindex == 1 >class="active"</#if>>邀请记录</a>
    </div>
    <#nested>
    <div class="clear"></div>
    <ul class="inviteRules">
        <li>
            <h5>邀请规则</h5>
            <p>
                1.被邀请老师注册使用并成为认证老师，即邀请成功；<br/>
                2.每成功邀请一位老师使用，你即可获得300园丁豆的课题奖励，成功邀请多位老师园丁豆累加；<br>
            </p>
        </li>
        <li>
            <h5>特别提示</h5>
            <p>
                1.课题组将对参与活动的老师资质进行严格审核以避免非真实老师扰乱课题秩序。<br/>
                2.一起作业网拥有对此次活动的最终解释。
            </p>
        </li>
    </ul>
</div>
<div class="footer spacing_vox_top">
    <div class="copyright">
    ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
    </div>
</div>
    <@sugar.site_traffic_analyzer_end />
</body>
</html>
</#macro>