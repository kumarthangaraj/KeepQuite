package com.superio.keepquite;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by kumar_thangaraj on 22/10/15.
 */
public class BootService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("inside BootSerice");
        return null;
    }
}
