package fr.zeygal.zpotato.arena;

public enum ArenaState {
    WAITING,    // En attente de joueurs
    STARTING,   // Compte à rebours avant le début
    RUNNING,    // Partie en cours
    ENDING      // Fin de partie (annonce du gagnant, etc.)
}