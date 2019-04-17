<#import "../layout.ftl" as winterCamp>
<@winterCamp.page title="2016成长冬令营" pageJs="reservation">
    <@sugar.capsule css=['wintercamp'] />
    <div class="wc-wrap wc-bgYellow">
        <#if shopId == 11>
            <#include "sanya.ftl">
        <#elseif shopId == 12>
            <#include "bjkj.ftl">
        <#elseif shopId == 13>
            <#include "cbs.ftl">
        </#if>
        <div class="wc-right-nav">
            <ul>
                <li><a class="detailTjBtn" data-name="reservation" data-url="/parent/trustee/reserve.vpage?shopId=${shopId}" href="javascript:void (0);">预约报名</a></li>
                <li><a class="detailTjBtn" data-name="apply" data-url="/parent/trustee/skupay.vpage?shopId=${shopId}" href="javascript:void (0);">立即报名</a></li>
                <li><a class="detailTjBtn" data-name="question" data-url="/parent/trustee/wintercamp/question.vpage?shopId=${shopId}" href="javascript:void (0);">常见问题</a></li>
            </ul>
        </div>
    </div>
</@winterCamp.page>