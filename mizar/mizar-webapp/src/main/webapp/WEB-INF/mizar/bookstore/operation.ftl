<#import "../module.ftl" as module>
<@module.page
title="经营状况"
pageJsFile={"siteJs" : "public/script/bookstore/data"}
pageJs=["echarts", "siteJs"]
leftMenu="经营数据"
>

<div class = "data">
    <div class="title-text" style="margin-top: 20px;width: 100%">
        |实时数据

    </div>
    <div class="reset-form" style="border: none">
        <div class="mybox leftbox" style="width: 100%">
            <div class="leftbox">
                <div class="leftdiv">订单总数</div>
                <div class="leftdiv"><span>${operationInfoBean.orderTotalNum!0}</span>单</div>
                <div class="leftdiv">昨日新增：<span>${operationInfoBean.yesterdayOrderNum!0}</span>单</div>
            </div>
            <div style="float: left;width: 1px;background: #d3d8df; height: 180px; margin-top: 30px"></div>
            <#if userRole != 21>
            <div class="rightbox">
                <div class="rightdiv">门店总数</div>
                <div class="rightdiv"><span>${operationInfoBean.storeTotalNum!0}</span>家</div>
                <div class="rightdiv">昨日新增：<span>${operationInfoBean.yesterdayStoreNum!0}</span>家</div>
            </div>
            </#if>
            <#if userRole == 21>
            <div class="rightbox">
                <div class="rightdiv">转介绍门店数</div>
                <div class="rightdiv"><span>${operationInfoBean.storeTotalNum!0}</span>家</div>
                <div class="rightdiv">昨日新增：<span>${operationInfoBean.yesterdayStoreNum!0}</span>家</div>
            </div>
            </#if>
        </div>

    </div>
</div>
<div class = "chart" >
    <div class="title-text" style="margin-top: 20px;width: 100%">
        |近7天数据（订单数）
    </div>
    <div class="reset-form" style="border: none">
        <div class="mybox leftbox" style="width: 100%;text-align: center">
           <#if (operationInfoBean.recentDaysOrderNum??) && operationInfoBean.recentDaysOrderNum != '{}'>
            <div id="chartData" style="display: none"> ${operationInfoBean.recentDaysOrderNum} </div>
           <#else>
              <div style="margin-top: 100px"> 暂无数据</div>
           </#if>
            <div id="chart" class="chartData" style="width: 100%;height:200px">

            </div>
        </div>

    </div>
</div>
<div style="clear:both"}</div>
<div style="width: 100%">
    <div class="title-text" style="margin-top: 20px;width: 100%">
        门店排行榜
    </div>
    <div class="reset-form" style="border: none">

        <table class="data-table one-page  displayed">
            <thead>
            <tr>
                <th>排名</th>
                <th>门店名</th>
                <th>门店ID</th>
                <th>成单量</th>
            </tr>
            </thead>
            <tbody>
 <#if operationInfoBean.orderNumRanks??>
     <#list operationInfoBean.orderNumRanks as item>
            <tr>
                <td>${item_index +1}</td>
                <td>${item.bookStoreName!''}</td>
                <td>${item.bookStoreId!''}</td>
                <td>${item.orderNum!''}</td>
            </tr>
     </#list>

 <#else>
 <tr>
     <td colspan="4" style="text-align: center">无数据</td>

 </tr>
 </#if>

            </tbody>
        </table>


    </div>
</div>
<#if userRole == 21>
<div style="width: 100%">
    <div class="title-text" style="margin-top: 20px;width: 100%">
        转介绍的门店数据
    </div>

    <div class="reset-form" style="border: none">
        <table class="data-table two-page displayed">
            <thead>
            <tr>
                <th>排名</th>
                <th>门店名</th>
                <th>门店ID</th>
                <th>成单量</th>
            </tr>
            </thead>

            <tbody>
<#if operationInfoBean.referralRanks??>
    <#list operationInfoBean.referralRanks.content as item>
                    <tr>
                        <td>${(page-1)* 10 + item_index +1}</td>
                        <td>${item.bookStoreName!''}</td>
                        <td>${item.bookStoreId!''}</td>
                        <td>${item.orderNum!''}</td>
                    </tr>
    </#list>
<#else>
 <tr>
     <td colspan="4" style="text-align: center">无数据</td>

 </tr>
</#if>


</tbody>
</table>


</div>

    <div id="paginator" pageIndex="${(page!0)}" class="paginator clearfix"
         totalPage="${totalPages!1}"></div>

</div>
</#if>

</@module.page>