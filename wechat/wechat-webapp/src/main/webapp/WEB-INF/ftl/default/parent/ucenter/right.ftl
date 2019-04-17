<#import "../layout.ftl" as vipRight>
<@vipRight.page title='何为VIP会员'>
<@sugar.capsule css=['base'] />
<div class="main">
    <div class="vip_main_box">
        <h2 class="text_blue">•&nbsp;&nbsp;普通会员</h2>
        <ul>
            <li>孩子注册一起作业即可成为“普通会员”，享有以下权利：</li>
            <li>1、可免费在一起作业网做作业</li>
            <li>2、可免费使用自学产品</li>
            <li>3、可免费PK</li>
            <li>4、可免费在班级空间签到、跟同学互动、点“赞”等</li>
            <li>5、可免费参与不定期的活动</li>
            <li>6、可免费使用微信：一起作业家长通的信息和资源</li>
            <li>7、可免费参与特定体验活动等</li>
        </ul>
    </div>
    <div class="vip_main_box">
        <h2 class="text_blue">•&nbsp;&nbsp;VIP会员</h2>
        <ul>
            <li>通过“课外乐园”购买相关产品可升级为“VIP会员”，除普通会员权利外，还享有以下权利：</li>
            <li>1、享有个性化的作业</li>
            <li>2、可享有VIP尊贵标识</li>
            <li>3、获得更多学豆、PK值、专属PK时装、武装等奖励</li>
            <li>4、教学用品中心兑换奖品，可享受折扣等</li>
        </ul>
    </div>
</div>
<script type="text/javascript">
    function pageLog(){
        require(['logger'], function(logger) {
            logger.log({
                module: 'ucenter',
                op: 'ucenter_pv_vip_privilege'
            })
        })
    }
</script>
</@vipRight.page>