
define(["jquery", "knockout", "$17", "prompt", "datetimepicker", "paginator"], function ($, ko, $17) {

    var startTime = $('#startTime'), endTime = $('#endTime');
    var series = $('#series').val();

    var startDate = new Date("2017-02-08");
    var endDate = new Date();
    /*时间控件*/
    startTime.datetimepicker({
        format: 'yyyy-mm-dd',
        startDate: startDate,
        endDate: endDate,
        minView: 3,
        autoclose: true
    }).on('changeDate', function (ev) {
        startTime = $(this).val();

    });

    endTime.datetimepicker({
        format: 'yyyy-mm-dd',
        startDate: startDate,
        endDate: endDate,
        // endDate: new Date(endDate.setDate(endDate.getDate() + 1)),
        minView: 3,
        autoclose: true
    }).on('changeDate', function (ev) {
        endTime = $(this).val();
    });

    //查询
    $("#ordercount_search").on("click", function () {
        startTime = $('#startTime').val();
        endTime = $('#endTime').val();
        location.href = '/picbook/'+ series +'/order/stat.vpage?startDate=' + startTime + "&endDate=" + endTime + "&series=Longman+eReading";
    });

});
