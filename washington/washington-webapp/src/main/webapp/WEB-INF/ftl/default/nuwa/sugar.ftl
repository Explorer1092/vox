<#macro capsule js=[] css=[] block=[] cdn=false><#compress>
    <#if (ProductDevelopment.isTestEnv() || ProductDevelopment.isStagingEnv() || ProductDevelopment.isProductionEnv())>
        <#assign jskid = ".min" />
    <#else>
        <#assign jskid = "" />
    </#if>
    <#if (ProductDevelopment.isTestEnv() || ProductDevelopment.isStagingEnv() || ProductDevelopment.isProductionEnv())>
        <#assign csskid = ".min" />
    <#else>
        <#assign csskid = "" />
    </#if>

    <#list js as name>
        <#switch name>
            <#case 'jquery'>
                <@app.script href="public/plugin/jquery/jquery-1.7.1.min.js" />
                <@app.script href="public/plugin/jquery-utils/jquery-utils.js" />
                <@app.script href="public/plugin/json2.js" />
                <#break>
            <#case 'core'>
                <@app.script href="public/script/$17${jskid!''}.js" />
                <@app.script href="public/script/$17Modules${jskid!''}.js" />
                <@app.script href="public/script/crossProjectShare${jskid!''}.js" />
                <#if (ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv())>
                    <script type="text/javascript">
                        window.env = "test";
                        $(function(){
                            <#--根据环境开启JS测试状态-->
                            $17.config.debug = true;
                        });
                    </script>
                <#elseif ProductDevelopment.isStagingEnv()>
                    <script type="text/javascript">window.env = 'staging'</script>
                <#elseif ProductDevelopment.isProductionEnv()>
                    <script type="text/javascript">window.env = 'prod'</script>
                </#if>
                <#break>
        </#switch>
    </#list>

    <#list css as name>
        <#switch name>
            <#case 'service'><@app.css href="public/skin/default/css/service${csskid!''}.css" /><#break>
            <#case 'serviceV2'><@app.css href="public/skin/default/images/serviceV2/skin${csskid!''}.css" /><#break>
            <#case 'specialskin'><@app.css href="public/skin/project/module/css/specialskin${csskid!''}.css" /><#break>
            <#case 'groupon'><@app.css href="public/skin/project/groupon/css/groupon${csskid!''}.css" /><#break>
            <#case "firstgrade"><@app.css href="public/skin/project/firstgrade/css/base${csskid!''}.css" /><#break />
            <#case "extension"><@app.css href="public/skin/project/extension/css/extension${csskid!''}.css" /><#break />

            <#case 'jquery.jcarousel'><@app.css href="public/plugin/jquery-jcarousel/skins/tango/skin.css" /><#break>

            <#case 'plugin.public'><@app.css href="public/app/default/css/public.css" /><#break>
            <#case 'plugin.frame'><@app.css href="public/app/default/css/frame.css" /><#break>
            <#case 'plugin.help'><@app.css href="public/skin/default/css/help${csskid!''}.css" /><#break>
            <#case 'plugin.cooperation'><@app.css href="public/skin/default/css/cooperation${csskid!''}.css" /><#break>
            <#case 'plugin.index3'><@app.css href="public/skin/default/css/indexv3${csskid!''}.css" /><#break>
            <#case 'plugin.index4'><@app.css href="public/skin/default/css/indexv4${csskid!''}.css" /><#break>
            <#case 'plugin.register'><@app.css href="public/skin/default/css/reg${csskid!''}.css" /><#break>
            <#case 'plugin.alert'><@app.css href="public/plugin/jquery-impromptu/jquery-impromptu.css" /><#break>
            <#case 'plugin.jqueryui'><@app.css href="public/plugin/jquery-ui/jquery-ui.css" /><#break>
            <#case 'plugin.jquery.qtip'><@app.css href="public/plugin/jquery-qtip2/jquery.qtip.css" /><#break>
            <#case 'plugin.mobile'><@app.css href="public/skin/mobile/pc/css/base${csskid!''}.css" /><#break>
            <#case 'plugin.datepicker'><@app.css href="public/plugin/jquery-datepicker/css/datepicker.blue${csskid!''}.css" /><#break>
            <#case 'plugin.jcarousel'><@app.css href="public/plugin/jquery-jcarousel/jcarousel/jcarousel.css" /><#break>
            <#case 'plugin.flexslider'><@app.css href="public/plugin/jquery.flexslider/flexslider.css" /><#break>
            <#case 'plugin.headfoot'><@app.css href="public/skin/default/images/headfoot/head-foot${csskid!''}.css" /><#break>
            <#case "plugin.checkboxtree"><@app.css href="public/plugin/jquery-checkboxtree/checkboxtree.css" /><#break />
            <#case 'plugin.so'><@app.css href="public/skin/common/css/SO.0.0.1${csskid!''}.css" /><#break>
            <#case "teacher.frame"><@app.css href="public/skin/teacher/css/frame${csskid!''}.css" /><#break />
            <#case "teacher.widget"><@app.css href="public/skin/teacher/css/widget${csskid!''}.css" /><#break />
            <#case "teacher.columns"><@app.css href="public/skin/teacher/css/columns${csskid!''}.css" /><#break />
            <#case "teacher.center"><@app.css href="public/skin/teacher/css/center${csskid!''}.css" /><#break />
            <#case "teacher.mobile"><@app.css href="public/skin/teacher/css/mobile${csskid!''}.css" /><#break />
            <#case "teacher.mobileBase"><@app.css href="public/skin/teacher/css/mobileBase${csskid!''}.css" /><#break />
            <#case "teacher.invite"><@app.css href="public/skin/teacher/css/invite${csskid!''}.css" /><#break />
            <#case "teacher.smartclazz"><@app.css href="public/skin/teacher/css/smartclazz${csskid!''}.css" /><#break />
            <#case "teacher.guideStep"><@app.css href="public/skin/teacher/css/guideStep${csskid!''}.css" /><#break />


            <#case "new_teacher.base"><@app.css href="public/skin/teacherv3/css/base${csskid!''}.css" /><#break />
            <#case "new_teacher.basev1"><@app.css href="public/skin/teacherv3/css/basev1${csskid!''}.css" /><#break />
            <#case "new_teacher.module"><@app.css href="public/skin/teacherv3/css/module${csskid!''}.css" /><#break />
            <#case "new_teacher.widget"><@app.css href="public/skin/teacherv3/css/widget${csskid!''}.css" /><#break />
            <#case "new_teacher.smartclazz"><@app.css href="/public/skin/teacherv3/css/smartclazz${csskid!''}.css" /><#break />
            <#case "new_teacher.quiz"><@app.css href="public/skin/teacherv3/css/quiz${csskid!''}.css" /><#break />
            <#case "new_teacher.message"><@app.css href="public/skin/teacher/css/systemmessage${csskid!''}.css" /><#break />
            <#case "new_teacher.guideStep"><#case "guideStep"><@app.css href="public/skin/teacherv3/css/guideStep${csskid!''}.css" /><#break />
            <#case "new_teacher.invite"><@app.css href="public/skin/teacherv3/css/invite${csskid!''}.css" /><#break />
            <#case "new_teacher.inviteupgrade"><@app.css href="public/skin/teacherv3/css/inviteupgrade${csskid!''}.css" /><#break />
            <#case "new_teacher.tts"><@app.css href="public/skin/teacherv3/css/tts${csskid!''}.css" /><#break />
            <#case "teacher.flower"><@app.css href="public/skin/teacherv3/css/flower${csskid!''}.css" /><#break />
            <#case "new_teacher.goal"><@app.css href="public/skin/teacherv3/css/goal${csskid!''}.css" /><#break />
            <#case "homework.pagination"><@app.css href="public/skin/teacherv3/css/pagination${csskid!''}.css" /><#break />
            <#case "new_teacher.cardlist"><@app.css href="public/skin/teacherv3/css/cardlist${csskid!''}.css" /><#break />
            <#case "new_teacher.poetryactivity"><@app.css href="public/skin/teacherv3/css/poetryactivity${csskid!''}.css" /><#break />
            <#case 'homeworkhistory.report'><@app.css href="public/skin/studentv3/css/homework-history${csskid!''}.css" /><#break>
            <#case 'homeworkhistory.studentwordteachandpracticedetail'><@app.css href="public/skin/studentv3/css/studentwordteachandpracticedetail${csskid!''}.css" /><#break>

            <#case "homeworkv3.ocrmental">
            <@app.css href="public/skin/teacherv3/css/ocr_content${csskid!''}.css" /><#break />
            <#case "homeworkv3.homework">
            <@app.css href="public/skin/teacherv3/css/homework${csskid!''}.css" /><#break />
            <#case "homeworkv3.wordrecognitionandreading">
            <@app.css href="public/skin/common/homework/recognitionreadingquestion${csskid!''}.css"/><#break/>
            <#case "homeworkv5.wordteachandpractice">
            <@app.css href="public/skin/teacherv3/css/wordteachandpractice${csskid!''}.css"/><#break/>
            <#case "homeworkv5.clazzwordteachandpracticedetail">
            <@app.css href="public/skin/teacherv3/css/clazzwordteachandpracticedetail${csskid!''}.css"/><#break/>
            <#case "homeworkv5.clazzwordteachmoduledetail">
            <@app.css href="public/skin/teacherv3/css/clazzwordteachmoduledetail${csskid!''}.css"/><#break/>
            <#case "homeworkv5.studentwordteachandpracticedetail">
            <@app.css href="public/skin/teacherv3/css/studentwordteachandpracticedetail${csskid!''}.css"/><#break/>
            <#case "homeworkv5.dictation">
            <@app.css href="public/skin/teacherv3/css/dictation${csskid!''}.css" /><#break />
            <#case "new_teacher.carts"><@app.css href="public/skin/teacherv3/css/carts${csskid!''}.css" /><#break />
            <#case "termreview.finalreview"><@app.css href="public/skin/teacherv3/css/finalreview${csskid!''}.css" /><#break />
            <#case "vacation.winterhomework"><@app.css href="public/skin/teacherv3/css/winterHomework${csskid!''}.css" /><#break />
            <#case "new_student.base"><@app.css href="public/skin/studentv3/css/base${csskid!''}.css" /><#break />
            <#case "new_student.module"><@app.css href="public/skin/studentv3/css/module${csskid!''}.css" /><#break />
            <#case "new_student.widget"><@app.css href="public/skin/studentv3/css/widget${csskid!''}.css" /><#break />
            <#case "new_student.widget.myclass"><@app.css href="public/skin/studentv3/css/myclass${csskid!''}.css" /><#break />
            <#case "new_student.mathhomework"><@app.css href="public/skin/studentv3/css/mathhomework${csskid!''}.css" /><#break />
            <#case "new_student.newexam"><@app.css href="public/skin/studentv3/css/newexam${csskid!''}.css" /><#break/>

            <#case "student.frame"><@app.css href="public/skin/student/css/frame${csskid!''}.css" /><#break />
            <#case "student.widget"><@app.css href="public/skin/student/css/widget${csskid!''}.css" /><#break />
            <#case "student.columns"><@app.css href="public/skin/student/css/columns${csskid!''}.css" /><#break />
            <#case "student.myclass"><@app.css href="public/skin/student/css/myclass${csskid!''}.css" /><#break />
            <#case "student.afenti"><@app.css href="public/skin/project/afenti/css/skin${csskid!''}.css" /><#break />
            <#case "student.talent"><@app.css href="public/skin/project/afenti/talent/skin1.0.1${csskid!''}.css" /><#break />
            <#case "student.parentReward"><@app.css href="public/skin/studentv3/css/parenten${csskid!''}.css" /><#break />
            <#case "student.koloalegend"><@app.css href="public/skin/studentv3/css/koloalegend${csskid!''}.css" /><#break />
            <#case "student.vacation"><@app.css href="public/skin/studentv3/css/vacation${csskid!''}.css" /><#break />

            <#case 'red.frame'><@app.css href="public/skin/red/css/frame${csskid!''}.css" /><#break>
            <#case 'red.inside'><@app.css href="public/skin/red/css/inside${csskid!''}.css" /><#break>
            <#case 'red.widget'><@app.css href="public/skin/red/css/widget${csskid!''}.css" /><#break>

            <#case 'common.practice'><@app.css href="public/skin/common/css/practice${csskid!''}.css" /><#break>
            <#case 'common.mathpractice'><@app.css href="public/skin/common/css/mathpractice${csskid!''}.css" /><#break>
            <#case 'common.practicenew'><@app.css href="public/skin/common/css/practicenew${csskid!''}.css" /><#break>
            <#case 'common.so'><@app.css href="public/skin/common/css/SO.0.0.1${csskid!''}.css" /><#break>

            <#case 'project.teacher'><@app.css href="public/skin/project/teacher/css/teacher${csskid!''}.css" /><#break>
            <#case 'project.midautumnfestival'><@app.css href="public/skin/project/midautumnfestival/css/skin${csskid!''}.css" /><#break>
            <#case 'project.install'><@app.css href="public/skin/project/install/css/skin${csskid!''}.css" /><#break>
            <#case 'project.exam'><@app.css href="public/skin/project/exam/css/skin${csskid!''}.css" /><#break>
            <#case 'project.network'><@app.css href="public/skin/project/network/css/skin${csskid!''}.css" /><#break>
            <#case 'project.guide'><@app.css href="public/skin/project/teacherStep/css/skin${csskid!''}.css" /><#break>
            <#case 'project.examactivity'><@app.css href="public/skin/project/examactivity/css/skin${csskid!''}.css" /><#break>
            <#case 'project.examspike'><@app.css href="public/skin/project/examspike/skin1.0.1${csskid!''}.css" /><#break>
            <#case 'project.talentspike'><@app.css href="public/skin/project/talentspike/skin1.0.1${csskid!''}.css" /><#break>
            <#case 'project.afentiexam'><@app.css href="public/skin/project/afenti/exam/skin${csskid!''}.css" /><#break>
            <#case 'project.afentibasic'><@app.css href="public/skin/project/afenti/basic/skin${csskid!''}.css" /><#break>
            <#case 'project.prizewinningessay'><@app.css href="public/skin/project/prizewinningessay/images/skin${csskid!''}.css" /><#break>
            <#case 'project.makelearningbeans'><@app.css href="public/skin/project/prizewinningessay/images_v1/skin${csskid!''}.css" /><#break>
            <#case 'project.lottery'><@app.css href="public/skin/project/lottery/images/lottery${csskid!''}.css" /><#break>
            <#case 'project.holidayhomework'><@app.css href="public/skin/project/holidayhomework/skin.css" /><#break>
            <#case 'project.coupon'><@app.css href="public/skin/project/coupon/images/skin${csskid!''}.css" /><#break>
            <#case 'project.summernotice'><@app.css href="public/skin/project/summernotice/skin${csskid!''}.css" /><#break>
            <#case 'project.skin'><@app.css href="public/skin/project/mingde/skin${csskid!''}.css" /><#break>
            <#case 'project.activityall'><@app.css href="public/skin/project/talent/css/skin${csskid!''}.css" /><#break>
            <#case 'project.beile'><@app.css href="public/skin/project/beile/css/style${csskid!''}.css" /><#break>
            <#case 'project.disney'><@app.css href="public/skin/project/disney/images/skin_1${csskid!''}.css" /><#break>
            <#case 'project.jingrui'><@app.css href="public/skin/project/jingrui/skin${csskid!''}.css" /><#break>
            <#case 'project.maxen'><@app.css href="public/skin/project/maxen/skin${csskid!''}.css" /><#break>
            <#case 'project.starbooks'><@app.css href="public/skin/project/starbooks/skin${csskid!''}.css" /><#break>
            <#case 'project.jiajiaobang'><@app.css href="public/skin/project/jiajiaobang/skin${csskid!''}.css" /><#break>
            <#case 'project.luckdraw'><@app.css href="public/skin/project/luckdraw/images/skin${csskid!''}.css" /><#break>
            <#case 'project.activity'><@app.css href="public/skin/project/activity/teacherlottery/skin${csskid!''}.css" /><#break>
            <#case 'travellottery'><@app.css href="public/skin/project/travellottery/skin${csskid!''}.css" /><#break>
            <#case 'redenvelope'><@app.css href="public/skin/project/redenvelope/css/schoolPackes${csskid!''}.css" /><#break>

            <#case 'paymentmobile.paymentskin'><@app.css href="public/skin/paymentmobile/css/paymentskin${csskid!''}.css" /><#break>

            <!-- 教研员 -->
            <#case 'rstaff.main'><@app.css href="public/skin/rstaff/skin/main${csskid!''}.css" /><#break>
            <#case 'rstaff.module'><@app.css href="public/skin/rstaff/skin/module${csskid!''}.css" /><#break>

            <!-- 贝乐页 -->
            <#case 'beile.common'><@app.css href="public/skin/student/beile/css/common${csskid!''}.css" /><#break>
            <#case 'beile.style'><@app.css href="public/skin/student/beile/css/style${csskid!''}.css" /><#break>

            <!--商城（礼物中心）-->
            <#case 'column'><@app.css href="public/skin/reward/css/column${csskid!''}.css" /><#break>
            <#case 'rewardBase'><@app.css href="public/skin/reward/css/rewardBase${csskid!''}.css" /><#break>


            <#case 'test.module'><@app.css href="public/skin/devtest/css/module${csskid!''}.css" /><#break>

            <!-- 学生移动端 -->
            <#case 'studentapp'><@app.css href="public/skin/mobile/pc/css/studentapp${csskid!''}.css" /><#break>

            <#-- 家长移动端 -->
            <#case 'parentMobile'><@app.css href="public/skin/parentMobile/css/skin${csskid!''}.css" /><#break>

            <#-- 主观作业上传 -->
            <#case 'subjHomeworkImgUpLoad'><@app.css href="public/skin/studentv3/css/uploadImage${csskid!''}.css" /><#break>

            <#-- 上传文件插件样式 -->
            <#case 'fileUpLoad'><@app.css href="public/skin/studentv3/css/webuploader${csskid!''}.css" /><#break>

            <#--模考V2.0样式-->
            <#case "newexamv2.questioncss">
                <#if (ProductDevelopment.isDevEnv())!false>
                <link rel="stylesheet" href="http://www.test.17zuoye.net/resources/apps/hwh5/homework-apps/pc-examination/v1.0.0/assets/css/skin.css" type="text/css" />
                <#else>
                    <@app.css href="/resources/apps/hwh5/homework-apps/pc-examination/v1.0.0/assets/css/skin.css" />
                </#if>
            <#break>
            <#case "newexamv2">
                <@app.css href="public/skin/newexamv2/css/histyreport${csskid!''}.css" />
            <#break>
             <#--模考V3.0样式-->
            <#case "newexamv3">
                <@app.css href="public/skin/newexamv3/css/historyreport${csskid!''}.css" />
                <@app.css href="public/skin/newexamv3/css/ztfxreport${csskid!''}.css" />
                <@app.css href="public/skin/newexamv3/css/detailsreport${csskid!''}.css" />
            <#break>
            <#case "plugin.venus">
                <#if (ProductDevelopment.isDevEnv())!false>
                <link rel="stylesheet" type="text/css" href="http://www.test.17zuoye.net/s17/lib/venus/2.15.21/css/venus.min.css"/>
                <#else>
                <link rel="stylesheet" type="text/css" href="<@app.link href="" />/s17/lib/venus/2.15.21/css/venus.min.css" />
                </#if>
                <@app.css href="public/skin/teacherv3/css/mental-reset${csskid!''}.css" />
            <#break>
            <#case "plugin.venus-pre">
                <#if (ProductDevelopment.isDevEnv())!false>
                <link rel="stylesheet" type="text/css" href="http://www.test.17zuoye.net/s17/lib/venus-pre/0.2.41/css/venus-pre.min.css"/>
                <#else>
                <link rel="stylesheet" type="text/css" href="<@app.link href="" />/s17/lib/venus-pre/0.2.41/css/venus-pre.min.css" />
                </#if>
                <@app.css href="public/skin/teacherv3/css/mental-reset${csskid!''}.css" />
            <#break>
            <#case "teachingresource.index">
            <@app.css href="public/skin/teacherv3/css/teachingresource-index${csskid!''}.css" />
            <@app.css href="public/skin/teacherv3/css/res-basicapp${csskid!''}.css" />
            <@app.css href="public/skin/teacherv3/css/res-levelreading${csskid!''}.css" />
            <@app.css href="public/skin/teacherv3/css/res-intelligentteaching${csskid!''}.css" />
            <@app.css href="public/skin/teacherv3/css/res-courseware${csskid!''}.css" />
            <@app.css href="public/skin/teacherv3/css/res-wordrecognitionandreading${csskid!''}.css" />
            <@app.css href="public/skin/teacherv3/css/res-keypoints${csskid!''}.css" />
            <@app.css href="public/skin/teacherv3/css/res-cuotibao${csskid!''}.css" />
            <@app.css href="public/skin/teacherv3/css/res-wordteachandpractice${csskid!''}.css" />
            <@app.css href="public/skin/teacherv3/css/res-naturalspelling${csskid!''}.css" />
            <@app.css href="public/skin/teacherv3/css/jquery-impromptu-reset${csskid!''}.css" />
            <#break>
            <#case "teachingresource.preview">
            <@app.css href="public/skin/teacherv3/css/preview${csskid!''}.css" />
            <#break>
            <#case "teachingresource.wordrecognitionandreadingdetail">
            <@app.css href="public/skin/teacherv3/css/wordrecognitionandreadingdetail${csskid!''}.css" />
            <#break>
            <!-- 故意触发一个解析错误，防止调用错了而不知道 -->
            <#default>${SHOULD_NOT_IN_THIS_CASE}<#break>
        </#switch>
    </#list>

    <#list js as name>
        <#switch name>
            <#case 'jquery'><#break>
            <#case 'core'><#break>
            <#case 'neoJquery'> <@app.script href="public/plugin/jquery/jquery-1.9.1.min.js" /> <#break>
            <#case 'crossProjectShare'><@app.script href="public/script/crossProjectShare${jskid!''}.js" /><#break>
            <#case 'ko'>
                <#if ProductDevelopment.isDevEnv()>
                    <@app.script href="public/plugin/knockoutjs-3.3.0/knockout.debug.js"/>
                    <@app.script href="public/plugin/knockoutjs-3.3.0/knockout.mapping-latest.debug.js"/>
                    <@app.script href="public/plugin/underscore1.8.2/underscore.js"/>
                    <#break>
                <#else>
                    <@app.script href="public/plugin/knockoutjs-3.3.0/knockout.js"/>
                    <@app.script href="public/plugin/knockoutjs-3.3.0/knockout.mapping-latest.js"/>
                    <@app.script href="public/plugin/underscore1.8.2/underscore-min.js"/>
                    <#break>
                </#if>
            <#case 'underscore'><@app.script href="public/plugin/underscore1.8.2/underscore-min.js"/><#break>
            <#case 'alert'><@app.script href="public/plugin/jquery-impromptu/jquery-impromptu${jskid!''}.js"/><#break>

            <#case 'toolkit'>
                <@app.script href="public/plugin/jquery-colorbox/jquery.colorbox.min.js" />
                <@app.script href="public/plugin/jquery-jqselectable/jquery.jqselectable.js" />
                <@app.script href="public/plugin/jquery-jcarousel/lib/jquery.jcarousel.js" />
                <#break>

            <#case 'teacher'>
                <@app.script href="public/script/teacherv3${jskid!''}.js" />
                <#break>
            <#case 'student'>
                <@app.script href="public/script/student${jskid!''}.js" />
                <#break>

            <#case 'loader'><@app.link href="public/plugin/loader${jskid!''}.js"/><#break>
            <#case 'colorbox'><@app.script href="public/plugin/jquery-colorbox/jquery.colorbox.min.js"/><#break>
            <#case 'datepicker'>
                <@app.script href="public/plugin/jquery-datepicker/jquery.datepicker.min.js"/>
                <script type="text/javascript">
                    $.datepicker.setDefaults({
                        monthNames  : ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
                        dayNamesMin : ["日", "一", "二", "三", "四", "五", "六"]
                    });
                </script>
                <#break>
            <#case 'datetimepicker'>
                <@app.script href="public/plugin/jquery-ui/jquery-ui.min.js"/>
                <@app.script href="public/plugin/jquery-ui/jquery-ui-slide.min.js"/>
                <@app.script href="public/plugin/jquery-ui/jquery-ui-timepicker-addon.js"/>
                <#break>
            <#case 'template'><@app.script href="public/plugin/template${jskid!''}.js"/><#break>
            <#case 'log'><@app.script href="public/plugin/log${jskid!''}.js"/><#break>
            <#case 'ZeroClipboard'><@app.script href="public/plugin/ZeroClipboard/ZeroClipboard.js"/><#break>
            <#case 'swfupload'><@app.script href="public/plugin/jquery-swfupload/swfupload.2.5.js"/><#break>
            <#case 'swfupload.handlers'><@app.script href="public/plugin/jquery-swfupload/handlers.js"/><#break>
            <#case 'resource.handlers'><@app.script href="public/plugin/jquery-swfupload/resource.handlers.js"/><#break>
            <#case 'swfupload.queue'><@app.script href="public/plugin/jquery-swfupload/swfupload.queue.js"/><#break>
            <#case 'jquery.flashswf'>
                <script type="text/javascript">
                    var ___client_setup_url = "<@app.client_setup_url />";
                    var ___isBrowserForTablet = <#if isBrowserForTablet>true;<#else>false;</#if>
                </script>
                <@app.script href="public/script/VoxExternalPlugin${jskid!''}.js"/>
                <@app.script href="public/plugin/jquery.flashswf${jskid!''}.js"/>
                <#break>
            <#case 'jqselectable'><@app.script href="public/plugin/jquery-jqselectable/jquery.jqselectable.js"/><#break>
            <#case 'fastLiveFilter'><@app.script href="public/plugin/jquery-fastLiveFilter/jquery.fastLiveFilter.js"/><#break>
            <#case 'qtip'><@app.script href="public/plugin/jquery-qtip2/jquery.qtip.min.js"/><#break>
            <#case 'jcarousel'><@app.script href="public/plugin/jquery-jcarousel/lib/jquery.jcarousel.js"/><#break>
            <#case 'stellar'><@app.script href="public/plugin/jquery.stellar.min.js"/><#break>
            <#case 'ebox'><@app.script href="public/plugin/jquery-eBox/js/jquery.eBox.js"/><#break>
            <#case 'flexslider'><@app.script href="public/plugin/jquery.flexslider/jquery.flexslider-min.js"/><#break>
            <#case 'voxLogs'><@app.script href="public/script/voxLogs${jskid!''}.js"/><#break>
            <#case 'DD_belatedPNG'>
                <!--[if IE 6]>
                    <@app.script href="public/plugin/DD_belatedPNG.js"/>
                    <script type="text/javascript">$(function(){ if(typeof DD_belatedPNG === "object"){DD_belatedPNG.fix('*');} });</script>
                <![endif]-->
                <#break>
            <#case 'DD_belatedPNG_class'>
                <!--[if IE 6]>
                    <@app.script href="public/plugin/DD_belatedPNG.js"/>
                    <script type="text/javascript">$(function(){ if(typeof DD_belatedPNG === "object"){ DD_belatedPNG.fix('.PNG_24'); } });</script>
                <![endif]-->
                <#break>
            <#case 'blockUI'><@app.script href="public/plugin/jquery-blockUI/jquery.blockUI-min.js"/><#break>
            <#case 'printArea'><@app.script href="public/plugin/jquery.PrintArea/jquery.PrintArea.js"/><#break>
            <#case 'ueditor'>
                <@app.script href="public/plugin/ueditor1_4_2/ueditor.config.js"/>
                <@app.script href="public/plugin/ueditor1_4_2/ueditor.all.min.js"/>
                <#break>
            <#case 'jquery-easy-pie-chart'>
                <!--[if lte IE 8]><@app.script href="public/plugin/jquery-easy-pie-chart/excanvas.js" /><![endif]-->
                <@app.script href="public/plugin/jquery-easy-pie-chart/jquery.easypiechart.min.js"/>
                <#break>

            <#case 'VoxExternalPlugin'><@app.script href="public/script/VoxExternalPlugin${jskid!''}.js" /><#break>
            <#case 'voxSpread'><@app.script href="public/script/voxSpread${jskid!''}.js" /><#break>
            <#case 'jmp3'><@app.script href="public/plugin/jquery-jmp3/jquery.jmp3.min.js" /><#break>
            <#case 'countdown'><@app.script href="public/plugin/jquery.countdown/jquery.countdown.min.js" /><#break>
            <#case 'jplayer'><@app.script href="public/plugin/jPlayer/jquery.jplayer.min.js"/><#break>
            <#case 'checkboxtree'><@app.script href="public/plugin/jquery-checkboxtree/jquery.checkboxtree.js"/><#break>
            <#case 'jquery.qrcode'><@app.script href="public/plugin/jquery-qrcode/jquery.qrcode.min.js" /><#break>
            <#case 'echarts'><@app.script href="public/plugin/echarts-2.1.10/dist/echarts-all.js" /><#break>
            <!--此echarts-3.4.0只包含了饼图和雷达图-->
            <#case 'echarts-3.4.0'><@app.script href="public/plugin/echarts-3.4.0/echarts.3.4.0.js" /><#break>
            <#case 'slick'><@app.script href="public/plugin/slick/slick.js" /><#break>


            <!--学生移动端-->
            <#case 'zepto'><@app.script href="public/plugin/zepto/1.1.6/zepto.min.js" /><#break>
            <#case 'studentapp'><@app.script href="public/skin/mobile/pc/js/studentapp.js" /><#break>

            <!--智慧课堂-->
            <#case 'smartclazz'><@app.script href="public/script/teacherv3/smartclazz/resource/english/index.js" /><#break>
            <#case 'teacherleague'><@app.script href="public/script/project/teacherleague.js" /><#break>
            <#case 'englishteacherleague'><@app.script href="public/script/project/englishteacherleague.js" /><#break>
            <#case 'chineseteacherleague'><@app.script href="public/script/project/chineseteacherleague.js" /><#break>
            <!-- 业务相关库 -->
            <#case "clazz.inviteteacherlist"><@app.script href="public/script/teacherv3/clazz/inviteteacherlist${jskid!''}.js"/><#break>
            <#case "clazz.handoverteacherlist"><@app.script href="public/script/teacherv3/clazz/handoverteacherlist${jskid!''}.js"/><#break>
            <#case "clazz.unprocessedapplication"><@app.script href="public/script/teacherv3/clazz/unprocessedapplication${jskid!''}.js"/><#break>
            <#case 'reading'><@app.script href="public/script/teacherv3/resource/reading${jskid!''}.js" /><#break>
            <#case "base"><@app.script href="public/script/teacherv3${jskid!''}.js"/><#break>
            <#case "teacher.index"><@app.script href="public/script/teacherv3/index${jskid!''}.js"/><#break>
            <#case "homework.english"><@app.script href="public/script/teacherv3/homework/english${jskid!''}.js"/><#break>
            <#case "homework.math"><@app.script href="public/script/teacherv3/homework/math${jskid!''}.js"/><#break>
            <#case "homework.list"><@app.script href="public/script/teacherv3/homework/list${jskid!''}.js"/><#break>
            <#case "homework"><@app.script href="public/script/teacherv3/homework${jskid!''}.js"/><#break>
            <#case "homeworkv3.homework">
                <@app.script href="public/script/utils/audioplayer${jskid!''}.js"/>
                <@app.script href="public/script/utils/remotequestion${jskid!''}.js"/>
                <@app.script href="public/script/compontents/homework/venusquestion${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/examexposurelog${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/homeworkcarts${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/kopagination${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/levels${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/book${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/objectivetabs${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/homeworktypetabs${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/tabdefault${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/exam${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/math/keypoints${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/math/calcintelligentteaching${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/homework${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/quiz${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/chinese/readrecitewithscore${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/chinese/newreadrecite${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/changebook${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/homeworkreview${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/intelligenceexam${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/fallibilityquestion${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/photoobjective${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/voiceobjective${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/math/mentalarithmetic${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/math/intelligentteaching${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/math/ocrmentalarithmetic${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/levelreadings${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/chinese/wordteachandpractice${jskid!''}.js"/>
                <#break>
            <#case "homeworkv3.chinese.compontents">
                <@app.script href="public/script/teacherv3/homeworkv3/chinese/wordrecognitionandreading${jskid!''}.js"/>
                <#break>
            <#case "chinese.recognitionreadingquestion">
                <@app.script href="public/script/compontents/homework/recognitionreadingquestion${jskid!''}.js"/>
                <#break>
            <#case "homeworkv5.homework">
                <@app.script href="public/script/utils/remotequestion${jskid!''}.js"/>
                <@app.script href="public/script/compontents/homework/venusquestion${jskid!''}.js"/>
                <@app.script href="public/script/utils/audioplayer${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/examexposurelog${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/homeworkcarts${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/kopagination${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/levels${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/book${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/objectivetabs${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/homeworktypetabs${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/tabdefault${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/exam${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/math/keypoints${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/math/calcintelligentteaching${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/homework${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/widgets/quiz${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/chinese/readrecitewithscore${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/changebook${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/widgets/homeworkreview${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/widgets/intelligenceexam${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/widgets/fallibilityquestion${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/widgets/photoobjective${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/widgets/voiceobjective${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/math/mentalarithmetic${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/math/intelligentteaching${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/math/ocrmentalarithmetic${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/levelreadings${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/chinese/wordteachandpractice${jskid!''}.js"/>
                <#break>
            <#case "plugin.venus">
                <#if (ProductDevelopment.isDevEnv())!false>
                    <script type="text/javascript" src="http://www.test.17zuoye.net/s17/lib/venus/2.15.21/venus.min.js"></script>
                <#else>
                    <script type="text/javascript" src="<@app.link href="" />/s17/lib/venus/2.15.21/venus.min.js"></script>
                </#if>
                    <script type="text/javascript">
                        var mjConfig = Venus.getMathJaxConfig()||{};
                        var head = document.getElementsByTagName('head')[0];
                        var script = document.createElement('script');
                        script.type = mjConfig.type;
                        script[mjConfig.attr]=mjConfig.value;
                        head.appendChild(script);
                    </script>
                <#if (ProductDevelopment.isDevEnv())!false>
                    <script type="text/javascript" src="http://cdn-cnc.17zuoye.cn/resources/apps/hwh5/mathjax/2.6-latest/MathJax.js"></script>
                    <script type="text/javascript" src="http://cdn-cnc.17zuoye.cn/resources/apps/hwh5/mathjax/contrib/forminput/forminput.js"></script>
                    <script type="text/javascript" src="http://cdn-static-shared.test.17zuoye.net/s17/lib/vue/2.2.5/vue.min.js"></script>
                    <script type="text/javascript" src="http://cdn-static-shared.test.17zuoye.net/s17/lib/vue/plugin/vuex/2.2.1/vuex.min.js"></script>
                    <script type="text/javascript" src="http://cdn-static-shared.test.17zuoye.net/s17/lib/polyfill/polyfill.min.js"></script>
                <#else>
                    <script src="<@app.link href="" />/resources/apps/hwh5/mathjax/2.6-latest/MathJax.js"></script>
                    <script src="<@app.link href="" />/resources/apps/hwh5/mathjax/contrib/forminput/forminput.js"></script>
                    <script type="text/javascript" src="<@app.link href="" />/s17/lib/vue/2.2.5/vue.min.js"></script>
                    <script type="text/javascript" src="<@app.link href="" />/s17/lib/vue/plugin/vuex/2.2.1/vuex.min.js"></script>
                    <script type="text/javascript" src="<@app.link href="" />/s17/lib/polyfill/polyfill.min.js"></script>
                </#if>
                <#break>
            <#case "plugin.venus-pre">
                <#if (ProductDevelopment.isDevEnv())!false>
                    <script type="text/javascript" src="http://www.test.17zuoye.net/s17/lib/venus-pre/0.2.41/venus-pre.min.js"></script>
                <#else>
                    <script type="text/javascript" src="<@app.link href="" />/s17/lib/venus-pre/0.2.41/venus-pre.min.js"></script>
                </#if>
                    <script type="text/javascript">
                        var mjConfig = Venus.getMathJaxConfig()||{};
                        var head = document.getElementsByTagName('head')[0];
                        var script = document.createElement('script');
                        script.type = mjConfig.type;
                        script[mjConfig.attr]=mjConfig.value;
                        head.appendChild(script);
                    </script>
                <#if (ProductDevelopment.isDevEnv())!false>
                    <script type="text/javascript" src="http://cdn-cnc.17zuoye.cn/s17/lib/MathJax/2.7/MathJax.js"></script>
                    <script type="text/javascript" src="http://cdn-cnc.17zuoye.cn/s17/lib/MathJax/forminput/forminput.js"></script>
                    <script type="text/javascript" src="http://cdn-static-shared.test.17zuoye.net/s17/lib/vue/2.2.5/vue.min.js"></script>
                    <script type="text/javascript" src="http://cdn-static-shared.test.17zuoye.net/s17/lib/vue/plugin/vuex/2.3.1/seed.min.js"></script>
                    <script type="text/javascript" src="http://cdn-static-shared.test.17zuoye.net/s17/lib/polyfill/polyfill.min.js"></script>
                <#else>
                    <script src="<@app.link href="" />/s17/lib/MathJax/2.7/MathJax.js"></script>
                    <script src="<@app.link href="" />/s17/lib/MathJax/forminput/forminput.js"></script>
                    <script type="text/javascript" src="<@app.link href="" />/s17/lib/vue/2.2.5/vue.min.js"></script>
                    <script type="text/javascript" src="<@app.link href="" />/s17/lib/vue/plugin/vuex/2.3.1/seed.min.js"></script>
                    <script type="text/javascript" src="<@app.link href="" />/s17/lib/polyfill/polyfill.min.js"></script>
                </#if>
                <#break>
            <#case "vue">
                <#if (ProductDevelopment.isDevEnv())!false>
                    <script type="text/javascript" src="http://cdn-static-shared.test.17zuoye.net/s17/lib/vue/2.2.5/vue.min.js"></script>
                    <script type="text/javascript" src="http://cdn-static-shared.test.17zuoye.net/s17/lib/polyfill/polyfill.min.js"></script>
                <#else>
                    <script type="text/javascript" src="<@app.link href="" />/s17/lib/vue/2.2.5/vue.min.js"></script>
                    <script type="text/javascript" src="<@app.link href="" />/s17/lib/polyfill/polyfill.min.js"></script>
                </#if>
                <#break>
            <#case "vuex">
                <#if (ProductDevelopment.isDevEnv())!false>
                    <script type="text/javascript" src="http://cdn-static-shared.test.17zuoye.net/s17/lib/vue/plugin/vuex/2.3.1/seed.min.js"></script>
                <#else>
                    <script type="text/javascript" src="<@app.link href="" />/s17/lib/vue/plugin/vuex/2.3.1/seed.min.js"></script>
                </#if>
                <#break>
            <#case "homeworkv3.newexam">
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/kopagination${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/changebook${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/levels${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/newexam/independent${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/newexam/newexamreview${jskid!''}.js"/>
                <#break>
            <#case "homeworkv3.newexamreport">
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/kopagination${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/newexam/independentreport${jskid!''}.js"/>
                <#break>
            <#case "homeworkv3.english">
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/examexposurelog${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/homeworkcarts${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/kopagination${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/simpleslider${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/levels${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/english/book${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/objectivetabs${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/homeworktypetabs${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/tabdefault${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/english/basicapp${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/english/exam${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/quiz${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/changebook${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/homeworkreview${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/fallibilityquestion${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/intelligenceexam${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/english/naturalspelling${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/english/previewdubbingdetail${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/english/dubbingwithscore${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/levelreadings${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/english/intelligentteachingv2${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/english/homework${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/english/oralcommunication${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/english/dictation${jskid!''}.js"/>
                <#break >
            <#case "homeworkv5.english">
                <@app.script href="public/script/utils/remotequestion${jskid!''}.js"/>
                <@app.script href="public/script/compontents/homework/venusquestion${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/examexposurelog${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/homeworkcarts${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/kopagination${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/simpleslider${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/levels${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/english/book${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/objectivetabs${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/homeworktypetabs${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/tabdefault${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/english/basicapp${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/english/exam${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/widgets/quiz${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/changebook${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/widgets/homeworkreview${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/widgets/fallibilityquestion${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/widgets/intelligenceexam${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/english/naturalspelling${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/english/previewdubbingdetail${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/english/dubbingwithscore${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/levelreadings${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/english/intelligentteachingv2${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/english/homework${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/english/oralcommunication${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv5/english/dictation${jskid!''}.js"/>
                <#break >
            <#case "termreview">
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/changebook${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/simpleslider${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/kopagination${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/termreview/termexam${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/termreview/termbasicword${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/termreview/math/intelligentteaching${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/tabdefault${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/termreview/termtabs${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/termreview/levelsandbook${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/termreview/termcarts${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/termreview/index${jskid!''}.js"/>
                <#break >
            <#case "basicreviewreport">
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/simpleslider${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/termreview/basicreviewreport${jskid!''}.js"/>
                <#break>
            <#case "vacation.index">
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/changebook${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/simpleslider${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/vacation/index${jskid!''}.js"/>
                <#break>
            <#case "vacationhistory.list">
                <@app.script href="public/script/teacherv3/vacationreport/list${jskid!''}.js"/>
                <#break>
            <#case "vacationhistory.clazzreport">
                <@app.script href="public/script/teacherv3/vacationreport/clazzreport${jskid!''}.js"/>
                <#break>
            <#case "vacationhistory.studentweekdetail">
                <@app.script href="public/script/teacherv3/vacationreport/studentweekdetail${jskid!''}.js"/>
                <#break>
            <#case "vacationhistory.studentdaydetail">
                <@app.script href="public/script/teacherv3/vacationreport/studentdaydetail${jskid!''}.js"/>
                <#break>
            <#case "homeworkv3.list">
                <@app.script href="public/script/teacherv3/homeworkhistoryv3/list${jskid!''}.js"/>
                <#break>
            <#case "homeworkv3.earlylist">
                <@app.script href="public/script/teacherv3/homeworkhistoryv3/earlylist${jskid!''}.js"/>
                <#break>
            <#case "homeworkv3.clazzreport">
                <@app.script href="public/script/utils/remotequestion${jskid!''}.js"/>
                <@app.script href="public/script/compontents/homework/venusquestion${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkhistoryv3/clazzreport${jskid!''}.js"/>
                <#break>
            <#case "util.hardcodeurl">
                <@app.script href="public/script/utils/index${jskid!''}.js"/>
                <#break>
            <#case "homeworkv3.clazzreportdetail">
                <@app.script href="public/script/utils/index${jskid!''}.js"/>
                <@app.script href="public/script/utils/remotequestion${jskid!''}.js"/>
                <@app.script href="public/script/compontents/homework/venusquestion${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkhistoryv3/clazzreportdetail${jskid!''}.js"/>
                <#break>
            <#case "homeworkv3.studentreportdetail">
                <@app.script href="public/script/utils/index${jskid!''}.js"/>
                <@app.script href="public/script/utils/remotequestion${jskid!''}.js"/>
                <@app.script href="public/script/compontents/homework/venusquestion${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkhistoryv3/studentreportdetail${jskid!''}.js"/>
                <#break>
            <#case "homeworkv5.clazzreportdetail">
                <@app.script href="public/script/utils/remotequestion${jskid!''}.js"/>
                <@app.script href="public/script/compontents/homework/venusquestion${jskid!''}.js"/>
                <@app.script href="public/script/utils/index${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkhistoryv5/clazzreportdetail${jskid!''}.js"/>
                <#break>
            <#case "homeworkv5.studentreportdetail">
                <@app.script href="public/script/utils/remotequestion${jskid!''}.js"/>
                <@app.script href="public/script/compontents/homework/venusquestion${jskid!''}.js"/>
                <@app.script href="public/script/utils/index${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkhistoryv5/studentreportdetail${jskid!''}.js"/>
                <#break>
            <#case "homeworkv5.singleoralcommunicationdetail">
                <@app.script href="public/script/utils/index${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkhistoryv5/singleoralcommunicationdetail${jskid!''}.js"/>
                <#break>
            <#case "homeworkv5.clazzwordteachmoduledetail">
                <@app.script href="public/script/utils/remotequestion${jskid!''}.js"/>
                <@app.script href="public/script/compontents/homework/venusquestion${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkhistoryv5/clazzwordteachmoduledetail${jskid!''}.js"/>
                <#break>
            <#case "teacherreport.readingdetail">
                <@app.script href="public/script/utils/remotequestion${jskid!''}.js"/>
                <@app.script href="public/script/compontents/homework/venusquestion${jskid!''}.js"/>
                <#break>
            <#case "homeworkv3.studentexam"><@app.script href="public/script/studentv3/examination${jskid!''}.js"/><#break>
            <#case "clazzresource.englishindex">
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/kopagination${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/levels${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/changebook${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/english/book${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/tabdefault${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/clazzresource/objectivetabs${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/clazzresource/basicapp${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/clazzresource/levelreadings${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/clazzresource/index${jskid!''}.js"/>
                <#break>
            <#case "goal.termreport">
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/simpleslider${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/goal/termreportv2${jskid!''}.js" />
                <#break>
            <#case "goal.unitreport">
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/changebook${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/homeworkv3/widgets/simpleslider${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/goal/unitreport${jskid!''}.js"/>
                <#break>
            <#case "newexamv2.history">
            <@app.script href="public/script/teacherv3/newexamv2/viewbypaper${jskid!''}.js"/>
            <@app.script href="public/script/teacherv3/newexamv2/viewbystudents${jskid!''}.js"/>
            <@app.script href="public/script/teacherv3/newexamv2/analyzepaper${jskid!''}.js"/>
            <@app.script href="public/script/teacherv3/newexamv2/history${jskid!''}.js"/>
            <#break>
            <#case "newexamv2.viewstudent">
                <@app.script href="public/script/newexamv2/viewstudent${jskid!''}.js"/>
                <#break>
            <#case "newexamv3.viewstudent">
                <@app.script href="public/script/newexamv3/examutils${jskid!''}.js"/>
                <@app.script href="public/script/newexamv3/viewstudent${jskid!''}.js"/>
                <@app.script href="public/script/newexamv3/detailsreport${jskid!''}.js"/>
                <#break>
            <#case "newexamv3.assign">
                <@app.script href="public/script/teacherv3/comblock/assignblock${jskid!''}.js"/>
                <@app.script href="public/script/teacherv3/newexamv3/assign${jskid!''}.js"/>
                <#break>
            <#case "clazz.clazzlist"><@app.script href="public/script/teacherv3/clazz/clazzlist${jskid!''}.js"/><#break>
            <#case "clazz.clazzedit"><@app.script href="public/script/teacherv3/clazz/clazzedit${jskid!''}.js"/><#break>
            <#case "clazz.clazzdetail"><@app.script href="public/script/teacherv3/clazz/clazzdetail${jskid!''}.js"/><#break>
            <#case "clazz.createclazz"><@app.script href="public/script/teacherv3/clazz/createclazz${jskid!''}.js"/><#break>
            <#case "clazz.inviteteacherlist"><@app.script href="public/script/teacherv3/clazz/inviteteacherlist${jskid!''}.js"/><#break>
            <#case "clazz.handoverteacherlist"><@app.script href="public/script/teacherv3/clazz/handoverteacherlist${jskid!''}.js"/><#break>
            <#case "clazz.unprocessedapplication"><@app.script href="public/script/teacherv3/clazz/unprocessedapplication${jskid!''}.js"/><#break>
            <#case "seajs"><@app.script href="public/plugin/seajs-3.0.0/dist/sea.js"/><#break>
            <#case "requirejs"><@app.script href="public/plugin/requirejs/require.2.1.9.min.js"/><#break>
            <#case "parentMobileRequireConfig"><@app.script href="public/script/parentMobile/requireConfig${jskid!''}.js"/><#break>
            <#case "targetDensitydpi"><@app.script href="public/script/parentMobile/targetDensitydpi${jskid!''}.js"/><#break>
            <#case "fastClick"><@app.script href="public/plugin/fastClick.js"/><#break>
            <#case "learningcenterad"><@app.script href="public/script/studentv3/learningcenterad${jskid!''}.js"/><#break>
            <#case "newstudapprove"><@app.script href="public/script/project/newstudapprove${jskid!''}.js"/><#break>
            <#case "newexamV2">
                <#if (ProductDevelopment.isDevEnv())!false>
                    <script type="text/javascript" src="http://www.test.17zuoye.net/resources/apps/hwh5/mathjax/2.6-latest/MathJax.js?delayStartupUntil=configured"></script>
                    <script type="text/javascript" src="http://www.test.17zuoye.net/resources/apps/hwh5/mathjax/contrib/forminput/forminput.js"></script>
                    <script type="text/javascript" src="http://www.test.17zuoye.net/resources/apps/hwh5/lib/systemjs-v0.20.18-production.js" ></script>
                    <script type="text/javascript" src="http://www.test.17zuoye.net//resources/apps/hwh5/homework-apps/pc-examination/v1.0.0/student-exam-pc/main.js" ></script>
                    <script type="text/javascript" src="http://www.test.17zuoye.net/resources/apps/hwh5/lib/whatwg-fetch.min.js" ></script>
                <#else>
                    <script type="text/javascript" src="<@app.link href="" />/resources/apps/hwh5/mathjax/2.6-latest/MathJax.js?delayStartupUntil=configured" ></script>
                    <script type="text/javascript" src="<@app.link href="" />/resources/apps/hwh5/mathjax/contrib/forminput/forminput.js" ></script>
                    <script type="text/javascript" src="<@app.link href="/resources/apps/hwh5/lib/systemjs-v0.20.18-production.js" />" ></script>
                    <script type="text/javascript" src="<@app.link href="/resources/apps/hwh5/homework-apps/pc-examination/v1.0.0/student-exam-pc/main.js" />" ></script>
                    <script type="text/javascript" src="<@app.link href="/resources/apps/hwh5/lib/whatwg-fetch.min.js" />" ></script>
                </#if>
            <#break>
            <#case "plugin.newexamv3">
                <#if (ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv())>
                    <@app.script href="public/plugin/freya/freya.umd.js"/>
                <#else >
                    <@app.script href="public/plugin/freya/freya.umd.min.js"/>
                </#if>
            <#break>
            <#case "newexamv3.history">
            <@app.script href="public/script/newexamv3/examutils${jskid!''}.js"/>
            <@app.script href="public/script/teacherv3/newexamv3/viewbypaper${jskid!''}.js"/>
            <@app.script href="public/script/teacherv3/newexamv3/viewbystudents${jskid!''}.js"/>
            <@app.script href="public/script/teacherv3/newexamv3/analyzepaper${jskid!''}.js"/>
            <@app.script href="public/script/teacherv3/newexamv3/ztfxreport${jskid!''}.js"/>
            <@app.script href="public/script/teacherv3/newexamv3/history${jskid!''}.js"/>
            <#break>
            <#case "newexamv3.previewpaper">
            <@app.script href="public/script/newexamv3/previewpaper${jskid!''}.js"/>
            <#break>
            <#case "newexamv3.list">
            <@app.script href="public/script/newexamv3/list${jskid!''}.js"/>
            <#break>
            <#--数学新版口算应用2015-12-25版-->
            <#case "mathoral">
                <#--es5-support.js 处理兼容IE-->
                <@app.script href="resources/apps/hwh5/exam/lib/es5-support.js"/>
                <#if (ProductDevelopment.isDevEnv()!false)>
                <#-- ultimate-math-oral-main.js  washington本地没有该文件 所以本地开发直接使用test上的 -->
                <script type="text/javascript" src="<@app.link href="resources/apps/hwh5/homework-pc/ultimate-math-oral/js/ultimate-math-oral-main.js"/>"></script>
                <#else>
                <#-- ultimate-math-oral-main 不能跨域，所以强制跳过cdn -->
                <script type="text/javascript" src="<@app.link href="resources/apps/hwh5/homework-pc/ultimate-math-oral/js/ultimate-math-oral-main${jskid!''}.js" cdnTypeFtl="skip" />"></script>
                </#if>
            <#break>

            <#--题库相关-->
            <#case "examCore">
                <#--es5-support.js 处理兼容IE-->
                <@app.script href="resources/apps/hwh5/exam/lib/es5-support.js"/>
                <#if (ProductDevelopment.isDevEnv()!false)>
                    <#-- examCore.js  washington本地没有该文件 所以本地开发直接使用test上的 -->
                    <script type="text/javascript" src="<@app.link href="resources/apps/hwh5/exam/pc/examCore.js"/>"></script>
                <#else>
                    <#-- examCore 不能跨域，所以强制跳过cdn -->
                    <script type="text/javascript" src="<@app.link href="resources/apps/hwh5/exam/pc/examCore${jskid!''}.js" cdnTypeFtl="skip" />"></script>
                </#if>
            <#break>

            <#--数学作业新形式下引用与题库相关的js-->
            <#case "homework2nd">
            <#--es5-support.js 处理兼容IE-->
                <@app.script href="resources/apps/hwh5/exam/lib/es5-support.js"/>
                <script type="text/javascript" src="<@app.link href="/resources/apps/hwh5/exam/homeworkv2/exam/homework-2nd${jskid!''}.js" />" ></script>
            <#break>
            <#case "studentv3.homeworklist">
                <script type="text/javascript" src="<@app.link href="/resources/apps/hwh5/exam/homeworkv2/list/homework-list${jskid!''}.js" />"></script>
            <#break>
            <#case "studentv3.vacation">
                <script type="text/javascript" src="<@app.link href="resources/apps/hwh5/new-homework-pc/vacation-homework-2016-primary-pc/startup${jskid!''}.js" />"></script>
            <#break>
            <#--统考考试js-->
            <#case "newexam">
                <#--es5-support.js 处理兼容IE-->
                <@app.script href="resources/apps/hwh5/exam/lib/es5-support.js"/>
                <#if (ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv())>
                <script type="text/javascript" src="<@app.link href="/resources/apps/hwh5/exam/examination/examination.js" />" ></script>
                <#else>
                <script type="text/javascript" src="<@app.link href="/resources/apps/hwh5/exam/examination/examination${jskid!''}.js" />" ></script>
                </#if>
            <#break>
            <#case "newexam.detail">
                <script type="text/javascript" src="<@app.link href="public/script/teacherv3/newexam/report/detail${jskid!''}.js" />" ></script>
            <#break>

            <#case "newexamindependent.detail">
                <script type="text/javascript" src="<@app.link href="public/script/teacherv3/newexam/report/independentdetail${jskid!''}.js" />" ></script>
            <#break >


            <#-- 学生作业报告 -->
            <#case "studentreport.report"><@app.script href="public/script/studentv3/homeworkreport${jskid!''}.js"/><#break>
            <#case "studentreport.detail">
                <@app.script href="public/script/utils/remotequestion${jskid!''}.js"/>
                <@app.script href="public/script/compontents/homework/venusquestion${jskid!''}.js"/>
                <@app.script href="public/script/utils/index${jskid!''}.js"/>
                <@app.script href="public/script/studentv3/homeworkdetail${jskid!''}.js"/>
                <#break>
            <#case "studentreport.list"><@app.script href="public/script/studentv3/homeworklist${jskid!''}.js"/><#break>
            <#case "studentreport.earlylist"><@app.script href="public/script/studentv3/earlyhomeworklist${jskid!''}.js"/><#break>
            <#case "studentreport.detailv5">
                <@app.script href="public/script/utils/remotequestion${jskid!''}.js"/>
                <@app.script href="public/script/compontents/homework/venusquestion${jskid!''}.js"/>
                <@app.script href="public/script/utils/index${jskid!''}.js"/>
                <@app.script href="public/script/studentv3/learning/history/newhomeworkv5/homeworkdetail${jskid!''}.js"/>
                <#break>
            <#case "studentreport.singleoralcommunicationdetail">
                <@app.script href="public/script/utils/index${jskid!''}.js"/>
                <@app.script href="public/script/studentv3/learning/history/newhomeworkv5/singleoralcommunicationdetail${jskid!''}.js"/>
                <#break>
            <#case "studentreport.readingdetail">
                <@app.script href="public/script/utils/remotequestion${jskid!''}.js"/>
                <@app.script href="public/script/compontents/homework/venusquestion${jskid!''}.js"/>
                <#break>
            <#--阿分题相关-->
            <#case "afenti">
                <#if (ProductDevelopment.isDevEnv()!false)>
                    <script type="text/javascript" src="<@app.link href="resources/apps/hwh5/exam/afenti/src/afenti_facade.js"/>"/></script>
                <#else>
                    <#-- afenti_facade 不能跨域，所以强制跳过cdn -->
                    <script type="text/javascript" src="<@app.link href="resources/apps/hwh5/exam/afenti/src/afenti_facade${jskid!''}.js" cdnTypeFtl="skip"/>"></script>
                </#if>
            <#break>

            <#-- 家长客户端 -->
            <#case "mobileParentCommon"><@app.script href="public/script/parentMobile/common${jskid!''}.js"/><#break>
            <#case "DateExtend"><@app.script href="public/script/parentMobile/DateExtend${jskid!''}.js"/><#break>

            <#--主观作业多图上传-->
            <#case "subjectHomworkImagesUpload"><@app.script href="public/script/studentv3/subjectHomeworkTpl.js"/><#break>

            <#--图片预览和上传-->
            <#case "fileUpLoader">
            <@app.script href="public/plugin/YQuploader-1.0/lib/webuploader/webuploader.min.js"/>
            <@app.script href="public/plugin/YQuploader-1.0/YQuploader.js"/>
            <#break>

            <#-- App共用支付 -->
            <#case 'paymentmobile.confirm'><@app.script href="public/script/paymentmobile/confirm${csskid!''}.js" /><#break>
            <#case 'paymentmobile.wechatpay'><@app.script href="public/script/paymentmobile/wechatpay${csskid!''}.js" /><#break>
            <#case "teachingresource.index">
            <#--<@app.script href="public/script/utils/remotequestion${jskid!''}.js"/>-->
            <@app.script href="public/script/compontents/venus_vue${jskid!''}.js"/>
            <@app.script href="public/script/teacherv3/teachingresource/daite${csskid!''}.js" />
            <@app.script href="public/script/teacherv3/teachingresource/components/level_readings${csskid!''}.js" />
            <@app.script href="public/script/teacherv3/teachingresource/components/basic_app${csskid!''}.js" />
            <@app.script href="public/script/teacherv3/teachingresource/components/intelligent_teaching${csskid!''}.js" />
            <@app.script href="public/script/teacherv3/teachingresource/components/courseware${csskid!''}.js" />
            <@app.script href="public/script/teacherv3/teachingresource/components/word_recognition_and_reading${csskid!''}.js" />
            <@app.script href="public/script/teacherv3/teachingresource/components/key_points${csskid!''}.js" />
            <@app.script href="public/script/teacherv3/teachingresource/components/cuotibao${csskid!''}.js" />
            <@app.script href="public/script/teacherv3/teachingresource/components/wordteachandpractice${csskid!''}.js" />
            <@app.script href="public/script/teacherv3/teachingresource/components/natural_spelling${csskid!''}.js" />
            <@app.script href="public/script/teacherv3/teachingresource/index${csskid!''}.js" />
            <#break>
            <#case "teachingresource.daiteutil">
            <@app.script href="public/script/teacherv3/teachingresource/daite${csskid!''}.js" />
            <#break>
            <#case "teachingresource.wordrecognitionandreadingdetail">
            <@app.script href="public/script/teacherv3/teachingresource/wordrecognitionandreadingdetail${csskid!''}.js" />
            <#break>
            <!-- 故意触发一个解析错误，防止调用错了而不知道 -->
            <#default>${name} ${SHOULD_NOT_IN_THIS_CASE}<#break>
        </#switch>
    </#list>
</#compress></#macro>

<#macro seajs names=[] cdn=false>
    <#if (ProductDevelopment.isTestEnv() || ProductDevelopment.isStagingEnv() || ProductDevelopment.isProductionEnv())>
        <#assign jskid = ".min" />
    <#else>
        <#assign jskid = "" />
    </#if>

    <#if (ProductDevelopment.isTestEnv() || ProductDevelopment.isStagingEnv() || ProductDevelopment.isProductionEnv())>
        <#assign csskid = ".min" />
    <#else>
        <#assign csskid = "" />
    </#if>
    <script type="text/javascript">
        define.amd = {
            jQuery : true
        };
        seajs.config({
            base: "../public/",
            alias: {
                <#list names as name>
                    <#switch name>
                        <#case "jquery">"jquery" : "<@app.link href="public/plugin/jquery/jquery-1.7.1.min.js"/>"<#if name_has_next>,</#if><#break>
                        <#case "underscore">"underscore" : "<@app.link href="public/plugin/underscore1.8.2/underscore-min.js"/>"<#if name_has_next>,</#if><#break>
                        <#case "Events">"Events" : "<@app.link href="public/script/teacherv3/modules/Events${jskid!''}.js"/>"<#if name_has_next>,</#if><#break>
                        <#case "LevelAndClazz">"LevelAndClazz" : "<@app.link href="public/script/teacherv3/modules/LevelAndClazz${jskid!''}.js"/>"<#if name_has_next>,</#if><#break>
                        <#case "BookAndUnit">"BookAndUnit" : "<@app.link href="public/script/teacherv3/modules/BookAndUnit${jskid!''}.js"/>"<#if name_has_next>,</#if><#break>
                    </#switch>
                </#list>
            }
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
setTimeout(function(){
    var w=window,d=document;
    if(w.jQuery==undefined){
        var idx=-1,keys=${json_encode(cdnDomainMapKeys)};
        if(!keys.length){alert('CDN配置错误，请联系客服或技术');return;}
        for(var i=0;i<keys.length;i++){if(keys[i] == '${currentCdnType!''}'){idx=i;break;}}
        var nct = keys[(idx + 1) % keys.length], t = new Date();
        t.setTime(t.getTime() + (nct == 'skip' ? 7200 * 1000 : 86400 * 14 * 1000));
        d.cookie = "cdntype=" + nct + ";path=/;expires=" + t.toGMTString();
        setTimeout(function () { w.top.location.href='/?_set_cdntype=' + nct; }, 500);
    }
}, 20 * 1000);
</script>
</#macro>

<#macro site_traffic_analyzer_begin>
<script type="text/javascript">
    <#-- 用于我们自己的日志分析 -->
    window._17zuoye = window._17zuoye || {};
    window._17zuoye.pathPattern = '${requestContext.pathPattern}';
    window._17zuoye.realRemoteAddr = '${requestContext.realRemoteAddr}';
</script>
</#macro>

<#macro site_traffic_analyzer_end>
<#--<script type="text/javascript">
    /*ga统计*/
    (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
                (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
            m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
    })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

    /*根据用户类型区分 老师抽样率100% 学生抽样率1% */
    var _ga_trackingId = 'UA-38181315-1',_ga_sampleRate = 1;
        <#if (currentUser.userType)?? && currentUser.userType == 1>
        _ga_trackingId = 'UA-38181315-3';
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

<#macro compress_single_line>
    <#local captured><#nested></#local>
    ${ captured?replace("^\\s+|\\s+$|\\n|\\r", "", "rm") }
</#macro>
<#macro mycompress js=[] css=[] cdn=false>
<@compress_single_line>
<script src='http://cdn-cc.test.17zuoye.net/compress/??
<#list js as bl>
    <#switch bl>
        <#case "jquery">
            <@app.versionedUrl href="plugin/jquery/jquery-1.7.1.min.js" />,
            <#break>
        <#case "jquery.utils">
            <@app.versionedUrl href="plugin/jquery-utils/jquery-utils.js" />,
            <#break>
        <#case "$17">
            <@app.versionedUrl href="script/$17.js" />,
            <#break>
        <#case "$17Modules">
            <@app.versionedUrl href="script/$17Modules.js" />,
            <#break>
        <#case "alert">
            <@app.versionedUrl href="plugin/jquery-impromptu/jquery-impromptu.js" />,
            <#break>
        <#case "crossProjectShare">
            <@app.versionedUrl href="script/crossProjectShare.js" />,
            <#break>
        <#case "json2">
            <@app.versionedUrl href="plugin/json2.js" />,
            <#break>
        <#case "template">
            <@app.versionedUrl href="plugin/template.js" />,
            <#break>
    </#switch>
</#list>
' type='text/javascript' charset='utf-8'></script>
</@compress_single_line>
</#macro>
