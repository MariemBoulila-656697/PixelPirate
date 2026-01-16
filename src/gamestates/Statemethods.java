package gamestates;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public interface Statemethods {
	//l'interfaccia stabilisce un contratto per tutti gli stati di gioco 
	//qualsiasi classe che implementa questa interfaccia è obbligata a fornire un'implementazione per tutti i metodi definiti al suo interno 
	public void update();

	public void draw(Graphics g);

	public void mouseClicked(MouseEvent e);

	public void mousePressed(MouseEvent e);

	public void mouseReleased(MouseEvent e);

	public void mouseMoved(MouseEvent e);

	public void keyPressed(KeyEvent e);

	public void keyReleased(KeyEvent e);

}
