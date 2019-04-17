<#import "../../nuwa/teachershellv3.ftl" as temp />
<@temp.page showNav="hide">
    <@app.css href="public/skin/project/schoolambassador/activateteacher.css?1.0.1" />
    <div class="dropDownBox_tip" style="position: relative; ">
        <div class="tip_content" style="width: auto; padding: 6px 0; color: #333; ">
            <a class="w-btn w-btn-mini w-circular-5 w-border-blue v-detailLook" href="javascript:void(0);" style="float: right; margin-right: 10px;">查看详情</a>
            <div style="line-height: 180%; font-size: 16px;">【重要通知】校园大使奖励规则调整将于2015年9月1日起执行</div>
        </div>
        <script type="text/html" id="T:各位校园大使请注意">
            <h5 style="font-size: 16px; margin-bottom: 10px;">各位校园大使请注意：</h5>
            <div style="line-height: 28px; padding-bottom: 20px;">
                <#--<#if currentTeacherWebGrayFunction.isAvailable("CampusActive", "Reward")>-->
                    <#--<div>原校园活跃度奖励按照全校认证老师计算，现调整规则后按照同科认证老师计算。</div>-->
                    <#--<div>本校有≥3名活跃的同科认证老师，且校园活跃度（同科）在70%以上，本月所有同科活跃认证老师获得园丁豆奖励：</div>-->
                    <#--<div>● 校园活跃度（同科）≥90%，每名活跃认证老师奖励200园丁豆；</div>-->
                    <#--<div>● 校园活跃度（同科）≥80%，<90%，每名活跃老师奖励150园丁豆；</div>-->
                    <#--<div>● 校园活跃度（同科）≥70%，<80%，每名活跃老师奖励100园丁豆；</div>-->
                    <#--<div>● 校园活跃度（同科）<70%，无园丁豆奖励</div>-->
                <#--<#else>-->
                    <div>2015年10月起，原有的校园活跃度奖励将下线，本校老师将不再根据校园活跃度获得园丁豆（园丁豆）奖励；</div>
                    <div>同时新的校园大使奖励正在制作当中，将会在近期推出，请校园大使们关注网站动态；</div>
                    <div>校园大使的相关规则和结构正在进行重大调整，目的是帮助校园大使们更好地开展工作，对各位造成的不便还请谅解！</div>
                <#--</#if>-->
            </div>
        </script>
        <script type="text/javascript">
            $(function(){
                $(document).on("click", ".v-detailLook", function(){
                    $.prompt(template("T:各位校园大使请注意", {}), {
                        title: "奖励规则",
                        buttons: {},
                        position: {width: 600}
                    });
                });
            });
        </script>
    </div>
    <div class="w-base" style="margin-top: 15px;">
        <div class="w-base-title">
            <h3>本校老师活跃情况</h3>
        </div>
        <div class="w-base-container">
            <p style="padding: 15px 0 15px 30px;">
                <i class="icon-live icon-electric"></i>
                <a href="http://help.17zuoye.com/?p=779" target="_blank" class="w-blue">本月校园活跃度： <span>${campusActiveLevel!0}%</span></a>
                <span style="display: inline-block; margin: 0 20px;">上月校园活跃度：<span class="text_red">${campusActiveLevelLastMonth!0}%</span></span>
                <span style="display: inline-block; margin: 0 20px;">本月活跃认证老师：<span class="text_red">${activeCount!0}</span></span>
                <span style="display: inline-block; margin-left: 10px;">当前认证老师总数：<span class="text_red">${(allTeacherList?size)!0}</span></span>
            </p>
        </div>
    </div>
    <#if allTeacherList?? && allTeacherList?has_content>
    <div class="w-base">
        <div class="w-base-title">
            <h3>本校认证老师</h3>
        </div>
        <div class="w-base-container">
            <div class="t-show-box">
                <div class="w-table">
                    <table>
                        <thead>
                        <tr>
                            <td style="width: 150px;">姓名</td>
                            <td style="width: 100px;">学科</td>
                            <td style="width: 180px;">
                                本月布置作业次数    <span title="本数据每天更新一次" class="w-icon-public w-icon-faq"></span>
                            </td>
                            <td style="width: 180px;">完成作业学生人数</td>
                            <td style="width: 120px;">状态</td>
                            <td>操作</td>
                        </tr>
                        </thead>
                        <tbody>
                            <#list allTeacherList as t>
                            <tr <#if t_index%2 == 0>class="odd" </#if>  <#if t_index gt 6>style="display: none;"</#if>>
                                <td>${t.name!'0'}(${t.userId!0})</td>
                                <td>
                                    <#if t.subject == "ENGLISH">
                                        <span class="w-gray">英语老师</span>
                                    <#elseif t.subject == "CHINESE">
                                        <span class="w-blue">语文老师</span>
                                    <#else>
                                        数学老师
                                    </#if>
                                </td>
                                <td>${t.hwCountMonth!'0'}次</td>
                                <td>${t.finishCount!'0'}人</td>
                                <td>${t.status!''}</td>
                                <td>
                                    <a href="javascript:void(0);" class="w-blue data-cancelAuth" data-type="2" style="font-size: 12px;" data-userid="${t.userId!0}" data-username="${t.name!0}">申请取消该老师认证 </a>
                                </td>
                            </tr>
                            </#list>
                        </tbody>
                    </table>
                    <#if allTeacherList?size gt 6>
                        <div class="t-show-box data-teacherCertification">
                            <div class="w-turn-page-list">
                                <a href="javascript:void (0)">查看更多<span class="w-icon-arrow"></span></a>
                            </div>
                        </div>
                    </#if>
                </div>
            </div>
        </div>
    </div>
    </#if>
    <div class="w-base">
        <div class="w-base-title">
            <h3>推荐认证</h3>
        </div>
        <div class="w-base-container">
            <dl class="t-awake-info">
                <dt>
                <div class="w-base-ext">
                    <span>姓名/ID：</span>
                    <input type="text" placeholder="请输入姓名或ID" class="w-int" id="token"/>
                    <a href="javascript:void(0)" class="w-btn w-btn-mini" id="tokenSearch">搜索</a>
                </div>
                </dt>
                <dd>
                    <p>推荐认证说明：</p>
                    <p>1、推荐老师将享有快速认证的权利。</p>
                    <p>2、您推荐老师必须为本校认识的老师，推荐及担保老师的真实性，如推荐老师为非真实老师或存在恶意刷园丁豆行为，  第一次扣除所有园丁豆，第二次取消校园大使资格并扣除所有园丁豆。</p>
                </dd>
            </dl>
            <div class="w-table w-table-border"  style="display: none; margin: 10px;" id="tokenTableBox">
                <table>
                    <thead>
                    <tr>
                        <th>姓名</th>
                        <th>ID</th>
                        <th>注册时间</th>
                        <th style="width: 150px">认证状态</th
                    </tr>
                    </thead>
                    <tbody id="tokenTbody"></tbody>
                </table>
            </div>
            <div class="w-clear"></div>
        </div>
    </div>

    <script type="text/javascript">
        $(function(){
            $(document).on("click", ".data-teacherCertification", function(){
                var $this = $(this);
                $this.hide();
                $this.siblings("table").find("tr").show();
            });

            //查询推荐认证
            $("#tokenSearch").on('click', function(){
                $.post('/teacher/invite/findteacher.vpage', {token: $("#token").val()}, function(data){
                    if(data.success){
                        var $data = data.teacherList;
                        var dataHtml = "";

                        $("#tokenTableBox").show();
                        for(var i in $data){
                            var authenticationState = ($data[i].authenticationState == 'SUCCESS') ? "<span class='text_well'>已认证</span>" : "<a href='javascript:void(0);' class='btn_mark btn_mark_small btn_mark_primary' data-id='" + $data[i].teacherId + "'>推荐认证</a>";

                            dataHtml += "<tr>" +
                            "<th>"+ $data[i].teacherName +"</th>" +
                            "<th>"+ $data[i].teacherId +"</th>" +
                            "<th>"+ $data[i].createDatetime +"</th>" +
                            "<th>" + authenticationState + "</th>" +
                            "</tr>";
                        }
                        $("#tokenTbody").html(dataHtml);
                    }else{
                        $17.alert(data.info);
                    }
                });
            });

            //推荐认证
            $(document).on('click', '#tokenTbody .btn_mark_primary', function(){
                $.post('/teacher/invite/recommendteacher.vpage', {recommendedTeacherId: $(this).data("id")}, function(data){
                    if(data.success){
                        $17.alert("推荐成功");
                    }else{
                        $17.alert(data.info);
                    }
                });
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