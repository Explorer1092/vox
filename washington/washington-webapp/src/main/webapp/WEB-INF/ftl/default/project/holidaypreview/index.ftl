<#import "../../layout/mobile.layout.ftl" as temp>
<#assign defaultDpi = ".595" currentTypeCount = 2/>
<#if (currentUser.userType == currentTypeCount)!false>
    <#assign defaultDpi = ""/>
</#if>
    <@temp.page title="阿分题预习方法" dpi=defaultDpi>
    <@app.css href="public/skin/project/holidaypreview/css/details.css"/>
    <@sugar.capsule js=['jquery', "core", "alert", "template"] css=["plugin.alert"] />
    <div class="partThree">
        <div class="banner"></div>
        <div class="text">
            <p>马上就要进入暑假了，爸爸妈妈为孩子准备了哪些精彩的暑期活动呢？</p>
            <p>正在使用《阿分题》在线提分课的爸爸妈妈们，这里有你不知道的<i>新学年预习技巧。只需三步,轻松为孩子制定新学年预习计划。</i></p>
        </div>
        <div class="step">
            <div class="title">第<span class="icon">1</span>步<span class="small">打开一起作业学生app，进入阿分题</span></div>
            <div class="intro intro01"></div>
        </div>
        <div class="step">
            <div class="title">第<span class="icon">2</span>步<span class="small">更换教材到下学期教材</span></div>
            <div class="intro intro02"></div>
            <div class="intro intro03"></div>
        </div>
        <div class="step">
            <div class="title">第<span class="icon">3</span>步<span class="small">开始下学期课程预习学习啦</span></div>
            <div class="intro intro04"></div>
            <div class="intro intro05"></div>
        </div>
        <div class="btns-group">
            <div class="btn js-englishBtn js-isShowBox" style="display: none;"></div>
            <div class="btn js-mathBtn"></div>
        </div>
        <div class="text">《阿分题》在线提分课画面活泼生动，充分调动学生自学积极性，同时限定每日学习内容。每天几分钟轻松学习。</div>
        <div class="footer js-isShowBox" style="display: none;">
            <div class="empty"></div>
            <div class="js-english-mBtn"></div>
        </div>
    </div>
    <script type="text/javascript">
        $(function(){
            var sid =  $17.getQuery("sid");

            $(".js-englishBtn").append('<a href="/parentMobile/ucenter/shoppinginfo.vpage?sid='+ sid +'&productType=AfentiExam"  class="btn-blue">阿分题英语</a>');
            $(".js-mathBtn").append('<a href="/parentMobile/ucenter/shoppinginfo.vpage?sid='+ sid +'&productType=AfentiMath" class="btn-green">阿分题数学</a>');
            $(".js-english-mBtn").append('<div class="footerFixed"><a href="/parentMobile/ucenter/shoppinginfo.vpage?sid='+ sid +'&productType=AfentiExam&specialProductType=AfentiSuit">购买阿分题英语+阿分题数学双科</a></div>');

            if($17.getQuery("pageType") == "show"){
                $(".js-isShowBox").show();
            }

            /*加载*/
            $17.voxLog({
                module: "m_lCMSZTpD",
                op: "o_W2ef8Cnl"
            },"student");
            /*购买英语*/
            $(document).on("click", ".js-englishBtn",function(){
                $17.voxLog({
                    module: "m_lCMSZTpD",
                    op: "o_OrVydB5s"
                },"student");
            });
            /*购买数学*/
            $(document).on("click", ".js-mathBtn",function(){
                $17.voxLog({
                    module: "m_lCMSZTpD",
                    op: "o_kR7dbKOp"
                },"student");
            });
            /*购买双科*/
            $(document).on("click", ".js-english-mBtn",function(){
                $17.voxLog({
                    module: "m_lCMSZTpD",
                    op: "o_bldS5CRC"
                },"student");
            });
        });
    </script>
</@temp.page>