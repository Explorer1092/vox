<#-- @ftlvariable name="userName" type="java.lang.String" -->
<#-- @ftlvariable name="userId" type="java.lang.Long" -->
<#-- @ftlvariable name="grindEarList" type="java.util.List<com.voxlearning.utopia.admin.controller.crm.CrmStudentController.GrindEarDataWrapper>" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend><a href="../user/userhomepage.vpage?userId=${userId}">${userName}</a>(${userId})家长奖励历史</legend>
        </fieldset>
        <br>
        时间区间:
        <input id="startDate" type="text" class="input input-small" style="margin-bottom: 0;" value="${startDate!}" maxlength="10">
        至
        <input id="endDate" type="text" class="input input-small" style="margin-bottom: 0;" value="${endDate!}" maxlength="10">

        <button class="btn btn-success" onclick="queryHistory()" style="margin-right: 10px">查询</button>
        <span><font color="red">(不填日期，默认查询最近七天的数据。查询范围最大为90天，超过90天，系统默认返回90天的数据)</font></span>
        <br><br>
        <span style="font-size: 24px">本学期家庭互动值：${itemCount!0}</span>
        <table id="rewards" class="table table-hover table-striped table-bordered" style="margin-top: 20px">
            <thead>
                <tr>
                    <th>奖励id</th>
                    <th hidden="hidden">奖励key</th>
                    <th>奖励名称</th>
                    <th hidden="hidden">real奖励类型</th>
                    <th>奖励类型</th>
                    <th>奖励数量</th>
                    <th>创建时间</th>
                    <th>发放时间</th>
                    <th>领取时间</th>
                    <th>奖励状态</th>
                    <th>奖励发放人</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <#if rewards?has_content>
                    <#list rewards as reward>
                        <tr>
                            <td>${reward.id!''}</td>
                            <td hidden="hidden">${reward.key!''}</td>
                            <td>${reward.title!''}</td>
                            <td hidden="hidden">${reward.realType!''}</td>
                            <td>${reward.type!''}</td>
                            <td>${reward.count!0}</td>
                            <td>${reward.createDate!''}</td>
                            <td>${reward.sendDate!''}</td>
                            <td>${reward.receiveDate!''}</td>
                            <td>${reward.status!''}</td>
                            <td>${reward.sendUser!''}</td>
                            <td>
                                <#if reward.status == 0>
                                    <input type="button" onclick="sendParentReward('${reward.id!""}','${reward.key!""}', '${reward.realType!""}', ${reward.count!0})" class="btn btn-success" value="发放"/>
                                    <#else >
                                        -
                                </#if>
                            </td>
                        </tr>
                    </#list>
                </#if>
            </tbody>
        </table>
    </div>
</div>

<script type="text/javascript">
    $('#startDate').datepicker({
        maxDate: 0,
        dateFormat: "yy-mm-dd",
        onSelect: function(dateText){
            $('#endDate').datepicker("option","minDate",dateText);
        }
    });
    $('#endDate').datepicker({
        maxDate: 0,
        dateFormat: 'yy-mm-dd',
        onSelect: function(dateText){
            $('#startDate').datepicker("option","maxDate",dateText);
        }
    });

    function queryHistory() {
        var queryStr = '?userId=${userId}';
        var startDate = $('#startDate').val();
        var endDate = $('#endDate').val();
        if (startDate == '' && endDate != '') {
            alert("请正确填写日期区间");
            return false;
        }
        queryStr += '&startDate=' + startDate + "&endDate=" + endDate;
        location.href = queryStr;
    }

    function sendParentReward(id, key, type, count) {
        $.ajax({
            type:"post",
            url:"sendReward.vpage",
            data:{
                studentId:${userId!},
                id:id,
                key:key,
                type:type,
                count:count
            },
            success: function (data) {
                if (data.success) {
                    location.href = '?userId=${userId}&startDate=${startDate!''}&endDate=${endDate!''}';
                } else {
                    alert(data.info)
                }
            }
        });
    }
</script>
</@layout_default.page>