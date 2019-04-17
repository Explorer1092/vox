<script type="text/html" id="T:WORD_RECOGNITION_AND_READING_QUESTION">
    <div class="characterBox" data-bind="css:{'sink-student':clientRole == 'student'}">
        <p class="characterTitle" style="display: none;">1</p>
        <div class="characterLeft">
            <div class="subCard" v-if="extras.chineseWordImgUrl">
                <img data-bind="attr:{src:extras.chineseWordImgUrl}">
            </div>
            <p class="pinyin"><i class="play" data-bind="click:$data.playingAudio,css:{'suspend':playing && playing()},clickBubble:false"></i><!--ko text:extras.wordContentPinyinMark--><!--/ko--></p>
            <p class="subCon" data-bind="text:extras.chineseWordStrokesOrder"></p>
        </div>
        <div class="characterRight">
            <div class="charaTotal" data-bind="text: '共' + extras.chineseWordStrokes + '画 | ' + extras.chineseWordStructure + ' | ' + extras.chineseWordRadical + '部首' "></div>
            <div class="charaInter">
                <p class="interTitle">释义</p>
                <p class="interCon" data-bind="text:extras.wordExplain ? extras.wordExplain : '无'"></p>
            </div>
            <div class="charaInter">
                <p class="interTitle">组词</p>
                <p class="interCon" data-bind="text:extras.wordWords ? extras.wordWords : '无'"></p>
            </div>
        </div>
    </div>
</script>