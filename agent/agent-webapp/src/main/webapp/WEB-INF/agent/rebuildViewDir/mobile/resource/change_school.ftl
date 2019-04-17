<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<#if choiceTeacherAble?? && choiceTeacherAble>
    <#assign footerIndex = 4/>
<#else>
    <#assign footerIndex = 2/>
</#if>
<@layout.page title="申请客服协助" pageJs="customer" footerIndex=footerIndex>
    <@sugar.capsule css=['custSer']/>
<div class="crmList-box resources-box">
    <div class="aut-title">请选择需要客服协助的事项</div>
    <div class="c-opts gap-line tab-head c-flex c-flex-3" style="border-top: 1px solid #cdd3dc; ">
        <span class="js-return the" data-type="change_school_page"><a href="javascript:void(0)">转校</a></span>
        <span class="js-return" data-type="create_class_page"><a href="javascript:void(0)">新建班级</a></span>
        <span class="js-return" data-type="bind_mobile_page"><a href="javascript:void(0)">绑定/解绑手机</a></span>
    </div>
    <div class="content">
        <div class="flow switchContent">
            <div class="aut-title">具体协助内容</div>
            <#if choiceTeacherAble?? && choiceTeacherAble>
                <div class="item">
                    老师姓名
                    <span class="inner-right js-chooseTeacher" data-type="change_school_page"><#if teacherName??>${teacherName!''}<#else>请选择</#if></span>
                    <input type="hidden" value="<#if teacherId??>${teacherId!''}</#if>" id="teacherId">
                </div>
            </#if>
            <div class="item">
                转入学校
                <span class="inner-right js-chooseSchool"><#if school??>${school.cname!''}<#else>请选择</#if></span>
                <input type="hidden" value="<#if school??>${school.id!''}</#if>" id="schoolId">
            </div>
            <div class="item GPS clearfix">
                是否带班转校
                <div class="js-select" id="changeOrNot">
                    <div class="btn-stroke withclass" data-type="1">
                        带班转校
                    </div>
                    <div class="btn-stroke withclass" data-type="2">
                        不带班转校
                    </div>
                </div>
            </div>
            <div class="item GPS clearfix">
                带班班级
                <div class="js-minSelect" id="classList" style="display: none;">
                    <#if clazzList?? && clazzList?size gt 0>
                        <#list clazzList as cl>
                            <div class="btn-stroke classitem" data-sid="${cl.cid!0}">
                                ${cl.cname!''}
                            </div>
                        </#list>
                    </#if>
                </div>
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
$(document).on("ready",function(){
   $(".js-select>div").on("click",function(){
     $(this).addClass("the").siblings("div").removeClass("the");
       if($(this).data("type") == 1){
           $("#classList").show();
       }else{
           $("#classList").hide();
       }
   });

   $(".js-minSelect>div").on("click",function(){
     $(this).toggleClass("the");
   });


    $(".js-chooseSchool").on("click",function(){
        window.location.href = "/mobile/work_record/chooseSchool.vpage?back=/mobile/task/change_school_page.vpage?teacherId=${teacherId!0}+&choiceTeacherAble=${choiceTeacherAble?c}";
   });

});
var callBackFn = function () {
    var postData = {};
    postData.teacherId = teacherId;
    var schoolId = $("#schoolId").val();
    if(schoolId){
        postData.targetSchoolId = schoolId;
    }else{
        AT.alert("请选择学校");
        return false;
    }
    var changeNode = $("#changeOrNot>.btn-stroke.the");
    if(changeNode.length){
        postData.includeClazz = true;
        if(changeNode.data("type") == 2){
            postData.includeClazz = false;
        }
    }else{
        AT.alert("请选择是否需要带转校班");
        return false;
    }

    var withClassNode = $("#classList>.btn-stroke.the");
    if(withClassNode.length){
        var list = [];
        $.each(withClassNode,function(i,item){
            list.push($(item).data("sid"));
        });
        postData.clazzIds = list.join(",");

    }else{
        if(postData.includeClazz){
            AT.alert("请选择带班班级");
            return false;
        }
    }

    postData.comment = $("#markMsg").val();

    $.post("teacher_change_school_task.vpage",postData,function(res){
        if(res.success){
            AT.alert("提交成功");
            setTimeout(window.location.href = "/mobile/task/task_list.vpage",1000);

        }else{
            AT.alert(res.info);
        }
    })
}
</script>
</@layout.page>