package entities;

import static utilz.Constants.Directions.DOWN;
import static utilz.Constants.Directions.LEFT;
import static utilz.Constants.Directions.UP;
import static utilz.HelpMethods.CanMoveHere;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import main.Game;

public abstract class Entity {

	protected float x, y;
	protected int width, height;
	
	protected Rectangle2D.Float hitbox;
	protected int aniTick, aniIndex;
	protected int state;
	protected float airSpeed;
	protected boolean inAir = false;
	protected int maxHealth;
	protected int currentHealth;
	protected Rectangle2D.Float attackBox;
	protected float walkSpeed;

	protected int pushBackDir;
	protected float pushDrawOffset;
	protected int pushBackOffsetDir = UP;

	public Entity(float x, float y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	/**
	 * Aggiorna lo spostamento grafico dell’entità quando viene respinta
	 * (ad esempio dopo aver subito un colpo).
	 * L’entità si muove leggermente verso l’alto, poi ritorna giù
	 * fino a riportare l’offset a 0.
	 */
	protected void updatePushBackDrawOffset() {
		float speed = 0.95f;
		float limit = -30f; // altezza massima che può raggiungere durante il rimbalzo 

		if (pushBackOffsetDir == UP) {
			pushDrawOffset -= speed;
			if (pushDrawOffset <= limit)
				pushBackOffsetDir = DOWN;
		} else {
			pushDrawOffset += speed;
			if (pushDrawOffset >= 0)
				pushDrawOffset = 0;
		}
	}

	/**
	 * Applica una spinta all’entità verso sinistra o destra dopo un colpo,
	 * controllando che la nuova posizione sia valida all’interno del livello.
	 * @param pushBackDir direzione della spinta (LEFT o RIGHT)
	 * @param lvlData dati della mappa per controllare le collisioni
	 * @param speedMulti fattore moltiplicativo della velocità
	 */
	protected void pushBack(int pushBackDir, int[][] lvlData, float speedMulti) {
		float xSpeed = 0;
		if (pushBackDir == LEFT)
			xSpeed = -walkSpeed;
		else
			xSpeed = walkSpeed;

		if (CanMoveHere(hitbox.x + xSpeed * speedMulti, hitbox.y, hitbox.width, hitbox.height, lvlData))
			hitbox.x += xSpeed * speedMulti;
	}

	/**
	 * Disegna il box d’attacco (attackBox) sullo schermo per debug.
	 * @param g oggetto Graphics usato per il rendering
	 * @param xLvlOffset offset orizzontale del livello (per scrolling)
	 */
	protected void drawAttackBox(Graphics g, int xLvlOffset) {
		g.setColor(Color.red);
		g.drawRect((int) (attackBox.x - xLvlOffset), (int) attackBox.y, (int) attackBox.width, (int) attackBox.height);
	}

	/**
	 * Disegna l’hitbox dell’entità (collision box) sullo schermo per debug.
	 * @param g oggetto Graphics usato per il rendering
	 * @param xLvlOffset offset orizzontale del livello (per scrolling)
	 */
	protected void drawHitbox(Graphics g, int xLvlOffset) {
		g.setColor(Color.PINK);
		g.drawRect((int) hitbox.x - xLvlOffset, (int) hitbox.y, (int) hitbox.width, (int) hitbox.height);
	}

	/**
	 * Inizializza l’hitbox dell’entità, scalata in base alla costante Game.SCALE.
	 * @param width larghezza della hitbox
	 * @param height altezza della hitbox
	 */
	protected void initHitbox(int width, int height) {
		hitbox = new Rectangle2D.Float(x, y, (int) (width * Game.SCALE), (int) (height * Game.SCALE));
	}

	/**
	 * Restituisce l’hitbox dell’entità.
	 * @return Rectangle2D.Float che rappresenta la hitbox
	 */
	public Rectangle2D.Float getHitbox() {
		return hitbox;
	}

	/**
	 * Restituisce lo stato corrente dell’entità.
	 * (Es. idle, attacco, camminata, salto, ecc.)
	 * @return intero che rappresenta lo stato
	 */
	public int getState() {
		return state;
	}

	/**
	 * Restituisce l’indice dell’animazione corrente.
	 * @return intero che rappresenta il frame corrente
	 */
	public int getAniIndex() {
		return aniIndex;
	}

	/**
	 * Imposta un nuovo stato per l’entità, resettando il contatore dell’animazione.
	 * @param state nuovo stato da impostare
	 */
	protected void newState(int state) {
		this.state = state;
		aniTick = 0;
		aniIndex = 0;
	}
}
