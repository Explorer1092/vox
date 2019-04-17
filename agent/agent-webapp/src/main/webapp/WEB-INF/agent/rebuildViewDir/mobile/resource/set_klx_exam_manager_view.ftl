<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="设为考试管理员" pageJs="" footerIndex=4 navBar="hidden">
    <@sugar.capsule css=['school']/>
<script src="/public/rebuildRes/js/common/common.js"></script>
<div class="flow">
    <div style="font-size:.75rem;padding:.5rem">
        <span>
            说明:
            <p style="padding:.3rem 0">
               1、考试管理员可创建全科考试，并负责流水阅卷各学科的任务分配、异常卡处理、阅卷进度管理等工作,请谨慎设置
            </p>
            <p style="padding:.3rem 0">
               2、学校需为字典表学校且开通了阅卷机权限
            </p>
            <#if isExamManager>
                <div class="sureBtn" style="background:#ff7d5a;width:100%;padding:.5rem 0;text-align:center;color:#fff;margin-top:1rem" data-info="false">取消考试管理员权限</div>
            <#else>
                <div class="sureBtn" style="background:#ff7d5a;width:100%;padding:.5rem 0;text-align:center;color:#fff;margin-top:1rem" data-info="true">设为考试管理员</div>
            </#if>
        </span>
    </div>
</div>
<script>
    var AT = agentTool();
    var teacherId = '${teacherId!0}';
    var schoolId = '${schoolId!0}';
    $(document).on('click','.sureBtn',function(){
        $.post('set_klx_exam_manager_data.vpage',{teacherId:teacherId,schoolId:schoolId,isExamManager:$(this).data("info")},function(res){
            if(res.success){
                AT.alert('操作成功');
                setTimeout('disMissViewCallBack()',2000);
            }else{
                AT.alert(res.info);
            }
        });
    });
</script>
</@layout.page>
