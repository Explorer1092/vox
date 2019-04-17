<#--奖励卡-->
<#if (data.showRewardsCard)!false>
    <li class="practice-block">
        <div class="practice-content">
            <h4>
                <span class="w-discipline-tag w-discipline-tag-7">奖励卡</span>
            </h4>
            <div class="pc-article" style="margin-top: 35px;">
                <p><span class="w-icon w-icon-4"></span> 获得<strong class="w-orange w-ft-large">${data.beanCount!''}</strong>学豆</p>
                <p><span class="w-icon w-icon-5"></span> 收到<strong class="w-orange w-ft-large">${data.commentCount!''}</strong>条评语</p>
            </div>
            <div class="pc-btn">
                <a id="view_reward_card_but" href="javascript:void(0);" class="w-btn w-btn-green">点击查看</a>
            </div>
        </div>
    </li>
    <script type="text/html" id="t:rewardCardDetail">
        <div class="t-homework-task">
            <h1>作业奖励</h1>
            <div class="hw-list w-fl-left">
                <h2 class="title">学豆奖励</h2>
                <ul>
                    <%if(detailData.histories && detailData.histories.length > 0){%>
                        <%for(var i = 0; i < detailData.histories.length; i++){%>
                            <li>
                                <%if(detailData.histories[i].homeworkType == 'ENGLISH'){%>
                                    <span class="w-tab w-tab-1 tab">英语</span>
                                <%}else if(detailData.histories[i].homeworkType == 'MATH'){%>
                                    <span class="w-tab w-tab-2 tab">数学</span>
                                <%}else if(detailData.histories[i].homeworkType == 'CHINESE'){%>
                                    <span class="w-tab w-tab-3 tab">语文</span>
                                <%}else if(detailData.histories[i].homeworkType == 'VACATION_ENGLISH' || detailData.histories[i].homeworkType == 'VACATION_MATH'){%>
                                    <span class="w-tab w-tab-3 tab">假期</span>
                                <%}%>
                                <p>
                                    <%if(detailData.histories[i].homeworkType == 'ENGLISH' || detailData.histories[i].homeworkType == 'MATH'){%>
                                        <!--园丁豆类型75表示智慧教室-->
                                        <%if(detailData.histories[i].integralType == 75){%>
                                            课堂
                                        <%}else{%>
                                    练习
                                        <%}%>
                                    <%}else if(detailData.histories[i].homeworkType == 'VACATION_ENGLISH' || detailData.histories[i].homeworkType == 'VACATION_MATH'){%>
                                        假期
                                    <%}%>

                                    奖励<%=detailData.histories[i].integral%>学豆
                                </p>

                                <span class="time"><%=detailData.histories[i].dateYmdString%></span>
                            </li>
                        <%}%>
                    <%}else{%>
                        暂无学豆奖励
                    <%}%>
                </ul>
            </div>
            <div class="hw-list w-fl-left">
                <h2 style="background-color: #91e461;" class="title">老师评语</h2>
                <ul>
                    <%if(detailData.comments.length > 0){%>
                        <%for(var i = 0; i < detailData.comments.length; i++){%>
                            <li>
                                <%if(detailData.comments[i].homeworkType == 'ENGLISH'){%>
                                    <span class="w-tab w-tab-1 tab">英语</span>
                                <%}else if(detailData.comments[i].homeworkType == 'MATH'){%>
                                    <span class="w-tab w-tab-2 tab">数学</span>
                                <%}else if(detailData.comments[i].homeworkType == 'CHINESE'){%>
                                    <span class="w-tab w-tab-3 tab">语文</span>
                                <%}else if(detailData.comments[i].homeworkType == 'VACATION_ENGLISH' || detailData.comments[i].homeworkType == 'VACATION_MATH'){%>
                                    <span class="w-tab w-tab-3 tab">假期</span>
                                <%}%>
                                <p><%=detailData.comments[i].comment%></p>
                                <span class="time"> <%=detailData.comments[i].commentTime%></span>
                            </li>
                        <%}%>
                    <%}else{%>
                        暂无评语
                    <%}%>
                </ul>
            </div>
        </div>
        <div class="w-clear"></div>
        <#if (data.taskMapper.parentWechatBinded)?? && !data.taskMapper.parentWechatBinded>
            <div class="t-homework-task-hwBot" style="height: 85px; margin-bottom: 20px; clear: both;">
                <dl>
                    <dt>加载中..</dt>
                    <dd>
                        <h4>微信扫一扫</h4>
                        <p>
                            下载家长通，全免费接收老师通知、练习信息<br/>
                            做错考点和学习报告，同时也将为你提供丰富的免费学习资料。
                        </p>
                    </dd>
                </dl>
            </div>
        </#if>
    </script>

    <script type="text/javascript">
        $(function(){
            $("#view_reward_card_but").on('click', function(){
                var $this = $(this);
                $.get('/student/checkrewardcard.vpage?commentCount=${data.commentCount!''}', function(data){
                    if(data.success){
                        $.prompt(template("t:rewardCardDetail", {detailData : data}),{
                            title : '',
                            position : {width : 740},
                            buttons : {"知道了" : true},
                            submit : function(e,v){
                                e.preventDefault();
                                if(v){
                                    location.reload();
                                }
                            },
                            close : function(){
                                location.reload();
                            },
                            loaded : function(){
                                var qrCodeUrl = "<@app.link href="public/skin/studentv3/images/2dbarcode.jpg"/>";
                                $17.get_jzt_qr("100307", function(url){
                                    debugger;
                                    $(".t-homework-task-hwBot dt").html("<img src='" + (url || qrCodeUrl) + "' width='90' height='90'/>");
                                });
                            }
                        });
                    }else{
                        $17.alert('练习奖励卡查看失败');
                    }
                });
                $17.tongji('首页-奖励卡片-查看');
            });
        });
    </script>
</#if>
