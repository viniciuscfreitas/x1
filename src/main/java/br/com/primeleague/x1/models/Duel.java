package br.com.primeleague.x1.models;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

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
    
    // Listas para duelos em equipe
    private List<String> team1;
    private List<String> team2;
    private List<String> winnerTeam;
    
    // Mapa para armazenar eliminações por jogador
    private Map<String, Integer> playerKills;
    
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
        
        // Inicializa listas vazias para equipes
        this.team1 = new ArrayList<>();
        this.team2 = new ArrayList<>();
        this.winnerTeam = new ArrayList<>();
        
        // Inicializa o mapa de eliminações
        this.playerKills = new HashMap<>();
        
        // Adiciona os líderes às suas respectivas equipes
        if (player1 != null) {
            this.team1.add(player1);
            this.playerKills.put(player1, 0);
        }
        if (player2 != null) {
            this.team2.add(player2);
            this.playerKills.put(player2, 0);
        }
    }
    
    /**
     * Construtor para duelos em equipe
     * 
     * @param id ID único do duelo
     * @param leader1 Nome do líder da equipe 1
     * @param leader2 Nome do líder da equipe 2
     * @param team1 Lista de membros da equipe 1
     * @param team2 Lista de membros da equipe 2
     * @param type Tipo de duelo
     */
    public Duel(UUID id, String leader1, String leader2, List<String> team1, List<String> team2, DuelType type) {
        this.id = id;
        this.player1 = leader1;
        this.player2 = leader2;
        this.type = type;
        this.state = DuelState.STARTING;
        this.draw = false;
        this.betAmount = 0;
        this.startTime = System.currentTimeMillis();
        this.inCountdown = false;
        
        // Inicializa as listas de equipe
        this.team1 = new ArrayList<>(team1);
        this.team2 = new ArrayList<>(team2);
        this.winnerTeam = new ArrayList<>();
        
        // Inicializa o mapa de eliminações
        this.playerKills = new HashMap<>();
        
        // Inicializa as eliminações para todos os jogadores
        for (String player : team1) {
            this.playerKills.put(player, 0);
        }
        
        for (String player : team2) {
            this.playerKills.put(player, 0);
        }
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
    
    /**
     * Verifica se este duelo é um duelo em equipe
     * 
     * @return true se for um duelo em equipe, false caso contrário
     */
    public boolean isTeamDuel() {
        return type.isTeamDuel();
    }
    
    /**
     * Obtém a lista de membros da equipe 1
     * 
     * @return Lista de membros da equipe 1
     */
    public List<String> getTeam1() {
        return team1;
    }
    
    /**
     * Obtém a lista de membros da equipe 2
     * 
     * @return Lista de membros da equipe 2
     */
    public List<String> getTeam2() {
        return team2;
    }
    
    /**
     * Define a lista de membros da equipe 1
     * 
     * @param team1 Nova lista de membros
     */
    public void setTeam1(List<String> team1) {
        this.team1 = team1;
    }
    
    /**
     * Define a lista de membros da equipe 2
     * 
     * @param team2 Nova lista de membros
     */
    public void setTeam2(List<String> team2) {
        this.team2 = team2;
    }
    
    /**
     * Obtém a equipe vencedora
     * 
     * @return Lista de membros da equipe vencedora
     */
    public List<String> getWinnerTeam() {
        return winnerTeam;
    }
    
    /**
     * Define a equipe vencedora
     * 
     * @param winnerTeam Nova lista de membros da equipe vencedora
     */
    public void setWinnerTeam(List<String> winnerTeam) {
        this.winnerTeam = winnerTeam;
    }
    
    /**
     * Verifica se um jogador está na equipe 1
     * 
     * @param playerName Nome do jogador
     * @return true se o jogador está na equipe 1, false caso contrário
     */
    public boolean isInTeam1(String playerName) {
        return team1.contains(playerName);
    }
    
    /**
     * Verifica se um jogador está na equipe 2
     * 
     * @param playerName Nome do jogador
     * @return true se o jogador está na equipe 2, false caso contrário
     */
    public boolean isInTeam2(String playerName) {
        return team2.contains(playerName);
    }
    
    /**
     * Verifica se dois jogadores estão na mesma equipe
     * 
     * @param player1 Nome do primeiro jogador
     * @param player2 Nome do segundo jogador
     * @return true se os jogadores estão na mesma equipe, false caso contrário
     */
    public boolean areInSameTeam(String player1, String player2) {
        return (isInTeam1(player1) && isInTeam1(player2)) || (isInTeam2(player1) && isInTeam2(player2));
    }
    
    /**
     * Adiciona um jogador à equipe 1
     * 
     * @param playerName Nome do jogador
     */
    public void addToTeam1(String playerName) {
        if (!team1.contains(playerName)) {
            team1.add(playerName);
        }
    }
    
    /**
     * Adiciona um jogador à equipe 2
     * 
     * @param playerName Nome do jogador
     */
    public void addToTeam2(String playerName) {
        if (!team2.contains(playerName)) {
            team2.add(playerName);
        }
    }
    
    /**
     * Obtém a equipe de um jogador
     * 
     * @param playerName Nome do jogador
     * @return 1 para equipe 1, 2 para equipe 2, 0 se não estiver em nenhuma equipe
     */
    public int getPlayerTeam(String playerName) {
        if (isInTeam1(playerName)) {
            return 1;
        } else if (isInTeam2(playerName)) {
            return 2;
        } else {
            return 0;
        }
    }
    
    /**
     * Obtém a lista de jogadores da equipe oposta
     * 
     * @param playerName Nome do jogador
     * @return Lista de jogadores da equipe oposta, ou null se o jogador não estiver em nenhuma equipe
     */
    public List<String> getOpponentTeam(String playerName) {
        if (isInTeam1(playerName)) {
            return team2;
        } else if (isInTeam2(playerName)) {
            return team1;
        } else {
            return null;
        }
    }
    
    /**
     * Registra uma eliminação para um jogador
     * 
     * @param killer Nome do jogador que eliminou
     */
    public void addKill(String killer) {
        int kills = playerKills.getOrDefault(killer, 0);
        playerKills.put(killer, kills + 1);
    }
    
    /**
     * Obtém o número de eliminações de um jogador
     * 
     * @param playerName Nome do jogador
     * @return Número de eliminações
     */
    public int getPlayerKills(String playerName) {
        return playerKills.getOrDefault(playerName, 0);
    }
    
    /**
     * Obtém o mapa completo de eliminações
     * 
     * @return Mapa com jogadores e suas eliminações
     */
    public Map<String, Integer> getAllKills() {
        return playerKills;
    }
    
    /**
     * Obtém o total de eliminações de uma equipe
     * 
     * @param teamNumber 1 para equipe 1, 2 para equipe 2
     * @return Total de eliminações da equipe
     */
    public int getTeamKills(int teamNumber) {
        List<String> team = (teamNumber == 1) ? team1 : team2;
        int totalKills = 0;
        
        for (String player : team) {
            totalKills += getPlayerKills(player);
        }
        
        return totalKills;
    }
    
    /**
     * Verifica se o jogador está participando deste duelo
     * 
     * @param playerName Nome do jogador
     * @return true se o jogador está no duelo, false caso contrário
     */
    public boolean hasPlayer(String playerName) {
        return isInTeam1(playerName) || isInTeam2(playerName);
    }
    
    /**
     * Obtém o valor atual da contagem regressiva, com base no estado do duelo
     * 
     * @return Valor da contagem regressiva em segundos, ou -1 se não estiver em contagem
     */
    public int getCountdown() {
        if (!isInCountdown() || getState() != DuelState.STARTING) {
            return -1;
        }
        
        // Por padrão, a contagem regressiva é de 5 segundos
        int totalCountdown = 5;
        
        // Calcular quanto tempo se passou desde o início
        long agora = System.currentTimeMillis();
        long passado = (agora - getStartTime()) / 1000; // em segundos
        
        // Calcular tempo restante
        int restante = totalCountdown - (int)passado;
        
        // Garantir que não seja negativo
        return Math.max(0, restante);
    }
} 