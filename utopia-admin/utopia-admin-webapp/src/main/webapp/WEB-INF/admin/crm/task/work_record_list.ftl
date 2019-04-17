<#import "../../layout_default.ftl" as layout_default/>
<@layout_default.page page_title="市场工作记录" page_num=3>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script src="${requestContext.webAppContextPath}/public/legacy/common.js"></script>

<div class="span11">
    <legend>
        <a href="/crm/task/record_list.vpage">工作记录查询</a>&nbsp;&nbsp;
        <a href="/crm/task/record_report.vpage">工作记录报表</a>&nbsp;&nbsp;
        市场工作记录
    </legend>

    <form id="iform" action="/crm/task/work_record_list.vpage" method="post">
        <ul class="inline">
            <li>
                <label for="startTime">
                    会议时间：
                    <input name="startTime" id="startTime" value="${startTime!}" type="text" class="date"/> -
                    <input name="endTime" id="endTime" value="${endTime!}" type="text" class="date"/>
                </label>
            </li>

            <li>
                <label for="provinceCode">
                    省份：
                    <select id="provinceCode" name="provinceCode" onchange="region.cities(this.value)">
                        <option value="">全国</option>
                        <#if provinces?has_content>
                            <#list provinces as province>
                                <#if provinceCode?? && provinceCode == province.provinceCode>
                                    <option value="${province.provinceCode!}" selected="selected">${province.provinceName!}</option>
                                <#else>
                                    <option value="${province.provinceCode!}">${province.provinceName!}</option>
                                </#if>
                            </#list>
                        </#if>
                    </select>
                </label>
            </li>

            <li>
                <label for="cityCode">
                    城市：
                    <select id="cityCode" name="cityCode" onchange="region.counties(this.value)"></select>
                </label>
            </li>

            <li>
                <label for="countyCode">
                    区县：
                    <select id="countyCode" name="countyCode"></select>
                </label>
            </li>
        </ul>

        <ul class="inline">
            <li>
                <button type="submit">查询</button>
            </li>
        </ul>

        <input id="PAGE" name="PAGE" type="hidden"/>
        <input id="SIZE" name="SIZE" value="25" type="hidden"/>
        <input id="ORDER" name="ORDER" value="DESC" type="hidden"/>
        <input id="SORT" name="SORT" value="workTime" type="hidden"/>
    </form>

    <#setting datetime_format="yyyy-MM-dd"/>
    <div>
        <table class="table table-bordered">
            <tr>
                <th>会议时间</th>
                <th>创建人</th>
                <th>会议主题</th>
                <th>会议内容</th>
                <th>会议地点</th>
                <th>会议类型</th>
                <th>参会人数</th>
                <th>教导员</th>
                <th>客服跟进时间</th>
            </tr>
            <tbody>
                <#if workRecords?has_content>
                    <#list workRecords.content as record>
                        <#assign workContent=record.workContent!"">
                        <#if (workContent?length) gt 15>
                            <#assign tip=workContent?substring(0, 15) + "...">
                            <#assign workContent="<a onclick='moreDetail(this)' style='cursor: help' detail='${workContent}'>${tip}</a>">
                        </#if>
                    <tr>
                        <td>${record.workTime!}</td>
                        <td>${record.workerName!}</td>
                        <td>${record.workTitle!}</td>
                        <td>${workContent!}</td>
                        <td>${record.provinceName!} ${record.cityName!} ${record.countyName!}</td>
                        <td>${(record.meetingType.value)!}</td>
                        <td>${record.meeteeCount!}</td>
                        <td>${record.instructorName!}</td>
                        <td>${record.followingTime!}</td>
                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>

        <#assign pager=workRecords!>
        <#include "../pager_foot.ftl">
    </div>

    <#include "common/detail_more.ftl">
</div>

<script type="text/javascript">
    $(function () {
        dater.render();
        region.render(${provinceCode!0}, ${cityCode!0}, ${countyCode!0});
    });
</script>
</@layout_default.page>