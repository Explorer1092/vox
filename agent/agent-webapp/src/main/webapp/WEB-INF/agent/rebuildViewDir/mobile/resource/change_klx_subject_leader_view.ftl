<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="设置学科组长" pageJs="" footerIndex=4 navBar="hidden">
    <@sugar.capsule css=['researchers']/>
<div class="visitResearchers-box">
    <ul class="vir-list">
        <li>
            老师管理年级（可多选）
            <div class="c-main clearfix" id="visit_pur" style="padding-bottom: 0.25rem;">
                <#if gradeDetailAndIfKlxSubjectLeaderList??>
                    <#list gradeDetailAndIfKlxSubjectLeaderList as lead>
                        <div class="btn-stroke fix-padding <#if lead.ifKlxSubjectLeader?? && lead.ifKlxSubjectLeader>js-clazz the</#if>" data-type="${lead.clazzLevel.level!0}">${lead.clazzLevel.description}</div>
                    </#list>
                </#if>
            </div>
        </li>
    </ul>
</div>
<div class="vir-content">
    <div class="vir-title"><i class="titleIco ico01"></i>添加/变更原因</div>
    <div class="text">
        <textarea name="flow" id="flow" maxlength="100" class="js-need" data-einfo="请填写拜访过程" placeholder="请点击填写，限100字" ></textarea>
    </div>
</div>

<script>
    $(document).ready(function () {
        var setTopBar = {
            show:true,
            rightText:"保存",
            rightTextColor:"ff7d5a",
            needCallBack:true
        } ;
        var callBackFn = function(){
            checkData();
            if(check) {
                var type = "";
                var index = '';
                for (var i = 0; i < $('.the').length; i++) {
                    type += $('.the').eq(i).data('type') + ',';
                }
                type = type.substring(0, type.length - 1);
                console.log(type)
                for (var j = 0; j < $('.js-clazz').length; j++) {
                    index += $('.js-clazz').eq(j).data('type') + ',';
                }
                index = index.substring(0, index.length - 1);
                console.log(index)
                $.post('change_klx_subject_leader.vpage', {teacherId:teacherId,schoolId:schoolId,newClazzLevels:type,oldClazzLevels:index,desc:$('#flow').val()}, function (res) {
                    if (res.success) {
                        AT.alert('保存成功');
                        setTimeout('disMissViewCallBack()',2000);
                    } else {
                        AT.alert(res.info);
                    }
                });
            }
        };
        setTopBarFn(setTopBar,callBackFn);
    });
    var check = true;
    var teacherId = '${teacherId!0}';
    var schoolId = '${schoolId!0}';
    $(document).on('click','.btn-stroke',function(){
        var _this = $(this);
        if(_this.hasClass('the')){
            _this.removeClass('the')
        }else{
            _this.addClass('the')
        }
    check = true;
    });
    $(document).on('change','#flow',function(){
        check = true;
    });

    function checkData(){
       /* if($('.the').length <=0){
            AT.alert('请选择老师管理年级');
            check = false;
        };*/
        if($('#flow').val() == ''){
            AT.alert('请输入添加/变更原因');
            check = false;
        }
    }

</script>
</@layout.page>