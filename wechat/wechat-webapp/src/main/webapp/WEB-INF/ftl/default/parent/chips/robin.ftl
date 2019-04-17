<#import "../layout.ftl" as layout>
<@layout.page title="薯条英语" pageJs="chipsAd">
    <@sugar.capsule css=["chipsAd"] />

<style>
    [v-cloak] { display: none }
    body,html{
        overflow-x: hidden;
    }
    .adFooter .footInner .footCont {
        overflow: inherit !important;
        height: 4rem;
    }
    .adContainer{
        -webkit-overflow-scrolling: touch;
    }
    .adFooter {
        height: 4.1rem;
        position: fixed;
        bottom: 0;
        width: 100%;
        z-index: 50;
    }
    #videoWrap{
        height: 100%;
        width:100%;
        overflow: hidden;
    }
    #videoWrap img{
        height: 100%;
        width:100%;
    }
    video{
        width:100% !important;
        /*height:100% !important;*/
    }
    .swiperBox{
        padding:0 1.5rem !important;
        height:21rem !important;
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
        left: -0.6rem;
        height: 20rem;
        width: 13.3rem;
        z-index: 4;
        transition: all 0.3s;
        -webkit-transition: all 0.3s;
        pointer-events: none;
        opacity:0;
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
    #video_img{
        display: none;
    }
</style>

    <div class="adContainer" id="ad" v-cloak style="overflow-y: auto;overflow-x: hidden">
        <!-- 视频 -->
        <div class="adVideo" @touchstart = 'play'>
            <div id="videoWrap">
                <video id="vid" src="//v.17zuoye.cn/ai-teacher/5b04e215c3666e174aeece36.mp4" preload="auto" autoplay="autoplay" loop="loop" muted="muted" poster="/public/images/parent/chips/ad-videoBg.jpg" webkit-playsinline="true" playsinline="true" x5-playsinline="true" x5-video-player-type="h5" x5-video-player-fullscreen="false" x5-video-orientation="portraint"></video>
                <img id="video_img" src="/public/images/parent/chips/ad-videoBg.jpg" alt="">
            </div>
            <div class="videoMessage">
                <p class="title-small">AI 情景对话</p>
                <div class="title-english">
                    <p class="english01">FRIES</p>
                    <p>ENGLISH</p>
                </div>
                <div class="title-chinese">薯条英语</div>
                <div class="beginBox">
                    <div class="beginText beginTime"><i></i>开课时间：${beginDate!''}</div>
                    <div class="beginText beginCycle"><i></i>课程时长：8天集训+90天有效期</div>
                    <div class="beginText beginCourse"><i></i>课程：${productName!''}</div>
                </div>
            </div>
            <!-- 新增 箭头 -->
            <div class="arrowIcon"></div>
        </div>
        <div class="adProblem">
            <p class="problemTitle">你的孩子有没有这样呢</p>
            <p>背烂了单词书</p>
            <p>英语考试98,一张口还是那句</p>
            <p>Fine thank you！ And you？</p>
            <p>花6千5报补习班，还是 开不了口？</p>
            <p>问路点餐，全靠演技</p>
        </div>

        <!-- 课程特色 -->
        <div class="adLanguage">
            <div class="adTitle">课程特色</div>
            <div class="paragraph paragraph02">薯条英语课程内容涵盖国外衣食住行，吃喝玩乐各种交际场景，场景化教学，更有老外1v1对话，身临其境感受英语口语学习的乐趣。</div>
            <div class="swiperBox">
                <div class="slide-ctnr" @touchstart="handleTouchStart()" @touchmove="handleTouchMove()" @touchend="handleTouchEnd()">
                    <div class="ctnr slide-img" v-for="(item,index) in imgs" v-bind:class="{'first':index == sign,'second':index == nextSign0,'third':index == nextSign1,'fourth':index == nextSign2,'fifth':index == nextSign3}">
                        <img v-bind:src="item" class="raw" alt="image" />
                    </div>
                </div>
            </div>
        </div>

        <!-- 情景对话学口语 -->
        <div class="adLanguage">
            <div class="adTitle adTitle02">情景对话学口语</div>
            <div class="paragraph paragraph02">薯条英语全程采用北美外教，通过AI技术，让孩子在手机上就可以随时完成和老外的 1v1 对话。</div>
            <div class="learnWays"></div>
        </div>

        <!-- 学完孩子有哪些收获？ -->
        <div class="adHarvest">
            <div class="adTitle adTitle03">学完孩子有哪些收获？</div>
            <div class="harvestList">
                <div class="harvestTitle"><i class="num">1</i>实用的<span>场景口语</span></div>
                <p class="harvestText">全方位涵盖生活场景，在情境中互动学习</p>
            </div>
            <div class="harvestList">
                <div class="harvestTitle"><i class="num num02">2</i>纯正的<span>文化知识</span></div>
                <p class="harvestText">在文化中学习语言，拓宽视野，增长见识</p>
            </div>
            <div class="harvestList">
                <div class="harvestTitle"><i class="num num03">3</i>良好的<span>开口习惯</span></div>
                <p class="harvestText">体系化课程培养开口习惯，每天都有新进步</p>
            </div>
        </div>


        <!-- 本期课程安排 -->
        <div class="adLanguage adSchedule" style="padding-bottom: 5rem;">
            <div class="adTitle adTitle04">本期课程安排</div>
            <p class="adText">报名截止时间：${sellOutDate!''}</p>
            <p class="adText02">（名额有限，售完即止）</p>
            <p class="adText">开课时间：${beginDate!''}</p>
            <p class="adText02">（课程有效期：${beginDate!''}—${endDate!''}）</p>
            <div class="dateBox">
                <ul>
                    <#if images ?? && (images?size > 0)>
                        <#list images as r >
                            <li><img src="${r}" alt=""></li>
                        </#list>
                    <#else >
                        <li><img src="/public/images/parent/chips/robin_8.png" alt=""></li>
                        <li><img src="/public/images/parent/chips/robin_9.png" alt=""></li>
                        <li><img src="/public/images/parent/chips/robin_10.png" alt=""></li>
                        <li><img src="/public/images/parent/chips/robin_11.png" alt=""></li>
                    </#if>

                </ul>
            </div>
            <div style="font-size: 0.6rem;text-align: center;color: #f98383;">
                <p>*开课2天内支持无理由退款*</p>
            </div>
        </div>

        <#if showBuy?? && showBuy>
            <#if sellOut?? && sellOut>
            <!-- 售罄状态 -->
            <div class="adFooter-finish">
                <div class="finishInner">
                    <div class="text">抱歉！课程卖完了～</div>
                    <div class="tips">去关注「薯条英语」微信公众号第一时间获得课程通知</div>
                </div>
            </div>
            <#else >
                <!-- 底部按钮 -->
                <div class="adFooter">
                    <div class="footInner">
                        <div class="footCont">
                            <#--<a href="/chips/center/studylist.vpage"><span class="listenBtn"></span></a>-->
                            <a href="/chips/order/create.vpage?productId=${productId ! ''}&inviter=${inviter ! ''}"><span class="priceBtn buy-btn" data-productid="${productId ! ''}" data-inviter="${inviter ! ''}"><i></i></span></a>
                            <div class="footTips">限量400人，预购从速</div>
                        </div>
                    </div>
                </div>
            </#if>

        </#if>

        <div class="loading" style="display:none">
            <div class="loadingBox">
                <div class="processBar">
                    <div class="pass" style="width: 50%;"></div>
                </div>
                <div class="percent">50%</div>
                <div class="txt">数据加载中....</div>
            </div>
        </div>
    </div>
</@layout.page>

<#--</@chipsIndex.page>-->
