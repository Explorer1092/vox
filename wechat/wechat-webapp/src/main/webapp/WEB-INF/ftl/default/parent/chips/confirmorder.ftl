<#import "../layout.ftl" as layout>
<@layout.page title="支付中" pageJs="chipsConfirmOrder">
    <@sugar.capsule css=["chipsConfirmOrder"] />

<style>
    [v-cloak] { display: none }
    .s_left{
        position: absolute !important;
    }
    .s_right{
        position: relative !important;
        height: 4.4rem !important;
        padding-left: 5.625rem !important;
    }
    .s_right .price{
        position: absolute !important;
        bottom: 0 !important;
    }
    .coupon_wrap .p_foot{
        position: fixed;
    }
</style>

<input id="orderid" type="hidden" value="${orderId!}">

<div style="height: 100%;" id="confirmorder" v-cloak="">
    <div class="pay_wrap" v-show="!select_coupon">
        <div class="p_header">
            <span class="b_text">订单时间: <i class="m_text">{{ createDate }}</i></span><span class="b_text right_line">待付款</span>
        </div>
        <div class="p_main">
            <div class="summary" >
                <div class="s_left"></div>
                <ul class="s_right">
                    <li class="b_text">{{ productName }}</li>
                    <li class="s_text" style="display: none;">Grandel(上)</li>
                    <li class="price"><i>￥</i>{{ price }}</li>
                </ul>
            </div>
            <div class="list_content">
                <div class="coupon">
                    <p class="xs_text">选择优惠方式</p>
                    <div @click="select">
                        <span class="b_text">优惠券: </span>
                        <#--<!--暂无优惠券&ndash;&gt;-->
                        <#--<span class="s_text right_line" style="display: none"></span>-->

                        <!--有优惠券--没有选择-->
                        <span class="s_text right_line" v-if="!iscoupon">
                            <template v-if="coupons.length > 0">
                                <i>{{ coupons.length }}</i> 张可用 <i class="icon_arrows"></i>
                            </template>
                            <template v-if="coupons.length <= 0">
                                暂无优惠券
                            </template>
                        </span>

                        <!--有优惠券 已选择-->
                        <span class="s_text right_line font_style" v-if="iscoupon">已选<i>{{ couponNum }}元</i>代金券<i class="icon_arrows"></i></span>
                    </div>
                </div>
                <div class="p_style"><span class="b_text">支付方式:</span><span class="right_line"> <i class="icon_pay"></i> </span> </div>
            </div>
        </div>
        <div class="p_foot">
            <div class="f_left b_text">实际支付: <span class="price"><i>￥</i>{{ discountPrice }}</span></div>
            <div class="f_right m_text"> <span @click="pay">付款</span> </div>
        </div>
    </div>

    <coupon v-bind:couponarr="coupons" v-show="select_coupon" v-on:confirm="usecoupon"></coupon>

</div>

<script id="coupon" type="text/html">
    <div class="coupon_wrap">
        <div class="c_header">
            <div class="remind">一个订单只能使用一张优惠券哦~</div>
        </div>
        <div class="c_main" style="padding-bottom: 4.575rem;">
            <div v-for="(item,index) in couponarr" class="couponList" @click="choseCoupon(index)">
                <div class="listLeft">
                    <!-- active 选中 -->
                    <span class="chooseBtn" :class="{active:index === sign}"></span>
                </div>
                <div class="listInfo">
                    <div class="infoLeft">
                        <div class="listPrice">¥<span>{{ item.typeValue }}</span></div>
                    </div>
                    <div class="infoRight">
                        <div class="listTitle">{{ item.couponName }}</div>
                        <div class="useTime">{{ item.effectiveDateStr }}</div>
                    </div>
                </div>
            </div>
        </div>
        <div class="p_foot">
            <div class="f_left b_text">默认已选: <span class="price"><i>￥</i>{{ couponPrice }}</span></div>
            <div class="f_right m_text"> <span @click="confirm">完成</span> </div>
        </div>
    </div>
</script>

</@layout.page>
