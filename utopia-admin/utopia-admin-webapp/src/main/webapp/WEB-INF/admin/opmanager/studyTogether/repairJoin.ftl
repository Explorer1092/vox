<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div class="span9">
    <fieldset>
        <legend>家长报名修复</legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get"
          action="">
        <div>
            <span style="white-space: nowrap;">
                家长手机号 或者id，用英文逗号间隔：<input type="text" id="parentMobile" name="parentMobile" value="${parentMobile!''}"/>
            </span>
            <span style="white-space: nowrap;">
                课程ID：<select id="selectLessonId" name="selectLessonId">
                <#if lessonIds?? && lessonIds?size gt 0>
                    <#list lessonIds as lessonId>
                        <option value="${lessonId}"
                                <#if (((selectLessonId)!'') == lessonId)>selected="selected"</#if>>${lessonId}</option>
                    </#list>
                <#else>
                    <option value="">暂无数据</option>
                </#if>
            </select>
            </span>
        </div>
    </form>
    <button class="btn btn-primary" type="button" id="searchBtn">修复</button>
</div>
<script type="text/javascript">
    $(function () {
        $("#searchBtn").on('click', function () {
            var pidstr = $("#parentMobile").val();
            $.post('repairParentJoin.vpage', {pids: pidstr,lessonId: $("#selectLessonId").val()}, function (data) {
                console.log(data);
                if (data.success) {
                    alert(JSON.stringify(data.result));
                } else {
                    alert(data.info);
                }
            });
        });
    });
</script>
</@layout_default.page>