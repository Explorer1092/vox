<!doctype html>
<html>
<head>
<#include "../nuwa/meta.ftl" />
    <title>练习</title>
<@sugar.capsule js=["jquery", "core", "alert", "jquery.flashswf"] css=["plugin.alert", "student.widget"] />
<@sugar.site_traffic_analyzer_begin />
</head>
<body style="padding:0;margin:0;background-color:white;">
<script>
    var webAppBaseUrl = '${requestContext.webAppBaseUrl}';
</script>
<#assign gameNameTempPage = gameName!''/>

<div id="movie" class="game_type_${type!'5'}">
    <div id="install_flash_player_box" style="margin:20px; display: none;">
        <span id="install_download_tip"
              style="font:16px/1.125 '微软雅黑', 'Microsoft YaHei', Arial, '黑体'; color:#333; background-color:#eee; display:block; text-align:center; padding:70px 0; border:2px solid #ccc;">
            您未安装Flash Player插件，请 <a href="http://get.adobe.com/cn/flashplayer/" target="_blank">［点击这里］</a> 下载并安装。
            <br/><br/>
            <span>
                如果已经是最新版，<a href="http://get.adobe.com/flashplayer" target="_top">请允许加载flash</a>
            </span>
        </span>
    </div>
</div>

<#if needRecord?? && needRecord>
<div style="text-align: center; font-size: 12px; color: #999; padding: 15px 0 0;">
    <span style="color: #aaa;">语音技术由驰声科技提供</span>
</div>

<div id="html_voice_help" style="display: none;">
    <div style="line-height: 150%;">
        <div style="display: none ; color: #f00; padding: 0 0 15px;" id="chromeInfo">
            <img src="<@app.link href="public/skin/default/images/help/chrome_info.png"/>"/>
            您正在使用谷歌Chrome浏览器，如果页面上方出现类似这样的提示，请点击“允许”
        </div>
        亲爱的同学，在做朗读作业的时候，请确保麦克风录音可以正常工作。<br/>
        <span class="download_client_tip">
                    如果录音遇到问题，
                    <a href="javascript:;" class="download_client_url" style="color: #f00; font-size: 16px;">
                        <b>请点此安装《一起作业电脑版》</b>
                    </a>。<br/>
                    安装之后，在电脑桌面上打开《一起作业》图标：<br/>
                    <a href="<@app.client_setup_url />">
                        <img src="<@app.link href="/public/skin/default/images/help/plug_25.png" />"
                             style=" border: none;"/>
                    </a><br/>
                </span>
        <br/>
        在朗读的时候，请在倒计时结束后再开始朗读，声音不要太小，<br/>不必故意模仿原文的腔调，用正常声音朗读即可。<br/>
        <br/>
    </div>
</div>


<script type="text/javascript">
    $(function(){
        $(".macrHelpInfo").on('click', function(){
            $.prompt($('#html_voice_help').html(), {
                title   : "朗读作业帮助",
                focus   : 1,
                position: { width: 600 },
                buttons : { "知道了": true }
            });
        });

        if($17.getQuery("__ref") == "embed"){
            if(AC_InstallActiveX && window){
                $(".download_client_url").click(function(){
                    AC_InstallActiveX();
                    return false;
                });
            }
            else{
                $(".download_client_url").attr('href', '<@app.client_setup_url />');
            }
        }else{
            if(parent.AC_InstallActiveX && window != parent){
                $(".download_client_url").click(function(){
                    parent.AC_InstallActiveX();
                    return false;
                });
            }
            else{
                $(".download_client_url").attr('href', '<@app.client_setup_url />');
            }
        }
    });
</script>
</#if>

<#include 'prepareflashloadercdntypes.ftl'>

<script type="text/javascript">
    var homeworkFlashId = 'HomeworkFlash';

    <#if needRecord?has_content>
    var needRecord = ${(needRecord?c)!};
    </#if>

    // 获取作业对象
    function getHomeworkFlashObject(){
        return document.getElementById(homeworkFlashId);
    }

    function destroyHomeworkJavascriptObject(){
        if($17.getQuery("__ref") != "embed"){
            window.top.voxVoiceEngine = null;
        }
        window.voxVoiceEngine = null;
        $.flashswf.removeFlashById(homeworkFlashId);
    }

    <#--
    FOR DEBUG ONLY!!!
    setTimeout(destroyHomeworkJavascriptObject, 2000);
    -->

    $(window).on('unload', function(){
    <#--保护学生视力-->
        $17.setCookieOneDay("protection", "show", 60);

        destroyHomeworkJavascriptObject();
    });

    //兼容遗留代码
    if(!window.AC_InstallActiveX){
        window.AC_InstallActiveX = function(){
            if(top.AC_InstallActiveX && top.AC_InstallActiveX != AC_InstallActiveX){
                top.AC_InstallActiveX();
            }
            else{
                alert('请您点击页面右上角“下载”，下载并安装最新的《一起作业电脑版》。');
            }
        }
    }

    var init = function(){
        var isVoxExternalPluginExisting = VoxExternalPluginExists();

        var screenWidth = $(window).width();
        var screenHeight = $(window).height();
        var screenRate = screenHeight/screenWidth;

        var flashHeight = Math.ceil(screenWidth * screenRate);
        //设置所有调用loader.ftl frame 宽高
        var gameSize = "${gameSize!"H"}"; //当前加载的尺寸类型
        var flashAndFrameWidth = screenWidth; //flash and frame width
        var flashAndFrameHeight = flashHeight; //flash and frame height
        var jqiFlashWidth = "flash_jqi_720"; //flash 弹出框class名
        var parentFrame = $(parent.window.document).find("iframe") || $(top.window.document).find("iframe") || $("<div/>");//查找父子iframe
        var parentGameTypeTop = $(parent.window.document).find("div.game-type-marginTop");

        parentGameTypeTop.css({ marginTop: "0" });
        if(gameSize == "A"){
            parentGameTypeTop.css({ marginTop: "64px" });
        }

        if(gameSize == "B"){
            //set flash 宽高手度
            flashAndFrameWidth = screenWidth;
            flashAndFrameHeight = flashHeight;
            //set frame 弹出框 class控制宽度
            jqiFlashWidth = "flash_jqi_920";
        }

        if(gameSize == "C"){
            //set flash 宽高手度
            flashAndFrameWidth = screenWidth;
            flashAndFrameHeight = flashHeight;
            //set frame 弹出框 class控制宽度
            jqiFlashWidth = "flash_jqi_980"
        }

        //赋值 flash 宽高手度
        parentFrame.width(flashAndFrameWidth);
        parentFrame.height(flashAndFrameHeight<#if needRecord?? && needRecord> + 58</#if>);
        //赋值 frame 弹出框 class控制宽度
        parentFrame.closest(".jqi").addClass(jqiFlashWidth);

        /**
         私有云 ChiVoxOld
         公有云 ChiVox
         云知声 Unisound
         */

        var voiceEngineType = 'Unisound';
        var userId = "${(currentUser.id)!}";
        /*var last = userId.charAt(userId.length - 1) * 1;
        var lastArray = [0]; //账号最后一位为（0） 使用云知声引擎
        var blackList = ["342584325","363213658"]; // 云知声黑名单（这些用户因为网络问题无法使用云之声，暂时放到黑名单里）
        if ($.inArray(last, lastArray) != -1 && $.inArray(userId,blackList) == -1) {
            voiceEngineType = 'Unisound';
        }*/

        <!-- (查看游戏详情)PracticeType.java -->
        var p = ${flashVars!''};
        p.domain = webAppBaseUrl + '/';
        p.gameDataURL = webAppBaseUrl + '/${gameDataURL!""}';
        p.gameExtraDataURL = webAppBaseUrl + '/${gameExtraDataURL!""}';
        p.endPath = webAppBaseUrl + '/${endPath!""}';
        p.getRankDataUrl = webAppBaseUrl + '/${getRankDataUrl!""}';
        p.sendRankDataUrl = webAppBaseUrl + '/${sendRankDataUrl!""}';
        p.netWorkUrl = webAppBaseUrl + '/${netWorkUrl!""}';
        p.imgDomain = '<@app.link_shared href='' />';
        p.promptDir = '<@app.link_shared href='${promptDir!""}'/>';
        p.userType = '${(currentUser.userType)!}'; //这里不一定有currentUser，因为可能未登录
        p.voiceEngineType = voiceEngineType;
        p.speechEnabled = true;
        p.resVersion = "1.0";
        p.noGoTest = "<#if fromModule?has_content && fromModule == "smartclazz">true<#else>false</#if>";
        p.skipAll = "<#if fromModule?has_content && fromModule == "smartclazz">true<#else>false</#if>";

        $("#movie").getFlash({
            id       : homeworkFlashId,
            width    : flashAndFrameWidth,//flash 宽度
            height   : flashAndFrameHeight, //flash 高度
            movie    : '<@flash.plugin name="homeworkloader"/>',
            scale    : 'showall',
            flashvars: p
        });

        if(isVoxExternalPluginExisting){
            $('.download_client_tip').hide();
        }

        //chrome
        if(window.navigator.userAgent.indexOf("Chrome") !== -1){
            $("#chromeInfo").show();
        }

        //如果是Linux系统Flash版本小于等于11.1提示不支持
        if(($.flashswf.version.major < 11 || $.flashswf.version.major == 11 && $.flashswf.version.minor <= 1) && window.navigator.userAgent.indexOf("Linux") !== -1){
            var tip = ($17.getOperatingSystem() == 'iOS' || $17.getOperatingSystem() == 'Android') ? '网页版暂不支持做作业，请下载“一起作业”手机客户端 <br /> <a class="btn_mark w-btn w-btn-green" style="font-size: 20px;" href="http://wx.17zuoye.com/download/17studentapp?cid=102020" target="_blank"><strong>立即下载</strong></a>' : '目前不支持此设备，请用电脑完成作业。';
            $('#movie').html("<div style='color:#333; background-color:#eee; display:block; text-align:center; padding:70px 0; border:2px solid #ccc;'>"+tip+"</div>");
            var l = { sys: 'web', type: 'notify', app: 'www', module: 'flash/loader', op: 'linuxAndLowFlashVersion', target: '${gameNameTempPage}', s0: window.navigator.userAgent, f_major: $.flashswf.version.major, f_minor: $.flashswf.version.minor };
            window.voxLogger.log(l);
        }
    };

    $(function(){
        //IE下必须延迟初始化，否则flash 14会卡死
        setTimeout(init, 500);
    });


    var setCookie = function(name, value){
        var date = new Date();
        date.setTime(date.getTime() + (365 * 24 * 60 * 60 * 1000));
        $.cookie(name, value ? value : '', { path: '/', expires: date });
    };
    var getCookie = function(name){
        var value = $.cookie(name);
        return value ? value : '';
    };
    var deleteCookie = function(name){
        $.cookie(name, null, { path: '/' });
    };

    $(function(){
    <#--保护学生视力-->
        $17.setCookieOneDay("protection", "dontShow", 60);
    });
</script>
<@sugar.site_traffic_analyzer_end />
</body>
</html>
