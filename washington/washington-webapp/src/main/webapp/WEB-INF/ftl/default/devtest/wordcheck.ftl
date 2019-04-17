<!doctype html>
<html>
<head>
<#include "../nuwa/meta.ftl" />
    <title>练习</title>
<@sugar.capsule js=["jquery", "jquery.flashswf"] />
</head>
<body style="background:none;padding:0;margin:0;">
<div id="movie" style="display:none;">
    <div id="install_flash_player_box" style="margin:50px auto;width:500px;">
        <a href="http://www.adobe.com/go/getflashplayer" target="_blank">
            <img src="/public/skin/common/images/plugin/install_flash_player.jpg" alt="前往Adobe官方网站下载" width="469"
                 height="172" style="border:none;"/>
        </a>

        <p style="font-size:14px;font-weight:700;text-align:center;">做作业需要最新版本的Flash Player支持。您尚未安装或版本过低，建议您：<a
                href="http://www.adobe.com/go/getflashplayer" target="_blank">下载安装最新版本的Flash Player</a>,安装完成后请刷新本页面</p>
        <img width="17" height="16" src="/public/skin/common/images/plugin/loadding2.gif"
             style="position:absolute; _top:; left:; top: 175px;left: 170px;"/>
    </div>
    <div style="display:none;">
        <object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"
                codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=10,2,0" height="0"
                width="0">
            <param name="movie" value="/resources/apps/flash/1.swf">
            <param name="quality" value="high">
            <param name="wmode" value="transparent">
            <param name="allowScriptAccess" value="always">
            <embed wmode="transparent" src="/resources/apps/flash/1.swf" quality="high"
                   pluginspage="http://www.macromedia.com/go/getflashplayer" type="application/x-shockwave-flash"
                   allowscriptaccess="always" height="0" width="0">
        </object>
    </div>
</div>
<div id="movie2" style="display:none;"></div>
<script type="text/javascript">
    var LOAD_TIMES = 0;
    var jsReady = false, pronuStudyFlashID = 'TestWord', movie, p = {};
    var isReady = function(){
        return jsReady;
    };
    var homeworkloader = '/resources/apps/flash/homeworkloader.swf?1.1.7';

    $(function(){

        p = {
            "timeXiShu"     : 2000,
            "studyCompleted": "parent.refreshHomeWorkState",
            "lessonId"      : 8790002,
            "json"          : "studyType:selfstudy;userId:30012;bookId:878;unitId:879;lessonId:8790002;",
            "nextHomeWork"  : "parent.nextHomeWork",
            "userName"      : 30012,
            "flashId"       : "TestWord",
            "type"          : 33
        };
        p.flashURL = '/resources/apps/flash/TestWord.swf?1.8.4';
        p.domain = '${requestContext.getWebAppFullUrl('/')}';
        p.promptDir = '/prompts/';
        var _name = pronuStudyFlashID;

        jsReady = true;
        /** 加载Flash */
        movie = $('#movie');

        movie.getFlash({
            id       : _name,
            name     : _name,
            height   : 470,
            movie    : homeworkloader,
            flashvars: p
        });
    });
    var setCookie = function(name, value){
                var date = new Date();
                date.setTime(date.getTime() + (365 * 24 * 60 * 60 * 1000));
                $.cookie(name, value ? value : '', { path: '/', expires: date });
            },
            getCookie = function(name){
                var value = $.cookie(name);
                return value ? value : '';
            },
            deleteCookie = function(name){
                $.cookie(name, null, { path: '/' });
            };
</script>
</body>
</html>