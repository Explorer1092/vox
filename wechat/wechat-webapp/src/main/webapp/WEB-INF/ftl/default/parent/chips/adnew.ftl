<#import "../layout.ftl" as layout>
<@layout.page title="薯条英语" pageJs="chipsAdNew">
    <@sugar.capsule css=["chipsAdNew"] />

<style>
    [v-cloak] { display: none }
    .fixd_top{
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        z-index: 999;
    }
    .disabled{
        background: #ddd !important;
        color: #bbb !important;
    }
    .newFooter {
        height: 2.4rem;
    }
</style>

<div class="adNewWrap" id="adnew" v-cloak="">
    <input type="hidden" id="inviter" value="${inviter!}">
    <div class="newHead">
        <img src="/public/images/parent/chips/adNew-headbg-30.png" alt="">
    </div>
    <div class="newMain">
        <ul class="newNav">
            <li @click="changeTab('intro')" :class="{active:sign==='intro'}">课程介绍</li>
            <li @click="changeTab('list')" :class="{active:sign==='list'}">课程列表</li>
        </ul>
        <!-- part01 课程介绍 -->
        <div class="courseIntro paddingTop" v-if="sign==='intro'">
            <!-- 课程特色 -->
            <div class="introTitle">课程特色</div>
            <div class="introDetail">
                <div class="title">课程划分等级，不同能力等级的孩子都可以找到适合自己的课程；</div>
                <div class="introBox">
                    <div class="topTable">
                        <div class="rank">报告等级</div>
                        <div class="gradeList gradeList01">
                            <div class="name">一级</div>
                            <div class="column"><span class="progress"></span></div>
                        </div>
                        <div class="gradeList gradeList02">
                            <div class="name">二级</div>
                            <div class="column"><span class="progress"></span></div>
                        </div>
                        <div class="gradeList gradeList03">
                            <div class="name">三级</div>
                            <div class="column"><span class="progress"></span></div>
                        </div>
                    </div>
                    <div class="gradeIntro">
                        <ul>
                            <li>
                                <div class="col01"></div>
                                <div class="col01 font-w">Grade1</div>
                                <div class="col01 font-w">Grade2</div>
                                <div class="col01 font-w">Grade3</div>
                            </li>
                            <li>
                                <div class="col01">词汇量</div>
                                <div class="col01 font-w">300以内</div>
                                <div class="col01 font-w">300-700</div>
                                <div class="col01 font-w">700-1000</div>
                            </li>
                            <li>
                                <div class="col01">GESE考试用时</div>
                                <div class="col01">5minutes</div>
                                <div class="col01">6minutes</div>
                                <div class="col01">7minutes</div>
                            </li>
                            <li>
                                <div class="col01">难度对应年级</div>
                                <div class="col01">4年级</div>
                                <div class="col01">6年级</div>
                                <div class="col01">初中2年级</div>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
            <div class="introDetail">
                <div class="title">对标三一口语考试相应能力等级，让口语学习更具有目的性；</div>
                <div class="pic"></div>
            </div>
            <!-- 与短期课有什么不同 -->
            <div class="introTitle title02">与短期课有什么不同</div>
            <div class="diff-section">
                <div class="sec-title">增加口语评测环节</div>
                <div class="sec-detail">外教口语考官模拟三一口语测试真题，检验口语学习效果。</div>
            </div>
            <div class="diff-section">
                <div class="sec-title">根据等级要求设计对话</div>
                <div class="sec-detail">根据三一口语考试（GESE）各等级测试话题场景要求设计对话内容。</div>
            </div>
            <div class="diff-section">
                <div class="sec-title">增加练习环节</div>
                <div class="sec-detail">根据三一口语考试（GESE）要求补充词汇量和重点句型练习。</div>
            </div>
            <div class="diff-section">
                <div class="sec-title">1对1答疑</div>
                <div class="sec-detail">专业英语班主任1对1解决发音问题。</div>
            </div>
            <!-- 意志力学习礼包 -->
            <div class="introTitle title03">意志力学习礼包</div>
            <div class="gift-title">鼓励孩子坚持每天学习，养成每天开口好习惯，完成80%的课程内容就可以获得意志力礼包。</div>
            <div class="gift-pic"></div>
        </div>
        <!-- part02 课程列表 -->
        <div class="courseList paddingTop"  v-if="sign==='list'">
            <ul class="listNav">
                <li @click="changeGrade(1)" :class="{active:grade === 1}" v-if="levelName.indexOf(1)>-1">Grade1</li>
                <li @click="changeGrade(2)" :class="{active:grade === 2}" v-if="levelName.indexOf(2)>-1" >Grade2</li>
                <li @click="changeGrade(3)" :class="{active:grade === 3}" v-if="levelName.indexOf(3)>-1" >Grade3</li>
            </ul>
            <div class="gradeBox">
                <div class="gradeInfo">
                    <div class="title" style="font-size: 0.9rem;">{{ title }}（上）</div>
                    <#--<div class="text">适合人群：4-6岁小朋友</div>-->
                    <div class="text">词汇要求：{{ course[grade].words }}</div>
                    <div class="text">学习时间：{{ course[grade].studyTime }}</div>
                </div>
                <div class="gradeSchedule">
                    <ul class="weekBox">
                        <li v-for="item in course[grade].up">
                            <div class="col01">{{ item.text1 }}</div>
                            <div class="col02">{{ item.text2 }}</div>
                            <div class="col03">{{ item.text3 }}</div>
                        </li>
                    </ul>
                    <#--<div class="tips">备注：W=Week</div>-->
                </div>
            </div>
            <!-- Grade1（下） -->
            <div class="gradeBox">
                <div class="gradeInfo">
                    <div class="title" style="font-size: 0.9rem;">{{ title }}（下）</div>
                    <#--<div class="text">适合人群：4-6岁小朋友</div>-->
                    <div class="text">词汇要求：{{ course[grade].words }}</div>
                    <div class="text">学习时间：{{ course[grade].studyTime }}</div>
                </div>
                <div class="gradeSchedule">
                    <ul class="weekBox">
                        <li v-for="item in course[grade].down">
                            <div class="col01">{{ item.text1 }}</div>
                            <div class="col02">{{ item.text2 }}</div>
                            <div class="col03">{{ item.text3 }}</div>
                        </li>
                    </ul>
                    <#--<div class="tips">备注：W=Week</div>-->
                </div>
            </div>
        </div>
    </div>
    <!-- 底部 01-->
    <div class="newFooter" v-if="sign==='intro'">
        <div class="footInner">
            <!-- 购买按钮 -->
            <div class="buyBox">
                <div class="leaveNum">剩余名额：<span>{{ productData.remaining }}</span></div>
                <div class="buyBtn" :class="{disabled:productData.remaining <= 0}">
                    <span @click="changeTab('list','buy')">立即购买</span>
                </div>
            </div>
        </div>
    </div>
    <!-- 底部02 -->
    <div class="newFooter" style="display: none">
        <div class="footInner">
            <!-- 报名截止 -->
            <div class="action">原价599，报名截止7月20日</div>
            <!-- 购买按钮 -->
            <div class="buyBox">
                <div class="leaveNum">剩余名额：<span>50</span></div>
                <div class="buyBtn">
                    <span class="price">¥499</span>
                    <span>立即购买</span>
                </div>
            </div>
        </div>
    </div>
    <!-- 底部03 -->
    <div class="newFooter" v-if="sign==='list'">
        <!-- 遮罩 -->
        <div class="popup" :style="{position: popup}" @click.stop="closePopup">
            <div class="footInner">
                <!-- 报名截止 -->
                <#--<div class="action">原价{{ originalPrice }}，现价{{ price }}</div>-->
                <!-- 选择年级 -->
                <div class="chooseGrade" v-if="degreeStatus">
                    <template v-for="(item,index) in productData['grade'+grade].products">
                        <div class="grade" @click.stop="select(index)" :class="{active:productIndex===index}">{{ item.productName }}</div>
                    </template>
                    <#--<div class="grade active" @click.stop="selectAll('up')">{{ productNameUp }}</div>-->
                    <#--<div class="grade" @click.stop="selectAll('all')" :class="{active:type === 'all'}">{{ productNameDown }}</div>-->
                    <#--<div class="grade" @click.stop="selectAll('all')" :class="{active:type === 'all'}">{{ productNameDown }}</div>-->
                </div>
                <!-- 购买按钮 -->
                <div class="buyBox">
                    <div class="leaveNum">剩余名额：<span>{{ single_remaining }}</span></div>
                    <div class="buyBtn" :class="{disabled:single_remaining <= 0}">
                        <span class="price">¥{{ price }}</span>
                        <span @click.stop="buy">立即购买</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

</@layout.page>

<#--</@chipsIndex.page>-->
