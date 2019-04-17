<#-- @ftlvariable name="vendorAppsResgRefBufferData" type="com.voxlearning.utopia.service.vendor.buffer.VersionedVendorAppsResgRefList" -->
<!--suppress HtmlUnknownTarget -->
<html>
<head>
    <title>VendorAppsResgRefBuffer</title>
</head>
<body>
<h3><a class="btn btn-default" href="/vendor-provider/index.do" role="button">VENDOR PROVIDER</a></h3>

<div class="container-fluid">

    <div class="row">
        <table class="table table-bordered table-condensed table-striped">
            <thead>
            <tr class="info">
                <td colspan="6"><strong>VendorAppsResgRefBuffer
                    (version=${vendorAppsResgRefBufferData.getVersion()})</strong></td>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td><strong>ID</strong></td>
                <td><strong>APP_ID</strong></td>
                <td><strong>APP_KEY</strong></td>
                <td><strong>RESG_ID</strong></td>
                <td><strong>CREATE_DATETIME</strong></td>
                <td><strong>UPDATE_DATETIME</strong></td>
            </tr>
            <#list vendorAppsResgRefBufferData.getRefList() as c>
            <tr>
                <td>${c.id!''}</td>
                <td>${c.appId!''}</td>
                <td>${c.appKey!''}</td>
                <td>${c.resgId!''}</td>
                <td>${c.createDatetime?datetime}</td>
                <td>${c.updateDatetime?datetime}</td>
            </tr>
            </#list>
            </tbody>
        </table>
    </div>

</div>

</body>
</html>