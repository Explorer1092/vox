<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="客服工作量" page_num=3>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<style>
    .date {
        width: 6em;
    }
</style>
<div id="main_container" class="span9">
    <legend>
        外呼工作量
    </legend>

    <form id="iform" action="workload_summary.vpage" method="get">
        <ul class="inline">
            <li>
                <label for="recorders">
                    人员 ：
                    <select id="recorders" name="recorders" multiple="multiple">
                        <#if members?has_content>
                            <#list members?keys as k>
                                <option value="${k}">${members[k]}</option>
                            </#list>
                        </#if>
                    </select>
                </label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <label for="date">
                    日期：
                    <input name="startDate" id="startDate" value="${startDate!}" type="text" class="date"/> -
                    <input name="endDate" id="endDate" value="${endDate!}" type="text" class="date"/>
                </label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <button type="submit">查询</button>
            </li>
            <li>
                <input id="reset" type="button" value="重置"/>
            </li>
        </ul>
        <input id="page" name="page" type="hidden"/>
        <input id="size" name="size" value="10" type="hidden"/>
    </form>

    <#setting datetime_format="yyyy-MM-dd"/>
    <div>
        <table class="table table-bordered">
            <tr>
                <th>组员</th>
                <th>外呼总数</th>
                <th>通话总时长(min)</th>
                <th>有效外呼数量</th>
                <th>平均通话时长(min)</th>
                <th>接通率</th>
                <th>认证学生数</th>
            </tr>
            <tbody>
                <#if result?has_content>
                    <#list result as e>
                    <tr>
                        <td>${e.recorderName!}</td>
                        <td>${e.totalCalls!0}</td>
                        <td>${((e.totalDuration!0)/60)?int}</td>
                        <td>${e.effectiveCalls!0}</td>
                        <td>
                            <#if e.effectiveCalls gt 0>
                                ${((e.totalDuration!0)/60/e.effectiveCalls)?int}
                            <#else>
                                0
                            </#if>
                        </td>
                        <td>
                            <#if e.totalCalls gt 0>
                                ${((e.effectiveCalls!0)/e.totalCalls * 100)?int}%
                            <#else>
                                0%
                            </#if>
                        </td>
                        <td>${e.totalStudents!0}</td>
                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>
    </div>
</div>

<script type="text/javascript">
    $(function () {
        $(".date").datepicker({
            dateFormat: "yy-mm-dd",
            monthNames: ["1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"],
            monthNamesShort: ["1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: true,
            changeYear: true
        });

        $("#iform").submit(function () {
            var startDate = $("#startDate").val();
            var endDate = $("#endDate").val();
            if (startDate == null || startDate == "" || endDate == null || endDate == "") {
                alert("请选择查询日期!");
                return false;
            }
            return true;
        });

        $("#reset").click(function () {
            $("#startDate").val("");
            $("#endDate").val("");
        });
    });
</script>
</@layout_default.page>