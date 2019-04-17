<script type="text/html" id="schoolInfos">
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
    <ul class="mobileCRM-V2-list">
        <%if(schoolInfos&&schoolInfos.length>0){%>
        <%for(var i=0;i< schoolInfos.length; i++){%>
        <%var schoolInfo = schoolInfos[i]%>
        <li name="sList">
            <a href="javascript:void(0)" class="link link-ico" name="sSchool">
                <div class="box">
                    <div class="side-fl" style="width:96%;" name="iSchool" sn="<%=schoolInfo.schoolName%>" si="<%=schoolInfo.schoolId%>" sl="<%=schoolInfo.schoolLevel%>">
                        <%=schoolInfo.schoolName%>
                    </div>
                </div>
            </a>
        </li>
        <%}%>
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
    window.schoolItemClick = function (schoolName, schoolId, schoolLevel) {
    };
    $(document).off("click", "a[name='sSchool']");
    $(document).on("click", "a[name='sSchool']", function () {
        var iSchool = $(this).find("div[name='iSchool']");
        var schoolName = iSchool.attr("sn");
        var schoolId = iSchool.attr("si");
        var schoolLevel = iSchool.attr("sl");
        window.schoolItemClick(schoolName, schoolId, schoolLevel)
    });
</script>
