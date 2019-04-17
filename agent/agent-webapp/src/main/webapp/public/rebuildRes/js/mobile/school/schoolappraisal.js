
define(["dispatchEvent"],function(dispatchEvent){

    var  eventOption = {
        base:[
            {
                selector:".js-name",
                eventType:"click",
                callBack:function(){
                    window.location.href = "auth_school.vpage"
                }
            },
            {
                selector:".js-photo",
                eventType:"click",
                callBack:function(){
                    openSecond("/mobile/school_clue/school_photo_page.vpage?type=" + type + "&returnUrl=schoolappraisal.vpage");
                }
            },
            {
                selector:".js-submit",
                eventType:"click",
                callBack:function(){
                    submitEvent()
                }
            }
        ]
    };
    new dispatchEvent(eventOption);
    var submitEvent = function(){
        $.post("appraisalSchool.vpage",function(res){
            if(res.success){
                AT.alert("提交成功");
                setTimeout("window.history.back();",1500);
            }else{
                AT.alert(res.info);
            }
        })
    };
    var setTopBar = {
        show:true,
        rightText:'提交',
        rightTextColor:"ff7d5a",
        needCallBack:true
    };
    setTopBarFn(setTopBar,submitEvent);
    $(document).ready(function () {
        reloadCallBack();
    })
});