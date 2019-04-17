<#import "../layout.ftl" as layout>
<@layout.page title="优惠券" pageJs="chipsCoupon">
    <@sugar.capsule css=['chipsAll'] />

<style>
    [v-cloak]{
        display: none;
    }
</style>

<div class="couponWrap" id="coupon" v-cloak="">
    <div class="couponNav">
        <div class="nav" @click="change_tab('NotUsed')" :class="{active:sign === 'NotUsed'}"><span>未使用</span></div>
        <div class="nav" @click="change_tab('Used')" :class="{active:sign === 'Used'}"><span>已使用</span></div>
        <div class="nav" @click="change_tab('Expired')" :class="{active:sign === 'Expired'}"><span>已过期</span></div>
    </div>
    <div class="couponMain">
        <template v-for="(item,index) in coupon_list">
            <template v-if="sign === 'NotUsed'">
                <!-- 未使用 -->
                <div class="couponList">
                    <div class="listLeft" @click="toggleSelect(item.couponUserRefId)">
                        <!-- active 选中 -->
                        <span class="chooseBtn" :class="{active:couponIds.indexOf(item.couponUserRefId) !== -1}"></span>
                    </div>
                    <div class="listInfo" @click="use_coupon">
                        <div class="infoLeft">
                            <div class="listTitle">{{ item.couponName }}</div>
                            <div class="listPrice">¥<span>9.9</span></div>
                            <span class="useWay">可用于购买薯条英语课程</span>
                        </div>
                        <div class="infoRight">
                            <div class="useBtn">去使用</div>
                            <div class="useTime">{{ item.effectiveEndTime | time }}到期</div>
                        </div>
                    </div>
                </div>
            </template>

            <template v-if="sign === 'used'">
                <!--
                    已使用、已提现、已过期状态下添加类名couponList-use
                    已使用、已提现都放在导航“已使用”下，
                    已过期放在导航“已过期”下
                 -->
                <div class="couponList couponList-use">
                    <div class="listLeft">
                        <span class="chooseBtn active" style="display: none"></span>
                    </div>
                    <div class="listInfo">
                        <div class="infoLeft">
                            <div class="listTitle">{{ item.couponName }}</div>
                            <div class="listPrice">¥<span>9.9</span></div>
                            <span class="useWay">可用于购买薯条英语课程</span>
                        </div>
                        <div class="infoRight">
                            <!-- useBtn02已使用 useBtn03已过期 useBtn04已提现 -->
                            <div class="useBtn02"></div>
                            <div class="useTime">{{ item.effectiveEndTime | time }}到期</div>
                        </div>
                    </div>
                </div>
            </template>
            <template v-if="sign === 'Expired'">
                <div class="couponList couponList-use">
                    <div class="listLeft">
                        <span class="chooseBtn active" style="display: none"></span>
                    </div>
                    <div class="listInfo">
                        <div class="infoLeft">
                            <div class="listTitle">{{ item.couponName }}</div>
                            <div class="listPrice">¥<span>9.9</span></div>
                            <span class="useWay">可用于购买薯条英语课程</span>
                        </div>
                        <div class="infoRight">
                            <!-- useBtn02已使用 useBtn03已过期 useBtn04已提现 -->
                            <div class="useBtn02 useBtn03"></div>
                            <div class="useTime">{{ item.effectiveEndTime | time }}到期</div>
                        </div>
                    </div>
                </div>
            </template>
        </template>
    </div>


    <!-- 底部区域 -->
    <div class="couponFoot" v-if="sign === 'NotUsed'">
        <div class="footInner">
            <div class="footContent">
                <div class="left">
                    <div class="changeBox" @click="select_all"><i class="changeBtn" :class="{active:couponIds.length === coupon_all['NotUsed'].length}"></i>全选</div>
                    <div class="changeState">已选：{{ couponIds.length }}张</div>
                </div>
                <div class="right">
                    <div class="cashBtn" @click="withdraw_cash" :class="{active:couponIds.length>=10}">提现</div>
                    <div class="tips">优惠券大于10张即可提现</div>
                </div>
            </div>
        </div>
    </div>

    <!--  toast弹窗  -->
    <div class="scene-tip" v-if="toast" style="text-align:center;font-size: 0.65rem;width: 70%;background: #242424;margin: 0 auto;border-radius: 0.3rem;color: #fff;padding: 1rem;position: fixed;top: 50%;left: 50%;box-sizing: border-box;transform: translate(-50%,-50%);z-index: 999;">
        <p class="scene-tip-txt">{{ toast_txt }}</p>
    </div>


    <!-- 弹窗 确认提现 -->
    <div class="coupon-popup" v-if="withdraw_cash_popup">
        <div class="popInner">
            <div class="closeBtn" @click="close_popup"></div>
            <div class="popBox">
                <div class="popTitle">确认提现</div>
                <div class="popTip">确认提现后，您将于${ exchangeDate ! '' }在薯条英语公众号领取，请留意公众号消息。</div>
                <div class="popMoney">提现金额：¥ <span>{{ Math.ceil(couponIds.length*99)/10 }}</span></div>
                <div class="popBtn" @click="get_cash">提现</div>
            </div>
        </div>
    </div>
</div>



</@layout.page>

<#--</@chipsIndex.page>-->
