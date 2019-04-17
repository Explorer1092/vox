<#import "../../layout/project.module.student.ftl" as temp />
<@temp.page title="我爱背单词">
<@app.css href="public/skin/project/talent/css/skin.css" version="1.0.3"/>
<style>
    html,body{background-image: none;}
</style>
    <div class="reviewHeader">
        <div class="inner"></div>
    </div>
    <div class="reviewBanner">
        <div class="inner">
            <p style="top: 32px; left: 142px;"><span class="" id="userName"></span>同学</p>
            <p style="top: 62px; left: 468px; width: 160px;"><span class="" id="userName1"></span></p>
            <p style="top: 53px; left: 280px;"><span class="rorange" id="passWordNum"></span></p>
            <p style="top: 101px; left: 280px;"><span class="rorange" id="passRate"></span></p>
            <p style="top: 247px; left: 0px; width: 350px;"><span class="" id="schoolName"></span></p>
            <p style="top: 204px; left: 124px;"><span class="" id="team"></span></p>
            <p style="top: 107px; left: 670px; width: 140px;"><span class="" id="team1"></span></p>
            <p style="top: 107px; left: 601px;"><span class="" id="season"></span></p>
            <p style="top: 62px; left: 123px;"><span class="" id="avatar"></span></p>
        </div>
    </div>
    <div class="main">
        <div class="billboardMain">
            <#if (currentStudentDetail.getClazzLevelAsInteger())??>
                 <#if currentStudentDetail.getClazzLevelAsInteger() lt 3>
                     <!--//start-->
                     <ul class="listBox">
                         <li class="title title_0" title="日榜"></li>
                         <#if dayReport12?? && dayReport12?size gt 0>
                             <@studentTalentInfo dayReport12/>
                         <#else>
                             <li>数据更新中</li>
                         </#if>
                     </ul>
                     <ul class="listBox">
                         <li class="title title_1" title="周榜"></li>
                         <#if weekReport12?? && weekReport12?size gt 0>
                             <@studentTalentInfo weekReport12/>
                         <#else>
                             <li>数据更新中</li>
                         </#if>
                     </ul>
                     <ul class="listBox listBox_2">
                         <li class="title title_2" title="总榜"></li>
                         <#if totalReport12?? && totalReport12?size gt 0>
                             <@studentTalentInfo totalReport12/>
                         <#else>
                             <li>数据更新中</li>
                         </#if>
                     </ul>
                     <!--end//-->
                 <#elseif currentStudentDetail.getClazzLevelAsInteger() gt 4>
                     <!--//start-->
                     <#--<h1 class="levelYard level_2" title="五六年级"></h1>-->
                     <ul class="listBox">
                         <li class="title title_0" title="日榜"></li>
                         <#if dayReport56?? && dayReport56?size gt 0>
                             <@studentTalentInfo dayReport56/>
                         <#else>
                             <li>数据更新中</li>
                         </#if>
                     </ul>
                     <ul class="listBox">
                         <li class="title title_1" title="周榜"></li>
                         <#if weekReport56?? && weekReport56?size gt 0>
                             <@studentTalentInfo weekReport56/>
                         <#else>
                             <li>数据更新中</li>
                         </#if>
                     </ul>
                     <ul class="listBox listBox_2">
                         <li class="title title_2" title="总榜"></li>
                         <#if totalReport56?? && totalReport56?size gt 0>
                             <@studentTalentInfo totalReport56/>
                         <#else>
                             <li>数据更新中</li>
                         </#if>
                     </ul>
                     <!--end//-->
                 <#else>
                     <!--//start-->
                     <#--<h1 class="levelYard level_1" title="三四年级"></h1>-->
                     <ul class="listBox">
                         <li class="title title_0" title="日榜"></li>
                         <#if dayReport34?? && dayReport34?size gt 0>
                             <@studentTalentInfo dayReport34/>
                         <#else>
                             <li>数据更新中</li>
                         </#if>
                     </ul>
                     <ul class="listBox">
                         <li class="title title_1" title="周榜"></li>
                         <#if weekReport34?? && weekReport34?size gt 0>
                             <@studentTalentInfo weekReport34/>
                         <#else>
                             <li>数据更新中</li>
                         </#if>
                     </ul>
                     <ul class="listBox listBox_2">
                         <li class="title title_2" title="总榜"></li>
                         <#if totalReport34?? && totalReport34?size gt 0>
                             <@studentTalentInfo totalReport34/>
                         <#else>
                             <li>数据更新中</li>
                         </#if>
                     </ul>
                     <!--end//-->
                 </#if>
            </#if>
        </div>
        <script type="text/javascript">
            $(function(){
                var lastSeason = ${lastSeason!'0'};
                if( lastSeason != 0 || lastSeason.season != null ){
                    $("#userName, #userName1").html(lastSeason.userName);
                    $("#schoolName").html(lastSeason.schoolName);
                    $("#passWordNum").html(lastSeason.passWordNum);
                    $("#team, #team1").html(lastSeason.team);
                    $("#passRate").html(lastSeason.passRate);
                    $("#season").html(lastSeason.season);
                    $(".listBox .title_0").html("${day}");
                    $(".listBox .title_1").html("${weekDay}");
                    $(".listBox .title_2").html("${totalDay}");
                    $("#avatar").html("<img src='http://cdn.17zuoye.com/gridfs/"+ lastSeason.avatar +"' width='140' height='140'/>");
                }else{
                    $(".reviewBanner").hide();
                    $(".listBox .title_0").html("${day}");
                    $(".listBox .title_1").html("${weekDay}");
                    $(".billboardMain .listBox_2").html('<li class="title title_2" title="总榜"></li><li>第一赛季比赛尚未结束，谁能成为首位赛季冠军？让我们拭目以待！</li>')
                }
            });
        </script>
        <div class="clear"></div>
        <div class="reviewBox">
            <a id="info"></a>
            <div class="inner">
                <h1>成绩公告说明：</h1>
                <ol>
                    <li>
                        <p>系统将根据各榜单的统计周期更新往期成绩榜单。</p>
                        <p>日榜每日0时更新，展示昨日日榜最终排名；</p>
                        <p>周榜每周一0时更新，展示上周周榜最终排名；</p>
                        <p>总榜新赛季开始时更新，展示上赛季总榜最终排名；</p>
                    </li>

                    <li>
                        <p>排行榜首先按照掌握单词数量进行排名；  </p>
                        <p>单词数量相同再按准确率进行排名； </p>
                        <p>如果准确率也相同，再按答题时长进行排名；</p>
                        <p>单词数量越多，准确率越高，答题时长越短排名越靠前。</p>

                    </li>
                    <li>
                        <p>单词数量计算规则如下：</p>
                        <p>日榜单词量是当日掌握的，且不重复的单词数量；(同一单词可以在不同的两日被掌握且计数)</p>
                        <p>周榜单词量是一周掌握的，且不重复的单词数量；(同一单词可以在不同的两周被掌握且计数) </p>
                        <p>总榜单词量是整个赛季掌握的，且不重复单的单词数量；(同一单词可以在不同的赛季被掌握且计数)   </p>
                    </li>
                    <li>
                        <p>准确率计算规则如下：      </p>
                        <p>日榜准确率等于当日题目答对次数除以当日答题总次数；      </p>
                        <p>周榜准确率等于一周题目答对次数除以一周答题总次数；      </p>
                        <p>总榜准确率等于整赛季题目答对次数除以赛季答题总次数；      </p>
                    </li>
                    <li>
                        <p>答题时长计算规则如下：        </p>
                        <p>日榜答题时长是当日答题时长的累加和；       </p>
                        <p>周榜答题时长是一周答题时长的累加和；     </p>
                        <p>总榜答题时长是整赛季答题时长的累加和；   </p>
                    </li>
                </ol>
            </div>
        </div>
    </div>
    <div class="footer">声明：如有举报作弊行为经查证属实，将取消参赛资格，一起作业网对本次活动拥有最终解释权。</div>
</@temp.page>

<#--榜单中个人信息的显示-->
<#macro studentTalentInfo talentReport>
    <#list talentReport as afentiTalentReport>
    <li class="rank_${afentiTalentReport_index + 1}">
        <dl>
            <dt>${afentiTalentReport_index + 1}</dt>
            <dd>
                <p class="picture"><img src="<@app.avatar href="${(afentiTalentReport.imgUrl)!}"/>" onerror="this.onerror='';this.src='<@app.avatar href=""/>'" alt="头像"></p>
                <p class="names">姓名：${(afentiTalentReport.userName)!""}</p>
             <p class="school">学校：<span>${(afentiTalentReport.schoolName)!}</span></p>
            <#--${controller.getSafeStudentSchoolInfoHtml((afentiTalentReport.schoolName)!)}-->
                <p class="statis">
                    <span class="word">单词数：${(afentiTalentReport.passWordNum)!0}</span>
                    <span class="exact">准确率${((afentiTalentReport.passRate)!0)?string("###.00")}%</span>
                </p>
            </dd>
        </dl>
        <p class="clear"></p>
    </li>
    </#list>
</#macro>