<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title='值得买'
bodyClass='bgB8'
pageJs=["jquery","voxLogs"]
pageCssFile={"index" : ["public/skin/mobile/groupon/css/index"]}>

<#include "../mizar/function.ftl"/>
<div class="specialTopic-box">
    <#if specialTopic?? && specialTopic?size gt 0>
        <div class="spt-banner">
            <img src="${pressImageAutoW((specialTopic.detailImg)!,640)}" alt="">
        </div>
    </#if>

    <#assign specialTag = {
    'hot': {'name': 'hot','color': 'tag-hot'},
    'new': {'name': 'new','color': 'tag-new'},
    'postFree': {'name': '包邮','color': 'tag-send'},
    'promotions': {'name': '促销','color': 'tag-sale'} }/>

    <div class="spt-main">
        <ul class="spt-list">
            <#if grouponGoodsList?? && grouponGoodsList?size gt 0>
                <#list grouponGoodsList as group>
                    <li onclick="location.href='/groupon/goodsdetail.vpage?goodsId=${group.id!''}'">
                        <div class="listImage">
                            <img src="${pressImageAutoW((group.image)!,224)}" alt="">
                        </div>
                        <div class="listDown">
                            <div class="bookTitle">${(group.shortTitle)!''}</div>
                            <div class="price">
                                <#if (group.price)?has_content>
                                    ¥
                                </#if>
                                ${(group.price)!''}
                            </div>
                            <#if group.specialTag?has_content>
                                <#list group.specialTag?split(',') as gr>
                                    <span class="bookTag ${(specialTag[gr].color)!}">${(specialTag[gr].name)!}</span>
                                </#list>
                            </#if>
                        </div>
                    </li>
                </#list>
            <#else>
                <li style="width: 100%">
                    <div style="text-align: center;">暂无数据</div>
                </li>
            </#if>
        </ul>
    </div>
</div>
<script type="text/javascript">
    signRunScript = function () {
        YQ.voxLogs({
            database: 'parent',
            module: 'm_sMNiwxrS',
            op: "o_2h4qcDOG",
            s0: "${(specialTopic.name)!''}"
        });
    };
</script>
</@layout.page>