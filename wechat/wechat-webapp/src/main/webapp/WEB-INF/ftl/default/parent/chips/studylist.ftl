<#import "../layout.ftl" as layout>
<@layout.page title="学习列表" pageJs="chipsStudyList">
    <@sugar.capsule css=["chipsAll"] />

<style>
    [v-cloak]{
        display:none;
    }
    .studyWrap {
        position: relative;
        padding: 1.4rem 1.4rem 0 1.075rem;
    }
</style>

<div class="studyWrap" id="studyList" v-cloak>
    <#--<div class="task-returnBtn"></div>-->

    <div class="studyPoints">
        <div class="pointRight">
            <div class="studyTitle">今日学习重点</div>
            <ul>
                <li>了解打包行李的清单</li>
                <li>知道如何询问物品在哪</li>
            </ul>
        </div>
    </div>
        <#--<div class="studyTitle">学习流程</div>-->
    <div class="studyProcess">

        <div class="processLeft">
            <!-- complete完成  spot点正在进行 lock未解锁 第二个元素加state02 -->
            <span v-for="(item,index) in lessons" class="state" :class="{lock:item.isLock,complete:item.finished,state02:index === 1,spot:!item.isLock && !item.finished}" :key="index"></span>
        </div>
        <div class="processRight">
            <ul>
                <!--
                    默认是白色完成 进行中是active  未解锁disabled
                    starsBox 星星评分
                -->
                <template v-for="(item,index) in lessons" :key="index">
                    <a :href="item.isLock ? 'javascript:void(0);' : url[index]">
                        <li :class="{active:(!item.isLock && !item.finished),disabled:item.isLock}">
                            <div class="stateBox" v-if="(!item.isLock && item.finished)">
                                <span class="completeState">已完成</span>
                            </div>
                            <div class="stateBox" v-if="(!item.isLock && !item.finished)">
                                <span class="completeState">进行中</span>
                                <span class="lookBtn" style="display: none;">查看对话实录></span>
                            </div>
                            <div class="stateBox" v-if="item.isLock">
                                <span class="completeState">未开始</span>
                            </div>
                            <span>{{ item.name }}</span>
                            <div class="starsBox" v-if="!item.isLock && item.finished">
                                <span class="star" :class="{starActive:item.star > 0}"></span>
                                <span class="star" :class="{starActive:item.star > 1}"></span>
                                <span class="star" :class="{starActive:item.star > 2}"></span>
                            </div>
                        </li>
                    </a>
                </template>
            </ul>

        </div>
    </div>
    <div class="studySummary" style="display: none;">今日学习总结</div>
</div>

</@layout.page>

<#--</@chipsIndex.page>-->
