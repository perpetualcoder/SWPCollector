package edu.lsu.cct.swp;

public class Here {
    public static void here(Object o) {
        Throwable t = new Throwable();
        StackTraceElement[] ste = t.getStackTrace();
        System.out.println(ste[1]+": "+o);
    }
}
