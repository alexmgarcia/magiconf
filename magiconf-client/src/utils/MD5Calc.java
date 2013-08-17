package utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Calc {

	public static String md5Calc(String s){
		MessageDigest m;
		try {
			m = MessageDigest.getInstance("MD5");

			m.update(s.getBytes(),0,s.length());
			return new BigInteger(m.digest()).toString(16);
			//System.out.println("MD5: "+);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}


	public static String md5Java(String message){
		String digest = null; 
		try { 
			MessageDigest md = MessageDigest.getInstance("MD5"); 
			byte[] hash = md.digest(message.getBytes("UTF-8"));
			StringBuilder sb = new StringBuilder(2*hash.length);
			for(byte b : hash){ 
				sb.append(String.format("%02x", b&0xff)); 
			} 
			digest = sb.toString(); 
		} 
		catch (UnsupportedEncodingException ex) {
			//Logger.getLogger(StringReplace.class.getName()).log(Level.SEVERE, null, ex); 
		} catch (NoSuchAlgorithmException ex) {
			//Logger.getLogger(StringReplace.class.getName()).log(Level.SEVERE, null, ex); 
		} 
		return digest; 
	}
}
