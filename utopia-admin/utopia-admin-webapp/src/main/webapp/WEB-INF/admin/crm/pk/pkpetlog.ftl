<#-- @ftlvariable name="battleReportList" type="java.util.List<com.voxlearning.utopia.service.pk.entity.BattleReport>" -->
<#-- @ftlvariable name="conditionMap" type="java.util.Map" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div>
    <fieldset><legend><a href="../student/studenthomepage.vpage?studentId=${(userId)!}">${(userName)!}</a>的宠物日志</legend></fieldset>

    <table class="table table-hover table-striped table-bordered">
        <#if logs??>
            <tr>
                <th></th>
                <th>操作时间</th>
                <th>宠物ID</th>
                <th>记录内容</th>
            </tr>
            <#list logs as petLog>
                <tr>
                    <td>${petLog_index + 1}</td>
                    <td>${petLog.logTime?string("yyyy-MM-dd HH:mm:ss")}</td>
                    <td>${petLog.petUid}</td>
                    <td>${petLog.logContent}</td>
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