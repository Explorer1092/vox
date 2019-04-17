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
                <th>系统老师名</th>
                <th>科目</th>
                <th>答案</th>
                <th>回答人数</th>
                <th>答案占比</th>
            </tr>
            <tbody>
                <#if ugcTeacherDetailList?has_content>
                    <#list ugcTeacherDetailList as ugcteacherdetailList>
                    <tr>
                        <td>${ugcteacherdetailList_index+1}</td>
                        <td>${sysTeacherName!}</td>
                        <td>${ugcteacherdetailList.subject!}</td>
                        <#assign ugcteachername = ugcteacherdetailList.ugcTeacherName!>
                        <td>${((ugcteachername=='NULL')?string('',ugcteachername))!}</td>
                        <td>${(ugcteacherdetailList.count)!}</td>
                        <td>${((ugcteacherdetailList.percentage)!0) * 100}%</td>
                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>
    </div>
    <legend></legend>
    <form id="name-form" action="/crm/ugc/updateugcteachername.vpage" method="post">
        老师姓名：<input id="updatedUgcTeacherName" type="text" name="updatedUgcTeacherName">
        <input type="hidden" name="schoolId" value="${schoolId!}">
        <input type="hidden" name="clazzId" value="${clazzId!}">
        <input type="hidden" name="subject" value="${subject!}">
        <input id="PAGE" name="PAGE" type="hidden" value="${page!}"/>
        &nbsp;&nbsp;
        <a href="javascript:updateTeacherName();">提交</a>
        <br>
    </form>

</div>

<script type="text/javascript">
    function updateTeacherName() {
        if ($.trim($("#updatedUgcTeacherName").val()) == "") {
            alert("老师姓名填写有误！");
        } else {
            $("#name-form").submit();
        }
    }
</script>
</@layout_default.page>
