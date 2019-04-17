<#-- @ftlvariable name="userName" type="java.lang.String" -->
<#-- @ftlvariable name="userId" type="java.lang.Long" -->
<#-- @ftlvariable name="grindEarList" type="java.util.List<com.voxlearning.utopia.admin.controller.crm.CrmStudentController.GrindEarDataWrapper>" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend><a href="../user/userhomepage.vpage?userId=${userId!}">${userName!}</a>(${userId!})磨耳朵活动详情</legend>
        </fieldset>
        <strong>用户最后一次作业教材为：${homeworkBookName!""}, 时间为：${homeworkDate!""}</strong>
        <strong>磨耳朵活动详情（本次活动 ${startDate!""}  -  ${endDate!""}）</strong>
        <input type="button" value="如果用户使用外研教材，请点此修复无法完成的问题" onclick="deleteWaiyanFlag()"/>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 日期</th>
                <th> 完成状态</th>
                <th> 连续完成家长奖励</th>
                <th> 标记当日已完成</th>
            </tr>
            <#if grindEarList?has_content>
                <#list grindEarList as grindEar>
                    <tr>
                        <td>
                        ${grindEar.day!""}
                        </td>
                        <td>
                            <span style="color: ${grindEar.color!""}"/> ${grindEar.statusStr!""} </span>
                        </td>
                        <td>
                            <#if grindEar.hasParentReward && grindEar.parentRewardIsSend>
                                已发
                            <#elseif grindEar.hasParentReward && !grindEar.parentRewardIsSend>
                                未发放
                            <#else>
                                无
                            </#if>
                        </td>
                        <td>
                            <#if grindEar.status == 0>
                                <input type="button" onclick="patchFinish(${grindEar.day!""})" value="标记已完成(同时发放随机学豆)"/>
                            <#else>
                                -
                            </#if>
                        </td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>
<script type="text/javascript">
    function patchFinish(day) {
        $.ajax({
            url: "/crm/student/patchDayFinish.vpage",
            type: "POST",
            async: false,
            data: {
                "day":day,
                "studentId":${userId!}
            },
            success: function (data) {
                if (data.success) {
                    alert("标记成功！");
                    window.location.reload();
                }else {
                    alert(data.info);
                }
            }
        });
    }
    function deleteWaiyanFlag() {
        $.ajax({
            url: "/crm/student/deleteWaiyanFlag.vpage",
            type: "POST",
            async: false,
            data: {
                "studentId":${userId!}
            },
            success: function (data) {
                if (data.success) {
                    alert("修复成功，请用户再次阅读即可完成！");
                    window.location.reload();
                }else {
                    alert(data.info);
                }
            }
        });
    }
</script>
</@layout_default.page>