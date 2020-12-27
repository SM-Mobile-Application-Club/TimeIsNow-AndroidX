package com.MAD.TimeIsNow.utilities;

import com.google.android.material.textfield.TextInputLayout;

public class shortcuts {
    public static String getText(TextInputLayout field) {
        return  field.getEditText().getText().toString().trim();
    }

}
