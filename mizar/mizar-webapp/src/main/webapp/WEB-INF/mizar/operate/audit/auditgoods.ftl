<#import "../../module.ftl" as module>
<@module.page
title="课程变更审核"
pageJsFile={"siteJs" : "public/script/operate/audit"}
pageJs=["siteJs"]
leftMenu="变更审核"
>

<style>
    .input-control>label{float: none; font-weight: bolder;}

</style>

<div class="bread-nav">
    <a class="parent-dir" href="/operate/audit/index.vpage">变更列表</a>
    &gt;
    <a class="current-dir" href="javascript:void(0);" style="cursor: default">课程审核</a>
</div>

<h3 class="h3-title">
    课程变更审核
</h3>
<#if shopId?? && shopName??>
<div id="selected-shop" class="op-wrapper clearfix" style="height: 50px;">
    <div id='' class='shop-label' data-sid='${shopId!}'><label>${shopName!}</label></div>
</div>
</#if>
<div class="op-wrapper clearfix">
    <input value="${gid!}" id="gid" type="hidden">
    <input value="${rid!}" id="rid" type="hidden">

    <div style="float:left; width: 300px; margin-right: 20px;">
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
                （元）
            </span>
        </div>
        <div class="input-control">
            <label>交易成功：</label>
            <span>
                <#if (analysis["dealSuccess"].diff)!false>
                    ${(analysis["dealSuccess"].after)!""}
                    <#if (analysis["dealSuccess"].before)?has_content>
                        <i class="old" title="${(analysis["dealSuccess"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                    ${(analysis["dealSuccess"].before)!""}
                </#if>
            </span>
        </div>

        <div class="input-control">
            <label>总名额：</label>
            <span>
                <#if (analysis["totalLimit"].diff)!false>
                    ${(analysis["totalLimit"].after)!""}
                    <#if (analysis["totalLimit"].before)?has_content>
                        <i class="old" title="${(analysis["totalLimit"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                    ${(analysis["totalLimit"].before)!""}
                </#if>
            </span>
        </div>
        <div class="input-control">
            <label>每天名额：</label>
            <span>
                <#if (analysis["dayLimit"].diff)!false>
                    ${(analysis["dayLimit"].after)!""}
                    <#if (analysis["dayLimit"].before)?has_content>
                        <i class="old" title="${(analysis["dayLimit"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                    ${(analysis["dayLimit"].before)!""}
                </#if>
            </span>
        </div>
        <div class="input-control">
            <label>短信文案：</label>
            <span>
                <#if (analysis["smsMessage"].diff)!false>
                    ${(analysis["smsMessage"].after)!""}
                    <#if (analysis["smsMessage"].before)?has_content>
                        <i class="old" title="${(analysis["smsMessage"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                    ${(analysis["smsMessage"].before)!""}
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
        <div class="input-control">
            <label>年级段：</label>
            <span>
                <#if (analysis["clazzLevel"].diff)!false>
                    ${(analysis["clazzLevel"].after)!""}
                    <#if (analysis["clazzLevel"].before)?has_content>
                        <i class="old" title="${(analysis["clazzLevel"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                    ${(analysis["clazzLevel"].before)!""}
                </#if>
            </span>
        </div>
        <div class="input-control">
            <label>学校：</label>
            <span>
                <#if (analysis["requireSchool"].diff)!false>
                    ${(analysis["requireSchool"].after)!""}
                    <#if (analysis["requireSchool"].before)?has_content>
                        <i class="old" title="${(analysis["requireSchool"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                    ${(analysis["requireSchool"].before)!""}
                </#if>
            </span>
        </div>
        <div class="input-control">
            <label>收货地址：</label>
            <span>
                <#if (analysis["requireAddress"].diff)!false>
                    ${(analysis["requireAddress"].after)!""}
                    <#if (analysis["requireAddress"].before)?has_content>
                        <i class="old" title="${(analysis["requireAddress"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                    ${(analysis["requireAddress"].before)!""}
                </#if>
            </span>
        </div>

        <div class="input-control">
            <label>按钮背景：</label>
            <span>
                <#if (analysis["buttonColor"].diff)!false>
                    ${(analysis["buttonColor"].after)!""}
                    <#if (analysis["buttonColor"].before)?has_content>
                        <i class="old" title="${(analysis["buttonColor"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                    ${(analysis["buttonColor"].before)!""}
                </#if>
            </span>
        </div>
        <div class="input-control">
            <label>按钮文案：</label>
            <span>
                <#if (analysis["buttonText"].diff)!false>
                    ${(analysis["buttonText"].after)!""}
                    <#if (analysis["buttonText"].before)?has_content>
                        <i class="old" title="${(analysis["buttonText"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                    ${(analysis["buttonText"].before)!""}
                </#if>
            </span>
        </div>
        <div class="input-control">
            <label>按钮文色：</label>
            <span>
                <#if (analysis["buttonTextColor"].diff)!false>
                    ${(analysis["buttonTextColor"].after)!""}
                    <#if (analysis["buttonTextColor"].before)?has_content>
                        <i class="old" title="${(analysis["buttonTextColor"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                    ${(analysis["buttonTextColor"].before)!""}
                </#if>
            </span>
        </div>
        <div class="input-control">
            <label>成功文案：</label>
            <span>
                <#if (analysis["successText"].diff)!false>
                    ${(analysis["successText"].after)!""}
                    <#if (analysis["successText"].before)?has_content>
                        <i class="old" title="${(analysis["successText"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                    ${(analysis["successText"].before)!""}
                </#if>
            </span>
        </div>
        <div class="input-control">
            <label>下线文言：</label>
            <span>
                <#if (analysis["offlineText"].diff)!false>
                    ${(analysis["offlineText"].after)!""}
                    <#if (analysis["offlineText"].before)?has_content>
                        <i class="old" title="${(analysis["offlineText"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                    ${(analysis["offlineText"].before)!""}
                </#if>
            </span>
        </div>
        <div class="input-control">
            <label>输入区域背景色：</label>
            <span>
                <#if (analysis["inputBGColor"].diff)!false>
                    ${(analysis["inputBGColor"].after)!""}
                    <#if (analysis["inputBGColor"].before)?has_content>
                        <i class="old" title="${(analysis["inputBGColor"].before)!""}">旧</i><#else><i class="new">新</i>
                    </#if>
                <#else>
                    ${(analysis["inputBGColor"].before)!""}
                </#if>
            </span>
        </div>

    </div>
    <div class="clearfix" style="clear:both;">
        <div class="input-control">
            <label>
                顶部图片：
                <#if (analysis["topImage"].diff)!false>
                    <#if (analysis["topImage"].before)?has_content>
                        <a class="detailHistoryBtn" data-detail="${(analysis["topImage"].before)!}" href="javascript:void (0);" style="color: #00b7ee">查看历史</a>
                    </#if>
                </#if>
            </label>
            <div class="image-preview clearfix">
                <input id="detail-img" type="hidden" name="detailImg" />
                <#if (analysis["topImage"].after)??>
                   <div class="image"><img src="${analysis["topImage"].after}" /></div>
                <#else>
                    <#if (analysis["topImage"].before)??>
                        <div class="image"><img src="${analysis["topImage"].before}" /></div>
                    </#if>
                </#if>
            </div>
        </div>
    </div>
    <div class="clearfix" style="clear:both;">
        <div class="input-control">
            <label>
                banner图：
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
    <#if available!false>
    <div class="clearfix submit-box">
        <a id="approve-btn" class="submit-btn approve-btn" href="javascript:void(0)">审核通过</a>
        <a id="reject-btn" class="submit-btn reject-btn" href="javascript:void(0);">驳回申请</a>
    </div>
    </#if>
</div>
</@module.page>