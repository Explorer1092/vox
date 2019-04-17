/*Created by yifei.peng on 2016/7/5.*/
define(["knockout", "YQ", "voxLogs", "voxSpread", "impromptu"], function(ko){
    var viewDataJson = {
        count : ko.observable(15)
    };

    console.info(YQ)

    ko.applyBindings(viewDataJson);

    $(".js-btn-openDialog").on({
        click : function(){
            $.prompt(location.host + " = 数据加载中..." + viewDataJson.count(), {
                title : "系统提示",
                position : { width: 260},
                buttons : {
                    "关闭" : true
                },
                submit : function(e, v){
                    //
                }
            });
        }
    });
});