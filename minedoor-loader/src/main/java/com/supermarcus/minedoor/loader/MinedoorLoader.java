package com.supermarcus.minedoor.loader;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

public class MinedoorLoader {
    public static void main(String args[]){
        try{
            System.out.println("Loading libraries...");
            File loaderJar = new File(MinedoorLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File libDir = new File(loaderJar.getParentFile(), "lib");

            ArrayList<URL> urls = new ArrayList<>();

            for(File f : libDir.listFiles((dir, name) -> {
                return name.endsWith(".jar");
            })){
                urls.add(f.toURI().toURL());
            }

            URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[urls.size()]));
            Class<?> mdClass = loader.loadClass("com.supermarcus.minedoor.MineDoor");
            Class[] c = new Class[1];
            c[0] = String[].class;
            Method method = mdClass.getMethod("main", c);
            String[][] a = new String[1][];
            a[0] = args;
            method.invoke(null, a);
        }catch (Exception e){
            System.out.println("Unable to load MineDoor");
            e.printStackTrace();
        }
    }
}
