<#import "../../module.ftl" as module>
<@module.page
title="亲子活动编辑"
pageJsFile={"siteJs" : "public/script/basic/activity"}
pageJs=["siteJs"]
leftMenu="亲子活动"
>
<script type="text/javascript" src="https://webapi.amap.com/maps?v=1.3&key=ae2ed07e893eb41d259a47e7ba258c00&plugin=AMap.Geocoder"></script>
<div class="bread-nav">
    <a class="parent-dir" href="/basic/activity/index.vpage">活动管理</a>
    &gt;
    <a class="current-dir" href="javascript:void(0)" style="cursor: default">查看活动</a>
</div>
<h3 class="h3-title">
    亲子活动详情
</h3>
<div>
    <input type="hidden" name="gid" value="${(activity.id)!}">
    <div style="float:left;">
        <div class="input-control">
            <label>活动名称：</label>
            <input class="readonly" disabled value="${(activity.goodsName)!}" />
        </div>
        <div class="input-control">
            <label>活动简介：</label>
            <span style="width: 300px; word-break: break-all; display: inline-table; ">
                ${(activity.desc)!}
            </span>
        </div>
        <div class="input-control">
            <label>活动标签：</label>
            <input  value="${(activity.tags)!?join(',')}" class="readonly" disabled />
        </div>
        <div class="input-control">
            <label>体验报告：</label>
            <input class="readonly" disabled value="${(activity.reportDesc)!}" />
        </div>
        <div class="input-control">
            <label>关联机构：</label>
            <input value="${(activity.shopId)!}" class="readonly" disabled />
        </div>
        <div class="input-control">
            <label>封面图：</label>
        </div>
        <div class="image-preview bannerImg clearfix" style="height:150px;">
            <#if (activity.bannerPhoto)??>
                <#list activity.bannerPhoto as imgUrl>
                <#if imgUrl_index == 0>
                    <div class="image">
                        <img src="${imgUrl}" />
                    </div>
                </#if>
                </#list>
            </#if>
        </div>
    </div>
    <div style="float:right;">
        <div class="input-control">
            <label>标题：</label>
            <input class="readonly" disabled value="${(activity.title)!}"  />
        </div>
        <div class="input-control">
            <label>价格：</label>
            <input class="readonly" disabled value="${(activity.price)!}"  />
        </div>
        <div class="input-control">
            <label>联系方式：</label>
            <input class="readonly" disabled value="${(activity.contact)!}" />
        </div>
        <div class="input-control">
            <label>类型：</label>
            <input class="readonly" disabled value="${(activity.category)!}" />
        </div>
        <div class="input-control">
            <label>活动类型：</label>
            <input class="readonly" disabled value="${(activity.goodsType)!}" />
        </div>
        <div class="input-control">
            <label>支付提示：</label>
            <input class="readonly" disabled value="${(activity.appointGift)!}" />
        </div>
        <div class="input-control">
            <label>跳转链接：</label>
            <input class="readonly" disabled value="${(activity.redirectUrl)!}"/>
            <#--<input name="redirectUrl" data-title="跳转链接" class="require" value="${(activity.redirectUrl)!}" />-->
        </div>
        <div class="input-control">
            <label>上课链接：</label>
            <input title="" class="readonly" disabled value="${(activity.successUrl)!}"/>
        </div>
        <div class="input-control">
            <label>活动详情：</label>
        </div>
        <div class="image-preview detailImg clearfix" style="height:150px;">
            <#if (activity.detail)?? && activity.detail?size gt 0>
                <#list activity.detail as imgUrl>
                 <#if imgUrl_index == 0>
                    <div class="image">
                        <img src="${imgUrl}" />
                    </div>
                 </#if>
                </#list>
            </#if>
        </div>
    </div>

    <div class="clearfix" style="clear:both;">
        <div class="input-control">
            <label>活动介绍：</label>
        </div>
        <div class="html-preview">
            ${(activity.activityDesc)!}
        </div>
    </div>

    <div class="clearfix" style="clear:both;">
        <div class="input-control">
            <label>费用说明：</label>
        </div>
        <div class="html-preview">
           ${(activity.expenseDesc)!}
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
            <#if (activity.items)??>
                <#list activity.items as item>
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
            <input class="readonly" disabled value="${(activity.address)!}" style="width:715px;"/>
            <div style="margin-top: 10px; padding-left: 84px; ">
                <div id="innerMap" data-disable="true" style="width: 100%; height: 400px;"></div>
            </div>
        </div>
        <div class="input-control">
            <label>GPS信息：</label>
            经度: <input class="readonly" readonly id="longitude" name="longitude" value="${(activity.longitude)!}" />
            纬度: <input class="readonly" readonly id="latitude" name="latitude"  value="${(activity.latitude)!}" />
        </div>
    </div>
</div>
</@module.page>