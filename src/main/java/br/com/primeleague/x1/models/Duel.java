package br.com.primeleague.x1.models;

import java.util.UUID;

import br.com.primeleague.x1.enums.DuelState;
import br.com.primeleague.x1.enums.DuelType;

/**
 * Classe que representa um duelo
 */
public class Duel {

    private final UUID id;
    private final String player1;
    private final String player2;
    private final DuelType type;
    private DuelState state;
    private String winner;
    private boolean draw;
    private double betAmount;
    private long startTime;
    private long endTime;
    private boolean inCountdown; // Flag para indicar que o duelo está em contagem regressiva
    
    /**
     * Construtor
     * 
     * @param id ID único do duelo
     * @param player1 Nome do jogador 1
     * @param player2 Nome do jogador 2
     * @param type Tipo de duelo
     */
    public Duel(UUID id, String player1, String player2, DuelType type) {
        this.id = id;
        this.player1 = player1;
        this.player2 = player2;
        this.type = type;
        this.state = DuelState.STARTING;
        this.draw = false;
        this.betAmount = 0;
        this.startTime = System.currentTimeMillis();
        this.inCountdown = false;
    }
    
    /**
     * Obtém o ID do duelo
     * 
     * @return ID do duelo
     */
    public UUID getId() {
        return id;
    }
    
    /**
     * Obtém o nome do jogador 1
     * 
     * @return Nome do jogador 1
     */
    public String getPlayer1() {
        return player1;
    }
    
    /**
     * Obtém o nome do jogador 2
     * 
     * @return Nome do jogador 2
     */
    public String getPlayer2() {
        return player2;
    }
    
    /**
     * Obtém o tipo de duelo
     * 
     * @return Tipo de duelo
     */
    public DuelType getType() {
        return type;
    }
    
    /**
     * Obtém o estado atual do duelo
     * 
     * @return Estado do duelo
     */
    public DuelState getState() {
        return state;
    }
    
    /**
     * Define o estado do duelo
     * 
     * @param state Novo estado
     */
    public void setState(DuelState state) {
        this.state = state;
    }
    
    /**
     * Obtém o nome do vencedor
     * 
     * @return Nome do vencedor ou null se não houver
     */
    public String getWinner() {
        return winner;
    }
    
    /**
     * Define o vencedor do duelo
     * 
     * @param winner Nome do vencedor
     */
    public void setWinner(String winner) {
        this.winner = winner;
    }
    
    /**
     * Verifica se o duelo terminou em empate
     * 
     * @return true se o duelo terminou em empate, false caso contrário
     */
    public boolean isDraw() {
        return draw;
    }
    
    /**
     * Define se o duelo terminou em empate
     * 
     * @param draw true se empate, false caso contrário
     */
    public void setDraw(boolean draw) {
        this.draw = draw;
    }
    
    /**
     * Obtém o valor da aposta
     * 
     * @return Valor da aposta (0 se não houver)
     */
    public double getBetAmount() {
        return betAmount;
    }
    
    /**
     * Define o valor da aposta
     * 
     * @param betAmount Valor da aposta
     */
    public void setBetAmount(double betAmount) {
        this.betAmount = betAmount;
    }
    
    /**
     * Obtém o momento de início do duelo
     * 
     * @return Timestamp de início
     */
    public long getStartTime() {
        return startTime;
    }
    
    /**
     * Define o momento de início do duelo
     * 
     * @param startTime Timestamp de início
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    /**
     * Obtém o momento de término do duelo
     * 
     * @return Timestamp de término
     */
    public long getEndTime() {
        return endTime;
    }
    
    /**
     * Define o momento de término do duelo
     * 
     * @param endTime Timestamp de término
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    
    /**
     * Verifica se o duelo está na fase de contagem regressiva
     * 
     * @return true se estiver na contagem regressiva, false caso contrário
     */
    public boolean isInCountdown() {
        return inCountdown;
    }
    
    /**
     * Define se o duelo está na fase de contagem regressiva
     * 
     * @param inCountdown true se estiver na contagem regressiva, false caso contrário
     */
    public void setInCountdown(boolean inCountdown) {
        this.inCountdown = inCountdown;
    }
    
    /**
     * Obtém a duração do duelo em segundos
     * 
     * @return Duração em segundos
     */
    public int getDuration() {
        if (state != DuelState.ENDED) {
            return (int) ((System.currentTimeMillis() - startTime) / 1000);
        }
        return (int) ((endTime - startTime) / 1000);
    }
} 