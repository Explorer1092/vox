<!doctype html>
<html>
<head>
<#include "../../nuwa/meta.ftl" />
    <title>一起作业，一起作业网，一起作业学生</title>
<@sugar.capsule js=["jquery", "core", "jquery.flashswf"] css=["teacher.frame", "teacher.columns"] />
<@sugar.site_traffic_analyzer_begin />
</head>
<body>
<div id="movie">
    <div id="install_flash_player_box" style="margin:20px;">
        <span id="install_download_tip"
              style="font:16px/1.125 '微软雅黑', 'Microsoft YaHei', Arial, '黑体'; color:#333; background-color:#eee; display:block; text-align:center; padding:70px 0; border:2px solid #ccc;">
            您未安装Flash Player插件，请 <a href="http://down.tech.sina.com.cn/content/1149.html" target="_blank">［点击这里］</a> 下载并安装。
            <br/><br/>
            <span>
                如果已经是最新版，<a href="http://get.adobe.com/flashplayer" target="_top">请允许加载flash</a>
            </span>
        </span>
    </div>
</div>

<#include "../../flash/prepareflashloadercdntypes.ftl"/>

<script type="text/javascript">
    var ExamMain = 'OralExamPreview';
    //根据flash高度 页面高度自适应
    function updateHeight(height){
    }

    $(function(){
        //flash参数
        p = ${flashVars!'{}'};
        p.flashURL = '<@flash.plugin name="OralExamPreview"/>';
        p.domain = '${requestContext.getWebAppFullUrl('/')}';
        p.imgDomain = '<@app.link_shared href=""/>';
        p.loadQuestionUrl = '/${loadQuestionUrl!}';
        p.debug = '${(!ProductDevelopment.isProductionEnv())?string}'; //判断当前是否线上
        p.flashId = ExamMain;
        /** 加载Flash */
        $('#movie').getFlash({
            id       : ExamMain,
            width    : '100%',
            height   : '100%',
            style    : 'position:fixed',
            movie    : '<@flash.plugin name="homeworkloader"/>',
            flashvars: p
        });
    });
</script>
</body>
</html>


