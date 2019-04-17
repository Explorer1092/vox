/**
 * 消息中心
 * */
define(["dispatchEvent"], function (dispatchEvent) {
    $(document).ready(function(){

        //注册事件
        var eventOption = {
            base:[
                {
                    selector:".js-list",
                    eventType:"click",
                    callBack:function(){
                        openSecond("/mobile/notice/noticeList.vpage?category="+$(this).data().info);
                    }
                }
            ]
        };

        new dispatchEvent(eventOption);

    });
});