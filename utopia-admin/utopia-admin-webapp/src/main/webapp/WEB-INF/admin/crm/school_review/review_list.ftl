<#import "../../layout_default.ftl" as layout_default/>
<@layout_default.page page_title="学校线索审核" page_num=3>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="http://api.map.baidu.com/api?v=2.0&ak=stpdH3wKubAUFfjRZ8ELoN2A"></script>

<div class="span11">
    <legend>
        学校信息抽审&nbsp;&nbsp;
    </legend>

    <form id="iform" action="/crm/school_review/review_list.vpage" method="post">
        <input id="PAGE" name="PAGE" type="hidden"/>
        <input id="SIZE" name="SIZE" value="50" type="hidden"/>
        <input id="ORDER" name="ORDER" value="DESC" type="hidden"/>
        <input id="SORT" name="SORT" value="" type="hidden"/>
    </form>
    <div>
        <table class="table table-bordered">
            <tr>
                <th>所属区域</th>
                <th>学校名称</th>
                <th>小学/中学</th>
                <th>申请人</th>
                <th>申请人联系电话</th>
                <th>信息审核通过时间</th>
                <th>抽审状态</th>
                <th>抽审人</th>
                <th>操作</th>
            </tr>
            <tbody>
                <#if schoolList??>
                    <#list schoolList as schoolInfo>
                    <tr>
                        <td>${schoolInfo.schoolRegion!}</td>
                        <td>${schoolInfo.schoolName!}</td>
                        <td>${schoolInfo.schoolLevel!}</td>

                        <td>${schoolInfo.recorderName!}</td>
                        <td>${schoolInfo.recorderPhone!}</td>
                        <td>${schoolInfo.authedTime!}</td>
                        <td>
                            <#if schoolInfo.reviewStatus??>
                                <#if schoolInfo.reviewStatus == 1>待抽审
                                <#elseif schoolInfo.reviewStatus == 2>抽审中
                                <#elseif schoolInfo.reviewStatus == 3>已抽审
                                </#if>
                            </#if>
                        </td>
                        <td>${schoolInfo.reviewUser!}</td>
                        <td>
                            <#if schoolInfo.canReview?? && schoolInfo.canReview>
                                <input type="button" value="审核" onclick="schoolReview('${schoolInfo.schoolId!}')"/>
                            </#if>
                        </td>
                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>

        <#include "../pager_foot.ftl">
    </div>
</div>

<div id="review_detail"></div>

<script type="text/javascript">
    function schoolReview(schoolId) {
        $("#review_detail").load('/crm/school_review/review_detail.vpage',{"schoolId": schoolId}, function(){
            $("#review_detail").dialog({
                height: "auto",
                width: "1000",
                autoOpen: true
            });
        });
    }

    function closeReviewDialog(){
        $("#review_detail").dialog("close");
        $("#review_detail").empty();
    }
</script>
</@layout_default.page>