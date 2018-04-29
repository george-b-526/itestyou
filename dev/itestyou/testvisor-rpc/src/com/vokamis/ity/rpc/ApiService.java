package com.vokamis.ity.rpc;


public interface ApiService {
	
	public static class LookupEnvelope {
		
		public enum Status {
			SUCCESS, DENY_ACCESS, FAILED
		}
		
		Status status;
		String reason;
		Integer userId;
		String name;
		boolean isPro;

		public LookupEnvelope(){}
		
		public LookupEnvelope(Status status, String reason){
			this(status, reason, null, null, false);
		}
		
		public LookupEnvelope(Status status, String reason, Integer userId, String name, boolean isPro){
			this.status = status;
			this.reason = reason;
			this.userId = userId;
			this.isPro = isPro;
			this.name = name;
		}

		public Status getStatus() {
    	return status;
    }

		public boolean isPro() {
    	return isPro;
    }
		
		public String getReason() {
    	return reason;
    }
		
		public Integer getUserId(){
			return userId;
		}

		public String getName() {
    	return name;
    }

		public static LookupEnvelope valueOf(String responseText){
			LookupEnvelope result = new LookupEnvelope();
			if (responseText == null){
				result.status = Status.FAILED;
				result.userId = null;
				result.name = null;
				result.reason = "Server sent no reply.";
			} else {
				XmlParserLight xml = new XmlParserLight();
				xml.parse(responseText);
				
				result.status = xml.get("status", Status.values(), Status.FAILED);

				String userId = xml.get("user-id", null);
				if (userId != null){
					result.userId = Integer.valueOf(userId);					
				}
				
				result.name = xml.get("name", null);
				result.isPro = "1".equals(xml.get("is-pro", null));
				result.reason = xml.get("reason", null);
			}
			return result;
		}
		
	}
	  
	public static class RegisterEnvelope {
		
		public enum Status {
			SUCCESS, FAILED_EXISTS_SUCCESS_AUTH, 
			FAILED_EXISTS_FAILED_AUTH, FAILED
		}
		
		Status status;
		String token;
		String reason;

		RegisterEnvelope(){ }
		
		public RegisterEnvelope(Status status, String authToken, String reason){
			this.status = status;
			this.token = authToken;
			this.reason = reason;
		}
				
		public Status getStatus() {
    	return status;
    }

		public String getToken() {
    	return token;
    }

		public String getReason() {
    	return reason;
    }
		
		public static RegisterEnvelope valueOf(String responseText){
			RegisterEnvelope result = new RegisterEnvelope();
			if (responseText == null){
				result.status = Status.FAILED;
				result.token = null;
				result.reason = "Server sent no reply.";
			} else {
				XmlParserLight xml = new XmlParserLight();
				xml.parse(responseText);
				
				result.status = xml.get("status", Status.values(), Status.FAILED);
				result.token = xml.get("token", null);
				result.reason = xml.get("reason", null);
			}
			return result;
		}
	}
	
	public static class RecoverEnvelope{
		public enum Status {
			SUCCESS, FAILED_UNKNOWN_CUSTOMER, FAILED
		}

		Status status;
		String reason;

		RecoverEnvelope(){}
		
		public RecoverEnvelope(Status status, String reason){
			this.status = status;
			this.reason = reason;
		}
		
		public Status getStatus(){
			return status;
		}
		
		public String getReason(){
			return reason;
		}
		
		public static RecoverEnvelope valueOf(String responseText){
			RecoverEnvelope result = new RecoverEnvelope();
			if (responseText == null){
				result.status = Status.FAILED;
				result.reason = "Server sent no reply.";
			} else {
				XmlParserLight xml = new XmlParserLight();
				xml.parse(responseText);
				
				result.status = xml.get("status", Status.values(), Status.FAILED);
				result.reason = xml.get("reason", null);
			}
			return result;
		}
	}

	public static class RepasswordEnvelope{
		public enum Status {
			SUCCESS, FAILED_AUTH, FAILED
		}

		Status status;
		String reason;

		RepasswordEnvelope(){}
		
		public RepasswordEnvelope(Status status, String reason){
			this.status = status;
			this.reason = reason;
		}
		
		public Status getStatus(){
			return status;
		}
		
		public String getReason(){
			return reason;
		}
		
		public static RepasswordEnvelope valueOf(String responseText){
			RepasswordEnvelope result = new RepasswordEnvelope();
			if (responseText == null){
				result.status = Status.FAILED;
				result.reason = "Server sent no reply.";
			} else {
				XmlParserLight xml = new XmlParserLight();
				xml.parse(responseText);
				
				result.status = xml.get("status", Status.values(), Status.FAILED);
				result.reason = xml.get("reason", null);
			}
			return result;
		}
	}
	
	public RegisterEnvelope register(String name, String pwd, boolean noCreate, String deviceId,
	    String ipAddress, String userAgent) throws PolicyException;
	
	public RecoverEnvelope recover(String name, String deviceId,
	    String ipAddress, String userAgent) throws PolicyException;

	public RepasswordEnvelope repassword(String name, String oldPwd, String newPwd, String deviceId, 
			String ipAddress, String userAgent) throws PolicyException;
	
}


