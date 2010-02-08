package jf.studybuddy;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by IntelliJ IDEA.
 * User: stork
 * Date: 07-Feb-2010
 * Time: 23:14:22
 * To change this template use File | Settings | File Templates.
 */
public class SBActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}
