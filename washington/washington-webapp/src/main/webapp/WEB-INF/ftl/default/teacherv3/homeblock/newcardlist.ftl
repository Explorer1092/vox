<#assign teacherAppBind = (data.appBinded)?? && data.appBinded/><#--是否绑定APP-->

<@sugar.capsule js=["ZeroClipboard"] css=["new_teacher.cardlist"] />

<div class="w-sets">
    <div class="w-sets-title">
        <h1 class="js-taskCenterTitle">任务中心</h1>
    </div>
    <div class="w-sets-container w-sets-container-back cardListContent js-cardList">
        <ul class="t-teacherMissionCenter slides" style="" id="cardListContent">
            <div style="height: 200px; background-color: white; width: 98%;"><img src="<@app.link href="public/skin/teacherv3/images/loading.gif"/>" style="margin: 70px auto;display: block;" /></div>
        </ul>
        <ul class="t-teacherMissionCenter-arrow js-cardNav">
            <li>
                <a href="javascript:void(0);" class="arrow_btn prev JS-flex-prev disabled"></a>
            </li>
            <li>
                <a href="javascript:void(0);" class="arrow_btn next JS-flex-next disabled"></a>
            </li>
        </ul>
    </div>
</div>
<#include "newCardlistTemp.ftl">
<script>
    $(function () {
        function teacherCardList() {
            var self = this;
            self.list = []; //获取的cardList
            self.showLogS1 = [];
            self.fetchData = function () {
                $.get('/teacher/cardlist.vpage',function (res) {
                    if(res.success){
                        self.list = res.cardList?res.cardList:[];
                        self.extendList(); //混入前端固定卡片
                        self.parseCardData();
                        self.render();

                        self.cardViewLog();
                    }
                })
            };

            self.cardViewLog = function () {
                var btnTextList = [],
                    btnNodes = $('.js-cardListBtn');
                $.each(btnNodes,function (i,item) {
                    btnTextList.push($(item).text());
                });
                //展示打点
                self.tclLog({
                    op:"taskcard_show",
                    s1:self.showLogS1,
                    s2:btnTextList
                });
            };

            self.parseCardData = function () {
                var list = self.list;
                if(list.length > 0) {
                    for(var i=0;i<list.length;i++){
                        if(list[i].cardDetails){
                            list[i].cardDetailsJson = JSON.parse(list[i].cardDetails);
                        }

                        if(list[i].cardType == "HOMEWORK"){ //作业卡片记录子类型
                            self.showLogS1.push(list[i].cardDetailsJson.taskType);
                        }else{
                            self.showLogS1.push(list[i].cardType);
                        }

                    }
                }
            };

            self.render = function () {
                var list = self.list,
                    cardTemp = '';
                if(list.length >0){
                    for(var i=0;i<list.length;i++){
                        var tempName = list[i].cardType + '_TEMP',
                            data = self.list[i];
                        cardTemp += template(tempName,data);
                    }

                    $("#cardListContent").html(cardTemp);

                    //切换动画
                    self.animation();
                }else{
                    /*并无卡片时移除这个任务中心*/
                    $('.js-taskCenterTitle').html('');
                    $(".js-cardList").remove();
                }
            };

            //混入前端固定卡片
            // TODO 对混合后的数据进行优先级排序
            self.extendList = function () {
                <#if !(teacherAppBind!false)> //标志
                    self.list.push({"cardType": "DownLoadTeacherApp"}); //模板
                </#if>
            };

            self.animation = function () {
                var eventLoop = setInterval(function () {
                    var liNodes = $("#cardListContent>li");
                    if(liNodes.length > 0 ){
                        clearInterval(eventLoop);
                        eventLoop = null;

                        $(".js-cardList").flexslider({
                            animation : "slide",
                            animationLoop : true,
                            directionNav: false,
                            controlNav: false,
                            slideshow : false,
                            slideshowSpeed: 4000, //展示时间间隔ms
                            animationSpeed: 400, //滚动时间ms
                            itemWidth : 90,
                            direction : "horizontal",//水平方向
                            minItems : 3,
                            maxItems : 3,
                            manualControls:".js-cardNav>a", //自定义导航切换
                            start: function (slider) {
                                if(slider.count > 3){
                                    $('.JS-flex-next').removeClass("disabled");
                                }
                                $('.JS-flex-prev').on("click", function () {
                                    if(!$(this).hasClass("disabled")){
                                        slider.flexAnimate(slider.getTarget("previous"), true);
                                    }
                                });

                                $('.JS-flex-next').on("click", function () {
                                    if(!$(this).hasClass("disabled")){
                                        slider.flexAnimate(slider.getTarget("next"), true);
                                    }
                                });
                            },
                            after: function (slider) {
                                var currentPage = slider.currentSlide;
                                if(currentPage == 0) {
                                    $('.JS-flex-prev').addClass("disabled");
                                    $('.JS-flex-next').removeClass("disabled");
                                }
                                if(currentPage == slider.last) {
                                    $('.JS-flex-next').addClass("disabled");
                                    $('.JS-flex-prev').removeClass("disabled");
                                }

                                if(currentPage != 0 && currentPage != slider.last){
                                    $('.js-cardNav li>a').removeClass("disabled");
                                }
                            }
                        });
                    }
                },100);
            };

            //卡片相关打点
            self.tclLog = function (data) {
                var logJson = {module : "m_wAzWanf9",s0:"${(currentTeacherDetail.subject)!}"},
                    op = data.op,
                    s1 = data.s1,
                    s2 = data.s2;
                logJson.op = op;
                if(s1){
                    logJson.s1 = s1;
                }
                if(s2){
                    logJson.s2 = s2;
                }
                $17.voxLog(logJson);
            };

            self.init = function () {
              self.fetchData();
            }
        }

        var tcl = new teacherCardList();
        tcl.init();

        //cardList 相关事件
        $(document).on("click",".js-switchCardStatusBtn",function () {
            var index = $(this).data('index');
            $(this).parent('li').addClass('active').siblings('li').removeClass('active');
            $('.js-progressInfo[data-index="'+index+'"]').show().siblings('span').hide();
        }).on("click",".js-switchNumberBtn",function () { //切换number
            var $noPhoneNumNode = $('.js-switchNumberBtn[data-index="1"]');
            var $phoneNumNode = $('.js-switchNumberBtn[data-index="2"]');
            if($noPhoneNumNode.is(":visible")){
                $noPhoneNumNode.hide();
                $phoneNumNode.show();
            }else{
                $noPhoneNumNode.show();
                $phoneNumNode.hide();
            }
            var mNumber = $(this).data('mid');
            if(mNumber){
                $('.js-mobileOrId').html(mNumber+"，加入班级");
                changeInviteContent();
            }else{
                alertPromptInfo('你还没绑定手机号');
            }
        }).on("click",".js-switchCardDayBtn",function () { //练习切换星期按钮
            var $parent = $(this).parent('li');
            if(!$parent.hasClass('disabled')){ //未到日期的不可切
                var index = $(this).data('index');
                $(this).parent('li').addClass('active').siblings('li').removeClass('active');
                $('.js-classListCon[data-index="'+index+'"]').show().siblings('ul.set-column').hide();
            }
        }).on("click",".js-rulDescBtn,.js-rollBackBtn",function () { //规则说明切换
             var $parentNode = $(this).parents(".cardBox");
             var $cardFace = $parentNode.find('.daily-card'),
                 $cardBack = $parentNode.find('.ruleExplain-card');
             if($cardFace.is(":visible")){
                 $cardBack.show();
                 $cardFace.hide();
             }else{
                 $cardBack.hide();
                 $cardFace.show();
             }
        }).on("click",".js-createNewHomeworkBtn",function () { //推荐新练习
            var assignHomeworkUrl = "/teacher/new/homework/batchassignhomework.vpage?subject=${(currentTeacherDetail.subject)!}",
                ableFlag = $(this).data('aid');
            if(ableFlag && ableFlag == "1"){
                location.href = assignHomeworkUrl;
            }else if(ableFlag == "2"){
                //周末练习周五单独处理
                $.prompt('<div class="setPop-box"><p>只有周五布置作业才能完成任务哦</p><p>是否继续布置作业</p></div>',{
                    title: "提示",
                    buttons: {"取消":false,"继续": true},
                    position: {width:400,height:200},
                    loaded:function () {
                        tcl.tclLog({
                            op:"taskcard_homeworktask_newhomework_window_load"
                        })
                    },
                    submit : function(e, v){
                        var btnText = "";
                        if(v){
                            setTimeout(function () {
                                location.href=assignHomeworkUrl;
                            },100);
                            btnText = "继续";
                        }else{
                            btnText = "取消";
                        }
                        tcl.tclLog({
                            op:"taskcard_homeworktask_newhomework_window_click",
                            s2:btnText
                        })
                    }
                });
            }else if(ableFlag == "3"){
                //运营活动练习弹窗
                var toast = $(this).data("toast");
                $.prompt('<div class="setPop-box"><p>'+toast+'</p></div>',{
                    title: "提示",
                    buttons: {"取消":false,"继续": true},
                    position: {width:400,height:200},
                    loaded:function () {
                        tcl.tclLog({
                            op:"taskcard_homeworktask_newhomework_holiday_window_load"
                        })
                    },
                    submit : function(e, v){
                        var btnText = "";
                        if(v){
                            setTimeout(function () {
                                location.href=assignHomeworkUrl;
                            },100);
                            btnText = "继续";
                        }else{
                            btnText = "取消";
                        }
                        tcl.tclLog({
                            op:"taskcard_homeworktask_newhomework_holiday_window_click",
                            s2:btnText
                        })
                    }
                });
            }else{
                location.href = assignHomeworkUrl;
            }
        }).on("click",".js-activityDetailBtn",function () { //活动查看详情
            var link = $(this).data('link');
            location.href = link;
        }).on("click",".js-getHomeworkRewardBtn",function () { //领取奖励
            var taskId = $(this).data('tid');
            $.ajax('/teacher/new/homework/task/rewardintegral.vpage?taskId='+taskId,{
                type:"GET",
                success:function (res) {
                    if(res.success){
                        alertPromptInfo(template("successRewordDialog_temp",{"number":res.integralCount?res.integralCount:5}),"提示",function () {
                            location.reload();
                        },{width:400,height:200});
                    }else{
                        alertPromptInfo(res.info?res.info:'好像出问题咯');
                    }
                },
                error:function (e) {
                    console.log(e);
                    alertPromptInfo('好像出问题咯');
                }
            })
        }).on("click",".js-inviteStudentBtn",function () { //邀请学生加入
            if(!$(this).hasClass("w-btn-gray")){
                var ucenterLink = "${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage",
                        aid = $(this).data('aid'),
                        mobile = $(this).data('mid');
                if(aid){
                    location.href = ucenterLink;
                }else{
                    // TODO 兼容没有绑定手机号
                    $.prompt(template("addStudentDialog_temp", {"mobile":mobile}), {
                        title: "如何添加学生？",
                        buttons: {},
                        position: {width: 760},
                        loaded : function(){
                            changeInviteContent();
                        },
                        submit : function(e, v){
                            if(v){

                            }
                        }
                    });
                }
            }
        }).on("click",".js-cardListBtn",function () { //底部按钮打点
            var text = $(this).text(),
                type = $(this).data("type");
            tcl.tclLog({
                op:"taskcard_bottom_click",
                s1:type,
                s2:text
            })
        });

        var alertPromptInfo = function (text,title,callback,position) {
            $.prompt('<p style="text-align: center;">'+text+'</p>', {
                title: title?title:"系统提示",
                buttons: {"确定": true},
                position: position?position:{},
                submit : function(e, v){
                    if(v){
                        if(typeof callback =="function"){
                            callback();
                        }
                    }
                }
            });
        };

        //改变复制文本的值
        var changeInviteContent = function () {
            var content = $("#copyToStudentInfo").html(),
                mobileOrId = $('.js-mobileOrId').html(),
                footerStr = $('.js-dialogTeacherInfo').html();
            $('#copyToStudentInfo').val(content+mobileOrId+footerStr);

            //复制到剪切板
            $17.copyToClipboard($("#copyToStudentInfo"), $("#clip_button_to_student"), "clip_button_to_student", "clip_container_to_student", function(){
                tcl.tclLog({
                    op:"taskcard_Authenticate_details_invite_copy_click"
                })
            });
        }
    });
</script>
