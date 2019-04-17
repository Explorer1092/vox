<#import "../layout.ftl" as layout>
<@layout.page title="薯条英语开课小调查" pageJs="chipsSurvey">
    <@sugar.capsule css=["chipsSurvey"] />

<style>
    [v-cloak] { display: none }
    #chips_survey{
        height:100%;
        width:100%;
    }
</style>

<div id="chips_survey" v-cloak>
    <div class="questionWrap">
        <div class="questionHead">
            <div class="title">薯条英语开课小调查</div>
            <div class="intro">亲爱的爸爸妈妈，欢迎您与可爱的宝贝一起加入薯条英语大家庭，开启全新的学习之旅！在这里，小薯条想问您几个小问题，以帮助小薯条更加了解孩子，更好地为孩子做学习规划~您准备好了吗？我们开始吧！</div>
        </div>
        <div class="questionMain">
            <!-- 1 unselected -->
            <div class="questionList" v-for="(item,index) in list" v-bind:class="{unselected:item.red_sign == 'red'}">
                <div class="listTitle"><i>*</i>{{ item.index + 1 }}.{{ item.title }}<span>{{ item.ps }}</span></div>
                <ul class="answerBox">

                    <li v-for="(sub_item,sub_index) in item.options" @click="select(item.index,sub_index,item.type)" style="display: flex">
                        <span class="choice" v-if="item.type != 'multi_select'" v-bind:class="{active:sub_item.is_active}"></span>
                        <span class="choice"v-if="item.type == 'multi_select'"  v-bind:class="{active:sub_item.is_active}"></span>
                        <span class="answer">{{ sub_item.text }}</span>
                    </li>
                </ul>
            </div>
        </div>
        <div class="questionSubmit" @click="submit">提交</div>
    </div>
    <!-- 提示弹窗 -->
    <div class="questionPopup" v-if="toast_status">
        <div class="tipsBox">请完成作答哦～</div>
    </div>
</div>

</@layout.page>

<#--</@chipsIndex.page>-->
