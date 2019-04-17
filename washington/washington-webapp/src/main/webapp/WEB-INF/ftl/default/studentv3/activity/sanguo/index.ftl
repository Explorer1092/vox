<#import "../../../layout/project.module.student.ftl" as temp />
<@temp.page title="进击的三国" header="show">
<@sugar.capsule js=["DD_belatedPNG_class"] css=[] />
<@app.css href="public/skin/project/sanguo/css/skin.css" />
<!--//start-->
<div class="main">
    <div class="header">
        <div class="text-back header-text"></div>
    </div>
    <div class="container">
        <div class="container-inner">
            <!--//start-->
            <!--少年郎，看你骨骼精奇，《进击的三国》让你成为英雄！-->
            <div class="text-back title-text-1"></div>
            <!--進擊的三国 登錄就送：-->
            <dl class="item-container">
                <dt><h3 class="text-back title-text-2">進擊的三国 登錄就送：</h3></dt>
                <dd>
                    <div class="item-inner-block" style="margin-right: 96px;">
                        <div class="item-inner-avt"><img src="<@app.link href="public/skin/project/sanguo/images/k-1.png"/>"></div>
                        <p>累计登录4次送5星貂蝉</p>
                    </div>
                    <div class="item-inner-block">
                        <div class="item-inner-avt"><img src="<@app.link href="public/skin/project/sanguo/images/k-3.png"/>"></div>
                        <p>活动期间每日登录送5000园丁豆、5钻石</p>
                    </div>
                </dd>
            </dl>
            <!--呼朋引伴 狂搶学豆：-->
            <dl class="item-container" style="margin-bottom: 60px;">
                <dt><h3 class="text-back title-text-3">進擊的三国 登錄就送：</h3></dt>
                <dd>
                    <div class="item-inner-block" style="margin-right: 96px;">
                        <div class="item-inner-avt"><img src="<@app.link href="public/skin/project/sanguo/images/k-2.png"/>"></div>
                    </div>
                    <div class="item-inner-block">
                        <p>每天19点至20点登录，赠送2学豆</p>
                        <p>每天19点至20点，同班在线8人，每人赠送2学豆</p>
                        <p>每天19点至20点，同班在线9~11人，每人赠送3学豆</p>
                        <p>每天19点至20点，同班在线≧12人，每人赠送4学豆</p>
                    </div>
                </dd>
                <dd class="item-btn">
                    <#--<a style="margin-left: 8px;"  href="javascript:void(0);" class="s-btn s-btn-gray"></a>-->
                    <#--<span class="text">4月24日开启</span>-->
                    <a href="/student/apps/index.vpage?app_key=SanguoDmz" class="s-btn" onclick="$17.tongji('学生端-进击三国-进入游戏');"></a>
                    <a href="/apps/afenti/order/sanguodmz-cart.vpage" class="s-btn s-btn-green" onclick="$17.tongji('学生端-进击三国-购买会员');"></a>
                </dd>
            </dl>
            <div class="answer-container">
                <div class="answer-container-inner">
                    <div class="answer-container-top PNG_24"></div>
                    <div class="answer-container-mid PNG_24">
                        <!--//start-->
                        <!--测一测你是哪种英雄-->
                        <div id="moduleContainer"></div>
                        <!--end//-->
                    </div>
                    <div class="answer-container-bot PNG_24"></div>
                </div>
            </div>
            <!--end//-->
        </div>
        <div class="left-carton PNG_24"></div>
    </div>
</div>
<!--end//-->
<script type="text/javascript">
    $(function(){
        var recordReply = {
            green : 0,
            red : 0,
            blue : 0
        };
        var moduleContainer = $("#moduleContainer");
        var dataJson = {
            questions : [
                {num : "一", title : "当老师收作业，你却没写的时候，你会？", reply : ["老实承认并补做", "跟老师讲说作业被家里的狗狗/猫猫吃掉了", "让老师现场出题考试，我都会了还做什么"]},
                {num : "二", title : "正在外边玩的高兴，妈妈喊你回家吃饭，你会？", reply : ["迅速回家吃完饭再出来玩", "豪气的不吃了，饿一顿又没什么", "邀请小伙伴边吃边玩"]},
                {num : "三", title : "去上厕所只剩一个坑位，却遇上你的同学也要用，你会？", reply : ["让TA先用，自己站外边憋着", "自己先用，让TA站外边憋着", "俩人猜拳决定谁先用"]},
                {num : "四", title : "考试的时候遇到一道题怎么看都不会，你会？", reply : ["随便写点什么填上", "参考周围同学的答案", "询问老师是不是出错题了"]},
                {num : "五", title : "课堂上你的同桌好友和你聊天，你会？", reply : ["假装没听见", "与TA开心的聊起来", "举手报告老师"]},
                {num : "六", title : "食堂吃饭的时候你很讨厌的同学坐在了你的旁边，你会？", reply : ["默默的挪开位置", "把TA盘里的肉夹走", "告诉TA最近老有同学吃出头发丝、肉虫子"]}
            ],
            result : {
                "green" : [
                    "绿色系的你擅长协调人际关系，懂得吃亏是福，能与各色人等和平共处",
                    "相生：对于个性鲜明的蓝色系英雄，你们可是很好的伙伴呢",
                    "相克：随和的性格小心不要被红色系的英雄得寸进尺哦"
                ],
                "red" : [
                    "红色系的你直率乐观，善于表达自己，是人群中的小太阳",
                    "相生：绿色系的英雄可是你们忠实的观众哦",
                    "相克：你率真的性格很容易被不按套路出牌的蓝色系英雄克制"

                ],
                "blue" : [
                    "蓝色系的你擅长曲线救国，一般人猜不透你的心思",
                    "相生：红色系的英雄在你面前简直太小儿科了",
                    "相克：绿色系温吞水的性格你有时候真的拿他们没办法"
                ]
            }
        };

        moduleContainer.html( template("T:开始测试", { questions : dataJson.questions}) );

        $(document).on("click", ".js-click-option", function(){
            var $this = $(this);
            var $thisIndex = $this.attr("data-index");
            var $thisSiblings = $this.siblings(".check-current");

            if($this.hasClass("check-current")){
                return false;
            }

            if($thisSiblings.length > 0){
                if($thisSiblings.attr("data-index") == "1" && recordReply.green > 0){
                    recordReply.green--;
                }
                if($thisSiblings.attr("data-index") == "2" && recordReply.red > 0){
                    recordReply.red--;
                }
                if($thisSiblings.attr("data-index") == "3" && recordReply.blue > 0){
                    recordReply.blue--;
                }
            }

            $this.addClass("check-current").siblings().removeClass('check-current');

            if($thisIndex == "1"){
                recordReply.green++;
            }

            if($thisIndex == "2"){
                recordReply.red++;
            }

            if($thisIndex == "3"){
                recordReply.blue++;
            }
        });

        $(document).on("click", ".js-click-submit", function(){
            var $this = $(this);
            var color = "";
            if( (recordReply.green + recordReply.blue + recordReply.red) < 6){
                $17.alert("所有测试题必须选择一个答案！");
                return false
            }

            $17.tongji("学生端-进击三国-测一测提交");

            if(recordReply.blue >= recordReply.green && recordReply.blue >= recordReply.red){
                color = "blue";
            }else{
                if(recordReply.red >= recordReply.green){
                    color = "red";
                }else{
                    color = "green";
                }
            }

            $(".left-carton").css({left: "-201px"});

            moduleContainer.html( template("T:测试结果", { result : dataJson.result, color : color}) );
        });
    });
</script>
<#--template module-->
<script type="text/html" id="T:开始测试">
    <#--开始测试-->
    <div class="answer-check-box">
        <h2 class="text-back title-text-4" style="margin: 0 auto;"></h2>
        <div class="check-flag-box">
            <%for(var i = 0, len = questions.length; i < len; i++){%>
            <dl>
                <dt><span class="flag"><%=questions[i].num%></span></dt>
                <dd>
                    <h3><%=questions[i].title%></h3>
                    <div class="option-list">
                        <%for(var r = 0, reply = questions[i].reply; r < reply.length; r++){%>
                        <p class="js-click-option" data-index="<%=(r+1)%>" data-col="col-<%=(i+1)%>"><span class="check"></span><%=reply[r]%></p>
                        <%}%>
                    </div>
                </dd>
            </dl>
            <%}%>
        </div>
        <div style="text-align: center; padding-right: 50px;"><a href="javascript:void(0);" class="s-btn s-btn-orange js-click-submit"></a></div>
    </div>
</script>
<script type="text/html" id="T:测试结果">
    <#--测试结果-->
    <div class="answer-result-box">
        <h2 class="text-back title-text-5-<%=color%>"></h2>
        <div class="Conclusion">
            <ul>
                <%for(var i = 0, result = result[color]; i < result.length; i++){%>
                    <li><%=result[i]%></li>
                <%}%>
            </ul>
        </div>
        <div class="time-start text-back title-text-6"></div>
    </div>
</script>
</@temp.page>