<#import "../../layout/project.module.student.ftl" as temp />
<@temp.page title="一起作业-传福袋，拿学豆" header="hide">
<@app.css href="public/skin/project/luckybag/student/skin.css" />
<#assign step1 = (data.firstStep)!false step2 = (data.secondStep)!false step3 = (data.thirdStep)!false/>
<div class="cfd-header">
    <div class="bgs">
        <div class="bg01"><div style="width: 950px; margin: 0 auto;"><a href="/" style="width: 180px; height: 100px; display: block;"></a></div></div>
        <div class="bg02"></div>
    </div>
    <div class="time-box">
        <div class="time">
            <p class="font-big">活动时间</p>
            <p>2月22日-3月22日</p>
        </div>
    </div>
</div>
<div class="cfd-main cdf-progamme">
    <div class="progamme">
        <div class="cm-top">
            <div class="cm-name"><span>活动<br>流程</span></div>
        </div>
        <div class="inner-box">
            <div class="infoBox-fixed">
                <div class="learnBean"></div>
                <#--是否有人传递-start-->
                <#if (data.sender)?? && data.sender?size gt 0>
                    <div class="cfd-openBox">
                        <div class="top">
                            <img src="<@app.avatar href='${(data.sender.img)!}'/>"/>
                            <div class="txt">
                                <span class="name">${((data.sender.name?has_content)?string("${data.sender.name}", "---"))!'---'}</span> 分享了福袋给你！<br>帮TA打开福袋
                            </div>
                        </div>
                        <a href="javascript:void(0)" class="btn-open v-openLuckBagBtn"></a>
                    </div>
                </#if>
                <#--是否有人传递-end-->
            </div>
        <#if (data.hasBagPage)!false>
            <#if step3 || step2 || step1>
                <div class="step01">
                    <div class="stop">
                        <div class="title title-green">
                            <div class="t-left"><i class="num-icon num-icon-04"></i></div>
                            <h2 class="t-name">分享福袋给两位同学</h2>
                            <div class="t-right"></div>
                        </div>
                    </div>
                </div>
            <#else>
                <div class="step01">
                    <div class="stop">
                        <div class="title title-red">
                            <div class="t-left"><i class="num-icon num-icon-01"></i></div>
                            <h2 class="t-name">分享福袋给两位同学</h2>
                            <div class="t-right"></div>
                        </div>
                    </div>
                    <div class="infoBox">
                        <div class="info-title">选择你要分享的同学</div>
                        <div class="info-list-box">
                            <ul class="info-list">
                                <#list data.noBagUsers as item>
                                    <li class="v-selectStudentItem" data-id="${(item.id)!0}" style="cursor: pointer;">
                                        <img src="<@app.avatar href='${(item.img)!}'/>" width="80" height="80"/>
                                        <span class="stu-name">${((item.name?has_content)?string("${(item.name)!0}", "${(item.id)!0}"))!'---'}</span>
                                    </li>
                                </#list>
                            </ul>
                        </div>
                        <div class="info-btn">
                            <a href="javascript:void(0)" class="btn v-shareTransferBtn"></a>
                        </div>
                    </div>
                </div>
            </#if>

            <#if step2 || step3>
                <div class="step02">
                    <div class="stop">
                        <div class="title title-green">
                            <div class="t-left"><i class="num-icon num-icon-04"></i></div>
                            <h2 class="t-name">你的福袋已打开</h2>
                            <div class="t-right"></div>
                        </div>
                        <div class="introTxt">两位同学登录后帮你打开你的福袋</div>
                    </div>
                </div>
            <#elseif step1>
                <div class="step02">
                    <div class="stop">
                        <div class="title title-red">
                            <div class="t-left"><i class="num-icon num-icon-02"></i></div>
                            <h2 class="t-name">打开福袋</h2>
                            <div class="t-right"></div>
                        </div>
                        <div class="introTxt">两位同学登录后帮你打开你的福袋</div>
                    </div>
                    <div class="infoBox">
                        <div class="intro-txt">你已经把福袋分享给了：</div>
                        <div class="intro-list">
                            <ul class="info-list">
                                <#if (data.receivers?size gt 0)!false>
                                    <#list data.receivers as item>
                                        <li>
                                            <img src="<@app.avatar href='${(item.img)!}'/>" width="80" height="80"/>
                                            <span class="stu-name">${((item.name?has_content)?string("${(item.name)!0}", "---"))!'---'}</span>
                                        </li>
                                    </#list>
                                </#if>
                            </ul>
                        </div>
                        <div class="intro-txt" style="width: 450px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${(data.unOpenName)!'---'}同学还没有登录，你可以催促TA登录哟~</div>
                    </div>
                </div>
            <#else>
                <div class="step02">
                    <div class="stop">
                        <div class="title title-grey">
                            <div class="t-left"><i class="num-icon num-icon-02"></i></div>
                            <h2 class="t-name">打开福袋</h2>
                            <div class="t-right"></div>
                        </div>
                        <div class="introTxt">两位同学登录后帮你打开你的福袋</div>
                    </div>
                </div>
            </#if>

            <#if step3>
            <#--完成领取-->
                <div class="step03">
                    <div class="stop">
                        <div class="title title-green">
                            <div class="t-left"><i class="num-icon num-icon-04"></i></div>
                            <h2 class="t-name">领取奖励</h2>
                            <div class="t-right"></div>
                        </div>
                        <div class="introTxt">10学豆已经发放！</div>
                    </div>
                </div>
            <#elseif step2>
            <#--可以领取奖励-->
                <div class="step03">
                    <div class="stop">
                        <div class="title title-red">
                            <div class="t-left"><i class="num-icon num-icon-03"></i></div>
                            <h2 class="t-name">领取奖励</h2>
                            <div class="t-right"></div>
                        </div>
                        <div class="introTxt">完成一次作业后即可领取奖励</div>
                        <a href="javascript:void(0)" class="btn-getAward v-receiveLuckyBagBtn" title="领取奖励"></a>
                    </div>
                </div>
            <#else>
                <div class="step03">
                    <div class="stop">
                        <div class="title title-grey">
                            <div class="t-left"><i class="num-icon num-icon-03"></i></div>
                            <h2 class="t-name">领取奖励</h2>
                            <div class="t-right"></div>
                        </div>
                        <div class="introTxt">完成一次作业后即可领取奖励</div>
                    </div>
                </div>
            </#if>
        <#else>
            <div class="step01">
                <div class="stop">
                    <div class="title title-red">
                        <div class="t-left"></div>
                        <h2 class="t-name">当前手上持有福袋的同学</h2>
                        <div class="t-right"></div>
                    </div>
                </div>
                <div class="infoBox">
                    <div class="info-title">快叫他们将福袋分享给你吧～</div>
                    <div class="info-list-box">
                        <ul class="info-list">
                            <#list data.hasBagNoSendUsersMap as item>
                                <li >
                                    <img src="<@app.avatar href='${(item.img)!}'/>" width="80" height="80"/>
                                    <span class="stu-name">${((item.name?has_content)?string("${(item.name)!0}", "${(item.id)!0}"))!'---'}</span>
                                </li>
                            </#list>
                        </ul>
                    </div>
                </div>
            </div>
        </#if>
        </div>
    </div>
</div>
<script type="text/javascript">
    (function () {
        var userItems = [];
        var bagListsCount = ${((data.noBagUsers)?size)!0};
        $(".v-selectStudentItem").on({
            click : function(){
                var $this = $(this);
                var $id = $this.attr("data-id");

                if( $17.isBlank($id) ){
                    return false;
                }

                if($this.hasClass("active")){
                    userItems.splice($.inArray($id, userItems), 1);
                    $this.removeClass("active");
                }else{
                    if(userItems.length >= 2){
                        $17.alert("最多分享给两位同学");
                        return false;
                    }

                    userItems.push($id);
                    $this.addClass("active");
                }
            },
            mouseleave : function(){
                var $this = $(this);

                $this.removeClass("hover");
            },
            mouseenter : function(){
                var $this = $(this);

                $this.addClass("hover");
            }
        });

        //分享
        $(".v-shareTransferBtn").on("click", function(){
            if(bagListsCount >= 2 && userItems.length < 2){
                $17.alert("最少选择两位同学分享！");
                return false;
            }

            if(userItems.length < 1){
                $17.alert("最少选择两位同学分享！");
                return false;
            }


            $.post("/student/activity/sendluckybag.vpage", { receiverIds : userItems.join()}, function(data){
                if(data.success){
                    $17.alert("传递成功。", function(){
                        location.reload();
                    });
                }else{
                    $17.alert(data.info ? data.info : "分享失败！");
                }
            });
        });

        //打开福袋
        $(".v-openLuckBagBtn").on("click", function(){
            $.post("/student/activity/openluckybag.vpage", {}, function(data){
                if(data.success){
                    $17.alert("成功帮TA打开。", function(){
                        location.reload();
                    });
                }else{
                    $17.alert(data.info ? data.info : "打开失败！");
                }
            });
        });

        //领取奖励
        $(".v-receiveLuckyBagBtn").on("click", function(){
            $.post("/student/activity/receiveluckybag.vpage", {}, function(data){
                if(data.success){
                    $17.alert("领取成功。", function(){
                        location.reload();
                    });
                }else{
                    $17.alert(data.info ? data.info : "领取失败！");
                }
            });
        });
    })();
</script>
<#if (step1 && data.noBagUsers?size gt 0)!false>
    <@sugar.capsule js=["flexslider"] css=["plugin.flexslider"] />
    <div class="cfd-main cdf-classAward">
        <div class="classAward">
            <div class="cm-top">
                <div class="cm-name"><span>班级<br>奖励</span></div>
                <div class="cm-title">如果你的班级全部学生都获得了奖励，该班级将获得100班级学豆，支持老师奖励学生</div>
            </div>
            <div class="infoBox">
                <div class="info-title">以下学生还未获得福袋：</div>
                <div class="info-list-box v-groupLuckBagList">
                    <ul class="info-list slides">
                        <#list data.noBagUsers as item>
                            <li>
                                <img src="<@app.avatar href='${(item.img)!}'/>" width="80" height="80"/>
                                <span class="stu-name">${((item.name?has_content)?string("${(item.name)!0}", "${(item.id)!0}"))!'---'}</span>
                            </li>
                        </#list>
                    </ul>
                </div>
            </div>
        </div>
    </div>
    <script type="text/javascript">
        (function () {
            $(".v-groupLuckBagList").flexslider({
                animation : "slide",
                animationLoop : true,
                slideshow : false,
                controlNav : false,
                directionNav: ${((data.noBagUsers?size gt 10)!false)?string},
                slideshowSpeed: 4000, //展示时间间隔ms
                animationSpeed: 400, //滚动时间ms
                itemWidth : 90,
                direction : "horizontal",//水平方向
                minItems : 10,
                maxItems : 10,
                move : 10
            });

            $17.voxLog({
                module : "luckybagActivity",
                op : "pc"
            }, "student");
        })();
    </script>
</#if>
</@temp.page>