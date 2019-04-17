<#import 'layout/layout.ftl' as temp>
<@temp.page clazzName='m-body-back-home' pageName="studentHome">
<@sugar.capsule js=["voxSpread"]/>

<div class="t-home-container">
<div class="t-home-main">
<!--课本-->
<div class="th-learning">
    <div class="t-learning-tasks">
        <!--left - right / btn -->
        <div class="tl-operation">
            <div class="t-relative-teacher-box" style="right: 75px; top: -40px;">
                <#if data.linkedTeachers?has_content >
                    <#list data.linkedTeachers as teacher >
                        <span class="avtor">
                        <i>
                            <img src="<@app.avatar href='${teacher.fetchImageUrl()!}'/>" alt=""/>
                        </i>
                        <span>
                            <#if teacher.fetchRealname()?has_content>
                                ${(teacher.fetchRealname())?substring(0, 1)}
                            </#if>
                            老师
                        </span>
                    </span>
                    </#list>
                </#if>
                <a class="w-relative-green-btn v-joinClazzBtn-popup" href="javascript:void (0);">输入老师号码</a>
            </div>
            <div class="th-line"><span class="m-header-line"></span></div>
            <div class="th-line" style="left: 550px;"><span class="m-header-line"></span></div>
            <span class="tlo-title" <#if data.linkedTeachers?has_content > style="margin-left: -310px" </#if>></span>
            <div id="homework_card_tab_box">
                <a href="javascript:void(0);" data-c_p_page="0" class="tlo-back tlo-back-dis prevBtn"></a>
                <a href="javascript:void(0);" data-c_n_page="1" class="tlo-next nextBtn"><span class="dong"></span></a>
            </div>
            <div class="student-guide" style="width: 165px; height: 72px; position:  absolute; right: -100px; top: -53px; z-index: 5;">
                <a href="http://mp.weixin.qq.com/s?__biz=MjM5NjE5OTc0MQ==&mid=553051557&idx=2&sn=ef3a8b8f85a4f3a6fa2d0bbe29f338d0#rd" target="_blank" onclick="$17.tongji('首页登录-点击不会注册');" style="display: block; width: 100%; height: 100%; position: static !important; background: url(<@app.link href="public/skin/studentv3/images/student-guide.gif"/>)"></a>
            </div>
        </div>
        <!--practice list-->
        <div class="tl-practice-box">
            <ul class="practice-box" id="homework_list_box" style="position: relative;">

                <#--加入班级-->
                <#if !(currentStudentDetail.clazz.classLevel)??>
                <li class="practice-block ">
                    <div class="practice-content">
                        <div class="no-content">
                            <p class="n-1">还没有班级呢~</p>
                            <p class="n-2"><span class="w-icon w-icon-7"></span></p>
                            <p class="n-3">加入班级，更多精彩赶紧行动吧！</p>
                        </div>
                        <div class="pc-btn">
                            <a href="javascript:void(0);" class="w-btn w-btn-orange v-joinClazzBtn-popup">加入班级</a>
                        </div>
                    </div>
                </li>
                <#include "taskcard/nohomework.ftl" >
                </#if>
                <#-- TODO 新手任务的位置绝对不能调动（有固定浮动引导,调错后果自负）-->
                <#include "taskcard/novice_new.ftl" >
                <#if (currentStudentDetail.clazz.classLevel)?? && !(currentStudentDetail.clazz.isTerminalClazz())>
                    <#if (data.homeworkCards)?has_content>
                        <#list data.homeworkCards as h>
                            <li class="practice-block stepNoviceTwoBox">
                                <div class="practice-content">
                                    <h4>
                                    <#--根据作业类型 显示不同的效果 -->
                                        <#switch h.homeworkType>
                                            <#case 'ENGLISH'>
                                                <span class="w-discipline-tag w-discipline-tag-1">英语作业</span>
                                                <#break >
                                            <#case 'MATH'>
                                                <span class="w-discipline-tag w-discipline-tag-2">数学作业</span>
                                                <#break >
                                            <#case 'CHINESE'>
                                                <span class="w-discipline-tag w-discipline-tag-3">语文作业</span>
                                                <#break >
                                        </#switch>
                                    </h4>
                                    <div class="count-box">
                                        <p class="count">
                                        <#-- 作业总数和完成数 -->
                                            <#if (h.finishPracticeCount)?has_content>
                                                <#list 1..h.finishPracticeCount?length as number><span class="w-icon-number  w-icon-number-blue w-icon-number-${h.finishPracticeCount?substring(number-1,number)}"></span></#list>
                                            <#else>
                                                <span class="w-icon-number  w-icon-number-blue w-icon-number-0"></span>
                                            </#if>
                                            <span class="w-icon-number w-icon-number-li"></span>
                                            <#if (h.practiceCount)?has_content>
                                                <#list 1..h.practiceCount?length as number><span class="w-icon-number w-icon-number-${h.practiceCount?substring(number-1,number)}"></span></#list>
                                            <#else>
                                                <span class="w-icon-number w-icon-number-0"></span>
                                            </#if>
                                        </p>

                                        <#--作业进度条颜色 作业为蓝色  -->
                                        <div data-box="flash-bar">
                                            <object width="72" height="72" data="<@app.link href='public/skin/studentv3/images/home_box/CircularProgressBar.swf'/>" type="application/x-shockwave-flash">
                                                <param name="movie" value="<@app.link href='public/skin/studentv3/images/home_box/CircularProgressBar.swf'/>">
                                                <param name="allowScriptAccess" value="always">
                                                <param name="allowFullScreen" value="true">
                                                <param name="flashvars" value="percent=${(h.finishPracticeCount/h.practiceCount)!0}">
                                                <param name="wmode" value="transparent">
                                            </object>
                                        </div>
                                    </div>
                                    <#if h.homeworkType == 'ENGLISH' || h.homeworkType == 'MATH'>
                                        <#if h.prize!false><div class="date-end">老师设置了随机学豆奖励</div></#if>
                                    </#if>
                                    <div class="date-end">结束时间 <strong class="time">${h.endDate!''}</strong></div>
                                    <#assign christmasType = "" christmasId = ""/>
                                    <div class="pc-btn">
                                        <#switch h.homeworkType>
                                            <#case 'ENGLISH'>
                                                <a onclick="$17.atongji('首页-开始作业按钮','/student/homework/index.vpage?from=indexCard&homeworkId=${h.homeworkId!''}')" href="javascript:void (0);" class="w-btn w-btn-green v-studentVoxLogRecord" data-op="submitVoiceWork">开始作业</a>
                                                <#assign christmasType = "ENGLISH" christmasId = h.homeworkId!''/>
                                                <#break >
                                            <#case 'MATH'>
                                                <a onclick="$17.atongji('首页-开始作业按钮','/student/homework/index.vpage?from=indexCard&homeworkId=${h.homeworkId!''}');" href="javascript:void (0);" class="w-btn w-btn-green">开始作业</a>
                                                <#assign christmasType = "MATH" christmasId = h.homeworkId!''/>
                                                <#break >
                                            <#case 'CHINESE'>
                                                <a onclick="$17.atongji('首页-开始作业按钮','/student/homework/index.vpage?from=indexCard&homeworkId=${h.homeworkId!''}');" href="javascript:void (0);" class="w-btn w-btn-green">开始作业</a>
                                                <#break >
                                        </#switch>
                                    </div>
                                </div>
                            </li>
                        </#list>
                    </#if>
                    <#include "taskcard/termreview.ftl">
                    <#include "taskcard/vacationhomework.ftl" >

                    <#include "taskcard/newexam.ftl">

                    <#-- 补做作业卡片与自学（通天塔）卡片相斥，只要有任意科目的补做作业卡片，不显示自学（通天塔）卡片；-->
                    <#include "taskcard/makeuphomework.ftl" >

                    <#--拓展任务卡片-->
                    <#include "taskcard/expandhomework.ftl">

                    <#--古诗大会-->
                    <#include "taskcard/poetry.ftl">

                    <li id="crmCardBox" style="display: none;"></li>

                    <#--班级小组-->
                    <#--<#include "taskcard/tinygroup.ftl" >-->

                    <#--魔法城堡-->
                    <#include "activity/magic/magicCard.ftl"/>

                    <#if !(data.homeworkCards)?has_content && !(data.makeUpHomeworkCards?has_content)>
                        <#include "taskcard/nohomework.ftl" >
                    </#if>
                    <#include "taskcard/reward.ftl" >
                </#if>
            </ul>
        </div>
        <div id="showCardTabBox" class="tl-practice-tab"></div>
    </div>
</div>

<#--学生PC端首页学习任务卡广告位-->
<script type="text/html" id="T:首页任务卡广告位">
    <%var popupItems = result.data[index];%>
    <li class="practice-block">
        <div class="practice-content" style="background: none;">
            <a style="display: block;height:209px; margin: 3px 6px 5px 4px; border-radius: 10px; background-image:url(<%=result.imgDoMain%>/gridfs/<%=popupItems.img%>);" href="<%=result.goLink%>?aid=<%=popupItems.id%>&index=<%=index%>" target="_blank"></a>
            <%if(popupItems.btnContent!=""){%>
                <div class="pc-btn">
                    <a class="w-btn w-btn-green" href="<%=result.goLink%>?aid=<%=popupItems.id%>&index=<%=index%>" target="_blank"><%=popupItems.btnContent%></a>
                </div>
            <%}%>
        </div>
    </li>
</script>

<#--学生PC端-首页-底部广告位 -->
<div class="th-banner">
    <div id="a-homeBanner-box"></div>
    <script type="text/javascript">
        YQ.voxSpread({
            keyId : 310102,
            boxId : $("#a-homeBanner-box")
        });
    </script>
</div>

<#--课外游戏练习-->
<div class="th-game-practice">
    <ul id="iconListBox">
        <#--学习中心-->
        <a onclick="$17.atongji('首页-学习中心','/student/learning/index.vpage');" href="javascript:void (0);">
            <li class="gm-type-1">
                <span class="gm-icon"></span>
                <div class="gm-icon-not"></div>
                <div class="gm-icon-name"></div>
            </li>
        </a>

        <#--课外乐园-->
        <li class="gm-type-2">
            <#--<#if !(currentStudentDetail.clazz.classLevel)??>
            <a href="javascript:void (0);" class="v-joinClazzBtn-popup" data-link="/student/fairyland/index.vpage">
            <#else>
            <a onclick="$17.atongji('首页-课外乐园','/student/fairyland/index.vpage');" class="v-studentVoxLogRecord" data-op="click-after-class" href="javascript:void (0);">
            </#if>-->
            <a href="javascript:void (0);">
                <span class="gm-icon"></span>
                <div class="gm-icon-not"></div>
                <div class="gm-icon-name" style="display: none"></div>
            </a>
        </li>

        <#--班级空间-->
        <#if !(currentStudentDetail.clazz.classLevel)??>
            <li class="gm-type-4 gm-lock v-joinClazzBtn-popup" data-link="/student/clazz/index.vpage">
                <span class="gm-icon"></span>
                <div class="gm-icon-not"></div>
                <div class="gm-icon-name"></div>
            </li>
        <#else>
            <li class="gm-type-4">
                <a onclick="$17.atongji('首页-班级空间','/student/clazz/index.vpage');" href="javascript:void (0);">
                    <span class="gm-icon"></span>
                    <div class="gm-icon-not"></div>
                    <div class="gm-icon-name"></div>
                </a>
            </li>
        </#if>
    </ul>
</div>
</div>
</div>
<div class="homework_content_box"></div>
<div id="invited_student_box"><#-- 新版学生被邀请 --></div>

<#--判断是否为PC客户端-->
<@sugar.capsule js=["VoxExternalPlugin"] />
<script type="text/javascript">
    <#--学生PC端首页学习任务卡广告位-->
    YQ.voxSpread({
        keyId : 310101
    }, function(result){
        var crmCardBox = $("#crmCardBox");
        if(result.success && result.data.length > 0){
            var popupItems = result.data.reverse();
            if(popupItems.length > 0){
                for(var i = 0; i < popupItems.length; i++){
                    $( template("T:首页任务卡广告位", { result : result, index : i }) ).insertAfter( crmCardBox );
                }
            }
        }
        crmCardBox.remove();
    });

    $(function(){
        var homeworkDetail={};
        //因为广告位任务卡是后渲染进来的，而这里需要计算任务卡的数量来确定容器宽度，所以这里必须用setTimeout
        setTimeout(function(){
            homeworkDetail = new $17.Model({
                cardTotal : $("#homework_list_box li.practice-block").length,
                homeworkListBox : $("#homework_list_box"),
                cardBoxMaxWidth : '',
                cardWidth : 190 ,//单个卡片宽度
                showCardNum : 3 , //显示卡片数
                homeworkCardTabBox : $("#homework_card_tab_box"),
                totalPage : 1
            });
            homeworkDetail.extend({
                updateBigTabClass : function(currentPage){
                    if(homeworkDetail.totalPage == currentPage){
                        $("a.nextBtn").addClass('tlo-next-dis');
                        $("a.prevBtn").removeClass('tlo-back-dis');
                    }else if(currentPage > 1){
                        $("a.prevBtn").removeClass('tlo-back-dis');
                        $("a.nextBtn").removeClass('tlo-next-dis');
                    }else if(currentPage - 1 == 0){
                        $("a.prevBtn").addClass('tlo-back-dis');
                        $("a.nextBtn").removeClass('tlo-next-dis');
                    }else if(currentPage - 1 < homeworkDetail.totalPage){
                        $("a.nextBtn").removeClass('tlo-next-dis');
                    }

                    homeworkDetail.homeworkListBox.css({left : -homeworkDetail.cardWidth*homeworkDetail.showCardNum*(currentPage-1) +"px"});

                    $('#homework_list_box').attr('data-left', -homeworkDetail.cardWidth*homeworkDetail.showCardNum*(currentPage-1));

                    //更新小标签选中的状态
                    homeworkDetail.updateSmallTabClass(currentPage);

                    //还原卡片状态
                    $("#homework_list_box li").removeClass('pv_active').show();
                },
                updateSmallTabClass : function(pageNum){
                    $("#showCardTabBox span[data-s_tab_index="+pageNum+"]").addClass('current').siblings().removeClass('current');
                },
                init : function(){

                    /*根据卡片数量 生成相应数量的小标签*/
                    var html = '';
                    for(var i = 0; i < Math.ceil(homeworkDetail.cardTotal/3) ; i++){
                        var currentClass = (i == 0) ? 'current' : '';
                        html += '<span data-s_tab_index='+(i+1)+' class='+currentClass+'></span>';
                    }
                    $('#showCardTabBox').html(html);

                    //根据卡片数量 生成相应页面宽度
                    cardBoxMaxWidth = homeworkDetail.cardWidth * homeworkDetail.cardTotal;
                    homeworkDetail.homeworkListBox.css({width : cardBoxMaxWidth + "px"});

                    //获取总页数
                    homeworkDetail.totalPage = Math.ceil(homeworkDetail.cardTotal/3);

                    //当卡片总数小于等于3时，小标签隐藏
                    if(homeworkDetail.cardTotal <= 3){
                        $('#showCardTabBox').hide();
                        $("#homework_card_tab_box").hide();
                    }

                    /*小标签切换卡片*/
                    $("#showCardTabBox span").on('click', function(){
                        var $this = $(this);
                        $this.addClass('current').siblings().removeClass('current');
                        var currentSmallTabIndex = $this.data('s_tab_index');
                        homeworkDetail.updateBigTabClass(currentSmallTabIndex);

                        //更换大标签的值
                        $("a.prevBtn").attr("data-c_p_page",currentSmallTabIndex - 1);
                        $("a.nextBtn").attr("data-c_n_page",currentSmallTabIndex);
                    });


                    /*大标签切换卡片*/
                    $("a.prevBtn").on('click', function(){
                        var $this = $(this);
                        if($this.hasClass('tlo-back-dis')){return false}
                        var cPage = $this.attr('data-c_p_page')* 1 - 1;
                        $this.attr('data-c_p_page',cPage);
                        $("a.nextBtn").attr("data-c_n_page",$("a.nextBtn").attr("data-c_n_page") * 1 - 1);
                        homeworkDetail.updateBigTabClass(cPage + 1);
                    });

                    $("a.nextBtn").on('click', function(){
                        var $this = $(this);
                        if($this.hasClass('tlo-next-dis')){return false}
                        var cPage = $this.attr('data-c_n_page')* 1 + 1;
                        $this.attr('data-c_n_page',cPage);
                        $("a.prevBtn").attr("data-c_p_page",$("a.prevBtn").attr("data-c_p_page") * 1 + 1);
                        homeworkDetail.updateBigTabClass(cPage);
                    });
                }

            }).init();
        },200);



        //
        $("#iconListBox li").hover(function(){
            $(this).addClass('active');
        },function(){
            $(this).removeClass('active');
        });

        //
        $("#homeworkCardsPk").on("click", function(){
            $("#pkUnfinishedQtipBox").toggle();
        });

        //fairylandNoOpenBut
        $("#fairylandNoOpenBut").on("click", function(){
            $('#fairylandNoOpenBox').toggle();
        });

        //系统弹窗
        $(function(){
            if(${(stuforbidden!false)?string}){
                $.prompt("<div style='text-align: center; padding: 30px 0;'>账号异常，暂时无法使用</div>", {
                    title : "账号异常",
                    buttons : {'退出登录': true},
                    classes : {
                        close: 'w-hide'
                    },
                    submit: function(){
                        $17.voxLog({
                            module : "studentForbidden",
                            op : "popup-logout"
                        }, 'student');
                        location.href = "/ucenter/logout.vpage";
                    },
                    loaded : function(){
                        $17.voxLog({
                            module : "studentForbidden",
                            op : "popup-load"
                        }, 'student');
                    }
                });
                return false;
            }

            <#--判断是否为PC客户端-->
            if(VoxExternalPluginExists()){
                $.prompt("<div style='text-align: center'>一起作业电脑客户端目前已停止维护，为了给您更好的体验，<br/>请使用最新浏览器登录一起作业网<a href='http://www.17zuoye.com' target='_blank'>www.17zuoye.com</a>。</div>", {
                    focus: 1,
                    title: '系统提示',
                    buttons: {"取消": false, "打开浏览器": true},
                    submit: function(e, v){
                        if(v){
                            window.open("http://www.17zuoye.com", "_blank");
                        }
                    }
                });
                return false;
            }

            //自动弹窗 优先级（1学霸、2土豪、3最赞、4同学的邀请5.走遍美国 or 充值赠送学豆）
            var popupType = "${(data.popup)!}";
            switch (popupType){
                case "studyMaster" :
                    $17.alert('<div class="text_center">恭喜你！上次作业成绩优异，获得学霸称号。</br><a href="/student/clazz/index.vpage">点此查看</a></div>');
                    break;

                case "wealthiest" :
                    //$17.alert('恭喜你！上周赚了一堆学豆，获得土豪称号。<a href="/student/clazz/index.vpage">点此查看</a>');
                    break;

                case "mostFavorite" :
                    $17.alert('<div class="text_center">恭喜你！昨日人气爆棚，收获了超多赞！赢得最赞称号。</br><a href="/student/clazz/index.vpage">点此查看</a></div>');
                    break;

                case "activationReceived" :
                        <#if ftlmacro.isInJuneForInvite>
                            $("#invited_student_box").load("/student/invite/invited.vpage");
                        </#if>
                    break;
                default :
                    <#if (data.vmpopup && !(data.noBindWindow!false))!false>
                        //强绑手机
                        if($['bindNewMobile']){
                            $.bindNewMobile();
                            return false;
                        }
                    </#if>

                    <#if !((currentStudentDetail.clazz.classLevel)??) || (currentStudentDetail.clazz.isTerminalClazz())>
                    <#--无班级学生，或者毕业班学生直接弹窗-->
                        if (true) {
                            $(".v-joinClazzBtn-popup").trigger("click", {"state": "state"});
                            return false;
                        }
                    </#if>
                    //2016-06-23 增加老师修改密码后，提示强行修改密码
                    var passwordNeedReset = ${((!(data.taskMapper.passwordModified))!false)?string}
                    //true:彈出强绑手机 - 是否CRM注册 - 是否为虚假老师
                    var mobileForceBind = ${(( (data.bmpopup!false && !(data.noBindWindow!false)) )!false)?string};
                    if ((mobileForceBind && !$17.getCookieWithDefault("ForceBind")) || passwordNeedReset) {
                        if($['startCrmTask']){ $.startCrmTask() }

                        $17.voxLog({
                            module : "studentPopupForceBind",
                            bind : "load",
                            op : "<#if (data.force)!false>1<#else>2</#if>"
                        }, "student");
                        return false;
                    } else {
                        //一天弹一次，弹一共弹5次记一个月Cookie，没有家长通知&&没有完成新手任务  false && true
                        if(!$17.getCookieWithDefault("NOVICE")){
                            $17.setCookieOneDay("NOVICE", "1", 1);
                            if($['startNewNovice']){ $.startNewNovice() }
                            return false;
                        }
                    }

                    <#if ((currentStudentWebGrayFunction.isAvailable("17Parent", "spread"))!false) && !((data.noBindWindow)!false)>
                        //强制绑定手机号 && parent App - 郑州，深圳，潍坊共70所学校
                        if(true){
                            $.noviceStrongTieApp();
                            return false;
                        }
                    </#if>

                    <#assign studentRegisterDayCount = (((.now?long - (currentStudentDetail.getCreateTime())?long) / (1000*60*60*24))?int)!0 userRegisterDay = 60/>
                    <#if .now lt "2016-05-10 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss")><#assign userRegisterDay = 2000/></#if>
                    <#if (studentRegisterDayCount gte 2 && studentRegisterDayCount lte userRegisterDay)!false>
                        //课外乐园引导
                        if( !$17.getCookieWithDefault("fyldEnter") ){
                            $17.voxLog({
                                module : "FairylandStepBox",
                                op : "index-load"
                            }, "student");
                            $.prompt(template("T:课外乐园引导", {}), {
                                prefix : "step-fairyland-popup",
                                buttons : { },
                                classes : {
                                    fade: 'jqifade',
                                    close: 'w-hide'
                                },
                                loaded : function(){
                                    $(".step-fairyland-popup .fy-back a").on("click", function(){
                                        $17.setCookieOneDay("fyldEnter", "70", 70);
                                        $17.voxLog({
                                            module : "FairylandStepBox",
                                            op : "index-click"
                                        }, "student");
                                    });
                                }
                            });
                            return false;
                        }
                    </#if>

                    <@ftlmacro.gameAreaVersion typeContent="popup"/>

                    if(!$17.getCookieWithDefault("ifcanter")){
                        $.ifCancelOrStopTeacher();
                        return false;
                    }
            }

            <#--首页广告位弹窗-->
            YQ.voxSpread({
                keyId : 310103
            }, function(result){
                if(result.success && result.data.length > 0){
                    var popupItems = result.data;
                    for(var i = 0; i < popupItems.length; i++){
                        if(!$17.getCookieWithDefault("sadver" + popupItems[i].id)){
                            $17.setCookieOneDay("sadver" + popupItems[i].id, "1");
                            if(popupItems[i].img){
                                $.prompt(template("T:PUBLIC-POPUP-BOX", { result : result, index : i }), {
                                    prefix : "null-popup",
                                    position : { width: 680},
                                    buttons : {},
                                    classes : {
                                        fade: 'jqifade',
                                        close: 'w-hide'
                                    }
                                });
                            }else{
                                $.prompt(popupItems[i].content+"<div style='padding-top:50px;text-align: center;'><a href='"+popupItems[i].url+"' class='w-btn w-btn-green w-btn-well'>"+popupItems[i].btnContent+"</a></div>",{
                                    title:popupItems[i].description,
                                    buttons:{}
                                });
                            }
                        }
                    }
                    return false;
                }
            });
        });
        var pf_page_enable_time_end = +new Date(); //页面可用时间结束
        window.onload = function(){
            var pf_page_load_time_end = +new Date(); //页面加载完成时间
            try{
                $17.voxLog({
                    module: "studentIndex",
                    op: "loadTime",
                    ws: pf_white_screen_time_end - pf_time_start,
                    pe: pf_page_enable_time_end - pf_time_start,
                    pl: pf_page_load_time_end - pf_time_start
                }, 'student');
            }
            catch(e){
                $17.voxLog({
                    module: "studentIndex",
                    op: "loadTime",
                    error: e.message
                });
            }

        }
    });
</script>
<#--虚拟班级-邀请弹出框-->
<#include "activity/teacherReport.ftl"/>

<@ftlmacro.gameAreaVersion typeContent="popupHtml"/>

<#if ((currentStudentWebGrayFunction.isAvailable("17Parent", "spread"))!false) && !((data.noBindWindow)!false)>
    <#include "taskcard/novice_strongtie.ftl" >
</#if>

<script type="text/html" id="T:课外乐园引导">
    <style>
    .step-fairyland-popup{ top : 0 !important;}
    .step-fairyland-popup .fy-main{ width: 100%;}
    .step-fairyland-popup .fy-inner{ width: 1000px; margin: 0 auto;}
    .step-fairyland-popup .fy-back{background: url(<@app.link href="public/skin/studentv3/images/publicbanner/fairyland-novice-guide.png"/>) no-repeat 0 0; width:724px; height:337px; margin-left: 178px;}
    .step-fairyland-popup .fy-back a{ width: 100%; height: 100%; display: block;}
    </style>
    <div class="fy-main">
        <div class="fy-inner">
            <div class="fy-back"><a href="/student/fairyland/index.vpage"></a></div>
        </div>
    </div>
</script>

<#--首页广告位弹窗-->
<script type="text/html" id="T:PUBLIC-POPUP-BOX">
    <%var popupItems = result.data[index];%>
    <div class="crm-popup-box" style="text-align: center;">
        <div style="text-align: center; display: inline-block; position: relative;">
            <div class="cp-close" style="position: absolute; right: -10px; top: -10px; width: 38px ; height: 38px; ">
                <a  href="javascript:$.prompt.close();" style="width: 100%; height: 100%; display: block; color: #fff; font-size: 32px;line-height: 100%; text-align: center; background-color: #000; border-radius: 100%;" title="关闭">×</a>
            </div>
            <a href="<%=result.goLink%>?aid=<%=popupItems.id%>&index=<%=index%>" onclick="$.prompt.close();" target="_blank">
                <img src="<%=result.imgDoMain%>/gridfs/<%=popupItems.img%>" alt="<%=popupItems.name%>">
            </a>
        </div>
    </div>
</script>
<#if (data.vmpopup)!true>
    <#include "taskcard/bind_newmobile.ftl" >
</#if>
</@temp.page>