<#import "../../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<script src="//cdn.17zuoye.com/public/plugin/jquery/jquery-1.7.1.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
<div class="span9">
    <div>
        <fieldset>
            <legend>学生趣味活动</legend>
        </fieldset>
    </div>
    <div>
        <fieldset>
            <legend>查询结果</legend>
        </fieldset>
        <table class="table table-striped table-bordered do_table" style="font-size: 14px;">
            <thead>
            <tr>
                <th>活动ID</th>
                <th>活动标题</th>
                <th>活动类型</th>
                <th>开始时间</th>
                <th>结束时间</th>
                <th>操作</th>
            </tr>
            </thead>
            <#if datas?has_content>
                <#list datas as data>
                    <tr>
                        <td nowrap>
                            ${data.id!}
                        </td>
                        <td nowrap>
                            ${data.title!}
                        </td>
                        <td nowrap>
                            ${data.type!}
                        </td>
                        <td nowrap>
                            ${data.startTime!}
                        </td>
                        <td nowrap>
                            ${data.endTime!}
                        </td>
                        <td nowrap>
                            <button class="btn btn-primary" onclick="addActivityOpportunity('${data.id}')">添加活动机会</button>
                        </td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>

<script type="text/javascript">
    function addActivityOpportunity(activityId) {
        $.ajax({
            url: "addActivityOpportunity.vpage",
            data: {
                'activityId': activityId,
                'studentId':${studentId!0},
            },
            type: 'post',
            success: function (data) {
                if (data.success) {
                    alert("操作成功");
                } else {
                    alert(data.info);
                }
            }
        });
    }
</script>
</@layout_default.page>
