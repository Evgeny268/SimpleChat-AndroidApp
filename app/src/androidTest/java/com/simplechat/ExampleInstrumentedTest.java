package com.simplechat;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import internet.InetWorker;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("com.simplechat", appContext.getPackageName());
    }

    @Test
    public void inetTest(){
        URL url = null;
        try {
            url = new URL("http://192.168.1.35:4444");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        URI uri = null;
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        //InetWorker inetWorker = new InetWorker(uri);
        InetWorker inetWorker = new InetWorker(uri);
        inetWorker.connect();
        while (!inetWorker.isOpen()){
            ;
        }
        inetWorker.send("test");
    }
}
