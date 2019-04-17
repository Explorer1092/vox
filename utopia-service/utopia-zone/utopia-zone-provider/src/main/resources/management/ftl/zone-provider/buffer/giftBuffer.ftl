<#-- @ftlvariable name="giftBufferData" type="com.voxlearning.utopia.service.zone.data.VersionedGiftData" -->
<!--suppress HtmlUnknownTarget -->
<html>
<head>
    <title>GiftBuffer</title>
</head>
<body>
<h3><a class="btn btn-default" href="/zone-provider/index.do" role="button">ZONE PROVIDER</a></h3>

<div class="container-fluid">

    <div class="row">
        <table class="table table-bordered table-condensed table-striped">
            <thead>
            <tr class="info">
                <td colspan="11"><strong>GiftBuffer(version=${giftBufferData.getVersion()})</strong></td>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td><strong>ID</strong></td>
                <td><strong>DISABLED</strong></td>
                <td><strong>CREATE_DATETIME</strong></td>
                <td><strong>UPDATE_DATETIME</strong></td>
                <td><strong>NAME</strong></td>
                <td><strong>GOLD</strong></td>
                <td><strong>SILVER</strong></td>
                <td><strong>IMG_URL</strong></td>
                <td><strong>GIFT_CATEGORY</strong></td>
                <td><strong>STUDENT_AVAILABLE</strong></td>
                <td><strong>TEACHER_AVAILABLE</strong></td>
            </tr>
            <#list giftBufferData.getGiftList() as c>
            <tr>
                <td>${c.id}</td>
                <td>${c.getDisabled()?c}</td>
                <td>${c.createDatetime?datetime}</td>
                <td>${c.updateDatetime?datetime}</td>
                <td>${c.name}</td>
                <td>${c.gold!''}</td>
                <td>${c.silver!''}</td>
                <td>${c.imgUrl}</td>
                <td>${c.giftCategory.name()}</td>
                <td>${c.getStudentAvailable()?c}</td>
                <td>${c.getTeacherAvailable()?c}</td>
            </tr>
            </#list>
            </tbody>
        </table>
    </div>

</div>

</body>
</html>