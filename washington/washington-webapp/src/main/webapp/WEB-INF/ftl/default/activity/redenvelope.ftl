<#import "../layout/project.module.student.ftl" as temp />
<@temp.page header="hide">
    <@sugar.capsule css=["redenvelope"] />
<!--//start-->
<div class="school-packs-main">
    <div class="head">
        <div class="s-inner">
            <a class="logo" href="/"></a>
            <div class="s-share">
                <div class="jiaThisShare">
                    <div class="jiathis jInner">
                        <div class="jiathis_style">
                            <span class="jiathis_txt">分享到：</span>
                            <a class="jiathis_button_qzone" title="QQ空间"></a>
                            <a class="jiathis_button_tsina" title="新浪微博"></a>
                            <a class="jiathis_button_tqq" title="腾讯微博"></a>
                            <a class="jiathis_button_douban" title="豆瓣"></a>
                            <a href="http://www.jiathis.com/share?uid=1613716" class="jiathis jiathis_txt jiathis_separator jtico jtico_jiathis" target="_blank" title="更多"></a>
                        </div>
                        <script type="text/javascript">
                            var summaryText = '';
                                <#if currentUser.userType == 1>
                                summaryText = '【开学抢红包大行动】活动开始啦！快来@一起作业网吧，百万红包大派送！布置作业即可获得50园丁豆红包；还可参加大抽奖，平板电脑、红米手机等你拿哦！新学期50多本新教材、40多款新应用上线，快快告诉你的小伙伴儿一起来参加吧！';
                                <#elseif currentUser.userType == 3>
                                summaryText = '【开学抢红包大行动】活动开始啦！快来@一起作业网吧，百万红包大派送！红包里有学豆、PK套装、宠物蛋等奖品哦！还有40多款新应用上线，完成作业即可获得，快快告诉你的小伙伴儿一起来参加吧！';
                                </#if>
                            var jiathis_config = {
                                data_track_clickback: true,
                                url: "http://www.17zuoye.com/activity/redenvelope.vpage",
                                title: " ",
                                summary: summaryText,
                                pic: "//cdn.17zuoye.com/static/project/student/redenvelopeshare.jpg",
                                shortUrl: false,
                                hideMore: false
                            }
                        </script>
                        <script type="text/javascript" src="http://v3.jiathis.com/code/jia.js?uid=1613716" charset="utf-8"></script>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="content">
        <div class="s-inner">
            <div class="procedure-active-box ">
                <strong class="s-title">红包大派送</strong>
                <ul>
                    <li><span>1</span>活动时间：3月2日——3月15日。</li>
                    <#if currentUser.userType == 1>
                        <li><span>2</span>老师首次布置作业即领取开学大红包，内含50园丁豆。</li>
                        <li><span>3</span>学生首次完成作业可以抢开学大红包，红包里面有学豆、PK时装和新年宠物蛋哦！</li>
                    <#elseif currentUser.userType == 3>
                        <li><span>2</span>学生首次完成新学期老师布置的作业可以抢开学大红包，红包里面有学豆、PK时装和新年宠物蛋哦！</li>
                    </#if>
                </ul>
                <#if (.now < '2015-03-15 23:59:59'?datetime('yyyy-MM-dd HH:mm:ss'))>
                    <a id="grabARedEnvelope" class="tips-bav tips-redbav" href="javascript:void (0)">抢红包</a>
                <#else>
                    <a class="tips-bav tips-redbav" href="javascript:void (0)">活动已结束</a>
                </#if>
            </div>
            <#if currentUser.userType == 1>
                <div class="procedure-active-box procedure-active-redbox">
                    <strong class="s-title">幸运大抽奖</strong>
                    <ul>
                        <li>深受老师们喜爱的布置作业抽大奖活动在3月2日开始新的一期啦！</li>
                        <li>在网站上布置作业，每天可获得1次免费抽奖机会；使用微信端布置作业每天可获得5次免费抽奖机会。</li>
                        <li>平板、红米等你拿，机会多多，赶快行动吧！</li>
                    </ul>
                    <a onclick="$17.tongji('开学红包-去抽奖')" class="tips-bav tips-purplebav" href="/campaign/teacherlottery.vpage" target="_blank">去抽奖</a>
                </div>
            </#if>

            <div class="procedure-active-box procedure-active-greenbox">
                <strong class="s-title">开学礼不停</strong>
                <ul>
                    <li>2月1日——3月15日活动期间，学生给老师免费赠送新年礼物，按照礼物数量进行人气教师排行。</li>
                    <li>全国榜前20名的老师获得1000学豆，区榜前20名的老师获得500学豆，在活动结束后统一赠送到老师账号。</li>
                    <li>有钱就是这么任性，随意奖励学生吧！</li>
                    <li>区榜、全国榜排名每天更新一次。</li>
                </ul>
            </div>
            <div class="info-content-box">
                <div class="inner">
                    <div class="ic-con">
                        <div style="width: 50%; float: left;" class="ic-con-left fl">
                            <p class="ib-title">区榜</p>
                            <p class="ib-nav">
                                <span style="width: 23%;">排名</span>
                                <span style="width: 36%;">老师姓名</span>
                                <span>礼物数</span>
                            </p>
                            <div class="ib-table">
                                <table>
                                    <#if countyRankList?has_content>
                                        <#list countyRankList as countyRankList>
                                            <tr>
                                                <td style="width: 30%;">
                                                    <span class="num">
                                                        <i class="icon-num icon-num-0${countyRankList_index + 1}">${countyRankList_index + 1}</i>
                                                    </span>
                                                </td>
                                                <td style="width:38%;">
                                                    <strong>${countyRankList.teacherName!''}</strong><br>
                                                    <span class="name">${countyRankList.schoolName!''}</span>
                                                </td>
                                                <td>${countyRankList.giftCount!''}</td>
                                            </tr>
                                        </#list>
                                    <#else>
                                        <tr>
                                            <td rowspan="3" style="text-align: center; color: #999;">暂无数据</td>
                                        </tr>
                                    </#if>
                                </table>
                            </div>
                        </div>
                        <div style="width: 50%; float: left;" class="ic-con-right fl">
                            <p class="ib-title">全国榜</p>
                            <p class="ib-nav">
                                <span style="width: 23%;">排名</span>
                                <span style="width: 36%;">老师姓名</span>
                                <span>礼物数</span>
                            </p>
                            <div class="ib-table">
                                <table>
                                    <#if countryRankList?has_content>
                                        <#list countryRankList as countryRankList>
                                            <tr>
                                                <td style="width: 30%;">
                                                    <span class="num">
                                                        <i class="icon-num icon-num-0${countryRankList_index + 1}">${countryRankList_index + 1}</i>
                                                    </span>
                                                </td>
                                                <td style="width:38%;">
                                                    <strong>${countryRankList.teacherName!''}</strong><br>
                                                    <span class="name">${countryRankList.schoolName!''}</span>
                                                </td>
                                                <td>${countryRankList.giftCount!''}</td>
                                            </tr>
                                        </#list>
                                    <#else>
                                        <tr>
                                            <td rowspan="3" style="text-align: center; color: #999;">暂无数据</td>
                                        </tr>
                                    </#if>
                                </table>
                            </div>
                        </div>
                        <div class="clear"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="more-content">
        <div class="s-inner">
            <div class="more-newest-title">最新上线</div>
            <div class="procedure-active-box procedure-active-fontbox">
                <i class="new-font">教师端新功能</i>
                <#if currentUser.userType == 1>
                    <a class="tips-bav-btn" onclick="$17.tongji('开学红包-查看详细功能介绍')" href="http://www.17huayuan.com/forum.php?mod=viewthread&tid=21417" target="_blank">查看详细功能介绍</a>
                </#if>
                <ul>
                    <li><em>1.</em> <strong class="bold">新学期新教材</strong> 英语22本，数学32本新教材陆续上线！快来布置作业吧！</li>
                    <li><em>2.</em> <strong class="bold">作业批量检查</strong> 多个班级的到期作业可以轻松一键批量检查！</li>
                    <li><em>3.</em> <strong class="bold">智慧课堂随机点名多人</strong> 智慧课堂可以随机选择多名同学进行对话练习哦！</li>
                    <li><em>4.</em> <strong class="bold">测验报告更详细</strong> 测验报告可以查看学生每道题的答案，还能导出报告哦！</li>
                </ul>
            </div>
            <div class="procedure-active-box procedure-active-fontbox">
                <i class="new-font">学生端新功能</i>
                <ul>
                    <li><em>1.</em> <strong class="bold">新学期新应用</strong> 英语、数学共42款新应用陆续上线！快来体验吧！</li>
                    <li><em>2.</em> <strong class="bold">历史记录新功能</strong> 作业历史可以查看答题结果啦！还能针对问题反复练习哦！</li>
                    <li><em>3.</em> <strong class="bold">宠物蛋孵化系统</strong> 宠物蛋可以孵化啦！带上自己的宠物跟小伙伴们一起PK吧！</li>
                </ul>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function(){
        //抢红包
        $('#grabARedEnvelope').on('click',function(){
            $17.tongji('开学红包-抢红包');
            $.post('/activity/receiveredenvelope.vpage',{},function(data){
                $17.alert(data.info);
            });
        });
    });
</script>
</@temp.page>