<?php

	function ity_cache_get($name, $min){
		  $cachetime = $min * 60;

		  $cachefile_created = (@file_exists($name)) ? @filemtime($name) : 0;
		  
		  @clearstatcache();

		  if (time() - $cachetime < $cachefile_created) {
			ob_start();
			@readfile($name);
			$body = ob_get_contents();
			ob_end_clean();
			return $body;
		  } else {
			return null;
		  }
	}

	function ity_cache_put($name, $body){
		  $fp = @fopen($name, 'w'); 
		  @fwrite($fp, $body);
		  @fclose($fp); 
	}

?>