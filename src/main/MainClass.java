package main;
//Definisce la classe principale
public class MainClass {

	public static void main(String[] args) {
		// L'unica istruzione eseguita: crea una nuova istanza della classe Game.
		// Questo avvia l'intera applicazione di gioco.
		// l'oggetto 'Game' viene creato ma non gli viene assegnato un riferimento, 
		// perché la sua logica (inclusi il Game Loop e la creazione della finestra) 
		// è interamente gestita all'interno del suo costruttore.
		new Game();
	}

}
