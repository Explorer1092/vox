<#import "module.ftl" as temp />
<@temp.pagecontent mainmenu="timer_reward" submenu="timer">
<style>
    div.d-hidden{
        display: none;
    }
</style>
<div class="s-tab-box">
    <div class="inner">
        <div class="st-title">
            <ul>
                <li class="active count_tab" data-sort="desc"><a href="javascript:void (0)" >倒计时</a></li>
                <li class="count_tab" data-sort="asc"><a href="javascript:void (0)">正计时</a></li>
            </ul>
        </div>
        <div class="st-time-box">
            <div class="si-bar d-clock">
                <p class="bar-time">
                    <input type="text" id="in-min" maxlength="2" class="w-int setclock" name="min" value="1"/>
                    <span>分</span>
                    <input type="text" id="in-sec" maxlength="2" class="w-int setclock" name="sec" value="0"/>
                    <span>秒</span>
                </p>
            </div>
            <div class="si-bar d-clock d-hidden">
                <p class="bar-time">
                    <span id="timer_min">0</span>
                    <span>:</span>
                    <span id="timer_sec">0</span>
                    <span>:</span>
                    <span id="timer_msec">00</span>
                </p>
            </div>
            <div class="si-circle">
                <span class="green-circle btn-circle" id="set_clock">开始计时</span>
                <span class="green-circle btn-circle" id="count_start" style="display: none;">继续</span>
                <span class="red-circle btn-circle" id="count_pause" style="display: none;">暂停</span>
                <span class="blue-circle btn-circle" id="count_reset"  style="display: none;">复位</span>
            </div>
        </div>
    </div>
</div>
<#--播放MP3文件-->
<div id="audiomp3flash"></div>
<audio id="endTimeAudio" src="<@app.link href="public/skin/teacherv3/images/smartclazz/alarm.mp3"/>" preload="metadata">
    推荐使用chrome55版本以上浏览器播放音频
</audio>
<script type="text/javascript">
    $(function(){
        $17.tongji("互动课堂-课堂奖励-计时工具");
        var timer_clock;
        var timerTool = {
            sort       : "desc", //倒计时或正计时 倒计时desc 正计时asc
            minute     : 0,     //初始化分钟
            second     : 0,     //初始化秒钟
            millisec   : 0,
            defaultMin : 0,
            defaultSec : 0,
            setSort : function(sort){
                if(!$17.isBlank(sort) && (sort.toLowerCase() == "desc" || sort.toLowerCase() == "asc")){
                    this.sort = sort;
                }
            },
            setCountInit : function(min,sec){
                if($17.isNumber(min) && min >= 0 && min < 59){
                    this.defaultMin = parseInt(min,0);
                }
                if($17.isNumber(sec) && sec >= 0 && sec < 59){
                    this.defaultSec = parseInt(sec,0);
                }
                this.minute = this.defaultMin;
                this.second = this.defaultSec;
                this.millisec = 0;
                this.displayCount();
            },
            displayCount : function(){
                $("#timer_min").text($17.strPad(this.minute,"0",2,"l"));
                $("#timer_sec").text($17.strPad(this.second,"0",2,"l"));
                $("#timer_msec").text(this.millisec);
                if(this.minute == 0 && this.second == 0 && this.millisec == 0){
                    var audioElem = document.getElementById("endTimeAudio");
                    if(audioElem && typeof audioElem.play === "function"){
                        audioElem.play();
                    }else{
                        $("#audiomp3flash").jmp3({
                            autoStart: 'true',
                            file: "<@app.link href="public/skin/teacherv3/images/smartclazz/alarm.mp3"/>",
                            width: "1",
                            height: "1"
                        });
                    }
                }
            },
            countDowning : function(timer){
                this.displayCount();
                if(this.millisec == 0 && this.minute == 0 && this.second == 0){
                    clearInterval(timer);
                    return;
                }
                if(this.millisec == 0){
                    this.millisec = 9;
                    if(this.second == 0){
                        this.second = 59;
                        this.minute -= 1;
                    }else{
                        this.second -= 1;
                    }
                }else{
                    this.millisec -= 1;
                }
            },
            countUping : function(timer){
                this.displayCount();
                if(this.millisec == 10 && this.minute == 59 && this.second == 59){
                    clearInterval(timer);
                    return false;
                }
                this.millisec += 1;
                if(this.millisec == 10){
                    this.millisec = 0;
                    this.second = this.second + 1;
                    if(this.second == 59){
                        this.second = 0;
                        this.minute = (this.minute == 59 ? 0 : this.minute + 1);
                    }
                }
            },
            countStart : function(){
                var $this = this;
                //开始计数
                var timer = setInterval(function(){
                    if($this.sort.toLowerCase() == "desc"){
                        $this.countDowning(timer);
                    }else if($this.sort.toLowerCase() == "asc"){
                        $this.countUping(timer);
                    }
                },100);
                return timer;
            },
            resetClock : function(){
                clearInterval(timer_clock);
                $("#set_clock").show();
                $("#count_start").hide();
                $("#count_pause").hide();
                $("#count_reset").hide();
            }
        };

        //设置时间
        var input_tools = {
             defaultVal : "",
             init : function(){
                 /*输入框只能输入数字*/
                 $('input.setclock').keyup(function(){
                     var inpt_value = this.value;
                     if (/\D/g.test(inpt_value)){
                         this.value = inpt_value.replace(/\D/g, input_tools.defaultVal);
                     }else{
                         if(inpt_value < 0 || inpt_value > 59){
                             this.value = input_tools.defaultVal;
                         }
                     }
                 }).focus(function(){
                     input_tools.defaultVal = this.value;
                     this.value = '';
                 }).blur(function(){
                     if (!$17.isNumber(this.value)){
                         this.value = input_tools.defaultVal;
                     }
                 });
             }
        };
        input_tools.init();

        // 倒计时 正计时
        $("li.count_tab").on("click", function(){
            var $this = $(this);
            if($this.attr("data-sort").toLowerCase() == "desc"){
                $("#in-min").val("1");
                $("#in-sec").val("0");
            }else{
                $("#in-min").val("0");
                $("#in-sec").val("0");
            }
            timerTool.resetClock();
            $(this).addClass("active").siblings().removeClass("active");
            $("div.d-clock").first().removeClass("d-hidden").siblings(".d-clock").addClass("d-hidden");
            timerTool.setSort($("li.active").attr("data-sort"));
        });

        //设置分秒
        $("#set_clock").on("click",function(){
            timerTool.setCountInit($("#in-min").val(),$("#in-sec").val());
            timerTool.displayCount();
            $(".d-clock").toggleClass("d-hidden");
            $(this).hide();
            $("#count_start").trigger("click");
        });


        // 继续
        $("#count_start").on("click", function(){
            $("#count_pause").show();
            $("#count_reset").show();
            $(this).hide();
            timer_clock = timerTool.countStart();
        });

        // 暂停
        $("#count_pause").on("click",function(){
            $("#count_start").show();
            $("#count_reset").show();
            $("#audiomp3flash").html("");
            $(this).hide();
            clearInterval(timer_clock);
        });

        //复位
        $("#count_reset").on("click",function(){
            if($("li.active").attr("data-sort").toLowerCase() == "desc"){
                $("#in-min").val("1");
                $("#in-sec").val("0");
            }else{
                $("#in-min").val("0");
                $("#in-sec").val("0");
            }
            timerTool.resetClock();
            $("div.d-clock").first().removeClass("d-hidden").siblings(".d-clock").addClass("d-hidden");
            $("#audiomp3flash").html("");
        });
    });
</script>
</@temp.pagecontent>