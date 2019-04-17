<#import "../layout.ftl" as layout>
<@layout.page title="薯条英语" pageJs="learning_duration">
    <@sugar.capsule css=['learning_duration'] />
<#-- // #dc8a57 #fae155 -->
<style>
    html, body {
        height: 100%;
        margin: 0;
        padding: 0;
        background-color: #fffcf7;
    }
    .option {
        width: 80%;
        margin: 0 auto;
        padding: 20px 0;
        text-align: center;
        border: 3px #adadad solid;
        color: #adadad;
        margin: 30px;
        background-color: #fff;
        margin: 30px auto;
    }
    .selected {
        border: 3px #dc8a57 solid;
        color: #dc8a57;
        position: relative;
    }
    .selected:after {
        content: '\2713';
        width: 20px;
        height: 20px;
        border-radius: 20px;
        border: 3px #dc8a57 solid;
        display: inline-block;
        font-size: 0.8rem;
        position: absolute;
        right: -12px;
        bottom: -12px;
        background-color: #fff;
    }
    .btn {
        background-color: #fae155;
        padding: 15px 0;
        text-align: center;
        width: 85%;
        margin: 0 auto;
        color: #464544;
        border-radius: 30px;
        margin-top: 20px;
    }
</style>
<div style="height: 100%;display: flex;justify-content: center;flex-direction: column;">
    <div style="padding-left: 5%;font-size: 1.2rem;">请选择学习英语的年限：</div>
    <div class="option" data-choice="LESS">学习英语一年以下</div>
    <div class="option" data-choice="MORE">学习英语一年以上</div>
    <div class="btn">继续</div>
</div>
<script type="application/javascript">
    var refer = "${refer !''}";
    var channel = "${channel !''}";
    var type = "${type !''}";
    var inviter = ${inviter !0};
</script>
</@layout.page>