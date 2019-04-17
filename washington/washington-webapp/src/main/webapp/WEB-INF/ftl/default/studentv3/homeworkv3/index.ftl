<#import '../layout/layout.ftl' as temp>
<@temp.page>
<@sugar.capsule js=["ko","jplayer", "homework2nd","studentv3.homeworklist"]/>
<#--<#if subject?has_content>
    <#import "./gamespread.ftl" as gamespreadPage/>
    <@gamespreadPage.gameSpread type=subject!'noNull'/>
</#if>-->

<div class="t-app-homework-box">
    <div class="t-app-homework">
        <div id="homeworkBox"></div>
    </div>
</div>
<script id="homeworkListError" type="text/html">
    <div style="padding: 100px 0; text-align: center; font-size: 16px;">数据没有加载出来!<a href="javascript:void (0);" style="color: #189cfb;" onclick="window.location.reload();">点击重试</a>或者联系客服</div>
</script>
<script type="text/html" id="t:HOMEWORK_VOICE_HELP">
    <div id="html_voice_help">
        <div style="line-height: 150%;">
            <div style="display: none ; color: #f00; padding: 0 0 15px;" id="chromeInfo">
                <img src="<@app.link href="public/skin/default/images/help/chrome_info.png"/>"/>
                您正在使用谷歌Chrome浏览器，如果页面上方出现类似这样的提示，请点击“允许”
            </div>
            <div>
                <p>1.无法录音，请扫码安装一起作业手机版。</p>
                <div style=" padding: 10px 20px 0 80px;">
                    <img src="<@app.link href="public/skin/studentv3/images/publicbanner/app-ref-102014.png"/>" />
                </div>
                <p style="padding: 20px 0;">2.没有手机，<a href="http://cdn.17zuoye.com/download/17zuoye_liebao_20150624.exe" target="_blank">请点此升级浏览器：【猎豹浏览器】</a>。</p>
            </div>
        </div>
    </div>
</script>
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
        var html = "<iframe class='vox17zuoyeIframe' class='vox17zuoyeIframe' width='600' height='430' frameborder=0 src='" + url + "'></iframe>";
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
</@temp.page>

