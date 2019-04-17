
    define(["dispatchEvent"],function(dispatchEvent){
        var eventOption = {
            base: [
                    {
                        selector:".js-tabItem",
                        eventType:"click",
                        callBack:function(){
                            var index = $(this).data().index;
                            $(this).addClass('the').siblings().removeClass('the');
                            $('.tabItem').hide();
                            $(".tab-"+index).show();
                    }
                },
                    {
                        selector:".js-itemBtn",
                        eventType:"click",
                        callBack:function(){
                            var id = $(this).data("sid");
                            var type = $(this).data("type");
                            window.location.href = "apply_detail.vpage?applyId=" + id + "&applyType="+type;
                        }
                }
            ]
        };
        new dispatchEvent(eventOption);
    });