package gamestates;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;


import entities.EnemyManager;
import entities.Player;
import levels.LevelManager;
import main.Game;
import objects.ObjectManager;
import ui.GameCompletedOverlay;
import ui.GameOverOverlay;
import ui.LevelCompletedOverlay;
import ui.PauseOverlay;
import utilz.LoadSave;


import static utilz.Constants.Environment.*;


public class Playing extends State implements Statemethods {


	// Gestori dei componenti principali del gioco
	private Player player;
	private LevelManager levelManager;
	private EnemyManager enemyManager;
	private ObjectManager objectManager;
	// Overlay (interfacce sopra il gioco) per diversi stati
	private PauseOverlay pauseOverlay;
	private GameOverOverlay gameOverOverlay;
	private GameCompletedOverlay gameCompletedOverlay;
	private LevelCompletedOverlay levelCompletedOverlay;
	
	// Flag di stato del gioco
	private boolean paused = false;
	private boolean gameOver;
	private boolean lvlCompleted;
	private boolean gameCompleted;
	private boolean playerDying;
	
	// Variabili per il controllo della telecamera (Scrolling)
	private int xLvlOffset; // L'offset in pixel del livello (spostamento orizzontale della telecamera)
	private int leftBorder = (int) (0.25 * Game.GAME_WIDTH); // Limite sinistro per lo scrolling (25% della larghezza)
	private int rightBorder = (int) (0.75 * Game.GAME_WIDTH); // Limite destro per lo scrolling (75% della larghezza)
	private int maxLvlOffsetX; // Limite massimo di spostamento (determina la fine del livello)

	// Immagini e Posizioni per lo Sfondo e gli Elementi Ambientali
	private BufferedImage backgroundImg, bigCloud, smallCloud, shipImgs[];
	private int[] smallCloudsPos;
	private Random rnd = new Random();
	
	//per fare il checkpoint 
	private int respawnX, respawnY;
	private boolean checkpointActive = false; //far in modo che sia per ogni livello separato 





	// Ship will be decided to drawn here. It's just a cool addition to the game
	// for the first level. Hinting on that the player arrived with the boat.

	// If you would like to have it on more levels, add a value for objects when
	// creating the level from lvlImgs. Just like any other object.

	// Then play around with position values so it looks correct depending on where
	// you want
	// it.

	
	// Animazione della Nave (dettaglio specifico per il primo livello)
	private boolean drawShip = true;
	private int shipAni, shipTick, shipDir = 1;
	private float shipHeightDelta, shipHeightChange = 0.05f * Game.SCALE;

	//Il costruttore carica tutte le risorse grafiche, inizializza i gestori dei componenti e imposta il livello iniziale.
	public Playing(Game game) {
		super(game);
		initClasses(); // Inizializza Player, LevelManager, EnemyManager, UI Overlays

		backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.PLAYING_BG_IMG);
		bigCloud = LoadSave.GetSpriteAtlas(LoadSave.BIG_CLOUDS);
		smallCloud = LoadSave.GetSpriteAtlas(LoadSave.SMALL_CLOUDS);
		smallCloudsPos = new int[8];
		// caricamento e posizionamento delle nuvole (logica di parallasse)
		for (int i = 0; i < smallCloudsPos.length; i++)
			smallCloudsPos[i] = (int) (90 * Game.SCALE) + rnd.nextInt((int) (100 * Game.SCALE));

		shipImgs = new BufferedImage[4];
		BufferedImage temp = LoadSave.GetSpriteAtlas(LoadSave.SHIP);
		// caricamento dei frame della nave
		for (int i = 0; i < shipImgs.length; i++)
			shipImgs[i] = temp.getSubimage(i * 78, 0, 78, 72);


		calcLvlOffset(); // Calcola il limite massimo di scrolling per il livello corrente

		loadStartLevel(); // Carica nemici e oggetti nel livello

	}

	public void loadNextLevel() {
		//Incrementa l'indice del livello, carica i dati del livello successivo e resetta lo stato.
		levelManager.setLevelIndex(levelManager.getLevelIndex() + 1);
		levelManager.loadNextLevel();
		
		checkpointActive = false;
		
		// 🔹 reset checkpoint per il nuovo livello
	    checkpointActive = false;
	    Point spawn = levelManager.getCurrentLevel().getPlayerSpawn();
	    respawnX = spawn.x;
	    respawnY = spawn.y;
		
		
		
		player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
		resetAll();
		drawShip = false;
	}

	private void loadStartLevel() {
		enemyManager.loadEnemies(levelManager.getCurrentLevel());
		objectManager.loadObjects(levelManager.getCurrentLevel());
	}

	private void calcLvlOffset() {
		maxLvlOffsetX = levelManager.getCurrentLevel().getLvlOffset();
	}

	private void initClasses() {
		// Crea tutti i gestori e il giocatore, passandogli riferimenti necessari
		levelManager = new LevelManager(game);
		enemyManager = new EnemyManager(this);
		objectManager = new ObjectManager(this);

		player = new Player(200, 200, (int) (64 * Game.SCALE), (int) (40 * Game.SCALE), this);
		player.loadLvlData(levelManager.getCurrentLevel().getLevelData());
		
		// 🔹 inizializza il punto di respawn del livello corrente
		Point spawn = levelManager.getCurrentLevel().getPlayerSpawn();
		respawnX = spawn.x;
		respawnY = spawn.y;

		player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn()); //pos iniziale 

		
		// Inizializza tutte le interfacce utente (Overlay)
		pauseOverlay = new PauseOverlay(this);
		gameOverOverlay = new GameOverOverlay(this);
		levelCompletedOverlay = new LevelCompletedOverlay(this);
		gameCompletedOverlay = new GameCompletedOverlay(this);

	}

	@Override //LOGICA DEL GIOCO 
	public void update() {
		//Gestione degli Overlay: Se un overlay è attivo, aggiorna SOLO quello
		if (paused)
			pauseOverlay.update();
		else if (lvlCompleted)
			levelCompletedOverlay.update();
		else if (gameCompleted)
			gameCompletedOverlay.update();
		else if (gameOver)
			gameOverOverlay.update();
		else if (playerDying)
			player.update();
		else {
			
			//Logica di Gioco Normale (se nessun overlay è attivo)
			levelManager.update();
			objectManager.update(levelManager.getCurrentLevel().getLevelData(), player);
			player.update(); //// Aggiorna posizione, animazione, attacchi del giocatore
			enemyManager.update(levelManager.getCurrentLevel().getLevelData());
			checkCloseToBorder(); //// Controlla e aggiorna lo scrolling della telecamera
			if (drawShip)
				updateShipAni();
		}
	}

	private void updateShipAni() {
		shipTick++;
		if (shipTick >= 35) {
			shipTick = 0;
			shipAni++;
			if (shipAni >= 4)
				shipAni = 0;
		}

		shipHeightDelta += shipHeightChange * shipDir;
		shipHeightDelta = Math.max(Math.min(10 * Game.SCALE, shipHeightDelta), 0);

		if (shipHeightDelta == 0)
			shipDir = 1;
		else if (shipHeightDelta == 10 * Game.SCALE)
			shipDir = -1;

	}

	




	private void checkCloseToBorder() {
		int playerX = (int) player.getHitbox().x;
		int diff = playerX - xLvlOffset; // Posizione del giocatore relativa alla telecamera
		// Se il giocatore supera il limite destro (75%), sposta la telecamera a destra
		if (diff > rightBorder)
			xLvlOffset += diff - rightBorder;
		// Se il giocatore supera il limite sinistro (25%), sposta la telecamera a sinistra
		else if (diff < leftBorder)
			xLvlOffset += diff - leftBorder;
		//non superare i confini del livello
		xLvlOffset = Math.max(Math.min(xLvlOffset, maxLvlOffsetX), 0);
	}

	@Override
	public void draw(Graphics g) {
		g.drawImage(backgroundImg, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null); //sfondo fisso 

		drawClouds(g); //nuvole con effetto parallasse 

		// Disegna la nave con l'offset del livello e l'animazione verticale
		if (drawShip)
			g.drawImage(shipImgs[shipAni], (int) (100 * Game.SCALE) - xLvlOffset, (int) ((288 * Game.SCALE) + shipHeightDelta), (int) (78 * Game.SCALE), (int) (72 * Game.SCALE), null);

		levelManager.draw(g, xLvlOffset);
		objectManager.draw(g, xLvlOffset);
		enemyManager.draw(g, xLvlOffset);
		player.render(g, xLvlOffset);
		objectManager.drawBackgroundTrees(g, xLvlOffset); //Disegna alberi di sfondo (parallasse leggera)

		// Disegno degli Overlay (vengono disegnati sopra tutto il resto)
		if (paused) {
			// Aggiunge un velo scuro per enfatizzare la pausa
			g.setColor(new Color(0, 0, 0, 150));
			g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
			pauseOverlay.draw(g);
		} else if (gameOver)
			gameOverOverlay.draw(g);
		else if (lvlCompleted)
			levelCompletedOverlay.draw(g);
		else if (gameCompleted)
			gameCompletedOverlay.draw(g);

	}

	private void drawClouds(Graphics g) {
		for (int i = 0; i < 4; i++)
			g.drawImage(bigCloud, i * BIG_CLOUD_WIDTH - (int) (xLvlOffset * 0.3), (int) (204 * Game.SCALE), BIG_CLOUD_WIDTH, BIG_CLOUD_HEIGHT, null);

		for (int i = 0; i < smallCloudsPos.length; i++)
			g.drawImage(smallCloud, SMALL_CLOUD_WIDTH * 4 * i - (int) (xLvlOffset * 0.7), smallCloudsPos[i], SMALL_CLOUD_WIDTH, SMALL_CLOUD_HEIGHT, null);
	}

	public void setGameCompleted() {
		gameCompleted = true;
	}

	public void resetGameCompleted() {
		gameCompleted = false;
	}

	public void setCheckpoint(int x, int y) {
	    respawnX = x;
	    respawnY = y - Game.TILES_SIZE;
	    checkpointActive = true;
	    
	}

	
	public void respawnPlayerAtCheckpoint() {
	    if (checkpointActive) {
	        player.setSpawn(new Point(respawnX, respawnY));
	    } else {
	        player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
	    }
	    player.respawn();
	}
	
	public void resetLevelFromMenu() {

	    gameOver = false;
	    paused = false;
	    lvlCompleted = false;
	    playerDying = false;

	    enemyManager.resetAllEnemies();
	    objectManager.resetAllObjects();

	    // Spawn originale del livello
	    Point spawn = levelManager.getCurrentLevel().getPlayerSpawn();
	    respawnX = spawn.x;
	    respawnY = spawn.y;

	    player.setSpawn(spawn);
	    player.respawn();

	    // 🔥 checkpoint cancellato
	    checkpointActive = false;
	}

	public void resetAll() { 
		//Riporta il gioco allo stato iniziale: resetta i flag, il giocatore, i nemici e gli oggetti.
		gameOver = false;
		paused = false;
		lvlCompleted = false;
		playerDying = false;
		player.resetAll();
		enemyManager.resetAllEnemies();
		objectManager.resetAllObjects();
		
		Point spawn = levelManager.getCurrentLevel().getPlayerSpawn();
	    player.setSpawn(spawn);
	    player.respawn();
	 // 👉 QUI NON SI TOCCA IL CHECKPOINT
	    // resetAll viene usato per:
	    // - fine livello
	    // - passaggio livello
	    // - restart globale interno
		
		 /*if (checkpointActive) {
		        // Usa il checkpoint corrente
		        player.setSpawn(new Point(respawnX, respawnY));
		    } else {
		        // Usa lo spawn iniziale del livello
		        Point spawn = levelManager.getCurrentLevel().getPlayerSpawn();
		        respawnX = spawn.x;
		        respawnY = spawn.y;
		        player.setSpawn(spawn);
		    }*/
		 
		 // 🔥 RESET della posizione del player ALLO SPAWN ORIGINALE
	    /*Point spawn = levelManager.getCurrentLevel().getPlayerSpawn();
	    player.setSpawn(spawn);
	    player.respawn();

	    // 🔥 RESET DEI CHECKPOINT 
	    checkpointActive = false;*/
		
	}




	public void setGameOver(boolean gameOver) {
		this.gameOver = gameOver;
	}

	public void checkObjectHit(Rectangle2D.Float attackBox) {
		objectManager.checkObjectHit(attackBox);
	}

	public void checkEnemyHit(Rectangle2D.Float attackBox) {
		enemyManager.checkEnemyHit(attackBox);
	}

	public void checkPotionTouched(Rectangle2D.Float hitbox) {
		objectManager.checkObjectTouched(hitbox);
	}

	public void checkSpikesTouched(Player p) {
		objectManager.checkSpikesTouched(p);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (!gameOver) {
			if (e.getButton() == MouseEvent.BUTTON1)
				player.setAttacking(true);
			else if (e.getButton() == MouseEvent.BUTTON3)
				player.powerAttack();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (!gameOver && !gameCompleted && !lvlCompleted)
			switch (e.getKeyCode()) {
			case KeyEvent.VK_A:
				player.setLeft(true);
				break;
			case KeyEvent.VK_D:
				player.setRight(true);
				break;
			case KeyEvent.VK_SPACE:
				player.setJump(true);
				break;
			case KeyEvent.VK_ESCAPE:
				paused = !paused;
			}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (!gameOver && !gameCompleted && !lvlCompleted)
			switch (e.getKeyCode()) {
			case KeyEvent.VK_A:
				player.setLeft(false);
				break;
			case KeyEvent.VK_D:
				player.setRight(false);
				break;
			case KeyEvent.VK_SPACE:
				player.setJump(false);
				break;
			}
	}

	public void mouseDragged(MouseEvent e) {
		if (!gameOver && !gameCompleted && !lvlCompleted)
			if (paused)
				pauseOverlay.mouseDragged(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (gameOver)
			gameOverOverlay.mousePressed(e);
		else if (paused)
			pauseOverlay.mousePressed(e);
		else if (lvlCompleted)
			levelCompletedOverlay.mousePressed(e);
		else if (gameCompleted)
			gameCompletedOverlay.mousePressed(e);

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (gameOver)
			gameOverOverlay.mouseReleased(e);
		else if (paused)
			pauseOverlay.mouseReleased(e);
		else if (lvlCompleted)
			levelCompletedOverlay.mouseReleased(e);
		else if (gameCompleted)
			gameCompletedOverlay.mouseReleased(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (gameOver)
			gameOverOverlay.mouseMoved(e);
		else if (paused)
			pauseOverlay.mouseMoved(e);
		else if (lvlCompleted)
			levelCompletedOverlay.mouseMoved(e);
		else if (gameCompleted)
			gameCompletedOverlay.mouseMoved(e);
	}

	public void setLevelCompleted(boolean levelCompleted) {
		game.getAudioPlayer().lvlCompleted();
		
		checkpointActive = false;
		if (levelManager.getLevelIndex() + 1 >= levelManager.getAmountOfLevels()) {
			// No more levels
			gameCompleted = true;
			levelManager.setLevelIndex(0);
			levelManager.loadNextLevel();
			
			resetAll();
			return;
		}
		this.lvlCompleted = levelCompleted;
	}

	public void setMaxLvlOffset(int lvlOffset) {
		this.maxLvlOffsetX = lvlOffset;
	}

	public void unpauseGame() {
		paused = false;
	}

	public void windowFocusLost() {
		player.resetDirBooleans();
	}

	public Player getPlayer() {
		return player;
	}

	public EnemyManager getEnemyManager() {
		return enemyManager;
	}

	public ObjectManager getObjectManager() {
		return objectManager;
	}

	public LevelManager getLevelManager() {
		return levelManager;
	}

	public void setPlayerDying(boolean playerDying) {
		this.playerDying = playerDying;
	}
}