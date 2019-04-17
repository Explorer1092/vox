<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="设为校长" pageJs="" footerIndex=4 navBar="hidden">
    <@sugar.capsule css=['school']/>
<script src="/public/rebuildRes/js/common/common.js"></script>
<div class="flow">
    <div style="font-size:.75rem;padding:.5rem">
        <span>
            说明:
            <p style="padding:.3rem 0">
               1、校长角色可查看整个学校的全学科考试分析报告,请谨慎设置
            </p>
            <p style="padding:.3rem 0">
               2、学校需为字典表学校且开通了阅卷机权限
            </p>
            <#if isSchoolMaster>
                <div class="sureBtn" style="background:#ff7d5a;width:100%;padding:.5rem 0;text-align:center;color:#fff;margin-top:1rem" data-info="false">取消校长权限</div>
            <#else>
                <div class="sureBtn" style="background:#ff7d5a;width:100%;padding:.5rem 0;text-align:center;color:#fff;margin-top:1rem" data-info="true">设为校长</div>
            </#if>
        </span>
    </div>
</div>
<script>
    var AT = agentTool();
    var teacherId = '${teacherId!0}';
    var schoolId = '${schoolId!0}';
    $(document).on('click','.sureBtn',function(){
        $.post('set_school_master_data.vpage',{teacherId:teacherId,schoolId:schoolId,isSchoolMaster:$(this).data("info")},function(res){
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
