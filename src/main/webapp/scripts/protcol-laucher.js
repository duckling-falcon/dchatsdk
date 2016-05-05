+ function(exports) {
	function ProtocolLaucher(url,checkActiveX,hiddenIframe,callback) {
		this.url = url;
		this.callback = callback;
		this.checkActiveX = checkActiveX;
		this.hiddenIframe = hiddenIframe;

		(function($) {
			function uaMatch(ua) {
				ua = ua.toLowerCase();

				var match = /(chrome)[ \/]([\w.]+)/.exec(ua) 
							|| /(webkit)[ \/]([\w.]+)/.exec(ua) 
							|| /(opera)(?:.*version|)[ \/]([\w.]+)/.exec(ua) 
							|| /(msie) ([\w.]+)/.exec(ua)
							|| /(trident)(?:.*?rv:([\w.]+))/.exec(ua)
							|| ua.indexOf("compatible") < 0 && /(mozilla)(?:.*? rv:([\w.]+)|)/.exec(ua) || [];

				return {
					browser : match[1] || "",
					version : match[2] || "0"
				};

			};
			var browser = {};
			var matched = uaMatch(navigator.userAgent);
			if (matched.browser) {
				if ("trident"===matched.browser){
					browser["msie"]=true;
				}else{
					browser[matched.browser] = true;
				}
				browser.version = matched.version;
			}
			$.browser = browser;
		})(this);
		
		(function($) {
			$.launch = function() {
				if ($.browser.mozilla ) {
					launchMozilla(this);
				} else if ($.browser.chrome) {
					launchChrome(this);
				} else if ($.browser.msie) {
					launchIE(this);
				}
			};
			function createInvisibleElement(tagname,id) {
				var d = (window.parent) ? window.parent.document : window.document;
				d.write("<"+tagname+" style='display:none' id='"+id+"'></"+tagname+">");
				var ele = d.createElement(tagname);
				return d.getElementById(id);
			};
			function createIFrame() {
				var d = (window.parent) ? window.parent.document : window.document;
				d.write("<iframe id=\"hiddenIframe\" src=\"about:blank\" style=\"display: none;\"></iframe>");
				return d.getElementById("hiddenIframe");
			};
			//Handle Firefox
			function launchMozilla(laucher) {
				var url = laucher.url;
				var iFrame = document.getElementById(laucher.hiddenIframe);

				laucher.isSupported = false;

				//Set iframe.src and handle exception
				try {
					iFrame.contentWindow.location.href = url;
					laucher.isSupported = true;
					laucher.callback();
				} catch(e) {
					//FireFox
					if (e.name == "NS_ERROR_UNKNOWN_PROTOCOL") {
						laucher.isSupported = false;
						laucher.callback();
					}
				}
			}

			//Handle Chrome
			function launchChrome(laucher) {
				var url = laucher.url;
				var protcolEl = window;
				var link = document.getElementById("hiddenAnchor");
				
				laucher.isSupported = false;

				protcolEl.focus();
				protcolEl.onblur = function() {
					laucher.isSupported = true;
				};
				//will trigger onblur
				location.href = url;
				setTimeout(function() {
					protcolEl.onblur = null;
					laucher.callback();
				}, 300);
			}

			//Handle IE
			function launchIE(laucher) {
				try{
					var checkObj = new ActiveXObject(laucher.checkActiveX);
					var url = laucher.url;
					
					var protocol = url.substr(0, url.indexOf(':'));
					if (!checkObj.ChkProtocol(protocol)){
						laucher.isSupported=false;
						laucher.callback();
						return;
					}
					
					window.location = url;
					laucher.isSupported=true;
					laucher.callback();
				}catch (e){
					laucher.isSupported=false;
					laucher.callback();
				}
			};
		})(this);
	};

	exports.ProtocolLaucher = ProtocolLaucher;
}(window); 