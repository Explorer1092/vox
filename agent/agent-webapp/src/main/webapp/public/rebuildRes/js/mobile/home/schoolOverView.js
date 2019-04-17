// /**
//  * 首页
//  * */

$(document).ready(function(){

    function lastDay_show(activeType){
        $('#month').hide();
        $('#lastDay').show();
        $("#lastDayTab_" + activeType).addClass("active").siblings('span').removeClass("active");
        $('#lastDay_ul_'+ activeType ).show().siblings().hide();
        // $('#container_'+ activeType ).show().siblings().hide();
        $('#lastDay_table_'+activeType).show().siblings().hide();
        $('.js-showHand').html('昨日');
    }
    function month_show(activeType){
        $('#month').show();
        $('#lastDay').hide();
        $("#monthTab_" + activeType).addClass("active").siblings('span').removeClass("active");
        $('#month_ul_'+ activeType ).show().siblings().hide();
        $('#month_table_'+activeType).show().siblings().hide();
        $('.js-showHand').html('本月');
    }
    $(document).on('click','.lastDayShow',function(){
        $(this).removeClass('lastDayShow');
        $('#thisMonth').addClass('thisMonthShow');
        $('#lastDay').show();
        $('#month').hide();
        $('#lastDayBtn').addClass('active');
        $('#thisMonth').removeClass('active');
        var _this = $('.js-monthTab.active').data('viewtype');
        lastDay_show(_this);
        maucOrDf = 2;
    });
    $(document).on('click','.thisMonthShow',function(){
        $(this).removeClass('thisMonthShow');
        $('#lastDayBtn').addClass('lastDayShow');
        var _this = $('.js-lastDayTab.active').data('viewtype');
        $('#month').show();
        $('#thisMonth').addClass('active');
        $('#lastDayBtn').removeClass('active');
        $('#lastDay').hide();
        month_show(_this);
        maucOrDf = 1;
    });
    if(maucOrDf == 1){
        month_show(activeType);
    }else if(maucOrDf == 2){
        lastDay_show(activeType);
        // column();
    }

});
$(document).on('click','table thead td.sortable',function(){
    var colIndex = $(this).index();
    var table = $(this).closest("table");
    sortTable(table, colIndex);
});
$(document).on('click','.js-showHand',function(){
    if($('.feedbackList-pop').hasClass('show_now')){
        $('.feedbackList-pop').removeClass('show_now').show();
        $(this).addClass('arrow_btn_up');
    }else{
        $('.feedbackList-pop').addClass('show_now').hide();
        $(this).removeClass('arrow_btn_up');
    }
});
$(document).on('click','.feedbackList-pop',function(){
    $(this).addClass('show_now').hide();
    $('.js-showHand').removeClass('arrow_btn_up');
});

$(document).on('click','.js-monthTab',function(){
    $(this).addClass('active').siblings().removeClass('active');
    var _this = $(this).data('viewtype');
    $('#month_ul_'+ _this).show().siblings().hide();
    $('#month_table_'+ _this).show().siblings().hide();
});

$(document).on('click','.sideTable tbody tr',function(){
    var id = $(this).data('id');
    location.href = "/mobile/resource/school/card.vpage?schoolId="+ id;
});

$(document).on('click','.js-lastDayTab',function(){
    $(this).addClass('active').siblings().removeClass('active');
    var _this = $(this).data('viewtype');
    $('#lastDay_ul_'+ _this).show().siblings().hide();
    $('#lastDay_table_'+ _this).show().siblings().hide();
    // $('#container_'+ _this ).show().siblings().hide();
});
$(document).on('click','.lastDayBtn',function(){
    $(this).removeClass('lastDayBtn');
    // column();
});
function column(){
    $.post('school_overview_df_chart.vpage',{id:id,idType:idType,viewType:activeType},function(res){
        if(res.success){
            for (var key in res.daysOverviewDfMap){
                var myChart = echarts.init(document.getElementById("container_"+ key));
                var option = {
                    tooltip : {
                        trigger: 'axis',
                        axisPointer : {            // 坐标轴指示器，坐标轴触发有效
                            type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                        }
                    },
                    title:{
                        show:false
                    },
                    legend: {
                        data:['新增认证','新增注册'],
                        x:'center',
                        y:'bottom',
                        show:true,
                        itemWidth:12,
                        itemHeight:12
                    },
                    grid:{
                        y:35,
                        x:45,
                        x2:35,
                        y2:50
                    },
                    xAxis : [
                        {
                            type : 'category',
                            data : res.days
                        }
                    ],
                    yAxis: [
                        {
                            type: 'value',
                            splitNumber : 4
                        }
                    ],
                    dataZoom: [
                        {
                            type: 'inside',
                            start:80,
                            end:100,
                            zoomLock:true
                        }
                    ],
                    series : [
                        {
                            name:'新增注册',
                            type:'bar',
                            barWidth: 10,
                            data:res.daysOverviewDfMap[key]['新增注册'],
                            itemStyle:{
                                normal:{color:'#ff7d5a'}
                            }
                        },
                        {
                            name:'新增认证',
                            type:'bar',
                            barWidth: 10,
                            data:res.daysOverviewDfMap[key]['新增认证'],
                            itemStyle:{
                                normal:{color:'#1fa2ff'}
                            }
                        }
                    ]
                };
                myChart.setOption(option);
            }
            $('#container').show();
        }else{
            alert(res.info)
        }
    });

}


