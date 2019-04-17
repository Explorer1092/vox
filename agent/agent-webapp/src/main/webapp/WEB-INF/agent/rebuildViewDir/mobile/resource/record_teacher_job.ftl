<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="记录老师职务" pageJs="recordteacherjob" footerIndex=2>
    <@sugar.capsule css=['res']/>
<style>
    .res-content{background: none}
    .resources-box .tagList{display: inline-block;background-color: #fff}
    .resources-box .tagList li{padding:.5rem  0;float: left;width: 33.33%;text-align: center}
    .resources-box .tagList li .btn-stroke{padding:0;float: none;margin: 0 auto}
</style>
<div class="resources-box">
    <#--<div class="res-top fixed-head">-->
        <#--<div class="return"><a href="javascript:window.history.back()"><i class="return-icon"></i>返回</a></div>-->
        <#--<span class="return-line"></span>-->
        <#--<span class="res-title">记录老师职务</span>-->
        <#--<a href="javascript:void(0)" class="inner-right js-submitTehBtn">保存</a>-->
    <#--</div>-->
    <div class="res-content teacherRecord-box js-TeacherCon">
        <ul class="tagList">
        <#if dataList?has_content && dataList?size gt 0>
            <#list dataList as d>
            <li>
                <div class="btn-stroke fix-padding <#if d.isSelected!false>orange</#if>"  data-tid="${d.tag.name()!}">${d.tag.desc!}</div>
            </li>
            </#list>
        </#if>
        </ul>
    </div>
</div>
</@layout.page>