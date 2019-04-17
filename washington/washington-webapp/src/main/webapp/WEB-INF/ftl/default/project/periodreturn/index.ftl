<#import "../../layout/project.module.ftl" as temp />
<@temp.page title="期末感恩回馈" header="hide">
<@app.css href="public/skin/project/periodreturn/skin.css" />
<div class="main">
    <div class="plan-head-banner">
        <div class="plan-inner">
            <a class="back-index" href="/teacher/index.vpage"></a>
        </div>
    </div>
    <!--活动一-->
    <div class="plan-active01-content plan-public-content">
        <div class="plan-inner">
            <dl>
                <dt></dt>
                <dd>
                    <p class="title">
                        每新增30学生，立得5元话费，15元封顶
                    </p>
                    <p>1.时间：即日起至2015年6月15日 23:59:59</p>
                    <p>2.奖励：每邀请30个新学生完成3次作业，可得到5元话费或微信红包</p>
                    <p>3.发放：话费或微信红包奖励将于2015年6月16日至2015年6月21日发放完毕</p>
                </dd>
                <dd class="pl">
                    <div class="plan-progress"></div>
                </dd>
            </dl>
        </div>
    </div>
    <!--活动二-->
    <div class="plan-active02-content plan-public-content">
        <div class="plan-inner">
            <dl>
                <dt></dt>
                <dd>
                    <p class="title">
                        园丁豆大放送：学生总数×5个园丁豆，500园丁豆封顶
                    </p>
                    <p>1.截止2015/6/15 23:59:59，计算每个老师下完成3次作业的学生总数</p>
                    <p>2.可于2015/6/16 00:00:00至2015/6/21 23:59:59，免费领取园丁豆</p>
                    <p>3.可领园丁豆数量=学生总数×5（绑定微信的老师再×2）</p>
                    <p>4.每个老师最多可领取500园丁豆</p>
                    <p class="getReward">
                        <a class="get-btn <#if false>click-getReward<#else>get-btn-disabled</#if>" href="javascript:void (0);">领取话费</a>
                    </p>
                    <p class="swap">
                        <span id="weixinCode"><img src=""></span>
                        <span>扫码绑微信，得双倍园丁豆奖励</span>
                    </p>
                </dd>
            </dl>
        </div>
    </div>
    <!--示例-->
    <div class="plan-information-content plan-public-content">
        <div class="plan-inner">
            <dl>
                <dt></dt>
                <dd>
                    <p>
                        李老师，目前有1个班级，班上完成3次以上作业的学生有15人：
                    </p>
                    <p>1.6月1日创建1个新班并邀请30个新学生，且在6月15日之前完成3次作业；</p>
                    <p>2.李老师已完成“一起作业”微信绑定</p>
                    <p>2015年6月16日可获得奖励：</p>
                    <p>5元话费：带来的30个新学生得5元奖励</p>
                    <p>450园丁豆：奖励园丁豆数=(15+30)×5×2=450园丁豆</p>
                </dd>
            </dl>
        </div>
    </div>
    <!--帮助任务-->
    <div class="plan-help-content plan-public-content">
        <div class="plan-inner">
            <dl>
                <dt></dt>
                <dd>
                    <p>1.本次活动参与有奖互助奖励计划</p>
                    <p>2.邀请长期未使用老师回来并帮助他们，当TA达成任务，更可额外获得园丁豆</p>
                    <p>3.快去有奖互助<a class="more" href="/teacher/invite/activateteacher.vpage">查看详情</a></p>
                </dd>
            </dl>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        $17.getQRCodeImgUrl({
            role : "teacher"
        }, function (url) {
            $("#weixinCode").html('<img src='+url+' alt="二维码"/>');
        });

        /*$(".click-getReward").on("click", function(){
            var $this = $(this);

            if( $this.hasClass("get-btn-disabled") ){
                return false;
            }

            $this.addClass("get-btn-disabled");
            $.post("/teacher/receiveintegral.vpage", {}, function(data){
                if(data.success){
                    $this.addClass("get-btn-disabled");
                    $17.alert("领取成功！");
                }else{
                    $17.alert(data.info);
                    $this.removeClass("get-btn-disabled");
                }
            });
        });*/
    });
</script>
</@temp.page>