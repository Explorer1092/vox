/**sql group by
 * 人教点读机订单统计页面
 */



define(["jquery", "knockout", "$17", "prompt", "datetimepicker", "paginator"], function ($, ko, $17) {

    var startTime = $('#startTime'), endTime = $('#endTime'), publisher_flag = $('#publisher_flag').val();
    var queryDate = new Date();
    var startDate = new Date();
    startDate.setMonth(queryDate.getMonth() - 1);
    var endDate = new Date(queryDate.setDate(queryDate.getDate() - 1));
    // var endDate = new Date();
    // var endDate2 = new Date(queryDate.setDate(queryDate.getDate() + 1));
    /*时间控件*/
    startTime.datetimepicker({
        format: 'yyyy-mm-dd',
        startDate: startDate,
        endDate: endDate,
        initialDate: endDate,
        minView: 3,
        autoclose: true
    }).on('changeDate', function (ev) {
        startTime = $(this).val();

    });

    endTime.datetimepicker({
        format: 'yyyy-mm-dd',
        startDate: startDate,
        endDate: endDate,
        initialDate: endDate,
        // endDate: endDate2,
        minView: 3,
        autoclose: true
    }).on('changeDate', function (ev) {
        endTime = $(this).val();
    });

    //查询
    $("#ordercount_search").on("click", function () {
        startTime = $('#startTime').val();
        endTime = $('#endTime').val();
        if (publisher_flag == 'REN_JIAO') {
            location.href = '/order/picorder/count.vpage?startDate=' + startTime + "&endDate=" + endTime + "&publisher_flag=" + publisher_flag;
        } else if (publisher_flag == 'SHAN_DONG') {
            location.href = '/order/sk_picorder/sk_count.vpage?startDate=' + startTime + "&endDate=" + endTime + "&publisher_flag=" + publisher_flag;
        } else if (publisher_flag == 'SHANG_HAI') {
            location.href = '/order/sh_picorder/sh_count.vpage?startDate=' + startTime + "&endDate=" + endTime + "&publisher_flag=" + publisher_flag;
        } else if (publisher_flag == 'LIAO_NING') {
            location.href = '/order/ln_picorder/ln_count.vpage?startDate=' + startTime + "&endDate=" + endTime + "&publisher_flag=" + publisher_flag;
        } else if (publisher_flag == 'YI_LIN') {
            location.href = '/order/yl_picorder/yl_count.vpage?startDate=' + startTime + "&endDate=" + endTime;
        }

    });

});
