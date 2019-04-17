/*-----------客户管理相关-----------*/
define(["jquery","prompt","datetimepicker","paginator","jqform"],function($){

    /*预约管理分页插件*/
    var paginator = $('#paginator');
    var pages = $(".one-page");
    var currentPage = 1;
    if(paginator.length>0){
        paginator.jqPaginator({
            totalPages: pages.length,
            visiblePages: 10,
            currentPage: parseInt(paginator.attr("data-startPage")||1),
            first: '<li class="first"><a href="javascript:void(0);">首页<\/a><\/li>',
            prev: '<li class="prev"><a href="javascript:void(0);">上一页<\/a><\/li>',
            next: '<li class="next"><a href="javascript:void(0);">下一页<\/a><\/li>',
            last: '<li class="last"><a href="javascript:void(0);">尾页<\/a><\/li>',
            page: '<li class="page"><a href="javascript:void(0);">{{page}}<\/a><\/li>',
            onPageChange: function (num) {
                pages.eq(num-1).addClass("displayed").siblings().removeClass("displayed");
                currentPage = num;
            }
        });
    }

    $(".checked-all").on("click",function(){
        $("table.displayed tbody").find("[type='checkbox']").click();
    });

    $("#js-filter").on("click",function(){
        $("#filter-form").submit();
    });
    //改变客户状态
    var changeType = $("#status-change-type");
    $(".change-status-batch").on("click",function(){
        $.prompt("<div style='text-align:center;'>是否确认将选中客户状态从"+changeType.find('option:selected').html()+"？</div>", {
            title: "填写备注",
            buttons: { "取消": false, "确定": true },
            submit: function( e,v ){
                if ( v ) {
                    var checked = $("tbody input:checked");
                    if(checked.length > 0){
                        var rids = [];
                        checked.each(function(){
                            rids.push($(this).attr("data-rid"));
                        });
                        var fromTo = changeType.val().split("-");
                        var data={
                            reservations : rids.join(','),
                            pre          : fromTo[0],
                            post         : fromTo[1]
                        };
                        $.post("/crm/reserve/changestatus.vpage",data,function(res){
                            if(res.success){
                                location.href = "/crm/reserve/index.vpage?page="+currentPage;
                            }else{
                                $.prompt("<div style='text-align:center;'>"+(res.info||"变更状态失败！")+"</div>", {
                                    title: "错误提示",
                                    buttons: { "确定": true },
                                    focus : 1,
                                    useiframe:true
                                });
                            }
                        });
                    }
                }
            },
            useiframe:true
        });
    });

    $(".change-status").on("click",function(){
        var $this = $(this);
        var data={
            reservations : $this.attr("data-rid"),
            pre          : $this.parent().prev().attr("data-value"),
            post         : $this.attr("data-value")
        };
        if(confirm("确认修改吗？")){
            $.post("/crm/reserve/changestatus.vpage",data,function(res){
                if(res.success){
                    location.href = "/crm/reserve/index.vpage?currentPageNumber="+currentPage;
                }else{
                    $.prompt("<div style='text-align:center;'>"+(res.info||"变更状态失败！")+"</div>", {
                        title: "错误提示",
                        buttons: { "确定": true },
                        focus : 1,
                        useiframe:true
                    });
                }
            });
        }

    });

    //添加备注
    $(".change-remark").on("click",function(){
        var $this = $(this);
        $.prompt("<div style='text-align:center;'>请在此填写备注内容<br /><div class='input-control' style='margin:20px auto 0;width:300px;'><textarea class='new-mark' style='width:300px;resize: none;'></textarea></div></div>", {
            title: "填写备注",
            buttons: { "取消": false, "确定": true },
            submit: function( e,v ){
                if ( v ) {
                    var newMark=$(".new-mark").val();
                    $.post("/crm/reserve/takenote.vpage",{reserveId:$this.attr("data-rid"),notes:newMark},function(res){
                        if(res.success){
                            $this.parent().next().children(".inner").html(newMark);
                        }else{
                            $.prompt("<div style='text-align:center;'>"+(res.info||"修改失败！")+"</div>", {
                                title: "错误提示",
                                buttons: { "确定": true },
                                focus : 1,
                                useiframe:true
                            });
                        }
                    });
                }
            },
            useiframe:true
        });
    });
});