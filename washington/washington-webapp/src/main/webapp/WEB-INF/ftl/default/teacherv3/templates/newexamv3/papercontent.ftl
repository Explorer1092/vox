<script type="text/html" id="T:PAPER_LIST">
    <div class="h-topicPackage" v-if="papers && papers.length > 0">
        <div class="sliderHolder">
            <div class="topicBox">
                <ul><!--width为li的个数*206px-->
                    <li class="slideItem" v-for="(paperObj,index) in papers" v-on:click="paperClick(paperObj)" v-bind:class="{'active':paperObj.paperId == paperId}" v-bind:title="paperObj.paperName">
                        <p v-text="paperObj.paperName"></p>
                        <span class="state" style="display: none;">用过</span>
                        <span class="triggle-icon"></span>
                    </li>
                </ul>
            </div>
        </div>
        <div class="line"></div>
    </div>
</script>


<script type="text/html" id="T:PAPER_INFO">
    <div class="paper-container">
        <div class="new-topicPackage-hd" style="height: 100%;">
            <div class="l-topic-inner">
                <div class="title">
                    <div>试卷总分：<span v-text="totalScore">0</span>分    共<span v-text="questionCount">0</span>题  预计<span v-text="minutes">0</span>分钟完成</div>
                    <div class="star-box" v-text="description" style="line-height: 21px;padding-bottom: 6px;">
                        &nbsp;
                    </div>
                </div>
            </div>
            <div class="allCheck" v-on:click="goAssign">
                <span class="txt-left">布&nbsp;&nbsp;置</span>
            </div>
        </div>
        <div class="s-line"></div>
        <div class="question-box">
            <div class="table-box">
                <table v-if="modules && modules.length > 0">
                    <thead>
                        <tr>
                            <td v-for="(item,index) in modules" v-text="item.moduleName"></td>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td v-for="(item,index) in modules" v-text="item.questionCount + '题'"></td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <div class="h-content" id="J_paperContent" style="position:relative;margin: 10px 0;padding: 0 15px;">
            </div>
        </div>
    </div>
</script>

<script id="TPL_DESCRIPTION" type="text/html">
    <div class="answeBox">
        <p class="answe-itm-2"><span>本题分值：<%=standardScore%>分</span></p>
    </div>
</script>