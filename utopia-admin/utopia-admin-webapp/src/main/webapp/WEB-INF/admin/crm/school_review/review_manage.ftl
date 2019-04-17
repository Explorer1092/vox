<#import "../../layout_default.ftl" as layout_default/>
<@layout_default.page page_title="学校信息抽审" page_num=3>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="http://api.map.baidu.com/api?v=2.0&ak=stpdH3wKubAUFfjRZ8ELoN2A"></script>

<div class="span11">
    <legend>
        学校信息抽审&nbsp;&nbsp;
    </legend>

    <form id="iform" action="/crm/school_review/review_manage.vpage" method="post">
        <ul class="inline">
            <li>
                <label for="schoolName">
                    学校ID列表：<textarea id="schoolIds" name="schoolIds" style="height: 120px; width: 300px"
                                     placeholder="请输入学校ID列表，以','分割"></textarea>
                </label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <button id="review_manage">确定</button>
            </li>
            <li>
                <button id="export_review_result">下载excel</button>
            </li>
        </ul>
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
                        <td>
                            <#if schoolInfo.reviewStatus?? && schoolInfo.reviewStatus == 3>
                                <input type="button" value="查看" onclick="reviewResult('${schoolInfo.schoolId!}')"/>
                            <#else>${schoolInfo.tip!}
                            </#if>
                        </td>
                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>
    </div>
</div>

<div id="review_detail"></div>

<script type="text/javascript">
    function reviewResult(schoolId) {
        $("#review_detail").load('/crm/school_review/review_result.vpage', {"schoolId": schoolId}, function () {
            $("#review_detail").dialog({
                height: "auto",
                width: "980",
                autoOpen: true
            });
        });
    }

    $(function () {
        $("#review_manage").on("click", function () {
            $("#iform").attr("action","/crm/school_review/review_manage.vpage");
            $("#iform").submit();
        });

        $("#export_review_result").on("click", function () {
            $("#iform").attr("action","/crm/school_review/export_review_result.vpage");
            $("#iform").submit();
        });
    });

    function closeReviewDialog() {
        $("#review_detail").dialog("close");
        $("#review_detail").empty();
    }
</script>
</@layout_default.page>