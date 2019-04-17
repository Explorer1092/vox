<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="设为教务老师" pageJs="" footerIndex=4 navBar="hidden">
    <@sugar.capsule css=['school']/>
<script src="/public/rebuildRes/js/common/common.js"></script>
<div class="flow">
    <div style="font-size:.75rem;padding:.5rem">
        <span>
            说明:
            <p style="padding:.3rem 0">
                1.教务老师每个学校可设置一名
            </p>
            <p style="padding:.3rem 0">
                2.教务老师账号可批量注册老师，请谨慎设置
            </p>
             <p style="padding:.3rem 0">
                3.学校需为字典表学校且开通了阅卷机权限
            </p>
            <#if !isAffair>
                <div class="sureBtn" style="background:#ff7d5a;width:100%;padding:.5rem 0;text-align:center;color:#fff;margin-top:1rem">设为教务老师</div>
            </#if>
        </span>
    </div>
</div>
<div id="changeSignDialog" class="closeParent" style="display: none;">
    <div class="clazz-popup">
        <div class="text">
            开通成功！<br/> 账号密码短信已成功发送至老师绑定的手机
        </div>
        <div class="popup-btn">
            <a href="javascript:void(0);" style="background:#ff7d5a;color:#fff;border-bottom-right-radius:0.2rem;width:100%" class="closeBtn">确定</a>
        </div>
    </div>
    <div class="popup-mask js-remove"></div>
</div>
    <#if schoolQuizBankAdministratorType?? && schoolQuizBankAdministratorType != 'NOTEXIST' && schoolQuizBankAdministratorType != 'ALREADY'>
    <div id="confirmSignDialog" class="closeParent" style="display:none">
        <div class="clazz-popup">
            <div class="text">
                该学校已有校本题库管理员${otherExistenceTeacherName}老师，确定要取消${otherExistenceTeacherName}老师的管理员权限，设置新的校本题库管理员？
            </div>
            <div class="popup-btn">
                <a href="javascript:void(0);" class="closeBtn">取消</a>
                <a href="javascript:void(0);" style="background:#ff7d5a;color:#fff;border-bottom-right-radius:0.2rem;" class="js-submit" id="getSchoolGateImageBtn">确定</a>
            </div>
        </div>
        <div class="popup-mask js-remove"></div>
    </div>


    </#if>
<script>
    var AT = agentTool();
    <#--var teacherStatus = '${schoolQuizBankAdministratorType!'00'}';-->
    var teacherId = '${teacherId!0}';
    var schoolId = '${schoolId!0}';
    $(document).on('click','.sureBtn',function(){
        $.post('createaffairteacher.vpage',{teacherId:teacherId,schoolId:schoolId},function(res){
            if(res.success){
                $("#changeSignDialog").show();
            }else{
                AT.alert(res.info);
            }
        });
    });
    $(document).on('click','.closeBtn',function(){
        $("#changeSignDialog").hide();
        disMissViewCallBack();
    });
//    $(document).on('click','#getSchoolGateImageBtn',function(){
//        $.post('change_school_quiz_bank_administrator.vpage',{teacherId:teacherId,schoolId:schoolId},function(data){
//            if(data.success){
//                AT.alert('更改成功');
//                setTimeout('location.href="/mobile/resource/teacher/card.vpage?teacherId="+teacherId',2000);
//            }else{
//                AT.alert(data.info)
//            }
//        })
//    });
</script>
</@layout.page>
