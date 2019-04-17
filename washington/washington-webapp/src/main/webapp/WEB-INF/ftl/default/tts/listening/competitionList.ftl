<!doctype html>
<html>
<head>
    <#include "../../nuwa/meta.ftl" />
    <title>一起作业，一起作业网，一起作业学生</title>
    <@sugar.capsule js=["jquery", "core", "alert", "template"] css=["plugin.alert", "new_teacher.widget", "new_teacher.quiz", "specialskin"] />
    <@sugar.site_traffic_analyzer_begin />
    <style>
        body{ background-color: #f2f2f2; font: 16px/22px "微软雅黑", regular;}
        .head, .head .inner{ background:#1892e7 url("/public/skin/teacherv3/images/head_bg.png") no-repeat center 0; width: 100%; height: 470px;}
        .head .inner{ position: relative; width: 1000px; margin: 0 auto; overflow: hidden; *zoom: 1; }
        .head .inner .logo{ position: absolute; top: 10px; left: 75px;}
        .head .inner .logo a{ background: url("/public/skin/teacherv3/images/logo.png") no-repeat 0 0; width: 113px; height: 38px; display: inline-block;}
        .head .inner a.do_btn{ padding: 14px 90px; background-color:#ffe506; border-bottom: 8px solid #ebae00; color: #1892e7; font-size: 22px; margin: 320px 0 0 483px; display: inline-block; }
        .head .inner a.do_btn:hover{ background-color: #ffea33;}
        .head .inner .share{ font-size: 12px; color: #fff; position: absolute; top: 438px; left: 380px;}
        .content, .content .inner{ background: #f2f2f2; padding: 0 0 70px 0;}
        .inner{ width: 1000px; margin: 0 auto; overflow: hidden; *zoom: 1; }
        .content .inner p.title{ padding: 25px 0;}
        .content .inner p.title a{ color: #fff; font-size: 20px; background-color: #1892e7; padding: 10px 25px;}
        .w-table{ margin: -1px 0 20px 0; overflow: hidden; clear: both; }
        .w-table table{ border-collapse: collapse; padding: 0; background-color: #fff; width: 100%;}
        .w-table table td, .w-table table th{ padding: 10px 5px; text-align: center;}
        .w-table table thead td, .w-table table thead th{ color: #fff; text-align: center; background-color: #51b1f4; font-size: 22px;}
        .w-table table tbody td, .w-table table tbody th{ color: #383a4b; text-align: center; font-weight: normal; font-size: 14px; line-height: 150%; border: 1px solid #d0d5d9; }
        .w-table table tbody td a, .w-table table tbody th a{ color: #51b1f4; display: inline-block; margin-right: 10px; }
        .message_page_list { text-align:center; padding:15px 10px; clear:both;}
        .message_page_list a{ display: inline-block; padding: 5px 8px; margin: 0 2px; font: 12px/1.125 Arial, Helvetica, sans-serif; color: #666; border-radius: 4px;}
        .message_page_list a.this { background-color: #189cfb; color: #fff; font-weight: bold;}
        .message_page_list a.enable,.message_page_list a.disable   { background: #f5f5f5; border:1px solid #ddd;}
        .message_page_list a.enable:hover{ background-color:#f8f8f8;}
        .message_page_list a.enable:active{ background-color:#eee;}
        .message_page_list a.disable{ color: #bbb;}
        /*修改更换英语教材样式*/
        .t-teachingMaterial{
            height:390px;overflow-y:auto;
        }
        div.jqi .jqimessage{
            margin-top:20px;
        }
        .w-background-gray input{
            float:left;
        }
    </style>
</head>
<body>
<!--//start-->
<div class="head">
    <div class="inner">
        <h1 class="logo"><a href="/"></a></h1>
        <div class="share">
            <div class="jiathis jiaThisShare">
                <!-- JiaThis Button BEGIN -->
                <div class="jiathis_style">
                    <span class="jiathis_txt">分享到：</span>
                    <a class="jiathis_button_qzone"></a>
                    <a class="jiathis_button_tsina"></a>
                    <a class="jiathis_button_tqq"></a>
                    <a class="jiathis_button_kaixin001"></a>
                    <a class="jiathis_button_renren"></a>
                    <a class="jiathis_button_douban"></a>
                    <a href="http://www.jiathis.com/share?uid=1613716" class="jiathis jiathis_txt jiathis_separator jtico jtico_jiathis" target="_blank"></a>
                </div>
                <script type="text/javascript" >
                    var jiathis_config={
                        data_track_clickback:true,
                        title: "#一起作业网免费制作听力材料#",
                        summary:"@一起作业网 举办听力材料制作大赛，参赛即得30园丁豆，大家快来参赛吧。",
                        shortUrl:false,
                        hideMore:false
                    }
                </script>
                <!-- JiaThis Button END -->
            </div>
        </div>
    </div>
</div>
<div class="content">
    <div class="inner">
        <p class="title">
            <a id="textbook" href="javascript:void (0)" style="padding: 5px;font-size: 15px;">选择教材</a>
            <#if bookName??>
                <span style="margin-left:50px;font-size: 20px" id="bookName">已选教材：${bookName!}</span>
                <a href="/tts_competitionList.vpage" style="margin-left: 10px; padding: 5px;font-size: 14px;">查看全部</a>
            </#if>
            <input type="hidden" id="bookId" />
        </p>
        <div class="w-table">
            <table>
                <thead>
                <tr>
                    <td>标题</td>
                    <td>所属年级</td>
                    <td>所属教材</td>
                    <td>作者</td>
                    <td>播放时长</td>
                    <td>操作</td>
                </tr>
                </thead>
                <tbody>
                <#if list?? && list.content?has_content>
                    <#list list.content as data>
                    <tr>
                        <td>${data.title!}</td>
                        <td>${data.fetchClassLevelString()!""}</td>
                        <td>${data.bookName!""}</td>
                        <td><#if data.authorName?has_content>${(data.authorName)!?substring(0, 1)}</#if>老师</td>
                        <td>${data.durationString!}</td>
                        <td style="text-align: center;">
                            <a href="/tts_view.vpage?id=${data.id!}" target="_blank" onclick="javascript:$17.tongji('TTS_在线查看次数', '');">查看</a>
                            <#if (data.format)?? && data.format != 1>
                                <a href="/tts_download.vpage?paperId=${data.id!}" onclick="javascript:$17.tongji('TTS_下载次数', '');">下载材料</a>
                            </#if>
                            <a href="javascript:void(0);" class="v-down-MP3" data-content-id="${data.id!}">下载MP3</a>
                        </td>
                    </tr>
                    </#list>
                </#if>
                </tbody>
            </table>
        </div>
        <div class="message_page_list source_list"></div>


    </div>
</div>
<!--end//-->
<div id="footerPablic"></div>
<script src="http://cdn.17zuoye.com/static/project/module/js/project-plug.js"></script>
<script type="text/javascript" src="http://v3.jiathis.com/code/jia.js?uid=1613716" charset="utf-8"></script>
<script>
    $(function(){
        //换教材
        $("#textbook").on('click', function(){
            $.get("/tts/changebook.vpage", function(data){
                $.prompt(data, {
                    title : '选择教材',
                    position : {width: '850'},
                    buttons: {},
                    loaded : function(){
                        BookList.init({
                            index: 1
                        });
                    }
                });
            });
        });

        $('a.v-down-MP3').click(function(){
            $17.tongji('TTS_下载MP3次数', '');
            var target = $(this);
            target.text('生成中...');
            $.post("/tts/listening/getCompleteVoice.vpage", {
                paperId:  $(this).data('content-id')
            }, function (data) {
                downloading=false;
                target.text('下载MP3');
                if (data.success) {
                    if (data && data.value)
                        window.location=data.value+"&_rand="+Math.random();
                }else{
                    if (data.info.indexOf("")>=0){
                        window.location="/login.vpage";
                    }else{
                        alert(data.info);
                    }
                }
            });
        });

        $("body").on("booklist.click", function(event, bookId, bookName){
            // 重新发一个url,包括“教材”“第几页”
            location.href = "/tts_competitionList.vpage?bookId="+bookId;
        });
        function createPageList( index ){
            // 重新发一个url,包括“教材”“第几页”
            location.href = "/tts_competitionList.vpage?pageNum="+index+"&bookId="+$('#bookId').val();
        }
        $(function () {
            $("div.source_list").page({
                total: ${list.totalPages!},
                current: ${list.number!}+1,
                jumpCallBack: createPageList
            });
            $('#bookId').val(${bookId!0});
        });
    });
</script>
</body>
</html>