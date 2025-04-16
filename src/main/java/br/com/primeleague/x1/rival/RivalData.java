package br.com.primeleague.x1.rival;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de dados para representar uma rivalidade entre dois jogadores
 */
public class RivalData {
    
    private String player1;
    private String player2;
    private int player1Victories;
    private int player2Victories;
    private List<String> lastDuels;
    
    /**
     * Construtor para criar um novo registro de rivalidade
     *
     * @param player1 Nome do primeiro jogador
     * @param player2 Nome do segundo jogador
     */
    public RivalData(String player1, String player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.player1Victories = 0;
        this.player2Victories = 0;
        this.lastDuels = new ArrayList<String>();
    }
    
    /**
     * Obtém o nome do primeiro jogador
     *
     * @return Nome do primeiro jogador
     */
    public String getPlayer1() {
        return player1;
    }
    
    /**
     * Obtém o nome do segundo jogador
     *
     * @return Nome do segundo jogador
     */
    public String getPlayer2() {
        return player2;
    }
    
    /**
     * Obtém o número de vitórias do primeiro jogador
     *
     * @return Número de vitórias do primeiro jogador
     */
    public int getPlayer1Victories() {
        return player1Victories;
    }
    
    /**
     * Obtém o número de vitórias do segundo jogador
     *
     * @return Número de vitórias do segundo jogador
     */
    public int getPlayer2Victories() {
        return player2Victories;
    }
    
    /**
     * Obtém as vitórias do jogador especificado
     *
     * @param playerName Nome do jogador
     * @return Número de vitórias do jogador
     */
    public int getVictories(String playerName) {
        if (playerName.equals(player1)) {
            return player1Victories;
        } else if (playerName.equals(player2)) {
            return player2Victories;
        }
        return 0;
    }
    
    /**
     * Obtém a lista dos últimos duelos
     *
     * @return Lista com os nomes dos vencedores dos últimos duelos
     */
    public List<String> getLastDuels() {
        return lastDuels;
    }
    
    /**
     * Obtém o vencedor do último duelo
     *
     * @return Nome do vencedor do último duelo ou null se não houver
     */
    public String getLastDuelWinner() {
        if (lastDuels.isEmpty()) {
            return null;
        }
        return lastDuels.get(lastDuels.size() - 1);
    }
    
    /**
     * Incrementa uma vitória para o jogador especificado
     *
     * @param playerName Nome do jogador
     */
    public void addVictory(String playerName) {
        if (playerName.equals(player1)) {
            player1Victories++;
            lastDuels.add(player1);
        } else if (playerName.equals(player2)) {
            player2Victories++;
            lastDuels.add(player2);
        }
        
        // Limitar o histórico de duelos aos 10 últimos
        if (lastDuels.size() > 10) {
            lastDuels.remove(0);
        }
    }
    
    /**
     * Obtém o número total de duelos entre os jogadores
     *
     * @return Número total de duelos
     */
    public int getTotalDuels() {
        return player1Victories + player2Victories;
    }
    
    /**
     * Verifica se existe uma rivalidade (número mínimo de duelos)
     *
     * @param minDuels Número mínimo de duelos para considerar uma rivalidade
     * @return true se existe uma rivalidade
     */
    public boolean isRivalry(int minDuels) {
        return getTotalDuels() >= minDuels;
    }
    
    /**
     * Cria uma chave única para identificar esta rivalidade
     *
     * @return Chave identificadora da rivalidade
     */
    public String getKey() {
        // Garantir que a chave seja sempre a mesma independente da ordem dos nomes
        if (player1.compareTo(player2) <= 0) {
            return player1 + "_" + player2;
        } else {
            return player2 + "_" + player1;
        }
    }
    
    /**
     * Verifica se o jogador faz parte desta rivalidade
     *
     * @param playerName Nome do jogador
     * @return true se o jogador faz parte da rivalidade
     */
    public boolean hasPlayer(String playerName) {
        return player1.equals(playerName) || player2.equals(playerName);
    }
    
    /**
     * Obtém o nome do outro jogador na rivalidade
     *
     * @param playerName Nome do jogador atual
     * @return Nome do outro jogador na rivalidade
     */
    public String getOpponent(String playerName) {
        if (player1.equals(playerName)) {
            return player2;
        } else if (player2.equals(playerName)) {
            return player1;
        }
        return null;
    }
} 