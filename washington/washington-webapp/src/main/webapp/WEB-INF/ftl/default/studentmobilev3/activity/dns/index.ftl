<#import "../../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='一起打年兽'
pageJs=["jquery", "voxLogs","weui"]
pageCssFile={"dns" : ["public/skin/mobile/student/app/activity/dns/css/skin"]}

>
<div class="beastHome-banner"><img src="<@app.link href="/public/skin/mobile/student/app/activity/dns/images/beast/banner-gif-v1.gif"/>" alt=""></div>
<div class="beastHome-box">
    <div class="beh-content">
        <div class="cTitle">活动时间</div>
        <p>1月16日-2月7日</p>
    </div>
    <div class="beh-content">
        <div class="cTitle">活动介绍</div>
        <p>
            每当春节将至，年兽都会袭击<span class=" fontYellow">【知识小镇】</span>，给小镇居民带来很多灾祸。
        </p>
        <p>
            今年春节，小镇居民决定用知识的力量武装自己，在每个班级组建<span class=" fontYellow">【打年兽小队】</span>。
        </p>
        <p>
            打年兽小队只能用在自学乐园里面收集的<span class=" fontYellow">【自学鞭炮】</span>赶走年兽。成功赶走年兽的小队会获得丰厚的奖励哦！
        </p>
    </div>
</div>
<div class="ptb-footer <#if !(applied!false)>differFooter</#if>">
    <#if applied!false>
        <a href="javascript:void(0);" class="green_btn gray_btn">距离年兽来袭还有${dayDiff!0}天${hourDiff!0}时${minuteDiff!0}分</a>
    <#else>
        <a id="applyBtn" href="javascript:void(0);" class="green_btn">加入打年兽小队，有礼包哦</a>
    </#if>
    <div class="btn-box">
        <a id="knowledgeBtn" href="javascript:void (0);" class="tip_btn">年兽小知识</a>
        <#if applied!false>
            <a href="/studentMobile/activity/dns/detail.vpage" class="tip_btn">活动大礼包</a>
        </#if>
    </div>
</div>

<script type="text/javascript">
    signRunScript = function ($) {
        var module = 'm_cKj5BuEp';

        //报名
        $("#applyBtn").on('click', function () {
            $.showLoading();
            $.post('/studentMobile/activity/warmnian.vpage', {}, function (data) {
                $.hideLoading();
                if (data.success) {
                    setTimeout(function () {
                        location.href = '/studentMobile/activity/dns/success.vpage';
                    },200);

                    YQ.voxLogs({
                        module: module,
                        op: "dns_apply_success"
                    });
                } else {
                    $.alert(data.info);
                }
            }).fail(function () {
                $.hideLoading();
            });

            YQ.voxLogs({
                module: module,
                op: "o_vUEXUoLF"
            });
        });

        //年兽小知识
        $("#knowledgeBtn").on('click', function () {
            var url = '/studentMobile/activity/dns/rule.vpage';
            if(window.external && ('pageQueueNew' in window.external)){
                window.external.pageQueueNew(JSON.stringify({
                    url: window.location.origin + url
                }))
            }else{
                location.href = url;
            }
        });

        YQ.voxLogs({
            module: module,
            op: "o_FQfzC1gT"
        });

    };
</script>

</@layout.page>