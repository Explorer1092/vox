<!DOCTYPE html>
<html>
<head>
    <title>预览试卷，师生家长互动作业平台 - 一起作业 www.17zuoye.com</title>
<#include "../../../nuwa/meta.ftl" />
    <style>
        body {
            padding: 0;
            margin: 0;
            overflow: hidden;
        }

        html {
            height: 100%;
        }

        body {
            height: 100%;
        }

        #FlashID {
            height: 100%;
        }
    </style>
<@sugar.capsule js=["jquery", "jquery.flashswf"] />
<@sugar.site_traffic_analyzer_begin/>
</head>
<body>
<#assign file="ExamPreviewResource"/>
<div id="movie">
    <div id="install_flash_player_box" style="margin:20px;">
            <span id="install_download_tip"
                  style="font:16px/1.125 '微软雅黑', 'Microsoft YaHei', Arial, '黑体'; color:#333; background-color:#eee; display:block; text-align:center; padding:70px 0; border:2px solid #ccc;">
                正在加载... 如果一段时间后仍无法加载完成，请联系客服，我们会全力帮您解决。<br/>
                <br/>
                客服电话：<@ftlmacro.hotline />
            </span>
    </div>
</div>
<script type="text/javascript">
    var paper_callback = function(){
        var opened = window.open('about:blank', '_self');
        opened.opener = null;
        opened.close();
    };
    //统计
    function pushTrackEvent(category, action, opt_label){
        // flash call:
        // pushTrackEvent('afentiExam','afenti_do_five_count');
        //pushTrackEvent('afentiExam','afenti_do_one_count','do');
        $17.tongji("教研员_预览试卷", "");
    }

    $(function(){
        var homeworkFlashId = 'HomeworkFlash';
        //flash参数
        p = ${flashVars!''};
        p.flashURL = '<@flash.plugin name="${file!''}"/>';
        p.domain = '${requestContext.getWebAppFullUrl('/')}';
        p.imgDomain = '<@app.link_shared href=''/>';
        p.closeCallback = 'paper_callback';
        p.bookImgUrl = '<@app.book href="${(bookImgUrl)!}"/>';
        p.debug = '${(!ProductDevelopment.isProductionEnv())?string}'; //判断当前是否线下
        p.showId = '${(!ProductDevelopment.isProductionEnv())?string}'; //是否显示题ID

        /** 加载Flash */
        $('#movie').getFlash({
            id       : homeworkFlashId,
            width    : '100%',
            height   : '100%',
            style    : 'position:fixed',
            movie    : '<@flash.plugin name="${file}"/>',
            flashvars: p
        });
    });
</script>
<@sugar.site_traffic_analyzer_end />
</body>
</html>