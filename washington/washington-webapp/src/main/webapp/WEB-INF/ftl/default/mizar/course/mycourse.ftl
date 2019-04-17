<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='我的课程'
pageJs=["init"]
pageJsFile={"init" : "public/script/mobile/course/main"}
pageCssFile={"css" : ["public/skin/mobile/course/css/skin"]}
bodyClass='bg-f4'>
<div class="courseWrap">
    <#if (courseOrderList?size gt 0)!false>
        <div class="courseTag">我的正式课</div>
        <#list courseOrderList as item>
            <a href="${(item.redirectUrl)!'javascript:;'}" class="courseList JS-logs" data-id="${(item.orderNo)!0}">
                <div class="header">
                    <div class="time">${(item.createTime)!'--'}</div>
                    <div class="orderNum">订单号：${(item.orderNo)!'--'}</div>
                </div>
                <div class="main">
                    <div class="left">
                        <p class="name">${(item.title)!'--'}</p>
                        <p class="price">￥${(item.price)!'--'}元</p>
                    </div>
                    <div class="right">详情</div>
                </div>
            </a>
        </#list>
    </#if>

    <#if (trusteeOrderList?size gt 0)!false>
        <div class="courseTag">我的试听课</div>
        <#list trusteeOrderList as item>
            <a href="${(item.redirectUrl)!'javascript:;'}" class="courseList JS-logs" data-id="${(item.orderNo)!0}">
                <div class="header">
                    <div class="time">${(item.createTime)!'--'}</div>
                    <div class="orderNum">订单号：${(item.orderNo)!'--'}</div>
                </div>
                <div class="main">
                    <div class="left">
                        <p class="name">${(item.title)!'--'}</p>
                        <p class="price">￥${(item.price)!'--'}元</p>
                    </div>
                    <div class="right">详情</div>
                </div>
            </a>
        </#list>
    </#if>

    <#if activityList?? && activityList?size gt 0>
        <div class="courseTag">我的亲子订单</div>
        <#list activityList as active>
            <a href="/mizar/familyactivity/paiddetail.vpage?orderNo=${(active.orderNo)!''}&actId=${(active.actId)!''}" class="courseList">
                <div class="header">
                    <div class="time">${(active.createTime)!''}</div>
                    <div class="orderNum">订单号：${(active.orderNo)!''}</div>
                </div>
                <div class="main">
                    <div class="left">
                        <p class="name">${(active.title)!''}</p>
                        <p class="price">￥${(active.price)!''}元</p>
                    </div>
                    <div class="right">详情</div>
                </div>
            </a>
        </#list>
    </#if>

    <#if (mizarOrderList?size gt 0)!false>
        <div class="courseTag">我的培训班</div>
        <#list mizarOrderList as item>
            <div class="courseList">
                <div class="header">
                    <div class="time">${(item.createTime)!'--'}</div>
                </div>
                <div class="main">
                    <div class="left">
                        <p class="name">${(item.shopName)!'--'}</p>
                    </div>
                    <div class="right state">预约成功</div>
                </div>
            </div>
        </#list>
    </#if>

    <#if ((mizarOrderList?size lt 1)!true) && ((trusteeOrderList?size lt 1)!true) && ((mizarOrderList?size lt 1)!true)>
        <@getContentNull/>
    </#if>
</div>
<#macro getContentNull info="暂无记录！">
    <div style="line-height: 150%; padding: 50px 0 ; text-align: center; color: #bbb; font-size: .625rem;">${info!}</div>
</#macro>

<script type="text/javascript">
    var initMode = "MyCourse";
</script>
</@layout.page>