<#-- 常用变量 -->
<#assign sid = sid!"">
<#assign subject = subject!"">
<#assign isBindClazz=isBindClazz!false>
<#assign result = result!{
    "success" : false,
    "info" : "",
    "errorCode": ""
}>

<#assign trackTypeObj = {
    "TravelAmerica" : "usa",
    "Walker" : "adven",
    "iandyou100": "iru",
    "AfentiExam" : "afenti",
    "SanguoDmz" : "sanguo"
}>

<#assign staticKid = "" />
<#if (ProductDevelopment.isTestEnv() || ProductDevelopment.isStagingEnv() || ProductDevelopment.isProductionEnv())>
    <#assign staticKid = ".min" />
</#if>

<#-- 公用的默认用户头像 -->
<#assign publicDefaultUserImg>
    <@app.avatar href=""/>
</#assign>
<#assign publicDefaultUserImg = publicDefaultUserImg?trim>

<#-- 公用的用户头像地址前缀 -->
<#assign publicUserImgPre = publicDefaultUserImg?replace("avatar_normal.gif", "")?trim>

<#function buildUserImgSrc userImgFileName = "">
    <#assign buildUserImgSrcResult = publicDefaultUserImg >
    <#if userImgFileName?exists && userImgFileName?has_content>
        <#assign buildUserImgSrcResult = publicUserImgPre + userImgFileName>
    </#if>
    <#return buildUserImgSrcResult?trim>
</#function>

<#--
    一个用于构建静态文件加载路径的函数， 同<@sugar.capsule...> 不同的是 他使用的是default上该文件的最后修改日期，因此并不能做到随时清除缓存 特点是 在开发环境会自动加入防止缓存策略  非开发环境则不会做任何改动且不影响发布版本号
    @param String staticName 静态文件的名字  TODO 不需要加后缀   图片需要加后缀
    @param String type  文件类型  目前仅支持 css js
    @param specialFilePath 特殊的静态文件路径
-->
<#function buildStaticFilePath staticName type specialFilePath = "">

    <#assign staticFilePathResult = "" isUseSpecialFilePath = specialFilePath?has_content>

    <#assign staticTimeStamp = (ProductDevelopment.isDevEnv())?string("time=${.now?long}", "")?trim > <#-- TODO 这里的`?long` IDE 虽然会报错 但并不会影响使用 后续观察学习 &time 是因为aap.link 在开发环境会默认带上 ?_=last_modify_date_on_default -->

    <#switch type>
        <#case "css">
            <#assign staticFilePathResult>
                <@app.link href="${isUseSpecialFilePath?string(specialFilePath, 'public/skin/parentMobile/css/' + staticName + staticKid)}.css"/>
            </#assign>
            <#break >
        <#case "js">
            <#assign staticFilePathResult>
                <@app.link href="${isUseSpecialFilePath?string(specialFilePath, 'public/script/parentMobile/' + staticName + staticKid)}.js"/>
            </#assign>
            <#break >
        <#case "img">
            <#assign staticFilePathResult>
                <@app.link href="${isUseSpecialFilePath?string(specialFilePath, 'public/skin/parentMobile/images/' + staticName)}"/>
            </#assign>
            <#break >
        <#case "other">
            <#assign staticFilePathResult>
                <@app.link href="${staticName}"/>
            </#assign>
            <#break >
        <#default>
            <#break >
    </#switch>

    <#assign connector = "">
    <#if staticTimeStamp != "">
        <#assign connector = (staticFilePathResult?index_of('?') == -1)?string("?", "&")>
    </#if>

    <#return (staticFilePathResult + connector + staticTimeStamp)?trim>

</#function>

<#--
     配合 buildStaticFilePath 来帮助我们构建同type匹配的html tab标签
    @param String staticName 静态文件的名字  TODO 不需要加后缀
    @param String type  文件类型  目前仅支持 css js
-->
<#function buildLoadStaticFileTag staticName type specialFilePath="">
    <#assign staticFilePath = buildStaticFilePath(staticName, type, specialFilePath) >

    <#assign staticFileTagResult = "">

    <#switch type>
        <#case "css">
            <#assign staticFileTagResult>
            <link rel="stylesheet" href="${staticFilePath}"/>
            </#assign>
            <#break >
        <#case "js">
            <#assign staticFileTagResult>
            <script src="${staticFilePath}" type="text/javascript"></script>
            </#assign>
            <#break >
        <#default>
            <#break >
    </#switch>

    <#return staticFileTagResult?trim>
</#function>

<#function buildTrackData dataTrack>
    <#assign buildTrackDataResult> data-track='${dataTrack}' </#assign>
    <#return  buildTrackDataResult >
</#function>

<#function buildAutoTrackTag dataTrack doScrollTrackAuto=false>
    <#assign trackClassName = doScrollTrackAuto?string("doScrollTrack", "doAutoTrack")>

    <#assign buildTrackTagResult>
        <em class="hide ${trackClassName}" ${buildTrackData(dataTrack)}></em>
    </#assign>
    <#return buildTrackTagResult?trim>
</#function>

<#--
    filter 方法 同javascript filter作用相同
    things : [{}]  要操作的对象数组
    name : 需要对那个key进行比较
    value : key的value满足什么条件

    eg :
    filter([{r:1},{r:2},{r:1,a:1}], "r", 1)  ===> [{r:1},{r:1,a:1}]
-->
<#function filter things name value>
    <#local filterResult = []>
    <#list things as thing>
        <#if thing[name] == value>
            <#local filterResult = filterResult + [thing]>
        </#if>
    </#list>
    <#return filterResult>
</#function>


<#--
<#function getDefaultByExtend target default>
    <#local cacheObj = {}>
    <#list target as key>
        <#if default[key]?exists>
        cacheObj[key] = default[key];
        <#elseif target[key]?exists>
        cacheObj[key] = target[key];
        </#if>
    </#list>
    <#return cacheObj>
</#function>

<#function test param1><#return param1></#function>
-->


<#function getTrustUriPrefix>
    <#assign TrustUriPrefix = "">

    <#-- TODO wechat 权重 == www 因此不能加 www -->
    <#if ProductDevelopment.isTestEnv()>
        <#assign TrustUri = "//wechat.test.17zuoye.net">
    <#elseif ProductDevelopment.isStagingEnv()>
        <#assign TrustUri = "//wechat.staging.17zuoye.net">
    <#elseif ProductDevelopment.isProductionEnv()>
        <#assign TrustUri = "//wechat.17zuoye.com">
    <#elseif ProductDevelopment.isDevEnv()>
        <#assign TrustUri = "//wechat.test.17zuoye.net">
    </#if>

    <#return TrustUri?trim>
</#function>

<#function getTrustUri  isNotSupportTrust = true>
    <#if isNotSupportTrust>
        <#return "/parentMobile/ucenter/upgrade.vpage">
    </#if>

    <#return (getTrustUriPrefix() + "/parent/trustee/index.vpage")?trim>
</#function>

<#function getTrustOrderUri  isNotSupportTrust = true>
    <#if isNotSupportTrust>
        <#return "/parentMobile/ucenter/upgrade.vpage">
    </#if>

    <#return (getTrustUriPrefix() + "/parent/trustee/orderlist.vpage")?trim>
</#function>

<#function getProductionType homeworkType>
    <#assign questionTypes = {
	"ENGLISH"      : "AfentiExam",
	"QUIZ_ENGLISH" : "AfentiExam",
	"MATH"         : "AfentiMath"
	}>

    <#return ((questionTypes[homeworkType!'ENGLISH'])!"AfentiExam")?trim>
</#function>
