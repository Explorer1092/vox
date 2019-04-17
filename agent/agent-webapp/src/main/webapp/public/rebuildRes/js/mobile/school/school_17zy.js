$(document).ready(function () {
    template.helper('Math', Math);
    bindSchool17Performance(schoolId);
    bindSchool17SubjectActive(schoolId);
});


function bindSchool17Performance(schoolId) {
    $.get('/mobile/resource/school/school_17_performance.vpage', {schoolId:schoolId}, function(res) {
        $(".base_kpi_data").html(template("base_kpi_data", {res: res.school17Performance}));
        $('.subject_data').html(template("subject_data", {res: res.school17Performance}));
        $('.wait_trans_data').html(template("wait_trans_data", {res: res.school17Performance}));
    });
}
var dateList = [];
var dateMonthList = [];
var mathDateList = [];
var mathMonthDateList = [];
var mathMonthRtRate = [];
var engDateList = [];
var engMonthRtRate = [];
var engMonthDateList = [];
$(document).on("click",".showTipsButton",function () {
   $(".showTips").show();
});
$(document).on("click",".showTips",function () {
    $(this).hide();
});
function bindSchool17SubjectActive(schoolId) {
    $.get('/mobile/resource/school/school_subject_active.vpage', {schoolId:schoolId}, function(res) {
        var weekMap = res.data["week"];
        var monthMap = res.data["month"];
        for(var item in weekMap){
                if(weekMap[item].subjectName == "数学"){
                    for(var j = 0; j < weekMap[item].aus.length; j++){
                        dateList.push(weekMap[item].aus[j].showName.replace(/---/g,"-"));
                        mathDateList.push(weekMap[item].aus[j].activeCount);
                    }
                }else if(weekMap[item].subjectName == "英语") {
                    for (var k = 0; k < weekMap[item].aus.length; k++) {
                        engDateList.push(weekMap[item].aus[k].activeCount);
                    }
                }
        }
        for(var item in monthMap){
            if(monthMap[item].subjectName == "数学"){
                for(var j = 0; j < monthMap[item].aus.length; j++){
                    dateMonthList.push(monthMap[item].aus[j].showName.replace(/---/g,"-"));
                    mathMonthDateList.push(monthMap[item].aus[j].activeCount);
                    mathMonthRtRate.push(Math.floor(monthMap[item].aus[j].rtRate * 100));
                }
            }else if(monthMap[item].subjectName == "英语") {
                for (var k = 0; k < monthMap[item].aus.length; k++) {
                    engMonthDateList.push(monthMap[item].aus[k].activeCount);
                    engMonthRtRate.push(Math.floor(monthMap[item].aus[k].rtRate * 100));
                }
            }
        }
        $('.month_data').eq(0).click();
    });
}
$(document).on("click",".month_data",function () {
    $(this).addClass("active").siblings().removeClass("active");
    if($(this).data("index") == "1"){
        if(schoolMode == "17zy"){
            showCharts1("container1",["英语周活","数学周活"],dateList,engDateList,mathDateList);
        }else if(schoolMode == "online"){
            showCharts1("container1",["英语周活"],dateList,engDateList);
        }
    }else if($(this).data("index") == "2"){
        if(schoolMode == "17zy") {
            showCharts2("container1", ["英语月活", "英语次月留存", "数学月活", "数学次月留存"], dateMonthList, engMonthDateList,engMonthRtRate, mathMonthDateList, mathMonthRtRate);
        }else if(schoolMode == "online"){
            showCharts2("container1", ["英语月活", "英语次月留存"], dateMonthList, engMonthDateList, engMonthRtRate);
        }
    }
});
function showCharts1(domId,data,dateList,engDateList,mathDateList){
    var myChart = echarts.init(document.getElementById(domId));
    var option = {
        tooltip : {
            trigger: 'axis',
            axisPointer : {            // 坐标轴指示器，坐标轴触发有效
                type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
            }
        },
        legend: {
            data: data
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis : [
            {
                type : 'category',
                data : dateList,
                axisTick: {
                    alignWithLabel: true
                },
                axisLabel:{
                    interval:1
                }
            }
        ],
        yAxis : [
            {
                type : 'value',
                minInterval:1
            }
        ],
        series : [
            {
                name:data[0],
                type:'bar',
                data:engDateList,
                barWidth:5
            },
            {
                name:data[1],
                type:'bar',
                data:mathDateList,
                barWidth:5
            }
        ]
    };
    myChart.setOption(option);
}
function showCharts2(domId,data,dateList,engDateList,engMonthRtRate,mathDateList,mathMonthRtRate){
    var myChart = echarts.init(document.getElementById(domId));
    var option = {
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'shadow',
                crossStyle: {
                    color: '#999'
                }
            }
        },
        legend: {
            data: data
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis : [
            {
                type : 'category',
                data : dateList,
                axisTick: {
                    alignWithLabel: true
                },
                axisLabel:{
                    interval:1
                },
                axisPointer: {
                    type: 'shadow'
                }
            }
        ],
        yAxis : [
            {
                type : 'value',
                minInterval:1
            },
            {
                type:'value',
                axisLabel: {
                    formatter: '{value}%'
                },
                minInterval:1
            }
        ],
        series : [
            {
                name:data[0],
                type:'bar',
                data:engDateList,
                barWidth:5
            },
            {
                name:data[2],
                type:'bar',
                data:mathDateList,
                barWidth:5
            },
            {
                name:data[1],
                type:'line',
                data:engMonthRtRate,
                yAxisIndex: 1
            },
            {
                name:data[3],
                type:'line',
                data:mathMonthRtRate,
                yAxisIndex: 1
            }
        ]
    };
    myChart.setOption(option);
}
