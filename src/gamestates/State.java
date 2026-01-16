package gamestates;

import java.awt.event.MouseEvent;

import audio.AudioPlayer;
import main.Game;
import ui.MenuButton;

public class State {

	protected Game game; //protected per esseere accesibile alle sotto classi 

	public State(Game game) {
		this.game = game; //inizializza il riferimento al gioco principale 
	}

	public boolean isIn(MouseEvent e, MenuButton mb) {
		// Controlla se le coordinate del mouse (e.getX(), e.getY()) si trovano all'interno 
		// dell'area (bounds) del MenuButton 'mb'.
		// utilità cruciale per l'interazione con l'interfaccia utente
		return mb.getBounds().contains(e.getX(), e.getY());
	}

	public Game getGame() {
		return game;
	}

	@SuppressWarnings("incomplete-switch")
	public void setGamestate(Gamestate state) {
		
		switch (state) {
		// Quando si passa al MENU, imposta la canzone del menu.
		case MENU -> game.getAudioPlayer().playSong(AudioPlayer.MENU_1);
		// Quando si passa a PLAYING, imposta la canzone basata sul livello corrente.
		case PLAYING -> game.getAudioPlayer().setLevelSong(game.getPlaying().getLevelManager().getLevelIndex());
		}
		// Infine, imposta lo stato statico globale con il nuovo stato.

		Gamestate.state = state;
	}

}