<#include "../index.ftl" />

<div class="container">
    <div class="row-fluid">
        <div class="span12">
            <div class="span12">
                <div class="well">
                    <legend>日志列表：</legend>
                    <ul class="pager">
                    <#if (adminLogPage.hasPrevious())>
                        <li><a href="#" onclick="pagePost(${pageNumber-1})" title="Pre">上一页</a></li>
                    <#else>
                        <li class="disabled"><a href="#">上一页</a></li>
                    </#if>
                    <#if (adminLogPage.hasNext())>
                        <li><a href="#" onclick="pagePost(${pageNumber+1})" title="Next">下一页</a></li>
                    <#else>
                        <li class="disabled"><a href="#">下一页</a></li>
                    </#if>
                        <li>当前第 ${pageNumber!} 页 |</li>
                        <li>共 ${adminLogPage.totalPages!} 页</li>
                    </ul>

                    <form class="form-horizontal">
                        <select name="departmentName">
                            <option value="">All</option>
                            <#list departmentList as departmentItem>
                            <option value="${departmentItem.name}" <#if departmentItem.name == departmentName> selected="selected" </#if>>${departmentItem.description}</option>
                            </#list>
                        </select>

                        <select name="logAction">
                            <option value="">All</option>
                            <#list logActionList as logActionItem>
                            <option value="${logActionItem}" <#if logActionItem == logAction> selected="selected" </#if>>${logActionItem}</option>
                            </#list>
                        </select>

                        <input type="text" class="input-small" placeholder="管理用户ID" name="adminUser" value="${adminUser!}">
                        <input type="text" class="input-small" placeholder="用户ID" name="targetUser" value="${targetUser!}">
                        <button type="submit" class="btn">查找</button>
                    </form>

                    <table class="table table-striped table-bordered">
                        <tr>
                            <td></td>
                            <td>管理用户</td>
                            <td>用户</td>
                            <td>内容</td>
                            <td>系统/部门</td>
                        </tr>
                        <#if adminLogPage.content??>
                            <#list adminLogPage.content as adminLogItem>
                            <tr>
                                <td>${adminLogItem_index+1}</td>
                                <td>${adminLogItem.ADMIN_USER_NAME!}</td>
                                <td>${adminLogItem.TARGET_STR!}</td>
                                <td>
                                    ${adminLogItem.OPERATION!}  @ ${adminLogItem.CREATE_DATETIME!} <br>
                                    ${adminLogItem.COMMENT!}
                                </td>
                                <td>${adminLogItem.DESCRIPTION!}</td>
                            </tr>
                            </#list>
                        </#if>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    function pagePost(pageNumber){
        $("#pageNumber").val(pageNumber);
        $("form").submit();
    }
    $("select").change(function () {
        $('form').submit();
    });
</script>