<#import "../layout.ftl" as winterCamp>
<@winterCamp.page title="2016成长冬令营" pageJs="">
    <@sugar.capsule css=['wintercamp'] />
    <style>
        html,body{ width: 100%; height: 100%;}
    </style>
    <div id="reservation_success_box" class="wc-wrap wc-bgYellow wc-success">
        <div class="successHeader"></div>
        <div class="successMain">
            <p>冬令营教育顾问将主动联系您</p>
            <p>为您提供专业产品咨询服务</p>
            <p class="tips">请保持电话畅通</p>
            <p class="btns">
                <a href="/parent/trustee/skupay.vpage?shopId=${shop.shopId!0}" class="wc-btnRed-s">立即购买</a>
                <a href="/parent/trustee/wintercamp/detail.vpage?shopId=${shop.shopId!0}" class="wc-btnBlue-s">查看详情</a>
            </p>
        </div>
    </div>
</@winterCamp.page>