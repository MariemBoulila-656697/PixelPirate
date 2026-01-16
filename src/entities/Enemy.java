package entities;

import static utilz.Constants.EnemyConstants.*;
import static utilz.HelpMethods.*;

import java.awt.geom.Rectangle2D;

import gamestates.Playing;

import static utilz.Constants.Directions.*;
import static utilz.Constants.*;

import main.Game;

public abstract class Enemy extends Entity {

	protected int enemyType;
	protected boolean firstUpdate = true;
	protected int walkDir = LEFT;
	protected int tileY;
	protected float attackDistance = Game.TILES_SIZE;
	protected boolean active = true;
	protected boolean attackChecked;
	protected int attackBoxOffsetX;

	public Enemy(float x, float y, int width, int height, int enemyType) {
		super(x, y, width, height);
		this.enemyType = enemyType;

		maxHealth = GetMaxHealth(enemyType);
		currentHealth = maxHealth;
		walkSpeed = Game.SCALE * 0.20f;
	}

	/** Aggiorna la posizione della attackBox senza considerare il flip. */
	protected void updateAttackBox() {
		attackBox.x = hitbox.x - attackBoxOffsetX;
		attackBox.y = hitbox.y;
	}

	/** Aggiorna la posizione della attackBox considerando la direzione del nemico (flip). */
	protected void updateAttackBoxFlip() {
		if (walkDir == RIGHT)
			attackBox.x = hitbox.x + hitbox.width;
		else
			attackBox.x = hitbox.x - attackBoxOffsetX;

		attackBox.y = hitbox.y;
	}

	/** Inizializza la attackBox con dimensioni e offset. */
	protected void initAttackBox(int w, int h, int attackBoxOffsetX) {
		attackBox = new Rectangle2D.Float(x, y, (int) (w * Game.SCALE), (int) (h * Game.SCALE));
		this.attackBoxOffsetX = (int) (Game.SCALE * attackBoxOffsetX);
	}

	/** Primo aggiornamento: controlla se il nemico si trova in aria. */
	protected void firstUpdateCheck(int[][] lvlData) {
		if (!IsEntityOnFloor(hitbox, lvlData))
			inAir = true;
		firstUpdate = false;
	}

	/** Gestisce i controlli quando il nemico è in aria (gravità, acqua, spine). */
	protected void inAirChecks(int[][] lvlData, Playing playing) {
		if (state != HIT && state != DEAD) {
			updateInAir(lvlData);
			playing.getObjectManager().checkSpikesTouched(this);
			if (IsEntityInWater(hitbox, lvlData))
				hurt(maxHealth);
		}
	}

	/** Applica la gravità al nemico quando è in aria e controlla collisioni. */
	protected void updateInAir(int[][] lvlData) {
		if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
			hitbox.y += airSpeed;
			airSpeed += GRAVITY;
		} else {
			inAir = false;
			hitbox.y = GetEntityYPosUnderRoofOrAboveFloor(hitbox, airSpeed);
			tileY = (int) (hitbox.y / Game.TILES_SIZE);
		}
	}

	/** Gestisce il movimento orizzontale del nemico e cambia direzione se trova un ostacolo. */
	protected void move(int[][] lvlData) {
		float xSpeed = 0;

		if (walkDir == LEFT)
			xSpeed = -walkSpeed;
		else
			xSpeed = walkSpeed;

		if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData))
			if (IsFloor(hitbox, xSpeed, lvlData)) {
				hitbox.x += xSpeed;
				return;
			}

		changeWalkDir(); // metodo di sotto 
	}

	/** Fa girare il nemico verso il giocatore. */
	protected void turnTowardsPlayer(Player player) {
		if (player.hitbox.x > hitbox.x)
			walkDir = RIGHT;
		else
			walkDir = LEFT;
	}

	/** Controlla se il nemico può vedere il giocatore (stessa altezza, distanza e linea di vista libera). */
	protected boolean canSeePlayer(int[][] lvlData, Player player) {
		int playerTileY = (int) (player.getHitbox().y / Game.TILES_SIZE);
		if (playerTileY == tileY)
			if (isPlayerInRange(player)) {
				if (IsSightClear(lvlData, hitbox, player.hitbox, tileY))
					return true;
			}
		return false;
	}

	/** Controlla se il giocatore è entro il raggio di rilevamento del nemico. */
	protected boolean isPlayerInRange(Player player) {
		int absValue = (int) Math.abs(player.hitbox.x - hitbox.x);
		return absValue <= attackDistance * 5;
	}

	/** Controlla se il giocatore è abbastanza vicino per un attacco, in base al tipo di nemico. */
	protected boolean isPlayerCloseForAttack(Player player) {
		int absValue = (int) Math.abs(player.hitbox.x - hitbox.x);
		switch (enemyType) {
		case CRABBY -> {
			return absValue <= attackDistance;
		}
		case SHARK -> {
			return absValue <= attackDistance * 2;
		}
		}
		return false;
	}

	/** Infligge danno al nemico e aggiorna lo stato (HIT o DEAD). */
	public void hurt(int amount) {
		currentHealth -= amount;
		if (currentHealth <= 0)
			newState(DEAD);
		else {
			newState(HIT);
			if (walkDir == LEFT)
				pushBackDir = RIGHT;
			else
				pushBackDir = LEFT;
			pushBackOffsetDir = UP;
			pushDrawOffset = 0;
		}
	}

	/** Controlla se l’attacco del nemico colpisce il giocatore. */
	protected void checkPlayerHit(Rectangle2D.Float attackBox, Player player) {
		if (attackBox.intersects(player.hitbox))
			player.changeHealth(-GetEnemyDmg(enemyType), this);
		else {
			if (enemyType == SHARK)
				return;
		}
		attackChecked = true;
	}

	/** Aggiorna il tick dell’animazione e gestisce la fine delle animazioni di attacco, hit o morte. */
	protected void updateAnimationTick() {
		aniTick++;
		if (aniTick >= ANI_SPEED) {
			aniTick = 0;
			aniIndex++;
			if (aniIndex >= GetSpriteAmount(enemyType, state)) {
				if (enemyType == CRABBY || enemyType == SHARK) {
					aniIndex = 0;

					switch (state) {
					case ATTACK, HIT -> state = IDLE;
					case DEAD -> active = false;
					}
				} else if (enemyType == PINKSTAR) {
					if (state == ATTACK)
						aniIndex = 3;
					else {
						aniIndex = 0;
						if (state == HIT) {
							state = IDLE;

						} else if (state == DEAD)
							active = false;
					}
				}
			}
		}
	}

	/** Cambia la direzione di camminata del nemico. */
	protected void changeWalkDir() {
		if (walkDir == LEFT)
			walkDir = RIGHT;
		else
			walkDir = LEFT;
	}

	/** Resetta il nemico al suo stato iniziale. */
	public void resetEnemy() {
		hitbox.x = x;
		hitbox.y = y;
		firstUpdate = true;
		currentHealth = maxHealth;
		newState(IDLE);
		active = true;
		airSpeed = 0;
		pushDrawOffset = 0;
	}

	/** Restituisce l’offset orizzontale per il flip dello sprite. */
	public int flipX() {
		if (walkDir == RIGHT)
			return width;
		else
			return 0;
	}

	/** Restituisce il moltiplicatore di scala orizzontale per il flip dello sprite. */
	public int flipW() {
		if (walkDir == RIGHT)
			return -1;
		else
			return 1;
	}

	/** Indica se il nemico è attivo. */
	public boolean isActive() {
		return active;
	}

	/** Restituisce l’offset verticale dovuto al knockback. */
	public float getPushDrawOffset() {
		return pushDrawOffset;
	}
}
