package br.com.primeleague.x1.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe que representa as estatísticas de um jogador
 */
public class PlayerStats {
    
    private final String playerName;
    private int victories;
    private int defeats;
    private int draws;
    private int streak;
    private int elo;
    private final List<Duel> recentDuels;
    
    /**
     * Construtor
     * 
     * @param playerName Nome do jogador
     */
    public PlayerStats(String playerName) {
        this.playerName = playerName;
        this.victories = 0;
        this.defeats = 0;
        this.draws = 0;
        this.streak = 0;
        this.elo = 1000;
        this.recentDuels = new ArrayList<>();
    }
    
    /**
     * Obtém o nome do jogador
     * 
     * @return Nome do jogador
     */
    public String getPlayerName() {
        return playerName;
    }
    
    /**
     * Obtém a quantidade de vitórias
     * 
     * @return Quantidade de vitórias
     */
    public int getVictories() {
        return victories;
    }
    
    /**
     * Define a quantidade de vitórias
     * 
     * @param victories Quantidade de vitórias
     */
    public void setVictories(int victories) {
        this.victories = victories;
    }
    
    /**
     * Incrementa a quantidade de vitórias
     */
    public void incrementVictories() {
        this.victories++;
        this.streak = Math.max(0, this.streak + 1);
    }
    
    /**
     * Obtém a quantidade de derrotas
     * 
     * @return Quantidade de derrotas
     */
    public int getDefeats() {
        return defeats;
    }
    
    /**
     * Define a quantidade de derrotas
     * 
     * @param defeats Quantidade de derrotas
     */
    public void setDefeats(int defeats) {
        this.defeats = defeats;
    }
    
    /**
     * Incrementa a quantidade de derrotas
     */
    public void incrementDefeats() {
        this.defeats++;
        this.streak = Math.min(0, this.streak - 1);
    }
    
    /**
     * Obtém a quantidade de empates
     * 
     * @return Quantidade de empates
     */
    public int getDraws() {
        return draws;
    }
    
    /**
     * Define a quantidade de empates
     * 
     * @param draws Quantidade de empates
     */
    public void setDraws(int draws) {
        this.draws = draws;
    }
    
    /**
     * Incrementa a quantidade de empates
     */
    public void incrementDraws() {
        this.draws++;
    }
    
    /**
     * Obtém a sequência atual
     * 
     * @return Sequência atual (positivo para vitórias, negativo para derrotas)
     */
    public int getStreak() {
        return streak;
    }
    
    /**
     * Define a sequência atual
     * 
     * @param streak Sequência atual
     */
    public void setStreak(int streak) {
        this.streak = streak;
    }
    
    /**
     * Obtém o Elo do jogador
     * 
     * @return Elo do jogador
     */
    public int getElo() {
        return elo;
    }
    
    /**
     * Define o Elo do jogador
     * 
     * @param elo Elo do jogador
     */
    public void setElo(int elo) {
        this.elo = elo;
    }
    
    /**
     * Adiciona Elo ao jogador
     * 
     * @param amount Quantidade a adicionar
     */
    public void addElo(int amount) {
        this.elo += amount;
    }
    
    /**
     * Remove Elo do jogador
     * 
     * @param amount Quantidade a remover
     */
    public void removeElo(int amount) {
        this.elo = Math.max(0, this.elo - amount);
    }
    
    /**
     * Calcula o KDR (Kill Death Ratio) do jogador
     * 
     * @return KDR do jogador
     */
    public double getKDR() {
        if (defeats == 0) {
            return victories;
        }
        return (double) victories / defeats;
    }
    
    /**
     * Calcula a taxa de vitórias (em porcentagem)
     * 
     * @return Taxa de vitórias
     */
    public double getWinRate() {
        int total = victories + defeats + draws;
        if (total == 0) {
            return 0;
        }
        return (double) victories / total * 100;
    }
    
    /**
     * Obtém o número total de duelos
     * 
     * @return Número total de duelos
     */
    public int getTotalDuels() {
        return victories + defeats + draws;
    }
    
    /**
     * Adiciona um duelo recente à lista
     * 
     * @param duel Duelo recente
     */
    public void addRecentDuel(Duel duel) {
        recentDuels.add(0, duel);
        
        // Limita a lista a 10 duelos recentes
        if (recentDuels.size() > 10) {
            recentDuels.remove(10);
        }
    }
    
    /**
     * Obtém a lista de duelos recentes
     * 
     * @return Lista de duelos recentes
     */
    public List<Duel> getRecentDuels() {
        return recentDuels;
    }
    
    /**
     * Limpa a lista de duelos recentes
     */
    public void clearRecentDuels() {
        recentDuels.clear();
    }
} 