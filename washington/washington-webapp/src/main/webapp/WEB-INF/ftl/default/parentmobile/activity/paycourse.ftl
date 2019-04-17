<#import '../../parentmobile/layout.ftl' as layout>
<@layout.page className='paycourse' title="翻转课堂" pageJs="paycourseDesc" specialCss='paycourse'>
<@sugar.capsule js=['jquery','voxLogs']/>
<div class="fc-header">
    <div class="bg01"></div>
</div>
<div class="fc-main" id="fc-main01">
    <div class="title title01"></div>
    <div class="info01">
        <div class="section">
            <p class="name">1. 什么是翻转课堂？</p>
            <p class="text">“翻转课堂式教学模式”是指学生在家完成知识的学习，而课堂变成了老师学生之间和学生与学生之间互动的场所，包括答疑解惑、知识的运用等，从而达到更好的教育效果。</p>
        </div>
        <div class="section">
            <p class="name">2. 翻转课堂家长需要做什么？</p>
            <p class="text">翻转课堂让孩子每天的进步都能用电脑学习系统进行记录 、跟踪。老师24小时答疑解惑。不需要家长检查作业，督促孩子背课文。孩子在自学后，老师答疑解惑。家长能从老师的报告中准确发现孩子学会什么，没学会什么。真正解放家长的学习方法！</p>
        </div>
        <div class="section">
            <p class="name">3. 翻转课堂有效果吗？</p>
            <p class="text leftText">据美国某中学的调查，在使用翻转课堂后，新生英语考试的及格率从50%上升到81%；数学考试及格率从56%上升到87%，99%的学生和家长愿意推荐他们的朋友使用翻转课堂。</p>
            <p class="pic-3"></p>
        </div>
        <div class="section">
            <p class="name">4. Phonics （自然拼音法）是什么？</p>
            <p class="text">自然拼音法是目前国际上最推崇的英语教学方法。通过自然拼音的学习，可以让孩子建立起字母与发音之间的直觉音感，看到单词可以立即直觉反映出发音，同样，听到发音亦可直觉反映出单词拼写。</p>
        </div>
        <div class="section">
            <p class="name">5. 学习效果是什么？</p>
            <p class="text">学生掌握记忆单词技巧，激发学生自己学习的兴趣。</p>
        </div>
        <div class="section">
            <p class="name">6. 老师都是哪儿的？</p>
            <p class="text">在北京有多年Phonics教学经验老师，注重引导孩子兴趣，英语专业八级水平。</p>
        </div>
        <div class="section">
            <p class="name">7.筋斗云翻转课堂Phonics课程和别的课程有什么区别?</p>
            <p class="pic-7"></p>
        </div>
    </div>
</div>
<div class="fc-main" id="fc-main04">
    <div class="title title04"></div>
    <div class="info04">
        <p><span>课程价格：888元</span></p>
        <p><span>报名截止时间：8月31日晚20点整</span></p>
        <p><span>咨询电话：15313676397 袁老师</span></p>
        <p><span>报名流程：</span></p>
        <p class="padLeft-m"><span>点击报名按钮，支付成功后，将获得上课QQ群号码。请及时加入上课QQ群</span></p>
    </div>
</div>
<div class="fc-footer">
    <#if !(hasPaid!true)>
        <div class="empty"></div>
        <div class="footerFixed">
            <div class="left">
                <p>筋斗云翻转课堂-新概念课程</p>
                <p><span>原价：<del>1580</del>元</span><span>现价：888元</span></p>
            </div>
            <div class="right">
                <a href="javascript:void(0)" id="js_buy" class="js-submitBtn"  data-type="${trusteeType}" data-sid="${studentId!0}" style="cursor: pointer; display: inline-block;">立即报名</a>
            </div>
        </div>
    <#else>
        <div class="btnLeft" style="width: 100%;text-align: center;font-size: 1.5rem;line-height:3rem;padding-bottom: 1rem;">请加入上课QQ群：464800688</div>
    </#if>
</div>
<script type="text/javascript">
    //页面加载打点
    YQ.voxLogs({module : 'parent_fanzhuan', op : 'parent_lesson_show_fanzhuan'});

    //报名按钮点击打点
    $("#js_buy").click(function(){
        YQ.voxLogs({module : 'parent_fanzhuan', op : 'parent_payment_btn_click_fanzhuan'});
    });
</script>
</@layout.page>