<#import "../layout.ftl" as layout>
<@layout.page title="我的优惠券" pageJs="chipsShareRecord">
    <@sugar.capsule css=["chipsShareRecord"] />

<style>

</style>

<input id="bookid" type="hidden" value="${bookId!}">

<div class="award_wrap" id="award">
    <div class="award_heard">
        <div class="l_box">
            <div class="num"><span class="big_text">{{ count*10 }}</span>元 </div>
            <div class="s_text">优惠券 / 累积</div>
        </div>
        <div class="r_box">
            <div class="num"> <span class="big_text">{{ count }}</span>天 </div>
            <div class="s_text">累计打卡 / 天数</div>
        </div>
    </div>
    <div class="award_main">
        <div class="title">打卡记录</div>
        <div class="content">
            <div class="c_list">
                <div v-for="item in records" class="time_box" :class="{already_clock:item.status}">
                    <div class="icon_clock s_text">Day{{ item.title }}</div>
                    <div class="icon_checked"></div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>

</script>

</@layout.page>

<#--</@chipsIndex.page>-->
