<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="兑换码"
bodyClass="couponpage"
pageCssFile={"reward" : ["public/skin/mobile/student/app/coupondetail/css/skin"]}
>
<div class="couponBox">
    <div class="couponHeader"></div>
    <div class="couponMain">
        <p>您的免费公开课兑换码是：<i><#if couponDetail?has_content>${couponDetail.couponNo!'--'}<#else>--</#if></i></p>
        <p>温馨提示：</p>
        <p>
            1、本活动有效期为：2017年6月30日前。<br>
            2、活动形式：手机APP兑换优惠码，线下参加活动（为保证安全需要家长进行陪同）。<br>
            3、兑换成功后，请联系火星人客服电话进行咨询：4000132391。<br>
            4、温馨提示：小朋友们兑换成功后不支持退订哦。<br>
            5、免费公开课过程中可能会有一些教具材料费用需要收取，具体情况火星人俱乐部会在电话预约时告知。<br>
            6、如有问题可与教学用品中心的冯老师联系：18510813430。<br>
        </p>
        <p>本活动最终解释权归一起作业所有 。</p>
    </div>
    <div class="couponHeight">
        <div class="couponFooter"></div>
    </div>
</div>
<script>
    if(window.external && window.external.updateTitle){
        window.external.updateTitle(document.title, "", "fee100");
    }
</script>
</@layout.page>