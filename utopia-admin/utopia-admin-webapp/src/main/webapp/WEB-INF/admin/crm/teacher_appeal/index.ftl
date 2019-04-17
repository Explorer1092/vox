<#import "../../layout_default.ftl" as layout_default/>
<@layout_default.page page_title="老师申诉审核" page_num=3>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script src="${requestContext.webAppContextPath}/public/legacy/common.js"></script>

<div class="span11">
    <legend>
        <a href="/crm/school_clue/clue_list.vpage">学校信息审核</a>&nbsp;&nbsp;
        <a href="/crm/teacher_fake/teacher_fakes.vpage">判假老师审核</a>&nbsp;&nbsp;
        老师申诉审核&nbsp;&nbsp;
        <a href="/crm/teachertransfer/teacherTransferSchool.vpage">转校审核</a>
    </legend>

    <form id="iform" action="/crm/teacher_appeal/index.vpage" method="post">
        <ul class="inline">
            <li>
                <label for="teacherId">
                    老师ID：
                    <input name="teacherId" id="teacherId" value="${teacherId!}" type="text"/>
                </label>
            </li>
            <li>
                <label for="schoolId">
                    学校ID：
                    <input name="schoolId" id="schoolId" value="${schoolId!}" type="text"/>
                </label>
            </li>
            <li>
                <label for="type">
                    申诉类型：
                    <select id="type" name="type">
                        <#if types?has_content>
                            <#list types as t>
                                <#if type?? && type.name() == t.name()>
                                    <option value="${t.name()}" selected="selected">${t.description!}</option>
                                <#else>
                                    <option value="${t.name()}">${t.description!}</option>
                                </#if>
                            </#list>
                        </#if>
                    </select>
                </label>
            </li>
            <li>
                <label for="status">
                    审核状态：
                    <select id="status" name="status">
                        <#if statusList?has_content>
                            <#list statusList as s>
                                <#if status?? && status.name() == s.name()>
                                    <option value="${s.name()}" selected="selected">${s.description!}</option>
                                <#else>
                                    <option value="${s.name()}">${s.description!}</option>
                                </#if>
                            </#list>
                        </#if>
                    </select>
                </label>
            </li>
        </ul>

        <ul class="inline">
            <li>

                <button class="btn btn-primary" type="submit">查询</button>
            </li>
        </ul>
        <input id="PAGE" name="PAGE" type="hidden"/>
        <input id="SIZE" name="SIZE" value="25" type="hidden"/>
    </form>

    <#setting datetime_format="yyyy-MM-dd HH:mm"/>
    <div>
        <table class="table table-bordered">
            <tr>
                <th>审核类别</th>
                <th>提交时间</th>
                <th>省</th>
                <th>市</th>
                <th>区</th>
                <th>学校名称</th>
                <th>申诉人</th>
                <th>审核时间</th>
                <th>审核意见</th>
                <th>操作</th>
            </tr>
            <tbody>
                <#if appealPage?has_content>
                    <#list appealPage.content as appeal>
                    <tr>
                        <td>${appeal.type.description!}</td>
                        <td>${appeal.createDatetime!}</td>
                        <td>${appeal.pname!}</td>
                        <td>${appeal.cname!}</td>
                        <td>${appeal.aname!}</td>
                        <td>${appeal.schoolName!}</td>
                        <td><a href="/crm/teachernew/teacherdetail.vpage?teacherId=${appeal.userId!}"
                               target="_blank">${appeal.userName!}</a> (${appeal.userId!"无用户信息"})
                        </td>
                        <td>${appeal.auditTime!}</td>
                        <td>${appeal.comment!}</td>
                        <td>
                            <#if appeal.status?? && appeal.status.name() == 'WAIT'>
                                <a role="button" class="btn btn-warning" href="audit.vpage?appealId=${appeal.id!}">审核</a>
                            </#if>
                        </td>
                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>

        <#assign pager = appealPage!>
        <#include "../pager_foot.ftl">
    </div>
</div>
</@layout_default.page>