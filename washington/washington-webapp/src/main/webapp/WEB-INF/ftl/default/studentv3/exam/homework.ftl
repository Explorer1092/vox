<#-- 英语 数学 应试作业 展示 -->
<#include "../layout/loading.ftl">
<div id="examBox" style="position: relative; 4;">
    <div style="padding: 100px 0; text-align: center;"><img src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>
</div>
<div class="w-clear"></div>

<script type="text/javascript">
    var studentHomeworkExam = null;

    //应试反馈设置默认选择值
    feedBackInner.homeworkType = "${subject!}".toLocaleLowerCase();
    feedBackInner.extStr1 = $17.getQuery("homeworkId");
    if(feedBackInner.homeworkType == "english"){
        feedBackInner.practiceName = "exam";
    }
    if(feedBackInner.homeworkType == "math"){
        feedBackInner.practiceName = "数学应试练习";
    }
    if(feedBackInner.homeworkType == "chinese"){
        feedBackInner.practiceName = "语文应试练习";
    }

    //10s info
    var _recordExamLoad = null;
    var _recordTime = 1;
    var _setRecordTime = setInterval(function(){
        _recordTime++;

        if(_recordTime > 9){
            clearInterval(_setRecordTime);
            if(_recordExamLoad == null){
                document.getElementById('examBox').innerHTML = '<div style="padding: 100px 0; text-align: center; font-size: 16px;">如果遇到作业加载问题，建议使用猎豹浏览器重新打开网站，<a href="//cdn.17zuoye.com/download/17zuoye_liebao_20150624.exe" target="_blank" style="color: #39f;">点击下载</a></div>';

                $17.voxLog({
                    module : "examLoadLieBaoDownloadInfo",
                    op : feedBackInner.homeworkType
                }, "student");
            }
        }
    }, 1000);

    $(function(){
        try{
            vox.exam.create(function(data){
                if(data.success){
                    var node = document.getElementById('examBox');
                    studentHomeworkExam = vox.exam.render(node, 'student_input', {
                        getQuestionsUrl : '${questionUrl!''}',
                        getCompleteUrl: '${completedUrl!''}',
                        examResultUrl : '${examResultUrl!''}',
                        subject: '${subject!''}',
                        learningType : '${learningType!''}',
                        hid:'${homeworkId!''}',
                        packageId:'${packageId!''}',
                        imgDomain : '${imgDomain!''}',
                        domain : '${requestContext.webAppBaseUrl}/',
                        env : <@ftlmacro.getCurrentProductDevelopment />,
                        showUploadPic : ${(showUploadPic?string)!'false'}
                    });
                    _recordExamLoad = "content";
                }else{
                    $('#examBox').html(template("examErrorTem", {}));
                    $17.voxLog({
                        module: 'vox_exam_create',
                        op:'create_error'
                    }, "student");
                    $17.tongji('voxExamCreate','create_error',location.pathname);
                }
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

        $(window).on('unload', function(){
            <#--保护学生视力-->
            $17.setCookieOneDay("protection", "show", 60);
        });

        <#--保护学生视力-->
        $17.setCookieOneDay("protection", "dontShow", 60);
    });
</script>
