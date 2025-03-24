package br.com.primeleague.x1.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import br.com.primeleague.x1.Main;

/**
 * Gerenciador de equipes
 */
public class TeamManager {
    
    private final Main plugin;
    private final Map<String, String> playerTeams; // playerName -> teamName
    private final Map<String, String> teamLeaders; // teamName -> leaderName
    
    /**
     * Construtor
     * 
     * @param plugin Instância do plugin
     */
    public TeamManager(Main plugin) {
        this.plugin = plugin;
        this.playerTeams = new HashMap<>();
        this.teamLeaders = new HashMap<>();
    }
    
    /**
     * Cria uma nova equipe
     * 
     * @param leaderName Nome do líder da equipe
     * @return true se a equipe foi criada com sucesso
     */
    public boolean createTeam(String leaderName) {
        // Verificar se o jogador já está em uma equipe
        if (playerTeams.containsKey(leaderName)) {
            return false;
        }
        
        // Gerar um nome de equipe único baseado no nome do líder
        String teamName = "Team" + leaderName;
        int counter = 1;
        while (teamLeaders.containsKey(teamName)) {
            teamName = "Team" + leaderName + counter;
            counter++;
        }
        
        // Criar a equipe
        playerTeams.put(leaderName, teamName);
        teamLeaders.put(teamName, leaderName);
        
        return true;
    }
    
    /**
     * Cria uma nova equipe com nome específico
     * 
     * @param leaderName Nome do líder da equipe
     * @param teamName Nome da equipe
     * @return true se a equipe foi criada com sucesso
     */
    public boolean createTeam(String leaderName, String teamName) {
        // Verificar se o jogador já está em uma equipe
        if (playerTeams.containsKey(leaderName)) {
            return false;
        }
        
        // Verificar se o nome da equipe já existe
        if (teamLeaders.containsKey(teamName)) {
            return false;
        }
        
        // Criar a equipe
        playerTeams.put(leaderName, teamName);
        teamLeaders.put(teamName, leaderName);
        
        return true;
    }
    
    /**
     * Adiciona um jogador a uma equipe
     * 
     * @param playerName Nome do jogador
     * @param teamName Nome da equipe
     * @return true se o jogador foi adicionado com sucesso
     */
    public boolean addPlayerToTeam(String playerName, String teamName) {
        // Verificar se o jogador já está em uma equipe
        if (playerTeams.containsKey(playerName)) {
            return false;
        }
        
        // Verificar se a equipe existe
        if (!teamLeaders.containsKey(teamName)) {
            return false;
        }
        
        // Adicionar o jogador à equipe
        playerTeams.put(playerName, teamName);
        
        return true;
    }
    
    /**
     * Remove um jogador de uma equipe
     * 
     * @param playerName Nome do jogador
     * @return true se o jogador foi removido com sucesso
     */
    public boolean removePlayerFromTeam(String playerName) {
        // Verificar se o jogador está em uma equipe
        if (!playerTeams.containsKey(playerName)) {
            return false;
        }
        
        String teamName = playerTeams.get(playerName);
        
        // Se o jogador for o líder da equipe, a equipe é deletada
        if (teamLeaders.get(teamName).equals(playerName)) {
            teamLeaders.remove(teamName);
        }
        
        // Remover o jogador da equipe
        playerTeams.remove(playerName);
        
        return true;
    }
    
    /**
     * Obtém a equipe de um jogador
     * 
     * @param playerName Nome do jogador
     * @return Nome da equipe ou null se o jogador não estiver em uma equipe
     */
    public String getPlayerTeam(String playerName) {
        return playerTeams.get(playerName);
    }
    
    /**
     * Obtém o líder de uma equipe
     * 
     * @param teamName Nome da equipe
     * @return Nome do líder ou null se a equipe não existir
     */
    public String getTeamLeader(String teamName) {
        return teamLeaders.get(teamName);
    }
    
    /**
     * Verifica se um jogador é líder de uma equipe
     * 
     * @param playerName Nome do jogador
     * @return true se o jogador for líder de uma equipe
     */
    public boolean isTeamLeader(String playerName) {
        return teamLeaders.containsValue(playerName);
    }
    
    /**
     * Verifica se um jogador está em uma equipe
     * 
     * @param player Nome do jogador
     * @return true se o jogador está em uma equipe
     */
    public boolean isInTeam(String player) {
        return playerTeams.containsKey(player);
    }
    
    /**
     * Verifica se dois jogadores estão na mesma equipe
     * 
     * @param player1 Nome do jogador 1
     * @param player2 Nome do jogador 2
     * @return true se os jogadores estão na mesma equipe
     */
    public boolean areInSameTeam(String player1, String player2) {
        if (!isInTeam(player1) || !isInTeam(player2)) {
            return false;
        }
        
        String team1 = playerTeams.get(player1);
        String team2 = playerTeams.get(player2);
        
        return team1.equals(team2);
    }
    
    /**
     * Envia uma mensagem para todos os membros de uma equipe
     * 
     * @param leader Nome do líder
     * @param message Mensagem a enviar
     */
    public void sendTeamMessage(String leader, String message) {
        if (!teamLeaders.containsKey(leader)) {
            return;
        }
        
        String teamName = leader;
        
        for (String player : playerTeams.keySet()) {
            if (playerTeams.get(player).equals(teamName)) {
                Player playerObj = plugin.getServer().getPlayerExact(player);
                if (playerObj != null) {
                    playerObj.sendMessage(message);
                }
            }
        }
    }
    
    /**
     * Obtém a lista de membros de uma equipe
     * 
     * @param playerName Nome do jogador
     * @return Lista de membros da equipe ou null se o jogador não estiver em uma equipe
     */
    public List<String> getTeam(String playerName) {
        String teamName = playerTeams.get(playerName);
        if (teamName == null) {
            return null;
        }
        
        List<String> members = new ArrayList<>();
        for (Map.Entry<String, String> entry : playerTeams.entrySet()) {
            if (entry.getValue().equals(teamName)) {
                members.add(entry.getKey());
            }
        }
        
        return members;
    }
    
    /**
     * Remove um jogador de sua equipe
     * 
     * @param playerName Nome do jogador
     * @return true se o jogador foi removido com sucesso
     */
    public boolean removeFromTeam(String playerName) {
        // Verificar se o jogador está em uma equipe
        if (!playerTeams.containsKey(playerName)) {
            return false;
        }
        
        String teamName = playerTeams.get(playerName);
        
        // Se o jogador for o líder da equipe, a equipe é deletada
        if (teamLeaders.get(teamName).equals(playerName)) {
            // Remover todos os membros da equipe
            List<String> members = getTeam(playerName);
            for (String member : members) {
                playerTeams.remove(member);
            }
            teamLeaders.remove(teamName);
        } else {
            // Remover apenas o jogador
            playerTeams.remove(playerName);
        }
        
        return true;
    }
} 