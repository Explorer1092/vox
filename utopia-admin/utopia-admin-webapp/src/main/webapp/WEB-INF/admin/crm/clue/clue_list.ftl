<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="线索查询" page_num=3>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script src="${requestContext.webAppContextPath}/public/legacy/common.js"></script>

<div class="span11">
    <legend>线索查询</legend>
    <form id="iform" action="/crm/clue/clue_list.vpage" method="post">
        <ul class="inline">
            <li>
                <label for="createTime">
                    创建日期：
                    <input name="createStart" id="createStart" value="${createStart!}" type="text" class="date"/> -
                    <input name="createEnd" id="createEnd" value="${createEnd!}" type="text" class="date"/>
                </label>
            </li>
            <li>
                <label for="type">
                    任务类型：
                    <select id="type" name="type">
                        <#if clueTypes?has_content>
                            <#list clueTypes as clueType>
                                <#if type?? && type.name() == clueType.name()>
                                    <option value="${clueType.name()}" selected="selected">${clueType.name()!}</option>
                                <#else>
                                    <option value="${clueType.name()}">${clueType.name()!}</option>
                                </#if>
                            </#list>
                        </#if>
                    </select>
                </label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <button type="submit" onclick="checkTaskListForm();">查询</button>
            </li>
            <li>
                <input type="button" value="重置" onclick="formReset()"/>
            </li>
        </ul>
    </form>

    <div>
        <table class="table table-bordered">
            <tr>
                <th>创建时间</th>
                <th>线索类型</th>
                <th>学校</th>
                <th>老师</th>
                <th>认证状态</th>
                <th>创建人</th>
                <th>接收人</th>
            </tr>
            <tbody>
                <#if clueList?has_content>
                    <#list clueList as clue>
                        <tr>
                            <td>${clue.createTime!}</td>
                            <td>${clue.type!}</td>
                            <td>${clue.schoolName!}(${(clue.schoolId)!})</td>
                            <td>${clue.teacherName!}${(clue.teacherId)!}</td>
                            <td>${(clue["authState"].description)!}</td>
                            <td>${clue.creator!}</td>
                            <td>${clue.receiver!}</td>
                        </tr>
                    </#list>
                </#if>
            </tbody>
        </table>
    </div>
</div>

<script type="text/javascript">
    $(function () {
        dater.render();
    });
</script>
</@layout_default.page>