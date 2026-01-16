package objects;

import main.Game;

public class Treasure extends GameObject {

	private boolean collected = false;
    private boolean animationFinished = false;
    private boolean activated = false;
	public Treasure(int x, int y, int objType) {
		super(x, y, objType);
		// TODO Auto-generated constructor stub
		doAnimation = true;
        createHitbox();
	}

	private void createHitbox() {
		// TODO Auto-generated method stub
		//initHitbox(60, 50);
		initHitbox(30, 25); // dimensioni collisione
	    xDrawOffset = (int) (4 * Game.SCALE);
	    yDrawOffset = (int) (5 * Game.SCALE);
	}
	
    public void update() {
    	if (collected && doAnimation) {
            updateAnimationTick();

            // controlla quando l'animazione ha raggiunto l’ultimo frame
            if (aniIndex == 5) { // ci sono 6 frame (0–5)
                aniIndex = 5;     // blocca sull’ultimo frame
                doAnimation = false;
                animationFinished = true;
            }
        }
    }
    
    public void onCollected() {
        collected = true;
        doAnimation = true;
        aniTick = 0;
        aniIndex = 0;
        animationFinished = false;
    }

    public boolean isCollected() {
        return collected;
    }
    
    public boolean isAnimationFinished() {
        return animationFinished;
    }
    
 

    public boolean isActivated() { return activated; }
    public void activate() { activated = true; }

}
