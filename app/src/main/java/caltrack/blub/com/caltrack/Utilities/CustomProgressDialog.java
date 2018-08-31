package caltrack.blub.com.caltrack.Utilities;

import android.app.ProgressDialog;
import android.content.Context;

public class CustomProgressDialog extends android.app.ProgressDialog
{
    public CustomProgressDialog(Context context)
    {
        super(context);

        setMessage("Loading...");
        setIndeterminate(false);
        setProgressStyle(ProgressDialog.STYLE_SPINNER);
        setCanceledOnTouchOutside(false);
        show();
    }
}
