<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="老师" pageJs="common" footerIndex=2>
<@sugar.capsule css=['res']/>
<#if schoolId?? && schoolId?has_content>
<style>
    .teacher .year-content .teacher_title{margin: .1rem 0;}
    .teacher .year-content ul{width: 100%;}
    .teacher .year-content .teacher_con{position: relative;padding: 0.3rem 0; border-bottom: 1px solid #bbb;}
    .teacher .year-content .teacher_title ul li,
    .teacher .year-content .teacher_con ul li{float: left;width: 33.3%;font-size: .6rem;color: #636880;}
    .teacher .year-content .teacher_con ul li span{font-size: .8rem;}
    .teacher .year-content .teacher_con:last-child{border: 0;}
    .teacher .year-content .teacher_infor{text-overflow: ellipsis;overflow: hidden;white-space: nowrap;}
    .teacher .year-content .teacher_infor span{font-size: .5rem;color: #9199bb;margin-right: .5rem;}

    .card{position: absolute;right: 0;top: 0;width: 0;height: 0;font-size: .6rem;color: #fff;border-bottom: .8rem solid transparent;border-left: .8rem solid transparent;}
    .teacher_con:nth-child(2) .card{top: -1rem;}
    .card-ENGLISH{border-top: .8rem solid #D589D9;border-right: .8rem solid #D589D9;}
    .card-MATH{border-top: .8rem solid #FF866C;border-right: .8rem solid #FF866C;}
    .card-CHINESE{border-top: .8rem solid #74C5FD;border-right: .8rem solid #74C5FD;}
    .card i{position: absolute;top: -.7rem;right: -.6rem;}
</style>
<div class="crmList-box resources-box">
    <div class="feedbackList-pop show_now" style="display: none; z-index:100;position:fixed;top:0;background-color:rgba(125,125,125,.5);width:100%;height:100%">
        <ul style="background:#fff;margin-top:-0.1rem">
            <li class="tab_row" style="padding:.2rem 0"><a id="applwao  yService" style="display:inline-block;height:2.5rem;line-height:2.5rem;width:100%;color:#000;padding-left:1rem;font-size: .75rem;border-bottom: .05rem solid #cdd3d3" <#if modeType?? && modeType =="offline"> href="teacher_list.vpage?schoolId=${schoolId!}&modeType=online"</#if> class="active">online模式</a></li>
            <li class="tab_row" style="padding:.2rem 0"><a id="retroAction" style="height:2.5rem;line-height:2.5rem;display:inline-block;width:100%;color:#000;padding-left:1rem;font-size: .75rem;"  <#if modeType?? && modeType =="online"> href="teacher_list.vpage?schoolId=${schoolId!}&modeType=offline" </#if> class="active">offline模式</a></li>
        </ul>
    </div>
    <div class="fixed-head" style="position: absolute">
            <div class="c-head">
                <a href="card.vpage?schoolId=${schoolId!0}&modeType=${modeType!}">学校</a>
                <a href="/view/mobile/crm/school/school_grade.vpage?schoolId=${schoolId!0}&modeType=${modeType!}">年级</a>
                <a class="the" href="javascript:;">老师</a>
                <#--<div class="return"><a href="javascript:window.history.back()"><i class="return-icon"></i>返回</a></div>-->
            </div>
        <div class="c-search">
            <input class="t-input" placeholder="请输入姓名 / ID / 手机号码" <#if key??>value="${key!}"</#if>/>
            <span class="js-search teacher-search">搜索</span>
        </div>
        <!--筛选选项-->
        <div class="c-opts gap-line tab-head c-flex c-flex-3">
            <span class="the" data-index="1">英语</span>
            <span data-index="2">数学</span>
            <#if level??>
                <#if level == 1 || level == 5>
                    <span data-index="3">语文</span>
                <#elseif level == 2 || level ==4>
                    <span data-index="4">其他</span>
                </#if>
            </#if>
        </div>
    </div>
    <div class="c-main">
        <!--老师列表-->
        <p class="error-tip" style="text-align:center;color:red;line-height:2em;font-size:0.75rem;padding:0 2rem;word-break: break-all;">${error!''}</p>
        <div id="teacher-list" class="c-list">
        </div>
    </div>
</div>
<#if (modeType?? && modeType == "online") || (level!0) == 1>
    <div>
        <div class="js-sortList" data-info="lmFinCsHwGte3AuStuCount" style="display:none;width:100%;position:fixed;bottom:0;background:#ff7d5a;color:#fff;font-size:.75rem;text-align: center;padding:.45rem 0">按上月月活排序</div>
        <div class="js-sortList" data-info="tmGroupMaxHwSc" style="width:100%;position:fixed;bottom:0;background:#ff7d5a;color:#fff;font-size:.75rem;text-align: center;padding:.45rem 0">按本月布置排序</div>
    </div>
</#if>
<#include "school/school_teacher_info.ftl">
<script>

        <#if modeType??>
        <#if modeType == "online">
            var modeType = "online模式";
        <#else>
            var modeType = "offline模式";
        </#if>
    </#if>
    var data;
    $(document).ready(function () {
        //根据学校区分是否显示右上角切换按钮 1 小学 不显示  24 初高中 显示
        <#if modeType?? && (level!0) != 1>
        //切换在校老师的online、offline模式
            var setTopBar = {
                show:true,
                rightText:modeType,
                rightTextColor:"ff7d5a",
                needCallBack:true
            } ;
        <#else>
            var setTopBar = {
                show:true,
                rightText:"",
                rightTextColor:"ff7d5a",
                needCallBack:true
            } ;
        </#if>
            var topBarCallBack = function () {
                $('.show_now').toggle();
            };
            setTopBarFn(setTopBar,topBarCallBack);
        YQ.voxLogs({
            database : "marketing", //不设置database  , 默认库web_student_logs
            module : "m_oI6bREkw", //打点流程模块名
            op : "o_kL2pUYhM" ,//打点事件名
            userId:${requestContext.getCurrentUser().getUserId()!0},
            s0 : ${schoolId!0}
        });
    });
    function search_teacher_list(info) {
        var teacherKey = $(".teacher-search").prev().val();
        $.post("find_teacher_list.vpage", {schoolId:${schoolId!0}, teacherKey: teacherKey, all: true}, function (res) {
            if (res.success){
                data = res;
                if(data.teacherList[0] && data.teacherList[0].subjects[0]) {
                    data.teacherList.sort(function (a, b) {
                        return (b.subjects[0].kpiData[info] || 0) - (a.subjects[0].kpiData[info] || 0);
                    });
                }
                var res={res:data};
                $("#teacher-list").html(template("T:老师列表",res));
            }
        });

    }
    $(document).on("click",".js-sortList",function () {
        $(this).hide().siblings().show();
        if($(this).data("info") == "tmGroupMaxHwSc"){
            if(data.teacherList[0].subjects[0]) {
                data.teacherList.sort(function (a, b) {
                    return (b.subjects[0].kpiData.lmFinCsHwGte3AuStuCount || 0) - (a.subjects[0].kpiData.lmFinCsHwGte3AuStuCount || 0);
                });
            }
        }else if($(this).data("info") == "lmFinCsHwGte3AuStuCount"){
            if(data.teacherList[0].subjects[0]) {
                data.teacherList.sort(function (a, b) {
                    return (b.subjects[0].kpiData.tmGroupMaxHwSc || 0) - (a.subjects[0].kpiData.tmGroupMaxHwSc || 0);
                });
            }
        }
        $("#teacher-list").html(template("T:老师列表", {res:data}));
    });
    function teacher_list(info){
        var subject = $(".tab-head .the").data('index');
        $.post("find_teacher_list.vpage",{schoolId:${schoolId!0},hide:hide,subject:subject},function(res){
            if (res.success){
                data = res;
                if(data.teacherList[0] && data.teacherList[0].subjects[0]) {
                    data.teacherList.sort(function (a, b) {
                        return (b.subjects[0].kpiData[info] || 0) - (a.subjects[0].kpiData[info] || 0);
                    });
                }
                var res={res:data};
                $("#teacher-list").html(template("T:老师列表",res));
            }
        });

    }
    var AT = new agentTool();

    //老师卡片可点击
    $(document).on("click",".teacher",function(){
        var tid = $(this).data().sid;
        var subjType = $(".tab-head .the").data('index');
        AT.setCookie("localTeacherSid",tid);
        AT.setCookie("localTeacherSubjType",subjType);
        var url = "/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId="+tid;
        openSecond(url);
    });
    $(".teacher-search").on("click",function(){
        var $this=$(this),keyWord=$this.prev().val();
        var info = $('.js-sortList:visible').data('info');//获取当前是按照哪种方式（月活、本月布置）排序
        AT.setCookie("stkw",keyWord);//搜索老师关键字
        search_teacher_list(info);
    });

    /*--tab切换--*/
    $(".tab-head").children("a,span").on("click",function(){
        var $this=$(this);
        var info = $('.js-sortList:visible').data('info');//获取当前是按照哪种方式（月活、本月布置）排序
        $this.addClass("the").siblings().removeClass("the");
        $(".tab-main").eq(0).children().eq($this.index()).show().siblings().hide();
        teacher_list(info);
    });

    var currentSubj = AT.getCookie("localTeacherSubjType") || 0;

    var currentTid = AT.getCookie("localTeacherSid");
    if(currentTid){
        var detailSchool = $("#detail"+currentTid);
        if(detailSchool.length!=0){
            var scroll_offset = $("#detail"+currentSid).offset();

            $("body,html").animate({
                scrollTop:parseFloat(scroll_offset.top) - 184 // 减掉被顶部和筛选条遮挡的部分
            },0);

        }
    }else{
        var info = $('.js-sortList:visible').data('info');//获取当前是按照哪种方式（月活、本月布置）排序
        teacher_list(info);
    }
    $($(".tab-head").children("a,span")[currentSubj-1]).click();

    //获取当前学科
    var getSubj = function(){
        return $(".c-opts.tab-head>span.the").data().index || 0;
    };

    //计算hide之后的数值
    var caculateNum = function(x) {
        var index = parseInt(getSubj());
        var hideNum = parseInt($($('span.hideNum')[index]).text().trim());

        hideNum  = hideNum + x;
        $($('span.hideNum')[index]).html(hideNum);
    };
    var submitAble = true;
    var displayAction = function(data){
        event.preventDefault();
        event.stopPropagation();
        var node = $(data.pNode).parents(".teacher");
        var tid = node.data().sid;
        if (submitAble) {
            submitAble = false;
            $.post(data.url, {teacherId: tid}, function (res) {
                if (res.success) {
                    submitAble = true;
                    node.remove();
                    caculateNum(data.type);
                    teacher_list();
                } else {
                    submitAble = true;
                    AT.alert(res.info);
                }
            });
        }
    };

    //取消隐藏
    $(document).on("click",".js-showBtn",function(){
        displayAction({
            url:"show_teacher.vpage",
            pNode:this,
            type:-1
        });
        return false;
    });

    var getHideTab = function(node){
        var type = $(node).data().type;
        var subj = getSubj();
        location.href = location.pathname + "?schoolId="+AT.getQuery("schoolId")+"&hide="+type+"&subject="+subj;
    };
    var hide = false;
    //返回正常列表
    $(document).on("click",".js-exportHideTab",function(){
        hide = false;
        $(this).addClass('tab_hide');
        $('.js-hideTab').removeClass('tab_hide');
        teacher_list();
    });


    //查看隐藏列表
    $(document).on("click",".js-hideTab",function(){
        $(this).addClass('tab_hide');
        YQ.voxLogs({
            database : "marketing", //不设置database  , 默认库web_student_logs
            module : "m_dmyGAMr5", //打点流程模块名
            op : "o_hEhvMZxV" ,//打点事件名
            userId:${requestContext.getCurrentUser().getUserId()!0}, //登录用户Id
            s0:${schoolId!0}
        });
        $('.js-exportHideTab').removeClass('tab_hide');
        hide = true;
        teacher_list();
    });
    $(document).on("click",".show_hide_teacher",function(){
        var _index = $(this).data('index');
        $(this).hide().siblings().show();
        if(_index == 2){
            hide = false ;
        }else{
            hide = true ;
        }
        teacher_list()
    });

</script>
<#else>
<p class="orange-color" style="text-align: center;padding:20px;">温馨提示：请在选择学校后重试！</p>
</#if>
<script>

</script>
</@layout.page>