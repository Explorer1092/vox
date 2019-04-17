<#-- @ftlvariable name="fairylandProductBufferData" type="com.voxlearning.utopia.service.vendor.api.mapper.VersionedFairylandProducts" -->
<!--suppress HtmlUnknownTarget -->
<html>
<head>
    <title>FairylandProductBuffer</title>
</head>
<body>
<h3><a class="btn btn-default" href="/vendor-provider/index.do" role="button">VENDOR PROVIDER</a></h3>

<div class="container-fluid">

    <div class="row">
        <table class="table table-bordered table-condensed table-striped">
            <thead>
            <tr class="info">
                <td colspan="29"><strong>FairylandProductBuffer
                    (version=${fairylandProductBufferData.getVersion()})</strong></td>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td><strong>ID</strong></td>
                <td><strong>PLATFORM</strong></td>
                <td><strong>PRODUCT_TYPE</strong></td>
                <td><strong>APP_KEY</strong></td>
                <td><strong>PRODUCT_NAME</strong></td>
                <td><strong>PRODUCT_DESC</strong></td>
                <td><strong>PRODUCT_ICON</strong></td>
                <td><strong>PRODUCT_RECT_ICON</strong></td>
                <td><strong>BACKGROUND_IMAGE</strong></td>
                <td><strong>LAUNCH_URL</strong></td>
                <td><strong>LAUNCH_BTN_TEXT</strong></td>
                <td><strong>STATUS</strong></td>
                <td><strong>SUSPEND_MESSAGE</strong></td>
                <td><strong>RANK</strong></td>
                <td><strong>OPERATION_MESSAGE</strong></td>
                <td><strong>HOT_FLAG</strong></td>
                <td><strong>NEW_FLAG</strong></td>
                <td><strong>CREATE_DATETIME</strong></td>
                <td><strong>UPDATE_DATETIME</strong></td>
                <td><strong>DISABLED</strong></td>
                <td><strong>USE_PLATFORM_DESC</strong></td>
                <td><strong>BASE_USING_NUM</strong></td>
                <td><strong>REDIRECT_TYPE</strong></td>
                <td><strong>RECOMMEND_FLAG</strong></td>
                <td><strong>CATALOG_DESC</strong></td>
                <td><strong>STAGING_LAUNCH_URL</strong></td>
                <td><strong>BANNER_IMAGE</strong></td>
                <td><strong>DESC_IMAGE</strong></td>
                <td><strong>PROMPT_MESSAGE</strong></td>
            </tr>
            <#list fairylandProductBufferData.getFairylandProducts() as c>
            <tr>
                <td>${c.id!''}</td>
                <td>${c.platform!''}</td>
                <td>${c.productType!''}</td>
                <td>${c.appKey!''}</td>
                <td>${c.productName!''}</td>
                <td>${c.productDesc!''}</td>
                <td>${c.productIcon!''}</td>
                <td>${c.productRectIcon!''}</td>
                <td>${c.backgroundImage!''}</td>
                <td>${c.launchUrl!''}</td>
                <td>${c.launchBtnText!''}</td>
                <td>${c.status!''}</td>
                <td>${c.suspendMessage!''}</td>
                <td>${c.rank!''}</td>
                <td>${c.operationMessage!''}</td>
                <td>${c.hotFlag?c}</td>
                <td>${c.newFlag?c}</td>
                <td>${c.createDatetime?datetime}</td>
                <td>${c.updateDatetime?datetime}</td>
                <td>${c.disabled?c}</td>
                <td>${c.usePlatformDesc!''}</td>
                <td>${c.baseUsingNum!''}</td>
                <td>
                    <#if c.redirectType??>
                        ${c.redirectType.name()}
                    </#if>
                </td>
                <td>${c.recommendFlag?c}</td>
                <td>${c.catalogDesc!''}</td>
                <td>${c.stagingLaunchUrl!''}</td>
                <td>${c.bannerImage!''}</td>
                <td>${c.descImage!''}</td>
                <td>${c.promptMessage!''}</td>
            </tr>
            </#list>
            </tbody>
        </table>
    </div>

</div>

</body>
</html>