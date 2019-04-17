<#-- @ftlvariable name="battleReportList" type="java.util.List<com.voxlearning.utopia.service.pk.entity.BattleReport>" -->
<#-- @ftlvariable name="conditionMap" type="java.util.Map" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div>
    <fieldset><legend>用户<a href="../student/studenthomepage.vpage?studentId=${(userId)!}">${(username)!}</a>PK经验记录查询</legend></fieldset>
    <form action="?" method="post" class="form-horizontal">
        <ul class="inline">
            <li>
                <label>查询日期：<input name="qDate" id="qDate" type="text" value="${(qDate)!}"/></label>
            </li>
            <li>
                <label><input name="userId" type="hidden" value="${(userId)!}"/></label>
            </li>
            <li>
                <button type="submit" class="btn btn-primary">查询</button>
            </li>
        </ul>
    </form>
    <table class="table table-hover table-striped table-bordered">
        <#if logList??>
            <tr>
                <th></th>
                <th>记录时间</th>
                <th>获得经验</th>
                <th>来源</th>
                <th>获得经验后等级</th>
            </tr>
            <#list logList as log>
                <tr>
                    <td>${log_index + 1}</td>
                    <td>${log.logTime?string("yyyy-MM-dd HH:mm:ss")}</td>
                    <td>${(log.delta)!}</td>
                    <td>${(log.experienceType)!}</td>
                    <td>${(log.levelAfter)!}</td>
                </tr>
            </#list>
        </#if>
    </table>
</div>
<script>
    $(function(){
        $("#qDate").datepicker({
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