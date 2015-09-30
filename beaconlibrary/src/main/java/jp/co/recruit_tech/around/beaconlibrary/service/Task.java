package jp.co.recruit_tech.around.beaconlibrary.service;

/**
 * Created by kusakabe on 15/02/21.
 */
public abstract class Task {

    private boolean isRunning;

    protected Task() {
        isRunning = false;
    }

    protected abstract void onStart();
    protected abstract void onStop();

    public void start() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        onStart();
    }

    public void stop() {
        if (!isRunning) {
            return;
        }
        isRunning = false;
        onStop();
    }
}
