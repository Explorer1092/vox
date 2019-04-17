<script type="text/html" id="T:DATETIME_PICKER">
    <div>
        <label style="cursor: pointer;padding-right: 0;">
            <input type="text" v-model="dateInput" placeholder="自定义时间" readonly="readonly" class="c-ipt datepicker20190225">
        </label>
        <label>
            <select class="w-int" style="width: 60px;" v-model="focusHour" v-on:change="hourChanged">
                <option v-for="(hour,index) in hourSelect" v-text="hour"></option>
            </select>时
            <select class="w-int" style="width: 60px;" v-model="focusMin" v-on:change="minChanged">
                <option v-for="(min,index) in minSelect" v-text="min"></option>
            </select>分
        </label>
    </div>
</script>
<script type="text/javascript">
    /*
    时间控件 datetime_picker.js
    依赖于jquery.ui.js
    * */
    (function(){
        var h = ['00', '01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23'];
        var m = [
            '00', '01', '02', '03', '04', '05', '06', '07', '08', '09',
            '10', '11', '12', '13', '14', '15', '16', '17', '18', '19',
            '20', '21', '22', '23', '24', '25', '26', '27', '28', '29',
            '30', '31', '32', '33', '34', '35', '36', '37', '38', '39',
            '40', '41', '42', '43', '44', '45', '46', '47', '48', '49',
            '50', '51', '52', '53', '54', '55', '56', '57', '58', '59'
        ];

        function splitDateTime(dateTime){
            return dateTime.split(/:|-|\s/g);
        }

        function getTimeArray(array, index){
            return $.grep(array, function (val, key) {
                return val >= index;
            });
        }

        var dateTimeElement;

        var dateTimePicker = {
            template : template("T:DATETIME_PICKER",{}),
            data : function(){
                var vm = this;
                var arr = splitDateTime(vm.defaultTimeStr);
                return {
                    dateInput : vm.defaultTimeStr.substring(0,10),
                    hourSelect : h,
                    minSelect : m,
                    focusHour : arr[3],
                    focusMin : arr[4],
                    focusSecond : "00"
                };
            },
            computed : {
                selectDateTime : function(){
                    return this.dateInput + " " + this.focusHour + ":" + this.focusMin + ":" + this.focusSecond;
                }
            },
            watch : {
                defaultTimeStr : function(newValue,oldValue){
                    var vm = this;
                    if(newValue){
                        var arr = splitDateTime(newValue);
                        vm.dateInput = arr.slice(0,3).join("-");
                        vm.hourSelect = getTimeArray(h,arr[3]);
                        vm.minSelect = getTimeArray(m,arr[4]);
                        vm.focusHour = arr[3];
                        vm.focusMin = arr[4];
                    }
                },
                minTimeStr : function(newValue,oldValue){
                    if(newValue){
                        var minDate = newValue.substring(0,10);
                        dateTimeElement.datepicker( "option", { minDate: minDate } );
                    }
                },
                maxTimeStr : function(newValue,oldValue){
                    if(newValue){
                        var maxDate = newValue.substring(0,10);
                        dateTimeElement.datepicker( "option", { maxDate: maxDate } );
                    }
                }
            },
            props :{
                defaultTimeStr : {
                    type : String,
                    default : "",
                    validator: function (value) {
                        // 验证格式为 yyyy-MM-dd hh:mm:ss
                        return true;
                    }
                },
                minTimeStr : {
                    type : String,
                    default : "",
                    validator: function (value) {
                        // 验证格式为 yyyy-MM-dd hh:mm:ss
                        return true;
                    }
                },
                maxTimeStr : {
                    type : String,
                    default : "",
                    validator: function (value) {
                        // 验证格式为 yyyy-MM-dd hh:mm:ss
                        return true;
                    }
                }
            },
            methods : {
                hourChanged : function(){
                    this.$emit("change-datetime",this.selectDateTime);
                },
                minChanged : function(){
                    this.$emit("change-datetime",this.selectDateTime);
                }
            },
            created : function(){

            },
            mounted : function(){
                var vm = this;
                this.$nextTick(function(){
                    dateTimeElement = $(".datepicker20190225");
                    var _minDate = null;
                    if(vm.minTimeStr){
                        _minDate = vm.minTimeStr.substring(0,10);
                    }
                    dateTimeElement.datepicker({
                        dateFormat      : 'yy-mm-dd',
                        defaultDate     : vm.dateInput,
                        numberOfMonths  : 1,
                        minDate         : _minDate,
                        maxDate         : null,
                        onSelect        : function(selectedDate){
                            vm.dateInput = selectedDate;
                            vm.$emit("change-datetime",vm.selectDateTime);
                        }
                    });
                });
            },
            beforeDestroy : function(){
                $17.info("dateTimePicker beforeDestroy");
                dateTimeElement = null;
            },
            destroyed : function(){
                $17.info("dateTimePicker destroyed");
            }
        };

        $17.comblock = $17.comblock || {};
        $17.extend($17.comblock, {
            dateTimePicker   : dateTimePicker
        });
    }());
</script>