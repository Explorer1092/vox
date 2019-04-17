/**
 * 团队——全国
 * */
define(["dispatchEvent","handlebars","echarts"], function (dispatchEvent,handlebars,echarts) {
    $(document).ready(function(){
        var AT = new agentTool();

        //渲染页面
        var renderTemp = function(tempSelector,data,container){
            var source   = $(tempSelector).html();
            var template = handlebars.compile(source);

            $(container).html(template(data));
        };

        // 指定图表的配置项和数据
        var option = {
            series: [
                {
                    name:'小学单活',
                    type:'pie',
                    radius: ['50%', '70%'],
                    avoidLabelOverlap: false,
                    label: {
                        normal: {
                            show: true,
                            position: 'center'
                        }
                    },
                    labelLine: {
                        normal: {
                            show: false
                        }
                    },
                    data:[
                    ]
                }
            ]
        };

        var renderPip = function(first,second,third){
            var innerList = [first,second,third];
            var colorList = ["#ff8e56","#bccb5b","#88b2fd"];
            $.each(innerList,function(i,item){
                var num = item;
                if(num > 100){
                    option.series[0].data = [
                        {value:num, name:num+'%',
                            itemStyle:{
                                normal:{color:colorList[i]}
                            }
                        }
                    ];
                }else{
                    option.series[0].data = [
                        {value:num, name:num+'%',
                            itemStyle:{
                                normal:{color:colorList[i]}
                            }
                        },
                        {
                            value:100-parseInt(num), name:'',
                            itemStyle:{
                                normal:{color:'lightgrey'}
                            }
                        }
                    ];
                }

                echarts.init(document.getElementById("js-chart"+i)).setOption(option);
            });

            $("#loadingDiv").hide();
        };



        //注册事件
        var eventOption = {
            base:[
                {
                    //团队成员
                    selector:".js-teamMb",
                    eventType:"click",
                    callBack:function(){
                        var uid = $(this).data("uid");
                        $(this).addClass("the").siblings("a").removeClass("the");
                        groupId = uid;

                        $("#loadingDiv").show();
                        $.post("get_region_performance_report.vpage",{groupId:uid},function(res){
                            if(res.success){
                                res.aveIntoSchoolCount = res.aveIntoSchoolCount.toFixed(1);
                                if(res.monthIntoSchoolCount != 0){
                                    res.planRate = parseInt(parseFloat(res.inPlanCount/res.monthIntoSchoolCount)*100+0.5);
                                }else{
                                    res.planRate = 0;
                                }
                                res.totalNum = res.visitResultData.visitedUnusedCount + res.visitResultData.unvisitedUnusedCount + res.visitResultData.visitedUsedCount + res.visitResultData.unvisitedUsedCount;
                                if(res.totalNum != 0){
                                    res.vuu = 100*(res.visitResultData.visitedUnusedCount/res.totalNum);
                                    res.uu = 100*(res.visitResultData.unvisitedUnusedCount/res.totalNum);
                                    res.vu = 100*(res.visitResultData.visitedUsedCount/res.totalNum);
                                    res.uvu = 100*(res.visitResultData.unvisitedUsedCount/res.totalNum);
                                }else{
                                    res.vuu = res.uu = res.vu = res.uvu = 0;
                                }
                                renderTemp("#teamMembDetailTemp",res,"#teamDetailContainer");
                                renderPip(res.juniorSascCompleteRate,res.juniorDascCompleteRate,res.middleSascCompleteRate);
                                $("#loadingDiv").hide();
                            }else{
                                AT.alert(res.info);
                                $("#loadingDiv").hide();
                            }
                        });
                    }
                }
            ]
        };

        new dispatchEvent(eventOption);

        $(".js-teamMb").first().click();
    });
});