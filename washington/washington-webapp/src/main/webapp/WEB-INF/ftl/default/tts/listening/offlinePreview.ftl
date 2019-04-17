<!doctype html>
<html lang="en">
<head>
<#include "../../nuwa/meta.ftl" />
    <title>一起作业，一起作业网，一起作业学生</title>
<@sugar.capsule js=["jquery", "core", "alert", "ebox", "template", "base", "jplayer","jmp3", "jquery.flashswf"] css=["plugin.alert", "new_teacher.base", "new_teacher.widget", "new_teacher.module", "new_teacher.quiz", "new_teacher.tts"] />
<@sugar.site_traffic_analyzer_begin />
</head>
<body>
<#if paper?? && paper.contentUrls?? && paper.contentUrls?has_content >
<div class="preview-listen-pop">
    <div class="navBar-btn">
        <a id="v-down-paper" class="w-blue" target="_blank" href="javascript:void(0);" data-id="${paper.id!}">下载试卷</a>
        <a id="v-down-mp3" class="w-blue v-down-MP3" href="javascript:void(0);" data-id="${paper.id!}">下载听力MP3</a>
        <a id="v-down-qr" class="w-blue" href="javascript:void(0);" data-id="${paper.id!}">下载听力二维码</a>
    </div>
    <div class="pl-con">
        <img src="${paper.contentUrls[0]}" alt="" width="650" />
    </div>
    <div class="t-pubfooter-btn">
        <a id="v-close" href="javascript:void(0);" class="v-wind-close w-btn w-btn-green w-btn-small">关闭</a>
        <a id="v-play" href="javascript:void(0);" class="v-wind-submit w-btn w-btn-small" url="${playUrl!""}">播放</a>
        <a id="v-pause" href="javascript:void(0);" class="v-wind-submit w-btn w-btn-small">暂停</a>
        <div id="jquery_jplayer_1" class="jp-jplayer"></div>
    </div>
</div>

<#else>
<div class="text_center text_big text_gray_6" style="padding:20px;text-align: center;">没有找到可预览的内容
</div>
</#if>
</body>
</html>
<script>
    $(function () {
        $("#v-close").live("click", function(){
            $("#jquery_jplayer_1").jPlayer("destroy");
            parent.closePrompt();
        });
        $("#v-down-paper").live("click", function(){
            $17.tongji("O2O-TTS-Paper-" + $(this).data("id"), "预览-下载试卷", "${currentUser.id}");
            if(parent.showDown){
                location.href = "/tts_downloadO2OPaper.vpage?paperId=" + $(this).data("id") + "&_rand="+Math.random();
            }else{
                $17.alert("教师等级达到1级后，才可以将听力材料下载使用。<a href='${(ProductConfig.getUcenterUrl())!}/teacher/center/index.vpage#/teacher/center/mylevel.vpage' class='w-blue'>查看教师等级</a> ");
                return;
            }
        });

        $("#v-down-qr").live("click", function(){
            $17.tongji("O2O-TTS-Paper-" + $(this).data("id"), "预览-下载二维码", "${currentUser.id}");
            if(parent.showDown){
                location.href = "/tts_downloadQR.vpage?paperId=" + $(this).data("id") + "&_rand="+Math.random();
            }else{
                $17.alert("教师等级达到1级后，才可以将听力材料下载使用。<a href='${(ProductConfig.getUcenterUrl())!}/teacher/center/index.vpage#/teacher/center/mylevel.vpage' class='w-blue'>查看教师等级</a> ");
                return;
            }
        });
        $("#v-down-mp3").live("click", function(){
            $17.tongji("O2O-TTS-Paper-" + $(this).data("id"), "预览-下载MP3", "${currentUser.id}");
            if (!parent.showDown){
                $17.alert("教师等级达到1级后，才可以将听力材料下载使用。<a href='${(ProductConfig.getUcenterUrl())!}/teacher/center/index.vpage#/teacher/center/mylevel.vpage' class='w-blue'>查看教师等级</a> ");
                return;
            }
            var target = $(this);
            if(parent.downloading){
                return;
            }
            downloading = true;
            target.text('生成中...');
            $.post("/tts/listening/getCompleteVoice.vpage", {
                paperId:  $(this).data('id')
            }, function (data) {
                downloading=false;
                target.text('下载听力MP3');
                if (data.success) {
                    if (data && data.value)
                        window.location=data.value+"&_rand="+Math.random();
                }
            });
        });
        var $url =  "http://" + $("#v-play").attr("url");
        $("#jquery_jplayer_1").jPlayer({
            ready: function (event) {
                $(this).jPlayer("setMedia", {
                    mp3: $url
                });
            },

            swfPath: "/public/plugin/jPlayer",
            supplied: "mp3",
        });
        $("#v-play").live('click', function () {
            $("#jquery_jplayer_1").jPlayer("play");
        });
        $("#v-pause").live('click', function() {
            $("#jquery_jplayer_1").jPlayer("pause");
        });
    });
</script>