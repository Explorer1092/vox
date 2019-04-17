(function(){
   "use strict";
    var TEMPLATE = "<div class=\"venus-font\" :class=\"getClass()\"><div  v-bind:id=\"contentId\"></div></div>";
    //现在仅满足最低报告渲染需求，如有其他需求或者修改配置，考虑兼容性以及扩展性；

    var venus_question = {
        template: TEMPLATE,
        data: function data() {
            return {};
        },
        props: {
            venus: {
                type: String,
                default: "Venus"
            },
            contentId: {
                type: String,
                required: true
            },
            showUserAnswer: {
                type: Boolean,
                default: false
            },
            showRightAnswer: {
                type: Boolean,
                default: false
            },
            questions: {
                type: Array,
                default: []
            },
            formulaContainer: {
                type: String,
                required: true
            },
            showAnalysis: {
                type: Boolean,
                default: false
            },
            objectiveConfig: {
                type: String,
                default: ""
            },
            startIndex: {
                type: Number,
                default: 0
            },
            showIntervene: {
                type: Boolean,
                default: false
            }
        },
        methods: {
            initQuestion: function initQuestion() {
                var vm = this;
                if (vm.questions.length > 0) {
                    var config = {
                        container: '#' + vm.contentId, //容器的id，（必须）
                        formulaContainer: '#' + vm.formulaContainer, //公式渲染容器（必须）
                        questionList: vm.questions, //试题数组，包含完整的试题json结构， （必须）
                        framework: {
                            vue: Vue, //vue框架的外部引用
                            vuex: Vuex //vuex框架的外部引用
                        },
                        cdnResEnv: window.env || "dev", //添加cdn切换
                        showAnalysis: vm.showAnalysis, //是否展示解析
                        showUserAnswer: vm.showUserAnswer, //是否展示用户答案
                        showRightAnswer: vm.showRightAnswer, //是否展示正确答案
                        startIndex: vm.startIndex, //从第几题开始
                        showIntervene: vm.showIntervene, //是否展示线索
                        onSendLog: function onSendLog(obj) {
                            vm.$emit("sendlog", obj);
                        }
                    };

                    window[vm.venus].init(config);
                }
            },
            getClass: function getClass() {
                var obj = {};
                obj[this.objectiveConfig] = true;
                return obj;
            }
        },
        mounted: function mounted() {
            this.$nextTick(function () {
                this.initQuestion();
            });
        }
    };

    $17.venusQuestion = $17.venusQuestion || {};
    $17.extend($17.venusQuestion, venus_question);
}());

