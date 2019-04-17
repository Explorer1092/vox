<#import "../layout_new.ftl" as layout>
<@layout.page group="业绩">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <div class="headerText">消息中心</div>
        </div>
    </div>
</div>
<ul class="mobileCRM-V2-list mobileCRM-V2-mt">
    <li>
        <a href="#" class="link link-ico">
            <div class="side-fl">月报<span>${monthlyReport!0}</span></div>
        </a>
    </li>
    <li>
        <a href="#" class="link link-ico">
            <div class="side-fl">周报<span>${weeklyReport!0}</span></div>
        </a>
    </li>
</ul>

<div class="mobileCRM-V2-list mobileCRM-V2-mt">
    <li>
        <a href="#" class="link link-ico">
            <div class="side-fl side-orange">重要通知<span>${impNotice!0}</span></div>
        </a>
    </li>
</div>

<ul class="mobileCRM-V2-list mobileCRM-V2-mt">
    <li>
        <a href="#" class="link link-ico">
            <div class="side-fl">陪访建议<span>${visitSuggest!0}</span></div>
        </a>
    </li>
    <li>
        <a href="#" class="link link-ico">
            <div class="side-fl">陪访提醒<span>${visitRemind!0}</span></div>
        </a>
    </li>
</ul>

<div class="mobileCRM-V2-list mobileCRM-V2-mt">
    <li>
        <a href="#" class="link link-ico">
            <div class="side-fl side-orange">平台更新日志<span>${platformUpdate!0}</span></div>
        </a>
    </li>
</div>

</@layout.page>
