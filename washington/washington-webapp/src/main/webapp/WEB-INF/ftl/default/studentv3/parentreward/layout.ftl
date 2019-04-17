<#import '../layout/layout.ftl' as temp>
<#macro p_reward pageName>
    <@temp.page pageName='parentReward'clazzName='parent-en-main'>
    <@sugar.capsule css=["student.parentReward"] />
    <#assign JZT_CHANNEL_ID = "202015">
    <div class="parent-en-main">
        <div class="parent-en-inner">
            <!--//side-->
            <div class="parent-side">
                <!--Wish-->
                <div class="parent-info-meg">
                    <!--name-->
                    <div class="parent-name">
                        <dl>
                            <dt><img src="<@app.avatar href='${currentUser.fetchImageUrl()}'/>"></dt>
                            <dd>
                                <h3>${(currentUser.profile.realname)!'---'}</h3>
                                <p>
                                    <span>学号：${(currentUser.id)!}</span>
                                    <span style="display: inline-block;">学校：${(currentStudentDetail.studentSchoolName)!'---'}</span>
                                </p>
                                <h3 style="font-size: 14px; line-height: 120%;"><i class="wish-icon icon-info" style="margin: 0;"></i><span class="w-icon-md">家长没有收到心愿？请他<a href="javascript:void(0);" class="js-clickDownloadParent" style="text-decoration: underline; color: #ffe21a;">下载一起作业家长通</a>吧！</span></h3>
                                <script type="text/javascript">
                                    $(document).on("click", ".js-clickDownloadParent", function(){
                                        $17.get_jzt_qr(
                                            "${JZT_CHANNEL_ID}",
                                            function(JZT_QR_URL){
                                                $.prompt('<div style="text-align: center;"><img src="'+JZT_QR_URL+'" alt="家长通二维码" class="doGetJZTQR" /><br/><p>扫一扫下载家长通</p></div>',{
                                                    title : "一起作业家长通下载",
                                                    buttons : {}
                                                });
                                            }
                                        );
                                    });
                                </script>
                            </dd>
                        </dl>
                    </div>
                    <!--menu-->
                    <div class="parent-menu">
                        <ul>
                            <li class="${(pageName == "index")?string("active", "")}">
                                <a href="/student/parentreward/index.vpage">未完成目标</a>
                            </li>
                            <li class="${(pageName == "footprint")?string("active", "")}">
                                <a href="/student/parentreward/footprint.vpage">全部目标</a>
                            </li>
                            <li>
                                <a onclick="$17.tongji('学生-家长奖励-家长指南')" href="http://help.17zuoye.com/?page_id=693" target="_blank">家长指南</a>
                            </li>
                        </ul>
                    </div>
                </div>
                <!--start content-->
                <#--判断是否已经通过微信 or 家长通绑定家长-->
                <#if hasParentBindWechat!false>
                    <#nested />
                <#else>
                    <div class="code-content">
                        <div class="c-inner">
                            <div class="code-box">
                                <img src="" class="doJZTQR" style="display: none;" alt="家长通二维码"/>
                                <script>
                                    $17.get_jzt_qr(
                                        "${JZT_CHANNEL_ID}",
                                        function(JZT_QR_URL){
                                            $(".doJZTQR").attr("src", JZT_QR_URL).show();
                                        }
                                    );
                                </script>
                            </div>
                            <h2>扫描右侧二维码，下载一起作业家长通，即可开通家长奖励</h2>
                            <p>1、使用家长通后每月赠送10学豆，用于奖励学生</p>
                            <p>2、学生还可设定心愿，在完成家长指定任务后，由家长奖励</p>
                        </div>
                    </div>
                </#if>
                <!--end content-->
            </div>
            <!--side//-->
            <div class="pt-clear"></div>
        </div>
    </div>
    </@temp.page>
</#macro>
