<#import "../layout.ftl" as layout>
<@layout.page title="薯条英语" pageJs="chipsBeNormal">
    <@sugar.capsule css=["chipsBeNormal"] />

<style>
    [v-cloak] { display: none }
    .hide{display: none;}
    .end-date {
        text-align: center;
        font-size: 0.6rem;
        color: #3c3c3c;
        font-weight: bold;
        position: absolute;
        width: 100%;
        bottom: 5%;
        display: flex;
        align-items: center;
        justify-content: center;
    }
    .end-date:before, .end-date:after {
        content: '';
        display: inline-block;
        width: 0.5rem;
        height: 1px;
        background: #3c3c3c;
        margin: 0.3rem;
    }
</style>

    <div id="new_be" class="new_be_warp" v-cloak="">
        <div class="top" style="position: relative;">
            <div class="top_banner"></div>
            <div class="end-date">{{ beginDate | normalTime }}</div>
        </div>

        <div id="middle" class="middle">
            <div id="tab" class="tab" @click="switchTab">
                <div id="introduce" class="tab_item introduce" v-bind:class="{active: tab === 'introduce'}">课程介绍</div>
                <div id="list" class="tab_item list" v-bind:class="{active: tab === 'list'}">课程列表</div>
                <div class="active_bottom" v-bind:class="tab"></div>
            </div>
            <div class="tab_content">
                <div class="content_item course_introduce" v-if="tab==='introduce'">
                    <div class="title">
                        <p class="content">课程特色</p>
                    </div>
                    <div class="item">
                        <p class="subtitle">课程划分等级，不同能力等级的孩子都可以找到适合自己的课程；</p>
                        <img width="100%" src="/public/images/parent/chips/short_5_formal/level.png" alt="">
                    </div>
                    <div class="item">
                        <p class="subtitle">对标三一口语考试相应能力等级，让口语学习更具有目的性；</p>
                        <img width="100%" src="/public/images/parent/chips/short_5_formal/ability.png" alt="">
                    </div>

                    <div class="title">
                        <p class="content">与短期课有什么不同</p>
                    </div>
                    <div class="item">
                        <p class="h2-title">正式课为系统课程</p>
                        <p class="text" style="margin-left: 1.5rem;">根据伦敦三一口语（GESE）各等级测试编写系统体系课程，学完对应课程即可参加三一口语对应级别考试。</p>
                    </div>
                    <div class="item">
                        <p class="h2-title">对接中考听力口语</p>
                        <p class="text" style="margin-left: 1.5rem;">针对中考失分多又难以快速提升的听力口语能力，薯条英语系统课贴近中考题型进行编写，1-5 级可以完全覆盖中考听力口语所有考试范围。</p>
                    </div>
                    <div class="item">
                        <p class="h2-title">充分实战演练</p>
                        <p class="text" style="margin-left: 1.5rem;">每个情景对话都增加巩固训练、每两周针对学过的知识进行一次模拟考，由外教口语考官模拟三一口语考场实战场景，检验口语学习效果。</p>
                    </div>
                    <div class="item">
                        <p class="h2-title">一对一专属服务</p>
                        <p class="text" style="margin-left: 1.5rem;">完成 App 学习内容后，会有专属老师对练习结果给与专业指导，并定期针对对话、模拟考和语法存在的问题给予督导和纠正。</p>
                    </div>

                    <div class="title">
                        <p class="content">配套纸质教材包邮到家</p>
                    </div>
                    <div style="font-size: 0.75rem;color: #4A4A4A;margin: 0 1rem;">系统课配套精美原版教材，包邮到家，先报名先安排配送。</div>
                    <div class="img_box">
                        <img  src="/public/images/parent/chips/new_be_gift.png" alt="">
                    </div>

                    <div class="title">
                        <p class="content">购买须知</p>
                    </div>
                    <div class="item">
                        <p class="text">1.购买后请务必关注【薯条英语公众号】并添加你的【专属班主任老师】;</p>
                        <p class="text">2.课程每天0点开始更新;</p>
                        <p class="text">3.开课2天内无条件退款;</p>
                        <p class="text">4.课程有效期3年.</p>
                    </div>
                </div>
                <div class="content_item course_list" v-if="tab==='list'">
                    <div class="img_box">
                        <img src="/public/images/parent/chips/new_be_grade1.png" alt="">
                        <img src="/public/images/parent/chips/new_be_grade2.png" alt="">
                        <img src="/public/images/parent/chips/new_be_grade3.png" alt="">
                    </div>
                </div>
            </div>
        </div>
        <div class="dialog" @click="close_dialog" v-bind:class="{dialog_show:dialog_show}">
            <div class="content" @click.stop="blank">
                <p class="dialog_title">请选择课程</p>
                <div class="dialog_course">
                    <div class="dialog_course_item" v-for="(item,index) in courseData">
                        <p class="dialog_course_item_title">{{ item.name }}</p>
                        <div class="sub_item" v-for="(sub_item,sub_index) in item.info" v-bind:class="{'active':sign === index+'_'+sub_index}" @click.stop="change_course(index,sub_index)">{{ sub_item.name }}|{{sub_item.courses}}课时</div>
                    </div>
                </div>
                <p class="dialog_time">开课时间：{{ beginDate | normalTime }}</p>
            </div>
        </div>

        <div class="bottom">
            <div class="left">
                <p>剩余名额：{{ surplus }}</p>
            </div>
            <div class="right" v-bind:class="{disabled: dialog_show && surplus === 0}" @click="show_dialog">
                <p><span>¥{{ price }}起</span>&nbsp;限时抢购</p>
            </div>
        </div>
    </div>

</@layout.page>

<#--</@chipsIndex.page>-->
