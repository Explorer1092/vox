/**
 * Created by free on 2016/12/13.
 */
define(["jquery","prompt","paginator","jqform","datetimepicker"],function ($) {

    //初始化开始结束时间控件
    var initTimePlugin = function(start,end){
        $("#"+start).datetimepicker({
            language:  'zh-CN',
            format: 'yyyy-mm-dd hh:ii:ss',
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false,
            // minView:3,
            autoclose:true
        });

        $("#"+end).datetimepicker({
            language:  'zh-CN',
            format: 'yyyy-mm-dd hh:ii:ss',
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false,
            // minView:3,
            autoclose:true
        })
    };

    initTimePlugin('start','end');

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

    $('#searchBtn').on('click', function () {
        $('#pagerForm').submit();
    });

    $('#exportBtn').on('click', function() {
       var link =  '/course/manage/exportpage.vpage?'
           + 'page=' + $('#pageNum').val()
           + '&id=' + $('#pid').val()
           + '&start=' + $('#start').val()
           + '&end='+ $('#end').val()
           + '&live=' + $('#live').val();

        var aTag = document.createElement('a');
        aTag.setAttribute("href", link);
        aTag.click();
    });

});