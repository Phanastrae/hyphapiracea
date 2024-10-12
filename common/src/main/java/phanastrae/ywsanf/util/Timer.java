package phanastrae.ywsanf.util;

public class Timer {

    private long startTime;
    private long dt;

    public Timer() {
        this.startTime = getTime();
    }

    public void start() {
        this.startTime = getTime();
    }

    public void stop() {
        long stopTime = getTime();
        this.dt = stopTime - this.startTime;
    }

    public long nano() {
        return this.dt;
    }

    public long micro() {
        return this.dt / 1000;
    }

    public long milli() {
        return this.dt / 1000000;
    }

    public static Timer time(Runnable runnable) {
        Timer timer = new Timer();
        timer.start();
        runnable.run();
        timer.stop();

        return timer;
    }

    private static long getTime() {
        return System.nanoTime();
    }
}
