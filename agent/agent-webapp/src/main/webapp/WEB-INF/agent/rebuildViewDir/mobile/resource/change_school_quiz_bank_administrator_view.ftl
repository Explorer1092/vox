<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="设为校本题库管理员" pageJs="" footerIndex=4 navBar="hidden">
    <@sugar.capsule css=['school']/>
<script src="/public/rebuildRes/js/common/common.js"></script>
<div class="flow">
    <div style="font-size:.75rem;padding:.5rem">
        <span>
            说明:
            <p style="padding:.3rem 0">
                1.校本题库管理员每个学校每个科目仅可设置一位老师
            </p>
            <p style="padding:.3rem 0">
                2.将老师设置为校本题库管理员后，1个月之内不可变更
            </p>
             <p style="padding:.3rem 0">
                3.学校需为字典表学校且开通了阅卷机权限
            </p>
            <p style="padding:.3rem 0">
                4.学校需开通校本题库权限
            </p>
            <div class="sureBtn" style="background:#ff7d5a;width:100%;padding:.5rem 0;text-align:center;color:#fff;margin-top:1rem">设为校本题库管理员</div>
        </span>
    </div>
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

        <div id="changeSignDialog" class="closeParent" style="display:none">
            <div class="clazz-popup">
                <div class="text">
                    该学校30天内已设置校本题库管理员${otherExistenceTeacherName}老师，如需变更请联系销运负责人
                </div>
                <div class="popup-btn">
                    <a href="javascript:void(0);" style="background:#ff7d5a;color:#fff;border-bottom-right-radius:0.2rem;width:100%" class="closeBtn">确定</a>
                </div>
            </div>
            <div class="popup-mask js-remove"></div>
        </div>
    </#if>
<script>
    var AT = agentTool();
    var teacherStatus = '${schoolQuizBankAdministratorType!'00'}';
    var teacherId = '${teacherId!0}';
    var schoolId = '${schoolId!0}';
    $(document).on('click','.sureBtn',function(){
        switch (teacherStatus){
            case 'ONLYBEMODIFIEDONCEAMONTH' :

                $('#changeSignDialog').show();
                $('#confirmSignDialog').hide();
                break;

            case 'OTHEREXISTENCE' :

                $('#confirmSignDialog').show();
                $('#changeSignDialog').hide();
                break;

            case 'NOTEXIST':
                $.post('change_school_quiz_bank_administrator.vpage',{teacherId:teacherId,schoolId:schoolId},function(res){
                    if(res.success){
                        AT.alert('添加成功');
                        setTimeout('window.history.back()',2000);
                    }else{
                        AT.alert(res.info);
                    }
                });
                break;
            case 'ALREADY':
                AT.alert("当前老师已经是校本题库管理员");
                break;
            case 'NOTEXISTSUBJECT':
                AT.alert("老师暂无负责科目");
                break;
            default:
                AT.alert("${schoolQuizBankAdministratorMessage}");
                break;

        }
    });
    $(document).on('click','.closeBtn',function(){
        $('.closeParent').hide();
    });
    $(document).on('click','#getSchoolGateImageBtn',function(){
        $.post('change_school_quiz_bank_administrator.vpage',{teacherId:teacherId,schoolId:schoolId},function(data){
            if(data.success){
                AT.alert('更改成功');
                setTimeout('disMissViewCallBack()',2000);
            }else{
                AT.alert(data.info)
            }
        })
    });
</script>
</@layout.page>
