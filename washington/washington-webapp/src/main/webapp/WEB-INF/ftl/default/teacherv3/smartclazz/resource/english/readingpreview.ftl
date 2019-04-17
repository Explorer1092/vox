<!DOCTYPE html>
<html>
<head>
    <title>单元测试，师生家长互动作业平台 - 一起作业 www.17zuoye.com</title>
<#include "../../../../nuwa/meta.ftl" />
    <style>
        html {
            height: 100%;
        }
        body {
            padding: 0;
            margin: 0;
            overflow: hidden;
            height:100%
        }
    </style>
<@sugar.capsule js=["jquery", "core", "jquery.flashswf"] css=["teacher.frame", "teacher.columns"] />
</head>
<body>
<div id="showViewContent" style="text-align: center">
    <div id="install_flash_player_box" style="margin:20px; display: none;">
        <div id="install_download_tip" style="font:16px/1.125 '微软雅黑', 'Microsoft YaHei', Arial, '黑体'; color:#333; background-color:#eee; display:block; text-align:center; padding:70px 0; border:2px solid #ccc;">
            <a href="<@app.client_setup_url />" target="_blank">您的系统组件需要升级。请点这里<span style="color:red;">下载</span>并<span style="color:red;">运行</span> “一起作业安装程序”。</a>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function(){
        if(screen){
            window.moveTo(0, 0);
            window.resizeTo(screen.availWidth, screen.availHeight);
        }

        var gameDataURL = "/appdata/flash/Reading/obtain-ENGLISH-${readingId!}.vpage";
        var baseUrl = "${requestContext.webAppBaseUrl}";
        var imgDomain = "<@app.link_shared href='' />";
        var ttsUrl = "${tts_url!}";
        var readingFlashUrl = "${readingFlashUrl!}";

        $("#showViewContent").getFlash({
            width    : $(document).width(),
            height   : $(document).height(),
            movie    : readingFlashUrl,
            flashvars: {
                isPreview   : 0,
                gameDataURL : baseUrl + gameDataURL,
                nextHomeWork: "nextHomeWork",
                tts_url     : ttsUrl,
                isTeacher   : 1,
                imgDomain   : imgDomain,
                domain      : baseUrl
            }
        });
    });

    function nextHomeWork(){
        var opened = window.open('about:blank','_self');
        opened.opener = null;
        opened.close();
    }
</script>
</body>
</html>