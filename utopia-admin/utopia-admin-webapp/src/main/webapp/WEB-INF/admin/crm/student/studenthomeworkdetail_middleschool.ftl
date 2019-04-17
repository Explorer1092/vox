<#-- @ftlvariable name="userName" type="java.lang.String" -->
<#-- @ftlvariable name="userId" type="java.lang.Long" -->
<#-- @ftlvariable name="studentHomeworkHistoryList" type="java.util.List<com.voxlearning.utopia.mapper.DisplayStudentHomeWorkHistoryMapper>" -->
<#-- @ftlvariable name="mathHomeWorkHistoryMapperList" type="java.util.List<com.voxlearning.utopia.mapper.DisplayStudentMathHomeWorkHistoryMapper>" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend><a href="../user/userhomepage.vpage?userId=${userId!}">${userName!}</a>(${userId!})作业详情</legend>
            <legend><a href="studenthomeworkdetailwithpractice.vpage?userId=${userId!}">按时间查询</a></legend>
        </fieldset>
        <strong>中学作业(30天内)</strong>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 作业ID</th>
                <th> 开始时间</th>
                <th> 结束时间</th>
                <th> 是否完成</th>
                <th> 作业正确率</th>
                <th> 班级ID</th>
            </tr>
            <#if studentHomeworkHistoryList?has_content>
                <#list studentHomeworkHistoryList as studentHomeworkHistory>
                    <tr>
                        <td>
                            <a href="../homework/homeworkhomepage.vpage?category=middle&homeworkId=${studentHomeworkHistory.homeworkId!}&studentId=${userId!}">${studentHomeworkHistory.homeworkId!""}</a>
                        </td>
                        <td>${studentHomeworkHistory.startDate!""}</td>
                        <td>${studentHomeworkHistory.endDate!""}</td>
                        <td><#if studentHomeworkHistory.finished>是<#else>否</#if></td>
                        <td>${(studentHomeworkHistory.getNote())!""}</td>
                        <td>
                            ${studentHomeworkHistory.clazzName!""}(${studentHomeworkHistory.clazzId!""})
                        </td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>
</@layout_default.page>