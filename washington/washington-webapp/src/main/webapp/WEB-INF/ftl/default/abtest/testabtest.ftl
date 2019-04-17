<#import "../layout/project.module.ftl" as temp />
<@temp.page header="show" title="abtest">
    <@sugar.capsule js=["ko", "examCore"] css=[]/>
<div>
    <h1>abtest 测试页面</h1>
    <br>
    <br>
    请输入实验id：<input type="text" id="experimentid">
    <br>
    <br>
    请输入用户id：<input type="text" id="userid">
    <br>
    <br>
    结果：
    <hr>
    <br>
    实验名称：<input type="text" id="experimentName">
    分组id：<input type="text" id="groupId">
    分组名称：<input type="text" id="groupName">
    方案id：<input type="text" id="planId">
    方案名称：<input type="text" id="planName">
    是否命中标签:<input type="text" id="hit">
    <hr>
    JSON:<textarea id="return_json" cols="100" rows="10"></textarea>
    <hr>
    <br>
    <br>
    <button id="search">查询</button>

    <div id="labelFancyTree"></div>
</div>
<script type="text/javascript">
    $(function () {
        $("#search").on("click", function () {
            var experimentId = $("#experimentid").val();
            var userId = $("#userid").val();
            $.post("generateuserabtestinfo.vpage", {experimentId: experimentId, userId: userId}, function (data) {
                console.info(data);
                $("#return_json").val(JSON.stringify(data));
                $("#experimentName").val(data.abtest.experimentName);
                $("#groupId").val(data.abtest.groupId);
                $("#groupName").val(data.abtest.groupName);
                $("#planId").val(data.abtest.planId);
                $("#planName").val(data.abtest.planName);
                $("#hit").val(data.abtest.hit);
            });
        })
    });
</script>
</@temp.page>