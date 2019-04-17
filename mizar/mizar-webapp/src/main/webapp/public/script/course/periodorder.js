/**
 * Created by free on 2016/12/13.
 */
define(["jquery","prompt","paginator"],function ($) {

    // 分页插件
    var paginator = $('#paginator');
    if (paginator.length > 0) {
        paginator.jqPaginator({
            totalPages:parseInt(paginator.attr("totalPage")),
            visiblePages: 10,
            currentPage: parseInt(paginator.attr("pageIndex")||1),
            first: '<li class="first"><a href="javascript:void(0);">首页<\/a><\/li>',
            prev: '<li class="prev"><a href="javascript:void(0);">上一页<\/a><\/li>',
            next: '<li class="next"><a href="javascript:void(0);">下一页<\/a><\/li>',
            last: '<li class="last"><a href="javascript:void(0);">尾页<\/a><\/li>',
            page: '<li class="page"><a href="javascript:void(0);">{{page}}<\/a><\/li>',
            onPageChange: function (pageIndex,opType) {
                if(opType=='change'){
                    $('#pageNum').val(pageIndex);
                    $('#pagerForm').submit();
                }
            }
        });
    }

});