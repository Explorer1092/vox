<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="月报详情" pageJs="">
    <@sugar.capsule css=['new_home','notice']/>
<style>
    body{background-color: #f6f6f6;}
</style>
<div class="res-top fixed-head">
    <a href="javascript:window.history.back();"><div class="return"><i class="return-icon"></i>返回</div></a>
    <span class="return-line"></span>
    <span class="res-title">月报详情</span>
</div>
<#--相除格式化返回结果-->
<#function getPersent x y formater>
    <#local result = 0>
    <#if y ==0>
        <#return result>
    <#else>
        <#local result = 100*x / y >
        <#return result?string(formater)>
    </#if>
</#function>

<#function pressImageSize(link,w,h)>
    <#if ((link?string)?index_of("oss-image.17zuoye.com") gt -1)>
        <#return '${link!}@${w!200}w_${h!200}h_1o_90q'/>
    <#else>
        <#return '${link!"#"}'/>
    </#if>
</#function>

<#--专员-->
<#if monthlyReport?has_content>
<div class="met-box">
    <div class="met-head">
        <div class="right">排名：${(monthlyReport.ranking)!0}</div>
        <#if requestContext.getCurrentUser().isBusinessDeveloper()>
            <i class="ico ico01"></i>个人情况
        <#elseif requestContext.getCurrentUser().isCityManager()>
            <i class="ico ico05"></i>本市情况
        </#if>
    </div>
    <#assign myPerformance = monthlyReport.myPerformance/>
    <#if myPerformance?has_content>
        <div class="met-list">
            <ul>
                <li>
                    <div class="per">${(myPerformance.juniorSascCompleteRate)!0}%</div>
                    <div class="item">小学单科</div>
                </li>
                <li>
                    <div class="per">${(myPerformance.juniorDascCompleteRate)!0}%</div>
                    <div class="item">小学双科</div>
                </li>
                <li>
                    <div class="per">${(myPerformance.middleSascCompleteRate)!0}%</div>
                    <div class="item">中学单科</div>
                </li>
            </ul>
        </div>
        <div class="met-info">
            <#if requestContext.getCurrentUser().isBusinessDeveloper()>
                <div class="right"><span class="num">未拜访学校：${(monthlyReport.notIntoSchoolCount)!0}</span></div>
                进校<span class="num">${(monthlyReport.intoSchoolCount)!0}</span>次 计划内进校<span class="num">${getPersent((monthlyReport.inPlanIntoSchoolCount)!0,(monthlyReport.intoSchoolCount)!0,"##")}</span>%
            <#elseif requestContext.getCurrentUser().isCityManager()>
                陪访<span class="num">${(monthlyReport.visitSchoolCount)!0}</span>次 <span style="margin-left: .5rem;">人均进校</span><span class="num">${(monthlyReport.perMemberIntoSchoolCount)!0}</span>  <span style="margin-left: .5rem;">计划内进校</span><span class="num">${getPersent((monthlyReport.inPlanIntoSchoolCount)!0,(monthlyReport.intoSchoolCount)!0,"##")}</span>%
            </#if>
            </div>
    </#if>
</div>
<div class="met-box">
    <div class="met-head">
        <i class="ico ico02"></i>大区优秀团队／个人
    </div>
    <div class="met-list listTwo">
        <#if (monthlyReport.excellentGroupAndUserList)?has_content && (monthlyReport.excellentGroupAndUserList)?size gt 0>
        <#assign excellList = (monthlyReport.excellentGroupAndUserList)/>
        <ul>
        <#list excellList as e>
            <#if e.type == 2>
                <li>
                    <div class="per">${e.groupName!''}</div>
                    <div class="item">优秀团队（全国排名${e.ranking!''}）</div>
                </li>
            <#elseif e.type == 3>
                <li>
                    <div class="per">${e.userName!''}<span class="area">${e.groupName!''}</span></div>
                    <div class="item">优秀个人（全国排名${e.ranking!''}）</div>
                </li>
            </#if>
        </#list>
        </ul>
        </#if>
    </div>
</div>
<div class="met-box">
    <div class="met-head">
        <i class="ico ico03"></i>大区进校王
    </div>
    <div class="met-list listOne">
    <#if (monthlyReport.visitSchoolRankingList)?has_content && (monthlyReport.visitSchoolRankingList)?size gt 0>
        <#assign vrank = (monthlyReport.visitSchoolRankingList)/>
        <ul>
        <#list vrank as v>
            <li>
                <div class="per">${v.userName!''}</div>
                <div class="item">${v.groupName!''} 进校${v.visitSchoolCount!0}次</div>
            </li>
        </#list>
        </ul>
    </#if>
    </div>
</div>
<div class="met-box">
    <div class="met-head">
        <i class="ico ico04"></i>老大推荐读物
    </div>
    <#if (monthlyReport.recommendBookList)?has_content && (monthlyReport.recommendBookList)?size gt 0>
        <#assign readList = (monthlyReport.recommendBookList)/>
        <#list readList as rl>
            <div class="met-side">
                <div class="image"><img src="${pressImageSize(rl.bookCoverUrl!"#",500,200)}" alt=""></div>
                <div class="title"><#if rl.bookName?has_content>《${rl.bookName!''}》</#if></div>
            </div>
        </#list>
    </#if>
</div>
</#if>
<#if requestContext.getCurrentUser().isCityManager()>
<div class="resources-box met-box">
    <div class="met-head">
        <i class="ico ico01"></i>专员情况
    </div>
    <div class="res-autInfor autInor-mar">
        <table class="aut-table">
            <thead>
            <tr>
                <td>专员</td>
                <td>小学单活</td>
                <td>小学双活</td>
                <td>中学单活</td>
            </tr>
            </thead>
            <tbody>
            <#if (monthlyReport.managedUserPerformanceList)?has_content && (monthlyReport.managedUserPerformanceList)?size gt 0>
                <#assign manper = (monthlyReport.managedUserPerformanceList)/>
                <#list manper as mp>
                <tr>
                    <td>${mp.userName!''}</td>
                    <td>${mp.juniorSascCompleteRate!'0'}%</td>
                    <td>${mp.juniorDascCompleteRate!'0'}%</td>
                    <td>${mp.middleSascCompleteRate!'0'}%</td>
                </tr>
                </#list>
            </#if>
            </tbody>
        </table>
    </div>
</div>
</#if>
<#if requestContext.getCurrentUser().isRegionManager()>
<div class="met-box">
    <div class="met-head">
        <i class="ico ico06"></i>大区专员在全国的271分布
    </div>
    <div class="met-column">
        <ul>
            <li class="col-1">
                <div>城市<br><span class="item">（全国）</span></div>
                <div>前20%<br><span class="item">（人数）</span></div>
                <div>中部70%<br><span class="item">（人数）</span></div>
                <div>后10%<br><span class="item">（人数）</span></div>
            </li>
    <#if (monthlyReport.performanceDistributionList)?has_content && (monthlyReport.performanceDistributionList)?size gt 0>
        <#assign perDis = (monthlyReport.performanceDistributionList)/>
        <#assign top20 = 0>
        <#assign top70 = 0>
        <#assign top10 = 0>
        <#list perDis as pd>
            <li class="<#if pd_index%2 == 0>active</#if>"><#--active是点击状态-->
                <div class="<#if (pd.rankingRate!0) lte 0.2>
                    fontOrange
                <#elseif (pd.rankingRate!0) gt 0.2 && (pd.rankingRate!0) lte 0.9>
                    fontYellow
                <#elseif (pd.rankingRate!0) gt 0.9 && (pd.rankingRate!0) lt 1>
                    fontBlue
                </#if>">${pd.groupName!''}</div>
                <div class="prev"><i class="sanjiaoIco"></i>${pd.interval1Count!0}</div>
                <div class="mid"><i class="sanjiaoIco"></i>${pd.interval2Count!0}</div>
                <div class="bot"><i class="sanjiaoIco"></i>${pd.interval3Count!0}</div>
            </li>
            <#assign top20 = (pd.interval1Count!0) + top20>
            <#assign top70 = (pd.interval2Count!0) + top70>
            <#assign top10 = (pd.interval3Count!0) + top10>
        </#list>
        <li>
            <div>合计</div>
            <div class="prev"><i class="sanjiaoIco"></i>${top20!0}</div>
            <div class="mid"><i class="sanjiaoIco"></i>${top70!0}</div>
            <div class="bot"><i class="sanjiaoIco"></i>${top10!0}</div>
        </li>
    </#if>
        </ul>
    </div>
</div>
<div class="met-arrow">备注：城市颜色代表市经理在全国经历的271分布，红色、黄色、蓝 色分别代表该城市市经理业绩情况属于全国前20%、中部70%、后10%</div>
</#if>
<#if monthlyReport?has_content && !requestContext.getCurrentUser().isCountryManager()>
<div class="met-arrow">说明：因11月初有重点校的调整，导致月报中业绩完成率数据与11月初天玑首页显示的业绩完成率略有差异；最终结算数据以11月初天玑首页显示的业绩完成率为准</div>
</#if>
</@layout.page>