<#--<#import 'module.ftl' as temp>-->
<#--<@temp.page title="领取奖励">-->
<#--<div class="rank-container">-->
    <#--<div class="inner" style="padding:20px 0 300px;">-->
        <#--<#if rewardList?size gt 0>-->
            <#--<div class="info-tip-box">-->
                <#--<#list rewardList as rl>-->
                    <#--<#if rl.rank == 0>-->
                        <#--<div class="it-list">-->
                            <#--<p class="list-b"><a class="receive-btn data-clickReceiveBtn" href="javascript:void(0);" data-rank="${rl.rank}" data-month="${rl.month}">点击领取<span class="arrow-white"></span></a></p>-->
                            <#--<div class="list-f"><span class="golden-cup"></span>${rl.month?substring(0, 4)}年${rl.month?substring(4, 6)}月 特殊排名获得特殊奖励</div>-->
                        <#--</div>-->
                    <#--<#else>-->
                        <#--<div class="it-list">-->
                            <#--<p class="list-b"><a class="receive-btn data-clickReceiveBtn" href="javascript:void(0);" data-rank="${rl.rank}" data-month="${rl.month}">点击领取<span class="arrow-white"></span></a></p>-->
                            <#--<div class="list-f"><span class="golden-cup"></span>${rl.month?substring(0, 4)}年${rl.month?substring(4, 6)}月 班级全国排名 ${rl.rank} 获得排名奖励</div>-->
                        <#--</div>-->
                    <#--</#if>-->
                <#--</#list>-->
            <#--</div>-->
        <#--<#else>-->
            <#--<div class="info-tip-box">-->
                <#--<p class="info-tip">每月最后一天24:00结算，根据排名可获得相应的班级奖励</p>-->
            <#--</div>-->
        <#--</#if>-->
    <#--</div>-->
<#--</div>-->
<#--<script type="text/javascript">-->
    <#--$(function(){-->
        <#--//点击领取-->
        <#--$(".data-clickReceiveBtn").on("click", function(){-->
            <#--var $this = $(this);-->
            <#--if($this.hasClass("dis")){-->
                <#--return false;-->
            <#--}-->
            <#--$this.addClass("dis");-->
            <#--$.post("/student/clazz/awardclazzrankreward.vpage", {-->
                <#--rank : $this.data("rank"),-->
                <#--month : $this.data("month")-->
            <#--}, function(data){-->
                <#--if(data.success){-->
                    <#--$17.alert("领取成功！", function(){-->
<#--//                        $this.closest(".it-list").remove();-->
                        <#--location.reload();-->
                    <#--});-->
                <#--}else{-->
                    <#--$17.alert(data.info);-->
                    <#--$this.removeClass("dis");-->
                <#--}-->
            <#--});-->
        <#--});-->
    <#--});-->
<#--</script>-->
<#--</@temp.page>-->
