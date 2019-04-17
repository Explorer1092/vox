<#import "../../layout/mobile.layout.ftl" as temp>
<@temp.page title="阿分题" dpi=defaultDpi>
<@app.css href="public/skin/project/afentidetailapp/css/afentidetail.css"/>
<@sugar.capsule js=['jquery', "core", "alert", "template"] css=["plugin.alert"] />
<div class="aft-banner"></div>
<div class="aft-container">
    <div class="aft-list aft-list02">
        <div class="title02">
            <i class="icon-l"></i>
            <p class="tit-text">
                <span class="text01">错题本</span>
                <span class="text02">突击重练本学期作业错题</span>
            </p>
            <i class="icon-r"></i>
        </div>
        <div class="info">
            <div class="name"><i class="tips-icon"></i><span>阿分题错题本功能，汇总孩子本学期作业中做错过的所有题目，为孩子制定专属的考前突击提分计划。</span></div>
            <div class="txtBlue">
                <div class="text"><i class="circle-icon"></i><span>本学期掌握不牢固的知识点一目了然</span></div>
                <div class="text"><i class="circle-icon"></i><span>错题集训，快速提分</span></div>
            </div>
            <div class="pic pic01"></div>
            <div class="txtGrey">
                <div class="text"><i class="label-icon"></i><span>“错题本”－“应试作业”中包括本学期所有老师作业错题</span></div>
                <div class="text"><i class="label-icon"></i><span>改完后还可以随时在“已改对”中重新训练</span></div>
            </div>
        </div>
    </div>
    <div class="aft-list aft-list02">
        <div class="title02">
            <i class="icon-l"></i>
            <p class="tit-text">
                <span class="text01">类题因子</span>
                <span class="text02">错题自动出类题，易错题型反复练</span>
            </p>
            <i class="icon-r"></i>
        </div>
        <div class="info">
            <div class="name"><i class="tips-icon"></i><span>《阿分题》类题因子，根据海量教辅书籍，近年考试试卷等，根据孩子的错题本智能推送相关知识点易考例题</span></div>
            <div class="txtBlue">
                <div class="text"><i class="circle-icon"></i><span>让孩子在自己的易错题型上反复练习</span></div>
                <div class="text"><i class="circle-icon"></i><span>类题题库来自近年易考例题</span></div>
            </div>
            <div class="pic pic02"></div>
            <div class="txtGrey">
                <div class="text"><i class="label-icon"></i><span>改正错题后，自动生成同知识点易考类题</span></div>
            </div>
        </div>
    </div>
    <div class="aft-list aft-list02">
        <div class="title02">
            <i class="icon-l"></i>
            <p class="tit-text">
                <span class="text01">因材施教</span>
                <span class="text02">根据教材生成考点</span>
            </p>
            <i class="icon-r"></i>
        </div>
        <div class="info">
            <div class="name"><i class="tips-icon"></i><span>《阿分题》学习产品专注全国小学教材考点，孩子选择自己学校使用的教材后，根据教材推送考点例题，达到精准练习快速提分。</span></div>
            <div class="txtBlue">
                <div class="text"><i class="circle-icon"></i><span>包含全国所有小学使用教材</span></div>
                <div class="text"><i class="circle-icon"></i><span>根据教材推送考点例题</span></div>
            </div>
            <div class="pic pic03"></div>
            <div class="txtGrey">
                <div class="text"><i class="label-icon"></i><span>使用前请选择孩子正在使用的教材</span></div>
            </div>
        </div>
    </div>
    <div class="aft-footer">
        <div class="buyBox englishBox">
            <div class="btns js-englishBtn"></div>
            <div class="logo logo01"></div>
            <div class="text">马上开通阿分题英语</div>
        </div>
        <div class="buyBox mathBox">
            <div class="logo logo02"></div>
            <div class="text">马上开通阿分题数学</div>
            <div class="btns js-mathBtn"></div>
        </div>
    </div>
</div>
<div class="aft-footer">
    <div class="empty"></div>
    <div class="fixedFooter">
        <div class="buyBox">
            <div class="logo logo03"></div>
            <div class="text">阿分题英语+阿分题数学</div>
            <div class="btns js-english-mBtn"></div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        var sid= $17.getQuery("sid");
        $(".js-englishBtn").append('<a href="/parentMobile/ucenter/shoppinginfo.vpage?sid='+ sid +'&productType=AfentiExam">购买</a>');
        $(".js-mathBtn").append('<a href="/parentMobile/ucenter/shoppinginfo.vpage?sid='+ sid +'&productType=AfentiMath">购买</a>');
        $(".js-english-mBtn").append('<a href="/parentMobile/ucenter/shoppinginfo.vpage?sid='+ sid +'&productType=AfentiExam&specialProductType=AfentiSuit">购买</a>');

        /*加载*/
        $17.voxLog({
            module : "afenti-detail",
            op : "load"
        },"student");
        /*购买英语*/
        $(document).on("click", ".js-englishBtn",function(){
            $17.voxLog({
                module: "afenti-detail",
                op: "click-english"
            },"student");
        });
        /*购买数学*/
        $(document).on("click", ".js-mathBtn",function(){
            $17.voxLog({
                module: "afenti-detail",
                op: "click-math"
            },"student");
        });
        /*购买双科*/
        $(document).on("click", ".js-english-mBtn",function(){
            $17.voxLog({
                module: "afenti-detail",
                op: "click-english-m"
            },"student");
        });
    });
</script>
</@temp.page>