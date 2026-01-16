package main;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JFrame;

//incapsulare il game panel in un jframe e configurarne il comportament 
public class GameWindow {
	private JFrame jframe; //oggetto finestra 

	public GameWindow(GamePanel gamePanel) {

		jframe = new JFrame();

		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//aggiunge il pannello di gioco
		jframe.add(gamePanel);
		
		jframe.setResizable(false);
		jframe.pack(); // Dimensiona la finestra per adattarsi alla dimensione preferita del GamePanel
		jframe.setLocationRelativeTo(null); // Centra la finestra sullo schermo.
		jframe.setVisible(true); // Rende la finestra visibile.
		
		jframe.addWindowFocusListener(new WindowFocusListener() { // Metodo chiamato quando la finestra perde il focus (diventa inattiva).
			@Override
			public void windowLostFocus(WindowEvent e) {
				// Chiama il metodo windowFocusLost() della classe Game. 
				// Questo serve tipicamente per fermare i movimenti del giocatore o mettere in pausa il gioco 
				// quando la finestra non è più l'applicazione attiva.
				gamePanel.getGame().windowFocusLost();
			}

			@Override
			public void windowGainedFocus(WindowEvent e) { // Metodo chiamato quando la finestra guadagna il focus (diventa attiva)
				// TODO Auto-generated method stub

			}
		});

	}

}
