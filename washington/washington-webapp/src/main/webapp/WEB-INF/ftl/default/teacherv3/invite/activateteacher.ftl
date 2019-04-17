<#-- @ftlvariable name="t" type="com.voxlearning.utopia.mapper.ActivateInfoMapper" -->
<#import "../../nuwa/teachershellv3.ftl" as temp />
<@temp.page showNav="hide">
<@app.css href="public/skin/project/schoolambassador/activateteacher.css" />
<#--是否校园大使-->
<#assign whetherShow = (currentTeacherDetail.schoolAmbassador)?? && currentTeacherDetail.schoolAmbassador>
<style type="text/css">
    .page{ }
    .page a{ display:inline-block; border:1px solid #ccc; padding:8px 12px; font:12px/1.125 arial; margin:0 2px; border-radius:3px; cursor:pointer; background-color:#f5f5f5;}
    .page a.active{ background-color:#ddd;}
    .page a.disable{ background-color:#f5f5f5; color:#999;}
    .page a.enable{ background-color:#ddd;}
</style>
<script type="text/javascript">
    window.location.replace('/');
</script>
<div class="w-base" style="margin-top: 15px;">
    <div class="t-awake-box">
        <div class="ab-up">
            <div class="info-detail">
                <div class="picture">
                    <i><img src="<@app.avatar href='${currentUser.fetchImageUrl()}'/>"></i>
                </div>
                <div class="tip">
                    <#--<#if whetherShow>
                        <a style="margin: 0 10px 0 0;" class="w-btn w-fl-right" href="javascript:void (0)" id="tjaut">推荐认证</a>
                    </#if>-->
                    <p>
                        <#if (currentTeacherDetail.subject)?? && (currentTeacherDetail.subject) =='ENGLISH'>
                            英语老师
                        <#elseif (currentTeacherDetail.subject)?? && (currentTeacherDetail.subject) =='MATH'>
                            数学老师
                        <#else>
                            语文老师
                        </#if>
                        ${(currentUser.profile.realname)!}
                    </p>
                    <#if whetherShow>
                        <p style="float: right; font-size: 14px; color: #667284; padding-right: 30px;">帮助本校老师达成认证，快速提高校园活跃度！还送100园丁豆！<a href="javascript:void(0);" class="w-blue click-see-detail">查看详情</a></p>
                        <p style="padding-bottom: 0;">
                            <span class="t-card-${(ambassadorLevel.level)!'SHI_XI'}"></span><span class="w-icon-md">${(ambassadorLevel.level.description)!'实习大使'}</span>
                            <a href="http://help.17zuoye.com/?p=766" target="_blank" class="w-blue" style="font-size: 14px; display: inline-block; margin-left: 20px;"><span class="w-icon-public w-icon-faq"></span><span class="w-icon-md">规则</span></a>
                        </p>
                    <#else>
                        ${(currentTeacherDetail.teacherSchoolName)!}
                    </#if>
                </div>
            </div>
        </div>
        ${pageBlockContentGenerator.getPageBlockContentHtml('TeacherIndex', 'InviteActivateteacher')}
    </div>
</div>

<#--//同校mentor - start-->
<#if (currentUser.fetchCertificationState() == "SUCCESS")!false>
    <#import "../block/mentor.ftl" as mentorTemplate />
    <@mentorTemplate.yesAuto/>

    <div id="findnoauthenticationteacher"></div>
    <div class="w-base">
        <div class="w-base-title">
            <h3>可帮助TA完成认证</h3>
            <div class="w-base-ext">
                <span class="w-bast-ctn" style="color: #667284;">每帮助 <strong class="w-orange">1</strong> 名未认证老师达成认证，可获得 <strong class="w-orange">100</strong> 园丁豆奖励</span>
            </div>
        </div>
        <div class="w-base-container">
            <div class="t-show-box">
                <div class="authTeacherItem-container"><#--老师列表--></div>
                <div class="authTeacherItemPageList message_page_list" data-title="分页" style="text-align: center; border-top: 1px solid #ddd; display: none;"></div>
            </div>
        </div>
    </div>
    <#--//T:期末回馈计划-->
    <div class="tableListBox_container_1" data-title="期末回馈计划"></div>
    <#--//T:可指导TA邀请新生-->
    <div class="tableListBox_container_2" data-title="可指导TA邀请新生"></div>
</#if>
<#--同校mentor - end//-->

<div class="w-base" id="HXUSER">
    <div class="w-base-switch w-base-two-switch">
        <ul class="Teachertitle" style="height: 40px;">
            <li class="active" data-type="theSchoolTeacherContainer">
                <a href="javascript:void(0);">
                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                    本校待唤醒认证老师（共${(teacherList?size)!'0'}位）
                </a>
            </li>
            <li data-type="findactivatingteacher">
                <a href="javascript:void(0);">
                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                    唤醒中的老师
                </a>
            </li>
           <#-- <li data-type="findactivatedteacher">
                <a href="javascript:void(0);">
                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                    查看成功记录
                </a>
            </li>-->
        </ul>
    </div>
   <#-- <div class="w-base-title" style="line-height: 45px; padding: 0 0 0 10px;">
        <span class="w-gray w-magR-10">唤醒人数：${(pcount)!'--'}人</span>
        <span class="w-gray w-magR-10">累计奖励：${(icount)!'--'}园丁豆</span>
    </div>-->
    <div class="w-base-container">
        <div class="t-show-box">
            <div class="teachersList_box w-table" >
                <#--数据加载-->
            </div>
            <div class="teachersList_theSchoolTeacherContainer w-table">
                <#--数据加载-->
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    //todo
    function loadPage(currentPage, groud, cP, trLen, $tr, obj, $this){
        var currentList = currentPage * groud;

        $this.addClass("active").siblings().removeClass("active");

        if( currentPage > 1){
            $("."+obj+"_page a:first").addClass("enable").removeClass("disable");
        }else{
            $("."+obj+"_page a:first").addClass("disable").removeClass("enable");
        }
        if( currentPage >= (trLen/groud)){
            $("."+obj+"_page a:last").addClass("disable").removeClass("enable");
        }else{
            $("."+obj+"_page a:last").addClass("enable").removeClass("disable");
        }

        for(var i= 0; i < trLen; i++){
            $tr.eq(i).show();
            if( i >= currentList || i < (currentList - groud)){
                $tr.eq(i).hide();
            }
        }
    }

    function jPage(obj){
        var $this	= $("#"+obj);
        var $tr		= $this.find("tbody tr");
        var groud	= 5;
        var trLen	= $tr.length;
        var cP;
        $("."+obj+"_page").append("<a href='javascript:void(0);' class='disable cPage back' data-c_page='back'><span>上一页</span></a>");
        $tr.each(function(index) {
            var x = index + 1;

//            $(this).find("td:first").append(x);

            if( index >= groud){
                $tr.eq(index).hide();
            }

            if( index < (trLen/groud) ){
                $("."+obj+"_page").append("<a pv=" + x + ">" + x + "</a>");
            }
        });

        $("."+obj+"_page").append("<a href='javascript:void(0);' class='enable cPage next' data-c_page='next'><span>下一页</span></a>");

        //初始化
        $("."+obj+"_page a[pv]:first").addClass("active").trigger('click');
        cP = 1;
        loadPage(cP, groud, cP, trLen, $tr, obj, $("."+obj+"_page a[pv]:first"));


        $("."+obj+"_page a[pv]").on('click', function(){
            var currentPage = $(this).attr("pv");
            cP = currentPage;
            loadPage(currentPage, groud, cP, trLen, $tr, obj, $(this));
        });

        //上下页
        $("."+obj+"_page").on('click','.enable.cPage', function(){
            var currentPage = cP * 1;
            var pType = $(this).data('c_page');
            if(pType == 'back'){
                currentPage = currentPage - 1;
            }else{
                currentPage = currentPage + 1;
            }
            cP = currentPage;
            loadPage(currentPage, groud, cP, trLen, $tr, obj, $(this).siblings("a[pv="+currentPage+"]"));
        });
    }

    $(function(){
        var activatingCount = ${activatingCount!'false'};
        var whetherShow = ${whetherShow?string};
        /*唤醒单个老师*/
        $(document).on('click', ".awaken_but", function(){
            var $this = $(this);
            var userId = $this.data('teacher-id');
            var homeworkType = $this.data('teacher-homowork-type');

            if(activatingCount.constructor != Boolean && !whetherShow){
                if(activatingCount >= 3){
                    $17.alert("亲爱的老师：您最多可以同时唤醒三个老师，成功唤醒一个老师后才可以继续唤醒更多老师~！");
                    return false;
                }
            }

            if($this.hasClass('loading')) return false;
            $this.addClass('loading').removeClass('btn_mark_primary');
            App.postJSON("/teacher/invite/teacheractivateteacher.vpage",{userList : [{userId : userId, type : homeworkType}]},function(data){
                if(data.success){
                    activatingCount++;
                    $(".awakenInfo").hide();
                    $this.hide().addClass("has").after('<span class="w-df-share" style="width: 90%;"><a href="javascript:void(0);" class="w-blue" style="cursor: default;">唤醒中</a><br/>记得当面告知老师布置作业哦</span>');
                }else{
                    $17.alert(data.info);
                }
                $this.removeClass('loading')
            })
        });

        $(document).on("click", ".data-teacherCertification", function(){
            var $this = $(this);
            $this.hide();
            $this.siblings("table").find("tr").show();
        });

        /*唤醒全部老师*/
        $("#save_active_but").on("click", function(){
            var $this = $(this);
            var that = $(".awaken .awaken_but");
            var teachers = [];
            that.each(function(){
                var _t = $(this);
                teachers.push({
                    userId: _t.data('teacher-id'),
                    type:_t.data('teacher-homowork-type')
                });
            });
            if($this.hasClass('loading')) return false;
            $this.addClass('loading');

            App.postJSON("/teacher/invite/teacheractivateteacher.vpage",{userList : teachers},function(data){
                if(data.success){
                    $(".awakenInfo").hide();
                    $(".awaken_but:not('.has')").hide().after('<a href="javascript:void(0);" class="btn_mark btn_mark_small" style="cursor: default;">唤醒中</a>');
                    $17.alert("邀请已发送，请记得提醒您邀请的老师到一起作业网布置作业哦！");
                    $this.hide();
                }else{
                    $17.alert(data.info);
                }
                $this.removeClass('loading')
            })
        });

        //唤醒中的老师&&查看成功记录
        var tempTeacherList = {
            wakein : {},
            record : {}
        };
        var authHasCountObj = {
            no : 0,
            has : 0,
            HXhas : 0
        };

        $(".Teachertitle li").click(function(){
            var $this = $(this);
            var box = $(".teachersList_box");
            var theBox = $(".teachersList_theSchoolTeacherContainer");
            var t = $this.data('type');

            $this.addClass('active').siblings().removeClass('active');
            theBox.hide();
            if(tempTeacherList.wakein.length > 0 && t == "findactivatingteacher"){
                box.html(template("t:"+t, {
                    content: tempTeacherList.wakein
                }));
                jPage(t);
                return false;
            }

            //暂无用
            if(tempTeacherList.record.length > 0  && t == "findactivatedteacher" ){
                box.html(template("t:"+t, {
                    content: tempTeacherList.record
                }));
                jPage(t);
                return false;
            }

            if(t == "theSchoolTeacherContainer" ){
                box.html("");
                if(theBox.hasClass("visible")){
                    theBox.show();
                }else{
                    theBox.addClass("visible").show().html( template("t:"+t, {}) );
                }
                return false;
            }

            box.html('<div class="loadingImg text_center"><span class="loading_big"></span><span class="w-df-share text_big">数据加载中…</span><span class="w-em-share"></span></div>');
            $.get('/teacher/invite/'+ t +'.vpage',function(data){
                box.parent().removeClass('loading');
                if(data.success){
//                    authHasCountObj.HXhas = data.teacherList.length;
//                    $(".authHasCount[data-type='HXhas']").text(authHasCountObj.HXhas);
//                    $(".authHasCount[data-type='HXgold']").text(authHasCountObj.HXhas * 100);

                    if(t == "findactivatingteacher"){
                        tempTeacherList.wakein = data.teacherList;
                    }else{
                        tempTeacherList.record = data.teacherList;
                    }

                    box.html(template("t:"+t, {
                        content: data.teacherList
                    }));

                    jPage(t);
                }
            });
        });

        $(document).on("click", ".removeInviteOne", function(){
            var $this = $(this);

            $.prompt("<div class='w-ag-center'>您确定取消唤醒？</div>", {
                title: "系统提示",
                focus : 1,
                buttons: {"取消" : false, "确定": true },
                submit: function(e, v){
                    if(v){
                        $.post("delete.vpage", {historyId : $this.data("history-id")}, function(data){
                            if(data.success){
                                $this.closest("tr").remove();
                                activatingCount--;
                            }else{
                                $17.alert(data.info);
                            }
                        });
                    }
                }
            });
        });

        //初始化
        $(".Teachertitle li[data-type='theSchoolTeacherContainer']").trigger('click');
        jPage("data-schoolTeacherList");

        function authTeacherItemLoading(pageIndex) {
            var $authTeacherItem = $('.authTeacherItem-container');

            $authTeacherItem.html('<div class="w-ag-center" style="line-height: 404px;">数据加载中…</div>');

            $.get("/teacher/mentor/unauthteacherlist.vpage?", {pageNum : pageIndex}, function (data) {
                if (data.success && data.dataPage) {
                    $authTeacherItem.html( template("T:帮助老师认证", { content : data.dataPage.content, dataType : "findnoauthenticationteacher_table" }) );

                    if(data.dataPage.totalPages > 1){
                        $(".authTeacherItemPageList").page({
                            total: data.dataPage.totalPages,
                            current: pageIndex,
                            autoBackToTop: false,
                            jumpCallBack: authTeacherItemLoading
                        });
                    }
                }else{
                    $authTeacherItem.html('<div class="w-ag-center" style="line-height: 150px;">暂无数据…</div>');
                }
            });
        }

        //可帮助TA完成认证loading
        authTeacherItemLoading(1);

        //帮TA认证
        $(document).on("click", ".data-forTaCertification", function(){
            var $this = $(this);

            if( $17.isBlank($this.attr("data-category")) || $this.hasClass("w-btn-disabled")){
                return false;
            }


            $.prompt("<div style='line-height: 26px; margin-bottom: 20px; font-size: 14px;'>确定要帮助"+$this.attr("data-name")+"老师？<br/>·我们会将您的联系方式告诉"+$this.attr("data-name")+"老师<br/>·帮助关系确立后将维持10天，10内达成将获得奖励<br/>·每人最多同时可帮助4名老师</div>", {
                title: "帮助TA",
                focus : 1,
                buttons: {"取消": false, "确定": true },
                position: {width: 500},
                submit: function(e, v){
                    if(v){
                        $.post('/teacher/mentor/choosementee.vpage', {
                            mentorCategory : $this.attr("data-category"),
                            menteeId : $this.attr("data-id")
                        }, function(data){
                            if(data.success){
                                $this.text("已发送");
                            }else{
                                $17.alert(data.info);
                            }
                        });
                    }
                }
            });
        });

        if($17.getQuery("type") == "findnoauthenticationteacher" || $17.getQuery("type") == "HBYO"){
            setTimeout(function(){
                $("html, body").animate({ scrollTop: $("#findnoauthenticationteacher").offset().top }, 200);
            }, 300);
        }

        if($17.getQuery("type") == "HBYM"){
            setTimeout(function(){
                $("html, body").animate({ scrollTop: $("#teacherItemBox").offset().top - 60 }, 200);
            }, 300);
        }

        if($17.getQuery("type") == "HXUSER"){
            setTimeout(function(){
                $("html, body").animate({ scrollTop: $("#HXUSER").offset().top - 60 }, 200);
            }, 300);
        }

        $(document).on("click", ".click-see-detail", function(){
            $("html, body").animate({ scrollTop: $("#findnoauthenticationteacher").offset().top }, 200);
        });

        //本校认证老师 tab 切换
        $("#wakeTeacher").find(".container_tab li").on('click', function(){
            var $this   = $(this);
            var $len    = $this.prevAll().length;

            $this.addClass("active").siblings().removeClass("active");
            $("#wakeTeacher").find(".wakeTeacherTwo").eq($len).show().siblings().hide();
        });

        //申请取消该老师认证
        $(document).on("click", ".data-cancelAuth", function(){
            var selectContent = "非本校老师";
            var $this = $(this);
            var $dataType = $this.attr("data-type");

            if($17.isBlank($dataType)){
                return false;
            }

            $.prompt(template("T:选择原因", { dataType : $dataType}), {
                title: "系统提示",
                focus : 1,
                buttons: { "取消": false, "提交" : true},
                position:{width : 500},
                loaded : function(){
                    $(".data-selectContentList li").on("click", function(){
                        var $that = $(this);
                        $that.addClass("active").siblings().removeClass("active");
                        $that.find(".w-radio").addClass("w-radio-current");
                        $that.siblings().find(".w-radio").removeClass("w-radio-current");

                        selectContent = $that.attr("data-val");
                    });
                },
                submit : function(e, v){
                    if(v){
                        $.post("/teacher/invite/reportTeacher.vpage", {
                            type : $dataType,
                            teacherId : $this.attr("data-userid"),
                            teacherName : $this.attr("data-username"),
                            reason : selectContent
                        }, function(data){
                            $17.alert(data.info);
                        });
                    }
                }
            });
        });

        /*//T:期末回馈计划
        $.get('/teacher/mentor/termendteacherlist.vpage', {},function(data){
            if(data.success){
                $(".tableListBox_container_1").html( template("T:期末回馈计划", { content : data.teacherList, dataType : "tableListBox_1" }) );
                jPage(("tableListBox_1"));
            }
        });*/

        //T:可指导TA邀请新生    \
        $.get('/teacher/mentor/incrscountlist.vpage', {},function(data){
            if(data.success){
                $(".tableListBox_container_2").html( template("T:可指导TA邀请新生", { content : data.teacherList, dataType : "tableListBox_2" }) );
                jPage(("tableListBox_2"));
            }
        });
    });
</script>

<script id="t:theSchoolTeacherContainer" type="text/html">
    <#if teacherList?? && teacherList?size gt 0>
        <div class="t-show-box">
            <div class="w-table">
                <table id="data-schoolTeacherList">
                    <thead>
                    <tr>
                        <td>姓名</td>
                        <td>ID</td>
                        <td>未布置作业时间</td>
                        <td>我的奖励</td>
                        <td>对方奖励</td>
                        <td style="width: 270px;">操作</td>
                    </tr>
                    </thead>
                    <tbody>
                        <#list  teacherList as t>
                        <tr <#if t_index%2 == 0>class="odd"</#if>>
                            <td>${t.userName!}</td>
                            <td>${t.userId!}</td>
                            <td>${t.lastHomeworkDays!'0'}</td>
                            <td><i class="icon-live icon-yellow-gold"></i>${t.activateIntegral!'0'}园丁豆</td>
                            <td><i class="icon-live icon-yellow-gold"></i>50园丁豆</td>
                            <td>
                                <a class="awaken_but" data-teacher-id="${t.userId!}" data-teacher-homowork-type="${t.type!}" href="javascript:void(0);">唤醒</a>
                            </td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
            <#if teacherList?size gt 5>
                <div class="t-show-box">
                    <div class="w-turn-page-list data-schoolTeacherList_page"></div>
                </div>
            </#if>
        </div>
    <#else>
        <div class="w-ag-center w-gray" style="font-size: 14px; padding: 50px 0;">暂无待唤醒的老师</div>
    </#if>
</script>

<script id="t:findactivatingteacher" type="text/html">
    <% if (content.length > 0) {%>
        <table id="findactivatingteacher">
            <thead>
            <tr>
                <td style="width:135px; ">唤醒老师</td>
                <td style="width:102px; ">登录</td>
                <td style="width:102px; ">布置作业</td>
                <td style="width:102px; ">检查作业</td>
                <td style="width:102px; ">同一个班至少8人完成作业</td>
                <td style="width:102px; ">我的奖励</td>
                <td style="width:160px; ">建议</td>
                <td>操作</td>
            </tr>
            </thead>
            <tbody>
            <%for(var i = 0; i < content.length; i++){%>
            <tr <%if(i%2 == 0){%>class="odd"<%}%>>
                <td><%= content[i].userName %><br/>（<%= content[i].userId %>）</td>
                <td>
                    <i class="icon-live icon-no <%if (content[i].lastLoginDays != null){%> icon-yes <%}%>"></i>
                    <p><%if (content[i].lastLoginDays != null){%> <%= content[i].lastLoginDays %> <% }else{ %>未登录<%}%></p>
                </td>
                <td>
                    <i class="icon-live icon-no <%if (content[i].lastHomeworkDays != null){%> icon-yes <%}%>"></i>
                    <p><%if (content[i].lastHomeworkDays != null){%> <%= content[i].lastHomeworkDays %> <% }else{ %>未布置<%}%></p>
                </td>
                <td>
                    <i class="icon-live icon-no <%if (content[i].lastCheckHomeworkDays != null && content[i].lastCheckHomeworkDays == '已检查'){%> icon-yes <%}%>"></i>
                    <p><%if (content[i].lastCheckHomeworkDays != null && content[i].lastCheckHomeworkDays == '已检查'){%> <%= content[i].lastCheckHomeworkDays %> <% }else{ %>未检查<%}%></p>
                </td>
                <td>
                    <i class="icon-live icon-no <%if (content[i].maxHomeworkFinishCount != null && content[i].finishLighted) {%> icon-yes <%}%>"></i>
                    <p><%if (content[i].maxHomeworkFinishCount != null && content[i].finishLighted){%> <%= content[i].maxHomeworkFinishCount %> <% }else{ %>未做<%}%></p>
                </td>
                <td><i class="icon-live icon-yellow-gold"></i><%if (content[i].activateIntegral != null){%><%= content[i].activateIntegral %><%}%></td>
                <td><%if (content[i].suggestion != null){%> <%= content[i].suggestion %> <%}%></td>
                <td>
                    <a class="removeInviteOne" data-history-id="<%= content[i].historyId %>" href="javascript:void(0);" style="width: 70px;">取消唤醒</a>
                </td>
            </tr>
            <%}%>
            </tbody>
        </table>
        <% if (content.length > 5) {%>
            <div class="t-show-box">
                <div class="w-turn-page-list findactivatingteacher_page"></div>
            </div>
        <%}%>
    <% }else{ %>
        <div class="w-ag-center w-gray" style="font-size: 14px; padding: 50px 0;">暂无唤醒中的老师</div>
    <%}%>
</script>

<script id="t:findactivatedteacher" type="text/html">
    <% if (content.length > 0) {%>
    <div class="w-table">
        <table>
            <thead>
                <tr>
                    <td>唤醒老师</td>
                    <td>成功唤醒</td>
                    <td>唤醒时长</td>
                    <td>我的奖励</td>
                    <td>对方奖励</td>
                </tr>
            </thead>
            <tbody>
                <%for(var i = 0; i < content.length; i++){%>
                    <tr <%if(i%2 == 0){%>class="odd"<%}%> <%if(i > 6){%>style="display: none;"<%}%>>
                        <td><%= content[i].userName %> （<%= content[i].userId %>）</td>
                        <td><%if (content[i].activateSuccessDays != 0){%> <%= content[i].activateSuccessDays%>天前 <%} else {%> 当天 <%}%></td>
                        <td><%if (content[i].activateSpendDays != 0){%> <%= content[i].activateSpendDays%>天 <%} else {%> 当天 <%}%></td>
                        <td><%if (content[i].activateIntegral != null){%> <%= content[i].activateIntegral %> <%}%></td>
                        <td><%if (content[i].oppoIntegral != null){%> <%= content[i].oppoIntegral %> <%}%></td>
                    </tr>
                <%}%>
            </tbody>
        </table>
        <% if (content.length > 5) {%>
        <div class="t-show-box data-teacherCertification">
            <div class="w-turn-page-list">
                <a href="javascript:void (0)">查看更多<span class="w-icon-arrow"></span></a>
            </div>
        </div>
        <%}%>
    </div>
    <% }else{ %>
    <div class="w-ag-center w-gray" style="font-size: 14px; padding: 50px 0;">暂无成功唤醒记录</div>
    <%}%>
</script>

<script id="T:帮助老师认证" type="text/html">
    <% if (content.length > 0) {%>
    <div class="w-table" id="<%=dataType%>">
        <table>
            <thead>
            <tr>
                <td style="width: 10%;">姓名</td>
                <td style="width: 10%">电话</td>
                <td style="width: 10%">登录</td>
                <td style="width: 10%">创建班级</td>
                <td style="width: 10%">学生登录</td>
                <td style="width: 10%">检查作业</td>
                <td style="width: 10%">完成认证</td>
                <td style="width: 15%">我的帮助奖励 <span class="w-orange">最高</span></td>
                <td style="width: 20%">&nbsp;</td>
            </tr>
            </thead>
            <tbody>
            <%for(var i = 0; i < content.length; i++){%>
                <tr <%if(i%2 == 0){%>class="odd"<%}%> data-index="<%=i%>">
                    <td><%= content[i].userName%><br/>(<%= content[i].userId%>)</td>
                    <td><%= (content[i].mobile ? content[i].mobile : '&nbsp;')%></td>
                    <td><i class="icon-live icon-no <%if(content[i].login){%>icon-yes<%}%>"></i></td>
                    <td><i class="icon-live icon-no <%if(content[i].clazzFlag){%>icon-yes<%}%>"></i></td>
                    <td><i class="icon-live icon-no <%if(content[i].studentLoginFlag){%>icon-yes<%}%>"></i></td>
                    <td><i class="icon-live icon-no <%if(content[i].hkFlag){%>icon-yes<%}%>"></i></td>
                    <td><i class="icon-live icon-no "></i></td>
                    <td><i class="icon-live icon-yellow-gold"></i>100园丁豆</td>
                    <td>
                        <%if(content[i].mentorExist){%>
                            <%=content[i].mentorName%>老师在帮TA
                        <%}else{%>
                        <a href="javascript:void(0);" style="width: 80px;" class="w-blue data-forTaCertification" data-id="<%= content[i].userId%>" data-category="MENTOR_AUTHENTICATION" data-name="<%= content[i].userName%>" data-index="<%=i%>">帮助TA</a>
                        <a href="javascript:void(0);" style="width: 60px;" class="w-blue data-cancelAuth" data-type="1" data-userid="<%= content[i].userId%>" data-username="<%= content[i].userName%>" data-index="<%=i%>">举报</a>
                        <%}%>
                    </td>
                </tr>
            <%}%>
            </tbody>
        </table>
        <% if (content.length > 5) {%>
        <div class="t-show-box">
            <div class="w-turn-page-list <%=dataType%>_page"></div>
        </div>
        <%}%>
    </div>
    <% }else{ %>
    <div class="w-ag-center w-gray" style="font-size: 14px; padding: 50px 0;">暂无记录</div>
    <%}%>
</script>

<#--<script id="T:期末回馈计划" type="text/html">-->
<#--<div class="w-base">-->
    <#--<div class="w-base-title">-->
        <#--<h3>限时帮助：期末回馈计划</h3>-->
        <#--<div class="w-base-ext">-->
            <#--<span class="w-bast-ctn" style="color: #667284;">5.26至6.15，帮助TA每增加 <strong class="w-orange">30</strong> 名新学生，可获得 <strong class="w-orange">100</strong> 园丁豆奖励，<strong class="w-orange">300</strong> 园丁豆封顶</span>-->
        <#--</div>-->
    <#--</div>-->
    <#--<div class="w-base-container">-->
        <#--<% if (content.length > 0) {%>-->
            <#--<div class="w-table" id="<%=dataType%>">-->
                <#--<table>-->
                    <#--<thead>-->
                        <#--<tr <%if(i%2 == 0){%>class="odd"<%}%> data-index="<%=i%>">-->
                            <#--<th style="width:20%;">姓名</th>-->
                            <#--<th style="width:20%;">电话</th>-->
                            <#--<th style="width:20%;">新学生数量</th>-->
                            <#--<th style="width:20%;">我的帮助奖励 <span class="w-orange">最高</span></th>-->
                            <#--<th style="width:20%;">操作</th>-->
                        <#--</tr>-->
                    <#--</thead>-->
                    <#--<tbody>-->
                    <#--<%for(var i = 0; i < content.length; i++){%>-->
                        <#--<tr <%if(i%2 == 0){%>class="odd"<%}%> data-index="<%=i%>">-->
                            <#--<th><%= content[i].userName%><br/>(<%= content[i].userId%>)</th>-->
                            <#--<th><%=(content[i].mobile ? content[i].mobile : '无')%></th>-->
                            <#--<th><%=content[i].sCount%></th>-->
                            <#--<th><i class="icon-live icon-yellow-gold"></i>300园丁豆</th>-->
                            <#--<th>-->
                                <#--<%if(content[i].mentorExist){%>-->
                                    <#--<%=content[i].mentorName%>老师在帮TA-->
                                <#--<%}else{%>-->
                                <#--<a href="javascript:void(0);" class="w-blue data-forTaCertification" data-id="<%= content[i].userId%>" data-category="MENTOR_TERM_END" data-mobile="<%= content[i].mobile%>" data-name="<%= content[i].userName%>" data-index="<%=i%>">帮助TA</a>-->
                                <#--<%}%>-->
                            <#--</th>-->
                        <#--</tr>-->
                    <#--<%}%>-->
                    <#--</tbody>-->
                <#--</table>-->
            <#--</div>-->
            <#--<% if (content.length > 5) {%>-->
                <#--<div class="t-show-box">-->
                    <#--<div class="w-turn-page-list <%=dataType%>_page"></div>-->
                <#--</div>-->
            <#--<%}%>-->
        <#--<% }else{ %>-->
            <#--<div class="w-ag-center w-gray" style="font-size: 14px; padding: 50px 0;">暂无数据</div>-->
        <#--<%}%>-->
    <#--</div>-->
<#--</div>-->
<#--</script>-->

<script id="T:可指导TA邀请新生" type="text/html">
<div class="w-base">
    <div class="w-base-title">
        <h3> 可指导TA邀请新生</h3>
        <div class="w-base-ext">
            <span class="w-bast-ctn" style="color: #667284;">每指导1名新认证老师达到 <strong class="w-orange">30/60/90</strong> 名学生(完成3次作业)，可累计奖励 <strong class="w-orange">100/200/300</strong> 园丁豆</span>
        </div>
    </div>
    <div class="w-base-container">
        <% if (content.length > 0) {%>
            <div class="w-table" id="<%=dataType%>">
                <table>
                    <thead>
                        <tr>
                            <th style="width:20%;">姓名</th>
                            <th style="width:20%;">电话</th>
                            <th style="width:20%;">当前学生数</th>
                            <th style="width:20%;">我的帮助奖励 <span class="w-orange">最高</span></th>
                            <th style="width:20%;">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                    <%for(var i = 0; i < content.length; i++){%>
                        <tr <%if(i%2 == 0){%>class="odd"<%}%> data-index="<%=i%>">
                            <th><%= content[i].userName%><br/>(<%= content[i].userId%>)</th>
                            <th><%=(content[i].mobile ? content[i].mobile : '无')%></th>
                            <th><%=content[i].sCount%></th>
                            <th><i class="icon-live icon-yellow-gold"></i>300园丁豆</th>
                            <th>
                                <%if(content[i].mentorExist){%>
                                    <%=content[i].mentorName%>老师在帮TA
                                <%}else{%>
                                <a href="javascript:void(0);" class="w-blue data-forTaCertification" data-id="<%= content[i].userId%>" data-category="MENTOR_NEW_ST_COUNT" data-name="<%= content[i].userName%>" data-index="<%=i%>">帮助TA</a>
                                <%}%>
                            </th>
                        </tr>
                    <%}%>
                    </tbody>
                </table>
            </div>
            <% if (content.length > 5) {%>
                <div class="t-show-box">
                    <div class="w-turn-page-list <%=dataType%>_page"></div>
                </div>
            <%}%>
        <% }else{ %>
            <div class="w-ag-center w-gray" style="font-size: 14px; padding: 50px 0;">暂无数据</div>
        <%}%>
    </div>
</div>
</script>
<script type="text/html" id="T:选择原因">
    <div class="t-changeclass-alert">
        <div class="class">
            <%if(dataType == 1){%>
            <div style="font-size: 12px; padding: 0 0 15px;">请选择举报原因，我们收到请求会认真核实情况，如果情况属实，将配合校园大使将该老师转出本校！</div>
            <ul class="data-selectContentList">
                <li data-val="非本校老师" class="active" style="cursor: pointer; width: 140px;">
                    <span class="w-radio w-radio-current"></span>
                    非本校老师
                </li>
                <li data-val="不是真实老师" style="cursor: pointer; width: 120px;">
                    <span class="w-radio"></span>
                    不是真实老师
                </li>
                <li data-val="该账号不再使用" style="cursor: pointer;width: 120px;">
                    <span class="w-radio"></span>
                    该账号不再使用
                </li>
            </ul>
            <%}else{%>
            <div style="font-size: 12px; padding: 0 0 15px; color: #f00;">目前仅对以下原因提供取消老师认证的功能，我们收到请求后将认真核实情况，<br/>如理由充足且符合取消认证的条件，将取消该老师认证</div>
            <ul class="data-selectContentList">
                <li data-val="非本校老师" class="active" style="cursor: pointer; width: 140px;">
                    <span class="w-radio w-radio-current"></span>
                    非本校老师
                </li>
                <li data-val="已退休" style="cursor: pointer; width: 120px;">
                    <span class="w-radio"></span>
                    已退休
                </li>
                <li data-val="不当老师了" style="cursor: pointer;">
                    <span class="w-radio"></span>
                    不当老师了
                </li>
            </ul>
            <%}%>
        </div>
    </div>
</script>
</@temp.page>