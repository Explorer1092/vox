<#import "../../layout_new.ftl" as layout>
<@layout.page group="业绩" title="我的业绩">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <div class="headerText">业绩</div>
            <#--<a href="javascript:void(0);" class="headerBtn" type="noAuth">看中学</a>-->
        </div>
    </div>
</div>

<a href="term_performance.vpage?preRegion=0&viewType=addStudent" class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <div class="hdLink">
        <span class="side-fl">我的业绩</span>
        <span class="side-fr side-time">${curDate!''}</span>
    </div>
    <#if todayMap??>
        <div class="mobileCRM-V2-rankInfo">
        <#if (todayMap['hasTodayData'])?? >
            <#if todayMap['hasTodayData']>
                    <div class="infoBox">
                        <div>
                            <div class="boxTitle">新增注册</div>
                            <div class="boxNum"><#if todayMap??&& todayMap?has_content>${todayMap["todayRegCount"]!}</#if></span></div>
                            <div class="boxFoot">本月累计注册<span><#if todayMap??&& todayMap?has_content>${todayMap["monthRegCount"]!}</#if></span></span></div>
                        </div>
                        <div>
                            <div class="boxTitle">新增认证</div>
                            <div class="boxNum"><#if todayMap??&& todayMap?has_content>${todayMap["todayAuthCount"]!}</#if></div>
                            <div class="boxFoot">本月累计认证<span><#if todayMap??&& todayMap?has_content>${todayMap["monthAuthCount"]!}</#if></span></div>
                        </div>
                    </div>
            <#else>
                    <div class="infoBox">
                        <div class="boxProm">数据还在计算中，请稍后查看</div>
                    </div>
            </#if>
        </#if>
        </div>
    </#if>
</a>
<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <div class="hdText">排行榜</div>
    <ul class="mobileCRM-V2-list">
        <li>
            <a href="/mobile/myperformance/country_top_city.vpage" class="link link-ico">
                <div class="side-fl">全国排行榜</div>
            </a>
        </li>
        <li>
            <a href="province_top_city.vpage" class="link link-ico">
                <div class="side-fl">省内排行榜</div>
            </a>
        </li>
    </ul>
</div>
<#--<script>
    $(function () {
        $(".headerBtn").hide();
        var selectSchoolLevel = $.cookie("selectSchoolLevel");
        if(selectSchoolLevel == 'MIDDLE') {
            $(".headerBtn").text("看小学");
        } else {
            $(".headerBtn").text("看中学");
        }
        $(".headerBtn").click(function(){
            var isAll = $.cookie("isAll");
            if(selectSchoolLevel == 'MIDDLE') {
                $.cookie("selectSchoolLevel","JUNIOR",{path: "/"});
            } else {
                $.cookie("selectSchoolLevel","MIDDLE",{path: "/"});
            }
            window.location.reload();
        });



        var isAll = $.cookie("isAll");
        if (isAll == 'true') {
            $(".headerBtn").show();
        }
    });
</script>-->
</@layout.page>