<script type="text/html" id="schoolInfos">
    <%if(region){%>
    <div class="mobileCRM-V2-header">
        <div class="inner">
            <div class="box">
                <a href="/mobile/school/index.vpage" class="headerBack">&lt;&nbsp;返回</a>
                <a href="/mobile/school/region_list.vpage" class="headerBtn">切换区域</a>

                <div class="headerText">选择学校</div>
            </div>
        </div>
    </div>
    <%}else{%>
    <div class="mobileCRM-V2-header">
        <div class="inner">
            <div class="box">
                <div class="headerSearch margin-l" style="margin-right:2.8rem;">
                    <form name="tschool">
                        <input type="text" placeholder="请输入学校名/ID" id="schoolKey" name="schoolKey" value="<%if(schoolName){%><%=schoolName%><%}%>"></input>
                    </form>
                </div>
                <a href="javascript:void(0);" class="headerBack" id="searchSchoolBack">&lt;&nbsp;返回</a>
                <a href="javascript:void(0);" name="tschool" class="headerBtn" id="searchSchoolSubmit">搜索</a>
            </div>
        </div>
    </div>
    <%}%>
    <ul class="mobileCRM-V2-list">
        <li>
            <div class="box link-ico">
                <div class="side-fl side-time">学校</div>
                <div class="side-fr side-time side-width">转化</div>
                <div class="side-fr side-time side-width">认证</div>
                <div class="side-fr side-time side-width">注册</div>
            </div>
        </li>
        <%if(schoolInfos&&schoolInfos.length>0){%>
        <%for(var i=0;i<schoolInfos.length;i++){%>
        <%var schoolInfo=schoolInfos[i]%>
        <li name="sList">
            <a href="javascript:void(0)" class="link link-ico" name="sSInfo">
                <div class="box">
                    <div class="side-fl" style="width:30%; <%if(schoolInfo.authStatus!=1){%>color:red;<%}%>" name="sinfo" sid="<%=schoolInfo.schoolId%>" sname="<%=schoolInfo.schoolName%>"><%=schoolInfo.schoolName%></div>
                    <div class="side-fr side-orange side-width"><%=schoolInfo.teacherAuthRate%>%</div>
                    <div class="side-fr side-orange side-width"><%=schoolInfo.teacherAuthCount%></div>
                    <div class="side-fr side-orange side-width"><%=schoolInfo.teacherTotalCount%></div>
                </div>
            </a>
        </li>
        <%}%>
        <%}else{%>
        <li>
            暂无相关数据
        </li>
        <%}%>
    </ul>
</script>
<script type="text/javascript">
    template.helper('Math_round', function (op1, op2) {
        return Math.round((op1 * 100) / op2);
    });
    //搜索事件API接口
    window.searchSchoolSumbimt = function (schoolKey) {
    };
    $(document).off("click", "#searchSchoolSubmit");
    $(document).on("click", "#searchSchoolSubmit", function () {
        var schoolKey = $("#schoolKey").val();
        if (schoolKey) {
            window.searchSchoolSumbimt(schoolKey);
        }
        else {
            alert("请输入学校名称或ID");
            return false;
        }
    });

    //搜索学校的回退按钮事件API接口
    window.searchSchoolBack = function () {
    };
    $(document).off("click", "#searchSchoolBack");
    $(document).on("click", "#searchSchoolBack", function () {
        window.searchSchoolBack();
    });

    //点击搜索结果中的每一个行执行结果事件
    window.schoolItemClick = function (schoolName, sid) {
    };
    $(document).off("click", "a[name='sSInfo']");
    $(document).on("click", "a[name='sSInfo']", function () {
        var elem = $(this).find("div[name='sinfo']");
        var sName = elem.attr("sname");
        var sId = elem.attr("sid");
        window.schoolItemClick(sName, sId)
    });
</script>
