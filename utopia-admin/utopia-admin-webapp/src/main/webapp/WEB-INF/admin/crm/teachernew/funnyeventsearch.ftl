<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <link  href="${requestContext.webAppContextPath}/public/css/bootstrap.css" rel="stylesheet">
    <link  href="${requestContext.webAppContextPath}/public/css/admin.css" rel="stylesheet">
    <link  href="${requestContext.webAppContextPath}/public/css/jquery-ui-1.10.3.custom.min.css" rel="stylesheet">
    <script src="${requestContext.webAppContextPath}/public/js/jquery-1.9.1.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/template.js"></script>
    <style>
        .table_soll{ overflow-y:hidden; overflow-x: auto;}
        .table_soll table td,.table_soll table th{white-space: nowrap;}
        .basic_info {margin-left: 2em;}
        .txt{margin-left: .5em;font-weight:800}
        .button_label{with:7em;height: 3em;margin-top: 1em}
        .info_td{width: 7em;}
        .info_td_txt{width: 13em;font-weight:600}
    </style>
</head>
<body style="background: none;">

<div style="margin-top: 2em">
    <ul class="inline">
        <li>
            <span style="font-weight:600">趣味活动记录<span style="font-size: 13px;color: grey;"></span></span>
        </li>
    </ul>
    <table class="table table-hover table-striped table-bordered">
        <tr id="title">
            <th> 标题</th>
            <th> 游戏规则</th>
            <th> 参与年级</th>
            <th> 参与班级id</th>
            <th> 活动开始时间</th>
            <th> 活动结束时间</th>
            <th> 创建时间</th>
            <th> 状态</th>
            <th> 操作</th>
        </tr>
    <#if activityConfigs?has_content>
        <#list activityConfigs as activityConfig>
            <tr data-activityid="${activityConfig["id"]!''}">
                <td>${activityConfig["title"]!''}</td>
                <td>${activityConfig["rules"].level!''}</td>
                <td>
                    <#foreach clazzLevel in activityConfig["clazzLevels"]>
                         ${clazzLevel!''}年级
                    </#foreach>
                </td>
                <#--<td>${activityConfig["clazzLevels"][0]!''}</td>-->
                <td>${activityConfig["clazzIds"][0]!''}</td>
                <td>${activityConfig["startTime"]!0}</td>
                <td>${activityConfig["endTime"]!''}</td>
                <td>${activityConfig["createTime"]!''}</td>
                <td><#if activityConfig["disabled"] = true>已取消<#else>正常</#if></td>
                <td><a href="javascript:void(0);" type="button" class="btn btn-danger delete_activity">删除</a></td>
            </tr>
        </#list>
    <#else >
        <tr><td colspan="10">暂无历史信息</td></tr>
    </#if>
    </table>
</div>

<script>
    $(document).on('click', '.delete_activity', function () {
        var activtiyId = $(this).parents('tr').attr('data-activityid');
        var confirm = window.confirm("确定删除？");
        if (!confirm) {
            return false;
        }
        $.ajax({
            url: '/crm/teachernew/funnyeventremove.vpage',
            type: 'POST',
            data: {
                id: activtiyId
            },
            success: function (res) {
                if (!res.success) {
                    alert(res.info);
                    return;
                }
                alert('删除成功');
                window.location.reload();
            }
,        });
    });
</script>
<style>
    .task_name{
        position: relative;
        cursor: pointer;
    }
    .task_name .task_hover_box{
        width: 200px; border: 1px solid grey;
        line-height:30px; padding:10px;
        position: absolute;
        background: #fff;
        z-index:10;
        right: -200px; top: 10px;
    }
    .reward_url{
        height: 40px;
        font-size: 16px;
        list-style: none;
    }
    .reward_ul li{
        list-style: none;
        overflow: hidden;
        margin-top:10px;
        float:left;width: 150px; line-height: 40px; height: 40px; border: 1px solid #0C0C0C;text-align: center;
        cursor: pointer;
    }
    .reward_ul li.active{
        background:#949494;
        text-shadow: 1px 1px 1px #000;
        color:#fff;
    }
    .reward_box .reward_div{
        display: none;}
</style>
</body>
</html>