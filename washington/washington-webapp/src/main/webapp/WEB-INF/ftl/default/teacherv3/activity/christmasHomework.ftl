<#if !(homework?has_content)!false>
    <div id="christmasRewardBox" style="padding: 0 5px 5px;"></div>
    <script type="text/html" id="T:christmasRewardBox">
        <style>
            .t-christmas-content{color:#4e5656;background: url(<@app.link href="public/skin/project/christmas/images/christmas-homework-bg.png"/>) no-repeat center bottom #ecf5f5 ; border-radius: 5px;  clear: both; height: 177px;}
            .t-christmas-content .title h1{font-size:16px;line-height: 35px;}
            .t-christmas-content .title p{font-size:14px;line-height: 30px;}
            .t-christmas-content .title {padding:5px 16px;}
            .t-christmas-content .column{text-align: center;_padding-top:10px;}
            .t-christmas-content .column .con{padding:0 75px 0 0;}
            .t-christmas-content .column p{display: inline;line-height: 45px;font-size:14px;}
            .t-christmas-content .info{color: #8bb4b4;font-size:14px;line-height: 45px;text-align: center;}
            .t-christmas-content .info .btn-rule{color: #189cfb;font-size:14px;line-height: 45px; text-decoration: underline;}
            .cur-tips-main { position: relative; display: inline-block;}
            .cur-tips-main .tips-box{ position: absolute; display: none; background: #fff; border: 1px solid #dae6ee; border-radius: 3px; width: 280px; line-height: 22px; font-size: 14px; color: #aab1b9; bottom: 48px; left: 0; text-align: left; padding: 20px;}
            .cur-tips-main .tips-box i{ display: block; width: 0; height: 0; line-height: 0; font-size: 0; border: 10px solid #ecf5f5; border-top-color: #fff; position: absolute; left: 20px; bottom: -20px;}
            .cur-tips-main:hover .tips-box{ display: block;}
        </style>
        <div class="t-christmas-content">
            <div class="title">
                <h1>作业奖励：</h1>
                <p>圣诞节来临，老师在作业／测验中将获得学豆装入圣诞袜，来奖励完成作业／测验的孩子。</p>
            </div>
            <div class="column">
                <p class="con">参与本次作业／测验的班级数：<%=clazzCount%>个</p>
                <p>给每班奖励学豆数：
                <span class="w-addSub-int" id="christmasRewardInput">
                    <a class="w-btn w-btn-mini <%if(numberCount.defCount <= 0){%>w-btn-disabled<%}%>" data-type="minus" href="javascript:void (0)" style="margin: 0;">-</a>
                    <input class="w-int" type="text" data-type="int" value="<%=numberCount.defCount%>" style="width: 40px;" maxlength="4" readonly="readonly">
                    <a class="w-btn w-btn-mini <%if(numberCount.maxCount <= numberCount.defCount){%>w-btn-disabled<%}%>" data-type="plus" href="javascript:void (0)">+</a>
                </span>
                </p>
            </div>
            <div class="info">
                完成作业的学生随机获得学豆。
                <a href="javascript:void(0)" class="cur-tips-main">
                    <span class="w-blue">详细规则</span>
                    <span class="tips-box" style="width: 380px; left: -150px;">
                        <b>规则：</b><br/>
                        1.完成作业／测验的学生都会获得圣诞袜。<br/>
                        2.每个圣诞袜内的学豆数量随机。<br/>
                        3.检查作业／测验后，将剩余未领取的学豆退回班级学豆。<br/>
                        4.奖励学豆优先使用班级剩余学豆，不足则使用园丁豆兑换。
                        <i style="left: 170px;"></i>
                    </span>
                </a>
            </div>
        </div>
    </script>
    <script type="text/javascript">
        (function($){
            var items = [];
            var numberCount = {
                defCount : 0,
                maxCount : 0
            };

            function christmasRewardInit(){
                items = LAC.clazzIds;
                if(items.join().length > 0){
                    $.post("/teacher/homework/maxic.vpage", {clazzIds : items.join()}, function(data){
                        numberCount.defCount = data.dc;//data.dc
                        numberCount.maxCount = data.mc;//data.mc

                        if(numberCount.defCount > 0) {
                            LAC.christmasReward = numberCount.defCount;//装入布置作业数据内

                            $("#christmasRewardBox").html(template("T:christmasRewardBox", {numberCount : numberCount, clazzCount : items.length}));
                        }
                    });
                }else{
                    $("#christmasRewardBox").html("");
                    LAC.christmasReward = 0;
                }
            }

            function init(){


                //监听布置作业下一步
                $(document).on("click", ".v-next-btn, .js-christmasSelectLevel .v-alltarget, .js-christmasSelectLevel .v-targets", function(){
                    setTimeout(function(){
                        christmasRewardInit();
                    }, 100);
                });

                //减学豆
                $(document).on("click", "#christmasRewardInput a[data-type='minus']", function(){
                    var $this = $(this);
                    var clazzNumBox = $this.siblings("input");

                    if($this.hasClass("w-btn-disabled")){
                        return false;
                    }

                    var clazzNum = parseInt(clazzNumBox.val()) - 1;

                    if(clazzNum <= 0 || !$17.isNumber(clazzNumBox.val()) ){
                        clazzNum = 0;
                        $this.addClass("w-btn-disabled");
                    }

                    clazzNumBox.val(clazzNum);
                    $this.siblings("a[data-type='plus']").removeClass("w-btn-disabled");

                    LAC.christmasReward = clazzNum;
                });

                //加学豆
                $(document).on("click", "#christmasRewardInput a[data-type='plus']", function(){
                    var $this = $(this);
                    var clazzNumBox = $this.siblings("input");

                    if($this.hasClass("w-btn-disabled")){
                        return false;
                    }

                    var clazzNum = parseInt(clazzNumBox.val()) + 1;

                    if(clazzNum >= numberCount.maxCount || !$17.isNumber(clazzNum) ){
                        clazzNum = numberCount.maxCount;
                        $this.addClass("w-btn-disabled");
                    }

                    clazzNumBox.val(clazzNum);
                    $this.siblings("a[data-type='minus']").removeClass("w-btn-disabled");

                    LAC.christmasReward = clazzNum;
                });

                //输入个数
                $(document).on("blur", "#christmasRewardInput input[data-type='int']", function(){
                    var $this = $(this);
                    var currentCount = parseInt($this.val());

                    $this.siblings("a[data-type='minus']").removeClass("w-btn-disabled");
                    $this.siblings("a[data-type='plus']").removeClass("w-btn-disabled");

                    if(currentCount < 1 || !$17.isNumber(currentCount)){
                        currentCount = 0;
                        $this.siblings("a[data-type='minus']").addClass("w-btn-disabled");
                    }else if(currentCount >= numberCount.maxCount){
                        currentCount = numberCount.defCount;
                    }

                    $this.val(currentCount);

                    LAC.christmasReward = currentCount;
                });
            }

            init();
            $.extend({
                christmasRewardInit : christmasRewardInit
            });
        }($));
    </script>
</#if>