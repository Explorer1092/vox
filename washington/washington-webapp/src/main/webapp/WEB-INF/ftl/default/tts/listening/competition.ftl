<!doctype html>
<html>
<head>
    <#include "../../nuwa/meta.ftl" />
    <title>一起作业，一起作业网，一起作业学生</title>
    <@sugar.capsule js=["jquery"] css=["specialskin"] />
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
        .clear{ clear: both; font: 0/0 ""; height: 1px; overflow: hidden; width: 100%;}
        .content, .content .inner{ background: #f2f2f2; width: 1000px; *zoom: 1; margin: 0 auto;}
        .content .inner .slide_box{ width: 592px; float: left;}
        .content .inner .slide_box .tip{ padding: 40px 0 0 0;}
        .content .inner .slide_box .tip p{ padding-left: 36px; color: #666; line-height: 28px;}
        .content .inner .slide_box .tip h2{ height: 35px; font-size: 22px; font-weight: normal; color: #1892e7; background: url("/public/skin/teacherv3/images/arrow.png") no-repeat 0 0; padding-left: 37px;}
        .content .inner .messages_box{ float: right; width: 337px; border-left: 30px solid #e2e2e2; padding-left: 34px; height: 720px; position: relative;}
        .content .inner .messages_box .tip{ background: url("/public/skin/teacherv3/images/tip.png") no-repeat 0 0; width: 30px; height: 35px; display: inline-block; position: absolute; top: -35px; left: -30px;}
        .content .inner .messages_box .up{ background-color: #33aafc; padding: 8px 0 0 0; margin: 5px 0 35px 0; border-bottom: 2px solid #cecece;  border-radius: 20px;}
        .content .inner .messages_box .up p{ font-size: 20px; padding: 8px 0 8px 26px; color: #fff;}
        .content .inner .messages_box .up p span{ color: #ffe400;}
        .content .inner .messages_box .up .mb{ background-color: #fff; text-align: center; border-radius: 0 0 20px 20px; padding: 14px 0; }
        .content .inner .messages_box .up .mb a{color: #369be9; text-decoration: underline; font-size: 20px;}
        .content .inner .messages_box .down{}
        .content .inner .messages_box .down h2{ font-size: 24px; height: 40px; color: #1892e7; font-weight: normal;}
        .content .inner .messages_box .down .list{ width: 337px; height: 362px; background-color: #fff; border-bottom: 1px solid #cecece; border-radius: 20px; overflow: hidden;}
        .content .inner .messages_box .down ul{ padding: 10px 20px;}
        .content .inner .messages_box .down ul li{padding: 6px 0; color: #616161;}

    </style>
</head>
<body>
<link href="http://v3.jiathis.com/code/css/jiathis_share.css" rel="stylesheet" type="text/css">
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
        <div class="slide_box">
            <div class="tip">
                <h2>大赛时间</h2>
                <p>2014年10月28日至2014年11月15日</p>
            </div>
            <div class="tip">
                <h2>活动规则</h2>
                <p>参赛即可获得30个园丁豆</p>
            </div>
            <div class="tip">
                <h2>参赛资格</h2>
                <p>所有英语老师</p>
            </div>
            <div class="tip">
                <h2>参赛方法</h2>
                <p>在线制作一份完整的听力材料，并分享至大赛</p>
            </div>
            <div class="tip">
                <h2>活动须知</h2>
                <p>
                    1 .每位老师可以上传多份听力参赛材料，但是只获得一次30园丁豆的参与奖；<br>
                    2 .自己上传的参赛作品可以在资源——听力材料中查看；<br>
                    3 .活动结束后，园丁豆将于11月20日统一发放到账户中；<br>
                    4 .本活动最终解释权归一起作业网所有。
                </p>
            </div>
        </div>
        <div class="messages_box">
            <span class="tip"></span>
            <div class="up">
                <p>已有 <span>${teacherCount!}</span> 位老师参赛</p>
                <p>制作了 <span>${paperCount!}</span> 份听力材料</p>
                <div class="mb">
                    <a href="/tts_competitionList.vpage">查看全部参赛作品 ﹥</a>
                </div>
            </div>
            <div class="down">
                <h2>▏参赛动态</h2>
                <div class="list">
                    <ul id="message_box" style=" overflow: hidden;height:354px; ">
                        <#if list?? && list?has_content>
                            <#list list as data>
                                <li><#if data.authorName?has_content>${(data.authorName)!?substring(0, 1)}</#if>老师
                                ${data.displayUserId()!''}参加了听力材料大赛</li>
                            </#list>
                        </#if>
                    </ul>
                </div>
            </div>
        </div>
        <div class="clear"></div>
    </div>
</div>
<!--end//-->
<div id="footerPablic"></div>
<script src="http://cdn.17zuoye.com/static/project/module/js/project-plug.js"></script>
<script type="text/javascript" src="http://v3.jiathis.com/code/jia.js?uid=1613716" charset="utf-8"></script>
<script type="text/javascript">
    var time = 100;
    var scrollHeight = 0;
    var divHeight = 0;
    window.ttt = 0;
    function messageList(){
        var _this = $("#message_box");
        divHeight = _this.height();
        scrollHeight = _this.get(0).scrollHeight;
        setInterval(run, time);
    }

    function run(){
        var _this = $("#message_box");
        nscrollTop =_this.get(0).scrollTop;
        if( nscrollTop + divHeight + 20 >= scrollHeight ) {
            _this.scrollTop(0);
        } else {
            _this.scrollTop( _this.scrollTop() + 1 );
        }
    }
    messageList();
</script>
</body>
</html>