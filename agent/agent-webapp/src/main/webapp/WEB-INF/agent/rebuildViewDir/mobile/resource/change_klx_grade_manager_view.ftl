<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="设置年级主任" pageJs="" footerIndex=4 navBar="hidden">
    <@sugar.capsule css=['researchers']/>
<div class="visitResearchers-box">
    <ul class="vir-list">
        <li>
            老师管理年级（可多选）
            <div class="c-main clearfix" id="visit_pur" style="padding-bottom: 0.25rem;">

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
<script type="text/html" id="gradeManager">
    <%if( res.gradeList && res.gradeList.length> 0){%>
        <%for(var i=0;i< res.gradeList.length;i++){%>
        <div class="btn-stroke fix-padding <%if(res.gradeList[i].selected){%>js-clazz the<%}%>" data-type="<%=res.gradeList[i].value%>"><%=res.gradeList[i].text%></div>
        <%}%>
    <%}%>
</script>
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
                for (var i = 0; i < $('.the').length; i++) {
                    type += $('.the').eq(i).data('type') + ',';
                }
                $.post('setgrademanagelist.vpage', {teacherId:teacherId,schoolId:schoolId,grades:type,desc:$('#flow').val()}, function (res) {
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
    $.get("getgrademanagelist.vpage?schoolId="+schoolId+"&teacherId="+teacherId,function (res) {
        $('#visit_pur').html(template("gradeManager",{res:res}))
    });
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
        if($('#flow').val() == ''){
            AT.alert('请输入添加/变更原因');
            check = false;
        }
    }

</script>
</@layout.page>