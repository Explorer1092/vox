<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <link href="${requestContext.webAppContextPath}/public/css/bootstrap.css" rel="stylesheet">
    <link href="${requestContext.webAppContextPath}/public/css/admin.css" rel="stylesheet">
    <link href="${requestContext.webAppContextPath}/public/css/jquery-ui-1.10.3.custom.min.css" rel="stylesheet">
    <script src="${requestContext.webAppContextPath}/public/js/jquery-1.9.1.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/template.js"></script>
    <style>
        .table_soll {
            overflow-y: hidden;
            overflow-x: auto;
        }

        .table_soll table td, .table_soll table th {
            white-space: nowrap;
        }

        .basic_info {
            margin-left: 2em;
        }

        .txt {
            margin-left: .5em;
            font-weight: 800
        }

        .button_label {
            with: 7em;
            height: 3em;
            margin-top: 1em
        }

        .info_td {
            width: 7em;
        }

        .info_td_txt {
            width: 13em;
            font-weight: 600
        }
    </style>
</head>
<body style="background: none;">
<div style="margin-left: 2em">
    <div style="margin-top: 2em">
        <legend>老师反馈记录</legend>
        <table class="table table-bordered">
            <tr>
                <th>编号</th>
                <th>反馈日期</th>
                <th>反馈类型</th>
                <th>建议/需求</th>
                <th>状态</th>
                <th>预计上线日期</th>
                <th>是否上线</th>
                <th>操作</th>
            </tr>
        <#if feedbackList?? >
            <#list feedbackList as f >
                <tr>
                    <td>${f.id!}</td>
                    <td>${f.createDatetime?string("yyyy-MM-dd")}</td>
                    <td>${f.feedbackType.desc!}</td>
                    <td>${f.content!}</td>
                    <td>${f.feedbackStatus.desc!}</td>
                    <td>${f.onlineEstimateDate!}</td>
                    <td>
                        <#if f.onlineFlag?? >
                            已经上线
                        </#if>
                    </td>
                    <td><a href="/audit/apply/apply_detail.vpage?applyType=AGENT_PRODUCT_FEEDBACK&workflowId=${f.id!}">查看</a>
                    </td>
                </tr>
            </#list>
        </#if>
        </table>
    </div>
</div>
</body>
</html>