package com.google.firebase.codelab.mlkit;

public class Canary {
    /*
     To deal with asynchronous nature of Firebase, we introduce a canary. This lets us know
     if an asynchronous function has finished.
     */

    private boolean isActive;

    public Canary() {
        this.isActive = false;
    }

    public boolean getIsActive() {
        return this.isActive;
    }

    public  void setIsActive(boolean bool) {
        this.isActive = bool;
    }
}
