<#import "resetpassword/module.ftl" as com>
<@com.page>
<@sugar.capsule js=["template"] />
<div id="searchId">
    <div class="main">
            <h4>
                忘记学号
            </h4>
            <div class="chooseDentity-con">
                <div class="register_box resetNav">
                    <div class="reg_type">
                        <ul class="mainNav">
                            <li class="student active js-clickSideTypeMenu" data-type="3"><div class="triangle"></div><a href="javascript:void(0);" class="mypt v-forgetStaticLog" data-op="click-forget-student" title="学生"><strong>我是<span>学生</span></strong>请选择这里</a></li>
                            <li class="teacher js-clickSideTypeMenu" data-type="1" style=""><div class="triangle"></div><a href="javascript:void(0);" class="mytc v-forgetStaticLog" data-op="click-forget-teacher" title="老师"><strong>我是<span>老师</span></strong>请选择这里</a></li>
                            <#--<li class="parents js-clickSideTypeMenu" data-type="2"><i class="triangle"></i><a href="/ucenter/forgotPassword.vpage?userType=2" class="myst v-forgetStaticLog" data-op="click-forget-parents" data-title="忘记密码-家长" title="家长"><strong>我是<span>家长</span></strong>请选择这里</a></li>-->
                        </ul>
                    </div>
                </div>
            </div>
            <div class="right-show">
                <ul class="stepInfoBox" style=" width: 400px;">
                    <li class="sel"><i>1</i><b>输入验证问题</b></li>
                    <li><s></s><i>2</i><b> 查询结果</b></li>
                </ul>
                <div style="width: 355px;margin: 0 0 30px 95px;line-height:35px;clear:both;">
                    <strong>已绑定手机号，可用手机号登录哦，</strong><a href="/login.vpage" class="clrblue">去登录</a>
                    <img src="<@app.link href="public/skin/default/images/password/loginTips.png"/>" alt=""/>
                </div>
                <ul class="formList">
                    <li class="inp dis">
                        <strong class="prompt">没有绑定手机？找回账号：</strong>
                    </li>
                    <li class="inp dis revise">
                        <b class="tit">我的姓名：</b>
                        <input name="userName" type="text" value="">
                        <span class="hint">姓名不能包含字母数字或下划线</span>
                    </li>
                    <li class="inp dis stages" id="level">
                        <b class="tit">我的学段：</b>
                        <label class="js-level-click w-radio-current" data-value="PRIMARY_SCHOOL"><span class="w-radio"></span>小学</label>
                        <label class="js-level-click" data-value="JUNIOR_SCHOOL"><span class="w-radio"></span>中学</label>
                    </li>
                    <li class="inp dis stages">
                        <b class="tit">我的年级：</b>
                        <select class="ty_1 js-grade"></select>
                    </li>
                    <li class="inp dis">
                        <b class="tit" style="vertical-align: top; line-height: 40px; *line-height: 28px;">我的学校：</b>
                    <span style=" display: inline-block;width:400px;" id="areaBox">
                        <select class="ty_0" style="width: 85px; margin-right: 5px;"><option value="所在省">所在省</option><option value="110000">北京</option><option value="120000">天津</option><option data-title="closed" value="130000">河北</option><option data-title="closed" value="140000">山西</option><option data-title="closed" value="150000">内蒙古</option><option data-title="closed" value="210000">辽宁</option><option data-title="closed" value="220000">吉林</option><option data-title="closed" value="230000">黑龙江</option><option data-title="closed" value="310000">上海</option><option data-title="closed" value="320000">江苏</option><option data-title="closed" value="330000">浙江</option><option data-title="closed" value="340000">安徽</option><option data-title="closed" value="350000">福建</option><option data-title="closed" value="360000">江西</option><option data-title="closed" value="370000">山东</option><option data-title="closed" value="410000">河南</option><option data-title="closed" value="420000">湖北</option><option data-title="closed" value="430000">湖南</option><option data-title="closed" value="440000">广东</option><option data-title="closed" value="450000">广西</option><option data-title="closed" value="460000">海南</option><option data-title="closed" value="500000">重庆</option><option data-title="closed" value="510000">四川</option><option data-title="closed" value="520000">贵州</option><option data-title="closed" value="530000">云南</option><option data-title="closed" value="540000">西藏</option><option data-title="closed" value="610000">陕西</option><option data-title="closed" value="620000">甘肃</option><option data-title="closed" value="630000">青海</option><option data-title="closed" value="640000">宁夏</option><option data-title="closed" value="650000">新疆</option><option data-title="closed" value="710000">台湾</option><option data-title="closed" value="810000">香港</option><option data-title="closed" value="820000">澳门</option></select>
                        <select class="ty_1" style="width: auto;  margin-right: 5px;">
                            <option value="市">市</option>
                        </select>
                        <select class="ty_2" style="width: auto;  margin-right: 5px;">
                            <option value="区">区</option>
                        </select>
                        <div style="position:relative;width: 100%;margin-top:10px;">
                            <ul id="select-school" class="ty_3" style="box-sizing:border-box;max-height:540px;overflow:auto;width: 100%;border:1px solid #189cfb;display: none;background:#fff;position:absolute;bottom:28px;left:0;">
                                <#--<li value="学校" class="unclickable">学校</li>-->
                                <li class="unclickable empty-result" style='margin-bottom:60px;display:none;' value="empty">无搜索结果</li>
                            </ul>
                            <input id="js_input" type="text" data-sid="学校" style="display:block;box-sizing:border-box;height:29px;padding:3px 6px;width:100%;line-height:21px;padding-right:16px;" placeholder="学校" autocomplete="off" name="schoolId" />
                        </div>
                        <span class="hint" style="width: auto;">请选择学校</span>
                    </span>
                    </li>
                    <li class="inp dis">
                        <b class="tit">验证码：</b><input id="captchaCode" type="text" name="code" value="" style="width:80px">
                        &nbsp;
                        <img id='captchaImage'>&nbsp;
                        看不清？<a href="javascript:refreshCaptcha();" class="clrblue">换一个</a>
                        <span class="hint" style=" width: auto;">请输入验证码</span>
                    </li>
                    <li class="btn" style="padding-top:10px;">
                        <a href="/ucenter/resetnavigation.vpage" class="w-btn w-btn-small w-btn-green">返回</a>
                        <a href="javascript:void(0);" class="w-btn w-btn-small js-submitButton" data-name="search">查询</a>
                    </li>
                </ul>
            </div>
    </div>
</div>

<div id="searchResult" style="display: none;">
    <h4>
        忘记学号
    </h4>
    <ul class="stepInfoBox" style=" width: 400px;">
        <li class="sel"><i>1</i><b>输入验证问题</b></li>
        <li class="sel"><s style="margin:0 10px;background:#383a4c"></s><i>2</i><b>查询结果</b></li>
    </ul>
    <ul class="formList" style="margin:0;">
        <li class="radioListBox"></li>
    </ul>
</div>
<script id="t:查询结果" type="text/html">
    <div class="spacing_vox text_gray_6 text_well">
        <p style="cursor:default;font-weight:bold;text-indent:120px;">你所在学校有<%= dataPut.length%>个查询结果</p>
    </div>
    <%if(dataPut.length > 0){%>
        <%for(var i = 0; i < dataPut.length; i++){%>
        <div class="result">
            <p style=" cursor: default;">
                <span style="display:inline-block; width:420px; color:#666; height:32px; vertical-align: middle; line-height:32px;">
                    <%=dataPut[i].userName%>
                    <span class="userId"><%=dataPut[i].userId%></span>
                    <%=dataPut[i].clazzName%>
                    <%if(dataPut[i].subject){%>
                        <%=dataPut[i].subject%>老师
                    <%}%>

                    <%if(dataPut[i].obscuredMobile || dataPut[i].obscuredEmail){%>
                        <%if(dataPut[i].obscuredMobile){%>
                            手机：<%=dataPut[i].obscuredMobile%>
                        <%}else{%>
                            邮箱：<%=dataPut[i].obscuredEmail%>
                        <%}%>
                    <%}%>
                </span>
                <span class="gotoBtn" style="color:#666;">
                   这是我的学号
                    <a href="javascript:void(0);" class="notPasswordBtn w-blue" account="<%=dataPut[i].userId%>">忘记密码？</a>
                    <%if(!(userType == "student" && ktwelve == "JUNIOR_SCHOOL")){%>
                    <a href="/?userId=<%=dataPut[i].userId%>" class="w-btn w-btn-light w-btn-mini nowGotoLogin" >立即去登录</a>
                    <%}%>
                </span>
            </p>
        </div>
        <%}%>
    <%}else{%>
    <div class="spacing_vox"><p style="cursor: default;font-weight:bold;text-indent:120px;">没有符合你所查询条件的账号，请仔细核对刚刚所输入查询条件，注意是否输入了错别字</p></div>
    <%}%>
    <div class="clear"></div>
    <div style="text-align: center; clear: both; padding: 30px 0;">
        <a href="javascript:void(0);" class="w-btn w-btn-small w-btn-green js-backSearch" data-name="back">返回</a>
    </div>
    <div class="serviceBox" style="padding: 30px 0;border-top: 1px dashed #dae6ee;text-align: center;">
        <a href="javascript:void(0);" onclick="window.open('${(ProductConfig.getMainSiteBaseUrl())!''}/redirector/onlinecs_new.vpage?type=<%=userType%>&question_type=question_account_ps&serverInfo=<%=serverInfo%>&origin=PC-找回账号查询结果', '','width=856,height=519');" style="width:auto;padding-left: 28px;background:url(/public/skin/default/images/indexv4/pc_icon.png) 0 0 no-repeat;line-height:18px;text-align: left;  display: inline-block; position: relative; top: 10px;color:#189cfb;">联系在线客服&nbsp;在线时间8:00-21:00</a>
    </div>
</script>
<script type="text/javascript">
    $(function(){
        var schoolInput = $("#js_input"), isEmpty = true;

        schoolInput.focus(function(){
            searchSchoolName($(this), $("#select-school"));
        });

        schoolInput.on('input propertychange keyup',function(){
            searchSchoolName($(this), $("#select-school"));
        });

        function searchSchoolName(id, target){
            var $content = id.val();
            var $regex = new RegExp( ".*" + $content.split('').join(".*") );
            var $li = target.find("li");

            target.show();

            if( $content == "" ){
                $li.show();
                target.children(".empty-result").hide();
            }else{
                for(var i = 1; i < $li.length; i++){
                    if( $regex.test($li.eq(i).text()) ){
                        $li.eq(i).show();
                        isEmpty = false;
                    }else{
                        $li.eq(i).hide();
                    }
                }

                if(isEmpty){
                    target.find("li[value='empty']").show();
                }else{
                    target.find("li[value='empty']").hide();
                }
            }

            isEmpty = true;
        }

        $(document).on("click",function(e){
            if(e.target.id != "js_input"){
                $("#select-school").hide();
            }
        });

        $("#select-school").on("click","li",function(){
            if(!$(this).hasClass("unclickable")){
                schoolInput.attr("data-sid",$(this).attr("value")).val($(this).html());
            }
        });

        var forgotAccount = {
            id : {
                searchId    : $("#searchId"),
                areaBox     : $("#areaBox"),
                searchResult: $("#searchResult")
            },
            areaTo : function(link, target, val){
                if(val == "学校"){
                    target.html('<!--<li value="学校" class="unclickable">学校</li>--><li class="unclickable empty-result" style="margin-bottom:60px;display:none;" value="empty">无搜索结果</li>');
                }
                else{
                    target.html("<option value='"+ val +"'>"+ val +"</option>");
                }
                $.getJSON(link, function(data){
                    if(val == "学校"){
                        $.each(data.rows, function(){
                            target.append("<li data-title='" + this.state + "' value='" + this.id + "'>" + this.cname + "</li>");
                        });
                    }else{
                        $.each(data, function(){
                            target.append("<option data-title='" + this.state + "' value='" + this.id + "'>" + this.text + "</option>");
                        });
                    }
                });
            },
            init : function(){
                var $this = this;
                var $id = $this.id;
                var $areaBox = $id.areaBox;
                var userType = 3;
                var ktwelve = "PRIMARY_SCHOOL";

                $(document).on("click", ".js-clickSideTypeMenu", function(){
                    var $this = $(this);
                    var $type = $this.data("type");

                    if( $17.isBlank($type) ){
                        return false;
                    }else{
                        userType = $type;

                        $this.siblings().removeClass("active");
                        $this.addClass("active");
                    }
                });

                $this.areaTo("/map/nodes.vpage?id=0", $areaBox.find(".ty_0"), "所在省");

                //所在省
                $areaBox.find("select").on("change", function(){
                    var $thatVal = $(this).val();

                    switch ($(this).attr("class")){
                        case "ty_0" :
                            $areaBox.find(".ty_1").html("<option value='市'>市</option>");
                            $areaBox.find(".ty_2").html("<option value='区'>区</option>");
                            $areaBox.find(".ty_3").html('<!--<li value="学校" class="unclickable">学校</li>--><li class="unclickable empty-result" style="margin-bottom:60px;display:none;" value="empty">无搜索结果</li>');
                            if($thatVal != "所在省"){
                                if($thatVal=="110000" || $thatVal == "120000" || $thatVal == "310000" || $thatVal=="500000"){
                                    $areaBox.find(".ty_1").hide();
                                    $this.areaTo("/map/nodes.vpage?id=" + $thatVal, $areaBox.find(".ty_2"), "区");
                                }else{
                                    $areaBox.find(".ty_1").show();
                                    $this.areaTo("/map/nodes.vpage?id=" + $thatVal, $areaBox.find(".ty_1"), "市");
                                }
                            }
                            break;
                        case "ty_1" :
                            $areaBox.find(".ty_2").html("<option value='区'>区</option>");
                            $areaBox.find(".ty_3").html('<!--<li value="学校" class="unclickable">学校</li>--><li class="unclickable empty-result" style="margin-bottom:60px;display:none;" value="empty">无搜索结果</li>');
                            if($thatVal != "市"){
                                $this.areaTo("/map/nodes.vpage?id=" + $thatVal, $areaBox.find(".ty_2"), "区");
                            }
                            break;
                        case "ty_2" :
                            $areaBox.find(".ty_3").html('<!--<li value="学校" class="unclickable">学校</li>--><li class="unclickable empty-result" style="margin-bottom:60px;display:none;" value="empty">无搜索结果</li>');
                            if($thatVal != "区"){
                                $this.areaTo("/school/areaschool.vpage?area=" + $thatVal + "&level=" + ktwelve, $areaBox.find(".ty_3"), "学校");
                            }
                            break;
                        case "ty_3" :
                            $areaBox.parent().removeClass("err");
                            break;
                    }
                });

                var grade = ["1年级","2年级","3年级","4年级","5年级","6年级"],
                        gradeJunior = ["6年级","7年级","8年级","9年级"],
                        gradeResult,gradeHtml = "";

                for (var i=0;i<grade.length;i++){
                    gradeHtml +=("<option>"+ grade[i] +"</option>");
                }
                $(".js-grade").html(gradeHtml);

                //学段
                $(document).on("click", ".js-level-click", function(){
                    var $this = $(this);
                    $("#js_input").attr("placeholder","学校").val("");
                    gradeHtml = "";
                    if($this.hasClass("w-radio-current")){
                        return false;
                    }

                    ktwelve = $this.attr("data-value");
                    $this.addClass("w-radio-current").siblings(".js-level-click").removeClass("w-radio-current");
                    $areaBox.find("select").change();

                    gradeHtml = "";
                    $(".js-grade").html("");
                    if (ktwelve == "PRIMARY_SCHOOL"){
                        gradeResult = grade;
                    }else{
                        gradeResult = gradeJunior;
                    }
                    for (var i=0;i<gradeResult.length;i++){
                        gradeHtml +=("<option>"+ gradeResult[i] +"</option>");
                    }
                    $(".js-grade").html(gradeHtml);

                });



                $id.searchId.find("input[name='userName'], input[name='code']").keyup(function(){
                    $(this).parent().removeClass("err");
                });

                //查询
                $id.searchId.find(".js-submitButton[data-name=search]").on("click", function(){
                    if(!$id.searchId.find("input[name='userName']").val()){
                        $id.searchId.find("input[name='userName']").parent().addClass("err");
                        return false;
                    }

                    if($areaBox.find("input[name='schoolId']").val() == "学校"){
                        $areaBox.parent().addClass("err");
                        return false;
                    }

                    if(!$id.searchId.find("input[name='code']").val()){
                        $id.searchId.find("input[name='code']").parent().addClass("err");
                        return false;
                    }
                    var clazzLevelValue = parseInt($('.js-grade option:selected').text().substr(0, 1));
                    var dataJson = {
                        userName : $id.searchId.find("input[name='userName']").val(),
                        schoolId : $id.searchId.find("input[name='schoolId']").attr("data-sid"),
                        captchaToken : "${captchaToken!''}",
                        captchaCode : $id.searchId.find("input[name='code']").val(),
                        userType : userType,
                        clazzLevel : clazzLevelValue
                    };
                    App.postJSON('/ucenter/possibleaccount.vpage', dataJson, function(data){
                        if(data.success){
                            $id.searchId.hide();
                            $id.searchResult.show();

                            var $serverInfo = "姓名："+ dataJson.userName +"|学段：" +  ktwelve + "|学校："+ dataJson.schoolId, userT;
                            if (userType == 3){
                                userT = "student";
                            }else{
                                userT = "teacher";
                            }
                            $id.searchResult.find(".radioListBox").html(template("t:查询结果", {
                                userName : dataJson.userName,
                                schoolId : dataJson.schoolId,
                                userType : userT,
                                ktwelve : ktwelve,
                                serverInfo : encodeURIComponent($serverInfo),
                                dataPut : data.accounts
                            }));
                            //统计查询
                            if(data.accounts.length < 1){
                                $17.tongji("找回学号-未获取学号","找回学号-查询");
                            }else{
                                $17.tongji("找回学号-已获取学号","找回学号-查询");
                            }
                        }else{
                            $17.alert(data.info);
                        }
                    });
                });

                $(document).on("click", ".js-backSearch", function(){
                    location.reload();
                });

                $id.searchResult.on("click", ".nowGotoLogin", function(){
                    $17.tongji("找回学号-立即去登录");
                });

                $id.searchResult.on("click", ".notPasswordBtn", function(){
                    var data = {
                        captchaToken : '${captchaToken!''}',
                        userType : userType,
                        account : $(this).attr("account")
                    };

                    $.post('/ucenter/resetpwdstartwoc.vpage', data, function(data){
                        if(data.success){
                            location.href = "/ucenter/resetpwdstep.vpage?" + $.param({
                                'step': 'step2', 'token': data.token
                            });
                        }else{
                            $17.alert((data && data.info) ? data.info : "没有找到相关用户信息", function(){
                                $17.tongji("忘记账号入口-进入忘记密码");
                                refreshCaptcha();
                            });
                        }
                    });
                });
            }
        }.init();

        refreshCaptcha();
    });
    function refreshCaptcha() {
        $('#captchaImage').attr('src', "/captcha?" + $.param({
            'module': 'findAccount',
            'token': '${captchaToken!''}',
            't': new Date().getTime()
        }));
    }

    setModeHeight("#signup_form_box", 1050, 250);
    $(window).resize(function () {
        setModeHeight("#signup_form_box", 1050, 250);
    });

    function setModeHeight(id, h ,n) {
        var _winHeight = $(window).height();
        var _defHeight = h || 700;
        var _defLessCount = n || 0;

        if(_winHeight <= _defHeight){
            $(id).height(_defHeight - _defLessCount);
        }else{
            $(id).height( _winHeight - _defLessCount);
        }
    }
</script>
</@com.page>