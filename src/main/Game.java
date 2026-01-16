package main;

import java.awt.Graphics;  // usato per disegnare sulla finestra 

import audio.AudioPlayer; //classe per gestire l'audio
//import gamestates.Credits; //
import gamestates.GameOptions;
import gamestates.Gamestate; //enumerazione per tracciare lo stato attuale 
import gamestates.Menu;
import gamestates.Playing;
import ui.AudioOptions; //interfaccia utente per le opzioni audio 

public class Game implements Runnable {

	private GamePanel gamePanel; //il pannello 
	private Thread gameThread; //il thread dedicato al game loop 
	//costanti per definire la ferequenza di aggiornamento e disegno 
	private final int FPS_SET = 120;
	private final int UPS_SET = 200;

	private Playing playing;
	private Menu menu;
	//private Credits credits; //
	private GameOptions gameOptions;
	private AudioOptions audioOptions;
	private AudioPlayer audioPlayer;

	//costanti di dimensione 
	public final static int TILES_DEFAULT_SIZE = 32;
	public final static float SCALE = 1.5f;
	public final static int TILES_IN_WIDTH = 26;
	public final static int TILES_IN_HEIGHT = 14;
	public final static int TILES_SIZE = (int) (TILES_DEFAULT_SIZE * SCALE);
	public final static int GAME_WIDTH = TILES_SIZE * TILES_IN_WIDTH;
	public final static int GAME_HEIGHT = TILES_SIZE * TILES_IN_HEIGHT;

	private final boolean SHOW_FPS_UPS = true;

	
	public Game() {
		//stampa la risoluzione 
		System.out.println("size: " + GAME_WIDTH + " : " + GAME_HEIGHT);
		initClasses(); //inizializza tutti gli stati e l'audio 
		
		gamePanel = new GamePanel(this); //crea pannello 
		new GameWindow(gamePanel);  // crea finestra 
		
		
		gamePanel.requestFocusInWindow(); // assicura la ricezione degli input 
		startGameLoop(); //avvia il game loop s un nuovo thread
	}
	private void initClasses() {
		
		audioOptions = new AudioOptions(this);
		audioPlayer = new AudioPlayer();
		menu = new Menu(this);
		playing = new Playing(this);
		gameOptions = new GameOptions(this);
	}

	private void startGameLoop() {
		gameThread = new Thread(this); //crea threas
		gameThread.start(); //avvia l'esecuzione del run  
	}

	public void update() { //aggiorna lo stato di gioco 
		switch (Gamestate.state) {
		case MENU -> menu.update();
		case PLAYING -> playing.update();
		case OPTIONS -> gameOptions.update();
		case QUIT -> System.exit(0);
		}
	}

	@SuppressWarnings("incomplete-switch") 
	public void render(Graphics g) { //disegna lo stato di gioco correntemente attivo 
		switch (Gamestate.state) {
		case MENU -> menu.draw(g);
		case PLAYING -> playing.draw(g);
		case OPTIONS -> gameOptions.draw(g);
		}
	}

	@Override
	public void run() {
		// Calcolo di quanto tempo deve passare per un frame e un update
		double timePerFrame = 1000000000.0 / FPS_SET;
		double timePerUpdate = 1000000000.0 / UPS_SET;

		long previousTime = System.nanoTime();

		int frames = 0; // Contatore per gli FPS
		int updates = 0; // Contatore per gli UPS
		long lastCheck = System.currentTimeMillis(); // Per controllare quando è passato 1 secondo

		double deltaU = 0; // Delta per gli Update (logica)
		double deltaF = 0;  // Delta per i Frame (render)

		while (true) {
			// Il ciclo principale, continua finché il gioco è aperto

			long currentTime = System.nanoTime(); //tempo attuale 
			// Aggiunge la frazione del tempo passato rispetto al tempo richiesto per un Update/Frame
			deltaU += (currentTime - previousTime) / timePerUpdate;
			deltaF += (currentTime - previousTime) / timePerFrame;
			previousTime = currentTime;
			
			//UPDATE LOGICA DEL GIOCO 
			if (deltaU >= 1) {  // Se è passato abbastanza tempo per un update (deltaU >= 1)

				update(); //esegue la logica del gioco 
				updates++;
				deltaU--; 

			}
			
			//RENDER 
			if (deltaF >= 1) {

				gamePanel.repaint(); //CHIEDE AL GAME PANEL di disegnare 
				frames++;
				deltaF--;

			}

			if (SHOW_FPS_UPS)
				if (System.currentTimeMillis() - lastCheck >= 1000) {
					lastCheck = System.currentTimeMillis();
					System.out.println("FPS: " + frames + " | UPS: " + updates);
					frames = 0;
					updates = 0;

				}

		}
	}

	public void windowFocusLost() {
		if (Gamestate.state == Gamestate.PLAYING)
			playing.getPlayer().resetDirBooleans(); //resetta i booleani per evitare che continui a muoversi 
	}

	public Menu getMenu() {
		return menu;
	}

	public Playing getPlaying() {
		return playing;
	}


	public GameOptions getGameOptions() {
		return gameOptions;
	}

	public AudioOptions getAudioOptions() {
		return audioOptions;
	}

	public AudioPlayer getAudioPlayer() {
		return audioPlayer;
	}
}