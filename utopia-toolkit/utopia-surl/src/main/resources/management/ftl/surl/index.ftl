<#-- @ftlvariable name="handlerCountManager" type="com.voxlearning.utopia.service.surl.module.monitor.HandlerCountManager" -->
<!--suppress HtmlUnknownTarget -->
<html>
<head>
    <title>SURL</title>
</head>
<body>
<h3>
    <a class="btn btn-default" href="/index.do" role="button">DASHBOARD</a>
</h3>

<div class="container-fluid">

    <div class="row">
        <table class="table table-bordered table-condensed table-striped">
            <thead>
            <tr class="info">
                <td colspan="2"><strong>Handler count</strong></td>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td><strong>type</strong></td>
                <td><strong>count</strong></td>
            </tr>
            <#list handlerCountManager.toList() as hc>
                <tr>
                    <td>${hc.type.name()}</td>
                    <td>${hc.count}</td>
                </tr>
            </#list>
            </tbody>
        </table>
    </div>

</div>

</body>
</html>