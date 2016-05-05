

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

var statefailed = false;
function rtp_show_state(rtp_uri, username) {
	if (statefailed){
		return "";
	}
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
	//obj.id = "rtp_state_"+json.result.presence;
}

///RM webchat start
function rtnFalse(){
	return;
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

var webchat_url = "";
function my_open_im(link, downurl) {
	if (link == "")
		return;
	var launcher = new ProtocolLaucher(link,'rtpcheck.ChkObj','hiddenIframe',function(){
		if(!launcher.isSupported){
			if (webchat_url == "" || !openChatWin(obj, link, webchat_url)) {
				//location.href = downurl;
			}
		}
	});
	launcher.launch();
}