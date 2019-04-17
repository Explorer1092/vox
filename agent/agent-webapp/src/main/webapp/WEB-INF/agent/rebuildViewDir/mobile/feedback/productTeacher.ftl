<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="选择老师" pageJs="productTeacher" footerIndex=4>
    <@sugar.capsule css=['new_base','school']/>
<div class="search">
    <input class="s-input" placeholder="请输入姓名 / ID / 手机号码" id="teacherSearchInput"/>
    <span class="s-go js-search">搜索</span>
</div>
<div class="s-list" id="teacherContainer"></div>
<div id="loading" style="display:none;position: absolute;top: 3.5rem;left: 0;width: 100%;height: 100%;z-index: 10;color: rgb(234, 164, 164);font-size: 38px;">
    <img style='display:block;padding:2rem 0;margin:3.4rem auto;width:6rem;' src='/public/rebuildRes/image/mobile/res/loading.gif' alt="正在加载……"/>
</div>
<script id="teacherListTemp" type="text/html">

    <%for(var i = 0; i < teacherList.length; i++) {%>
        <%var rank=teacherList[i]%>
        <div class="item js-teacherItem" data-sid="<%=rank.teacherId %>">
            <div>
                <p class="name"><%=rank.teacherName%>(<%= rank.teacherId%>)
                    <span>
                        <%if(rank.isSchoolQuizBankAdmin !=null && rank.isSchoolQuizBankAdmin){%><i class="icon-guan"></i><%}%>
                        <%if(rank.subjectLeaderFlag != null && rank.subjectLeaderFlag){%><i class="icon-zu"></i><%}%>
                    </span>
                    <span>
                        <%for (var j=0;j< rank.subjects.length;j++){%>
                            <i class="icon-<%=rank.subjects[j]%>"></i>
                        <%}%>
                    </span>
                </p>
                <span style="font-size: .6rem">
                    <%=rank.schoolName%>
                </span>
            </div>
        </div>
    <% } %>
</script>
<script>
    var backUrl = "${back!""}";
    var type = "${type!""}";
    $(document).ready(function () {
        var setTopBar = {
            show:true,
            rightText:'',
            rightTextColor:"ff7d5a",
            needCallBack:false
        } ;
        setTopBarFn(setTopBar);
    });
</script>
</@layout.page>