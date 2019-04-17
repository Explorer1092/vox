<#import "../../nuwa/teachershellv3.ftl" as temp />
<@temp.page showNav="hide">
    <@app.css href="public/skin/project/ambassador/newamb.css" />
    <div class="w-base">
        <div class="w-base-container">
            <div class="Amb-teacher-avatar">
                <dl class="ata-info w-magT-10" style="width: auto;">
                    <dl class="ata-info w-magT-10" style="width: auto;">
                        <dt>
                            <img src="<@app.avatar href='${ambassador.fetchImageUrl()}'/>">
                        </dt>
                        <dd>
                            <h3><#if (ambassador.subject)?? && (ambassador.subject) =='ENGLISH'>英语老师 <#else>数学老师 </#if>：${(ambassador.profile.realname)!}</h3>
                            <p><i class="t-card-${(level.level)!'SHI_XI'}"></i><span class="w-icon-md">${(level.level.description)!'实习大使'}</span></p>
                        </dd>
                    </dl>
                </dl>
                <div class="w-clear"></div>
            </div>
        </div>
    </div>

    <div class="apply-success mc-box">
        <div class="as-hd mc-hd">
            <p>已成为预备大使</p>
        </div>
        <div class="as-mn mc-mn">
            <p class="top">当现任大使下任时，努力值排名第一者当选。</p>
            <div class="inner-box">
                <div class="left">
                    <span class="txt">我的努力值：</span><span class="num">${(myRankInfo.score)!0}</span>
                    <#if (myRankInfo.score gt 0)!false>
                        <a href="javascript:void(0)" class="link v-clickScore">具体明细</a>
                    <#else>
                        <a href="javascript:void(0)" class="link v-clickLlValue">努力值计算规则</a>
                    </#if>
                </div>
                <#if (myRankInfo.rank gt 0)!false><div class="right"><p>在所有预备大使中排名第${(myRankInfo.rank)!'---'}</p></div></#if>
            </div>
            <p class="bot-r">注：仅累计近3个月努力值</p>
        </div>
    </div>
    <script type="text/javascript">
        $(function(){

            //努力值
            $(document).on("click", ".v-clickLlValue", function(){
                $.prompt(template("T:努力值计算公式", {}), {
                    title: "努力值计算规则",
                    buttons: { "知道了": false},
                    position: {width: 600}
                });
            });
            //努力值明细
            $(document).on("click", ".v-clickScore", function(){
                $.prompt(template("T:努力值明细", {}), {
                    title: "努力值明细",
                    buttons: {},
                    position: {width: 600},
                    loaded: function(){
                        //分页
                        var detailLists = $("#detailTableList tbody tr");
                        var len = detailLists.length;
                        var pageSize = 5;
                        var pageCount = Math.ceil(len/pageSize);
                        var regularExp=/\d+/;
                        var currentPage = 1;
                        var goto = function(iCur) {
                            var currentPage = iCur;
                            if (regularExp.test(currentPage)){
                                if (currentPage > 0 && currentPage < (pageCount + 1)) {
                                    for (var i = 0; i < len; i++) {
                                        detailLists.eq(i).hide();
                                    }
                                }
                                var totalNum = pageSize * currentPage < len ? pageSize * currentPage : len;
                                for (var i = (currentPage - 1) * pageSize; i < totalNum; i++) {
                                    detailLists.eq(i).show();
                                }
                            }
                            if(currentPage==1){
                                $("a[v='prev']").attr("class", "disable");
                            }else{
                                $("a[v='prev']").attr("class", "enable");
                            }
                            if(currentPage==pageCount){
                                $("a[v='next']").attr("class", "disable");
                            }else{
                                $("a[v='next']").attr("class", "enable");
                            }
                            $("a[data='"+currentPage+"']").addClass("this").siblings().removeClass("this");
                        };

                        if(pageCount>1){
                            $(".message_page_list").append("<a v=\"prev\" href=\"javascript:void(0);\" class=\"disable\"><span>上一页</span></a>");
                            for(var i = 1 ; i<=pageCount; i++){
                                if(i==1){
                                    $(".message_page_list").append("<a href=\"javascript:void(0);\"  class=\"this\" data=\""+i+"\"><span>"+i+"</span></a>");
                                }else{
                                    $(".message_page_list").append("<a href=\"javascript:void(0);\" data=\""+i+"\"><span>"+i+"</span></a>");
                                }
                            }
                            $(".message_page_list").append("<a v=\"next\" href=\"javascript:void(0);\" class=\"enable\"><span>下一页</span></a>");
                            goto(currentPage);
                        }
                        $(".message_page_list a").click(function(){
                            var currentPage = $(this).find('span').text();
                            goto(currentPage);
                        });
                        $("a[v='prev']").click(function(){
                            currentPage = (currentPage-1)>=1?(currentPage-1):1;
                            goto(currentPage);
                        });
                        $("a[v='next']").click(function(){
                            currentPage = (currentPage+1)<=pageCount?(currentPage+1):pageCount;
                           goto(currentPage);
                        });
                    }
                });
            });
        });
    </script>
    <script type="text/html" id="T:努力值明细">
        <div class="detail-dialog" style="margin-top: -40px;">
            <div class="dialog-inner">
                <p class="tips">注：当天获得的部分努力值，会在第二天进行累加</p>
                <table cellpadding="0" cellspacing="0" id="detailTableList">
                    <thead>
                        <tr>
                            <td class="bg" style="width: 160px;">获取时间</td><td class="bg" style="width: 70px;">努力值</td><td class="bg" >来源</td>
                        </tr>
                    </thead>
                    <tbody>
                    <#if (myRankInfo.scoreDetails)?has_content>
                        <#list myRankInfo.scoreDetails as scoreDetails>
                        <tr>
                            <td>${(scoreDetails.createDatetime)!'---'}</td>
                            <td>${(scoreDetails.score)!0}</td>
                            <td>${(scoreDetails.scoreType.description)!'---'}</td>
                        </tr>
                        </#list>
                    <#else>
                    <tr>
                        <td colspan="3"><div style="padding: 30px 0;">暂无数据</div></td>
                    </tr>
                    </#if>
                    </tbody>
                </table>
                <p class="bot-r v-clickLlValue"><a href="javascript:void(0)">努力值计算规则</a></p>
                <#--分页-->
                <div class="system_message_page_list message_page_list" style="padding:0 0 10px;"></div>
            </div>
        </div>
    </script>
    <script type="text/html" id="T:努力值计算公式">
        <div class="count-box">
            <div class="count-rule" style="height: 258px; overflow: hidden; overflow-y: auto; text-align: left;">
                <table>
                    <tbody>
                    <tr><td class="td01">努力值获取方法</td><td class=" td02">基础积分</td></tr>
                    <tr>
                        <td>给1个班布置作业，有30名及以上学生完成</td>
                        <td>+3</td>
                    </tr>
                    <tr>
                        <td colspan="1">给1个班布置作业，有20-29名学生完成</td>
                        <td colspan="1">+2</td>
                    </tr>
                    <tr>
                        <td colspan="1">给1个班布置作业，有10-19名学生完成</td>
                        <td colspan="1">+1</td>
                    </tr>
                    <tr>
                        <td colspan="1">微信布置，30及以上完成</td>
                        <td colspan="1">+6</td>
                    </tr>
                    <tr>
                        <td colspan="1">微信布置，20-29完成</td>
                        <td colspan="1">+4</td>
                    </tr>
                    <tr>
                        <td colspan="1">微信布置，10-19完成</td>
                        <td colspan="1">+2</td>
                    </tr>
                    <tr>
                        <td colspan="1">当月唤醒老师成功</td>
                        <td colspan="1">+20</td>
                    </tr>
                    <tr>
                        <td>使用智慧课堂，奖励5名及以上学生（每月最多奖励10）</td>
                        <td>+2</td>
                    </tr>
                    <tr>
                        <td>帮助1名老师完成认证</td>
                        <td>+50</td>
                    </tr>
                    <tr>
                        <td>论坛发帖、回帖（每月最多加5）</td>
                        <td>+1</td>
                    </tr>
                    <tr>
                        <td>使用校讯通（每月最多加5）</td>
                        <td>+1</td>
                    </tr>
                    <tr>
                        <td colspan="1">使用评论功能（每月最多加5，每天最多奖励1）</td>
                        <td colspan="1">+1</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </script>
</@temp.page>