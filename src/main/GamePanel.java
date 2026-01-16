package main;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;
import inputs.KeyboardInputs;
import inputs.MouseInputs;
import static main.Game.GAME_HEIGHT;
import static main.Game.GAME_WIDTH;

public class GamePanel extends JPanel {

	private MouseInputs mouseInputs;
	private Game game;

	public GamePanel(Game game) {
		mouseInputs = new MouseInputs(this);
		this.game = game;
		setPanelSize(); //imposta la dim preferita 
		addKeyListener(new KeyboardInputs(this)); //collega il gestore della tastiera 
		// Collega il gestore degli eventi click/press/release del mouse
		addMouseListener(mouseInputs);
		// Collega il gestore degli eventi movimento/drag del mouse
		addMouseMotionListener(mouseInputs);
	}

	private void setPanelSize() {
		// Crea un oggetto Dimension usando le costanti globali
		Dimension size = new Dimension(GAME_WIDTH, GAME_HEIGHT);
		setPreferredSize(size);
	}

	public void updateGame() {

	}

	public void paintComponent(Graphics g) {
		// Chiama il metodo della superclasse per disegnare lo sfondo e gestire l'inizializzazione
		super.paintComponent(g);
		//il compito di disegno effettivo passa alla classe game 
		game.render(g);
	}

	public Game getGame() {
		return game;
	}

}