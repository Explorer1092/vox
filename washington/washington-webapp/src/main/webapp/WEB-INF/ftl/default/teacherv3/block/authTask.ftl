<#if (data.showAuth)!false>
<div class="w-base">
    <div class="w-base-title">
        <h3>新手认证，更多特权</h3>
        <div class="w-base-ext">
            <span class="w-bast-ctn">轻松教，让学生爱上作业！</span>
        </div>
        <div class="w-base-right w-base-more">
            <#if (data.authConditions.nameSetted && data.authConditions.mobileAuthenticated && data.authConditions.enoughStudentsFinishedHomework && data.authConditions.enoughStudentsBindParentMobile)>
                <a href="javascript:void(0);" title="等待认证中" style="cursor: default;"><span class="w-icon-md">等待认证中</span></a>
            <#else>
                <a href="http://help.17zuoye.com/?p=1" target="_blank"><span class="w-icon-public w-icon-faq"></span><span class="w-icon-md"> 快速认证攻略</span></a>
            </#if>
        </div>
    </div>
    <div class="w-base-container">
        <!--//start-->
        <div class="t-journal-list t-auth-task">
            <ul>
                <#--未完成-->
                <#if !data.authConditions.nameSetted>
                    <li>
                        <span class="count"></span><div class="content">设置自己的真实姓名</div>
                        <div class="task-btn"><a class="w-btn w-btn-mini" href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myprofile.vpage">设置姓名</a></div>
                    </li>
                </#if>
                <#if !data.authConditions.mobileAuthenticated>
                    <li>
                        <span class="count"></span><div class="content">绑定手机，使用手机号登录，保障账号安全</div>
                        <div class="task-btn"><a class="w-btn w-btn-mini" href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/securitycenter.vpage?type=mobileLi">开始</a></div>
                    </li>
                </#if>
                <#if !data.authConditions.enoughStudentsBindParentMobile>
                    <li>
                        <span class="count"></span><div class="content">验证学生，3名学生登录并绑定手机</div>
                        <div class="task-btn"><a class="w-btn w-btn-mini data-certificationAcceleration" href="javascript:void(0);">认证加速</a></div>
                    </li>
                </#if>
                <#if !data.authConditions.enoughStudentsFinishedHomework>
                    <li>
                        <span class="count"></span><div class="content">布置作业，8名同学完成了3次作业或测验</div>
                        <div class="task-btn"><a class="w-btn w-btn-mini" href="/teacher/homework/batchassignhomework.vpage">布置作业</a></div>
                    </li>
                </#if>
                <#--以下是已经完成-->
                <#if data.authConditions.nameSetted>
                <li class="task-gray">
                    <span class="count"></span><div class="content">设置自己的真实姓名</div>
                    <div class="task-btn"><a class="w-btn w-btn-mini w-btn-disabled" href="javascript:void(0);">已完成</a></div>
                </li>
                </#if>
                <#if data.authConditions.mobileAuthenticated>
                <li class="task-gray">
                    <span class="count"></span><div class="content">绑定手机，使用手机号登录，保障账号安全</div>
                    <div class="task-btn"><a class="w-btn w-btn-mini w-btn-disabled" href="javascript:void(0);">已完成</a></div>
                </li>
                </#if>
                <#if data.authConditions.enoughStudentsBindParentMobile>
                <li class="task-gray">
                    <span class="count"></span><div class="content">验证学生，3名学生登录并绑定手机</div>
                    <div class="task-btn"><a class="w-btn w-btn-mini w-btn-disabled" href="javascript:void(0);">已完成</a></div>
                </li>
                </#if>
                <#if data.authConditions.enoughStudentsFinishedHomework>
                <li class="task-gray">
                    <span class="count"></span><div class="content">布置作业，8名同学完成了3次作业或测验</div>
                    <div class="task-btn"><a class="w-btn w-btn-mini w-btn-disabled" href="javascript:void(0);">已完成</a></div>
                </li>
                </#if>
            </ul>
            <div class="entrance">
                <a href="/reward/index.vpage" target="_blank" data-title="领100园丁豆，兑换奖品" class="s-2"></a>
                <a href="/campaign/teacherlottery.vpage" target="_blank" data-title="布置作业抽大奖" class="s-3"></a>
                <a href="/teacher/newfeatures/index.vpage" target="_blank" data-title="更多功能特权" class="s-4"></a>
            </div>
            <div class="w-clear"></div>
        </div>
    <#--end//-->
    </div>
</div>
<script type="text/html" id="T:certificationAcceleration">
    <div class='t-homework-step' id="certificationAcceleration">
        <p class='add-content'>
            帮助学生登录，完成新手绑定手机任务
        </p>
        <p class='add-success add-success-auth'></p>
        <p style='height:90px; margin: 10px 70px;'>
            <a href='/teacher/clazz/clazzlist.vpage' target="_blank" class='w-btn w-fl-left'>下载学生名单</a>
            <a href='http://help.17zuoye.com/?p=446' target="_blank" class='w-btn w-fl-right'>了解新手任务</a>
        </p>
    </div>
</script>
<script type="text/javascript">
    $(function(){
        $(".data-certificationAcceleration").on("click", function(){
            $.prompt(template("T:certificationAcceleration", {}), {
                title: "认证加速",
                buttons: {},
                position:{width : 680},
                loaded : function(){
                    $("#certificationAcceleration .w-btn").on("click", function(){
                        $.prompt.close();
                    });
                }
            });
        });
    });
</script>
</#if>