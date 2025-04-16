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
    
    // Sistema de convites para equipes
    private final Map<String, String> teamInvites; // invitedPlayer -> teamName
    private final Map<String, Long> inviteTimestamps; // invitedPlayer -> timestamp
    private final int INVITE_TIMEOUT_SECONDS = 60; // Tempo limite para o convite expirar
    
    /**
     * Construtor
     * 
     * @param plugin Instância do plugin
     */
    public TeamManager(Main plugin) {
        this.plugin = plugin;
        this.playerTeams = new HashMap<>();
        this.teamLeaders = new HashMap<>();
        this.teamInvites = new HashMap<>();
        this.inviteTimestamps = new HashMap<>();
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

    /**
     * Obtém o líder da equipe de um jogador
     * 
     * @param playerName Nome do jogador
     * @return Nome do líder ou null se o jogador não estiver em uma equipe
     */
    public String getTeamLeaderByPlayer(String playerName) {
        String teamName = getPlayerTeam(playerName);
        if (teamName == null) {
            return null;
        }
        return getTeamLeader(teamName);
    }
    
    /**
     * Convida um jogador para uma equipe
     * 
     * @param leaderName Nome do líder da equipe
     * @param playerName Nome do jogador a ser convidado
     * @return true se o convite foi enviado com sucesso
     */
    public boolean invitePlayerToTeam(String leaderName, String playerName) {
        // Verificar se o líder tem uma equipe
        if (!isTeamLeader(leaderName)) {
            return false;
        }
        
        // Verificar se o jogador já está em uma equipe
        if (isInTeam(playerName)) {
            return false;
        }
        
        // Verificar se o jogador já tem um convite pendente
        if (hasInvite(playerName)) {
            return false;
        }
        
        // Obter o nome da equipe do líder
        String teamName = "";
        for (Map.Entry<String, String> entry : teamLeaders.entrySet()) {
            if (entry.getValue().equals(leaderName)) {
                teamName = entry.getKey();
                break;
            }
        }
        
        // Verificar se a equipe tem menos de 3 membros (incluindo o líder)
        int teamSize = 0;
        for (String team : playerTeams.values()) {
            if (team.equals(teamName)) {
                teamSize++;
            }
        }
        
        if (teamSize >= 3) {
            return false; // Equipe cheia
        }
        
        // Enviar o convite
        teamInvites.put(playerName, teamName);
        inviteTimestamps.put(playerName, System.currentTimeMillis());
        
        // Agendar expiração do convite
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (teamInvites.containsKey(playerName) &&
                    inviteTimestamps.get(playerName) + (INVITE_TIMEOUT_SECONDS * 1000) <= System.currentTimeMillis()) {
                
                teamInvites.remove(playerName);
                inviteTimestamps.remove(playerName);
                
                // Notificar jogadores
                Player invitedPlayer = plugin.getServer().getPlayerExact(playerName);
                if (invitedPlayer != null && invitedPlayer.isOnline()) {
                    plugin.getMessageManager().sendMessage(invitedPlayer, "equipes.convite-expirado");
                }
                
                Player leader = plugin.getServer().getPlayerExact(leaderName);
                if (leader != null && leader.isOnline()) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("jogador", playerName);
                    plugin.getMessageManager().sendMessage(leader, "equipes.convite-expirado-lider", placeholders);
                }
            }
        }, INVITE_TIMEOUT_SECONDS * 20L);
        
        return true;
    }
    
    /**
     * Verifica se um jogador tem um convite pendente
     * 
     * @param playerName Nome do jogador
     * @return true se o jogador tem um convite pendente
     */
    public boolean hasInvite(String playerName) {
        return teamInvites.containsKey(playerName);
    }
    
    /**
     * Obtém o nome da equipe que convidou um jogador
     * 
     * @param playerName Nome do jogador
     * @return Nome da equipe ou null se não houver convite
     */
    public String getInvitingTeam(String playerName) {
        return teamInvites.get(playerName);
    }
    
    /**
     * Obtém o líder de uma equipe que convidou um jogador
     * 
     * @param playerName Nome do jogador
     * @return Nome do líder ou null se não houver convite
     */
    public String getInvitingLeader(String playerName) {
        String teamName = teamInvites.get(playerName);
        if (teamName == null) {
            return null;
        }
        
        return teamLeaders.get(teamName);
    }
    
    /**
     * Aceita um convite para entrar em uma equipe
     * 
     * @param playerName Nome do jogador
     * @return true se o convite foi aceito com sucesso
     */
    public boolean acceptInvite(String playerName) {
        // Verificar se o jogador tem um convite
        if (!hasInvite(playerName)) {
            return false;
        }
        
        // Obter o nome da equipe
        String teamName = teamInvites.get(playerName);
        
        // Remover o convite
        teamInvites.remove(playerName);
        inviteTimestamps.remove(playerName);
        
        // Adicionar o jogador à equipe
        playerTeams.put(playerName, teamName);
        
        return true;
    }
    
    /**
     * Recusa um convite para entrar em uma equipe
     * 
     * @param playerName Nome do jogador
     * @return true se o convite foi recusado com sucesso
     */
    public boolean rejectInvite(String playerName) {
        // Verificar se o jogador tem um convite
        if (!hasInvite(playerName)) {
            return false;
        }
        
        // Remover o convite
        teamInvites.remove(playerName);
        inviteTimestamps.remove(playerName);
        
        return true;
    }
    
    /**
     * Obtém o tamanho de uma equipe
     * 
     * @param teamName Nome da equipe
     * @return Número de membros na equipe
     */
    public int getTeamSize(String teamName) {
        int count = 0;
        for (String team : playerTeams.values()) {
            if (team.equals(teamName)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Obtém todos os líderes de equipe
     * 
     * @return Lista com os nomes de todos os líderes de equipe
     */
    public List<String> getAllTeamLeaders() {
        return new ArrayList<>(teamLeaders.values());
    }
} 