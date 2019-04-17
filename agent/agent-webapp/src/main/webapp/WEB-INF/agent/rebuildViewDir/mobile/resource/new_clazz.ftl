<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="申请客服协助" pageJs="customer" footerIndex=2>
    <@sugar.capsule css=['custSer']/>
<div class="crmList-box resources-box">
    <div class="aut-title">请选择需要客服协助的事项</div>
    <div class="c-opts gap-line tab-head c-flex c-flex-3" style="border-top: 1px solid #cdd3dc; ">
        <span class="js-return" data-type="change_school_page"><a href="javascript:void(0)">转校</a></span>
        <span class="js-return the" data-type="create_class_page"><a href="javascript:void(0)">新建班级</a></span>
        <span class="js-return" data-type="bind_mobile_page"><a href="javascript:void(0)">绑定/解绑手机</a></span>
    </div>
    <div class="content">
        <div class="flow newContent">
            <#if choiceTeacherAble?? && choiceTeacherAble>
                <div class="item">
                    老师姓名
                    <span class="inner-right js-chooseTeacher" data-type="create_class_page"><#if teacherName??>${teacherName!''}<#else>请选择</#if></span>
                    <input type="hidden" value="<#if teacherId??>${teacherId!''}</#if>" id="teacherId">
                </div>
            </#if>
            <div class="aut-title">请填写年级和班号</div>
            <div class="classList">
                <div class="classItem" data-index="0">
                    <div class="gradle-info">
                        <span><input type="tel" class="js-gradle" maxlength="5"></span>年级 <span><input type="tel" class="js-class" maxlength="5"></span>班
                        <span>
                            <span class="js-add" style="padding: .2rem"> + </span>
                            <span class="js-remove" style="padding: .2rem"> - </span>
                        </span>
                    </div>
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
        var maxSize = 9;
        $(document).on("click",".js-add",function(){
            if(maxSize){
                var index = $(".classList>.classItem").length;
                var temp = '<div class="classItem" data-index="'+index+'">'+
                        '<div class="gradle-info">'+
                        '<span><input type="tel" class="js-gradle"></span>年级 <span><input type="tel" class="js-class"></span>班'+
                        '<span>'+
                        '<span class="js-add" style="padding: .2rem"> + </span>'+
                        '<span class="js-remove" style="padding: .2rem"> - </span>'+
                        '</span>'+
                        '</div>'+
                        '</div>';
                $(".classList").append(temp);
                maxSize--;
            }else{
                AT.alert("一次最多创建10个班级");
            }
        });

        $(document).on("click",".js-remove",function(){
            var index = $(".classList>.classItem").length;
            if(index >1){
                $(this).parents(".classItem").remove();
            }else{
                AT.alert("至少新建一个班级");
            }
        });
    });
    var callBackFn = function () {
        var postData = {};
        postData.teacherId = ${teacherId!0};
        var changeNode = $(".gradle-info");
        var gradleFlag = true;
        var classFlag = true;
        if(changeNode.length){
            var list = [];
            $.each(changeNode,function(i,item){
                var gradleName = $(item).find(".js-gradle").val();
                var className = $(item).find(".js-class").val();
                if(!gradleName){
                    gradleFlag = false;
                }
                if(!className){
                    classFlag = false;
                }
                if(gradleName && className){
                    list.push(gradleName+"年级"+className+"班");
                }
            });
            postData.clazzNames = list.join(",");
        }else{
            AT.alert("请填写年级和班号");
            return false;
        }

        if(!gradleFlag || !classFlag){
            AT.alert("请填写完整年级和班级名称");
        }

        postData.comment = $("#markMsg").val();

        if(postData.clazzNames.length != 0){
            $.post("teacher_create_clazz_task.vpage",postData,function(res){
                if(res.success){
                    AT.alert("创建成功");
                    location.href = "/mobile/task/task_list.vpage";
                }else{
                    AT.alert(res.info);
                }
            })
        }
    }
</script>
</@layout.page>