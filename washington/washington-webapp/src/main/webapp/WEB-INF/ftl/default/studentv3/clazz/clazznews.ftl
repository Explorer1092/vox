<#-- 学生班级动态 -->
<#macro clazznews>
    <@sugar.capsule js=["jcarousel", "qtip"] css=["plugin.jcarousel", "plugin.jquery.qtip"] />

    <#-- 我的班级-班级动态 -->
    <div class="my-space-novelty">
        <div class="tab">
            <span data-isload="false" data-newtype="all">班级新鲜事<i>◆</i></span>
            <span data-isload="false" data-newtype="personal">我的新鲜事<i>◆</i></span>
            <span data-isload="false" data-newtype="popularity" style="position: relative;">我的人气<i>◆</i><span id="alarm" style="display: none;"></span></span>
        </div>
        <div class="container">
            <div id="all"></div>
            <div id="personal"></div>
            <div id="popularity"></div>
            <div class='message_page_list' ></div>
        </div>
    </div>

    <script id="t:班级动态" type="text/html">
        <%for(var i = 0; i < news.length; i++){%>
            <dl>
                <dt <%if(news[i].relevantUserType == "STUDENT" && news[i].relevantUserId != ${(currentUser.id)!}){%>class="studentAvatar" data-student_id= <%=news[i].relevantUserId%> <%}%>  <%if(news[i].relevantUserId == 99999){%>style="background: none;"<%}%>>
                    <%if(news[i].relevantUserImg){%>
                        <%if(news[i].relevantUserId == 99999){%>
                            <img src="<@app.link href='public/skin/common/images/soplugin/systemAvatar.png'/>" width="60" height="60">
                        <%}else{%>
                            <img src="<@app.avatar href='<%=news[i].relevantUserImg%>'/>" width="60" height="60">
                        <%}%>
                    <%}%>
                    <i class='icon_general <%if(news[i].relevantUserType == "TEACHER"){%> icon_general_9 <%}%>'></i>
                </dt>
                <dd>
                    <div class="ms-space-skin ms-space-skin-<%=news[i].bubble%>">
                        <i class="ms-space-arrow"></i>
                        <i class="ms-space-icon"></i>
                        <i class="ms-space-icon ms-space-icon-1"></i>
                        <i class="ms-space-icon ms-space-icon-2"></i>
                        <i class="ms-space-icon ms-space-icon-3"></i>
                        <div class="mes-info">
                            <%if(news[i].journalType == "CLAZZ_LEAK"){%>
                            <%}else if(news[i].journalType == "BABEL_WIN_PRIZE") {%>
                            <%}else if(news[i].journalType == "STUDENT_BUY_TRAVEL_AMERICA") {%>
                            <%}else if(news[i].journalType == "SANGUO_SHARE") {%>
                            <%}else if(news[i].journalType == "TALENT_PARKOUR_RESULT_SHARE") {%>
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
                            <%}else if(news[i].journalType == "TALENT_PARKOUR_WEEK_RANK"){%>
                                <!-- TALENT_PARKOUR_WEEK_RANK("单词达人跑酷模式周排行")-->
                                <%if(news[i].param.rank != undefined) {%>
                                    <div class="evanWordparkour">
                                        <div class="evanWordparkour_main">
                                            <h4 class="title basic_title"></h4>
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
                                <%}else{%>
                                    <%==news[i].param.content%>
                                <%}%>
                            <%}else if(news[i].journalType == "STUDENT_BUY_AFENTI"){%>
                                <#--阿分题推广-->
                               <#-- <div>
                                    <img src="<@app.link href="public/skin/student/images/myclass/paymodule/afenti.png"/>"/>
                                </div>-->
                                <div class="evanpublic_info" style="margin-top: 0;">
                                    <span style="display: inline-block; vertical-align: middle; width: 300px; padding-left: 0;"><%==news[i].param.content%></span>
                                    <a class="evanpublic_btn" target="_blank" onclick="$17.tongji('学生-班级空间-分享阿分题-去看看')" href="/apps/afenti/order/exam-cart.vpage?refer=300003">去看看</a>
                                </div>
                            <%}else if(news[i].journalType == "APP_SHARE"){%>
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
                            <%}else if(news[i].journalType == "TALENT_PARKOUR_RANKUP_SHARE"){%>
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
                            <%}else if(news[i].journalType == "WALKER_ADVENTURE"){%>
                            <#-- 沃克单词冒险在班级空间、应用中心分享 "CROWN":皇冠;   "EXCHANGE":PK武器兑换;   "BEYOND"：班级超越-->
                                <%if(news[i].param.type == 'CROWN'){%>
                                    <div>
                                        <img style="width: 44px; height: 41px;" src="<@app.link href="public/skin/studentv3/images/myclass/paymodule/crown.png"/>"/>
                                    </div>
                                    <div class="evanpublic_info">
                                        <span><%==news[i].param.content%></span>
                                        <a class="evanpublic_btn" onclick="$17.atongji('学生-班级空间-沃克单词冒险-去挑战','/student/nekketsu/adventure.vpage')" href="javascript:void (0);">去挑战</a>
                                    </div>
                                <%}else if(news[i].param.type == 'EXCHANGE'){%>
                                    <div>
                                        <img style="width: 80px; height: 80px;" src="<%==news[i].param.img%>"/>
                                    </div>
                                    <div class="evanpublic_info">
                                        <span><%==news[i].param.content%></span>
                                        <a class="evanpublic_btn" onclick="$17.atongji('学生-班级空间-沃克单词冒险-去挑战','/student/nekketsu/adventure.vpage')" href="javascript:void (0);">去挑战</a>
                                    </div>
                                <%}else{%>
                                    <span><%==news[i].param.content%></span>
                                    <div class="userListBox">
                                        <ul>
                                            <%if(news[i].param.classmates.length > 0){%>
                                                <%for(var j=0; j < news[i].param.classmates.length;j++){%>
                                                    <li>
                                                        <div style="cursor: default;" class="avatar">
                                                            <img style="width: 60px; height: 60px;" src="<%=news[i].param.classmates[j].img%>">
                                                        </div>
                                                        <div class="title"><%=news[i].param.classmates[j].name%></div>
                                                    </li>
                                                <%}%>
                                            <%}%>
                                        </ul>
                                    </div>
                                    <div class="evanpublic_info">
                                        <a class="evanpublic_btn" onclick="$17.atongji('学生-班级空间-沃克单词冒险-去挑战','/student/nekketsu/adventure.vpage')" href="javascript:void (0);">去挑战</a>
                                    </div>
                                <%}%>
                            <%}else if(news[i].journalType == "SMARTCLAZZ_REWARD_WEEKLY_RANK"){%>
                                <!-- SMARTCLAZZ_REWARD_WEEKLY_RANK("智慧教室奖励周排行")-->
                                <%if(news[i].param.rank != undefined) {%>
                                    <div class="evanWordparkour">
                                        <div class="evanWordparkour_main">
                                            <h4 class="title smart_title"></h4>
                                            <div class="evanWordparkour_table">
                                                <table>
                                                    <tr>
                                                        <td style="width: 120px;">姓名</td>
                                                        <td style="width: 80px">排名</td>
                                                        <td style="width: 80px">获得奖励(学豆)</td>
                                                    </tr>
                                                    <%for(var k = 0; k < news[i].param.rank.length; k++){%>
                                                    <tr>
                                                        <td><%=news[i].param.rank[k].studentName%></td>
                                                        <td>
                                                            <%if(news[i].param.rank[k].rank == 1){%>
                                                            <i class="evan_img_public evan_gold_ys"></i>
                                                            <%}else if(news[i].param.rank[k].rank == 2){%>
                                                            <i class="evan_img_public evan_silver_ys"></i>
                                                            <%}else{%>
                                                            <i class="evan_img_public evan_copper_ys"></i>
                                                            <%}%>
                                                        </td>
                                                        <td><%=news[i].param.rank[k].totalIntegral%></td>
                                                    </tr>
                                                    <%}%>
                                                </table>
                                            </div>
                                        </div>
                                        <div class="evanpublic_info">
                                            <span>在课堂上好好表现，即有机会上榜哦，加油吧！</span>
                                        </div>
                                    </div>
                                <%}else{%>
                                    <%==news[i].param.content%>
                                <%}%>
                            <%}else if(news[i].journalType == "GROUPON_INVITE"){%>
                                <#--团购邀请-->
                                <div class="space-clazz-chat-box">
                                    <div class="font">
                                        <%==news[i].param.content%>
                                    </div>
                                    <p class="share-btn">
                                        <a class="orange-share-btn" onclick="$17.atongji('学生-班级空间-团购-查看','/groupon/index.vpage');" href="javascript:void (0)">去看看</a>
                                    </p>
                                </div>
                            <%}else{%>
                                <%==news[i].param.content%>
                            <%}%>

                            <%if(news[i].journalType == "CHANGE_BUBBLE" && news[i].relevantUserId != ${(currentUser.id)!}){%>
                                <a href="javascript:void(0)" class="bubble_but"><i class="space-icon space-icon-18"></i></a>
                            <%}%>

                            <%if(news[i].journalType == "BIRTHDAY" && news[i].param.studentId != ${(currentUser.id)!}){%>
                                <div class="receive-sendGifts">
                                    <div class="set-birthday">
                                        <a href="/student/center/information.vpage"><span class="set-birthday-icon"></span> 设置我的生日</a>
                                    </div>
                                    <div class="btn">
                                        <a href="javascript:void(0);" class="send-gift-btn send-gift-button" data-student_id="<%=news[i].param.studentId%>">
                                            <i class="send-gift-icon"></i>
                                            送礼物
                                        </a>
                                    </div>
                                </div>
                            <%}%>

                            <%if(news[i].journalType == "SEND_BIRTHDAY_GIFT" && news[i].relevantUserId != ${(currentUser.id)!}){%>
                                <div class="receive-sendGifts">
                                    <div class="set-birthday">
                                        <a href="/student/center/information.vpage"><span class="set-birthday-icon"></span> 设置我的生日</a>
                                    </div>
                                    <div class="send-gift-image"><img src="<@app.link href='public/skin/common/images/gift/<%=news[i].param.img%>'/>" width="65" height="65"></div>
                                    <div class="btn">
                                        <a href="javascript:void(0);" class="send-gift-btn send-gift-button" data-student_id="<%=news[i].param.receiverId%>">
                                            <i class="send-gift-icon"></i>
                                            送礼物
                                        </a>
                                    </div>
                                </div>
                            <%}%>

                            <%if(news[i].journalType == "SIGN_IN"){%><p class="gift-img"><img src="<@app.link href='public/skin/common/images/mood/<%=news[i].param.img%>'/>" height="100" ></p><%}%>
                            <%if(news[i].journalType == "SEND_GIFT"){%><p class="gift-img"><img src="<@app.link href='public/skin/common/images/gift/<%=news[i].param.img%>'/>" width="65" height="65"></p><%}%>

                            <%if(news[i].journalType == "STUDENT_UPLOAD_PHOTO"){%>
                            <div class="myclassShowPhoto">
                                <%for(var m = 0, len = news[i].param.photos.length; m < len; m++){%>
                                <span data-content-id="<%=news[i].param.photos[m]%>"><img src="<@app.avatar href='<%=news[i].param.photos[m]%>'/>" width="72" height="72"/></span>
                                <%}%>
                            </div>
                            <%}%>

                            <#--乐朗乐读广告推广-->
                            <%if(news[i].journalType == "EXCHANGE_COUPON_LELANGLEDU"){%>
                                <div class="winners_clazz_zoom"></div>
                                <div>
                                    参加暑假夏令营，你也想去北京？ <a href="/student/reward/coupon.vpage?type=lelang" target="_blank" class="winners_clazz_zoom winners_clazz_zoom_submit"></a>
                                </div>
                            <%}%>
                        </div>
                    </div>
                    <p class="date-time"><%=news[i].date%></p>
                    <%if(news[i].relevantUserType == "STUDENT"){%>
                        <%var commentCount=0%>
                        <%for(var j = 0; j < news[i].comments.length; j++){%>
                            <%commentCount= commentCount + news[i].comments[j]['count']%>
                        <%}%>
                        <p class="comment_but laud-count v-studentVoxLogRecord" data-op="clazzReview" data-journal_id="<%=news[i].journalId%>">
                            <a href="javascript:void(0);">
                                <span class="space-icon space-icon-22"></span><span class="text_gray_6 canCommentText"><%if(news[i].canComment){%>已评论<%}else{%>评论<%}%></span>(<span class="text_gray_6 commentCountText"><%=commentCount%></span>)
                            </a>
                        </p>
                        <p class="laud-count" <%if(news[i].names.length > 0){%>data-title="<%for(var x = 0; x < news[i].names.length; x++){%><%=news[i].names[x]%><%if(x < (news[i].names.length - 1)){%>，<%}%><%}%> 觉得很赞"<%}%>>
                        <%if(news[i].canLike){%>
                            <a href="javascript:void(0);" class="studentSpacePraise v-studentVoxLogRecord" data-op="clazzReview" data-journalId="<%=news[i].journalId%>" data-clazzId="<%=news[i].clazzId%>" data-relevantUserId="<%=news[i].relevantUserId%>">
                                <span class="space-icon space-icon-19"></span><span class="text_f text_gray_6">赞</span>
                            </a>
                        <%}else{%>
                            <span class="space-icon space-icon-5"></span>己赞
                        <%}%>
                            (<span class="count" data-count="<%=news[i].likeCount%>"><%=news[i].likeCount%></span>)
                        </p>
                        <#--删除功能，只能删除自己的新鲜事。-->
                        <%if(news[i].relevantUserId == ${(currentUser.id)!}){%>
                            <p class="laud-count">
                                <a href="javascript:void(0);" data-journalId="<%=news[i].journalId%>" class="removeFreshButton" data-tongji-value="<%if(news[i].journalType == "STUDENT_UPLOAD_PHOTO"){%>删除照片新鲜事<%}else{%>删除新鲜事<%}%>">
                                <span class="space-icon space-icon-23"></span><span class="text_f text_gray_6">删除</span>
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
                                                <img width="26" height="26" src="<@app.link href="/public/skin/common/images/expression/e<%=j+1%>.gif?1.0.4"/>" />
                                            <%}else{%>
                                                <img width="26" height="26" src="<@app.link href="/public/skin/common/images/expression/eg<%=j+1%>.png?1.0.4"/>" />
                                            <%}%>
                                        </p>
                                        <span class="count" <%if(news[i].comments[j]['count'] < 1) {%>style="display: none"<%}%>><%=news[i].comments[j]['count']%></span>
                                    </li>
                                <%}%>
                            </ul>
                        </div>
                    <%}%>
                </dd>
            </dl>
        <%}%>
    </script>
    <script id="t:人气动态" type="text/html">
        <%for(var i = 0; i < news.length; i++){%>
        <dl>
            <dt>
                <%if(news[i].userImg){%>
                    <%if(news[i].userId == 99999){%>
                        <img src="<@app.link href='public/skin/common/images/soplugin/systemAvatar.png'/>" width="60" height="60">
                    <%}else{%>
                        <img src="<@app.avatar href='<%=news[i].userImg%>'/>" width="60" height="60">
                    <%}%>
                <%}%>
                <i class="icon_general"></i>
            </dt>
            <dd>
                <div class="ms-space-skin ms-space-skin-1">
                    <i class="ms-space-arrow"></i>
                    <div class="mes-info" style="font-size: 14px;;">
                        <div style="color: #5eac14;">
                            <%if(news[i].type == "LIKE"){%>
                                <%=news[i].userName %>&nbsp;&nbsp;赞了我的新鲜事
                                <%if(news[i].commentImg > 0){%>
                                    <img width="37" style="position: relative; top: -5px;" src="<@app.link href='/public/skin/common/images/expression/e<%=news[i].commentImg %>.gif?1.0.4'/>">
                                <%}%>
                            <%}else{%>
                                <%=news[i].userName %>&nbsp;&nbsp;评论了我的新鲜事
                                <%if(news[i].commentImg > 0){%>
                                    <img width="37" style="position: relative; top: -5px;" src="<@app.link href='/public/skin/common/images/expression/e<%=news[i].commentImg %>.gif?1.0.4'/>">
                                <%}%>
                            <%}%>
                        </div>
                        <div style="color: #333;margin-top: 5px;"><%==news[i].param.content %></div>
                    </div>
                </div>
                <p class="date-time"><%=news[i].date%></p>
            </dd>
        </dl>
        <%}%>
    </script>

    <#--照片展示-->
    <script id="t:照片展示漂浮" type="text/html">
        <div class="popupShowPhotoBox" >
            <div class="jcarousel-skin-tango" style="display:none;">
                <ul>
                    <%for(var i = 0; i < showPhotoUrl.length; i++){%>
                        <li><img src="<@app.avatar href='<%=showPhotoUrl[i]%>'/>"><span class="cMi"></span></li>
                    <%}%>
                </ul>
            </div>
        </div>
    </script>

    <script type="text/javascript">
        $(function(){
            <#--班级动态-->
            var showPhotoUrl = [];
            var clazzlatestnews = new $17.Model({
                ajaxUrl : "/student/clazz/clazzlatestnews.vpage"
            });
            clazzlatestnews.extend({
                transTips : function(){
                    $(".ms-space-skin .ms-space-leak .trans").each(function(){
                        var $this = $(this);
                        var ps = $this.attr("data-title");

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
                                my: 'bottom center',
                                viewport: $(window),
                                effect: false,
                                adjust: {
                                    y : -15
                                }
                            },
                            style : {
                                classes : 'qtip-bootstrap'
                            }
                        });
                    });
                },
                statePageList : function(type, page){
                    var $this = this;
                    var $templateName = "t:班级动态";

                    if(type == "popularity"){
                        $this.ajaxUrl = "/student/clazz/popularity.vpage";
                        $templateName = "t:人气动态";
                    }else{
                        $this.ajaxUrl = "/student/clazz/clazzlatestnews.vpage";
                        $templateName = "t:班级动态";
                    }

                    $.get($this.ajaxUrl,{type :  type.toUpperCase(), currentPage : page}, function(data){
                        if(data.success){
                            var content = data.journalPage.content;
                            $("#newsLength").html(data.journalPage.totalElements);
                            if(content.length > 0){
                                $("#" + type).html(template($templateName, {
                                    news : content
                                }));
                                $this.transTips();

                                $(".my-space-novelty .laud-count").each(function(){
                                    showQtip($(this), $(this).data("title"), 220);
                                });

                                loadStudentCard();

                                $(".message_page_list").page({
                                    total           : data.journalPage.totalPages,
                                    current         : data.journalPage.number + 1,
                                    autoBackToTop   : false,
                                    jumpCallBack    : function(index){
                                        $.get($this.ajaxUrl, {type : type.toUpperCase(),currentPage : index}, function(data){
                                            if(data.success){
                                                $("#" + type).html(template($templateName, {
                                                    news : data.journalPage.content
                                                }));

                                                $(".my-space-novelty .laud-count").each(function(){
                                                    showQtip($(this), $(this).data("title"), 220);
                                                });

                                                $("html, body").animate({ scrollTop: $("#"+type).offset().top - 47 }, 0);

                                                $this[type + "CurrentPage"] = index;
                                                $this.showLeakTranslation();
                                                $this.showStudentsName();
                                                $this.transTips();
                                                $this.showPhoto();
                                                loadStudentCard();
                                            }
                                        });
                                    }
                                });

                                $this.showLeakTranslation();
                                $this.showStudentsName();
                                $this.showPhoto();
                            }else{
                                $("#" + type).html("<div class='w-noData-box'>暂无新鲜事</div>");
                                $(".message_page_list").html("");
                            }
                        }else{
                            $("#" + type).html("<div class='no_content_box'>"+ data.info+"---"+type+"</div>");
                        }
                    });

                },
                showStudentsName : function() {
                    //表情评论人提示
                    $("#comment_img_list_box li").each(function(){
                        var $this = $(this);
                        var studentName = $this.data('students_name');
                        showQtip($this,studentName,220);
                    });
                },
                showPhoto : function(){
                    //上传照片放大预览
                    $(".myclassShowPhoto span[data-content-id]").live("click", function(){
                        var $that = $(this);
                        var parentSpan = $that.parent().find("span");
                        var thisIndex = $that.prevAll().length;

                        if($.inArray($that.attr("data-content-id"), showPhotoUrl) < 0){
                            showPhotoUrl = [];
                            for(var i=0; i<=parentSpan.prevAll().length; i++){
                                showPhotoUrl.push(parentSpan.eq(i).attr("data-content-id"));
                            }
                        }

                        $.prompt(template("t:照片展示漂浮", {
                            showPhotoUrl : showPhotoUrl
                        }), {
                            prefix: 'myPrompt',
                            title : '',
                            buttons : {},
                            position : { width : 720},
                            classes : {
                                box: '',
                                fade: 'jqifade',
                                prompt: '',
                                close: 'popupShowPhotoBoxClose',
                                title: '',
                                message: '',
                                buttons: '',
                                button: '',
                                defaultButton: ''
                            },
                            loaded : function(){
                                var jcarouselSkinTango = $('.popupShowPhotoBox .jcarousel-skin-tango');

                                jcarouselSkinTango.jcarousel({ scroll : 1, start : thisIndex+1});
                                setTimeout(function(){
                                    jcarouselSkinTango.find(".jcarousel-item").height(jcarouselSkinTango.height());

                                    jcarouselSkinTango.show();
                                }, 300);

                                $(document).on("click",function(e){
                                    if($(e.target).closest("#myPromptstate_state0").length == 0 && $("#myPromptstate_state0").is(':visible')){
                                        $.prompt.close();
                                    }
                                });
                            }
                        });
                    });

                    $(document).keydown(function(event){
                        if(event.keyCode == 27){
                            event.returnValue = null;
                            window.returnValue = null;
                            document.getElementById("popupUploadPhotoBox").innerHTML = "";
                            $("#uploadPhotoButton").removeClass("isShow");
                        }
                    });
                },
                showLeakTranslation : function(){
                    //爆料提示
                    $(".ms-space-skin .ms-space-leak .trans").each(function(){
                        var $this = $(this);
                        var translation = $this.attr("data-title");
                        $this.qtip({
                            content: {
                                text: translation
                            },
                            hide: {
                                fixed: true,
                                delay: 150,
                                leave: false
                            },
                            position: {
                                at: 'bottom center',
                                my: 'bottom center',
                                viewport: $(window),
                                effect: false,
                                adjust: {
                                    y : -15
                                }
                            },
                            style : {
                                classes : 'qtip-bootstrap'
                            }
                        });
                    });
                },
                init: function(){
                    var $this = this;
                    var $newTab = $("[data-newtype]");
                    $newTab.on("click",function(){
                        var $that = $(this);
                        var typeName = $that.data('newtype');
                        if($that.hasClass('active')){ return false}
                        $newTab.removeClass("active");
                        $that.addClass("active");

                        $this.statePageList(typeName, $this[typeName + "CurrentPage"]);

                        if( $("#alarm:visible")){
                            if(typeName !== "popularity"){
                                $("#alarm:visible").hide();
                            }
                        }

                        $("#all, #teacher, #student, #personal, #popularity").hide().filter(function(){
                            return $(this).attr("id") == $that.data("newtype");
                        }).show();

                    });

                    $newTab.hover(function(){
                        $(this).addClass("hover");
                    }, function(){
                        $(this).removeClass("hover");
                    });

                    if($17.getQuery("type") == "popularity"){
                        $("[data-newtype='popularity']").click();
                    }else{
                        $("[data-newtype='all']").click();
                    }

                    var studentSpacePraise = $(".studentSpacePraise");
                    studentSpacePraise.live("click", function(){
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

                                showQtip($this.parents(".laud-count"), $this.parents(".laud-count").attr("data-title"), 220);

                                $this.find(".space-icon").addClass("space-icon-5").removeClass("space-icon-19");
                                $this.find('.text_f').text('');
                                $this.after("己赞");
                                $this.css({ cursor: 'default'}).removeClass("studentSpacePraise");
                                count.text(parseInt(count.data("count")) + 1);
                            }
                        });
                    });

                    //班级空间表情评论
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
                                $this.find(".express img").attr("src", "<@app.link href='/public/skin/common/images/expression/e"+ imgId +".gif?1.0.2'/>");
                                //更新评论数
                                $this.find(".count").show().html(parseInt(count + 1));
                                $this.parent("ul").find(".express").addClass("default");

                                canCommentText.text("已评论");
                                commentCountText.text(parseInt(commentCountText.text()*1 + 1));
                                //更新评论人列表
                                $this.data('students_name',studentsName + comma +'${(currentUser.profile.realname)!}' );
                                clazzlatestnews.showStudentsName();
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
                            $this.find("img").attr("src", "<@app.link href='/public/skin/common/images/expression/e"+ imgId +".gif?1.0.4'/>");
                        }
                    }).live("mouseleave", function(){
                        var $this = $(this);
                        var imgId = $this.parent().data('c_img_id');
                        if($this.siblings(".count").text() < 1){
                            $this.find("img").attr("src", "<@app.link href='/public/skin/common/images/expression/eg"+ imgId +".png?1.0.4'/>");
                        }
                    });

                    //评论
                    $(".comment_but").live('click', function(){
                        $(this).siblings(".expressionBox").toggle();
                    });

                    //remove Fresh
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

                                            $17.tongji($this.attr("data-tongji-value"));
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
                            $.prompt("<iframe class='vox17zuoyeIframe' width='700' height='470' frameborder='0' scrolling='no' frameborder='0' src='"+ dataUrl +"'></iframe>", {
                                title : "看看",
                                position : {width: 760},
                                buttons : {}

                            });
                        }
                    });

                    <#if popularity!false>
                        $("#alarm").show();
                    </#if>
                }
            }).init();
        });
    </script>
</#macro>