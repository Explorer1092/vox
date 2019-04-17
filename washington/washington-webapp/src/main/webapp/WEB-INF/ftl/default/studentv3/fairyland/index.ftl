<#import '../layout/layout.ftl' as temp>
<@temp.page pageName='fairyland' clazzName='t-park-bg'>
<@sugar.capsule js=["qtip", "voxSpread"] css=["plugin.jquery.qtip"] />
    <#assign PageBalckList = false/>
    <#if (currentStudentDetail.inPaymentBlackListRegion)!false>
        <#assign PageBalckList = true/>
    </#if>
    <#if PageBalckList>
        <style>
            /*fairyland*/
            .t-park-bg{ background: url(<@app.link href="public/skin/studentv3/images/christmas/p-banner.jpg?1.0.1"/>) #62ad65 0 52px repeat-x;}
            .t-park-content, .t-park-inner{ background-image: url(<@app.link href="public/skin/studentv3/images/christmas/p-banner.jpg?1.0.1"/>);}
            .t-park-game{ margin-top: 430px;}
        </style>
    <#else>
        <#--学生PC端-课外乐园-顶部广告位-->
        <#include "headbanner.ftl"/>
        <#--Header新版只开放白名单地区-->
        <div class="t-paymentsDetail-box" style="height: 50px; background-position: center bottom; background-image: none; top: 0px;"></div>
    </#if>

    <#--所有付费产品-->
    <div class="t-park-content" data-level="${(currentStudentDetail.getClazzLevelAsInteger())!'--'}">
        <div class="t-park-inner">
            <div id="GameMainList"><#--游戏列表--></div>
        </div>
    </div>

    <script type="text/html" id="T:GameMainList">
        <div class="t-park-game">
            <ul>
                <%for(var i = 0; i < appInfo.length; i++ ){%>
                <%item = appInfo[i]%>
                <li id="Apps_<%=item.appKey%>">
                    <dl>
                        <dt>
                            <span class="game-box">
                                <a href="javascript:void (0);" class="pk-img-a">
                                    <span class="pk-game">
                                        <img width="136" height="136" src="<@app.link href="public/skin/common/images/app-icon/big/<%=item.appKey%>.png?1.0.8"/>">
                                    </span>
                                </a>
                            </span>
                        </dt>
                        <dd>
                            <h2><span class="pk-font"><%=item.productName%></span><span style="display: inline-block;"><%=item.productDesc%></span></h2>
                            <div class="haveBuyStudentListBox" style="height: 57px;" data-content-id="<%=item.appKey%>" data-appname="<%=item.appName%>" data-appstatus="<%=item.appStatus%>"></div>
                            <p style="font-size: 14px;"><%=item.operationMessage%>&nbsp;</p>
                        </dd>
                    </dl>
                    <div class="pk-game-btn">
                        <%
                            var goFlashBtnText="进入", cartBtnText="续费", cartLink = item.appKey, isShowCartBtn = true, productCartType='1';
                            if(item.appStatus == 0){
                                goFlashBtnText="试用";
                                cartBtnText="开通";
                            }

                            if(item.appKey == "AfentiExam"){ cartLink = 'exam' }
                            if(item.appKey == "KaplanPicaro"){ cartLink = 'picaro'; isShowCartBtn = false;}
                            if(item.appKey == "TravelAmerica"){ cartLink = 'travel' }
                            if(item.appKey == "iandyou100"){ cartLink = 'iandyou'; isShowCartBtn = false;}
                            if(item.appKey == "A17ZYSPG"){ cartLink = 'spg' }
                            if(item.appKey == "Stem101"){ cartLink = 'stem' }
                            if(item.appKey == "SanguoDmz"){ productCartType = '0' }
                            if(item.appKey == "PetsWar"){ productCartType = '0' }
                            if(item.appKey == "Walker"){ isShowCartBtn = false;}
                        %>
                        <a href="<%=item.launchUrl%>" class="pk-btn pk-orange" target="_blank"><%=goFlashBtnText%></a>
                        <%if(isShowCartBtn && ${(!PageBalckList!false)?string}){%>
                            <a href="/apps/afenti/order/<%=cartLink.toLowerCase()%>-cart.vpage?ref=fairyland&type=<%=productCartType%>" target="_blank" class="pk-btn pk-green"><%=cartBtnText%></a>
                        <%}%>
                    </div>
                    <i class="leaf"></i>
                </li>
                <%}%>
            </ul>
        </div>
    </script>

    <script type="text/html" id="t:haveBuyStudentListBox">
        <p>
            <%if(appInfo.studentList.length > 0){%>
                <%for(var i = 0;i < appInfo.studentList.length; i++ ){%>
                    <%if(i == 0){%>
                        <%if(appStatus == 0){%>
                            <a class="pk-img-a w-game-icon" href="javascript:void (0)">
                                <span class="w-game-complete showUsersMessage" data-comment="你未开通<%=appName%>，头像无法点亮<%if(appKeyName == 'AfentiExam'){%>，无法获得学豆奖励哦<%}%>。"></span>
                                <span class="pk-game">
                                    <%if(appInfo.studentList[i].studentImg == ''){%>
                                        <img src="<@app.avatar href=''/>" width="50" height="50">
                                    <%}else{%>
                                        <img src="<@app.avatar href='<%=appInfo.studentList[i].studentImg%>'/>" width="50" height="50">
                                    <%}%>
                                </span>
                            </a>
                        <%}else{%>
                            <a class="pk-img-a pk-img-active" href="javascript:void (0)">
                                <span class="pk-game showUsersMessage" data-comment="你已开通<%=appName%><#--<%if(appKeyName == 'AfentiExam'){%>，获得<%=appInfo.studentList[i].bean%>学豆<%}%>-->">
                                    <%if(appInfo.studentList[i].studentImg == ''){%>
                                        <img src="<@app.avatar href=''/>" width="50" height="50">
                                    <%}else{%>
                                        <img src="<@app.avatar href='<%=appInfo.studentList[i].studentImg%>'/>" width="50" height="50">
                                    <%}%>
                                </span>
                            </a>
                        <%}%>
                    <%}else{%>
                        <a class="pk-img-a pk-img-active" href="javascript:void (0)">
                            <span class="pk-game showUsersMessage" data-comment="<%=appInfo.studentList[i].studentName%>已开通<#--<%if(appKeyName == 'AfentiExam'){%>，获得<%=appInfo.studentList[i].bean%>学豆<%}%>-->">
                                <%if(appInfo.studentList[i].studentImg == ''){%>
                                    <img src="<@app.avatar href=''/>" width="50" height="50">
                                <%}else{%>
                                    <img src="<@app.avatar href='<%=appInfo.studentList[i].studentImg%>'/>" width="50" height="50">
                                <%}%>
                            </span>
                        </a>
                    <%}%>
                <%}%>
            <%}%>
        </p>
    </script>

    <script type="text/javascript">
        (function(){
            //T:GameMainList
            $.ajax({
                url: "/student/fairyland/pc/applist.vpage",
                type: 'GET',
                data: {},
                success: function (data) {
                    if(data.success && data.appInfo){
                        $("#GameMainList").html( template("T:GameMainList", {
                            appInfo: data.appInfo
                        }) );

                        setTimeout(function(){
                            ShowStudentList();
                        }, 100);
                    }else{
                        //fail
                    }
                },
                error: function (data) {
                    //fail 404 - 500
                }
            });

            function ShowStudentList(){
                //付费产品开通用户
                $('.haveBuyStudentListBox').each(function(){
                    var $this = $(this);
                    var appKeyName = $this.data("content-id");
                    var appName = $this.data("appname");
                    var appStatus = $this.data("appstatus");

                    $this.html('<div style="padding: 50px 0; text-align: center; color: #FFFFFF;"><img src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>');
                    $.get('/student/fairyland/appdetail.vpage?appType='+appKeyName, function(data){
                        if(data.success){
                            $this.html(template("t:haveBuyStudentListBox", {appInfo : data, appKeyName : appKeyName, appName : appName, appStatus : appStatus}));

                            //等待页面渲染
                            setTimeout(function(){
                                showTip();
                            },200);

                        }else{
                            $this.html('<p>暂无数据</p>');
                        }
                    }).fail(function(){
                        $this.html('<p>暂无数据</p>');
                    });
                });
            }

            function showTip(){
                $(".showUsersMessage").each(function(){
                    var $this = $(this);
                    var ps = $this.attr("data-comment");
                    $this.qtip({
                        content: {
                            text: ps
                        },
                        hide: {
                            fixed: true,
                            delay: 150,
                            leave: false
                        },
                        position: {
                            at: 'bottom center',
                            my: 'top center',
                            viewport: $(window),
                            effect: false,
                            adjust: {
                                y : 6
                            }
                        },
                        style : {
                            classes : 'qtip-bootstrap'
                        }
                    });
                });
            }

        })();
    </script>
</@temp.page>