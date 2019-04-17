<!--suppress HtmlUnknownTarget -->
<html>
<head>
    <title>NEWHOMEWORK RPC TEST</title>
</head>
<body>
<h3><a class="btn btn-default" href="/index.do" role="button">DASHBOARD</a></h3>

<div class="container-fluid">


    <div class="row">

        <table class="table table-bordered table-condensed table-striped">
            <thead>
            <tr class="info">
                <td colspan="2"><strong>参数说明支持类型后期逐渐扩展</strong></td>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td><strong>Long,Integer,String,Boolean</strong></td>
                <td>注意：Long需要有L后缀</td>
            </tr>
            <tr>
                <td><strong>Date</strong></td>
                <td>支持（yyyy-MM-dd HH:mm:ss）格式</td>
            </tr>
            <tr>
                <td><strong>Enum</strong></td>
                <td>name值</td>
            </tr>
            <tr>
                <td><strong>List OR Map</strong></td>
                <td>支持基本格式,遇到问题在扩展</td>
            </tr>
            </tbody>
        </table>

        <table class="table table-bordered table-condensed table-striped">
            <thead>
            <tr class="info">
                <td colspan="2"><strong>调用地址</strong></td>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td><strong>Service</strong></td>
                <td><strong>${serviceClassName!''}</strong></td>
            </tr>
            <tr>
                <td><strong>Method</strong></td>
                <td><strong>${methodId!''}</strong></td>
            </tr>
            </tbody>
        </table>

        <form class="form-horizontal" id="reqForm" action="/newhomework-provider/methodinvoke.do" method="post">

            <input class="hidden" name="methodId" value="${methodId!''}"/>
            <input class="hidden" name="serviceClassName" value="${serviceClassName!''}"/>

            <table class="table table-bordered table-condensed table-striped">
                <thead>
                <tr class="info">
                    <td><strong>Parameter</strong></td>
                    <td><strong>Value</strong></td>
                    <td><strong>Type</strong></td>
                </tr>
                </thead>
                <tbody>
                <#if parameterMappers??>
                    <#list parameterMappers as alone>
                    <tr>
                        <td><label class="col-sm-2 control-label">${alone.name!''}</label></td>
                        <td>
                            <input type="text" name="${alone.name!''}" id="${alone.name!''}" value="${alone.valueStr!''}"/>
                        </td>
                        <td><label class="col-sm-2 control-label">${alone.typeStr!''}</label></td>
                    </tr>
                    </#list>
                </#if>

                <tr>
                    <td>
                        <button type="submit" id="btn_save" name="btn_save" class="btn btn-primary">获取结果</button>
                    </td>
                    <td colspan="2">
                    <#if resultData??><pre id="resultData"></pre></#if>
                    </td>
                </tr>

                </tbody>
            </table>
            <div class="control-group">
                <div class="controls">
                </div>
            </div>
        </form>

    </div>

</div>

<script type="text/javascript">
    <#if resultData??>

    $("#resultData").text(JSON.stringify(${resultData!""}, null, 2));
    </#if>
</script>
</body>
</html>