/**
 * 进校计划列表
 * */
define(["dispatchEvent","handlebars"], function (dispatchEvent,Handlebars) {
    $(document).ready(function(){
        var AT = new agentTool();

        //注册事件
        var eventOption = {
            base:[
                {
                    selector:".js-delItem",
                    eventType:"click",
                    callBack:function(){
                        var pid = $(this).data("pid");

                        if(confirm("确定要删除该计划?")){
                            $.post("removeProgram.vpage",{
                                recordId:pid
                            },function(res){
                                if(res.success){
                                    AT.alert("删除成功");
                                    location.reload();
                                }else{
                                    AT.alert(res.info);
                                }
                            });

                        }
                    }
                },
                {
                    selector:".js-updateTime",
                    eventType:"click",
                    callBack:function(){
                        var pid = $(this).data("pid");
                        $("#updateDateDialog").show();
                        $("#upDateSure").attr("data-reid",pid);
                        $("#upDate").val($(this).parents(".js-planItem").find(".js-vtime").html().trim());
                    }
                },
                {
                    selector:"#upDateSure",
                    eventType:"click",
                    callBack:function(){
                        var newDate = $("#upDate").val();
                        var reId = $(this).data("reid");

                        $.post("updatePlanTime.vpage",{
                            recordId:reId,
                            updateTime:newDate
                        },function(res){
                            if(res.success){
                                AT.alert("修改时间成功");
                                location.reload();
                            }else{
                                AT.alert(res.info);
                            }
                        });

                        $("#updateDateDialog").hide();
                    }
                },
                {
                    selector:"#upDateCancel",
                    eventType:"click",
                    callBack:function(){
                        $("#updateDateDialog").hide();
                    }

                }
            ]
        };

        new dispatchEvent(eventOption);

    });
});