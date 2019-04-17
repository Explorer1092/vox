/* global define : true, PM : true, $:true */
/**
 *  @date 2015/10/29
 *  @auto liluwei
 *  @description 该模块主要负责学习轨迹
 */

define(['ajax', 'template', 'chart/Chart.Core', 'chart/Chart.PolarArea'], function(promise, template, Chart){

    'use strict';

    var version=(PM.client_params&&PM.client_params.app_version)||PM.app_version||'0.0';
    if(version>='1.6'){
        $("#J-do-title").remove();
    }

    var chartCommonSetting = {
            showTooltips: false,
            pointDot : false
        },
        extendPolarAreaData = function(polarAreaData, color, type ){
            return {
                color : color,
                value : polarAreaData[type]
            };
        };


    var drawPolarArea = function($canvas, polarAreaData){
            var polarData = [
                extendPolarAreaData(polarAreaData, "#5875d1", "target"),
                extendPolarAreaData(polarAreaData, "#25cee3", "source"),
                extendPolarAreaData(polarAreaData, "#5ee664", "result")
            ];

            new Chart(
                $canvas[0].getContext('2d')
            ).PolarArea(polarData, chartCommonSetting);
        };

    var drawAction = {
        PolarArea : drawPolarArea
    };

    var drawChartByData = function(chartData, method){

        if( !(method in drawAction) ){
            return ;
        }

        var doDrawAction = drawAction[method];

        $.each((chartData || []), function(name, chartInfo){
            var $canvas = $('.' + method + name);

            if($canvas.length === 0){
                return ;
            }

            doDrawAction($canvas, chartInfo);

        });
    };

    $("#mockBody").waitSomething(
        "bodyIsShow",
        function(){
            return $("#mockBody").is(":visible");
        },
        function(){
            // 绘制饼图
            drawChartByData(PM.PolarAreaChart, "PolarArea");
        }
    );

});

/*  TODO  v1.3.2 暂且隐藏折线图 如折线图需要打开，则以下代码直接替换上面的代码即可
define(['ajax', 'chart/Chart.Core', 'chart/Chart.PolarArea', 'chart/Chart.Line'], function(promise, Chart){

    'use strict';

    var chartCommonSetting = {
            showTooltips: false,
            pointDot : false
        },
        extendPolarAreaData = function(polarAreaData, color, type ){
            return {
                color : color,
                value : polarAreaData[type]
            };
        },
        lineCommon = {
        },
        extendLineData = function(data, fillColor, strokeColor){
            return $.extend(
                {},
                lineCommon,
                {
                    fillColor : fillColor,
                    strokeColor : strokeColor,
                    data : data
                }
            );
        };


    var drawPolarArea = function($canvas, polarAreaData){
            var polarData = [
                extendPolarAreaData(polarAreaData, "#5875d1", "target"),
                extendPolarAreaData(polarAreaData, "#25cee3", "source"),
                extendPolarAreaData(polarAreaData, "#5ee664", "result")
            ];

            new Chart(
                $canvas[0].getContext('2d')
            ).PolarArea(polarData, chartCommonSetting);
        },
        drawLine = function($canvas, lineData){

            var lineChartData = {
                labels : lineData.range,
                datasets : [
                    extendLineData(lineData.clazz, "rgba(220,220,220,0.2)", "#5875D1"),
                    extendLineData(lineData.student, "rgba(151,187,205,0.2)", "#5EE664")
                ]
            };

            new Chart(
                $canvas[0].getContext('2d')
            ).Line(lineChartData, chartCommonSetting);

        };

    var drawAction = {
        PolarArea : drawPolarArea,
        Line : drawLine
    };

    var drawChartByData = function(chartData, method){

            if( !(method in drawAction) ){
                return ;
            }

            var doDrawAction = drawAction[method];

            $.each(chartData, function(name, chartInfo){
                var $canvas = $('.' + method + name);

                if($canvas.length === 0){
                    return ;
                }

                doDrawAction($canvas, chartInfo);

            });
    };

    $("#mockBody").waitSomething(
        "bodyIsShow",
        function(){
            return $("#mockBody").is(":visible");
        },
        function(){
            // 绘制饼图
            drawChartByData(PM.PolarAreaChart, "PolarArea");

            var buildLineData = function(buildData, source, type){

                if(buildData.length === 0){
                    return ;
                }

                var lineData = {
                    range : [],
                    student : [],
                    clazz : []
                };

                $.each(buildData, function(index, englishLineInfo){

                    lineData.range.push(
                        ++index % 7 === 0 ?
                        PM.formatDate("dd/MM", new Date(englishLineInfo.homeworkCreatetime)) :
                        ""
                    );

                    lineData.student.push(englishLineInfo.score);
                    lineData.clazz.push(englishLineInfo.clazzAvgScore);
                });

                $(".Line" + type + "Block").show();
                source[type] = lineData;

            };

            promise(
                    "/parentMobile/home/studyTrackHistory.vpage",
                    {
                        sid : PM.sid
                    }
                )
                .done(function(result){

                    if(!result.success){
                        //TODO 错误处理
                    }

                    var lineData = {},
                        mathInfo = result.maths || [],
                        englisInfo = result.englishs || [];

                    buildLineData(englisInfo, lineData, "English");
                    buildLineData(mathInfo, lineData, "Math");

                    drawChartByData(lineData, "Line");

                });

        }
    );
});
*/
