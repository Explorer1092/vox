<#import "../../layout_default.ftl" as layout_default/>
<@layout_default.page page_title="用户道具变化记录" page_num=24>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">

<style>
    .odd-line {background: #FFFFFF}
</style>
<div id="main_container" class="span9">

    <form class="form-horizontal" action="/equator/student/history/material.vpage" method="get"
          id="materialForm">
        <ul class="inline">
            学生Id：<input type="text" id="studentId" name="studentId" value="${studentId!''}" autofocus="autofocus" placeholder="请输入学生Id"/>
            时间：<input type="text" id="time" name="time" value="${time!''}" readonly/>
            <input type="button" class="btn btn-default" id="submit_query" value="查询"/>
        </ul>
    </form>

    <div class="table_soll">
        <table class="table table-bordered" style="font-size: small">
            <tr>
                <th width="20%">行为</th>
                <th>时间</th>
                <th>道具Id</th>
                <th>道具名称</th>
                <th>道具icon</th>
                <th>原数量</th>
                <th>变换量</th>
                <th>最终量</th>
                <th>操作类型</th>
                <th>操作人员</th>
            </tr>
            <tbody id="tbody">
                <#if materialChangeList ?? >
                <#list materialChangeList as info>
                <#list info.dataList as data>
                <tr class="${(info_index%2 == 0)?string('odd-line','')}">
                <#if data_index == 0>
                    <td rowspan="${info.dataList?size}">${info.desc!''}</td>
                    <td rowspan="${info.dataList?size}">${info.createTime!''}</td>
                    <td>${data.materialId!''}</td>
                    <td>${data.materialName!''}</td>
                    <td><img width="40px" src="${data.materialIcon!''}"/></td>
                    <td>${data.before!'-'}</td>
                    <td>${data.delta!'-'}</td>
                    <td>${data.after!'-'}</td>
                    <td>
                        <#if data.addFlag??>
                            <#if data.addFlag>
                                <span style="color: green; font-weight: bold">增加</span>
                            <#else>
                                <span style="color: red; font-weight: bold">消耗</span>
                            </#if>
                        </#if>
                    </td>
                    <td rowspan="${info.dataList?size}">${info.operator!''}</td>
                <#else>
                    <td>${data.materialId!''}</td>
                    <td>${data.materialName!''}</td>
                    <td><img width="40px" src="${data.materialIcon!''}"/></td>
                    <td>${data.before!'-'}</td>
                    <td>${data.delta!'-'}</td>
                    <td>${data.after!'-'}</td>
                    <td>
                        <#if data.addFlag??>
                            <#if data.addFlag>
                                <span style="color: green; font-weight: bold">增加</span>
                            <#else>
                                <span style="color: red; font-weight: bold">消耗</span>
                            </#if>
                        </#if>
                    </td>
                </#if>
                </tr>
                </#list>
                </#list>
                </#if>
            </tbody>
        </table>
    </div>
</div>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script type="text/javascript">
    $(function () {
        $.fn.datetimepicker.dates['zh-CN'] = {
            days: ["星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"],
            daysShort: ["周日", "周一", "周二", "周三", "周四", "周五", "周六", "周日"],
            daysMin: ["日", "一", "二", "三", "四", "五", "六", "日"],
            months: ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
            monthsShort: ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
            today: "今日",
            suffix: [],
            meridiem: []
        };
        $('#time').datetimepicker({
            language: 'zh-CN',
            weekStart: 1,
            autoclose: 1,
            minView: 2,
            todayHighlight: 1,
            startView: 2,
            forceParse: 0,
            showMeridian: 1,
            format: "yyyy-mm-dd",
            endDate: new Date()
        });
    });

    $(function () {
        $("#submit_query").on("click", function () {
            $("#materialForm").submit();
        });
    });

</script>
</@layout_default.page>