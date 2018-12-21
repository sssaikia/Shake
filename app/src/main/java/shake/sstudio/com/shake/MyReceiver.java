package shake.sstudio.com.shake;

import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {

    public MyReceiver() {
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        String START="RESTART_SERVICE";
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");
        //Toast.makeText(context,"recieving",Toast.LENGTH_SHORT).show();
        ClipboardManager.OnPrimaryClipChangedListener mPrimaryChangeListener = new ClipboardManager.OnPrimaryClipChangedListener() {
            public void onPrimaryClipChanged() {
                Toast.makeText(context, "Text copied..(brdcst)", Toast.LENGTH_SHORT).show();
                // this will be called whenever you copy something to the clipboard
            }
        };
        if (intent.getAction().equals(START)){
            Intent sstart=new Intent(context,MyService.class);
            context.startService(sstart);
        }
    }
}
