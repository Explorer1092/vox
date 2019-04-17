<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="审核平台" page_num=21>
<div class="span9">
    <div>
        <form id="iform" method="get" action="todo_list.vpage" class="form-horizontal">
            <fieldset><legend>待处理审核</legend></fieldset>
            <ul class="inline form_datetime">
                <li>
                    <label for="userId">
                        审核类型
                        <select id="workflowType" name="workflowType">
                            <option value="0" <#if workflowTypeId == 0>selected </#if>>全部</option>
                            <#list workFlowTypeList as item>
                                <option value="${item.type}" <#if item.type == workflowTypeId >selected </#if>>${item.desc!}</option>
                            </#list>
                        </select>
                    </label>
                </li>
                <li>
                    <label for="startDate">
                        起始时间
                        <input name="startDate" id="startDate" type="text" placeholder="格式：2013-11-04" value="${startDate!}"/>
                    </label>
                </li>
                <li>
                    <label for="endDate">
                        截止时间
                        <input name="endDate" id="endDate" type="text" placeholder="格式：2013-11-04" value="${endDate!}"/>
                    </label>
                </li>
                <li>
                    <label for="applicant">
                        申请人
                        <input name="applicant" id="applicant" type="text"  value="${applicant!}"/>
                    </label>
                </li>
                <li>
                    <button type="submit" class="btn btn-success">查询</button>
                </li>
            </ul>
            <input id="PAGE" name="PAGE" type="hidden"/>
        </form>
        <table class="table table-striped table-bordered" style="font-size: 14px;">
            <thead>
            <tr>
                <th style="width: 90px;max-width:90px;">申请时间</th>
                <th>申请人</th>
                <th>申请类型</th>
                <th>简介</th>
                <th>处理意见</th>
                <th style="width: 80px;">操作</th>
            </tr>
            </thead>
            <#if dataPage?has_content && dataPage.content?has_content>
                <#list dataPage.content as item>
                    <#if item.workFlowRecord?has_content>
                    <tr>
                        <td>${item.workFlowRecord.createDatetime?string("yyyy-MM-dd")}</td>
                        <td>${item.workFlowRecord.creatorName!}</td>
                        <td>${(item.workFlowRecord.workFlowType.desc)!}</td>
                        <td>${item.workFlowRecord.taskContent!}</td>
                        <#if item.processHistory?has_content>
                            <td>${item.processHistory.processNotes!}</td>
                        <#else>
                            <td></td>
                        </#if>
                        <td>
                            <#if (item.workFlowRecord.workFlowType)?? && (item.workFlowRecord.workFlowType) == 'ADMIN_SEND_APP_PUSH'>
                                <a href="/audit/apppush/apppushapply.vpage?id=${item.workFlowRecord.id!}"> 审 核</a>
                            <#elseif (item.workFlowRecord.workFlowType)?? && (item.workFlowRecord.workFlowType) == 'ADMIN_WECHAT_NOTICE'>
                                <a href="/audit/wechat/wechatapply.vpage?id=${item.workFlowRecord.id!}"> 审 核</a>
                            <#else>
                                <a href="proccess_page.vpage?workflowId=${item.workFlowRecord.id!}&applyType=${item.workFlowRecord.workFlowType!}"> 审 核</a>
                            </#if>
                        </td>
                    </tr>
                    </#if>
                </#list>
            </#if>
        </table>
        <#assign pager=dataPage!>
        <#include "../pager_foot.ftl">
    </div>
</div>

<script type="text/javascript">
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

    $(function(){

    });
</script>
</@layout_default.page>
