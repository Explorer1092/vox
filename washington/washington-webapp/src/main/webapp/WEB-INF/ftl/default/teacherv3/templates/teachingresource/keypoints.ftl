<script type="text/html" id="T:KEY_POINTS">
    <div class="videoPracticeBox" v-if="wordPackagesList && wordPackagesList.length>0">
        <div class="h-topicPackage">
            <div class="topicBox">
                <ul>
                    <li v-for="(wordPackages,index) in wordPackagesList"
                        v-bind:class="{'active':index == focusCategoryGroupIndex}"
                        v-on:click="categoryGroupClick(index)">
                        <p v-text="wordPackages.videoName"></p>
                    </li>
                </ul>
            </div>
        </div>
        <div class="finR-video">
            <div class="frv-left" data-playst="stop" data-init="1" @click="playVideo(wordPackagesList[focusCategoryGroupIndex].videoId,wordPackagesList[focusCategoryGroupIndex].videoUrl,wordPackagesList[focusCategoryGroupIndex].coverUrl)">
                <img :src="wordPackagesList[focusCategoryGroupIndex].coverUrl" alt="">
                <i class="playVideoBtn"></i>
            </div>
            <div class="frv-right">
                <p v-text="wordPackagesList[focusCategoryGroupIndex].videoSummary"></p>
                <p>解题技巧：</p>
                <p v-text="wordPackagesList[focusCategoryGroupIndex].solutionTracks.join(' ')"></p>
            </div>
        </div>
        <div  id="KEY_POINTS">
            <div class="h-set-homework examTopicBox" v-if="questionInfo && questionInfo.length>0" v-for="(question,index) in questionInfo" v-bind:key="question.id">
                <div class="seth-hd">
                    <p class="fl">
                        <span v-text="question.questionType">解决问题</span>
                        <span v-text="question.difficultyName">容易</span>
                        <span class="noBorder" v-if="question.assignTimes && question.assignTimes > 0 && (!question.teacherAssignTimes || question.teacherAssignTimes == 0)" v-text="'共被使用'+question.assignTimes+'次'"></span>
                    </p>
                    <p class="fr"></p>
                </div>
                <div class="seth-mn">
                    <div class="testPaper-info">
                        <div class="inner">
                            <div class="box">
                                <venus-question
                                        formula-container = "KEY_POINTS"
                                        :objectiveConfig = "type"
                                        :questions = "[questionMap[question.id]]"
                                        :content-id="'question_self' + index + question.id">
                                </venus-question>
                            </div>
                        </div>
                    <#--<div class="linkGroup">
                        <a href="javascript:void(0)" class="viewExamAnswer" @click="lookAnalysis">查看答案解析</a>
                    </div>-->
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>