<@sugar.capsule js=["flexslider"] css=["plugin.flexslider"] />
<div id="starModel"></div>
<script type="text/html" id="t:starHeader">
    <%var stars = data.stars, students = stars[currentMenu].students;
        var subjectTextMap = {
            "ENGLISH" : "英语",
            "MATH" : "数学",
            "CHINESE" : "语文"
        }
    %>
    <div class="my-starHead">
        <div class="groupStar groupStar-1">
            <div class="medal"></div>
        </div>
        <%if(stars[currentMenu].contain){%>
            <div class="notice4 notice">
                <p class="honorEdition">荣誉版</p>
                <p class="text" style="color: #fff;">恭喜 ${(currentUser.profile.realname)!'---'} 同学</br>获得上周<%=subjectTextMap[stars[currentMenu].subject]%>之星！</p>
            </div>
        <%}else{%>
            <div class="notice notice5">
                <p class="notObtained">未获得小组之星</p>
                <p class="text js-ownFlag" style="color: #fff;padding: 0 15px;">督促组员按时完成作业并邀请同学加入小组吧！</p>
            </div>
        <%}%>
        <div class="rule rule-1">评选规则</div>
        <div class="notice3 notice notice6" style="display: none;">
            <div class="info"></div>
            <p class="sub">1.小组之星为上周做作业次数最多，且绑定手机号成员最多，最活跃的小组。</p>
            <p class="con">2.显示的头像为：上周该组长+组员，显示不分先后。</p>
        </div>
        <div class="starsShowArea">
            <div class="tabs tabs-1">
            <%for(var i = 0; i < stars.length; i++){%>
                <%if(stars[i].students.length > 0){%>
                    <div class="<%=(currentMenu == i ? 'active' : '')%> js-clickStarTab" data-index="<%=i%>"><%=subjectTextMap[stars[i].subject]%>之星</div>
                <%}%>
            <%}%>
            </div>
            <div class="flexslider js-starStudentList" datatype="<%=stars[currentMenu].subject %>">
                <ul class="slides">
                    <% for(var d = 0; d < students.length ; d++){%>
                        <li class="<%=students[d].isLeader ? 'pr-big' : ''%>" style="margin-top: <%=d%2==1 ? '52px' : 0 %>">
                            <%if(students[d].isLeader){%><span class="starer imperialCrown"></span><%}%>
                            <div class="grouper grouper-green">
                                <span class="avatar circle">
                                    <%if(students[d].img){%>
                                        <img src="<@app.avatar href='<%=students[d].img %>'/>" width="92" height="92"/>
                                    <%}else{%>
                                        <img src="<@app.avatar href=''/>" width="92" height="92"/>
                                    <%}%>
                                </span>
                            </div>
                            <span class="name"><%= students[d].name%></span>
                        </li>
                    <%}%>
                </ul>
            </div>
        </div>
    </div>
</script>
<script type="text/javascript">
    $(function(){
        var tempDataJson = {};
        var currentMenu = -1;
        function showTemplate(){
            $("#starModel").html( template("t:starHeader", { data: tempDataJson, currentMenu : currentMenu}) );
            setTimeout(function(){
                //star list
                $(".js-starStudentList").flexslider({
                    animation : "slide",
                    animationLoop : true,
                    slideshow : false,
                    slideshowSpeed: 4000, //展示时间间隔ms
                    animationSpeed: 400, //滚动时间ms
                    itemWidth : 80,
                    direction : "horizontal",//水平方向
                    minItems : 0,
                    maxItems : 5,
                    move : 5,
                    directionNav: tempDataJson.stars[currentMenu].students.length > 5 ? true : false
                });
            }, 200);
        }

        $.post('/student/tinygroup/tgs.vpage', {}, function(data){
            tempDataJson = data;
            if(data.stars.length > 0){
                for(var i = 0; data.stars.length > 0; i++){
                    if(data.stars[i].students.length > 0){
                        currentMenu = i;
                        break;
                    }
                }

                //students size gt -1
                if(currentMenu > -1){
                    $(".my-class-container").addClass("my-class-starBack");
                    showTemplate();
                }else{
                    $(".my-class-container").removeClass("my-class-starBack");
                }
            }else{
                $(".my-class-container").removeClass("my-class-starBack");
            }
        });

        $(document).on({
            mouseover: function () {
                $(".notice3").show();
            },
            mouseout: function(){
                $(".notice3").hide();
            }
        }, ".rule");

        //tab切换
        $(document).on("click", ".js-clickStarTab", function() {
            currentMenu = $(this).data("index");
            showTemplate();
        });
    });
</script>