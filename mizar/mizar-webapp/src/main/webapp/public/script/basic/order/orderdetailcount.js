/**
 * 人教点读机订单统计页面
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
        initialDate: new Date(queryDate.setDate(queryDate.getDate() - 1)),
        // initialDate: new Date(queryDate.setDate(queryDate.getDate())),
        minView: 3,
        autoclose: true
    }).on('changeDate', function (ev) {
        queryTime = $(this).val();
    });

    //查询
    $("#detail_search").on("click", function (e) {
        var pageIndex = $("#pageIndex").val();
        if (e.hasOwnProperty("originalEvent")) {
            pageIndex = 1;
        }
        queryTime = $('#queryTime').val();
        if (publisher_flag == 'REN_JIAO') {
            location.href = '/order/picorder/detailcount.vpage?queryDate=' + queryTime + "&pageIndex=" + pageIndex + "&publisher_flag=" + publisher_flag;
        } else if (publisher_flag == 'SHAN_DONG') {
            location.href = '/order/sk_picorder/sk_detailcount.vpage?queryDate=' + queryTime + "&pageIndex=" + pageIndex + "&publisher_flag=" + publisher_flag;
        } else if (publisher_flag == 'SHANG_HAI') {
            location.href = '/order/sh_picorder/sh_detailcount.vpage?queryDate=' + queryTime + "&pageIndex=" + pageIndex + "&publisher_flag=" + publisher_flag;
        } else if (publisher_flag == 'LIAO_NING') {
            location.href = '/order/ln_picorder/ln_detailcount.vpage?queryDate=' + queryTime + "&pageIndex=" + pageIndex + "&publisher_flag=" + publisher_flag;
        } else if (publisher_flag == 'YI_LIN') {
            location.href = '/order/yl_picorder/yl_detailcount.vpage?queryDate=' + queryTime + "&pageIndex=" + pageIndex;
        }

    });

    //下载
    $("#download_data").on("click", function () {
        downloadTime = $('#queryTime').val();
        if (!$.trim(downloadTime)) {
            alert("请选择日期");
            return false;
        }
        if (publisher_flag == 'REN_JIAO') {
            location.href = '/order/picorder/downloadOrderDetail.vpage?queryDate=' + downloadTime + "&publisher_flag=" + publisher_flag;
        } else if (publisher_flag == 'SHAN_DONG') {
            location.href = '/order/sk_picorder/downloadOrderDetail.vpage?queryDate=' + downloadTime + "&publisher_flag=" + publisher_flag;
        } else if (publisher_flag == 'SHANG_HAI') {
            location.href = '/order/sh_picorder/downloadOrderDetail.vpage?queryDate=' + downloadTime + "&publishName=沪教版";
        } else if (publisher_flag == 'LIAO_NING') {
            location.href = '/order/ln_picorder/downloadOrderDetail.vpage?queryDate=' + downloadTime + "&publisher_flag=" + publisher_flag;
        } else if (publisher_flag == 'YI_LIN') {
            location.href = '/order/yl_picorder/downloadOrderDetail.vpage?queryDate=' + downloadTime + "&publishName=译林版";
        }

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
