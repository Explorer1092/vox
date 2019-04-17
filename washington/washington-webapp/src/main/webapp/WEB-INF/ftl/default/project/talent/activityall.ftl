<#import "../../layout/project.module.student.ftl" as temp />
<@temp.page title="我爱背单词">
<@app.css href="public/skin/project/talent/css/skin.css" version="1.0.5"/>
    <div class="header header_third">
        <#--<#if inAreaForAfenti?? && inAreaForAfenti>-->
            <#--<a href="/student/afenti/talent/index.vpage" onclick="statistics()" class="myParticipate"></a>-->
        <#--<#else>-->
            <#--<a href="javascript:void(0);" onclick="statistics()" class="myParticipate"></a>-->
        <#--</#if>-->
        <a href="/student/afenti/talent/index.vpage" onclick="statistics()" class="myParticipate"></a>
    </div>
    <div class="main">
        <ul class="processOne">
            <li>一起作业用户进入单词达人后选择“大赛模式”即可参加我爱背单词大赛。闯关模式的成绩不计入我爱背单词大赛；
            </li>
            <li>我爱背单词大赛分三个赛季进行。每个赛季为期一个月；
                <p><strong> 赛季具体时间如下：</strong></p>
                <p><strong> 第一赛季 <span class="clrOrange">2013年12月01日-2013年12月31日</span>  </strong></p>
                <p><strong> 第二赛季 <span class="clrOrange">2014年01月01日-2014年01月31日</span> </strong></p>
                <p><strong> 第三赛季 <span class="clrOrange">2014年02月01日-2014年02月28日</span> </strong> </p>
            </li>
            <li>新赛季开始后，上赛季大赛模式下的闯关记录将被清除，闯关模式的记录不受影响；        </li>
            <li>大赛分为：1-2年级组、3-4年级组、5-6年级组，并将分组别进行排名；        </li>
            <li>排行榜首先按照掌握单词数量进行排名；单词数量相同再按准确率进行排名；如果准确率也相同，再按答题时长进行排名；
                <a href="/project/talent/index.vpage?lookBack=true&#info"  target="_blank">了解详细规则</a>
            </li>
            <li>大赛排行榜分为日榜、周榜、总榜。日榜以参赛者当日0时至24时闯关成绩进行排名；周榜按每周一0时至周日24时的闯关
                成绩进行排名；总榜按本赛季开始后所有闯关成绩进行排名；</li>
            <li>我爱背单词大赛只对部分区域开放；
                <div class="text_center spacing_vox">
                    <a href="/apps/afenti/order/talent-cart.vpage" onclick="statistics2()" class="btn_mark btn_mark_primary"><strong>立即购买</strong></a>
                </div>
            </li>
        </ul>
        <ul class="processTwo">
            <li>
                每年级组日榜排名第一名奖励 <b class="clrOrange">10学豆</b>，每日24时发放；
            </li>
            <li>每个年级组周榜排名第一名奖励 <b class="clrOrange">30学豆</b>，每周日24时发放；   </li>
            <li>每个年级组月榜排名第一名奖励 <b class="clrOrange">5000学豆</b>，每月最后一天凌晨截止。奖励在赛季结束后统一发放；    <b class="clrOrange">最终结果将在2014年3月5日公布</b> </li>
        </ul>

        <p class="foot">
            <a href="http://weibo.com/yiqizuoye" class="postnAbs" target="_blank" style="width: 144px; height: 46px; top: 140px; left: 273px;" title="关注一起作业"></a>
            <a href="http://17zuoyeweixin.diandian.com/post/2013-03-14/40049678671" target="_blank" class="postnAbs" style="width: 194px; height: 46px; top: 140px; left: 704px;" title="关注一起作业微信"></a>
        </p>
    </div>
    <div class="footer">声明：如有举报作弊行为经查证属实，将取消参赛资格，一起作业网对本次活动拥有最终解释权。</div>
    <script type="text/javascript">
        function statistics(){
            $17.tongji("单词达人-排行榜-我要参加");
        }

        function statistics2(){
            $17.tongji("单词达人-排行榜-立即购买");
        }

    </script>
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
                <#--报错了-->
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