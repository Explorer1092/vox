<#import "../layout.ftl" as layout>
<@layout.page title="薯条英语" pageJs="chipsFormalGroupBuy">
    <@sugar.capsule css=["chipsFormalGroupBuy", "swiper3"] />

<style>
    [v-cloak] { display: none }
    .hide{display: none;}
    [v-cloak] { display: none }
    body,html{
        overflow-x: hidden;
    }

    .grade-card {
        background: #FFFFFF;
        box-shadow: 0 16px 32px 0 rgba(0,0,0,0.10);
        border-radius: 8px;
        width: 90%;
        margin: 0 auto 1.5rem auto;
    }
    .card-header {
        background: #FFF177;
        padding: 1rem 0.75rem;
        font-size: 0.8rem;
        border-radius: 8px 8px 0 0;
        font-weight: bold;
    }
    .product-name {
        margin-bottom: 0.3rem;
    }
    .words-count {
        font-size: 0.7rem;
    }
    .product-content {
        padding: 0.8rem 1rem;
        font-size: 0.65rem;
        color: #333333;
    }
    .target {
        margin-bottom: 0.4rem;
        font-size: 0.7rem;
        font-weight: bold;
    }
    .list-item {
        margin-bottom: 0.2rem;
        display: flex;
        align-items: center;
    }
    .list-item:before {
        content: '';
        display: inline-block;
        width: 4px;
        height: 4px;
        background: #333333;
        border-radius: 100%;
        margin-right: 10px;
    }
</style>

<div id="formal_group_buy" class="new_be_warp" v-cloak>
    <div class="top" style="position: relative;">
        <div class="top_banner"></div>
    </div>
    <div class="group-wrapper" v-if="type === 'default' && groupList.length > 0">
        <div class="group-content" @click="onGroup($event)">
            <div class="group-title">可直接参与拼团</div>
            <div class="swiper-container" style="height: 7rem;">
                <div class="swiper-wrapper">
                    <div class="user-item swiper-slide " v-for="item in groupList">
                        <img class="avatar" v-bind:src="item.image" alt="">
                        <div class="user-name">{{ item.userName }}</div>
                        <div style="text-align: right;margin-right: 0.3rem;font-weight: 500;">
                            <div>还差<span style="color: red;">1人</span>成团</div>
                            <div style="font-size: 0.4rem;color:rgba(156,156,156,1);">{{ item.surplusTime | timeFormat }}结束</div>
                        </div>
                        <div class="group-purchase-btn" v-bind:data-code="item.code">去拼团</div>
                        <#-- @click="onBuy(item.code, $event)" -->
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="group-wrapper" v-if="type === 'invite'">
        <div class="group-content" style="padding-top: 1.5rem;">
            <div style="text-align: center;">
                <div class="partner-item">
                    <img class="partner-avatar" v-bind:src="partnerInfo.image" alt="">
                    <div class="partner-label">
                        团长
                    </div>
                    <div style="font-size: 0.7rem;color: rgba(74,74,74,1);margin-top: 0.5rem;">{{ partnerInfo.userName }}</div>
                </div>
                <div class="partner-item" style="vertical-align: top;margin-left: 1rem;">
                    <div class="waiting-avatar">?</div>
                </div>
            </div>
            <div style="font-weight:500;color:rgba(74,74,74,1);font-size: 0.8rem;text-align: center;margin-top: 1rem;padding-bottom: 1rem;">
                拼团中，还差1人成团，{{ partnerInfo.surplusTime | timeFormat }}后结束
            </div>
        </div>
    </div>

    <div id="middle" class="middle">
        <div id="tab" class="tab" @click="switchTab">
            <div id="introduce" class="tab_item introduce" v-bind:class="{active: tab === 'introduce'}">课程介绍</div>
            <div id="list" class="tab_item list" v-bind:class="{active: tab === 'list'}">课程列表</div>
            <div class="active_bottom" v-bind:class="tab"></div>
        </div>
        <div class="tab_content">
            <div class="content_item course_introduce" v-if="tab==='introduce'">
                <div class="title">
                    <p class="content">课程特色</p>
                </div>
                <div class="item">
                    <p class="subtitle">课程划分等级，不同能力等级的孩子都可以找到适合自己的课程；</p>
                    <img width="100%" src="/public/images/parent/chips/formal_group_buy/level.png" alt="">
                </div>
                <div class="item">
                    <p class="subtitle">对标三一口语考试相应能力等级，让口语学习更具有目的性；</p>
                    <img width="100%" src="/public/images/parent/chips/formal_group_buy/ability.png" alt="">
                </div>

                <div class="title">
                    <p class="content">与短期课有什么不同</p>
                </div>
                <div class="item">
                    <p class="h2-title">正式课为系统课程</p>
                    <p class="text" style="margin-left: 1.5rem;">根据伦敦三一口语（GESE）各等级测试编写系统体系课程，学完对应课程即可参加三一口语对应级别考试。</p>
                </div>
                <div class="item">
                    <p class="h2-title">对接中考听力口语</p>
                    <p class="text" style="margin-left: 1.5rem;">针对中考失分多又难以快速提升的听力口语能力，薯条英语系统课贴近中考题型进行编写，1-5 级可以完全覆盖中考听力口语所有考试范围。</p>
                </div>
                <div class="item">
                    <p class="h2-title">充分实战演练</p>
                    <p class="text" style="margin-left: 1.5rem;">每个情景对话都增加巩固训练、每两周针对学过的知识进行一次模拟考，由外教口语考官模拟三一口语考场实战场景，检验口语学习效果。</p>
                </div>
                <div class="item">
                    <p class="h2-title">一对一专属服务</p>
                    <p class="text" style="margin-left: 1.5rem;">完成 App 学习内容后，会有专属老师对练习结果给与专业指导，并定期针对对话、模拟考和语法存在的问题给予督导和纠正。</p>
                </div>

                <div class="title">
                    <p class="content">配套纸质教材包邮到家</p>
                </div>
                <div style="font-size: 0.75rem;color: #4A4A4A;margin: 0 1rem;">系统课配套精美原版教材，包邮到家，先报名先安排配送。</div>
                <div class="img_box">
                    <img  src="/public/images/parent/chips/formal_group_buy/new_be_gift.png" alt="">
                </div>

                <div class="title">
                    <p class="content">购买须知</p>
                </div>
                <div class="item">
                    <p class="text">1.购买后请务必关注【薯条英语公众号】并添加你的【专属班主任老师】;</p>
                    <p class="text">2.课程每天0点开始更新;</p>
                    <p class="text">3.开课2天内无条件退款;</p>
                    <p class="text">4.课程有效期3年.</p>
                </div>
            </div>
            <div class="content_item course_list" v-if="tab==='list'">
                <div class="grade-card">
                    <div class="card-header">
                        <div class="product-name">初到美国插班生活体验（Grade1）</div>
                        <div class="words-count">词汇量：300以内</div>
                    </div>
                    <div class="product-content">
                        <div class="target">达成目标</div>
                        <div class="list-item">学会问候和介绍和告别用语</div>
                        <div class="list-item">说出姓名和年龄</div>
                        <div class="list-item">辨认和说出所示物品和动物的名称</div>
                        <div class="list-item">学会数数</div>
                        <div class="list-item">辨认和说出颜色及服装</div>
                    </div>
                </div>
                <div class="grade-card">
                    <div class="card-header">
                        <div class="product-name">成为寄宿家庭生活一员（Grade2）</div>
                        <div class="words-count">词汇量：300-500</div>
                    </div>
                    <div class="product-content">
                        <div class="target">达成目标</div>
                        <div class="list-item">简单描述人、动物、物品、地点、场景</div>
                        <div class="list-item">表述简单的事实</div>
                        <div class="list-item">描绘个人所拥有的物品</div>
                        <div class="list-item">简单提问有关个人情况的问题</div>
                        <div class="list-item">学会说&nbsp;50&nbsp;以内的数字以及表达日期</div>
                    </div>
                </div>
                <div class="grade-card">
                    <div class="card-header">
                        <div class="product-name">在美国的第一个暑假（Grade3）</div>
                        <div class="words-count">词汇量：500-1000</div>
                    </div>
                    <div class="product-content">
                        <div class="target">达成目标</div>
                        <div class="list-item">学会描述日常生活及任意时间</div>
                        <div class="list-item">表达“能”与“不能”</div>
                        <div class="list-item">简单地指示方向和地点</div>
                        <div class="list-item">描述正在进行的活动</div>
                        <div class="list-item">表述过去的状态</div>
                        <div class="list-item">就日常生活简单地提问</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="dialog" @click="closeDialog" v-bind:class="{dialog_show: dialogShow}">
        <div class="content" @click.stop="blank">
            <p class="dialog_title">请选择课程</p>
            <div class="dialog_course">
                <div class="dialog_course_item" v-for="item in groupByGrade">
                    <p class="dialog_course_item_title">{{ item.name }}</p>
                    <div class="sub_item" v-for="subItem in item.products" v-bind:class="{'active': productId === subItem.productId}"
                        @click="onSelectProduct(subItem.productId, $event)">
                        {{ subItem.name }}|{{subItem.courses}}课时
                    </div>
                </div>
            </div>
            <p class="dialog_time">开课时间：{{ currentProduct.beginDate | normalTime }}</p>
        </div>
    </div>

    <div class="already-success-dialog" v-if="groupSuccess">
        <div class="already-wrapper">
            <div style="font-size: 1rem;font-weight: bold;margin-bottom: 0.6rem;text-align: center;">该拼团已结束</div>
            <div v-if="directGroupList && !!directGroupList.length">
                <div style="font-size: 0.9rem;margin-bottom: 1.2rem;text-align: center;">您可以查看其它拼团</div>
                <div style="border-bottom: 2px #EFEFEF solid;padding: 0 0 0.5rem 0.8rem;font-size: 0.8rem;">可直接参与的拼团</div>
                <div>
                    <div class="user-item" v-for="item in directGroupList">
                        <img class="avatar" v-bind:src="item.image" alt="">
                        <div class="user-name">{{ item.userName }}</div>
                        <div style="text-align: right;margin-right: 0.3rem;font-weight: 500;">
                            <div>还差<span style="color: red;">1人</span>成团</div>
                            <div style="font-size: 0.4rem;color:rgba(156,156,156,1);">{{ item.surplusTime | timeFormat }}结束</div>
                        </div>
                        <div class="group-purchase-btn" v-bind:data-code="item.code" @click="onDirectGroup(item.code)">去拼团</div>
                    </div>
                </div>
            </div>
            <div v-else style="text-align: center;font-size: 0.8rem;">
                <div>当前没有可拼的团，快去开个团吧！</div>
                <div style="margin-top: 0.5rem;color: #565fff;">
                    <span id="auto-direct-second">3</span>s后自动跳转
                </div>
            </div>
        </div>
    </div>

    <div class="bottom" v-if="!groupSuccess">
        <div class="left" @click="onBuy(false, $event)" v-if="type === 'default'">
            ¥{{ productId ? currentProduct.originalPrice : leastPrice.originalPrice }}元&nbsp;原价购买</p>
        </div>
        <div class="left plain" v-if="type !== 'default'">
            原价<span class="original-price">{{ productId ? currentProduct.originalPrice : leastPrice.originalPrice }}</span>
        </div>
        <div class="right" @click="onBuy('TEMP', $event)" >
            ¥{{ productId ? currentProduct.discountPrice : leastPrice.discountPrice }}元&nbsp;{{ buyBtnText }}</p>
        </div>
    </div>
</div>
<script src="/public/js/utils/weixin-1.4.0.js"></script>
<script type="text/javascript">
    var type = '${type!''}';
    wx.config({
        debug: false,
        appId: '${appid!""}',
        timestamp:'${timestamp!""}',
        nonceStr: '${nonceStr!""}',
        signature: '${signature!""}',
        jsApiList: [
            'updateAppMessageShareData',
            'updateTimelineShareData',
            'checkJsApi',
            'onMenuShareTimeline',
            'onMenuShareQQ',
            'onMenuShareAppMessage'
        ]
    });

    wx.ready(function () {   //需在用户可能点击分享按钮前就先调用
        var title = '每天10分钟让英语脱口而出';
        if(location.search.indexOf('origin=invite') >= 0) {
            title = '仅差1人拼团成功！每天10分钟让英语脱口而出';
        }
        wx.updateAppMessageShareData({ 
            title: title, // 分享标题
            link: location.href,
            desc: '点击参与拼团，最高减600元', // 分享描述
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            success: function () {},
            fail: function(err) {
                console.log(err)
            }
        });
        wx.updateTimelineShareData({ 
            title: title, // 分享标题
            link: location.href,
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            success: function () {},
            fail: function(err) {
                console.log(err)
            }
        });
        wx.onMenuShareAppMessage({ 
            title: title, // 分享标题
            link: location.href,
            desc: '点击参与拼团，最高减600元', // 分享描述
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            success: function () {},
            fail: function(err) {
                console.log(err)
            }
        });
        wx.onMenuShareTimeline({
            title: title, // 分享标题
            link: location.href,
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            success: function () {},
            fail: function(err) {
                console.log(err)
            }
        });
        wx.onMenuShareQQ({
            title: title, // 分享标题
            link: location.href,
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            success: function () {},
            fail: function(err) {
                console.log(err)
            }
        });
    });
</script>
</@layout.page>

<#--</@chipsIndex.page>-->
