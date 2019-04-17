<#import "../../layout/project.module.student.ftl" as temp />
<@temp.page title="12月感恩送豪礼" header="show">
<link rel="stylesheet" type="text/css" href="http://cdn.17zuoye.com/static/project/travelAmericaChristmas/skin1.0.1.css">
<div class="christmas-American-main">
    <div class="head">
        <div class="inner"></div>
    </div>
    <div class="white-con-main">
        <div class="white-con">
            <div class="inner">
                <div class="old-person"><span></span></div>
                <div class="font-item">
                    <div class="font-red">
                        想要在走遍美国中变身成为圣诞老人吗？机会来了!<br>
                        抢购时间：12月17日-1月8日
                    </div>
                    <ul class="font-list">
                        <li style="padding: 5px 0 10px 0;">活动规则</li>
                        <li>1.只能在活动期间内购买的到圣诞老人角色</li>
                        <li>2.请进入游戏内购买，可享受8折优惠</li>
                        <li>3.每个用户有机会通过“免费抽一次”获得圣诞老人角色，活</li>
                        <li>动期间只有一次免费机会</li>
                        <li style="color: #d13820;">角色优惠价格：960钻石</li>
                    </ul>
                </div>
                <div class="white-btn">
                    <span class="brown">圣诞老人</span>
                    <a class="start start-free" href="/student/apps/index.vpage?app_key=TravelAmerica" target="_blank">8折抢购</a>
            <#if lotteryTimes == 1>
                <span id="freeDrawOne"><a class="start-free" href="javascript:void (0);" >免费抽一次</a></span>
            <#else>
                <a class="start-free dis" href="/student/apps/index.vpage?app_key=TravelAmerica">您已抽过奖</a>
            </#if>
                </div>
            </div>
        </div>
    </div>
    <div class="red-con">
        <div class="inner"></div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        $("#freeDrawOne a").on("click", function(){
            var $this = $(this);

            if($this.hasClass("dis")){
                return false;
            }

            $this.addClass("dis");
            $.get("/campaign/15/lottery.vpage", {}, function (data) {
                if(data.success){
                    if(data.win){
                        $this.parent().html('<a class="start-free" href="/student/apps/index.vpage?app_key=TravelAmerica" >抽中奖,去领取</a>')
                    }else{
                        $this.text("未中奖");
                    }
                }else{
                    $17.alert(data.info, function () {
                        $this.removeClass("dis");
                    });
                }
            });
        });
    });
</script>
</@temp.page>