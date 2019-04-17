/**
 * 月报详情
 * */
define(["dispatchEvent"], function (dispatchEvent) {
    $(document).ready(function(){
        //注册事件
        var eventOption = {
            base:[
                {
                    selector:"",
                    eventType:"click",
                    callBack:function(){

                    }
                }
            ]
        };

        new dispatchEvent(eventOption);


    });
});