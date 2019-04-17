<script type="text/html" id="T:CLASS_COURSE_WARE">
    <div class="coursewareBox" data-comment="课件">
        <div class="explainTips" v-if="courseInfoList && courseInfoList.length>0">
            <div class="explainTipsCard" v-bind:style="{'padding-right':isOpen ? '35px':'75px'}">
                <p class="explainCon" v-bind:style="{height:isOpen ? 'auto':'28px'}">目前显示的课件内容，均来自一起作业课件大赛，版权归课件作者所有。
                    访问者可将本网站提供的内容或服务用于个人学习、研究或欣赏，以及其他非商业性或非盈利性用途，但同时应遵守著作权法及其他相关法律的规定，不得侵犯本网站及相关权利人的合法权利。除此以外，将本网站任何内容或服务用于其他用途时，须征得本网站及相关权利人的书面许可，并支付报酬。
                    本网站内容原作者如不愿意在本网站刊登内容，请及时通知本站，予以删除。<i class="tackUp" v-show="isOpen" v-on:click="openOrDown">收起</i></p>
                <i class="tackDown" v-show="!isOpen" v-on:click="openOrDown">展开</i>
            </div>
        </div>
        <ul class="courseCard" v-if="courseInfoList && courseInfoList.length>0">
            <li class="courseSingle" v-for="courseInfo in courseInfoList" v-on:click="previewCourseware(courseInfo.pptCoursewareFile)">
                <div class="coursePic">
                    <img v-bind:src="courseInfo.coverUrl">
                </div>
                <div class="courseInfoCard">
                    <p class="courseCon" v-text="courseInfo.title"></p>
                    <div class="courseInfo">
                        <span class="courseLeft" v-text="courseInfo.schoolName"></span>
                        <span class="courseRight" v-text="courseInfo.teacherName"></span>
                    </div>
                    <p class="courseDate" v-text="courseInfo.createTime"></p>
                    <div class="courseScore">
                        <span class="starBox">
                            <i :class="star <= Math.floor(courseInfo.totalScore/20) ? 'starLighten': 'star'" v-for = "star in 5"></i>
                        </span>
                        <span class="scoreNum" v-text="courseInfo.totalScore"></span>
                        <span class="commentNum" v-text="courseInfo.commentNum+'条评论'"></span>
                    </div>
                </div>
            </li>
        </ul>
        <div v-if="isCourse" class="noCourse">暂无更多课件</div>
    </div>
</script>