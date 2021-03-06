/**
 * @author Petr (http://www.sallyx.org/)
 */
package core;

import java.awt.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import common.misc.CppToJava;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import common.Time.PrecisionTimer;

import static common.misc.Cgdi.gdi;

import javax.swing.*;
import java.awt.image.BufferedImage;

import static core.constants.*;
import static common.misc.WindowUtils.*;
import static core.resource.*;
import static common.windows.*;
import static core.resource.IDR_MENU1;
import static common.misc.WindowUtils.ChangeMenuState;

public class Main {
//--------------------------------- Globals ------------------------------
//
//------------------------------------------------------------------------

    static String g_szApplicationName = "Steering Behaviors - Another Big Shoal";
    // static String g_szWindowClassName = "MyWindowClass";
    static GameWorld g_GameWorld;
    static Lock GameWorldLock = new ReentrantLock(); // bacause of restart (g_GameWorld could be null for a while)

    /**
     *	The entry point of the windows program
     */
    public static void main(String[] args) {
        final JFrame window = new JFrame(g_szApplicationName);
        window.setIconImage(LoadIcon("/core/example.png"));
        window.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        final BufferedImage buffer = new BufferedImage(constWindowWidth, constWindowHeight, BufferedImage.TYPE_INT_RGB);
        final Graphics2D hdcBackBuffer = buffer.createGraphics();
        //these hold the dimensions of the client window area
        final int cxClient = buffer.getWidth();
        final int cyClient = buffer.getHeight();
        //seed  number generator
        common.misc.utils.setSeed(0);

        /**
         * This sets this screen to full screen
         * TODO: maybe make 100% full screen, or maybe decide on smaller window size
         */
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        window.setUndecorated(true);
        window.setVisible(true);
        window.setLayout(new BorderLayout());

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point hotSpot = new Point(0,0);
        BufferedImage cursorImage = new BufferedImage(1, 1, BufferedImage.TRANSLUCENT);
        Cursor invisibleCursor = toolkit.createCustomCursor(cursorImage, hotSpot, "InvisibleCursor");
        window.getContentPane().setCursor(invisibleCursor);


        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        //Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        window.setResizable(false);

        int y = center.y - window.getHeight() / 2;
        window.setLocation(center.x - window.getWidth() / 2, y >= 0 ? y : 0);
        g_GameWorld = new GameWorld(cxClient, cyClient);


        final JPanel panel = new JPanel() {

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                hdcBackBuffer.setPaint(Color.RED);
                gdi.StartDrawing(hdcBackBuffer);
                //fill our backbuffer with white
                gdi.fillRect(Color.DARK_GRAY, 0, 0, constWindowWidth, constWindowHeight);
                GameWorldLock.lock();
                g_GameWorld.Render();
                GameWorldLock.unlock();
                gdi.StopDrawing(hdcBackBuffer);


                g.drawImage(buffer, 0, 0, null);
            }
        };
        panel.setSize(constWindowWidth, constWindowHeight);
        panel.setPreferredSize(new Dimension(constWindowWidth, constWindowHeight));
        window.add(panel);
        window.pack();

//        ChangeMenuState(window.getMenu(), IDR_PRIORITIZED, MFS_CHECKED);
//        ChangeMenuState(window.getMenu(), ID_VIEW_FPS, MFS_CHECKED);

        window.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    GameWorldLock.lock();
                    g_GameWorld.SetCrosshair(new POINTS(e.getPoint()));
                    GameWorldLock.unlock();
                }
            }
        });

        window.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                CppToJava.keyCache.released(e);
                switch (e.getKeyChar()) {
                    case KeyEvent.VK_ESCAPE: {
                        System.exit(0);
                    }
                    break;
                    /*
                    case 'r':
                    case 'R': {
                        GameWorldLock.lock();
                        g_GameWorld = null;
                        g_GameWorld = new GameWorld(cxClient, cyClient);
                        GameWorldLock.unlock();
                    }
                    break;*/
                }//end switch

                //handle any others
                GameWorldLock.lock();
                g_GameWorld.HandleKeyPresses(e);
                GameWorldLock.unlock();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                CppToJava.keyCache.pressed(e);
            }
        });

        //make the window visible
        window.setVisible(true);

        //create a timer
        final PrecisionTimer timer = new PrecisionTimer();

        timer.SmoothUpdatesOn();

        //start the timer
        timer.Start();

        while (true) {
            //update
            GameWorldLock.lock();
            g_GameWorld.Update(timer.TimeElapsed());
            GameWorldLock.unlock();
            //render
            //panel.revalidate();
            panel.repaint();

            try {
                //System.out.println(timer.TimeElapsed());
                Thread.sleep(2);
            } catch (InterruptedException ex) {
            }
        }//end while
    }


}