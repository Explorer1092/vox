<#-- @ftlvariable name="conditionMap" type="java.util.LinkedHashMap" -->
<#macro queryPage>
<div>
    <form method="post" action="/crm/faultOrder/faultorderlist.vpage" class="form-horizontal" id="faultOrderQuery">
        <input type="hidden" id="pageNum" name="page" value="${conditionMap.page!'1'}"/>
        <input type="hidden" id="pageSize" name="pageSize" value="10"/>
        <fieldset>
            <legend>跟踪查询</legend>
            <ul class="inline">
                <li>
                    <label>开始日期：&nbsp;
                        <input id="startDate" name="start" type="text" value="<#if conditionMap.start??>${conditionMap.start!}</#if>" placeholder="2016-01-01" style="width: 100px">
                    </label>
                </li>
                <li>
                    <label>结束日期：&nbsp;
                        <input id="endDate" name="end" type="text" value="<#if conditionMap.end??>${conditionMap.end!}</#if>" placeholder="2016-12-31" style="width: 100px">
                    </label>
                </li>
                <li>
                    <label>追踪项：&nbsp;
                        <select id="faultType" name="faultType" style="width: 100px">
                            <option value=-1>全部</option>
                                <option value="1" <#if conditionMap.faultType == "1">selected</#if>>用户登录（PC）</option>
                                <option value="2" <#if conditionMap.faultType == "2">selected</#if>>用户登录（APP）</option>
                                <option value="3" <#if conditionMap.faultType == "3">selected</#if>>绑定手机</option>
                                <option value="4" <#if conditionMap.faultType == "4">selected</#if>>提交作业</option>
                                <option value="5" <#if conditionMap.faultType == "5">selected</#if>>作业录音</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label>当前状态：&nbsp;
                        <select id="status" name="status" style="width: 100px">
                            <option value=-1>所有状态</option>
                            <option value="0" <#if conditionMap.status == "0">selected</#if>>追踪中</option>
                            <option value="1" <#if conditionMap.status == "1">selected</#if>>已关闭</option>
                        </select>
                    </label>
                </li>
                <li>
                    <button id="query_info_btn"  class="btn btn-primary" onclick="pagePost(1)">查 询</button>
                </li>
            </ul>
        </fieldset>
    </form>
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

        $(document).keydown(function (evt) {
            if (evt.keyCode === 13) {
                $('#query_info_btn').click();
            }
        });
    });

    function pagePost(pageNumber) {
        $("#pageNum").val(pageNumber);
        $("#faultOrderQuery").submit();
    }
</script>
</#macro>