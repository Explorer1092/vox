/*-----------经营罗盘相关-----------*/
define(["jquery","prompt","datetimepicker","paginator","jqform"],function($){
    /*门店概况*/

    var startTime = $('#startTime'),
        endTime   = $('#endTime');
    /*时间控件*/
    startTime.datetimepicker({
        language:  'zh-CN',
        format: 'yyyy-mm-dd',
        monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate: new Date(),
        numberOfMonths: 1,
        changeMonth: false,
        changeYear: false,
        minView:3,
        autoclose:true
    });
    endTime.datetimepicker({
        language:  'zh-CN',
        format: 'yyyy-mm-dd',
        monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate: new Date(),
        numberOfMonths: 1,
        changeMonth: false,
        changeYear: false,
        minView:3,
        autoclose:true
    });
    $(".submit-search").on("click",function(){
        $("#filter-form").submit();
    });
});