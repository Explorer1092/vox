<#import "module.ftl" as com>
<@com.page title="register">
<div style="both; padding: 20px; text-align: right;">邀请老师加入一起作业，成功加入，可得<span class="text_orange">300学豆</span></div>

<div class="rule">
    <#if hasEnglishTeacher?? && hasEnglishTeacher>
        <div class="top student_top"></div>
    <#elseif hasMathTeacher?? && hasMathTeacher>
        <div class="top student_top_e"></div>
    </#if>
    <div class="summary">
        <div class="intiveSelect" style=" width: 97%;">
            <ul class="tab" id="tab_list_box" style="width: 430px; margin: 0 auto;">
                <li data-tab="sms" class="active"><span><i class="invite_icon invite_icon_1"></i><strong>短信邀请</strong></span></li>
                <li data-tab="email"><span><i class="invite_icon invite_icon_2"></i><strong>邮件邀请</strong></span></li>
                <li data-tab="link"><span><i class="invite_icon invite_icon_3"></i><strong>链接邀请</strong></span></li>
            </ul>
            <dl class="horizontal_vox sms">
                <dt>老师姓名：</dt>
                <dd>
                    <input id="teacher_username" name="teacher_username" type="text" class="w-int">
                    <span class="text_gray_9">仅用于发短信提示</span>
                </dd>
                <dt>老师手机号：</dt>
                <dd>
                    <input id="teacher_mobile" name="teacher_mobile" type="text" class="w-int">
                    <span class="text_gray_9">一起作业将严格保密</span>
                </dd>
                <dt>验证码：</dt>
                <dd><input id="teacher_code" name="teacher_code" type="text" class="w-int"></dd>
                <dt>&nbsp;<!--code--></dt>
                <dd>
                    <span class="inline_df_share"><img id='captchaImage'/></span>
                    <span class=" inline_df_share text_gray_9">看不清 <a href="javascript:createCode();">换一张</a></span>
                </dd>
                <dd class="ctn">
                    <h5>你邀请的老师将收到如下短信：</h5>

                    <p>我是您的学生${(currentUser.profile.realname)!}（学号：${(currentUser.id)!}），邀请您加入一起作业，这是一个有趣的作业平台，我和同学们使用后学习都有不同程度提高，邀请您来和我们同学们一起分享学习的乐趣！注册地址：17zuoye.com</p>

                    <div class="text_center">
                        <a id="teacher_sms_submit" href="javascript:void(0);" class="w-btn w-btn-green"><strong>发送邀请</strong></a>
                    </div>
                </dd>
            </dl>

            <dl class="horizontal_vox email" style="display: none;">
                <dt>老师姓名：</dt>
                <dd>
                    <input id="teacher_email_username" name="teacher_email_username" type="text" class="w-int">
                    <span class="text_gray_9">仅用于发邮件提示</span>
                </dd>
                <dt>老师邮箱：</dt>
                <dd>
                    <input id="teacher_email" name="teacher_email" type="text" class="w-int">
                    <span class="text_gray_9">一起作业将严格保密</span>
                </dd>
                <dd class="ctn">
                    <div class="text_center">
                        <a id="teacher_email_submit" href="javascript:void(0);" class="w-btn w-btn-green">
                            <strong>发送邀请</strong>
                        </a>
                    </div>
                </dd>
            </dl>

            <#-- 链接邀请 -->
            <dl class="horizontal_vox link" style="display: none;">
                <dd class="ctn">
                    <h5>复制下面的链接地址通过 QQ 、 msn、飞信、微博、微信、邮箱等任何你方便的使用工具发给ta</h5>

                    <p>
                        <textarea id="copy_info_url" readonly="readonly" style="border: none; width: 98%; height: 150px; line-height: 20px; color: #666; overflow: hidden;">Hey，我是${(currentUser.profile.realname)!} 同学，正在使用一起作业平台做作业，作业内容非常有趣，效果也特别棒，产品好用又免费，邀请您注册使用，快快行动吧！${(link)!'no link'}</textarea>
                    </p>
                </dd>
            </dl>
            <div class="text_center copyLink link_btn" style="position: relative; z-index:1000; top:-58px; visibility: hidden ;">
                <a href="javascript:void(0);" class="w-btn w-btn-green">
                    <span id="clip_container"><span id="clip_button"><strong>复制链接地址</strong></span></span>
                </a>
            </div>
            <div class="clear"></div>
        </div>
    </div>
    <div class="bot"></div>
</div>
<div class="inrule" style="padding: 20px 0; overflow: hidden; *zoom:1; *display: inline;">
    <p class="w-fl-left">
        <strong>邀请注册奖励规则：</strong>
        1. 如果你和你的小伙伴儿们都向同一位老师发出了邀请，那么，被邀请的老师只能确认你们其中一位拿300学豆，所以行动要趁早哦！<br/>
        2. 你邀请的老师必须完成教师身份认证，才可以算邀请成功的，所以你得学豆的前提是，这位老师成功通过认证！<br/>
        3. 每周只能向没有使用一起作业网的老师发一次邀请短信，所以要好好珍惜每次机会，正确填写你想要邀请的老师的手机号；其他邀请方式不限制次数哦！<br/>
    </p>

    <p class="w-fl-right">
        <strong>特别声明：</strong>
        1.主办方将对所有参与者进行严格审核，任何恶意注册、重复注册、虚假信息等均视为舞弊，一经查出，除取消获奖资格外，还将从系统中扣除所有学豆和奖品兑换资格；<br>
        2. 一起作业网拥有对此次活动的最终解释。
    </p>
</div>
<script type="text/javascript">
    //验证码
    function createCode() {
        var $target = $("#captchaImage");
        $target.attr('src', "/captcha?" + $.param({
            'module': 'studentInviteTeacherBySms',
            'token': '${captchaToken}',
            't': new Date().getTime()
        }));
        return;
    }

    $(function () {
        createCode();
        //标签切换
        $("#tab_list_box li").click(function () {
            var _this = $(this);
            var tab = _this.data('tab');
            var div = _this.closest("div");
            _this.addClass('active').siblings().removeClass('active');
            $("dl", div).hide();
            $("dl." + tab, div).show();
            if (tab == "link") {
                div.find(".link_btn").css({'visibility': 'visible', 'display': 'block'});
            } else {
                div.find(".link_btn").css({'visibility': 'hidden', 'display': 'none'});
            }
        });

        //短信数据提交
        $("#teacher_sms_submit").on('click', function () {
            var _this = $(this);
            var tName = $("#teacher_username").val().replace(/\s+/g, "");
            var tMobile = $("#teacher_mobile").val();
            var tCode = $("#teacher_code").val();
            if ($17.isBlank(tName) || !$17.isCnString(tName)) {
                $17.alert("请正确填写老师的姓名。");
                return false;
            }
            if (!$17.isMobile(tMobile)) {
                $17.alert('请正确填写老师的手机号。');
                return false;
            }
            if ($17.isBlank(tCode) || $17.isCnString(tCode)) {
                $17.alert('验证码错误，请重新填写。');
                return false;
            }

            var data = {
                teacherName: tName,
                teacherMobile: tMobile,
                captchaToken: "${captchaToken}",
                captchaCode: tCode
            };
            if (_this.hasClass("waiting")) {
                return false;
            }
            _this.addClass("waiting");
            $.post("/student/invite/register.vpage", data, function (data) {
                if (data.success) {
                    $17.alert("手机邀请发送成功。");
                } else {
                    $17.alert(data.info);
                    createCode();
                }
                _this.removeClass("waiting");
            });
        });


        //邮件数据提交
        $("#teacher_email_submit").on('click', function () {
            var _this = $(this);
            var tName = $("#teacher_email_username").val().replace(/\s+/g, "");
            var tEmail = $("#teacher_email").val();
            if ($17.isBlank(tName) || !$17.isCnString(tName)) {
                $17.alert("请正确填写老师的姓名。");
                return false;
            }
            if (!$17.isEmail(tEmail)) {
                $17.alert('请正确填写老师的邮箱。');
                return false;
            }
            var data = {
                teacherName: tName,
                teacherEmail: tEmail
            };

            $.post("/student/invite/activate.vpage", data, function (data) {
                if (data.success) {
                    $17.alert("邮件邀请发送成功。");
                } else {
                    $17.alert(data.info);
                }
            });
        });

        $17.copyToClipboard($("#copy_info_url"), $("#clip_button"));
    });
</script>

</@com.page>