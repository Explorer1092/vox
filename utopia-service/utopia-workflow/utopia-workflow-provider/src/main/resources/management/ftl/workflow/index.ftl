<#-- @ftlvariable name="pageNames" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="globalTagNames" type="java.util.List<com.voxlearning.utopia.service.config.api.constant.GlobalTagName>" -->


<!DOCTYPE html>
<!--suppress HtmlUnknownTarget -->
<html>
<head>
</head>
<body>

<h3><a class="btn btn-default" href="/index.do" role="button">Config Service</a></h3>

<div class="alert alert-success" role="alert">
    如果你喜欢这个管理页面，请捐赠。目前只接受<span class="glyphicon glyphicon-yen" aria-hidden="true"></span>。
</div>
<hr>
<h5>CurrentConfig Version: ${currentVersion!}</h5>
<div class="container-fluid">
    <div class="row">
        <form class="form-inline" action="/workflow/query_config.do" method="post">
            <div class="form-group">
            <#if testKeys?has_content>
                <select name="type" id="type" class="form-control">
                    <#list testKeys as key>
                        <option value="${key}">${key}</option>
                    </#list>
                </select>
            </#if>
            </div>
            <input name="mode" value="test" type="hidden">
            <button type="submit" class="btn btn-default">Query Test Config</button>
        </form>
    </div>

    <div class="row">
        <form class="form-inline" action="/workflow/query_config.do" method="post">
            <div class="form-group">
            <#if prodKeys?has_content>
                <select name="type" id="type" class="form-control">
                    <#list prodKeys as key>
                        <option value="${key}">${key}</option>
                    </#list>
                </select>
            </#if>
            </div>
            <input name="mode" value="prod" type="hidden">
            <button type="submit" class="btn btn-default">Query Prod Config</button>
        </form>
    </div>
</div>

</body>
</html>

