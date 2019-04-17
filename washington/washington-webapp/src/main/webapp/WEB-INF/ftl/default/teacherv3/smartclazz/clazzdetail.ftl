<#import "module.ftl" as temp />
<@temp.pagecontent mainmenu="classroom_reward" submenu="iwen">
<@sugar.capsule js=["underscore"] />
<style type="text/css">
    .smart_clazz_content .smart_clazz_beans_box:hover{
        border : 1px solid #189cfb;
        z-index: 2;
    }
</style>
<!--当前操作-->
<div >
    <!--//start-->
    <!--选择多个-->
    <div id="smartClazzMultiChoiceDiv" style="display: none;">
        <div class="s-dateTip-box">
            <p class="s-fl-left s-magT-10">
                <span class="s-blue"><i class="w-checkbox"></i> 选中全部</span>
            </p>
            <div class="sd-t s-fl-right">
                <a style="width: 144px; padding: 7px 0;" href="javascript:void (0)" class=" s-btn-big" id="give_reward_but">给予奖励</a>
                <a href="javascript:void (0)" id="rewardStudentCancelBtn"> &lt; 返回</a>
            </div>
            <div class="s-clear"></div>
        </div>
    </div>

    <div class="smart_clazz_content">
        <div class="smart_clazz_beans_main" id="students_list_box" data-clazz_id="${clazz.id!''}">
        <#--smart_clazz_beans_active-->
            <#if studentList?has_content>
                <#list studentList as sl>
                    <div class="smart_clazz_beans_box smartStudentName <#if sl.integral gt 0>smart_clazz_beans_onCount</#if>" data-student_id="${sl.studentId!''}" data-student='${json_encode(sl)}' data-groupid="${(sl.tinyGroupId)!0}">
                        <strong class="icon icon_group">${((sl.tinyGroupName)?has_content)?string("${sl.tinyGroupName}", "未分组")}</strong>
                        <span>
                            <img width="60" height="60" src="<@app.avatar href= '${sl.studentImg}'/>">
                        </span>
                        <strong title="${sl.studentName!''}">${sl.studentName!''}</strong>
                        <i class="icon icon_cirl integralNum" <#if sl.integral == 0> style="display: none" </#if>  >${sl.integral!'0'}</i>
                        <div class="active_back"><div class="back"></div><div class="data_g PNG_24"></div></div>
                    </div>
                </#list>
            <#else>
                <div class="smart_clazz_data_box">
                    <div class="iconNullData iconNullUser"></div>
                    <h4><span>${clazz.formalizeClazzName()}</span>暂无学生姓名</h4>
                    <p>请<a href="/teacher/clazz/clazzsdetail.vpage?clazzId=${clazz.id!}">添加姓名</a>后再使用该功能</p>
                </div>
            </#if>
            <div class="hint_box" style="display: none;"><#--弹窗--></div>
            <div style="clear: both"></div>
        </div>
    </div>
    <!--end//-->
</div>
<#--播放MP3文件-->
<div id="audiomp3flash">
    <span></span>
</div>
<#include "clazzdetailtemplate.ftl" />

<script type="text/javascript">
//班级Id (很多地方需要)
var clazzId = "${clazz.id!''}";

//存放选择过的学生(若学生都被选择过，则清空该数组)
var selectedStudentIdList = [];
//兑换学豆
function changeSilver($that){
    var totalSilver = 0;
    $(".tempNum").each(function(){
        totalSilver += $(this).val() * 1;
    });

    if(totalSilver == 0 || totalSilver == 500){
        $that.addClass("btn_disable");
        $("#tempNumBox").focus();
    }

    if(totalSilver == 0){
        $that.css({"cursor" : "default"});
        $('.silverInfo').hide().siblings(".errorInfo").show().html('请添加学豆数量');
        return false;
    }else{
        $("#tempNumBox").removeClass("btn_disable");
        $that.siblings().closest('a.minusBtn ').css({"cursor" : "pointer"});
        $('.silverInfo').show().siblings(".errorInfo").hide();
    }

    var mBox = $('.historyAllTableBox');
    mBox.find('.goldCountBox').text(Math.ceil(totalSilver/5));
    if(totalSilver%5 == 0){
        mBox.find('.silverCountBox').text(totalSilver);
    }else{
        totalSilver = 5 - totalSilver%5 + totalSilver ;
        mBox.find('.silverCountBox').text(totalSilver);
        $("#tempNumBox").val(totalSilver);
    }
    return false;
}

function checkNum(){
    var maxReward = 500; //兑换学豆最多值
    var changeNum = 5; //加减学豆数差值

    if(arguments[0] > maxReward || !$17.isNumber(arguments[0])){
        return 100;
    }

    if(arguments[1] == "minus"){
        if(arguments[0] - changeNum < 0 || arguments[0] - changeNum > maxReward){
            return arguments[0];
        }else{
            return arguments[0] - changeNum;
        }
    }else{
        if(arguments[0] + changeNum < 0 || arguments[0] + changeNum > maxReward){
            return arguments[0];
        }else{
            return arguments[0] + changeNum;
        }
    }
}

<#--弹窗列表-->
var popupList = {
    clazzPoolZero : {
        name    : 'clazzPoolZero',
        comment : '班级学币池学豆为零的弹窗',
        html    : '<div class="jqicontent">您的学豆数量为0，请兑换后给学生发放奖励。 <br /></div>',
        title   : '消息提醒',
        buttons : {"暂不兑换" : false , "马上兑换" : true },
        focus   : 1,
        submit  : function(e,v,m,f){
            if(v){
                e.preventDefault();
                $.prompt.goToState('exchangeIntegral');
                return false;
            }
            $.prompt.close();
            return false;
        }
    },
    clazzPoolLessThanZero : {
        name    : 'clazzPoolLessThanZero',
        comment : '奖励学生时，学豆数量不足情况弹窗',
        html    :'<div class="jqicontent" style="line-height: 30px;"> 您的学豆数量仅能给<span id="studentCntPop">X</span>名同学发放奖励，如想奖励更多同学，请兑换学豆。 <br /></div>',
        title   : '消息提醒',
        buttons : {"暂不兑换，重新选择" : false , "马上兑换" : true },
        focus   : 1,
        submit  : function(e,v,m,f){
            e.preventDefault();
            if(v){
                //马上兑换
                $.prompt.goToState('exchangeIntegral');
                return false;
            }else{
                //暂不兑换，重新选择
                $.prompt.close();
                return false;
            }
        }
    },
    exchangeIntegral : {
        name    : 'exchangeIntegral',
        comment : '兑换学豆弹窗',
        html    : template("t:exchange", {}),
        title   : '兑换学豆',
        buttons : {"取消" : false, "确定" : true},
        position: { width : 590},
        focus   : 1,
        submit : function(e,v){
            e.preventDefault();
            if(v){
                function exchangeIntegral(force) {
                    $.post('/teacher/smartclazz/exchangeintegral.vpage', {clazzId : clazzId, integralCnt: silverCount, force: force, subject: "${curSubject!}" }, function(data){
                        if(data.success){
                            $17.tongji('老师-课堂-兑换学豆-成功');
                            $17.alert('<div class="jqicontent">您已成功兑换'+silverCount+'学豆，快去给学生发放奖励吧！</div>');
                            //更新本周剩余学豆数量
                            var $totalIntergral = $("#totalIntegral");
                            $totalIntergral.text($totalIntergral.text() * 1 + silverCount );
                        }else{
                            if (data.info == "needCodeVerification") {// 老师兑换当天额度超限，需要发送验证码，验证通过后才可以继续兑换
                                if (data.mobile != null) {// 仅在绑定手机号的情况下验证验证码
                                    $("#v-exchangeMobile").empty().html(data.mobile).show();
                                    $("#v-exchangeVerifyCode").show();
                                }
                            } else {
                                $17.alert(data.info);
                            }
                        }
                    });
                }

                var silverCount = $('#tempNumBox').val() * 1;
                if(silverCount == 0 || silverCount > 500){
                    return false;
                }

                if ($("#v-exchangeVerifyCode").is(":visible")) {// 如果验证码部分是显示的，需要用户输入验证码
                    var $smsCode = $(".v-smsCode");

                    if(!$17.isNumber($smsCode.val()) ){
                        $smsCode.addClass("w-int-error");
                        return false;
                    }

                    // 校验验证码
                    $.post("/teacher/smartclazz/verifyTEICode.vpage", { code : $smsCode.val()}, function(data){
                        if(data.success){
                            exchangeIntegral(true);
                        }else{
                            $smsCode.siblings(".errorMsg").show().find(".info").text(data.info);
                        }
                    });
                } else {
                    exchangeIntegral(false);
                }
            }else{
                $.prompt.close();
            }
            return false;
        }
    },
    loadProcess : {
        name : 'loadProcess',
        comment : '业务处理图标',
        html : template("t:业务处理图标", {}),
        title : "奖励",
        buttons : {}
    },
    randomSelect : {
        name : 'randomSelect',
        comment : '随机选择学生动画',
        html : template("t:多人随机弹出框",{}),
        title : "奖励",
        buttons : {},
        position : { width:680}
    },
    rewardItemV3 : {
        name     : 'newRewardItem',
        comment  : '奖励学生时，奖项列表弹窗',
        html     : template("t:奖励项和学豆",{}),
        title    : '奖励学生',
        buttons  : {},
        position : {width:830},
        focus    : 1,
        submit : function(e,v){
            e.preventDefault();
            $.prompt.close();
        }
    }
};

<#--弹窗-->
function alertImpromptu(stateobj,options){
    var temp = {};
    temp[stateobj.name] = stateobj;
    if(options == undefined)
        options = {};
    return $.prompt(temp,options);
}

function processInputByFocus($o) {
    var myId = $o.attr('id');
    var $myLabel = $("label[for=" + myId + "]");
    $17.isBlank($o.val()) ? $myLabel.show() : $myLabel.hide();
}

<#--奖励项的单击事件-->
function rewardItemClickEvent(studentObjArr,rewardFrom){
    if(!$.inArray(studentObjArr)){
        $17.alert("选择学生有误，刷新页面重试");
        return false;
    }

    // 【取消】按钮
    $("#pop_reward_cancel_btn").die().on("click", function(){
        $.prompt.close();
    });

    //【确定】给奖励按钮
    $("#pop_reward_ok_btn").die().on("click",function(){
        var $this = $(this);
        var $li = $("#rewardItemList").find("li.active");
        var rewardType = $li.data('reward_type');
        $17.tongji("课堂-发放奖励-" + rewardType);
        var studentIds = [];
        $.each(studentObjArr, function(index){
            studentIds.push(studentObjArr[index].studentId);
        });

        var itemContent = $li.data('content');
        if(rewardType == "CUSTOM_TAG"){
            itemContent = $("#customTagContent").val();
        }

        var integralCnt = $("#rewardIntegralCnt").val();
        var data = {
            userIds     : studentIds.toString(),
            clazzId     : clazzId,
            integralCnt : integralCnt,
            rewardItem  : rewardType,
            customContent:itemContent,
            subject     : "${curSubject!}"
        };

        if($this.hasClass('loading')){return false;}
        $this.addClass('loading');

        //$.prompt.goToState(popupList.loadProcess.name,true);
        App.postJSON('/teacher/smartclazz/updaterewardintegral.vpage', data, function(data){
            if(data.success){
                var audioUrl = $li.data("audio_url");
                if(!$17.isBlank(audioUrl)){
                    $("#audiomp3flash").find("span").html("").jmp3({
                        autoStart: 'true',
                        file: audioUrl,
                        width: "1",
                        height: "1"
                    });
                }

                $.prompt(template("t:奖励成功结果",{content : itemContent, integralCnt : integralCnt, students : studentObjArr}),{
                    prefix : "smartclazz-popup",
                    title : '系统提示',
                    buttons : { },
                    classes : {
                        fade: 'jqifade',
                        close: 'w-hide',
                        title: 'w-hide'
                    },
                    loaded : function(){
                        setTimeout(function(){
                            $.prompt.close();
                        },2000);
                    }
                });

                //数据更新
                for (var id in studentIds ) {
                    var $that = $("div[class*='smartStudentName'][data-student_id='" + studentIds[id] + "']");
                    $that.find('.integralNum').text($that.find('.integralNum').text() * 1 + integralCnt * 1).show();
                    $that.addClass("smart_clazz_beans_onCount");
                }
                var totalIntegralElem = $("#totalIntegral");
                totalIntegralElem.text((totalIntegralElem.text() * 1) - (studentIds.length * integralCnt));

                //从随机选择来选择学生
                if(!$17.isBlank(rewardFrom) && rewardFrom == "random"){
                    selectedStudentIdList = _.uniq(selectedStudentIdList.concat(studentIds));
                }

                if(studentObjArr.length > 1){
                    //页面返回
                    setTimeout(function(){$("#rewardStudentCancelBtn").trigger('click');},2500);
                }
            }else{
                $17.alert(data.info);
            }
            $this.removeClass('loading');
        });
        return false;
    });


    //若选择的学生头像多于5个，则把【下一个】图标的不可用的类值去掉
    if(studentObjArr.length > 5){
        $("div.icon_arrow_before").show();
        $("div.icon_arrow_back").show().removeClass("icon_disable");
    }

    // 【上一个】图标
    $("div.icon_arrow_before").die().on("click", function(){
       if($(this).hasClass("icon_disable")) {
           return false;
       }
       var $pn = $("#selectStudentList").children("span");
       var $pnvisible = $pn.filter(":visible");
       // 返回这个元素在同辈中的索引位置
       var index = $pnvisible.first().index();
       if((index - 5) >= 0){
           $pnvisible.hide();
           index = index - 5;
           // 选取一个匹配的子集
           $pn.slice(index, index + 5).show();
       }

       if(index <= 0){
           $(this).addClass("icon_disable");
       }
       $("div.icon_arrow_back").removeClass("icon_disable");
       return false;
    });

    // 【下一个】图标
    $("div.icon_arrow_back").die().on("click", function(){
        if($(this).hasClass("icon_disable")) {
            return false;
        }
        var $pn = $("#selectStudentList").children("span");
        var $pnvisible = $pn.filter(":visible");
        // 返回这个元素在同辈中的索引位置
        var index = $pnvisible.last().index();
        //后台多于5个学生时，显示下5个学生
        if(($pn.length - 1 - index) > 5){
            $pnvisible.hide();

            index = index + 1;
            // 选取一个匹配的子集
            $pn.slice(index, index + 5).show();
        }else if(($pn.length - 1 - index) > 0){
            // 不够5个，显示后面所有
            $pnvisible.hide();
            index = index + 1;
            $pn.slice(index, $pn.length).show();
            //后面没有 当前按钮置灰
            $(this).addClass("icon_disable");
        }else{
            //后面没有 当前按钮置灰
            $(this).addClass("icon_disable");
        }

        if(index > 4){
            $("div.icon_arrow_before").removeClass("icon_disable");
        }
        return true;
    });

    //奖励项列表
    $("#rewardItemList").find("li").die().on("click", function(){
        var $this = $(this);
        $this.siblings().removeClass("active");
        $this.addClass("active");
        if($(this).attr("data-reward_type") == "CUSTOM_TAG"){
            $("#customTag").show();
        }else{
            $("#customTag").hide();
        }
    });

    $("#customTagContent").focus(function(){
        processInputByFocus($(this));
    }).blur(function(){
        processInputByFocus($(this));
    }).keyup(function(){
        processInputByFocus($(this));
    });


    //奖励的学豆减少【-】
    $("#diffIntegral").die().on("click", function(){
        var rewardIntegralCntEle = $("#rewardIntegralCnt");
        var rewardIntegralCnt = rewardIntegralCntEle.val() * 1;
        rewardIntegralCnt = rewardIntegralCnt - 1;
        if(rewardIntegralCnt <= 0){
            return false;
        }
        rewardIntegralCntEle.val(rewardIntegralCnt);
        return true;
    });
    // 奖励的学豆增加【+】
    $("#addIntegral").die().on("click", function(){
        var rewardIntegralCntEle = $("#rewardIntegralCnt");
        var rewardIntegralCnt = rewardIntegralCntEle.val() * 1;
        rewardIntegralCnt = rewardIntegralCnt + 1;
        //奖励的人数
        var $pn = $("#selectStudentList").children("span");

        var totalIntegral = $("#totalIntegral").text() * 1;
        if(rewardIntegralCnt > Math.floor(totalIntegral/$pn.length)){
            return false;
        }
        rewardIntegralCntEle.val(rewardIntegralCnt);
        return true;
    });
    // 奖励的学豆输入框
    /*输入框只能输入数字*/
    $('input[id="rewardIntegralCnt"]').keyup(function(){
        if (/\D/g.test(this.value)){
            this.value = this.value.replace(/\D/g, '1');
        }else{
            var totalIntegral = $("#totalIntegral").text() * 1;
            var rewardIntegral = this.value * 1;
            //奖励的人数
            var $pn = $("#selectStudentList").children("span");
            var tempVal = Math.floor(totalIntegral/$pn.length);
            if(rewardIntegral >= tempVal){
                this.value = tempVal;
            }
        }
    });

    return false;
}

$(function(){
    $17.voxLog({
        module: "smartclazz-clazzdetail",
        op    : "smartclazz-clazzdetail-load"
    });

    $17.tongji("互动课堂-课堂奖励");
    //是否选择多个状态
    var multiChoiceStatus = false;
    //选择多个按钮
    $("#multiChoiceBtn").on("click",function(){
        $17.tongji('老师-课堂-选择多个');
        $("#multiChoiceBtn").parent().toggleClass("active");

        if(multiChoiceStatus){
            $("#smartClazzMultiChoiceDiv").hide();
            multiChoiceStatus = false;
        }else{
            $("#smartClazzMultiChoiceDiv").show();
            multiChoiceStatus = true;
        }
    });

    //选择全部学生
    $("#smartClazzMultiChoiceDiv i.w-checkbox").on("click",function(){
        var $this = $(this);
        if($this.hasClass("w-checkbox-current")){
            $this.removeClass("w-checkbox-current");
            $(".smartStudentName").each(function(){
                $(this).removeClass("smart_clazz_beans_active");
            });
        }else{
            $this.addClass("w-checkbox-current");
            $(".smartStudentName").each(function(){
                $(this).addClass("smart_clazz_beans_active");
            });
        }
    });

    //给予奖励取消按钮
    $("#rewardStudentCancelBtn").on("click",function(){
        multiChoiceStatus = false;
        $("#smartClazzMultiChoiceDiv i.w-checkbox").removeClass("w-checkbox-current");
        $(".smartStudentName").each(function(){
            $(this).removeClass("smart_clazz_beans_active");
        });
        $("#smartClazzStudentListDiv").show();
        $("#smartClazzMultiChoiceDiv").hide();
        $("#multiChoiceBtn").parent().toggleClass("active");
    });

    // 单击学生头像
    $(".smartStudentName").die().live("click", function(event){
        var $this = $(this);

        if(multiChoiceStatus){
            if($this.hasClass("smart_clazz_beans_active")){
                $this.removeClass("smart_clazz_beans_active");
            }else{
                $this.addClass("smart_clazz_beans_active");
            }
            //全选状态标记
            var studentsNum = $('#students_list_box .smartStudentName').length;
            var studentsSelectedNum = $('#students_list_box .smartStudentName.smart_clazz_beans_active').length;
            var select_all_but = $("#smartClazzMultiChoiceDiv i.w-checkbox");
            if(studentsNum != studentsSelectedNum){
                select_all_but.removeClass('w-checkbox-current');
            }else{
                select_all_but.addClass('w-checkbox-current');
            }
        }else{
            var studentEntity = $this.data('student');
            var students = [];
            students.push(studentEntity);
            <#--奖项列表-->
            drawRewardItem(students);
        }
    });

    //给予奖励
    $("#give_reward_but").on('click' ,function(){
        var studentList = [];
        $("#students_list_box .smart_clazz_beans_active").each(function(){
            var $student = $(this).data('student');
            studentList.push($student);
        });
        if(studentList.length == 0){
            $17.alert('请选择要奖励的学生');
            return false;
        }

        //校验园丁豆
        var clazzSilverCoinPool = $("#totalIntegral").text() * 1;
        if(clazzSilverCoinPool == 0){
            alertImpromptu(popupList.clazzPoolZero,{
                loaded : function(e){
                    $.prompt.addState(popupList.exchangeIntegral.name,popupList.exchangeIntegral);
                }
            });
            return false;
        }else if(clazzSilverCoinPool - studentList.length < 0){
            alertImpromptu(popupList.clazzPoolLessThanZero,{
                loaded : function(e){
                    $.prompt.addState(popupList.exchangeIntegral.name,popupList.exchangeIntegral);
                    var statecontent = $.prompt.getState(popupList.clazzPoolLessThanZero.name);
                    $("#studentCntPop",statecontent).text(clazzSilverCoinPool);
                }
            });
            return false;
        }
        drawRewardItem(studentList);
        return false;
    });


    <#--渲染奖项列表页-->
    function drawRewardItem(studentList,rewardFrom){
        var myImpropObj = alertImpromptu(popupList.rewardItemV3,{
            loaded : function(e){
                var statecontent = $.prompt.getState(popupList.rewardItemV3.name);

                var rewardItemContent = template("t:学生头像",{students : studentList});
                $("#selectStudentList",statecontent).html(rewardItemContent);
                //业务处理图标
                $.prompt.addState(popupList.loadProcess.name,popupList.loadProcess);
                rewardItemClickEvent(studentList,rewardFrom);
            }
        });
    }

    /******************随机选择********开始****************/
    var $randomBtn = $("#random_but");
    $randomBtn.hover(function(){
        $(this).find("div").show();
    },function(){
        $(this).find("div").hide();
    });

    $randomBtn.on("mouseover","p",function(){
        $(this).addClass("current").siblings("p").removeClass("current");
    });

    $randomBtn.on('click',"p", function(){
        if($(".hint_box").is(':visible')){return false;}
        $17.tongji('老师-课堂-随机选择');
        var $this = $(this);
        //抽选的学生数量
        var quota = $this.attr("data-value");
        //页面的学生列表
        var tempStudentMapper = ${json_encode(studentList)};
        var studentCnt = tempStudentMapper.length;
        if(studentCnt <= 0){return false;}
        if(studentCnt == selectedStudentIdList.length){
            selectedStudentIdList = [];
        }

        //乱序后的学生列表,用于页面头像切换
        var studentMapper = [];
        //乱序后未选择过的学生列表
        var unCheckStudentMapper = [];
        for(var i = 0,iLen = tempStudentMapper.length; i < iLen; i++){
            var randomIndex = parseInt(Math.random()*(tempStudentMapper.length));
            //没有选择过的学生，push到studentMapper
            if($.inArray(tempStudentMapper[randomIndex].studentId, selectedStudentIdList) == -1){
                unCheckStudentMapper.push(tempStudentMapper[randomIndex]);
            }
            studentMapper.push(tempStudentMapper[randomIndex]);
            //删除元素
            tempStudentMapper.splice(randomIndex,1);
        }
        //如果未选择过的学生列表个数不够名额数，则名额数按未选择的学生列表数计算
        if(unCheckStudentMapper.length < quota){
            quota = unCheckStudentMapper.length;
        }
        //被抽中的学生
        var winnerList = [];
        var groupStudentCnt = Math.floor(unCheckStudentMapper.length/quota);
        for(var z = 0; z < quota; z++ ){
            /*slice() 方法可从已有的数组中返回选定的元素。*/
            var groupStudentList;
            if(z == (quota - 1)){
                groupStudentList = unCheckStudentMapper.slice(z * groupStudentCnt);
            }else{
               groupStudentList = unCheckStudentMapper.slice(z * groupStudentCnt,(z + 1) * groupStudentCnt);
            }
            //被选中的学生下标
            var selectStudentIndex = parseInt(Math.random()*(groupStudentList.length));
            winnerList.push(groupStudentList[selectStudentIndex]);
        }

        //被选中的学生下标
        var hz = 100 ; //学生卡片切换的频率 （毫秒数）
        var myImpropObj = alertImpromptu(popupList.randomSelect,{
            classes : {
                box: 'smartclass_gettip_message'
            },
            loaded : function(){
                //填充学生信息
                var $currentState = $.prompt.getCurrentState();
                var $iconSpan = $currentState.find(".jqimessage span");
                $iconSpan.each(function(index){
                    if(index >= quota){return false;}
                    $(this).html(template("t:随机选择单人渲染", {student : studentMapper[index]})).show();
                });
                var $visibleSpan = $currentState.find(".jqimessage span:visible");

                //添加奖励项列表
                $.prompt.addState(popupList.rewardItemV3.name, popupList.rewardItemV3);

                var intervalCnt = 0;
                var studentRandom = setInterval(function(){
                    intervalCnt = intervalCnt + 1;
                    if(intervalCnt == 20){
                        $visibleSpan.each(function(index){
                            $(this).html(template("t:随机选择单人渲染", {student : winnerList[index]}));
                        });
                        clearInterval(studentRandom);
                        setTimeout(function(){
                            drawRewardItem(winnerList,"random");
                        },1000);
                    }else{
                        //显示被随机到的学生
                        $visibleSpan.each(function(){
                            var randomStudentIndex = parseInt(Math.random()*(studentMapper.length));
                            $(this).html(template("t:随机选择单人渲染", {student : studentMapper[randomStudentIndex]}));
                        });
                    }
                },hz);
            }
        });
        return false;
    });
    /******************随机选择********结束***************/
    //兑换
    $("#exchange_but").on('click', function(){
        $17.tongji('老师-课堂-兑换学豆');
        alertImpromptu(popupList.exchangeIntegral,{
            loaded : function(){
                /*输入框只能输入数字*/
                $('input[id="tempNumBox"]').keyup(function(){
                    if (/\D/g.test(this.value)){
                        this.value = this.value.replace(/\D/g, '');
                    }
                });

                $(".tempNum").die().live({
                    focus : function(){
                        $("#tip_box").show();
                    },
                    change : function(){
                        changeSilver($(this));
                    }
                });
            }
        });

    });

    $("#exchangeHoverEnvet").on({
        mouseenter: function(){
            $(this).find(".t-reward-flowerBean").show();
        },
        mouseleave: function(){
            $(this).find(".t-reward-flowerBean").hide();
        }
    });

    //减学豆
    $(".minusBtn").live('click', function(){
        var $that = $(this);
        var tempNum = $that.siblings('.tempNum');
        var tempNumVal = tempNum.val() * 1;

        tempNum.attr("value", checkNum(tempNumVal,'minus'));
        $that.siblings().closest('a.plusBtn').removeClass("btn_disable");
        //园丁豆学豆计数器
        changeSilver($that);

    });

    //加学豆
    $(".plusBtn").live('click', function(){
        var $that = $(this);
        var tempNum = $that.siblings('.tempNum');
        var tempNumVal = tempNum.val() * 1;

        tempNum.attr("value", checkNum(tempNumVal,'plus'));
        $that.siblings().closest('a.minusBtn').removeClass("btn_disable");
        //园丁豆学豆计数器
        changeSilver($that);
    });

    //重置显示数据
    $("#reset_but").on('click', function(){
        $17.tongji('老师-课堂-重置显示数据');
        $.prompt('<div class="jqicontent"> 重置后该页面学生的学豆数将全部显示为0，你确定要这样做吗？ <br /> <span class="text_small text_gray_6">说明：该操作仅清除该页的显示数量，全部记录您可以到”发放记录“中查看</span> </div>',{
            title : '重置提示',
            buttons : {"取消" : false , "重置全部" : true },
            focus : 1,
            submit : function(e,v){
                e.preventDefault();
                if(v){
                    var $this = $("#students_list_box .smartStudentName");
                    var userIdsList = [];
                    //获取已被奖励的学生Id
                    $this.each(function (index, domEle) {
                        if($(this).find('.integralNum').text() * 1 != 0 ){
                            userIdsList.push($(this).data('student_id'))
                        }
                    });
                    if(userIdsList.length == 0){
                        $.prompt.close();
                        return false;
                    }
                    $.post('/teacher/smartclazz/resetstudentdisplay.vpage', {clazzId : clazzId, userIds : userIdsList.toString(), subject : "${curSubject!}"}, function(data){
                        if(data.success){
                            $17.tongji('老师-课堂-重置显示数据-成功');
                            $17.alert("学豆显示数据已重置成功，快去给学生发放奖励吧！", function(){
                                //重置显示数据 为0
                                $this.find('.integralNum').text(0).hide();
                            });
                        }else{
                            $17.alert(data.info);
                        }
                    });
                }else{
                    $.prompt.close();
                }
                return false;
            }
        });
    });

    //输入
    $(document).on("keyup", ".v-smsCode", function(){
        $(this).removeClass("w-int-error").siblings(".errorMsg").hide();
    });

    //发送验证码
    $(document).on("click", ".v-getSmsCode", function(){
        var $this = $(this);

        var userMobile = $("#v-exchangeMobile").text();
        console.log(userMobile);

        $.post("/teacher/smartclazz/sendTEICode.vpage", { mobile : userMobile}, function(data){
            if(data.success){
                $17.getSMSVerifyCode($this, data);
            }else{
                $17.alert(data.info);// 先暂时这样吧。。。
            }
        })
    });



    /***************排序操作****开始*************/
    var $sortBtn = $("#sort_btn");
    $sortBtn.hover(function(){
        $(this).find("div").show();
    },function(){
        $(this).find("div").hide();
    });
    $sortBtn.on("mouseover","p",function(){
        $(this).addClass("current").siblings("p").removeClass("current");
    });
    $sortBtn.on("click", "p", function(){
        var $this = $(this);
        var studentList = [];
        $(".smartStudentName").each(function(){
            studentList.push($(this).data("student"));
        });
        if(studentList.length <= 1){
            return false;
        }

        var orderValue = $this.attr("data-value");
        switch(orderValue){
            case "INTEGRAL_DESC":
                $17.tongji('老师-课堂-学豆降序');
                studentList.sort(function(a, b){
                    var $that1 = $("div[class*='smartStudentName'][data-student_id='" + a.studentId + "']");
                    var bean1 = +$.trim($that1.find('.integralNum').text()) || 0;
                    var $that2 = $("div[class*='smartStudentName'][data-student_id='" + b.studentId + "']");
                    var bean2 = +$.trim($that2.find('.integralNum').text()) || 0;
                    return bean2 - bean1;
                });
                break;
            case "INTEGRAL_ASC":
                $17.tongji('老师-课堂-学豆升序');
                studentList.sort(function(a, b){
                    var $that1 = $("div[class*='smartStudentName'][data-student_id='" + a.studentId + "']");
                    var bean1 = +$.trim($that1.find('.integralNum').text()) || 0;
                    var $that2 = $("div[class*='smartStudentName'][data-student_id='" + b.studentId + "']");
                    var bean2 = +$.trim($that2.find('.integralNum').text()) || 0;
                    return bean1 - bean2;
                });
                break;
            case "LETTER_ASC":
                $17.tongji('老师-课堂-[A-Z]排序');
                studentList.sort(function(a, b){
                    var alphaIndex1 = +a.initial || 0,alphaIndex2 = +b.initial || 0;
                    return alphaIndex1 - alphaIndex2;
                });
                break;
            case "NUMBER_ASC":
                $17.tongji('老师-课堂-学号排序');
                studentList.sort(function(a, b){
                    return a.studentId - b.studentId;
                });
                break;
            case "GROUP_ASC":
                $17.tongji('老师-课堂-小组升序');
                studentList.sort(function(a, b){
                    return b.tinyGroupId - a.tinyGroupId;
                });
                break;
        }
        var $div = $("<div></div>");
        var $studentListBox = $("#students_list_box");
        for(var i = 0; i < studentList.length; i++){
            $div.append($studentListBox.find("div[data-student_id='" + studentList[i].studentId + "']"));
        }
        $studentListBox.empty().append($div.html());
        return false;
    });
    /***************排序操作****结束**********/

    function sortNumber(a,b)
    {
        return a - b
    }

    var arr = new Array(6)
    arr[0] = "10"
    arr[1] = "5"
    arr[2] = "40"
    arr[3] = "25"
    arr[4] = "1000"
    arr[5] = "1"

    console.info(arr)
    console.info(arr.sort(sortNumber))

});
</script>
</@temp.pagecontent>