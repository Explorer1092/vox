<#import "../module.ftl" as temp />
<@temp.pagecontent mainmenu="classroom_ask" submenu="iwen">
<div class="s-tab-box">
    <div class="inner">
        <div class="st-title">
            <h2>爱提问下载</h2>
        </div>
        <div class="pc-inner">
            <div class="s-pc-sweep">
                <p style="padding: 25px 0 45px 0;">爱提问是一起作业为老师提供的用于快速收集学生课堂练习客观题答案的移动端产品，您只需拿手机/平板电脑轻松扫描，即可快速获取学生答题情况分析。</p>
                <dl>
                    <dt>
                        <span class="ps-img"><img src="<@app.link href="public/skin/project/iwen/images/iquestion_1.0.3_v1.png"/>" width="114"></span>
                    </dt>
                    <dd>
                        <p>
                            手机扫描下载爱提问
                            <i class="ws-icon"></i><span class="w-blue">iOS</span>手机版
                            <a class="s-sp-btn s-sp-btn-apple" href="https://itunes.apple.com/cn/app/yi-qi-zuo-ye-ai-ti-wen/id910850414?mt=8" target="_blank"></a>
                        </p>
                    </dd>
                </dl>
                <dl>
                    <dt>
                        <span class="ps-img"><img src="<@app.link href="public/skin/project/iwen/images/iquestion_1.0.3_v1.png"/>" width="114"></span>
                    </dt>
                    <dd>
                        <p>
                            快速下载爱提问
                            <i class="ws-icon ws-icon-2"></i><span class="w-blue">Android</span>手机版
                            <a class="s-sp-btn s-sp-btn-android" href="//cdn.17zuoye.com/static/project/iwen/iquestion_1.0.4.apk" target="_blank"></a>
                        </p>
                    </dd>
                </dl>
            </div>
        </div>
    </div>
</div>
<div class="s-tab-box">
    <div class="inner">
        <div class="st-title">
            <h2>学生卡片下载</h2>
        </div>
        <div class="s-down-box">
            <dl>
                <dt>
                <p class="s-magB-10">学生卡片由二维码、卡片编号、试题选项组成，用于课上学生回答问题使用，每名学生的卡片是唯一的。</p>
                <p class="s-red">温馨提示：请下载打印后按照学生卡片编号与学生姓名的对应关系发放；</p>
                <#--<p class="s-red sd">打印时请根据学生数量选择打印页面，如班级有30名同学，则打印1-30页。</p>-->
                </dt>
                <dd>
                    <#if maxCardNo?has_content && maxCardNo gt 0 && maxCardNo lt 313>
                        <a href="//cdn.17zuoye.com/static/project/iwen/iwencardsv2/student_card_${maxCardNo}.pdf" target="_blank"><i class="s-sp-btn s-sp-btn-down"></i></a>
                    <#else>
                        <a href="//cdn.17zuoye.com/static/project/iwen/iwencardsv2/student_card_30.pdf" target="_blank"><i class="s-sp-btn s-sp-btn-down"></i></a>
                    </#if>
                </dd>
            </dl>
        </div>
        <div class="s-table s-table-line">
            <table>
                <tbody>
                <#if studentCardList?has_content>
                    <#list studentCardList?chunk(5) as subList>
                    <tr>
                        <#list subList as studentCard>
                            <td>${studentCard.studentName}  No.${studentCard.cardNo}</td>
                        </#list>
                    </tr>
                    </#list>
                <#else>
                  <tr><td colspan="5">${message!"班级无学生或卡号未生成"}</td></tr>
                </#if>
                </tbody>
            </table>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        $17.tongji("互动课堂-课堂提问-爱提问&学生卡片");
        $("a.s-sp-btn-apple").on("click",function(){
            $17.tongji("互动课堂-课堂提问-iOS下载");
        });
        $("a.s-sp-btn-android").on("click",function(){
            $17.tongji("互动课堂-课堂提问-android下载");
        });
        $("a.s-sp-btn-down").on("click",function(){
            $17.tongji("互动课堂-课堂提问-下载-学生卡片");
        });
    });
</script>
</@temp.pagecontent>