<#import "../../layout_new.ftl" as layout>
<@layout.page group="业绩" title="注册日报">

<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:void(0);" class="headerBack">&lt;&nbsp;返回</a>
            <div class="headerSelect margin-l margin-r">
                <span id="pageTitle"><#if searchType?? && searchType == 2>注册月报<#else>注册日报</#if></span>
                <select id="searchType">
                    <option value="1" <#if searchType?? && searchType == 1>selected="selected"</#if>>注册日报</option>
                    <option value="2" <#if searchType?? && searchType == 2>selected="selected"</#if>>注册月报</option>
                </select>
            </div>
        </div>
    </div>
</div>

<div class="mobileCRM-V2-page" id="switchDate">

    <#if searchType??&&searchType ==1>
        <div class="prev">&lt;&nbsp;前一天</div>
        <div class="next">后一天&nbsp;&gt;</div>
        <div class="dateName" id="dayDate" name="serverDate" value="${searchDate!''}">${searchDate!''}</div>
    </#if>
    <#if searchType??&&searchType ==2>
        <div class="prev">&lt;&nbsp;上一月</div>
        <div class="next">下一月&nbsp;&gt;</div>
        <div class="dateName" id="monthDate" name="serverDate" value="${searchDate!''}">${searchDate!''}</div>
    </#if>
</div>

<ul class="mobileCRM-V2-list">
    <#if regMapList?? && regMapList?has_content>
        <#list regMapList as regMap>
            <li>
                <#if searchType??&&searchType ==2>
                    <a href="/mobile/myperformance/reg_month_performance.vpage?searchDate=${searchDate!''}&regionCode=${regMap["regionCode"]!''}" class="link link-ico">
                <#elseif searchType??&&searchType ==1>
                    <a href="/mobile/myperformance/reg_day_performance.vpage?searchDate=${searchDate!''}&regionCode=${regMap["regionCode"]!''}" class="link link-ico">
                <#else >
                    <a href="javascript:void(0)" class="link link-ico">
                </#if>
                    <div class="side-fl">${regMap["regionName"]!''}</div>
                    <div class="side-fr side-orange">${regMap["studentRegister"]!}</div>
                </a>
            </li>
        </#list>
    <#else >
        暂无数据
    </#if>
</ul>

<script type="text/javascript">
    $(function () {
        var searchType =${searchType};
        var searchDate ="${searchDate}";
        console.info(searchDate);
        console.info(searchType);
        function getRequestMap() {
            var result = {};
            var searchDateParam = searchDate.split("-");
            var date;
            console.info(searchDateParam.length);
            //日报
            if (searchType == 1 && searchDateParam.length == 3) {
                date = new Date(searchDateParam[0], searchDateParam[1], searchDateParam[2]);
            //月报
            }else if(searchType == 2 && searchDateParam.length == 2){
                date = new Date(searchDateParam[0], searchDateParam[1], "01");
            }else{
                alert("查询日期错误,请重试");
                return false;
            }
            result['month'] = date.getFullYear() + "-" + date.getMonth() + "-01";
            result['day'] = date.getFullYear() + "-" + date.getMonth()+ "-" + date.getDate();
            return result;
        }

        $("div.prev").click(function () {
            var dateParam;
            var keyValue = getRequestMap();
            console.info(keyValue);
            var ymr;
            var date;
            if (searchType == 2) {
                ymr = keyValue['month'].split("-");
                date = new Date(parseInt(ymr[0]), parseInt(ymr[1]) - 1, parseInt(ymr[2]));
                var preMonth = new Date(date.getFullYear(), date.getMonth() - 1, date.getDate());
                var dateNums = getMDays(preMonth.getMonth() + 1, preMonth.getFullYear());
                dateParam = getDateFormat(date, -(dateNums));
                window.location.href = "/mobile/myperformance/reg_month_performance.vpage?searchDate=" + dateParam;
            }
            else if (searchType == 1) {
                ymr = keyValue['day'].split("-");
                date = new Date(parseInt(ymr[0]), parseInt(ymr[1]) - 1, parseInt(ymr[2]));
                dateParam = getDateFormat(date, -1);
                window.location.href = "/mobile/myperformance/reg_day_performance.vpage?searchDate=" + dateParam;
            }
        });
        $("div.next").click(function () {
            var dateParam;
            var ymr;
            var date;
            var keyValue = getRequestMap();
            if (searchType == 2) {
                ymr = keyValue['month'].split("-");
                date = new Date(parseInt(ymr[0]), parseInt(ymr[1]) - 1, parseInt(ymr[2]));
                var preMonth = new Date(date.getFullYear(), date.getMonth(), date.getDate());
                dateParam = getDateFormat(date, getMDays(preMonth.getMonth() + 1, preMonth.getFullYear()));
                window.location.href = "/mobile/myperformance/reg_month_performance.vpage?searchDate=" + dateParam;
            }
            else if (searchType == 1) {
                ymr = keyValue['day'].split("-");
                date = new Date(parseInt(ymr[0]), parseInt(ymr[1]) - 1, parseInt(ymr[2]));
                dateParam = getDateFormat(date, 1);
                window.location.href = "/mobile/myperformance/reg_day_performance.vpage?searchDate=" + dateParam;
            }
        });
        function getMDays(month, year) {
            if (month === 1 || month === 3 || month === 5 || month === 7 || month === 8 || month === 10 || month === 12) {
                return 31;
            }
            else if (month === 4 || month === 6 || month === 9 || month === 11) {
                return 30;
            }
            else {
                if ((year % 4 === 0 && year % 100 !== 0) || year % 4 === 0)return 29;
                else return 28;

            }
        }

        function getDateFormat(curDate, ncount) {
            var date = curDate;
            var dateParam = "{#yyyy}-{#mm}-{#dd}";
            date.setDate(date.getDate() + ncount);
            var month = date.getMonth() < 9 ? 0 + "" + (date.getMonth() + 1) : (date.getMonth() + 1);
            var day = date.getDate() < 10 ? 0 + "" + date.getDate() : date.getDate();
            dateParam = dateParam.replace("{#mm}", month);
            dateParam = dateParam.replace("{#dd}", day);
            dateParam = dateParam.replace("{#yyyy}", date.getFullYear());
            return dateParam;
        }

        $("#searchType").change(function () {
            var type = $('#searchType option:selected').val();
            searchType = type;
            var date ="";
            if (type == 1) {
                //从月报切到日报。日期就不传入。默认前一天的数据
                window.location.href = "/mobile/myperformance/reg_day_performance.vpage?";
            } else if (type == 2) {
                date = $("#dayDate").attr("value");
                window.location.href = "/mobile/myperformance/reg_month_performance.vpage?searchDate=" + date;
            } else {
                alert("日报类型错误,请重试");
                return false;
            }
        });
    });
</script>
</@layout.page>