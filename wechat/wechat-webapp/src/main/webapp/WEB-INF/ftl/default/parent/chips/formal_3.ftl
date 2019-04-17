<#import "../layout.ftl" as layout>
<@layout.page title="薯条英语" pageJs="chipsFormal3Be">
    <@sugar.capsule css=["chipsFormal3Be"] />

<style>
    [v-cloak] { display: none }
    body,html{
        overflow-x: hidden;
    }
       .swiperBox{
        padding: 0 1.5rem !important;
        height: 18rem !important;
        position: relative;
    }
    .slide-ctnr {
        margin-top: 0.959rem;
        height: 20rem;
        width: 100%;
        display: -webkit-flex;
        display: flex;
        overflow: visible;
        position: relative;
        touch-action: auto;
    }
    .slide-img {
        position: absolute;
        top: -.481rem;
        height: 18rem;
        width: 12.3rem;
        z-index: 4;
        transition: all 0.3s;
        -webkit-transition: all 0.3s;
        pointer-events: none;
        opacity: 0;
    }
    .slide-img img{
        box-shadow: 5px 5px 15px rgba(0,0,0,0.1);
        -webkit-box-shadow: 5px 5px 15px rgba(0,0,0,0.1);
        -webkit-border-radius: 0.5rem;
        -moz-border-radius: 0.5rem;
        border-radius: 0.5rem;
    }

    .slide-img.first {
        opacity: 0;
        -webkit-transform: translateX(-120%);
        transform: translateX(-120%);
        z-index: 6;
    }
    .slide-img.second {
        opacity: 1;
        -webkit-transform: translateX(0);
        transform: translateX(0);
        z-index: 5;
    }

    .slide-img.third {
        opacity: .9;
        -webkit-transform: scale(.9) translateX(2.2rem);
        transform: scale(.9) translateX(2.2rem);
        z-index: 3;
    }

    .slide-img.fourth {
        opacity: .8;
        -webkit-transform: scale(.81) translateX(4.4rem);
        transform: scale(.81) translateX(4.4rem);
        z-index: 2;
    }

    .slide-img.fifth {
        opacity: 0;
        -webkit-transform: scale(.73) translateX(9rem);
        transform: scale(.73) translateX(9rem);
        z-index: 1;
    }

    .blur,
    .raw {
        display: block;
        height: 100%;
        width: 100%;
        object-fit: cover;
    }

    .trans {
        opacity: 0;
    }

    .slide-list-move {
        transition: all 0.3s;
    }
    .popup-layerBox{
        z-index: 99999;
    }
</style>
    <div id="formal-advertisement" class="formal-ad-wrapper" v-cloak>
        <div class="head" v-bind:class="{g2: grade === 2, g3: grade === 3}"></div>
        <div style="text-align: center;">
            <div class="circle-txt">新课解锁</div>
        </div>
        <div class="describe" style="margin-top: 1rem;">
            <div v-if="grade === 2" class="content-txt">
                通过上一级别的课程的学习，孩子已经学会了互相问候、说出年龄、道别用语以及表达身体部位，接下来我们来看下全新的课程内容~
            </div>
            <div v-if="grade === 3" class="content-txt">
                通过上一级别的课程的学习，孩子已经学会了讨论养宠物，日期和月份，50 以内的数字，描述正在做某事，描绘个人所拥有的物品等等，接下来我们来看下全新的课程内容~
            </div>
        </div>

        <div class="swiperBox">
            <div class="slide-ctnr" @touchstart="handleTouchStart()" @touchmove="handleTouchMove()" @touchend="handleTouchEnd()">
                <div class="ctnr slide-img" v-for="(item,index) in imgs" v-bind:class="{'first':index == sign,'second':index == nextSign0,'third':index == nextSign1,'fourth':index == nextSign2,'fifth':index == nextSign3}">
                    <img v-bind:src="item" class="raw" alt="image" />
                </div>
            </div>
        </div>

        <div class="describe" style="margin-top: 2rem;">
            <div class="h1-title">课程详情</div>
            <div v-if="grade === 2" class="content-txt">
                以寄宿生活为主题，在 Tom 家寄宿，和 Tom 的爸爸 Jack 妈妈 Emily 一起生活，讨论养宠物，日期和月份，50 以内的数字，描述正在做某事，描绘个人所拥有的物品等等。轻松掌握三一口语（GESE）2 级口语技能。
            </div>
            <div v-if="grade === 3" class="content-txt">
                以暑假生活为主题，孩子与 Tom 和 Lily 讨论家庭生活，学习场所，时间和日期，以及兴趣爱好，天气等等。在日常生活话题中轻松掌握三一口语（GESE）3 级口语技能。
            </div>
        </div>
        <div class="describe" style="margin-top: 2rem;">
            <div v-if="grade === 2" class="card">
                <div class="card-title">
                    <div style="font-size: 0.8rem; font-weight: bolder;">成为寄宿家庭生活一员（Grade2）</div>
                    <div style="font-size: 0.7rem; font-weight: 500;">词汇量：300-500</div>    
                </div>
                <div class="card-content">
                    <div>达成目标</div>
                    <ol>
                        <li>简单描述人、动物、物品、地点、场景</li>
                        <li>表述简单的事实</li>
                        <li>描绘个人所拥有的物品</li>
                        <li>简单提问有关个人情况的问题</li>
                        <li>学会说 50 以内的数字以及表达日期</li>
                    </ol>
                </div>
            </div>
        </div>
        <div v-if="grade === 3" class="describe" style="margin-top: 2rem;">
            <div class="card">
                <div class="card-title">
                    <div style="font-size: 0.8rem; font-weight: bolder;">在美国的第一个暑假（Grade3）</div>
                    <div style="font-size: 0.7rem; font-weight: 500;">词汇量：500-1000</div>    
                </div>
                <div class="card-content">
                    <div>达成目标</div>
                    <ol>
                        <li>学会描述日常活动及任意时间</li>
                        <li>表达“能”与“不能”</li>
                        <li>简单地指示方向和地点</li>
                        <li>描述正在进行的活动</li>
                        <li>表述过去的状态</li>
                        <li>就日常生活简单地提问</li>
                    </ol>
                </div>
            </div>
        </div>

        <div style="text-align: center;">
            <div class="circle-txt">课程特色</div>
        </div>
        <div class="describe" style="margin-top: 0.5rem;">
            <div class="sub-title">全面升级为保过班</div>
            <div class="content-txt">
                薯条英语课程全面升级为保过班！学完对应课程即可参加伦敦三一口语（GESE）相应级别的考试，考试不通过可以免费再学一次！
            </div>
        </div>
        <div class="describe" style="margin-top: 0.5rem;">
            <div class="sub-title">对接中考听力口语</div>
            <div class="content-txt">
                针对中考失分多又难以快速提升的听力口语能力，薯条英语系统课贴近中考题型进行编写，1-5 级可以完全覆盖中考听力口语所有考试范围。
            </div>
        </div>
        <div class="describe" style="margin-top: 0.5rem;">
            <div class="sub-title">充分实战演练</div>
            <div class="content-txt">
                每个情景对话都增加巩固训练、每两周针对学过的知识进行一次模拟考，由外教口语考官模拟三一口语考场实战场景，检验口语学习效果。
            </div>
        </div>
        <div class="describe" style="margin-top: 0.5rem;">
            <div class="sub-title">一对一专属服务</div>
            <div class="content-txt">
                完成 App 学习内容后，会有专属老师对练习结果给与专业指导，并定期针对对话、模拟考和语法存在的问题给予督导和纠正。
            </div>
        </div>

        <div style="text-align: center;margin-bottom: 1rem;">
            <div class="circle-txt">续报优惠</div>
        </div>
        <div class="describe" style="margin-top: 0.5rem;">
            <div class="sub-title">11月30日之前购买可以额外获 299 元知识礼包</div>
            <div class="content-txt" style="text-align: center;">
                精美学习礼包，包邮到家，包含单词卡片 127 张，亲子互动对话卡片 40-60 张。
            </div>
            <img width="100%" src="/public/images/parent/chips/formal_3/renewal.png" alt="">
        </div>

        <div style="text-align: center;margin-bottom: 1rem;">
            <div class="circle-txt">购买须知</div>
        </div>
        <ol class="describe" style="margin-bottom: 3.5rem;list-style-type: none;">
            <li>1.&nbsp;购买后请务必关注【薯条英语公众号】并添加你的【专属班主任老师】；</li>
            <li>2.&nbsp;课程每天 0 点开始更新；</li>
            <li>3.&nbsp;开课 2 天内无条件退款；</li>
            <li>4.&nbsp;课程有效期 3 年。</li>
        </ol>
        <div class="footer">
            <div class="origin-price">保过班价格：{{ originalPrice || '--' }}元</div>
            <div class="real-price" @click.stop="buy">¥{{ price || '--' }}&nbsp;&nbsp;限时抢购</div>
        </div>
    </div>

<script type="text/javascript">
    var type = '${type!''}';
</script>
</@layout.page>

<#--</@chipsIndex.page>-->
