<!doctype html>
<html>
<head>
    <#include "../nuwa/meta.ftl" />
    <title>一起作业</title>
    <style>
        html, body {
            width: 100%;
            height: 100%;
            background: #fff;
        }

        body, button, input, select, textarea {
            font: 12px/1.125 Tahoma, Geneva, sans-serif;
        }

        body, h1, h2, h3, h4, h5, h6, dl, dt, dd, ul, ol, li, th, td, p, blockquote, pre, form, fieldset, legend, input, button, textarea, hr {
            padding: 0;
            margin: 0;
        }

        input, button, select {
            vertical-align: middle;
        }

        table {
            border-collapse: collapse;
        }

        li {
            list-style: none outside;
        }

        fieldset, img {
            vertical-align: middle;
            border: 0 none;
        }

        address, caption, cite, code, dfn, em, i, s, th, var {
            font-style: normal;
            font-weight: normal;
        }

        s {
            vertical-align: middle;
            font: 0px/0px arial;
        }

        a {
            color: #39f;
            text-decoration: none;
        }

        a:hover {
            text-decoration: underline;
        }

        .clear {
            clear: both;
            font: 0pt/0 Arial;
            height: 0px;
            visibility: hidden;
        }

        /**/
        .tabbox {
            padding: 4px;
            background: #eaf3ff;
            border: solid 1px #3580dd;
            border-radius: 6px;
            width: 500px;
            overflow: hidden;
            clear: both;
            margin: 100px auto 0;
        }

        .messageAlertBox {
            background: #fff;
            padding: 10px 30px
        }

        .messageAlertBox .title {
            font: bold 26px/1.125 "微软雅黑", "Microsoft YaHei", Arial;
            color: #F60;
            padding: 0 0 10px;
        }

        .messageAlertBox .content {
            text-align: center;
        }

        .messageAlertBox .content div {
            padding: 10px 0;
            color: #999 !important;
            font: bold 14px/1.125 "微软雅黑", "Microsoft YaHei", Arial;
        }

        .messageAlertBox .content button {
            border: solid 1px #eaf3ff;
            background: #39f;
            padding: 6px 20px;
            margin: 5px auto;
            color: #fff;
            font: bold 14px/1.125 "微软雅黑", "Microsoft YaHei", Arial;
        }

        .messageAlertBox .btn {
            padding: 10px;
            text-align: right;
        }
    </style>
</head>

<body>
<!---Start--->
<div class="tabbox">
    <div class="messageAlertBox">
        <div class="title">信息提示:</div>
    <#if message?exists>
        <div class="content">${message}</div>
    </#if>
        <div class="btn"><a href="${ProductConfig.getMainSiteBaseUrl()}">点此返回</a></div>
        <div class="clear"></div>
    </div>
</div>
<!---End--->
</body>
</html>

