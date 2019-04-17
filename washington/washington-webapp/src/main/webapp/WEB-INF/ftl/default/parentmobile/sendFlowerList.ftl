<#import './layout.ftl' as layout>

<@layout.page className='Index' pageJs='index' title="本月送花榜">

    <#escape x as x?html>
        <#assign topType = "topTitle">
        <#assign topTitle = "本月送花榜">
        <#include "./top.ftl" >

        <#if result.success>
            <#assign rankList = result.rankList![]>
            <#if rankList?size == 0 >
                <#assign tipType = "medal" tipText = "暂无送花历史">
                <#include  "./tip.ftl">
            <#else>
                <ul class="parentApp-personalRank">
                    <#list rankList as rank>
                        <#assign index = rank_index+1 >
                        <li class="${(index<4)?string("rank-" + index , "")}">
                            <em>${index}</em>
                            <span class="number">${rank.flowerCount!0}</span>
                            <div class="text">${rank.studentName!""}的家长</div>
                        </li>
                    </#list>
                </ul>
            </#if>
        <#else>
            <#assign info = result.info errorCode = result.errorCode>
            <#include "errorTemple/errorBlock.ftl">
        </#if>
    </#escape>

</@layout.page>
