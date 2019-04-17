<#import '../layout.ftl' as layout>
<@layout.page className='PersonQuestion bg-fff' title="常见问题与解答" pageJs="ucenter" specialCss="skin2"  specialHead='
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
    <meta name="format-detection" content="telephone=no" />
    <meta name="format-detection" content="email=no" />
    <meta name="apple-mobile-web-app-status-bar-style" content="black" />
    <title>常见问题与解答</title>
'>
<#assign questionInfos = [
    {
        "title" : "账号及登录问题",
        "secondMenus" : [
            "我下载了家长通APP，可以注册学生账号吗？",
            "如何下载一起作业学生APP?",
            "在家长通APP如何修改孩子密码呢？",
            "一位家长最多可以关注几个孩子？怎么关注？",
            "家长关注了多个孩子，怎么切换到其他孩子的账号？",
            "如何修改孩子名字",
            "如何修改家长头像"
        ],
        "questionType" : "question_account",
        "destId":"9101"
    },
    {
        "title" : "作业使用问题",
        "secondMenus" : [
            "1.手机做作业提示录不上音或提示没有录音权限怎么办？",
            "2.手机做作业提示加载中、读取作业状态失败、页面白屏等怎么办？",
            "3.手机APP如何补做作业？",
            "4.因网络中断作业是否能重做？",
            "5.如何在家长通上完成作业？",
            "6.作业显示未完成是怎么回事？",
            "7.课本随身听如何更换教材？",
            "8.家长通里面的课本随身听是什么？",
            "9.没有作业时，我想做更多的练习怎么办？"
        ],
        "questionType" : "question_homework",
        "destId":"9102"
    },
    {
        "title" : "查看作业成绩及报告",
        "secondMenus" : [
            "1.如何在家长通查看学生的作业情况",
            "2.如何听孩子的录音",
            "3.怎么查看孩子的错题"
        ],
        "questionType" : "",
        "destId":"9102"
    },
    {
        "title" : "学豆相关",
        "secondMenus" : [
            "1.在家长通如何查看学生近期获得学豆的记录？",
            "2.学生获得的学豆会清零吗？"
        ],
        "questionType" : "question_award",
        "destId":"9103"
    },
    {
        "title" : "奖品相关",
        "secondMenus" : [
            "1.当月兑换的奖品什么时候送到？",
            "2.奖品出现质量问题的时候怎么办？",
            "3.兑换奖品的时候提示【老师没有填写地址】我应该怎么办？",
            "4.家长通可以帮学生兑换奖品吗？"
        ],
        "questionType" : "question_award",
        "destId":"9103"
    },
    {
        "title" : "家长奖励",
        "secondMenus" : [
            "1.如何设置目标？",
            "2.如何增加进度、得到奖励"
        ],
        "questionType" : "question_award",
        "destId":"9102"
    },
    {
        "title" : "学习应用推荐",
        "secondMenus" : [
            "1.自学应用如何开通？",
            "2.我购买的自学产品为什么不能在家长通使用？",
            "3.购买自学产品错误怎么办？"
        ],
        "questionType" : "",
        "destId":"9103"
    },
    {
        "title" : "其他",
        "secondMenus" : [
            "如何参与班级群聊？",
            "如果我有多个孩子，怎样能够分别进入班级群聊？",
            "如何查看老师发布的作业及其他通知？",
            "教育资讯推送规则？",
            "没有找到我的问题，点击这里咨询人工客服"
        ],
        "questionType" : "question_other",
        "destId":"9103"
    },
    {
        "title" : "建议与反馈",
        "secondMenus" : [
            "点击这里，给我们提建议"
        ],
        "questionType" : "give_us_advice",
        "destId":"9100"
    }

]>
<script>
    <#if (source!'NONE') == 'book_listen'>
        location.replace(encodeURI('/parentMobile/ucenter/questionDetail.vpage?hash=7.课本随身听如何更换教材？&qs_type=question_homework&dest_id=9102&type_title=作业使用问题'))
    </#if>
    <#--改版的样式，不适用adapt-->
    window.notUseAdapt=true;
</script>
<div class="proSolutions-box doQuestionTabs">
    <ul>
    <#list questionInfos as questionInfo>
        <li class="doQuestionTab">
            <#if questionInfo.secondMenus?size gt 0>
                <a href="javascript:void(0);" class="ps-title">${questionInfo.title}</a>
                <ul class="listLevel"><!--二级列表通过active类来控制-->
                    <#list questionInfo.secondMenus as menu>
                        <li>
                            <#if menu=="点击这里，给我们提建议">
                                <a href="/view/mobile/parent/send_question?dest_id=${questionInfo.destId}&qs_type=${questionInfo.questionType}" class="doTrack" data-track = "m_1dib82tl|o_U0Ld05Ch">${menu}</a>
                            <#elseif menu="没有找到我的问题，点击这里咨询人工客服">
                                <a href="/view/mobile/parent/send_question?dest_id=${questionInfo.destId}&qs_type=${questionInfo.questionType}">${menu}</a>
                            <#else>
                                <a href="/parentMobile/ucenter/questionDetail.vpage?hash=${menu}&qs_type=${questionInfo.questionType}&dest_id=${questionInfo.destId}&type_title=${questionInfo.title}">${menu}</a>
                            </#if>
                        </li>
                    </#list>
                </ul>
            <#else>
                <a href="/parentMobile/ucenter/questionDetail.vpage?hash=${questionInfo.title}&qs_type=${questionInfo.questionType}&dest_id=${questionInfo.destId}&type_title=${questionInfo.title}" class="oneItem">${questionInfo.title}</a>
            </#if>
        </li>
    </#list>
    </ul>
</div>
<p class="hide doAutoTrack" data-track = "m_1dib82tl|o_v0v6iOlB"></p>
</@layout.page>
