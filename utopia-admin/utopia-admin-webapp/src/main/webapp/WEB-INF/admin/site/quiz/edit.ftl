<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<link  href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div class="span9">
    <#if info?has_content>
        <div class="alert <#if success?? && success>alert-success<#else>alert-danger</#if>">${info}</div>
    </#if>

    <form id="pushForm" action="updatepushregiontime.vpage" method="post">
        <fieldset>
            <legend>编辑记录</legend>
        </fieldset>
        <ul class="inline">
            <li>
                试卷ID：${pushRegionRef.examPaperId}
                <input type="hidden" id="refId" name="refId" value="${pushRegionRef.id}">
            </li>
            <li>
                推送区域code： ${pushRegionRef.regionCode}

            </li>
            <li>
                年级：${pushRegionRef.clazzLevel}

            </li>

            <li>
                开始时间
                <input id="beginDateTimeStr" value="${pushRegionRef.getBeginDateTimeStr()}" name="beginDateTimeStr" placeholder="格式：2012-12-22 00:00:00" type="text"/>
            </li>
            <li>
                结束时间
                <input id="endDateTimeStr" value="${pushRegionRef.getEndDateTimeStr()}" name="endDateTimeStr" placeholder="格式：2012-12-22 00:00:00" type="text"/>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <input type="submit" value="提交" class="btn btn-primary" id="searchSubmit"/>
                <a class="btn btn-primary" href="list.vpage"/>返回</a>
            </li>
        </ul>
    </form>
</div>
<script type="text/javascript">
    $(function(){
        $.fn.datetimepicker.dates['zh-CN'] = {
            days        : ["星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"],
            daysShort   : ["周日", "周一", "周二", "周三", "周四", "周五", "周六", "周日"],
            daysMin     : ["日", "一", "二", "三", "四", "五", "六", "日"],
            months      : ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
            monthsShort : ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
            today       : "今日",
            suffix      : [],
            meridiem    : []
        };
        $('[name="beginDateTimeStr"]').datetimepicker({
            language:  'zh-CN',
            weekStart: 1,
            autoclose: 1,
            format: "yyyy-mm-dd hh:ii:ss",
            todayHighlight: 1,
            startView: 2,
            forceParse: 0,
            showMeridian: 1
        });
        $('[name="endDateTimeStr"]').datetimepicker({
            language:  'zh-CN',
            weekStart: 1,
            autoclose: 1,
            todayHighlight: 1,
            format: "yyyy-mm-dd hh:ii:ss",
            startView: 2,
            forceParse: 0,
            showMeridian: 1
        });
    });
</script>
</@layout_default.page>