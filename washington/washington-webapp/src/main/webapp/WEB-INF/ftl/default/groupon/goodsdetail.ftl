<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title='值得买'
bodyClass='bgB8'
pageJs=["jquery","voxLogs"]
pageCssFile={"index" : ["public/skin/mobile/groupon/css/index"]}>
    <#assign specialTag = {
    'hot': {'name': 'hot','color': 'tag-hot'},
    'new': {'name': 'new','color': 'tag-new'},
    'postFree': {'name': '包邮','color': 'tag-send'},
    'promotions': {'name': '促销','color': 'tag-sale'} }/>

    <#assign goodsSource = {
    'tian_mao':{'name':'天猫'},
    'tao_bao':{'name':'淘宝'},
    'jing_dong':{'name':'京东'},
    'dang_dang':{'name':'当当'}
    }/>
<#include "../mizar/function.ftl"/>
<div class="bookDetails-box">
    <div class="spt-banner">
        <img src="${pressImageAutoW((grouponGoods.image)!,640)}" alt="">
    </div>
    <div class="bod-main">
        <div class="bod-info"><span class="website">${(goodsSource[grouponGoods.goodsSource].name)!}</span> <span class="time">${(grouponGoods.deployDay)!}</span></div>
        <div class="bod-title">${(grouponGoods.shortTitle)!}</div>
        <div class="bod-content">
            ${(grouponGoods.recommend)!}
        </div>
        <#if (grouponGoods.goodsTag)?has_content>
            <div class="bod-side">
                <div class="bod-title">相关标签</div>
                <div class="bod-tag">
                    <#list grouponGoods.goodsTag?split(',') as tag>
                        <span class="tags" style="overflow: hidden; vertical-align: middle; white-space: nowrap;">${tag}</span>
                    </#list>
                </div>
            </div>
        </#if>

    </div>
    <#if recommendList?? && recommendList?size gt 0>
        <div class="lor-title">
            精品推荐 <i class="titleIco"></i>
        </div>
        <div class="lor-main">
            <#list recommendList as rl>
                <dl class="lor-list" onclick="location.href='/groupon/goodsdetail.vpage?goodsId=${(rl.id)!}&_from=goodsdetail'">
                    <dt class="lor-pic"><img src="${pressImageAutoW((rl.image)!,310)}" alt=""></dt>
                    <dd class="lor-column">
                        <div class="bookTitle">${(rl.shortTitle)!}</div>
                        <div class="price">
                            ¥ ${(rl.price)!}
                            <#if rl.specialTag?has_content>
                                <#list rl.specialTag?split(',') as gr>
                                    <span class="bookTag ${(specialTag[gr].color)!}">${(specialTag[gr].name)!}</span>
                                </#list>
                            </#if>
                        </div>
                        <div class="website">${goodsSource[rl.goodsSource].name}</div>
                        <div class="time">${(rl.deployDay)!}</div>
                    </dd>
                </dl>
            </#list>
        </div>
    </#if>

    <div class="bod-footer">
        <div class="inner">
            <a id="urlLinkBtn" data-url="${(grouponGoods.url)!}" href="javascript:void (0);" class="f-btn-red">直达链接</a>
            <div class="price">
                <#if (grouponGoods.price)?has_content>
                    ￥
                </#if>
                ${(grouponGoods.price)!}
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    function getQuery(item) {
        var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
        return svalue ? decodeURIComponent(svalue[1]) : '';
    }

    signRunScript = function () {
        $("#urlLinkBtn").on('click', function () {
            var url = $(this).data('url');
            setTimeout(function () {
                if (window.external && ('openSecondWebview' in window.external)) {
                    window.external.openSecondWebview(JSON.stringify({url: url}));
                } else {
                    location.href = url;
                }
            },200);

            YQ.voxLogs({
                database: 'parent',
                module: 'm_sMNiwxrS',
                op: "o_W3rspFOd",
                s0: getQuery('goodsId')
            });
        });

        YQ.voxLogs({
            database: 'parent',
            module: 'm_sMNiwxrS',
            op: "o_uN1QS66E",
            s0: getQuery('goodsId'),
            s1: getQuery('_from') == 'goodsdetail' ? '爱读书商品列表页' : '专题活动页'
        });
    };
</script>
</@layout.page>