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
                <#break>
            <#case 'core'><@app.script href="public/script/$17${jskid!''}.js" /><#break>
        </#switch>
    </#list>

    <#list css as name>
        <#switch name>
            <#case 'plugin.register'><@app.css href="public/skin/default/css/reg${csskid!''}.css" /><#break>
            <#case 'plugin.index4'><@app.css href="public/skin/default/css/indexv4${csskid!''}.css" /><#break>
            <#case 'plugin.alert'><@app.css href="public/plugin/jquery-impromptu/jquery-impromptu.css" /><#break>
            <#case 'plugin.flexslider'><@app.css href="public/plugin/jquery.flexslider/flexslider.css" /><#break>

            <#case 'plugin.headfoot'><@app.css href="public/skin/default/images/headfoot/head-foot${csskid!''}.css" /><#break>

            <#--小学老师-->
            <#case "new_teacher.basev1"><@app.css href="public/skin/teacherv3/css/basev1${csskid!''}.css" /><#break />
            <#case "new_teacher.module"><@app.css href="public/skin/teacherv3/css/module${csskid!''}.css" /><#break />
            <#case "new_teacher.widget"><@app.css href="public/skin/teacherv3/css/widget${csskid!''}.css" /><#break />
            <#case "new_teacher.guideStep"><#case "guideStep"><@app.css href="public/skin/teacherv3/css/guideStep${csskid!''}.css" /><#break />
            <#case "new_teacher.message"><@app.css href="public/skin/teacherv3/css/systemmessage${csskid!''}.css" /><#break />

            <#--小学学生-->
            <#case "new_student.base"><@app.css href="public/skin/studentv3/css/base${csskid!''}.css" /><#break />
            <#case "new_student.module"><@app.css href="public/skin/studentv3/css/module${csskid!''}.css" /><#break />
            <#case "new_student.widget"><@app.css href="public/skin/studentv3/css/widget${csskid!''}.css" /><#break />

            <#--极算学生-->
            <#case "ssz_student.base"><@app.css href="public/skin/studentssz/css/base${csskid!''}.css" /><#break />
            <#case "ssz_student.module"><@app.css href="public/skin/studentssz/css/module${csskid!''}.css" /><#break />
            <#case "ssz_student.widget"><@app.css href="public/skin/studentssz/css/widget${csskid!''}.css" /><#break />

            <!-- 教研员 -->
            <#case 'rstaff.main'><@app.css href="public/skin/rstaff/skin/main${csskid!''}.css" /><#break>

            <#case 'loginv5'><@app.css href="public/skin/default/v5/css/pc-home${csskid!''}.css" /><#break>
            <#-- 新官网首页 -->
            <#case 'indexv5'><@app.css href="public/skin/default/v5/css/indexv5${csskid!''}.css" /><#break>
            <#-- 新官网新闻页-翻页插件 -->
            <#case 'paging'><@app.css href="public/skin/default/v5/css/paging${csskid!''}.css" /><#break>

            <#--陈经纶-->
            <#case 'cjlschool'><@app.css href="public/skin/default/css/cjlschool${csskid!''}.css" /><#break>
            <#--希悦-->
            <#case 'seiueschool'><@app.css href="public/skin/default/css/seiueschool${csskid!''}.css" /><#break>
            <#--cnedu-->
            <#case 'cnedu'><@app.css href="public/skin/default/css/cnedu${csskid!''}.css" /><#break>
            <#---->
            <#case 'new_teacher.kuailexue'><@app.css href="public/skin/teacherv3/css/kuailexue${csskid!''}.css" /><#break>

            <!-- 故意触发一个解析错误，防止调用错了而不知道 -->
            <#default>${SHOULD_NOT_IN_THIS_CASE}<#break>
        </#switch>
    </#list>

    <#list js as name>
        <#switch name>
            <#case 'jquery'><#break>
            <#case 'core'><#break>
            <#case '17module'><@app.script href="public/script/$17Modules${jskid!''}.js" /><#break>
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
            <#case 'alert'><@app.script href="public/plugin/jquery-impromptu/jquery-impromptu${jskid!''}.js"/><#break>
            <#case 'template'><@app.script href="public/plugin/template${jskid!''}.js"/><#break>
            <#case 'flexslider'><@app.script href="public/plugin/jquery.flexslider/jquery.flexslider-min.js"/><#break>
            <#case 'fastLiveFilter'><@app.script href="public/plugin/jquery-fastLiveFilter/jquery.fastLiveFilter.js"/><#break>
            <#case 'hashchange'><@app.script href="public/plugin/jquery.ba-hashchange.min.js"/><#break>
            <#case 'ZeroClipboard'><@app.script href="public/plugin/ZeroClipboard/ZeroClipboard.js"/><#break>

            <#case 'teacher'><@app.script href="public/script/teacherv3${jskid!''}.js" /><#break>
            <#case 'student'><@app.script href="public/script/student${jskid!''}.js" /><#break>
            <#case 'ssz.student'><@app.script href="public/script/studentssz${jskid!''}.js" /><#break>

            <#case 'fastLiveFilter'><@app.script href="public/plugin/jquery-fastLiveFilter/jquery.fastLiveFilter.js"/><#break>

            <#case "clazz.clazzlist"><@app.script href="public/script/teacherv3/clazz/clazzlist${jskid!''}.js"/><#break>
            <#case "clazz.clazzdetail"><@app.script href="public/script/teacherv3/clazz/clazzdetail${jskid!''}.js"/><#break>
            <#case "clazz.inviteteacherlist"><@app.script href="public/script/teacherv3/clazz/inviteteacherlist${jskid!''}.js"/><#break>
            <#case "clazz.unprocessedapplication"><@app.script href="public/script/teacherv3/clazz/unprocessedapplication${jskid!''}.js"/><#break>

            <#case 'loginv5'><@app.script href="public/script/loginv5${jskid!''}.js" /><#break>
            <#case 'cjlschool'><@app.script href="public/script/cjlschool${jskid!''}.js" /><#break>
            <#case 'seiueschool'><@app.script href="public/script/seiueschool${jskid!''}.js" /><#break>

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
            <!-- 故意触发一个解析错误，防止调用错了而不知道 -->
            <#default>${name} ${SHOULD_NOT_IN_THIS_CASE}<#break>
        </#switch>
    </#list>
</#compress></#macro>


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