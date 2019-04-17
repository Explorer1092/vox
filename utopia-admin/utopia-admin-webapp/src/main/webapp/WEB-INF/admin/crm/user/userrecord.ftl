<#-- @ftlvariable name="intervalDay" type="java.lang.Integer" -->
<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<#-- @ftlvariable name="conditionMap" type="java.util.Map" -->
<#-- @ftlvariable name="userName" type="java.lang.String" -->
<#-- @ftlvariable name="userRecordInfoList" type="java.util.List<java.util.Map>" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div class="span9">

    <fieldset>
        <legend>用户<a href="userhomepage.vpage?userId=${(conditionMap.userId)!}">${userName!}</a>登录历史(默认为最近${(intervalDay + 1)!0}天，查询间隔最多为${(intervalDay + 1)!0}天)</legend>
    </fieldset>

    <form id="queryForm" action="?" method="post" class="form-horizontal">
        <ul class="inline form_datetime">
            <li>
                <label>
                    起始时间
                    <input id="startDate" name="startDate" value="${(conditionMap.startDate?string('yyyy-MM-dd'))!}" type="text" placeholder="格式：2013-12-25"/>
                </label>
            </li>
            <li>
                <label>
                    截止时间
                    <input id="endDate" name="endDate" value="${(conditionMap.endDate?string('yyyy-MM-dd'))!}" type="text" placeholder="格式：2013-12-25"/>
                </label>
            </li>
            <li>
                <button type="button" class="btn btn-primary" onclick="submitdata(0);">查询</button>
            </li>
            <#if queryClassmateShowFlag>
                <li>
                    <button type="button" class="btn btn-primary" onclick="submitdata(1);">查询同班</button>
                </li>
            </#if>
            <li>
                <label><input name="userId" value="${(conditionMap.userId!)}" type="hidden"/></label>
            </li>
            <input id="classmateQuery" name="classmateQuery" value="" type="hidden"/>
        </ul>
    </form>

    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th></th>
            <#if queryClassmates>
                <th>学生ID</th>
            </#if>
            <th>登录时间</th>
            <th>IP</th>
            <th>地点</th>
        </tr>
        <#if userRecordInfoList?has_content>
            <#list userRecordInfoList as userRecordInfo>
                <tr>
                    <td>${userRecordInfo_index + 1}</td>
                    <#if queryClassmates>
                        <td>${userRecordInfo.userId!}</td>
                    </#if>
                    <td>${userRecordInfo.time?string('yyyy-MM-dd HH:mm:ss')}</td>
                    <td>${userRecordInfo.ip!}</td>
                    <td>${userRecordInfo.region!}</td>
                </tr>
            </#list>
        </#if>
    </table>
</div>
<script>
    $(function(){
        $("#startDate").datepicker({
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

        $("#endDate").datepicker({
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
    function submitdata(flag) {
        $('#classmateQuery').val(flag == 1);
        $('#queryForm').submit();
    }
</script>
</@layout_default.page>