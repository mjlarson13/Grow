package core;

import common.D2.Vector2D;

import java.awt.*;

import static common.D2.Vector2D.Vec2DNormalize;
import static common.D2.Vector2D.div;
import static common.D2.Vector2D.mul;

public class Player extends Sprite {

    private static int score = 0;


    public Player(GameWorld world) {
        super(
                world,          //gameworld
                new Vector2D(100, 100),  //position
                0,  //rotation
                new Vector2D(0, 0),  //initial velocity
                10, //mass
                10,  //max speed
                100,
                10,
                20);
    }



    public void Update(double time_elapsed) {
        //update the time elapsed
        m_dTimeElapsed = time_elapsed;
        Vector2D OldPos = Pos();
        m_vPos = new Vector2D(MouseInfo.getPointerInfo().getLocation().getX(), MouseInfo.getPointerInfo().getLocation().getY() );

        score++;
       // System.out.println(score / 100);

        //grow(.05);

        //EnforceNonPenetrationConstraint(this, World()->Agents());

        //treat the screen as a toroid
        //WrapAround(m_vPos, m_pWorld.cxClient(), m_pWorld.cyClient());

        //update the sprite's current cell if space partitioning is turned on
        if (Steering().isSpacePartitioningOn()) {
            World().CellSpace().UpdateEntity(this, OldPos);
        }

    }

}
