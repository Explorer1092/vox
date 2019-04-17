<#import "../layout.ftl" as layout>
<@layout.page title="个人中心" pageJs="chipsIndex">
    <@sugar.capsule css=['chips'] />
<style>
    .personalPop .popInner .closeBtn {
        position: absolute;
        top: 0;
        right: 0;
        width: .875rem;
        height: .875rem;
        background-size: 0.875rem 0.875rem;
        background-position: center center;
        padding: 1rem;
    }
</style>
<div class="personalWrap" style="border-top: 1px solid transparent;">
    <div class="personalMain">
        <ul>
            <li data-status="${Paid?string('true','false')}">
                <span class="pic"></span>
                <span class="title">我的老师</span>
            </li>
            <a href="/chips/center/mycourse.vpage" style="display: block;">
            <li>
                <span class="pic pic02"></span>
                <span class="title">我的课程</span>
            </li>
            </a>
            <a href="/chips/center/invite_award_activity.vpage">
            <li>
                <span class="pic pic03"></span>
                <span class="title">推荐有奖</span>
            </li>
            </a>
            <li class="logout">
                <span class="pic pic04"></span>
                <span class="title">重新登录</span>
            </li>
        </ul>
    </div>
</div>


<!-- 弹窗 -->
<div class="personalPop">
    <div class="popInner">
        <div class="closeBtn"></div>
        <div class="popContent">
            <p class="title">你的老师是<span class="teacherName">Hailey</span></p>
            <div class="codeImg">
                <img src="/public/images/parent/chips/teacherCode.png" alt="">
            </div>
            <p class="addTip">长按上图 加我好友</p>
            <p class="wxNumber">你也可以通过微信号添加：<span>frenchfriesteacher01</span></p>
            <p class="test">老师会在1～3天内通过验证</p>
            <p class="test">添加老师出现问题，可以在公众号内留言</p>
        </div>
    </div>
</div>

<script type="text/javascript">
    function pageLog(){
        require(['logger'], function(logger) {
            // 个人中心页_被加载
            logger.log({
                module: 'm_XzBS7Wlh',
                op: 'index_load'
            })
        })
    }
</script>

</@layout.page>

<#--</@chipsIndex.page>-->
