<#import "../../../layout/project.module.student.ftl" as temp />
<@temp.page header="show">
    <@sugar.capsule js=["DD_belatedPNG", "flexslider"] css=[] />
    <@app.css href="public/skin/project/magiccastal/magic.css?1.0.2" />
    <div class="magic">
        <div class="bgs">
            <div class="bg01"></div>
            <div class="bg02"></div>
            <div class="bg03"></div>
            <div class="bg04"></div>
        </div>
        <div class="main">
            <div class="m-hd"><p style="line-height:normal;"><span style="line-height:3.8;display:inline-block;margin-top:30px;">据传，每位17作业的孩子都是一位魔法师。17魔法城堡的大门开启，魔法盛会开始了~</span></p></div>
            <div class="m-mn">
                <div class="mn-top" style="position: relative;">
                    <div class="mnt01" >
                        <div class="mnt01-left" >
                            <div class="intro"><img class="avatar" src="<@app.avatar href='${(currentUser.fetchImageUrl())!}'/>"><p><span class="first">${(currentUser.profile.realname)!''}</span><span>${(levelName)!'--'}</span></p></div>
                            <div class="info" >
                                <p><span class="circle">总魔力值：</span><span class="num">${(level.levelValue)!0} 点</span></p>
                                <p><span class="circle">本周唤醒药水：</span><span class="num">${waterCount!0} 瓶</span></p>
                                <a class="intro-link js-hover-detail" href="javascript:void(0)">详细介绍》</a>
                            </div>
                            <div class="describe">做作业、唤醒魔法师可以获得魔力值，魔力值可以提升魔法等级哦！</div>
                        </div>
                        <div class="mnt01-right"><a href="javascript:void(0)" class="mbtn-hx magic-icons js-ifhx <#if (!showActive)!true>mbtn-hx-disable</#if>"><#--mbtn-hx-disable禁用按钮--><p>去唤醒魔法师</p><p class="time">（每日开启）</p></a></div>
                    </div>
                    <div class="magic-dialog04 magic-dialog d-none">
                        <h3 class="h3-title">详细介绍：</h3>
                        <span class="hat"></span>
                        <div class="inner">
                            <ul>
                                <li>1.每做1次作业，可获得1点魔力值；</li>
                                <li class="even">2.每唤醒1位魔法师，获得3点或5点魔力值；</li>
                                <li>3.每唤醒1位魔法师，获得1瓶或2瓶药水；</li>
                                <li class="even">4.每做1次作业，获得1瓶唤醒药水；</li>
                                <li>5.绑定家长APP每周可额外获得1瓶药水；</li>
                                <li class="even">6.每周一重新统计本周魔力值与药水；</li>
                                <li>7.每3级可升级到下一称号的魔法师；</li>
                            </ul>
                            <#if (!isBindApp)!false>
                                <div class="codebox">
                                    <span class="code"><img src="<@app.link href="public/skin/project/magiccastal/magic-code-app.png"/>"></span>
                                    <p class="txt">请家长扫一扫<br>下载安装家长APP</p>
                                </div>
                            </#if>
                        </div>
                    </div>
                    <div class="mnt02">
                        <a href="javascript:void(0)" class="hd-link js-clickDetailPopup">查看魔法之路》</a>
                        <div class="mnt02-box">
                            <div class="arrow-btn arrow-btn-l js-switchLevelPrev"><span class="arrow-left arrow-icon <#--arrow-left-disable-->"></span></div>
                            <div class="mnt02-inner js-switchLevelBox" data-title="魔法之路列表">
                                <ul class="slides">
                                    <#assign currentLevel = ((level.level/3)?ceiling)!1, nextLevel = (level.level + (3 - level.level%3) + 1)!0, nextLevelVaule = (nextLevel * ( nextLevel - 1) * 3)!0>
                                    <#list 1..12 as i>
                                        <li data-index="${(i * 3 - 2)}" class="js-clickSignLevel" style="cursor: pointer;">
                                            <span class="magic-icon mcn-icon${i} <#if currentLevel lt i>magic-icon-disable</#if>"></span>
                                            <#if currentLevel == i>
                                                <#if currentLevel == 12>
                                                    <p class="tips magic-icons mlabel-green-big">已达到最高</p>
                                                <#else>
                                                    <p class="tips magic-icons mlabel-green-big">还有<span>${(nextLevelVaule - level.levelValue)!'--'}</span>点魔力值升级</p>
                                                </#if>
                                            </#if>
                                        </li>
                                    </#list>
                                </ul>
                            </div>
                            <div class="arrow-btn arrow-btn-r js-switchLevelNext"><span class="arrow-right arrow-icon"></span></div>
                        </div>
                    </div>
                </div>
                <div class="mn-bottom">
                    <div class="left mac-ranking">
                        <div class="tab">
                            <ul>
                                <li class="js-clickRankTab" data-type="week">本周魔法新星</li>
                                <li class="js-clickRankTab" data-type="rank">魔法师排行</li>
                            </ul>
                        </div>
                        <div class="main" id="rankContentBox"><#--content--></div>
                    </div>
                    <div class="right">
                        <#if (myActiveInfo.detailList?size gt 0)!false>
                            <div class="hd">
                                <div class="hd01" style="position:relative">我的唤醒进度<#if (myActiveInfo.detailList?size>0)><a class="js-remindQQStudent" target="_blank">去提醒</a></#if></div>
                                <div class="hd02"><p class="txt-white">发出${myActiveInfo.totalCount}次唤醒，成功${myActiveInfo.successCount}次</p><p>(快去请同学在倒计时结束前做作业吧)</p></div>
                            </div>
                            <div class="mn">
                                <table cellpadding="0" cellspacing="0">
                                    <#list myActiveInfo.detailList as myGrabInfo>
                                        <tr>
                                            <td class="avatar">
                                                <img src="<@app.avatar href="${(myGrabInfo.imgUrl)!}"/>"/>
                                            </td>
                                            <td class="name"><div>${(myGrabInfo.userName)!'--'}</div></td>
                                            <td class="time">
                                                <@countdown endDate= (myGrabInfo.endDate)!'2015/12/30 00:00:00' studentId = (myGrabInfo.userId)!0 />
                                            </td>
                                            <td class="state">
                                                未做作业
                                            </td>
                                        </tr>
                                    </#list>
                                </table>
                            </div>
                            <#--未完成倒计时-->
                            <#macro countdown endDate studentId>
                                <p class="hour"><span class="t_h1_${studentId}">0</span><span class="t_h2_${studentId}">0</span>时</p>
                                <p class="minute"><span class="t_m1_${studentId}">0</span><span class="t_m2_${studentId}">0</span>分</p>
                                <p class="second"><span class="t_s1_${studentId}">0</span><span class="t_s2_${studentId}">0</span>秒</p>
                                <script type="text/javascript">
                                    var NowTimeTemp_${studentId} = new Date("${.now}").getTime();
                                    function countdown() {
                                        var EndTime = new Date('${endDate}');
                                        NowTimeTemp_${studentId} += 1000;
                                        var t = EndTime.getTime() - NowTimeTemp_${studentId};
                                        var d = Math.floor(t / 1000 / 60 / 60 / 24);
                                        var h = Math.floor(t / 1000 / 60 / 60 % 24);
                                        var m = Math.floor(t / 1000 / 60 % 60);
                                        var s = Math.floor(t / 1000 % 60);

                                        if (s < 0) {
                                            return false;
                                        }

                                        if (d < 10) {
                                            d = "0" + d;
                                        }
                                        if (h < 10) {
                                            h = "0" + h;
                                        }
                                        if (m < 10) {
                                            m = "0" + m;
                                        }
                                        if (s < 10) {
                                            s = "0" + s;
                                        }
                                        $('.t_h1_'+${studentId}).html((h + "").substring(0,1));
                                        $('.t_h2_'+${studentId}).html((h + "").substring(1,2));
                                        $('.t_m1_'+${studentId}).html((m + "").substring(0,1));
                                        $('.t_m2_'+${studentId}).html((m + "").substring(1,2));
                                        $('.t_s1_'+${studentId}).html((s + "").substring(0,1));
                                        $('.t_s2_'+${studentId}).html((s + "").substring(1,2));
                                    }

                                    countdown();
                                    setInterval(countdown,1000);
                                </script>
                            </#macro>
                        <#else>
                            <div class="magic-dialog06">
                                <h3>我的唤醒进度</h3>
                                <div class="inner">
                                    <span class="cartoon"></span>
                                    <div class="txt"><p>还没有发出唤醒<br>快去唤醒沉睡的魔法师吧~</p></div>
                                </div>
                            </div>
                        </#if>

                    </div>
                </div>
            </div>

            <div id="footerPablic" data-type="0" data-service=""></div>
        </div>
    </div>

    <!-- 唤醒沉睡魔法师魔板-->
    <script type="text/html" id="T:唤醒沉睡魔法师弹窗">
        <div class="magic-dialog03 magic-dialog">
            <h3 class="h3-title">唤醒沉睡魔法师</h3>
            <span class="hat"></span>

                <p class="intro">发出唤醒后，去督促同学完成作业吧！</p>

                <% var dataMapOne = data.dataMap[1]?data.dataMap[1]:[], dataMapTwo = data.dataMap[2]?data.dataMap[2]:[]; %>
                <div class="inner">
                    <h3>第一层梦境:成功唤醒可得<var>3魔力值+1瓶药水</var></h3>
                    <%if (data.success && dataMapOne && dataMapOne.length > 0){%>
                        <ul data-level="1">
                            <%for (var i = 0 ; i < dataMapOne.length; i++){%>
                            <li class="towake" dataId = "<%=dataMapOne[i].userId%>">
                                <%if(dataMapOne[i].userAvatar != ""){%>
                                <img src="<@app.avatar href='<%==dataMapOne[i].userAvatar%>'/>" >
                                <%}else{%>
                                <img src="<@app.link href="public/skin/project/magiccastal/avatar-none.png"/>" >
                                <%}%>
                                <span class="magic-icons mbtn-success"></span>
                                <span class="name" style="display: block;width:71px;text-overflow: ellipsis;overflow: hidden;white-space: nowrap;vertical-align: middle;"><%=dataMapOne[i].userName%></span>
                            </li>
                            <%}%>
                        </ul>
                    <%}else{%>
                        <p style="clear: both;height:140px;text-align: left;padding-top:50px;padding-left:44px;">该梦境中暂无魔法师可唤醒哦~</p>
                        <span class="magic-pro-wakeup" style="top:72px;"></span>
                    <%}%>
                    <h3>第二层梦境:成功唤醒可得<var>5魔力值+2瓶药水</var></h3>
                    <%if (data.success && dataMapTwo &&dataMapTwo.length > 0 ){%>
                        <ul  data-level="2">
                            <%for (var i = 0 ; i < dataMapTwo.length; i++){%>
                            <li class="towake" dataId = "<%=dataMapTwo[i].userId%>">
                                <%if(dataMapTwo[i].userAvatar != ""){%>
                                <img src="<@app.avatar href='<%=dataMapTwo[i].userAvatar%>'/>" >
                                <%}else{%>
                                <img src="<@app.link href="public/skin/project/magiccastal/avatar-none.png"/>" >
                                <%}%>
                                <span class="magic-icons mbtn-success"></span>
                                <span class="name" style="display: block;width:71px;text-overflow: ellipsis;overflow: hidden;white-space: nowrap;vertical-align: middle;"><%=(dataMapTwo)[i].userName%></span>
                            </li>
                            <%}%>
                        </ul>
                    <%}else{%>
                        <p style="clear: both;height:140px;text-align: left;padding-top:50px;padding-left:44px;">该梦境中暂无魔法师可唤醒哦~</p>
                        <#--<span class="magic-pro-wakeup" style="top:332px;"></span>-->
                    <%}%>

                    <p class="describe">我的药水<span>${waterCount!0}</span>瓶，发出1次唤醒消耗1瓶，不足则扣除<span>5</span>学豆哦！</p>
                    <p class="info js-alertInfo" style="font-size: 14px;margin: -15px 0 8px 0; color: #f00; display: none;"></p>
                    <a href="javascript:void(0)" class="magic-icons mbtn-hx-small js-send-wake">发出唤醒</a>
                </div>

        </div>
    </script>
    <script id="T:魔法排行" type="text/html">
        <%if (data.rankList.length > 0){%>
            <table cellpadding="0" cellspacing="0">
                <thead>
                    <tr class="table-hd"><td class="ranking" style="padding-right: 20px;">排名</td><td class="name">姓名</td><td class="title">魔法称号</td><td class="count">魔力值</td></tr>
                </thead>
            </table>
            <div style="height: 185px;overflow-y: auto;">
            <table cellpadding="0" cellspacing="0">
                <tbody>
                    <%for (var i = 0 ; i < data.rankList.length; i++){%>
                        <%if ((data.rankList)[i].rank == 1 || (data.rankList)[i].rank == 2 || (data.rankList)[i].rank == 3){%>
                            <%if (type == "week"){%>
                                <tr><td class="ranking"><span class="magic-icons li-cell-icon li-cell-icon0<%=(data.rankList)[i].rank %>"></span></td><td class="name"><div class="txt-over "><%=(data.rankList)[i].studentName %></div></td><td class="title"><div class="txt-over "><%=(data.rankList)[i].levelName %></div></td><td class="count"><%=(data.rankList)[i].magicValue %></td></tr>
                            <%}else{%>
                                <tr><td class="ranking"><span class="ranking magic-icons li-badge-icon li-badge-icon0<%=(data.rankList)[i].rank %>"></span></td><td class="name"><div class="txt-over"><%=(data.rankList)[i].studentName %></div></td><td class="title"><div class="txt-over "><%=(data.rankList)[i].levelName %></div></td><td class="count"><%=(data.rankList)[i].magicValue %></td></tr>
                            <%}%>
                        <%}else{%>
                            <tr><td class="ranking"><span><%=(data.rankList)[i].rank %></span></td><td class="name"><div class="txt"><%=(data.rankList)[i].studentName %></div></td class="title"><td><%=(data.rankList)[i].levelName %></td><td class="count"><%=(data.rankList)[i].magicValue %></td></tr>
                        <%}%>
                    <%}%>
                </tbody>
            </table>
            </div>
        <%}else{%>
            <p class="nobody">还没有人登上榜单哦，</br> 魔法师们快努力吧！</p>
            <span class="magic-pro-rank"></span>
        <%}%>
    </script>
    <script type="text/html" id="T:查看魔法之路">
        <div class="magic-dialog01">
            <h3>魔法师进阶之路</h3>
            <ul class="clearfix">
                <%var levelKey = [1, 2, 3, 4, 8, 7, 6, 5, 9, 10, 11, 12]%>
                <%for(var i = 0; i < levelKey.length; i++){%>
                <li>
                    <span class="magic-icon mcn-icon<%=levelKey[i]%>"></span>
                    <%if( currentLevel == levelKey[i]){%>
                    <span class="tips magic-icons mlabel-green"><%=(maxLevel == currentLevel ? "我最高" : "我在这里")%></span>
                    <%}%>
                    <%if(maxLevel == levelKey[i] && maxLevel != currentLevel){%>
                    <span class="tips magic-icons mlabel-orange">班级最高</span>
                    <%}%>
                </li>
                <%}%>
            </ul>
        </div>
    </script>
    <script type="text/html" id="T:已达成的魔法师">
        <div class="magic-dialog02 magic-dialog">
            <h3 class="h3-title">已达成的魔法师：</h3>
            <span class="hat"></span>
            <p class="intro">达成该魔法称号需要<span> <%=(index * ( index - 1) * 3)%> </span>魔力值，我拥有<span> ${(level.levelValue)!0} </span>魔力值</p>
            <div class="inner">
                <%if(items.length > 0){%>
                <ul>
                    <%for(var i = 0; i < items.length; i++){%>
                    <li>
                        <%if(items[i].userAvatar != ""){%>
                            <img src="<@app.avatar href='<%=items[i].userAvatar%>'/>">
                        <%}else{%>
                            <img src="<@app.link href="public/skin/project/magiccastal/avatar-none.png"/>">
                        <%}%>
                        <p><%=items[i].userName%></p>
                    </li>
                    <%}%>
                </ul>
                <%}else{%>
                <div class="noman">
                    <span class="avatar-none"></span>
                    <p>还没有人获得这个魔法称号哦</p>
                </div>
                <%}%>
            </div>
        </div>
    </script>
    <script type="text/javascript">
        $(function(){
            //本周魔法新星 || 魔法师排行
            $(document).on("click", ".js-clickRankTab", function(){
                $(this).addClass("active").siblings().removeClass("active");
                var type = $(this).attr("data-type");
                $.post("/student/magic/loadrank.vpage", {type :type}, function(data){
                    $(".mac-ranking .main").html( template("T:魔法排行", {data : data, type : type}));
                });
            });

            //初始化排行
            $(".js-clickRankTab:first").click();

            function alertInfo(info){
                $(".js-alertInfo").text(info).slideDown();
                setTimeout(function(){
                    $(".js-alertInfo").slideUp(function(){
                        $(this).text("");
                    });
                }, 3000);
            }

            //选择唤醒同学
            var activeId = "",level="1";
            var selected;
            $(document).on("click", "li.towake", function(){
                $(".towake").removeClass("success");
                $(this).addClass("success");
                activeId = $(this).attr("dataId");
                level=$(this).parent().attr("data-level");
                selected = $(this);
            });

            //发出唤醒
            $(document).on("click", ".js-send-wake", function(){
                if($(this).hasClass("mbtn-hx-small-disable")){
                    return false;
                }

                if( $17.isBlank(activeId) ){
                    alertInfo("请选择你的同学~");
                    return false;
                }

                $.post("/student/magic/active.vpage", {activeId : activeId,activeLevel : level}, function(data){
                    if(data.success){
                        alertInfo("成功发出唤醒~");
                        activeId = "";
                        selected.remove();
                    }else{
                        alertInfo(data.info);
                    }
                });
            });

            //去唤醒魔法师
            $(document).on("click", ".js-ifhx", function(){
                if($(this).hasClass("mbtn-hx-disable")){
                    return false;
                }

                $.get("/student/magic/loadactivelist.vpage", {}, function(data){
                    $.prompt(template("T:唤醒沉睡魔法师弹窗", {data : data}),{
                        prefix : "magicSmall-jqi-popup",
                        buttons : {},
                        position : { width:560},
                        loaded : function(){
                            if(data.canActive){
                                $(".js-send-wake").removeClass("mbtn-hx-small-disable");
                            }else{
                                $(".js-send-wake").addClass("mbtn-hx-small-disable");
                                $(".js-alertInfo").text("每天最多发出10次唤醒~").slideDown();
                            }
                        },
                        classes : {
                            fade: 'jqifade'
                        }
                    });
                });
            });

            $(document).on("mouseover mouseleave" , ".js-hover-detail", function(){
                $(".magic-dialog04").toggleClass("d-none");
            });

            $(".js-switchLevelBox").flexslider({
                animation : "slide",
                animationLoop : true,
                slideshow : false,
                itemWidth : 186,
                direction : "horizontal",//水平方向
                controlNav : false,
                directionNav : false,
                startAt : ${((currentLevel/3)?ceiling - 1)!0},
                minItems : 3,
                slideToStart : 0,
                move : 3,
                start : function(slider){
                    $(document).on("click", ".js-switchLevelPrev", function(){
                        slider.flexAnimate(slider.getTarget("previous"), true);
                    });
                    $(document).on("click", ".js-switchLevelNext", function(){
                        slider.flexAnimate(slider.getTarget("next"), true);
                    });
                }
            });

            //查看魔法之路
            var clazzMaxLevel = null;
            $(document).on("click", ".js-clickDetailPopup", function(){
                if(clazzMaxLevel == null){
                    $.get("/student/magic/loadmaxmagician.vpage", {}, function(data){
                        clazzMaxLevel = data.maxMagician;
                        detailPPup(clazzMaxLevel.level);
                    });
                }else{
                    detailPPup(clazzMaxLevel.level);
                }

                function detailPPup(maxLevel){
                    var tpLevel = (maxLevel ? (parseInt(maxLevel/3) + 1) : -1);

                    $.prompt(template("T:查看魔法之路", {
                        currentLevel : ${currentLevel},
                        maxLevel : tpLevel
                    }), {
                        prefix : "magic-jqi-popup",
                        classes : { fade: 'jqifade' },
                        position : { width: 882},
                        //title: "魔法师进阶之路",
                        buttons: {}
                    });
                }
            });

            //已达成的魔法师
            var currentClazzLevelItems = {};
            $(document).on("click", ".js-clickSignLevel", function(){
                var $this = $(this);
                var $index = $this.data("index");

                if( $17.isBlank($index) ){ return false; }//index null false

                //缓存true
                if(currentClazzLevelItems[$index]){
                    detailPPup($index);
                }else{
                    $.post("/student/magic/loadsupermagician.vpage", {level : $index}, function(data){
                        if(data.success){
                            currentClazzLevelItems[$index] = data.superList;
                            detailPPup($index);
                        }else{
                            $17.alert(data.info);
                        }
                    });
                }

                function detailPPup(index){
                    $.prompt(template("T:已达成的魔法师", { items : currentClazzLevelItems[index] ? currentClazzLevelItems[index] : [], index : $index }), {
                        prefix : "magicSmall-jqi-popup",
                        classes : { fade: 'jqifade' },
                        position : { width: 570},
                        buttons: {}
                    });
                }
            });

            var remindQQStudent=$(".js-remindQQStudent");
            if(remindQQStudent.length){
                var p = {
                    url : location.href,
                    desc : '嗨小伙伴，老师布置的作业你还没有做哦，老师马上就检查作业了！快点到“一起作业网”做作业吧',
                    title:'老师布置的作业你还没有做哦',
                    summary:'嗨小伙伴，老师布置的作业你还没有做哦，老师马上就检查作业了！快点到“一起作业网”做作业吧',
                    pics: '<@app.link href="public/skin/project/magiccastal/magiclogo.jpg"/>',
                    site:'一起作业网'
                };

                var s = [];
                for(var i in p){
                    s.push(i + '=' + encodeURIComponent(p[i] || ''));
                }
                remindQQStudent.attr("href","//connect.qq.com/widget/shareqq/index.html?"+s.join('&'));
            }
        });
    </script>
</@temp.page>
