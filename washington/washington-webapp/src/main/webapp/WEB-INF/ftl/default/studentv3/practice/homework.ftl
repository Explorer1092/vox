<#-- 目前数学V5 应用作业 展示 -->
<#include "../layout/loading.ftl">
<div id="mathOral" style="position: relative; 4;">
    <div style="padding: 100px 0; text-align: center;"><img src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>
</div>
<div class="w-clear"></div>

<script type="text/javascript">
    var studentHomeworkBasic = null;

    //应试反馈设置默认选择值
    feedBackInner.homeworkType = "${subject!}".toLocaleLowerCase();
    feedBackInner.extStr1 = $17.getQuery("homeworkId");
    if(feedBackInner.homeworkType == "english"){
        feedBackInner.practiceName = "英语基础作业";
    }
    if(feedBackInner.homeworkType == "math"){
        feedBackInner.practiceName = "数学基础作业";
    }
    if(feedBackInner.homeworkType == "chinese"){
        feedBackInner.practiceName = "语文基础作业";
    }

    //10s info
    var _recordPracticeLoad = null;
    var _recordTime = 1;
    var _setRecordTime = setInterval(function(){
        _recordTime++;

        if(_recordTime > 9){
            clearInterval(_setRecordTime);
            if(_recordPracticeLoad == null){
                document.getElementById('mathOral').innerHTML = '<div style="padding: 100px 0; text-align: center; font-size: 16px;">如果遇到作业加载问题，建议使用猎豹浏览器重新打开网站，<a href="//cdn.17zuoye.com/download/17zuoye_liebao_20150624.exe" target="_blank" style="color: #39f;">点击下载</a></div>';

                $17.voxLog({
                    module : "examLoadLieBaoDownloadInfo",
                    op : feedBackInner.practiceName
                }, "student");
            }
        }
    }, 1000);

    $(function(){
        try{
            var p = {
                domain      : '${requestContext.webAppBaseUrl}/',
                imgDomain   : '${imgDomain!''}',
                param       : ${param!''},
                gameDataURL : "/${gameDataURL!''}",
                subject     : "${subject!}",
                completedUrl: "/${completedUrl!''}",
                env         : <@ftlmacro.getCurrentProductDevelopment />
            };

            vox.homework.UltimateMathOral.create(function (ret) {
                if (ret.success) {
                    _recordPracticeLoad = "content";
                    vox.homework.UltimateMathOral.render(document.getElementById("mathOral"), p);
                } else {
                    $('#mathOral').html(template("examErrorTem", {}));
                    $17.voxLog({
                        module: 'vox_practice_create',
                        op:'create_error'
                    }, "student");
                    $17.tongji('voxPracticeCreate','create_error',location.pathname);
                }
            }, p);
        }catch(exception){
            $17.voxLog({
                module: 'vox_practice_create',
                op: 'practiceCoreJs_error',
                errMsg: exception.message,
                userAgent: (navigator && navigator.userAgent) ? navigator.userAgent : "No browser information"
            }, "student");
            $17.tongji('voxPracticeCreate','practiceCoreJs_error',exception.message);

        }

        $(window).on('unload', function(){
        <#--保护学生视力-->
            $17.setCookieOneDay("protection", "show", 60);
        });

    <#--保护学生视力-->
        $17.setCookieOneDay("protection", "dontShow", 60);
    });
</script>
