<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="产品活动" pageJs="">
<@sugar.capsule css=['new_home']/>
<style type="text/css">
    body{background-color: #f6f6f6;}
</style>
<div class="productActivity-box productDiffer">
    <div class="res-top fixed-head">
        <a href="javascript:window.history.back();"><div class="return"><i class="return-icon"></i>返回</div></a>
        <span class="return-line"></span>
        <span class="res-title">产品活动</span>
    </div>

    <#if activity?? && activity?size gt 0>
        <#list activity as a>
            <div class="pa-content">
                <a href="${a.fileUrl!"#"}">
                    <dl class="pa-list">
                        <#--<dt><img src="${a.thumbnailsUrl!"#"}" alt=""></dt>-->
                        <dd style="margin-left: 0rem;">
                            <p class="pa-title">${a.activityName!''}</p>
                            <p class="pa-info">${a.activityEntrance!''}</p>
                            <#--<p class="pa-info">${a.timeOut!''}</p>-->
                            <p class="pa-time">${a.startDate?string("yyyy-MM-dd")!''}—${a.endDate?string("yyyy-MM-dd")!''}</p>
                        </dd>
                    </dl>
                </a>
            </div>
        </#list>
    <#else>
        <p style="margin-top: 40px;text-align: center;">
            暂时无产品活动
        </p>
    </#if>
</div>
</@layout.page>