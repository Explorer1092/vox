<div id="tinyGroupsDetailBox" style="float: left; margin-left: 5px; position: relative;"></div>
<script type="text/javascript">
    $(function(){
        var _tempData = null;
        var currentIndex = 0;
        var currentSubject = "ENGLISH";

        function apiShareInvite(){
            //分享
            var p = {
                url : location.href,
                desc : '嗨小伙伴，老师布置的作业你还没有做哦，老师马上就检查作业了！快点到“一起作业网”做作业吧',
                title:'老师布置的作业你还没有做哦',
                summary:'嗨小伙伴，老师布置的作业你还没有做哦，老师马上就检查作业了！快点到“一起作业网”做作业吧',
                pics: '<@app.link href="public/skin/studentv3/images/tinygroup/spare-public-info.png"/>',
                site:'一起作业网'
            };

            var s = [];
            for(var i in p){
                s.push(i + '=' + encodeURIComponent(p[i] || ''));
            }
            $(".v-remindQQStudent").html( ['<a class="qcShareQQDiv share-qq" href="//connect.qq.com/widget/shareqq/index.html?',s.join('&'),'" target="_blank">提醒</a>'].join('') );
        }

        $.post("/student/tinygroup/tgd.vpage", {}, function(data){
            if(data.success){
                _tempData = data;
                $("#tinyGroupsDetailBox").html( template("T:tinyGroupsDetailBtn", {data : _tempData, currentIndex : currentIndex, currentSubject : currentSubject, showFlag : "none"}) );
                setTimeout(function(){
                    apiShareInvite();
                }, 200);
            }else{
                $("#tinyGroupsDetailBox").html( template("T:tinyGroupsDetailNull", {}) );
                setTimeout(function(){
                    $(".js-ownFlag").text("快去加入小组吧！");
                }, 200);
            }
        });

        $(document).on("mouseenter", "#tinyGroupsDetailBtn", function(){
            $(this).parent().siblings(".t-spare-group-box").show();
        }).on("mouseleave", "#tinyGroupsDetailBox", function(){
            $(this).find(".t-spare-group-box").hide();
        });

        $(document).on("click", ".v-selectSubjectGroup li", function(){
            var $this = $(this);
            if($this.hasClass("active")){
                currentIndex = $this.attr("data-index");
                currentSubject = $this.attr("data-type");
                $("#tinyGroupsDetailBox").html( template("T:tinyGroupsDetailBtn", {data : _tempData, currentIndex : currentIndex, currentSubject : currentSubject, showFlag : "block"}) );

                setTimeout(function(){
                    apiShareInvite();
                }, 200);
                return false;
            }
        });
    });
</script>
<script type="text/html" id="T:tinyGroupsDetailNull">
    <span class="t-spareGroup-ref">
        还未加入小组 <a class="t-spareGroup-detail-icon v-joinTeacherGroupBtn" href="javascript:void (0);">点击加入 </a>
    </span>
</script>
<#--<#include "../taskcard/tinygroup.ftl" >-->
<@addTgTinyGroupJs/>
<script type="text/html" id="T:tinyGroupsDetailBtn">
    <%var moduleData = data.list[currentIndex]%>
    <span class="t-spareGroup-ref">
        <%=(data.message)%><a class="t-spareGroup-detail-icon" id="tinyGroupsDetailBtn" href="javascript:void (0);">小组详情 </a>
    </span>
    <div class="t-spare-group-box" style="display: <%=showFlag%>">
        <div class="sp-title">
            <ul class="v-selectSubjectGroup">
                <%for(var i = 0, list = data.list; i < list.length; i++){%>
                    <%if(list[i].subject == "ENGLISH"){%><li data-type="ENGLISH" data-index="<%=i%>" class="<%=(i == currentIndex ? '' : 'active')%>">英语小组</li><%}%>
                    <%if(list[i].subject == "MATH"){%><li data-type="MATH" data-index="<%=i%>" class="<%=(i == currentIndex ? '' : 'active')%>">数学小组</li><%}%>
                    <%if(list[i].subject == "CHINESE"){%><li data-type="CHINESE" data-index="<%=i%>" class="<%=(i == currentIndex ? '' : 'active')%>">语文小组</li><%}%>
                <%}%>
            </ul>
            <p class="info">
                <span>组员加错组了？找老师帮忙修改哦！</span>
                <%if(moduleData.isLeader){%>
                <a href="/student/tinygroup/index.vpage?subject=<%=currentSubject%>" target="_blank">我的任命书</a>
                <%}%>
            </p>
            <div class="w-clear"></div>
        </div>
        <div class="sp-content">
            <!--我的小组-->
            <div class="sp-list">
                <div class="sl">
                    <div class="sl-title"><%=moduleData.myTinyGroupName%></div>
                    <div class="su">
                        <p>未做作业</p>
                        <ul>
                            <%for(var n in moduleData.no){%>
                                <li style="text-align: left;"><span class="remind v-remindQQStudent"><%if(moduleData.isLeader){%><a href="javascript:void(0);">提醒</a><%}%></span><%=moduleData.no[n]%></li>
                            <%}%>
                        </ul>
                    </div>
                    <div class="su">
                        <p>已做作业</p>
                        <%if(moduleData.yes.length > 0){%>
                            <ul>
                                <%for(var y in moduleData.yes){%>
                                <li><%=moduleData.yes[y]%></li>
                                <%}%>
                            </ul>
                        <%}else{%>
                            <div class="t-spare-public-info-icon"></div>
                        <%}%>
                    </div>
                    <%if((moduleData.yes.length + moduleData.no.length) <= 3){%>
                    <div style="border-top: 1px solid #d1e7c3; background-color: #e6f5dd; line-height: 22px; width: 90%; position: relative; top: -44px; _top: -56px; clear: both; font-size: 12px; color: #ebb46e; padding: 0 5%; border-radius: 0 0 8px 8px;">
                        小组成员数量过少，快去教你班里的组员注册<br/>
                        登录，选择你成为他们的组长吧！
                    </div>
                    <%}%>
                </div>
                <!---->
                <div class="sc">
                    小组成员数量过少，快去教你班里的组员注册
                    登录，选择你成为他们的组长吧！
                </div>
            </div>
            <!--排名-->
            <div class="sp-table">
                <table>
                    <thead>
                    <tr>
                        <td>本周排名</td>
                        <td>组名</td>
                        <td>小组长</td>
                        <td>作业人数</td>
                        <td>作业次数</td>
                    </tr>
                    </thead>
                    <tbody>
                        <%for(var r = 0, rank = moduleData.rank; r < rank.length; r++){%>
                            <tr class="<%=(r%2 != 0 ? 'odd': '')%>">
                                <td><%=rank[r].rank%></td>
                                <td><%=(rank[r].tinyGroupName == '' ? '未命名组' : rank[r].tinyGroupName)%></td>
                                <td><%=rank[r].leaderName%></td>
                                <td><%=rank[r].weekCount%></td>
                                <td><%=rank[r].hwCount%></td>
                            </tr>
                        <%}%>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</script>