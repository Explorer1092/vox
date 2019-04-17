<#import "../../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='抽奖'
pageJs=["luckymoney"]
pageJsFile={"luckymoney" : "public/script/mobile/student/activity/luckymoney/luckymoney"}
pageCssFile={"luckymoney" : ["public/skin/mobile/student/app/activity/luckymoney/css/skin"]}
>
<@app.script href="/public/plugin/vue/2.1.6/vue.min.js" />
<div id="lottery">
<#-- 抽奖转盘 -->
<div class="gf-lottery">
    <div class="lotteryBox" style="display:none" v-show="dataReady">
        <div class="lotteryList" v-for="(item,i) in awards" :class="['list-'+i, (pointer % 10 == i) ? 'active' : '']" :id="item.id"></div>
    </div>
    <div class="lotteryBtn">
        <a href="javascript:void(0)" class="btn draw-lottery" @click="drawLottery" :class="{'drawing': drawing}"></a>
    </div>
    <div class="tips">每次抽奖都会消耗100自学积分哦～每天只有一次机会哦！</div>
</div>
<#-- 抽奖结果弹窗01: 中奖提示 -->
<div class="gf-popup" style="display:none;" v-show="popup">
    <div class="gf-popupBox">
        <div class="hd"><span class="close" @click="closePopup"></span></div>
        <div class="mn">
            <div class="info bg">
                <div class="tips">
                    <div class="icon" :class="{'icon01': product.award.category == 'Li', 'icon02': product.award.category == 'Chuang'}"></div>
                    <div class="txt">{{product.award.name}}</div>
                </div>
            </div>
        </div>
        <div class="ft">
            <a href="javascript:void(0)" class="btn use-now" @click="closePopup" v-if="product.award.name === '谢谢参与'">我知道了</a>
            <a href="javascript:void(0)" class="btn use-now" @click="openApp(product)" v-else>立即使用</a>
        </div>
    </div>
</div>
<#-- 抽奖结果弹窗02: 错误提示 -->
<div class="gf-popup" style="display:none;" v-show="popupTip">
    <div class="gf-popupBox">
        <div class="hd"><span class="close" @click="closePopup"></span></div>
        <div class="mn">
            <div class="info">
                <div class="rules">
                    <h3 class="title">提示</h3>
                    <div class="text">{{popupText}}</div>
                </div>
            </div>
        </div>
        <div class="ft">
            <a href="javascript:void(0)" class="btn" @click="closePopup">好的</a>
        </div>
    </div>
</div>
<#-- 我的学习礼物 -->
<div class="gf-section">
    <div class="title">我的学习礼物</div>
    <div class="gf-empty" v-if="dataReady && !bingo.length"></div>
    <ul class="lm-list-wrapper" style="max-height:3.8rem;display:none" v-show="dataReady">
        <li class="gf-list" v-for="(item, index) in bingo">
            <i class="icon" :class="{'icon01': item.category == 'Li', 'icon02': item.category == 'Chuang'}"></i>
            <span class="txt">{{item.name}}</span>
            <a href="javascript:void(0)" class="btn use-btn" v-if="item.status=='AVAILABLE'" @click="openApp(item)">立即使用</a>
            <a href="javascript:void(0)" class="btn success used-btn" v-if="item.status=='USED'">已使用</a>
        </li>
    </ul>
</div>
<#-- 同学领取动态 -->
<div class="gf-section">
    <div class="title">同学领取动态</div>
    <div class="dyn-empty" v-if="dataReady && !latest.length"></div>
    <ul class="lm-list-wrapper" style="max-height:3.8rem;display:none" v-show="dataReady">
        <li class="dyn-list" v-for="item in latest">
            <img class="avatar" :src="item.studentImg ? '<@app.avatar href='/'/>' + item.studentImg : '<@app.avatar href=''/>'">
            <div class="txt01">
                <p class="name">{{item.studentName}}</p>
                <p class="grade">{{item.clazzName}}</p>
            </div>
            <div class="txt03"><span>{{item.date}}</span></div>
            <div class="txt02">{{item.awardName}}</div>
        </li>
    </ul>
</div>
</div>
<script>
    (function(){
        // 缓冲函数，t: current time, b: beginning value, c: change in value, d: duration
        function easeInQuart(t, b, c, d) {return c*(t/=d)*t*t*t + b;}

        var vm = new Vue({
            el: '#lottery',
            data: {
                dataReady: false,
                pointer: 0,         //转动active指针
                popup: false,       //是否弹出中奖弹窗
                popupTip: false,    //是否弹出提示弹窗
                popupText: '...',      //提示弹窗提示语
                drawing: false,      //是否正在抽奖转动中
                n: -1,               //第几个格子是中奖项
                timer: null,         //setTimeout用
                product: {           //中奖项
                    award: {}
                },

                freeChance: 0,
                awards: [],
                bingo: [],
                latest: []
            },
            methods: {
                //抽奖转动函数，count: 共有多少个奖品，callback: 转动结束回调
                roll: function(count, callback){
                    callback = callback || function(){};

                    if(vm.pointer >= 3*count && vm.pointer % count === vm.n){       //递归终止条件
                        clearTimeout(vm.timer);
                        callback();
                        return;
                    }

                    vm.pointer+=1;
                    vm.timer = setTimeout(function(){
                        vm.roll(count, callback);
                    },easeInQuart(vm.pointer, 0.1, 1, 3*count + vm.n )*468);
                },
                stopRoll: function(){
                    clearTimeout(vm.timer);                      //停止转动
                    vm.drawing = false;                          //解锁抽奖按钮
                    vm.pointer = 0;                              //复位
                },
                //开始抽奖
                drawLottery: function(){
                    if(!vm.dataReady){         //如果数据还没加载完毕
                        return;
                    }
                    if(!vm.freeChance){       //如果没有抽奖次数，弹窗提示
                        vm.openPopupTip('今日领奖次数已用完，明天再来吧');
                        return;
                    }
                    if(vm.drawing){           //如果抽奖按钮是锁住状态
                        return;
                    }
                    lotteryVM.roll(10, function(){                         //开始转动抽奖
                        vm.openPopup();                             //转动结束时展示弹窗
                        vm.drawing = false;                         //解锁抽奖按钮
                        vm.initData();
                    });
                },
                openPopup: function(){
                    vm.popup = true;
                },
                openPopupTip: function(msg){
                    vm.popupTip = true;
                    vm.popupText = msg;
                },
                closePopup: function(){
                    vm.pointer = 0;           //从左上角第一个开始转起
                    vm.popup=false;
                    vm.popupTip=false;
                },
                //ajax初始化数据，在luckymoney.js中实现
                initData: function(){},
                openApp: function(){}          //打开自学乐园应用
            }
        });
        window.lotteryVM = vm;
    })();
</script>
</@layout.page>


