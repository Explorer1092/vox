<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='口碑机构'
pageJs=["mechanismList"]
pageJsFile={"mechanismList" : "public/script/mobile/mizar/mechanismList"}
pageCssFile={"remarkIndex" : ["public/skin/mobile/mizar/css/remark"]}>

<#if nearMap?? && nearMap?size gt 0>
    <#list nearMap?keys as key>
        <div class="vrk-top vrk-top-1">${key!}</div>
        <div class="aeg-top topMar">
            <#list nearMap[key] as list>
                <dl>
                    <dt class="aeg-num"><span class="rankNum rankNum${list.rank!}">${list.rank!}</span></dt>
                    <dt class="aeg-img"><img src="${list.shopImg!}" alt=""></dt>
                    <dd class="aeg-side">
                        <div class="head">${list.shopName!}</div>
                        <div class="starBg">
                            <#list 1..5 as star>
                                <a href="javascript:void(0);" <#if star_index+1 lte (list.ratingStar)!0>class="cliBg"</#if> ></a>
                            </#list>

                            <span>${(list.ratingCount)!0}条</span>
                        </div>
                        <div class="ordered-btn">
                            <a data-bind="click: subscribeBtn.bind($data,'${list.shopId!}','${key!}')" href="javascript:void (0);" class="w-orderedBtn">点击预约</a>
                        </div>
                        <div class="aeg-apart">
                        ${list.tradeArea!}
                            <#if list.distance?has_content>
                                <span>${list.distance?string("#.##")!}km</span>
                            </#if>
                        </div>
                    </dd>
                </dl>
            </#list>
        </div>
        <a href="javascript:void(0);" class="w-btn-more" data-bind="click: showMoreBtn">更多</a>
    </#list>
<#else >
<div class="vrk-top vrk-top-1">暂无数据</div>
</#if>


</@layout.page>