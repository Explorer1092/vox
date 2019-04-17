<#import "../../layout_new.ftl" as layout>
<@layout.page group="业绩" title="全国排行榜">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:window.history.back()" class="headerBack">&lt;&nbsp;返回</a>
            <div class="headerText">全国排行榜</div>
        </div>
    </div>
</div>
<div class="mobileCRM-V2-tab">
    <div <#if cityType?? && cityType == "CityLevelA">class="active"</#if> name="cityLevel" data-level="CityLevelA">A类城市</div>
    <div <#if cityType?? && cityType == "CityLevelB">class="active"</#if> name="cityLevel" data-level="CityLevelB" >B类城市</div>
</div>
<ul class="mobileCRM-V2-list">
    <#if countryTopCity?? && countryTopCity?has_content>
        <#assign dataRank=0 />
        <#list countryTopCity as city>
            <#assign dataRank = dataRank + 1 />
            <li>
                <div class="box">
                    <div class="side-fl" style="width:10%;">${dataRank}</div>
                    <div class="side-fl" style="width:22%;">${city["regionName"]!''}</div>
                    <div class="side-fr side-orange">${city["studentAuth"]!}</div>
                </div>
            </li>
        </#list>
    <#else >
        &nbsp;&nbsp;&nbsp;&nbsp;暂无数据
    </#if>
</ul>
<script>
    $(function(){
        $("div[name='cityLevel']").on("click",function(){
            var level = $(this).attr("data-level");
            window.location.href = "/mobile/myperformance/country_top_city.vpage?cityType=" + level;
        });
    });
</script>


</@layout.page>