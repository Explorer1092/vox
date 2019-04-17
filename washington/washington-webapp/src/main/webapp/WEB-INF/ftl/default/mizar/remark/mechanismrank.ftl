<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="bg-fff"
title='机构排名'
pageJs=["voxLogs"]
pageCssFile={"remarkIndex" : ["public/skin/mobile/mizar/css/remark"]}>

<div class="viewRanking-box">
    <div class="vrk-top">北京市口碑教育机构排名</div>
    <div class="vrk-list">
        <ul class="vrk-head">
            <li><span>排名</span></li>
            <li><span>机构名</span></li>
            <li><span>人气</span></li>
        </ul>
        <ul class="vrk-title">
            <#if shopRankList?? && shopRankList?size gt 0>
                <#list shopRankList as rank>
                    <li>
                        <div class="vrk-rank"><span class="rankNum rankNum${rank.rank!}">${rank.rank!}</span></div>
                        <div class="info">${(rank.shopName)!}</div>
                        <div class="popularity">${(rank.likeCount)!}</div>
                    </li>
                </#list>
            <#else>
                <li>
                    <div style="text-align: center; width: 100%">暂无排名</div>
                </li>
            </#if>
        </ul>
    </div>
    <div class="footer footerHei">
        <div class="inner">
            <a href="/mizar/remark/nearrank.vpage" class="w-orderedBtn w-btn-green w-btnWidPer">查看我身边的口碑机构</a>
        </div>
    </div>
</div>

<script>
    signRunScript = function(){
        YQ.voxLogs({
            database: 'parent',
            module: 'm_Ug7dW2ob',
            op : "o_flXOUPED"
        });
    };
</script>
</@layout.page>