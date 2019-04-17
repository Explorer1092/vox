/**
 * 绘本阅读页面
 */



define(["jquery", "knockout", "$17", "prompt", "datetimepicker", "paginator"], function ($, ko, $17) {

    var queryTime = $('#startTime').val();
    var endTime = $('#endTime').val();

    var queryDate = new Date();
    /*时间控件*/
    $('#startTime').datetimepicker({
        format: 'yyyy-mm-dd',
        endDate: queryDate,
        // initialDate: new Date(queryDate.setDate(queryDate.getDate() - 1)),
        // initialDate: new Date(queryDate.setDate(queryDate.getDate())),
        minView: 3,
        autoclose: true
    }).on('changeDate', function (ev) {
        queryTime = $(this).val();
    });

    $('#endTime').datetimepicker({
        format: 'yyyy-mm-dd',
        endDate: queryDate,
        // initialDate: new Date(queryDate.setDate(queryDate.getDate() - 1)),
        // initialDate: new Date(queryDate.setDate(queryDate.getDate())),
        minView: 3,
        autoclose: true
    }).on('changeDate', function (ev) {
        endTime = $(this).val();
    });

    //查询
    $("#detail_search").on("click", function (e) {
        var pageIndex = $("#pageIndex").val();
        if (e.hasOwnProperty("originalEvent")) {
            pageIndex = 1;
        }

        var searchBookName = $("#book-name").val();
        location.href = '/picbook_ps/reading/stat.vpage?series=Longman+eReading&startDate=' + queryTime
            + '&endDate='+ endTime
            + '&pageIndex=' + pageIndex
            + '&bookName=' + searchBookName;

    });

    //分页插件
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
                    $('#detail_search').trigger("click");
                }
            }
        });
    }
});
