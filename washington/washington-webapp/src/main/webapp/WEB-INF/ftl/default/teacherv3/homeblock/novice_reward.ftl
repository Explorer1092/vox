<#--新手任务奖品中心-->
<#if (!currentTeacherWebGrayFunction.isAvailable("Reward", "Close"))!false>
    <#if taskInfo??>
    <div class="t-rewardRef-Box ">
        <div class="v-noviceAwardBtn" style="position: absolute; right: 0; top: 0; height: 38px; width: 130px;">
            <a href="javascript:void(0);" class="ref" title="新手奖励"></a>
            <ul style="display: none;">
                <li class="arrow"></li>
                <li class="<#if (taskInfo.addressFlag)!false>active</#if>"><a href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myprofile.vpage">填写资料</a><i class="ic"></i></li>
                <li class="<#if (taskInfo.wechatFlag)!false>active</#if>"><a href="javascript:void(0);" class="click-binding-weixin">绑定微信</a><i class="ic"></i></li>
                <li class="<#if (taskInfo.bbsFlag)!false>active</#if>"><a href="http://www.17huayuan.com/forum.php?mod=viewthread&tid=29879" target="_blank" style="border: none;">论坛报到</a><i class="ic"></i></li>
                <li class="last"><a href="/reward/index.vpage" target="_blank">新手任务</a></li>
            </ul>
        </div>
    </div>
    <script type="text/javascript">
        $(function(){
            $(document).on("mouseenter", ".v-noviceAwardBtn", function(){
                var $this = $(this);

                $this.find("ul").show();
            }).on("mouseleave", ".v-noviceAwardBtn", function(){
                var $this = $(this);

                $this.find("ul").hide();
            });
        });
    </script>
    </#if>
</#if>