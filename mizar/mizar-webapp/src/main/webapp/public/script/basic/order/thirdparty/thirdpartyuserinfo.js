/**
 * 第三方订单统计页面
 */



define(["jquery", "knockout", "$17", "prompt", "datetimepicker", "paginator"], function ($, ko, $17) {

    var queryTime = $('#queryTime');
    var downloadTime = '';
    var queryDate = new Date();
    var publisher_flag = $('#publisher_flag').val();
    /*时间控件*/
    queryTime.datetimepicker({
        format: 'yyyy-mm-dd',
        endDate: queryDate,
        initialDate: queryDate,
        // initialDate: new Date(queryDate.setDate(queryDate.getDate())),
        minView: 3,
        autoclose: true
    }).on('changeDate', function (ev) {
        queryTime.val($(this).val());
    });

    //查询
    $("#detail_search").on("click", function (e) {
        var pageIndex = $("#pageIndex").val();
        if (e.hasOwnProperty("originalEvent")) {
            pageIndex = 1;
        }
        queryTime = $('#queryTime').val();
        location.href = '/thirdParty/userInfo/infoList.vpage?pageIndex=' + pageIndex;

    });
    //下载
    $("#download_data").on("click", function () {
        location.href = '/thirdParty/userInfo/downloadData.vpage';
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
