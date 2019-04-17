<#import "../../layout/project.module.student.ftl" as temp />
<@temp.page title="优惠充值得学豆" header="hide">
    <@app.css href="public/skin/project/recharge/css/skin.css" />
    <div class="Preferential-recharge-main">
        <div class="head-box">
            <div class="inner">
                <#--<a class="logo" href="/"></a>-->
                <a class="back-index" href="/" style="color: #fff;">返回首页</a>
            </div>
        </div>
        <div class="content-box">
            <div class="inner">
                <span class="num">￥${recharge!"0"}</span>
                <a class="now-recharge-btn" href="/student/center/recharging.vpage?types=recharging-go&ref=fairyland">立即充值</a>
            </div>
        </div>
        <div class="colum-box">
            <div class="inner">
                <ul>
                    <li>活动规则</li>
                    <li>1．凡3月16日至4月15日期间充值用户，累计充值金额达到20元以上即可获得优惠和奖励。</li>
                    <li>2．奖品会在4月16日至4月20日陆续发放。</li>
                    <li>3．任意充值方式充值均可。 </li>
                    <li>4．活动期间，充值除可获得相应奖励作业币，学豆、体验产品、多出的作业币都是额外奖励。<br/>
                        如，100元充值，可获得100个作业币，额外获得750个学豆、3个作业币、10天沃克vip</li>
                </ul>
            </div>
        </div>
        <div class="foot-box">
            <div class="inner"></div>
        </div>
    </div>
</@temp.page>