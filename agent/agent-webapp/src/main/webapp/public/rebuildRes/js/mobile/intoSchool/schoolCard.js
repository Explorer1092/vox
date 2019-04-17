define(["common"], function () {
    var dataList;
    var dataName;
    var data;
    $(".school_data").eq(0).addClass("active");
    $(document).on('click','#schoolDetail',function(){
        openSecond("/mobile/school_clue/schooldetail.vpage?schoolId="+schoolId);
    });
    var dataBoolean = true ;
    $(document).on("click",".school_data",function(){
        dataName = $(this).data("name");
        var index = $(this).data("index");
        $(this).addClass("active").siblings().removeClass("active");
        var _thisSub = $(this).parent().siblings(".container")[0];
        if(dataBoolean && needOldEcharts){
            rendermyChart(_thisSub,index);
        }
    });
    $(document).on("click","#vacChartDimension li",function(){
        $(this).addClass("active").siblings().removeClass("active");
    });

    function rendermyChart(_thisSub,index){
        var myChart = echarts.init(_thisSub);
        var arr = [],dataNameValue = [];
        if(index == 1){
            dataList = data.scanDataMap
        }else{
            dataList = data.mauDataMap
        }
        for(var i=0;i < Object.keys(dataList[dataName]).length;i++){
            arr.push(Object.keys(dataList[dataName])[i].toString().substring(4).replace(/\b(0+)/gi,"")+"月");
        }
        for (var j in dataList[dataName]){
            dataNameValue.push(dataList[dataName][j])
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
                data: dataName,
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
                    name: dataName,
                    type: 'bar',
                    barWidth: 5,
                    data: dataNameValue,
                    itemStyle: {
                        normal: {color: '#ff7d5a'}
                    }
                }
            ]
        };
        myChart.setOption(option);
        $('#container').show();
    }
    $.get("schooldynamics.vpage", {schoolId: schoolId}, function (res) {
        if (res.success) {
            $('.school_information').html(template("teacher_information", {res: res}));
            if($('.tip_num').html() > 99){
                $('.tip_num').html("99+")
            }
        }
    });
    $(".tab_school").on("click", function () {
        var DataIndex = $(this).index();
        $(this).addClass("_active").siblings().removeClass("_active");
        $('.teacher_detail').eq(DataIndex).show().siblings().hide();
        $('.teacher_detail').eq(DataIndex).find(".school_data").eq(0).click();
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
