package com.jbeleno.photobox.util;

/**
 * Created by Juan on 25/09/16.
 */

import android.content.Context;
import android.widget.Toast;

public class Message {
    public static void show(String msj, Context contexto){
        Toast toast = Toast.makeText(contexto, msj, Toast.LENGTH_LONG);
        toast.show();
    }
}
