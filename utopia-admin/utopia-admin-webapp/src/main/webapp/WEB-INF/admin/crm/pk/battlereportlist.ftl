<#-- @ftlvariable name="battleReportList" type="java.util.List<com.voxlearning.utopia.service.pk.entity.BattleReport>" -->
<#-- @ftlvariable name="conditionMap" type="java.util.Map" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div>
    <fieldset><legend>用户<a href="../student/studenthomepage.vpage?studentId=${(conditionMap.studentId)!}">${(conditionMap.studentId)!}</a>PK记录查询</legend></fieldset>
    <form action="?" method="post" class="form-horizontal">
        <ul class="inline">
            <li>
                <label>PK日期：<input name="pkDate" id="pkDate" type="text" value="${(conditionMap.pkDate?string('yyyy-MM-dd'))!}"/></label>
            </li>
            <li>
                <label><input name="studentId" type="hidden" value="${(conditionMap.studentId)!}"/></label>
            </li>
            <li>
                <button type="submit" class="btn btn-primary">查询</button>
            </li>
        </ul>
    </form>
    <table class="table table-hover table-striped table-bordered">
        <#if battleReportList??>
            <tr>
                <th></th>
                <th>创建时间</th>
                <th>对方角色</th>
                <th>战斗结果</th>
                <th>是否PK陪练员</th>
            </tr>
            <#list battleReportList as battleReport>
                <tr>
                    <td>${battleReport_index + 1}</td>
                    <td>${battleReport.createTime?string("yyyy-MM-dd HH:mm:ss")}</td>
                    <td><#switch battleReport.defenderInfo.career>
                            <#case 'WARRIOR'>勇士<#break/>
                            <#case 'SAGE'>智者<#break/>
                            <#case 'BARD'>奇才<#break/>
                        </#switch>
                        (<a href="../student/studenthomepage.vpage?studentId=${battleReport.defenderInfo.roleId}">${battleReport.defenderInfo.roleId}, </a>
                        <#switch battleReport.defenderInfo.gender>
                            <#case 'MALE'>男<#break/>
                            <#case 'FEMALE'>女<#break/>
                            <#case 'NOT_SURE'>未知<#break/>
                        </#switch>)
                    </td>
                    <td>${(battleReport.battleResult = 'WIN')?string('胜', '负')}</td>
                    <td>${((battleReport.withNpc?? && battleReport.withNpc)?string('是', '否'))!}</td>
                </tr>
            </#list>
        </#if>
    </table>
</div>
<script>
    $(function(){
        $("#pkDate").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });
    });
</script>
</@layout_default.page>