<#import '../layout/layoutblank.ftl' as temp>
<@temp.page pageName="newexamv2" clazzName=''>
    <@sugar.capsule js=["ko","newexamV2","jplayer"] css=["newexamv2.questioncss"] />
    <style type="text/css">
        .answerCard .cardTitle{
            text-overflow: ellipsis;
            white-space: nowrap;
            overflow: hidden;
        }
    </style>
    <div id="beginExamBox">
        开始测试
    </div>
<script type="text/javascript">
    var noTimeProtection = 'close';
    $(function() {
        if(!!navigator.userAgent.match(/AppleWebKit.*Mobile.*/)){
            var goHome = function(){ location.href="/student/index.vpage"; return false; };
            $17.alert("请使用电脑打开浏览器或下载一起小学学生APP完成考试哦~", goHome, goHome);
            return false;
        }

        SystemJS.import('main').then(function(){
                vox.examination.create({
                domain      : '${requestContext.webAppBaseUrl}/',
                img_domain  : '<@app.link_shared href='' />',
                env         : <@ftlmacro.getCurrentProductDevelopment />,
                user_id     : '${(currentUser.id)!0}',
                dom         : 'beginExamBox',
                renderType  : 'student_input',
                newExamId   : '${id!}',
                getExaminationDetail: '/flash/loader/newexam/index.vpage',
                submitExaminationPaper: '/exam/flash/newexam/submit.vpage'
            });
        });
    });
</script>
</@temp.page>

