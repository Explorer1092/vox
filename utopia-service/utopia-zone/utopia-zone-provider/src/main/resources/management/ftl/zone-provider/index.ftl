<#-- @ftlvariable name="zoneEventTypes" type="java.util.List<com.voxlearning.utopia.queue.zone.ZoneEventType>" -->
<#-- @ftlvariable name="zoneEventStatistics" type="com.voxlearning.utopia.service.zone.impl.listener.ZoneEventStatistics" -->
<#-- @ftlvariable name="userMoodBufferData" type="com.voxlearning.utopia.service.zone.data.VersionedUserMoodData" -->
<#-- @ftlvariable name="giftBufferData" type="com.voxlearning.utopia.service.zone.data.VersionedGiftData" -->
<#-- @ftlvariable name="clazzZoneProductBufferData" type="com.voxlearning.utopia.service.zone.data.VersionedClazzZoneProductData" -->
<!--suppress HtmlUnknownTarget -->
<html>
<head>
    <title>ZONE PROVIDER</title>
</head>
<body>
<h3><a class="btn btn-default" href="/index.do" role="button">DASHBOARD</a></h3>

<div class="container-fluid">

    <div class="row">
        <table class="table table-bordered table-condensed table-striped">
            <thead>
            <tr class="info">
                <td colspan="2"><strong>ZONE QUEUE LISTENER</strong></td>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td><strong>EventType</strong></td>
                <td><strong>ReceivedEvent</strong></td>
            </tr>
            <#list zoneEventTypes as t>
            <tr>
                <td>${t.name()}</td>
                <td>${zoneEventStatistics.sum(t)}</td>
            </tr>
            </#list>
            </tbody>
        </table>
    </div>

    <div class="row">
        <table class="table table-bordered table-condensed table-striped">
            <thead>
            <tr class="info">
                <td colspan="4"><strong>ZONE PROVIDER BUFFER(S)</strong></td>
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
                <td>UserMoodBuffer</td>
                <td>
                    <form class="form-inline" action="/zone-provider/userMoodBuffer/inspect.do"
                          method="get">
                        <button type="submit"
                                class="btn btn-default">${userMoodBufferData.getUserMoodList()?size}</button>
                    </form>
                </td>
                <td>
                    <form class="form-inline" action="/zone-provider/userMoodBuffer/increment.do"
                          method="post">
                        <button type="submit"
                                class="btn btn-default">${userMoodBufferData.getVersion()}</button>
                    </form>
                </td>
                <td>
                    <form class="form-inline" action="/zone-provider/userMoodBuffer/reload.do"
                          method="post">
                        <button type="submit" class="btn btn-default"><span class="glyphicon glyphicon-refresh"
                                                                            aria-hidden="true"></span></button>
                    </form>
                </td>
            </tr>
            <tr>
                <td>GiftBuffer</td>
                <td>
                    <form class="form-inline" action="/zone-provider/giftBuffer/inspect.do"
                          method="get">
                        <button type="submit"
                                class="btn btn-default">${giftBufferData.getGiftList()?size}</button>
                    </form>
                </td>
                <td>
                    <form class="form-inline" action="/zone-provider/giftBuffer/increment.do"
                          method="post">
                        <button type="submit"
                                class="btn btn-default">${giftBufferData.getVersion()}</button>
                    </form>
                </td>
                <td>
                    <form class="form-inline" action="/zone-provider/giftBuffer/reload.do"
                          method="post">
                        <button type="submit" class="btn btn-default"><span class="glyphicon glyphicon-refresh"
                                                                            aria-hidden="true"></span></button>
                    </form>
                </td>
            </tr>
            <tr>
                <td>ClazzZoneProductBuffer</td>
                <td>
                    <form class="form-inline" action="/zone-provider/clazzZoneProductBuffer/inspect.do"
                          method="get">
                        <button type="submit"
                                class="btn btn-default">${clazzZoneProductBufferData.getClazzZoneProductList()?size}</button>
                    </form>
                </td>
                <td>
                    <form class="form-inline" action="/zone-provider/clazzZoneProductBuffer/increment.do"
                          method="post">
                        <button type="submit"
                                class="btn btn-default">${clazzZoneProductBufferData.getVersion()}</button>
                    </form>
                </td>
                <td>
                    <form class="form-inline" action="/zone-provider/clazzZoneProductBuffer/reload.do"
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
