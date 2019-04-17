<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='趣味活动数据查看' page_num=4>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<style>
    span { font: "arial"; }
</style>
<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend><a href="/site/activity/audit/list.vpage">趣味活动审批</a>&nbsp; &nbsp;趣味活动数据查看</legend>
        </fieldset>
        <div>
            <form id="ad-query" class="form-horizontal" action="">
                申请时间：
                <input type="text" id="startDate" value="${startTime!''}">
                -
                <input type="text" id="endDate" value="${endTime!''}">
                &nbsp; &nbsp;
                <button type="button" class="btn btn-primary" onclick="queryChange()">查询</button>
                &nbsp; &nbsp;
                <button type="button" class="btn" onclick="resetQuery()">重置</button>
            </form>
            <table class="table table-bordered">
                <tr>
                    <th>参与总人数</th>
                    <th>七巧板总参与人数</th>
                    <th>24点总参与人数</th>
                    <th>数独总参与人数</th>
                </tr>
                <tr>
                    <td>${activityTotal!''}</td>
                    <td>${TANGRAM!''}</td>
                    <td>${TWENTY_FOUR!''}</td>
                    <td>${SUDOKU!''}</td>
                </tr>
            </table>
        </div>
    </div>
</div>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script>
    console.log('111', ${startTime!''})
    $('#startDate').datetimepicker({
        format: 'yyyy-mm-dd',  //日期格式，自己设置
        minView: 'month', // 设置只显示到月份
        autoclose: true, // 选择年月日后立即关闭
        todayBtn: true, // 显示今天按钮
    });
    $('#endDate').datetimepicker({
        format: 'yyyy-mm-dd',  //日期格式，自己设置
        minView: 'month', // 设置只显示到月份
        autoclose: true, // 选择年月日后立即关闭
        todayBtn: true, // 显示今天按钮
    });

    function queryChange() {
        var startTime = $('#startDate').val();
        var endTime = $('#endDate').val();
        window.location.replace('/site/activity/data/list.vpage?start=' + startTime + '&end=' + endTime);
    }

    function resetQuery() {
        window.location.replace('/site/activity/data/list.vpage');
    }
</script>
</@layout_default.page>
