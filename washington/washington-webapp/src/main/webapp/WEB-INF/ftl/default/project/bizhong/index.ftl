<#--例：layout 1 -->
<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
fastClickFlag=false
title="一起作业年会大抽奖"
pageJs=['jquery', 'weui', 'template']
pageCssFile={"css" : ["public/skin/project/bizhong/css/skin"]}
>
<div class="cardWrap">
    <div id="ListContent"></div>
</div>

<div>
    <div class="item-btn JS-startReward" title="开始抽奖"></div>
</div>

<script type="text/html" id="T:LIST">
    <div class="cardList">
        <#list 1..10 as col>
            <div class="cardBox cardBox${col}">
                <ul style="position: relative;">
                    <#list 1..20 as row>
                        <li class="card">
                            <div class="icon"></div>
                            <%if(luckyMans){%>
                                <div class="txt JS-workId" data-workid="<%=luckyMans[${col_index}].workNo%>">
                                    <p class="name" style="white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"><%=(luckyMans[${col_index}].userName)%></p>
                                    <p class="num">NO.<%=PrefixInteger((luckyMans[${col_index}].workNo), 5)%></p>
                                </div>
                            <%}%>
                        </li>
                    </#list>
                </ul>
            </div>
        </#list>
    </div>
</script>
<script type="text/javascript">
    signRunScript = function ($) {
        var _resultItems = {
            target1 : '.cardBox1 ul',
            target2 : '.cardBox2 ul',
            target3 : '.cardBox3 ul',
            target4 : '.cardBox4 ul',
            target5 : '.cardBox5 ul',
            target6 : '.cardBox6 ul',
            target7 : '.cardBox7 ul',
            target8 : '.cardBox8 ul',
            target9 : '.cardBox9 ul',
            target10 : '.cardBox10 ul'
        };

        var keyJSON = {
            49: 1,
            50: 2,
            51: 3,
            52: 4,
            53: 5,
            54: 6,
            55: 7,
            56: 8,
            57: 9,
            48: 10
        };

        var currentStatus = 'def';//状态
        var enterKeyRecord = 0;
        var startEnterEffect;
        var prizeHeight = 268;

        $("#ListContent").html( template("T:LIST", { }) );

        $(document).ready(function(){
            $(this).keydown(function(e){
                var eKey = e.keyCode || e.which;

                switch (eKey){
                    case 13:
                        startReward();
                        break;
                    default:
                        if(keyJSON[eKey]){
                            if(currentStatus == 'end'){
                                SpinEffect(keyJSON[eKey]);
                            }else{
                                startInfo();
                            }
                        }
                }
            });
        });

        for(var c = 1; c <= 10; c++){
            $(document).on('click', _resultItems['target' + c], function(){
                if(currentStatus == 'end'){ SpinEffect($(this).parent().index() + 1); }else{startInfo();}
            });
        }

        $(document).on('click', '.JS-startReward', function(){
            startReward();
        });

        function startInfo(){
            if(currentStatus == 'def'){
                $.alert("请按回车开始抽奖");
            }
        }

        function startReward(){
            if(currentStatus == 'start'){
                if(enterKeyRecord == 1){
                    enterKeyRecord = 0;

                    clearInterval(startEnterEffect);

                    lotteryGoTo(_resultItems, function(){
                        currentStatus = 'end';
                        $(".JS-startReward").removeClass("item-btn-active");
                    });
                }
                return false;
            }

            $.get("/project/loadluckymans.vpage", {}, function(data){
                if(data.success){
                    if(data.luckyMans){
                        data.luckyMans = data.luckyMans.concat(data.luckyMans);
                        $("#ListContent").html( template("T:LIST", {luckyMans: data.luckyMans, PrefixInteger: PrefixInteger }) );
                        ScrollEffect();
                        startEnterEffect = setInterval(ScrollEffect, 1000);
                        currentStatus = 'start';
                        enterKeyRecord = 1;
                        $(".JS-startReward").addClass("item-btn-active");
                    }
                }else{
                    $.alert(data.info);
                }
            });
        }
        function PrefixInteger(num, n) {
            return (Array(n).join(0) + num).slice(-n);
        }

        function SpinEffect(index){
            var idx = $('.cardBox' + (index) + ' ul');

            if(idx.find('li:first').hasClass('wined')){
                //content
                console.info("已经抽中");
            }else{
                idx.find('li').addClass('fliPed');

                setTimeout(function(){
                    idx.removeAttr('style');
                    idx.find('li').removeClass('fliPed').addClass('wined');
                }, 500);

                var workNoId = idx.find('li:first .JS-workId').attr('data-workid');

                $.post("/project/bingo.vpage", {
                    workNo: workNoId
                }, function(data){
                    if(data.success){
                        //console.info('恭喜您获奖');
                    }else{
                        $.alert(data.info);
                    }
                });
            }
        }

        //停止转动效果
        function lotteryGoTo(items, callback){
            var animateCount = 10;

            for(var i = 1; i <= 10; i++){
                if(i == 10){
                    $(items.target10).animate({"top":"-" + animateCount * prizeHeight}, 500, "linear", function () {
                        $(this).css("top",0).animate({"top":"-" + Math.ceil(Math.random()*5) * prizeHeight}, 300,"linear", function(){
                            if(callback){
                                callback();
                            }
                        });
                    });
                }else{
                    $(items['target' + i]).animate({"top":"-" + animateCount * prizeHeight}, 200 + i*20, "linear", function () {
                        $(this).css("top",0).animate({"top":"-" + Math.ceil(Math.random()*5) * prizeHeight}, 300,"linear");
                    });
                }
            }
        }

        function ScrollEffect(){
            var setObj = {
                line: 20,
                speed: 1000
            };

            for(var i = 1; i <= 10; i++){
                var _index = i;
                var idx = ('.cardBox' + _index + ' ul');

                $(idx).animate({"top":"-" + setObj.line * prizeHeight}, setObj.speed, "linear", function(){
                    $(this).css({top:0});

                    if(currentStatus == "stop"){
                        $(this).css("top",0).animate({"top":"-" + _index * prizeHeight}, setObj.speed,"linear");
                    }
                });
            }
        }
    };
</script>
</@layout.page>