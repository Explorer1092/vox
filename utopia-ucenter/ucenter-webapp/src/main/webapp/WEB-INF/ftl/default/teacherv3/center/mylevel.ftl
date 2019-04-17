<div class="taskCenter-popup" style="display: none;">
    <div class="popMain">
        <div class="upgradeIcon"></div>
        <div class="upgradeTxt">恭喜你升级为<span></span></div>
        <div class="smallTxt">
            <p>有效期为半年，做任务升等级优惠享不停</p>
            <p>请下载一起小学老师APP进入【我的-等级福利中心】查看</p>
        </div>
        <div class="knowBtn"></div>
    </div>
</div>
<div id="myLevel">
    <div class="w-base">
        <div class="w-base-title">
            <h3>
            <span class="w-icon-md">
                我的当前等级：<span class="w-blue">${teacherLevel!'---'}级</span>
            </span>
            <span class="w-lv-grade-box" style="vertical-align: baseline;">
                    <#assign tempMyLevel = (teacherLevel)!0/>
                    <#if (tempMyLevel/64)?int gte 1>
                        <#list 1..((tempMyLevel/64)?int) as i>
                            <i class="w-lv-grade w-lv-grade-4"></i>
                        </#list>
                    </#if>
                    <#if (tempMyLevel%64/16)?int gte 1>
                        <#list 1..((tempMyLevel%64/16)?int) as i>
                            <i class="w-lv-grade w-lv-grade-3"></i>
                        </#list>
                    </#if>
                    <#if ((tempMyLevel)%16/4)?int gte 1>
                        <#list 1..((tempMyLevel%16/4)?int) as i>
                            <i class="w-lv-grade w-lv-grade-2"></i>
                        </#list>
                    </#if>
                    <#if ((tempMyLevel)%16%4/1)?int gte 1>
                        <#list 1..((tempMyLevel%16%4/1)?int) as i>
                            <i class="w-lv-grade w-lv-grade-1"></i>
                        </#list>
                    </#if>
            </span>
            </h3>
            <div class="w-base-right" style="font-size: 12px; padding-top: 8px; padding-bottom: 2px;">
                <span class="w-icon-md">
                    活跃值： <span class="w-orange">${(teacherLevelValue)!'---'}</span>
                    <span style="margin-left: 15px; display: inline-block;">距离升级还有： <span class="w-orange">${upValue!'---'}</span> 活跃值</span>
                </span>
                <a href="http://help.17zuoye.com/?p=258" target="_blank" class="w-btn w-btn-mini w-btn-mBlue" style="float: none; width: 65px; padding: 3px 0;"><span class="w-icon-public w-icon-faq"></span><span class="w-icon-md">规则</span></a>
            </div>
        </div>
        <div class="t-lv-baseInfo">
            <#if (teacherLevelName.nextLevelPrivilege)?has_content>
                <#if (teacherLevelName.level gt 0 || currentTeacherDetail.subject == "ENGLISH")!false>
                    <div class="lv-info-next">
                        <h4>
                            <a target="_blank" href="http://help.17zuoye.com/?p=262">
                                下一个特权：<strong style="font-size: 16px;">${(teacherLevelName.nextLevelPrivilege)!'----'}</strong></h4>
                        </a>
                    </div>
                </#if>
            </#if>
            <dl>
                <dt>活跃值获得方法：</dt>
                <dd>
                <#--<div class="lv-bar">
                    <div class="lv-bar-inner"></div>
                </div>-->
                    <div class="lv-task">
                        <span class="val">+10活跃值</span><i class="sub <#if arrangeHomeWorkFlag!false>sub-blue</#if>"></i>[完成布置作业] <#if arrangeHomeWorkFlag!false><span class="w-orange">已完成</span><#else>未完成</#if>
                    </div>
                    <div class="lv-task">
                        <span class="val">+3活跃值</span><i class="sub <#if rewardStudentFlag!false>sub-blue</#if>"></i>[使用智慧课堂，奖励5名学生] <#if rewardStudentFlag!false><span class="w-orange">已完成</span><#else>未完成</#if>
                    </div>
                </dd>
            </dl>
            <#--  下掉了
            <dl>
                <dt>活跃值获取加速： </dt>
                <dd>
                    <div class="lv-task">
                        <span class="val">+50活跃值</span><i class="sub"></i>[<a href="${(ProductConfig.getMainSiteBaseUrl())!''}/teacher/invite/index.vpage" target="_blank" class="w-blue">邀请新老师，达成认证</a>]
                    </div>
                    <div class="lv-task">
                        <span class="val">+10活跃值</span><i class="sub"></i>[<a href="${(ProductConfig.getMainSiteBaseUrl())!''}/teacher/invite/activateteacher.vpage" target="_blank" class="w-blue">唤醒老师回来布置作业</a>]
                    </div>
                </dd>
            </dl>-->
            <div class="w-clear"></div>
        </div>
    </div>

    <div class="w-base">
        <div class="w-base-title">
            <h3>我的特权</h3>
            <div class="w-base-right w-base-more">
                <a target="_blank" href="http://help.17zuoye.com/?p=262"><span class="w-icon-md">查看所有特权</span><span class="w-icon-arrow w-icon-arrow-lRight"></span></a>
            </div>
        </div>
        <div class="t-lv-privilegeInfo">
            <dl>
                <dt><span class="lv-prMed lv-prMed-1"></span><h4>奖励特权</h4></dt>
                <dd>
                    <ol>
                        <#if (teacherLevel gte 11)!false>
                            <li>兑换大使奖品：可以兑换校园大使专区奖品</li>
                        </#if>
                        <#if (teacherLevel gte 13)!false>
                            <li>专享抽奖次数：<a href="${(ProductConfig.getMainSiteBaseUrl())!''}/campaign/teacherlottery.vpage" target="_blank" class="w-blue">获得额外一次免费抽奖机会</a></li>
                        </#if>
                    </ol>
                </dd>
            </dl>
            <#if (teacherLevel gte 3)!false>
                <dl>
                    <dt><span class="lv-prMed lv-prMed-2"></span> <h4>荣誉特权</h4></dt>
                    <dd>
                        <ol>
                            <#-- 2017-04-27 校园大使下线前提示 -->
                            <#if (teacherLevel gte 7)!false><li>优先体验特权：有机会优先体验网站最新功能</li></#if>
                            <#if (teacherLevel gte 9)!false><li>功能研发特权：加入核心用户团队，优先参与一起作业网最核心功能研发 </li></#if>
                            <#if (teacherLevel gte 15)!false><li>参与制作宣传视频：有机会参与一起作业网宣传视频制作~！</li></#if>
                        </ol>
                    </dd>
                </dl>
            </#if>
            <#if (teacherLevel gt 0)!false>
                <dl>
                    <dt><span class="lv-prMed lv-prMed-3"></span><h4>功能特权</h4></dt>
                    <dd>
                        <ol style="margin: 30px 0 30px 0">
                            <li>听力材料下载： <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/tts/listening.vpage" target="_blank" class="w-blue">可以将制作的听力材料下载使用</a> <span class="w-icon-public w-icon-new"></span></li>
                        </ol>
                    </dd>
                </dl>
            </#if>
        </div>
    </div>
    <#if rank?? >
        <div class="w-base">
            <div class="w-base-title">
                <h3>学校等级排行榜：
                您当前在第 <span class="w-orange">${rank!'--'}</span> 名</h3>
            </div>
            <div class="t-lv-charts">
                <ul id="contentChartsList">
                    <#list rankList as rl>
                        <li <#if rl_index gt 9> style="display: none" </#if>>
                            <div class="b-left">
                                <span class="number number-${rl_index + 1}">${rl_index + 1}</span>
                                <span class="avatar"><img src="<@app.avatar href='${rl.IMG_URL!}'/>" width="84" height="84"></span>
                            </div>
                            <div class="b-right">
                                <span class="name">${rl.REALNAME}</span>
                                <span class="lv w-blue">${rl.LEVEL}级</span>
                            <span class="w-lv-grade-box">
                                <#assign tempRlLevel = (rl.LEVEL)!0/>
                                <#if (tempRlLevel/64)?int gte 1>
                                    <#list 1..((tempRlLevel/64)?int) as i>
                                        <i class="w-lv-grade w-lv-grade-4"></i>
                                    </#list>
                                </#if>
                                <#if (tempRlLevel%64/16)?int gte 1>
                                    <#list 1..((tempRlLevel%64/16)?int) as i>
                                        <i class="w-lv-grade w-lv-grade-3"></i>
                                    </#list>
                                </#if>
                                <#if ((tempRlLevel)%16/4)?int gte 1>
                                    <#list 1..((tempRlLevel%16/4)?int) as i>
                                        <i class="w-lv-grade w-lv-grade-2"></i>
                                    </#list>
                                </#if>
                                <#if ((tempRlLevel)%16%4/1)?int gte 1>
                                    <#list 1..((tempRlLevel%16%4/1)?int) as i>
                                        <i class="w-lv-grade w-lv-grade-1"></i>
                                    </#list>
                                </#if>
                            </span>
                            </div>
                        </li>
                    </#list>
                </ul>
            </div>
            <div class="w-clear"></div>
            <div class="t-show-box">
                <div class="w-turn-page-list" id="pageList">
                    <a class="v-page-number back" href="javascript:void(0);" v="prev"><span>上一页</span></a>
                    <a class="this" href="javascript:void(0);">--</a>
                    <span>/</span>
                    <a class="total" href="javascript:void(0);">--</a>
                    <a class="v-page-number next" href="javascript:void(0);" v="next"><span>下一页</span></a>
                </div>
            </div>
        </div>
    <#else>
        <div class="w-base">
            <div class="w-base-title">
                <h3>学校等级排行榜：</h3>
            </div>
            <div style="padding: 80px 0;text-align: center;">
                <#if currentUser.authenticationState == 1>
                    榜单更新中，明天再来查看吧。
                <#else>
                    认证后开启校园等级排行榜，与全校教师PK！快<a href="/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage" class="w-blue">去认证</a>吧~
                </#if>
            </div>
        </div>
    </#if>
</div>
<script type="text/html" id="T:专享园丁豆系数">
<div class="w-table w-table-border w-table-border-width">
    <table>
        <tr class="odd">
            <th colspan="8">每次作业和测验减负系数</th>
        </tr>
        <tr>
            <th>作业/测验次数</th>
            <th>第1次</th>
            <th>第2次</th>
            <th>第3次</th>
            <th>第4次</th>
            <th>第5次</th>
            <th>第6次</th>
            <th>第7次</th>
        </tr>
        <tr>
            <th>减负系数</th>
            <th>1</th>
            <th>1.5</th>
            <th>0.1</th>
            <th>0.1</th>
            <th>0.1</th>
            <th>0.1</th>
            <th>0.1</th>
        </tr>
    </table>
</div>
<div style="margin: 15px 0 0; line-height: 24px;">
    <p>1.每次升级，可从第三次作业和测验开始，将减负系数提高0.1</p>
    <p>2.从第三次作业和测验开始，减负系数最高可提升到1</p>
    <p>3.通过提高教师等级将第三次作业和测验减负系数提高到1后，下次升级开始提高第四次作业和测验的减负系数，以此类推</p>
    <p>4.最高将第7次作业和测验的减负系数提高至1后，不再向上累积</p>
    <p>5.作业和测验次数是合并计算的，每周检查一次作业再检查一次测验即为2次</p>
</div>
</script>

<script type="text/javascript">
    $.ajax({
        type: "GET",
        url: "/teacherTask/privilege/getTeacherLevel.vpage",
        success: function(res) {
            if (res.success) {
                $(".taskCenter-popup").show();
                $(".upgradeTxt span").text(res.level_name + '用户');
            }
        }
    });

    $(".knowBtn").click(function () {
       window.location.replace("/");
    });

    if(location.pathname == "/teacher/center/mylevel.vpage"){
        location.href = "/teacher/center/index.vpage#/teacher/center/mylevel.vpage";
    }

    $(function(){
        LeftMenu.changeMenu();
        LeftMenu.focus("mylevel");

        $("#exclusiveGoldCoefficient").on("click", function(){
            $.prompt(template("T:专享园丁豆系数", {}), {
                title: "专享园丁豆系数",
                buttons: { },
                position:{width : 600}
            });
        });

        //翻页
        var pageList = $("#pageList");
        var contentChartsList = $("#contentChartsList li");
        var thisPageNum = 1;
        var thisPageTotal = parseInt(contentChartsList.prevAll().length + 1) / 10;
            thisPageTotal = parseInt(thisPageTotal) < thisPageTotal ? parseInt(thisPageTotal) + 1 : parseInt(thisPageTotal);

        contentMethod(thisPageNum);
        pageList.find(".next").on("click", function(){
            if(thisPageNum < thisPageTotal){
                thisPageNum++;
                contentMethod(thisPageNum);
            }
        });

        pageList.find(".back").on("click", function(){
            if(thisPageNum > 1){
                thisPageNum--;
                contentMethod( thisPageNum);
            }
        });


        function contentMethod( thisNum){
            pageList.find(".this").text(thisNum);
            pageList.find(".total").text(thisPageTotal);

            contentChartsList.each(function(index){
                var $this = $(this);
                if(index >= (thisNum * 10 - 10) && index < (thisNum * 10) ){
                    $this.show();
                }else{
                    $this.hide();
                }
            });
        }
    });

    // 头像上传回调
    function Avatar_callback(data){
        data = eval("(" + data + ")");
        if ( $17.isBlank( data ) ) {
            setTimeout(function(){ window.location.reload(); }, 200);
        } else if ( data ){
            var dataInfo = "上传成功";
            if (!data.success){
                dataInfo = data.info;
            }
            $.prompt("<div style='text-align: center;'>" + dataInfo + "</div>", {
                title: "系统提示",
                buttons: { "知道了": true },
                close: function(){
                    window.location.reload();
                }
            });
        }
    }

    //关闭上传头像弹窗
    function Avatar_Cancel(){
        window.location.reload();
    }
</script>