<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div id="main_container" class="span9">
    <div>
        <legend>作业列表</legend>
        <table id="homeworkHistory" class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 作业ID</th>
                <th> 创建时间</th>
                <th> 更改作业创建时间</th>
            </tr>
            <#if homeworkList?has_content>
                <#list homeworkList as homework>
                    <tr id="teacher_homework_${homework.homeworkId!""}">
                        <td><a href="../homework/homeworkhomepage.vpage?homeworkId=${homework.homeworkId!}&homeworkSubject=${homeworkSubject!}">${homework.homeworkId!""}</td>
                        <td>${homework.createTime!""}</td>
                        <td><a href="../teacher/delayhomeworkcreatetime.vpage?homeworkId=${homework.homeworkId!""}&userId=${homework.userId!""}" >推迟作业创建时间一周</a></td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>
</@layout_default.page>