/**
 * 产品反馈选择老师
 * ziqi.feng 2-21-2017
 * */
define(["dispatchEvent","handlebars","common"], function (dispatchEvent) {
    $(document).ready(function(){
        var AT = new agentTool();

        var getTeacherList = function(keyWord){
            $("#loading").show();
            $.post("/mobile/resource/teacher/search.vpage",{searchKey:keyWord, scene:3},function(res){
                $("#loading").hide();
                if(res.success){
                    renderTeacherList("teacherListTemp", res.dataMap, "#teacherContainer");
                }else{
                    AT.alert("未找到老师信息");
                }
            });
        };

        //渲染模板
        var renderTeacherList = function(tempSelector,data,container){
            var contentHtml = template(tempSelector, data);
            $(container).html(contentHtml);
        };

        //注册事件
        var eventOption = {
            base:[
                {
                    selector:".js-search",
                    eventType:"click",
                    callBack:function(){
                        var teacherKeyWords = $("#teacherSearchInput").val().trim("");
                        getTeacherList(teacherKeyWords);
                    }
                },{
                    selector:".js-teacherItem",
                    eventType:"click",
                    callBack:function(){
                        var teacherId = $(this).data().sid;
                        if(backUrl === "customer.vpage"){
                            if (!type) {
                                type = "change_school_page";
                            }
                            if(type ==="change_school_page"){
                                window.location.href = "/mobile/task/change_school_page.vpage?teacherId="+teacherId +"&choiceTeacherAble=true";
                            }
                            if(type ==="create_class_page"){
                                window.location.href = "/mobile/task/create_class_page.vpage?teacherId="+teacherId +"&choiceTeacherAble=true" ;
                            }
                            if(type ==="bind_mobile_page"){
                                window.location.href = "/mobile/task/bind_mobile_page.vpage?teacherId="+teacherId +"&choiceTeacherAble=true";
                            }
                        }else{
                            window.location.href = "feedbackinfo.vpage?teacherId="+teacherId ;
                        }

                    }
                }
            ]
        };

        new dispatchEvent(eventOption);

    });
});