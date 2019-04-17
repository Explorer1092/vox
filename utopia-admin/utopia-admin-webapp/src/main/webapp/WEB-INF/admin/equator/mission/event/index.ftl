<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='事件' page_num=24>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<style>.form-cell{border: 1px solid #ddd;margin-bottom: 5rem; padding: 0 1rem;border-radius: .5rem;}</style>
<div class="span9" id="eventIndexBox" style="display: none" v-show="pageLoadFinished">
    <div class="form-horizontal form-cell">
        <fieldset>
            <legend class="">完成应用闯关任务</legend>
            <!-- 学生ID -->
            <div class="control-group">
                <label class="control-label" for="input01">学生ID</label>
                <div class="controls">
                    <input type="number" class="input-xlarge" v-model="missionFormData.studentId">
                </div>
            </div>

            <!-- 应用 -->
            <div class="control-group">
                <label class="control-label">应用</label>
                <div class="controls">
                    <select class="input-xlarge" v-model="missionFormData.appKey">
                        <option v-for="item in sourceApps" :value="item.value">
                            {{ item.key}}
                        </option>
                    </select>
                </div>
            </div>

            <!-- 分数 -->
            <div class="control-group">
                <label class="control-label">分数</label>
                <div class="controls">
                    <input type="number" v-model="missionFormData.score" class="input-xlarge">
                </div>
            </div>

            <!-- 年级 -->
            <div class="control-group">
                <label class="control-label">年级<span style="color: green">(选填)</span></label>
                <div class="controls">
                    <select class="input-xlarge" v-model="missionFormData.grade">
                        <option v-for="item in sourceGrades" :value="item.value">
                            {{ item.key}}
                        </option>
                    </select>
                </div>
            </div>

            <!-- 学期 -->
            <div class="control-group">
                <label class="control-label">学期<span style="color: green">(选填)</span></label>
                <div class="controls">
                    <select class="input-xlarge" v-model="missionFormData.term">
                        <option v-for="item in sourceTerms" :value="item.value">
                            {{ item.key}}
                        </option>
                    </select>
                </div>
            </div>

            <!-- submit -->
            <div class="control-group">
                <div class="controls">
                    <div v-if="
                    missionFormData.studentId == '' ||
                    missionFormData.score == '' ||
                    missionFormData.appKey == ''" class="btn disabled">发送</div>
                    <div v-else="" class="btn btn-success" @click="missionFormSubmitBtn">发送</div>
                </div>
            </div>

        </fieldset>
    </div>

    <div class="form-horizontal form-cell" >
        <fieldset>
            <legend class="">完成免费学习任务</legend>

            <!-- 学生ID -->
            <div class="control-group">
                <label class="control-label" for="input01">学生ID</label>
                <div class="controls">
                    <input type="number" class="input-xlarge" v-model="learningTasksFormData.studentId">
                </div>
            </div>

            <!-- 分数 -->
            <div class="control-group">
                <label class="control-label">分数</label>
                <div class="controls">
                    <input type="number" class="input-xlarge" v-model="learningTasksFormData.score">
                </div>
            </div>

            <!-- 次数 -->
            <div class="control-group">
                <label class="control-label">次数</label>
                <div class="controls">
                    <input type="number" class="input-xlarge" v-model="learningTasksFormData.times">
                </div>
            </div>

            <!-- 模板id -->
            <div class="control-group">
                <label class="control-label">模板id</label>
                <div class="controls">
                    <input  class="input-xlarge" v-model="learningTasksFormData.templateId">
                </div>
            </div>

            <!-- submit -->
            <div class="control-group">
                <label class="control-label"></label>
                <div class="controls">
                    <div v-if="
                    learningTasksFormData.studentId == '' ||
                    learningTasksFormData.score == '' ||
                    learningTasksFormData.times == '' ||
                    learningTasksFormData.templateId == ''" class="btn disabled">发送</div>
                    <div v-else="" class="btn btn-success" @click="learningTasksFormSubmitBtn">发送</div>
                </div>
            </div>

        </fieldset>
    </div>
</div>

<script>
    $(() => {
        let vm = new Vue({
            el: '#eventIndexBox',
            data: {
                pageLoadFinished: false,
                sourceApps: [
                    {key: "", value: ""},
                    {key: "阿分题英语", value: "AfentiExam"},
                    {key: "阿分题数学", value: "AfentiMath"},
                    {key: "阿分题语文", value: "AfentiChinese"},
                    {key: "走遍美国学英语", value: "UsaAdventure"},
                    {key: "百科大挑战", value: "EncyclopediaChallenge"},
                    {key: "酷跑学单词", value: "GreatAdventure"},
                    {key: "动物大冒险", value: "AnimalLand"},
                    {key: "恐龙时代", value: "DinosaurLand"},
                    {key: "魔力科技", value: "ScienceLand"},
                    {key: "速算100分", value: "MathGarden"},
                    {key: "字词100分", value: "ChinesePilot"},
                    {key: "单词100分", value: "WordBuilder"},
                    {key: "配音100分", value: "ListenWorld"},
                    {key: "小U绘本", value: "ELevelReading"}
                ],
                sourceGrades: [
                    {key: "", value: ""},
                    {key: "一年级", value: 1},
                    {key: "二年级", value: 2},
                    {key: "三年级", value: 3},
                    {key: "四年级", value: 4},
                    {key: "五年级", value: 5},
                    {key: "六年级", value: 6}
                ],
                sourceTerms: [
                    {key: "", value: ""},
                    {key: "上学期", value: 1},
                    {key: "下学期", value: 2},
                    {key: "全年", value: 0}
                ],
                //闯关任务form
                missionFormData: {
                    studentId: '',//学生ID
                    appKey: '', //应用
                    score: '', //分数
                    grade: '', //年级
                    term: '' //学期

                },
                //学习任务form
                learningTasksFormData: {
                    studentId: '',
                    score: '',
                    times: '',
                    templateId: ''
                }
            },
            mounted(){
                this.pageLoadFinished = true;
            },
            methods: {
                //闯关任务提交
                missionFormSubmitBtn() {
                    let self = this;
                    $.get('/equator/mission/event/taskapppractice.vpage', self.missionFormData, (data) => {
                        alert(data.info);
                        if(data.success){
                            self.missionFormData = {studentId: '',appKey: '',score: '',grade: '',term: ''};
                        }
                    });
                },

                //学习任务提交
                learningTasksFormSubmitBtn(){
                    let self = this;
                    $.get('/equator/mission/event/assignmentstudy.vpage', self.learningTasksFormData, (data) => {
                        alert(data.info);
                        if(data.success){
                            self.learningTasksFormData = {studentId: '',score: '',times: '',templateId: ''};
                        }
                    });
                }
            },
            created() {}
        });
    });
</script>
</@layout_default.page>