// 中文字符判断
function isChineseChar(str) {
    var reg = /[\u4E00-\u9FA5\uF900-\uFA2D]/;
    return reg.test(str);
}

// 获取参数
function getQuery() {
    let query = location.search,
        result = {};
    if (query !== '') {
        query = query.substring(1).split('&');
        for (let i in query) {
            var item = query[i].split('=');
            result [item[0]] = item[1];
        }
    }
    return result
}

// 比较函数
function compare(item_a, item_b) {
    // 比较函数 a,b命名是否相同
    let a_r = '',
        b_r = '',
        a = item_a.name.split('.'),
        b = item_b.name.split('.'),
        type = ['image', 'audio'],
        type_r = [item_a.type.split('/')[0], item_b.type.split('/')[0]];
    for (let i = 0; i < a.length - 1; i++) {
        a_r += a[i];
    }
    for (let j = 0; j < b.length - 1; j++) {
        b_r += b[j];
    }
    return a_r === b_r && (JSON.stringify(type) == JSON.stringify(type_r) || JSON.stringify(type) == JSON.stringify(type_r.reverse()))
}
// formdata函数
function makeFormData(obj, form_data) {
    var data = [];
    if (obj instanceof File) {
        data.push({key: "", value: obj});
    }
    else if (obj instanceof Array) {
        for (var j = 0, len = obj.length; j < len; j++) {
            var arr = makeFormData(obj[j]);
            for (var k = 0, l = arr.length; k < l; k++) {
                var key = !!form_data ? j + arr[k].key : "[" + j + "]" + arr[k].key;
                data.push({key: key, value: arr[k].value})
            }
        }
    }
    else if (typeof obj == 'object') {
        for (var j in obj) {
            var arr = makeFormData(obj[j]);
            for (var k = 0, l = arr.length; k < l; k++) {
                var key = !!form_data ? j + arr[k].key : "[" + j + "]" + arr[k].key;
                data.push({key: key, value: arr[k].value})
            }
        }
    }
    else {
        data.push({key: "", value: obj});
    }
    if (!!form_data) {
        // 封装
        for (var i = 0, len = data.length; i < len; i++) {
            form_data.append(data[i].key, data[i].value)
        }
    }
    else {
        return data;
    }
}

// 上传组件
Vue.component('vue-upload', {
    props: {
        accept: {
            type: String,
            default: "*"
        },
        model: {
            type: Array,
            default: []
        },
        limit: {
            type: Number,
            default: 1
        },
        type: [String, Number],
    },
    template: `
            <el-upload
              style="display: inline-block"
              ref="upload"
              multiple
              :limit="limit"
              :on-exceed="handleExceed"
              :accept="accept"
              :before-upload="beforeUpload"
              :action="action"
              :on-preview="handlePreview"
              :http-request="uploadRequest"
              :file-list="fileList"
              :show-file-list="false">
              <el-button slot="trigger" size="mini" type="primary">选取</el-button>
            </el-upload>
          `,
    data() {
        return {
            action: '/opmanager/studyTogether/template/upload_signal_file_to_oss.vpage',
            index: 0,
            resultList: [],
            fileList: []
        }
    },
    methods: {
        handlePreview(file) {
            console.log(file);
        },
        beforeUpload(file) {
            const size = file.size / 1024 / 1024 < 30;
            const chinese = isChineseChar(file.name);
            let type = true;
            if (this.type == 1) {
                if (this.fileList.length == 1) {
                    type = compare(file, this.fileList[0])
                } else {
                    this.fileList.push(file);
                }
            }
            if (chinese) {
                this.$message.error('文件名中不可包含中文!');
            }
            if (!size) {
                this.$message.error('上传图片大小不能超过 30MB!');
            }
            if (!type) {
                this.$message.error('请保证格式正确，且图片命名和音频命名需一致!');
            }
            return size && !chinese && type;
        },
        handleExceed(files, fileList) {
            this.$message.warning('当前限制选择 ' + this.limit + ' 个文件!');
        },
        uploadRequest(content) {
            const loading = this.$loading({
                lock: true,
                text: '文件上传中',
                spinner: 'el-icon-loading',
                background: 'rgba(0, 0, 0, 0.7)'
            });
            let formData = new FormData();
            console.log(content);
            formData.append('inputFile', content.file);
            $.ajax({
                url: '/opmanager/studyTogether/template/upload_signal_file_to_oss.vpage',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: (data) => {
                    //重置成空。保证连续选择同一个文件能触发上传
                    if (this.limit === 1) {
                        console.log("------>" + data);
                        this.$emit('result', data, this.model);
                        this.fileList = [];
                        loading.close();
                    } else {
                        data.file = content.file;
                        this.resultList.push(data);
                        console.log(this.index, this.limit);
                        if (++this.index == this.limit) {
                            this.$emit('result', this.resultList, this.model);
                            this.index = 0;
                            this.resultList = [];
                            this.fileList = [];
                            loading.close();
                        }
                    }
                },
                error: (err) => {
                    this.$message.error('文件上传失败！')
                    loading.close();
                }
            });
        }
    }
});
// 添加行数组件
Vue.component('vue-add', {
    props: ['result'],
    template: `
            <div>
                <div v-for="(item, index) in result">
                    <el-input style="width: 240px;" v-model="result[index]"></el-input>
                    <el-button v-if="index===0" type="primary" size="mini" @click="add()">添加</el-button>
                    <el-button v-else type="primary" size="mini" @click="remove(index)">删除</el-button>
                </div>
            </div>
          `,
    methods: {
        add() {
            this.result.push('');
        },
        remove(index) {
            this.result.splice(index, 1);
        }
    }
});