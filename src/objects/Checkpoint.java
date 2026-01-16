package objects;

import main.Game;
import static utilz.Constants.ANI_SPEED;
public class Checkpoint extends GameObject {

    public static final int IDLE = 0;
    public static final int ACTIVE = 1;

    private boolean activated = false;
    private int aniTick = 0, aniIndex = 0;
    public Checkpoint(int x, int y, int objType) {
        super(x, y, objType);
        doAnimation = false; // per ora frame singolo
        createHitbox();
    }
    
    @Override
    public void reset() {
        super.reset();          // rimette aniIndex=0, aniTick=0, active=true, doAnimation coerente
        this.activated = false; // torna allo stato IDLE
        // opzionale: se vuoi forzare il frame idle
        // this.aniIndex = 0;
    }

    private void createHitbox() {
        // hitbox un po’ più stretta dell’immagine
        //initHitbox(20, 60);
        //xDrawOffset = (int)( (CHECKPOINT_WIDTH - 20) / 2f ); // centra la hitbox
        //yDrawOffset = (int)(CHECKPOINT_HEIGHT - 60);
    	initHitbox(16, 16);
    	xDrawOffset = 0;
    	yDrawOffset = 0;

    }

    public void update() {
        // qui in futuro puoi animare l’attivazione
    	if (!active) return;
    	aniTick++;
      /*  if (aniTick >= ANI_SPEED) {
            aniTick = 0;
            aniIndex++;
            if (aniIndex >= 8) 
            	aniIndex = 0;
        }*/
    	if (aniTick >= ANI_SPEED) {
            aniTick = 0;
            aniIndex++;

            if (!activated) { 
                // Loop animation of row 0
                if (aniIndex >= 8) aniIndex = 0;
            } else {
                // Row 1 animation → when it ends, disappear
                if (aniIndex >= 8) {
                    active = false;   // <-- SCOMPARE
                    aniIndex =7;
                    System.out.println("nnnnnnnnnnnnnnn");
                    return;
                }
            }
        }
    }

    public void activate() {
    	  if (!activated) {
    	        activated = true;
    	        aniIndex = 0; 
    	        aniTick = 0;
    	    }
    }
    
    public int getAnimationRow() {
        return activated ? ACTIVE : IDLE;
    }
    
    public boolean isActivated() {
    	return activated; 
    }
    
    public int getAniIndex() { return aniIndex; }
}

