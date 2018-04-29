package com.oy.tv.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class StreamHelper {
	
	public static void cleanDir(String path){
		forceDir(path);
		if ( !deleteDir(new File(path))){
			throw new RuntimeException("Failed to delete: " + path);
		}  
		if (!forceDir(path)){
			throw new RuntimeException("Failed to create: " + path);
		}
	}  

	public static boolean forceDir(String path){
		File dir = new File(path);
		if (!dir.exists()){
			return dir.mkdirs();
		} else {   
			return true;
		}
	}
	
	public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
     
        // The directory is now empty so delete it
        return dir.delete();
    }

	public static String fetch(File file) throws Exception {
		return fetch(new BufferedReader(new FileReader(file)));
	}
	
	public static String fetch(BufferedReader in) throws Exception {
        StringBuffer sb = new StringBuffer();
        String str;
        try {		        
	        while (true) {
	        	str = in.readLine();		  
	        	if (str == null) break;
	        	sb.append(str);
	        	sb.append("\r\n");
	        }
        } finally {
        	in.close();
        }
        
        str = sb.toString();
        
        return str;
	}
	
	public static String fetch(String absoluteUrl) throws Exception {
		URL url = new URL(absoluteUrl);			
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("User-agent", 
			"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.0.2) Gecko/20060308 Firefox/1.5.0.2"
		);
		
		InputStreamReader isr = new InputStreamReader(conn.getInputStream(), "UTF-8");
        BufferedReader in = new BufferedReader(isr);
		return fetch(in);
	}
	
	public static String fetchAndCache(String cacheDir, String absoluteUrl) throws Exception {
		return fetchAndCache(cacheDir, absoluteUrl, 0);
	}
	
	public static String cacheHash(String cacheDir, String absoluteUrl){
		return cacheDir + "/" + UtilMD5.string2md5HMA("fetchAndCache", absoluteUrl) + ".dat";		
	}
	
	public static String fetchAndCache(String cacheDir, String absoluteUrl, long waitFor) throws Exception {
		final String hash = cacheHash(cacheDir, absoluteUrl);
		
		File fhash = new File(hash);
		if (!fhash.exists()){
			if (waitFor != 0){
				Thread.sleep(waitFor);
			}
			
			String body = fetch(absoluteUrl);
			
			FileWriter fw = new FileWriter(fhash);
			fw.write(body);
			fw.close();
			  
			System.out.println("Cached " + fhash);
		}
		
		return fetch(new BufferedReader( new FileReader(fhash)));
	}
		
	
	public static byte[] compress(byte[] input) throws Exception {
    
	    // Create the compressor with highest level of compression
	    Deflater compressor = new Deflater();
	    compressor.setLevel(Deflater.BEST_COMPRESSION);
	    
	    // Give the compressor the data to compress
	    compressor.setInput(input);
	    compressor.finish();
	    
	    // Create an expandable byte array to hold the compressed data.
	    // You cannot use an array that's the same size as the orginal because
	    // there is no guarantee that the compressed data will be smaller than
	    // the uncompressed data.
	    ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
	    
	    // Compress the data
	    byte[] buf = new byte[1024];
	    while (!compressor.finished()) {
	        int count = compressor.deflate(buf);
	        bos.write(buf, 0, count);
	    }
        bos.close();
	    
	    // Get the compressed data
	    return bos.toByteArray();
	}
	
	public static byte[] decompress(byte[] input) throws Exception {
		// Create the decompressor and give it the data to compress
	    Inflater decompressor = new Inflater();
	    decompressor.setInput(input);
	    
	    // Create an expandable byte array to hold the decompressed data
	    ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
	    
	    // Decompress the data
	    byte[] buf = new byte[1024];
	    while (!decompressor.finished()) {
            int count = decompressor.inflate(buf);
            bos.write(buf, 0, count);
	    }
        bos.close();
	    
	    // Get the decompressed data
	    return bos.toByteArray();
	}
}
