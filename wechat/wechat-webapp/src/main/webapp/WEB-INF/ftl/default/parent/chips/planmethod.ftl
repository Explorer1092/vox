<#import "../layout.ftl" as layout>
<@layout.page title="学习方案" pageJs="chipsPlanmethod">
    <@sugar.capsule css=['chipsAll'] />

<style>
    [v-cloak]{
        display: none;
    }
    .colorRed{
        color: #FF0000 !important;
    }
</style>

<div class="planWrap" id="planmethod" v-cloak="">
    <!-- 主要内容 -->
    <div class="planBox">
        <div class="course">- 第 {{ data.rank }} 课 -</div>
        <div class="title">{{ data.title }}</div>

        <!-- 等级A -->
        <div class="place" v-if="data.grade === 'A'">第 {{ data.finishRanking }} 个完成学习，目前排名第 {{ data.scoreRanking }} 名</div>
        <!-- 等级B -->
        <div class="place" v-if="data.grade === 'B'">第 {{ data.finishRanking }} 个完成学习，目前排名第 {{ data.scoreRanking }} 名</div>
        <!-- 等级C -->
        <div class="place" v-if="data.grade === 'C'">第 {{ data.scoreRanking }} 个完成学习，再努力一下，进入排行榜吧！</div>

        <div class="grade">{{ data.grade }}</div>
        <div class="gradeTxt" @click="show_description">等级说明</div>

        <!-- 等级A -->
        <div class="gradeDetail" :class="{colorRed:data.grade === 'C'}">{{ data.summary }}</div>
        <!-- 等级B -->
        <div class="gradeDetail" style="display: none">今天完成得很不错呢！</div>
        <!-- 等级C -->
        <div class="gradeDetail gradeDetail-c" style="display: none">诶，要加油了呢！<br>强烈建议陪孩子再学一遍~</div>

        <div class="abilityBox">
            <div class="abilityTitle">能力概况</div>
            <div class="canvasBox" id="container" style="height:12rem;">

            </div>
        </div>
        <!-- <div class="rewardBtn" v-if="data.grade === 'A'"><a href="/chips/center/reward.vpage?id=${id!''}&book=${book!''}" style="display:inline-block;width: 100%;height: 100%;">神秘奖励</a></div> -->
    </div>

    <!-- 指导方案 -->
    <div class="planGuide" v-if="data.grade !== 'A'">
        <div class="abilityTitle">指导方案</div>
        <div class="keyTitle">今日重点：{{ data.pointAbilityName }}</div>
        <ul class="keyContent">
            <li>{{ data.studyPlan }}</li>
            <#--<li>2.	知道意思后，再点击外教的气泡，多听几遍</li>-->
        </ul>
        <div class="rewardBtn againBtn" v-if="data.grade === 'C'"><a href="/chips/center/tostudy.vpage" style="display:inline-block;width: 100%;height: 100%;">再学一次</a></div>
    </div>

    <!-- 学习历史 -->
    <div class="planHistory">
        <div class="abilityTitle">学习历史</div>
        <div class="canvasContent" id="container2" style="height: 12rem;">

        </div>
    </div>
    <!-- 底部 -->
    <div class="rankingFoot">数据由 薯条英语 提供</div>

    <!-- 等级说明弹窗 -->
    <div class="planPopup" v-if="description_status">
        <div class="popupInner">
            <div class="closeBtn" @click="close_description"></div>
            <div class="popupTitle">等级说明</div>
            <div class="detailTitle">计分基准是按照今日首次学习得分为准</div>
            <ul>
                <li>A 级：大于等于 90 分；</li>
                <li>B 级：大于 40 分小于 90 分；</li>
                <li>C 级：小于等于 40 分。</li>
            </ul>
        </div>
    </div>
</div>

<script type="text/javascript">
    function pageLog(){
        require(['logger'], function(logger) {
            // 今日方案页面_被加载
            logger.log({
                module: 'm_XzBS7Wlh',
                op: 'dailyreport_load'
            })
        })
    }
</script>

</@layout.page>



