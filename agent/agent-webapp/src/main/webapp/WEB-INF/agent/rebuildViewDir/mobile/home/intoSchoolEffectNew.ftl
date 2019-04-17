<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="进校效果" pageJs="" footerIndex=1>
<@sugar.capsule css=['intoSchoEffeNew']/>
<style>
    .res-top {position: relative;padding: .375rem 0 0 3.75rem;line-height: 2.25rem;background-color: #fff;}
    .return {position: absolute;left: 0;top: .5rem;padding: 0 .25rem 0 .625rem;cursor: pointer;font-size: .9rem;}
    .return .return-icon {margin-right: .25rem;display: inline-block;width: .4rem;height: .7rem;background: url(/public/rebuildRes/image/mobile/researchers/arrow_left_01.png) no-repeat;background-size: 100% 100%;}
    .res-top .return-line {position: absolute;top: 50%;margin: -.375rem 0 0 0;content: "";height: 1.25rem;border-right: 1px solid #979797;}
    .res-top .res-title {padding: 0 0 0 .625rem;}
    .res-top span {display: inline-block;vertical-align: middle;font-size: .9rem;color: #898c91;}
    img{display:inherit}
    .schoolEffect-box{background-color:#f1f2f5}
    .schoolRecord-pop .inner .sre-list li{margin:.5rem 0;height:1.75rem;line-height:1.75rem;border:.05rem solid #636880;border-radius:.2rem;cursor:pointer}
    .schoolRecord-pop .inner .sre-btn a{width:50%;display:inline-block;text-align:center;font-size:.9rem}
    .srd-top{margin-top:.5rem;height:2.125rem;text-align:center;font-size:0;line-height:2.125rem;background:#fff6ee;border-top:.025rem solid #dde2ea;border-bottom:.025rem solid #dde2ea}
    .srd-top span{padding:0 1.625rem;display:inline-block;font-size:.75rem;color:#77595a;cursor:pointer}
    .srd-top span.active{color:#ff7d5a}
    .srd-time{padding:.75rem 0 .375rem 1rem;font-size:.65rem;color:#9199bb;background-color:#fff}
    .srd-module{margin:.5rem 0;background-color:#fff}
    .srd-module .mHead{margin-left:1rem;padding:.5rem 1rem .5rem 0;font-size:.65rem;color:#9199bb;border-bottom:.025rem dashed #dde2ea}
    .srd-module .mHead .mRight{float:right;color:#636880;line-height:1.125rem}
    .srd-module .mHead .ml-icon{display:inline-block;vertical-align:middle;width:.65rem;height:.625rem;background:url(/public/rebuildRes/image/mobile/home/schoolrecord-icon01.png) no-repeat;background-size:100% 100%}
    .srd-module .mHead .mr-icon{display:inline-block;vertical-align:middle;width:.875rem;height:.875rem;text-align:center;font-size:.55rem;color:#fff;line-height:.875rem;background:#fbbc74;border-radius:2.5rem}
    .srd-module .mInfo{padding:.75rem;font-size:.65rem;color:#636880}
    .srd-module .mTable table{width:100%;text-align:center;font-size:.65rem;color:#636880}
    .srd-module .mTable table thead tr{border-bottom:.025rem solid #e6e9f0;background-color:#f9f9fa}
    .srd-module .mTable table tr:nth-child(even){background-color:#f9f9fa}
    .srd-module .mTable table tr td{padding:.75rem 0;vertical-align:middle;width:20%}
    .srd-module .mTable table tfoot td{padding-left:.75rem;text-align:left}
    .fontDefault{color:#636880}
    .srd-foot{margin:2.125rem 0}
    .nonInfo{padding:2rem 0;text-align: center;font-size:.75rem;color:#636880;}
    .sce-foot{height:2rem;line-height:2rem;text-align:center;font-size:.65rem;color:#9199bb}
    .footIco{margin-right:.25rem;display:inline-block;vertical-align:middle;width:.6rem;height:.65rem;background:url(/public/rebuildRes/image/mobile/intoSchool/icon_title-visit.png) no-repeat;background-size:100% 100%}
    .srd-foot .view_btn{margin:0 4.25rem;padding:.5rem 0;display:block;text-align:center;font-size:.75rem;color:#9199bb;background:#fff;border:.05rem solid #9199bb;border-radius:.2rem}
</style>

    <div class="schoolEffect-box">
        <div class="res-top fixed-head">
            <div class="return"><a href="/mobile/performance/index.vpage"><i class="return-icon"></i>返回</a></div>
            <span class="return-line"></span>
            <span class="res-title">进校效果</span>
            <#if user?has_content><a href="choose_agent.vpage?breakUrl=visit_school_result&selectedUser=${user.id!0}&needCityManage=1" class="icoPersonal js-changeBtn"></a></#if>
            <#if user?has_content><span class="nameInfo">${user.realName!""}</span></#if>
        </div>
        <div class="srd-top">
            <span class="now_into active now_native" data-time="${currentTime!''}">本月进校</span>
            <span class="last_into now_native" data-time="${preMonthTime!''}">上月进校</span>
        </div>
        <div class="sce-main" style="background:#f1f2f5"></div>

        <script type="text/html" id="schoolEffect">
            <div class="schoolRecord-box">
                <%if(visitResultMap.length >0){%>
                    <%for(var i = 0;i < visitResultMap.length;i++){%>
                        <%var result = visitResultMap[i]%>
                        <div class="srd-time"><%=result.day%></div>
                        <%for(var j = 0;j < result.visitDetailList.length;j++){%>
                            <%var detail = result.visitDetailList[j]%>
                            <div class="srd-module">
                                <div class="mHead">
                                    <div class="mRight">规模：<%=detail.schoolSize%></div>
                                    <i class="ml-icon icon-1"></i>
                                    <a href="/mobile/resource/school/card.vpage?schoolId=<%=detail.schoolId%>"><%=detail.schoolName%></a>

                                    <i class="mr-icon"><%=detail.visitSchoolMonthCount%></i>
                                </div>
                                <div class="mInfo">本月拜访日期：<%for(var k = 0;k < detail.visitDayList.length;k++){%>
                                    <%var dayList = detail.visitDayList[k], str =dayList.toString().substring(4,8)%>
                                    <%=str.substr(0,2)+'-'+str.substr(2,4)%>
                                    <%}%>
                                </div>
                                <div class="mTable">
                                    <table>
                                        <thead>
                                        <tr>
                                            <td>数据</td>
                                            <td>注册</td>
                                            <td>认证</td>
                                            <td>单活</td>
                                            <td>双活</td>
                                        </tr>
                                        </thead>
                                        <tbody>
                                            <tr>
                                                <td>拜访前</td>
                                                <td><%if(detail.preStuRegNum != null){%><%=detail.preStuRegNum%><%}else{%>--<%}%></td>
                                                <td><%if(detail.preStuAuthNum != null){%><%=detail.preStuAuthNum%><%}else{%>--<%}%></td>
                                                <td><%if(detail.preSascData != null){%><%=detail.preSascData%><%}else{%>--<%}%></td>
                                                <td><%if(detail.preDascData != null){%><%=detail.preDascData%><%}else{%>--<%}%></td>
                                            </tr>
                                            <tr>
                                                <td>增长／预测</td>
                                                <td><span class="fontDefault" style="color:<%if(detail.addStuRegNum != null && detail.addStuRegNum >= 0){%>#ff7d5a<%}else{%>#70BBA0<%}%>"><%if(detail.addStuRegNum != null){%><%=detail.addStuRegNum%><%}else{%>--<%}%></span>/<span class="fontDefault"><%if(detail.forecastStuRegNum != null){%><%=detail.forecastStuRegNum%><%}else{%>--<%}%></span></td>
                                                <td><span class="fontDefault" style="color:<%if(detail.addStuAuthNum != null &&detail.addStuAuthNum >= 0){%>#ff7d5a<%}else{%>#70BBA0<%}%>"><%if(detail.addStuAuthNum != null){%><%=detail.addStuAuthNum%><%}else{%>--<%}%></span>/<span class="fontDefault"><%if(detail.forecastStuAuthNum != null){%><%=detail.forecastStuAuthNum%><%}else{%>--<%}%></span></td>
                                                <td><span class="fontDefault" style="color:<%if(detail.addSascData != null &&detail.addSascData >= 0){%>#ff7d5a<%}else{%>#70BBA0<%}%>"><%if(detail.addSascData != null){%><%=detail.addSascData%><%}else{%>--<%}%></span>/<span class="fontDefault"><%if(detail.forecastSascData != null){%><%=detail.forecastSascData%><%}else{%>--<%}%></span></td>
                                                <td><span class="fontDefault" style="color:<%if(detail.addDascData != null &&detail.addDascData >= 0){%>#ff7d5a<%}else{%>#70BBA0<%}%>"><%if(detail.addDascData != null){%><%=detail.addDascData%><%}else{%>--<%}%></span>/<span class="fontDefault"><%if(detail.forecastDascData != null){%><%=detail.forecastDascData%><%}else{%>--<%}%></span></td>
                                            </tr>
                                        </tbody>
                                        <tfoot>
                                            <tr>
                                                <td colspan="5">拜访老师：<%for(var d = 0;d < detail.teacherInfoList.length;d++){%>
                                                    <%var teacherList = detail.teacherInfoList[d]%><%if(!teacherList.usedFlg){%><span class="fontDefault"><a style="color:#ff7d5a" href="/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId=<%=teacherList.teacherId%>"><%=teacherList.teacherName%></a></span><%}else{%><a href="/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId=<%=teacherList.teacherId%>"><%=teacherList.teacherName%></a>
                                                    <%}}%></td>
                                            </tr>
                                        </tfoot>
                                    </table>
                                </div>
                            </div>
                        <%}%>
                    <%}%>

                <%}else{%>
                    <div class="nonInfo" style="background:#fff" >暂无进校信息</div>
                <%}%>
            </div>
        </script>
        <#--<div class="sce_click">dianjijiazai</div>-->
        <div class="srd-foot">
            <a href="javascript:;" class="view_btn">查看更多</a>
        </div>
        <div class="sce-foot"><i class="footIco"></i>仅供查看本月及上月拜访学校</div>
    </div>
<script type="text/javascript">
    var userId = <#if user?has_content>${user.id!0}<#else>0</#if>;
    var AT = new agentTool(),
            cDate = "",
            nDate = "";

    var dateObject = new Date();
    $(document).on('click','.now_native',function(){
        $(this).addClass("active").siblings().removeClass('active');

        //上月
        if($(this).hasClass("last_into")){
            $('.srd-foot').removeClass('fff').addClass('srd-foot2');
            nDate = $(this).data("time");
            var lasetEndTime = nDate;
            //请求数据
            $.get('visit_school_result_data.vpage?endTime='+lasetEndTime+'&userId='+userId,function(res){
                if(res.success){
                    if(res.visitResultMap && res.visitResultMap.length != 0 ) {
                        var main_html = template('schoolEffect', res);
                        $('.sce-main').html(main_html);
                        nDate = res.nextDate;
                        $(".view_btn").show();
                    }else{
                        $(".view_btn").hide();
                        //若无数据，则显示 暂无进校信息
                        var main_html = template('schoolEffect', res);
                        $('.sce-main').html(main_html);
                    }
                }else{
                    AT.alert(res.info);
                }
            });

        //本月
        }else{
            $('.srd-foot').addClass("fff").removeClass('srd-foot2');
            initCurrentDate(cDate);
        }
    });

    //上月加载更多
    $(document).on('click','.srd-foot2',function(){
        $.ajax({
            method : 'get' ,
            url : 'visit_school_result_data.vpage?endTime=' + nDate +'&userId='+userId,
            success:function(data){
                if(data.success && data!=''){
                    if(data.visitResultMap && data.visitResultMap.length != 0 ){
                        var main_html = template('schoolEffect',data);
                        $('.sce-main').append(main_html);
                        nDate = data.nextDate;
                        $(".view_btn").show();
                    }else{
                        $(".view_btn").hide();
                    }
                }else{
                    alert(data.info)
                }
            },
            error:function(data){

            }
        })
    });

    //初始化当前月份数据
    var initCurrentDate = function(next){
        $('.srd-foot').addClass("fff").removeClass('srd-foot2');
        $.ajax({
            method : 'get' ,
            url : 'visit_school_result_data.vpage?endTime=' + next+'&userId='+userId ,
            success:function(data){
                if(data.success){
                    $(".view_btn").show();
                    if(data.visitResultMap && data.visitResultMap.length != 0) {
                        var main_html = template('schoolEffect', data);
                        $('.sce-main').html(main_html);
                        cDate = data.nextDate;
                    }else{
                        $(".view_btn").hide();
                        var main_html = template('schoolEffect', data);
                        $('.sce-main').html(main_html);
                    }
                }else{
                    AT.alert(data.info);

                }
            }
        })
    };

    cDate = $(".now_into").data("time");
    initCurrentDate(cDate);
    //本月加载更多
    $(document).on('click','.fff',function(){
        $.ajax({
            method : 'get' ,
            url : 'visit_school_result_data.vpage?endTime=' + cDate +'&userId='+userId ,
            success:function(data){
                if(data.success){
                    if(data.visitResultMap && data.visitResultMap.length != 0){
                        var main_html = template('schoolEffect',data);
                        $('.sce-main').html(main_html);
                        cDate = data.nextDate;
                        $(".view_btn").show();
                    }else{
                        $(".view_btn").hide();
                    }
                }
            }
        })
    });
</script>
</@layout.page>
