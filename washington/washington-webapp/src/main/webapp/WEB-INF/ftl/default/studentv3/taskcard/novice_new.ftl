<#assign studentName = currentUser.profile.realname!''>

<#if (data.showParentsNoticecard!false) && (data.taskMapper.nameSetted) && ((data.taskMapper.passwordModified)!false)>
<#assign JZT_CHANNEL_ID = "202009">
<#--家长通知-->
<li class="practice-block">
    <div class="practice-content">
        <h4>
            <span class="w-discipline-tag w-discipline-tag-9">老师通知</span>
        </h4>
        <div class="no-content" style="margin: 20px 20px; text-align: left;">
            <p class="n-3"><#--${data.xxtMessage!}-->
                收到<strong class="w-orange w-ft-large">${data.xxtMessageCount!0}</strong> 条老师新消息
                <#if (data.xxtIntegralReward)?? && data.xxtIntegralReward gt 0>奖励：<strong class="w-orange w-ft-large">${data.xxtIntegralReward!0}</strong> 学豆</#if>
            </p>
        </div>
        <div class="pc-btn">
            <a class="w-btn w-btn-green js-clickDownloadParent" href="javascript:void(0);" data-banding_type="xxt" data-campaign_id="9">去查收</a>
        </div>

        <script type="text/javascript">
            $(document).on("click", ".js-clickDownloadParent", function(){
                $17.get_jzt_qr(
                    "${JZT_CHANNEL_ID}",
                    function(JZT_QR_URL){
                        var popupContent =
                                '<div style="text-align: center;">' +
                                    '<p style="color:red; margin:20px 0;">请家长下载家长通，接收老师通知</p>' +
                                        '<img style="margin-bottom:10px;"  class="doGetJZTQR" height="144px" width="144px" src="'+ JZT_QR_URL +'"/>' +
                                    '<p>扫描二维码立即下载</p>' +
                                '</div>';

                        $.prompt(popupContent,{
                            title : "扫一扫下载家长通",
                            buttons : {}
                        });
                    }
                );
            });
        </script>
    </div>
</li>
<#else>
    <#assign JZT_CHANNEL_ID = "100110">
    <#--家长任务卡 data.displayNoviceCard 位于学习任务轮播模块中 -->
    <#if (!data.taskMapper.nameSetted || !data.taskMapper.mobileVerfied || !data.taskMapper.parentWechatBinded)!false>
    <li class="practice-block" id="stepNoviceOne">
        <div class="practice-content">
            <h4>
                <span class="w-discipline-tag w-discipline-tag-9">家长任务</span>
            </h4>
            <div class="pc-article">
                <#if !data.taskMapper.nameSetted>
                    <p class="noviceUnfinished" data-novice="name" data-index="1"  data-next_task_copy_write="${(!data.taskMapper.parentWechatBinded)?string("下一步，下载家长通","设置姓名")}" >
                        <strong class="w-fl-right w-orange ">
                            <span>未完成</span>
                        </strong>设置姓名
                    </p>
                </#if>
                <#if !data.taskMapper.parentWechatBinded>
                    <p class="noviceUnfinished" data-novice="weixin" data-index="2" data-next_task_copy_write="${(!data.taskMapper.mobileVerfied)?string("下一步，绑定手机","下载家长通")}">
                        <strong class="w-fl-right w-orange">
                            <span>未完成</span>
                        </strong>下载家长通
                    </p>
                </#if>
                <#if !data.taskMapper.mobileVerfied>
                    <p class="noviceUnfinished" data-novice="mobile" data-index="3" data-next_task_copy_write="确认绑定">
                        <strong class="w-fl-right w-orange">
                            <span>未完成</span>
                        </strong>绑定手机
                    </p>
                </#if>
            </div>
            <#if !data.taskMapper.nameSetted || !data.taskMapper.mobileVerfied || !data.taskMapper.parentWechatBinded>
                <div class="pc-btn">
                    <a href="javascript:void(0);" class="w-btn w-btn-green" id="startNewNovice">去完成</a>
                </div>
            </#if>
            <#if !data.taskMapper.passwordModified>
                <p class="noviceUnfinished" data-novice="password" data-index="4" style="display: none">
                    <strong class="w-fl-right <#if data.taskMapper.passwordModified>w-green<#else>w-orange</#if>">
                        <span><#if data.taskMapper.passwordModified>已完成<#else>未完成</#if></span>
                    </strong>设置登录密码
                </p>
            </#if>
        </div>
    </li>
    </#if>
    <script type="text/html" id="t:publicNovicePopup">
        <div class="t-set-password t-homework-task">
            <h1>家长任务</h1>
            <div id="novicePopupContent" class="homePromote-content">
                <div class="sp-step">
                    <%if(btnName === "name"){%>
                        <ul>
                            <li class="current <%if(state.name){%> active<%}%>">
                                <span class="sp-icon sp-icon-1"><span class="sp-icon sp-arrow"></span></span>
                                <p>设置姓名<span class="w-red w-ft-small">(必填)</span></p>
                            </li>
                        </ul>
                        <div class="w-clear"></div>
                    <%}else if(btnName === "weixin"){%>
                        <div class="content">
                            <div class="text">
                                <div class="red">请家长下载家长通，接收老师通知</div>
                                <div>老师会在家长通发布作业消息，校讯通和批改后的错误报告</div>
                            </div>
                            <div class="code">
                                <img src="<%= JZT_QR_URL %>"  alt="家长通二维码" class="doGetJZTQR"  height="144px" width="144px"/>
                                <p>扫描二维码立即下载</p>
                            </div>
                        </div>
                    <%}else if(btnName === "mobile"){%>
                        <div class="content">
                            <div class="text">
                                <div class="mobile red">绑定密保手机，防止学号或密码丢失无法找回！</div>
                            </div>
                        </div>
                    <%}%>
                </div>
                <div class="sp-form">
                <#--template content-->
                </div>
                <div class="btn-foot">
                    <%
                        var copyWrite = btnCopyWrite || null;

                        if(
                            (btnName === "mobile" && state.mobile)
                            ||
                            (btnName === "name" && state.name)
                            ||
                            (btnName === "weixin" && state.weixin)
                        ){
                            copyWrite = null;
                        }

                        if(copyWrite){
                    %>
                        <a class="w-btn-dic w-btn-green-well" href="javascript:;" id="n_edit_<%= btnName %>_but"><%= copyWrite %></a>
                    <% } %>
                </div>
            </div>
        </div>
    </script>

    <#--CRM注册学生需要强制绑定手机、修改密码-->
    <script type="text/html" id="t:publicNovicePopupForCrm">
        <div class="t-set-password t-homework-task">
            <h1 class="safety-warn">安全提醒</h1>
            <div id="novicePopupContent">
                <div class="sp-step">
                    <ul>
                        <li class="<%if(btnName == 'mobile'){%>current<%}%> <%if(state.mobile){%> active<%}%>">
                            <span class="sp-icon sp-icon-2"><span class="sp-icon sp-arrow"></span></span>
                            <p>绑定手机</p>
                        </li>
                        <%if(!state.password){%>
                            <li class="<%if(btnName == 'password'){%>current<%}%> <%if(state.password){%> active<%}%>">
                                <span class="sp-icon sp-icon-4"><span class="sp-icon sp-arrow"></span></span>
                                <p>修改初始密码</p>
                            </li>
                        <%}%>
                    </ul>
                    <div class="w-clear"></div>
                    <%if(!state.password && btnName == 'password'){%>
                        <p class="sub">目前密码不安全，请修改密码防止账号丢失哦。</p>
                    <%}else{%>
                        <p class="sub">你的账号存在风险，请绑定手机号提高安全性。</p>
                    <%}%>
                </div>
                <div class="sp-form">
                    <div class="w-form-table">
                           <dl>
                               <dt>初始密码：</dt>
                               <dd>
                                   <input id="n_current_pwd" type="password" value="" class="w-int">
                                    <span class="w-form-misInfo w-form-info-error qtip_n" style="display: none">
                                        <i class="w-spot w-icon-error"></i>
                                        <strong class="info">请填写密码</strong>
                                    </span>
                               </dd>
                               <dt>新密码：</dt>
                               <dd>
                                   <input id="n_new_pwd" type="password" value="" class="w-int">
                                    <span class="w-form-misInfo w-form-info-error qtip_n" style="display: none">
                                        <i class="w-spot w-icon-error"></i>
                                        <strong class="info">请填写确认密码</strong>
                                    </span>
                               </dd>
                               <dt>确认新密码：</dt>
                               <dd>
                                   <input id="n_config_pwd" type="password" value="" class="w-int">
                                    <span class="w-form-misInfo w-form-info-error qtip_n" style="display: none">
                                        <i class="w-spot w-icon-error"></i>
                                        <strong class="info">请再次填写确认密码</strong>
                                    </span>
                               </dd>
                           </dl>
                       </div>
                </div>
                <div class="sp-btn w-ag-center">
                    <a class="w-btn-dic w-btn-gray-well" id="logoutBtn" href="/ucenter/logout.vpage">退出登录</a>
                    <a class="w-btn-dic w-btn-green-well" href="javascript:void(0);" id="n_edit_<%=btnName%>_but">确定，下个任务</a>
                </div>
            </div>
        </div>
    </script>

    <#--模板专区-->
    <script type="text/html" id="t:name">
        <!-- step1 设置姓名 -->
        <%if(state.name){%>
            <div class="bindState"><span class="complete">你已设置姓名！</span></div>
        <%}else{%>
        <div class="w-form-table">
            <dl>
                <dt style="width: 180px"><span class="w-red">＊</span>学生真实姓名（必填）：</dt>
                <dd style="margin-left: 185px">
                    <input id="userName" type="text" value="" class="w-int" style="width: 160px" MAXLENGTH="6">
                    <span class="w-form-misInfo w-form-info-error qtip_n" style="display: none">
                        <i class="w-spot w-icon-error"></i>
                        <strong class="info">请输入您的真实姓名</strong>
                    </span>
                </dd>
            </dl>
        </div>
        <div class="sp-info" style="text-align: center;">
            真实姓名只能填写一次，不能修改哦！
        </div>
        <%}%>
    </script>

    <script type="text/html" id="t:mobile">
        <%if(state.mobile){%>
            <div class="bindState"><span class="complete">绑定成功！</span></div>
        <%}else{%>
            <div class="w-orange w-ag-center" style="padding-bottom: 10px;">绑定手机，否则学号或密码丢失无法找回！</div>
            <div class="w-form-table">
                <dl>
                    <dt>绑定家长手机：</dt>
                    <dd style="margin-bottom: 10px;">
                        <input id="mobile_box" type="text" maxlength="11" value="" class="w-int" placeholder="请输入家长手机号">
                                <span class="w-form-misInfo w-form-info-error qtip_n" style="display: none" data-title="请输入正确的手机号码">
                                    <i class="w-spot w-icon-error"></i>
                                    <strong class="info">请输入正确的手机号码</strong>
                                </span>
                        <#--<span class="w-orange" style="display: inline-block;">不要填写老师手机号</span>-->
                    </dd>
                    <dd style="margin-bottom: 10px;"><a id="get_captcha_but" href="javascript:void(0);" class="w-btn-dic w-btn-gray-normal"><span>免费获取短信验证码</span></a></dd>
                    <dt>短信验证码：</dt>
                    <dd style="margin-bottom: 10px;">
                        <input id="captcha_box" type="text" maxlength="4" class="w-int" style="width: 125px">
                        <span class="w-form-misInfo w-form-info-error qtip_n" style="display: none">
                            <i class="w-spot w-icon-error"></i>
                            <strong class="info">请输入正确的短信验证码</strong>
                        </span>
                    </dd>
                </dl>
            </div>
        <%}%>
    </script>

    <script type="text/html" id="t:weixin">
        <%if(state.weixin){%>
            <div class="bindState"><span class="complete">你已下载手机家长通！</span></div>
        <%}else{%>
            <div class="foot">
                <div class="text">
                    <div class="main">
                        <div class="pic"><img src="<@app.link href="public/skin/studentv3/images/publicbanner/JZT.png"/>"/></div>使用《家长通》还可以：<br>在完成作业后给老师送花<br>查看${(studentName == "")?string("孩子", studentName)}的学习报告，关注孩子成长
                    </div>
                </div>
            </div>
        <%}%>
    </script>

    <script type="text/html" id="t:password">
        <%if(state.password){%>
        <div class="over-text">你已设置自己的常用登录密码！</div>
        <%}else{%>
        <div class="w-form-table">
            <dl>
                <dt>现在的登录密码：</dt>
                <dd>
                    <input id="n_current_pwd" type="text" value="" class="w-int">
                    <span class="w-form-misInfo w-form-info-error qtip_n" style="display: none">
                        <i class="w-spot w-icon-error"></i>
                        <strong class="info">请填写密码</strong>
                    </span>
                </dd>
                <dt>新登录密码：</dt>
                <dd>
                    <input id="n_new_pwd" type="text" value="" class="w-int">
                    <span class="w-form-misInfo w-form-info-error qtip_n" style="display: none">
                        <i class="w-spot w-icon-error"></i>
                        <strong class="info">请填写确认密码</strong>
                    </span>
                </dd>
                <dt>确认新登录密码：</dt>
                <dd>
                    <input id="n_config_pwd" type="text" value="" class="w-int">
                    <span class="w-form-misInfo w-form-info-error qtip_n" style="display: none">
                        <i class="w-spot w-icon-error"></i>
                        <strong class="info">请再次填写确认密码</strong>
                    </span>
                </dd>
            </dl>
        </div>
        <%}%>
    </script>

    <script type="text/html" id="t:noviceComplete">
        <div class="sp-step sp-step-bg"></div>
        <div class="sp-form">
            <p class="w-ag-center" style="color: #95a7af;">点击下载你的账号到电脑上</p>
        </div>
        <div class="sp-btn w-ag-center">
            <a class="w-btn-dic w-btn-green-well" href="/clazz/fetchaccount.vpage" id="noviceDownLoadUserId" target="_blank">下载账号</a>
        </div>
    </script>

    <script type="text/javascript">
    (function($){
        var state = {
            name : ${((data.taskMapper.nameSetted)!false)?string},
            mobile : ${((data.taskMapper.mobileVerfied)!false)?string},
            weixin : ${((data.taskMapper.parentWechatBinded)!false)?string},
            password : ${((data.taskMapper.passwordModified)!false)?string}
        };

        //
        var popupTemplate = "t:publicNovicePopup";

        //是否完成所有任务
        function noviceFlagOver(number){
            if(state.mobile && state.weixin || !number){
                $("#novicePopupContent").html( template("t:noviceComplete", {}) );
                if(number){
                    $("#stepNoviceOne").remove();//li card
                }
                return false;
            }
            $.prompt.close();
            if(number){
                $(".noviceUnfinished[data-index='"+ number +"']").click();
            }
        }

        // Crm注册学生跳转
        function noviceFlagOverForCrm(number){
            if(state.password || !number) {
                $.prompt.close();
                $17.alert("设置成功，以后用手机号和新密码即可登录哦！");
                return false;
            }
            $.prompt.close();
            if(number){
                $(".noviceUnfinished[data-index='"+ number +"']").click();
            }
        }

        $(document).on("click", "#startNewNovice", function(){
            startNewNovice();
        });

        function startNewNovice(){
            if(!state.name){
                $(".noviceUnfinished[data-index='1']").click();
                return false;
            }

            if(!state.weixin){
                popupTemplate = "t:publicNovicePopup";
                $(".noviceUnfinished[data-index='2']").click();
                return false;
            }

            if(!state.mobile){
                popupTemplate = "t:publicNovicePopup";
                $(".noviceUnfinished[data-index='3']").click();
                return false;
            }

        }

        function startCrmTask() {
            if(!state.mobile){
                popupTemplate = "t:publicNovicePopupForCrm";
                $(".noviceUnfinished[data-index='3']").click();
                return false;
            }

            if(!state.password){
                popupTemplate = "t:publicNovicePopupForCrm";
                $(".noviceUnfinished[data-index='4']").click();
                return false;
            }

            $.cookie("crms", 1, { expires: 3 });
        }

        $.extend($, {
            startNewNovice : startNewNovice,
            startCrmTask : startCrmTask
        });

        //下次再说
        $(document).on("click", ".v-nextTimeNoviceBtn", function(){
            $.prompt.close();
        });

        //新手
        $(".noviceUnfinished").on('click', function(){
            var $this = $(this);
            var noviceName = $(this).data('novice');

            $17.get_jzt_qr(
                "${JZT_CHANNEL_ID}",
                function(JZT_QR_URL){
                    var nextTaskCopyWrite = $this.data("next_task_copy_write"),
                            btnCopyWrite = nextTaskCopyWrite && $.trim(nextTaskCopyWrite);

                    $.prompt(template(popupTemplate, {JZT_QR_URL:JZT_QR_URL, btnName : noviceName, state : state, btnCopyWrite : btnCopyWrite}),{
                        position    : { width: 620},
                        buttons     : {},
                        classes : {
                            //data.force == true : not close popup
                            close: popupTemplate == "t:publicNovicePopupForCrm" ? '${((data.force)!false)?string("w-hide", "")}' : ''
                        },
                        close : function(){
                            $.prompt.close();
                            /*关闭弹窗*/
                                $17.voxLog({
                                    module : "studentPopupForceBind",
                                    bind : "close",
                                    op : "<#if (data.force)!false>1<#else>2</#if>"
                                }, "student");
                            <#if (!data.force)!false>if(popupTemplate == "t:publicNovicePopupForCrm"){$17.setCookieOneDay("ForceBind", 1, 1);}</#if>
                        },
                        loaded : function(){
                            if(noviceName == 'weixin'){
                                var qrCodeUrl = "<@app.link href="public/skin/studentv3/images/2dbarcode.jpg"/>";
                                $.get("/student/qrcode.vpage", function(data){
                                    if(data.success){
                                        qrCodeUrl = data.qrcode_url;
                                    }
                                    $('#wenxinImg').html("<img src='"+ qrCodeUrl +"' width='130'/>");
                                });
                            }

                            var novicePopupContent = $("#novicePopupContent");

                            novicePopupContent.find(".sp-form").html( template("t:"+noviceName, {window:window, state : state}) );
                        }
                    });

                    $17.tongji('首页-学习任务卡片-家长任务-'+noviceName);
                }
            );
        });

        //下载账号密码
        $(document).on('click', '#noviceDownLoadUserId', function(){
            $.prompt.close();
        });

        //设置姓名
        $(document).on('click', '#n_edit_name_but', function(){
            if(state.name){
                $(".noviceUnfinished[data-index='2']").click();
                return false;
            }

            var nameBox = $("#userName");
//            var name = $17.trimString(nameBox.val());
            var name = nameBox.val();
            if($17.isBlank(name) || !$17.isValidCnName(name) || name.length > 10){
//            if($17.isBlank(name) || !$17.isCnString(name) || name.length > 10){
                nameBox.siblings("span").show();
                nameBox.focus();
                return false;
            }else{
                $.post('/ucenter/resetmyname.vpage',{name : name},function(data){
                    if(data.success){
                        $.prompt.close();
                        state.name = true;
                        $(".noviceUnfinished[data-index='1']").find("span").text("已完成").end().find("strong").addClass("w-green").removeClass("w-orange");
                        $(".noviceUnfinished[data-index='2']").click();
                    }else{
                        nameBox.siblings("span").children('strong').text(data.info);
                        nameBox.siblings("span").show();
                    }
                });
            }
        });

        //绑定手机
        $(document).on('click', '#n_edit_mobile_but', function(){
            if(state.mobile){
                $(".noviceUnfinished[data-index='4']").click();
                return false;
            }

            var mobileBox = $("#mobile_box");
            if(!$17.isMobile(mobileBox.val())){
                mobileBox.siblings("span").show().find(".info").html(mobileBox.siblings("span").data("title"))
                mobileBox.focus();
                return false;
            }

            var captchaBox = $("#captcha_box");
            if(!$17.isNumber(captchaBox.val())){
                captchaBox.siblings("span").show();
                captchaBox.focus();
                return false;
            }

            var callBack = function(data){
                if(data.success){
                    state.mobile = true;
                    $(".noviceUnfinished[data-index='3']").find("span").text("已完成").end().find("strong").addClass("w-green").removeClass("w-orange");

                    // 绑定成功
                    if (popupTemplate == "t:publicNovicePopup") {
                        noviceFlagOver(3);
                    } else {
                        noviceFlagOverForCrm(4);

                        $17.voxLog({
                            module : "studentPopupForceBind",
                            bind : "success",
                            op : "<#if (data.force)!false>1<#else>2</#if>"
                        }, "student");
                    }

                }else{
                    captchaBox.siblings("span").children('strong').text(data.info);
                    captchaBox.siblings("span").show();
                }
            };

            $.post('/student/nonameverifymobile.vpage', {code : captchaBox.val()}).done(callBack);
        });

        //绑定家长微信
        $(document).on('click', '#n_edit_weixin_but', function(){
            <#--
             TODO 是否需要ajax真正的获取是否真的绑定?
            state.weixin = true;
            $(".noviceUnfinished[data-index='2']").find("span").text("已完成").end().find("strong").addClass("w-green").removeClass("w-orange");
             -->

            noviceFlagOver(3);
        });

        //修改初始密码
        $(document).on('click', '#n_edit_password_but', function(){
            if(state.password){
                $("#novicePopupContent").html( template("t:noviceComplete", {}) );
                return false;
            }

            var currentPWD = $("#n_current_pwd");
            var newPWD = $("#n_new_pwd");
            var configPWD = $("#n_config_pwd");
            if($17.isBlank(currentPWD.val())){
                currentPWD.siblings("span").show();
                currentPWD.focus();
                return false;
            }

            if($17.isBlank(newPWD.val())){
                newPWD.siblings("span").show();
                newPWD.focus();
                return false;
            }

            if($17.isBlank(currentPWD.val())){
                newPWD.siblings("span").show();
                newPWD.focus();
                return false;
            }

            if(newPWD.val() != configPWD.val()){
                configPWD.siblings("span").children('strong').text('两次填写的密码不一致')
                configPWD.siblings("span").show();
                configPWD.focus();
                return false;
            }

            $.post('/ucenter/resetmypw.vpage', {current_password : currentPWD.val(), new_password : newPWD.val()}, function(data){
                if(data.success){
                    state.password = true;
                    $(".noviceUnfinished[data-index='4']").find("span").text("已完成").end().find("strong").addClass("w-green").removeClass("w-orange");
                    if (popupTemplate == "t:publicNovicePopup") {
                        $("#novicePopupContent").html(template("t:noviceComplete", {}));
                    } else {
                        $.prompt.close();
                        $17.alert("设置成功，以后用手机号和新密码即可登录哦！");
                    }
                }else{
                    configPWD.siblings("span").children('strong').text(data.info);
                    configPWD.siblings("span").show();
                }
            });

        });

        $(document).on('blur', 'dd input', function(){
            $(".qtip_n").hide();
        });

        /*获取验证码*/
        $(document).on('click','#get_captcha_but', function(){
            var $this = $(this);
            var mobileBox = $("#mobile_box");
            if(!$17.isMobile(mobileBox.val())){
                mobileBox.siblings("span").show().find(".info").html(mobileBox.siblings("span").data("title"));
                mobileBox.focus();
                return false;
            }
            if($this.hasClass("btn_disable")){return false;}

            $this.addClass("btn_disable");
            if($17.isMobile(mobileBox.val())){
                $.post("/student/sendmobilecode.vpage", {mobile : mobileBox.val()}, function(data){
                    if(!data.success){
                        mobileBox.siblings("span").children('strong').text(data.info);
                        mobileBox.siblings("span").show();
                    }
                    $17.getSMSVerifyCode($this, data);

                    $17.voxLog({
                        module : "studentPopupForceBind",
                        bind : "getCode",
                        op : "<#if (data.force)!false>1<#else>2</#if>"
                    }, "student");
                });
            }
        });

        $(document).on("click", "#logoutBtn", function(){
            $17.voxLog({
                module : "studentPopupForceBind",
                bind : "logout",
                op : "<#if (data.force)!false>1<#else>2</#if>"
            }, "student");
        });
    }($));
    </script>
</#if>
