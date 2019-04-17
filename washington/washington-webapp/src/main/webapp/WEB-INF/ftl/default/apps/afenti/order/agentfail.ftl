<#import "module.ftl" as com>
<@com.page step=1 title="订单已失效 - 一起作业" paymentType="agent" stepOnOff = "Canceled">
<div class="main">
    <!--step2-->
    <div class="payMainBox">
        <div class="curaddress">订单已失效</div>
        <div class="tabbox">
            <div class="tabLevel productView">
                <!--product-->
                <div class="tabLevel">
                    <div class="successBox loseBox">
                        <s class="iblock iexclamation"></s>
                        <div class="content" style="margin-top: -45px;">
                            <p class="txt">订单已失效，已经被别人抢先付款啦！</p>
                        </div>
                    </div>
                </div>
                <!--//-->
            </div>
            <div class="clear"></div>
        </div>
    </div>
</div>
</@com.page>
