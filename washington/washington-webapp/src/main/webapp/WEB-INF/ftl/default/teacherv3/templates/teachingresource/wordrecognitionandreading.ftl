<script type="text/html" id="T:WORD_RECOGNITION_AND_READING">
    <div class="wordReadingBox" v-if="wordPackagesList.length>0" data-comment="生字认读">
        <p class="wordTitle">共有{{wordPackagesList[0].questionNum}}个生字：跟读、音节笔顺、部首、结构、字义、组词</p>
        <ul class="wordCard">
            <li class="wordsSingle" v-for="(questionCon,index) in questionList" @click="lookDetail(questionCon.id)">
                <div class="sz-box">
                    <div class="top">
                        <div class="top_bg"></div>
                    </div>
                    <ul class="bottom">
                        <li class="li-1"></li>
                        <li class="li-2"></li>
                        <li class="li-3"></li>
                        <li class="li-4"></li>
                    </ul>
                    <ul class="sz-content">
                        <li class="c-li1" v-text="questionCon.content.subContents[0].extras.wordContentPinyinMark"></li>
                        <li class="c-li2">
                            <div v-text="questionCon.content.subContents[0].extras.chineseWordContent"></div>
                        </li>
                        <li class="audioPlay" :class="{'audioPause': audioIndex == index}" @click.stop="playAudio(index,questionCon.content.subContents[0].extras.chineseWordAudioUrl)"></li>
                    </ul>
                </div>
                <div v-if="isDateduppt" class="wordSelect" :class="{'wordSelected':selectQuestionIds.indexOf(questionCon.id)>-1}" v-text="selectQuestionIds.indexOf(questionCon.id)>-1?'已选':'选入'" @click.stop="addOrRemove(questionCon.id)"></div>
                <div v-if="!isDateduppt" class="lookDetails">查看详情</div>
            </li>
            <audio style="display:none;" id="bgFile" style="width: 100%;"></audio>
        </ul>
    </div>
</script>