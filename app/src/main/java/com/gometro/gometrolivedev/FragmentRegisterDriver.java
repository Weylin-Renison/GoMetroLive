package com.gometro.gometrolivedev;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * Created by wprenison on 2015/10/13.
 */
public class FragmentRegisterDriver extends Fragment implements View.OnClickListener
{
    private final String TAG = "fragRegisterDriver";

    //Vars
    private WelcomeActivity activity;

    //Views
    private EditText etxtFirstName;
    private EditText etxtLastName;
    private EditText etxtEmail;
    private EditText etxtPassword;
    private EditText etxtRetypePassword;
    private EditText etxtOrgCode;
    private DatePicker dpDateofBirth;
    private ImageButton ibtnHelpOrgCode;
    private Button btnRegister;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View constuctedView = inflater.inflate(R.layout.frag_register_driver, container, false);

        //Get handle on views
        etxtFirstName = (EditText) constuctedView.findViewById(R.id.etxtFRDFirstName);
        etxtLastName = (EditText) constuctedView.findViewById(R.id.etxtFRDLastName);
        etxtEmail = (EditText) constuctedView.findViewById(R.id.etxtFRDEmail);
        etxtPassword = (EditText) constuctedView.findViewById(R.id.etxtFRDPassword);
        etxtRetypePassword = (EditText) constuctedView.findViewById(R.id.etxtFRDRetypePassword);
        etxtOrgCode = (EditText) constuctedView.findViewById(R.id.etxtFRDOrganizationCode);
        dpDateofBirth = (DatePicker) constuctedView.findViewById(R.id.dpFRDDateOfBirth);
        dpDateofBirth.setMaxDate(System.currentTimeMillis());

        ibtnHelpOrgCode = (ImageButton) constuctedView.findViewById(R.id.ibtnFRDOrganizationCodeInfo);
        btnRegister = (Button) constuctedView.findViewById(R.id.btnFRDRegister);

        ibtnHelpOrgCode.setOnClickListener(this);
        btnRegister.setOnClickListener(this);

        return constuctedView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        activity = (WelcomeActivity) getActivity();
    }

    @Override
    public void onClick(View v)
    {
        if(v == ibtnHelpOrgCode)
        {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setTitle("Organization Code?");
            dialogBuilder.setMessage("This is a code specially provided to your organization. You can not register without this code. If you do not have this code please contact your organization.");
            dialogBuilder.setPositiveButton("OK, GOT IT", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });

            AlertDialog orgCodeInfoDialog = dialogBuilder.create();
            orgCodeInfoDialog.show();

        }
        else if(v == btnRegister)
        {
            //Get values
            String firstName = etxtFirstName.getText().toString();
            String lastName = etxtLastName.getText().toString();
            String dateOfBirth = dpDateofBirth.getDayOfMonth() + "/" + dpDateofBirth.getMonth() + "/" + dpDateofBirth.getYear();
            String email = etxtEmail.getText().toString();
            String password = etxtPassword.getText().toString();
            String retypePassword = etxtRetypePassword.getText().toString();
            String orgCode = etxtOrgCode.getText().toString();

            //Validate fields
            boolean valid = true;

            if(firstName.isEmpty())
            {
                valid = false;
                etxtFirstName.setError("Required field");
            }

            if(lastName.isEmpty())
            {
                valid = false;
                etxtLastName.setError("Required field");
            }

            if(email.isEmpty())
            {
                valid = false;
                etxtEmail.setError("Required field");
            }

            //TODO: regex check for valid email address


            if(password.isEmpty())
            {
                valid = false;
                etxtPassword.setError("Required field");
            }

            if(retypePassword.isEmpty())
            {
                valid = false;
                etxtRetypePassword.setError("Required field");
            }

            if(password != retypePassword)
            {
                valid = false;
                etxtRetypePassword.setError("Password field do not match");
            }

            if(orgCode.isEmpty())
            {
                valid = false;
                etxtOrgCode.setError("Required field");
            }

            //register user to server
            if(valid)
            {
                ServerApiHelper apiHelp = new ServerApiHelper(activity);
                apiHelp.registerDriver(firstName, lastName, dateOfBirth, email, password, orgCode, new OnServerResponseListener()
                {
                    @Override
                    public void onResponse(boolean successfull)
                    {
                        if(successfull)
                        {
                            Toast.makeText(activity, "You have been successfully registered", Toast.LENGTH_LONG).show();
                            activity.getSupportFragmentManager().popBackStack();
                        }
                        else
                            Toast.makeText(activity, "Your registration has failed", Toast.LENGTH_LONG).show();
                    }
                });
            }

        }
    }
}
