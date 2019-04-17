<#import "../../layout/project.module.ftl" as temp />
<@temp.page title="我爱背单词">
    <@app.css href="public/skin/project/newtextbooks/skin.css"/>
    <!--//start-->
    <div class="main">
        <h1 title="新教材计划上线预告">
            　　尊敬的老师，为了便于您了解新教材的上线进度，我们将持续播报2014年新增教材计划。
            如下表中没有您正在使用的新教材，请联系客服：
        </h1>
        <div class="booksTable">
            ${pageBlockContentGenerator.getPageBlockContentHtml('TeacherIndex', 'NewTextBooks')}
        </div>
        <!--<div class="jiathis_box">
            &lt;!&ndash; JiaThis Button BEGIN &ndash;&gt;
                    <div class="jiathis_style">
                        <span class="jiathis_txt">分享到：</span>
                        <a class="jiathis_button_qzone">QQ空间</a>
                        <a class="jiathis_button_tsina">新浪微博</a>
                        <a class="jiathis_button_tqq">腾讯微博</a>
                        <a class="jiathis_button_renren">人人网</a>
                        <a class="jiathis_button_kaixin001">开心网</a>
                        <a href="http://www.jiathis.com/share?uid=1613716" class="jiathis jiathis_txt jiathis_separator jtico jtico_jiathis" target="_blank">更多</a>
                        <a class="jiathis_counter_style"></a>
                    </div>
                    <script type="text/javascript" >
                        var jiathis_config={
                            data_track_clickback:true,
                            title: "#新学年，邀请你的数学合伙人#",
                            summary:"一直都是你一个人在@一起作业网 孤军奋战？是不是也曾感到“孤苦伶仃”？现在机会来啦！立刻邀请数学老师加入你的班级吧！每成功邀请一位数学老师，你还可得300园丁豆哦！",
                            pic:"http://cdn.17zuoye.com/static/project/directedtoinvite/images/spe.jpg",
                            shortUrl:false,
                            hideMore:false
                        }
                    </script>
                    <script type="text/javascript" src="http://v3.jiathis.com/code/jia.js?uid=1613716" charset="utf-8"></script>
                    &lt;!&ndash; JiaThis Button END &ndash;&gt;
                </div>-->
    </div>
    <!--end//-->
    <div id="footerPablic"></div>
    <script src="//cdn.17zuoye.com/static/project/module/js/project-plug.js"></script>
    <script type="text/javascript">
        $(function(){
            $(".booksTable table tr:odd").addClass("even");
        });
    </script>
</@temp.page>