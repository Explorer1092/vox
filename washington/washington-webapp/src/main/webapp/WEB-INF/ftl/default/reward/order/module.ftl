<#import "../layout/layout.ftl" as temp />
<#macro page title='myorder'>
    <@temp.page index='' columnType="empty">
        <#--当前登录用户类型-->
        <#assign currentUserType><#switch (currentUser.userType)!'3'><#case 1>TEACHER<#break><#case 8>RSTAFF<#break><#case 3>STUDENT<#break></#switch></#assign>
        <#assign integarlType><#switch (currentUser.userType)!'3'><#case 1><@ftlmacro.garyBeansText/><#break><#case 8><@ftlmacro.garyBeansText/><#break><#case 3>学豆<#break></#switch></#assign>
        <#--<p class="bread_crumb_nav clearfix">
            <a href="/reward/index.vpage">首页</a><span class="active">></span>
            <a href="/reward/order/myorder.vpage" class="active">我的奖品</a>
        </p>-->
        <div class="my_prize_title_box clearfix">
            <div class="my_prize_title_bg">
                <i class="J_sprites"></i>
            </div>
            <div class="my_prize_title">
                <a href="/reward/order/myorder.vpage" <#if title='myorder' || title= 'history' >class="J_red"</#if> >实物兑换</a><span>|</span>
                <a href="/reward/order/myexperience.vpage" <#if title='myexperience'>class="J_red"</#if>>虚拟兑换</a><span>|</span>
                <#if currentUser.userType == 1 ><a href="/reward/order/substreceive.vpage" <#if title='substreceive'>class="J_red"</#if>>我的代收</a><span>|</span></#if>
                <a href="/reward/order/mywish.vpage" <#if title='mywish'>class="J_red"</#if>>我的收藏</a>
                <#--<#if (currentUser.userType == 1 && currentTeacherDetail.isPrimarySchool())!false><span>|</span><a href="/reward/order/weekreward.vpage" <#if title='weekreward'>class="J_red"</#if>>本周奖励</a></#if>-->
            </div>
        </div>
        <#nested />
    </@temp.page>
</#macro>

