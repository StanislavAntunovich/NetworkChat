package ru.geekbrains.java3.lesson4;

public class MainClass {
    private static volatile char current_char = 'A';
    private static final Object monitor = new Object();

    private static final int REPEAT_COUNT = 5;

    public static void main(String[] args) {
        new Thread(MainClass::printA).start();
        new Thread(MainClass::printC).start();
        new Thread(MainClass::printB).start();
    }

    public static void printA() {
        synchronized (monitor) {
            for (int i = 0; i< REPEAT_COUNT; i++) {
                try {
                    while (current_char != 'A') {
                        monitor.wait();
                    }
                    System.out.print('A');
                    current_char = 'B';
                    monitor.notifyAll();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void printB() {
        synchronized (monitor) {
            for (int i = 0; i< REPEAT_COUNT; i++) {
                try {
                    while (current_char != 'B') {
                        monitor.wait();
                    }
                    System.out.print('B');
                    current_char = 'C';
                    monitor.notifyAll();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void printC() {
        synchronized (monitor) {
            for (int i = 0; i< REPEAT_COUNT; i++) {
                try {
                    while (current_char != 'C') {
                        monitor.wait();
                    }
                    System.out.print('C');
                    current_char = 'A';
                    monitor.notifyAll();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }





}
