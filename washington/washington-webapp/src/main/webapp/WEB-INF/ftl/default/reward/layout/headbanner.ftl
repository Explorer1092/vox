<#if showNewHand!false>
<div class="w-banner">
    <div class="w-public-info"><a class="w-btn-info w-orange" href="javascript:void (0);" style="cursor: default;"><span>新手任务  免费领大礼</span><i class="w-arrow w-miarrow"></i></a></div>
    <div class="w-clear"></div>
    <div class="t-newTeacher-banner-box">
        <a href="/index.vpage"><img src="/public/skin/reward/imagesV1/reward.jpg" alt=""/></a>
    </div>
    <div class="t-newTeacher-gold-box">
        <div class="tea-gold"></div>
        <div class="tea-btn">
            <a class="w-but v-receiveRewardBtn" href="javascript:void (0);">新手奖励</a>
            <script type="text/javascript">
                $(function(){
                    $(document).on("click", ".v-receiveRewardBtn", function(){
                        var $this = $(this);

                        if( $this.hasClass("dis") ){
                            return false;
                        }

                        $this.addClass("dis");
                        $.post("/reward/getreward.vpage", {
                            taskType : "NEW_HAND_TASK",
                            rewardName : "GOLD"
                        }, function(data){
                            if(data.success){
                                $17.alert("领取成功");
                                $(".v-receiveRewardBtn").addClass("w-but-disabled");
                            }else{
                                $17.alert(data.info);
                            }
                            $this.removeClass("dis");
                        });
                    });
                });
            </script>
        </div>
    </div>
    <div class="w-clear"></div>
</div>
<#else>
<div class="tab_banner_box">
    <div class="tab_pic_box">
        <div id="a-rewardBanner-big-box"></div>
        <#--学生广告-->
        <script type="text/html" id="T:学生奖品中心广告Big">
            <div class="switchBox">
                <ul class="tab_pic">
                    <%for(var i = 0, len = dataInfo.length; i < len; i++){%>
                    <%if( i < 5 ){%>
                    <li data-banner-voxlog="<%=dataInfo[i].id%>" <%if(i == 0){%>style="display: block;"<%}%>>
                    <%if(!dataInfo[i].resourceUrl){%>
                    <img src="<@app.avatar href="<%=dataInfo[i].img%>"/>"/>
                    <%}else{%>
                    <a href="<%=dataInfo[i].resourceUrl%>" target="_blank"><img src="<@app.avatar href="<%=dataInfo[i].img%>"/>"/></a>
                    <%}%>
                    </li>
                    <%}%>
                    <%}%>
                </ul>
                <div class="tab tab_btn"></div>
            </div>
        </script>

        <#--老师广告-->
        <script type="text/html" id="T:老师奖品中心广告Big">
            <div class="switchBox">
                <ul class="tab_pic">
                    ${pageBlockContentGenerator.getPageBlockContentHtml('RewardIndex', 'RewardIndexAdBox')}
                    <li style="display: none;">
                        <a href="javascript:void(0);">
                            <img src="<@app.link href="public/skin/reward/images/index/zhekou.png"/>">
                        </a>
                    </li>
                </ul>
                <div class="tab tab_btn"></div>
            </div>
        </script>
        <script type="text/javascript">
            $(function(){
                var userType = "${(temp.currentUserType)!''}";
                var id = "#a-rewardBanner-big-box";
                var clazz   = "even";
                var second  = 4000;
                var index	= 0;					//default
                var idx		= $(id);				//id

                if(userType == "STUDENT"){
                    $.get("/be/info.vpage?p=3", function(data){
                        if(data.success){
                            movementMethod(data);
                        }else{
                            //加载错误
                        }
                    });
                }else{
                    movementMethod();
                }

                function movementMethod(data){
                    if(userType == "STUDENT" && data){
                        idx.html( template("T:学生奖品中心广告Big", { dataInfo : data.data}) );
                    }else{
                        idx.html( template("T:老师奖品中心广告Big", {}) );
                    }

                    var pic		= idx.find("li");		//listBox
                    var time	= setInterval(function(){
                        index++;
                        initSwitch();
                    }, second);

                    if(pic.length != 1 && pic.prevAll().length > 0){
                        //遍历
                        pic.eq(0).show().siblings().hide();
                        pic.each(function(i){
                            idx.find(".tab").append("<span class='prve "+ (i==index ? clazz : '') +"'>"+ (i+1) +"</span>");
                        });
                    }else{
                        pic.eq(0).show();
                    }

                    //通用
                    function initSwitch(){
                        if(pic.length == 1){
                            idx.eq(0).addClass(clazz);
                            return false;
                        }

                        if( index >= pic.length){
                            index = 0;
                        }
                        if( index < 0){
                            index = pic.length-1;
                        }
                        idx.find(".prve").eq(index).addClass(clazz).siblings().removeClass(clazz);
                        pic.eq(index).fadeIn(60).siblings().hide();
                    }

                    //经过
                    idx.find(".prve, li").on("mouseover", function(){
                        clearInterval(time);
                        index = $(this).prevAll().length;
                        initSwitch();
                    }).on("mouseout", function(){
                        time = setInterval(function(){
                            index++;
                            initSwitch();
                        }, second);
                    });

                    //左点击
                    idx.find(".back, .next").on("click", function(){
                        switch( $(this).attr("class") ){
                            case "back":
                                index--;
                                break;
                            case "next":
                                index++;
                                break;
                        }
                        initSwitch();
                        clearInterval(time);
                        time = setInterval(function(){
                            index++;
                            initSwitch();
                        }, second);
                    });
                }
            });
        </script>
    </div>
</div>
</#if>