<!--w-base我的认证 -->
<div class="w-base">
    <div class="w-base-title">
        <h3>我的认证</h3>
    </div>
    <div class="w-base-container">
        <div class="t-myTeacher-auth-box">
            <dl>
                <dt>
                    <p>认证状态：
                    <#if (currentUser.fetchCertificationState() == "SUCCESS")!false>
                        <span class="w-green"><i class="w-icon-public w-icon-success"></i>已认证</span>
                    <#else>
                        <span class="w-red"><i class="w-icon-public w-icon-error"></i>未认证</span>
                    </#if>
                    </p>
                    <#if (currentUser.lastAuthDate)??><p class="time">认证时间：${(currentUser.lastAuthDate)!'----/--/-- --:--:--'?datetime("yyyy/MM/dd HH:mm:ss")}</p></#if>
                </dt>
                <dd>
                    <p>若您已满足认证条件，可以选择如下方式认证</p>
                    <div class="tm-btn" style="text-align: center;">
                        <#--<a style="margin-right: 16px;" class="w-btn w-btn-small w-circular-5 w-border-blue" href="javascript:void (0);">在线认证</a>-->
                        <#--<#switch state>
                            <#case "SUCCESS">
                                &lt;#&ndash;<a class="w-btn w-btn-small w-btn-gray w-btn-disabled" href="javascript:void(0);">已认证</a>&ndash;&gt;
                                <#break/>
                            <#case "WAITING">
                                <a class="w-btn w-btn-small w-btn-orange" href="javascript:void(0);" style="cursor: default;">审核中</a>
                                <#break/>
                            <#case "FAILURE">
                                <a class="w-btn w-btn-small w-btn-orange" href="javascript:void(0);" style="cursor: default;">认证失败</a>
                                <#break/>
                        </#switch>-->
                        <span class="w-icon w-icon-phone"></span><span style="font-size: 18px; display: inline-block; vertical-align: middle; color: #00aced;"><@ftlmacro.hotline phoneType="teacher"/></span>
                    </div>
                </dd>
            </dl>
        </div>
    </div>
</div>

<!--w-base我的认证 -->
<#if (currentTeacherDetail.isPrimarySchool())!false>
<div class="w-base">
    <div class="w-base-title">
        <h3>认证身份，专享特权 <span class="w-ft-small w-gray">认证真实老师身份后，可享受更丰富的产品功能</span></h3>
    </div>
    <div class="w-base-container">
        <div class="t-mySpecial-auth-box">
            <div class="tp-foot">
                <div class="con">
                    <a class="w-blue" href="${(ProductConfig.getMainSiteBaseUrl())!''}/teacher/homework/batchassignhomework.vpage?ref=centerauth"><span class="tp-icon tp-icon-1"></span></a>
                    <p class="th-btn"><a class="w-blue" href="${(ProductConfig.getMainSiteBaseUrl())!''}/teacher/homework/batchassignhomework.vpage?ref=centerauth">布置作业</a></p>
                    <div class="font">
                        全部上百种作业应用</br>
                        全部近千本专业绘本</br>
                        全部数十万道同步习题
                    </div>
                </div>
                <div class="con">
                    <a class="w-blue" href="${(ProductConfig.getMainSiteBaseUrl())!''}/teacher/smartclazz/list.vpage?ref=centerauth"><span class="tp-icon tp-icon-2"></span></a>
                    <p class="th-btn"><a class="w-blue" href="${(ProductConfig.getMainSiteBaseUrl())!''}/teacher/smartclazz/list.vpage?ref=centerauth">智慧课堂</a></p>
                    <div class="font">
                        海量课堂资源</br>
                        随机提问，即时奖励</br>
                        计时工具，学生卡片
                    </div>
                </div>
                <#if (!currentTeacherWebGrayFunction.isAvailable("Reward", "Close"))!false>
                <div class="con">
                    <a class="w-blue" target="_blank" href="${(ProductConfig.getMainSiteBaseUrl())!''}/reward/product/exclusive/index.vpage?ref=centerauth"><span class="tp-icon tp-icon-4"></span></a>
                    <p class="th-btn"><a class="w-blue" target="_blank" href="${(ProductConfig.getMainSiteBaseUrl())!''}/reward/product/exclusive/index.vpage?ref=centerauth">礼品兑换</a></p>
                    <div class="font">
                        教学用品中心免费兑好礼</br>
                        更有作业抽奖特权</br>
                        平板，手机，现金红包
                    </div>
                </div>
                </#if>
            </div>
            <div class="w-clear"></div>
        </div>
    </div>
</div>
</#if>

<div class="w-base">
    <div class="w-base-title">
        <h3>认证条件</h3>
    </div>
    <div class="t-security">
        <ul>
            <#--姓名认证 / 手机认证-->
            <li>
                <div class="ts-icon "><span class="ts-ic ts-ic-4"></span></div>
                <div class="ts-note">
                    <h3>设置姓名并绑定手机</h3>
                    <p style="padding: 0;">绑定手机，使用手机号登录更安全</p>
                </div>
            </li>

            <#--作业认证-->
            <li>
                <div class="ts-icon "><span class="ts-ic ts-ic-5"></span></div>
                <div class="ts-note">
                    <h3>8名学生完成3次作业</h3>
                    <p style="padding: 0;">布置作业，至少8名学生，每人完成过3次作业</p>
                </div>
            </li>
            <#--3名同学绑定手机-->
            <li>
                <div class="ts-icon "><span class="ts-ic ts-ic-6"></span></div>
                <div class="ts-note">
                    <h3>3名学生绑定手机 </h3>
                    <p style="padding: 0;">验证学生，至少3名学生，每人绑定了家长手机或自己手机</p>
                </div>
            </li>
        </ul>
        <div class="w-clear"></div>
    </div>
</div>

<script id="t:申请提交成功" type="text/html">
    <div class="w-base">
        <div class="t-security">
            <ul>
                <li>
                    <div class="ts-note" style="width: 95%; height: 88px;">
                        <h3><i class="w-icon-public w-icon-success"></i>您的认证申请已成功提交！</h3>
                        <p style="border-top:solid 1px #ccc;font:14px/24px arial; color:#39f;">
                            请拨打客服电话<b> <@ftlmacro.hotline phoneType="teacher"/></b> <br>
                            一起教育科技的工作人员会跟您核实信息处理您的申请并反馈结果
                        </p>
                    </div>

                </li>
            </ul>
            <div class="w-clear"></div>
        </div>
    </div>
</script>
<script type="text/javascript">
    if(location.pathname == "/teacher/center/myauthenticate.vpage"){
        location.href = "/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage";
    }

    $(function(){
        LeftMenu.changeMenu();
        LeftMenu.focus("authentication");

        //申请认证
        $("#applyAuth").on("click",function(){
            if($(this).isFreezing()){
                return false;
            }
            $(this).freezing();
            $.get('/teacher/center/authenticatechip.vpage',function(data){
                $(this).thaw();
                $.prompt(data, {
                    title: "确认信息",
                    position: { width: 700 },
                    buttons: {"修改信息" : false,"确定" : true},
                    submit : function(e,v,m,f){
                        e.preventDefault();
                        if(v){
                            $.get("/teacher/center/authenticatesubmit.vpage", function(data){
                                if(!data.success){
                                    alert(data.info);
                                    return false;
                                }else{
                                    $.prompt(template("t:申请提交成功", {}), {
                                        title: "系统提示",
                                        buttons: { "知道了": true },
                                        position:{width : 500},
                                        submit : function(e,v,m,f){
                                            setTimeout(function(){
                                                location.reload();
                                            },200);
                                        }
                                    });
                                    return false;
                                }
                            });
                        }else{
                            location.href = "#/teacher/center/myprofile.vpage";
                        }
                    }
                });
            });

        });

    });
</script>