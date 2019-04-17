<#import "../../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='压岁钱'
pageJs=["luckymoney"]
pageJsFile={"luckymoney" : "public/script/mobile/student/activity/luckymoney/luckymoney"}
pageCssFile={"luckymoney" : ["public/skin/mobile/student/app/activity/luckymoney/css/skin"]}
>
<@app.script href="/public/plugin/vue/2.1.6/vue.min.js" />
<div id="index">
    <div class="luckyMoney-header" v-if="!dataReady">
        <div class="ball-pulse-sync" style="margin-top: 5rem;">
            <div></div>
            <div></div>
            <div></div>
        </div>
    </div>
    <template v-else>
    <#-- 已领取 -->
        <div class="luckyMoney-header" v-if="received">
            <div class="lm-details">
                <div class="m-icon"></div>
                <div class="info">
                    <p class="name">{{studentName}}共拥有</p>
                    <p class="count"><span>{{usable}}</span>自学积分</p>
                    <div class="btnBox">
                        <a href="javascript:void(0);" class="log-rule anchor btn">兑换预览</a>
                        <a href="javascript:void(0);" class="log-draw btn" style="background: #bfbfbf">去抽奖</a>
                    </div>
                    <p style="padding-bottom: .8rem;" v-if="!finished">
                        <a href="javascript:void(0);" class="log-buy anchor link">获取更多自学积分</a>
                    </p>
                </div>
            </div>
        </div>
    <#-- 未领取 -->
        <div class="luckyMoney-header h-bg" v-else>
            <div class="lm-drawBox animated" :class="{bubble: locked}"><span class="btn receive-credit"></span></div>
            <#--<div class="lm-drawBox animated bubble"><span class="btn receive-credit" @click="receive" :class="{'locked':locked}"></span></div>-->
        </div>
    </template>

<#-- 压岁钱领取弹窗 -->
    <div class="luckyMoney-pop" style="display: none;" v-show="popup">
        <div class="inner">
            <div class="lClose" @click="closePopup"></div>
            <div class="lTop"></div>
            <div class="lMain">
                <h1>恭喜你啦</h1>
                <P class="sub">收到红包压岁钱</P>
                <P class="num">{{amount}}自学积分</P>
            </div>
            <div class="lBtn">
                <a href="javascript:void(0);" class="green_btn" @click="closePopup">好的，我知道了</a>
            </div>
        </div>
    </div>
<#-- 我的压岁钱历史 -->
    <div class="gf-section">
        <div class="title">压岁钱历史</div>
        <p style="text-align: center;font-size: .75rem;" v-if="dataReady && !histories.length">暂无压岁钱历史</p>
        <ul class="lm-list-wrapper" style="display:none;" v-show="dataReady">
            <li class="lm-list" v-for="item in histories">
                <div class="right">{{item.amount}}自学积分</div>
                <div class="left">
                    <p class="txt">{{item.resource}}<span style="font-size:.5rem;color:#a0a0a0;margin-left:.4rem;">{{item.date}}</span></p>
                </div>
            </li>
        </ul>
    </div>
<#-- 同学领取动态 -->
    <div class="gf-section">
        <div class="title">我的同学</div>
        <p style="text-align: center;font-size: .75rem;" v-if="dataReady && !latest.length">暂无同学动态</p>
        <ul class="lm-list-wrapper" style="display:none" v-show="dataReady">
            <li class="lm-list" v-for="item in latest">
                <div class="right">{{item.amount}}自学积分</div>
                <div class="left">
                    <div class="avatar"><img :src="item.img ? '<@app.avatar href='/'/>' + item.img : '<@app.avatar href=''/>'"></div>
                    <div class="info">
                        <p class="name">{{item.name}}</p>
                        <p class="state">{{item.resource}}</p>
                    </div>
                </div>
            </li>
        </ul>
    </div>
</div>
<script>
    (function(){
        var vm = new Vue({
            el: '#index',
            data: {
                dataReady: false,           //Vue实例化是否完成
//                dataLoaded: false,        //Ajax加载数据是否完成
                popup: false,
                amount:0,
                locked: false,      //点击领取按钮

                finish: false,
                received: false,
                studentName: '...',
                usable: 0,
                histories: [],
                latest: []
            },
            methods: {
                closePopup: function(){
                    vm.popup = false;
//                    vm.received = true;
                }
            }
        });
        window.indexVM = vm;
    })();
</script>
</@layout.page>