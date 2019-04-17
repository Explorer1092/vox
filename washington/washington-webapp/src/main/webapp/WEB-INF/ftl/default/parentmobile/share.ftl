<#import './layout.ftl' as layout>

<#assign isShareExists = isShare?exists>

<#if !title?exists>
    <#if !isShareExists && !title?exists>
        <#assign title ="学业报告分享">
    <#else>
        <#assign title = (studentName!'') + "的学业报告分享">
    </#if>
</#if>

<@layout.page className='HomeworkSharet schoolReport-bg' title=title pageJs="second">

<#assign module = (module!'none')?lower_case >

<#if !isShareExists>
    <#assign topType = "topTitle">
    <#assign topTitle = "${title}">
    <#include "./top.ftl" >
</#if>

<#include "shareTemplates/main.ftl" >

<#if !isShare?exists >
    <div class="foot_btn_box">

        <#-- share button track event -->
        <#if module == "none">
            <#assign trackData = buildTrackData("report|transcript_share")>
        <#elseif module == "math">
            <#assign trackData = buildTrackData("report|math_transcript_share")>
        <#elseif module =="english">
            <#assign trackData = buildTrackData("report|en_transcript_share")>
        </#if>

        <a class="btn_mark btn_mark_block doTrack doShare btn_mark_orange" ${trackData} data-content="${title!''}" href="javascript:;"><span style="color: #FFFFFF; font-weight: normal;">分享</span></a>

    </div>
    <a href="javascript:;" class="hide doAutoTrack" data-track = "report|transcript_open"></a>
</#if>
</@layout.page>
