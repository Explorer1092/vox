<script type="text/html" id="T:INTELLIGENT_TEACHING">
    <div class="speakTestBox" data-comment="讲练测">
        <div class="h-topicPackage">
            <div class="topicBox">
                <ul v-if="intelligentInfo && intelligentInfo.length>0">
                    <li v-for="(intelligent,index) in intelligentInfo"
                        v-bind:class="{'active':index == focusCategoryGroupIndex}"
                        v-on:click="categoryGroupClick(index)">
                        <p v-text="intelligent.title"></p>
                    </li>
                </ul>
            </div>
            <div class="line"></div>
        </div>
        <div class="h-set-homework">
            <div class="seth-hd it-header" v-if="intelligentInfo && intelligentInfo.length>0">
                <span class="packageTitle"  v-text="intelligentInfo[focusCategoryGroupIndex].title"></span>
            </div>
            <div class="seth-mn" v-if="questionInfo && questionInfo.length>0">
                <div class="testPaper-info" id="TEACHING_PACKAGE_DETAIL">
                    <div class="inner" v-for="(question,index) in questionInfo" v-bind:key="question.id">
                        <div class="box">
                            <div class="subjectNum" data-tip="题号"><span v-text="index+1"></span></div>
                            <div class="teachingQuestion">
                                <venus-question
                                        formula-container = "TEACHING_PACKAGE_DETAIL"
                                        :objectiveConfig = "type"
                                        :questions = "[questionMap[question.id]]"
                                        :content-id="'question_self' + index + question.id">
                                </venus-question>
                            </div>
                        </div>
                        <ul class="h-analysis-box2">
                            <li class="postInfo">
                                <div class="c-title">课程辅导 <i class="tag">辅导做错的学生</i></div>
                                <div class="text" v-on:click="previewCourse(question.courseId)">
                                    <div class="coursePic">
                                        <i class="playBtn" style="cursor: pointer;"></i>
                                    </div>
                                    <div class="desc" style="cursor: pointer;"></div>
                                </div>
                                <i class="tagIcon"></i>
                            </li>
                            <li class="postInfo lastChild" v-if="question.postQuestions && question.postQuestions.length > 0">
                                <div class="c-title">测试巩固题 <i class="tag">测试学生能否最终学会</i></div>
                                <div class="context">
                                    <div class="box" v-for="(postQuestion,index) in question.postQuestions">
                                        <div class="subjectNum" data-tip="题号"><span v-text="index+1"></span></div>
                                        <div class="teachingQuestion">
                                            <venus-question
                                                    formula-container = "TEACHING_PACKAGE_DETAIL"
                                                    :objectiveConfig = "type"
                                                    :questions = "[questionMap[postQuestion.id]]"
                                                    :content-id="'question_post' + index + postQuestion.id">
                                            </venus-question>
                                        </div>
                                    </div>
                                </div>
                                <i class="tagIcon"></i>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>