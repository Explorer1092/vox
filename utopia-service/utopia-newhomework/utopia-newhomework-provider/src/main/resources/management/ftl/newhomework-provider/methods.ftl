<html>
<head>
    <title>NEWHOMEWORK RPC TEST</title>
</head>
<body>
<h3><a class="btn btn-default" href="/index.do" role="button">DASHBOARD</a></h3>

<div class="container-fluid">

    <div class="row">
        <h4>Service: ${serviceClassName!''}</h4>
        <table class="table table-bordered table-condensed table-striped">
            <thead>
            <tr class="info">
                <td><strong>Method</strong></td>
                <td><strong>Operation</strong></td>
            </tr>
            </thead>
            <tbody>

            <#list methodMappers as alone>
            <tr>
                <td>${alone.methodId!''}</td>
                <td>
                    <form class="form-inline" action="/newhomework-provider/methoddetail.do"
                          method="get" target="_blank">
                        <input class="hidden" name="methodId" value="${alone.methodId!''}" />
                        <input class="hidden" name="serviceClassName" value="${serviceClassName!''}" />
                        <button type="submit" class="btn btn-default">GO</button>
                    </form>
                </td>
            </tr>
            </#list>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>