package br.com.primeleague.x1.enums;

/**
 * Estados possíveis para a scoreboard do sistema de X1
 */
public enum X1HUDState {
    /**
     * Estado antes do duelo (contagem regressiva)
     */
    PRE_DUEL,
    
    /**
     * Estado durante o duelo
     */
    IN_DUEL,
    
    /**
     * Estado após o duelo (resultado por 3s)
     */
    POST_DUEL,
    
    /**
     * Estado de estatísticas finais detalhadas (vencedor, dano, etc)
     */
    FINAL_STATS,
    
    /**
     * Sem scoreboard ativa
     */
    NONE
} 