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
    <script type="text/javascript">
        var pf_time_start = +new Date(); //性能统计时间起点
    </script>
<#include "../../nuwa/meta.ftl" />
    <title>一起作业，一起作业网，一起作业学生</title>
<@sugar.check_the_resources />
<@sugar.capsule js=["jquery", "core", "alert", "ebox", "template", "DD_belatedPNG", "student"] css=["plugin.alert", "new_student.base", "new_student.module", "new_student.widget"] />
<@sugar.site_traffic_analyzer_begin />
    <script type="text/javascript">
        var pf_white_screen_time_end = +new Date(); //白屏时间结束
    </script>
</head>
<body class="${clazzName!''}" style="background-color: #a9dcf6">
<@sugar.capsule js=["ko", "homework2nd","studentv3.homeworklist"]/>
<div class="t-app-homework-box">
    <div class="t-app-homework">
        <div id="homeworkBox"></div>
    </div>
</div>
<script type="text/javascript">
    function homeworkSuggest(flashFooterParams){
        //基础应用的帮助按钮
        var reqParams = {};

        //目前examId和lessonId公用一个扩展字段extStr1记录
        if('homeworkType' in flashFooterParams) reqParams['homeworkType'] = flashFooterParams['homeworkType'];
        if( 'homeworkId' in flashFooterParams) reqParams['extStr2'] = flashFooterParams['homeworkId'];
        if( 'lessonId' in flashFooterParams) reqParams['extStr1'] = flashFooterParams['lessonId'];
        if( 'practiceType' in flashFooterParams) reqParams['practiceType'] = flashFooterParams['practiceType'];
        if( 'refUrl' in flashFooterParams) reqParams['refUrl'] = flashFooterParams['refUrl'];

        var url = '/ucenter/feedback.vpage?' + $.param(reqParams);
        var html = "<iframe class='vox17zuoyeIframe' class='vox17zuoyeIframe' allow='geolocation; microphone; camera' width='600' height='430' frameborder=0 src='" + url + "'></iframe>";
        $.prompt(html, { title: "给一起作业提建议", position : { width:660 }, buttons: {} } );
        return false;
    }

    function homeworkHelpInfo(){
        $.prompt(template("t:HOMEWORK_VOICE_HELP",{}), {
            title: "作业帮助",
            focus: 1,
            position: {width:450},
            buttons: { "知道了": true }
        });
        $17.tongji("学生端-点击作业下方帮助");
    }

    $(function(){
        try{
            vox.createList({
                domain    : '${requestContext.webAppBaseUrl}/',
                imgDomain : '<@app.link_shared href='' />',
                env       : <@ftlmacro.getCurrentProductDevelopment />,
                hid       : '${homeworkId!''}',
                listUrl   : '${listUrl!}',
                dom       : document.getElementById("homeworkBox"),
                from      : $17.getQuery("from") ? $17.getQuery("from") : "studentIndex"
            });
        }catch(exception){
            $17.voxLog({
                module: 'vox_exam_create',
                op: 'examCoreJs_error',
                errMsg: exception.message,
                userAgent: (navigator && navigator.userAgent) ? navigator.userAgent : "No browser information"
            }, "student");
            $17.tongji('voxExamCreate','examCoreJs_error',exception.message);

        }
        var logFrom,from = $17.getQuery("from");
        switch (from){
            case "indexCard":
                logFrom = "首页卡片";
                break;
            case "history":
                logFrom = "作业历史";
                break;
            default:
                logFrom = "";
        }

        $17.voxLog({
            module: "m_9vFa5c0g",
            op : "homework_information_load",
            s0 : "${subject!}",
            s1 : logFrom
        }, 'student');
    });
</script>
<#if stuforbidden!false>
<script type="text/javascript">
    $.prompt("<div style='text-align: center; padding: 30px 0;'>账号异常，暂时无法使用</div>", {
        title : "账号异常",
        buttons : {'退出登录': true},
        classes : {
            close: 'w-hide'
        },
        submit: function(){
            $17.voxLog({
                module : "studentForbidden",
                op : "popup-logout"
            }, 'student');
            location.href = "/ucenter/logout.vpage";
        },
        loaded : function(){
            $17.voxLog({
                module : "studentForbidden",
                op : "popup-load"
            }, 'student');
        }
    });
</script>
</#if>
<@sugar.site_traffic_analyzer_end />
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
