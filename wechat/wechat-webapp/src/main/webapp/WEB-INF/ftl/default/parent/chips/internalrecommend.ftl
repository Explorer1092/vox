<#import "../layout.ftl" as layout>
<@layout.page title="推荐" pageJs="chipsRecommend">
    <@sugar.capsule css=["chipsAll"] />

<style>
    [v-cloak]{display: none;}
</style>

<div class="pushWrap" id="recommend" v-cloak="">
    <div class="pushHead">
        <div class="redPacket" @click="openBook"></div>
        <a href="/chips/center/recommend.vpage"><div class="myRecommend"></div></a>
    </div>
    <div class="pushMain">
        <p>{{showPlay ? '↑↑点击上方配套教材，可以提前预习哦~' :  '还差1人，可查看电子教材'}}</p>
        <a href="/chips/center/invite.vpage"><div class="mainTitle"></div></a>
        <div class="mainPrize"></div>
        <div class="mainRule">
            <div class="ruleTitle"><span>活动参与规则：</span></div>
            <div class="ruleText">
                <p>1. 成功推荐1人报名可得课程配套电子教材和9.9现金优惠券</p>
                <p>2. 成功推荐10人及10人以上者，可得现金红包</p>
                <p class="money">红包金额=推荐人数*9.9元</p>
                <p>3. 现金优惠券可用于下次购买课程</p>
                <p>4. 本活动最终解释权归薯条英语。</p>
            </div>
        </div>
    </div>
    <div class="pushFoot" v-if="rankList.length>=5">
        <div class="pushFootInner">
            <div class="rankTitle"></div>
            <div class="rankContainer">
                <p class="updateTime">每周一00:00更新</p>
                <div class="rankHead">
                    <div class="column01">名次</div>
                    <div class="column02">ID</div>
                    <div class="column03">推荐人数</div>
                    <div class="column04">获得奖励</div>
                </div>
                <ul class="rankBox">
                    <li v-for="(item,index) in rankList" :key="index">
                        <div class="liBox">
                            <div class="column01"><i class="num" :class="{num01:index === 0,num02:index === 1,num03:index === 2}" v-if="index <= 2"></i>{{ index <= 2 ? '': index+1 }}</div>
                            <div class="column02">{{ item.userName }}</div>
                            <div class="column03">{{ item.number }}</div>
                            <div class="column04">{{ Math.ceil(item.number*99)/10  }}元</div>
                        </div>
                    </li>

                    <#--
                    <li>
                        <div class="liBox">
                            <div class="column01"><i class="num num01"></i></div>
                            <div class="column02">涂为明</div>
                            <div class="column03">20</div>
                            <div class="column04">2000元</div>
                        </div>
                    </li>
                    <li>
                        <div class="liBox">
                            <div class="column01"><i class="num num02"></i></div>
                            <div class="column02">大地</div>
                            <div class="column03">19</div>
                            <div class="column04">1500元</div>
                        </div>
                    </li>
                    <li>
                        <div class="liBox">
                            <div class="column01"><i class="num num03"></i></div>
                            <div class="column02">李姥姥爱林的话李姥姥爱林的话</div>
                            <div class="column03">10</div>
                            <div class="column04">1000元</div>
                        </div>
                    </li>
                    <li>
                        <div class="liBox">
                            <div class="column01">4</div>
                            <div class="column02">王娜娜的小花猫</div>
                            <div class="column03">9</div>
                            <div class="column04">800元</div>
                        </div>
                    </li>
                    <li>
                        <div class="liBox">
                            <div class="column01">5</div>
                            <div class="column02">名字</div>
                            <div class="column03">8</div>
                            <div class="column04">500元</div>
                        </div>
                    </li>
                    -->
                </ul>
            </div>
        </div>
    </div>

    <!--  toast弹窗  -->
    <div class="scene-tip" v-if="toast" style="text-align:center;font-size: 0.65rem;width: 70%;background: rgba(36,36,36,0.9);margin: 0 auto;border-radius: 0.3rem;color: #fff;padding: 1rem;position: fixed;top: 50%;left: 50%;box-sizing: border-box;transform: translate(-50%,-50%);z-index: 999;">
        <p class="scene-tip-txt">{{ toast_txt }}</p>
    </div>

</div>

</@layout.page>

