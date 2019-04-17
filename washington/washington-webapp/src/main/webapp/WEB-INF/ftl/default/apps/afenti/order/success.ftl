<#import "module.ftl" as com>
<@com.page step=3 title="支付成功" paymentType="${payment!''}">
    <div class="main">
    <div class="payMainBox">
        <div class="curaddress">购买成功</div>
        <div class="tabbox">
            <!--Purchase success-->
            <div class="tabLevel">
                <div class="successBox">
                    <s class="iblock ireceiving"></s>
                    <div class="content">
                        <#if payment??>
                            <p class="txt">支付成功！</p>
                            <p class="ctn">您已经帮 ${studentName}（一起作业号：${studentId}）开通了${productName} ${productDate}天的使用权，赶快告诉他吧！</p>
                            <#if productType?has_content && productType =='AfentiExam' && pkGiftPacks!false>
                                <span class="ctn">
                                    恭喜你同时获得PK大礼包，你可以去PK馆里查看哦。
                                </span>
                            </#if>
                            <script type="text/javascript">
                                $(function(){
                                    $17.tongji("找人代付-支付成功-${productName!}")
                                });
                            </script>
                        <#else>
                            <p class="txt">购买成功！</p>
                            <#if productType?has_content && productType =='AfentiExam' && pkGiftPacks!false>
                                <span class="ctn">
                                    恭喜你同时获得PK大礼包，你可以去PK馆里查看哦。
                                </span>
                            </#if>
                            <#--<p class="ctn">您购买的产品在激活后方可使用，产品使用周期以激活时间作为起始。</p>-->
                            <#-- 这个url可能会跨payment和order，所以用相对路径 -->
                            <div class="btn"><a href="/student/center/order.vpage" class="publicBtn greenBtn"><i class="lB"></i><i class="tB"><span>查看已购买订单</span></i><i class="rB"></i></a></div>
                            <script type="text/javascript">
                                $(function(){
                                    $17.tongji("支付成功-${productName!}")
                                });
                            </script>
                        </#if>
                    </div>
                </div>
            </div>
            <!--//-->
        </div>

    </div>
</div>

</@com.page>
