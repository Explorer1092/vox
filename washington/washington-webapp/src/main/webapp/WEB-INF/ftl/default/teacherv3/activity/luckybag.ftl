<#import "../../layout/project.module.student.ftl" as temp />
<@temp.page title="一起作业-传福袋，拿学豆" header="hide">
<@app.css href="public/skin/project/luckybag/teacher/skin.css" />
<div class="cfd-header">
    <div class="bgs"><div style="width: 950px; margin: 0 auto;"><a href="/" style="width: 180px; height: 100px; display: block;"></a></div></div>
    <div class="inner-wrap">
        <div class="tips">学生打开福袋后完成一次作业即可领取奖励，快来布置作业帮他们领奖吧！</div>
        <p>
            <a href="javascript:void(0);" class="get-btn v-rewardClazzBtn" style="display: none;">领取班级奖励</a>
            <a href="/teacher/homework/batchassignhomework.vpage?ref=luckbag" target="_blank" class="get-btn"  style="display: none;">帮助学生领取奖励</a>
        </p>
    </div>
</div>
<div class="cdf-main">
    <div class="inner-wrap inner-main">
        <div class="selectBox selectBox-active" id="v-selectClazzItems">
            <div class="select">请选择</div>
            <div class="arrow"></div>
            <ul class="selectInfo" style="display: none;">
                <#list clazzs as item>
                    <li data-id="${(item.groupId)!0}">${(item.clazzName)!'${(item.groupId)!0}'}</li>
                </#list>
            </ul>
        </div>
        <div class="main-list">
            <div class="cm-hd">
                <div class="label bgGreen">如果班级全部学生都获得了奖励，该班将获得<span class="txt-yellow">100班级学豆</span>，你可以用来奖励学生哟~</div>
            </div>
            <div class="cm-mn">
                <div class="proBox">
                    <div class="proTips"><p>班级学豆<br>×<span class="count">100</span></p></div>
                    <div class="curBox" id="totalCount" style="width: 0;"><span class="num">0%</span></div>
                </div>
            </div>
        </div>
        <div id="loadStudentList"></div>
    </div>
</div>

<script type="text/html" id="T:allStudentList">
    <div class="main-list">
        <div class="cm-hd">
            <div class="label bgYellow">已经打开福袋的学生</div>
            <div class="txt">他们完成你布置的作业后即可领取奖励</div>
        </div>
        <div class="cm-mn">
            <ul>
                <%if(items.successList.length > 0){%>
                    <%for(var i = 0, item = items.successList; i < item.length; i++){%>
                        <li >
                            <%if(item[i].img){%>
                            <img src="<@app.avatar href='<%=(item[i].img)%>'/>" class="pic"/>
                            <%}else{%>
                            <img src="<@app.avatar href=''/>" class="pic"/>
                            <%}%>
                            <span class="name">
                                <%=(item[i].name ? item[i].name : '---')%>
                            </span>
                        </li>
                    <%}%>
                <%}else{%>
                    <li style="float: none; width: 100%; margin: 0; padding: 50px 0 80px; text-align: center; font-size: 16px;">
                        没有打开福袋的学生！！
                    </li>
                <%}%>
            </ul>
        </div>
    </div>
    <div class="main-list">
        <div class="cm-hd">
            <div class="label bgRed">还未分享福袋的学生</div>
            <div class="txt">将福袋分享给两位同学才有机会打开自己的福袋</div>
        </div>
        <div class="cm-mn">
            <ul>
                <%if(items.holdList.length > 0){%>
                    <%for(var i = 0, item = items.holdList; i < item.length; i++){%>
                        <li >
                            <%if(item[i].img){%>
                            <img src="<@app.avatar href='<%=(item[i].img)%>'/>" class="pic"/>
                            <%}else{%>
                            <img src="<@app.avatar href=''/>" class="pic"/>
                            <%}%>
                            <span class="name">
                                <%=(item[i].name ? item[i].name : '---')%>
                            </span>
                        </li>
                    <%}%>
                <%}else{%>
                    <li style="float: none; width: 100%; margin: 0; padding: 50px 0 80px; text-align: center; font-size: 16px;">
                        没有未分享福袋的学生！
                    </li>
                <%}%>
            </ul>
        </div>
    </div>
    <div class="main-list">
        <div class="cm-hd">
            <div class="label bgBlue">等待其他人传递福袋的学生</div>
        </div>
        <div class="cm-mn">
            <ul>
                <%if(items.waitList.length > 0){%>
                <%for(var i = 0, item = items.waitList; i < item.length; i++){%>
                    <li >
                        <%if(item[i].img){%>
                        <img src="<@app.avatar href='<%=(item[i].img)%>'/>" class="pic"/>
                        <%}else{%>
                        <img src="<@app.avatar href=''/>" class="pic"/>
                        <%}%>
                        <span class="name">
                            <%=(item[i].name ? item[i].name : '---')%>
                        </span>
                    </li>
                <%}%>
                <%}else{%>
                    <li style="float: none; width: 100%; margin: 0; padding: 50px 0 80px; text-align: center; font-size: 16px;">
                        没有等待其他人传递福袋的学生！
                    </li>
                <%}%>
            </ul>
        </div>
    </div>
</script>

<script type="text/javascript">
    (function () {
        var selectClazzItems = $("#v-selectClazzItems");
        var currentClazzids;

        selectClazzItems.on({
            mouseenter : function(){
                $(this).find(".selectInfo").show();
            },
            mouseleave : function(){
                $(this).find(".selectInfo").hide();
            }
        });

        selectClazzItems.find("li").on("click", function(){
            var $this = $(this);
            var $parent = $this.parent();
            currentClazzids = $this.data("id");

            if( $17.isBlank(currentClazzids) ){
                return false;
            }

            $parent.hide();

            $parent.siblings(".select").text($this.text());

            $("#loadStudentList").html('<div class="w-ag-center" style="padding: 100px 0;"><img src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 加载中…</div>');

            $.post("/teacher/activity/loadgroupluckybaginfo.vpage", {groupId : currentClazzids }, function(data){
                if(data.success){
                    //加载成功

                    var items = {};

                    items.successList = data.data.successList;
                    items.holdList = data.data.holdList;
                    items.waitList = data.data.waitList;
                    items.totalCount = data.data.totalCount;
                    items.clazzFlag = data.data.clazzFlag || false;

                    var totalCount = parseInt(items.totalCount);

                    var $rewardClazzBtn = $(".v-rewardClazzBtn");
                    if(items.clazzFlag){
                        $rewardClazzBtn.show();
                        $rewardClazzBtn.siblings().hide();
                    }else{
                        $rewardClazzBtn.hide();
                        $rewardClazzBtn.siblings().show();
                    }

                    $("#totalCount").css({ width : totalCount + "%"}).find(".num").text(totalCount + "%");
                    $("#loadStudentList").html( template("T:allStudentList", { items : items}) );
                }else{
                    $("#loadStudentList").html('<div class="w-ag-center" style="padding: 100px 0;">' + (data.info ? data.info : "请求失败！请重新选择班级查看！") +'</div>');
                }
            });
        });

        var clazzsCount = ${(clazzs?size)!0};
        if(clazzsCount > 0){
            selectClazzItems.find("li:first").click();
        }

        //领取班级奖励
        $(".v-rewardClazzBtn").on("click", function(){
            if( $17.isBlank(currentClazzids) ){
                return false;
            }

            $.post("receiveclazzreward.vpage", {groupId : currentClazzids}, function(data){
                if(data.success){
                    $17.alert("领取成功！");
                }else{
                    $17.alert(data.info ? data.info : "领取失败！");
                }
            });
        });
    })();
</script>
</@temp.page>