<#-- @ftlvariable name="fairylandProductBufferData" type="com.voxlearning.utopia.service.vendor.api.mapper.VersionedFairylandProducts" -->
<#-- @ftlvariable name="vendorAppsBufferData" type="com.voxlearning.utopia.service.vendor.api.mapper.VersionedVendorAppsList" -->
<#-- @ftlvariable name="vendorAppsResgRefBufferData" type="com.voxlearning.utopia.service.vendor.buffer.VersionedVendorAppsResgRefList" -->
<#-- @ftlvariable name="vendorResgContentBufferData" type="com.voxlearning.utopia.service.vendor.buffer.VersionedVendorResgContentList" -->
<!--suppress HtmlUnknownTarget -->
<html>
<head>
    <title>VENDOR PROVIDER</title>
</head>
<body>
<h3><a class="btn btn-default" href="/index.do" role="button">DASHBOARD</a></h3>

<div class="container-fluid">

    <div class="row">
        <table class="table table-bordered table-condensed table-striped">
            <thead>
            <tr class="info">
                <td colspan="4"><strong>VENDOR PROVIDER BUFFER(S)</strong></td>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td><strong>name</strong></td>
                <td><strong>inspect</strong></td>
                <td><strong>increment</strong></td>
                <td><strong>reload</strong></td>
            </tr>
            <tr>
                <td>FairylandProductBuffer</td>
                <td>
                    <form class="form-inline" action="/vendor-provider/fairylandProductBuffer/inspect.do"
                          method="get">
                        <button type="submit"
                                class="btn btn-default">${fairylandProductBufferData.getFairylandProducts()?size}</button>
                    </form>
                </td>
                <td>
                    <form class="form-inline" action="/vendor-provider/fairylandProductBuffer/increment.do"
                          method="post">
                        <button type="submit"
                                class="btn btn-default">${fairylandProductBufferData.getVersion()}</button>
                    </form>
                </td>
                <td>
                    <form class="form-inline" action="/vendor-provider/fairylandProductBuffer/reload.do"
                          method="post">
                        <button type="submit" class="btn btn-default"><span class="glyphicon glyphicon-refresh"
                                                                            aria-hidden="true"></span></button>
                    </form>
                </td>
            </tr>
            <tr>
                <td>VendorAppsBuffer</td>
                <td>
                    <form class="form-inline" action="/vendor-provider/vendorAppsBuffer/inspect.do"
                          method="get">
                        <button type="submit"
                                class="btn btn-default">${vendorAppsBufferData.getVendorAppsList()?size}</button>
                    </form>
                </td>
                <td>
                    <form class="form-inline" action="/vendor-provider/vendorAppsBuffer/increment.do"
                          method="post">
                        <button type="submit"
                                class="btn btn-default">${vendorAppsBufferData.getVersion()}</button>
                    </form>
                </td>
                <td>
                    <form class="form-inline" action="/vendor-provider/vendorAppsBuffer/reload.do"
                          method="post">
                        <button type="submit" class="btn btn-default"><span class="glyphicon glyphicon-refresh"
                                                                            aria-hidden="true"></span></button>
                    </form>
                </td>
            </tr>
            <tr>
                <td>VendorAppsResgRefBuffer</td>
                <td>
                    <form class="form-inline" action="/vendor-provider/vendorAppsResgRefBuffer/inspect.do"
                          method="get">
                        <button type="submit"
                                class="btn btn-default">${vendorAppsResgRefBufferData.getRefList()?size}</button>
                    </form>
                </td>
                <td>
                    <form class="form-inline" action="/vendor-provider/vendorAppsResgRefBuffer/increment.do"
                          method="post">
                        <button type="submit"
                                class="btn btn-default">${vendorAppsResgRefBufferData.getVersion()}</button>
                    </form>
                </td>
                <td>
                    <form class="form-inline" action="/vendor-provider/vendorAppsResgRefBuffer/reload.do"
                          method="post">
                        <button type="submit" class="btn btn-default"><span class="glyphicon glyphicon-refresh"
                                                                            aria-hidden="true"></span></button>
                    </form>
                </td>
            </tr>
            <tr>
                <td>VendorResgContentBuffer</td>
                <td>
                    <form class="form-inline" action="/vendor-provider/vendorResgContentBuffer/inspect.do"
                          method="get">
                        <button type="submit"
                                class="btn btn-default">${vendorResgContentBufferData.getContentList()?size}</button>
                    </form>
                </td>
                <td>
                    <form class="form-inline" action="/vendor-provider/vendorResgContentBuffer/increment.do"
                          method="post">
                        <button type="submit"
                                class="btn btn-default">${vendorResgContentBufferData.getVersion()}</button>
                    </form>
                </td>
                <td>
                    <form class="form-inline" action="/vendor-provider/vendorResgContentBuffer/reload.do"
                          method="post">
                        <button type="submit" class="btn btn-default"><span class="glyphicon glyphicon-refresh"
                                                                            aria-hidden="true"></span></button>
                    </form>
                </td>
            </tr>
            </tbody>
        </table>
    </div>

</div>

</body>
</html>