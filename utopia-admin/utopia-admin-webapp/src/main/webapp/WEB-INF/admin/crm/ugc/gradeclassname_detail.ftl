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
                <th>年级</th>
                <th>系统班级分布</th>
                <th>UGC班级分布</th>
                <th>回答人数</th>
                <th>答案占比</th>
            </tr>
            <tbody>
                <#if ugcGradeClassDetailList?has_content>
                    <#list ugcGradeClassDetailList as gradeclassdetaillist>
                    <tr>
                        <td>${gradeclassdetaillist_index+1}</td>
                        <td>${gradeclassdetaillist.grade!}</td>
                        <td>${sysclassname!}</td>
                        <#assign ugcclassstart = gradeclassdetaillist.ugcClassStart!"">
                        <td>${((ugcclassstart=='NULL')?string('',ugcclassstart))!}</td>
                        <td>${(gradeclassdetaillist.count)!}</td>
                        <td>${((gradeclassdetaillist.percentage)!0) * 100}%</td>
                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>
    </div>

    <legend></legend>
    <form id="name-form" action="/crm/ugc/updateugcgradeclassname.vpage" method="post">
        年级分布：<input id="updatedUgcClassName" type="text" name="updatedUgcClassName">
        <input type="hidden" name="schoolId" value="${schoolId!}">
        <input type="hidden" name="grade" value="${grade!}">
        <input id="PAGE" name="PAGE" type="hidden" value="${page!}"/>
        &nbsp;&nbsp;
        <a href="javascript:updateShortName();">提交</a>
        <br>
    </form>
</div>
<script type="text/javascript">
    function updateShortName() {
        if ($.trim($("#updatedUgcClassName").val()) == "") {
            alert("年级分布填写有误！");
        } else {
            $("#name-form").submit();
        }
    }
</script>
</@layout_default.page>
