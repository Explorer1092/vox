
define(['dispatchEvent'], function (dispatchEvent) {
    $(document).ready(function(){
        first_lick();
    });
    var eventOption = {
        base: [
            {
                selector:".js-tab",
                eventType:"click",
                callBack:function(){
                    $(this).addClass("active").siblings().removeClass("active");
                    $('.view-box').hide();
                    var viewtype = $(this).data().viewtype;
                    $(".show_"+viewtype).show().siblings().hide();
                    $(".show_"+viewtype+ " .tab_li.active").click();
                }
            },{
                selector:".tab_li",
                eventType:"click",
                callBack:function(){
                    $(this).addClass("active").siblings().removeClass("active");
                    var index = $(this).data().index;
                    $(".table_"+index).show().siblings().hide();
                }
            }, {
                selector: ".srd-nav li",
                eventType: "click",
                callBack: function () {
                    $(this).addClass("active").siblings().removeClass("active");
                }
            }, {
                selector: ".tab_row",
                eventType: "click",
                callBack: function () {
                    var data = $(this).data();
                    var tabIndex = $(".js-tab.active").data().tabindex;
                    var idType = $(this).data().idtype;
                    if(!$(this).hasClass("active")){
                        if(idType == "GROUP"){
                            var href = "/mobile/performance/group_performance.vpage?id=" + data.id +"&idType="+ data.idtype +"&viewType="+ data.views +"&schoolLevel="+$(this).data().index+"&tabIndex="+tabIndex;
                        }else if(idType == "USER" || idType == "OTHER_SCHOOL"){
                            var href = "/mobile/performance/school_performance.vpage?id=" + data.id +"&idType="+ data.idtype +"&viewType="+ data.views +"&schoolLevel="+$(this).data().index+"&tabIndex="+tabIndex;
                        }
                        openSecond(href);
                    }else{
                        $(".show_now").hide();
                    }
                }
            },{
                selector: ".school_row",
                eventType: "click",
                callBack: function () {
                    var data = $(this).data();
                    var idType = $(this).data().idtype;
                    if(!$(this).hasClass("active")){
                            openSecond("/mobile/performance/school_performance.vpage?id=" + data.id +"&idType="+ data.idtype +"&viewType="+ data.views +"&schoolLevel="+$(this).data().index)
                    }else{
                        $(".show_now").hide();
                    }
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
            },{
                selector: ".js-item",
                eventType: "click",
                callBack: function () {
                    var data = $(this).data();
                    var tabIndex = $(".js-tab.active").data().tabindex + 1;
                    var idType= $(this).data().type;
                    if(idType == "GROUP"){
                        var href = "/mobile/performance/group_performance.vpage?id=" + data.id + "&schoolLevel="+ data.level +"&viewType=" + data.views + "&idType=" + data.type + "&tabIndex=" + tabIndex;
                    }else if(idType == "USER" || idType == "OTHER_SCHOOL"){
                        var href = "/mobile/performance/school_performance.vpage?id=" + data.id + "&schoolLevel="+ data.level +"&viewType=" + data.views + "&idType=" + data.type + "&tabIndex=" + tabIndex;
                    }
                    openSecond(href);
                }
            },{
                selector: ".js-userItem",
                eventType: "click",
                callBack: function () {
                    var data = $(this).data();
                    var tabIndex = $(".js-tab.active").data().tabindex + 1;
                    openSecond("/mobile/performance/school_performance.vpage?id=" + data.id + "&schoolLevel="+ data.level +"&viewType=" + data.views + "&idType=" + data.type + "&tabIndex=" + tabIndex);
                }
            },{
                selector: ".tab_index",
                eventType: "click",
                callBack: function () {
                    var index = $(this).index();
                    $(".nav_2").eq(index).toggle().siblings().hide();
                }
            },{
                selector: ".school_card",
                eventType: "click",
                callBack: function () {
                    var modeType = $(this).data().type?$(this).data().type:'';
                    openSecond("/mobile/resource/school/card.vpage?schoolId="+$(this).data().sid+"&modeType="+modeType);
                }
            },{
                selector: ".show_now",
                eventType: "click",
                callBack: function () {
                    $('.show_now').hide();
                }
            },{
                selector: ".nav_2 li",
                eventType: "click",
                callBack: function () {
                    $('.nav_2').hide();
                    if($(this).data().index == "reorder"){
                        $('.tab_index').eq(0).html($(this).html());
                    }else if($(this).data().index == "Infiltration"){
                        $('.tab_index').eq(1).html($(this).html());
                    }
                    $(this).addClass("active").siblings().removeClass("active");
                    var tabIndex = $(".reorder li.active").data("tabindex");
                    var permeability = $(".Infiltration li.active").data("permeability");
                    renderSchool(tabIndex,permeability);
                }
            }
        ]
    };

    new dispatchEvent(eventOption);
    var first_lick = function(){
        $('.js-tab.active').click();
        $('.tab_li.active_btn').click();

    };
    var compare = function(property){
        return function(a,b){
            var value1 = a[property];
            var value2 = b[property];
            return value2 - value1;
        }
    };
    var renderSchool = function(tabIndex,permeability){
        var newFix = [];
        for(var i=0;i<schoolDataList.length;i++){
            if(permeability != ""){
                if(schoolDataList[i].permeability == permeability){
                    newFix.push(schoolDataList[i]);
                }
            }else{
                newFix.push(schoolDataList[i]);
            }
        }
        newFix.sort(compare(tabIndex));
        // console.log(newFix)
        $('.schoolBox').html(template("#schoolContant",{data:newFix}));
    };
    var schoolDataList = [];
        $.get("fetch_school_list.vpage?id=" + id + "&idType=" + idType + "&schoolLevel=" + schoolLevel+"&mode="+mode,function(res){
            if(res.success){
                schoolDataList = res.schoolDataList;
                renderSchool(renderIndex,"");
                $('.nav_2.reorder li.active').click();
            }else{

            }
        });

    var setTopBar = {
        show: true,
        rightText:rightText ,
        rightTextColor: "ff7d5a",
        needCallBack: false
    };
    var topBarCallBack =  function(){
        $(".show_now").toggle();
    };
    setTopBarFn(setTopBar, topBarCallBack);
});