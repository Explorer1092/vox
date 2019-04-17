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
    <script src="${requestContext.webAppContextPath}/public/legacy/common.js"></script>
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

        .date {
            width: 6em;
        }

        .ui-dialog-titlebar-close {
            display: none;
        }
    </style>
</head>

<body style="margin-left: 2em; background: none;">
<div style=";margin-top: 2em">
<#setting datetime_format="yyyy-MM-dd HH:mm"/>
    <div>
        <table width="99%">
            <tbody>
            <#if userRecords?has_content>
                <#list userRecords as record>
                <tr>
                    <td>${record.recorderName!}（${record.recordType!}）</td>
                    <td style="text-align: right">${record.recordTime!}</td>
                </tr>
                <tr style="line-height: 50px;">
                    <td colspan="2" style="color: #fa7252">${record.recordTitle!}</td>
                </tr>
                <tr>
                    <td colspan="2">${record.recordContent!}<#if (record.recordNote)?has_content><br>${record.recordNote!}</#if></td>
                </tr>
                <tr>
                    <td colspan="2">
                        <legend></legend>
                    </td>
                </tr>
                </#list>
            </#if>
            </tbody>
        </table>
    </div>
</div>
</body>