/**
 * 添加进校计划
 * */
define(["dispatchEvent","handlebars","common"], function (dispatchEvent) {
    $(document).ready(function(){
        console.log(store)

        var AT = new agentTool();

        var getSchoolList = function(keyWord){
            $("#loading").show();
            var url = "searchVisitSchool.vpage?";

            var scene = 1;
            if(backUrl === "add_intoSchool_record.vpage"){
                scene = 3;
            }else if(backUrl === "add_visit_plan.vpage") { //拜访计划选择学校
                scene = 3;
            }else if (backUrl === "addMeeting"){
                scene = 3;
            }else if(backUrl.indexOf("change_school_page.vpage") != -1) { //转校选择学校
                scene = 2;
            }


            if(keyWord){
                url = url +"schoolKey="+keyWord+"&scope=" + scene;
            }

            $.get(url,function(res){
                $("#loading").hide();
                if(res.success){
                    renderSchoolList("schoolListTemp",res,"#schoolContainer");
                }else{
                    AT.alert(res.info);
                }
            });
        };

        //渲染模板
        var renderSchoolList = function(tempSelector,data,container){
            var contentHtml = template(tempSelector, data);
            $(container).html(contentHtml);
        };

        if(backUrl.indexOf("change_school_page.vpage") != -1){
        }else{
            getSchoolList();
        }

        //注册事件
        var eventOption = {
            base:[
                {
                    selector:".js-search",
                    eventType:"click",
                    callBack:function(){
                        var schoolKeyWords = $("#schoolSearchInput").val().trim("");
                        getSchoolList(schoolKeyWords);
                    }
                },
                {
                    selector:".js-schoolItem",
                    eventType:"click",
                    callBack:function(){
                        var sid = $(this).data("sid");
                        var schoolName = $(this).data("name");
                        //var url = "saveVisitSchool.vpage";
                        if(backUrl === "add_intoSchool_record.vpage"){ //进校选择学校
                            $.post("saveSchoolRecordSchool.vpage", {schoolId: sid}, function (res) {
                                if (res.success) {
                                    disMissViewCallBack();
                                } else {
                                    AT.alert(res.info);
                                }
                            });
                        }else if(backUrl === "add_visit_plan.vpage") { //拜访计划选择学校
                            $.post("saveVisitSchool.vpage", {schoolId: sid}, function (res) {
                                if (res.success) {
                                    location.href = backUrl;
                                } else {
                                    AT.alert(res.info);
                                }
                            });
                        }else if (backUrl === "addMeeting"){
                            // $.post("saveVisitSchool.vpage", {schoolId: sid}, function (res) {
                            //     if (res.success) {
                            //         disMissViewCallBack();
                            //     } else {
                            //         AT.alert(res.info);
                            //     }
                            // });
                            store.set("meetingSchoolId",sid);
                            store.set("meetingSchoolName",schoolName);
                            setTimeout(disMissViewCallBack(),100)
                        }else if(backUrl.indexOf("change_school_page.vpage") != -1){ //转校选择学校
                            url = "/mobile/task/change_school_page.vpage?teacherId=" + getQuery("teacherId") + "&schoolId=" + sid + "&choiceTeacherAble=" + choiceTeacherAble;
                            location.href = url;
                            return;

                        }
                    }
                }
            ]
        };

        new dispatchEvent(eventOption);

    });
});