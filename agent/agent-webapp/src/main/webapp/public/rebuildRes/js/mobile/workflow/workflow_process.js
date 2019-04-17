
define(["dispatchEvent"],function(dispatchEvent){
    var processResult ;
    var eventOption = {
        base: [
            {
                selector:".js-submit",
                eventType:"click",
                callBack:function(){
                    processResult = $(this).data().result;
                    if(processResult == 1){
                        $(".apply_text textarea").html("");
                        $("#repatePane").show();
                    }else if(processResult == 2){
                        $(".apply_pop").show();
                    }
                }
            },
            {
                selector:".white_btn",
                eventType:"click",
                callBack:function(){
                    $(".submitBox").hide();
                }
            },
            {
                selector:".submitBtn",
                eventType:"click",
                callBack:function(){
                    var data = {workflowId:workflowId,processNote:$(".apply_text textarea").val(),processResult:processResult};
                    if(applyType == 'agent_modify_dict_school'){
                        data.processUsers = JSON.stringify([{
                            userPlatform:'agent',
                            account:'system',
                            accountName:'系统'
                        }]);
                    };
                    if(processResult == 1){
                        data.processNote = "同意";
                    }
                    submit(data);
                }
            }
        ]
    };
    new dispatchEvent(eventOption);

    var submit = function(data){
        $.post("process.vpage",data,function(res){
            if(res){
                if(res.success){
                    $(".inner").hide();
                    AT.alert("审核成功");
                    setTimeout("window.location.href='/mobile/audit/todo_list.vpage'",1000);
                }else{
                    AT.alert(res.info);
                }
            }else{
                AT.alert("提交失败");
            }
        });
    }
});