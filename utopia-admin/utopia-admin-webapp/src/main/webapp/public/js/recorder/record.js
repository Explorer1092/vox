(function() {
	function Recorder(config) {
		this.realTimeWorker = new Worker('/public/js/recorder/worker-realtime.js');	
		this.config = config || {};
		this.context = null;
		this.microphone = null;
		this.processor = null;
		this.mp3ReceiveSuccess = null;
		this.currentErrorCallback = null;
		this.source = null;
		this.buffer = [];
		this.config.sampleRate = (config && config.sampleRate) || 44100;
		this.config.bitRate = (config && config.bitRate) || 128;
		this.getUserMedia = navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia || navigator.msGetUserMedia || window.getUserMedia;
		if(navigator.getUserMedia) {
			var thiz = this;
			navigator.getUserMedia({audio: true}, function(stream) {
				var AudioContext = window.AudioContext || window.webkitAudioContext;
				thiz.context = new AudioContext();
				thiz.microphone = thiz.context.createMediaStreamSource(stream);
				thiz.processor = thiz.context.createScriptProcessor(0, 1, 1);
				thiz.processor.onaudioprocess = function (event) {
					var array = event.inputBuffer.getChannelData(0);
					thiz.realTimeWorker.postMessage({ cmd: 'encode', buf: array });
				};

				thiz.realTimeWorker.onmessage = function (e) {
					switch (e.data.cmd) {
						case 'init':
							console.log('初始化成功');
							if (thiz.config.funOk) {
								thiz.config.funOk();
							}
							break;
						case 'end':
							console.log('MP3大小：', e.data.buf.length);
							if (thiz.mp3ReceiveSuccess) {
								thiz.mp3ReceiveSuccess(new Blob(e.data.buf, { type: 'audio/mp3' }));
							}
							break;
						case 'error':
							console.log('错误信息：' + e.data.error);
							if (currentErrorCallback) {
								currentErrorCallback(e.data.error);
							}
							break;
						default:
							console.log('未知信息：', e.data);
					}
				};

				thiz.realTimeWorker.postMessage({
					cmd: 'init',
					config: {
						sampleRate: thiz.config.sampleRate,
						bitRate: thiz.config.bitRate
					}
				});

				if(thiz.config.initCallback) {
					thiz.config.initCallback();	
				}
			}, function (error) {
				var msg;
				switch (error.code || error.name) {
					case 'PERMISSION_DENIED':
					case 'NotAllowedError':
					case 'PermissionDeniedError':
						msg = '您拒绝访问麦客风';
						break;
					case 'NOT_SUPPORTED_ERROR':
					case 'NotSupportedError':
						msg = '浏览器不支持麦客风';
						break;
					case 'MANDATORY_UNSATISFIED_ERROR':
					case 'MandatoryUnsatisfiedError':
						msg = '找不到麦客风设备';
						break;
					default:
						msg = '无法打开麦克风，建议使用 Firefox 浏览器';
						break;
				}
				if (thiz.config.funCancel) {
					thiz.config.funCancel(msg);
				}
			});
		} else {
			if (thiz.config.funCancel) {
				thiz.config.funCancel('当前浏览器不支持录音功能');
			}
		}
	}

	Recorder.prototype.start = function() {
		var processor = this.processor,
			microphone = this.microphone,
			context = this.context;
		if (processor && microphone) {
			microphone.connect(processor);
			processor.connect(context.destination);
			console.log('开始录音');
		}
	}
	Recorder.prototype.stop = function () {
		var processor = this.processor,
			microphone = this.microphone,
			context = this.context;
		if (processor && microphone) {
			microphone.disconnect();
			processor.disconnect();
			console.log('录音结束');
		}
	}
	Recorder.prototype.getMp3Blob = function (onSuccess, onError) {
		this.currentErrorCallback = onError;
		this.mp3ReceiveSuccess = onSuccess;
		this.realTimeWorker.postMessage({ cmd: 'finish' });
	};
	Recorder.prototype.play = function() {
		this.source = this.context.createBufferSource();
		this.source.connect(this.processor);
	}
	window._Recorder = Recorder;
	return Recorder;
})();