<#import "../layout/project.module.ftl" as temp />
<@temp.page title="abtest example">
    <@sugar.capsule js=["ko", "examCore",'template'] css=[]/>
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
    <div>
        <span></span>
    </div>
    <button id="search">查询</button>
</div>
<script type="text/javascript">
    $(function () {
        $("#search").on("click", function () {
            var experimentId = $("#experimentid").val();
            var userId = $("#userid").val();
            $.post("getabtestplanforuser.vpage", {experimentId: experimentId, userId: userId}, function (data) {
                console.info(data);
                $("#experimentName").val(data.experimentName);
                $("#groupId").val(data.groupId);
                $("#groupName").val(data.groupName);
                $("#planId").val(data.planId);
                $("#planName").val(data.planName);
                $("#hit").val(data.hit);
            })
        })
    });
</script>
</@temp.page>