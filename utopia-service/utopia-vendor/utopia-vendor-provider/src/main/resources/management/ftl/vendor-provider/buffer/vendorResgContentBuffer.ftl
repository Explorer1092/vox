<#-- @ftlvariable name="vendorResgContentBufferData" type="com.voxlearning.utopia.service.vendor.buffer.VersionedVendorResgContentList" -->
<!--suppress HtmlUnknownTarget -->
<html>
<head>
    <title>VendorResgContentBuffer</title>
</head>
<body>
<h3><a class="btn btn-default" href="/vendor-provider/index.do" role="button">VENDOR PROVIDER</a></h3>

<div class="container-fluid">

    <div class="row">
        <table class="table table-bordered table-condensed table-striped">
            <thead>
            <tr class="info">
                <td colspan="5"><strong>VendorResgContentBuffer
                    (version=${vendorResgContentBufferData.getVersion()})</strong></td>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td><strong>ID</strong></td>
                <td><strong>RESG_ID</strong></td>
                <td><strong>RES_NAME</strong></td>
                <td><strong>CREATE_DATETIME</strong></td>
                <td><strong>UPDATE_DATETIME</strong></td>
            </tr>
            <#list vendorResgContentBufferData.getContentList() as c>
            <tr>
                <td>${c.id!''}</td>
                <td>${c.resgId!''}</td>
                <td>${c.resName!''}</td>
                <td>
                    <#if c.createDatetime??>
                        ${c.createDatetime?datetime}
                    </#if>
                </td>
                <td>
                    <#if c.updateDatetime??>
                        ${c.updateDatetime?datetime}
                    </#if>
                </td>
            </tr>
            </#list>
            </tbody>
        </table>
    </div>

</div>

</body>
</html>