/*-----------书店管理相关-----------*/
define(["jquery", "prompt", "datetimepicker", "paginator", "jqform", "template"], function ($) {
    function getQuery(item) {
        var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
        return svalue ? decodeURIComponent(decodeURIComponent(svalue[1])) : '';
    }

    $(function () {

        var name = getQuery("name");
        var bookStoreId = getQuery("bookStoreId");
        $("#storeName").text(getQuery("name"));
        var paginator = $('#paginator');
        if (paginator.length > 0) {
            paginator.jqPaginator({
                totalPages: parseInt(paginator.attr("totalPages")),
                visiblePages: 5,
                currentPage: parseInt(paginator.attr("pageIndex") || 1),
                first: '<li class="first"><a href="javascript:void(0);">首页<\/a><\/li>',
                prev: '<li class="prev"><a href="javascript:void(0);">上一页<\/a><\/li>',
                next: '<li class="next"><a href="javascript:void(0);">下一页<\/a><\/li>',
                last: '<li class="last"><a href="javascript:void(0);">尾页<\/a><\/li>',
                page: '<li class="page"><a href="javascript:void(0);">{{page}}<\/a><\/li>',
                onPageChange: function (pageIndex, opType) {
                    if (opType == 'change') {
                        $('#pageIndex').val(pageIndex);
                        var url = window.location.href;
                        location.href = '/bookstore/manager/bookStoreOrderInfo.vpage?bookStoreId=' + bookStoreId
                           +"&name=" +name
                            + '&page=' + pageIndex;

                    }
                }
            });
        }
    });
})
