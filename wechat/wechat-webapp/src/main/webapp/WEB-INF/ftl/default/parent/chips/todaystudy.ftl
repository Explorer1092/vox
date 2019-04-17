<#import "../layout.ftl" as layout>
<@layout.page title="今日学习内容" pageJs="chipsTodayStudy">
    <@sugar.capsule css=["chipsTodayStudy"] />

<style>
[v-cloak]{
    display: none;
}
</style>

<div id="data" data-unitid="${unitId!}" data-clazzid="${clazzId!}"></div>

<div class="today_study" v-cloak id="today_study">
    <div class="container">
        <div class="header">
            <p>{{ data.title }}</p>
        </div>

        <div class="dialog_box">
            <div class="portrait">
                <div class="img">
                    <img src="/public/images/parent/chips/ts_avator_winston.png" alt="portrait">
                </div>
                <p>Winston</p>
            </div>
            <div class="dialog_box_container">
                <div class="arrow">
                    <span></span>
                </div>
                <div class="title">
                    <p>#今日主题#</p>
                </div>
                <div class="content">
                    <p v-html="data.subject"></p>
                </div>
            </div>
        </div>

        <div class="dialog_box">
            <div class="portrait">
                <div class="img">
                    <img src="/public/images/parent/chips/ts_avator_winston.png" alt="portrait">
                </div>
                <p>Winston</p>
            </div>
            <div class="dialog_box_container">
                <div class="arrow">
                    <span></span>
                </div>
                <div class="title">
                    <p>#今日优秀视频#</p>
                </div>
                <div class="content">
                    <p v-html="data.videoDesc"></p>
                </div>
            </div>
        </div>

        <div class="video_box">
            <div class="video_con">
                <video controls :src="data.videoUrl" :poster="data.videoImg"></video>
                <#--<img src="/public/images/parent/chips/ts_ic_report_video_play.png" alt="">-->
            </div>
        </div>

        <div class="dialog_box">
            <div class="portrait">
                <div class="img">
                    <img src="/public/images/parent/chips/ts_avator_winston.png" alt="portrait">
                </div>
                <p>Winston</p>
            </div>
            <div class="dialog_box_container">
                <div class="arrow">
                    <span></span>
                </div>
                <!--<div class="title">-->
                <!--<p>#今日主题#</p>-->
                <!--</div>-->
                <div class="content">
                    <p v-html="data.videoContent"></p>
                </div>
            </div>
        </div>

        <div class="dialog_box">
            <div class="portrait">
                <div class="img">
                    <img src="/public/images/parent/chips/ts_avator_winston.png" alt="portrait">
                </div>
                <p>Winston</p>
            </div>
            <div class="dialog_box_container">
                <div class="arrow">
                    <span></span>
                </div>
                <div class="title">
                    <p>#今日彩蛋#</p>
                </div>
                <div class="content">
                    <p v-html="data.eggContent"></p>
                </div>
            </div>
        </div>

        <div class="video_box">
            <div class="video_con">
                <video controls :src="data.eggVideoUrl" :poster="data.eggImg"></video>
                <#--<img src="/public/images/parent/chips/ts_ic_report_video_play.png" alt="">-->
            </div>
        </div>

        <div class="dialog_box">
            <div class="portrait">
                <div class="img">
                    <img src="/public/images/parent/chips/ts_avator_winston.png" alt="portrait">
                </div>
                <p>Winston</p>
            </div>
            <div class="dialog_box_container">
                <div class="arrow">
                    <span></span>
                </div>
                <div class="title">
                    <p>#热度排行榜#</p>
                </div>
                <div class="content">
                    <p v-html="data.hotContent"></p>
                </div>
            </div>
        </div>



        <div class="rank_box" v-for="item in data.hotRankData">
            <div class="rank_header">
                <div class="rank_img">
                    <div class="rank_img_common" :class="{first:item.rank === 1,second:item.rank === 2,third:item.rank === 3}"></div>
                </div>
                <p class="title">{{ item.title }}</p>
                <a :href="item.url" style="display: inline-flex;"><div class="rank_play_video"></div></a>
            </div>
            <div class="rank_content">
                <p>
                    <span>@{{ item.name }}</span>
                    {{ item.content }}
                </p>
            </div>
        </div>


        <template v-if="data.hotRankData.length === 0">
            <div class="rank_box">
                <div class="rank_header">
                    <div class="rank_img">
                        <div class="rank_img_common first"></div>
                    </div>
                    <p class="title">薯条英语0806期第一名</p>
                    <a href="#" style="display: inline-flex;"><div class="rank_play_video"></div></a>
                </div>
                <div class="rank_content">
                    <p>
                        <span>@知名的葫芦</span> 你这个英俊、帅气、可爱的孩子，给老师最深的印象就是流畅自然的对话~
                    </p>
                </div>
            </div>
            <div class="rank_box">
                <div class="rank_header">
                    <div class="rank_img">
                        <div class="rank_img_common second"></div>
                    </div>
                    <p class="title">薯条英语0806期第二名</p>
                    <a href="#" style="display: inline-flex;"><div class="rank_play_video"></div></a>
                </div>
                <div class="rank_content">
                    <p>
                        <span>@知名的葫芦</span> 你这个英俊、帅气、可爱的孩子，给老师最深的印象就是流畅自然的对话~
                    </p>
                </div>
            </div>
            <div class="rank_box">
                <div class="rank_header">
                    <div class="rank_img">
                        <div class="rank_img_common third"></div>
                    </div>
                    <p class="title">薯条英语0806期第三名</p>
                    <a href="#" style="display: inline-flex;"><div class="rank_play_video"></div></a>
                </div>
                <div class="rank_content">
                    <p>
                        <span>@知名的葫芦</span> 你这个英俊、帅气、可爱的孩子，给老师最深的印象就是流畅自然的对话~
                    </p>
                </div>
            </div>
        </template>

        <p class="more_rank">
            <a href="/chips/center/ranking.vpage?clazz=${clazzId!}&id=${unitId!}" style="color: inherit;height: 100%;width: 100%;display: inline-block;">更多排名看这里</a>
        </p>

        <div class="dialog_box">
            <div class="portrait">
                <div class="img">
                    <img src="/public/images/parent/chips/ts_avator_winston.png" alt="portrait">
                </div>
                <p>Winston</p>
            </div>
            <div class="dialog_box_container">
                <div class="arrow">
                    <span></span>
                </div>
                <div class="title">
                    <p>#学习数据总结#</p>
                </div>
                <div class="content">
                    <p>{{ data.summaryContent }}</p>
                </div>
            </div>
        </div>

        <div class="grade_box">
            <div class="grade_container">
                <div class="grade_item">
                    <p class="grade_sign right_line">A</p>
                    <p class="grade_num">{{ data.level_a }}</p>
                </div>
                <div class="grade_item">
                    <p class="grade_sign right_line">B</p>
                    <p class="grade_num">{{ data.level_b }}</p>
                </div>
                <div class="grade_item">
                    <p class="grade_sign">C</p>
                    <p class="grade_num">{{ data.level_c }}</p>
                </div>
            </div>
        </div>

        <div class="data_analysis">
            <div class="data_analysis_content">
                <p class="title">点击查看Ray老师的详细数据分析↓</p>
                <div class="content">
                    <div class="left">
                        <div class="img">
                            <img src="/public/images/parent/chips/ts_avator_ray.png" alt="">
                        </div>
                        <p>Ray</p>
                    </div>
                    <div class="right">
                        <p><a :href="data.summaryLink" style="color: inherit;height: 100%;width: 100%;display: inline-block;text-decoration: underline;">旅行口语-学习数据统计-Day{{ data.day }}通过薯条英语的数据统计，帮助孩子更精确、更有效的提升英语能力和成绩。</a></p>
                    </div>
                </div>
            </div>
        </div>

        <div class="dialog_box">
            <div class="portrait">
                <div class="img">
                    <img src="/public/images/parent/chips/ts_avator_winston.png" alt="portrait">
                </div>
                <p>Winston</p>
            </div>
            <div class="dialog_box_container">
                <div class="arrow">
                    <span></span>
                </div>
                <div class="title">
                    <p>#小提示#</p>
                </div>
                <div class="content">
                    <p>
                        从哪里能看到学习等级（带有ABC级别的学习报告）？<br />
                        第一步：关注“薯条英语微信公众号”，在右下角点击“个人中心”<br />
                        第二步：用已购买的手机号登录薯条英语<br />
                        第三步：让孩子完成今天课程后，就可以看到今天学习等级啦！<br />
                        注意：只有第一次做且当日完成的时候才有推送；一定要先登录再让孩子做课程哟~如果做完了才登录公众号，会收不到报告。<br />
                        <br>最后的最后，今日特别活动 #推荐有奖# 开启啦<br />
                        只要推荐一名好友成功报名，就可以获得我们的配套电子教材哦<br />
                        <br>【配套电子教材】<br />
                        是每天学习任务的有声读版本，通过完整的呈现对话内容，帮助孩子磨耳朵，通过反复听预习或者复习当天的学习内容，巩固学习的知识。<br />
                        <br>【如何学习】<br />
                        老师建议在晨起、运动、公交车上等碎片化休息时间播放电子教材，帮助孩子磨耳朵产生语感，从熟悉语感到脱口而出<br />
                    </p>
                </div>
            </div>
        </div>


        <div class="video_box">
            <div class="video_con">
                <video controls :src="data.tipsVideoUrl" :poster="data.tipsVideoImg"></video>
                <#--<img src="/public/images/parent/chips/ts_ic_report_video_play.png" alt="">-->
            </div>
        </div>

        <div class="ebook_container">
            <div class="ebook_content">
                <p>电子教材庐山真面目↑</p>
            </div>
        </div>

        <div class="dialog_box">
            <div class="portrait">
                <div class="img">
                    <img src="/public/images/parent/chips/ts_avator_winston.png" alt="portrait">
                </div>
                <p>Winston</p>
            </div>
            <div class="dialog_box_container">
                <div class="arrow">
                    <span></span>
                </div>
                <!--<div class="title">-->
                <!--<p>#下节课程介绍#</p>-->
                <!--</div>-->
                <div class="content">
                    <p>有全部课程的中文英文和声音哟，预习、复习、过关卡全靠它了~</p>
                </div>
            </div>
        </div>

        <div class="ebook_container">
            <div class="ebook_content">
                <img src="/public/images/parent/chips/ts_img_recommend.png" alt="">
            </div>
        </div>

        <div class="dialog_box">
            <div class="portrait">
                <div class="img">
                    <img src="/public/images/parent/chips/ts_avator_winston.png" alt="portrait">
                </div>
                <p>Winston</p>
            </div>
            <div class="dialog_box_container">
                <div class="arrow">
                    <span></span>
                </div>
                <div class="title">
                    <p>#下节课程介绍#</p>
                </div>
                <div class="content">
                    <p v-html="data.tipsNextClass"></p>
                </div>
            </div>
        </div>

        <p class="footer">-数据由 薯条英语 提供-</p>

    </div>
</div>

</@layout.page>

<#--</@chipsIndex.page>-->
