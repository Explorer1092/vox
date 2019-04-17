/*-----------客户管理相关-----------*/
define(["jquery","prompt","datetimepicker","paginator","jqform"],function($){

    /*评论管理分页插件*/
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

    $("#js-filter").on("click",function(){
        $("#filter-form").submit();
    });

    $(".show-pic").on("click",function(){
        var $this = $(this);
        var pic = $this.data("pic").split(",");
        var $html = "";
        for ( var i=0; i<pic.length; ++i) {
            $html += "<img style='width:143px;height:109px;margin-top:20px;' src='"+pic[i]+"' />"
        }
        $.prompt("<div style='text-align:center;'>"+$html+"</div>", {
            title: "评论图片",
            buttons: { "确定": true },
            useiframe:true
        });
    });

    $(".show-detail").on("click", function(){
        var $this = $(this);
        var $html = $this.html();
        $.prompt("<div style='text-align:center;'>"+$html+"</div>", {
            title: "评论详情",
            buttons: { "确定": true },
            useiframe:true
        });
    });

    $(".change-online").on("click", function () {
        var $this = $(this);
        var data={
            rid     : $this.data("rid")
        };
        $.post("/biz/rating/online.vpage",data,function(res){
            if(res.success){
                location.href = "/biz/rating/index.vpage?page="+currentPage;
            }else{
                $.prompt("<div style='text-align:center;'>"+(res.info||"评论上线失败！")+"</div>", {
                    title: "错误提示",
                    buttons: { "确定": true },
                    focus : 1,
                    useiframe:true
                });
            }
        });
    });

    $(".change-offline").on("click", function () {
        var $this = $(this);
        var data={
            rid     : $this.data("rid")
        };
        $.post("/biz/rating/offline.vpage",data,function(res){
            if(res.success){
                location.href = "/biz/rating/index.vpage?page="+currentPage;
            }else{
                $.prompt("<div style='text-align:center;'>"+(res.info||"评论下线失败！")+"</div>", {
                    title: "错误提示",
                    buttons: { "确定": true },
                    focus : 1,
                    useiframe:true
                });
            }
        });
    });

    $(".change-delete").on("click",function(){
        var $this = $(this);
        var data={
            rid     : $this.data("rid")
        };
        $.post("/biz/rating/delete.vpage",data,function(res){
            if(res.success){
                location.href = "/biz/rating/index.vpage?page="+currentPage;
            }else{
                $.prompt("<div style='text-align:center;'>"+(res.info||"删除评论失败！")+"</div>", {
                    title: "错误提示",
                    buttons: { "确定": true },
                    focus : 1,
                    useiframe:true
                });
            }
        });
    });
});