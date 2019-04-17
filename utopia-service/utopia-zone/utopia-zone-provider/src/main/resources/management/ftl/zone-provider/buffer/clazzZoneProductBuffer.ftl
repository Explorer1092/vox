<#-- @ftlvariable name="clazzZoneProductBufferData" type="com.voxlearning.utopia.service.zone.data.VersionedClazzZoneProductData" -->
<!--suppress HtmlUnknownTarget -->
<html>
<head>
    <title>ClazzZoneProductBuffer</title>
</head>
<body>
<h3><a class="btn btn-default" href="/zone-provider/index.do" role="button">ZONE PROVIDER</a></h3>

<div class="container-fluid">

    <div class="row">
        <table class="table table-bordered table-condensed table-striped">
            <thead>
            <tr class="info">
                <td colspan="7">
                    <strong>ClazzZoneProductBuffer(version=${clazzZoneProductBufferData.getVersion()})</strong>
                </td>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td><strong>ID</strong></td>
                <td><strong>NAME</strong></td>
                <td><strong>PRICE</strong></td>
                <td><strong>CURRENCY</strong></td>
                <td><strong>SPECIES</strong></td>
                <td><strong>SUBSPECIES</strong></td>
                <td><strong>PERIOD_OF_VALIDITY</strong></td>
            </tr>
            <#list clazzZoneProductBufferData.getClazzZoneProductList() as c>
            <tr>
                <td>${c.id}</td>
                <td>${c.name}</td>
                <td>${c.price}</td>
                <td>${c.currency.name()}</td>
                <td>${c.species}</td>
                <td>${c.subspecies}</td>
                <td>${c.periodOfValidity}</td>
            </tr>
            </#list>
            </tbody>
        </table>
    </div>

</div>

</body>
</html>