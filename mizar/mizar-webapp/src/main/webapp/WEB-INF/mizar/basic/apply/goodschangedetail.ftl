<#import "../../module.ftl" as module>
<@module.page
title="机构变更详情"
pageJsFile={"siteJs" : "public/script/basic/apply"}
pageJs=["siteJs"]
leftMenu="我的申请"
>
<style>
    .input-control>label{float: none; font-weight: bolder;}
</style>

<div class="bread-nav">
    <a class="parent-dir" href="/basic/apply/index.vpage">我的申请</a>
    &gt;
    <a class="current-dir" href="javascript:void(0);" style="cursor: default">课程变更详情</a>
</div>
<h3 class="h3-title">
    课程变更详情
</h3>
<#if shopId?? && shopName??>
    <div id="selected-shop" class="op-wrapper clearfix" style="height: 50px;">
        <div id='' class='shop-label' data-sid='${shopId!}'><label>${shopName!}</label></div>
    </div>
</#if>
<div class="op-wrapper clearfix">
    <div style="float:left; width: 300px; margin-right: 20px;">
        <div class="input-control">
            <label>课程ID：</label>
            <span>
                <#if (analysis["id"].diff)!false>
                ${(analysis["id"].after)!"新增课程"}
                    <#if (analysis["id"].before)?has_content>
                        <i class="old" title="${(analysis["id"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                ${(analysis["id"].before)!""}
                </#if>
            </span>

        </div>

        <div class="input-control">
            <label>课程名称：</label>
            <span>
                <#if (analysis["goodsName"].diff)!false>
                ${(analysis["goodsName"].after)!""}
                    <#if (analysis["goodsName"].before)?has_content>
                        <i class="old" title="${(analysis["goodsName"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                ${(analysis["goodsName"].before)!""}
                </#if>
            </span>
        </div>

        <div class="input-control">
            <label>课程简介：</label>
            <span>
                <#if (analysis["desc"].diff)!false>
                ${(analysis["desc"].after)!""}
                    <#if (analysis["desc"].before)?has_content>
                        <i class="old" title="${(analysis["desc"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                ${(analysis["desc"].before)!""}
                </#if>
            </span>
        </div>

        <div class="input-control">
            <label>上课时间：</label>
            <span>
                <#if (analysis["goodsTime"].diff)!false>
                ${(analysis["goodsTime"].after)!""}
                    <#if (analysis["goodsTime"].before)?has_content>
                        <i class="old" title="${(analysis["goodsTime"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                ${(analysis["goodsTime"].before)!""}
                </#if>
            </span>

        </div>

        <div class="input-control">
            <label>课程标签：</label>
            <span>
                <#if (analysis["tags"].diff)!false>
                ${(analysis["tags"].after?join(' , '))!''}
                    <#if (analysis["tags"].before)?has_content>
                        <i class="old" title="${(analysis["tags"].before?join(' , '))!''}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                ${(analysis["tags"].before?join(' , '))!''}
                </#if>
            </span>
        </div>

        <div class="input-control">
            <label>到店礼：</label>
            <span>
                <#if (analysis["welcomeGift"].diff)!false>
                ${(analysis["welcomeGift"].after)!""}
                    <#if (analysis["welcomeGift"].before)?has_content>
                        <i class="old" title="${(analysis["welcomeGift"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                ${(analysis["welcomeGift"].before)!""}
                </#if>
            </span>
        </div>
        <div class="input-control">
            <label>跳转链接：</label>
            <span>
                <#if (analysis["redirectUrl"].diff)!false>
                ${(analysis["redirectUrl"].after)!""}
                    <#if (analysis["redirectUrl"].before)?has_content>
                        <i class="old" title="${(analysis["redirectUrl"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                ${(analysis["redirectUrl"].before)!""}
                </#if>
            </span>
        </div>
    </div>

    <div style="float:left;width: 480px;">
        <div class="input-control">
            <label>课程标题：</label>
            <span style="width: 100px">
                <#if (analysis["title"].diff)!false>
                ${(analysis["title"].after)!""}
                    <#if (analysis["title"].before)?has_content>
                        <i class="old" title="${(analysis["title"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                ${(analysis["title"].before)!""}
                </#if>
            </span>

        </div>
        <div class="input-control">
            <label>课程价格：</label>
            <span>
                <#if (analysis["price"].diff)!false>
                ${(analysis["price"].after)!""}
                    <#if (analysis["price"].before)?has_content>
                        <i class="old" title="${(analysis["price"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                ${(analysis["price"].before)!""}
                </#if>
            </span>
        </div>

        <div class="input-control">
            <label>课时：</label>
            <span>
                <#if (analysis["goodsHours"].diff)!false>
                ${(analysis["goodsHours"].after)!""}
                    <#if (analysis["goodsHours"].before)?has_content>
                        <i class="old" title="${(analysis["goodsHours"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                ${(analysis["goodsHours"].before)!""}
                </#if>
            </span>
        </div>
        <div class="input-control">
            <label>时长：</label>
            <span>
                <#if (analysis["duration"].diff)!false>
                ${(analysis["duration"].after)!""}
                    <#if (analysis["duration"].before)?has_content>
                        <i class="old" title="${(analysis["duration"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                ${(analysis["duration"].before)!""}
                </#if>
            </span>
        </div>
        <div class="input-control">
            <label>年龄段：</label>
            <span>
                <#if (analysis["target"].diff)!false>
                ${(analysis["target"].after)!""}
                    <#if (analysis["target"].before)?has_content>
                        <i class="old" title="${(analysis["target"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                ${(analysis["target"].before)!""}
                </#if>
            </span>

        </div>
        <div class="input-control clearfix">
            <label>试听：</label>
            <span>
                <#if (analysis["audition"].diff)!false>
                ${(analysis["audition"].after)!""}
                    <#if (analysis["audition"].before)?has_content>
                        <i class="old" title="${(analysis["audition"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                ${(analysis["audition"].before)!""}
                </#if>
            </span>

        </div>
        <div class="input-control">
            <label>分类：</label>
            <span>
                <#if (analysis["category"].diff)!false>
                ${(analysis["category"].after)!""}
                    <#if (analysis["category"].before)?has_content>
                        <i class="old" title="${(analysis["category"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                ${(analysis["category"].before)!""}
                </#if>
            </span>
        </div>
        <div class="input-control">
            <label>预约礼：</label>
            <span>
                <#if (analysis["appointGift"].diff)!false>
                ${(analysis["appointGift"].after)!""}
                    <#if (analysis["appointGift"].before)?has_content>
                        <i class="old" title="${(analysis["appointGift"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                ${(analysis["appointGift"].before)!""}
                </#if>
            </span>
        </div>
        <div class="input-control">
            <label>是否推荐：</label>
            <span>
                <#if (analysis["recommended"].diff)!false>
                    <#if (analysis["recommended"].after)??>${(analysis["recommended"].after)?string("是","否")}
                        <#if (analysis["recommended"].before)?has_content>
                            <i class="old" title="${(analysis["recommended"].before)?string("ss","ss")}">旧</i><#else><i class="new">新</i>
                        </#if>
                    </#if>
                <#else>
                    <#if (analysis["recommended"].before)??> ${(analysis["recommended"].before)?string("是","否")} </#if>
                </#if>
            </span>

        </div>
    </div>
    <div class="clearfix" style="clear:both;">
        <div class="input-control">
            <label>
                课程头图：
                <#if (analysis["bannerPhoto"].diff)!false>
                    <#if (analysis["bannerPhoto"].before)?has_content>
                        <a class="detailHistoryBtn" data-detail="${(analysis["bannerPhoto"].before)!?join(',')}" href="javascript:void (0);" style="color: #00b7ee">查看历史</a>
                    </#if>
                </#if>
            </label>
            <div class="image-preview clearfix">
                <input id="detail-img" type="hidden" name="detailImg" />
                <#if (analysis["bannerPhoto"].after)?? && analysis["bannerPhoto"].after?size gt 0>
                    <#list analysis["bannerPhoto"].after as imgUrl>
                        <div class="image"><img src="${imgUrl}" /></div>
                    </#list>
                <#else>
                    <#if (analysis["bannerPhoto"].before)?? && analysis["bannerPhoto"].before?size gt 0>
                        <#list analysis["bannerPhoto"].before as imgUrl>
                            <div class="image"><img src="${imgUrl}" /></div>
                        </#list>
                    </#if>
                </#if>
            </div>
        </div>
    </div>
    <div class="clearfix" style="clear:both;">
        <div class="input-control">
            <label>
                课程详情：
                <#if (analysis["detail"].diff)!false>
                    <#if (analysis["detail"].before)?has_content>
                        <a class="detailHistoryBtn" data-detail="${(analysis["detail"].before)!?join(',')}" href="javascript:void (0);" style="color: #00b7ee">查看历史</a>
                    </#if>
                </#if>
            </label>
            <div class="image-preview clearfix">
                <input id="detail-img" type="hidden" name="detailImg" />
                <#if (analysis["detail"].after)?? && analysis["detail"].after?size gt 0>
                    <#list analysis["detail"].after as imgUrl>
                        <div class="image"><img src="${imgUrl}" /></div>
                    </#list>
                <#else>
                    <#if (analysis["detail"].before)?? && analysis["detail"].before?size gt 0>
                        <#list analysis["detail"].before as imgUrl>
                            <div class="image"><img src="${imgUrl}" /></div>
                        </#list>
                    </#if>
                </#if>
            </div>
        </div>
    </div>
</div>
<div class="clearfix submit-box">
    <a class="submit-btn abandon-btn " href="/basic/apply/index.vpage">返回</a>
</div>
</@module.page>