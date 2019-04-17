<!doctype html>
<!--
<html>
<head>
<script type="text/javascript" src="/main.js">
</script>
<style>
</style>
</head>
<body>
</body>
</html>
-->
<html>
<head>
    <#include "../../nuwa/meta.ftl" />
    <title>一起作业，一起作业网，一起作业学生</title>
    <@app.css href="public/skin/project/inviteparent/skin.css" />
    <style>
        html, body{ margin: 0; padding: 0;}
        .explain_main_box .con_box .tip_box span{ width: auto;}
    </style>
    <@sugar.site_traffic_analyzer_begin />
</head>
<body>
<!--//start-->

<div class="explain_main_box" style="width: 320px; margin: 0 auto">
    <div class="con_box" style="padding: 0; ">
        <p class="cb_title" style="padding: 0 20px;background-color: #189cfb; color: #fff; line-height: 45px; margin-bottom: 15px;">请各家长绑定微信：一起作业家长通</p>
        <div class="tip_box" style="padding: 0 0 10px; margin: 0 0 0 10px;">
                 <span style="margin-bottom: 13px;">
                    <i class="number">1</i>打开微信扫描二维码
                </span>
                <span style="margin-bottom: 13px;">
                    <i class="number">2</i>点击一起作业家长通【最新作业】
                </span>
                <span>
                    <i class="number">3</i>根据提示输入孩子账号和密码
                </span>
        </div>
        <div class="cb_con">
            <div class="slide" style="width: auto; float: none; border: none;">
                <i class="weixin">
                    <img src="<@app.link href="/public/skin/project/inviteparent/weixin-xxt.jpg"/>" width="200"/>
                </i>
                <p>微信扫码，关注：一起作业家长通</p>
            </div>
            <div class="list" style="margin: 10px 0 0 10%; padding: 0;">
                <p class="cb_title" style="padding: 0 0 10px 40px; height: auto;">绑定微信好处：</p>
                <ul>
                    <li>
                        <i class="number">1</i>接收老师通知
                    </li>
                    <li>
                        <i class="number">2</i>第一时间了解老师布置了作业
                    </li>
                    <li>
                        <i class="number">3</i>查看孩子作业成绩及对错
                    </li>
                    <li>
                        <i class="number">4</i>了解孩子课堂表现
                    </li>
                </ul>
                <p class="font">如有疑问，请联系客服 <@ftlmacro.hotline/></p>
            </div>
        </div>
    </div>
</div>
<@sugar.site_traffic_analyzer_end />
<!--end//-->
</body>
</html>
<!--
<html>
<head>
<script type="text/javascript" src="/main.js">
</script>
<style>
</style>
</head>
<body>
</body>
</html>

