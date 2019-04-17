
<script type="text/html" id="teacher_information">
    <div class="vacation_box">
        <div class="vacTitle">处理换班
            <%if (res.pendingClazzAlterCount){%>
            <div class="tip_num" style="border-radius:100%;text-align:center;color: white;width:1rem;height:1rem;display: inline-block;font-size:.4rem;line-height:1rem;background:#ff7d5a;position:absolute;left: 18%;"><%=res.pendingClazzAlterCount%></div>
            <%}%>
            <a onclick="openSecond('/mobile/resource/school/clazz_alter.vpage?schoolId=${schoolId!0}')" class="more_btn">更多></a>
        </div>
    </div>
    <div class="vacation_box">
        <div class="vacTitle">竞品信息
            <a onclick = 'openSecond("/view/mobile/crm/school/school_competing_info.vpage?schoolId=${schoolId!0}")' class="more_btn"><%if(res.competitiveProductFlag && res.competitiveProductFlag >1 ){%><i class="icon-competitiveProductFlag"></i><%}else{%>更多><%}%></a>
        </div>
    </div>
    <div class="vacation_box">
    <#--进校记录-->
        <div class="vacTitle">
            进校记录
            <@apptag.pageElement elementCode="b02fc44600ad4767">
                <a class="add_btn add_into_school">+</a>
            </@apptag.pageElement>
        </div>
        <%if (res.schoolRecord){%>
        <% var schoolRecord = res.schoolRecord%>
        <div class="vacItem">
            <div><%=schoolRecord.writeTime%></div>
            <div>拜访人：<%=schoolRecord.workerName%></div>
            <div>拜访老师：<%=schoolRecord.visitTeacherList%></div>
        </div>
        <a onclick="openSecond('/mobile/work_record/schoolRecordListPage.vpage?schoolId=${schoolId!0}')" class="more_btn">更多></a>
        <%}%>
    </div>
    <#--备忘录-->
    <div class="vacation_box">
        <div class="vacTitle">
            备忘录
            <a onclick="openSecond('/mobile/memorandum/add_memorandum_page.vpage?schoolId=${schoolId!0}')" class="add_btn">+</a>
        </div>
        <%if (res.textAgentMemorandum){%>
        <% var textAgentMemorandum = res.textAgentMemorandum%>
        <div class="vacItem">
            <div><%=textAgentMemorandum.content%></div>
            <div><%=textAgentMemorandum.time%></div>
        </div>
        <a onclick="openSecond('/mobile/memorandum/school_memorandum_page.vpage?schoolId=${schoolId!0}')" class="more_btn">更多></a>
        <%}%>
    </div>
    <#--奖品中心-->
    <%if (res.rewardLogistics){%>
    <div class="vacation_box">
        <div class="vacTitle">
            学生奖品
        </div>
        <% var rewardLogistics = res.rewardLogistics%>
        <div class="vacItem">
            <div>发货日期：<%=rewardLogistics.month%></div>
            <div>收货老师：<%=rewardLogistics.receiverName%>
                <span style="float:right">
                        <%if(rewardLogistics.rewardStatus == -1){%><i class="reward_expired"></i><%}%>
                        <%if(rewardLogistics.rewardStatus == 0){%><i class="reward_to_get"></i><%}%>
                        <%if(rewardLogistics.rewardStatus == 1){%><i class="reward_recevied"></i><%}%>
                    </span>
            </div>
            <div>快递单号：<%=rewardLogistics.logisticNo%> &nbsp;<%if (rewardLogistics.companyName){%>（<%=rewardLogistics.companyName%>）<%}%></div>
        </div>
        <a onclick="openSecond('/mobile/resource/school/student_reward_logistics.vpage?schoolId=${schoolId!0}')" class="more_btn">更多></a>
    </div>
    <%}%>
    <#--照片库-->
    <div class="vacation_box">
        <div class="vacTitle">照片库
            <%if (res.pictureAgentMemorandum){%>
            <div class="vacItem">
                <img src="<%=res.pictureAgentMemorandum.content%>${shortIconTail}" alt="">
            </div>
            <%}%>
            <a onclick="openSecond('/mobile/memorandum/photo_library.vpage?schoolId=${schoolId!0}')" class="more_btn">更多></a>
        </div>
    </div>

</script>
