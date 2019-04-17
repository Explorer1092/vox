<#-- @ftlvariable name="userName" type="java.lang.String" -->
<#-- @ftlvariable name="userId" type="java.lang.Long" -->
<#-- @ftlvariable name="studentHomeworkHistoryList" type="java.util.List<com.voxlearning.utopia.mapper.DisplayStudentHomeWorkHistoryMapper>" -->
<#-- @ftlvariable name="mathHomeWorkHistoryMapperList" type="java.util.List<com.voxlearning.utopia.mapper.DisplayStudentMathHomeWorkHistoryMapper>" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div id="main_container" class="span9">
    <div>
    <#if success>
        <strong>英语基础必过作业</strong>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 作业ID</th>
                <th> 关卡名称</th>
                <th> 是否完成</th>
                <th> 关卡详情</th>
                <th> 个人关卡详情</th>
            </tr>
            <#if englishPackage?has_content>
                <#list englishPackage.stages as stage>
                    <tr>

                        <td>${stage.homeworkId!""}</td>
                        <td>${stage.stageName!""}</td>
                        <#if stage.finished>
                            <td>已完成</td>
                        <#else>
                            <td>未完成</td>
                        </#if>
                        <td>
                            <a href="../homework/newhomeworkhomepage.vpage?homeworkId=${stage.homeworkId!}">关卡详情</a>
                        </td>

                        <#if stage.finished><td>
                            <a href="../homework/usernewhomeworkresultdetail.vpage?homeworkId=${stage.homeworkId!}&userId=${stage.userId!}">个人关卡详情</a></td>
                        <#else>
                            <td>未完成</td>
                        </#if>

                    </tr>
                </#list>
            </#if>
        </table>
        <strong>数学基础必过作业(${time!})</strong>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 作业ID</th>
                <th> 关卡名称</th>
                <th> 是否完成</th>
                <th> 关卡详情</th>
                <th> 个人关卡详情</th>
            </tr>
            <#if mathPackage?has_content>
                <#list mathPackage.stages as stage>
                    <tr>

                        <td>${stage.homeworkId!""}</td>
                        <td>${stage.stageName!""}</td>
                        <#if stage.finished>
                            <td>已完成</td>
                        <#else>
                            <td>未完成</td>
                        </#if>
                        <td>
                            <a href="../homework/newhomeworkhomepage.vpage?homeworkId=${stage.homeworkId!}">关卡详情</a>
                        </td>
                        <#if stage.finished><td>
                            <a href="../homework/usernewhomeworkresultdetail.vpage?homeworkId=${stage.homeworkId!}&userId=${stage.userId!}">个人关卡详情</a></td>
                        <#else>
                            <td>未完成</td>
                        </#if>

                    </tr>
                </#list>
            </#if>
        </table>
        <strong>语文基础必过作业(${time!})</strong>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 作业ID</th>
                <th> 关卡名称</th>
                <th> 是否完成</th>
                <th> 关卡详情</th>
                <th> 个人关卡详情</th>
            </tr>
            <#if chinesePackage?has_content>
                <#list chinesePackage.stages as stage>
                    <tr>

                        <td>${stage.homeworkId!""}</td>
                        <td>${stage.stageName!""}</td>
                        <#if stage.finished>
                            <td>已完成</td>
                        <#else>
                            <td>未完成</td>
                        </#if>
                        <td>
                            <a href="../homework/newhomeworkhomepage.vpage?homeworkId=${stage.homeworkId!}">关卡详情</a>
                        </td>
                        <#if stage.finished><td>
                            <a href="../homework/usernewhomeworkresultdetail.vpage?homeworkId=${stage.homeworkId!}&userId=${stage.userId!}">个人关卡详情</a></td>
                        <#else>
                            <td>未完成</td>
                        </#if>

                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>
    <#else >
    <div>学生没有基础必过练习</div>
    </#if>
</@layout_default.page>