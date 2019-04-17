<#import "../layout.ftl" as layout>
<@layout.page title="今日学习内容" pageJs="chipsTodayStudyNormal">
    <@sugar.capsule css=["chipsTodayStudy"] />

<style>
[v-cloak]{
    display: none;
}
</style>

<div class="today_study" v-cloak id="today_study_normal">
    <div class="container">
        <div class="header">
            <p>{{ title }}</p>
        </div>

        <div v-if="res.firstCommentDesc" class="dialog_box">
            <div class="portrait">
                <div class="img">
                    <img src="/public/images/parent/chips/ts_avator_winston.png" alt="portrait">
                </div>
                <p>Winston</p>
            </div>
            <div class="dialog_box_container dialog_box_iconArrow">
                <div class="title">
                    <p>#今日对话第一句#</p>
                </div>
                <div class="content">
                    <p v-html="res.firstCommentDesc"></p>
                </div>
            </div>
        </div>
        <audio v-if="res.firstAudioUrl" v-bind:src="res.firstAudioUrl" controls style="margin-top: 20px;"></audio>
        <div v-if="res.firstGrammaticalExplanation" class="dialog_box">
            <div class="portrait">
                <div class="img">
                    <img src="/public/images/parent/chips/ts_avator_winston.png" alt="portrait">
                </div>
                <p>Winston</p>
            </div>
            <div class="dialog_box_container dialog_box_iconArrow">
                <div class="content">
                    <p v-html="res.firstGrammaticalExplanation"></p>
                </div>
            </div>
        </div>
        <div v-if="res.firstKnowledgeStation" class="dialog_box">
            <div class="portrait">
                <div class="img">
                    <img src="/public/images/parent/chips/ts_avator_winston.png" alt="portrait">
                </div>
                <p>Winston</p>
            </div>
            <div class="dialog_box_container dialog_box_iconArrow">
                <div class="content">
                    <p v-html="res.firstKnowledgeStation"></p>
                </div>
            </div>
        </div>

        <div v-if="res.secondCommentDesc" class="dialog_box">
            <div class="portrait">
                <div class="img">
                    <img src="/public/images/parent/chips/ts_avator_winston.png" alt="portrait">
                </div>
                <p>Winston</p>
            </div>
            <div class="dialog_box_container dialog_box_iconArrow">
                <div class="title">
                    <p>#今日对话第二句#</p>
                </div>
                <div class="content">
                    <p v-html="res.secondCommentDesc"></p>
                </div>
            </div>
        </div>
        <audio v-if="res.secondAudioUrl" v-bind:src="res.secondAudioUrl" controls style="margin-top: 20px;"></audio>
        <div v-if="res.secondGrammaticalExplanation" class="dialog_box">
            <div class="portrait">
                <div class="img">
                    <img src="/public/images/parent/chips/ts_avator_winston.png" alt="portrait">
                </div>
                <p>Winston</p>
            </div>
            <div class="dialog_box_container dialog_box_iconArrow">
                <div class="content">
                    <p v-html="res.secondGrammaticalExplanation"></p>
                </div>
            </div>
        </div>
        <div v-if="res.secondKnowledgeStation" class="dialog_box">
            <div class="portrait">
                <div class="img">
                    <img src="/public/images/parent/chips/ts_avator_winston.png" alt="portrait">
                </div>
                <p>Winston</p>
            </div>
            <div class="dialog_box_container dialog_box_iconArrow">
                <div class="content">
                    <p v-html="res.secondKnowledgeStation"></p>
                </div>
            </div>
        </div>

        <div v-if="res.thirdCommentDesc" class="dialog_box">
            <div class="portrait">
                <div class="img">
                    <img src="/public/images/parent/chips/ts_avator_winston.png" alt="portrait">
                </div>
                <p>Winston</p>
            </div>
            <div class="dialog_box_container dialog_box_iconArrow">
                <div class="title">
                    <p>#今日对话第三句#</p>
                </div>
                <div class="content">
                    <p v-html="res.thirdCommentDesc"></p>
                </div>
            </div>
        </div>
        <audio v-if="res.thirdAudioUrl" v-bind:src="res.thirdAudioUrl" controls style="margin-top: 20px;"></audio>
        <div v-if="res.thirdGrammaticalExplanation" class="dialog_box">
            <div class="portrait">
                <div class="img">
                    <img src="/public/images/parent/chips/ts_avator_winston.png" alt="portrait">
                </div>
                <p>Winston</p>
            </div>
            <div class="dialog_box_container dialog_box_iconArrow">
                <div class="content">
                    <p v-html="res.thirdGrammaticalExplanation"></p>
                </div>
            </div>
        </div>
        <div v-if="res.thirdKnowledgeStation" class="dialog_box">
            <div class="portrait">
                <div class="img">
                    <img src="/public/images/parent/chips/ts_avator_winston.png" alt="portrait">
                </div>
                <p>Winston</p>
            </div>
            <div class="dialog_box_container dialog_box_iconArrow">
                <div class="content">
                    <p v-html="res.thirdKnowledgeStation"></p>
                </div>
            </div>
        </div>


        <p class="footer">-数据由 薯条英语 提供-</p>
    </div>
</div>
</@layout.page>

<#--</@chipsIndex.page>-->
