<#-- 静态文件min后缀 -->
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

<#-- 创建用户头像url -->
<#function buildUserImgSrc userImgFileName = "">
    <#local buildUserImgSrcResult = publicDefaultUserImg >
    <#if userImgFileName?exists && userImgFileName?has_content>
        <#local buildUserImgSrcResult = publicUserImgPre + userImgFileName>
    </#if>
    <#return buildUserImgSrcResult?trim>
</#function>

<#--
    一个用于构建静态文件加载路径的函数， 同<@sugar.capsule...> 不同的是 他使用的是default上该文件的最后修改日期，因此并不能做到随时清除缓存 特点是 在开发环境会自动加入防止缓存策略  非开发环境则不会做任何改动且不影响发布版本号
    @param String type  文件类型  目前仅支持 css js
    @param filePath 特殊的静态文件路径
	@param String autoAddStaticKid  是否需要自动补上staticKid
-->
<#function buildStaticFilePath filePath type autoAddStaticKid = true>

    <#local staticFilePathResult = "" staticFilePath_StaticKid = autoAddStaticKid?string(staticKid, "") >

    <#local staticTimeStamp = (ProductDevelopment.isDevEnv())?string("time=${.now?long}", "")?trim > <#-- TODO 这里的`?long` IDE 虽然会报错 但并不会影响使用 后续观察学习 &time 是因为aap.link 在开发环境会默认带上 ?_=last_modify_date_on_default -->

    <#switch type>
        <#case "css">
            <#local staticFilePathResult>
                <@app.link href="${filePath + staticFilePath_StaticKid}.css"/>
            </#local>
            <#break >
        <#case "js">
            <#local staticFilePathResult>
                <@app.link href="${filePath + staticFilePath_StaticKid}.js"/>
            </#local>
            <#break >
        <#case "img">
        <#case "other">
            <#local staticFilePathResult>
                <@app.link href="${filePath}"/>
            </#local>
            <#break >
        <#default>
            <#break >
    </#switch>

    <#local connector = "">
    <#if staticTimeStamp != "">
        <#local connector = (staticFilePathResult?index_of('?') == -1)?string("?", "&")>
    </#if>

    <#return (staticFilePathResult + connector + staticTimeStamp)?trim>

</#function>

<#--
     配合 buildStaticFilePath 来帮助我们构建同type匹配的html tab标签
    @param String staticName 静态文件的名字  TODO 不需要加后缀
    @param String type  文件类型  目前仅支持 css js
	@param String autoAddStaticKid  是否需要自动补上staticKid
-->
<#function buildLoadStaticFileTag filePath type  autoAddStaticKid = true>
    <#local staticFilePath = buildStaticFilePath(filePath, type, autoAddStaticKid) >

    <#local result = "">

    <#switch type>
        <#case "css">
            <#local result>
            <link rel="stylesheet" href="${staticFilePath}"/>
            </#local>
            <#break >
        <#case "js">
            <#local result>
            <script src="${staticFilePath}" type="text/javascript"></script>
            </#local>
            <#break >
        <#default>
            <#break >
    </#switch>

    <#return result?trim>
</#function>

<#--
    配合 buildLoadStaticFileTag 来帮助我们构建一组同type匹配的html tab标签
	@param List files 一组静态文件信息
-->
<#function buildLoadStaticFileTagByList files>

    <#local result>
        <#list files as file>
            <#if file.path?exists && file.type?exists>
                ${buildLoadStaticFileTag(file.path, file.type, file.addKid!true)}
            </#if>
        </#list>
    </#local>

    <#return result?trim>
</#function>

<#-- 配合打点操作 -->
<#function buildTrackData dataTrack>
    <#local buildTrackDataResult> data-track='${dataTrack}' </#local>
    <#return  buildTrackDataResult >
</#function>

<#-- 打点Dom -->
<#function buildAutoTrackTag dataTrack doScrollTrackAuto=false>
    <#local trackClassName = doScrollTrackAuto?string("doScrollTrack", "doAutoTrack")>

    <#local buildTrackTagResult>
        <em class="hide ${trackClassName}" ${buildTrackData(dataTrack)}></em>
    </#local>
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

