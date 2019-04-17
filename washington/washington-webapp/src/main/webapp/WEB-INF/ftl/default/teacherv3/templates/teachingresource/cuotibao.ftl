<script type="text/html" id="T:CUO_TI_BAO">
    <div class="wrongTopicBox" v-if="wordContentList && wordContentList.length>0">
        <div class="explainTips">
            <p class="explainCon">该教材所有易错题讲解都在这了呦</p>
        </div>
        <div class="wrongTopicBox">
            <ul>
                <li v-for="wordContent in wordContentList" class="wrong-pictureList" @click="playVideo(wordContent.id,wordContent.videoUrl,wordContent.videoThumbUrl)">
                    <div class="wrongPic">
                        <img :src="wordContent.imgUrl">
                        <div class="wrongPlay"></div>
                    </div>
                    <div class="wrongInfo" v-text="wordContent.name"></div>
                </li>
            </ul>
        </div>
    </div>
</script>