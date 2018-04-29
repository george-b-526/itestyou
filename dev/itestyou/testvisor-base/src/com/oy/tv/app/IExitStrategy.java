package com.oy.tv.app;


public interface IExitStrategy {

	public void exit();
	
	public static class UrlExitStrategy implements IExitStrategy {
		
		private BaseViewCtx ctx;
		private String exitUrl;
		
		public UrlExitStrategy(BaseViewCtx ctx, String exitUrl){
			this.ctx = ctx;
			this.exitUrl = exitUrl;
		}
		
		public void exit(){
			ctx.sendRedirect(303, exitUrl);
		}
	}  
	
}
