<!DOCTYPE HTML>
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
    <meta charset="utf-8"/>
    <#--<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">-->
    <title>一起作业网学生注册教程</title>
    <@sugar.capsule js=["jquery", "core", "ZeroClipboard"] css=[] />
    <@sugar.site_traffic_analyzer_begin />
    <@app.css href="public/skin/project/professorstudent/css/skin.css" />
</head>
<body>

<div class="viewCourse-layerShare v-layerShare-close" style="display: none;"></div>
<div class="viewCourse-wrapper">
    <!--//start-->
    <div class="reg-program-copy">
        <div class="title">
            <h5>推荐老师：通过<span>校讯通、飞信、微信群、QQ群</span>等，通知家长帮孩子注册账号！</h5>
        </div>
        <div class="paragraph">
            <i class="par-space-arrow"></i>
            <p class="firstContent">家长好！我发现了一个教材同步、资源丰富的学习网站：一起作业网。学生可以在线学习，寓教于乐、大大提高学习兴趣。我会在网站布置辅助课堂的在线作业，请各位家长帮孩子注册、领取孩子账号，注册时输入老师号码：<span class="teacherIdBox"></span></p>
            <p>网站地址：<a href="http://www.17zuoye.com" target="_blank">http://www.17zuoye.com</a>（有手机的可下载手机端做作业）</p>
            <p>注册时输入我的号：<span class="teacherIdBox" style="color:#189cfb;"></span></p>
            <p>不会注册或需下载手机端，请点击链接查看说明：<span class="professorCopyUrl"></span></p>
            <p class="sub">（<span class="teacherSubjectBox"></span>老师：<span class="teacherNameBox"></span>）</p>
        </div>
        <div class="btn">
            <a class="copy-btn"  href="javascript:void(0);" id="clip_container1" style="position: relative; width: 150px;"><span id="clip_button1" style="display: block; line-height: 45px; width: 100%;">复制上面内容</span></a>
        </div>
    </div>
    <!--end//-->
    <div class="clearfix">
        <!--//start-->
        <div class="registration-ins-main">
            <!--step0-->
            <div class="registration-ins-title">
                <h1>1. <span>推荐使用智能手机注册：</span></h1>
            </div>
            <div class="registration-ins-module registration-ins-moduleBlue">
                <span class="moduleStep">下载手机版</span>
                <div class="moduleTitle">
                    <p>输入<span class="teacherNameBox"></span>老师的号码<span class="teacherIdBox"></span>，并加入学生的班级；</p>
                </div>
                <div class="module-content">
                    <div class="module-QR-code">
                        <img src="<@app.link href="public/skin/project/professorstudent/images/cid_146.png" />" alt="" style="width: 90%;">
                        <div class="write">手机扫描二维码下载</div>
                        <div class="btn">
                            <a href="http://wx.17zuoye.com/download/17studentapp?cid=102006" class="clickDownload" target="_blank">点击下载</a>
                        </div>
                    </div>
                    <div class="moduleDescription">
                        <p class="moduleInfo">手机学习三大优点：</p>
                        <p>1.随时随地手机做作业、做测验；</p>
                        <p>2.录音更清晰、识别更准确；</p>
                        <p>3.告别点读机，英语课文随身听。</p>
                    </div>
                </div>
            </div>
            <!--step1-->
            <div class="registration-ins-title">
                <h1>2. <span>没有智能手机请用电脑注册：（已有账号？<a href="#step4">点击查看添加新老师方法</a>）</span></h1>
            </div>
            <div class="registration-ins-module registration-ins-moduleRed">
                <span class="moduleStep">第1步</span>
                <div class="moduleTitle">
                    <p>打开17zuoye.com，点击右下角“学生注册”，在下面填写<span class="teacherNameBox"></span>老师的号码：<span class="teacherIdBox"></span>；</p>
                </div>
                <img src="<@app.link href="public/skin/project/professorstudent/images/step-01.jpg" />" alt="" style="width: 90%;">
            </div>
            <!--step2-->
            <div class="registration-ins-module registration-ins-moduleRed">
                <span class="moduleStep">第2步</span>
                <div class="moduleTitle">
                    <p>选择学生的班级，填写学生姓名、设置登录密码；</p>
                </div>
                <img src="<@app.link href="public/skin/project/professorstudent/images/step-02.jpg" />" alt="" style="width: 90%;">
            </div>
            <!--step3-->
            <div class="registration-ins-module registration-ins-moduleRed">
                <span class="moduleStep">第3步</span>
                <div class="moduleTitle">
                    <p>【注意】一定要记住账号和密码，以后要用这个账号密码登录哦！</p>
                </div>
                <img src="<@app.link href="public/skin/project/professorstudent/images/step-03.jpg" />" alt="" style="width: 90%;">
            </div>
            <!--step4-->
            <a id="step4"></a>
            <div class="registration-ins-module registration-ins-modulePink">
                <span class="moduleStep">已有账号?</span>
                <div class="moduleTitle">
                    <p>在电脑上登录17作业账号，点击老师头像右侧的“输入老师编号”绿色按钮，填写<span class="teacherNameBox"></span>老师</p>
                    <p>的号码<span class="teacherIdBox"></span>加入班级；</p>
                </div>
                <img src="<@app.link href="public/skin/project/professorstudent/images/step-04.jpg" />" alt="" style="width: 90%;">
            </div>
        </div>
        <!--end//-->
    </div>
</div>

<textarea disabled="disabled" readonly="readonly" id="copy_info_url" style="display: none;"></textarea>
<script type="text/javascript">
    $(function(){
        switch ( $17.getQuery("ref") ){
            case "pcShare": //分享出去地址访问
                $17.voxLog({
                    app : "shares",
                    module : "teacherSharesRegCourse",
                    op : "pc-share-ref"
                });

                $(".reg-program-copy").hide();
                break;
            case "card"://老师卡片访问
                $17.voxLog({
                    app : "shares",
                    module : "teacherSharesRegCourse",
                    op : "pc-card-ref"
                });
                break;
            default :
                $17.voxLog({
                    app : "shares",
                    module : "teacherSharesRegCourse",
                    op : "pc-loading"
                });
        }

        var subjectText = "";
        switch ($17.getQuery("subject")){
            case "ENGLISH" :
                subjectText = "英语";
                break;
            case "MATH" :
                subjectText = "数学";
                break;
            case "CHINESE" :
                subjectText = "语文";
                break;
        }

        if( $17.getQuery("id") != "" ){
            $(".teacherNameBox").html($17.getQuery("name"));
            $(".teacherIdBox").html($17.getQuery("id"));
            $(".teacherSubjectBox").html(subjectText);
        }

        var sharpLink = "http://"+ location.host + "/project/professorstudent/index.vpage?ref=pcShare&id="+$17.getQuery("id")+"&name="+encodeURIComponent($17.getQuery("name"))+"&subject="+$17.getQuery("subject");
        $17.getShortUrl(sharpLink, function(u){
            sharpLink = u;

            var content = "家长好！我发现了一个教材同步、资源丰富的学习网站：一起作业网。学生可以在线学习，寓教于乐、大大提高学习兴趣。我会在网站布置辅助课堂的在线作业，请各位家长帮孩子注册、领取孩子账号，注册时输入老师号码：" + $17.getQuery("id");
            if($17.getQuery("id")%10 >= 5){
                content = "重要：通知各位家长尽快帮孩子注册账号，完成班级在线作业。我将在教育部课题平台——一起作业网上布置少量配合教材的在线作业。学生可以在线学习，这个平台内容、资源丰富且完全免费，作业寓教于乐、可大大提高学习兴趣。请及时帮孩子注册，并注意控制孩子使用时长。"
            }

            $(".firstContent").html(content);
            var textAreaVal = content
                    +"网站地址：http://www.17zuoye.com（有手机的可下载手机端做作业）"
                    +"注册时输入我的号：" + $17.getQuery("id")
                    +"不会注册或需下载手机端，请点击链接查看说明：" + sharpLink
                    + "（"+subjectText+"老师：" + $17.getQuery("name") + "）";

            $(".professorCopyUrl").html("<span style='color:#189cfb;'>"+sharpLink+"</span>");
            $("#copy_info_url").val(textAreaVal);

            $17.copyToClipboard($("#copy_info_url"), $("#clip_button1"), "clip_button1", "clip_container1", function(){
                $17.voxLog({
                    app : "shares",
                    module : "teacherSharesRegCourse",
                    op : "pc-click-copyLink"
                });
            });
        });
    });
</script>
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