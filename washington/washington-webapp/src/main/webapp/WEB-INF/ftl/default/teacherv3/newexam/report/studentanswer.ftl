<#import "../../../tts/module.ftl" as temp />
<@temp.page level="测试报告">
    <@sugar.capsule js=["ko","newexam","jplayer"] css=["homeworkv3.homework"]/>
<div class="m-main" id="reportList">
    <div class="h-homeworkCorrect">
        <h4 class="link">
            <a href="/teacher/newexam/report/index.vpage?subject=${subject!'ENGLISH'}">我的统考</a>
            &gt;<a href="/teacher/newexam/report/detail.vpage?clazzId=${clazzId!}&newExamId=${id!}">测试详情</a>
            &gt;<span>结果预览</span>
        </h4>
        <div class="w-base">
            <div class="hc-main" id="beginExamBox">

            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        try{
            vox.exam.initEntry({
                renderType:'student_history',
                domain    : '${requestContext.webAppBaseUrl}/',
                imgDomain : '<@app.link_shared href='' />',
                env       : <@ftlmacro.getCurrentProductDevelopment />,
                dom       : "beginExamBox",
                clientType: "pc",
                clientName: "pc",
                newExamId : "${id!}",
                historyUrl: "/flash/loader/newexam/view.vpage",
                userId    : ${userId!0},
                callback  : function(obj){
                    if($17.isBlank(obj) || !obj.success){
                        $("#beginExamBox").html('<div class="w-noData-block">加载学生答案错误，请与客服联系,失败信息：' + JSON.stringify(obj) + '</div>')
                    }
                }
            });
        }catch(exception){
            $17.voxLog({
                module: 'vox.exam_initEntry',
                op: 'examinationJs_error',
                errMsg: exception.message,
                userAgent: (navigator && navigator.userAgent) ? navigator.userAgent : "No browser information"
            });
            $17.tongji('voxExamInitEntry','examinationJs_error',exception.message);
            $("#beginExamBox").html('<div class="w-noData-block">加载学生答案异常，请与客服联系,异常信息：' + exception.message + '</div>');
        }
    });
</script>
</@temp.page>