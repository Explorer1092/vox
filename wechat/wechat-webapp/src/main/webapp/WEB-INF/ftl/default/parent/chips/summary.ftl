<#import "../layout.ftl" as layout>
<@layout.page title="总结" pageJs="chipsSummary">
    <@sugar.capsule css=["chipsAll"] />

<style>
    [v-cloak]{display: none;}
    .pubSummary-head {padding: 3rem 0 .5rem;}
</style>

<div class="publicSummary-warp" id="summary" v-cloak>
    <#--<a href="/chips/center/studylist.vpage"><div class="task-closeBtn"></div></a>-->
    <div class="pubSummary-head">
        <!-- 默认全灰，pubStar01点亮1颗 pubStar02点亮2颗 pubStar03点亮3颗 -->
        <div class="pubSummaryStar"  v-bind:class="{pubStar01:star==1,pubStar02:star==2,pubStar03:star==3}"></div>
    </div>
    <div class="pubSummary-main" v-if="from !== 'warm_up'">
        <div class="pubSummaryBox">
            <div class="summaryText">不错呦，继续加油！<br>查看对话实录让自己更进一步！</div>
            <div class="dialogBtn" @click="open">对话实录</div>
        </div>
    </div>
    <div style="height: 10rem;" v-if="from === 'warm_up'" ></div>
    <!-- 情景对话总结按钮 -->
    <a :href="url['scene']" v-if="from === 'scene'">
        <div class="pubBtn">做任务 GO</div>
    </a>
    <!-- 任务总结页面按钮 -->
    <a :href="url['task']" v-if="from === 'task'">
        <div class="pubBtn">完成</div>
    </a>
    <!-- 跟读总结页面按钮 -->
    <a :href="url['warm_up']" v-if="from === 'warm_up'">
        <div class="pubBtn">继续 GO！</div>
    </a>
</div>

<script type="text/javascript">

</script>

</@layout.page>

<#--</@chipsIndex.page>-->
