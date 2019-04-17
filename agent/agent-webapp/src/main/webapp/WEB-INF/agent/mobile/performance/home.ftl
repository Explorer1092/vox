<#import "../layout/mobile_layout.ftl" as layout>
<@layout.page group="业绩" title="首页">
<#macro chartLoading>
<div>
    <img src="/public/images/loading.gif" alt="正在加载……" width="100%">
</div>
</#macro>
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <div class="headerText">首页</div>
        </div>
    </div>
</div>
<div class="rightTop">最新数据时间：2016-09-18</div>
<div class="homeCard">
    <@chartLoading/>
    <div class="cardItem">
        <div class="info">
            <canvas id="primarySingleChart" width="125px;" height="100px;"></canvas>
        </div>
        <div class="info">
            <div>小学单活</div>
            <div>目标/完成：5500/10000</div>
            <div>
                剩余每日需完成：375
            </div>
        </div>
        <div class="tip">昨日+90</div>
    </div>
    <div class="clearfix"></div>
    <div class="cardItem">
        <div class="info">
            <canvas id="primaryDoubleChart" width="125px;" height="100px;"></canvas>
        </div>
        <div class="info">
            <div>小学双活</div>
            <div>目标/完成：5500/10000</div>
            <div>
                剩余每日需完成：375
            </div>
        </div>
        <div class="tip">昨日+90</div>
    </div>
    <div class="clearfix"></div>
    <div class="cardItem">
        <div class="info">
            <canvas id="middleSingleChart" width="125px;" height="100px;"></canvas>
        </div>
        <div class="info">
            <div>中学单活</div>
            <div>目标/完成：5500/10000</div>
            <div>
                剩余每日需完成：375
            </div>
        </div>
        <div class="tip">昨日+90</div>
    </div>
</div>
</@layout.page>