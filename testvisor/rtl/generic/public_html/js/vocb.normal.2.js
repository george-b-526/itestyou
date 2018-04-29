/*
	ITestYou JS Library 1.0
	Copyright (C) 2012 www.itestyou.com
	All Rights Reserved
*/

function ItyVocabularyTest(answerHash) { 
  this.answerHash = answerHash;
  this.votes = new Array();
}

ItyVocabularyTest.prototype.castVote = function(voteId){
    if (this.ityDone){
		return false;
    }

	var input = document.getElementById("vote" + voteId);  
	if (this.answerHash == calcMD5("" + voteId)) {
		this.ityDone = true;

		if (this.votes.length == 0){
			var tag = document.getElementById("tbrPass");
			if (tag){
				tag.innerHTML = 1 + parseInt(tag.innerHTML);
			}
			this.votes.push(voteId);
		}

		if (input != null){
		  input.className = "vote correct";
		  input.onclick = function(){ return false };
		}

		if (this.beforeSubmit){
			this.beforeSubmit();
		}

		var inVote = this.votes.join(";");
		setTimeout(
			function(){ var frm = document.forms['main']; frm.elements['inVote'].value = inVote; frm.submit(); },  
			1000);
		return false;
	} else {
		var tag = document.getElementById("tbrFail");
		if(tag){
			tag.innerHTML = 1 + parseInt(tag.innerHTML);
		}
		this.votes.push(voteId);
		if (input != null){
		  input.className = "vote incorrect";
		  input.onclick = function(){ return false };
		}  
		return false;
	}
}