package com.oy.tv.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class MementoManager {

	public static String encode(Object object){
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
			ObjectOutputStream ous = new ObjectOutputStream(bos) ;
	        ous.writeObject(object);
	        ous.close();
	        
	        byte [] bytes = bos.toByteArray();
	        bytes = compress(bytes);  
	        
	        return new sun.misc.BASE64Encoder().encode(bytes);
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}
	    
	public static Object decode(String memento){
		if (memento == null || memento.length() == 0){
			throw new RuntimeException("Empty memento.");
		}
		try {
			byte [] bytes = new sun.misc.BASE64Decoder().decodeBuffer(memento);
			bytes = uncompress(bytes);
			ObjectInputStream ois =  new ObjectInputStream(new ByteArrayInputStream(bytes));
			return ois.readObject();
		} catch (Exception e){
			throw new RuntimeException("Error decoding memento.", e);
		}
	} 
	
	public static byte[] compress(byte [] buffer) throws IOException {
	    // Create the compressor with highest level of compression
	    Deflater compressor = new Deflater();
	    compressor.setLevel(Deflater.BEST_COMPRESSION);
	    
	    // Give the compressor the data to compress
	    compressor.setInput(buffer);
	    compressor.finish();
	    
	    // Create an expandable byte array to hold the compressed data.
	    // You cannot use an array that's the same size as the orginal because
	    // there is no guarantee that the compressed data will be smaller than
	    // the uncompressed data.
	    ByteArrayOutputStream bos = new ByteArrayOutputStream(buffer.length);
	      
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
	
	public static byte[] uncompress(byte [] buffer) throws IOException {
		// Create the decompressor and give it the data to compress
	    Inflater decompressor = new Inflater();
	    decompressor.setInput(buffer);
	    
	    // Create an expandable byte array to hold the decompressed data
	    ByteArrayOutputStream bos = new ByteArrayOutputStream(buffer.length);
	    
	    // Decompress the data
	    byte[] buf = new byte[1024];
	    while (!decompressor.finished()) {
	        try {
	            int count = decompressor.inflate(buf);
	            bos.write(buf, 0, count);
	        } catch (DataFormatException e) {
	        }
	    }
        bos.close();
	    
	    // Get the decompressed data
	    return bos.toByteArray();
	}
	
}
