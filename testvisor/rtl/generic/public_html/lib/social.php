<?php

	function ity_new_fb(){		
		require_once '../lib/json/jsonwrapper.php';
		require_once '../lib/fb/facebook-php-sdk/src/facebook.php';

		$facebook = new Facebook(array(
		  'appId'  => '334171366611787',
		  'secret' => '4f4bfb98aac890f88e2f9ff7c5113c91',
		));
		
		return $facebook;
	}

	function ity_post_fb($message, $uid, &$reason, $link = null, $name = null){
		$token = "AAAEv7VWZA20sBAN5fPoUGXhYwZCarToYOeXME3yRnTqOCUNhJ2s8XTIWVnVCMDuZCJOuxS07UPSq5V0vIxxRHZAZBrGLrj8wZD";
		
		$facebook = ity_new_fb();
		$fields = "access_token=".urlencode($token)."&uid=".urlencode($uid)."&message=".urlencode($message);
		if ($link != null){
			$action_links = '[{"text": "'.$name.'", "href": "'.$link.'"}]';
			$fields .= "&action_links=".urlencode($action_links);
		}

		$ch = curl_init("https://api.facebook.com/method/stream.publish");
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
		curl_setopt($ch, CURLOPT_POST, 1);
		curl_setopt($ch, CURLOPT_POSTFIELDS, $fields);		  

		$output = curl_exec($ch);      
		curl_close($ch);

		$reason = $output;
		if (strstr($output, "error_code") === FALSE){
			return true;	
		} else {
			return false;
		}
	}

	function ity_fb_make_allow_url(){		
		$facebook = ity_new_fb();

		return $facebook->getLoginUrl(
				array( 
						'scope'  => 'email,publish_stream,user_birthday,user_location,user_about_me,user_hometown,manage_pages,offline_access',
						'enable_profile_selector' => '1',
						'profile_selector_ids' => 'ITestYou'
				)
		);
	}

	function ity_post_twitter($message, $hash_tag, &$reason){
		require_once 'json/jsonwrapper.php';
		require_once "twitter/twitteroauth.php";

		define("CONSUMER_KEY", "hZsSkuv96zNaLI50LYiOBA");
		define("CONSUMER_SECRET", "vWeaI8kZKPmohcYTfHrdGGFdIzrPrvjNESiXfBUec");
		define("OAUTH_TOKEN", "368693446-p1EhPVzk4W96o8u8pdqN2srZTeUGRgoAVYMXu6WN");
		define("OAUTH_SECRET", "lSVconGPk0E22UvNbtrVwJIETz4TbnHRrvzCouE2W6g");

		// trim to 140 and hash tag
		if (strlen($message) > 130){
			$message = substr($message, 0, 130);
			$message .= "...";
		}
		$message .= " ".$hash_tag;

		$connection = new TwitterOAuth(CONSUMER_KEY, CONSUMER_SECRET, OAUTH_TOKEN, OAUTH_SECRET);
		$content = $connection->get('account/verify_credentials');
 
		// validate
		if ($content != null && $content->id_str = "368693446"){
		} else {
			$reason = $content;
			return false;	
		}

		// post
		$content = $connection->post('statuses/update', array('status' => $message));
		if ($content){
			return true;
		} else {
			$reason = $content;
			return false;
		}
	}
?>