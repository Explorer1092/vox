<#import "../../layout_default.ftl" as layout_default/>
<@layout_default.page page_title="包班制申请记录" page_num=3>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>

<div class="span11">
    <legend>
        包班制申请记录&nbsp;&nbsp;
        <a href="apply_statistic.vpage">包班制申请记录统计</a>
    </legend>
    <form id="frm" action="/crm/main_sub_account/apply_list.vpage" method="post">
        老师ID: <input id="teacherId" name="teacherId" type="text" class="input-medium"  value="<#if teacherId??>${teacherId!}</#if>"/>&nbsp;&nbsp;
        状态: <select id="status" name="status">
                <option value="">全部状态</option>
                <option value="PENDING" <#if status?? && status=='PENDING'> selected </#if> >申请中</option>
                <option value="APPROVED" <#if status?? && status=='APPROVED'> selected </#if>>已开通</option>
                <option value="REJECT" <#if status?? && status=='REJECT'> selected </#if>>审核未通过</option>
            </select>&nbsp;&nbsp;
        日期: <input id="start" name="start" type="text" class="input-small"  value="<#if start??>${start!}</#if>"/>&nbsp;~&nbsp;
        <input id="end" name="end" type="text" class="input-small"  value="<#if end??>${end!}</#if>"/><br>
        发起人: <input id="applicant" name="applicant" type="text" class="input-medium"
                    placeholder="市场人员ID或者名称" value="<#if applicant??>${applicant!}</#if>"/>&nbsp;&nbsp;
        审核人: <input id="auditor" name="auditor" type="text" class="input-medium"
                    placeholder="市场人员ID或者名称" value="<#if auditor??>${auditor!}</#if>"/>&nbsp;&nbsp;
        <button type="submit" class="btn btn-primary">查  询</button>
    </form>
    <div class="well">
        <table class="table table-bordered">
            <tr>
                <th>老师姓名</th>
                <th>发起申请账户</th>
                <th>生成副账户</th>
                <th>市场发起人</th>
                <th>班级</th>
                <th>发起时间</th>
                <th>审核人</th>
                <th>审核时间</th>
                <th>状态</th>
                <th>备注</th>
            </tr>
            <tbody>
                <#if applyList??>
                    <#list applyList as apply>
                    <tr>
                        <td>${apply.teacherName!}</td>
                        <td><a href="/crm/teachernew/teacherdetail.vpage?teacherId=${apply.mainAccount!}" target="_blank">${apply.mainAccount!}(${apply.mainSubject!'--'})</a></td>
                        <td><#if apply.subAccounts?has_content><a href="/crm/teachernew/teacherdetail.vpage?teacherId=${apply.subAccounts!}">${apply.subAccounts!}(${apply.applySubject!'--'})</a><#else>--</#if></td>
                        <td>${apply.agentUser!}(${apply.agentUid!})</td>
                        <td><a href="/crm/clazz/groupinfo.vpage?clazzId=${apply.clazzId}" target="_blank">${apply.clazzName!}</td>
                        <td>${apply.createTime!'--'}</td>
                        <td><#if apply.status!="PENDING">${apply.auditorName!'--'}(${apply.auditor!})<#else>--</#if></td>
                        <td><#if apply.status!="PENDING">${apply.auditTime!'--'}<#else>--</#if></td>
                        <td>
                            <#switch apply.status>
                                <#case "PENDING">申请中<#break>
                                <#case "APPROVED">已开通<#break>
                                <#case "REJECT">审核未通过<#break>
                                <#default>--<#break>
                            </#switch>
                        </td>
                        <td>${apply.auditorNote!'--'}</td>
                    </#list>
                </#if>
            </tbody>
        </table>
    </div>
    <ul class="pager">
        <#if hasPrev>
            <li><a href="#" onclick="pagePost(${currentPage})" title="Pre">&lt;</a></li>
        <#else>
            <li class="disabled"><a href="#">&lt;</a></li>
        </#if>
        <li class="disabled"><a>第 ${currentPage+1!} 页</a></li>
        <li class="disabled"><a>共 <#if totalPage==0>1<#else>${totalPage!}</#if> 页</a></li>
        <#if hasNext>
            <li><a href="#" onclick="pagePost(${currentPage+1})" title="Next">&gt;</a></li>
        <#else>
            <li class="disabled"><a href="#">&gt;</a></li>
        </#if>
    </ul>
</div>

<script type="text/javascript">
    $(function () {
        $('#start').datepicker({
            maxDate: 0,
            dateFormat: "yy-mm-dd",
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeYear: false,
            onSelect: function(dateText){
                $('#end').datepicker("option","minDate",dateText);
            }
        });
        $('#end').datepicker({
            maxDate: 0,
            dateFormat: 'yy-mm-dd',
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeYear: false,
            onSelect: function(dateText){
                $('#start').datepicker("option","maxDate",dateText);
            }
        });
    });

</script>
</@layout_default.page>