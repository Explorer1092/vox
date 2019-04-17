/**
 * 修改进校计划
 * */
define(["dispatchEvent"], function (dispatchEvent) {
    $(document).ready(function(){
        var AT = new agentTool();
        var getVisitTeachers = function(){
            var visitTeacher = [];
            $.each($(".js-newItem"), function (i, item) {
                visitTeacher.push({
                    teacherId: $(item).data("tid"),
                    visitInfo: $(item).find("textarea").val().trim()
                })
            });
            return visitTeacher;
        };

        var subPost = function(data,url){
            $.ajax({
                url:"updateSchoolRecord.vpage",
                contentType: 'application/json;charset=UTF-8',
                data:JSON.stringify(data),
                dataType:"json",
                type:"POST",
                success:function(res){
                    if(res.success){
                        disMissViewCallBack();
                    }else{
                        AT.alert(res.info);
                    }
                },
                error:function(e){

                }
            });
        };


        //注册事件
        var eventOption = {
            base:[
                {
                    selector: ".js-submitSchoolSRecord",
                    eventType: "click",
                    callBack: function () {
                        var visitTeacher = getVisitTeachers();
                        var url = "/mobile/work_record/add_intoSchool_record.vpage";
                        subPost({visitTeacher: visitTeacher, schoolMemorandum: $("#school_memorandum").val()}, url);
                    }
                },
                {
                    selector:".js-editSubmit",
                    eventType:"click",
                    callBack:function(){
                        var visitTeacher = getVisitTeachers();
                        var postData = {visitTeacher:visitTeacher,
                            schoolRecordId:sid,
                            schoolMemorandum: $("#school_memorandum").val()
                        };
                        var url = "/mobile/work_record/showSchoolRecord.vpage?recordId="+sid;
                        subPost(postData, url);
                    }
                }
            ]
        };

        new dispatchEvent(eventOption);

    });
});
var setTopBar = {
    show:true,
    rightText:'提交',
    rightTextColor:"ff7d5a",
    needCallBack:true
} ;
var topBarCallBack = function () {
    $(".submitBtn").click();
};
setTopBarFn(setTopBar,topBarCallBack);