<#import "guideLayout.ftl" as temp />
<@temp.page>
<#--短信邀请自动生成账号的老师-->
<#assign isInvitedTeacherFlag = (data.isInvited)!false/>
<div class="build_head_box <#if isInvitedTeacherFlag>build_head_box_isInvite</#if>">
    <div class="aph-back"></div>
    <div class="build_head_main">
        <a href="/login.vpage" class="logo"></a>
        <#if isInvitedTeacherFlag>
            <div style="width: 570px">
                <h2>恭喜您注册成功！</h2>
                <#--<p style="font-size: 22px; font-weight: normal; color: #333;">现在注册并认证，赢 <strong class="w-orange">30</strong> 元话费！</p>-->
            </div>
            <div class="w-form-table" id="setPasswordContainer">
                <h3>设置新的密码，以便您下次登录</h3>
                <dl>
                    <dt>账号(手机号) </dt>
                    <dd>
                        <input type="text" style="border: none;" class="w-int" value="${currentUserProfileMobile!''}" readonly="readonly">
                    </dd>
                    <dt>新的登录密码：</dt>
                    <dd>
                        <input type="password" id="newPassword" class="w-int" value="" maxlength="18">
                        <span class="w-form-misInfo w-form-info-error errorMsg" style="display: none;"><i class="w-icon-public w-icon-error"></i><strong class="info">请输入新密码</strong></span>
                    </dd>
                    <dt>确认登录密码：</dt>
                    <dd>
                        <input type="password" id="newPasswordConfirm" class="w-int" value="" maxlength="18">
                        <span class="w-form-misInfo w-form-info-error errorMsg" style="display: none;"><i class="w-icon-public w-icon-error"></i><strong class="info">请确认新密码</strong></span>
                    </dd>
                    <dd class="form-btn">
                        <a class="w-btn" style="width: 160px; margin: 0;" id="confirm_validate_code" href="javascript:void(0);">确定</a>
                    </dd>
                </dl>
            </div>
            <script type="text/javascript">
                $(function(){
                    $("#confirm_validate_code").on("click", function(){
                        var newPassword = $("#newPassword");
                        var newPasswordVal = newPassword.val();
                        var newPasswordConfirm = $("#newPasswordConfirm");
                        var newPasswordConfirmVal = newPasswordConfirm.val();

                        if( $17.isBlank(newPasswordVal)){
                            newPassword.addClass("w-int-error");
                            newPassword.siblings(".errorMsg").show();
                            return false;
                        }

                        if( $17.isBlank(newPasswordConfirmVal) ){
                            newPasswordConfirm.addClass("w-int-error");
                            newPasswordConfirm.siblings(".errorMsg").show().find(".info").html("请输入确认新密码");
                            return false;
                        }

                        if( newPasswordVal != newPasswordConfirmVal){
                            newPasswordConfirm.addClass("w-int-error");
                            newPasswordConfirm.siblings(".errorMsg").show().find(".info").html("密码不一致，请填写输入!")
                            return false;
                        }

                        $.post("/ucenter/setmypw.vpage", {
                            new_password : newPasswordVal
                        }, function(data){
                            if(data.success){
                                $(".build_head_box_isInvite").hide();
                                $(".build_down_box").show();

                                //新密码设置成功-下一步
                                $17.voxLog({
                                    module: "reg",
                                    op : "guide-click-setNewPassword",
                                    step : 4
                                });
                            }else{
                                $17.alert(data.info);
                            }
                        });
                    });

                    $("#setPasswordContainer input").on("keydown", function(){
                        $(this).removeClass("w-int-error");
                        $(this).siblings(".errorMsg").hide();
                    });
                });
            </script>
        <#else>
            <h2>恭喜您注册成功！</h2>
            <p>下次请用
                <#if (currentUser.profile.sensitiveMobile)?has_content>
                    手机号 <span>${(currentUserProfileMobile)!''}</span>
                <#else>
                    一起作业学号 <span>${(currentUser.id)!''}</span>
                </#if>
                和<span>密码</span>登录一起作业</p>
            <#--<p>带领学生使用，领取<span style="color:#ff4d3f;">30</span>元话费！</p>-->
            <#--<p>您的一起作业学号是 <span>${currentUser.id}</span> 下次可使用此学号<#switch currentUser.webSource><#case "mobile">或者您的手机<#break /><#case "email">或者您的邮箱<#break /></#switch>登录一起作业</p>-->
        </#if>
    </div>
    <#if !isInvitedTeacherFlag>
    <div class="build_head_back"></div>
    </#if>
    <div class="build_step_btn" style="overflow: visible;">
        <#--<div style="background-color: #fff095; border: 1px solid #fce194; border-radius: 3px; color: #8f4324; font-size: 24px; font-weight: bold; padding: 10px; line-height: 100%; position: absolute; top: -70px; left: 50%; margin-left: -280px; text-align: center; width: 560px;">现在注册并认证，还能赢<span style="color: #ff4d3f;">30元</span>话费！快点下一步~</div>-->
        <a title="下一步" href="javascript:void (0)" id="nextBtn" data-event-tongji="跟踪_添加学校_点击下一步">
            <i class="build_publicimg build_arrow"></i>
            <span>下一步</span>
        </a>
    </div>
</div>
<div class="build_down_box" <#if isInvitedTeacherFlag>style="display: none;"</#if>>
    <div class="build_nav">
        <h4 id="stepInfo">按照下面的步骤来选择您的学校！</h4>
        <ul class="clear">
            <li class="active stepGuideContent" data-code="1"><span data-content="1"></span><em>◆</em></li>
            <li class="stepGuideContent" data-code="2"><span data-content="2"></span><em>◆</em></li>
            <li class="stepGuideContent" data-code="3"><span data-content="3"></span><em>◆</em></li>
            <li class="stepGuideContent sc_4" data-code="4"><span data-content="4"></span><em>◆</em></li>
        </ul>
    </div>
    <div class="build_info_box">
        <ul class="clear" id="stepGuide">
            <li class="" data-code="1">
                <i class="build_publicimg blue_cirl">1</i>
                <strong>选择学科</strong>
                <i class="build_publicimg selected_btn"></i>
            </li>
            <li class="active" data-code="2">
                <i class="build_publicimg blue_cirl">2</i>
                <strong>选择省份</strong>
                <i class="build_publicimg selected_btn"></i>
            </li>
            <li class="active" data-code="3">
                <i class="build_publicimg blue_cirl">3</i>
                <strong>选择区县</strong>
                <i class="build_publicimg selected_btn"></i>
            </li>
            <li class="active" data-code="4">
                <i class="build_publicimg blue_cirl">4</i>
                <strong>选择学校</strong>
                <i class="build_publicimg selected_btn"></i>
            </li>
        </ul>
        <div class="build_choice_box stepGuideGoToBox" data-code="1">
            <div class="build_arrow" style="right: 160px;">◆<span>◆</span></div>
            <p>请选择您的学科</p>
            <div id="subject">
                <a href="javascript:void(0);" data-subject="ENGLISH">小学英语</a>
                <a href="javascript:void(0);" data-subject="MATH">小学数学</a>
                <a href="javascript:void(0);" data-subject="CHINESE">小学语文</a>
            </div>
        </div>
        <div class="build_choice_box build_choice_box1 stepGuideGoToBox" style="margin-left: 240px; display: none;" data-code="2">
            <div class="build_arrow" style="right: 390px;">◆<span>◆</span></div>
            <p>请选择您的省份</p>
            <div id="shengList">
                <div>
                    <span class="letter-type">A-G</span>
                    <a code="340000" href="javascript:void(0);">安徽</a>
                    <a code="110000" href="javascript:void(0);">北京</a>
                    <a code="500000" href="javascript:void(0);">重庆</a>
                    <a code="350000" href="javascript:void(0);">福建</a>
                    <a code="620000" href="javascript:void(0);">甘肃</a>
                    <a code="440000" href="javascript:void(0);">广东</a>
                    <a code="450000" href="javascript:void(0);">广西</a>
                    <a code="520000" href="javascript:void(0);">贵州</a>
                </div>
                <div>
                    <span class="letter-type">H-K</span>
                    <a code="460000" href="javascript:void(0);">海南</a>
                    <a code="130000" href="javascript:void(0);">河北</a>
                    <a code="230000" href="javascript:void(0);">黑龙江</a>
                    <a code="410000" href="javascript:void(0);">河南</a>
                    <a code="420000" href="javascript:void(0);">湖北</a>
                    <a code="430000" href="javascript:void(0);">湖南</a>
                    <a code="320000" href="javascript:void(0);">江苏</a>
                    <a code="360000" href="javascript:void(0);">江西</a>
                    <a code="220000" href="javascript:void(0);">吉林</a>

                </div>
                <div>
                    <span class="letter-type">L-S</span>
                    <a code="210000" href="javascript:void(0);">辽宁</a>
                    <a code="150000" href="javascript:void(0);">内蒙古</a>
                    <a code="640000" href="javascript:void(0);">宁夏</a>
                    <a code="630000" href="javascript:void(0);">青海</a>
                    <a code="370000" href="javascript:void(0);">山东</a>
                    <a code="310000" href="javascript:void(0);">上海</a>
                    <a code="140000" href="javascript:void(0);">山西</a>
                    <a code="610000" href="javascript:void(0);">陕西</a>
                    <a code="510000" href="javascript:void(0);">四川</a>
                </div>
                <div>
                    <span class="letter-type">T-Z</span>
                    <a code="120000" href="javascript:void(0);">天津</a>
                    <a code="650000" href="javascript:void(0);">新疆</a>
                    <a code="540000" href="javascript:void(0);">西藏</a>
                    <a code="530000" href="javascript:void(0);">云南</a>
                    <a code="330000" href="javascript:void(0);">浙江</a>
                    <a code="820000" href="javascript:void(0);">澳门</a>
                    <a code="710000" href="javascript:void(0);">台湾</a>
                    <a code="810000" href="javascript:void(0);">香港</a>
                </div>
            </div>

        </div>
        <div class="build_choice_box build_choice_box3 stepGuideGoToBox" style="margin-left: 380px; display: none;" data-code="3">
            <div class="build_arrow" style="right: 280px;">◆<span>◆</span></div>
            <div id="shiListBox">
                <p>请选择您的城市</p>
                <div id="shiList"></div>
            </div>
            <div id="quListBox" style="display: none;">
                <p>请选择您的区县</p>
                <div id="quList"></div>
            </div>
        </div>
        <div class="build_choice_box build_choice_box2 stepGuideGoToBox" style="width: 90%; display: none;" data-code="4">
            <div class="build_arrow">◆<span>◆</span></div>
            <div class="selectSchool">
                <p>请选择您的学校</p>
                <ul class="clear menu" id="shaixuan">
                    <li code="0" class="selected"><a href="javascript:void(0);">全部</a></li>
                    <li code="1"><a href="javascript:void(0);">A B C D</a></li>
                    <li code="2"><a href="javascript:void(0);">E F G H</a></li>
                    <li code="3"><a href="javascript:void(0);">I J K L</a></li>
                    <li code="4"><a href="javascript:void(0);">M N O P</a></li>
                    <li code="5"><a href="javascript:void(0);">Q R S T</a></li>
                    <li code="6"><a href="javascript:void(0);">U V W X</a></li>
                    <li code="7"><a href="javascript:void(0);">Y Z</a></li>
                </ul>
                <div id="xuexiao" class="school"></div>
                <#--也可以<a href="javascript:void (0)" data-content-id="add" class="switchSchool" data-event-tongji="跟踪_添加学校_点击自定义添加学校">添加学校</a>-->
                <div class="addSchoolBox">如果没有找到学校  请拨打客服电话<@ftlmacro.hotline/></div>
            </div>
            <div class="addSchoolForm build_choice_box4" style="display: none;">
                <p>
                    请添加您的学校
                </p>
                <dl>
                    <dt>您的学校名称：</dt>
                    <dd><input type="text" id="auto_addSchoolName" maxlength="20"></dd>
                </dl>
                <dl>
                    <dt>学校所在区域：</dt>
                    <dd class="clear">
                        <div class="select_down" data-code="1">
                            <em>请选择</em>
                            <span class="arrow"></span>
                            <div class="selectList shengList_add"></div>
                        </div>
                        <b>省</b>
                        <div class="select_down shiList_add_box" data-code="2">
                            <em>请选择</em>
                            <span class="arrow"></span>
                            <div class="selectList shiList_add"></div>
                        </div>
                        <b class="shiList_add_box" data-code="2">市</b>
                        <div class="select_down" data-code="3">
                            <em>请选择</em>
                            <span class="arrow"></span>
                            <div class="selectList quList_add"></div>
                        </div>
                        <b>区</b>
                    </dd>
                </dl>
                <dl>
                    <dt>您的手机号码：</dt>
                    <dd><input type="text" id="auto_addSchoolMobile"></dd>
                    <dd>
                        <div id="infoContentBox"></div>
                        <a style="margin-right: 10px;" class="line botton_close switchSchool" data-content-id="cancel" href="javascript:void (0)" data-event-tongji="跟踪_添加学校_取消自定义添加学校">取消</a>
                        <a class="line botton_sure" href="javascript:void (0)" id="autoAddSchoolSubmit" data-event-tongji="跟踪_添加学校_自定义添加学校确定">确定</a>
                    </dd>
                </dl>
            </div>
        </div>
        <div class="build_foot_box">
            <a class="blue gray" title="确定" href="javascript:void (0)" id="confirm_keti_register">确定</a>
        </div>
    </div>
</div>
<@sugar.capsule js=["fastLiveFilter"] />
<script type="text/javascript">
    function selectInit($this){
        var step = $this.closest(".stepGuideGoToBox");
        var code = $this.closest(".stepGuideGoToBox").attr("data-code");
        var stepGuide = $("#stepGuide");
        var content = $this.text();

        step.hide();

        $(".stepGuideContent[data-code='"+ (parseInt(code)+1) +"']").addClass("active").siblings().removeClass("active");

        $(".stepGuideContent[data-code='"+ code +"']").find("span[data-content]").html("<span class='contentMe' title='"+ content +"'>"+ content +"</span>" + "<i class='build_publicimg arrow'></i>");

        if(code == 2){
            $(".stepGuideContent[data-code='3'] span[data-content]").empty();
            $(".stepGuideContent[data-code='4'] span[data-content]").empty();
            $("#confirm_keti_register").addClass("gray");

            stepGuide.find("li[data-code='3']").addClass("active");
            stepGuide.find("li[data-code='4']").addClass("active");
        }

        if(code == 3){
            $(".stepGuideContent[data-code='4'] span[data-content]").empty();
            $("#confirm_keti_register").addClass("gray");
            stepGuide.find("li[data-code='4']").addClass("active");
        }

        $(".stepGuideGoToBox[data-code='"+ (parseInt(code)+1) +"']").show().siblings(".stepGuideGoToBox[data-code]").hide();

        stepGuide.find("li[data-code='"+ (parseInt(code)+1) +"']").removeClass("active");

        if(code == 4){
            $("#confirm_keti_register").removeClass("gray").show();
        }
    }

    function selectAreaCity(code, ctn, i, off){
        var select_down = $(".select_down[data-code='"+ i +"']");

        if(i == 1){
            $(".select_down[data-code='2'] em").empty().text("请选择");
            $(".select_down[data-code='3'] em").empty().text("请选择");

            if(off){
                $(".shiList_add_box[data-code='2']").hide();
            }else{
                $(".shiList_add_box[data-code='2']").show();
            }
        }else if(i == 2){
            $(".select_down[data-code='3'] em").empty().text("请选择");
        }

        $(".select_down[data-code]").removeClass("select_down_active");

        select_down.find("em").attr("code", code).text(ctn);

        if(i == 3 && off){
            $(".selectSchool").show();
            $(".addSchoolForm").hide();
        }
    }

    $(function(){
        $17.voxLog({
            module : "newTeacherRegStep",
            op : "create-load"
        });

        //是否为移动端
        var isMobile = {
            Android: function() {
                return navigator.userAgent.match(/Android/i) ? true : false;
            },
            BlackBerry: function() {
                return navigator.userAgent.match(/BlackBerry/i) ? true : false;
            },
            iOS: function() {
                return navigator.userAgent.match(/iPhone|iPad|iPod/i) ? true : false;
            },
            Windows: function() {
                return navigator.userAgent.match(/IEMobile/i) ? true : false;
            },
            any: function() {
                return (isMobile.Android() || isMobile.BlackBerry() || isMobile.iOS() || isMobile.Windows());
            }
        };

        /*fix time: 2014-07-22*/
        var nextBtn = $("#nextBtn");
        var stepGuide = $("#stepGuide");
        var selectSchool = $(".selectSchool");
        var addSchoolForm  = $(".addSchoolForm");
        var selectDown  = $(".select_down");
        var _tempArea = {
            province : $("#shengList, .shengList_add"),
            city : $("#shiList, .shiList_add"),
            classify : $("#quList, .quList_add"),
            schoolName : $("#xuexiao")
        };
        var _tempAreaAdd = {
            province : $(".shengList_add"),
            city : $(".shiList_add"),
            classify : $(".quList_add"),
            schoolName : $("#null")
        };

        var area = _tempArea;

        //3秒自动滚动
        <#if !isInvitedTeacherFlag>
        var nextAutoScroll = setTimeout(function(){
            $("html, body").animate({scrollTop : nextBtn.offset().top + 130}, 500);
        }, 3000);
        </#if>

        //点击下一步
        nextBtn.on("click", function(){
            var $this = $(this);

            <#if !isInvitedTeacherFlag>
            clearTimeout(nextAutoScroll);
            </#if>
            $("html, body").animate({scrollTop : $this.offset().top + 130}, 500);
        });

        //点击选择
        stepGuide.find("li[data-code]").on("click", function(){
            var $this = $(this);
            var $code = $this.data("code");
            var stepBox = $(".stepGuideGoToBox[data-code='"+ $code +"']");

            if($(this).hasClass("active")){
                return false;
            }

            stepBox.show().siblings(".stepGuideGoToBox[data-code]").hide();

            selectSchool.show();
            addSchoolForm.hide();
            $("#confirm_keti_register").show();
        });

        //切换添加学校
        $(".switchSchool[data-content-id]").on("click", function(){
            var $this =$(this);
            var ckr = $("#confirm_keti_register");

            if($this.data("content-id") == "add"){
                ckr.hide();
                selectSchool.hide();
                addSchoolForm.show();
            }else{
                if($this.hasClass("botton_gray")){
                    return false;
                }
                ckr.show();
                selectSchool.show();
                addSchoolForm.hide();
            }
        });

        //添加学校里-点击选地区下拉
        selectDown.on("click", function(){
            var $this = $(this);

            $this.toggleClass("select_down_active");
        });
        /*-------------------------------------*/
        var sheng   = null;
        var shi     = null;
        var qu      = null;
        var leixing = 1;
        var xuexiao = null;
        var subject = null;

        //选择学科
        $("#subject a").on("click", function(){
            var $self = $(this);

            //统计代码
            if(!$("#subject").isFreezing()){
                $("#subject").freezing();
            }

            $("#stepInfo").hide();
            $self.addClass("active").siblings().removeClass("active");
            subject = $self.attr("data-subject");

            selectInit($(this));

            $17.voxLog({
                module : "newTeacherRegStep",
                op : "create-subject"
            });
        });

        function myFilter(){
            var $target = $("#shaixuan li.selected");

            var n = $target.attr("code");
            if(n == 1){
                $("#xuexiao li a").filter(function(){
                    return !($(this).hasClass("A") || $(this).hasClass("B") || $(this).hasClass("C") || $(this).hasClass("D"));
                }).closest("li").hide();
            }else if(n == 2){
                $("#xuexiao li a").filter(function(){
                    return !($(this).hasClass("E") || $(this).hasClass("F") || $(this).hasClass("G") || $(this).hasClass("H"));
                }).closest("li").hide();
            }else if(n == 3){
                $("#xuexiao li a").filter(function(){
                    return !($(this).hasClass("I") || $(this).hasClass("J") || $(this).hasClass("K") || $(this).hasClass("L"));
                }).closest("li").hide();
            }else if(n == 4){
                $("#xuexiao li a").filter(function(){
                    return !($(this).hasClass("M") || $(this).hasClass("N") || $(this).hasClass("O") || $(this).hasClass("P"));
                }).closest("li").hide();
            }else if(n == 5){
                $("#xuexiao li a").filter(function(){
                    return !($(this).hasClass("Q") || $(this).hasClass("R") || $(this).hasClass("S") || $(this).hasClass("T"));
                }).closest("li").hide();
            }else if(n == 6){
                $("#xuexiao li a").filter(function(){
                    return !($(this).hasClass("U") || $(this).hasClass("V") || $(this).hasClass("W") || $(this).hasClass("X"));
                }).closest("li").hide();
            }else if(n == 7){
                $("#xuexiao li a").filter(function(){
                    return !($(this).hasClass("Y") || $(this).hasClass("Z"));
                }).closest("li").hide();
            }
        }

        //加载省
        /*$.getJSON('/map/nodes.vpage?id=0', function(data){
            $.each(data, function(){
                $("#shengList, .shengList_add").append('<a code="' + this.id + '" href="javascript:void(0);">' + this.text + '</a>');
            });
        });*/

        //点击省事件
        $("#shengList a, .shengList_add a").live("click", function(){
            var $this = $(this);

            //统计代码
            if(!$("#shengList").isFreezing()){
                $("#shengList").freezing();
            }

            $17.voxLog({
                module : "newTeacherRegStep",
                op : "create-rootRegion"
            });

            $this.parents("#shengList").find("a").removeClass("active");
            $this.addClass("active")
            sheng = $this.attr("code");
            //条件不是自定义添加学校
            if(!$this.closest(".select_down").hasClass("select_down")){
                shi     = null;
                qu      = null;
                xuexiao = null;
                area = _tempArea;

                $(".schoolType:visible").addClass("active");
                $("#shaixuan li").removeClass("selected").first().addClass("selected");

                selectInit($this);
            }else{
                area = _tempAreaAdd;
            }

            area.classify.html("<div style='text-align: center; color: #999; padding: 0 0 10px;'>正在努力加载中，请稍等...</div>");

            if(sheng=="110000" || sheng == "120000" || sheng == "310000" || sheng=="500000"){
                $.getJSON("/map/nodes.vpage?id=" + sheng, function(data){
                    var _htmlData = "";

                    if(data.length > 0){
                        $.each(data, function(){
                            _htmlData += '<a code="' + this.id + '" href="javascript:void(0);" title="'+ this.text +'">' + this.text + '</a>';
                        });
                    }else{
                        _htmlData = "<div style='padding: 0 0 10px;'>暂不支持此地区 ，作业君正在努力开拓中</div>"
                    }

                    area.classify.html(_htmlData);

                    selectAreaCity($this.attr("code"), $this.text(), 1, true);
                    if($this.closest(".select_down").hasClass("select_down")){
                        $(".shiList_add_box").hide();
                    }else{
                        $("#shiListBox").hide();
                        $("#quListBox").show();
                    }
                });
            }else{
                $.getJSON("/map/nodes.vpage?id=" + sheng, function(data){
                    var _htmlData = "";

                    if(data.length > 0){
                        $.each(data, function(){
                            _htmlData += '<a code="' + this.id + '" href="javascript:void(0);" title="'+ this.text +'">' + this.text + '</a>';
                        });
                    }else{
                        _htmlData = "<div style='padding: 0 0 10px;'>暂不支持此地区 ，作业君正在努力开拓中</div>"
                    }

                    area.city.html(_htmlData);

                    selectAreaCity($this.attr("code"), $this.text(), 1);
                    if($this.closest(".select_down").hasClass("select_down")){
                        $(".shiList_add_box").show();
                    }else{
                        $("#shiListBox").show();
                        $("#quListBox").hide();
                    }
                });
            }
        });

        //点击市事件
        $("#shiList a, .shiList_add a").live("click", function(){
            var $this = $(this);
            if($this.hasClass("active")){
                return false;
            }

            //统计代码
            if(!$("#shiList").isFreezing()){
                $("#shiList").freezing();
            }

            $17.voxLog({
                module : "newTeacherRegStep",
                op : "create-cityCode"
            });

            //条件不是自定义添加学校
            $this.radioClass("active");
            shi = $this.attr("code");
            if(!$this.closest(".select_down").hasClass("select_down")){
                qu      = null;
                xuexiao = null;
                area = _tempArea;

                $(".schoolType:visible").addClass("active");
                $("#shaixuan li").removeClass("selected").first().addClass("selected");
                $("#quListBox").show();
            }else{
                area = _tempAreaAdd;
            }

            area.classify.html("<div style='text-align: center; color: #999; padding: 0 0 10px;'>正在努力加载中，请稍等...</div>");

            $.getJSON("/map/nodes.vpage?id=" + shi, function(data){
                var _htmlData = "";
                if(data.length > 0){
                    $.each(data, function(){
                        _htmlData += '<a code="' + this.id + '" href="javascript:void(0);" title="'+ this.text +'">' + this.text + '</a>'
                    });
                }else{
                    _htmlData = "<div style='padding: 0 0 10px;'>暂不支持此地区 ，作业君正在努力开拓中</div>"
                }

                area.classify.html(_htmlData);
            });

            selectAreaCity($this.attr("code"), $this.text(), 2);
        });

        //区事件
        $("#quList a, .quList_add a").live("click", function(){
            var $this = $(this);

            //统计代码
            if(!$("#quList").isFreezing()){
                $("#quList").freezing();
            }

            $17.voxLog({
                module : "newTeacherRegStep",
                op : "create-regionCode"
            });

            $this.radioClass("active");
            qu = $this.attr("code");
            xuexiao = null;

            selectAreaCity($this.attr("code"), $this.text(), 3);
            if(!$this.closest(".select_down").hasClass("select_down")){
                area = _tempArea;

                area.schoolName.empty();

                $("#shaixuan li").removeClass("selected").first().addClass("selected");
                selectInit($this);
            }else{
                area = _tempAreaAdd;
            }

            $.getJSON('/school/schoolgbfl.vpage?area=' + qu, function(data){
                if(data.rows.length > 0){
                    $.each(data.rows, function(){
                        var _html = '<li code="' + this.id + '"><a class="school_item ';
                        for(var _i = 0, _l = this.letters.length; _i < _l; _i++){
                            _html += this.letters[_i] + " ";
                        }

                        _html += '" href="javascript:void(0);" title="' + this.name + '">' + this.name + '</a></li>';
                        area.schoolName.append(_html);
                    });
                }else{
                    area.schoolName.append("<div style='padding: 40px 0; text-align: center;'>暂不支持此地区 ，作业君正在努力开拓中</div>");
                }

                if(data.rows.length > 24){
                    $(".build_info_box").css({height : "auto"});
                }

                $('#searchSchool').fastLiveFilter('#xuexiao', {
                    filter: myFilter
                });
            });
        });

        //学校筛选
        $("#shaixuan li").on("click", function(){
//            xuexiao = null;
            var $self = $(this);
            var n = $self.attr("code");
            $self.radioClass("selected");
            $("#xuexiao").find("li").hide();
            if(n == 0){
                $("#xuexiao").find("li").show();
            }else if(n == 1){
                $(".A, .B, .C, .D").closest("li").show();
            }else if(n == 2){
                $(".E, .F, .G, .H").closest("li").show();
            }else if(n == 3){
                $(".I, .J, .K, .L").closest("li").show();
            }else if(n == 4){
                $(".M, .N, .O, .P").closest("li").show();
            }else if(n == 5){
                $(".Q, .R, .S, .T").closest("li").show();
            }else if(n == 6){
                $(".U, .V, .W, .X").closest("li").show();
            }else if(n == 7){
                $(".Y, .Z").closest("li").show();
            }
        });

        //学校事件
        $("#xuexiao li").live("click", function(){
            //统计代码
            if(!$("#xuexiao").isFreezing()){
                $("#xuexiao").freezing();
            }

            $17.voxLog({
                module : "newTeacherRegStep",
                op : "create-clickSchool"
            });

            var $self = $(this);
            $("#xuexiao li").removeClass("selected");

            $self.closest("li").addClass("selected");
            xuexiao = $self.attr("code");

            selectInit($(this));
        });

        //提交学校
        $("#confirm_keti_register").on("click", function(){
            //统计代码
            if($(this).hasClass("gray")){
                return false;
            }

            $17.voxLog({
                module : "newTeacherRegStep",
                op : "create-submitSubject"
            });

            if(!$("#confirm_keti_register").isFreezing()){
                $("#confirm_keti_register").freezing();
            }

            if($17.isBlank(subject)){
                $17.alert("请选择学科");
                return false;
            }
            if($17.isBlank(xuexiao)){
                $17.alert("请选择学校");
                return false;
            }

            $.get("/teacher/guide/selectschoolsubject.vpage", {
                schoolId : xuexiao,
                subject : subject
            }, function(data){
                if(data.success){
                    setTimeout(function(){
                        location.href = "/teacher/index.vpage";
                    }, 200);
                }else{
                    $17.alert(data.info,function(){
                        if(!$17.isBlank(subject) && subject == "CHINESE"){
                            setTimeout(function(){ location.href = "/ucenter/logout.vpage"; }, 200);
                        }
                    });
                }
            });
        });

        $("#autoAddSchoolSubmit").on("click", function(){
            var $this = $(this);
            var schoolName = $("#auto_addSchoolName").val();
            var mobile = $("#auto_addSchoolMobile").val();
            var $info = $("#infoContentBox");

            if($this.hasClass("botton_gray")){
                $info.html("确定中...");
                return false;
            }

            if($17.isBlank(subject)){
                $info.html("请选择学科");
                return false;
            }

            if($17.isBlank(schoolName)){
                $info.html("请输入您的学校名称");
                return false;
            }

            if(schoolName.length > 19){
                $info.html("请输入20个字以内的学校名称");
                return false;
            }

            if($17.isBlank(qu)){
                $info.html("请选择区");
                return false;
            }

            if($17.isBlank(mobile)){
                $info.html("请输入您的手机号码");
                return false;
            }

            if(!$17.isMobile(mobile)){
                $info.html("请输入正确手机号码");
                return false;
            }

            $info.html("");
            $this.addClass("botton_gray").html("确定中...").siblings().addClass("botton_gray");

            $.post("/teacher/guide/noschoolfb.vpage", {
                subject : subject,
                county: qu,
                schoolName : schoolName,
                mobile: mobile
            }, function(data){
                if(data.success){
                    $info.html("添加成功");
                    setTimeout(function(){
                        location.href = "/";
                    }, 100);
                }else{
                    $info.html(data.info);
                    $this.removeClass("botton_gray").html("确定").siblings().removeClass("botton_gray");
                }
            });
        });
    });
</script>
</@temp.page>