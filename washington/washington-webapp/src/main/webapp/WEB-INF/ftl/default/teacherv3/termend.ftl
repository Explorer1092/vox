<#import "../nuwa/teachershellv3.ftl" as temp />
<@temp.page show="resource-reading" showNav="hide">
<@sugar.capsule js=["ko"] />
<div class="finalReview-box" id="termEndDiv">
    <div class="fin-inner" style="display: none" data-bind="if:!loading(),visible:!loading()">
        <div class="fin-banner">
            <img src="<@app.link href='public/skin/teacherv3/images/termend/final-head01.png'/>" style="display: none;" data-bind="visible:$root.level() < 3">
            <img src="<@app.link href='public/skin/teacherv3/images/termend/final-head02.png'/>" style="display: none;" data-bind="visible:$root.level() >= 3">
        </div>
        <div class="fin-main">
            <#--重难点查缺补漏_1-2-->
            <div class="fin-list" style="display: none;" data-bind="visible:($root.level() < 3)">
                <img src="<@app.link href='public/skin/teacherv3/images/termend/final-image01.png'/>">
                <div class="listContent">
                    <div class="line"></div>
                    <div class="title">
                        <p class="titleBar">重难点查缺补漏</p>
                        <p class="englishInfo">Enhancement</p>
                    </div>
                    <div class="text">根据课标要求精选每单元的重点内容，结合大数据甄别出来的班级薄弱知识点，辅助老师针对性地查缺补漏！</div>
                </div>
            </div>
            <div class="fin-list" style="display: none;" data-bind="visible:($root.level() >= 3 && $root.subject() == 'MATH')">
                <img src="<@app.link href='public/skin/teacherv3/images/termend/final-image01.png'/>">
                <div class="listContent">
                    <div class="line"></div>
                    <div class="title">
                        <p class="titleBar">重难点专项</p>
                        <p class="englishInfo">Key Points</p>
                    </div>
                    <div class="text">课标要求&nbsp;&nbsp;精选每单元的重点内容</div>
                    <div class="text">视频解析&nbsp;&nbsp;掌握方法</div>
                </div>
            </div>
            <!--单词必会-->
            <div class="fin-list" style="display: none;" data-bind="visible:$root.level() >= 3 && $root.subject() == 'ENGLISH'">
                <img src="<@app.link href='public/skin/teacherv3/images/termend/final-image02.png'/>">
                <div class="listContent">
                    <div class="item">记不住的单词，是不是因人而异？</div>
                    <div class="line"></div>
                    <div class="title">
                        <p class="titleBar">单词必会</p>
                        <p class="englishInfo">Words</p>
                    </div>
                    <div class="text">夯实基础 考前必会！</div>
                </div>
            </div>
            <#--高频易错题-->
            <div class="fin-list listRight" style="display:none;" data-bind="visible:$root.level() < 3">
                <img src="<@app.link href='public/skin/teacherv3/images/termend/final-image03.png'/>">
                <div class="listContent">
                    <div class="line"></div>
                    <div class="title">
                        <p class="titleBar">高频易错题</p>
                        <p class="englishInfo">Accuracy</p>
                    </div>
                    <div class="text">通过一个学期的作业记录大数据，汇总高频易错题，考前练一练，让满分的小小心愿变成现实</div>
                </div>
            </div>
            <#--班级错题排行-->
            <div class="fin-list listRight" style="display: none;" data-bind="visible:$root.level() >= 3">
                <img src="<@app.link href='public/skin/teacherv3/images/termend/final-image03.png'/>">
                <div class="listContent listLeft">
                    <div class="item">学霸是不是也有意想不到的盲点？</div>
                    <div class="line"></div>
                    <div class="title">
                        <p class="titleBar">班级错题排行</p>
                        <p class="englishInfo">Accuracy</p>
                    </div>
                    <div class="text">基于班级 提分必备。</div>
                </div>
            </div>
            <#--全国错题排行-->
            <div class="fin-list" style="display: none;" data-bind="visible:$root.level() >= 3">
                <img src="<@app.link href='public/skin/teacherv3/images/termend/final-image04.png'/>">
                <div class="listContent listMar">
                    <div class="item">其他人做错的题，对你有没有参考意义？</div>
                    <div class="line"></div>
                    <div class="title">
                        <p class="titleBar">全国错题排行</p>
                        <p class="englishInfo">Nation-wide database</p>
                    </div>
                    <div class="text">1900万小学生 易错集结。</div>
                </div>
            </div>
            <#--重难点查漏补缺-->
            <div class="fin-list listRight" style="display: none;" data-bind="visible:$root.level() >= 3">
                <img src="<@app.link href='public/skin/teacherv3/images/termend/final-image05.png'/>">
                <div class="listContent listLeftDiff">
                    <div class="item">找到薄弱，是靠记忆还是靠数据？</div>
                    <div class="line"></div>
                    <div class="title">
                        <p class="titleBar">重难点查漏补缺</p>
                        <p class="englishInfo">Enhancement</p>
                    </div>
                    <div class="text">数据诊断 攻克薄弱！</div>
                </div>
            </div>
            <div class="fin-list">
                <img src="<@app.link href='public/skin/teacherv3/images/termend/final-arrow.png'/>" class="pic" style="display: none;" data-bind="visible:$root.level() < 3">
                <div class="fin-info">惊喜在一起作业即将呈现的心血之作里……</div>
                <div class="fin-btn">
                    <a href="javascript:void(0);" class="f-btn" data-bind="attr:{href:'/teacher/termreview/index.vpage?log=ad&subject=' + subject()}"></a>
                    <#--<a href="javascript:void(0);" class="f-btn soon-btn"></a>--><!--期末复习即将上线，尽请期待！-->
                </div>
            </div>
        </div>
    </div>
    <div class="fin-foot">
        <div class="subTitle">除了精彩内容，我们还有缤纷彩蛋…一样的规则，更多的收获</div>
        <div class="foot-item">
            <p class="itemTitle">布置期末复习作业，园丁豆多得50%</p>
            <p class="itemTitleBar">学生完成期末复习作业，也会获得额外学豆奖励 <i class="icon-package"></i></p>
        </div>
        <div class="foot-column">
            <div class="text">
                <p><i class="icon-circle"></i>活动时间：从期末作业开放至1月10日</p>
                <p><i class="icon-circle"></i>限定条件：每人每周3次机会哦</p>
            </div>
        </div>
    </div>
</div>
<!--end//-->
<script type="text/javascript">
    $(function(){
        var viewModule = {
            loading : ko.observable(true),
            level   : ko.observable(0),
            subject : ko.observable(""),
            run     : function(){
                var self = this;
                $.post("/teacher/termendinfo.vpage",{},function(data){
                    if(data.success){
                        self.level(data.level || 0);
                        self.subject(data.subject || "");
                        self.loading(false);
                    }
                });
            }
        };
        viewModule.run();
        ko.applyBindings(viewModule,document.getElementById("termEndDiv"));

        $17.voxLog({
            module : "m_8NOEdAtE",
            op     : "page_final_revision_ad_pc"
        });
    });
</script>
</@temp.page>