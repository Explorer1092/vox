<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div id="main_container" class="span9">
    <div>
        <legend>手机绑定列表</legend>
        <table id="homeworkHistory" class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th>班级名称</th>
                <th>学生ID</th>
                <th>学生姓名</th>
                <th>学生绑定手机</th>
                <th>家长ID</th>
                <th>家长姓名</th>
                <th>家长绑定手机</th>
            </tr>
            <#if binds?has_content>
                <#list binds as bind>
                    <tr>
                        <td>${bind.clazzName!''}</td>
                        <td>${bind.studentId!''}</td>
                        <td><a href="../user/userhomepage.vpage?userId=${bind.studentId!}">${bind.studentName!''}</a></td>
                        <td>${bind.studentBindMobile!''}</td>
                        <td>${bind.parentId!''}</td>
                        <td><a href="../parent/parenthomepage.vpage?parentId=${bind.parentId!}">${bind.parentName!''}</a></td>
                        <td>${bind.parentBindMobile!''}</td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>
</@layout_default.page>