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
    <script src="${requestContext.webAppContextPath}/public/legacy/common.js"></script>

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



<div style="margin-left: 2em">
    <div style="margin-top: 2em">
        <form id="iform" action="/crm/teachernew/teacherrewardstudenthistory.vpage?teacherId=${teacherId!''}" method="post">
            <ul class="inline">
                <li>
                    <label for="startDate">
                        创建时间：
                        <input name="startDate" id="startDate" value="${startDate!}" type="text" class="date"/> -
                        <input name="endDate" id="endDate" value="${endDate!}" type="text" class="date"/>
                    </label>
                </li>
                <li>
                    <button type="submit">查询</button>
                </li>
                <li>
                    <input type="button" value="重置" onclick="formReset()"/>
                </li>
            </ul>
        </form>
    </div>

    <div style="margin-top: 2em">
        <ul class="inline">
            <li>
                <span style="font-weight:600">学生学豆奖励记录<span style="font-size: 13px;color: grey;"></span></span>
                <span style="margin-left: 3em;">获得学豆总量(仅计算当前查询数据)：<span style="font-weight:600">${totalIntegral!0}</span></span>
            </li>
        </ul>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 创建时间</th>
                <th> 姓名</th>
                <th> 学生ID</th>
                <th> 学豆</th>
                <th> 组</th>
                <th> 班级</th>
                <th> 奖励项</th>
            </tr>
        <#if rewardStudentHistoryList?has_content>
            <#list rewardStudentHistoryList as rewardStudentHistoryListMap>
                <tr>
                    <td>${rewardStudentHistoryListMap["createTime"]!''}</td>
                    <td><a href="/crm/student/studenthomepage.vpage?studentId=${rewardStudentHistoryListMap["studentId"]!''}" target="_blank">${rewardStudentHistoryListMap["studentName"]!''}</a></td>
                    <td>${rewardStudentHistoryListMap["studentId"]!''}</td>
                    <td>${rewardStudentHistoryListMap["integral"]!''}</td>
                    <td><a href="/crm/clazz/groupinfo.vpage?groupId=${rewardStudentHistoryListMap["groupId"]!''}" target="_blank">${rewardStudentHistoryListMap["groupId"]!''}</a></td>
                    <td><a href="/crm/clazz/groupinfo.vpage?clazzId=${rewardStudentHistoryListMap["clazzId"]!''}" target="_blank">${rewardStudentHistoryListMap["clazzId"]!''}</a></td>
                    <td>${rewardStudentHistoryListMap["smartClazzRewardItem"]!''}</td>
                </tr>
            </#list>
        <#else ><td >暂无历史信息</td>
        </#if>
        </table>
    </div>

</div>
</body>
<script type="text/javascript">
    $(function () {
        dater.render();
    });

    function formReset() {
        $("#startDate").val("");
        $("#endDate").val("");
    }
</script>
</html>