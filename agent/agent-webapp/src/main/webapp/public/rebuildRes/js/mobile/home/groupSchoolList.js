
define(['dispatchEvent'], function (dispatchEvent) {
    var setTopBar = {
        show:true,
        rightText:groupLevel,
        rightTextColor:"ff7d5a",
        needCallBack:false
    } ;
    /*var callBackFn = function(){
        $(".js-showHand").click();
    };*/
    setTopBarFn(setTopBar);
    var eventOption = {
        base: [
            {
                selector:".tab_li",
                eventType:"click",
                callBack:function(){
                    $(this).addClass("active").siblings().removeClass("active");
                    var index = $(this).data().index;
                    $(".table_"+index).show().siblings().hide();
                }
            }, {
                selector: ".chooseGroupCode",
                eventType: "click",
                callBack: function () {
                    var groupCode = $(this).data("info");
                    $(this).addClass("active").siblings().removeClass("active");
                    renderGroupList(groupCode)
                }
            },{
                selector: ".js-showHand",
                eventType: "click",
                callBack: function () {
                    $(".show_now").show();
                }
            },{
                selector: ".sortable",
                eventType: "click",
                callBack: function () {
                    var colIndex = $(this).index();
                    var table = $(this).closest("table");
                    $(this).addClass("active").siblings().removeClass("active");
                    sortTable(table, colIndex);
                }
            }
        ]
    };
    new dispatchEvent(eventOption);
    template.helper('Math', Math);
    var renderGroupList = function (groupCode) {
        $('.schoolBox').html("");
        $.get("performance_data_list.vpage?id="+id+"&idType="+idType+"&schoolLevel="+schoolLevel+"&mode="+mode+"&groupCode="+ groupCode,function(res){
            if(res.success){
                res.groupCode = groupCode;
                $('.schoolBox').html(template("groupList",{res:res}));
                $('.tab_li').eq(0).click();
            }
        });
    };
    renderGroupList(1);
});