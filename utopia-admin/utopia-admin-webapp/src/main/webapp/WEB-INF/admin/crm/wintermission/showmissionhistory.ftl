<#-- @ftlvariable name="userName" type="java.lang.String" -->
<#-- @ftlvariable name="vitalityLogList" type="java.util.List<com.voxlearning.utopia.admin.data.VitalityMapper>" -->
<#-- @ftlvariable name="userId" type="java.lang.Long" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend>学生<a href="../student/studenthomepage.vpage?studentId=${userId!}">${userName!}</a>寒假推送任务完成历史
            </legend>
        </fieldset>
        <div>
            <table class="table table-hover table-striped table-bordered">
                <tr id="vitalitylog_title">
                    <td>时间</td>
                    <td>正确率</td>
                    <td>是否完成任务</td>
                    <td>累计完成任务天数</td>
                    <td>奖励内容</td>
                </tr>
                <#if missionHistoryList?has_content>
                    <#list missionHistoryList as historyLog>
                        <tr>
                            <td>${historyLog.finishTime?string("yyyy-MM-dd HH:mm:ss")}</td>
                            <td>${historyLog.correctRate!}</td>
                            <td>${historyLog.accomplished?string('是','否')}</td>
                            <td>${historyLog.accomplishedDay!}</td>
                            <td>
                                <#list historyLog.detail?keys as mapKey>
                                    ${mapKey}:${historyLog.detail[mapKey]};
                                </#list>
                            </td>
                        </tr>
                    </#list>
                </#if>
            </table>
        </div>
    </div>

    <form id="importSkillForm" method="post" action="/crm/wintermission/importnohw.vpage" enctype="multipart/form-data">
        <legend>无作业班级导入</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">无作业班级导入</button>
                </td>
            </tr>
        </table>
    </form>

</div>

</@layout_default.page>