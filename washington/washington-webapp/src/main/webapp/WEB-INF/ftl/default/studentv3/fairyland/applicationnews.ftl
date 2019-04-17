<div  class="pc-center">
    <div id="applicationNewsListBox"></div>
    <div class='message_page_list' ></div>
</div>


<script id="t:班级应用动态" type="text/html">
    <%for(var i = 0; i < news.length; i++){%>
        <dl>
            <dt>
                <%if(news[i].relevantUserImg){%>
                    <%if(news[i].relevantUserId == 99999){%>
                        <img src="<@app.link href='public/skin/common/images/soplugin/systemAvatar.png'/>" width="60" height="60">
                    <%}else{%>
                        <img src="<@app.avatar href='<%=news[i].relevantUserImg%>'/>" width="60" height="60">
                    <%}%>
                <%}else{%>
                    <img src="<@app.link href='public/skin/studentv3/images/avatar.png'/>" width="60" height="60">
                <%}%>
            </dt>
            <dd>
                <div class="t-pk-skin">
                    <i class="t-pk-arrow"></i>
                    <div class="mes-info">
                        <%switch (news[i].journalType){

                        case "TALENT_PARKOUR_RESULT_SHARE": %>
                            <!--单词达人跑酷模式游戏结果分享  TALENT_PARKOUR_RESULT_SHARE -->
                            <div class="evanWordmaster">
                                <div class="evanWordmaster_main">
                                    <span class="best"><%=news[i].param.topHistory%></span>
                                    <div class="evanWordmaster_info">
                                        <p class="distance"><%=news[i].param.distance%></p>
                                        <p class="preformence"><%=news[i].param.expressionScore%></p>
                                        <p class="range"><%=news[i].param.score%></p>
                                    </div>
                                    <%if(news[i].param.surpassList.length > 0) {%>
                                    <div class="evanWordmaster_list">
                                        <ul>
                                            <%for(var j = 0; j < news[i].param.surpassList.length; j++){%>
                                            <li>
                                                <img width="43" height="43" src="<@app.avatar href='<%=news[i].param.surpassList[j].userAvatar%>'/>" onerror="this.onerror='';this.src='<@app.avatar href=""/>'"/>
                                                <p><%=news[i].param.surpassList[j].userName%></p>
                                            </li>
                                            <%}%>
                                        </ul>
                                    </div>
                                    <%}else{%>
                                    <div class="evanWordmaster_list text_center text_gray_9">暂无同学名单</div>
                                    <%}%>
                                </div>
                                <div class="evanpublic_info">
                                    <span>挑战最高分，赶快来单词达人吧！</span>
                                </div>
                            </div>
                        <%break; case "TALENT_PARKOUR_WEEK_RANK": %>
                            <!--TALENT_PARKOUR_WEEK_RANK("单词达人跑酷模式周排行")-->
                            <div class="evanWordparkour">
                                <div class="evanWordparkour_main">
                                    <div class="evanWordparkour_table">
                                        <table>
                                            <tr>
                                                <td style="width: 120px;">姓名</td>
                                                <td style="width: 80px">排名</td>
                                                <td style="width: 80px">学豆</td>
                                                <td style="width: 80px" >PK活力值</td>
                                            <#--<td style="width: 80px" >天空PK套装</td>-->
                                            </tr>
                                            <%for(var k = 0; k < news[i].param.rank.length; k++){%>
                                            <tr>
                                                <td><%=news[i].param.rank[k].userName%></td>
                                                <td>
                                                    <%if(news[i].param.rank[k].rank == 1){%>
                                                    <i class="evan_img_public evan_gold_ys"></i>
                                                    <%}else if(news[i].param.rank[k].rank == 2){%>
                                                    <i class="evan_img_public evan_silver_ys"></i>
                                                    <%}else{%>
                                                    <i class="evan_img_public evan_copper_ys"></i>
                                                    <%}%>
                                                </td>
                                                <td><%=news[i].param.rank[k].integral%></td>
                                                <td><%=news[i].param.rank[k].pk%></td>
                                            <#--<td><%=news[i].param.rank[k].arena%></td>-->
                                            </tr>
                                            <%}%>
                                        </table>
                                    </div>
                                </div>
                                <div class="evanpublic_info">
                                    <span>争夺冠军，赶快来单词达人吧！</span>
                                </div>
                            </div>

                        <%break; case "STUDENT_BUY_AFENTI": %>
                            <#--阿分题推广-->
                            <div>
                                <img src="<@app.link href="public/skin/student/images/myclass/paymodule/afenti.png"/>"/>
                            </div>
                            <div class="evanpublic_info">
                                <span><%==news[i].param.content%></span>
                                <a class="evanpublic_btn" target="_blank" href="/apps/afenti/order/exam-cart.vpage?refer=300003">去看看</a>
                            </div>
                        <%break; case "STUDENT_BUY_TRAVEL_AMERICA": %>
                            <#--走遍美国购买分享-->
                            <div>
                                <img width="465" src="<@app.link href='public/skin/project/afenti/travel/travel-sp-banner.png'/>"/>
                            </div>
                            <div class="evanpublic_info">
                                <span><%==news[i].param.content%></span>
                                <a class="evanpublic_btn" target="_blank" href="/student/apps/index.vpage?app_key=TravelAmerica">去看看</a>
                            </div>
                        <%break; case "TALENT_PARKOUR_RANKUP_SHARE": %>
                            <!--TALENT_PARKOUR_RANKUP_SHARE("单词达人跑酷模式排行上升分享");-->
                            <div class="evanWordrange">
                                <div class="evanWordrange_main">
                                    <div class="evanWordrange_box">
                                        <div class="evanWordrange_left">
                                            <dl>
                                                <dt>
                                                    <span class="evanWordrange_img">
                                                        <img src="<@app.avatar href='<%=news[i].param.userAvatar%>'/>" onerror="this.onerror='';this.src='<@app.avatar href=""/>'" width="60" height="60"/>
                                                    </span>
                                                </dt>
                                                <dd>
                                                    <span><%=news[i].param.userName%></span><i class="evan_img_public evan_copper_lv"></i>
                                                    <strong><%=news[i].param.userLevel%></strong>
                                                    <p class="p1" ><%=news[i].param.topScore%></p>
                                                    <p>排名升至第<%=news[i].param.rank%>位！</p>
                                                </dd>
                                            </dl>
                                        </div>

                                        <%if(news[i].param.rankDropUserListInfo != undefined) {%>
                                        <div class="evanWordrange_right">
                                            <ul>
                                                <%for(var l = 0; l < news[i].param.rankDropUserListInfo.length; l++){%>
                                                <li>
                                                    <span class="evanWordrange_img">
                                                        <img onerror="this.onerror='';this.src='<@app.avatar href=""/>'" src="<@app.avatar href='<%=news[i].param.rankDropUserListInfo[l].userAvatar%>'/>" width="43" height="43"/>
                                                    </span>
                                                    <p><%=news[i].param.rankDropUserListInfo[l].userName%></p>
                                                    <p><i class="evan_img_public evan_copper_lv"></i><strong><%=news[i].param.rankDropUserListInfo[l].userLevel%></strong></p>
                                                </li>
                                                <%}%>
                                            </ul>
                                        </div>
                                        <%}else{%>
                                        <div class="evanWordmaster_list text_center text_gray_9">暂无同学名单</div>
                                        <%}%>
                                    </div>
                                </div>
                                <div class="evanpublic_info">
                                    <span>快速提升排名，赶快来单词达人吧！</span>
                                </div>
                            </div>
                        <%break; case "EXCHANGE_COUPON_LELANGLEDU": %>
                            <!--EXCHANGE_COUPON_LELANGLEDU("学生兑换乐朗乐读优惠劵");-->
                            <div class="winners_clazz_zoom"></div>
                            <div>
                                参加暑假夏令营，你也想去北京？ <a href="/student/reward/coupon.vpage?type=lelang" target="_blank" class="winners_clazz_zoom winners_clazz_zoom_submit"></a>
                            </div>

                        <%break; case "APP_SHARE": %>
                            <!--APP_SHARE("第三方应用班级空间分享");-->
                        <#--通用APP-->
                        <div class="drawing_main_box">
                                <%if(news[i].param.share_img != ""){%>
                                <div class="drawing_con_box">
                                <p class="title">
                                        <span class="font">
                                            <%=news[i].param.app_name%>
                                        </span>
                                    <span class="arrow"></span>
                                </p>
                                <div class="content">
                                    <p>
                                            <a onclick="$17.tongji('学生-班级空间-<%=news[i].param.app_name%>-观看作品')" href="javascript:void(0);" data-url="<%=news[i].param.share_img_link%>" class="watchWorksBtn"><img src="<%=news[i].param.share_img%>" width="229" height="143"></a>
                                        </p>
                                    </div>
                                </div>
                                <%}%>
                                <div class="evanpublic_info">
                                    <span><%if(news[i].param.share_text_link != ""){%><a target="_blank" href="javascript:void(0);" ><%==news[i].param.share_text%></a><%}else{%><%==news[i].param.share_text%><%}%></span>
                                    <a class="evanpublic_btn" onclick="$17.tongji('学生-班级空间-<%=news[i].param.app_name%>-去看看')" href="<%=news[i].param.app_url%>">去看看</a>
                                </div>
                            </div>
                        <%break; case "BABEL_WIN_PRIZE": %>
                            <#--通天塔分享-->
                            <div>
                                <img width="50" height="50" src="<@app.link_shared href=''/><%=news[i].param.img%>"/>
                            </div>
                            <div class="evanpublic_info">
                                <span style="padding:0; width: 360px;"><%=news[i].param.text%><b <%if(news[i].param.itemType == "PK_ITEM"){%>style="color:#<%=news[i].param.color%>"<%}%>><%=news[i].param.itemName%></b></span>
                                <a class="evanpublic_btn" target="_blank" onclick="$17.tongji('通天塔入口-课外乐园去挑战')" href="/student/babel/api/index.vpage">去挑战</a>
                            </div>
                        <%break; case "WALKER_ADVENTURE": %>
                            <#-- 沃克单词冒险在班级空间、应用中心分享 "CROWN":皇冠;   "EXCHANGE":PK武器兑换;   "BEYOND"：班级超越-->
                            <%if(news[i].param.type == 'CROWN'){%>
                                <div>
                                    <img style="width: 44px; height: 41px;" src="<@app.link href="public/skin/studentv3/images/myclass/paymodule/crown.png"/>"/>
                                </div>
                                <div class="evanpublic_info">
                                    <span><%==news[i].param.content%></span>
                                    <a class="evanpublic_btn" onclick="$17.atongji('学生-课外乐园-沃克单词冒险-去挑战','/student/nekketsu/adventure.vpage')" href="javascript:void (0);">去挑战</a>
                                </div>
                            <%}else if(news[i].param.type == 'EXCHANGE'){%>
                                <div>
                                    <img style="width: 80px; height: 80px;" src="<%==news[i].param.img%>"/>
                                </div>
                                <div class="evanpublic_info">
                                    <span><%==news[i].param.content%></span>
                                    <a class="evanpublic_btn" onclick="$17.atongji('学生-课外乐园-沃克单词冒险-去挑战','/student/nekketsu/adventure.vpage')" href="javascript:void (0);">去挑战</a>
                                </div>
                            <%}else{%>
                                <span><%==news[i].param.content%></span>
                                <div class="">
                                    <ul>
                                        <%if(news[i].param.classmates.length > 0){%>
                                            <%for(var j=0; j < news[i].param.classmates.length;j++){%>
                                                <li style="float:left; width: 100px; text-align: center; padding: 10px 0;">
                                                    <div style="cursor: default;" class="avatar">
                                                        <img style="width: 60px; height: 60px;" src="<%=news[i].param.classmates[j].img%>">
                                                    </div>
                                                    <div class="title"><%=news[i].param.classmates[j].name%></div>
                                                </li>
                                            <%}%>
                                        <%}%>
                                    </ul>
                                </div>
                                <div class="evanpublic_info" style="clear:both; text-align: right;">
                                    <a class="evanpublic_btn" onclick="$17.atongji('学生-课外乐园-沃克单词冒险-去挑战','/student/nekketsu/adventure.vpage')" href="javascript:void (0);">去挑战</a>
                                </div>
                            <%}%>
                        <%break; default: %>
                            <#--基本模板-->
                            <%==news[i].param.content%>
                        <%}%>
                    </div>
                </div>
                <p class="date-time"><%=news[i].date%></p>
                <#--评论-->
                <%var commentCount=0%>
                <%for(var j = 0; j < news[i].comments.length; j++){%>
                <%commentCount= commentCount + news[i].comments[j]['count']%>
                <%}%>
                <p class="comment_but laud-count" data-journal_id="<%=news[i].journalId%>">
                    <a href="javascript:void(0);">
                        <span class="w-icon w-icon-1"></span>
                        <span class="canCommentText"><%if(news[i].canComment){%>已评论<%}else{%>评论<%}%></span>
                        (<span class="commentCountText"><%=commentCount%></span>)
                    </a>
                </p>

                <#--赞-->
                <p class="laud-count" <%if(news[i].names.length > 0){%>data-title="<%for(var x = 0; x < news[i].names.length; x++){%><%=news[i].names[x]%><%if(x < (news[i].names.length - 1)){%>，<%}%><%}%> 觉得很赞"<%}%>>
                    <%if(news[i].canLike){%>
                        <a href="javascript:void(0);" class="studentSpacePraise" data-journalId="<%=news[i].journalId%>" data-clazzId="<%=news[i].clazzId%>" data-relevantUserId="<%=news[i].relevantUserId%>">
                            <span class="w-icon w-icon-2"></span>
                            <span class="canCommentText text_f">赞</span>
                        </a>
                    <%}else{%>
                        <span class="w-icon w-icon-2"></span>己赞
                    <%}%>
                    (<span class="commentCountText count" data-count="<%=news[i].likeCount%>"><%=news[i].likeCount%></span>)
                </p>

                <#--删除-->
                <%if(news[i].relevantUserId == ${(currentUser.id)!}){%>
                    <p class="laud-count">
                        <a href="javascript:void(0);" data-journalId="<%=news[i].journalId%>" class="removeFreshButton">
                            <span class="w-icon w-icon-3"></span>
                            <span class="canCommentText">删除</span>
                        </a>
                    </p>
                <%}%>

                <!-- 表情 -->
                <div class="expressionBox" style="display:none;">
                    <span class="arrow">◆<span class="inArrow">◆</span></span>
                    <ul id="comment_img_list_box" data-journal_id="<%=news[i].journalId%>" data-clazz_id="<%=news[i].clazzId%>" data-relevant_user_id="<%=news[i].relevantUserId%>">
                        <%for(var j = 0; j < news[i].comments.length; j++){%>
                        <li data-can_comment="<%=news[i].canComment.toString()%>" data-c_img_id="<%=j+1%>" data-count='<%=news[i].comments[j]['count']%>' data-students_name="<%for(var k = 0; k < news[i].comments[j]['names'].length; k++){%><%=news[i].comments[j]['names'][k]%> <%if(news[i].comments[j]['names'].length-1-k != 0){%>,<%}%> <%}%>" >
                        <p class="express <%if(news[i].canComment){%>default<%}%>">
                            <%if(news[i].comments[j]['count'] > 0) {%>
                            <img src="<@app.link href="/public/skin/common/images/expression/e<%=j+1%>.gif?1.0.1"/>" />
                            <%}else{%>
                            <img src="<@app.link href="/public/skin/common/images/expression/eg<%=j+1%>.png?1.0.1"/>" />
                            <%}%>
                        </p>
                        <span class="count" <%if(news[i].comments[j]['count'] < 1) {%>style="display: none"<%}%>><%=news[i].comments[j]['count']%></span>
                        </li>
                        <%}%>
                    </ul>
                </div>
            </dd>
        </dl>
    <%}%>


</script>

<script type="text/javascript">
    function showQtips($this, content,width,mTop){
        if($17.isBlank(content)){return}
        mTop = $17.isBlank(mTop) ? 0 : mTop;
        $this.qtip({
            content: {
                text: content
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
                adjust : {
                    y : mTop
                }
            },
            style : {
                classes : 'qtip-rounded',
                width: width
            }
        });

    }

    function statePageList(page){
        var applicationNewsListBox = $("#applicationNewsListBox");
        applicationNewsListBox.html('<div style="padding: 50px 0; text-align: center;"><img src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>');
        $.get('/student/fairyland/news.vpage', {currentPage : page}, function(data){
            if(data.success){
                var content = data.journalPage.content;
                if(content.length > 0){
                    applicationNewsListBox.html(template("t:班级应用动态", {
                        news : content
                    }));

                    $(".laud-count").each(function(){
                        showQtips($(this), $(this).data("title"), 220);
                    });


                    $(".message_page_list").page({
                        total           : data.journalPage.totalPages,
                        current         : data.journalPage.number + 1,
                        autoBackToTop   : false,
                        jumpCallBack    : function(index){
                            $.get('/student/fairyland/news.vpage', {currentPage : index}, function(data){
                                if(data.success){
                                    applicationNewsListBox.html(template("t:班级应用动态", {
                                        news : data.journalPage.content
                                    }));

                                    $(".laud-count").each(function(){
                                        showQtips($(this), $(this).data("title"), 220);
                                    });
                                }
                            });
                        }
                    });
                }else{
                    applicationNewsListBox.html("<div style='padding: 20px 300px;'>暂无班级应用动态</div>");
                    $(".message_page_list").html("");
                }
            }else{
                applicationNewsListBox.html("<div style='padding: 50px 0; text-align: center;'>暂无班级应用动态</div>");
            }
        });
    }



    $(function(){
        //初始化
        statePageList(1);


        //赞
        $(".studentSpacePraise").live("click", function(){
            var $this = $(this);
            var count = $this.siblings(".count");
            var laudCount = $this.parents(".laud-count");
            var laudCountVal = laudCount.attr("data-title");
            $.post("/student/clazz/like.vpage", {
                journalId        : $this.attr("data-journalId"),
                clazzId          : $this.attr("data-clazzId"),
                relevantUserId  : $this.attr("data-relevantUserId")
            }, function(data){
                if(data.success){
                    if($17.isBlank(laudCountVal)){
                        laudCount.attr("data-title", "${(currentUser.profile.realname)!''} 觉得很赞");
                    }else{
                        var laudCountNewVal = laudCountVal.replace(/觉得很赞/, "，${(currentUser.profile.realname)!''} 觉得很赞");
                        laudCount.attr("data-title", laudCountNewVal);
                    }

                    showQtips($this.parents(".laud-count"), $this.parents(".laud-count").attr("data-title"), 220);

                    $this.find(".space-icon").addClass("space-icon-5").removeClass("space-icon-19");
                    $this.find('.text_f').text('');
                    $this.after("己赞");
                    $this.css({ cursor: 'default'}).removeClass("studentSpacePraise");
                    count.text(parseInt(count.data("count")) + 1);
                }
            });
        });

        //评论
        $(".comment_but").live('click', function(){
            $(this).siblings(".expressionBox").toggle();
        });

        var commentListBoxLi = $("#comment_img_list_box li");

        commentListBoxLi.live('click', function(){
            var $this = $(this);
            var journalId = $this.parent().data('journal_id');
            var clazzId = $this.parent().data('clazz_id');
            var relevantUserId = $this.parent().data('relevant_user_id');
            var imgId = $this.data('c_img_id');
            var count = $this.data('count');
            var studentsName = $this.data('students_name');
            var canComment = $this.data('can_comment');

            if(canComment){
                return false;
            }
            var data = {
                journalId : journalId,
                clazzId : clazzId,
                relevantUserId : relevantUserId,
                imgId : imgId
            };

            /*表情*/
            if($this.hasClass('loading')){return false}
            $this.addClass('loading');
            $.post("/student/clazz/czcomment.vpage", data, function(data){
                if(data.success){
                    var comma = count > 0 ? "," : "";
                    var canCommentText = $this.closest("dd").find(".comment_but .canCommentText");
                    var commentCountText = $this.closest("dd").find(".comment_but .commentCountText");
                    $17.tongji('学生-班级空间-评论-成功');
                    $this.parent().find('li').data('can_comment', true);
                    $this.find(".express img").attr("src", "<@app.link href='/public/skin/common/images/expression/e"+ imgId +".gif?1.0.1'/>");
                    //更新评论数
                    $this.find(".count").show().html(parseInt(count + 1));
                    $this.parent("ul").find(".express").addClass("default");

                    canCommentText.text("已评论");
                    commentCountText.text(parseInt(commentCountText.text()*1 + 1));
                    //更新评论人列表
                    $this.data('students_name',studentsName + comma +'${(currentUser.profile.realname)!}' );

                    $("#comment_img_list_box li").each(function(){
                        var $this = $(this);
                        var studentName = $this.data('students_name');
                        showQtips($this,studentName,220);
                    });
                }else{
                    $17.tongji('学生-班级空间-评论-失败');
                    $17.alert(data.info);
                }
                $this.removeClass('loading');
            });
        });

        commentListBoxLi.find(".express").live("mouseenter", function(){
            var $this = $(this);
            var imgId = $this.parent().data('c_img_id');
            if(!$this.hasClass("default")){
                $this.find("img").attr("src", "<@app.link href='/public/skin/common/images/expression/e"+ imgId +".gif?1.0.1'/>");
            }
        }).live("mouseleave", function(){
            var $this = $(this);
            var imgId = $this.parent().data('c_img_id');
            if($this.siblings(".count").text() < 1){
                $this.find("img").attr("src", "<@app.link href='/public/skin/common/images/expression/eg"+ imgId +".png?1.0.1'/>");
            }
        });

        //删除
        $(".removeFreshButton").live("click", function(){
            var $this = $(this);
            var journalId = $this.attr("data-journalid");

            $.prompt("<div style='padding: 30px 0 20px; text-align: center;'>确定删除本条新鲜事？</div>", {
                title : "提示",
                buttons : { "取消": false, "确定" : true},
                focus : 1,
                position: { width: 400 },
                submit: function(e, v){
                    if(v){
                        //post url
                        $.post("/student/clazz/delMyJournal.vpage", { journalId : journalId }, function(data){
                            if(data.success){
                                //remove success
                                $this.closest("dl").slideUp(300, function(){
                                    $(this).remove();
                                });

                            }else{
                                $17.alert(data.info);
                            }
                        });
                    }
                }
            });
        });

        //点击观看作品
        $(document).on("click", ".watchWorksBtn", function(){
            var $this =$(this);

            var dataUrl = $this.data("url");

            if(dataUrl != ""){
                $.prompt("<iframe class='vox17zuoyeIframe'width='700' height='470' frameborder='0' scrolling='no' frameborder='0' src='"+ dataUrl +"'></iframe>", {
                    title : "看看",
                    position : {width: 760},
                    buttons : {}

                });
            }
        });
    });
</script>