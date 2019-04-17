<style>
    .addClassTip-alert{ font-size: 14px; color: #333;}
    .addClassTip-alert p.title{ padding-bottom: 5px; font-size: 16px; text-align: center; }
    .addClassTip-alert h5{ font-size: 14px; padding: 8px 0; font-weight: normal;  clear: both;}
    .addClassTip-alert h5 a{ color: #333; width: 66px; height: 20px; display: inline-block; line-height: 20px; text-decoration: none; text-align: center; background-color: #e9f9ff; border: 1px solid #afe2f6; border-radius: 4px;}
    .addClassTip-alert h5 a:hover{ border-color: #afe2f6; background-color: #fff;}
    .addClassTip-alert .ate{ margin: 0 auto 5px; width: 400px; position: relative; clear: both;}
    .addClassTip-alert .ate span.sen{ position: absolute; left: -50px; top: 15px;}
    .addClassTip-alert .ate ul{ margin: 5px 0 5px -15px;}
    .addClassTip-alert .ate li.ls{ background-color: #cce5ef; _display: inline; box-shadow: 0 1px 2px 1px #b6d4e0 inset; margin: 8px 12px; border-radius: 4px; cursor: pointer; float: left; padding: 7px 0; text-align: center;  width: 108px; text-overflow: ellipsis;white-space: nowrap; overflow: hidden;  }
    .addClassTip-alert .ate li.active{ background-color: #1faacf; z-index: 2; color: #fff; box-shadow: none;}
    .addClassTip-alert .game-ic{ text-align: center; padding: 10px 0 0;}
    .addClassTip-alert .game-ic a { display: inline-block; margin: 0 20px;}
    .addClassTip-alert .game-ic a strong{ display: block;}
    .addClassTip-alert .game-ic a .ic{ display: inline-block; background: url(<@app.link href="public/skin/studentv3/images/app_box/game-ic.png"/>) no-repeat 0 0; width: 82px; height: 82px; }
    .addClassTip-alert .game-ic .babel .ic { background-position: 0 0;}
    .addClassTip-alert .game-ic .travel .ic { background-position: -126px 0;}
    .addClassTip-alert .game-ic .pk .ic { background-position: -252px 0;}

    .addClassTip-alert-step1 .ate h5{ font-size: 14px; padding: 0 0 10px; font-weight: normal;  clear: both; line-height: 160%;}
    .addClassTip-alert-step1 .ate{ margin: 0 auto 5px; width: 400px; position: relative; clear: both;}
    .addClassTip-alert-step1 .ate h5 span{ font-size:18px;font-weight: 600;}
    .addClassTip-alert-step1 .ate span.sen{ position: absolute; left: -50px; top: 15px;}
    .addClassTip-alert-step1 .ate ul{ margin: 5px 0 5px -15px;}
    .addClassTip-alert-step1 .ate li.ls{ background-color: #cce5ef; _display: inline; box-shadow: 0 1px 2px 1px #b6d4e0 inset; margin: 8px 12px; border-radius: 4px; cursor: pointer; float: left; padding: 7px 0; text-align: center;  width: 402px; white-space: nowrap;overflow: hidden;}
    .addClassTip-alert-step1 .ate li:hover{background-color: #a5d0e1;}
    .addClassTip-alert-step1 .jqiform .jqimessage{margin:30px 60px 50px;}

    .addClassTip-alert-step2 .w-form-table{padding:8px 0 0 0;}
    .addClassTip-alert-step2 .w-form-table dt{width: 108px; line-height: 32px;}
    .addClassTip-alert-step2 .w-form-table .input-ver-code .w-int{width:110px;}
    .addClassTip-alert-step2 .w-form-table dd{ display: inline-block;*display:inline;*float:left;*margin:-50px 0 0 280px; margin: 0;}
    .addClassTip-alert-step2 .con{padding: 10px 0;font-size: 14px;}
    .addClassTip-alert-step2 .jqidefaultbutton{margin-left:30px;}

    .addClassTip-alert-step3 .title,.addClassTip-alert-step2 .title{padding: 10px;font-size: 14px;text-align: center;}
    .addClassTip-alert-step3 .jqibuttons button{margin:0 0 0 20px;}
</style>
<#-- 学生加入班级 -->
<script id="t:加入班级" type="text/html">
    <div style="padding: 12px; color: #ff0000; text-align: center;">
        <#if !((currentStudentDetail.clazz.isTerminalClazz())!false)>
            必须填写真实老师，填写错误将不能换老师，不能做作业！
        <#else>
            请填写初中老师号码，加入中学班级<br>
            （购买的课外乐园产品，升入初中将无法继续使用哦）
        </#if>
    </div>
    <div class="w-form-table" style="padding: 0;">
        <dl style="padding: 0;">
            <dt style="width: 170px; line-height: 32px;">老师告诉我的号码：</dt>
            <dd style="margin: 0 0 15px 170px;">
                <input id="student_clazz_id_box" name="student_clazz_id" placeholder="老师手机号或学号" type="text" style="width:236px;" class="w-int"/>
                <span class="init w-red"></span>
            </dd>
            <dt style="width: 170px; line-height: 32px;">验证码：</dt>
            <dd style="margin: 0 0 15px 170px;">
                <input id="captchaCode" name="code" type="text" style="" class="w-int"/>
            </dd>
            <dd style="margin: 0 0 15px 170px;">
                <img id='captchaImage' />&nbsp;
                看不清？<a href="javascript:void(0);" id="captchaImageRef" class="w-blue">换一个</a>
            </dd>
        </dl>
    </div>
</script>

<script id="t:加入班级失败" type="text/html">
    <div class="spacing_vox text_center">
        <%=title%>
    </div>
    <div class="spacing_vox">
        <div><%==tip%></div>
    </div>
</script>
<script type="text/html" id="t:查询加入老师班级">
    <div class="addClassTip-alert" id="allSearchClazzItem">
        <div class="act">
            <div class="ate">
                <h5>学校：<%=clazzList[0].schoolName%></h5>
                <h5 id="v-cl-teacher">老师：<%=clazzList[0].teachers%></h5>
                <ul style="overflow: hidden; *zoom: 1; *height: 100%;">
                    <%for(var i = 0; i < clazzList.length; i++){%>
                    <li class="ls" data-teachers="<%=clazzList[i].teachers%>" data-clazzid="<%=clazzList[i].clazzId%>" title="<%=clazzList[i].clazzName%>"><%=clazzList[i].clazzName%></li>
                    <%}%>
                </ul>
                <h5 class="init" style="display:none; color:#f00;">请选择就读班级</h5>
                <div style="color: #ff0000; clear: both;">必须填写真实老师，填写错误将不能换老师，不能做作业！</div>
            </div>
        </div>
    </div>
</script>
<script type="text/html" id="t:换班提示">
    <div class="addClassTip-alert-step1">
        <div class="act">
            <div class="ate">
            <#assign subjectFlag = 0  subjectText = "---" />
            <#if (data.linkedTeachers)??>
                <h5>你在<span>${(currentStudentDetail.clazzLevel.description)!}${(currentStudentDetail.clazz.className)!}</span>，你的
                    <#list data.linkedTeachers as teacher >
                        <#if (teacher.subject == "ENGLISH")!false><#assign subjectFlag = (subjectFlag + 1) subjectText = "英语" /></#if>
                        <#if (teacher.subject == "MATH")!false><#assign subjectFlag = (subjectFlag + 1) subjectText = "数学" /></#if>
                        <#if (teacher.subject == "CHINESE")!false><#assign subjectText = "语文" /></#if>
                            ${subjectText!} 老师是：<span>${(teacher.fetchRealname())!'---'}</span><br/>
                    </#list>
                </h5>
            </#if>
                <h5>你需要：</h5>
                <ul style="overflow: hidden; *zoom: 1; *height: 100%;">
                    <#if subjectFlag lt 2>
                        <li class="ls vjoinTip">新增一个老师</li>
                    </#if>
                    <li class="ls vjoinTip">班级换了老师，我要修改老师</li>
                    <li class="ls vjoinTip">我换了班级，我要去另一个班</li>
                </ul>
            </div>
        </div>
    </div>
</script>
<script type="text/html" id="T:换班提示绑定手机">
    <div class="addClassTip-alert-step2">
        <%if(mobile){%>
        <div class="con">家长手机号： <%=mobile%> <a href="${(ProductConfig.getUcenterUrl())!''}/student/center/account.vpage" style="color:#00aced;">更换</a></div>
        <div class="w-form-table">
            <dl>
                <dt>请输入验证码：</dt>
                <dd class="input-ver-code">
                    <input type="text" class="w-int" id="joinClazzMobileCode" maxlength="11" placeholder="请输入验证码">
                </dd>
                <dd class="ver-code">
                    <a class="w-btn-dic w-btn-gray-normal joinClazz_GetCaptchaBut" href="javascript:void(0);"><span>免费获取短信验证码</span></a>
                </dd>
            </dl>
        </div>
        <div id="joinClazzCodeInfo" style="color: #f00;padding: 10px 0 0; margin-left: 108px;"></div>
        <%}else{%>
        <div class="con">家长手机号：你尚未绑定家长手机，请先 <a href="${(ProductConfig.getUcenterUrl())!''}/student/center/account.vpage" style="color:#00aced;"> 绑定家长手机</a></div>
        <%}%>
    </div>
</script>

<script type="text/javascript">
    $(function(){
        var captchaToken;
        function refreshCaptcha() {
            $.get("/student/clickjoinclazz.vpage", {} ,function(data){
                if(data.success){
                    $('#captchaImage').attr('src', "/captcha?" + $.param({
                                'module': 'joinClazz',
                                'token': data.captchaToken,
                                't': new Date().getTime()
                            }));

                    captchaToken = data.captchaToken;
                }
            });
        }

        $(document).on("click", "#allSearchClazzItem li", function(){
            $(this).addClass("active").siblings().removeClass("active");
            $(this).parent("ul").siblings("#v-cl-teacher").text("老师："+$(this).attr("data-teachers"));

            $("#allSearchClazzItem .init").hide();
        });

        //加入新的班级
        var joinClazzBackMobile = "";
        $(".v-joinClazzBtn-popup").on('click', function(event, data){
            var addClazz = {
                stateBefore : {
                    title   : "加入班级",
                    html    : template("t:换班提示", {}),
                    buttons     : {},
                    position    : { width: 500, height: 720 }
                },
                state : {
                    title       : "加入班级",
                    html        : template("t:加入班级", {}),
                    position    : { width: 500, height: 720 },
                    focus       : 1,
                    buttons     : {"取消" : false, "确定" : true},
                    submit : function(e,v){
                        e.preventDefault();
                        if(v){
                            var clazzId = $("#student_clazz_id_box").getClassId();
                            var captchaCode = $("#captchaCode").val();
                            var teacherId = null;

                            if($17.isBlank(clazzId) || clazzId.length > 11 || !$17.isNumber(clazzId)){
                                $.prompt.goToState('stateError');
                                return false;
                            }

                            if($17.isBlank(captchaCode) || captchaCode.length != 4){
                                $.prompt.goToState('stateErrorCode');
                                return false;
                            }

                            $.post("/student/checkclazzinfo.vpage", {
                                id :  clazzId,
                                captchaToken : captchaToken,
                                captchaCode : captchaCode
                            }, function(data){
                                if(data.success){
                                    if(data.clazzList.length > 0){
                                        if (data.clazzList[0].creatorType == "SYSTEM") {
                                            var teacherId = clazzId;
                                        }
                                        $.prompt(template("t:查询加入老师班级", {clazzList : data.clazzList}), {
                                            title   : "你要加入吗？",
                                            focus       : 1,
                                            buttons : {"不加入":false, "确定加入" : true},
                                            submit  : function(e, v){
                                                if(v){
                                                    var allSearchClazzItem = $("#allSearchClazzItem");

                                                    if(!allSearchClazzItem.find("li").hasClass("active")){
                                                        allSearchClazzItem.find(".init").show();
                                                        return false;
                                                    }

                                                    clazzId = allSearchClazzItem.find("li.active").attr("data-clazzid");
                                                    var clazzName = allSearchClazzItem.find("li.active").attr("title");

                                                    var url = "/student/systemclazz/joinclazz.vpage?clazzId=" + clazzId + "&teacherId=" + teacherId;

                                                    $.getJSON(url, function(data){
                                                        $.prompt.close();
                                                        if(data.success){
                                                            var message = "加入班级成功！";
                                                            if (data.jumpMSPage) {
                                                                message = "加入班级成功！即将进入中学页面";
                                                            }
                                                            $.prompt(message, {
                                                                title   : "加入班级",
                                                                buttons : {"确定":true},
                                                                submit  : function(){
                                                                    setTimeout(function(){ location.href = "/student/index.vpage"; }, 200);
                                                                }
                                                            });
                                                        }else{
                                                            var msgTitle = null;
                                                            var msgTip   = null;

                                                            switch(data.type){
                                                                case "NO_SUCH_CLASS":
                                                                    msgTitle = "加入失败：班级不存在";
                                                                    msgTip = "请输入正确的老师手机号";
                                                                    break;
                                                                case "ABOVE_QUOTA":
                                                                    msgTitle = "加入失败：班级人数已满";
                                                                    msgTip = "";
                                                                    break;
                                                                case "CLASS_FREE_JOIN_CLOSED":
                                                                    msgTitle = "加入失败：老师不允许加入";
                                                                    msgTip = "老师设置不允许新学生加入，请提醒老师修改";
                                                                    break;
                                                                case "ALREADY_IN_CLASS":
                                                                    msgTitle = "加入失败：你已在此班级";
                                                                    msgTip = "请与你的老师核实编号，如果编号记忆不便可以把你的学号发给老师，让老师把你加入班级";
                                                                    break;
                                                                case "DIFFERENT_CLAZZ"://处理
                                                                    joinClazzBackMobile = data.mobile;
                                                                    msgTitle = "加入失败：你现在不能更换班级。如需更换请验证家长手机：";
                                                                    msgTip = template("T:换班提示绑定手机", {mobile : joinClazzBackMobile});
                                                                    verifySCTCodeInfo({msgTitle : msgTitle, msgTip: msgTip, clazzId : clazzId, teacherId : teacherId, clazzName : clazzName});
                                                                    //msgTip = "如有问题，请联系客服<@ftlmacro.hotline/>";
                                                                    return false;
                                                                    break;
                                                                case "MULTI_TEACHER_ONE_SUBJECT"://处理
                                                                    joinClazzBackMobile = data.mobile;
                                                                    msgTitle = "加入失败：你已经有一个同科目老师。如需更换请验证家长手机：";
                                                                    msgTip = template("T:换班提示绑定手机", {mobile : joinClazzBackMobile});
                                                                    verifySCTCodeInfo({msgTitle : msgTitle, msgTip: msgTip, clazzId : clazzId, teacherId : teacherId, clazzName : clazzName});
                                                                    //msgTip = "您已有同科目老师，不能随意更换！有问题，请联系客服！";
                                                                    return false;
                                                                case "CHEATING_TEACHER":
                                                                    msgTitle = "加入失败";
                                                                    msgTip = "此老师使用异常，你不能添加Ta为老师！";
                                                                    break;
                                                            }

                                                            $.prompt(template("t:加入班级失败", {
                                                                title : msgTitle,
                                                                tip   : msgTip
                                                            }), {
                                                                title: "系统提示",
                                                                buttons: { "知道了" : true }
                                                            });
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }else{
                                        $17.alert("没有找到班级");
                                    }
                                }else{
                                    if (data.info == "不能换到初中"){
                                        $17.alert("你填写的是中学老师，请直接下载app使用吧！", function() {
                                            window.location = "http://www.17zyw.cn/AviMVb";
                                        });
                                    } else {
                                        $17.alert(data.info);
                                    }
                                }
                                refreshCaptcha();
                            });
                        }else{
                            $.prompt.goToState('stateBefore');
                        }
                    }

                },
                stateError: {
                    title   : "加入班级",
                    html    : '<h4>请输入老师告诉你的手机号.</h4>',
                    buttons : { "知道了": true },
                    submit  : function(e, v){
                        e.preventDefault();
                        $.prompt.goToState('state');
                        refreshCaptcha();
                    }
                },
                stateErrorCode: {
                    title   : "加入班级",
                    html    : '<h4>请输入正确的验证码.</h4>',
                    buttons : { "知道了": true },
                    submit  : function(e, v){
                        e.preventDefault();
                        $.prompt.goToState('state');
                        refreshCaptcha();
                    }
                }
            };

            $.prompt(addClazz,{
                loaded : function(){
                    refreshCaptcha();
                    $("#student_clazz_id_box").focus();
                    $17.tongji('首页-学习任务卡片-加入班级');

                    if (data) {
                        if (data.state) {// 传递了state参数，字段跳转到指定state
                            $.prompt.goToState(data.state);
                        }
                    }

                    $(document).on("click", "#captchaImageRef", function(){
                        refreshCaptcha();
                    });

                    $(document).on("click", ".vjoinTip", function(){
                        $.prompt.goToState('state');
                    });
                }
            });
        });

        /*获取验证码*/
        $(document).on('click', '.joinClazz_GetCaptchaBut', function(){
            var $this = $(this);
            var mobileBox = joinClazzBackMobile;

            if(!$17.isMobile(mobileBox)){
                joinClazzCodeInfo("手机号有误");
                return false;
            }

            if($this.hasClass("btn_disable")){return false;}

            $.post("/student/systemclazz/sendSCTCode.vpage", {mobile : mobileBox}, function(data){
                if(!data.success){ joinClazzCodeInfo(data.info) }
                $17.getSMSVerifyCode($this, data);
            });
        });

        function joinClazzCodeInfo(content){
            $("#joinClazzCodeInfo").html(content).slideDown();
            setTimeout(function(){
                $("#joinClazzCodeInfo").slideUp();
            }, 3000);
        }

        function verifySCTCodeInfo(dataVal){
            //验证手机
            $.prompt(template("t:加入班级失败", {
                title : dataVal.msgTitle,
                tip   : dataVal.msgTip
            }), {
                focus : 1,
                title: "系统提示",
                buttons     : {"取消" : false, "确定" : true},
                loaded : function(){},
                submit : function(e, v){
                    if(v){
                        var verifySCTCodeVal = $("#joinClazzMobileCode").val();
                        if( $17.isBlank(verifySCTCodeVal) ){
                            joinClazzCodeInfo("验证码不能为空");
                             return false;
                        }

                        $.post("/student/systemclazz/verifySCTCode.vpage", {code : verifySCTCodeVal}, function(data){
                            if(data.success){
                                var url = "/student/systemclazz/joinclazz.vpage?clazzId=" + dataVal.clazzId + "&teacherId=" + dataVal.teacherId + "&clazzName=" + dataVal.clazzName + "&forceLink=true";
                                $.getJSON(url, function(data){
                                    if(data.success){
                                        var message = "加入班级成功！";
                                        if (data.jumpMSPage) {
                                            message = "加入班级成功！即将进入中学页面";
                                        }
                                        $.prompt(message, {
                                            title   : "加入班级",
                                            buttons : {"确定":true},
                                            submit  : function(){
                                                setTimeout(function(){ location.href = "/student/index.vpage"; }, 200);
                                            }
                                        });
                                    }else{
                                        $17.alert("加入失败，请重新选择班级加入。");
                                    }
                                });
                            }else{
                                joinClazzCodeInfo(data.info)
                            }
                        });
                        return false;
                    }
                }
            });
        }
    });
</script>