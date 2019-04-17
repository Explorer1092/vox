<#import "../../layout_default.ftl" as layout_default/>
<@layout_default.page page_title="工作记录报表" page_num=3>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script src="${requestContext.webAppContextPath}/public/legacy/common.js"></script>
<style>
    .table th, .table td {
        text-align: center;
        vertical-align: middle;
    }

    .table td {
        font-weight: bold;
    }
</style>

<div class="span11">
    <legend>
        <a href="/crm/task/record_list.vpage">工作记录查询</a>&nbsp;&nbsp;
        工作记录报表&nbsp;&nbsp;
        <a href="/crm/task/work_record_list.vpage">市场工作记录</a>
    </legend>

    <form id="iform" action="/crm/task/record_report.vpage" method="post" onsubmit="return checkForm();">
        <ul class="inline">
            <li>
                <label for="createTime">
                    创建时间：
                    <input name="createStart" id="createStart" value="${createStart!}" type="text" class="date"/> -
                    <input name="createEnd" id="createEnd" value="${createEnd!}" type="text" class="date"/>
                </label>
            </li>

            <li>
                <label for="userType">
                    用户角色：
                    <select id="userType" name="userType">
                        <option value="">全部</option>
                        <option value="TEACHER" <#if userType?? && userType.name() == "TEACHER">selected="selected"</#if>>教师</option>
                        <option value="STUDENT" <#if userType?? && userType.name() == "STUDENT">selected="selected"</#if>>学生</option>
                    </select>
                </label>
            </li>

            <li>
                <label for="contactType">
                    沟通渠道：
                    <select id="contactType" name="contactType" multiple="multiple">
                        <option value="">全部</option>
                        <#if contactTypes?has_content>
                            <#list contactTypes as contact>
                                <#assign iContact = "contactType=" + contact.name() + "&">
                                <#if contactType?? && contactType?contains(iContact)>
                                    <option value="${contact.name()!}" selected="selected">${contact.name()!}</option>
                                <#else>
                                    <option value="${contact.name()!}">${contact.name()!}</option>
                                </#if>
                            </#list>
                        </#if>
                    </select>
                </label>
            </li>
        </ul>

        <ul class="inline">
            <li>
                <label for="firstCategory">
                    一级分类：
                    <select id="firstCategory" name="firstCategory" onchange="secondCategories(true)" class="firstCategory" category="${(firstCategory.name())!''}"></select>
                </label>
            </li>

            <li>
                <label for="secondCategory">
                    二级分类：
                    <select id="secondCategory" name="secondCategory" onchange="thirdCategories(true)" class="secondCategory" category="${(secondCategory.name())!''}"></select>
                </label>
            </li>

            <li>
                <label for="thirdCategory">
                    三级分类：
                    <select id="thirdCategory" name="thirdCategory" class="thirdCategory" category="${(thirdCategory.name())!''}"></select>
                </label>
            </li>
        </ul>

        <ul class="inline">
            <li>
                <button type="submit">查询</button>
            </li>
            <li>
                <input type="button" value="重置" onclick="formReset()"/>
            </li>
        </ul>
    </form>

    <div>
        <#setting number_format="0.##">
        <#assign base = (taskRecordReport.total)!1>
        <#if base < 1>
            <#assign base = 1>
        </#if>
        <#assign detail = (taskRecordReport.detail)!>
        <table class="table table-bordered">
            <tr style="background: #F5F5F5; font-size: x-large">
                <th>TOP1</th>
                <th>TOP2</th>
                <th>TOP3</th>
                <th>TOP4</th>
                <th>TOP5</th>
            </tr>
            <tbody>
            <tr>
                <td>${(detail[0].category.name())!"暂无"}</td>
                <td>${(detail[1].category.name())!"暂无"}</td>
                <td>${(detail[2].category.name())!"暂无"}</td>
                <td>${(detail[3].category.name())!"暂无"}</td>
                <td>${(detail[4].category.name())!"暂无"}</td>
            </tr>
            <tr>
                <td>${((detail[0].count)!0)*100/base}%</td>
                <td>${((detail[1].count)!0)*100/base}%</td>
                <td>${((detail[2].count)!0)*100/base}%</td>
                <td>${((detail[3].count)!0)*100/base}%</td>
                <td>${((detail[4].count)!0)*100/base}%</td>
            </tr>
            </tbody>
        </table>

        <#assign summary = (taskRecordReport.summary)!>
        <table class="table table-bordered">
            <tr style="background: #E3DFED; font-size: larger">
                <th>一级分类</th>
                <th>百分比</th>
                <th>二级分类</th>
                <th>百分比</th>
                <th>三级分类</th>
                <th>百分比</th>
            </tr>
            <tbody>
                <#if summary?has_content>
                    <#list summary as first>
                        <#assign i = 0>
                        <#assign seconds = first.children!>
                        <#list seconds as second>
                            <#assign j = 0>
                            <#assign thirds = second.children!>
                            <#list thirds as third>
                                <#assign i = i+1>
                                <#assign j = j+1>
                            <tr>
                                <#if i == 1>
                                    <td rowspan="${first.base}">${(first.category.name())!}</td>
                                    <td rowspan="${first.base}" style="background: #C4D8F1">${((first.count)!0)*100/base}%</td>
                                </#if>
                                <#if j == 1>
                                    <td rowspan="${second.base}">${(second.category.name())!}</td>
                                    <td rowspan="${second.base}" style="background: #C4D8F1">${((second.count)!0)*100/base}%</td>
                                </#if>
                                <td>
                                    <a href="/crm/task/record_list.vpage?createStart=${createStart!}&createEnd=${createEnd!}&firstCategory=${(first.category.name())!}&secondCategory=${(second.category.name())!}&thirdCategory=${(third.category.name())!}&userType=${userType!}&${contactType!}"
                                       target="_blank">${(third.category.name())!}
                                </td>
                                <td class="rate" style="background: #C4D8F1">${((third.count)!0)*100/base}%</td>
                            </tr>
                            </#list>
                        </#list>
                    </#list>
                </#if>
            </tbody>
        </table>
    </div>
</div>

<script type="text/javascript">
    var CATEGORIES = ${recordCategoryJson!''};

    $(function () {
        dater.render();
        record.renderCategories(true);
        renderRate();
    });

    function renderRate() {
        var MAX = 3.00;
        $(".rate").each(function () {
            var rate = $.trim($(this).text()).split("%")[0];
            if (validNumber(rate) && rate >= MAX) {
                $(this).css("color", "red");
            }
        });
    }

    function formReset() {
        $("#createStart").val("");
        $("#createEnd").val("");
        $("#userType").val("");
        $("#contactType").val("");
        $("#firstCategory").val("");
        $("#secondCategory").val("");
        $("#thirdCategory").val("");
    }

    function checkForm() {
        var startTime = $("#createStart").val();
        var endTime = $("#createEnd").val();
        if (blankString(startTime) || blankString(endTime)) {
            alert("请指定创建时间的开始日期和截止日期！");
            return false;
        }
        var start = dater.parse("yy-mm-dd", startTime);
        var end = dater.parse("yy-mm-dd", endTime);
        if (start == null || end == null) {
            alert("开始日期或截止日期有误！");
            return false;
        }
        var maxTime = 1000 * 60 * 60 * 24 * 90;
        if (end.getTime() - start.getTime() > maxTime) {
            alert("单次报表，最多可查询90天的数据！");
            return false;
        }
        return true;
    }
</script>
</@layout_default.page>