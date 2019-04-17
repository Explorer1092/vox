<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>

<@layout.page title="天玑榜" pageJs="tianji_proclama">
<@sugar.capsule css=['new_home']/>
<div class="rankingList-box">
    <div class="res-top fixed-head">
        <a href="javascript:window.history.back();"><div class="return"><i class="return-icon"></i>返回</div></a>
        <span class="return-line"></span>
        <span class="res-title">天玑榜</span>
        <a href="rankrule.vpage"><span class="res-right"></span></a>
    </div>

    <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isRegionManager()>
        <div class="c-opts js-tab c-flex c-flex-3" style="text-align: center">
            <span class="active" data-type="1"><i class="triangle"></i>大区</span>
            <span data-type="2"><i class="triangle"></i>市经理</span>
            <span data-type="3"><i class="triangle"></i>专员</span>
        </div>
    <#elseif requestContext.getCurrentUser().isCityManager()>
        <div class="c-opts js-tab c-flex c-flex-2" style="text-align: center">
            <span class="active" data-type="2"><i class="triangle"></i>市经理</span>
            <span data-type="3"><i class="triangle"></i>专员</span>
        </div>
    <#elseif requestContext.getCurrentUser().isBusinessDeveloper()>

    </#if>
    <div class="rl-banner" style="clear:both;">
        <ul id="topThree">
            <li><#--<i class="rl-ico ico-1"></i>--><span class="rl-name"></span></li>
            <li><#--<i class="rl-ico ico-2"></i>--><span class="rl-name"></span></li>
            <li><#--<i class="rl-ico ico-3"></i>--><span class="rl-name"></span></li>
        </ul>
    </div>
    <#if requestContext.getCurrentUser().isCountryManager()>
    <div class="c-search" style="display: none;" id="searchItem">
        <input placeholder="请输入姓名" maxlength="30" id="schoolSearch">
        <span class="js-search">搜索</span>
    </div>
    </#if>
    <div class="rl-title" id="localContent" type="all">

    </div>
    <div class="rl-table" id="rankingTable" order="-1">

    </div>
</div>
<#--表格数据模板-->
<script id="tableTemp" type="text/html">
    <table colspan="0" cellpadding="0" cellspacing="0" style="text-align: center;">
        <tr>
            <#if requestContext.getCurrentUser().isCountryManager()>
                <td class="js-reOrderAgent"><span class="rl-name">排序<i class="rl-Arrow downArr"></i></span></td>
            <#else>
                <td><span class="rl-name">排序</span></td>
            </#if>
            <td><span class="rl-name">姓名</span></td>
            <td><span class="rl-area">部门</span></td>
            <td><span class="rl-name">升降</span></td>
        </tr>
        <%for(var i = 0; i < rankingDataList.length; i++) {%>
        <%var rank=rankingDataList[i]%>
        <tr>
            <td><span class="rl-num"><%=rank.ranking%></span></td>
            <td><span class="rl-name"><%=rank.userName%></span></td>
            <td><span class="rl-area"><%=rank.groupName%></span></td>
            <%if(rank.rankingFloat > 0){%>
                <td><i class="rl-Arrow"></i></td>
            <%}else if(rank.rankingFloat == 0){%>
                <td><i class="rl-Arrow lineArr"></i></td>
            <%}else if(rank.rankingFloat < 0){%>
                <td><i class="rl-Arrow downArr"></i></td>
            <%}%>
        </tr>
        <%}%>
    </table>
</script>

<script id="localContentTemp" type="text/html">
    <#if requestContext.getCurrentUser().isCountryManager()>
    <div class="c-search" style="display: none;" id="searchItem">
        <input placeholder="请输入姓名" maxlength="30" id="schoolSearch">
        <span class="js-search">搜索</span>
    </div>
    <#else>
    <%if (myData){ %>
        <div class="rl-left">
            <i class="rl-nameIco"></i>
            <span class="rl-name" style="width:auto;"><%=userName%></span><span style="width:auto;" class="rl-area"><%=groupName%></span><span>排名</span><span class="rl-ranking"><%=ranking%>
            <%if(rankingFloat > 0){%>
            <i style="margin:0;" class="rl-Arrow"></i>
            <%}else if(rankingFloat == 0){%>
            <i style="margin:0;" class="rl-Arrow lineArr"></i>
            <%}else if(rankingFloat < 0){%>
            <i style="margin:0;" class="rl-Arrow downArr"></i>
            <%}%></span>
        </div>
    <%}%>
    <div class="rl-right">
        <a style="font-size: 0.65rem;" href="javascript:void(0);" class="see-btn js-onlyLocal">只看本大区<i class="seeArrow"></i></a>
    </div>
    </#if>
</script>

<script>
var showFlag = false;
<#if requestContext.getCurrentUser().isRegionManager()>
showFlag = true;
</#if>
var renderTemplate = function(tempSelector,data,container){
    var contentHtml = template(tempSelector, data);
    $(container).html(contentHtml);
};
</script>
</@layout.page>