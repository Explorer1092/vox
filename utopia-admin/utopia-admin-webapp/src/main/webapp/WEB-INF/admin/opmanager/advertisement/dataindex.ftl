<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="广告数据详情查看" page_num=9>
<link href="${requestContext.webAppContextPath}/public/css/advertisement/advertisement.css" rel="stylesheet">
<div id="main_container" class="span9">
    <div>
        <#if error??>
            <div class="alert alert-error">
                <button type="button" class="close" data-dismiss="alert">×</button>
                <strong>${error!}</strong>
            </div>
        </#if>
    </div>
    <legend class="legend_title">
        <a href="/opmanager/advertisement/config/realtimedata.vpage?adId=${adId}">实时数据</a> &nbsp;&nbsp;&nbsp;&nbsp;
        <strong>数据详情</strong> &nbsp;&nbsp;&nbsp;&nbsp;
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
    <div class="row-fluid">
        <div class="inline span12">
            <legend class="legend_title">统计数据 (截止日期:<span
                    id="upToDate"><#if upToDate??>${upToDate?string('yyyy年MM月dd日')}</#if></span>)
            </legend>
            <table>
            <#-- todo add totalSummaryData -->
                <#if summary??>
                    <tr>
                        <td class="info_td">曝光量：<span id="totalShowPv"
                                                      class="info_td_txt">${summary.totalShowPv!'-'}</span></td>
                        <td class="info_td">点击量：<span id="totalClickPv"
                                                      class="info_td_txt">${summary.totalClickPv!'-'}</span></td>
                        <td class="info_td">独立用户点击量：<span id="totalClickUv"
                                                          class="info_td_txt">${summary.totalClickUv!'-'}</span>
                        </td>
                    </tr>
                    <tr>
                        <td class="info_td">独立用户曝光量：<span id="totalShowUv"
                                                          class="info_td_txt">${summary.totalShowUv!'-'}</span></td>
                        <td class="info_td">点击率(%)：<span id="totalClickRatePv"
                                                         class="info_td_txt">${summary.totalClickRatePv!'-'}</span>
                        </td>
                        <td class="info_td">独立用户点击率(%)：<span id="totalClickRateUv"
                                                             class="info_td_txt">${summary.totalClickRateUv!'-'}</span>
                        </td>
                    </tr>
                <#else>
                    <tr>
                        <td class="info_td">曝光量：<span class="info_td_txt">-</span></td>
                        <td class="info_td">点击量：<span class="info_td_txt">-</span></td>
                        <td class="info_td">独立用户点击量：<span class="info_td_txt">-</span></td>
                    </tr>
                    <tr>
                        <td class="info_td">独立用户曝光量：<span class="info_td_txt">-</span></td>
                        <td class="info_td">点击率(%)：<span class="info_td_txt">-</span></td>
                        <td class="info_td">独立用户点击率(%)：<span class="info_td_txt">-</span></td>
                    </tr>
                </#if>
            </table>
            <div class="well">
                <form id="data_frm" class="form-horizontal" method="post" action="downloadsummarydata.vpage"
                      enctype="multipart/form-data">
                    截止日期 : <input id="endDate" name="endDate" type="text"
                                  value="<#if upToDate??>${upToDate?string('yyyy-MM-dd')}</#if>"
                                  placeholder="yyyy-MM-dd"> &nbsp;&nbsp;&nbsp;&nbsp;
                    <input id="adId" name="adId" type="hidden" value="<#if adId??>${adId}</#if>">
                    <input id="dataType" name="dataType" type="hidden" value="date">
                    <a id="view_data_date" class="btn btn-primary">按日期查看</a>&nbsp;&nbsp;
                    <a id="view_data_region" class="btn">按地域查看</a>
                    <button id="download_data_btn" class="btn btn-info" style="float: right">下载报表</button>
                </form>
                <table class="table table-striped table-bordered">
                    <thead id="table_head">
                    <tr>
                        <td>广告编码</td>
                        <td>广告ID</td>
                        <td>广告位ID</td>
                        <td>日期</td>
                        <td>曝光量</td>
                        <td>独立用户曝光量</td>
                        <td>点击量</td>
                        <td>点击率(%)</td>
                        <td>独立用户点击量</td>
                        <td>独立用户点击率(%)</td>
                    </tr>
                    </thead>
                    <tbody id="table_body">
                        <#if summary?? && summary.detailDataList?has_content>
                            <#list summary.detailDataList as detail>
                                <#if detail_index gte 20><#break></#if>
                            <tr>
                                <td>${detail.adCode!'-'}</td>
                                <td>${detail.adId!'-'}</td>
                                <td>${detail.adSlot!'-'}</td>
                                <td>${detail.dateStr!'-'}</td>
                                <td>${detail.showPv!'-'}</td>
                                <td>${detail.showUv!'-'}</td>
                                <td>${detail.clickPv!'-'}</td>
                                <td>${detail.clickRatePv!'-'}</td>
                                <td>${detail.clickUv!'-'}</td>
                                <td>${detail.clickRateUv!'-'}</td>
                            </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<script>
    var tableHead = $('#table_head');
    var tableBody = $('#table_body');
    $(function () {
        $('#endDate').datepicker({
            maxDate: -1,
            dateFormat: 'yy-mm-dd',  //日期格式，自己设置
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false,
            onSelect: function (selectedDate) {
            }
        });

        $("a[id^='view_data_']").on('click', function () {
            var endDate = $('#endDate').val().trim();
            if (endDate == '') {
                alert("请选择一个日期！");
                return false;
            }
            var $this = $(this);
            var dataType = $this.attr("id").substring("view_data_".length);
            $('#dataType').val(dataType);
            $this.addClass("btn-primary").siblings().removeClass("btn-primary");
            var headHtml = "";
            if (dataType == 'date') {
                headHtml = "<tr><td>广告编码</td><td>广告ID</td><td>广告位ID</td>"
                        + "<td>日期</td>"
                        + "<td>曝光量</td>"
                        + "<td>独立用户曝光量</td>"
                        + "<td>点击量</td>"
                        + "<td>点击率(%)</td>"
                        + "<td>独立用户点击量</td>"
                        + "<td>独立用户点击率(%)</td></tr>";
                tableHead.html(headHtml);
                tableBody.html('');
                queryByDate(endDate);
            } else if (dataType == 'region') {
                headHtml = "<tr><td>广告编码</td><td>广告ID</td><td>广告位ID</td>"
                        + "<td>省</td><td>市</td><td>区</td>"
                        + "<td>曝光量</td>"
                        + "<td>独立用户曝光量</td>"
                        + "<td>点击量</td>"
                        + "<td>点击率(%)</td>"
                        + "<td>独立用户点击量</td>"
                        + "<td>独立用户点击率(%)</td></tr>";
                tableHead.html(headHtml);
                tableBody.html('');
                queryByRegion(endDate);
            }
        });

        $('#download_data_btn').on('click', function () {
            var endDate = $('#endDate').val().trim();
            var dataType = $('#dataType').val();
            if (endDate == '') {
                alert("请选择一个日期！");
                return false;
            }
            $('#data_frm').submit();
        });
    });

    function queryByDate(endDate) {
        var adId = $('#adId').val();
        $.post('querybydate.vpage', {adId: adId, endDate: endDate}, function (data) {
            if (!data.success) {
                alert(data.info);
            } else {
                tableBody.html(template("T:按日期查看", {list: data.summary.detailDataList}))
            }
        });
    }

    function queryByRegion(endDate) {
        var adId = $('#adId').val();
        $.post('querybyregion.vpage', {adId: adId, endDate: endDate}, function (data) {
            if (!data.success) {
                alert(data.info);
            } else {
                tableBody.html(template("T:按地区查看", {list: data.summary.detailDataList}))
            }
        });
    }

</script>
<script type="text/html" id="T:按日期查看">
    <%for(var i = 0; i < list.length && i < 20; ++i){%>
    <tr>
        <td><%=list[i].adCode%></td>
        <td><%=list[i].adId%></td>
        <td><%=list[i].adSlot%></td>
        <td><%=list[i].dateStr%></td>
        <td><%=list[i].showPv%></td>
        <td><%=list[i].showUv%></td>
        <td><%=list[i].clickPv%></td>
        <td><%=list[i].clickRatePv%></td>
        <td><%=list[i].clickUv%></td>
        <td><%=list[i].clickRateUv%></td>
    </tr>
    <%}%>
</script>
<script type="text/html" id="T:按地区查看">
    <%for(var i = 0; i < list.length && i < 20; ++i){%>
    <tr>
        <td><%=list[i].adCode%></td>
        <td><%=list[i].adId%></td>
        <td><%=list[i].adSlot%></td>
        <td><%=list[i].provName%></td>
        <td><%=list[i].cityName%></td>
        <td><%=list[i].countyName%></td>
        <td><%=list[i].showPv%></td>
        <td><%=list[i].showUv%></td>
        <td><%=list[i].clickPv%></td>
        <td><%=list[i].clickRatePv%></td>
        <td><%=list[i].clickUv%></td>
        <td><%=list[i].clickRateUv%></td>
    </tr>
    <%}%>
</script>
</@layout_default.page>