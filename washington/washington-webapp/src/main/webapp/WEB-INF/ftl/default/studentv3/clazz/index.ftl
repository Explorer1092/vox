<#import '../layout/layout.ftl' as temp>
<#import "clazznews.ftl" as clazznews />
    <@temp.page pageName='clazzRoom' clazzName="">
    <@sugar.capsule css=["new_student.widget.myclass"] />
    <div class="my-class-container">
        <div class="my-class-header">
            <div class="my-space-inner" style="position: relative;">
                <#--<#include "starhead.ftl">-->
                <div class="my-space-info" style="position: absolute; bottom: 10px; left: 0; width: 100%;">
                    <div class="class-info">
                        <div style="float: left;">
                            <span class="title">${(currentStudentDetail.clazzLevel.description)!}${(currentStudentDetail.clazz.className)!}</span>
                            <#--<span class="level classRatingRules" style="cursor: pointer;">${(currentStudentDetail.clazz.level)!0}</span>-->
                        </div>
                    <#--任命小组长-->
                        <#--<#include "tinygroup.ftl"/>-->
                    </div>
                    <div style="position: absolute; right:0; top: 10px;">
                        <span class="spacing">
                            <#if teachers?has_content && teachers?size gt 0>
                            <#list teachers as teacher>
                                <#if teacher_index == 0>
                                <#--${(teacher.subject.value)!}老师：${(teacher.profile.realname)!}-->给老师送
                                <a href="javascript:void(0);" data-teacher_id="${(teacher.id)!}" class="send-gift-button"><i class="space-icon space-icon-4"></i></a>&nbsp;&nbsp;&nbsp;
                                </#if>
                            </#list>
                            </#if>
                        </span>
                    </div>
                </div>
                <div class="w-clear"></div>
            </div>
        </div>
        <div class="my-class-content">
            <div class="my-space-inner">
                <div class="content-left">
                    <!--//start - 我的信息-->
                    <div class="my-space-case">
                        <div class="case-top">
                            <div class="my-space-skin-arrow bubble_but"><span class="space-icon space-icon-10"></span></div>
                        </div>
                        <div class="case-ctn">
                            <dl class="my-space-personal">
                                <dt id="edit_my_photo_but" style="cursor: default;" class="v-studentVoxLogRecord" data-op="fixAvatar">
                                    <#if showDecoration!false><div style="position: absolute; right: 0; top: 0;"><span class="w-icon w-icon-exclusive"></span></div></#if>
                                    <img id="user_avatar" src="<@app.avatar href='${(currentUser.fetchImageUrl())!}'/>"/>
                                <#--<div class="photo-edit">
                                    <span class="space-icon space-icon-8"></span>
                                    <span class="space-icon space-icon-9"></span>
                                </div>-->
                                </dt>
                                <dd>
                                    <h3>
                                    ${(currentUser.profile.realname)!''}
                                        <#if (personalCard.isLeader)!false><i class="t-spareGroup-maneger-icon"></i></#if>
                                    </h3>

                                    <p><span class="title">学豆：</span>${(personalCard.silver)!'0'}</p>

                                    <p><span class="title">学霸：</span>${(personalCard.smCount)!'0'}</p>

                                    <p><span class="title">被赞：</span>${(personalCard.likeCount)!'0'}</p>
                                    <p class="service-product">
                                    <#--<#if (personalCard.payOpen)?? && (personalCard.payOpen)>-->

                                            <#-- 一二年级的学生不应该看到阿分题的图标 -->
                                            <#--<#if currentStudentDetail.getClazzLevelAsInteger() gt 2 >
                                                <a href="/apps/afenti/exam.vpage" target="_blank" class="space-icon <#if (personalCard.afentiExam)?? && (personalCard.afentiExam)>space-icon-1<#else>space-icon-1-gray</#if>"></a>
                                            </#if>-->

                                    <#--</#if>-->
                                    <#if (personalCard.isLittleChampion)!false>
                                        <span class="space-icon space-icon-24"></span>
                                    </#if>
                                    </p>
                                </dd>
                            </dl>

                            <#--module start-->
                            <div class="receive-gifts-paradise">
                                <div class="banner">
                                    <div class="tag">礼物乐园</div>
                                    <div class="info">
                                        <p class="sub">别懒！你和同学的友谊需要小礼物～</p>
                                        <div class="btn">
                                            <a href="/student/gift/index.vpage" class="btn-send-gift">去送礼物</a>
                                        </div>
                                    </div>
                                </div>
                                <!--有收到礼物-->
                                <div class="no-gift-content gifts-acknowledge">
                                    <div class="title">
                                        <span class="prompt">收到的礼物</span>
                                        <#if (personalCard.noThanksCount gt 0)!false><span class="caption">（还有 <b>${(personalCard.noThanksCount)!0}</b> 份友谊没有答谢）</span></#if>
                                    </div>
                                    <#if (personalCard.gifts?size gt 0)!false>
                                        <div class="giftTabGift">
                                            <div class="content">
                                                <ul class="listbox">
                                                    <#list personalCard.gifts as g>
                                                        <li data-ps="<#if (g['ps'])?has_content>${(g['ps'])}<#else>无赠言</#if>">
                                                            <p>
                                                                <i class="gift_icon" style="background-color: #eee; width: 60px; height: 60px;">
                                                                    <img src="<@app.link href="public/skin/common/images/gift/${g['img']}"/>" width="60"/>
                                                                </i>
                                                            </p>
                                                            <div class="line">
                                                                <strong class="t_1">${(g['senderName'])!'----'}</strong>
                                                                <#if (g['isThanks'])!false>
                                                                    <strong class="t_3"><a href="javascript:void(0);" class="acknowledge-btn acknowledge-disabled-btn">已答谢</a></strong>
                                                                <#else>
                                                                    <strong class="t_3"><a href="javascript:void(0);" class="acknowledge-btn send-gift-button" data-student_id="${(g['senderId'])!}" data-history_id="${(g['historyId'])!}">答谢</a></strong>
                                                                </#if>
                                                            </div>
                                                        </li>
                                                    </#list>
                                                    <li>
                                                        <a href="/student/gift/receive/index.vpage" class="w-blue"><strong class="t_4">更多</strong></a>
                                                    </li>
                                                </ul>
                                                <div class="clear"></div>
                                            </div>
                                        </div>
                                    <#else>
                                        <div class="column">
                                            <i class="no-gift-icon"></i>
                                            <p class="no-gift-icon-con" style="padding-right: 0;">啊噢，近30天还没有收到任何礼物噢～<br/>主动送礼物才会收到更多的回赠！</p>
                                        </div>
                                    </#if>
                                </div>
                            </div>
                            <#--module end-->
                        </div>
                        <div class="case-bot"></div>
                    </div>
                    <!--我的信息 - end//-->

                    <!--//start - 同班同学-->
                    <#--<div class="my-space-case">-->
                        <#--<div class="case-top"></div>-->
                        <#--<div class="case-ctn">-->
                            <#--<div class="my-space-classmate">-->
                                <#--<div class="space-title">-->
                                    <#--<h4>同班同学 (<span id="my_classmates_count">0</span>人)</h4>-->

                                    <#--<p class="tag" id="rankList">-->
                                        <#--<a href="javascript:void(0);" data-rank="silverrank">学豆榜</a>|-->
                                        <#--<a href="javascript:void(0);" data-rank="smcountrank">学霸榜</a>|-->
                                        <#--<a href="javascript:void(0);" data-rank="likecountrank">被赞榜</a>-->
                                    <#--</p>-->
                                <#--</div>-->
                                <#--<div class="clear"></div>-->
                                <#--<div class="userListBox">-->
                                    <#--<ul id="students_list_box">-->
                                    <#--&lt;#&ndash;我的同班同学&ndash;&gt;-->
                                    <#--</ul>-->
                                <#--</div>-->
                            <#--</div>-->
                        <#--</div>-->
                        <#--<div class="case-bot"></div>-->
                    <#--</div>-->
                <#include "../activity/magic/magicBanner.ftl"/>
                </div>
                <div class="content-right">
                    <#--签到按钮-->
                    <div class="moods-click-btn">
                        <a href="javascript:void(0);" id="signIn_but" title="点击可以签到" class="v-studentVoxLogRecord" data-op="clazzRegistration">
                            <span class="day">${(personalCard.signInCount)!'0'}</span>
                            <span>Days</span>
                        </a>
                    </div>
                    <#--上传照片按钮-->
                    <#--<#if (currentStudentDetail.cityCode?? && currentStudentDetail.cityCode == 321000) || ftlmacro.devTestSwitch>
                        <div class="upload-photo-button">
                            <a href="javascript:void(0)" id="uploadPhotoButton" class="uploadPhotoButton" title="上传照片"></a>

                            <div id="popupUploadPhotoBox"></div>
                        </div>
                    </#if>-->
                    <#--班级新鲜事-->
                    <@clazznews.clazznews />
                </div>
            </div>
        </div>
    </div>

    <#--<#if passwordPopup!false>
        <#include "../organ/rmpassword.ftl" />
    </#if>-->

    <script type="text/html" id="t:classmates">
        <%for(var i = 0; i < data.length; i++){%>
        <li>
            <div class="avatar <% if ('${(currentUser.id)!0}' != data[i].studentId) {%> studentAvatar <%}%>"
                 style="cursor: default;" data-student_id='<%=data[i].studentId%>'>
                <%if(data[i].studentImg.length > 4){%>
                <img src="<@app.avatar href="<%= data[i].studentImg%>"/>"/>
                <%}%>
            </div>
            <div class="title"><%= data[i].studentName %><br>
                <% if (rankType == 'silverrank') {%>
                <i class="space-icon space-icon-6"></i><span class="silver"><%= data[i].studentUsableIntegral %></span>
                <%} else if(rankType == 'smcountrank'){%>
                <i class="space-icon space-icon-15"></i><span class="silver"><%= data[i].smCount %></span>
                <%} else if(rankType == 'likecountrank') {%>
                <i class="space-icon space-icon-5"></i><span class="silver"><%= data[i].likeCount %></span>
                <%}%>
            </div>
        </li>
        <%}%>
    </script>

    <script type="text/html" id="t:bubble">
        <div class="space-pop-skin">
            <div class="sk-menu">
                <span class="active js-clickBubbleType" data-type="DEFAULT">普通气泡</span>
                <span class=" js-clickBubbleType" data-type="MAGIC_CASTLE">魔法气泡</span>
            </div>
            <ul id="bubble_list_box">
                <%for(var i = 0; i < bubblesList.length; i++){%>
                    <li data-owned="<%=bubblesList[i].owned.toString()%>"
                        data-silver="<%=bubblesList[i].price%>"
                        data-category="<%=bubblesList[i].category%>"
                        data-name="<%=bubblesList[i].name%>"
                        data-bubble_id='<%=bubblesList[i].bubbleId%>'
                        data-order="${(vipUser!false)?string}"
                    <#--默认显示气泡-->
                    <%if(bubblesList[i].category.indexOf("MAGIC_CASTLE") > -1){%>style="display: none;" data-type="MAGIC_CASTLE"<%}else{%>data-type="DEFAULT"<%}%>
                    <%if(bubblesList[i].currentUsing) {%> class="active" <%}%> >
                    <div class="ms-space-skin ms-space-skin-<%=bubblesList[i].bubbleId%>">
                        <%if(bubblesList[i].category == 'FREE') {%>
                            <i class="space-icon space-icon-20" title="免费"></i>
                        <%}%>
                        <%if(bubblesList[i].owned) {%>
                            <i class="space-icon space-icon-21" title="已拥有"></i>
                        <%}%>
                        <i class="ms-space-arrow"></i>
                        <i class="ms-space-icon"></i>
                        <i class="ms-space-icon ms-space-icon-1"></i>
                        <i class="ms-space-icon ms-space-icon-2"></i>
                        <i class="ms-space-icon ms-space-icon-3"></i>
                        <div class="space-icon space-icon-13"></div>
                        <div class="ms-space-default">
                            <%=bubblesList[i].name%>
                            <%if(bubblesList[i].price > 0) {%>
                            <div><%=bubblesList[i].price%>学豆 有效期<%=bubblesList[i].periodOfValidity%>天</div>
                            <%}%>
                            <%if(bubblesList[i].bubbleId >= 9 && bubblesList[i].bubbleId <=11){%>
                            <div>有效期15天</div>
                            <%}%>
                        </div>
                    </div>
                    </li>
                <%}%>
            </ul>
            <div class="clear"></div>
        </div>
    </script>

    <#-- 签到 -->
    <script type="text/html" id="t:signInBox">
        <div class="moods-box">
            <div class="info">
                <div class="inner">
                    每天作业完成后获得<span>免费签到机会!</span>  <#if (personalCard.payOpen)!false> VIP特权：天天免费签到!</#if>
                </div>
            </div>
            <div class="moods-list">
                <ul id="moods_list_box">
                    <%for(var i = 0; i < data.length; i++){%>
                        <#--过滤表白签到-->
                        <%if( data[i].imgUrl != "m00010.gif" ){%>
                        <li data-moods_id="<%=data[i].id %>" data-is_need_pay='<%=needPay.toString()%>'>
                            <a href="javascript:void(0);">
                                <span class="mood">
                                    <i></i>
                                    <img src="<@app.link href='public/skin/common/images/mood/'/><%= data[i].imgUrl %>">
                                </span>
                                <span class="name"><%= data[i].title %></span>
                                <span class="sign">
                                    <%if(needPay){%>
                                        <strong class="silver">5<i class="icon_general icon_general_12"></i></strong>
                                    <%}%>
                                    <strong>签到</strong>
                                </span>
                            </a>
                        </li>
                        <%}%>
                    <%}%>
                </ul>
            </div>
            <div class="after-box" style="display: none;">
                <div class="fade"></div>
                <div class="after">
                    <!--step1-->
                    <p class="signIn_success" style="display: none;">
                        <i class="mood-icon"><img src="" alt="心情图"/></i><span class="f-mid">签到成功！</span>
                    </p>
                    <!--step2-->
                    <p class="signed" style="display: none;">今天已经签过到了<br/>记得明天再来哦：）</p>
                    <!--step3-->
                    <p class="small sign_pay" style="display: none;">需要5学豆签到，确认支付吗？</p>

                    <div class="sign_pay" style="display: none; text-align: center;">
                        <a id="mood_need_pay_cancel_but" href="javascript:void(0);" class="w-btn-dic w-btn-gray-new"><strong class="text_gray_6">取消</strong></a>
                        <a id="mood_need_pay_submit_but" href="javascript:void(0);" class="w-btn-dic w-btn-green-new">确认</a>
                    </div>
                </div>
            </div>
        </div>
    </script>

    <script type="text/javascript">
    /*编辑头像*/
    function Avatar_callback() {
        //为了更新同班同学 新鲜事 等等 头像
        setTimeout(function () {
            window.location.reload();
        }, 200);
    }

    function Avatar_Cancel() {
        $.prompt.close();
    }

    function showQtip($this, content, width, mTop) {
        if ($17.isBlank(content)) {
            return false;
        }
        mTop = $17.isBlank(mTop) ? 0 : mTop;
        $this.qtip({
            content: {
                text: content
            },
            hide: {
                fixed: true,
                delay: 150,
                leave: false
            },
            position: {
                at: 'bottom center',
                my: 'top center',
                viewport: $(window),
                effect: false,
                adjust: {
                    y: mTop
                }
            },
            style: {
                classes: 'qtip-rounded',
                width: width
            }
        });

    }

    //加载学生名片
    function loadStudentCard() {
        $(".studentAvatar").each(function () {
            var $this = $(this);
            var userId = $this.data('student_id');

            $this.qtip({
                content: {
                    text: '<div class="text_center"><img class="throbber" src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>',
                    ajax: {
                        url: '/student/clazz/studentcard.vpage?userId=' + userId,
                        success: function (data) {
                            this.set('text_center', data);
                        }
                    }
                },
                hide: {
                    fixed: true,
                    delay: 150
                },
                position: {
                    at: 'bottom left',
                    my: 'top left',
                    viewport: $(window),
                    effect: false
                },
                style: {
                    classes: 'qtip-bootstrap',
                    width: 300
                }
            });
        });
    }

    //签到 数据提交
    function submitMoodsById(moodId, imgUrl) {
        $.post('/student/clazz/czsignin.vpage', {moodId: moodId}, function (data) {
            if (data.success) {
                var successBox = $(".after-box, .signIn_success");
                var signInCount = $("#signIn_but span:first").text();
                $('.signIn_success').find('img').attr('src', imgUrl);
                successBox.show();
                $("#signIn_but span:first").text(signInCount * 1 + 1);
                setTimeout(function () {
                    successBox.hide();
                }, 2000);
                reloadClazzNews();
            } else {
                var signedBox = $(".after-box, .signed");
                signedBox.show();
                $(".signed").text(data.info);
                setTimeout(function () {
                    signedBox.hide();
                }, 3000);
            }
        });
    }

    //修改气泡
    function changeBubble() {
        var bubbleId = $("#bubble_list_box li.active").data('bubble_id');
        if ($17.isBlank(bubbleId)) {
            $17.alert('请选择气泡');
            return false;
        }

        $.post('/student/clazz/changebubble.vpage', {bubbleId: bubbleId}, function (data) {
            if (data.success) {
                reloadClazzNews();
            }
            $17.alert(data.info);
        });
    }

    //更新班级新鲜事
    function reloadClazzNews() {
        //当前班级新鲜事所在标签
        var target = $("[data-newtype].active").data('newtype');
        $.get('/student/clazz/clazzlatestnews.vpage?type=' + target.toUpperCase() + '&currentPage=1', function (data) {
            $("#" + target).html(template("t:班级动态", {
                news: data.journalPage.content
            }));
        });
    }

    $(function () {
        $("#summerSeeDetail").on("click", function () {
            $17.tongji($(this).data("tongji"));
        });

        //设置qtip
        $.fn.qtip.zindex = 500;

        /*我的同班同学*/
        var rankListCache = {};
        $("#rankList a").on('click', function () {
            var $this = $(this);
            if ($this.hasClass('active')) {
                return false
            }
            $this.addClass('active').siblings().removeClass('active');
            var rank = $this.data('rank');
            var box = $("#students_list_box");
        <#if (currentStudentDetail.cityCode == 510100)!false>
            if(rank == "silverrank"){
                box.html("数据更新中...");
                return false;
            }
        </#if>

            //根据rank加载cache中的排行榜
            if(!$17.isBlank(rankListCache[rank])){
                box.html(template("t:classmates", {
                    data: rankListCache[rank][0].data,
                    rankType: rankListCache[rank][0].rankType
                }));
                loadStudentCard();
                return false;
            }

            $.get('/student/clazz/' + rank + '.vpage', function (data) {

                box.html('<div class="text_center text_gray_9">数据加载中...</div>');
                if (data.success) {
                    box.html(template("t:classmates", {
                        data: data.rankList,
                        rankType: rank
                    }));
                    var templateDate = {
                        data : data.rankList,
                        rankType: rank
                    };
                    rankListCache[rank] = [].concat(templateDate);

                    $("#my_classmates_count").text(data.rankList.length);

                    loadStudentCard();
                }
            });
        });
        $("#rankList a:first").trigger('click');

        /*var edit_my_photo_but = $("#edit_my_photo_but");

        edit_my_photo_but.hover(function () {
            $(this).find(".space-icon-9").show().animate({right: 0}, 200).siblings().hide();
        }, function () {
            $(this).find(".space-icon-8").show().siblings().css({right: -65}).hide();
        });

        //编辑我的头像
        edit_my_photo_but.on('click', function () {
            var avatar = '<iframe class="vox17zuoyeIframe" src="/ucenter/avatar.vpage?avatar_cancel=parent.Avatar_Cancel&avatar_callback=parent.Avatar_callback" width="660" marginwidth="0" height="470" marginheight="0" scrolling="no" frameborder="0"></iframe>'
            $.prompt(avatar, {
                title: "上传头像",
                buttons: {},
                position: { width: 700 }
            });
        });*/

        //切换气泡类型
        $(document).on("click", ".js-clickBubbleType", function(){
            var $this = $(this);
            var $type = $this.data("type");
            var $bubbleBox = $("#bubble_list_box");

            $this.addClass("active").siblings().removeClass("active");
            $bubbleBox.find(" li").hide();
            $bubbleBox.find(" li[data-type='"+ $type +"']").show();
        });

        //气泡
        $(document).on('click', ".bubble_but", function () {
            var currentBubbleId;
            $.post("/student/clazz/showbubbles.vpage", {}, function (data) {
                //根据数据为气泡排序
                if (data.success) {
                    var bubble = {
                        state: {
                            title: "气泡",
                            html: template("t:bubble", {bubblesList: data.bubbles}),
                            position: { width: 700, height: 720 },
                            focus: 1,
                            buttons: {"取消": false, "确定": true},
                            submit: function (e, v) {
                                e.preventDefault();
                                if (v) {
                                    changeBubble();
                                } else {
                                    $.prompt.close();
                                }
                            }
                        },
                        needPay: {
                            title: "气泡",
                            html: '是否花费 <span id="silverNumber" class="text_orange"></span> 学豆购买新鲜事气泡 <span id="bubbleName" class="text_orange"></span>？',
                            position: { width: 420},
                            focus: 1,
                            buttons: {"取消": false, "确定": true},
                            submit: function (e, v) {
                                e.preventDefault();
                                if (v) {
                                    changeBubble();

                                } else {
                                    $.prompt.goToState('state');
                                    //点击取消后 默认选中当前使用的气泡
                                    $("#bubble_list_box li[data-bubble_id=" + currentBubbleId + "]").addClass('active').siblings().removeClass('active');
                                }
                            }
                        },
                        order: {
                            title: "气泡",
                            html: '购买<span id="afentiName" data-bubble_type="" class="text_orange"></span>,解锁专属气泡',
                            position: { width: 420},
                            focus: 1,
                            buttons: {"取消": false, "了解更多": true},
                            submit: function (e, v) {
                                e.preventDefault();
                                if (v) {
                                    //根据付费产品类型 去相应的宣传页
                                    var t = $("#afentiName").attr('data-bubble_type');
                                    switch (t) {
                                        case "AFENTI_EXAM":
                                            location.href = '/apps/afenti/exam.vpage';
                                            break;
                                        case "AFENTI_BASIC":
                                            location.href = '/apps/afenti/basic.vpage';
                                            break;
                                        case "TALENT":
                                            location.href = '/apps/afenti/order/talent-cart.vpage';
                                            break;
                                    }
                                } else {
                                    $.prompt.goToState('state');
                                    //点击取消后 默认选中当前使用的气泡
                                    $("#bubble_list_box li[data-bubble_id=" + currentBubbleId + "]").addClass('active').siblings().removeClass('active');

                                }
                            }
                        }
                    };

                    $.prompt(bubble, {
                        loaded: function () {
                            currentBubbleId = $("#bubble_list_box li.active").data('bubble_id');
                            //选择气泡
                            $("#bubble_list_box").on('click', 'li', function () {
                                var $this = $(this);
                                $this.addClass('active').siblings().removeClass('active');
                                var silver = $this.data('silver');
                                var order = $this.data('order');
                                var bubbleName = $this.data('name');
                                var category = $this.data('category');
                                var owned = $this.data('owned');
                                //需要学豆支付
                                if (silver > 0 && !owned && category == 'PAY') {
                                    $.prompt.goToState('needPay');
                                    $("#silverNumber").text(silver);
                                    $("#bubbleName").text(bubbleName);
                                    return false;
                                }
                                //付费产品
                                if (!order && (category == 'AFENTI_EXAM' || category == 'AFENTI_BASIC' || category == 'TALENT')) {
                                    $.prompt.goToState('order');
                                    $("#afentiName").text(bubbleName).attr('data-bubble_type', category);

                                }
                            });

                            if (!$("#bubble_list_box li").hasClass('active')) {
                                $("#bubble_list_box li:first").addClass('active');
                            }
                        }
                    });
                } else {
                    $17.alert('数据加载失败，请重新点击');
                }
            });
        });

        showQtip($(".bubble_but"), '修改新鲜事气泡', 150);
        showQtip($(".studyMasterBang"), '从周一开始，选出本周贡献学分最多的同学，贡献分数相同则按贡献时间先后排序，榜单每天更新一次，每周一重新排序。', 200, -30);

        //查看礼物赠言
        $(".gift_ps li").each(function () {
            var $this = $(this);
            var ps = $this.data('ps');

            $this.qtip({
                content: {
                    text: ps
                },
                hide: {
                    fixed: true,
                    delay: 150,
                    leave: false
                },
                position: {
                    at: 'bottom center',
                    my: 'top center',
                    viewport: $(window),
                    effect: false
                },
                style: {
                    classes: 'qtip-rounded',
                    width: 150
                }
            });
        });

        //签到 弹出框
        $("#signIn_but").on('click', function () {
            $.post('/student/clazz/moods.vpage', function (data) {
                if (data.success) {
                    $.prompt(template("t:signInBox", {data: data.moods, needPay: data.needPay }), {
                        prefix: "jqi-orange-",
                        title: '<div class="text_center">选择心情</div>',
                        buttons: {},
                        position: { container: '#signIn_but', x: -380, y: 100, width: 544},
                        loaded: function () {
                            var moodsLi = $("#moods_list_box li");

                            moodsLi.each(function () {
                                var $this = $(this);
                                var moodImg = $this.find("img");
                                var moodWidth = moodImg.width();
                                var moodHeight = moodImg.height();
                                if (moodWidth > moodHeight) {
                                    moodImg.width(115);
                                } else {
                                    moodImg.height(106);
                                }
                            });
                        }
                    });

                    //点击弹窗以外的区域 弹窗关闭  (jqi-orange-state_state0 是弹窗自定义的ID)
                    $(document).on("click", function (e) {
                        if ($(e.target).closest("#jqi-orange-state_state0").length == 0 && $("#jqi-orange-state_state0").is(':visible')) {
                            $.prompt.close();
                        }
                    });
                } else {
                    $17.alert('数据请求失败');
                }
            });
        });

        //签到
        $(document).on('click', '#moods_list_box li', function () {
            var $this = $(this);
            var moodId = $this.data('moods_id');
            var imgUrl = $this.find('img').attr('src');
            var isNeedPay = $this.data('is_need_pay');
            var signPayBox = $(".after-box, .sign_pay");

            //判断是否签过到 如果签过 则显示‘今日签到过’ ；如果没有签到 先判断是否需要花费学豆 。
            <#if signIn?? && signIn>
                var signedBox = $(".after-box, .signed");
                signedBox.show();
                setTimeout(function () {
                    signedBox.hide();
                }, 3000);
            <#else>
                if (isNeedPay) {
                    signPayBox.show();
                    //取消
                    $("#mood_need_pay_cancel_but").die().live('click', function () {
                        signPayBox.hide();
                    });
                    //确定
                    $("#mood_need_pay_submit_but").die().live('click', function () {
                        signPayBox.hide();
                        submitMoodsById(moodId, imgUrl);
                    });

                } else {
                    submitMoodsById(moodId, imgUrl);
                }
            </#if>
        });

        //强化学生记住密码
        <#--<#if passwordPopup!false>
            if(true){
                RmPassword.init("clazz");
                return false;
            }
        </#if>-->

        /*// 学生ugc基础数据收集
        if( !$17.getCookieWithDefault("UGCTJ") ){
            $17.setCookieOneDay("UGCTJ", "1", 1);
            $.UgcClazzPopup();
            return false;
        }*/
    });
    </script>
        <#include "sendgift.ftl"/>
        <#-- 学生上传图片地区开发 321000-扬州市 -->
        <#--<#if (currentStudentDetail.cityCode?? && currentStudentDetail.cityCode == 321000) || ftlmacro.devTestSwitch>
            <#include "uploadphoto.ftl"/>
        </#if>-->

        <#--学生ugc基础数据收集-->
        <#--<#import "../collect/record.ftl" as record />
        <@record.UGC_Record/>-->
    </@temp.page>
