<#macro page group="index" title="17zuoye Mobile Marketing System">
<!DOCTYPE html>
<html>
<head>
    <title>${title}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="17zuoye Mobile Marketing System">
    <meta name="viewport"
          content="width=device-width, minimum-scale=1.0, maximum-scale=1.0, initial-scale=1.0, user-scalable=1.0"/>
    <meta name="apple-mobile-web-app-capable" content="yes"/>
    <meta name="apple-mobile-web-app-status-bar-style" content="black"/>
    <meta name="format-detection" content="telephone=no"/>
    <link href="/public/css/jquery.mobile-flat-ui/jquery.mobile.flatui.css" rel="stylesheet">
    <link href="/public/css/jquery.mobile-1.4.5/jquery.mobile.datepicker.css" rel="stylesheet">
    <script src="/public/js/jquery-1.9.1.min.js"></script>
    <script src="/public/js/jquery.ui.datepicker.js"></script>
    <script src="/public/js/jquery.mobile-1.4.5.min.js"></script>
    <script src="/public/js/jquery.mobile.datepicker.js"></script>
    <style type="text/css">
        body, input, select, textarea, button, .ui-btn {
            font-family: "微软雅黑", "Microsoft YaHei", arial;
        }

        .ui-table-reflow.ui-responsive td, .ui-table-reflow.ui-responsive th {
            float: none;
            display: table-cell;
            width: auto;
            padding: 0.5em;
            font-size: 1em;
        }

        .ui-footer-fixed .ui-link {
            font-size: 16px !important;
            padding: 12px 0;
            font-weight: normal;
        }
    </style>
</head>

<body>
<div data-role="page" data-theme="a">
    <div data-role="header" data-position="fixed">
        <a href="#" data-rel="back" data-ajax="false"
           class="ui-btn ui-btn-left ui-btn-icon-notext ui-icon-carat-l">Back</a>

        <h1>${title}</h1>
        <a id="header-logout" class="ui-btn-right" href="/auth/logout.vpage?client=h5" data-icon="power"
           data-ajax="false" style="display: none">退出</a>
        <a id="header-plus" class="ui-btn-right" href="#" data-icon="plus" data-ajax="false"
           style="display: none">新增</a>
    </div>

    <div role="main" class="ui-content">
        <#nested />
    </div>

    <div data-role="footer" data-position="fixed">
        <div id="foot-nav" data-role="navbar">
            <ul>
                <li>
                    <a href="/mobile/myperformance/index.vpage" data-ajax="false" page-group="myperformance">我的业绩</a>
                </li>
                <li>
                    <a href="/mobile/teacher/index.vpage" data-ajax="false" page-group="teacher">信息查询</a>
                </li>
                <li>
                    <a href="/mobile/workbench/index.vpage" data-ajax="false" page-group="workbench">我的工作台</a>
                </li>
            </ul>
        </div>
    </div>
</div>

<script>
    $(function () {
        $("#foot-nav a.ui-btn-active").removeClass("ui-btn-active");
        $("#foot-nav a[page-group='${group}']").first().addClass("ui-btn-active");
    });
</script>
</body>
</html>
</#macro>