<#-- @ftlvariable name="vendorAppsBufferData" type="com.voxlearning.utopia.service.vendor.api.mapper.VersionedVendorAppsList" -->
<!--suppress HtmlUnknownTarget -->
<html>
<head>
    <title>VendorAppsBuffer</title>
</head>
<body>
<h3><a class="btn btn-default" href="/vendor-provider/index.do" role="button">VENDOR PROVIDER</a></h3>

<div class="container-fluid">

    <div class="row">
        <table class="table table-bordered table-condensed table-striped">
            <thead>
            <tr class="info">
                <td colspan="40"><strong>VendorAppsBuffer
                    (version=${vendorAppsBufferData.getVersion()})</strong></td>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td><strong>ID</strong></td>
                <td><strong>VENDOR_ID</strong></td>
                <td><strong>CNAME</strong></td>
                <td><strong>ENAME</strong></td>
                <td><strong>SHORT_NAME</strong></td>
                <td><strong>VITALITY_TYPE</strong></td>
                <td><strong>INTEGRAL_TYPE</strong></td>
                <td><strong>APP_URL</strong></td>
                <td><strong>APP_ICON</strong></td>
                <td><strong>APP_KEY</strong></td>
                <td><strong>SECRET_KEY</strong></td>
                <td><strong>CALLBACK_URL</strong></td>
                <td><strong>PURCHASE_URL</strong></td>
                <td><strong>DAY_MAX_ACCESS</strong></td>
                <td><strong>DAY_MAX_ADD_PK</strong></td>
                <td><strong>DAY_MAX_ADD_INTEGRAL</strong></td>
                <td><strong>STATUS</strong></td>
                <td><strong>DISABLED</strong></td>
                <td><strong>CREATE_DATETIME</strong></td>
                <td><strong>UPDATE_DATETIME</strong></td>
                <td><strong>RUNTIME_MODE</strong></td>
                <td><strong>SERVER_IPS</strong></td>
                <td><strong>SUSPEND_MESSAGE</strong></td>
                <td><strong>RANK</strong></td>
                <td><strong>IS_PAYMENT_FREE</strong></td>
                <td><strong>IS_EDU_APP</strong></td>
                <td><strong>PLAY_SOURCES</strong></td>
                <td><strong>APPM_URL</strong></td>
                <td><strong>VERSION</strong></td>
                <td><strong>APPM_ICON</strong></td>
                <td><strong>WECHAT_BUY_FLAG</strong></td>
                <td><strong>SUBHEAD</strong></td>
                <td><strong>ORIENTATION</strong></td>
                <td><strong>CLAZZ_LEVEL</strong></td>
                <td><strong>DESCRIPTION</strong></td>
                <td><strong>VIRTUAL_ITEM_EXIST</strong></td>
                <td><strong>BROWSER</strong></td>
                <td><strong>VERSION_ANDROID</strong></td>
                <td><strong>IOS_PARENT_VERSION</strong></td>
                <td><strong>ANDROID_PARENT_VERSION</strong></td>
            </tr>
            <#list vendorAppsBufferData.getVendorAppsList() as c>
            <tr>
                <td>${c.id!''}</td>
                <td>${c.vendorId!''}</td>
                <td>${c.cname!''}</td>
                <td>${c.ename!''}</td>
                <td>${c.shortName!''}</td>
                <td>${c.vitalityType!''}</td>
                <td>${c.integralType!''}</td>
                <td>${c.appUrl!''}</td>
                <td>${c.appIcon!''}</td>
                <td>${c.appKey!''}</td>
                <td>${c.secretKey!''}</td>
                <td>${c.callBackUrl!''}</td>
                <td>${c.purchaseUrl!''}</td>
                <td>${c.dayMaxAccess!''}</td>
                <td>${c.dayMaxAddPK!''}</td>
                <td>${c.dayMaxAddIntegral!''}</td>
                <td>${c.status!''}</td>
                <td>${c.getDisabled()?c}</td>
                <td>${c.createDatetime?datetime}</td>
                <td>${c.updateDatetime?datetime}</td>
                <td>${c.runtimeMode!''}</td>
                <td>${c.serverIps!''}</td>
                <td>${c.suspendMessage!''}</td>
                <td>${c.rank!''}</td>
                <td>${c.getIsPaymentFree()?c}</td>
                <td>${c.getIsEduApp()?c}</td>
                <td>${c.playSources!''}</td>
                <td>${c.appmUrl!''}</td>
                <td>${c.version!''}</td>
                <td>${c.appmIcon!''}</td>
                <td>${c.getWechatBuyFlag()?c}</td>
                <td>${c.subhead!''}</td>
                <td>${c.orientation!''}</td>
                <td>${c.clazzLevel!''}</td>
                <td>${c.description!''}</td>
                <td>${c.getVirtualItemExist()?c}</td>
                <td>${c.browser!''}</td>
                <td>${c.versionAndroid!''}</td>
                <td>${c.iosParentVersion!''}</td>
                <td>${c.androidParentVersion!''}</td>
            </tr>
            </#list>
            </tbody>
        </table>
    </div>

</div>

</body>
</html>