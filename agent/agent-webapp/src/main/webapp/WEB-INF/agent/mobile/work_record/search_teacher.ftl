<script type="text/html" id="sTeacher">
    <div class="mobileCRM-V2-header">
        <div class="inner">
            <div class="box">
                <div class="headerSearch margin-l" style="margin-right:2.8rem;">
                    <form  name="teacher" <%if(url){%> action="<%=url%>" <%}%>>
                    <input type="text" placeholder="请输入老师手机号/姓名/ID" id="teacherKey" name="teacherKey" value ="<%if(teacherKey){%><%=teacherKey%><%}%>">
                    </form>
                </div>
                <a href="javascript:void(0);" class="headerBack" id="searchTeacherBack">&lt;&nbsp;返回</a>
                <a href="javascript:void(0);" class="headerBtn" name="tSearch" id="searchTeacherSubmit">搜索</a>
            </div>
        </div>
    </div>
    <ul class="mobileCRM-V2-list">
        <%for(var i = 0; i < teacherSummaryList.length; i++){%>
        <li name="sList">
            <a href="javascript:void(0)" class="link link-ico" name="listTeacher">
                <div class="box">
                    <div class="side-fl"><%=teacherSummaryList[i].realName%></div>
                    <div name = "info" tid="<%=teacherSummaryList[i].teacherId%>" num = "<%=teacherSummaryList[i].mobile%>" tname="<%=teacherSummaryList[i].realName%>" sname="<%=teacherSummaryList[i].schoolName%>" sid="<%=teacherSummaryList[i].schoolId%>"></div>
                    <div class="state-box">
                        <%if (teacherSummaryList[i].authStatus == 1){%>
                        <div class="green">已认证</div>
                        <%}%>
                        <%if (teacherSummaryList[i].ambassador){%>
                        <div class="blue">校园大使</div>
                        <%}%>
                    </div>
                </div>
                <p><%=teacherSummaryList[i].schoolName%></p>
            </a>
        </li>
        <%}%>
    </ul>
    <%if(!teacherSummaryList || teacherSummaryList.length===0){%>
    <ul class="mobileCRM-V2-list mobileCRM-V2-mt" id="notFound">
        <li>
            <a href="javascript:void(0)" class="link link-ico" id="add_new_teacher">
                <div class="side-fl side-time" id="addNewTeacherTip">暂无相关数据</div>
            </a>
        </li>
    </ul>
    <%}%>
</script>

<#--分学科的添加老师模板-->
<script type="text/html" id="subjectTeacherTemp">
    <div class="mobileCRM-V2-header">
        <div class="inner">
            <div class="box">
                <a href="javascript:void(0);" class="headerBack" id="searchTeacherBack">&lt;&nbsp;返回</a>
                <div class="headerText margin-l margin-r">
                    <span>选择老师</span>
                </div>
            </div>
        </div>
    </div>
    <div class="mobileCRM-V2-tab">
        <div class="js-ctrlTab active" data-tab="english">英语</div>
        <div class="js-ctrlTab" data-tab="math">数学</div>
        <div class="js-ctrlTab" data-tab="chinese">语文</div>
    </div>
    <div class="js-englishBox js-sub">
        <ul class="mobileCRM-V2-list js-englishBox">
            <%for(var i = 0; i < englishTeacherSummaryList.length; i++){%>
            <li name="sList">
                <a href="javascript:void(0)" class="link link-ico" name="listSubTeacher" data-type="english">
                    <div class="box">
                        <div class="side-fl"><%=englishTeacherSummaryList[i].realName%></div>
                        <div name = "info" tid="<%=englishTeacherSummaryList[i].teacherId%>" num = "<%=englishTeacherSummaryList[i].mobile%>" tname="<%=englishTeacherSummaryList[i].realName%>"></div>
                    </div>
                </a>
            </li>
            <%}%>
        </ul>
        <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
            <ul class="mobileCRM-V2-list">
                <li>
                    <a href="javascript:void(0);" class="link link-ico js-addNewTeacher" data-type="english">
                        <div class="side-fl side-orange">添加新老师</div>
                    </a>
                </li>
            </ul>
        </div>
    </div>
    <div class="js-mathBox js-sub"  style="display: none;">
        <ul class="mobileCRM-V2-list">
            <%for(var i = 0; i < mathTeacherSummaryList.length; i++){%>
            <li name="sList">
                <a href="javascript:void(0)" class="link link-ico" name="listSubTeacher" data-type="math">
                    <div class="box">
                        <div class="side-fl"><%=mathTeacherSummaryList[i].realName%></div>
                        <div name = "info" tid="<%=mathTeacherSummaryList[i].teacherId%>" num = "<%=mathTeacherSummaryList[i].mobile%>" tname="<%=mathTeacherSummaryList[i].realName%>"></div>
                    </div>
                </a>
            </li>
            <%}%>
        </ul>
        <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
            <ul class="mobileCRM-V2-list">
                <li>
                    <a href="javascript:void(0);" class="link link-ico js-addNewTeacher" data-type="math">
                        <div class="side-fl side-orange">添加新老师</div>
                    </a>
                </li>
            </ul>
        </div>
    </div>
    <div class="js-chineseBox js-sub"  style="display: none;">
        <ul class="mobileCRM-V2-list">
            <%for(var i = 0; i < chineseTeacherSummaryList.length; i++){%>
            <li name="sList">
                <a href="javascript:void(0)" class="link link-ico" name="listSubTeacher" data-type="chinese">
                    <div class="box">
                        <div class="side-fl"><%=chineseTeacherSummaryList[i].realName%></div>
                        <div name = "info" tid="<%=chineseTeacherSummaryList[i].teacherId%>" num = "<%=chineseTeacherSummaryList[i].mobile%>" tname="<%=chineseTeacherSummaryList[i].realName%>"></div>
                    </div>
                </a>
            </li>
            <%}%>
        </ul>
        <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
            <ul class="mobileCRM-V2-list">
                <li>
                    <a href="javascript:void(0);" class="link link-ico js-addNewTeacher" data-type="chinese">
                        <div class="side-fl side-orange">添加新老师</div>
                    </a>
                </li>
            </ul>
        </div>
    </div>
</script>

<script type="text/javascript">
    //查询老师请求 API
    window.searchTeacherSubmit=function(val){
    };
    //查询老师返回API
    window.searchTeacherBack = function(){
    };
    //老师列表点击API
    window.teacherItemClick=function(paramObj){
    };
    //增加新老师API
    window.addNewTeacher =function(isAdd){
    }
    //点击搜索事件
    $(document).off("click","#searchTeacherSubmit");
    $(document).on("click","#searchTeacherSubmit",function(){
        var teacherKey = $("#teacherKey").val();
        if(teacherKey){
            window.searchTeacherSubmit(teacherKey);
        }
        else{
            alert("请输入老师名称或ID");
            return false;
        }
    });

    //点击返回事件
    $(document).off("click","#searchTeacherBack");
    $(document).on("click","#searchTeacherBack",function(){
        window.searchTeacherBack();
    });

    //点击老师列表事件
    $(document).off("click","a[name='listTeacher']");
    $(document).on("click","a[name='listTeacher']",function(){
        var elem  = $(this).find("div[name='info']");
        var params ={
            "tid":elem.attr("tid"),
            "tmobile":elem.attr("num"),
            "tname":elem.attr("tname"),
            "sname":elem.attr("sname"),
            "sid":elem.attr("sid"),
            'searchKey':$("#teacherKey").val()
        };
        teacherItemClick(params);
    });
    //新增老师事件
    $(document).off("click","a#add_new_teacher");
    $(document).on("click","a#add_new_teacher",function(){
        window.addNewTeacher();
    });


    //学科方式模板对应事件
    //切换tab
    $(document).on("click",".js-ctrlTab",function(){
        var subTab = this.dataset.tab;
        $(this).addClass("active");
        $(this).siblings("div").removeClass("active");
        $(".js-"+subTab+"Box").show();
        $(".js-"+subTab+"Box").siblings('div.js-sub').hide();
    });

    //添加新老师
    $(document).on("click",".js-addNewTeacher",function(){
        var sub = this.dataset.type;
        //清空上次缓存
        $("input[name='add_teacher_name']").val("");
        $("input[name='add_teacher_mobile']").val("");
        displaySubNameAndStart(sub);
        window.addNewTeacher();
    });

    //点击老师列表
    $(document).on("click","a[name='listSubTeacher']",function(){
        var elem  = $(this).find("div[name='info']");
        var sub = this.dataset.type;
        var params ={
            tid:elem.attr("tid"),
            tmobile:elem.attr("num"),
            tname:elem.attr("tname"),
            sub: sub
        };
        subTeacherItemClick(params);
    });
</script>

