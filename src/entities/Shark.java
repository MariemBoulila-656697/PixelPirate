package entities;

import static utilz.Constants.Directions.LEFT;
import static utilz.Constants.EnemyConstants.*;
import static utilz.HelpMethods.CanMoveHere;
import static utilz.HelpMethods.IsFloor;

import gamestates.Playing;

public class Shark extends Enemy {

	/** Costruttore: inizializza un nemico di tipo Shark con hitbox e attack box. */
	public Shark(float x, float y) {
		super(x, y, SHARK_WIDTH, SHARK_HEIGHT, SHARK);
		initHitbox(18, 22);
		initAttackBox(20, 20, 20);
	}

	/** Aggiorna il comportamento, l’animazione e la posizione dell’attack box. */
	public void update(int[][] lvlData, Playing playing) {
		updateBehavior(lvlData, playing);
		updateAnimationTick();
		updateAttackBoxFlip();
	}

	/** Gestisce la logica del comportamento dello Shark in base allo stato attuale. */
	private void updateBehavior(int[][] lvlData, Playing playing) {
		if (firstUpdate)
			firstUpdateCheck(lvlData);

		if (inAir)
			inAirChecks(lvlData, playing);
		else {
			switch (state) {
			case IDLE:
				if (IsFloor(hitbox, lvlData))
					newState(RUNNING);
				else
					inAir = true;
				break;
			case RUNNING:
				if (canSeePlayer(lvlData, playing.getPlayer())) {
					turnTowardsPlayer(playing.getPlayer());
					if (isPlayerCloseForAttack(playing.getPlayer()))
						newState(ATTACK);
				}
				move(lvlData);
				break;
			case ATTACK:
				if (aniIndex == 0)
					attackChecked = false;
				else if (aniIndex == 3) {
					if (!attackChecked)
						checkPlayerHit(attackBox, playing.getPlayer());
					attackMove(lvlData, playing);
				}
				break;
	 		case HIT:
				if (aniIndex <= GetSpriteAmount(enemyType, state) - 2)
					pushBack(pushBackDir, lvlData, 2f);
				updatePushBackDrawOffset();
				break;
			}
		}
	}

	/** Esegue un movimento d’attacco veloce in avanti, moltiplicando la velocità. */
	protected void attackMove(int[][] lvlData, Playing playing) {
		float xSpeed = 0;

		if (walkDir == LEFT)
			xSpeed = -walkSpeed;
		else
			xSpeed = walkSpeed;

		if (CanMoveHere(hitbox.x + xSpeed * 4, hitbox.y, hitbox.width, hitbox.height, lvlData))
			if (IsFloor(hitbox, xSpeed * 4, lvlData)) {
				hitbox.x += xSpeed * 3; //modificata da 4 a 3 
				return;
			}
		newState(IDLE);
	}
}
