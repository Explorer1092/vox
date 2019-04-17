<#import '../layout/layout.ftl' as temp>
<@temp.page>
    <@sugar.capsule js=["ko","newexam","jplayer"]/>
<div class="t-app-homework-box">
    <div class="t-app-homework">
        <div id="beginExamBox">
            
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
                userId    : ${(currentUser.id)!0},
                historyUrl: "/flash/loader/newexam/view.vpage",
                callback  : function(obj){
                    if($17.isBlank(obj) || !obj.success){
                        $("#beginExamBox").html('<div class="w-noData-block">加载学生答案错误，请与客服联系,失败信息：' + JSON.stringify(obj) + '</div>');
                    }
                }
            });
        }catch(exception){
            $17.voxLog({
                module: 'vox.exam_initEntry',
                op: 'examinationJs_error',
                errMsg: exception.message,
                userAgent: (navigator && navigator.userAgent) ? navigator.userAgent : "No browser information"
            }, "student");
            $17.tongji('voxExamInitEntry','examinationJs_error',exception.message);

            $("#beginExamBox").html('<div class="w-noData-block">加载学生答案异常，请与客服联系,异常信息：' + exception.message + '</div>');

        }
    });
</script>
</@temp.page>