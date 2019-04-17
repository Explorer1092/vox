<#import "../../layout/project.module.student.ftl" as temp />
<@temp.page title="星星榜" header="hide">
<@sugar.capsule js=["DD_belatedPNG"] css=[] />
<@app.css href="public/skin/project/studentstarrank/skin1.0.2.css" />
<#assign JZT_CHANNEL_ID = "202011">
<div class="star-student-main">
    <div class="head">
        <div class="stars-rank-box">
            <div class="sr-lab">
                <ul>
                    <#if (.now > '2014-12-25 00:00:00'?datetime('yyyy-MM-dd HH:mm:ss'))>
                        <li>
                            <span>上月排名奖励：<i id="getLearnBeansCount">--</i>学豆</span>
                            <a class="sr-btn getStar-btn sr-disable getLearnBeansBtn" data-key="1" href="javascript:void (0);" data-info="扫描下侧二维码，${.now?string("MM")?number}月<dateTimeLast>号前，在家长通领取学豆奖励"></a>
                        </li>
                    </#if>
                    <#if .now?string("MM") == "9" || .now?string("MM") == "3">
                        <li class="js-lastTerm">
                            <span>上学期排名奖励：<i id="getLastTermLearnBeansCount">--</i>学豆</span>
                            <strong style="font-size: 16px; display: inline-block; padding-left: 15px;">${.now?string("MM")}月1日领取</strong>
                        <#--<a class="sr-btn getStar-btn getLearnBeansBtn" data-key="2" href="javascript:void (0);" data-info="扫描下侧二维码，在家长通的“作业&报告”领取学豆奖励"></a>-->
                        </li>
                    <#else>
                        <li class="js-monthTerm">
                            <span>本月排名奖励：<i id="getMonthBeansCount">--</i>学豆</span>
                            <strong style="font-size: 16px; display: inline-block; padding-left: 15px;"><#if .now?string("MM")?number == 12>1<#else>${(.now?string("MM")?number) + 1}</#if>月1日领取</strong>
                        </li>
                    </#if>
                    <li class="sl"><strong>2位</strong>家长下载家长通，每月奖<strong>20星星</strong>，最多领<strong>50学豆</strong></li>
                </ul>
            </div>
        </div>
        <div class="inner">
            <h2 class="logo" style="top: -544px;"><a href="/"></a></h2>
            <div class="star-nav next-month active PNG_24" template-type="semesterStarsListBox" data-title="奖励将于3月1日后系统自动发放。">本学期星星榜</div>
            <div class="star-nav last-month PNG_24" template-type="starsListThisMonthBox" data-title="&nbsp;">本月星星榜</div>
            <div id="likeCountRankRightBox"></div>
        </div>
    </div>

    <div class="comSlide" style="margin-top: 40px;">
        <div class="inner"></div>
    </div>
    <div class="slide">
        <div class="inner">
            <div class="sf">
                <ul>
                    <li>1、领取条件：上月(上学期)老师奖励过星星，请在家长通领取学豆奖励</li>
                    <li>2、月度奖励次月领取，学期奖励开学第一月领取，过期将清零</li>
                    <li>3、星星数量相同时，按练习分数和完成时间进行排名</li>
                    <li>
                        <table class="sr-table">
                            <tr>
                                <th style="width: 14%;">月排名</th>
                                <th style="width: 12%;">1</th>
                                <th style="width: 12%;">2</th>
                                <th style="width: 12%;">3</th>
                                <th style="width: 12%;">4-9</th>
                                <th style="width: 12%;">10-19</th>
                                <th style="width: 12%;">20-29</th>
                                <th>30以上</th>
                            </tr>
                            <tr>
                                <th>奖励学豆</th>
                                <th>50</th>
                                <th>20</th>
                                <th>10</th>
                                <th>5</th>
                                <th>3</th>
                                <th>2</th>
                                <th>1</th>
                            </tr>
                        </table>
                        <table class="sr-table">
                            <thead>
                            <tr>
                                <th style="width: 14%;">学期排名</th>
                                <th style="width: 12%;">1</th>
                                <th style="width: 12%;">2</th>
                                <th style="width: 12%;">3</th>
                                <th style="width: 12%;">4-9</th>
                                <th style="width: 12%;">10-19</th>
                                <th style="width: 12%;">20-29</th>
                                <th>30以上</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <th>奖励学豆</th>
                                <th>100</th>
                                <th>40</th>
                                <th>20</th>
                                <th>10</th>
                                <th>6</th>
                                <th>4</th>
                                <th>2</th>
                            </tr>
                            </tbody>
                        </table>
                    </li>
                </ul>
            </div>
        </div>
    </div>
    <div class="foot">
        <div class="inner"></div>
    </div>
</div>
<script type="text/html" id="T:starsListThisMonthBox">
<div class="star-message-info" id="starMessageInfo"><%=info%></div>
<div class="w-table">
<%if(item.starRank.length > 0){%>
    <%if(!item.myRank.rank){%>
        <div style="padding: 120px 0 0; font-size: 20px; text-align: center; color: #b08941;">本月老师还没发星星，我们也就没有50学豆奖励，快去告诉老师吧！</div>
    <%}else{%>
        <table>
            <tr>
                <td style="width: 150px;"><span class="gold gold-<%=item.myRank.rank%>"><%=item.myRank.rank%></span></td>
                <td style="width: 180px;">我(<%if(item.myRank.userName == ""){%><%=item.myRank.userId%><%}else{%><%=item.myRank.userName%><%}%>)</td>
                <td style="width: 260px;"><%=item.myRank.star - (item.myRank.bindWechatParentCount > 1 ? (2*10) : (item.myRank.bindWechatParentCount*10))%>+<%=item.myRank.bindWechatParentCount > 1 ? (2*10) : (item.myRank.bindWechatParentCount*10)%>
                    <%if(item.myRank.bindWechatParentCount < 2){%>
                    <a href="javascript: void(0);" class="getStar-btn PNG_24" data-info="请下载家长通，1位家长关注每月奖励10个星星，2位家长关注每月奖励20个星星。每月的学豆奖励，仅在家长通领取。">奖20星星</a>
                    <%}%>
                    <div class="trHoverBack">
                        <div class="inner-n">
                            老师检查练习，奖励：<strong><%=item.myRank.star - (item.myRank.bindWechatParentCount > 1 ? (2*10) : (item.myRank.bindWechatParentCount*10))%></strong> 个星星<br/>
                            <%
                                var bindWechatParentCount =  +item.myRank.bindWechatParentCount || 0;
                                if(bindWechatParentCount === 1){
                            %>
                                <strong>1位家长使用家长通，奖励： 10个星星</strong><br/>
                            <%
                                }else if(bindWechatParentCount > 1){
                            %>
                                <strong>已有2位家长使用家长通，奖励：20个星星</strong><br/>
                            <% } %>
                            当前排名第<strong><%=item.myRank.rank%></strong>，可领奖励：<strong><%=item.myRank.integral%></strong> 学豆
                        </div>
                    </div>
                </td>
                <td >
                    <a href="javascript: void(0);" class="getStar-btn getStar-btn-blue PNG_24" data-info="请下载家长通，2位家长下载家长通，每月额外奖励20星星。" data-type="get">领取 <%=item.myRank.integral%> 学豆</a>
                </td>
            </tr>
        </table>
    <div style="height: 289px; overflow: hidden; overflow-y: auto; margin-right: 4px; width: 99%; position: relative;">
        <table style="margin-bottom: 60px;">
        <%for(var i=0, len=item.starRank.length; i<len; i++){%>
            <%if(item.starRank[i].userId != ${(currentUser.id)!}){%>
            <tr>
                <td style="width: 150px;"><span class="gold gold-<%=item.starRank[i].rank%>"><%=item.starRank[i].rank%></span></td>
                <td style="width: 180px;"><%if(item.starRank[i].userName == ""){%><%=item.starRank[i].userId%><%}else{%><%=item.starRank[i].userName%><%}%></td>
                <td style="width: 260px;"><%=item.starRank[i].star - (item.starRank[i].bindWechatParentCount > 1 ? (2*10) : (item.starRank[i].bindWechatParentCount*10))%><%if(item.starRank[i].bindWechatParentCount > 0){%>+<%=item.starRank[i].bindWechatParentCount > 1 ? (2*10) : (item.starRank[i].bindWechatParentCount*10)%>
                    <%}%>
                    <div class="trHoverBack">
                        <div class="inner-n">
                            老师检查练习，奖励：<strong><%=item.starRank[i].star - (item.starRank[i].bindWechatParentCount > 1 ? (2*10) : (item.starRank[i].bindWechatParentCount*10))%></strong> 个星星<br/>
                            <%
                                var bindWechatParentCount =  +item.starRank[i].bindWechatParentCount || 0;
                                if(bindWechatParentCount === 1){
                            %>
                                <strong>1位家长使用家长通，奖励： 10个星星</strong><br/>
                            <%
                                }else if(bindWechatParentCount > 1){
                            %>
                                <strong>已有2位家长使用家长通，奖励：20个星星</strong><br/>
                            <% } %>
                            当前排名第<strong><%=item.starRank[i].rank%></strong>，可领奖励：<strong><%=item.starRank[i].integral%></strong> 学豆
                        </div>
                    </div>
                </td>
                <td><%=item.starRank[i].integral%> 学豆</td>
            </tr>
            <%}%>
        <%}%>
        </table>
    </div>
    <%}%>
<%}else{%>
    <div class="no-content"><p><%=item.info ? item.info : '暂无数据'%></p></div>
<%}%>
</div>
</script>
<script type="text/html" id="T:weiXinSideDetail">
    <div style="font-size: 16px;  padding: 15px 0; line-height: 150%;">
        扫描下方二维码，安装家长通APP，即可在“首页”-“星星奖励”中领取学豆
    </div>
    <div class="weiXinSideDetail" style="text-align: center; background: url(<@app.link href='public/skin/project/studentstarrank/act-arrow.png'/>) no-repeat 200px 100px;  margin: 0 auto;">
        <dl style="float: left;">
            <dd style="font-size: 18px;">步骤1</dd>
            <dt><img src='<%= JZT_QR_URL %>' class="doGetJZTQR" width='200' height='200' alt="家长通二维码"/></dt>
            <dd>扫一扫下载家长通<br>学习动态全知道</dd>
        </dl>
        <dl style="float: right;">
            <dd style="font-size: 18px;">步骤2</dd>
            <dt style="padding: 10px;"><img src="<@app.link href="public/skin/project/studentstarrank/act-info-v2.png"/>"></dt>
        </dl>
        <div style="clear:both;"></div>
    </div>
</script>
<script type="text/javascript">
    $(function () {
        <#--判断是否已经通过微信 or 家长通绑定家长-->
        var hasParentBindWechat = ${(hasParentBindWechat!false)?string};

        //上月排名奖励
        $.get("/student/xxt/starrank.vpage", {}, function(data){
            if(data.success){
                if(!data.receivedStarRankReward){
                    $(".getLearnBeansBtn[data-key='1']").removeClass("sr-disable");
                }else{
                    $(".getLearnBeansBtn[data-key='1']").addClass("sr-disable");
                }

                $("#getLearnBeansCount").html(data.myRank.integral);
            }
        });

        //上学期排名奖励
        $.get("/student/xxt/startermrank.vpage", {}, function(data){
            if(data.success){
                if(!data.receivedStarRankReward){
                    $(".getLearnBeansBtn[data-key='2']").removeClass("sr-disable");
                }else{
                    $(".getLearnBeansBtn[data-key='2']").addClass("sr-disable");
                }
                $('#getLastTermLearnBeansCount').html(data.myRank.integral);
            }
        });

        //经过Tr
        var rankTemplate = $("#likeCountRankRightBox");
        rankTemplate.on("mouseenter", "tr", function(){
            $(this).addClass("current");
            $(this).find(".trHoverBack").show();
        }).on("mouseleave", "tr", function(){
            $(this).removeClass("current");
            $(this).find(".trHoverBack").hide();
        });

        //Nav switch
        var $tempData = {};
        $(".star-nav[template-type]").on("click", function(){
            var $this = $(this);
            var tempType = $this.attr("template-type");
            var getLink = "/student/xxt/starrank.vpage";

            $this.addClass("active").siblings().removeClass("active");

            if($tempData[tempType]){
                rankTemplate.html( template("T:starsListThisMonthBox", {item : $tempData[tempType], info : $this.attr("data-title")}) );
                return false;
            }

            if(tempType == 'semesterStarsListBox'){
                //获取上月老师
                $.get("/student/xxt/startermrank.vpage?currentTerm=true", {}, function(data){
                    $tempData[tempType] = data;
                    rankTemplate.html( template("T:starsListThisMonthBox", {item : $tempData[tempType], info : $this.attr("data-title")}) );
                });
            }else{
                //获取本月排行榜
                var $items = {
                    myRank : {
                        rank : true
                    },
                    starRank : []
                };
                rankTemplate.html("<div style='text-align: center; padding: 250px 0 0; line-height: 150%; font-size: 18px; color: #b08941;'>星星榜功能已经下线，上学期星星榜的学豆奖励将于3月1日之后系统自动发放，无需领取。 <br/>开学后星星榜将由“学豆奖励榜”替代，到时记得来领取学豆哟~</div>");
                /*$.get("/student/xxt/starrank.vpage", {currentMonth: true}, function(data){
                    $tempData[tempType] = data;
                    rankTemplate.html( template("T:starsListThisMonthBox", {item : $tempData[tempType], info : $this.attr("data-title")}) );
                    $('#getMonthBeansCount').html(data.myRank.integral);
                });*/
            }
        }).eq(1).click();

        $("#getLearnBeansDisableBtn").on("click", function(){
            $(".star-nav[template-type]").eq(0).click();
        });

        //领取奖励
        var qrCodeUrl = "<@app.link href="public/skin/studentv3/images/2dbarcode.jpg"/>";
        var qrCodeUrlSuccess = false;
        $(document).on("click", ".getStar-btn", function(){
            var $this = $(this);
            var popupTitle = "领取奖励";
            var $recordInfo = $this.attr("data-info");

            if($this.hasClass("sr-disable")){
                return false;
            }

            if($this.attr("data-type") == "get"){
                //popupTitle = "领取学豆"   //改动说明 ：http://project.17zuoye.net/redmine/issues/15871
            }

            //每个月有多少天
            if($recordInfo.indexOf("<dateTimeLast>") > -1){
                $recordInfo = $recordInfo.replace(/<dateTimeLast>/, $17.getMonthTotalDay());
            }

            <#--TODO 现在全部使用家长通 所以不需要判断 并且流量全部引导到家长通操作  如有疑问  请联系 caihong.li  luwei.li
            if(hasParentBindWechat){
                qrCodeUrl = "<@app.link href="public/skin/project/studentstarrank/star-bonus-code.png?1.0.1"/>";
                qrCodeUrlSuccess = true;
            }
            -->

            var qrCodeUrlSuccessCb = function(){
                if($this.attr("data-type") == "load"){
                    $("#loadCode").html("<img src='"+ qrCodeUrl +"' width='140' height='140' />");
                    return false;
                }

                $17.get_jzt_qr("${JZT_CHANNEL_ID}", function(JZT_QR_URL){
                    $.prompt(template("T:weiXinSideDetail", {
                        hasParentBindWechat : hasParentBindWechat,
                        qrCodeUrl : qrCodeUrl,
                        JZT_QR_URL : JZT_QR_URL,
                        info : $recordInfo
                    }), {
                        title: popupTitle,
                        buttons: { },
                        position: {width: 600}
                    });
                });
            };

            <#--  TODO 暂且不判断 理由同上
            if(qrCodeUrlSuccess){
                qrCodeUrlSuccessCb();
            }
            -->

            qrCodeUrlSuccessCb();

            <#-- TODO 注释理由同上
            $.get("/student/qrcode.vpage?campaignId=14", function(data){
                if(data.success){
                    qrCodeUrl = data.qrcode_url;
                }
                qrCodeUrlSuccess = true;

                if($this.attr("data-type") == "load"){
                    $("#loadCode").html("<img src='"+ qrCodeUrl +"' width='140' height='140' />");
                    return false;
                }

                $.prompt(template("T:weiXinSideDetail", {
                    qrCodeUrl : qrCodeUrl,
                    info : $recordInfo
                }), {
                    title: popupTitle,
                    buttons: { },
                    position: {width: 600}
                });
            });
            -->
        });
    })

    /*var month=date.getMonth()+1;
    var hr = day.getHours();

    if ( hr >= 7) && (hr <= 10)
    {
        document.getElementById("kefu").style.display="block";
        document.getElementById('QQ').style.display="none";
    }
    else
    {
        document.getElementById('kefu').style.display="none";
        document.getElementById('QQ').style.display="block";
    }
*/
</script>
</@temp.page>