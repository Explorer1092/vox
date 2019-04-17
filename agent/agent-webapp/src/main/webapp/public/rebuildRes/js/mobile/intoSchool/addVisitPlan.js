/**
 * 添加进校计划
 * */
define(["dispatchEvent"], function (dispatchEvent) {
    $(document).ready(function(){
        var AT = new agentTool();
        //保存信息跳页
        var saveInfoToNewPage = function(url){

            var visitTime = $("#visitTime").val();
            var content = $("#content").val();
            var schoolId = $("#schoolId").val();
            var postDate = {
                schoolId:schoolId,
                visitTime:visitTime,
                content:content
            };
            //alert(JSON.stringify(postDate));
            $.post("saveVisitPlan.vpage",postDate,function(res){
                if(res.success){
                    location.href = url;
                }else{
                    AT.alert(res.info);
                }
            });
        };

        //检测提交数据
        var checkData = function(){
            var flag = true;
            $.each($(".js-need"),function(i,item){
                if(!($(item).val())){
                    AT.alert($(item).data("einfo"));
                    flag = false;
                    return false;
                }
            });

            return flag;
        };

        //注册事件
        var eventOption = {
            base:[
                {
                    selector:".js-visitDateBtn",
                    eventType:"click",
                    callBack:function(){
                        $("#visitDateDialog").show();
                    }
                },
                {
                    selector:".js-visitSchoolBtn",
                    eventType:"click",
                    callBack:function(){
                        saveInfoToNewPage("chooseSchool.vpage?back=add_visit_plan.vpage");
                    }
                },
                {
                    selector:".js-selectDate",
                    eventType:"click",
                    callBack:function(){
                        $("#nextVisitTime").click();
                    }
                },
                {
                    selector: "#nextVisitTime",
                    eventType: "change",
                    callBack: function () {
                        $(".select-date").html($(this).val());
                    }
                },
                {
                    selector:".js-submitVisPlan",
                    eventType:"click",
                    callBack:function(){
                        if(checkData()){
                            var postData = {};
                            $.each($(".js-postData"),function(i,item){
                                postData[item.name] = $(item).val();
                            });

                            $.post("savePlan.vpage",postData, function (res) {
                                if(res.success){
                                    location.href =  "/mobile/work_record/visitplan.vpage";
                                }else{
                                    AT.alert(res.info);
                                }
                            })
                        }
                    }
                }
            ]
        };

        new dispatchEvent(eventOption);

    });
});