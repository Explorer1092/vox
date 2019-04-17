define(["common"], function () {
    var dataList;
    var dataName;
    var data;

    $(document).on('click','#schoolDetail',function(){
        openSecond("/mobile/school_clue/schooldetail.vpage?schoolId="+schoolId);
    });

    getOffLineData()
    function getOffLineData() {
        $.get('school_klx_chart.vpage', {schoolId: schoolId}, function (res) {
            if (res.success) {
                var arr = [];
                var xAxis = res.data.xAxis;
                var legend = res.data.legend;
                var data = res.data[legend];
                var myChart = echarts.init($('.container')[0]);

                for(var i=0;i < xAxis.length;i++){
                    arr.push(xAxis[i].toString().substring(4).replace(/\b(0+)/gi,"")+"月");
                }
                var option = {
                    tooltip: {
                        trigger: 'axis',
                        axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                            type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                        }
                    },
                    title: {
                        show: false
                    },
                    legend: {
                        data: legend,
                        x: 'center',
                        y: 'bottom',
                        show: true,
                        itemWidth: 12,
                        itemHeight: 12
                    },
                    grid: {
                        y: 35,
                        x: 45
                    },
                    xAxis: [
                        {
                            type: 'category',
                            data: arr
                        }
                    ],
                    yAxis: [
                        {
                            type: 'value',
                            splitNumber: 4
                        }
                    ],
                    series: [
                        {
                            name: legend,
                            type: 'bar',
                            barWidth: 5,
                            data: data,
                            itemStyle: {
                                normal: {color: '#ff7d5a'}
                            }
                        }
                    ]
                };
                myChart.setOption(option);
                $('#container').show();
            }
        });
    }

    $(".tab_school").on("click", function () {
        var DataIndex = $(this).index();
        $(this).addClass("_active").siblings().removeClass("_active");
        $('.teacher_detail').eq(DataIndex).show().siblings().hide();
        $('.teacher_detail').eq(DataIndex).find(".school_data").eq(0).click();
    });


    $.get("schooldynamics.vpage", {schoolId: schoolId}, function (res) {
        if (res.success) {
            $('.school_information').html(template("teacher_information", {res: res}));
            if($('.tip_num').html() > 99){
                $('.tip_num').html("99+")
            }
        }
    });

    $.get('school_klx_performance.vpage', {schoolId: schoolId}, function (res) {
        if (res.success) {
            $('.scanData').html(template("scanData", {res: res}));
        }
    });

    $(document).ready(function () {
        YQ.voxLogs({
            database : "marketing", //不设置database  , 默认库web_student_logs
            module : "m_oI6bREkw", //打点流程模块名
            op : "o_M4U6Tdk8" ,//打点事件名
            userId:userId,
            s0 : schoolId
        });
    })
});
