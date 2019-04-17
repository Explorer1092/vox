<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="选择参与会议场次" pageJs="addMeeting" footerIndex=4 navBar="hidden">
    <@sugar.capsule css=['res','school']/>
<style>
    .top_head{padding:1rem;font-size:.65rem;text-align:center;
        cursor: pointer;}
    .top_head span{padding:.5rem;border:.05rem solid #000}
    .bottom_head{padding:.5rem 1rem;font-size:.65rem;text-align:left;}
    .inner-right{float:right}
    .main_body{font-size:.7rem}
    .body_div_bottom{padding:.5rem 0}
    .body_div{padding:.5rem 1rem;
        cursor: pointer;}
</style>
<a href="javascript:void(0)" class="inner-right js-submitVisBtn js-success" style="display:none;">提交</a>
<div class="flow vacation_list">
    <div class="top_head" onclick= creatMeeting()>
        <span>
            我是会议组织人，直接创建组会。
        </span>
    </div>
    <div class="bottom_head">
        <span>
            我是会议参与人，选择参与会议场次：
        </span>
    </div>
    <div class="main_body ">

    </div>

</div>
<script id="main_body" type="text/html">
        <ul>
            <%if(res && res.length > 0){%>
            <%for(var i = 0; i< res.length;i++){%>
            <%var CrmWorkRecord = res[i]%>
            <li class="js-todoRecord" onclick="addMeeting(this)" data-id="<%=CrmWorkRecord.id%>" style="cursor: pointer">
                <div class="student_name">
                    <div class="icon_info" style="color:#636880"><%=CrmWorkRecord.workTime%></div>
                    <%=CrmWorkRecord.workerName%>
                </div>
                <div class="side">
                    <%if(CrmWorkRecord.meetingType == 'SCHOOL_LEVEL'){%>
                        <span><%=CrmWorkRecord.schoolName%></span>
                    <%}else{%>
                        <span><%=CrmWorkRecord.workTitle%></span>
                    <%}%>
                </div>
            </li>
            <%}%>
            <%}else{%>
            <li style="text-align: center;">暂无数据</li>
            <%}%>
        </ul>
</script>

<script src="/public/rebuildRes/js/mobile/intoSchool/add_intoschool.js"></script>
<script>
    $(document).ready(function () {
        reloadCallBack();
    });
    var moduleName = "m_9HYOoJg7";
    var workRecordType = "VISIT" ;
    var userId = "${requestContext.getCurrentUser().getUserId()!0}";
    var ojbArr = ["1分/差","2分/一般","3分/好","4分/很好","5分/标杆"];
    $(document).on("click",".evaluate ul li",function(){
        var index = $(this).index();
        var parent = $(this).parent();
        var parentLi = parent.find("li");
        parentLi.removeClass("active");
        parent.siblings(".per").html(ojbArr[index]);
        for(var i=0;i <= index ;i++){
            parentLi.eq(i).addClass("active");
        }
    });
    var schoolRecordId = "${schoolRecordId!}";
    var AT = new agentTool();
    var alertDialog = AT.getCookie("alertDialog");
    if(alertDialog == -1){
        $("#completeSchoolInfo").hide();
    }
    AT.clearCookie("alertDialog");
    $(document).on("click","#completeSchoolInfo .schoolSubmitBtn",function () {
        window.location.href = "/mobile/school_clue/confirm_school_info.vpage?schoolId=${schoolId!0}";
    });
    function creatMeeting(){
        openSecond('/mobile/work_record/addGroupMeeting.vpage');
    };
    function addMeeting(_this){
        openSecond('/mobile/work_record/joinMeeting.vpage?id='+$(_this).data('id'));
    };
</script>
</@layout.page>
