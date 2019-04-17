<#import "../module.ftl" as temp />
<@temp.pagecontent mainmenu="classroom_ask" submenu="synscan">
<@sugar.capsule js=["jquery.qrcode"] />
<#if questionId?has_content>
<p class="s-red s-magB-10">温馨提示：请打开“爱提问”客户端扫描题目右侧二维码，即可获取班级及学生信息</p>
<div class="s-exercise-box s-exercise-detail">
    <div class="se-l s-fl-right" id="qrcodeTable"><#--<i class="weixin-good" ></i>--></div>
    <div class="se-r s-fl-left">
        <#--题的内容-->
        <div id="questionContent">

        </div>
        <div>
            <ul>
                <li>
                    <a id="preQuestion" class="s-btn s-magR-30" href="javascript:void(0);">上一题</a>
                    <a id="nextQuestion" class="s-btn" href="javascript:void(0);">下一题</a>
                </li>
                <#if ProductDevelopment.isDevEnv()>
                    <li>
                        <a id="startClock" class="s-btn s-magR-30" href="javascript:void(0);">开始</a>
                        <a id="closeClock" class="s-btn" href="javascript:void(0);">结束</a>
                    </li>
                </#if>
            </ul>
        </div>
    </div>
    <div class="s-clear"></div>
</div>
<p class="s-magB-10 v-parent" id="showAnswer"><i class="s-checkbox" data-title="显示答案"></i>显示答案</p>
<p class="s-magB-10 v-parent" id="showRightAnswer" style="display: none;"><i class="s-checkbox" data-title="显示正确答案"></i>显示正确答案</p>
<div class="s-tab-box">
    <div class="inner">
        <div class="st-title">
            <ul>
                <li class="active l-answer" data-value="answerDetail"><a href="javascript:void(0);">学生答题情况</a></li>
                <li class="l-answer" data-value="answerChart"><a href="javascript:void(0);">答案分布柱状图</a></li>
            </ul>
        </div>
        <div id="answerDetail" class="st-con panel_tab">
            <#if studentList?has_content>
                <#list studentList as student>
                    <dl>
                        <dt><i id="s_${student.id}" class="s-chair s-chair-gray"></i></dt>
                        <dd>
                            <p>
                            ${student.fetchRealname()}<br>
                            ${student.id}
                            </p>
                        </dd>
                    </dl>
                </#list>
            </#if>
        </div>
        <div id="answerChart" class="st-con panel_tab" style="display: none;">
            <div class="s-tree-detail">
                <p>答题人数：<span id="answerCount">40</span> 人 </p>
                <p>正确率：   <span id="rightRate">40%</span></p>
                <ul>
                    <li style="height: 50%" class="s-1 blue"><span>12人</span></li>
                    <li style="height: 30%" class="s-2 blue"><span>5人</span></li>
                    <li style="height: 20%" class="s-3 blue"><span>8人</span></li>
                    <li style="height: 80%" class="s-4 blue"><span>8人</span></li>
                </ul>
                <div class="st-foot">
                    <p><strong>A</strong><span>错误</span></p>
                    <p><strong>B</strong><span>错误</span></p>
                    <p><strong>C</strong><span>错误</span></p>
                    <p><strong>D</strong><span>错误</span></p>
                </div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    function toUtf8(str) {
        var out, i, len, c;
        out = "";
        len = str.length;
        for(i = 0; i < len; i++) {
            c = str.charCodeAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                out += str.charAt(i);
            } else if (c > 0x07FF) {
                out += String.fromCharCode(0xE0 | ((c >> 12) & 0x0F));
                out += String.fromCharCode(0x80 | ((c >>  6) & 0x3F));
                out += String.fromCharCode(0x80 | ((c >>  0) & 0x3F));
            } else {
                out += String.fromCharCode(0xC0 | ((c >>  6) & 0x1F));
                out += String.fromCharCode(0x80 | ((c >>  0) & 0x3F));
            }
        }
        return out;
    }

    $(function(){
        $17.tongji("互动课堂-课堂提问-同步扫描");
        $17.tongji("互动课堂-课堂提问-我的问题-扫描答案");
        $('#qrcodeTable').qrcode({
            render	: "table",
            width       : 144,     //设置宽度
            height      : 245,     //设置高度
            typeNumber  : -1,      //计算模式
            correctLevel: 2,       //纠错等级
            text	: toUtf8('${param}')
        });

        //复选框
        $(".v-parent .s-checkbox").on("click",function(){
            var $this = $(this);
            $17.tongji("互动课堂-课堂提问-同步扫描-答案分布-" + $this.attr("data-title"));
            $this.toggleClass("s-checkbox-current");
            if($this.hasClass("s-checkbox-current")){
                $this.parent().attr("data-value","show");
            }else{
                $this.parent().removeAttr("data-value");
            }
        });

        var IntervalClock = {
             timeHandler : null,
             start : function(){
                 IntervalClock.timeHandler = setInterval(function(){
                     $.get("/teacher/smartclazz/getanswerdetail.vpage",{clazzId : synScan.clazzId},function(data){
                         if(!$17.isBlank(data.detail) && !$17.isBlank(data.detail.students) && $.isArray(data.detail.students)){
                             var tabVal = $("li.l-answer.active").attr("data-value");
                             switch(tabVal){
                                 case "answerDetail" :
                                     drawAnswerDetail(data.detail.students);
                                     break;
                                 case "answerChart":
                                     drawAnswerChart(data.detail);
                                     break;
                                 default :
                                     break;
                             }
                             setTimeout(function(){
                                 data.detail.questionId = synScan.questionId;
                                 data.detail.subject = synScan.subject;
                                 data.detail.answer = synScan.rightAnswer;

                                 App.postJSON("/teacher/smartclazz/savequestionreport.vpage",data.detail,function(data){});
                             },2000);
                         }
                     },'json');
                 },5000);
             },
             close : function(){
                 clearInterval(IntervalClock.timeHandler);
             },
             init : function(){
                 //开始
                 $("#startClock").on("click",function(){
                     IntervalClock.start();
                 });

                 //结束
                 $("#closeClock").on("click",function(){
                     IntervalClock.close();
                 });

                 //设定五分钟
                 var st = setTimeout(function(){
                     IntervalClock.close();
                 },300000);
             }
        };

        IntervalClock.init();

        var synScan = {
            subject        : "${currentTeacherDetail.subject}",
            clazzId        : "${clazz.id}",
            questionId     : "${questionId}",
            questionIndex  : ${questionIndex!1},
            totalPage      : ${total!1},
            rightAnswer    : "",
            loadQuestion   : function(param){
                //渲染题
                $("#questionContent").load("/teacher/smartclazz/getquestion.vpage",param,function(data){
                    var rightAnswer = $(data).attr("data-answer");
                    var questionId = $(data).attr("data-questionid");
                    //加载到题目
                    if(!$17.isBlank(rightAnswer) && !$17.isBlank(questionId)){
                        synScan.questionId = questionId;
                        synScan.rightAnswer = rightAnswer;
                        IntervalClock.start();
                    }else{
                        IntervalClock.close();
                    }
                });
            },
            initAnswerChart : function(){
                var $answerChart = $("#answerChart");
                $("#answerCount").text("0");
                $("#rightRate").text("");
                $("li",$answerChart).each(function(index){
                    var $this = $(this);
                    $this.attr("class","s-" + (index+1)).css("height",0);
                    $("span",$this).text("");
                });
                $("div.st-foot p",$answerChart).each(function(){
                    var $this = $(this);
                    $("span",$this).removeAttr("class").text("错误").hide();
                });
            },
            initAnswerDetail : function(){
                var $answerDetail = $("#answerDetail");
                $("i",$answerDetail).each(function(){
                    $(this).attr("class","s-chair s-chair-gray");
                });
            },
            drawPreAndNextBtn : function(){
                if(synScan.questionIndex <= 1){
                    $("#preQuestion").addClass("s-btn-disable");
                }else{
                    $("#preQuestion").removeClass("s-btn-disable");
                }
                if(synScan.questionIndex >= synScan.totalPage){
                    $("#nextQuestion").addClass("s-btn-disable");
                }else{
                    $("#nextQuestion").removeClass("s-btn-disable");
                }
            },
            resetCache : function(){
                $.post("/teacher/smartclazz/resetdatacache.vpage",{clazzId : synScan.clazzId},function(data){});
            },
            init : function(){
                synScan.loadQuestion({qId : this.questionId, subject : "${curSubject!}"});
                synScan.initAnswerChart();
                synScan.initAnswerDetail();
                //学生答题情况和答案分布柱状图切换
                $("li.l-answer").on("click",function(){
                    $17.tongji("互动课堂-课堂提问-同步扫描-学生答题情况");
                    var $this = $(this);
                    $this.addClass("active").siblings().removeClass("active");
                    $("div.panel_tab").hide();
                    $("#" + $this.attr("data-value")).show();
                    $("#showAnswer").toggle($this.attr("data-value") == "answerDetail");
                    $("#showRightAnswer").toggle($this.attr("data-value") == "answerChart");
                });

                synScan.drawPreAndNextBtn();

                //上一题
                $("#preQuestion").on("click",function(){
                    $17.tongji("互动课堂-课堂提问-同步扫描-上一题");
                    if($(this).hasClass("s-btn-disable")){
                        return;
                    }
                    IntervalClock.close();
                    synScan.resetCache();
                    synScan.questionIndex = synScan.questionIndex - 1;
                    synScan.drawPreAndNextBtn();
                    synScan.loadQuestion({clazzId : synScan.clazzId, pageNo : synScan.questionIndex, subject : "${curSubject!}"});
                    setTimeout(function(){
                        synScan.initAnswerChart();
                        synScan.initAnswerDetail();
                    },100);
                });

                //下一题
                $("#nextQuestion").on("click",function(){
                    $17.tongji("互动课堂-课堂提问-同步扫描-下一题");
                    if($(this).hasClass("s-btn-disable")){
                        return;
                    }
                    IntervalClock.close();
                    synScan.resetCache();
                    synScan.questionIndex = synScan.questionIndex + 1;
                    synScan.drawPreAndNextBtn();
                    synScan.loadQuestion({clazzId : synScan.clazzId,pageNo : synScan.questionIndex, subject : "${curSubject!}"});
                    setTimeout(function(){
                        synScan.initAnswerChart();
                        synScan.initAnswerDetail();
                    },100);
                });
            }
        };
        synScan.init();

        function drawAnswerDetail(pStudents){
            for(var i = 0; i < pStudents.length; i++){
                var stduent = pStudents[i];
                if($17.isBlank(stduent.studentId)){ continue;}
                var $student = $("#s_" + stduent.studentId);
                if($17.isBlank(stduent.studentAnswer)){
                    $student.removeClass("s-chair-blue").addClass("s-chair-gray");
                }else{
                    $student.removeClass("s-chair-gray").addClass("s-chair-blue");
                    $student.removeClass("s-chair-blue-A s-chair-blue-C s-chair-blue-B s-chair-blue-D");
                    if(!$17.isBlank($("#showAnswer").attr("data-value"))){
                        $student.addClass("s-chair-blue-" + stduent.studentAnswer);
                    }
                }
            }
        }

        function drawAnswerChart(detailInfo){
            var answerOptions = ['A','B','C','D'];
            var answerCnt = detailInfo.studentAnswerCount * 1;
            $("#answerCount").text(answerCnt);
            var $answerChart = $("#answerChart");
            $("li",$answerChart).each(function(index){
                var $this = $(this);
                var key = "answerCount" + answerOptions[index];
                var answerRate = Math.floor(detailInfo[key] * 100/answerCnt) + "%";
                $this.css("height",answerRate);
                $("span",$this).text(detailInfo[key] + "人");
                if(answerOptions[index] == synScan.rightAnswer){
                    //设置正确率
                    $("#rightRate").text(answerRate);
                }
                if(!$17.isBlank($("#showRightAnswer").attr("data-value"))){
                    $this.removeClass("blue");
                    if(answerOptions[index] == synScan.rightAnswer){
                        $this.addClass("green");
                    }
                }else{
                    $this.removeClass("green").addClass("blue");
                }
            });

            $("div.st-foot p",$answerChart).each(function(index){
                var $this = $(this);
                var $span = $("span", $this);
                if(!$17.isBlank($("#showRightAnswer").attr("data-value"))){
                    if(answerOptions[index] == synScan.rightAnswer){
                        $span.addClass("s-green");
                        $span.text("正确");
                    }
                    $("span",$this).show();
                }else{
                    $span.hide();
                }
            });
        }

    });
</script>
<#else>
<div class="s-exercise-box s-exercise-up-box" style="padding: 25px 0;">
    <div class="se-add">
        <p>
            <span class="s-fl-right">您还没有选择问题哦，<br/>快去“我的问题”选择一个吧！</span>
            <i class="s-all s-arrow-big s-fl-left"></i>
            <div class="s-clear"></div>
        </p>
    </div>
</div>
</#if>
</@temp.pagecontent>