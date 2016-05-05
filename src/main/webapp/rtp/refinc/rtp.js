
if (!this.JSON) {
    this.JSON = {};
}

(function () {
    "use strict";

    function f(n) {
        // Format integers to have at least two digits.
        return n < 10 ? '0' + n : n;
    }

    if (typeof Date.prototype.toJSON !== 'function') {

        Date.prototype.toJSON = function (key) {

            return isFinite(this.valueOf()) ?
                   this.getUTCFullYear()   + '-' +
                 f(this.getUTCMonth() + 1) + '-' +
                 f(this.getUTCDate())      + 'T' +
                 f(this.getUTCHours())     + ':' +
                 f(this.getUTCMinutes())   + ':' +
                 f(this.getUTCSeconds())   + 'Z' : null;
        };

        String.prototype.toJSON =
        Number.prototype.toJSON =
        Boolean.prototype.toJSON = function (key) {
            return this.valueOf();
        };
    }

    var cx = /[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
        escapable = /[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
        gap,
        indent,
        meta = {    // table of character substitutions
            '\b': '\\b',
            '\t': '\\t',
            '\n': '\\n',
            '\f': '\\f',
            '\r': '\\r',
            '"' : '\\"',
            '\\': '\\\\'
        },
        rep;


    function quote(string) {

        escapable.lastIndex = 0;
        return escapable.test(string) ?
            '"' + string.replace(escapable, function (a) {
                var c = meta[a];
                return typeof c === 'string' ? c :
                    '\\u' + ('0000' + a.charCodeAt(0).toString(16)).slice(-4);
            }) + '"' :
            '"' + string + '"';
    }


    function str(key, holder) {


        var i,
            k,
            v,
            length,
            mind = gap,
            partial,
            value = holder[key];


        if (value && typeof value === 'object' &&
                typeof value.toJSON === 'function') {
            value = value.toJSON(key);
        }


        if (typeof rep === 'function') {
            value = rep.call(holder, key, value);
        }

        switch (typeof value) {
        case 'string':
            return quote(value);

        case 'number':

            return isFinite(value) ? String(value) : 'null';

        case 'boolean':
        case 'null':

            return String(value);

        case 'object':

            if (!value) {
                return 'null';
            }

            gap += indent;
            partial = [];

            if (Object.prototype.toString.apply(value) === '[object Array]') {

                length = value.length;
                for (i = 0; i < length; i += 1) {
                    partial[i] = str(i, value) || 'null';
                }

                v = partial.length === 0 ? '[]' :
                    gap ? '[\n' + gap +
                            partial.join(',\n' + gap) + '\n' +
                                mind + ']' :
                          '[' + partial.join(',') + ']';
                gap = mind;
                return v;
            }

            if (rep && typeof rep === 'object') {
                length = rep.length;
                for (i = 0; i < length; i += 1) {
                    k = rep[i];
                    if (typeof k === 'string') {
                        v = str(k, value);
                        if (v) {
                            partial.push(quote(k) + (gap ? ': ' : ':') + v);
                        }
                    }
                }
            } else {

                for (k in value) {
                    if (Object.hasOwnProperty.call(value, k)) {
                        v = str(k, value);
                        if (v) {
                            partial.push(quote(k) + (gap ? ': ' : ':') + v);
                        }
                    }
                }
            }

            v = partial.length === 0 ? '{}' :
                gap ? '{\n' + gap + partial.join(',\n' + gap) + '\n' +
                        mind + '}' : '{' + partial.join(',') + '}';
            gap = mind;
            return v;
        }
    }

    if (typeof JSON.stringify !== 'function') {
        JSON.stringify = function (value, replacer, space) {

            var i;
            gap = '';
            indent = '';

            if (typeof space === 'number') {
                for (i = 0; i < space; i += 1) {
                    indent += ' ';
                }

            } else if (typeof space === 'string') {
                indent = space;
            }

            rep = replacer;
            if (replacer && typeof replacer !== 'function' &&
                    (typeof replacer !== 'object' ||
                     typeof replacer.length !== 'number')) {
                throw new Error('JSON.stringify');
            }

            return str('', {'': value});
        };
    }


    if (typeof JSON.parse !== 'function') {
        JSON.parse = function (text, reviver) {

            var j;

            function walk(holder, key) {

                var k, v, value = holder[key];
                if (value && typeof value === 'object') {
                    for (k in value) {
                        if (Object.hasOwnProperty.call(value, k)) {
                            v = walk(value, k);
                            if (v !== undefined) {
                                value[k] = v;
                            } else {
                                delete value[k];
                            }
                        }
                    }
                }
                return reviver.call(holder, key, value);
            }


            text = String(text);
            cx.lastIndex = 0;
            if (cx.test(text)) {
                text = text.replace(cx, function (a) {
                    return '\\u' +
                        ('0000' + a.charCodeAt(0).toString(16)).slice(-4);
                });
            }

            if (/^[\],:{}\s]*$/
.test(text.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g, '@')
.replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, ']')
.replace(/(?:^|:|,)(?:\s*\[)+/g, ''))) {

                j = eval('(' + text + ')');

                return typeof reviver === 'function' ?
                    walk({'': j}, '') : j;
            }

            throw new SyntaxError('JSON.parse');
        };
    }
}());

function RTPJSON(){

	this.toJSONString = function (filter) {
        return JSON.stringify(this, filter);
    };

	this.parseJSON = function (s, filter) {
        return JSON.parse(s, filter);
    };
}

function RTP_XHConn()
{
  var xmlhttp = false;
  try { xmlhttp = new ActiveXObject("Msxml2.XMLHTTP"); }
  catch (e) { try { xmlhttp = new ActiveXObject("Microsoft.XMLHTTP"); }
  catch (e) { try { xmlhttp = new XMLHttpRequest(); }
  catch (e) { xmlhttp = false; }}}
  if (!xmlhttp) return null;
  this.http = xmlhttp;
  this.connect = function(sURL, sMethod, sVars)
  {
    if (!xmlhttp) return false;
    bComplete = false;
    sMethod = sMethod.toUpperCase();

    try {
      if (sMethod == "GET")
      {
        xmlhttp.open(sMethod, sURL+"?"+sVars, false);
        xmlhttp.setRequestHeader("Cache-Control", "no-cache");
        sVars = "";
      }
      else
      {	  
        xmlhttp.open(sMethod, sURL, false);
        //xmlhttp.setRequestHeader("Method", "POST "+sURL+" HTTP/1.1");
		ctype = "application/x-www-form-urlencoded";
		if (document.charset)
			ctype += "; charset=" + document.charset;
        xmlhttp.setRequestHeader("Content-Type", ctype);
        xmlhttp.setRequestHeader("Cache-Control", "no-cache");
      }
      xmlhttp.send(sVars);
    }
    catch(z) { return false; }
    return true;
  };
  return this;
}

var webchat_url = "";

function rtp_open_im(obj) {
	userfrom = obj.getAttribute("rtp_userfrom");
	userto = obj.getAttribute("rtp_userto");
	params = obj.getAttribute("rtp_params");
	cmd = obj.getAttribute("rtp_cmd");
	sdkuri = obj.getAttribute("rtp_sdkuri");
	downurl = obj.getAttribute("rtp_downurl");

	link = rtp_get_link(sdkuri, userfrom, userto, params, cmd);

	var rtpjson = new RTPJSON();
	var json = rtpjson.parseJSON(link);
	if (json.status && json.status != 0){
		alert("Error: " + json.details);
		return false;
	}

	link = json.result.uri;

	if (link == "")
		return;
	
	//add
	var launcher = new ProtocolLaucher(link,'rtpcheck.ChkObj','hiddenIframe',function(){
		if(!launcher.isSupported){
			if (webchat_url == "" || !openChatWin(obj, link, webchat_url)) {
				location.href = downurl;
			}
		}
	});
	
	launcher.launch();

}

function rtp_urlencode(str) {
	if (!str)
		return "";
	return str.replace(/\&/g,'%26').replace(/\+/g,'%2B').replace(/%20/g, '+').replace(/\*/g, '%2A').replace(/\//g, '%2F').replace(/@/g, '%40');
}

function rtp_get_link(sdkuri, userfrom, userto, params, cmd){
	var conn = new RTP_XHConn();
	if(sdkuri.indexOf('?')==-1){
		sdkuri += '?rtpact=getlinktext'; 
	}else{
		sdkuri += '&rtpact=getlinktext'; 
	}
	var rtpParams = new RTPJSON();
	rtpParams.userfrom = userfrom;
	rtpParams.user = userto;
	rtpParams.cmd = cmd;
	rtpParams.exparams = params;
	conn.connect(sdkuri, "POST", rtpParams.toJSONString());
	if (conn.http.status != 200)
		return "{'status':200, 'details':'" + conn.http.statusText + "'}";
	rtn = conn.http.responseText;
	return rtn;
}


var statefailed = false;
function rtp_show_state(obj) {
	if (statefailed){
		return "";
	}
	var rtp_uri = obj.parentNode.getAttribute("rtp_sdkuri");
	var username = obj.parentNode.getAttribute("rtp_userto");
	var conn = new RTP_XHConn();
	if(rtp_uri.indexOf('?')==-1){
		rtp_uri += '?rtpact=getpresence'; 
	}else{
		rtp_uri += '&rtpact=getpresence'; 
	}
	var rtpParams = new RTPJSON();
	rtpParams.user = username;

	conn.connect(rtp_uri, "POST", rtpParams.toJSONString());
	if (conn.http.status != 200) {
		statefailed = true;
		return "";
	}
	state = conn.http.responseText;

	var rtpjson = new RTPJSON();
	var json = rtpjson.parseJSON(state);

	if (json.status && json.status != 0){
		return;
	}
	obj.id = "rtp_state_"+json.result.presence;
	var autoclick = obj.parentNode.getAttribute("autoclick");
	if(autoclick === 'true'){
		rtp_open_im(obj.parentNode);
		//obj.parentNode.click();
	}
}


///RM webchat start
function rtnFalse(){
	return;
}
function openChatWin(obj,link,webchat_url){
	try{
		var openWin = getCookie("openChatWin");
		///*
		var uid = getCookie("uid");
		if(typeof(uid) == "undefined" || uid == "undefined"){
			uid = "";
		}
		setCookie("uid", link, getExpDate(1,0,0));
		//*/
		if(openWin){
			obj.target = "";
			obj.href="javascript:rtnFalse();";
		}else{
			var url = webchat_url+"?"+link;
			obj.target = "rooyeeweb";
			obj.href = url;
		}
	   return true;
   }catch(e){
       return false;
   }
}
function getExpDate(days, hours, minutes)
{
    var expDate = new Date();
    if(typeof(days) == "number" && typeof(hours) == "number" && typeof(minutes) == "number")
    {
        expDate.setDate(expDate.getDate() + parseInt(days));
        expDate.setHours(expDate.getHours() + parseInt(hours));
        expDate.setMinutes(expDate.getMinutes() + parseInt(minutes));
        return expDate.toGMTString();
    }
}
function getCookieVal(offset)
{
    var endstr = document.cookie.indexOf(";", offset);
    if(endstr == -1)
    {
        endstr = document.cookie.length;
    }
    return unescape(document.cookie.substring(offset, endstr));
}
function getCookie(cName)
{
    var arg = cName + "=";
    var alen = arg.length;
    var clen = document.cookie.length;
    var i = 0;   
    
    while(i < clen)
    {
        var j = i + alen;
        
        if (document.cookie.substring(i, j) == arg)
        {        	
            return getCookieVal(j);
        }
        
        i = document.cookie.indexOf(" ", i) + 1;
        if(i == 0) break;
    }
    return;
}
function setCookie(cName, cValue, cExpires)
{
    var cStr = cName + "=" + escape(cValue) +
        ((cExpires) ? "; expires=" + cExpires : "") +
        (  "; path=/") ;
   document.cookie = cStr;
}

  SERVICE_UI_WINDOW_WEBBROWSER = '/service/ui/window/webbrowser';

  SERVICE_FUNC_RTP = '/service/function/rtp';

function Service(AService){
	return window.external.Service(AService);
}

function ShowDialog(AUrl, AParams, AFeature){
	return window.external.ShowDialog(AUrl, AParams, AFeature);
}

function ShowModalDialog(AUrl, AParams, AFeature){
	return window.external.ShowModalDialog(AUrl, AParams, AFeature);
}

function TransUrl(AUrl, ASource, AToken){
	var rtp = Service(SERVICE_FUNC_RTP);
	return rtp.TransURL(AUrl, ASource, AToken);
}

function OpenUrl(AUrl, ASource, AToken, ABrowser){
	var rtp = Service(SERVICE_FUNC_RTP);
	var url = TransUrl(AUrl, ASource, AToken);
	if(ABrowser){
		rtp.OpenUrlInBrowser(url);
	}else{
		ShowDialog(url, null, '');
	}
}
/// rtp client service API end