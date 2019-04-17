/**
 * 模考的试卷详情-学生端
 * tsingjing
 */
$(function(){
    var ajaxData = {};
    var if_dot_bottom  = false;
    var url = "/container/student/newexam/report.vpage";
    var params = {
        exam_id:$17.getQuery("examId"),
        user_id:$17.getQuery("userId")
    }
    $.ajax(url,{
        method   : 'GET',
        data:params,
        dataType : 'json'
    }).done(function( res, textStatus, jqXHR ){
        if(!res.success){//获取数据失败
            $("#showAction").hide();
            $("#hideAction").show();
            $("#hideAction_p").html(res.info);
        }else{
            initData(res);
        }
    }).fail(function(jqXHR, textStatus, e){

    }).always(function(){

    });
    function initData(res) {
        $17.voxLog({
            module : "m_86DDZQCl",
            op     : "o_UQkiwgpMUU",
            s0     : $17.getQuery("examId")
        },"student");
        var ajaxData = [res.data];
        var Detailsreport = function (options) {
            this.ajaxData = ko.observable(ajaxData);
            this.boxStar = function (value) {
                if(value == 1){
                    return "star-box star-box-1";
                }else if(value == 2){
                    return "star-box star-box-2";
                }else if(value == 3){
                    return "star-box star-box-3";
                }else if(value == 4){
                    return "star-box star-box-4";
                }else if(value == 5){
                    return "star-box star-box-5";
                }else{
                    return "star-box";
                }
            }
            this.init();
        };
        Detailsreport.prototype = {
            constructor: Detailsreport,
            init: function () {
                $(document).on("click",".gotop",function () {
                    $('html,body').animate({scrollTop: 0});
                });
                $(window).scroll(function () {
                    var scrollTop = $(this).scrollTop(); //已经滚动到上面的页面高度
                    var scrollHeight = $(document).height();//页面高度
                    var windowHeight = $(this).height();//浏览器窗口高度
                    if (scrollTop + windowHeight == scrollHeight) {//此处是滚动条到底部时候触发的事件，在这里写要加载的数据，或者是拉动滚动条的操作
                        if(!if_dot_bottom){
                            if_dot_bottom = true;
                            $17.voxLog({
                                module : "m_86DDZQCl",
                                op     : "o_Lv593M7ah9",
                                s0     : $17.getQuery("examId")
                            },"student");
                        }
                    };
                });
            },
        };
        ko.cleanNode(document.getElementById("detailsreport"));
        ko.applyBindings(new Detailsreport(),document.getElementById("detailsreport"));
    }


})