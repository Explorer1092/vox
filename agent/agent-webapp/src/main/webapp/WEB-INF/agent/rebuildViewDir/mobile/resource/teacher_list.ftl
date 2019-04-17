<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="全部老师" pageJs="common" footerIndex=2 navBar="show">
<@sugar.capsule css=['res']/>
<style>
    .yearCard-box{margin:.5rem 0;padding:.5rem .75rem;background-color:#fff}
    .yearCard-box .year-column .left .name{font-size:.7rem;color:#636880}
    .yearCard-box .year-column .right .hide_btn{float:right;font-size:.7rem;color:#ff7d5a}
    .yearCard-box .year-content{padding:.375rem 0}
    .yearCard-box .year-content .right{float:right}
    .yearCard-box .year-content .right .frequent_btn{width:2.925rem;height:1.05rem;line-height:1.05rem;display:inline-block;text-align:center;font-size:.55rem;color:#fff;background-color:#ff7d5a;border-radius:1.5rem}
    .yearCard-box .year-content ul{width:70%;overflow:hidden}
    .yearCard-box .year-content ul li{float:left;width:50%;font-size:.6rem;color:#636880}
    .yearCard-box .year-content ul li .font{font-size:.8rem}
    .yearCard-box .year-content ul li .font .orange{display:inline-block;color:#ff7d5a}
    .yearCard-box .year-side{padding:.5rem 0 0 0;border-top:.05rem dashed #f0eff5;font-size:.5rem;color:#9199bb}
    .yearCard-box .year-side span{display:inline-block;padding:0 .75rem 0 0;text-overflow:ellipsis;overflow:hidden;white-space:nowrap}
    .yearCard-box .year-side span.area{width:5.875rem}
    .teacher .year-content .teacher_title{margin: .1rem 0;}
    .teacher .year-content ul{width: 100%;}
    .teacher .year-content .teacher_con{position: relative;padding: 0.3rem 0; border-bottom: 1px solid #bbb;}
    .teacher .year-content .teacher_title ul li,
    .teacher .year-content .teacher_con ul li{float: left;width: 33.3%;font-size: .6rem;color: #636880;}
    .teacher .year-content .teacher_con ul li span{font-size: .8rem;}
    .teacher .year-content .teacher_con:last-child{border: 0;}
    .teacher .year-content .teacher_infor{text-overflow: ellipsis;overflow: hidden;white-space: nowrap;}
    .teacher .year-content .teacher_infor span{font-size: .5rem;color: #9199bb;margin-right: .5rem;}

    .card{position: absolute;right: 0;top: 0;width: 0;height: 0;font-size: .6rem;color: #fff;border-bottom: .8rem solid transparent;border-left: .8rem solid transparent;}
    .teacher_con:nth-child(2) .card{top: -1rem;}
    .card-ENGLISH{border-top: .8rem solid #D589D9;border-right: .8rem solid #D589D9;}
    .card-MATH{border-top: .8rem solid #FF866C;border-right: .8rem solid #FF866C;}
    .card-CHINESE{border-top: .8rem solid #74C5FD;border-right: .8rem solid #74C5FD;}
    .card i{position: absolute;top: -.7rem;right: -.6rem;}
</style>
<div class="crmList-box">
    <div class="fixed-head">
        <div class="c-head">
            <a href="../school/list.vpage">学校列表</a>
            <a class="the">全部老师</a>
        </div>
        <!--查询输入框-->
        <div class="c-search">
            <input class="t-input" placeholder="请输入姓名 / ID / 手机号码" <#if key??>value="${key!}"</#if>/>
            <span class="js-search teacher-search">搜索</span>
        </div>
    </div>

    <div class="c-main">
        <!--老师列表-->
        <p class="error-tip" style="text-align:center;color:red;line-height:2em;font-size:0.75rem;padding:0 2rem;word-break: break-all;">${error!''}</p>
        <div id="teacher-list" class="c-list">
        </div>
    </div>
</div>
<div class="mask"></div>
<script type="text/html" id="T:老师列表">
    <%for(var i = 0; i < data.length; ++i){%>
    <div class="teacher yearCard-box" data-sid="<%= data[i].teacherId%>" data-info="<%if(data[i].isRealTeacher){%>1<%}else{%>2<%}%>">
        <div class="year-column">
            <div class="left">
                <p class="name" id="detail<%= data[i].teacherId%>"><%=data[i].teacherName %>(<%= data[i].teacherId%>)
                    <%if(data[i].isRealTeacher){%>
                        <span>
                            <%if(data[i].isSchoolQuizBankAdmin){%><i class="icon-guan"></i><%}%>
                            <%if(data[i].subjectLeaderFlag){%><i class="icon-zu"></i><%}%>
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
                            <%if(data[i].isAmbassador){%><i class="icon-shi"></i><%}%>
                            <%if(data[i].authState==1){%><i class="icon-zheng"></i><%}%>
                        </span>
                        <span>
                            <%if(data[i].vacnHwGroupCount > 0){%><i class="icon-summer-homework"></i><%}%>
                            <%if(data[i].termReviewGroupCount >0){%><i class="icon-final-review"></i><%}%>
                            <%if(data[i].isNewTeacher){%><span class="icon-new"></span><%}%>
                        </span>

                    <%}else{%>
                        <span>
                            <i class="icon-fake"></i>
                            <%for (var j = 0 ; j < data[i].subjects.length ; j++){%>
                                <i class="icon-<%=data[i].subjects[j].subject%>"></i>
                            <%}%>
                        </span>
                    <%}%>
                </p>
            </div>
            <%if(data[i].isHidden){%>
                <div class="right js-showTeacher" data-id="<%= data[i].teacherId%>">
                    <p class="name" style="font-size:.65rem;color:#ff7d5a">显示</p>
                </div>
            <%}%>
        </div>
        <%if(data[i].isRealTeacher){%>

            <#---->
        <#--中学数学-->
            <%if(data[i].schoolLevel == 'MIDDLE' || data[i].schoolLevel == "HIGH"){%>
                <div class="year-content">
                    <%var _subject = data[i].subjects[0].kpiData%>
                    <ul>
                        <li>
                            <div>本月扫描试卷套数</div>
                            <div class="font"><span>
                                <%if(_subject.tmScanTpCount !=null){%><%=_subject.tmScanTpCount%><%}else{%>0<%}%>
                                (上月<%if(_subject.lmScanTpCount != null){%><%=_subject.lmScanTpCount%><%}else{%>0<%}%>)
                            </span></div>
                        </li>
                        <li>
                            <div>普通扫描(≥1次)</div>
                            <div class="font"><%if(_subject.tmFinCsTpGte1StuCount != null){%><%=_subject.tmFinCsTpGte1StuCount%><%}else{%>0<%}%></div>
                        </li>
                    </ul>
                    <p class="teacher_infor">
                        <%if(_subject.klxTnCount != null){%><%=_subject.klxTnCount%><%}else{%>0<%}%>考号
                        <span>带<%if(_subject.classCount != null){%><%=_subject.classCount%><%}else{%>0<%}%>个班</span>
                        <span><%=data[i].schoolName%></span>
                    </p>
                </div>
            <%}else{%>
                <div class="year-content">
                    <div class="teacher_title">
                        <ul class="clearfix">
                            <li>本月布置</li>
                            <li>月活</li>
                        </ul>
                    </div>
                    <%var len = data[i].subjects.length%>
                    <%for(var j = 0; j < data[i].subjects.length;j++){%>
                        <%var _subject = data[i].subjects[j].kpiData%>
                        <div class="teacher_con">
                            <%if(len > 1){%>
                            <span class="card card-<%=data[i].subjects[j].subject%>"><i><%=data[i].subjects[j].subjectName.substring(0,1)%></i></span>
                            <%}else{%>
                            <div class="right">
                                <%if((_subject.tmHwSc || 0) > 3){%><a href="javascript:void(0);" class="frequent_btn">频繁布置</a><%}%>
                            </div>
                            <%}%>
                            <ul class="clearfix">
                                <li>
                                    <%if(_subject.tmGroupMinHwSc != _subject.tmGroupMaxHwSc){%>
                                        <span><%if(_subject.tmGroupMinHwSc != null){%><%=_subject.tmGroupMinHwSc%><%}else{%>0<%}%></span>-
                                        <span><%if(_subject.tmGroupMaxHwSc != null){%><%=_subject.tmGroupMaxHwSc%><%}else{%>0<%}%></span></li>
                                    <%}else{%>
                                    <span><%if(_subject.tmGroupMaxHwSc != null){%><%=_subject.tmGroupMaxHwSc%><%}else{%>0<%}%></span></li>
                                <%}%>
                                <li>
                                    <span><%if(_subject.tmFinCsHwGte3AuStuCount != null){%><%=_subject.tmFinCsHwGte3AuStuCount%><%}else{%>0<%}%></span>
                                    (<%if(_subject.lmFinCsHwGte3AuStuCount != null){%><%=_subject.lmFinCsHwGte3AuStuCount%><%}else{%>0<%}%>)
                                </li>
                            </ul>
                        </div>
                        <p class="teacher_infor">
                            <span><%if(_subject.regStuCount != null){%><%=_subject.regStuCount%><%}else{%>0<%}%>注册</span><span><%if(_subject.auStuCount != null){%><%=_subject.auStuCount%><%}else{%>0<%}%>认证</span><span>带<%if(_subject.classCount != null){%><%=_subject.classCount%><%}else{%>0<%}%>个班</span><span><%=data[i].schoolName%></span>
                        </p>
                    <%}%>
                </div>
            <%}%>
        <%}else{%>
        <div class="year-content">
            <p class="teacher_infor">
                <span><%=data[i].schoolName%></span>
            </p>
        </div>
        <%}%>
    </div>
    <%}%>
</script>
<script>
    $(document).ready(function () {
        //隐藏顶部title
        try{
            var setTopBar = {
                show:false
            };
            setTopBarFn(setTopBar);
        }catch(e){

        }
    });
    var AT = new agentTool();

    //老师卡片可点击
    $(document).on("click",".teacher",function(){
        var sid = $(this).data('sid');
        var userId = ${requestContext.getCurrentUser().getUserId()!0};
        $.post('/mobile/resource/teacher/teacher_detail_authority_message.vpage',{
            userId:userId,
            teacherId:sid,
            scene:3
        },function (res) {
            if(res.success){
                AT.setCookie("currentSid",sid);
                openSecond("/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId="+sid);
            }else{
                AT.alert(res.info);
            }
        });
    });
    $(document).on("click",".js-showTeacher",function(event){
        event.preventDefault();
        event.stopPropagation();
        var teacherId = $(this).data('id');
        $.post("/mobile/resource/school/show_teacher.vpage",{teacherId:teacherId},function (res) {
            if(res.success){
                searchTeacher(teacherId);
            }else{
                AT.alert(res.info);
            }
        })
    });

    var searchTeacher = function(keyWord){
        $.post("search.vpage",{teacherKey:keyWord, scene:2},function(res){
            if(res.success){
                var data={data:res.teacherList};
                $("#teacher-list").html(template("T:老师列表",data));
            }else{AT.alert(res.info);}
        });
    };

    $(".teacher-search").on("click",function(){
        var $this=$(this),keyWord=$this.prev().val();
        searchTeacher(keyWord);
    });

</script>
</@layout.page>
