<#import "../layout.ftl" as trusteeMain>
<@trusteeMain.page title='规则说明' pageJs="paysuccess">
<@sugar.capsule css=['trusteetwo'] />
<div class="active-wrap active-bgpink">
    <div class="success-box">
        <div class="bg"></div>
        <h1><span class="icon"></span>恭喜你  支付成功！</h1>
        <a href="javascript:void(0)" class="active-btn-well js-continuePurchaseBtn">继续购买</a>
    </div>
</div>
</@trusteeMain.page>