/**
 * 首页
 * */
define(["dispatchEvent"], function (dispatchEvent) {
    $(document).ready(function(){
        var AT = new agentTool();
        //注册事件
        var eventOption = {
            base:[
                {
                    selector:".js-return",
                    eventType:"click",
                    callBack:function(){
                        var type = $(this).data().type;
                        if (!type) {
                            type = "change_school_page";
                        }
                        if (type === "change_school_page") {
                            window.location.href = "/mobile/task/change_school_page.vpage?teacherId=" + teacherId + "&choiceTeacherAble=" + choiceTeacherAble;
                        }
                        if (type === "create_class_page") {
                            window.location.href = "/mobile/task/create_class_page.vpage?teacherId=" + teacherId + "&choiceTeacherAble=" + choiceTeacherAble;
                        }
                        if (type === "bind_mobile_page") {
                            window.location.href = "/mobile/task/bind_mobile_page.vpage?teacherId=" + teacherId + "&choiceTeacherAble=" + choiceTeacherAble;
                        }
                    }
                },{
                    selector:".js-chooseTeacher",
                    eventType:"click",
                    callBack:function(){
                        var type = $(this).data().type;
                        window.location.href = "/mobile/feedback/view/searchteacher.vpage?back=customer.vpage" +"&type="+type+ "&choiceTeacherAble=true";
                    }
                }
            ]
        };

        new dispatchEvent(eventOption);


    });
    var setTopBar = {
        show: true,
        rightText:"提交" ,
        rightTextColor: "ff7d5a",
        needCallBack: true
    };
    var topBarCallBack =  function(){
        try{
        callBackFn();
        }catch(e){
            alert(e)
        }
    };
    setTopBarFn(setTopBar, topBarCallBack);
});