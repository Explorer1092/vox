<!DOCTYPE html>
<html>
<head>
<#include "../../../nuwa/meta.ftl" />
    <title>一起作业，一起作业网，一起作业学生</title>
<@sugar.capsule js=["jquery", "core", "jmp3"] css=["rstaff.main"] />
    <style type="text/css">
        .mp3_bg_icon { background: url(/public/app/default/images/mp3_play_icon.jpg) no-repeat scroll center 0 transparent; cursor: pointer; }
    </style>
<@sugar.site_traffic_analyzer_begin />
</head>
<body>
<div class="header" style="width: 70%;margin: auto">
    <div class="head_inline">
        <p><a href="/rstaff/index.vpage" title="一起作业" class="logo"></a></p>
    </div>
</div>
<table class="table_vox table_vox_bordered table_vox_striped" style="width: 70%; text-align: center;margin: auto">
    <thead>
    <tr>
        <th colspan="3">作业录音</th>
    </tr>
    <tr>
        <td valign="top"><p>学生学号 </p></td>
        <td valign="top"><p>学生姓名 </p></td>
        <td valign="top"><p>学生作业录音 </p></td>
    </tr>
    </thead>
    <tbody id="test">
    <#if classResult?? && classResult?size gt 0>
        <#list classResult as t>
        <tr>
            <td>${t.userId!}</td>
            <td>${t.userName!}</td>
            <td>
                <div class="mp3 mp3_bg_icon"title="听${t.userName!}的作业录音" url="<@app.voice href='${t.voiceName!}'/>" style="width:18px;height:18px;overflow: hidden; display: inline-block; margin: 0 auto;  vertical-align: middle;" >&nbsp;&nbsp;</div>
            </td>
        </tr>
        </#list>
    <#else>
    <tr>
        <th colspan="3" class="text_big text_gray_9" style="height: 80px;">暂无数据</th>
    </tr>
    </#if>
    </tbody>
</table>

<div class="footer">
    <div class="copyright">
    ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
    </div>
</div>

<script>
    $(function(){
        /** 听录音 */
        $(".mp3").live("click",function(){
            var _this = $(this);
            if( _this.attr("isplay") == 1 ){
                return false;
            }
            var curPlay = $(".mp3[isplay='1']");
            if( curPlay.length > 0 ){
                curPlay.html("&nbsp;&nbsp;");
                curPlay.attr("isplay", 0);
            }
            _this.attr("isplay", 1);
            _this.jmp3({autoStart: 'true', file: _this.attr("url")});
        });
    });
</script>
<@sugar.site_traffic_analyzer_end />
</body>
</html>



