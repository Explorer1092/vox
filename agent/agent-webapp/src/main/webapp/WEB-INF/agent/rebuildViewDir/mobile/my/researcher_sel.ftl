<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="教研员选择班级" pageJs="" footerIndex=4>
    <@sugar.capsule css=['researchers']/>
<div class="res-top fixed-head">
    <div class="return"><i class="return-icon"></i>返回</div>
    <span class="return-line"></span>
    <span class="res-title">选择班级</span>
</div>
<div class="sel-content">
    <div class="vir-title"><i class="titleIco ico03"></i>小学</div>
    <div class="sel-list">
        <ul>
            <li class="active">一年级</li>
            <li>二年级</li>
            <li>三年级</li>
            <li>一年级</li>
            <li>二年级</li>
            <li>三年级</li>
        </ul>
    </div>
</div>
<div class="sel-content">
    <div class="vir-title"><i class="titleIco ico04"></i>中学</div>
    <div class="sel-list">
        <ul>
            <li class="active">一年级</li>
            <li>二年级</li>
            <li>三年级</li>
            <li>一年级</li>
            <li>二年级</li>
            <li>三年级</li>
        </ul>
    </div>
</div>
<div class="sel-info">小学和中学不支持同时选择</div>
</@layout.page>