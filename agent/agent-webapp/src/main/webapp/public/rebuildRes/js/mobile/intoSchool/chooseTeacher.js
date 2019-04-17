/**
 * 选择老师
 * */
define(["dispatchEvent","common"], function (dispatchEvent) {
    $(document).ready(function(){
        var AT = new agentTool();
        //注册事件
        var eventOption = {
            base:[
                {
                    selector:".js-subTab>a",
                    eventType:"click",
                    callBack:function(){
                        var type = $(this).data("type");
                        $(this).addClass("the").siblings("a").removeClass("the");
                        $("."+type+"Con").show().siblings("div").hide();
                    }
                },
                {
                    selector:".btn-stroke",
                    eventType:"click",
                    callBack:function(){
                        $(this).toggleClass("orange");
                    }
                },
                {
                    selector:".js-submitTehBtn",
                    eventType:"click",
                    callBack:function(){
                        var list = [];
                        $.each($(".js-TeacherCon").find('.orange'),function(i,item){
                            list.push($(item).data("tid"));
                        });
                        if(backUrl == 'addMeeting'){
                            store.set("meetingTeacherIds",list);
                            setTimeout(disMissViewCallBack(),100);
                        }else{
                            $.post("saveTeacherList.vpage",{teacherIds:list.join(",")},function(res){
                                if(res.success){
                                    disMissViewCallBack();
                                }else{
                                    AT.alert(res.info);
                                }
                            });
                        }
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
};
var topBarCallBack = function () {
    $(".js-submitTehBtn").click();
};
setTopBarFn(setTopBar,topBarCallBack);