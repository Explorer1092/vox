<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title="${(activity.title)!''}"
pageJs=['init']
pageJsFile={"init" : "public/script/mobile/seattle/main"}
pageCssFile={"css" : ["public/skin/mobile/seattle/css/pay"]}
>
<div data-bind="template: {name : 'T:孩子列表', data: getStudentList()}"></div>

<#if (activity.innerUrl?lower_case)?index_of(".jpg") gt -1 || (activity.innerUrl?lower_case)?index_of(".png") gt -1 || (activity.innerUrl?lower_case)?index_of(".gif") gt -1>
    <img src="${(activity.innerUrl)!}" width="100%">
<#else>
    <iframe src="${(activity.innerUrl)!}" height="600" width="100%" id="vox17zuoyeIframe" frameborder="0" scrolling="yes"></iframe>
</#if>

<div data-bind="template: {name : 'T:status', data: database}"></div>

<script type="text/html" id="T:孩子列表">
    <div class="tma-head headDiffer" data-bind="visible: $data.length > 1" id="studentListBox">
        <div class="tma-inner">
            <ul data-bind="foreach: $data">
                <li data-bind="css: { active: $root.SID() == id}">
                    <!-- ko if : $root.SID() == null && $index() == 0-->
                    <span data-bind="click: $root.selectStudent($data, $element)"></span>
                    <!-- /ko -->
                    <a href="javascript:;" data-bind="click: $root.selectStudent">
                        <div class="tma-image">
                            <!-- ko if : img -->
                            <img src="" data-bind="attr: {src: img}" style="border-radius: 50%; overflow: hidden;"/>
                            <!-- /ko -->
                            <!-- ko ifnot : img -->
                            <img src="<@app.link href="public/skin/parentMobile/learning/images/tool03.png"/>">
                            <!-- /ko -->
                            <!-- ko if : $root.SID() != id -->
                            <em class="tma-mask"></em>
                            <!-- /ko -->
                        </div>
                        <div class="tma-name" data-bind="text: name">--</div>
                    </a>
                </li>
            </ul>
        </div>
    </div>
</script>

<#switch activity.activityType>
    <#case "Pay">
        <#assign activityHref="paydetail.vpage?activityId="+activity.id activityText="立即支付" successText="您已成功购买"/>
        <#break>
    <#case "Reserve">
        <#assign activityHref="reserve.vpage?activityId="+activity.id activityText="立即报名" successText="您已成功报名"/>
        <#break>
    <#case "joinGroup">
        <#assign activityHref="#" activityText="立即加群" successText=""/>
        <#break>
    <#case "Subscribe">
        <#assign activityHref="#" activityText="立即预约"/>
        <#break>
    <#default>
</#switch>

<script type="text/html" id="T:status">
    <div class="thematicTitle-box">
        <div class="footer">
            <div class="inner" id="footerBox">
            <!-- ko if : isNotBuy -->
                <#if (activity.successBarContent)?has_content>
                    <a href="${(activity.returnUrl)!}" class="w-orderedBtn w-btn-green">${(activity.successBarContent)!'查看详情'}</a>
                </#if>
                <div class="tma-title">${(activity.successContent)!}</div>
            <!-- /ko -->

            <!-- ko if : !isNotBuy -->
                <a href="javascript:;" data-bind="attr: {'href': '${activityHref!}&sid=' + sid + '&track=' + $root.track}" class="w-orderedBtn JS-indexSubmit" data-id="${(activity.id)!}" data-type="${(activity.activityType)!'pay'}" data-qcode="${(activity.qcode)!}" data-qname="${(activity.qname)!}">${activityText!}</a>
                <div class="tma-title">${(activity.barContent)!}</div>
            <!-- /ko -->
            </div>
        </div>
    </div>
</script>

<#if ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv()>
    <#assign weChatLinkHost = "//wechat.test.17zuoye.net">
<#elseif ProductDevelopment.isStagingEnv()>
    <#assign weChatLinkHost = "//wechat.staging.17zuoye.net">
<#elseif ProductDevelopment.isProductionEnv()>
    <#assign weChatLinkHost = "//wechat.17zuoye.com">
</#if>
<script type="text/javascript">
    var activityType = "${(activity.activityType)!'pay'}";
    var activityId = "${(activity.id)!0}";
    var weChatLinkHost = "${weChatLinkHost!}";
</script>
</@layout.page>
