<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>

<div class="span9">
    <legend> ${(school.cname)!}(${schoolId!})</legend>

    <#setting number_format="0.##">
    <div>
        <strong>1、UGC</strong>
        <table class="table table-bordered" id="triggerType">
            <tr>
                <th>序号</th>
                <th>系统班级学生数量</th>
                <th>答案</th>
                <th>回答人数</th>
                <th>答案占比</th>
            </tr>
            <tbody>
                <#if ugcStudentCountDetailList?has_content>
                    <#list ugcStudentCountDetailList as studentcountdetaillist>
                    <tr>
                        <td>${studentcountdetaillist_index+1}</td>
                        <td>${sysstudentcount!}</td>
                        <#assign ugcstudentcount = studentcountdetaillist.ugcStudentCount!>
                        <td>${ugcstudentcount!0}</td>
                        <td>${(studentcountdetaillist.count)!}</td>
                        <td>${((studentcountdetaillist.percentage)!0) * 100}%</td>
                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>
    </div>
    <legend></legend>
    <form id="name-form" action="/crm/ugc/updateugcstudentcount.vpage" method="post">
        学生数量：<input id="updatedUgcStudentCount" type="text" name="updatedUgcStudentCount">
        <input type="hidden" name="schoolId" value="${schoolId!}">
        <input type="hidden" name="clazzId" value="${clazzId!}">
        <input id="PAGE" name="PAGE" type="hidden" value="${page!}"/>
        &nbsp;&nbsp;
        <a href="javascript:updateStudentCount();">提交</a>
        <br>
    </form>

</div>

<script type="text/javascript">
    function updateStudentCount() {
        var reg = new RegExp("^[0-9]*$");
        var studentCount = $("#updatedUgcStudentCount").val();
        if ($.trim(studentCount) == "" || !reg.test(studentCount)) {
            alert("学生数量填写有误！");
        } else {
            $("#name-form").submit();
        }
    }
</script>
</@layout_default.page>
