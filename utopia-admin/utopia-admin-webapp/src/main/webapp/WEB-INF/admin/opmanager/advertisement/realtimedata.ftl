<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="广告实时数据查看" page_num=9>
<!-- chart libraries start -->
<script src="${requestContext.webAppContextPath}/public/js/highcharts.js"></script>
<#--<script src="${requestContext.webAppContextPath}/public/js/jquery.flot.min.js"></script>-->
<#--<script src="${requestContext.webAppContextPath}/public/js/jquery.flot.pie.min.js"></script>-->
<#--<script src="${requestContext.webAppContextPath}/public/js/jquery.flot.stack.js"></script>-->
<#--<script src="${requestContext.webAppContextPath}/public/js/jquery.flot.resize.min.js"></script>-->
<style>
    .inline table {
        width: 100%;
        margin-bottom: 10px;
    }

    .inline table td {
        padding: 0.5em 0;
        text-align: center;
    }

    .inline table td div, ul.inline table td input {
        display: inline-block;
    }

    .inline table td input {
        margin: 0 auto;
        width: 7em;
    }

    .inline table .info_td {
        text-align: left;
        width: 25%;
    }

    legend {
        margin-bottom: 0;
        border-color: #ccc;
    }

    .table td, .table th {
        padding: 8px;
        line-height: 20px;
        text-align: center;
        vertical-align: middle;
        border-top: 1px solid #dddddd;
    }

    .info_td {
        width: 7em;
        font-size: 14px;
    }

    .info_td_txt {
        width: 13em;
        font-weight: 600
    }
</style>
<div id="main_container" class="span9">
    <div>
        <#if error??>
            <div class="alert alert-error">
                <button type="button" class="close" data-dismiss="alert">×</button>
                <strong>${error!}</strong>
            </div>
        </#if>
    </div>
    <legend>
        <strong>实时数据</strong> &nbsp;&nbsp;&nbsp;&nbsp;
        <a href="dataindex.vpage?adId=${adId}">数据详情</a> &nbsp;&nbsp;&nbsp;&nbsp;
    </legend>
    <div class="inline">
        <table>
            <#if adDetail??>
                <tr>
                    <td class="info_td">广告ID：<span class="info_td_txt">${adDetail.id!'--'}</span></td>
                </tr>
                <tr>
                    <td class="info_td">广告名称：<span class="info_td_txt"><a
                            href="/opmanager/advertisement/addetail.vpage?adId=${adId}">${adDetail.name!'--'}</a></span>
                    </td>
                </tr>
                <tr>
                    <td class="info_td">广告编码：<span class="info_td_txt">${adDetail.adCode!'--'}</span></td>
                </tr>
            </#if>
            <#if adSlot??>
                <tr>
                    <td class="info_td">广告位ID：<span class="info_td_txt">${adSlot.id!'--'}</span></td>
                </tr>
                <tr>
                    <td class="info_td">广告位名称：<span class="info_td_txt">${adSlot.name!'--'}</span></td>
                </tr>
            </#if>
        </table>
    </div>
    <div>
        <button id="view_hour" class="btn">小 时</button>
        <button id="view_minute" class="btn">分 钟</button>
    </div>
    <br>
    <div id="adChart">
    </div>
    <form id="export_excel" action="downloadrealtimedata.vpage" method="post">
        <input name="adId" type="hidden" value="${adId!}">
        <input id="dateType" name="dateType" type="hidden" value="five_minute">
        <a href="javascript:void(0);" id="download_btn" class="btn btn-warning" style="margin: 10px 0; float: right;">导出Excel</a>
    </form>
    <table class="table table-hover table-striped table-bordered">
        <thead>
        <th style="width: 200px;">时间</th>
        <th style="width: 150px;">曝光量</th>
        <th style="width: 150px;">点击量</th>
        <th style="width: 200px;">独立用户曝光量</th>
        <th style="width: 200px;">独立用户点击量</th>
        </thead>
        <tbody id="adTable"></tbody>
    </table>
</div>
<script>
    var res;
    var timeoutId;
    $(function () {
        var title = "广告点击实时数据";
        $("#view_hour").click(function () {
            $(this).addClass("btn-primary").siblings().removeClass("btn-primary");
            $('#dateType').val("hour");
            reloadData("hour");
            startReloadDataTimer("hour");
        });
        $("#view_minute").click(function () {
            $(this).addClass("btn-primary").siblings().removeClass("btn-primary");
            $('#dateType').val("five_minute");
            reloadData("five_minute");
            startReloadDataTimer("five_minute");
        });
        //default load hour data
        $("#view_minute").addClass("btn-primary").siblings().removeClass("btn-primary");
        reloadData("five_minute");
        startReloadDataTimer("five_minute");

        $('#download_btn').on('click', function(){
            $('#export_excel').submit();
        });
    });
    function startReloadDataTimer(dataType) {
        if (timeoutId != null) {
            window.clearInterval(timeoutId);
        }
        timeoutId = window.setInterval("reloadData('" + dataType + "')", 60 * 1000);
    }
    function reloadData(dateType) {
        $.get("/opmanager/advertisement/config/realtimedatadetail.vpage?adId=${adDetail.id!'--'}", {dateType: dateType}, function (result) {
            if (result.success) {
                loadHighCharData("#adChart", result.highchartsResult);
                loadTableData("#adTable", result.highchartsResult);
            } else if (result.info != null) {
                alert(result.info);
            }else{
                alert("登陆失败，是否刷新重新登陆");
                location.reload();
            }
        }).fail(function () {
            alert("请求服务器异常");
        });

    }
    function loadHighCharData(node, highchartsData) {
        var baseInfo = highchartsData.baseInfo;
        $(node).highcharts({
            chart: {
                type: 'spline'
            },
            title: {
                text: baseInfo.title
            },
            subtitle: {
                text: baseInfo.subTitle
            },
            xAxis: {
                categories: highchartsData.xAxisList,
                crosshair:true
            },
            yAxis: [{ // Primary yAxis
                title: {
                    text: baseInfo.yAxisTitle,
                    style: {
                        color: Highcharts.getOptions().colors[1]
                    }
                }
            }, { // Secondary yAxis
                title: {
                    text: '点击率',
                    style: {
                        color: Highcharts.getOptions().colors[5]
                    }
                },
                labels: {
                    format: '{value} %',
                    style: {
                        color: Highcharts.getOptions().colors[5]
                    }
                },
                opposite: true
            }],
            tooltip: {
//                enabled: true,
                shared: true
//                formatter: function () {
//                    return '<b>' + this.series.name + '</b><br/>' + this.x + ': ' + this.y;
//                }
            },
            plotOptions: {
                line: {
                    dataLabels: {
                        enabled: true
                    },
                    enableMouseTracking: true
                }
            },
            credits: {
                enabled: false
            }
        });
        var charts = $(node).highcharts();
        var names = highchartsData.names;
        var series = highchartsData.series;
        $.each(series, function (name, value) {
            charts.addSeries({name: names[name], data: value});
        });

        var rates = highchartsData.rates;
        $.each(rates, function (name, value) {
            charts.addSeries({name: name, data: value, yAxis:1,tooltip:{valueSuffix:'%'}});
        });

    }

    function loadTableData(node, highchartsData) {
        var series = highchartsData.series;
        var data = {
            timeList:highchartsData.xAxisList,
            clickPv:series["click_pv"],
            clickUv:series["click_uv"],
            showPv:series["show_pv"],
            showUv:series["show_uv"]
        };
//        console.info(data);
        $(node).html(template("T:AdTable", data));
    }
</script>
<script id="T:AdTable" type="text/html">
    <% if (timeList.length > 0) { %>
        <% for(var i = 0; i < timeList.length; i++){ %>
        <tr>
            <td><%=timeList[i]%></td>
            <td><%=showPv[i]%></td>
            <td><%=clickPv[i]%></td>
            <td><%=showUv[i]%></td>
            <td><%=clickUv[i]%></td>
        </tr>
        <% } %>
    <% } else { %>
        <tr><td colspan="5">当前时间段没有数据</td></tr>
    <% } %>
</script>
</@layout_default.page>