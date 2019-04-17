<script type="text/html" id="T:WORD_TEACH_AND_PRACTICE">
    <div class="wordTeachBox" v-if="wordsPracticeMap">
        <div class="h-topicPackage">
            <div class="topicBox">
                <ul>
                    <#--<li v-if="wordsPracticeMap['wordExerciseMap'] && wordsPracticeMap['wordExerciseMap'] !== {}" v-bind:class="{'active':focusCategoryGroupIndex == 0}" @click="categoryGroupClick(0)">
                        <i class="wordIcon"></i>
                        <span v-text="wordsPracticeMap['wordExerciseMap']['questionBoxTypeTitle']"></span>
                    </li>-->
                    <li v-if="wordsPracticeMap['imageTextMap']" v-bind:class="{'active':focusCategoryGroupIndex == 1}" @click="categoryGroupClick(1)">
                        <i class="imgIcon"></i>
                        <span v-text="wordsPracticeMap['imageTextMap']['questionBoxTypeTitle']"></span>
                    </li>
                    <li v-if="wordsPracticeMap['chineseCharacterCultureMap']" v-bind:class="{'active':focusCategoryGroupIndex == 2}" @click="categoryGroupClick(2)">
                        <i class="chineseIcon"></i>
                        <span v-text="wordsPracticeMap['chineseCharacterCultureMap']['questionBoxTypeTitle']"></span>
                    </li>
                </ul>
            </div>
        </div>
        <!--字词训练-->
        <div class="wordExercise" id="WORD_TEACH_AND_PRACTICE" v-if="wordsPracticeMap['wordExerciseMap'] && focusCategoryGroupIndex == 0">
            <p class="wordTeachInfo" v-text="'共'+wordsPracticeMap['wordExerciseMap']['questionCount']+'题 | 预计'+Math.ceil(wordsPracticeMap['wordExerciseMap']['seconds']/60)+'分钟'"></p>
            <div class="h-set-homework examTopicBox"
                 v-if="questionInfo && questionInfo.length>0"
                 v-for="(question,index) in questionInfo" v-bind:key="question.id"
                >
                <div class="seth-hd">
                    <p class="fl">
                        <span v-text="question.questionType"></span>
                        <span  v-text="question.difficultyName"></span>
                        <span class="noBorder" v-if="question.assignTimes && question.assignTimes > 0 && (!question.teacherAssignTimes || question.teacherAssignTimes == 0)" v-text="'共被使用'+question.assignTimes+'次'"></span>
                    </p>
                    <p class="fr"></p>
                </div>
                <div class="seth-mn">
                    <div class="testPaper-info">
                        <div class="inner">
                            <div class="box">
                                <venus-question
                                        formula-container = "WORD_TEACH_AND_PRACTICE"
                                        :objectiveConfig = "type"
                                        :questions = "[questionMap[question.id]]"
                                        :content-id="'question_self' + index + question.id">
                                </venus-question>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <!--图文入韵-->
        <div class="imageText" v-if="wordsPracticeMap['imageTextMap'] && focusCategoryGroupIndex == 1">
            <p class="wordTeachInfo" v-text="'共'+wordsPracticeMap['imageTextMap']['questionCount']+'篇 | 预计'+Math.ceil(wordsPracticeMap['imageTextMap']['seconds']/60)+'分钟'"></p>
            <ul v-if="wordsPracticeMap['imageTextMap']['imageTextRhymeList'] && wordsPracticeMap['imageTextMap']['imageTextRhymeList'].length>0">
                <li v-for="imageTextList in wordsPracticeMap['imageTextMap']['imageTextRhymeList']"
                    @click="previewImageText(wordsPracticeMap['imageTextMap']['questionBoxType'],wordsPracticeMap['imageTextMap']['doUrl'],imageTextList.id)">
                    <div class="imageTextPic">
                        <img :src="imageTextList.imageUrl" alt="">
                    </div>
                    <div class="imageTextCon">
                        <p class="imageTextTitle" v-text="imageTextList.title"></p>
                        <p class="imageTextTime" v-text="'预计'+Math.ceil(imageTextList.seconds/60)+'分钟'"></p>
                    </div>
                </li>
            </ul>
        </div>
        <!--汉子文化-->
        <div class="chineseCharacterCulture" v-if="wordsPracticeMap['chineseCharacterCultureMap'] && focusCategoryGroupIndex == 2">
            <p class="wordTeachInfo" v-text="'共'+wordsPracticeMap['chineseCharacterCultureMap']['questionCount']+'课程 | 预计'+Math.ceil(wordsPracticeMap['chineseCharacterCultureMap']['seconds']/60)+'分钟'"></p>
            <ul v-if="wordsPracticeMap['chineseCharacterCultureMap']['courses'] && wordsPracticeMap['chineseCharacterCultureMap']['courses'].length>0">
                <li v-for="courses in wordsPracticeMap['chineseCharacterCultureMap']['courses']" @click="previewCourse(wordsPracticeMap['chineseCharacterCultureMap']['questionBoxType'],courses.id)">
                    <div class="imageTextPic">
                        <img :src="courses.cover['fileUrl']" alt="">
                    </div>
                    <div class="imageTextCon">
                        <p class="imageTextTitle" v-text="courses.name"></p>
                        <p class="imageTextTime" v-text="'预计'+Math.ceil(courses.seconds/60)+'分钟'"></p>
                    </div>
                </li>
            </ul>
        </div>

    </div>
</script>