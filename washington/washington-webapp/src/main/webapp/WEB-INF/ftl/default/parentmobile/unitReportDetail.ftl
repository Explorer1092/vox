<#import './layout.ftl' as layout>

<@layout.page className='UnitReportDetail' pageJs='unitReportDetail' title="单元报告详情">

    <#include "constants.ftl">

    <#if result.success>
        <#assign urd = result.urd>

        <#function pointTable(pointInfo, title, theads)>

            <#assign result>
                <div class="title"> <i class="icon icon-1"></i> ${title} </div>
                <div class="container">
                    <table>
                        <thead>
                        <tr class="odd-1">
                            <#list theads as thead>
                                <td>${thead}</td>
                            </#list>
                        </tr>
                        </thead>
                        <tbody>
                            <#list pointInfo as point>
                            <#--
                            eid : pointg.eid
                            pointId : pointg.pointId
                            -->
                            <tr <#if point_index % 2 != 0>class="odd-2"</#if>>
                                <td>${point_index+1}</td>
                                <td>${point.pointName!''}</td>
                                <td class="view">查看</td>
                            </tr>
                            </#list>
                        </tbody>
                    </table>
                    <#--
                    <div class="turn-list">
                        <a href="javascript:;">
                            查看更多
                            <span class="triangle down"></span>
                        </a>
                    </div>
                    -->
                </div>
            </#assign>
            <#return result>
        </#function>

        <div class="unitReports-box">
            <div class="header">${urd.unitName}</div>

            ${pointTable(urd.points, "单元重点", ["序号", "重点知识", "易考例题"])}

            <#assign wqls = urd.wql![]>
            <#if wqls?size gt 0>
                <div class="title"> <i class="icon icon-2"></i> 本班情况 </div>
                <#list urd.wql as wql>
                    <div class="container doRenderQuestionByIds" data-eids="${wql.eid!''}" > </div>
                </#list>
            </#if>

            ${pointTable(urd.nextPoints, "预习安排", ["序号", "重点知识", "易考例题"])}

        </div>
    <#else>
        <#assign info = result.info errorCode = result.errorCode>
        <#include "errorTemple/errorBlock.ftl">
    </#if>

</@layout.page>
