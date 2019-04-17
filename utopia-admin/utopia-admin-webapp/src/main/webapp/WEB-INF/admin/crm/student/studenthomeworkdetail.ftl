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

            <li>
                <form action="?" method="get">
                    开始时间(格式2017-04-14)：<input name="startTime"/>
                    结束时间(格式2017-04-15)：<input name="endTime"/>
                    <input type="hidden" name="userId" value="${userId!}"/>
                    <input type="submit" class="btn" value="搜索"/>
                </form>
            </li>

        </fieldset>
        <strong>英语作业(${time!})</strong>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 作业ID</th>
                <th> 开始时间</th>
                <th> 结束时间</th>
                <th> 作业类型</th>
                <th> 是否检查</th>
                <th> 是否完成</th>
                <th> 作业分数</th>
                <th> 班级ID</th>
                <th> 课本名称</th>
                <th> 提交时间</th>
            </tr>
            <#if studentHomeworkHistoryList?has_content>
                <#list studentHomeworkHistoryList as studentHomeworkHistory>
                    <tr>
                        <td>
                            <a href="../homework/newhomeworkhomepage.vpage?homeworkId=${studentHomeworkHistory.homeworkId!}">${studentHomeworkHistory.homeworkId!""}</a>
                        </td>
                        <td>${studentHomeworkHistory.startDate!""}</td>
                        <td>${studentHomeworkHistory.endDate!""}</td>
                        <#if (studentHomeworkHistory["homeworkType"]=="TermReview")>
                            <td>期末作业</td>
                        <#else>
                            <#if (studentHomeworkHistory["homeworkType"]=="Similar")>
                                <td>类题作业</td>
                            <#else>
                                <td>普通作业</td>
                            </#if>
                        </#if>
                        <td>${studentHomeworkHistory.checked?string('是', '否')}</td>
                        <td><#if studentHomeworkHistory.state == 'FINISHED' || studentHomeworkHistory.state == 'UNCHECKED'>是<#else>否</#if></td>
                        <td>${(studentHomeworkHistory.homeworkScore)!""}</td>
                        <td>
                            ${studentHomeworkHistory.clazzName!""}(${studentHomeworkHistory.clazzId!""})
                        </td>
                        <td>${studentHomeworkHistory.bookName!""}</td>
                        <td>${studentHomeworkHistory.submitTime!""}</td>
                    </tr>
                </#list>
            </#if>
        </table>
        <strong>数学作业(${time!})</strong>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 作业ID</th>
                <th> 开始时间</th>
                <th> 结束时间</th>
                <th> 作业类型</th>
                <th> 是否检查</th>
                <th> 是否完成</th>
                <th> 作业分数</th>
                <th> 班级ID</th>
                <th> 课本名称</th>
                <th> 提交时间</th>
            </tr>
            <#if mathHomeWorkHistoryMapperList?has_content>
                <#list mathHomeWorkHistoryMapperList as studentHomeworkHistory>
                    <tr>
                        <td>
                            <a href="../homework/newhomeworkhomepage.vpage?homeworkId=${studentHomeworkHistory.homeworkId!}">${studentHomeworkHistory.homeworkId!""}</a>
                        </td>
                        <td>${studentHomeworkHistory.startDate!""}</td>
                        <td>${studentHomeworkHistory.endDate!""}</td>
                        <#if (studentHomeworkHistory["homeworkType"]=="TermReview")>
                            <td>期末作业</td>
                        <#else>
                            <#if (studentHomeworkHistory["homeworkType"]=="Similar")>
                                <td>类题作业</td>
                            <#else>
                                <td>普通作业</td>
                            </#if>
                        </#if>
                        <td>${studentHomeworkHistory.checked?string('是', '否')}</td>
                        <td><#if studentHomeworkHistory.state == 'FINISHED' || studentHomeworkHistory.state == 'UNCHECKED'>是<#else>否</#if></td>
                        <td>${(studentHomeworkHistory.homeworkScore)!""}</td>
                        <td>
                        ${studentHomeworkHistory.clazzName!""}(${studentHomeworkHistory.clazzId!""})
                        </td>
                        <td>${studentHomeworkHistory.bookName!""}</td>
                        <td>${studentHomeworkHistory.submitTime!""}</td>
                    </tr>
                </#list>
            </#if>
        </table>
        <strong>语文作业(${time!})</strong>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 作业ID</th>
                <th> 开始时间</th>
                <th> 结束时间</th>
                <th> 作业类型</th>
                <th> 是否检查</th>
                <th> 是否完成</th>
                <th> 作业分数</th>
                <th> 班级ID</th>
                <th> 课本名称</th>
                <th> 提交时间</th>
            </tr>
            <#if chineseHomeworkHistoryList?has_content>
                <#list chineseHomeworkHistoryList as studentHomeworkHistory>
                    <tr>
                        <td>
                            <a href="../homework/newhomeworkhomepage.vpage?homeworkId=${studentHomeworkHistory.homeworkId!}">${studentHomeworkHistory.homeworkId!""}</a>
                        </td>
                        <td>${studentHomeworkHistory.startDate!""}</td>
                        <td>${studentHomeworkHistory.endDate!""}</td>
                        <#if (studentHomeworkHistory["homeworkType"]=="TermReview")>
                            <td>期末作业</td>
                        <#else>
                            <#if (studentHomeworkHistory["homeworkType"]=="Similar")>
                                <td>类题作业</td>
                            <#else>
                                <td>普通作业</td>
                            </#if>
                        </#if>
                        <td>${studentHomeworkHistory.checked?string('是', '否')}</td>
                        <td><#if studentHomeworkHistory.state == 'FINISHED' || studentHomeworkHistory.state == 'UNCHECKED'>是<#else>否</#if></td>
                        <td>${(studentHomeworkHistory.homeworkScore)!""}</td>
                        <td>
                        ${studentHomeworkHistory.clazzName!""}(${studentHomeworkHistory.clazzId!""})
                        </td>
                        <td>${studentHomeworkHistory.bookName!""}</td>
                        <td>${studentHomeworkHistory.submitTime!""}</td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>
</@layout_default.page>