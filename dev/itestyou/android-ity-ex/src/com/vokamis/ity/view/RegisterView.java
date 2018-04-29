package com.vokamis.ity.view;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.vokamis.ity.R;
import com.vokamis.ity.cmd.RegisterCmd;
import com.vokamis.ity.cmd.ShowHomeViewCmd;
import com.vokamis.ity.cmd.StartTestCmd;
import com.vokamis.ity.mvc.IViewContainer;
import com.vokamis.ity.mvc.IViewManager;
import com.vokamis.ity.rpc.EntityPolicy;
import com.vokamis.ity.rpc.PolicyException;

public class RegisterView implements IViewContainer {

	private IViewManager parent;
	private View root;

	public RegisterView(IViewManager parent, View root) {
		this.root = root;
		this.parent = parent;
	}
	
	public String getTitle(){
		return "New Account";
	}

	public View getContentView() {
		return root;
	}
  
	public void dispatch(final RegisterCmd cmd) {
		// customize ux
		TextView caption = (TextView) root.findViewById(R.id.title);
		caption.setText("New Account");		
		
		// cleanup
		TextView email = (TextView) root.findViewById(R.id.email);
		email.setText("");
		TextView pwd1 = (TextView) root.findViewById(R.id.pwd1);
		pwd1.setText("");
		TextView pwd2 = (TextView) root.findViewById(R.id.pwd2);
		pwd2.setText("");
		
		// act
		Button act = (Button) root.findViewById(R.id.ok);
		act.setText("Register");
		act.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
		act.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				TextView email = (TextView) root.findViewById(R.id.email);
				TextView pwd1 = (TextView) root.findViewById(R.id.pwd1);
				TextView pwd2 = (TextView) root.findViewById(R.id.pwd2);

				String _email = email.getText().toString();
				String _pwd1 = pwd1.getText().toString();
				String _pwd2 = pwd2.getText().toString();

				try {
					EntityPolicy.assertNewAccountData(_email, _pwd1, _pwd2);
				} catch (PolicyException pe){
					parent.toast(pe.getMessage());
					return;					
				}
				
				int accountIdx;
				try {
					accountIdx = parent.getStateManager().register(_email, _pwd1, cmd.getGrade(), false);
				} catch (PolicyException pe){
					parent.toast(pe.getMessage());
					return;					
				}
  
				parent.dispatch(new StartTestCmd(cmd.getGrade(), accountIdx));
			}
		});

		// cancel
		Button cancel = (Button) root.findViewById(R.id.cancel);
		cancel.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
		cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				parent.dispatch(new ShowHomeViewCmd());
			}
		});
	}

}
