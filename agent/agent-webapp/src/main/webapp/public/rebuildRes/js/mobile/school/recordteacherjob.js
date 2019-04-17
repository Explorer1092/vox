/**
 * Created by fengwei on 2017/6/5.
 */
define(["dispatchEvent","common"], function (dispatchEvent) {
    $(document).ready(function(){
        var AT = new agentTool();
        //注册事件
        var eventOption = {
            base:[
                {
                    selector:".btn-stroke",
                    eventType:"click",
                    callBack:function(){
                        $(this).toggleClass("orange");
                    }
                }
            ]
        };

        new dispatchEvent(eventOption);
    });
    var setTopBar = {
        show: true,
        rightText:"保存" ,
        rightTextColor: "ff7d5a",
        needCallBack: true
    };
    var topBarCallBack =  function(){
        var list = [];
        $.each($(".js-TeacherCon").find('.orange'),function(i,item){
            list.push($(item).data("tid"));
        });

        $.post("save_teacher_tags.vpage",{teacherId:AT.getQuery("teacherId"),tags:list.join(",")},function(res){
            if(res.success){
                AT.alert('提交成功');
                setTimeout("window.history.back()",1500);
            }else{
                AT.alert(res.info);
            }
        });
    };
    setTopBarFn(setTopBar, topBarCallBack);
});