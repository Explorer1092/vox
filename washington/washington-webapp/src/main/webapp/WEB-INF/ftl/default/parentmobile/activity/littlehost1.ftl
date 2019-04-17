<#import '../../parentmobile/layout.ftl' as layout>
<@layout.page className='sopcast' title="一对多直播课" pageJs="paycourseDesc">
<@sugar.capsule js=['jquery','voxLogs']/>
<#include "../constants.ftl">
${buildLoadStaticFileTag("sopcast", "css")}

<div class="sopcast-banner"></div>
    <#if hasPaid!false>
    <div class="sopcast-code">
        <div class="name">您已成功预约，我们会尽快与您联系</div>
        <div class="text">请扫描下面二维码加入微信群，以免错过课程</div>
        <div class="code">
            <img src="/public/skin/parentMobile/images/activity/sopcast/qrcode2.png">
        </div>
    </div>
    </#if>
<div class="sopcast-main sopcast-main01">
    <div class="tag">主讲老师</div>
    <div class="info">
        <div class="sc-base intro">
            <div class="intro-l"></div>
            <div class="intro-r">
                <p class="name">张倩</p>
                <p>播音主持专业，普通话一级甲等</p>
                <p>5年主持经验，4年授课经验</p>
            </div>
        </div>
        <div class="sc-base sc-yellow">
            <div class="sc-text">勇敢、自信、突破自我，我是主持人，我在线上课堂等你</div>
        </div>
        <div class="sc-base sc-white">
            <div class="sc-text">2016 北京“天才声小主持人培训”金牌讲师2014 “东方好BABY儿童嘉年华”主持人2014 天津市艺术展演特邀辅导员、声音美化师</div>
        </div>
        <div class="sc-base intro">
            <div class="show01"></div>
            <div class="show02"></div>
        </div>
    </div>
</div>
<div class="sopcast-main sopcast-main04">
    <div class="tag">课程介绍</div>
    <div class="sc-base sc-green">
        <div class="textBox">
            <div class="sc-text">
                <p class="small">上课方式</p>
                <p>网络直播课，电脑上与老师实时互动</p>
            </div>
            <div class="sc-text">
                <p class="small">授课老师</p>
                <p>资深少儿主持培训老师</p>
            </div>
            <div class="sc-text">
                <p class="small">全程服务</p>
                <p>1. 享受作业点评，节目排演专业指导</p>
                <p>2. 优秀学员还会收到成长记录视频哦</p>
            </div>
        </div>
    </div>
    <div class="sc-base sc-white">
        <div class="title sc-green">
            <span class="fl">7.17 上午 10:00-11:30</span>
            <span class="fr">镜头前的我</span>
        </div>
        <div class="textBox">
            <div class="sc-text">
                <p class="small">内容</p>
                <p>自我介绍、勇敢交友 / 台风台型</p>
            </div>
            <div class="sc-text">
                <p class="small">课程目的和效果</p>
                <p>勇敢张口说话 / 大方自信的站在镜头前</p>
            </div>
        </div>
    </div>
    <div class="sc-base sc-white">
        <div class="title sc-green">
            <span class="fl">7.24 上午 10:00-11:30</span>
            <span class="fr">主持范儿</span>
        </div>
        <div class="textBox">
            <div class="sc-text">
                <p class="small">内容</p>
                <p>副语言表达 / 奔跑吧美食开场词</p>
            </div>
            <div class="sc-text">
                <p class="small">课程目的和效果</p>
                <p>主持中3力练习：声音、动作、眼神</p>
            </div>
        </div>
    </div>
    <div class="sc-base sc-white">
        <div class="title sc-green">
            <span class="fl">7.31 上午 10:00-11:30</span>
            <span class="fr">郎朗贯口</span>
        </div>
        <div class="textBox">
            <div class="sc-text">
                <p class="small">内容</p>
                <p>关联词运用、大胆表达 / 报菜名</p>
            </div>
            <div class="sc-text">
                <p class="small">课程目的和效果</p>
                <p>口、手、脑三者协调统一性练习</p>
            </div>
        </div>
    </div>
    <div class="sc-base sc-white">
        <div class="title sc-green">
            <span class="fl">8.07 上午 10:00-11:30</span>
            <span class="fr">让表演最丰富</span>
        </div>
        <div class="textBox">
            <div class="sc-text">
                <p class="small">内容</p>
                <p>表情、动作、声音的一致性 / 超幸福鞋垫</p>
            </div>
            <div class="sc-text">
                <p class="small">课程目的和效果</p>
                <p>趣味性相声，语气、表情和沟通感练习习</p>
            </div>
        </div>
    </div>
    <div class="sc-base sc-white">
        <div class="title sc-green">
            <span class="fl">8.14 上午 10:00-11:30</span>
            <span class="fr">我是小演员</span>
        </div>
        <div class="textBox">
            <div class="sc-text">
                <p class="small">内容</p>
                <p>《奔跑吧，美食》节目排练</p>
            </div>
        </div>
    </div>
</div>
<div class="sopcast-main sopcast-main05">
    <div class="tag">报名方法</div>
    <div class="sc-base sc-white">
        <div class="textBox">
            <div class="sc-text">
                <p>正式课5节原价200元，活动期间优惠价<span class="text-red">150元</span></p>
            </div>
            <div class="sc-text">活动截止时间：2016年7月15日</div>
        </div>
    </div>
</div>
<#if !(hasPaid!false)>
<div class="sopcast-footer">
    <div class="empty"></div>
    <div class="sc-btns">
        <div class="btns"><a id="js_buy" class="js-submitBtn" data-type="${trusteeType!0}" data-sid="${studentId!0}" href="javascript:void(0)"><span>￥</span>150元，报名吧 </a></div>
    </div>
    </#if>
<script type="text/javascript">
    //获得地址栏参数
    function getQuery(item){
        var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
        return svalue ? decodeURIComponent(svalue[1]) : '';
    }
    //正式课活动页页面加载打点
    YQ.voxLogs({database : "parent", module : 'm_eIEsMv6Q', op : 'o_sgLCkzaq', s0 : getQuery("s0")});
    //正式课活动页付款按钮点击打点
    $("#js_buy").on("click",function(){
        YQ.voxLogs({database : "parent",module:"m_eIEsMv6Q",op:"o_PhkHxxER"});
    });
</script>
</@layout.page>