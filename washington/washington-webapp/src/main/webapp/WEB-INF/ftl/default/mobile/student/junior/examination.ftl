<#assign extraJs = [
    {
        "path" : "date_diff"
    },
    {
        "path" : "examination"
    }
]>

<#include "./layout.ftl">

<@layout.page className=CONFIG.CSSPRE + 'examination' title="考试" headBlock=headBlock bottomBlock=bottomBlock>
    <#escape x as x?html>
        <script>
            window.exam = {
                id : "${id}"
            };
        </script>
		<div id="examination">
            <div class="studentJuniorSchool-scoreBanner studentJuniorSchool-scoreBanner-blue">
                <div class="text">{{name}}</div>
            </div>
            <div class="studentJuniorSchool-examTime">
                <div class="studentJuniorSchool-examTimeHead">{{ REGISTRABLE ? "距报名" : "距考试" }}截止时间还有：</div>
                <div class="studentJuniorSchool-examTimeBar">
                    <div class="item item-1"> <div>{{date_unit.d}}</div> <div>天</div> </div>
                    <div class="item item-2"> <div>{{date_unit.h}}</div> <div>小时</div> </div>
                    <div class="item item-3"> <div>{{date_unit.m}}</div> <div>分钟</div> </div>
                    <div class="item item-4"> <div>{{date_unit.s}}</div> <div>秒</div> </div>
                </div>
            </div>
            <div class="studentJuniorSchool-footSubmit" v-if="REGISTRABLE" >
                <div class="footInner">
                    <a href="javascript:;" v-on:click="do_sign_up('hello', $event)" class="btnSubmit btnSubmit-green"><span>我要报名</span></a>
                </div>
            </div>
            <div class="studentJuniorSchool-footSubmit" v-else>
                <div class="btnProm">你已成功报名</div>
            </div>
		</div>
		<div class="studentJuniorSchool-examPromFixed">所有考试信息可在“学习记录-测试”找到</div>
    </#escape>
</@layout.page>

