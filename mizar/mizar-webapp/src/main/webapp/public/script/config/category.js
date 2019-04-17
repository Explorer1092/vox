/*-----------用户管理相关-----------*/
define(["jquery","prompt","datetimepicker","paginator","jqform","template"],function($){

    /*------------------------列表页------------------------------*/
    $("#index-filter").on("click",function(){
        $("#index-form").submit();
    });

    /*分页插件*/
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

    // 新增功能
    $("#add-category").on('click', function() {
        categoryDialog({});
    });

    // 编辑功能
    $(".edit-category").on('click', function() {
        var cid = $(this).data().cid;
        var category = {
            id : cid,
            first : $('#first_'+cid).html().trim(),
            second : $('#second_'+cid).html().trim()
        };
        categoryDialog(category);
    });

    // 删除功能
    $(".del-category").on('click', function() {
        var cid = $(this).data().cid;
        var category = {
            id : cid,
            first : $('#first_'+cid).html().trim(),
            second : $('#second_'+cid).html().trim()
        };
        $.prompt("<div style='text-align:center;'>是否确认删除？<br/>一级分类："+category.first+"<br/>二级分类："+category.second+"</div>", {
            title: "操作提示",
            buttons: { "确定": true, "取消": false},
            focus : 1,
            submit: function (e, v) {
                if (v) {
                    $.post('/config/category/remove.vpage', {cid:cid}, function (res) {
                        var str = "删除成功";
                        if (res.success) {
                            str = "删除成功";
                        } else {
                            str = (res.info||"删除失败");
                        }
                        $.prompt("<div style='text-align:center;'>"+str+"</div>", {
                            title: "操作提示",
                            buttons: { "确定": true },
                            focus : 1,
                            submit : function (e, v) {
                                if (v) {
                                    window.location.reload();
                                }
                            },
                            useiframe:true
                        });
                    });
                }
            },
            useiframe:true
        });
    });

    function categoryDialog(category) {
        $.prompt(template("T:CATEGORY_MODEL", {cat:category}), {
            title: "机构分类编辑",
            buttons: {"确定": true, "取消":false},
            position: {width: 600},
            submit: function (e, v, m, f) {
                var cat = {
                    cid : $('#category_id').val(),
                    first : $('#category_1').val(),
                    second : $('#category_2').val()
                };
                if (v) {
                    if (cat.first == '') {
                        alert('请填写一级分类');
                        return false;
                    }
                    if (cat.second == '') {
                        alert('请填写二级分类');
                        return false;
                    }
                    $.post('/config/category/save.vpage', cat, function (res) {
                        var str = "保存成功";
                        if (res.success) {
                            str = "保存成功";
                        } else {
                            str = (res.info||"保存失败");
                        }
                        $.prompt("<div style='text-align:center;'>"+str+"</div>", {
                            title: "操作提示",
                            buttons: { "确定": true },
                            focus : 1,
                            submit : function (e, v) {
                                if (v) {
                                    window.location.reload();
                                }
                            },
                            useiframe:true
                        });
                    });
                }
            }
        });
    }
});