<script type="text/html" id="T:老师列表">
    <p class="s-info bluegrey-color" style="padding:.65rem .85rem .45rem 1.65rem ;border-bottom:0rem">
        注册<%=res.regCnt%>人 被隐藏<span class="hideNum"><%=res.hides%></span>老师
        <span style="float:right">
            <span class="show_hide_teacher" data-index = '1' <%if(res.hide){%>style='display:none'<%}%>> 查看隐藏老师<i class="icon-close"></i>  </span>
            <span class="show_hide_teacher" data-index = '2' <%if(!res.hide){%>style='display:none'<%}%>> 查看老师列表<i class="icon-open"></i>  </span>
        </span>
    </p>
    <%var data = res.teacherList %>
    <%for(var i = 0; i < data.length; ++i){%>
    <div class="teacher yearCard-box" data-sid="<%= data[i].teacherId%>" data-info="<%if(data[i].isRealTeacher){%>1<%}else{%>2<%}%>">
        <div class="year-column">
            <div class="right">
                <%if(data[i].isHidden){%>
                <a href="javascript:void(0);" class="hide_btn js-showBtn">显示</a>
                <%}%>
            </div>
            <div class="left">
                <p class="name" id="detail<%= data[i].teacherId%>"><%=data[i].teacherName%>(<%= data[i].teacherId%>)
                    <%if(data[i].isRealTeacher){%>
                    <span>
                        <%if(data[i].isSchoolQuizBankAdmin){%><i class="icon-guan"></i><%}%>
                        <%if(data[i].isSubjectLeader){%><i class="icon-zu"></i><%}%>
                        <%for (var j = 0 ; j < data[i].subjects.length ; j++){%>
                            <i class="icon-<%=data[i].subjects[j]['subject']%>"></i>
                        <%}%>
                    </span>
                    <span>
                        <%if(data[i].isHidden){%>
                            <i class="icon-hide"></i>
                        <%}%>
                    </span>
                    <span class="icon-box">
                        <%if(data[i].authState==1){%><i class="icon-zheng"></i><%}%>
                    </span>
                    <span>
                        <%if(data[i].vacnHwGroupCount > 0){%><i class="icon-summer-homework"></i><%}%>
                        <%if(data[i].termReviewGroupCount >0){%><i class="icon-final-review"></i><%}%>
                    </span>
                    <%}else{%>
                    <span>
                        <i class="icon-fake"></i>
                        <%for (var j = 0 ; j < data[i].subjects.length ; j++){%>
                        <i class="icon-<%=data[i].subjects[j]%>"></i>
                        <%}%>
                    </span>
                    <%}%>
                </p>
            </div>
        </div>
        <%if(data[i].isRealTeacher){%>
        <div class="year-content">
        <#--中学数学-->
            <%if(data[i].schoolLevel == "MIDDLE" || data[i].schoolLevel == "HIGH"){%>
                <#if modeType?? && modeType == "online">
                    <div class="teacher_title">
                        <ul class="clearfix">
                            <li>本月布置</li>
                            <li>月活</li>
                        </ul>
                    </div>
                    <%var len = data[i].subjects.length%>
                    <%for (var j = 0 ; j < data[i].subjects.length ; j++){%>
                    <%var d = data[i].subjects[j].kpiData%>
                    <div class="teacher_con">
                        <%if(len > 1){%>
                        <span class="card card-<%=data[i].subjects[j].subject%>"><i><%=data[i].subjects[j].subjectName.substring(0,1)%></i></span>
                        <%}else{%>
                        <div class="right">
                            <%if((d.tmHwSc || 0) > 3){%><a href="javascript:void(0);" class="frequent_btn">频繁布置</a><%}%>
                        </div>
                        <%}%>
                        <ul class="clearfix">
                            <li>
                                <%if(d.tmGroupMinHwSc != d.tmGroupMaxHwSc){%>
                                    <span><%if(d.tmGroupMinHwSc != null){%><%=d.tmGroupMinHwSc%><%}else{%>0<%}%></span>-
                                    <span><%if(d.tmGroupMaxHwSc != null){%><%=d.tmGroupMaxHwSc%><%}else{%>0<%}%></span>
                                <%}else{%>
                                    <span><%if(d.tmGroupMinHwSc != null){%><%=d.tmGroupMinHwSc%><%}else{%>0<%}%></span>
                                <%}%>
                            </li>
                            <li>
                                <span><%if(d.tmFinCsHwGte3AuStuCount != null){%><%=d.tmFinCsHwGte3AuStuCount%><%}else{%>0<%}%></span>
                                (上月<%if(d.lmFinCsHwGte3AuStuCount != null){%><%=d.lmFinCsHwGte3AuStuCount%><%}else{%>0<%}%>)
                            </li>
                        </ul>
                        <p class="teacher_infor">
                            <span><%if(d.regStuCount != null){%><%=d.regStuCount%><%}else{%>0<%}%>注册</span>
                            <span><%if(d.auStuCount != null){%><%=d.auStuCount%><%}else{%>0<%}%>认证</span>
                            <span>带<%if(d.classCount != null){%><%=d.classCount%><%}else{%>0<%}%>个班</span>
                            <span><%=data[i].schoolName%></span>
                        </p>
                    </div>
                    <%}%>
                <#else>
                    <%var d = data[i].subjects[0].kpiData%>
                    <ul style="width:75%">
                        <li style="width:40%;">
                        <div>本月扫描试卷套数</div>
                        <div class="font">
                            <%if(d.tmScanTpCount != null){%><%=d.tmScanTpCount%><%}else{%>0<%}%>
                            <span style="font-size:.6rem">（上月<%if(d.lmScanTpCount != null){%><%=d.lmScanTpCount%><%}else{%>0<%}%>）</span>
                        </div>
                        </li>
                        <li style="width:40%;">
                            <div>普通扫描(≥1次)</div>
                            <div class="font"><%if(d.tmFinCsTpGte1StuCount != null){%><%=d.tmFinCsTpGte1StuCount%><%}else{%>0<%}%></div>
                        </li>
                    </ul>
                    <p class="teacher_infor">
                        <span><%if(d.klxTnCount != null){%><%=d.klxTnCount%><%}else{%>0<%}%>考号</span>
                        <span>带<%if(d.classCount != null){%><%=d.classCount%><%}else{%>0<%}%>个班</span>
                        <span><%=data[i].schoolName%></span>
                    </p>
                </#if>
            <%}else{%>
                <div class="teacher_title">
                    <ul class="clearfix">
                        <li>本月布置</li>
                        <li>月活</li>
                    </ul>
                </div>
                <%var len = data[i].subjects.length%>
                <%for (var j = 0 ; j < data[i].subjects.length ; j++){%>
                <%var d = data[i].subjects[j].kpiData%>
                <div class="teacher_con">
                    <%if(len > 1){%>
                    <span class="card card-<%=data[i].subjects[j].subject%>"><i><%=data[i].subjects[j].subjectName.substring(0,1)%></i></span>
                    <%}else{%>
                    <div class="right">
                        <%if((d.tmHwSc || 0) > 3){%><a href="javascript:void(0);" class="frequent_btn">频繁布置</a><%}%>
                    </div>
                    <%}%>
                    <ul class="clearfix">
                        <li>
                            <%if(d.tmGroupMinHwSc != d.tmGroupMaxHwSc){%>
                            <span><%if(d.tmGroupMinHwSc != null){%><%=d.tmGroupMinHwSc%><%}else{%>0<%}%></span>-
                            <span><%if(d.tmGroupMaxHwSc != null){%><%=d.tmGroupMaxHwSc%><%}else{%>0<%}%></span>
                            <%}else{%>
                            <span><%if(d.tmGroupMinHwSc != null){%><%=d.tmGroupMinHwSc%><%}else{%>0<%}%></span>
                            <%}%>
                        </li>
                        <li>
                            <span><%if(d.tmFinCsHwGte3AuStuCount != null){%><%=d.tmFinCsHwGte3AuStuCount%><%}else{%>0<%}%></span>
                            (上月<%if(d.lmFinCsHwGte3AuStuCount != null){%><%=d.lmFinCsHwGte3AuStuCount%><%}else{%>0<%}%>)
                        </li>
                    </ul>
                    <p class="teacher_infor">
                        <#if modeType?? && modeType == "offline">
                            <span><%if(d.stuKlxTnCount != null){%><%=d.stuKlxTnCount%><%}else{%>0<%}%>考号</span>
                            <span>带<%if(d.clazzCount != null){%><%=d.clazzCount%><%}else{%>0<%}%>个班</span>
                            <span><%=d.schoolName%></span>
                        <#else>
                            <span><%if(d.regStuCount != null){%><%=d.regStuCount%><%}else{%>0<%}%>注册</span>
                            <span><%if(d.auStuCount != null){%><%=d.auStuCount%><%}else{%>0<%}%>认证</span>
                            <span>带<%if(d.classCount != null){%><%=d.classCount%><%}else{%>0<%}%>个班</span>
                            <span><%=data[i].schoolName%></span>
                        </#if>
                    </p>
                </div>
                <%}%>
            <%}%>
        </div>
        <%}else{%>
        <div class="year-content">
            <div class="teacher_con">
                <p class="teacher_infor">
                    <span><%=data[i].schoolName%></span>
                </p>
            </div>
        </div>
        <%}%>
    </div>
    <%}%>
</script>