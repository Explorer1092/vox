<!doctype html>
<html>
<head>
    <#include "../../nuwa/meta.ftl" />
    <title>一起作业，一起作业网，一起作业学生</title>
    <style type="text/css">
        body { background-color: #eff2f6; }
       .tw-box{ padding-top: 50px;}
    </style>
    <@sugar.capsule js=["jquery", "ko","jplayer", "core","homework2nd"] css=["plugin.alert", "new_student.base", "new_student.module", "new_student.widget"] />
    <@sugar.site_traffic_analyzer_begin />
</head>
<body>
    <div class="tw-box">
        <div class="tw-item first">
            <div id="examListBox">
                <div style="height: 200px; background-color: white; width: 98%;"><img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="margin-top: 25px; margin-left: 40%;" /></div>
            </div>
        </div>
    </div>
<script type="text/javascript">
    $(function(){
        var ids = [];
    <#if qids?? && qids?size gt 0>
        <#list qids as ids>
            ids.push("${ids}");
        </#list>
    </#if>
        try{

            vox.exam.create(function(data){
                if(!data.success){
                    $('#examListBox').html('<div style="padding: 50px 0; text-align: center;">数据加载失败</div>');
                    $17.voxLog({
                        module: 'vox_exam_create',
                        op:'create_error'
                    });
                }else{
                    var node = document.getElementById('examListBox');
                    if(ids.length == 0){
                        $('#examListBox').html('<div style="padding: 50px 0; text-align: center;">数据加载失败</div>');
                        return false;
                    }
                    vox.exam.render(node, 'student_input', {
                        ids: ids,
                        examResultUrl : "exam/flash/review/question.vpage",
                        imgDomain : '${imgDomain!''}',
                        domain : '${requestContext.webAppBaseUrl}/',
                        env : <@ftlmacro.getCurrentProductDevelopment />
                    });
                }
            },false,{
                imgDomain : '${imgDomain!''}',
                domain : '${requestContext.webAppBaseUrl}/',
                env : <@ftlmacro.getCurrentProductDevelopment />
            });
        }catch(exception){
            $17.voxLog({
                module: 'vox_exam_create',
                op: 'examCoreJs_error',
                errMsg: exception.message
            });
            $17.tongji('voxExamCreate','examCoreJs_error',exception.message);
        }
    });
</script>
    <@sugar.site_traffic_analyzer_end />
</body>
</html>