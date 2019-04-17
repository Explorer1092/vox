<#import '../layout/layout.ftl' as temp>
<@temp.page>
    <@sugar.capsule js=["ko","newexam","jplayer"]/>
<div class="t-app-homework-box">
    <div class="t-app-homework">
        <div id="beginExamBox">
            开始测试
        </div>
    </div>
</div>
<script type="text/javascript">
    var noTimeProtection = 'close';
    $(function(){
        try{
            vox.exam.initEntry({
                renderType:'student_input',
                domain    : '${requestContext.webAppBaseUrl}/',
                imgDomain : '<@app.link_shared href='' />',
                env       : <@ftlmacro.getCurrentProductDevelopment />,
                dom       : "beginExamBox",
                clientType: "pc",
                clientName: "pc",
                newExamId : "${id!}",
                userId    : ${(currentUser.id)!0}
            });
        }catch(exception){
            $17.voxLog({
                module: 'vox.exam_initEntry',
                op: 'examinationJs_error',
                errMsg: exception.message,
                userAgent: (navigator && navigator.userAgent) ? navigator.userAgent : "No browser information"
            }, "student");
            $17.tongji('voxExamInitEntry','examinationJs_error',exception.message);

        }
    });
</script>
</@temp.page>

