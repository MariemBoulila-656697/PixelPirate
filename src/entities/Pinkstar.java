package entities;

import static utilz.Constants.EnemyConstants.*;

import static utilz.HelpMethods.CanMoveHere;
import static utilz.HelpMethods.IsFloor;
import static utilz.Constants.Directions.*;

import gamestates.Playing;

public class Pinkstar extends Enemy {

	private boolean preRoll = true;
	private int tickSinceLastDmgToPlayer;
	private int tickAfterRollInIdle;
	private int rollDurationTick, rollDuration = 300;

	/** Costruttore: inizializza un nemico di tipo Pinkstar con la sua hitbox. */
	public Pinkstar(float x, float y) {
		super(x, y, PINKSTAR_WIDTH, PINKSTAR_HEIGHT, PINKSTAR);
		initHitbox(17, 21);
	}

	/** Aggiorna il comportamento e l’animazione del nemico. */
	public void update(int[][] lvlData, Playing playing) {
		updateBehavior(lvlData, playing);
		updateAnimationTick();
	}

	/** Gestisce la logica del comportamento del Pinkstar in base allo stato attuale. */
	private void updateBehavior(int[][] lvlData, Playing playing) {
		if (firstUpdate)
			firstUpdateCheck(lvlData);

		if (inAir)
			inAirChecks(lvlData, playing);
		else {
			switch (state) {
			case IDLE:
				preRoll = true;
				if (tickAfterRollInIdle >= 120) {
					if (IsFloor(hitbox, lvlData))
						newState(RUNNING);
					else
						inAir = true;
					tickAfterRollInIdle = 0;
					tickSinceLastDmgToPlayer = 60;
				} else
					tickAfterRollInIdle++;
				break;
			case RUNNING:
				if (canSeePlayer(lvlData, playing.getPlayer())) {
					newState(ATTACK);
					setWalkDir(playing.getPlayer());
				}
				move(lvlData, playing);
				break;
			case ATTACK:
				if (preRoll) {
					if (aniIndex >= 3)
						preRoll = false;
				} else {
					move(lvlData, playing);
					checkDmgToPlayer(playing.getPlayer());
					checkRollOver(playing);
				}
				break;
			case HIT:
				if (aniIndex <= GetSpriteAmount(enemyType, state) - 2)
					pushBack(pushBackDir, lvlData, 2f);
				updatePushBackDrawOffset();
				tickAfterRollInIdle = 120;
				break;
			}
		}
	}

	/** Controlla se il Pinkstar danneggia il giocatore durante l’attacco a rotolata. */
	private void checkDmgToPlayer(Player player) {
		if (hitbox.intersects(player.getHitbox()))
			if (tickSinceLastDmgToPlayer >= 60) {
				tickSinceLastDmgToPlayer = 0;
				player.changeHealth(-GetEnemyDmg(enemyType), this);
			} else
				tickSinceLastDmgToPlayer++;
	}

	/** Imposta la direzione di movimento verso la posizione del giocatore. */
	private void setWalkDir(Player player) {
		if (player.getHitbox().x > hitbox.x)
			walkDir = RIGHT;
		else
			walkDir = LEFT;
	}

	/** Gestisce il movimento del Pinkstar (con velocità doppia durante l’attacco). */
	protected void move(int[][] lvlData, Playing playing) {
		float xSpeed = 0;

		if (walkDir == LEFT)
			xSpeed = -walkSpeed;
		else
			xSpeed = walkSpeed;

		if (state == ATTACK)
			xSpeed *= 2;

		if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData))
			if (IsFloor(hitbox, xSpeed, lvlData)) {
				hitbox.x += xSpeed;
				return;
			}

		if (state == ATTACK) {
			rollOver(playing);
			rollDurationTick = 0;
		}

		changeWalkDir();
	}

	/** Controlla la durata della rotolata e la termina se supera il limite. */
	private void checkRollOver(Playing playing) {
		rollDurationTick++;
		if (rollDurationTick >= rollDuration) {
			rollOver(playing);
			rollDurationTick = 0;
		}
	}

	/** Termina la rotolata e riporta il Pinkstar allo stato IDLE. */
	private void rollOver(Playing playing) {
		newState(IDLE);
	}
}
