<#import "../../../layout/project.module.student.ftl" as temp />
<@temp.page title="爱儿优" header="show">
<@app.css href="public/skin/project/iandyou/skin.css" />
<div class="main">
    <div class="head">
        <div class="inner">
            <#--<a style="color: #522120; font-size: 16px; display: block; margin: 25px 0 0 40px;" href="javascript:void (0);">返回首页</a>-->
        </div>
    </div>
    <div class="content_one_box">
        <div class="inner">
            <div id="answerSheetInfo" style="display: none;"><#--答题板--></div>
            <div id="answerSheet" style="display: none;"><#--答题板--></div>
            <#--<a class="mathNet-but" href="/student/activity/iandyou/experience.vpage" target="_blank"></a>-->
            <a class="mathNet-but" id="startAnswerBtn" href="javascript:void(0);"></a>
        </div>
    </div>
    <div class="content_two_box">
        <div class="inner"></div>
    </div>
    <div class="content_three_box">
        <div class="inner"></div>
    </div>
    <div class="content_four_box">
        <div class="inner">
            <a class="mathNet-but-star" href="/apps/afenti/order/iandyou-cart.vpage?ref=testActivity" target="_blank"></a>
        </div>
    </div>
</div>
<script type="text/html" id="T:答题板">
    <div class="math-containTest-box">
        <div class="title">
            嗨！小朋友你的数学知识掌握的怎么样？三题答对两题即可获得：
            <span class="math-text-back"></span>
        </div>
        <div class="question">
            <p>
                <span class="formula"><%=current.title%></span>
                <span class="equal">=</span>
                <span class="result">
                    <input placeholder="请输入答案" id="answerContentInt">
                </span>
            </p>
            <p class="laodao-back">
                <span class="time"><span id="recordDateTime"><%=recordDateTime%></span>秒</span>
                <a class="btn v-submit-answer" href="javascript:void (0);"></a>
            </p>
        </div>
    </div>
</script>
<script type="text/html" id="T:答题错误">
    <div class="math-test-alert">
        <a class="pop-close v-close-info" href="javascript:void(0);"></a>
        <div class="pop-try">
            <div  class="title">
                <p class="resolve"><%=current.title%> = <%=current.answer%></p>
                <%if( result == 'firstOpen'){%>
                <p>快去叫爸爸妈妈帮助答题，提升正确率吧！</p>
                <%}else{%>
                <p>开通爱儿优学好数学不再死背公式</p>
                <%}%>
            </div>
            <div class="but">
                <%if( result == 'firstOpen'){%>
                <a class="pop-green-btn v-close-result" href="javascript:void (0);">再试一次</a>
                <%}else{%>
                <a class="pop-green-btn v-next-answer" href="javascript:void (0);">下一题</a>
                <a class="pop-yellow-btn" href="/apps/afenti/order/iandyou-cart.vpage?ref=testActivity-popup" target="_blank">开通爱儿优</a>
                <%}%>
            </div>
        </div>
    </div>
</script>
<script type="text/html" id="T:答题正确">
    <div class="math-test-alert">
        <a class="pop-close v-close-info" href="javascript:void(0);"></a>
        <div class="pop-succeed">
            <div class="pop-count"><%=count%></div>
            <a class="v-next-answer" href="javascript:void(0);"></a>
        </div>
    </div>
</script>
<script type="text/html" id="T:最后结果">
    <div class="math-test-alert">
        <a class="pop-close v-close-result" href="javascript:void(0);"></a>
        <%if(result == "success"){%>
            <div class="pop-order">
                <a href="/student/center/order.vpage?ref=testActivity-popup"></a>
            </div>
        <%}else{%>
            <div class="pop-failure">
                <a href="/apps/afenti/order/iandyou-cart.vpage?ref=testActivity-popup" target="_blank"></a>
            </div>
        <%}%>
    </div>
</script>
<script type="text/javascript">
    $(function(){
        var allItemBank = [
            {title : "加数+加数", answer : "和"},
            {title : "被减数–减数", answer : "差"},
            {title : "每份数×份数", answer : "总数"},
            {title : "速度×时间", answer : "路程"},
            {title : "单价×数量", answer : "总价"},
            {title : "工作效率×工作时间", answer : "工作总量"},
            {title : "乘数×乘数", answer : "积"},
            {title : "被除数÷除数", answer : "商"},
            {title : "正方形：边长×4", answer : "周长"},
            {title : "正方形：边长×边长", answer : "面积"},
            {title : "长方形：(长+宽)×2", answer : "周长"},
            {title : "长方形：长×宽", answer : "面积"},
            {title : "正方体：棱长×棱长×6", answer : "表面积"},
            {title : "正方体：棱长×棱长×棱长", answer : "体积"},
            {title : "长方体：(长×宽+长×高+宽×高)×2", answer : "表面积"},
            {title : "长方体：长×宽×高", answer : "体积"},
            {title : "三角形：底×高÷2", answer : "面积"},
            {title : "三角形：面积×2÷底", answer : "高"},
            {title : "三角形：面积×2÷高", answer : "底"},
            {title : "平行四边形：底×高", answer : "面积"},
            {title : "梯形：(上底+下底)×高÷2", answer : "面积"},
            {title : "圆形：2×π×半径", answer : "周长"},
            {title : "圆形：半径×半径×π", answer : "面积"},
            {title : "圆柱体：底面周长×高", answer : "侧面积"},
            {title : "圆柱体：侧面积+底面积×2", answer : "表面积"},
            {title : "圆柱体：底面积×高", answer : "体积"},
            {title : "圆柱体：侧面积÷2×半径", answer : "体积"},
            {title : "圆锥体：底面积×高÷3", answer : "体积"}
        ];

        var recordTempDate = [];
        var recordCurrentIndex = 0;
        var recordAnswerCount = 0; //记录答题
        var recordGetsCount = false;
        var recordDateTime = 30;
        var recordCountDown;

        function iandyouFirstOpen(){
            $.get("/campaign/iandyoufirstopen.vpage", {}, function(data){
                if(data.success){
                    recordGetsCount = data.participated;
                }
            });
        }

        //关闭提示
        $("#answerSheetInfo").on("click", ".v-close-result", function(){
            recordAnswerCount = 0;

            iandyouFirstOpen();
            $("#answerSheetInfo").html("").hide();
            $("#answerSheet").hide().html("");
        });

        $("#answerSheetInfo").on("click", ".v-close-info", function(){
            getAnswerTest();

            $("#answerSheetInfo").html("").hide();
            $("#answerSheet").show().html( template("T:答题板", { current : allItemBank[recordCurrentIndex], recordDateTime : recordDateTime}) );
        }).on("click", ".v-next-answer", function(){
            getAnswerTest();

            $("#answerSheetInfo").html("").hide();
            $("#answerSheet").show().html( template("T:答题板", { current : allItemBank[recordCurrentIndex], recordDateTime : recordDateTime}) );
        });

        $("#answerSheet").on("click", ".v-submit-answer", function(){
            var $Int = $("#answerContentInt").val();

            if($17.isBlank($Int)){
                return false;
            }

            clearIntervalCount();

            if(recordAnswerCount >= 2){
                lastResult($Int);
                return false;
            }

            if(allItemBank[recordCurrentIndex].answer == $Int){
                recordTempDate.push(allItemBank[recordCurrentIndex]);
                allItemBank.splice(recordCurrentIndex, 1);

                $("#answerSheetInfo").show().html( template("T:答题正确", { count : recordTempDate.length}) );
            }else{
                $("#answerSheetInfo").show().html( template("T:答题错误", { current : allItemBank[recordCurrentIndex]}) );
            }

            recordAnswerCount++;
        });

        $("#startAnswerBtn").on("click", function(){
            recordTempDate = [];

            if(!recordGetsCount){
                $.get("/campaign/iandyoufirstopen.vpage", {}, function(data){
                    if(data.success){
                        recordGetsCount = data.participated;
                        if(!recordGetsCount){
                            getAnswerTest();
                            $("#answerSheet").show().html( template("T:答题板", { current : allItemBank[recordCurrentIndex], recordDateTime : recordDateTime}) );
                        }else{
                            $17.alert("你已经参加过测试！<a href='/student/apps/index.vpage?app_key=iandyou100' class='w-blue'>赶紧去体验吧！</a>");
                        }
                    }
                });
            }else{
                $17.alert("你已经参加过测试！<a href='/student/apps/index.vpage?app_key=iandyou100' class='w-blue'>赶紧去体验吧！</a>");
            }
        });


        function lastResult($Int){
            if(allItemBank[recordCurrentIndex].answer == $Int){
                recordTempDate.push(allItemBank[recordCurrentIndex]);
//                    allItemBank.splice(recordCurrentIndex, 1);
            }

            if(recordTempDate.length >= 2){
                //success
                $("#answerSheetInfo").show().html( template("T:最后结果", {result : "success"}) );

                $.get("/campaign/iandyousubmit.vpage", {answerRight : true}, function(data){});
            }else{
                //error

                $.get("/campaign/iandyousubmit.vpage", {answerRight : false}, function(){
                    $.get("/campaign/iandyoufirstopen.vpage", {}, function(data){
                        if(data.success){
                            recordGetsCount = data.participated;

                            if(!recordGetsCount){
                                $("#answerSheetInfo").show().html( template("T:答题错误", {current : allItemBank[recordCurrentIndex], result : "firstOpen"}) );
                            }else{
                                $("#answerSheetInfo").show().html( template("T:最后结果", {result : "error"}) );
                            }
                        }
                    });
                });
            }
        }

        function getAnswerTest(){
            recordCurrentIndex = Math.floor(Math.random() * allItemBank.length);
            clearIntervalCount();
            recordCountDown = setInterval(setCountDown, 1000);
        }

        function setCountDown(){
            if(recordDateTime <= 0){
                clearIntervalCount();

                if(recordAnswerCount >= 2){
                    lastResult('');
                    return false;
                }else{
                    $("#answerSheetInfo").show().html( template("T:答题错误", { current : allItemBank[recordCurrentIndex]}) );
                }
                recordAnswerCount++;
            }else{
                recordDateTime--;
                $("#recordDateTime").text(recordDateTime);
            }
        }

        function clearIntervalCount(){
            recordDateTime = 30;
            clearInterval(recordCountDown);
        }
    });
</script>
</@temp.page>