/*-----------书店管理相关-----------*/
define(["jquery","echarts", "prompt", "datetimepicker", "paginator", "jqform", "template"], function ($,echarts) {

    $(function(){
        //指定图标的配置和数据
        function setOption() {
            var xAxisData = [];
            var sData = [];
            if($("#chartData").text() == ''){return;}
            var data = $.parseJSON( $("#chartData").text() );
            var width = $("#chart").width();
            $("#chart").css("width",width+"px")
            for (x in data)
            {
                xAxisData.push(x);
                sData.push(data[x]);

            }
            var option = {
                color:['#27a9bf'],
                title:{
                    text:''
                },
                tooltip:{},
                legend:{
                    data:['用户来源']
                },
                xAxis:{
                    axisLine: {show:false},
                    axisTick: {show:false},
                    splitLine:{show:false},
                    data:xAxisData
                },
                yAxis:{
                    show:true,
                    axisLine: {show:false},
                    axisTick: {show:false},
                    splitLine:{show:false},
                    splitArea:{show:false},
                    axisLabel : {
                        formatter: function(){
                            return "";
                        }
                    }

                },
                series:[{
                    name:'订单数',
                    type:'bar',
                    data:sData,
                    label: {
                        normal: {
                            show: true,
                            position: 'top'
                        }
                    }
                }]
            };
            //初始化echarts实例
            var myChart = echarts.init(document.getElementById('chart'));
            myChart.setOption(option);

                window.onresize = function () {
                    myChart.resize();

                }

        }
        setOption();
        /*分页插件*/
        var paginator = $('#paginator');
        var pages = $(".two-page");
        var currentPage = 1;
        if(paginator.length>0){
            paginator.jqPaginator({
                totalPages: parseInt(paginator.attr("totalPage")||1),
                currentPage: parseInt(paginator.attr("pageIndex") || 1),
                first: '<li class="first"><a href="javascript:void(0);">首页<\/a><\/li>',
                prev: '<li class="prev"><a href="javascript:void(0);">上一页<\/a><\/li>',
                next: '<li class="next"><a href="javascript:void(0);">下一页<\/a><\/li>',
                last: '<li class="last"><a href="javascript:void(0);">尾页<\/a><\/li>',
                page: '<li class="page"><a href="javascript:void(0);">{{page}}<\/a><\/li>',
                onPageChange: function (pageIndex, opType) {

                    if (opType == 'change') {
                        $('#pageIndex').val(pageIndex);
                       // location.href = '/bookstore/manager/operation.vpage?a=-100';
                       location.href = '/bookstore/manager/operation.vpage?page='+pageIndex;
                    }

                }
            });
        }
    });
});