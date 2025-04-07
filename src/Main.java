import javax.swing.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Scene scene = new Scene();
        JDBC jdbc = new JDBC();
        Window window = new Window();
        JFrame frame = window.getFrame();
        Palette palette = new Palette();
        Intro intro = new Intro(palette);
        Login login = new Login(palette, jdbc);
        BookingMenu bookingMenu = new BookingMenu(palette, scene, frame, jdbc);
        MainMenu mainMenu = new MainMenu(palette, scene, frame, intro, login, bookingMenu);
        bookingMenu.setMainMenu(mainMenu);

        window.setVisible(true);

        updateScene(scene, frame, intro, login, mainMenu, bookingMenu);
    }

    private static void updateScene(Scene scene, JFrame frame, Intro intro, Login login, MainMenu mainMenu, BookingMenu bookingMenu) {
        boolean running = true;

        while (running) {
            switch (scene.getScene()) {
                case "Intro":
                    intro.resetIntro();
                    frame.getContentPane().removeAll();
                    frame.add(intro);
                    frame.revalidate();
                    frame.repaint();

                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    frame.remove(intro);
                    scene.setScene("Login");
                    break;

                case "Login":
                    frame.getContentPane().removeAll();
                    JSplitPane splitPane = login.createSplitPane();
                    frame.add(splitPane);
                    frame.revalidate();
                    frame.repaint();

                    login.clearFields();
                    Object lock = new Object();

                    login.setOnLoginSuccess(() -> {
                        synchronized (lock) {
                            scene.setScene("Menu");
                            lock.notify();
                        }
                    });

                    synchronized (lock) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    break;

                case "Menu":
                    frame.getContentPane().removeAll();
                    frame.add(mainMenu);
                    frame.revalidate();
                    frame.repaint();

                    while (!mainMenu.isSignedOut()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    mainMenu.setSignedOut(false);
                    scene.setScene("Intro");
                    break;

                case "Booking":
                    frame.getContentPane().removeAll();
                    frame.add(bookingMenu);
                    frame.revalidate();
                    frame.repaint();
                default:
                    running = false;
                    break;
            }
        }
    }
}
