<script type="text/javascript">
    (function($){
        $(document).on("click", ".notClassJoinMethod-popup", function(){
            notClassJoinMethod("default", $(this).attr("data-link"));
        });

        function notClassJoinMethod(type, link){
            var $clazzLevel = null;
            var $promptProperty = {
                focus : type == "usa" && type ? 0 : 1,
                title : type == "usa" && type ? "解锁神秘乐园" : "解锁任务",
                buttons : type == "usa" && type ? {} : {"取消": false, "确定": true },
                close : type == "usa" && type ? "w-hide" : ""
            };

            $.prompt(template("T:notClazzJoinPopup", {showFlag : type}), {
                title: $promptProperty.title,
                focus : $promptProperty.focus,
                buttons: $promptProperty.buttons,
                position: {width: 550},
                classes : {
                    close: $promptProperty.close
                },
                submit : function(e, v){
                    if(v){
                        var notClazzJoinPopupContent = $("#notClazzJoinPopupContent");

                        if($clazzLevel == null || $clazzLevel < 0 || $clazzLevel > 6){
                            notClazzJoinPopupContent.find(".init").show();
                            return false;
                        }

                        $.post("/student/clazz/joinvclazz.vpage", { clazzLevel : $clazzLevel}, function(data){
                            if(data.success){
                                location.href = link ? link : "/";
                            }else{
                                $17.alert(data.info);
                            }
                        });
                    }
                },
                loaded : function(){
                    var homeNotClazzJoinBtn = $("#homeNotClazzJoinBtn");
                    var notClazzJoinPopupContent = $("#notClazzJoinPopupContent");
                    notClazzJoinPopupContent.on("click", ".ate li", function(){
                        var $this = $(this);

                        notClazzJoinPopupContent.find(".init").hide();
                        $this.addClass("active").siblings().removeClass("active");
                        $clazzLevel = parseInt($this.attr("data-value"));

                        homeNotClazzJoinBtn.show();
                    });

                    //首页弹框，点击进入游戏
                    homeNotClazzJoinBtn.on("click", "a", function(){
                        var $this = $(this);
                        var notClazzJoinPopupContent = $("#notClazzJoinPopupContent");
                        var $thisLink = $(this).attr("data-link");

                        if($this.hasClass("dis")){
                            return false;
                        }

                        if($clazzLevel == null || $clazzLevel < 0 || $clazzLevel > 6){
                            notClazzJoinPopupContent.find(".init").show();
                            return false;
                        }

                        $this.addClass("dis");
                        $.post("/student/clazz/joinvclazz.vpage", { clazzLevel : $clazzLevel}, function(data){
                            if(data.success){
                                location.href = $thisLink == undefined ? "/" : $thisLink;
                            }else{
                                $17.alert(data.info);
                                $this.removeClass("dis");
                            }
                        });
                    });

                    $(document).on("click", "#haveClazzLevel", function(){
                        $.prompt.close();
                        $(".v-joinClazzBtn-popup").click();
                    });
                }
            });
        }

        $.extend($, {
            notClassJoinMethod : notClassJoinMethod
        });
    }($));
</script>
<script type="text/html" id="T:notClazzJoinPopup">
    <%if(showFlag == "usa"){%>
    <div class="addClassTip-alert" id="notClazzJoinPopupContent">
        <p class="title">一起作业开放啦！PK竞技场、通天塔、走遍美国，等你探险！</p>
        <div class="act">
            <div class="ate">
                <h5>第1步：选择就读年级（老师告诉你班级编号了？<a href="javascript:void(0);" id="haveClazzLevel" class="w-blue">点这里</a>）</h5>
                <ul>
                    <li class="ls" data-value="1">1年级</li>
                    <li class="ls" data-value="2">2年级</li>
                    <li class="ls" data-value="3">3年级</li>
                    <li class="ls" data-value="4">4年级</li>
                    <li class="ls" data-value="5">5年级</li>
                    <li class="ls" data-value="6">6年级</li>
                </ul>
                <h5 class="init" style="display:none; color:#f00;">请选择就读年级</h5>
            </div>
            <div class="ate">
                <h5>第2步：体验一下新应用吧！</h5>
                <div class="game-ic" id="homeNotClazzJoinBtn" style="display: none;">
                    <a href="javascript:void(0);" data-link="/student/babel/api/index.vpage" class="babel">
                        <span class="ic"></span>
                        <strong>去通天塔</strong>
                    </a>
                    <a href="javascript:void(0);" data-link="/student/apps/index.vpage?app_key=TravelAmerica" class="travel">
                        <span class="ic"></span>
                        <strong>去走遍美国</strong>
                    </a>
                </div>
            </div>
        </div>
    </div>
    <%}else{%>
    <div class="addClassTip-alert" id="notClazzJoinPopupContent" style="margin: 0 0 0 50px;">
        <p class="title" style="padding-bottom: 15px;">我需要知道你就读的年级，才能让你进入</p>
        <div class="act">
            <div class="ate">
                <span class="sen">我读：</span>
                <ul>
                    <li class="ls" data-value="1">1年级</li>
                    <li class="ls" data-value="2">2年级</li>
                    <li class="ls" data-value="3">3年级</li>
                    <li class="ls" data-value="4">4年级</li>
                    <li class="ls" data-value="5">5年级</li>
                    <li class="ls" data-value="6">6年级</li>
                </ul>
                <h5 class="init" style="display:none; color:#f00;">请选择就读年级</h5>
                <div style="clear: both;"></div>
            </div>
        </div>
    </div>
    <%}%>
</script>
<#--问卷调查-->
<script type="text/html" id="T:QuestionnaireCtnBox">
<div class="t-learn-unit">
    <p style="margin: 0 0 0 35px; font-size: 18px; line-height: 160%;">你是？</p>
    <ul id="QuestionnaireCtnBox" style="padding: 0;">
        <%for(var i = 0, len = item.length; i < len; i++){%>
            <li style="float: none; padding: 2px 0;"><a href="javascript:void(0);" data-title="<%=item[i]%>" style="text-align: left; width: 80%; padding: 0 10px; margin: 0 0 0 35px; font-size: 14px; color: #000;"><%=i+1%>、<%=item[i]%></a></li>
        <%}%>
    </ul>
</div>
</script>