
define(['dispatchEvent'], function (dispatchEvent) {
    $(document).ready(function () {
        YQ.voxLogs({
            database : "marketing", //不设置database  , 默认库web_student_logs
            module : "m_sGRM6Xci", //打点流程模块名
            op : "o_gGCAtgIB" ,//打点事件名
            userId:userId,
            s0 : $(".js-showHand").html()
        });
        if(groupLevel=='小学'){
            var setTopBar = {
                show:true,
                rightText:'小学',
                rightTextColor:"ff7d5a",
                needCallBack:false
            };
        }else{
            var setTopBar = {
                show:true,
                rightText:groupLevel,
                rightTextColor:"ff7d5a",
                needCallBack:true
            };
        }
        var callBackFn = function(){
            $('.show_now').toggle();
        };
        setTopBarFn(setTopBar,callBackFn);
    });
    var eventOption = {
        base: [
            {
                selector: ".tab_row",
                eventType: "click",
                callBack: function () {
                    var data = $(this).data();
                    var idType = $(this).data().idtype;
                    if(!$(this).hasClass("active")){
                            window.location.href = "/mobile/performance/performance_overview.vpage?id=" + data.id +"&idType="+ data.idtype +"&schoolLevel="+$(this).data("index") +"&mode="+$(this).data("mode");
                    }else{
                        $(".show_now").hide();
                    }
                }
            },{
                selector: ".js-item",
                eventType: "click",
                callBack: function () {
                    if(idType == "GROUP"){
                        openSecond("/mobile/performance/performance_list_page.vpage?id="+id+"&idType="+idType+"&schoolLevel="+schoolLevel+"&mode="+mode)
                    }else if(idType == "USER" || idType == "OTHER_SCHOOL"){
                        openSecond("/mobile/performance/school_performance.vpage?id="+id+"&idType="+idType+"&schoolLevel="+schoolLevel+"&mode="+mode);
                    }
                }
            },{
                selector: ".tab_index",
                eventType: "click",
                callBack: function () {
                    var index = $(this).index();
                    $(".nav_2").eq(index).toggle().siblings().hide();
                }
            }
        ]
    };
    new dispatchEvent(eventOption);
});