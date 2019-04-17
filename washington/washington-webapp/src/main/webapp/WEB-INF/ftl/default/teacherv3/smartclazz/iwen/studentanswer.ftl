<#import "../module.ftl" as temp />
<@temp.pagecontent mainmenu="classroom_ask" submenu="active">
<div class="s-tab-box">
    <div class="inner">
        <div class="s-table s-table-line-b">
            <table>
                <thead>
                <tr>
                    <th>学生卡号</th>
                    <th>学生姓名</th>
                    <th>学生答案</th>
                </tr>
                </thead>
                <tbody>
                    <#if report?has_content && report.students?has_content>
                        <#list report.students as student>
                        <tr>
                            <td>No.${student.studentCode}</td>
                            <td>${(student.studentName)!}</td>
                            <td class="<#if student.studentAnswer?has_content && report.answer == student.studentAnswer> s-green <#else>s-red</#if>">
                                <#if student.studentAnswer?has_content>
                                    ${student.studentAnswer}
                                <#else>
                                    --
                                </#if>
                            </td>
                        </tr>
                        </#list>
                    <#else>
                        <tr>
                            <td colspan="3">暂无详情</td>
                        </tr>
                    </#if>
                </tbody>
            </table>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        $17.tongji("互动课堂-课堂提问-我的报告-查看详情");
    });
</script>
</@temp.pagecontent>