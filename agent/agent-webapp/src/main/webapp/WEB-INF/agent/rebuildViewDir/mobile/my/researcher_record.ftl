<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="教研员拜访记录" pageJs="" footerIndex=4>
    <@sugar.capsule css=['researchers']/>
    <#if recordList?? && recordList?size gt 0>
    <div class="s-record">
        <#list recordList as rl>
        <div class="item">
            <div class="time">${rl.workTime!}</div>
            <div class="info">
                <div class="info_1">${rl.intention!} | ${rl.place!}</div>
                <div class="list">
                    <p class="info_2">拜访过程:</p>
                    <p class="info_3">${rl.flow!}</p>
                </div>
                <div>
                    <p class="info_2">达成结果:</p>
                    <p class="info_3">${rl.conclusion!}</p>
                </div>
            </div>
        </div>
        </#list>

    </div>
    </#if>
</@layout.page>