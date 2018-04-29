package com.vokamis.ity.view;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.vokamis.ity.R;
import com.vokamis.ity.cmd.AddExistingUserCmd;
import com.vokamis.ity.cmd.AuthUserCmd;
import com.vokamis.ity.cmd.StartTestCmd;
import com.vokamis.ity.mvc.IViewContainer;
import com.vokamis.ity.mvc.IViewManager;
import com.vokamis.ity.rpc.EntityPolicy;
import com.vokamis.ity.rpc.PolicyException;
import com.vokamis.ity.state.ActorAccount;

public class AuthUserView implements IViewContainer {
	private IViewManager parent;
	private View root;

	public AuthUserView(IViewManager parent, View root) {
		this.root = root;
		this.parent = parent;
	}
	
	public String getTitle(){
		return "Existing Account";
	} 

	public View getContentView() {
		return root;
	}

	public void dispatch(AuthUserCmd cmd) {
		dispatchAny(cmd.getGrade(), cmd.getAccoundIdx(), false);
	}
		
	public void dispatch(final AddExistingUserCmd cmd) {
		dispatchAny(cmd.getGrade(), cmd.getAccoundIdx(), true);
	}
		
	private void dispatchAny(final int grade, int accountIdx, boolean canEditEmail) {
		
		// title
		TextView title = (TextView) root.findViewById(R.id.title);
		title.setText("Existing Account");				
		
		// pwd1
		final TextView pwd1 = (TextView) root.findViewById(R.id.pwd1);
		pwd1.setText("");
		
		// hide confirm password
		View caption = (View) root.findViewById(R.id.pwd2Caption);
		caption.setVisibility(View.INVISIBLE);		
		TextView pwd2 = (TextView) root.findViewById(R.id.pwd2);
		pwd2.setVisibility(View.INVISIBLE);
				
		// email
		final TextView email = (TextView) root.findViewById(R.id.email);
		email.setEnabled(true);
		if (accountIdx != -1){
  		email.setText(
  			parent.getStateManager().getSettings().
  			getAccount(accountIdx).getEmail());	
			email.setEnabled(canEditEmail);
		} else {
			email.setText("");
    }
		
		// act
		Button act = (Button) root.findViewById(R.id.ok);
		act.setText("Sign In");
		act.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
		act.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				register(email.getText().toString(), pwd1.getText().toString(), grade, true);
			}
		});	  

		// forgot password
		Button cancel = (Button) root.findViewById(R.id.cancel);
		cancel.setText("Forgot Password");
		cancel.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
		cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					parent.getStateManager().recover(email.getText().toString());
					parent.toast("New password was sent to your email.");
				} catch (PolicyException pe){ 
					parent.toast(pe.getMessage());
					return;					
				}							
			}
		});
			
		// try creating account automatically
		ActorAccount aa = parent.getStateManager().getSettings().getAccount(accountIdx);
		if (aa != null && aa.isGeneratedPassword()){
			register(aa.getEmail(), aa.getPassword(), grade, false);			
		}
  
	}
		
	private void register(String email, String pwd, int grade, boolean existing){
		int newAccountIdx;
		try {
			EntityPolicy.assertNewAccountData(email, pwd, pwd);
			newAccountIdx = parent.getStateManager().register(email, pwd, grade, existing);
			parent.toast("Authenticated!");
		} catch (PolicyException pe){
			parent.toast(pe.getMessage());
			return;					
		}
		parent.dispatch(new StartTestCmd(grade, newAccountIdx));
	}
	
}
