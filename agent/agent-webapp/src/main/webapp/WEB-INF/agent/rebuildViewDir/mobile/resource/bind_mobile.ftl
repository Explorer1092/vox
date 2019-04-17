<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="申请客服协助" pageJs="customer" footerIndex=2>
    <@sugar.capsule css=['custSer']/>
<script src="/public/rebuildRes/js/common/common.js"></script>
<div class="crmList-box resources-box">
    <div class="aut-title">请选择需要客服协助的事项</div>
    <div class="c-opts gap-line tab-head c-flex c-flex-3" style="border-top: 1px solid #cdd3dc; ">
        <span class="js-return" data-type="change_school_page"><a href="javascript:void(0)">转校</a></span>
        <span class="js-return" data-type="create_class_page"><a href="javascript:void(0)">新建班级</a></span>
        <span class="the js-return" data-type="bind_mobile_page"><a href="javascript:void(0)">绑定/解绑手机</a></span>
    </div>
    <div class="content">
        <div class="flow bindContent">
            <#if choiceTeacherAble?? && choiceTeacherAble>
                <div class="item">
                    老师姓名
                    <span class="inner-right js-chooseTeacher" data-type="bind_mobile_page"><#if teacherName??>${teacherName!''}<#else>请选择</#if></span>
                    <input type="hidden" value="<#if teacherId??>${teacherId!''}</#if>" id="teacherId">
                </div>
            </#if>
            <div class="aut-title">提示:只解绑手机时,可不填写绑定手机号</div>
            <div class="bind">
                解绑手机号
                <span class="inner-right">
                    <div style="max-width: 10rem;overflow: hidden;" id="oldPhone">${mobile!''}</div>
                </span>
            </div>
            <div class="bind">
                绑定手机号
                <span class="inner-right">
                    <input type="tel" id="newPhone" placeholder="请输入绑定的新手机号" maxlength="11">
                </span>
            </div>
        </div>
    </div>
    <div class="custItem">
        <div class="title">备注(选填)</div>
        <div class="custText">
            <textarea maxlength="50" placeholder="特殊说明,如客服方便和老师电话沟通的时间等(50字内)" id="markMsg"></textarea>
        </div>
    </div>
</div>
<script>

    var AT = new agentTool();
    var choiceTeacherAble = "${choiceTeacherAble?c}";
    var teacherId = "${teacherId!0}";
    var callBackFn = function () {
        var postData = {};
        postData.teacherId = ${teacherId!0};
        var newPhone = $("#newPhone").val();
        if(newPhone){
            if(isMobile(newPhone)){
                postData.bindMobile = newPhone;
            }else{
                AT.alert("请输入正确格式的手机号");
                return;
            }
        }else{
            postData.bindMobile = "";
        }
        postData.comment = $("#markMsg").val();
        postData.unbindMobile = "${mobile!''}";

        $.post("teacher_bind_mobile_task.vpage",postData,function(res){
            if(res.success){
                AT.alert("操作成功");
                location.href = "/mobile/task/task_list.vpage";
            }else{
                AT.alert(res.info);
            }
        })
    }
</script>
</@layout.page>