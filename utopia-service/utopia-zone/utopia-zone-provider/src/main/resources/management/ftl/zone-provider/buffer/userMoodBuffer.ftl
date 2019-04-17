<#-- @ftlvariable name="userMoodBufferData" type="com.voxlearning.utopia.service.zone.data.VersionedUserMoodData" -->
<!--suppress HtmlUnknownTarget -->
<html>
<head>
    <title>UserMoodBuffer</title>
</head>
<body>
<h3><a class="btn btn-default" href="/zone-provider/index.do" role="button">ZONE PROVIDER</a></h3>

<div class="container-fluid">

    <div class="row">
        <table class="table table-bordered table-condensed table-striped">
            <thead>
            <tr class="info">
                <td colspan="5"><strong>UserMoodBuffer(version=${userMoodBufferData.getVersion()})</strong></td>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td><strong>ID</strong></td>
                <td><strong>CREATE_DATETIME</strong></td>
                <td><strong>TITLE</strong></td>
                <td><strong>DESCRIPTION</strong></td>
                <td><strong>IMG_URL</strong></td>
            </tr>
            <#list userMoodBufferData.getUserMoodList() as c>
            <tr>
                <td>${c.id}</td>
                <td>${c.createDatetime?datetime}</td>
                <td>${c.title}</td>
                <td>${c.description}</td>
                <td>${c.imgUrl}</td>
            </tr>
            </#list>
            </tbody>
        </table>
    </div>

</div>

</body>
</html>