<#if (ProductDevelopment.isTestEnv() || ProductDevelopment.isStagingEnv() || ProductDevelopment.isProductionEnv())>
    <#assign jskid = ".min" />
<#else>
    <#assign jskid = "" />
</#if>

<#macro capsule js=[] css=[] block=[] cdn=false>
    <#compress>
        <#list css as name>
            <#switch name>
                <#case 'base'><@app.css href="public/css/parent/base${jskid}.css" /><#break>
                <#case 'jbox'><@app.css href="public/lib/jbox/Source/jBox${jskid}.css" /><#break>
                <#case 'flexslider'><@app.css href="public/lib/jquery.flexslider/flexslider${jskid}.css" /><#break>
                <#case 'report'><@app.css href="public/css/parent/report${jskid}.css" /><#break>
                <#case 'product'><@app.css href="public/css/parent/product${jskid}.css" /><#break>
                <#case 'receiveLoginReward'><@app.css href="public/css/parent/receive_login_reward${jskid}.css" /><#break>
                <#case 'wronglist'><@app.css href="public/css/parent/wronglist${jskid}.css" /><#break>
				<#case 'unitReport'><@app.css href="public/css/ureport/unitReport${jskid}.css" /><#break>
                <#case 'trustee'><@app.css href="public/css/parent/trustee${jskid}.css" /><#break>
                <#case 'trusteetwo'><@app.css href="public/css/parent/trusteetwo${jskid}.css" /><#break>
                <#case 'openclass'><@app.css href="public/css/parent/openclass${jskid}.css" /><#break>
                <#case 'ucenterchildren'><@app.css href="public/css/parent/ucenterchildren${jskid}.css" /><#break>
                <#case 'ucentersetmission'><@app.css href="public/css/parent/ucentersetmission${jskid}.css" /><#break>
                <#case 'thanksgiving'><@app.css href="public/css/parent/thanksgiving${jskid}.css" /><#break>
                <#case 'christmas'><@app.css href="public/css/parent/christmas${jskid}.css" /><#break>
                <#case 'globalmath'><@app.css href="public/css/parent/globalmath${jskid}.css" /><#break>
                <#case 'onlineqa'><@app.css href="public/css/parent/onlineqa${jskid}.css" /><#break>
                <#case 'wintercamp'><@app.css href="public/css/parent/wintercamp${jskid}.css" /><#break>
                <#case 'mytrustee'><@app.css href="public/css/parent/mytrustee${jskid}.css" /><#break>
                <#case 'ustalk'><@app.css href="public/css/parent/ustalkpromot${jskid}.css" /><#break>
                <#case 'ustalkvipkid'><@app.css href="public/css/parent/ustalkpromotvipkid${jskid}.css" /><#break>
                <#case 'swiper3'><@app.css href="public/lib/swiper/css/swiper${jskid}.css" /><#break>
                <#case 'downloadGuide'><@app.css href="public/css/parent/downloadGuide${jskid}.css" /><#break>



                <#---------------------老师微信专区------------------->
                <#case 'weui'><@app.css href="public/css/weui/weui${jskid}.css" /><#break>
                <#case 'jquery-weui'><@app.css href="public/css/weui/jquery.weui${jskid}.css" /><#break>
                <#case 'widget'><@app.css href="public/css/teacher/widget${jskid}.css" /><#break>
                <#case 'picker'><@app.css href="public/lib/datetimepicker/default.css" /><#break>

                <#--作业相关-->
                <#case 'homework'><@app.css href="public/css/teacher/homework/math${jskid}.css" /><#break>

                <#--会场历程活动-->
                <#case 'dgmeetingNew'><@app.css href="public/css/teacher/activity/dgmeetingNew${jskid}.css" /><#break>
                <#case 'swiper'><@app.css href="public/lib/swiper/swiper2.css" /><#break>
                <#case 'swiperAnimate'><@app.css href="public/lib/swiper/animate.min.css" /><#break>

                <#--暑期预选开学要使用的教材-->
                <#case 'recommendbook'><@app.css href="public/css/teacher/activity/recommendbook${jskid}.css" /><#break>
                <#case 'chipsVoiceDemo'><@app.css href="public/css/parent/chipsVoiceDemo${jskid}.css" /><#break>
                <#--班级管理-->
                <#case 'clazzManage'><@app.css href="public/css/teacher/clazzmanage/clazz${jskid}.css" /><#break>

                <#--活动-->
                <#case 'teachersDay2016'><@app.css href="public/css/teacher/activity/teachersDay2016${jskid}.css" /><#break>
                <#--作业单-->
                <#case 'offlinehomework'><@app.css href="public/css/teacher/homework/offlinehomework${jskid}.css" /><#break>

                <#--薯条英语公众号-->
                <#case 'chips'><@app.css href="public/css/parent/chips${jskid}.css" /><#break>
                <#case 'chipsAd'><@app.css href="public/css/parent/chipsAd${jskid}.css" /><#break>
                <#case 'chipsAdNew'><@app.css href="public/css/parent/chipsAdNew${jskid}.css" /><#break>
                <#case 'chipsFormalAdvertisement'><@app.css href="public/css/parent/formal_advertisement${jskid}.css" /><#break>
                <#case 'chipsFormal3Be'><@app.css href="public/css/parent/chipsFormal3Be${jskid}.css" /><#break>
                <#case 'chipsFormalGroupBuy'><@app.css href="public/css/parent/chipsFormalGroupBuy${jskid}.css" /><#break>
                <#case 'chipsFormalGroupMessage'><@app.css href="public/css/parent/chipsFormalGroupMessage${jskid}.css" /><#break>
                <#case 'learning_duration'><@app.css href="public/css/parent/learning_duration${jskid}.css" /><#break>
                <#case 'ground_be_travel'><@app.css href="public/css/parent/ground_be_travel${jskid}.css" /><#break>
                <#case 'invite_award_activity'><@app.css href="public/css/parent/invite_award_activity${jskid}.css" /><#break>
                <#case 'invite_award_pic'><@app.css href="public/css/parent/invite_award_pic${jskid}.css" /><#break>
                <#case 'invite_personal_center'><@app.css href="public/css/parent/invite_personal_center${jskid}.css" /><#break>
                 <#case 'invite_my_income'><@app.css href="public/css/parent/invite_my_income${jskid}.css" /><#break>
                 <#case 'invite_personal_num'><@app.css href="public/css/parent/invite_personal_num${jskid}.css" /><#break>
                <#case 'waiting_for_you'><@app.css href="public/css/parent/temporary/waiting_for_you${jskid}.css" /><#break>
                <#case 'drawing_update'><@app.css href="public/css/parent/app/drawing_update${jskid}.css" /><#break>
                <#case 'friend_detaile'><@app.css href="public/css/parent/app/drawing_update${jskid}.css" /><#break>

                <#case 'chipsSuccess'><@app.css href="public/css/parent/chipsSuccess${jskid}.css" /><#break>
                <#case 'chipsAll'><@app.css href="public/css/parent/chipsAll${jskid}.css" /><#break>
                <#case 'chipsDemo'><@app.css href="public/css/parent/chipsDemo${jskid}.css" /><#break>
                <#case 'chipsTeacher'><@app.css href="public/css/parent/chipsTeacher${jskid}.css" /><#break>
                <#case 'chipsShareVideo'><@app.css href="public/css/parent/chipsShareVideo${jskid}.css" /><#break>
                <#case 'chipsShareRecord'><@app.css href="public/css/parent/chipsShareRecord${jskid}.css" /><#break>
                <#case 'chipsTodayStudy'><@app.css href="public/css/parent/chipsTodayStudy${jskid}.css" /><#break>
                <#case 'chipsActiveServicePreview'><@app.css href="public/css/parent/chipsActiveServicePreview${jskid}.css" /><#break>
                <#case 'chipsConfirmOrder'><@app.css href="public/css/parent/chipsConfirmOrder${jskid}.css" /><#break>
                <#case 'chipsSurvey'><@app.css href="public/css/parent/chipsSurvey${jskid}.css" /><#break>
                <#case 'chipsOralTest'><@app.css href="public/css/parent/oralTest${jskid}.css" /><#break>
                 <#case 'emailQuestionnaire'><@app.css href="public/css/parent/emailQuestionnaire${jskid}.css" /><#break>
                <#case 'chipsBeNormal'><@app.css href="public/css/parent/chipsBeNormal${jskid}.css" /><#break>


            </#switch>
        </#list>

        <#list js as name>
            <#switch name>
                <#case "jquery">
                    <@app.script href="public/lib/jquery/dist/jquery.min.js"/>
                    <#break>
                <#case "requirejs"><@app.script href="public/lib/requirejs/require.min.js"/><#break>
                <#case "fastClick"><@app.script href="public/js/fastclick${jskid}.js"/><#break>
                <#case "requirejsConfig"><@app.script href="public/js/requirejsConfig${jskid}.js"/><#break>
            </#switch>
        </#list>
    </#compress>
</#macro>
<#-- requirejs setting (global, alias) TODO -->
<#function buildRequirejsScriptUrl withOutExtUrl jskid>
    <#assign scriptUrl>
        <#--处理examCore.js-->
        <#if withOutExtUrl?index_of('/wechat/js/examCore') == -1 >
            <@app.link href=withOutExtUrl + jskid + '.js'/>
        <#else>
            <@app.link href=withOutExtUrl + jskid + '.js' cdnTypeFtl='skip'/>
        </#if>
    </#assign>

    <#if scriptUrl?index_of('?') == -1>
        <#return scriptUrl?replace(".js", "")>
    <#else>
        <#return scriptUrl>
    </#if>
</#function>

<#macro requirejs names=[] cdn=false>
    <script type="text/javascript">
        if(window.jQuery){
            define("jquery", function(){
                return jQuery;
            });
        }
        requirejs.config({
            paths :  {
                <#list names as name>
                    <#switch name>
                        <#case "requirejs">
                            "requirejs" : "${buildRequirejsScriptUrl("public/lib/requirejs/require.min", '')}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "jquery">
                            "jquery" : "${buildRequirejsScriptUrl("public/lib/jquery/dist/jquery.min", '')}"<#if name_has_next>,</#if>
                            <#break>
                         <#case "vue">
                            "vue" : "${buildRequirejsScriptUrl("public/lib/vue/vue.min", '')}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "knockout">
                            "knockout" : "${buildRequirejsScriptUrl("public/lib/knockout/dist/knockout","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "komapping">
                            "komapping" : "${buildRequirejsScriptUrl("public/lib/knockout.mapping/knockout-mapping","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "$17">
                            "$17" : "${buildRequirejsScriptUrl("public/js/$17",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "jbox">
                            "jbox" : "${buildRequirejsScriptUrl("public/lib/jbox/Source/jBox",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "flexslider">
                            "flexslider" : "${buildRequirejsScriptUrl("public/lib/jquery.flexslider/jquery.flexslider.min","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "login">
                            "login" : "${buildRequirejsScriptUrl("public/js/parent/login",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "loginVerify">
                            "loginVerify" : "${buildRequirejsScriptUrl("public/js/parent/loginVerify",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "mobileLogin">
                            "mobileLogin" : "${buildRequirejsScriptUrl("public/js/parent/mobileLogin",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "parentRegist">
                            "parentRegist" : "${buildRequirejsScriptUrl("public/js/parent/regist",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "smsBtn">
                            "smsBtn" : "${buildRequirejsScriptUrl("public/js/smsBtn",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "getVerifyCodeModal">
                            "getVerifyCodeModal" : "${buildRequirejsScriptUrl("public/js/getVerifyCodeModal","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "bindMobile">
                            "bindMobile" : "${buildRequirejsScriptUrl("public/js/parent/ucenter/bindMobile",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "ucenter">
                            "ucenter" : "${buildRequirejsScriptUrl("public/js/parent/ucenter/ucenter",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "bandingLoginVerify">
                            "bandingLoginVerify" : "${buildRequirejsScriptUrl("public/js/parent/ucenter/bandingLoginVerify",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "ucenterOrderList">
                            "ucenterOrderList" : "${buildRequirejsScriptUrl("public/js/parent/ucenter/ucenterOrderList",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "homework">
                            "homework" : "${buildRequirejsScriptUrl("public/js/parent/homework/homework",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "homeworkDetail">
                            "homeworkDetail" : "${buildRequirejsScriptUrl("public/js/parent/homework/homeworkDetail",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "wrongQuestionList">
                            "wrongQuestionList" : "${buildRequirejsScriptUrl("public/js/parent/homework/wrongQuestionList",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "smart">
                            "smart" : "${buildRequirejsScriptUrl("public/js/parent/homework/smart",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "integralOrder">
                            "integralOrder" : "${buildRequirejsScriptUrl("public/js/parent/integral/integralOrder",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "weeklyReport">
                            "weeklyReport" : "${buildRequirejsScriptUrl("public/js/parent/homework/weeklyReport",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "reportIndex">
                            "reportIndex" : "${buildRequirejsScriptUrl("public/js/parent/homework/reportindex",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "audio">
                            "audio" : "${buildRequirejsScriptUrl("public/js/parent/homework/audio",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "userpopup">
                            "userpopup" : "${buildRequirejsScriptUrl("public/js/parent/userpopup",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "menu">
                            "menu" : "${buildRequirejsScriptUrl("public/js/parent/menu",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "resetstudentpwd">
                            "resetstudentpwd" : "${buildRequirejsScriptUrl("public/js/parent/ucenter/resetstudentpwd",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "starreward">
                            "starreward" : "${buildRequirejsScriptUrl("public/js/parent/ucenter/starreward",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "parentWard">
                            "parentWard" : "${buildRequirejsScriptUrl("public/js/parent/reward/parentWard",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "wishNotices">
                            "wishNotices" : "${buildRequirejsScriptUrl("public/js/parent/reward/wishNotices",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "setmission">
                            "setmission" : "${buildRequirejsScriptUrl("public/js/parent/reward/setmission",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "errordetail">
                            "errordetail" : "${buildRequirejsScriptUrl("public/js/parent/homework/errordetail",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "examCore_new">
                            "examCore_new" :
                                    <#if ProductDevelopment.isDevEnv()>
                                            "${scheme}://www.test.17zuoye.net/resources/apps/hwh5/exam/wechat/js/examCore"
                                    <#else>
                                        "${buildRequirejsScriptUrl("/resources/apps/hwh5/exam/wechat/js/examCore",jskid)}"
                                    </#if>
                                    <#if name_has_next>,</#if>
                            <#break>
                        <#case "product">
                            "product" : "${buildRequirejsScriptUrl("public/js/parent/product/product",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "stem">
                            "stem" : "${buildRequirejsScriptUrl("public/js/parent/product/stem",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "pay">
                            "pay" : "${buildRequirejsScriptUrl("public/js/parent/wxpay/pay",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "wx">
                            <#if (scheme=='https')!false>
                                "wx": "${buildRequirejsScriptUrl("public/lib/weixin/jweixin-1.0.0-ssl", "")}"<#if name_has_next>,</#if>
                            <#else>
                                "wx": "${buildRequirejsScriptUrl("public/lib/weixin/jweixin-1.0.0", "")}"<#if name_has_next>,</#if>
                            </#if>
                            <#break>
                        <#case "logger">
                            "logger" : "${buildRequirejsScriptUrl("public/js/utils/logger",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "crossProjectShare">
                            "crossProjectShare": "${(scheme=='http')?string('http://cdn-cnc.17zuoye.cn/public/script/crossProjectShare.min','https://cdn-cncs.17zuoye.cn/public/script/crossProjectShare.min')}"<#if name_has_next>,</#if>
                            <#break>
						<#case "unitReport">
                            "unitReport" : "${buildRequirejsScriptUrl("public/js/parent/ureport/unitReport",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "uDetail">
                            "unitWrong" : "${buildRequirejsScriptUrl("public/js/parent/ureport/models/unitWrong",jskid)}",
                            "unitPrepare" : "${buildRequirejsScriptUrl("public/js/parent/ureport/models/unitPrepare",jskid)}",
                            "unitImportant" : "${buildRequirejsScriptUrl("public/js/parent/ureport/models/unitImportant",jskid)}",
                            "uDetail" : "${buildRequirejsScriptUrl("public/js/parent/ureport/detail",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "unitExample">
                            "unitExample" : "${buildRequirejsScriptUrl("public/js/parent/ureport/unitExample",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "wrongExercise">
                            "wrongExercise" : "${buildRequirejsScriptUrl("public/js/parent/ureport/wrongExercise",jskid)}"<#if name_has_next>,</#if>
                            <#break>                     
                        <#case "share">
                            "share" : "${buildRequirejsScriptUrl("public/js/utils/share",jskid)}"<#if name_has_next>,</#if>
                            <#break>
						<#case "reserve">
                            "reserve" : "${buildRequirejsScriptUrl("public/js/parent/trustee/reserve","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "signpic">
                            "signpic" : "${buildRequirejsScriptUrl("public/js/parent/trustee/signpic","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "trusteePay">
                            "trusteePay" : "${buildRequirejsScriptUrl("public/js/parent/trustee/trusteePay","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "activityList">
                            "activityList" : "${buildRequirejsScriptUrl("public/js/parent/activity/activityList",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "thanksgiving">
                            "thanksgiving" : "${buildRequirejsScriptUrl("public/js/parent/activity/thanksgiving",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "hotcss">
                            "hotcss" : "${buildRequirejsScriptUrl("public/js/parent/activity/hotcss",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "hot">
                            "hot" : "${buildRequirejsScriptUrl("public/js/parent/activity/hot",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "swiper">
                            "swiper" : "${buildRequirejsScriptUrl("public/lib/swiper/js/swiper",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "christmas">
                            "christmas" : "${buildRequirejsScriptUrl("public/js/parent/activity/christmas",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "globalmath">
                            "globalmath" : "${buildRequirejsScriptUrl("public/js/parent/product/globalmath",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "globalmathActivity">
                            "globalmathActivity" : "${buildRequirejsScriptUrl("public/js/parent/activity/globalmathActivity",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "confirmProduct">
                            "confirmProduct" : "${buildRequirejsScriptUrl("public/js/parent/wxpay/confirmProduct",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "msgcenter">
                            "msgcenter" : "${buildRequirejsScriptUrl("public/js/parent/ucenter/msgcenter",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "onlineqaPurchase">
                            "onlineqaPurchase" : "${buildRequirejsScriptUrl("public/js/parent/onlineqa/onlineqaPurchase",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "onLineqaHistory">
                            "onLineqaHistory" : "${buildRequirejsScriptUrl("public/js/parent/onlineqa/onLineqaHistory",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "onlineqaComment">
                            "onlineqaComment" : "${buildRequirejsScriptUrl("public/js/parent/onlineqa/onlineqaComment",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "onlineqaPay">
                            "onlineqaPay" : "${buildRequirejsScriptUrl("public/js/parent/onlineqa/onlineqaPay",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "bookregist">
                            "bookregist" : "${buildRequirejsScriptUrl("public/js/parent/trusteetwo/bookregist",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "activityintroducte">
                            "activityintroducte" : "${buildRequirejsScriptUrl("public/js/parent/trusteetwo/activityintroducte",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "trusteeclazzdesc">
                            "trusteeclazzdesc" : "${buildRequirejsScriptUrl("public/js/parent/trusteetwo/trusteeclazzdesc",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "booksuccess">
                            "booksuccess" : "${buildRequirejsScriptUrl("public/js/parent/trusteetwo/booksuccess",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "registtrustee">
                            "registtrustee" : "${buildRequirejsScriptUrl("public/js/parent/trusteetwo/registtrustee",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "paysuccess">
                            "paysuccess" : "${buildRequirejsScriptUrl("public/js/parent/trusteetwo/paysuccess",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "ocpresent">
                            "ocpresent" : "${buildRequirejsScriptUrl("public/js/parent/openclass/ocpresent",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "ocdetail">
                            "ocdetail" : "${buildRequirejsScriptUrl("public/js/parent/openclass/ocdetail",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "ocreserve">
                            "ocreserve" : "${buildRequirejsScriptUrl("public/js/parent/openclass/ocreserve",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "octeacher">
                            "octeacher" : "${buildRequirejsScriptUrl("public/js/parent/openclass/octeacher",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "ocresuccess">
                            "ocresuccess" : "${buildRequirejsScriptUrl("public/js/parent/openclass/ocresuccess",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "mytrusteeindex">
                            "mytrusteeindex" : "${buildRequirejsScriptUrl("public/js/parent/mytrustee/mytrusteeindex",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "branchdetail">
                            "branchdetail" : "${buildRequirejsScriptUrl("public/js/parent/mytrustee/branchdetail",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "mytrusteecreateorder">
                            "mytrusteecreateorder" : "${buildRequirejsScriptUrl("public/js/parent/mytrustee/mytrusteecreateorder",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "parentReceiveLoginReward">
                            "parentReceiveLoginReward" : "${buildRequirejsScriptUrl("public/js/parent/reward/receive_login_reward",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "branchalbum">
                            "branchalbum" : "${buildRequirejsScriptUrl("public/js/parent/mytrustee/branchalbum",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "trusteeclsconfirm">
                            "trusteeclsconfirm" : "${buildRequirejsScriptUrl("public/js/parent/mytrustee/trusteeclsconfirm",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "mytrusteeorderlist">
                            "mytrusteeorderlist" : "${buildRequirejsScriptUrl("public/js/parent/mytrustee/mytrusteeorderlist",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "mytrusteeorderdetail">
                            "mytrusteeorderdetail" : "${buildRequirejsScriptUrl("public/js/parent/mytrustee/mytrusteeorderdetail",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "mytrusteerefund">
                            "mytrusteerefund" : "${buildRequirejsScriptUrl("public/js/parent/mytrustee/mytrusteerefund",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "refundExplain">
                            "refundExplain" : "${buildRequirejsScriptUrl("public/js/parent/mytrustee/refundExplain",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "refundDetail">
                            "refundDetail" : "${buildRequirejsScriptUrl("public/js/parent/mytrustee/refundDetail",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "mytrusteeCountdown">
                            "mytrusteeCountdown" : "${buildRequirejsScriptUrl("public/js/parent/mytrustee/mytrusteeCountdown",jskid)}"<#if name_has_next>,</#if>
                            <#break>

                        /*专题区*/
                        <#case "reservation">
                            "reservation" : "${buildRequirejsScriptUrl("public/js/parent/trustee/reservation",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "wintercampskupay">
                            "wintercampskupay" : "${buildRequirejsScriptUrl("public/js/parent/trustee/wintercampskupay",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        /*夏令营*/
                        <#case "summercampskupay">
                            "summercampskupay" : "${buildRequirejsScriptUrl("public/js/parent/trustee/summercampskupay",jskid)}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsDemo1">
                            "chipsDemo1" : "${buildRequirejsScriptUrl("public/js/parent/chips/demo1","")}"<#if name_has_next>,</#if>
                            <#break>
                        /*薯条英语*/
                        <#case "chipsIndex">
                            "chipsIndex" : "${buildRequirejsScriptUrl("public/js/parent/chips/index","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsLogin">
                            "chipsLogin" : "${buildRequirejsScriptUrl("public/js/parent/chips/login","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsAd">
                            "chipsAd" : "${buildRequirejsScriptUrl("public/js/parent/chips/ad","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsInvite">
                            "chipsInvite" : "${buildRequirejsScriptUrl("public/js/parent/chips/invite","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsPaySuccess">
                            "chipsPaySuccess" : "${buildRequirejsScriptUrl("public/js/parent/chips/paysuccess","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsStudyList">
                            "chipsStudyList" : "${buildRequirejsScriptUrl("public/js/parent/chips/studylist","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsFollowWordList">
                            "chipsFollowWordList" : "${buildRequirejsScriptUrl("public/js/parent/chips/followwordlist","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsSummary">
                            "chipsSummary" : "${buildRequirejsScriptUrl("public/js/parent/chips/summary","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsSceneIntro">
                            "chipsSceneIntro" : "${buildRequirejsScriptUrl("public/js/parent/chips/sceneintro","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsTaskIntro">
                            "chipsTaskIntro" : "${buildRequirejsScriptUrl("public/js/parent/chips/taskintro","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsDialogue">
                            "chipsDialogue" : "${buildRequirejsScriptUrl("public/js/parent/chips/dialogue","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsCoupon">
                            "chipsCoupon" : "${buildRequirejsScriptUrl("public/js/parent/chips/coupon","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsBookCatalog">
                            "chipsBookCatalog" : "${buildRequirejsScriptUrl("public/js/parent/chips/bookcatalog","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsBookDrama">
                            "chipsBookDrama" : "${buildRequirejsScriptUrl("public/js/parent/chips/bookdrama","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsPlanmethod">
                            "chipsPlanmethod" : "${buildRequirejsScriptUrl("public/js/parent/chips/planmethod","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsReport">
                            "chipsReport" : "${buildRequirejsScriptUrl("public/js/parent/chips/report","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsReportV2">
                            "chipsReportV2" : "${buildRequirejsScriptUrl("public/js/parent/chips/reportV2","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsRanking">
                            "chipsRanking" : "${buildRequirejsScriptUrl("public/js/parent/chips/ranking","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsShareVideo">
                            "chipsShareVideo" : "${buildRequirejsScriptUrl("public/js/parent/chips/sharevideo","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsTravelCatalog">
                            "chipsTravelCatalog" : "${buildRequirejsScriptUrl("public/js/parent/chips/travelcatalog","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsMyRecommend">
                            "chipsMyRecommend" : "${buildRequirejsScriptUrl("public/js/parent/chips/myrecommend","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsAdNew">
                            "chipsAdNew" : "${buildRequirejsScriptUrl("public/js/parent/chips/adnew","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsFormalAdvertisement">
                            "chipsFormalAdvertisement" : "${buildRequirejsScriptUrl("public/js/parent/chips/formal_advertisement","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsFormal3Be">
                            "chipsFormal3Be" : "${buildRequirejsScriptUrl("public/js/parent/chips/chipsFormal3Be","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsFormalGroupBuy">
                            "chipsFormalGroupBuy" : "${buildRequirejsScriptUrl("public/js/parent/chips/chipsFormalGroupBuy","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsFormalGroupMessage">
                            "chipsFormalGroupMessage" : "${buildRequirejsScriptUrl("public/js/parent/chips/chipsFormalGroupMessage","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "learning_duration">
                            "learning_duration" : "${buildRequirejsScriptUrl("public/js/parent/chips/learning_duration","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "ground_be_travel">
                            "ground_be_travel" : "${buildRequirejsScriptUrl("public/js/parent/chips/ground_be_travel","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "invite_award_activity">
                            "invite_award_activity" : "${buildRequirejsScriptUrl("public/js/parent/chips/invite_award_activity","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "invite_award_pic">
                            "invite_award_pic" : "${buildRequirejsScriptUrl("public/js/parent/chips/invite_award_pic","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "invite_personal_center">
                            "invite_personal_center" : "${buildRequirejsScriptUrl("public/js/parent/chips/invite_personal_center","")}"<#if name_has_next>,</#if>
                            <#break>
                      <#case "invite_personal_num">
                            "invite_personal_num" : "${buildRequirejsScriptUrl("public/js/parent/chips/invite_personal_num","")}"<#if name_has_next>,</#if>
                            <#break>
                       <#case "invite_my_income">
                            "invite_my_income" : "${buildRequirejsScriptUrl("public/js/parent/chips/invite_my_income","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "drawing_update">
                            "drawing_update" : "${buildRequirejsScriptUrl("public/js/parent/chips/app/drawing_update","")}"<#if name_has_next>,</#if>
                            <#break>
                      <#case "friend_details">
                            "friend_details" : "${buildRequirejsScriptUrl("public/js/parent/chips/app/friend_details","")}"<#if name_has_next>,</#if>
                            <#break>

                        <#case "chipsShareRecord">
                            "chipsShareRecord" : "${buildRequirejsScriptUrl("public/js/parent/chips/sharerecord","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsTodayStudy">
                            "chipsTodayStudy" : "${buildRequirejsScriptUrl("public/js/parent/chips/todaystudy","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsConfirmOrder">
                            "chipsConfirmOrder" : "${buildRequirejsScriptUrl("public/js/parent/chips/confirmorder","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsSurvey">
                            "chipsSurvey" : "${buildRequirejsScriptUrl("public/js/parent/chips/chipsSurvey","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsOralTest">
                            "chipsOralTest" : "${buildRequirejsScriptUrl("public/js/parent/chips/oralTest","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "emailQuestionnaire">
                            "emailQuestionnaire" : "${buildRequirejsScriptUrl("public/js/parent/chips/emailQuestionnaire","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsTodayStudyNormal">
                            "chipsTodayStudyNormal" : "${buildRequirejsScriptUrl("public/js/parent/chips/todaystudynormal","")}"<#if name_has_next>,</#if>
                            <#break>

                        <#case "chipsRecommend">
                            "chipsRecommend" : "${buildRequirejsScriptUrl("public/js/parent/chips/recommend","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsDemo">
                            "chipsDemo" : "${buildRequirejsScriptUrl("public/js/parent/chips/demo","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsActiveServicePreview">
                            "chipsActiveServicePreview" : "${buildRequirejsScriptUrl("public/js/parent/chips/activeServicePreview","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsActiveServicePreviewV2">
                            "chipsActiveServicePreviewV2" : "${buildRequirejsScriptUrl("public/js/parent/chips/activeServicePreviewV2","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "otherServiceTypePreview">
                            "otherServiceTypePreview" : "${buildRequirejsScriptUrl("public/js/parent/chips/otherServiceTypePreview","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "otherServiceTypePreviewV2">
                            "otherServiceTypePreviewV2" : "${buildRequirejsScriptUrl("public/js/parent/chips/otherServiceTypePreviewV2","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "renewPreviewV1">
                            "renewPreviewV1" : "${buildRequirejsScriptUrl("public/js/parent/chips/renewPreviewV1","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "renewPreviewV2">
                            "renewPreviewV2" : "${buildRequirejsScriptUrl("public/js/parent/chips/renewPreviewV2","")}"<#if name_has_next>,</#if>
                            <#break>
                        <#case "chipsBeNormal">
                            "chipsBeNormal" : "${buildRequirejsScriptUrl("public/js/parent/chips/chipsBeNormal","")}"<#if name_has_next>,</#if>
                            <#break>
                    </#switch>
                </#list>
            },
            shim   : {
                "jquery": {
                    exports: "jquery"
                },
                "sammy" : {
                    deps: ["jquery"]
                },
                'jbox': {
                    deps: ['jquery']
                },
                "examCore" : {
                    deps : ['jquery','knockout']
                },
                "flexslider" : {
                    deps : ['jquery']
                },
                "hotcss" : {

                },
                "swiper" : {

                }
            },
            urlArgs: <#if (ProductDevelopment.isDevEnv())!false>"bust=" +  (new Date()).getTime()<#else>""</#if>
        });
    </script>
</#macro>

<#macro check_the_resources>
<#--
    现在有2个地方依赖 cdntype=skip，一个是 CdnBaseTag/CdnResourceUrlGenerator ，一个是 PageBlockContentGenerator
    如果网页加载了20秒，还没有可用的 jQuery 和 $17 (所有的JS/CSS都要放这两个文件后面!!!)， 则跳过cdn重新加载 。
    优先加载 jquery 和 core 用于cdn判断
-->
<script type="text/javascript">
    setTimeout(function () {
        var w = window, d = document;
        if (w.jQuery == undefined) {
            var idx = -1, keys =${json_encode(cdnDomainMapKeys)};
            if (!keys.length) {
                alert('CDN配置错误，请联系客服或技术');
                return;
            }
            for (var i = 0; i < keys.length; i++) {
                if (keys[i] == '${currentCdnType!''}') {
                    idx = i;
                    break;
                }
            }
            var nct = keys[(idx + 1) % keys.length], t = new Date();
            t.setTime(t.getTime() + (nct == 'skip' ? 7200 * 1000 : 86400 * 14 * 1000));
            d.cookie = "cdntype=" + nct + ";path=/;expires=" + t.toGMTString();
            setTimeout(function () {
                w.top.location.href = '/?_set_cdntype=' + nct;
            }, 500);
        }
    }, 20 * 1000);
</script>
</#macro>

<#macro site_traffic_analyzer>
<#--<script>
    (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
                (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
            m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
    })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

    /*根据用户类型区分 老师抽样率100% 家长抽样率10% */
    var _ga_trackingId = 'UA-38181315-2',_ga_sampleRate = 10;
    <#if currentUserType?? && currentUserType == 1>
        _ga_trackingId = 'UA-38181315-4';
        _ga_sampleRate = 100;
    </#if>
    ga('create', {
        trackingId: _ga_trackingId,
        cookieDomain: 'auto',
        sampleRate: _ga_sampleRate
    });
    ga('send', 'pageview');

</script>-->
</#macro>

