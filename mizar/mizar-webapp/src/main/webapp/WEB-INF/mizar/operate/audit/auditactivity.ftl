<#import "../../module.ftl" as module>
<@module.page
title="活动变更审核"
pageJsFile={"siteJs" : "public/script/operate/audit"}
pageJs=["siteJs"]
leftMenu="变更审核"
>

<#macro diffShow analysisBean  name >
    <#if ((analysisBean[name].diff)!false)>
        <#if (analysisBean[name].before)??>
        <i class="old" title="${(analysisBean[name].before)!""}">旧</i>
        <#else>
        <i class="new">新</i>
        </#if>
    ${(analysisBean[name].after)!""}
    <#else>
    ${(analysisBean[name].before)!""}
    </#if>
</#macro>
<style>
    .input-control>label{float: none; font-weight: bolder;}
</style>
<script type="text/javascript" src="https://webapi.amap.com/maps?v=1.3&key=ae2ed07e893eb41d259a47e7ba258c00&plugin=AMap.Geocoder"></script>

<div class="bread-nav">
    <a class="parent-dir" href="/operate/audit/index.vpage">变更列表</a>
    &gt;
    <a class="current-dir" href="javascript:void(0);" style="cursor: default">亲子活动</a>
</div>

<h3 class="h3-title">
    亲子活动变更审核
</h3>

<div class="op-wrapper clearfix">
    <input value="${gid!}" id="gid" type="hidden">
    <input value="${rid!}" id="rid" type="hidden">
    <div style="float:left; width: 360px; margin-right: 20px;">
        <div class="input-control">
            <label>活动ID：
                <span>
                    <#if (analysis["id"].diff)!false>
                    ${(analysis["id"].after)!"新增活动"}
                        <#if (analysis["id"].before)?has_content>
                            <i class="old" title="${(analysis["id"].before)!""}">旧</i><#else><i class="new">新</i>
                        </#if>
                    <#else>
                    ${(analysis["id"].before)!"新增活动"}
                    </#if>
                </span>
            </label>
        </div>
        <div class="input-control">
            <label>活动名称：</label>
            <span><@diffShow analysisBean=analysis name="goodsName" /></span>
        </div>
        <div class="input-control">
            <label>活动简介：</label>
            <span><@diffShow analysisBean=analysis name="desc" /></span>
        </div>
        <div class="input-control">
            <label>活动标签：</label>
            <span>
                <#if ((analysis["tags"].diff)!false)>
                    <#if (analysis["tags"].before)??>
                        <i class="old" title="${(analysis["tags"].before)!?join(",")}">旧</i>
                    <#else>
                        <i class="new">新</i>
                    </#if>
                </#if>
                ${(analysis[name].after)!?join(",")}
            </span>
        </div>
        <div class="input-control">
            <label>体验报告：</label>
            <span><@diffShow analysisBean=analysis name="reportDesc" /></span>
        </div>
        <div class="input-control">
            <label>关联机构：</label>
            <span><@diffShow analysisBean=analysis name="shopId" /></span>
        </div>
    </div>

    <div style="float:left;width: 360px;">
        <div class="input-control">
            <label>标题：</label>
            <span><@diffShow analysisBean=analysis name="title" /></span>
        </div>
        <div class="input-control">
            <label>价格：</label>
            <span><@diffShow analysisBean=analysis name="price" /></span>
        </div>
        <div class="input-control">
            <label>联系方式：</label>
            <span><@diffShow analysisBean=analysis name="contact" /></span>
        </div>
        <div class="input-control">
            <label>类型：</label>
            <span><@diffShow analysisBean=analysis name="category" /></span>
        </div>
        <div class="input-control">
            <label>活动类型：</label>
            <span><@diffShow analysisBean=analysis name="goodsType" /></span>
        </div>
        <div class="input-control">
            <label>支付提示：</label>
            <span><@diffShow analysisBean=analysis name="appointGift" /></span>
        </div>
        <div class="input-control">
            <label>跳转链接：</label>
            <span><@diffShow analysisBean=analysis name="redirectUrl" /></span>
        </div>
        <div class="input-control">
            <label>上课链接：</label>
            <span><@diffShow analysisBean=analysis name="successUrl" /></span>
        </div>
    </div>
</div>
<div class="op-wrapper clearfix">
    <div style="float:left; width: 360px; margin-right: 20px;">
        <div class="input-control">
            <label>
                封面图：
                <#if (analysis["bannerPhoto"].diff)!false>
                    <#if (analysis["bannerPhoto"].before)?has_content>
                        <a class="detailHistoryBtn" data-detail="${(analysis["bannerPhoto"].before)[0]!}" href="javascript:void (0);" style="color: #00b7ee">查看历史</a>
                    </#if>
                </#if>
            </label>
            <div class="image-preview clearfix">
                <#if (analysis["bannerPhoto"].after)?? && analysis["bannerPhoto"].after?size gt 0>
                    <#list analysis["bannerPhoto"].after as imgUrl>
                        <#if imgUrl_index == 0>
                        <div class="image"><img src="${imgUrl}" /></div>
                        </#if>
                    </#list>
                <#else>
                    <#if (analysis["bannerPhoto"].before)?? && analysis["bannerPhoto"].before?size gt 0>
                        <#list analysis["bannerPhoto"].before as imgUrl>
                            <#if imgUrl_index == 0>
                            <div class="image"><img src="${imgUrl}" /></div>
                            </#if>
                        </#list>
                    </#if>
                </#if>
            </div>
        </div>
    </div>

    <div style="float:left;width: 360px;">
        <div class="input-control">
            <label>
                活动详情：
                <#if (analysis["detail"].diff)!false>
                    <#if (analysis["detail"].before)?has_content>
                        <a class="detailHistoryBtn" data-detail="${(analysis["detail"].before)[0]!}" href="javascript:void (0);" style="color: #00b7ee">查看历史</a>
                    </#if>
                </#if>
            </label>
            <div class="image-preview clearfix">
                <#if (analysis["detail"].after)?? && analysis["detail"].after?size gt 0>
                    <#list analysis["detail"].after as imgUrl>
                        <#if imgUrl_index == 0>
                        <div class="image"><img src="${imgUrl}" /></div>
                        </#if>
                    </#list>
                <#else>
                    <#if (analysis["detail"].before)?? && analysis["detail"].before?size gt 0>
                        <#list analysis["detail"].before as imgUrl>
                            <#if imgUrl_index == 0>
                            <div class="image"><img src="${imgUrl}" /></div>
                            </#if>
                        </#list>
                    </#if>
                </#if>
            </div>
        </div>
    </div>

    <div class="clearfix" style="clear:both;">
        <div class="input-control">
            <label>活动介绍：</label>
            <span>
                <#if ((analysis["activityDesc"].diff)!false)>
                   <#if (analysis["activityDesc"].before)??> <i class="old">旧</i><#else><i class="new">新</i></#if>
                </#if>
            </span>
        </div>
        <div class="html-preview">
            <#if (analysis["activityDesc"].after)??>
                ${(analysis["activityDesc"].after)!}
            </#if>
        </div>
    </div>

    <div class="clearfix" style="clear:both;">
        <div class="input-control">
            <label>费用说明：</label>
            <span>
                <#if ((analysis["expenseDesc"].diff)!false)>
                    <#if (analysis["expenseDesc"].before)??> <i class="old">旧</i><#else><i class="new">新</i></#if>
                </#if>
            </span>
        </div>
        <div class="html-preview">
            <#if (analysis["expenseDesc"].after)??>
            ${(analysis["expenseDesc"].after)!}
            </#if>
        </div>
    </div>

    <div class="clearfix" style="clear:both; margin-top: 5px;margin-bottom: 20px;">
    <div class="input-control">
        <label>产品类型：</label>
    </div>
    <table class="data-table" id="goodsList" style="margin-left: 84px; width: 720px;">
        <thead>
        <tr>
            <th style="text-align: center; width:30%;">产品类型</th>
            <th style="text-align: center; width:30%;">出行时间</th>
            <th style="text-align: center; width:20%;">价格</th>
            <th style="text-align: center; width:20%;">库存</th>
        </tr>
        </thead>
        <tbody id="itemList">
            <#if  (analysis["items"].after)??>
                <#list (analysis["items"].after) as item>
                <tr>
                    <td style="text-align: center;">${(item.categoryName)!}</td>
                    <td style="text-align: center;">${(item.itemName)!}</td>
                    <td style="text-align: center;">${(item.price)!}</td>
                    <td style="text-align: center;">${(item.inventory)!}</td>
                </tr>
                </#list>
            </#if>
        </tbody>
    </table>
</div>

    <div class="clearfix" style="clear:both">
        <div class="input-control">
            <label>活动位置：</label>
            <span><@diffShow analysisBean=analysis name="address" /></span>
            <div style="margin-top: 10px; padding-left: 84px; ">
                <div id="innerMap" data-disable="true" style="width: 100%; height: 400px;"></div>
            </div>
        </div>
        <div class="input-control">
            <label>GPS信息：</label>
            <input id="longitude" type="hidden" value="${(analysis["longitude"].after)!""}">
            <input id="latitude" type="hidden" value=" ${(analysis["latitude"].after)!""}">
            <span>经度:<@diffShow analysisBean=analysis name="longitude" /></span>
            <span style="margin-left: 100px;">纬度:<@diffShow analysisBean=analysis name="latitude" /></span>
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