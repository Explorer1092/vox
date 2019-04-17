<#import "../../nuwa/teachershellv3.ftl" as temp />
<@temp.page showNav="hide">
    <@app.css href="public/skin/project/schoolambassador/activateteacher.css" />
    <div class="dropDownBox_tip" style="position: relative; ">
        <div class="tip_content" style="width: auto; padding: 6px 0; color: #333; ">
            <div style="line-height: 180%; font-size: 16px;">【重要通知】校园大使奖励规则调整将于2015年9月1日起执行</div>
        </div>
    </div>
    <div class="wrapper campusAm-1">
        <div class="campusHd">
            <em class="ico-1"></em>
            <em class="ico-2"></em>
            <div class="inner" style="font-size: 25px;">校园大使第一职责：全面推广一起作业，点亮本校老师功能图标！</div>
        </div>
        <div class="campusMain clearfix">
            <div class="mod-fl">
                <em class="ico-1"></em>
                <em class="ico-2"></em>
                <p>我们把一起作业的功能做成图标，校园大使教本校老师使用功能即可点亮图标，获得丰厚园丁豆奖励！</p>
            </div>
            <div class="mod-fr clearfix">
                <div class="mod first">
                    <div class="clearfix">
                        <div class="top-pic">
                            <div class="pic pic-gray"><em></em></div>
                            <div class="txt">从未点亮</div>
                        </div>
                        <div class="top-ico"></div>
                        <div class="top-pic">
                            <div class="pic"><em></em></div>
                            <div class="txt">首次点亮</div>
                        </div>
                    </div>
                    <div class="head">首次点亮，每个+10园丁豆！</div>
                    <p class="text">本校老师首次点亮图标，校园大使+10园丁豆！</p>
                </div>
                <div class="mod">
                    <div class="clearfix">
                        <div class="top-pic">
                            <div class="pic pic-gray"></div>
                            <div class="txt">本月未点亮</div>
                        </div>
                        <div class="top-ico"></div>
                        <div class="top-pic">
                            <div class="pic"></div>
                            <div class="txt">本月已点亮</div>
                        </div>
                    </div>
                    <div class="head">每月点亮，每月+100园丁豆！</div>
                    <p class="text">每个老师当月点亮超过5个图标（>=5个）校园大使+50园丁豆！点亮全部图标再+50园丁豆！</p>
                </div>
            </div>
        </div>
    </div>

    <#if allTeacherList?? && allTeacherList?has_content>
    <div class="wrapper campusAm-2">
        <div class="campusHd clearfix">
            <div class="title">本校认证老师</div>
            <div class="text clearfix">
                <span><a href="http://help.17zuoye.com/?p=779" target="_blank" class="w-blue">本月校园活跃度(同科)： ${campusActiveLevel!0}%</a></span>
                <span style="display: inline-block; margin: 0 10px;">上月校园活跃度(同科)：${campusActiveLevelLastMonth!0}%</span>
                <span style="display: inline-block; margin: 0 10px;">本月活跃认证老师(同科)：${activeCount!0}</span>
                <span style="display: inline-block; margin-left: 10px;">认证老师总数(同科)：${(allTeacherList?size)!0}</span>
            </div>
        </div>
        <#--//list start-->
        <#list allTeacherList as t>
            <div class="campusModule" <#if t_index gt 4>style="display: none;"</#if>>
                <div class="hd-pro">
                    <img src="<@app.avatar href='${t.img!}'/>" width="138" height="138">
                </div>
                <table>
                    <thead>
                    <tr>
                        <td>姓名</td>
                        <td>本月作业次数</td>
                        <td>完成作业学生数</td>
                        <td>状态</td>
                        <td>操作</td>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>${t.name!'0'}(${t.userId!0})</td>
                        <td>${t.hwCountMonth!'0'}</td>
                        <td>${t.finishCount!'0'}</td>
                        <td>${t.status!''}</td>
                        <td>
                            <a href="javascript:void(0);" class="w-blue data-cancelAuth" data-type="2" style="font-size: 12px;" data-userid="${t.userId!0}" data-username="${t.name!0}">申请取消该老师认证 </a>
                        </td>
                    </tr>
                    </tbody>
                </table>
                <div class="list clearfix">
                    <div title="布置一次作业，如果微信布置了作业，则作业和微信作业同时点亮" class="mod mod-01 <#if (t.AMBASSADOR_MENTOR_HOMEWORK)??><#if t.AMBASSADOR_MENTOR_HOMEWORK == "F_Y" || t.AMBASSADOR_MENTOR_HOMEWORK == "Y"><#else>mod-01-gray</#if><#else>mod-01-gray</#if>">
                        <div class="pic">
                            <#if (t.AMBASSADOR_MENTOR_HOMEWORK)??><#if t.AMBASSADOR_MENTOR_HOMEWORK == "F_Y"><em></em></#if><#else><em></em></#if>
                        </div>
                        <div class="hd">作业</div>
                    </div>
                    <div title="布置一次测验" class="mod mod-02 <#if (t.AMBASSADOR_MENTOR_QUIZ)??><#if t.AMBASSADOR_MENTOR_QUIZ == "F_Y" || t.AMBASSADOR_MENTOR_QUIZ == "Y"><#else>mod-02-gray</#if><#else>mod-02-gray</#if>">
                        <div class="pic">
                            <#if (t.AMBASSADOR_MENTOR_QUIZ)??><#if t.AMBASSADOR_MENTOR_QUIZ == "F_Y"><em></em></#if><#else><em></em></#if>
                        </div>
                        <div class="hd">测验</div>
                    </div>
                    <div title="使用微信布置一次作业" class="mod mod-03 <#if (t.AMBASSADOR_MENTOR_WECHAT_HOMEWORK)??><#if t.AMBASSADOR_MENTOR_WECHAT_HOMEWORK == "F_Y" || t.AMBASSADOR_MENTOR_WECHAT_HOMEWORK == "Y"><#else>mod-03-gray</#if><#else>mod-03-gray</#if>">
                        <div class="pic">
                            <#if (t.AMBASSADOR_MENTOR_WECHAT_HOMEWORK)??><#if t.AMBASSADOR_MENTOR_WECHAT_HOMEWORK == "F_Y"><em></em></#if><#else><em></em></#if>
                        </div>
                        <div class="hd">微信</div>
                    </div>
                    <div title="用智慧课堂奖励一名学生" class="mod mod-04 <#if (t.AMBASSADOR_MENTOR_SMART_CLAZZ)??><#if t.AMBASSADOR_MENTOR_SMART_CLAZZ == "F_Y" || t.AMBASSADOR_MENTOR_SMART_CLAZZ == "Y"><#else>mod-04-gray</#if><#else>mod-04-gray</#if>">
                        <div class="pic">
                            <#if (t.AMBASSADOR_MENTOR_SMART_CLAZZ)??><#if t.AMBASSADOR_MENTOR_SMART_CLAZZ == "F_Y"><em></em></#if><#else><em></em></#if>
                        </div>
                        <div class="hd">智慧课堂</div>
                    </div>
                    <div title="使用布置作业抽大奖抽一次奖" class="mod mod-05 <#if (t.AMBASSADOR_MENTOR_DO_LOTTERY)??><#if t.AMBASSADOR_MENTOR_DO_LOTTERY == "F_Y" || t.AMBASSADOR_MENTOR_DO_LOTTERY == "Y"><#else>mod-05-gray</#if><#else>mod-05-gray</#if>">
                        <div class="pic">
                            <#if (t.AMBASSADOR_MENTOR_DO_LOTTERY)??><#if t.AMBASSADOR_MENTOR_DO_LOTTERY == "F_Y"><em></em></#if><#else><em></em></#if>
                        </div>
                        <div class="hd">抽奖</div>
                    </div>
                    <div title="在教学用品中心兑换一次奖品" class="mod mod-06 <#if (t.AMBASSADOR_MENTOR_REWARD_ORDER)??><#if t.AMBASSADOR_MENTOR_REWARD_ORDER == "F_Y" || t.AMBASSADOR_MENTOR_REWARD_ORDER == "Y"><#else>mod-06-gray</#if><#else>mod-06-gray</#if>">
                        <div class="pic">
                            <#if (t.AMBASSADOR_MENTOR_REWARD_ORDER)??><#if t.AMBASSADOR_MENTOR_REWARD_ORDER == "F_Y"><em></em></#if><#else><em></em></#if>
                        </div>
                        <div class="hd">兑换奖品</div>
                    </div>
                    <div title="在教师论坛发表一个新帖or回复其他老师的帖子" class="mod mod-07 <#if (t.AMBASSADOR_MENTOR_BBS)??><#if t.AMBASSADOR_MENTOR_BBS == "F_Y" || t.AMBASSADOR_MENTOR_BBS == "Y"><#else>mod-07-gray</#if><#else>mod-07-gray</#if>">
                        <div class="pic">
                            <#if (t.AMBASSADOR_MENTOR_BBS)??><#if t.AMBASSADOR_MENTOR_BBS == "F_Y"><em></em></#if><#else><em></em></#if>
                        </div>
                        <div class="hd">论坛</div>
                    </div>
                    <div title="检查作业时，为学生写一次评语" class="mod mod-08 <#if (t.AMBASSADOR_MENTOR_COMMENT)??><#if t.AMBASSADOR_MENTOR_COMMENT == "F_Y" || t.AMBASSADOR_MENTOR_COMMENT == "Y"><#else>mod-08-gray</#if><#else>mod-08-gray</#if>">
                        <div class="pic">
                            <#if (t.AMBASSADOR_MENTOR_COMMENT)??><#if t.AMBASSADOR_MENTOR_COMMENT == "F_Y"><em></em></#if><#else><em></em></#if>
                        </div>
                        <div class="hd">评语</div>
                    </div>
                    <#if t.subject == "ENGLISH">
                        <div title="布置一次带有阅读绘本的作业" class="mod mod-10 <#if (t.AMBASSADOR_MENTOR_READING)??><#if t.AMBASSADOR_MENTOR_READING == "F_Y" || t.AMBASSADOR_MENTOR_READING == "Y"><#else>mod-10-gray</#if><#else>mod-10-gray</#if>">
                            <div class="pic">
                                <#if (t.AMBASSADOR_MENTOR_READING)??><#if t.AMBASSADOR_MENTOR_READING == "F_Y"><em></em></#if><#else><em></em></#if>
                            </div>
                            <div class="hd">绘本</div>
                        </div>
                    <#elseif t.subject == "MATH">
                        <div title="检查作业时，奖励学生星星" class="mod mod-09 <#if (t.AMBASSADOR_MENTOR_REWARD_STAR)??><#if t.AMBASSADOR_MENTOR_REWARD_STAR == "F_Y" || t.AMBASSADOR_MENTOR_REWARD_STAR == "Y"><#else>mod-09-gray</#if><#else>mod-09-gray</#if>">
                            <div class="pic">
                                <#if (t.AMBASSADOR_MENTOR_REWARD_STAR)??><#if t.AMBASSADOR_MENTOR_REWARD_STAR == "F_Y"><em></em></#if><#else><em></em></#if>
                            </div>
                            <div class="hd">奖励星星</div>
                        </div>
                    </#if>

                </div>
            </div>
        </#list>
        <#--end//-->
        <#--<div class="campusFt"><div class="prev"></div><span><em>1</em>/10</span><div class="next"></div></div>-->
        <#if allTeacherList?size gt 4>
            <div class="t-show-box data-teacherCertification">
                <div class="w-turn-page-list">
                    <a href="javascript:void (0)">查看更多<span class="w-icon-arrow"></span></a>
                </div>
            </div>
        </#if>
    </div>
    </#if>

    <script type="text/javascript">
        $(function(){
            $(document).on("click", ".data-teacherCertification", function(){
                var $this = $(this);
                $this.hide();
                $this.siblings(".campusModule").show();
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
        });
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