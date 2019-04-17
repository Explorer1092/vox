<#import "../../layout_default.ftl" as layout_default/>
<@layout_default.page page_title="工作记录查询" page_num=3>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script src="${requestContext.webAppContextPath}/public/legacy/common.js"></script>

<div class="span11">
    <legend>
        工作记录查询&nbsp;&nbsp;
        <a href="/crm/task/record_report.vpage">工作记录报表</a>&nbsp;&nbsp;
        <a href="/crm/task/work_record_list.vpage">市场工作记录</a>
    </legend>

    <form id="iform" action="/crm/task/record_list.vpage" method="post">
        <ul class="inline">
            <li>
                <label for="createTime">
                    创建时间：
                    <input name="createStart" id="createStart" value="${createStart!}" type="text" class="date"/> -
                    <input name="createEnd" id="createEnd" value="${createEnd!}" type="text" class="date"/>
                </label>
            </li>

            <li>
                <label for="recorder">
                    记录人员：
                    <select id="recorder" name="recorder">
                        <option value="">全部</option>
                        <#if taskUsers?has_content>
                            <#list taskUsers?keys as user>
                                <#if recorder?? && recorder == user>
                                    <option value="${user}" selected="selected">${taskUsers[user]!}</option>
                                <#else>
                                    <option value="${user}">${taskUsers[user]!}</option>
                                </#if>
                            </#list>
                        </#if>
                    </select>
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
            <li>
                <input type="button" value="导出Excel" onclick="excelExport()"/>
            </li>
        </ul>

        <input id="PAGE" name="PAGE" type="hidden"/>
        <input id="SIZE" name="SIZE" value="25" type="hidden"/>
        <input id="ORDER" name="ORDER" value="DESC" type="hidden"/>
        <input id="SORT" name="SORT" value="createTime" type="hidden"/>
    </form>

    <#setting datetime_format="yyyy-MM-dd HH:mm"/>
    <div>
        <table class="table table-bordered">
            <tr>
                <th>记录时间</th>
                <th>用户角色</th>
                <th>关联用户</th>
                <th>一级分类</th>
                <th>二级分类</th>
                <th>三级分类</th>
                <th>记录内容</th>
                <th>沟通渠道</th>
                <th>记录人</th>
            </tr>
            <tbody>
                <#if taskRecords?has_content>
                    <#list taskRecords.content as record>
                        <#assign userType=(record.userType.name())!"">
                        <#if userType == "TEACHER">
                            <#assign userLink="/crm/teachernew/teacherdetail.vpage?teacherId=${record.userId!}">
                        <#elseif userType == "STUDENT">
                            <#assign userLink="/crm/student/studenthomepage.vpage?studentId=${record.userId!}">
                        <#else>
                            <#assign userLink="javascript:void(0);">
                        </#if>
                        <#assign content=record.content!"">
                        <#if (content?length) gt 15>
                            <#assign tip=content?substring(0, 15) + "...">
                            <#assign content="<a onclick='moreDetail(this)' style='cursor: help' detail='${content}'>${tip}</a>">
                        </#if>
                    <tr>
                        <td>${record.createTime!}</td>
                        <td>${(record.userType.description)!}</td>
                        <td><a href="${userLink!}" target="_blank">${record.userName!}</a> (${record.userId!"无用户信息"})</td>
                        <td>${(record.firstCategory.name())!}</td>
                        <td>${(record.secondCategory.name())!}</td>
                        <td>${(record.thirdCategory.name())!}</td>
                        <td>${content!}</td>
                        <td>${(record.contactType.name())!}</td>
                        <td>${record.recorderName!}</td>
                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>

        <#assign pager=taskRecords!>
        <#include "../pager_foot.ftl">
    </div>

    <#include "common/detail_more.ftl">

    <form id="export-form" action="/crm/task/record_export.vpage" method="post" style="display: none">
        <input id="export-createStart" type="hidden" name="createStart">
        <input id="export-createEnd" type="hidden" name="createEnd">
        <input id="export-recorder" type="hidden" name="recorder">
        <input id="export-contactType" type="hidden" name="contactType">
        <input id="export-userType" type="hidden" name="userType">
        <input id="export-firstCategory" type="hidden" name="firstCategory">
        <input id="export-secondCategory" type="hidden" name="secondCategory">
        <input id="export-thirdCategory" type="hidden" name="thirdCategory">
    </form>
</div>

<script type="text/javascript">
    var CATEGORIES = ${recordCategoryJson!''};

    $(function () {
        dater.render();
        record.renderCategories(true);
    });

    function formReset() {
        $("#createStart").val("");
        $("#createEnd").val("");
        $("#recorder").val("");
        $("#contactType").val("");
        $("#userType").val("");
        $("#firstCategory").val("");
        $("#secondCategory").val("");
        $("#thirdCategory").val("");
    }

    function excelExport() {
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
        var maxTime = 1000 * 60 * 60 * 24 * 30;
        if (end.getTime() - start.getTime() > maxTime) {
            alert("单次导出，最多可导出30天的数据！");
            return false;
        }
        $("#export-createStart").val(startTime);
        $("#export-createEnd").val(endTime);
        $("#export-recorder").val($("#recorder").val());
        $("#export-contactType").val($("#contactType").val());
        $("#export-userType").val($("#userType").val());
        $("#export-firstCategory").val($("#firstCategory").val());
        $("#export-secondCategory").val($("#secondCategory").val());
        $("#export-thirdCategory").val($("#thirdCategory").val());
        $("#export-form").submit();
    }
</script>
</@layout_default.page>