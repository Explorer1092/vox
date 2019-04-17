<#import "guideLayout.ftl" as temp />
<@temp.page>
<@sugar.capsule js=["ko", "fastLiveFilter"]/>
<#--第一屏-->
<div class="build_head_box" data-bind="css : {'build_head_box_isInvite' : isInvited}, visible : isInvited">
    <div class="aph-back"></div>
    <div class="build_head_main">
        <a href="/login.vpage" class="logo"></a>
        <div>
            <div style="width: 570px">
                <h2>恭喜您注册成功！</h2>
            </div>
            <div class="w-form-table" id="setPasswordContainer">
                <h3>设置新的密码，以便您下次登录</h3>
                <dl>
                    <dt>账号(手机号) </dt>
                    <dd>
                        <input type="text" style="border: none;" class="w-int" data-bind="value: userMobile" readonly="readonly">
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
        </div>
    </div>
    <div class="build_head_back"></div>
</div>

<#--第二屏-->
<div class="build_down_box" data-bind="visible : !isInvited">
    <div class="build_head_box" style="padding-top: 40px;"><div class="build_head_main"><a href="/login.vpage" class="logo"></a></div></div>
    <div class="build_nav">
        <h4 id="stepInfo">按照下面的步骤来选择您的学校！</h4>
        <ul class="clear">
            <li class="active stepGuideContent" data-code="0"><span data-content="0"></span><em>◆</em></li>
            <li class="stepGuideContent" data-code="1"><span data-content="1"></span><em>◆</em></li>
            <li class="stepGuideContent" data-code="2"><span data-content="2"></span><em>◆</em></li>
            <li class="stepGuideContent sc_4" data-code="3"><span data-content="3"></span><em>◆</em></li>
        <#--<li id="myClazzNumber" class="stepGuideContent sc_4" data-code="4"><span data-content="4"></span><em>◆</em></li>--> <#--sc_4 隐藏 末尾箭头  2018-3-23 去掉任教班级数量 http://wiki.17zuoye.net/pages/viewpage.action?pageId=37407639-->
        </ul>
    </div>

    <div class="build_info_box">
        <ul class="clear" id="stepGuide">
            <li class="" data-code="0">
                <i class="build_publicimg blue_cirl">1</i>
                <strong>选择学段</strong>
                <i class="build_publicimg selected_btn"></i>
            </li>
            <li class="active" data-code="1">
                <i class="build_publicimg blue_cirl">2</i>
                <strong>选择学科</strong>
                <i class="build_publicimg selected_btn"></i>
            </li>
            <li class="active" data-code="2">
                <i class="build_publicimg blue_cirl">3</i>
                <strong>选择省市</strong>
                <i class="build_publicimg selected_btn"></i>
            </li>
            <li class="active" data-code="3">
                <i class="build_publicimg blue_cirl">4</i>
                <strong>选择学校</strong>
                <i class="build_publicimg selected_btn"></i>
            </li>
            <#--2018-3-23 去掉任教班级数量 http://wiki.17zuoye.net/pages/viewpage.action?pageId=37407639-->
            <#--<li class="active" data-code="4">
                <i class="build_publicimg blue_cirl">5</i>
                <strong>班级数量</strong>
                <i class="build_publicimg selected_btn"></i>
            </li>-->
        </ul>

        <div class="build_choice_box stepGuideGoToBox" data-code="0">
            <div class="build_arrow" style="right: 160px;">◆<span>◆</span></div>
            <p>请选择您的学段</p>
            <div id="andLearn" data-bind="foreach : ktwelve">
                <a href="javascript:void(0);" data-bind="text:text, attr:{'data-ktwelve':key}">---</a>
            </div>
        </div>

        <div class="build_choice_box stepGuideGoToBox" data-code="1" style="margin-left: 208px; display: none;">
            <div class="build_arrow" style="right: 170px;">◆<span>◆</span></div>
            <p>请选择您的学科</p>
            <div id="subject" data-bind="foreach : subject">
                <a href="javascript:void(0);" data-bind="text:text, attr:{'data-subject':key}">---</a>
            </div>
        </div>

        <div class="build_choice_box build_choice_box1 stepGuideGoToBox" style="margin-left: 140px; display: none;" data-code="2">
            <div class="build_arrow" style="right: 138px;">◆<span>◆</span></div>
            <p>请选择您的省市</p>
            <div id="shengList" data-bind="foreach: rootRegion">
                <div>
                    <span class="letter-type" data-bind="text: key"><!--key--></span>
                    <span data-bind="foreach: list">
                        <a href="javascript:void(0);" data-bind="text: text, attr: {'code': code}"></a>
                    </span>
                </div>
            </div>
            <div class="cityCode-box" id="shiListBox" style="display: none;">
                <p>请选择您的城市</p>
                <div id="shiList"></div>
            </div>
        </div>

        <div class="build_choice_box build_choice_box2 stepGuideGoToBox" style="width: 95%; display: none;" data-code="3">
            <div class="build_arrow" style="right: 300px;">◆<span>◆</span></div>
            <div class="selectSchool">
                <div style="clear: both; overflow: hidden; border-bottom: 1px solid #d9d9d9; margin: -10px -20px 0 -28px;">
                    <ul class="menu" id="regionMenuBox">
                        <li data-region="0" class="selected"><a href="javascript:void(0);">全部区县</a></li>
                        <!-- ko foreach : currentRegion-->
                        <li data-bind="attr: {'data-region': id, title: text}"><a href="javascript:void(0);" data-bind="text: text"></a></li>
                        <!-- /ko-->
                    </ul>
                </div>
                <p>请选择您的学校</p>
                <ul class="menu" id="shaixuan" style="float: right; width: 600px;">
                    <li code="0" class="selected"><a href="javascript:void(0);">全部</a></li>
                    <li code="1"><a href="javascript:void(0);">A B C D</a></li>
                    <li code="2"><a href="javascript:void(0);">E F G H</a></li>
                    <li code="3"><a href="javascript:void(0);">I J K L</a></li>
                    <li code="4"><a href="javascript:void(0);">M N O P</a></li>
                    <li code="5"><a href="javascript:void(0);">Q R S T</a></li>
                    <li code="6"><a href="javascript:void(0);">U V W X</a></li>
                    <li code="7"><a href="javascript:void(0);">Y Z</a></li>
                </ul>
                <div style="margin: 0 0 20px;">
                     <span style="position: relative; display: inline-block; vertical-align: middle;">
                        <i style="position: absolute; right: 0; top: 5px;" class="w-icon w-icon-36 searchSchool-btn"></i>
                        <label for="searchSchool" style="position: absolute; left: 10px; top: 5px; color: #aaa; cursor: text;">请输入学校名称搜索，如“清华”</label>
                        <input style="width: 280px; padding-left: 10px;" type="text" value="" id="searchSchool" placeholder="" class="w-int searchSchool-btn">
                    </span>
                </div>
                <div id="xuexiao" class="school" style="height: 375px; position: relative; overflow: hidden; overflow-y: auto; width: 100%;"></div>
                <div class="pageMessage"></div>
                <div class="addSchoolBox">如果没有找到学校  请拨打客服电话<@ftlmacro.hotline/></div>
            </div>
        </div>
        <#--2018-3-23 去掉任教班级数量 http://wiki.17zuoye.net/pages/viewpage.action?pageId=37407639-->
        <#--<div class="build_choice_box stepGuideGoToBox" data-code="4" style="margin-left: 680px; display: none;width:270px;">
            <div class="build_arrow" style="right: 85px;">◆<span>◆</span></div>
            <p>请选择您的任教班级数量</p>
            <div id="clazzNumber" data-bind="foreach : clazzNumber">
                <a href="javascript:void(0);" style="width:40px;padding-bottom:10px;" data-bind="text:text, attr:{'data-clazzNumber':key}">---</a>
            </div>
        </div>-->
        <div class="build_foot_box">
            <a class="blue gray" title="确定" href="javascript:void (0)" id="confirm_keti_register">确定</a>
        </div>
    </div>
</div>

<script type="text/javascript">
    var registerModule = {
        devTestSwitch : ${((ftlmacro.devTestSwitch)!false)?string},
        isInvited : ${((data.isInvited)!false)?string}, // True:是邀请注册老师
        userId : "${(currentUser.id)!''}",
        userMobile : "${currentUserProfileMobile!}"
    };

    //防止用户会退到上一个页面
    history.pushState(null, null, document.URL);
    window.addEventListener('popstate', function () {
        history.pushState(null, null, document.URL);
    });
</script>
<@app.script href="public/script/selectsSchoolInner.js" />
</@temp.page>