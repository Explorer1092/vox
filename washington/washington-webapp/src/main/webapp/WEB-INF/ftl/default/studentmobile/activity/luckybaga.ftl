<#import "../../layout/mobile.layout.ftl" as temp>
<@temp.page dpi=".595">
<@sugar.capsule js=['jquery', "core", "alert", "template"] css=["plugin.alert"] />
<@app.css href="public/skin/project/luckybag/studentapp/skin.css" />
<#if (currentStudentDetail.isPrimaryStudent())!false>
    <#assign step1 = (data.firstStep)!false step2 = (data.secondStep)!false step3 = (data.thirdStep)!false/>
    <div class="transferBags-box">
        <div class="banner"></div>
        <div class="activity-flow"></div>
        <#if (data.sender)?? && data.sender?size gt 0>
            <div class="column">
                <div class="head">
                    <i class="head-icon"><img src="<@app.avatar href='${(data.sender.img)!}'/>"/></i>
                    <span class="name">${((data.sender.name?has_content)?string("${data.sender.name}", "---"))!'---'}</span>分享了福袋给你！帮TA打开福袋
                </div>
                <div class="open-btn">
                    <a href="javascript:void(0);" class="helpOpen-btn v-openLuckBagBtn">帮TA打开</a>
                </div>
            </div>
        </#if>

    <#if (data.hasBagPage)!false>
        <#if (step3 || step2 || step1)><#else>
            <div class="shareTo-content">
                <div class="tag"><b class="sub">1</b><span class="con">分享福袋给两位同学</span></div>
                <div class="s-left"></div>
                <div class="s-right">
                    <div class="s-title">选择你要分享的同学</div>
                    <ul>
                        <#list data.noBagUsers as item>
                            <li class="v-selectStudentItem" data-id="${(item.id)!0}" style="cursor: pointer;">
                                <div class="student-cont">
                                    <p class="image"><img src="<@app.avatar href='${(item.img)!}'/>"/></p>
                                    <p class="name">${((item.name?has_content)?string("${(item.name)!0}", "${(item.id)!0}"))!'---'}</p>
                                </div>
                            </li>
                        </#list>
                    </ul>
                </div>
            </div>
            <div style="height: 130px;"></div>
            <div class="transmit-btn">
                <a href="javascript:void(0);" class="immediate-delivery v-shareTransferBtn">立即传递</a>
            </div>
        </#if>

        <#--step2-->
        <#if (step1)!false>
            <div class="shareTo-content step-1">
                <div class="tag tag-green"><b class="sub"></b><span class="con">分享福袋给两位同学</span></div>

                <#if step2 || step3>
                    <#--你的福袋已打开-->
                    <div class="s-left s-line"></div>
                    <div class="tag-info infoMar3">
                        <div class="tag tag-green"><b class="sub"></b><span class="con">打开福袋</span></div>
                        <div class="txt">你的福袋已打开</div>
                    </div>
                <#elseif step1>
                    <div class="s-left"></div>
                    <div class="tag-info">
                        <div class="tag"><b class="sub">2</b><span class="con">打开福袋</span></div>
                        <div class="txt">两位同学登录后帮助你打开福袋</div>
                    </div>

                    <div class="s-right">
                        <div class="s-title">你已经把福袋分享给了</div>
                        <#if (data.receivers?size gt 0)!false>
                            <#list data.receivers as item>
                                <dl>
                                    <dt>
                                    <div class="student-cont">
                                        <p class="image"><img src="<@app.avatar href='${(item.img)!}'/>"/></p>
                                        <p class="name">${((item.name?has_content)?string("${(item.name)!0}", "---"))!'---'}</p>
                                    </div>
                                    </dt>
                                    <dd>
                                        <#if (item.isOpen)!false>
                                            <div class="dialog">
                                                <p>TA已帮助你</p>
                                            </div>
                                        <#else>
                                            <div class="dialog d-red">
                                                <p>TA还未帮助你</p>
                                            </div>
                                        </#if>
                                    </dd>
                                </dl>
                            </#list>
                        </#if>
                    </div>
                </#if>

                <#if step3>
                    <#--完成领取-->
                    <div class="tag-info infoMar3">
                        <div class="tag tag-green"><b class="sub"></b><span class="con">领取奖励</span></div>
                        <div class="txt">10学豆已经发放！</div>
                    </div>
                <#elseif step2>
                    <#--可以领取奖励-->
                    <div class="tag-info infoMar3">
                        <div class="tag"><b class="sub">3</b><span class="con">领取奖励</span></div>
                        <div class="txt">完成一次作业后即可领奖</div>
                    </div>

                    <div class="s-right">
                        <div class="bean-title">
                            <i class="icon-bean"></i>
                            学豆×10
                        </div>
                    </div>
                    <div class="prize-btn">
                        <a href="javascript:void(0);" class="immediate-delivery v-receiveLuckyBagBtn">立即领奖</a>
                    </div>
                <#else>
                    <div style="clear: both;"></div>
                    <div class="tag-info infoMar1">
                        <div class="tag tag-grey"><b class="sub">3</b><span class="con">领取奖励</span></div>
                        <div class="txt">完成一次作业后即可领奖</div>
                    </div>
                </#if>
            </div>
            <#--list start-->
            <#if (data.noBagUsers?size gt 0)!false>
                <div class="class-reward">
                    <i class="icon"></i>
                    <p class="tips">如果你的班级每人都获得了奖励，班级将获得100班级学豆，支持老师奖励学生</p>
                </div>
                <div class="shareTo-content step-2">
                    <div class="s-right">
                        <div class="s-title">以下学生还未获得福袋：</div>
                        <ul>
                            <#list data.noBagUsers as item>
                                <li>
                                    <div class="student-cont">
                                        <p class="image"><img src="<@app.avatar href='${(item.img)!}'/>"/></p>
                                        <p class="name">${((item.name?has_content)?string("${(item.name)!0}", "${(item.id)!0}"))!'---'}</p>
                                    </div>
                                </li>
                            </#list>
                        </ul>
                    </div>
                </div>
            </#if>
        </#if>
    <#else>
        <div class="shareTo-content">
            <div class="tag" style="width: 330px;"><b class="sub">&nbsp;</b><span class="con">当前手上持有福袋的同学</span></div>
            <div class="s-left"></div>
            <div class="s-right">
                <div class="s-title">快叫他们将福袋分享给你吧～</div>
                <ul>
                    <#list data.hasBagNoSendUsersMap as item>
                        <li>
                            <div class="student-cont">
                                <p class="image"><img src="<@app.avatar href='${(item.img)!}'/>"/></p>
                                <p class="name">${((item.name?has_content)?string("${(item.name)!0}", "${(item.id)!0}"))!'---'}</p>
                            </div>
                        </li>
                    </#list>
                </ul>
            </div>
        </div>
        <div style="height: 130px;"></div>
    </#if>

    <#--list end-->
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

            $17.voxLog({
                module : "luckybagActivity",
                op : "primary-app"
            }, "student");
        })();
    </script>
<#else>
    <div class="transferBags-box"><div class="banner"></div></div>
    <div style="font-size: 30px; text-align: center; padding: 150px 0 0; color: #666; line-height: 150%;">福袋活动中学暂未开放，敬请期待</div>
    <script type="text/javascript">
        $(function(){
            $17.voxLog({
                module : "luckybagActivity",
                op : "junior-app"
            }, "student");
        });
    </script>
</#if>
</@temp.page>