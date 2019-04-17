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

    /*分页插件*/
    var paginator = $('#paginator');
    var pages = $(".one-page");
    var currentPage = 1;
    if(paginator.length>0){
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
                pages.eq(num-1).addClass("displayed").siblings().removeClass("displayed");
                currentPage = num;
            }
        });
    }
});