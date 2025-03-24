package br.com.primeleague.x1.models;

import java.util.Date;

/**
 * Classe que representa o histórico de um duelo
 */
public class DuelHistory {
    
    private String player1;
    private String player2;
    private String winner;
    private String mode;
    private String location;
    private long date;
    private int duration;
    private double bet;
    
    /**
     * Construtor
     * 
     * @param player1 Nome do jogador 1
     * @param player2 Nome do jogador 2
     * @param winner Nome do vencedor (ou null para empate)
     * @param mode Modo do duelo (X1, 2v2, 3v3)
     * @param location Local do duelo (Arena ou Local)
     * @param date Data do duelo
     * @param duration Duração do duelo em segundos
     * @param bet Valor da aposta
     */
    public DuelHistory(String player1, String player2, String winner, String mode, String location, long date, int duration, double bet) {
        this.player1 = player1;
        this.player2 = player2;
        this.winner = winner;
        this.mode = mode;
        this.location = location;
        this.date = date;
        this.duration = duration;
        this.bet = bet;
    }
    
    /**
     * Construtor que define a data como a atual
     * 
     * @param player1 Nome do jogador 1
     * @param player2 Nome do jogador 2
     * @param winner Nome do vencedor (ou null para empate)
     * @param mode Modo do duelo (X1, 2v2, 3v3)
     * @param location Local do duelo (Arena ou Local)
     * @param duration Duração do duelo em segundos
     * @param bet Valor da aposta
     */
    public DuelHistory(String player1, String player2, String winner, String mode, String location, int duration, double bet) {
        this(player1, player2, winner, mode, location, System.currentTimeMillis(), duration, bet);
    }
    
    /**
     * Construtor específico para criar histórico a partir de um duelo finalizado
     * 
     * @param player1 Nome do jogador 1
     * @param player2 Nome do jogador 2
     * @param winner Nome do vencedor (ou null para empate)
     * @param isDraw Se o duelo terminou em empate
     * @param betAmount Valor da aposta
     * @param startTime Horário de início
     * @param endTime Horário de término
     */
    public DuelHistory(String player1, String player2, String winner, boolean isDraw, double betAmount, long startTime, long endTime) {
        this.player1 = player1;
        this.player2 = player2;
        this.winner = isDraw ? null : winner;
        this.mode = "X1";
        this.location = "Desconhecido";
        this.date = startTime;
        this.duration = (int)((endTime - startTime) / 1000);
        this.bet = betAmount;
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
     * Obtém o nome do vencedor
     * 
     * @return Nome do vencedor (ou null para empate)
     */
    public String getWinner() {
        return winner;
    }
    
    /**
     * Verifica se é um empate
     * 
     * @return true se é um empate
     */
    public boolean isDraw() {
        return winner == null || winner.isEmpty();
    }
    
    /**
     * Obtém o modo do duelo
     * 
     * @return Modo do duelo (X1, 2v2, 3v3)
     */
    public String getMode() {
        return mode;
    }
    
    /**
     * Obtém o local do duelo
     * 
     * @return Local do duelo (Arena ou Local)
     */
    public String getLocation() {
        return location;
    }
    
    /**
     * Obtém a data do duelo
     * 
     * @return Data do duelo em milissegundos
     */
    public long getDate() {
        return date;
    }
    
    /**
     * Obtém a data do duelo como um objeto Date
     * 
     * @return Data do duelo
     */
    public Date getDateAsDate() {
        return new Date(date);
    }
    
    /**
     * Obtém a duração do duelo
     * 
     * @return Duração do duelo em segundos
     */
    public int getDuration() {
        return duration;
    }
    
    /**
     * Obtém o valor da aposta
     * 
     * @return Valor da aposta
     */
    public double getBet() {
        return bet;
    }
    
    /**
     * Verifica se o jogador participou do duelo
     * 
     * @param playerName Nome do jogador
     * @return true se o jogador participou do duelo
     */
    public boolean hasPlayer(String playerName) {
        return player1.equals(playerName) || player2.equals(playerName);
    }
    
    /**
     * Verifica se o jogador venceu o duelo
     * 
     * @param playerName Nome do jogador
     * @return true se o jogador venceu o duelo
     */
    public boolean isWinner(String playerName) {
        return playerName.equals(winner);
    }
} 