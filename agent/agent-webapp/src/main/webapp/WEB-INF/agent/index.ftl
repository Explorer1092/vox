<#import "layout_default.ftl" as layout_default>
<@layout_default.page page_title='Support System'>
    <div class="sortable row-fluid ui-sortable">
        <#if tasks??>
            <a data-rel="tooltip" class="well span3 top-block" href="/task/todolist/index.vpage" data-original-title="${tasks!0} 条待处理任务.">
                <span class="icon32 icon-color icon-envelope-closed"></span>
                <div>待处理任务</div>
                <#if tasks gt 0>
                    <span class="notification red">${tasks!0}</span>
                </#if>
            </a>
        </#if>
        <#if msgs??>
            <a data-rel="tooltip" class="well span3 top-block" href="/notify/info/index.vpage" data-original-title="${msgs!0} 条未读消息.">
                <span class="icon32 icon-color icon-envelope-closed"></span>
                <div>消息列表</div>
                <#if msgs gt 0>
                    <span class="notification red">${msgs}</span>
                </#if>
            </a>
        </#if>
        <#if workflowCount??>
            <a data-rel="tooltip" class="well span3 top-block" href="/workflow/todo/list.vpage" data-original-title="${workflowCount!0} 条待审核.">
                <span class="icon32 icon-color icon-envelope-closed"></span>
                <div>待审核（新版）</div>
                <#if workflowCount gt 0>
                    <span class="notification red">${workflowCount!0}</span>
                </#if>
            </a>
        </#if>
    </div>
    <div id="kpiMarker" class=""></div>



<script type="text/javascript">
    Date.prototype.Format = function (fmt) { //author: meizz
        var o = {
            "M+": this.getMonth() + 1, //月份
            "d+": this.getDate(), //日
            "h+": this.getHours(), //小时
            "m+": this.getMinutes(), //分
            "s+": this.getSeconds(), //秒
            "q+": Math.floor((this.getMonth() + 3) / 3), //季度
            "S": this.getMilliseconds() //毫秒
        };
        if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
        for (var k in o)
            if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
        return fmt;
    }
    $(function(){

        var plot = renderChart();

        var previousPoint = null;
        $("div[id^='chart_']").live("plothover", function (event, pos, item) {
            if (item) {
                if (previousPoint != item.dataIndex) {
                    previousPoint = item.dataIndex;

                    $("#tooltip").remove();
                    var x = new Date(parseInt(item.datapoint[0].toFixed(0))).Format("yyyy/MM/dd"),
                        y = item.datapoint[1].toFixed(0);

                    showTooltip(item.pageX, item.pageY,
                                    item.series.label + "<br> 时间:" + x + "  完成数量:" + y);
                }
            }
            else {
                $("#tooltip").remove();
                previousPoint = null;
            }
        });

        $("div[id^='chartSummary_']").live("plothover", function (event, pos, item) {
            if (item) {
                if (previousPoint != item.dataIndex) {
                    previousPoint = item.dataIndex;

                    $("#tooltip").remove();
                    var x = item.datapoint[0].toFixed(0),
                        y = item.datapoint[1].toFixed(0);
                    if (x == 0) {
                        showTooltip(item.pageX, item.pageY,
                                        "已完成:" + y);
                    }
                    else {
                        showTooltip(item.pageX, item.pageY,
                                        "目标:" + y);
                    }
                }
            }
            else {
                $("#tooltip").remove();
                previousPoint = null;
            }
        });

    });

    //KPI对象定义，为plot服务
    function KPI(data,label){
        this.data=data;
        this.label=label;
        return this;
    }

    //渲染图表
    function renderChart(){
        var data = [];
        $.getJSON("loadchartdata.vpage",function(data){
            if(data.success){
                data = data.value;
                var index = 0;
                for(var key in data){
                    execute(data[key],index);
                    index++;
                }
            }
        });
    }

    //生成某一个kpi的数据表
    function execute(innerData,index){
        var dailyCounts = [];//每天的数据
        var total = [];//每天的历史累加数据
        var kpiName;//绩效名称
        var kpiTarget;//绩效目标
        var totalCompleteCnt = 0;
        var maxValue = 0,minValue = 99999999;
        //后台数据统一调用的，js先倒排一下
        for(var i = innerData.length-1 ;i >= 0;i--){
            if($.isNumeric(innerData[i].completeCnt)){
                if(maxValue < parseInt(innerData[i].date)){
                    maxValue = parseInt(innerData[i].date);
                }
                if(minValue > parseInt(innerData[i].date)){
                    minValue = parseInt(innerData[i].date);
                }
                totalCompleteCnt += innerData[i].completeCnt;
                dailyCounts.push([changeToDate(innerData[i].date.toString()), innerData[i].completeCnt]);
                total.push([changeToDate(innerData[i].date.toString()), totalCompleteCnt]);
                kpiName = innerData[i].kpiName;
                if (innerData[i].regionName != '') {
                    kpiName = kpiName + "(" + innerData[i].regionName + ")";
                }
                kpiTarget = innerData[i].kpiTarget;
            }
        }

        var kpiDiv = "<div class=\"sortable row-fluid ui-sortable\" id=\"kpiData_"+index+"\"></div>";
        $('#kpiMarker').after(kpiDiv);
        generatePlotBar(kpiTarget,totalCompleteCnt,index);
        generatePlot(dailyCounts,total,kpiName,index);
    }

    //生成每天数据图
    function generatePlot(counts,total,kpiName,index){
        var plots = [];//线状图

        var kpi = new KPI(counts,"(按天)");
        plots.push(kpi);

        var totalKpi = new KPI(total,"(合计)");
        plots.push(totalKpi);

        var plotStr = "<div class=\"box span8\">";
        plotStr += "<div class=\"box-header well\">";
        plotStr += "<h2><i class=\"icon-list-alt\"></i> "+kpiName+"业绩数据</h2>";
        plotStr += "<div class=\"box-icon\">";
        plotStr += "<a href=\"#\" class=\"btn btn-minimize btn-round\"><i class=\"icon-chevron-up\"></i></a>";
        plotStr += "<a href=\"#\" class=\"btn btn-close btn-round\"><i class=\"icon-remove\"></i></a>";
        plotStr += "</div>";
        plotStr += "</div>";
        plotStr += "<div class=\"box-content\">";
        plotStr += "<div id=\"chart_"+index+"\" class=\"center\" style=\"height:300px\"></div>";
        plotStr += "</div>";
        plotStr += "</div>";


        $('#kpiData_'+index).append(plotStr);
        $.plot($("#chart_"+index),
                plots, {
                    series: {
                        lines:  { show: true },
                        points: { show: true }
                    },
                    grid: { hoverable: true, clickable: true, backgroundColor: { colors: ["#fff", "#eee"] } },
                    xaxis: { mode:"time",
                        tickFormatter: function (val,axis) {
                                var d = new Date(parseInt(val));
                                return d.Format("yyyy/MM/dd");
                            }
                        },
                    colors: ["#539F2E", "#3C67A5"]
                }
        );
    }

    //生成总计图
    function generatePlotBar(kpiTarget,kpiTotal,index){
        var plotsBar = [];//柱状图
        var kpiArray = [];
        kpiArray.push([0,kpiTotal]);
        kpiArray.push([1,kpiTarget]);

        var plotBarStr = "<div class=\"box span3\">";
        plotBarStr += "<div class=\"box-header well\">";
        plotBarStr += "<h2><i class=\"icon-list-alt\"></i> 业绩数据总计</h2>";
        plotBarStr += "<div class=\"box-icon\">";
//        plotBarStr += "<a href=\"#\" class=\"btn btn-minimize btn-round\"><i class=\"icon-chevron-up\"></i></a>";
//        plotBarStr += "<a href=\"#\" class=\"btn btn-close btn-round\"><i class=\"icon-remove\"></i></a>";
        plotBarStr += "</div>";
        plotBarStr += "</div>";
        plotBarStr += "<div class=\"box-content\">";
        plotBarStr += "<div id=\"chartSummary_"+index+"\" class=\"center\" style=\"height:300px;width: 200px\"></div>";
        plotBarStr += "</div>";
        plotBarStr += "</div>";

        $('#kpiData_'+index).append(plotBarStr);
        $.plot($("#chartSummary_"+index),
            [{data:kpiArray}],
                {
                series: {
                    bars: { show: true }
                },
                grid: { hoverable: true, clickable: true, backgroundColor: { colors: ["#fff", "#eee"] } },
                yaxis: { labelWidth:10,position:"left",tickLength:30},
                xaxis: { min: 0, max: 2,tickDecimals:0 },
                colors: ["#539F2E", "#3C67A5"]
            }
        );
    }

    //展示鼠标滑过的tip
    function showTooltip(x, y, contents) {
        $('<div id="tooltip">' + contents + '</div>').css( {
            position: 'absolute',
            display: 'none',
            top: y + 5,
            left: x + 5,
            border: '1px solid #fdd',
            padding: '2px',
            'background-color': '#dfeffc',
            opacity: 0.80
        }).appendTo("body").fadeIn(200);
    }

    //把20140731格式的数据转化成date
    function changeToDate(str){
        if(typeof str === 'string'){
            var result = new Date(str.replace(/^(\d{4})(\d{2})(\d{2})$/,"$1/$2/$3"));
            return result.getTime();
        }
        return null;
    }


</script>
</@layout_default.page>
