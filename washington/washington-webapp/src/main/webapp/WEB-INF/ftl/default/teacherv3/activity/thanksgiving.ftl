<#import "../../layout/project.module.ftl" as temp />
<@temp.page header="show">
    <@sugar.capsule js=["DD_belatedPNG"] css=[] />
    <@app.css href="public/skin/project/thanks2015/teacher/skin.css" />
    <!--//start-->
    <div class="thanksgiving-tea">
        <div class="bgs">
            <div class="bgs01"></div>
            <div class="bgs02"></div>
        </div>
        <div class="main">
            <div class="title">
                <div class="mnlist">
                    <span>活动时间：11月25日—12月8日</span>
                    <p>感恩节到了，学生们的进步成长，就是对老师辛勤培养最好的感恩！</p>
                    <h2>学生们为自己设置了目标，他们完成了吗？</h2>
                </div>
            </div>
            <div class="stulist">
                <div class="mnlist">
                    <div class="b-hd">
                        <select class="js-changeSelectClazz">
                            <option>请选择班级</option>
                            <#if (clazzInfo?size gt 0)!false>
                            <#list clazzInfo as cl>
                                <option value="${cl.clazzId!}" <#if cl_index == 0>selected="selected"</#if>>${cl.clazzName!}</option>
                            </#list>
                            </#if>
                        </select>
                    </div>
                    <div id="template_studentsList"></div>
                </div>
            </div>
            <div class="rule">
                <div class="mnlist">
                    <h2>活动规则：</h2>
                    <div class="info">
                        <p>1、活动时间：11月25日——12月8日</p>
                        <p>2、活动期间，同学们每天都会选择一项要努力的方向</p>
                        <p>3、我们会告诉老师，每位同学的选择</p>
                        <p>4、当老师认为哪位同学达标，将奖励该同学一只“火鸡”<i class="turkey-icon-s"></i></p>
                        <p>5、老师根据同学们的表现，还可以奖励学豆<i class="gold-icon-s"></i></p>
                    </div>
                </div>
            </div>
            <div class="aim">
                <div class="bg03"></div>
                <div class="mnlist">
                    <h2>达成目标：</h2>
                    <div id="template_goalAchieved"></div>
                </div>
            </div>
        </div>
    </div>
    <!--end//-->

    <script type="text/html" id="T:studentsList">
        <div class="b-mn">
            <ul class="tit">
                <li>
                    <p><span>学生</span><span class="bor">目标</span></p>
                </li>
                <li class="odd">
                    <p><span>学生</span><span class="bor">目标</span></p>
                </li>
            </ul>
            <ul class="info">
                <%if(targets.length > 0){%>
                    <%for(var i = 0; i < targets.length; i++){%>
                    <li class="<%if(i%2 == 1){%>odd<%}%> <%if(i%4 == 2 || i%4 == 3){%>back<%}%>" >
                        <p><span><%=targets[i].studentName%>&nbsp;</span><span class="bor"><%=targets[i].target%>&nbsp;</span></p>
                    </li>
                    <%}%>
                <%}else{%>
                    <li style="float: left; width: 98%; border: none;"><div style="text-align: center; padding: 40px 0;">暂无数据</div></li>
                <%}%>
            </ul>
        </div>
        <%if(targets.length >= 0){%>
        <a href="javascript:void(0)" class="award-btn js-clickRewardStudents">奖励该班学生</a>
        <%}%>
    </script>

    <script type="text/html" id="T:达成目标">
        <div class="info-box">
            <ul class="tit">
                <li><span>学生</span><span>获得<i class="turkey-icon"></i></span><span>获得奖励<i class="gold-icon"></i></span></li>
            </ul>
            <ul class="info">
                <%if(rankInfo.length > 0){%>
                    <%for(var i = 0; i < rankInfo.length; i++){%>
                        <li><span><%=rankInfo[i].studentName%>&nbsp;</span><span><%=rankInfo[i].chickenCount%>&nbsp;</span><span><%=rankInfo[i].coinCount%>&nbsp;</span></li>
                    <%}%>
                <%}else{%>
                    <li style="float: left; width: 100%;"><div style="text-align: center; padding: 40px 0;">暂无数据</div></li>
                <%}%>
            </ul>
        </div>
    </script>

    <script type="text/html" id="T:选择要奖励的学生">
        <div class="thank-dialog thank-dialog01">
            <div class="hd"><p>选择要奖励的学生</p></div>
            <div class="mn">
                <%var keyArray = ['积极发言', '团结友爱', '热爱劳动', '遵守纪律']%>
                <div class="tab">
                    <ul>
                        <%for(var i = 0; i < keyArray.length; i++){%>
                        <li data-key="<%=keyArray[i]%>" class="js-clickTab">
                            <%=keyArray[i]%>
                        </li>
                        <%}%>
                    </ul>
                </div>
                <div class="list js-tabContent">
                    <%for(var i = 0; i < keyArray.length; i++){%>
                        <ul data-key="<%=keyArray[i]%>" style="display: <%=( i == 0 ? 'block' : 'none')%>">
                            <%if(dataMap[keyArray[i]] && dataMap[keyArray[i]].length > 0){%>
                                <%var students = dataMap[keyArray[i]]%>
                                <%for(var s = 0, len = students.length; s < len; s++){%>
                                <li data-studentid="<%=students[s].studentId%>">
                                    <div class="avatar">
                                        <i class="back"></i>
                                        <%if(students[s].img){%>
                                        <img src="<@app.avatar href='<%=students[s].img%>'/>"/>
                                        <%}else{%>
                                        <img src="<@app.avatar href=''/>"/>
                                        <%}%>
                                    </div>
                                    <%if(students[s].ischicken){%><span class="chicken"></span><%}%>
                                    <p><%=students[s].studentName%></p>
                                </li>
                                <%}%>
                            <%}else{%>
                                <li style="float: left; width: 100%; text-align: center; line-height: 127px; height: 127px; margin: 0;">暂无数据</li>
                            <%}%>
                        </ul>
                    <%}%>
                </div>
                <div id="alertInfo"></div>
                <div class="button">
                    <a href="javascript:void(0)" class="jl-btn jl-btn-green js-determineRewardType" data-type="CHICKEN">奖励火鸡</a>
                    <a href="javascript:void(0)" class="jl-btn jl-btn-orange js-determineRewardType"  data-type="COIN">奖励学豆</a>
                </div>
            </div>
            <div class="bot"><p>学生们达成了目标，奖励他们火鸡或学豆吧！</p></div>
        </div>
    </script>

    <script type="text/html" id="T:给选中的学生发学豆">
        <div class="thank-dialog thank-dialog02">
            <div class="hd"><p>给选中的学生发学豆</p></div>
            <div class="mn">
                <p class="tit">奖励<span class="v-studentCount"><%=studentLen.length%></span>名同学每人<span class="v-studentCount1">5</span>学豆，共 <span class="v-studentCount2"><%=(studentLen.length * 5)%></span>学豆</p>

                <div class="w-addSub-int num">
                    <a class="v-minus-btn l" href="javascript:void (0)"></a>
                    <input class="v-count-int" type="text" value="5" maxlength="5"/>
                    <a class="v-plus-btn r" href="javascript:void (0)"></a>
                </div>
                <div id="alertInfo"></div>
                <p class="button"><a href="javascript:void(0)" class="jl-btn jl-btn-orange js-sendReward" data-type="<%=rewardType%>">确定</a></p>
            </div>
            <div class="bot"><p>当前班级学豆有<%=crrCount%>个<span class="v-hideFlag" style="<%if(crrCount > (studentLen.length * 5)){%>display: none;<%}%>">，奖励将消耗您<span class="v-botCount1">1</span>园丁豆兑换<span class="v-botCount2">5</span>学豆，剩余<span class="v-botCount3">0</span>学豆将自动存入班级学豆</span></p></div>
        </div>
    </script>

    <script type="text/html" id="T:奖励成功">
        <div class="thank-dialog thank-dialog03">
            <%if(rewardType == "CHICKEN"){%>
                <img src="<@app.link href="public/skin/project/thanks2015/teacher/jl-success.png"/>" />
                <p class="intro">还可以奖励学生学豆哦~</p>
            <%}%>
            <%if(rewardType == "COIN"){%>
                <img src="<@app.link href="public/skin/project/thanks2015/teacher/jl-success-gold.png"/>" />
            <%}%>
            <p class="button"><a href="javascript:void(0)" class="jl-btn jl-btn-orange js-rewardClose">确定</a></p>
        </div>
    </script>
    <#--js script-->
<script type="text/javascript">
    $(function(){
        var currentClazzId = 0;
        var currentSelectStudents = [];
        var currentIntegralCount = 0;//当前班级学豆有

        //load list
        function getClazzInfoList($id){
            $.post("getclazzthanksinfo.vpage", { clazzId : $id}, function(data){
                $("#template_studentsList").html( template("T:studentsList", {targets : data.targets}) );
                $("#template_goalAchieved").html( template("T:达成目标", {rankInfo : data.rankInfo}) );
            });
        }

        function alertInfo(content){
            if(content){
                $("#alertInfo").text(content).show();

                setTimeout(function(){
                    $("#alertInfo").slideUp(function(){
                        $(this).text("");
                    });
                }, 2000);
            }
        }

        //select clazz
        $(document).on("change", ".js-changeSelectClazz", function(){
            var $this = $(this);
            currentClazzId = $this.val();

            if( $17.isBlank(currentClazzId) ){
                currentClazzId = 0
            }

            getClazzInfoList(currentClazzId);

        });

        //初始 clazz
        $(".js-changeSelectClazz").change();

        //选择奖励学生
        $(document).on("click", ".js-clickRewardStudents", function(){
            var $this = $(this);

            if( $17.isBlank(currentClazzId) ){
                $17.alert("班级ID为空");
                return false;
            }

            currentSelectStudents = [];
            $.post("getthanksrewardinfo.vpage", {clazzId : currentClazzId}, function(data){
                if(data.success){
                    $.prompt(template("T:选择要奖励的学生", {dataMap : data.dataMap}),{
                        prefix : "selectReward-popup",
                        buttons : {},
                        classes : {
                            fade: 'jqifade'
                        },
                        loaded : function(){
                            $(".js-clickTab:first").click();
                        }
                    });

                    currentIntegralCount = data.integralCount;
                }else{
                    $17.alert(data.info);
                }
            });
        });

        //tab
        $(document).on("click", ".js-clickTab", function(){
            var $this = $(this);
            var $key = $this.attr("data-key");

            if($this.hasClass("active")){
                return false;
            }

            currentSelectStudents = [];
            $this.addClass("active").siblings().removeClass("active");
            $(".js-tabContent ul[data-key='"+ $key +"']").show().siblings().hide();
            $(".js-tabContent li").removeClass("active");

            if( !$17.isBlank($key) ){
                $(".js-tabContent ul[data-key='"+ $key +"'] li").each(function(){
                    $(this).click();
                });
            }
        });

        //select student
        $(document).on("click", ".js-tabContent li", function(){
            var $this = $(this);
            var $studentId = $this.attr("data-studentid");

            if( $this.hasClass("active") ){
                $this.removeClass("active");

                currentSelectStudents.splice($.inArray($studentId, currentSelectStudents), 1);
            }else{
                $this.addClass("active");
                currentSelectStudents.push($studentId);
            }
        });

        //确定奖励类型
        $(document).on("click", ".js-determineRewardType", function(){
            var $this = $(this);
            var $type = $this.attr("data-type");

            if( currentSelectStudents.length < 1){
                alertInfo("请选择学生");
                return false;
            }

            //奖励火鸡
            if($type == "CHICKEN"){
                App.postJSON("thanksrewardstudent.vpage", {
                    rewardType : $type,
                    clazzId : currentClazzId,
                    userIds : currentSelectStudents.join(),
                    source : "teacherPC"
                }, function(data){
                    if(data.success){
                        $.prompt(template("T:奖励成功", {rewardType : $type, source : "teacherPC"}),{
                            prefix : "rewardSuccess-popup",
                            buttons : {},
                            loaded : function(){
                                //确定
                                $(document).on("click", ".js-rewardClose", function(){
                                    $.prompt.close();
                                });
                            },
                            classes : {
                                fade: 'jqifade'
                            }
                        });
                    }else{
                        $17.alert(data.info);
                    }
                });
            }else{
                $.prompt(template("T:给选中的学生发学豆", {rewardType : $type, studentLen : currentSelectStudents, crrCount : currentIntegralCount}),{
                    prefix : "selectReward-popup",
                    buttons : {},
                    classes : {
                        fade: 'jqifade'
                    },
                    loaded: function(){
                        studentCount(currentSelectStudents, 5);
                    }
                });
            }
        });

        //发奖励
        $(document).on("click", ".js-sendReward", function(){
            var $this = $(this);
            var $type = $this.attr("data-type");

            if($this.hasClass("dis")){
                return false;
            }

            if( $17.isBlank(currentClazzId) ){
                $17.alert("班级ID为空");
                return false;
            }

            $this.addClass("dis");
            App.postJSON("thanksrewardstudent.vpage", {
                rewardType : $type,
                clazzId : currentClazzId,
                userIds : currentSelectStudents.join(),
                integralCnt : $(".v-count-int").val(),
                source : "teacherPC"
            }, function(data){
                if(data.success){
                    $.prompt(template("T:奖励成功", {rewardType : $type}),{
                        prefix : "rewardSuccess-popup",
                        buttons : {},
                        loaded : function(){
                            //确定
                            $(document).on("click", ".js-rewardClose", function(){
                                $.prompt.close();
                            });
                        },
                        classes : {
                            fade: 'jqifade'
                        }
                    });
                }else{
                    $17.alert(data.info);
                }
                $this.removeClass("dis");
            });
        });

        //减学豆
        $(document).on('click', '.v-minus-btn', function(){
            var $this = $(this);
            var clazzNumBox = $this.siblings("input");

            if($this.hasClass("w-btn-disabled")){
                return false;
            }

            var clazzNum = parseInt(clazzNumBox.val()) - 1;

            if(clazzNum < 1 || !$17.isNumber(clazzNumBox.val()) ){
                clazzNum = 1;
                $this.addClass("w-btn-disabled");
            }

            clazzNumBox.val(clazzNum);
            $this.siblings(".v-plus-btn").removeClass("w-btn-disabled");
            studentCount(currentSelectStudents, $(".v-count-int").val());
        });

        //加学豆
        $(document).on('click', '.v-plus-btn', function(){
            var $this = $(this);
            var clazzNumBox = $this.siblings("input");

            if($this.hasClass("w-btn-disabled")){
                return false;
            }

            var clazzNum = parseInt(clazzNumBox.val()) + 1;

            if(clazzNum >= 100 || !$17.isNumber(clazzNumBox.val()) ){
                clazzNum = 100;
                $this.addClass("w-btn-disabled");
            }

            clazzNumBox.val(clazzNum);
            $this.siblings(".v-minus-btn").removeClass("w-btn-disabled");

            studentCount(currentSelectStudents, $(".v-count-int").val());
        });

        //输入个数
        $(document).on('blur', '.v-count-int', function(){
            var $this = $(this);
            var currentCount = parseInt($this.val());

            $this.siblings(".v-minus-btn").removeClass("w-btn-disabled");
            $this.siblings(".v-plus-btn").removeClass("w-btn-disabled");

            if(currentCount < 1 || !$17.isNumber(currentCount)){
                $this.val(1);
                $this.siblings(".v-minus-btn").addClass("w-btn-disabled");
            }else if(currentCount >= 100){
                $this.val(100);
                $this.siblings(".v-plus-btn").addClass("w-btn-disabled");
            }

            studentCount(currentSelectStudents, $(".v-count-int").val());
        });

        function studentCount(count, beans){
            $(".v-studentCount").text(count.length);
            $(".v-studentCount1").text(beans);
            $(".v-studentCount2").text(beans * count.length);

            if(currentIntegralCount < count.length * beans){
                $(".v-hideFlag").show();

                var $gold = Math.ceil( (count.length * beans - currentIntegralCount)/5 );

                $(".v-botCount1").text( $gold );
                $(".v-botCount2").text( $gold * 5 );
                $(".v-botCount3").text( $gold * 5 - (count.length * beans - currentIntegralCount) );
            }else{
                $(".v-hideFlag").hide();
            }
        }
    });
</script>
</@temp.page>