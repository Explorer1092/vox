<!DOCTYPE HTML>
<html>
<head>
    <title>一起作业 - 一起作业 www.17zuoye.com</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="Refresh" content="0; url=/" />
</head>
<body>
</body>
</html>


<#--
<#import "module.ftl" as temp>
<#assign schoolSectionFlag = currentTeacherWebGrayFunction.isAvailable("JMS", "Invitation")!false>&lt;#&ndash;中小学邀请灰度&ndash;&gt;
<#assign isHolidayGrayFlag = (!currentTeacherWebGrayFunction.isAvailable("PHONEFEE", "BLACKLIST"))/>
<@temp.page title="index">
<#assign chineseSubFlag = currentTeacherWebGrayFunction.isAvailable("Chinese", "Register")!false>&lt;#&ndash;语文灰度&ndash;&gt;
&lt;#&ndash;
<#if (isHolidayGrayFlag && schoolSectionFlag)!false>
<div class="w-base" style="border: none;">
    <img src="<@app.link href='public/skin/teacherv3/images/intive/invite-headBanner.png'/>" width="100%">
</div>
</#if>
&ndash;&gt;

<div class="t-awardInvitation-box">
    <div class="w-base">
        <div class="w-base-title">
            <h3>邀请</h3>
            &lt;#&ndash;<a href="http://help.17zuoye.com/?page_id=1427" target="_blank" class="w-orange" style="display: inline-block; padding: 17px 0 0 14px">关于邀请活动11月9号下线的说明</a>&ndash;&gt;
            &lt;#&ndash;<div class="w-base-ext">
                <span class="w-bast-ctn">当前为假期，邀请新老师或您邀请的老师在假期达成认证，双方无奖励</span>
            </div>&ndash;&gt;
        </div>
        <div class="ai-main">
            <div class="ai-side">
                <h4>短信邀请：</h4>
                <dl class="invitationList">
                    <dt>被邀请老师姓名：</dt>
                    <dd><span><input type="text" class="w-int" placeholder="你想邀请谁？" id="invitedTeacherName"></span></dd>
                </dl>
                <dl class="invitationList">
                    <dt>被邀请老师学段：</dt>
                    <dd>
                        <div class="schoolSection">
                            <span class="subject js-schoolSection" data-type="primary">小学</span>
                            <#if schoolSectionFlag>
                                <span class="subject js-schoolSection" data-type="junior">中学</span>
                            </#if>
                        </div>
                    </dd>
                </dl>
                <dl class="invitationList">
                    <dt>被邀请老师学科：</dt>
                    <dd class="js-subItemDiv" data-chinesetag="${chineseSubFlag?string}">
                        <span class="subject js-subjItem" data-type="ENGLISH"><span class="text"><#if (currentTeacherDetail.subject == "ENGLISH")!false>同科<#else>同班</#if></span>英语</span>
                        <span class="subject js-subjItem" data-type="MATH"><span class="text"><#if (currentTeacherDetail.subject == "MATH")!false>同科<#else>同班</#if></span>数学</span>
                        <#if (chineseSubFlag)!false><span class="subject js-subjItem" data-type="CHINESE"><span class="text"><#if (currentTeacherDetail.subject == "CHINESE")!false>同科<#else>同班</#if></span>语文</span></#if>
                        &lt;#&ndash;<span class="subject js-subjItem" data-type="${(currentTeacherDetail.subject)!}">同科老师</span>&ndash;&gt;
                    </dd>
                </dl>
                <#if schoolSectionFlag>
                    <dl class="invitationList" data-type="junior" style="display: none;">
                        <dt>被邀请老师学校：</dt>
                        <dd><span><input type="text" class="w-int" placeholder="仅邀请本城市的中学老师可得奖励" id="schoolName" maxlength="60" style="width: 180px;"></span></dd>
                    </dl>
                </#if>
                <dl class="invitationList">
                    <dt>被邀请老师手机：</dt>
                    <dd><span><input type="text" class="w-int" placeholder="一起作业将严格保密" id="invitedTeacherMobile" maxlength="11"></span></dd>
                </dl>
                <dl class="invitationList">
                    <dt>验证码：</dt>
                    <dd>
                        <span><input id="code" name="code" type="text" class="w-int smsCodeContent" style="width: 56px;"></span>
                        <span class="inline_df_share">
                            <img id='captchaImage' height="25px;"/>
                        </span>
                        <span class=" inline_df_share w-blue" >
                            <a class="w-blue" href="javascript:createCode();" style="line-height: 150%;"> 看不清 换一张</a>
                        </span>
                    </dd>
                    <dd><div class="ai-btn"><a href="javascript:void(0);" class="w-btn w-btn-small" id="invite_submit_but">发送邀请</a></div></dd>
                </dl>
            </div>
            <div class="ai-rule">
                <h4>奖励规则</h4>
                <div class="ai-column">

                    <a href="javascript:void(0);" class="binding_btn" id="submit_weiXin_but">点击绑定</a>
                    <script type="text/html" id="t:weiXinSideDetail">
                        <div class='weiXinSideDetail' style='text-align: center'>
                            <dl>
                                <dt><img src='<%=weiXinCode%>' width='200' height='200'/></dt>
                                <dd>微信扫一扫<br>您获得的话费奖励将加倍</dd>
                            </dl>
                            <div style="clear:both;"></div>
                        </div>
                    </script>
                    <script type="text/javascript">
                        $(function(){
                            var weiXinCode;
                            $("#submit_weiXin_but").on("click", function(){
                                if( !$17.isBlank(weiXinCode) ){
                                    $.prompt(template("t:weiXinSideDetail", { weiXinCode : weiXinCode}),{
                                        title : "扫一扫二维码",
                                        buttons : {}
                                    });
                                    return false;
                                }

                                $.get("/teacher/qrcode.vpage", function(data){
                                    if(data.success){
                                        weiXinCode = data.qrcode_url;
                                    }else{
                                        weiXinCode = "//cdn.17zuoye.com/static/project/app/publiccode_teacher.jpg";
                                    }

                                    $.prompt(template("t:weiXinSideDetail", { weiXinCode : weiXinCode}),{
                                        title : "扫一扫二维码",
                                        buttons : {}
                                    });
                                    $17.tongji("个人中心-基本信息-点击获取二维码");
                                });
                            });
                        });
                    </script>
                    <p>不关注微信或取消关注只得一半话费奖励</p>
                </div>
                <#if isHolidayGrayFlag!false>
                <div class="ai-list">
                    <ul style="color: #f00;">
                        <li>邀请小学同科或同班老师，双方最多各得30元！</li>
                        <#if schoolSectionFlag>
                            <li>邀请中学老师双倍话费！双方最多各得<span style="font-size: 22px;">60</span>元！</li>
                        </#if>
                    </ul>
                    <div class="l-btn">
                        <a href="http://help.17zuoye.com/?p=<#if schoolSectionFlag>1343<#else>1339</#if>" class="rule_btn" target="_blank">查看详细规则></a>
                    </div>
                </div>
                </#if>
                <div class="ai-tip">

                </div>
            </div>
            <div style="clear: both;"></div>
        </div>
    </div>
    <div class="w-base" id="inviteTableDiv"></div>
    <div class="message_page_list"></div>

</div>
<script type="text/html" id="t:progressSearchTable">
    <div class="invite-title">
        <h3>邀请进度查询</h3>
    </div>
    <div class="ai-container">
        <div class="w-table">
            <table>
                <thead>
                <tr class="odd">
                    <td style="width: 156px">姓名（手机号）</td>
                    <td style="width: 156px">是否达成认证</td>
                    <td style="width: 156px">活动剩余天数</td>
                    <td>获得话费</td>
                    <td>老师学段</td>
                </tr>
                </thead>
                <tbody>
                <% for(var i = 0 ;i < data.length; i++ ) { %>
                <tr>
                    <td><span><%=data[i].name%></span></td>
                    <td><% if(data[i].auth) { %>已认证<% }else{ %>未认证<% } %></td>
                    <td><%=data[i].cd%></td>
                    <td><%=data[i].fee%></td>
                    <td><%=data[i].ktwelve%></td>
                </tr>
                <% } %>
                </tbody>
            </table>
            <div class="ai-arrow">邀请同班老师需要至少30名共同认证学生完成双方各6次作业才可以获得奖励噢！</div>
        </div>
    </div>

</script>
<script>
    var subjectKey = $uper.subject.key;
    //验证码
    function createCode() {
        $("#captchaImage").attr('src', "/captcha?" + $.param({
                    'module': 'teacherInviteTeacher',
                    'token': '${captchaToken!''}',
                    't': new Date().getTime()
                }));
        $(".smsCodeContent").val("");
    }

    /*渲染邀请搜索*/
    function renderInvitedTable(param){
        $.post("/teacher/invite/progressjmstjmst.vpage",$.param(param),function(result){
            if(result.success){
                $('#inviteTableDiv').html(template('t:progressSearchTable',{data : result.pagination.content}));
                $(".message_page_list").page({
                    total: result.pagination.totalPages || 1,
                    current: result.pn || 1,
                    autoBackToTop: false,
                    jumpCallBack: function(index){
                        var param = {
                            pn : index
                        };
                        renderInvitedTable(param);
                    }
                });
            }else{
                $17.alert(result.info);
                createCode();
            }
        });
    }

    $(function () {
        createCode();

        renderInvitedTable({pn : 1});

        //pageInvited
        $(".message_page_list").page({
            total: ${(historyOrdersPage.getTotalPages())!1},
            current: ${(pageNum)!1},
            autoBackToTop: false,
            jumpCallBack: function(index){
                var param = {
                    pn : index
                };
                renderInvitedTable(param);
            }
        });

        //学段
        var schoolSectionType = "primary";
        var currentSubject = "${(currentTeacherDetail.subject)!0}";
        var selectSubject = "";
        var selectClassItems = [];
        $(document).on('click','.js-schoolSection',function(){
            var $this = $(this);
            var $subjItem = $(".js-subjItem");
            var $subChinese = $(".js-subjItem[data-type='CHINESE']");

            $this.addClass("active")
                    .siblings().removeClass("active");

            $subjItem.removeClass("active");
            if($this.attr("data-type") == "junior"){
                $subjItem.find(".text").hide();
                $subChinese.hide();
                schoolSectionType = "junior";
                selectClassItems = [];

                $(".invitationList[data-type='junior']").show();
            }else{
                $subjItem.find(".text").show();
                $subChinese.show();
                schoolSectionType = "primary";
                $(".invitationList[data-type='junior']").hide();
            }
        });

        //学科
        $(document).on('click','.js-subjItem',function(){
            var $this = $(this);
            var $thisType = $this.attr("data-type");

            selectSubject = $thisType;

            if(!$this.hasClass("active")){
                selectClassItems = [];
            }

            if(schoolSectionType == "primary" && $thisType != currentSubject){
                //同班老师
                SelectJoinClass(selectClassItems, $this);
            }

            $this.addClass("active")
                    .siblings().removeClass("active");
        });

        $(document).on("click", ".js-inviteCheckClazz", function(){
            var $this = $(this);
            var $groupId = $this.attr("data-groupid");

            if($this.hasClass("active")){
                $this.removeClass("active");
                selectClassItems.splice( $.inArray($groupId, selectClassItems), 1)
            }else{
                $this.addClass("active");
                selectClassItems.push($groupId);
            }
        });

        function SelectJoinClass($currentItems, $that){
            $.post("/teacher/invite/groups.vpage", {}, function(data){
                if(data.success){
                    if(data.clazzs.length > 0){
                        $.prompt(template("T:选择同班班级", {items : data.clazzs, currentItems : $currentItems}), {
                            title : "请选择班级",
                            buttons : {"确定": true},
                            position : { width: 600}
                        });
                    }else{
                        $.prompt("<div style='font-size: 16px; text-align: center;'>你还没有创建班级！</div>", {
                            title : "系统提示",
                            buttons : {"去创建班级": true},
                            submit : function(e, v){
                                if(v){
                                    window.location.href = "${ProductConfig.getUcenterUrl()}/teacher/systemclazz/clazzindex.vpage?step=showtip";
                                }
                            }
                        });
                    }
                }else{
                    //$17.alert(data.info);
                }
            });
        }

        /*发出邀请*/
        $('#invite_submit_but').on('click', function () {
            var invitedTeacherName = $('#invitedTeacherName');
            var invitedTeacherMobile = $('#invitedTeacherMobile');
            var code = $('#code');
            var $schoolName = $('#schoolName');
            var $currentSubject = $('.js-subjItem.active');

            if (!$17.isValidCnName(invitedTeacherName.val())) {
                invitedTeacherName.focus();
                $17.alert("请填写老师的中文姓名");
                return false;
            }

            if(!$('.schoolSection>span.active').length){
                $17.alert("请选择被邀请老师学段");
                return false;
            }

            if(!$currentSubject.length){
                $17.alert("请选择被邀请老师学科");
                return false;
            }

            var $data = {
                realname: $.trim(invitedTeacherName.val()),
                mobile: invitedTeacherMobile.val(),
                captchaToken: "${captchaToken!''}",
                captchaCode: code.val(),
                subject: $currentSubject.attr("data-type")
            };

            //同班
            if(schoolSectionType == "primary" && selectSubject != currentSubject){
                if( selectClassItems.join().length < 1 ){
                    SelectJoinClass(selectClassItems, $currentSubject);
                    //$17.alert("请选择同班班级");
                    return false;
                }

                $data.groupIds = selectClassItems.join();
            }

            if(schoolSectionType == "junior"){
                $data.schoolName = $schoolName.val();

                if( $17.isBlank($data.schoolName) ){
                    $17.alert("请选择被邀请老师学校");
                    return false;
                }

                $17.voxLog({
                    module : "teacherJuniorInvite",
                    op : "submit",
                    realname_invite : $data.realname,
                    schoolName_invite : $data.schoolName,
                    mobile_invite : $data.mobile,
                    subject_invite : $data.subject
                });
            }

            if(!$17.isMobile(invitedTeacherMobile.val())){
                invitedTeacherMobile.focus();
                $17.alert("请填写正确的手机号");
                return false;
            }

            if($17.isBlank(code.val())){
                code.focus();
                $17.alert("请填验证码");
                return false;
            }

            $.post('/teacher/invite/sms.vpage', $data ,function(data){
                if(data.success){
                    $.prompt("<div class='w-ag-center'><h3 style='margin-bottom: 20px;'>邀请短信已发送！</h3><p>" + $data.realname + "老师收到邀请后可能仍然不会使用，快去指导指导TA吧！</p></div>", {
                        title: "系统提示",
                        buttons: {"好的，我会当面告诉TA使用": true},
                        position: {width: 500},
                        submit: function () {
                            location.reload();
                        },
                        close: function () {
                            location.reload();
                        }
                    });
                }else{
                    $17.alert(data.info);
                    createCode();
                }
            });
        });

        /*获取二维码*/
        var QRCodeImgUrl = null;
        $17.getQRCodeImgUrl({
            role : "teacher"
        }, function(url){
            QRCodeImgUrl = url;
            $(".QRCodeImgUrl").append('<img style="width: 120px; height: 120px;" src='+QRCodeImgUrl+' alt="二维码"/>');
        });

        $(document).on("click", ".click-binding-weixin", function(){
            $.prompt('<div style="text-align: center;"><div style="color: #f00;">请扫描下方二维码绑定微信！可得双倍话费奖励！</div><img style="width: 200px; height: 200px;" src='+QRCodeImgUrl+' alt="二维码"/></div>',{
                title : "绑定微信",
                buttons : {"完成": true}
            });
        });

    });
</script>

<script type="text/html" id="T:选择同班班级">
    <h4 style="font-size: 16px; font-weight: normal; margin-top: -10px; color: #666;">选择与我邀请的老师共同执教的班级</h4>
    <div class="w-check-list">
        <%for(var i=0; i< items.length; i++){%>
            <% var selected = "";
                for(var s = 0; s < currentItems.length; s++){
                    if(items[i].groupId == currentItems[s]){
                        selected = "active";
                    }
                }
            %>
            <a href="javascript:void(0);" class="js-inviteCheckClazz <%=selected%>" title="请选择" data-groupid="<%=items[i].groupId%>" style="height: 18px; width: 130px;">
                <span class="w-checkbox"></span>
                <span class="w-icon-md" style="width: 100px; text-overflow: ellipsis; overflow: hidden;"><%=items[i].clazzName%></span>
            </a>
        <%}%>
        <div style="clear: both;"></div>
    </div>
</script>
</@temp.page>
-->
