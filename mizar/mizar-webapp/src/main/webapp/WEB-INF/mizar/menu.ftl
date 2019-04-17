<div class="sidebar" style="overflow: scroll">
<#if currentUser.checkSysAuth("basic", "*") || currentUser.checkSysAuth("operate", "*")>
    <div class="box" >
        <a href="javascript:void(0);" class="title message">工作台</a>
        <#if currentUser.checkSysAuth("basic", "brand")><a href="/basic/brand/index.vpage"
                                                           <#if leftMenu == "品牌管理">class="active"</#if>>品牌管理</a></#if>
        <#if currentUser.checkSysAuth("basic", "shop")><a href="/basic/shop/index.vpage"
                                                          <#if leftMenu == "机构管理">class="active"</#if>>机构管理</a></#if>
        <#if currentUser.checkSysAuth("basic", "goods")><a href="/basic/goods/index.vpage"
                                                           <#if leftMenu == "课程管理">class="active"</#if>>课程管理</a></#if>
        <#if currentUser.checkSysAuth("basic", "activity")><a href="/basic/activity/index.vpage"
                                                              <#if leftMenu == "亲子活动">class="active"</#if>>亲子活动</a></#if>
        <#if currentUser.checkSysAuth("basic", "apply")><a href="/basic/apply/index.vpage"
                                                           <#if leftMenu == "我的申请">class="active"</#if>>我的申请</a></#if>
        <#if currentUser.checkSysAuth("basic", "notify")><a href="/basic/notify/index.vpage"
                                                            <#if leftMenu == "我的消息">class="active"</#if>>我的消息</a></#if>
        <#if currentUser.checkSysAuth("operate", "audit")><a href="/operate/audit/index.vpage"
                                                             <#if leftMenu == "变更审核">class="active"</#if>>变更审核</a></#if>
        <#if currentUser.checkSysAuth("basic", "officialaccount")><a href="/basic/officialaccount/index.vpage"
                                                                     <#if leftMenu == "基本信息">class="active"</#if>>基本信息</a></#if>
        <#if currentUser.checkSysAuth("basic", "officialaccount")><a href="/basic/officialaccount/material.vpage"
                                                                     <#if leftMenu == "素材管理">class="active"</#if>>素材管理</a></#if>
        <#if currentUser.checkSysAuth("basic", "officialaccount")><a href="/basic/officialaccount/articlelist.vpage"
                                                                     <#if leftMenu == "发布管理">class="active"</#if>>发布管理</a></#if>
        <#if currentUser.checkSysAuth("basic", "albumnews")><a href="/basic/albumnews/index.vpage"
                                                               <#if leftMenu == "专辑管理">class="active"</#if>>专辑管理</a></#if>
        <#if currentUser.checkSysAuth("basic", "albumnews")><a href="/basic/albumnews/news/index.vpage"
                                                               <#if leftMenu == "专辑文章管理">class="active"</#if>>专辑文章管理</a></#if>
        <#if currentUser.checkSysAuth("basic", "settlement")><a href="/basic/settlement/index.vpage"
                                                                <#if leftMenu == "我的收入">class="active"</#if>>我的收入</a></#if>
    </div>
</#if>

<#if currentUser.checkSysAuth("crm", "*")>
    <div class="box">
        <a href="javascript:void(0);" class="title customer">客户管理</a>
        <#if currentUser.checkSysAuth("crm", "reserve")><a href="/crm/reserve/index.vpage"
                                                           <#if leftMenu == "预约信息">class="active"</#if>>预约信息</a></#if>
    </div>
</#if>


<#if currentUser.checkSysAuth("order", "*")>
    <div class="box">
        <a href="javascript:void(0);" class="title customer">点读机订单</a>
        <#if currentUser.checkSysAuth("order", "picorder")><a href="/order/picorder/count.vpage"
                                                              <#if leftMenu == "订单统计">class="active"</#if>>订单统计</a></#if>
        <#if currentUser.checkSysAuth("order", "picorder")><a href="/order/picorder/detailcount.vpage"
                                                              <#if leftMenu == "单日详情">class="active"</#if>>单日详情</a></#if>
        <#if currentUser.checkSysAuth("order", "sk_picorder")><a href="/order/sk_picorder/sk_count.vpage"
                                                                 <#if leftMenu == "订单统计">class="active"</#if>>订单统计</a></#if>
        <#if currentUser.checkSysAuth("order", "sk_picorder")><a href="/order/sk_picorder/sk_detailcount.vpage"
                                                                 <#if leftMenu == "单日详情">class="active"</#if>>单日详情</a></#if>
        <#if currentUser.checkSysAuth("order", "sh_picorder")><a href="/order/sh_picorder/sh_count.vpage"
                                                                 <#if leftMenu == "订单统计">class="active"</#if>>订单统计</a></#if>
        <#if currentUser.checkSysAuth("order", "sh_picorder")><a href="/order/sh_picorder/sh_detailcount.vpage"
                                                                 <#if leftMenu == "单日详情">class="active"</#if>>单日详情</a></#if>
        <#if currentUser.checkSysAuth("order", "ln_picorder")><a href="/order/ln_picorder/ln_count.vpage"
                                                                 <#if leftMenu == "订单统计">class="active"</#if>>订单统计</a></#if>
        <#if currentUser.checkSysAuth("order", "ln_picorder")><a href="/order/ln_picorder/ln_detailcount.vpage"
                                                                 <#if leftMenu == "单日详情">class="active"</#if>>单日详情</a></#if>
        <#if currentUser.checkSysAuth("order", "yl_picorder")><a href="/order/yl_picorder/yl_count.vpage"
                                                                 <#if leftMenu == "订单统计">class="active"</#if>>订单统计</a></#if>
        <#if currentUser.checkSysAuth("order", "yl_picorder")><a href="/order/yl_picorder/yl_detailcount.vpage"
                                                                 <#if leftMenu == "单日详情">class="active"</#if>>单日详情</a></#if>
    </div>
</#if>

<#if currentUser.checkSysAuth("picbook_ps", "*")>
    <div class="box">
        <a href="javascript:void(0);" class="title customer">培生绘本</a>
        <#if currentUser.checkSysAuth("picbook_ps", "order")><a href="/picbook_ps/order/stat.vpage"
                                                                <#if leftMenu == "订单统计">class="active"</#if>>订单统计</a></#if>
        <#if currentUser.checkSysAuth("picbook_ps", "reading")><a href="/picbook_ps/reading/stat.vpage"
                                                                  <#if leftMenu == "阅读统计">class="active"</#if>>阅读统计</a></#if>
    </div>
</#if>

<#if currentUser.checkSysAuth("picbook", "zhss")>
    <div class="box">
        <#if currentUser.checkSysAuth("picbook", "zhss")>
            <a href="javascript:void(0);" class="title customer">中少社绘本</a>
            <a href="/picbook/zhss/order/stat.vpage" <#if leftMenu == "zhssOrderStat">class="active"</#if>>订单统计</a>
        </#if>
    </div>
</#if>

<#if currentUser.checkSysAuth("thirdParty", "userInfo")>
    <div class="box">
        <a href="javascript:void(0);" class="title customer">数据查询</a>
        <#if currentUser.checkSysAuth("thirdParty", "userInfo")>
            <a href="/thirdParty/userInfo/infoList.vpage"<#if leftMenu == "用户数据查询">class="active"</#if>>用户数据查询</a>
        </#if>
    </div>
</#if>

<#if currentUser.checkSysAuth("picbook", "hds")>
    <div class="box">
        <#if currentUser.checkSysAuth("picbook", "hds")>
            <a href="javascript:void(0);" class="title customer">红袋鼠绘本</a>
            <a href="/picbook/hds/order/stat.vpage"<#if leftMenu == "hdsOrderStat">class="active"</#if>>订单统计</a>
        </#if>
    </div>
</#if>

<#if currentUser.checkSysAuth("picbook", "wk")>
    <div class="box">
        <#if currentUser.checkSysAuth("picbook", "wk")>
            <a href="javascript:void(0);" class="title customer">文康绘本</a>
            <a href="/picbook/wk/order/stat.vpage"<#if leftMenu == "wkOrderStat">class="active"</#if>>订单统计</a>
        </#if>
    </div>
</#if>

<#if currentUser.checkSysAuth("picbook", "efuture")>
    <div class="box">
        <#if currentUser.checkSysAuth("picbook", "efuture")>
            <a href="javascript:void(0);" class="title customer">E-Future绘本</a>
            <a href="/picbook/efuture/order/stat.vpage"<#if leftMenu == "efutureOrderStat">class="active"</#if>>订单统计</a>
        </#if>
    </div>
</#if>

<#if currentUser.checkSysAuth("picbook", "farfaria")>
    <div class="box">
        <#if currentUser.checkSysAuth("picbook", "farfaria")>
            <a href="javascript:void(0);" class="title customer">Farfaria绘本</a>
            <a href="/picbook/farfaria/order/stat.vpage"<#if leftMenu == "farfariaOrderStat">class="active"</#if>>订单统计</a>
        </#if>
    </div>
</#if>

<#if currentUser.checkSysAuth("picbook", "jq")>
    <div class="box">
        <#if currentUser.checkSysAuth("picbook", "jq")>
            <a href="javascript:void(0);" class="title customer">剑桥绘本</a>
            <a href="/picbook/jq/order/stat.vpage"<#if leftMenu == "jqOrderStat">class="active"</#if>>订单统计</a>
        </#if>
    </div>
</#if>

<#if currentUser.checkSysAuth("picbook", "csie")>
    <div class="box">
        <#if currentUser.checkSysAuth("picbook", "csie")>
            <a href="javascript:void(0);" class="title customer">英语文化绘本</a>
            <a href="/picbook/csie/order/stat.vpage"<#if leftMenu == "csieOrderStat">class="active"</#if>>订单统计</a>
        </#if>
    </div>
</#if>

<#if currentUser.checkSysAuth("activity", "*")>
    <#if currentUser.isTangramJury()>
        <div class="box">
            <a href="javascript:void(0);" class="title service">七巧板活动</a>
            <#if currentUser.checkSysAuth("activity", "tangram")><a href="/activity/tangram/index.vpage"
                                                                    <#if leftMenu == "作品评选">class="active"</#if>>作品评选</a></#if>
        </div>
    </#if>
</#if>

<#if currentUser.checkSysAuth("biz", "*")>
    <div class="box">
        <a href="javascript:void(0);" class="title compass">经营罗盘</a>
        <#if currentUser.checkSysAuth("biz", "report")><a href="/biz/report/index.vpage"
                                                          <#if leftMenu == "门店概况">class="active"</#if>>门店概况</a></#if>
        <#if currentUser.checkSysAuth("biz", "rating")><a href="/biz/rating/index.vpage"
                                                          <#if leftMenu == "用户评论">class="active"</#if>>用户评论</a></#if>
    </div>
</#if>

<#if currentUser.checkSysAuth("data", "*")>
    <div class="box">
        <a href="javascript:void(0);" class="title compass">数据概况</a>
        <#if currentUser.checkSysAuth("data", "statistic")><a href="/data/statistic/index.vpage"
                                                              <#if leftMenu == "经营数据">class="active"</#if>>经营数据</a></#if>
        <#if currentUser.checkSysAuth("data", "reserve")><a href="/data/reserve/index.vpage"
                                                            <#if leftMenu == "预约数据">class="active"</#if>>预约数据</a></#if>
    </div>
</#if>

<#if currentUser.checkSysAuth("config", "*")>
    <div class="box">
        <a href="javascript:void(0);" class="title service">系统管理</a>
        <#if currentUser.checkSysAuth("config", "syspath")><a href="/config/syspath/index.vpage"
                                                              <#if leftMenu == "功能权限">class="active"</#if>>功能权限</a></#if>
        <#if currentUser.checkSysAuth("config", "user")><a href="/config/user/index.vpage"
                                                           <#if leftMenu == "用户管理">class="active"</#if>>用户管理</a></#if>
        <#if currentUser.checkSysAuth("config", "group")><a href="/config/group/index.vpage"
                                                            <#if leftMenu == "部门管理">class="active"</#if>>部门管理</a></#if>
    </div>
</#if>

<#if currentUser.checkSysAuth("course", "*")>
<#--老师登录的个性化菜单-->
    <#if currentUser.isMicroTeacher()>
        <div class="box">
            <#if currentUser.checkSysAuth("course", "manage")><a href="/course/manage/index.vpage"
                                                                 <#if leftMenu == "课程管理">class="active"</#if>>我的课程</a></#if>
            <#if currentUser.checkSysAuth("course", "manage")><a href="/course/manage/tk_download.vpage"
                                                                 <#if leftMenu == "下载直播软件">class="active"</#if>>下载直播软件</a></#if>
        </div>
    <#else>
        <div class="box">
            <a href="javascript:void(0);" class="title service">微课堂CRM</a>
            <#if currentUser.checkSysAuth("course", "manage")><a href="/course/manage/index.vpage"
                                                                 <#if leftMenu == "课程管理">class="active"</#if>>课程管理</a></#if>
            <#if currentUser.checkSysAuth("course", "manage")><a href="/course/manage/teachers.vpage"
                                                                 <#if leftMenu == "教师管理">class="active"</#if>>教师管理</a></#if>
            <#if currentUser.checkSysAuth("course", "manage")><a href="/course/manage/itemlist.vpage"
                                                                 <#if leftMenu == "列表管理">class="active"</#if>>列表管理</a></#if>
            <#if currentUser.checkSysAuth("course", "manage")><a href="/course/manage/statistic.vpage"
                                                                 <#if leftMenu == "数据统计">class="active"</#if>>数据统计</a></#if>
        </div>
    </#if>
</#if>

<#if !currentUser.isMicroTeacher() && !currentUser.isTangramJury()>
    <div class="box">
        <a href="javascript:void(0);" class="title account">账户管理</a>
    <#--<a style="display:none;" href="javascript:void(0);" <#if leftMenu == "账号信息">class="active"</#if>>账号信息</a>-->
        <a href="/auth/modifyPass.vpage" <#if leftMenu == "密码修改">class="active"</#if>>密码修改</a>
    </div>
</#if>

<#if currentUser.isJxTeacherResearch()>
    <div class="box">
        <a href="javascript:void(0);" class="title account">教学资源</a>
        <a href="/jiangxi/teachingresource/index.vpage" <#if leftMenu == "图文">class="active"</#if>>图文</a>
        <a href="/jiangxi/17JTConf/index.vpage" <#if leftMenu == "视频">class="active"</#if>>视频</a>
    </div>
</#if>
<#if currentUser.checkSysAuth("bookstore", "manage")>
    <div class="box">
        <a href="javascript:void(0);" class="title service">经营数据</a>
        <a href="/bookstore/manager/operation.vpage" <#if leftMenu == "经营状况">class="active"</#if>>经营状况</a>
    </div>

    <div class="box">
        <a href="javascript:void(0);" class="title service">书店管理</a>
        <a href="/bookstore/manager/list.vpage" <#if leftMenu == "门店列表">class="active"</#if>>门店列表</a>
    <#if currentUser.isAdmin()>
         <a href="/bookstore/manager/whiteList/list.vpage" <#if leftMenu == "转介绍白名单">class="active"</#if>>转介绍白名单</a>
    </#if>
    </div>


</#if>
</div>