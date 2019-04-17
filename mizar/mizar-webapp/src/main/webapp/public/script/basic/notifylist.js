/**
 * Created by free on 2016/12/6.
 */
define(["jquery","prompt","paginator"],function($){

    // 分页
    var paginator = $('#paginator');
    var pages = $(".one-page");
    var currentPage = 1;
    if (paginator.length > 0) {
        paginator.jqPaginator({
            totalPages: pages.length,
            visiblePages: 10,
            currentPage: 1,
            first: '<li class="first"><a href="javascript:void(0);">首页<\/a><\/li>',
            prev: '<li class="prev"><a href="javascript:void(0);">上一页<\/a><\/li>',
            next: '<li class="next"><a href="javascript:void(0);">下一页<\/a><\/li>',
            last: '<li class="last"><a href="javascript:void(0);">尾页<\/a><\/li>',
            page: '<li class="page"><a href="javascript:void(0);">{{page}}<\/a><\/li>',
            onPageChange: function (num) {
                pages.eq(num - 1).addClass("displayed").siblings().removeClass("displayed");
                currentPage = num;
            }
        });
    }

    var alertDialog =function(data){
        var contentHtml = "<div style='text-align:center;'>"+ data.info +"</div>";
        $.prompt(contentHtml,{
            title: "温馨提示",
            buttons: {"确定": true},
            focus: 1,
            submit: function (e, v) {
                if (v) {
                    if(data.success){
                        location.reload();
                    }
                }
            }
        });
    };

    //标记已读信息
    $(document).on("click",".js-readBtn",function(){
        $.post("read.vpage",{id:$(this).data("sid")},function(res){
            alertDialog(res);
        });
    });

    //删除信息
    $(document).on("click",".js-delBtn",function(){
        $.post("remove.vpage",{id:$(this).data("sid")},function(res){
            alertDialog(res);
        });
    });

});