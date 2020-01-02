package com.heylhh.crawler;


public class MoveEntity {
    private int x;
    private int y;
    private int sleepTime;//毫秒

    public MoveEntity(){

    }

    public MoveEntity(int x, int y, int sleepTime) {
        this.x = x;
        this.y = y;
        this.sleepTime = sleepTime;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }
}
