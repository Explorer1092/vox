<#-- @ftlvariable name="commandCounter" type="com.voxlearning.utopia.service.newhomework.impl.listener.CommandCounter" -->
<!--suppress HtmlUnknownTarget -->
<html>
<head>
    <title>NewHomework Provider</title>
</head>
<body>
<h3><a class="btn btn-default" href="/index.do" role="button">Newhomework Provider</a></h3>

<div class="container-fluid">

<#if commandCounter?? && commandCounter.commands?has_content>
    <div class="row">
        <table class="table table-bordered table-condensed table-striped">
            <thead>
            <tr class="info">
                <td colspan="2"><strong>Received/handled command count in listener</strong></td>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td><strong>command</strong></td>
                <td><strong>count</strong></td>
            </tr>
                <#list commandCounter.commands as command>
                <tr>
                    <td>${command}</td>
                    <td>${commandCounter.fetchCount(command)}</td>
                </tr>
                </#list>
            </tbody>
        </table>
    </div>
</#if>
<#if serviceClassNameList?? && (serviceClassNameList?size > 0)>
    <div class="row">
        <table class="table table-bordered table-condensed table-striped">
            <thead>
            <tr class="info">
                <td colspan="4"><strong>NEWHOMEWORK PROVIDER SPRING SERVICE(S)</strong></td>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td><strong>SpringService</strong></td>
                <td><strong>op</strong></td>
            </tr>

            <#list serviceClassNameList as alone>
            <tr>
                <td>${alone!''}</td>
                <td>
                    <form class="form-inline" action="/newhomework-provider/methods.do" method="get" target="_blank">
                        <input class="hidden" name="serviceClassName" value="${alone!''}"/>
                        <button type="submit" class="btn btn-default">GO</button>
                    </form>
                </td>
            </tr>
            </#list>
            </tbody>
        </table>
    </div>
</#if>
</div>
</body>
</html>