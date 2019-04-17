<#import "module.ftl" as temp />
<@temp.page title='weekreward'>
<script type="text/javascript">
    location.href = '/reward/product/exclusive/index.vpage';
</script>
<div class="my_order_box">
    <div class="my_order_inner_box">
        <div class="no_order_box">
            <div class="t-getReward-box">
                <h2 class="title">领奖区 奖品</h2>
                <p class="my_order_title" style="padding: 30px 0 0 0; text-align: center;">规则：一周任意3天（及以上）布置作业，即可在领奖区选择奖品。</p>
                <ul style="text-align: center;">
                    <li class="gw-list" style="padding-left: 0;float: none;display: inline-block;">
                        <span class="gold v-receiveRewardBtn" data-name="GOLD">
                            <div class="img"><a href="javascript:void(0);"><img src="<@app.link href="public/skin/reward/imagesV1/get-100glod.jpg"/>"/></a></div>
                            <#--<span class="info">10园丁豆</span>-->
                        </span>
                        <a class="w-but v-receiveRewardBtn ${(canReward!false)?string("", "w-but-disabled")}" data-name="GOLD" href="javascript:void (0);">领取<@ftlmacro.garyBeansText/></a>
                    </li>
                    <#--<li class="gw-list">-->
                        <#--<span class="gold v-receiveRewardBtn" data-name="LOTTERY">-->
                            <#--<div class="img"><a href="javascript:void(0);"><img src="<@app.link href="public/skin/reward/imagesV1/get-reward-v1.png"/>"/></a></div>-->
                        <#--</span>-->
                        <#--<a class="w-but" style="width: 45%; float: left; font-size: 20px; background-color: #189cfb;" href="/help/downloadApp.vpage?refrerer=pc&count=0" target="_blank">下载老师APP</a>-->
                        <#--<a style="width: 50%; float: right; font-size: 20px;" class="w-but v-receiveRewardBtn ${(canReward!false)?string("", "w-but-disabled")}" data-name="LOTTERY" href="javascript:void (0);">领取抽奖机会</a>-->
                    <#--</li>-->
                    <li class="w-clear"></li>
                </ul>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        $(document).on("click", ".v-receiveRewardBtn", function(){
            var $this = $(this);
            var $thisName = $this.attr("data-name");

            if( $this.hasClass("w-but-disabled") ||  $17.isBlank($thisName) ){
                return false;
            }

            $this.addClass("w-but-disabled");
            $.post("/reward/getreward.vpage", {
                taskType : "WEEK_ASSIGN_TASK",
                rewardName : $thisName
            }, function(data){
                if(data.success){
                    if($thisName  == "LOTTERY"){
                        $17.alert("领取成功，抽奖机会仅当天有效哦！");
                    }else{
                        $17.alert("领取成功");
                    }
                    $(".v-receiveRewardBtn").addClass("w-but-disabled");
                }else{
                    $17.alert(data.info);
                }
                $this.removeClass("w-but-disabled");
            });
        });
    });
</script>
</@temp.page>