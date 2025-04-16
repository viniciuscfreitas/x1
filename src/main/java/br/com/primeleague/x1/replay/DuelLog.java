package br.com.primeleague.x1.replay;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import br.com.primeleague.x1.models.Duel;

/**
 * Classe para registrar eventos de duelo para visualização posterior
 */
public class DuelLog {
    
    private UUID duelId;
    private String duelDate;
    private String duelType;
    private List<String> team1;
    private List<String> team2;
    private List<DuelEvent> events;
    private long startTime;
    private long endTime;
    
    // Estatísticas adicionais para métricas
    private Map<String, Integer> playerDamageDealt; // Dano causado por jogador
    private Map<String, Integer> playerDamageReceived; // Dano recebido por jogador
    private Map<String, Integer> playerKills; // Eliminações por jogador
    private int totalDamage; // Dano total no duelo
    
    /**
     * Construtor para um novo log de duelo
     * 
     * @param duel Duelo para registrar eventos
     * @param logId UUID a ser usado para este log
     */
    public DuelLog(Duel duel, UUID logId) {
        this.duelId = logId;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        this.duelDate = sdf.format(new Date());
        this.duelType = duel.isTeamDuel() ? "TEAM" : "SOLO";
        this.team1 = new ArrayList<>(duel.getTeam1());
        this.team2 = new ArrayList<>(duel.getTeam2());
        this.events = new ArrayList<>();
        this.startTime = System.currentTimeMillis();
        
        // Inicializar mapas de estatísticas
        this.playerDamageDealt = new HashMap<>();
        this.playerDamageReceived = new HashMap<>();
        this.playerKills = new HashMap<>();
        this.totalDamage = 0;
        
        // Inicializar as contagens de dano para cada jogador
        for (String player : team1) {
            playerDamageDealt.put(player, 0);
            playerDamageReceived.put(player, 0);
            playerKills.put(player, 0);
        }
        
        for (String player : team2) {
            playerDamageDealt.put(player, 0);
            playerDamageReceived.put(player, 0);
            playerKills.put(player, 0);
        }
        
        System.out.println("[PrimeLeagueX1] Criando DuelLog com UUID: " + logId.toString());
    }
    
    /**
     * Adiciona um evento de início de duelo
     */
    public void logDuelStart() {
        events.add(new DuelEvent(EventType.DUEL_START, null, null, null, null));
    }
    
    /**
     * Adiciona um evento de fim de duelo
     * 
     * @param winnerTeam Nome da equipe vencedora ou null se empate
     */
    public void logDuelEnd(List<String> winnerTeam) {
        this.endTime = System.currentTimeMillis();
        
        // Armazenar corretamente o time vencedor para o resultado do duelo
        int winnerTeamNumber = 0; // 0 = empate, 1 = equipe 1, 2 = equipe 2
        String winnerPlayerName = null;
        
        if (winnerTeam != null && !winnerTeam.isEmpty()) {
            // Verificar a qual equipe o vencedor pertence
            boolean isTeam1 = false;
            for (String player : winnerTeam) {
                if (team1.contains(player)) {
                    isTeam1 = true;
                    winnerPlayerName = player;
                    break;
                }
            }
            
            if (isTeam1) {
                winnerTeamNumber = 1;
                System.out.println("[PrimeLeagueX1] Registrando vitória para jogador da Equipe 1: " + 
                                   (team1.size() == 1 ? team1.get(0) : "Equipe 1"));
            } else {
                winnerTeamNumber = 2;
                // Para duelos 1v1, usar o nome do jogador vencedor
                if (team2.size() == 1 && winnerTeam.size() == 1) {
                    winnerPlayerName = team2.get(0);
                }
                System.out.println("[PrimeLeagueX1] Registrando vitória para jogador da Equipe 2: " + 
                                   (team2.size() == 1 ? team2.get(0) : "Equipe 2"));
            }
        } else {
            System.out.println("[PrimeLeagueX1] Registrando empate no duelo");
        }
        
        // Registrar o evento com informação clara sobre o vencedor
        events.add(new DuelEvent(EventType.DUEL_END, null, null, null, winnerTeam, winnerTeamNumber, winnerPlayerName));
    }
    
    /**
     * Adiciona um evento de eliminação
     * 
     * @param killed Jogador eliminado
     * @param killer Jogador que eliminou (null se morte natural)
     */
    public void logKill(Player killed, Player killer) {
        String killedName = killed.getName();
        String killerName = killer != null ? killer.getName() : null;
        Location loc = killed.getLocation();
        
        // Registrar eliminação nas estatísticas
        if (killerName != null && playerKills.containsKey(killerName)) {
            int kills = playerKills.get(killerName);
            playerKills.put(killerName, kills + 1);
            System.out.println("[PrimeLeagueX1] Registrada eliminação para " + killerName + " (Total: " + (kills + 1) + ")");
        }
        
        events.add(new DuelEvent(EventType.PLAYER_KILL, killedName, killerName, loc, null));
    }
    
    /**
     * Adiciona um evento de dano significativo
     * 
     * @param damaged Jogador que recebeu dano
     * @param damager Jogador que causou o dano
     * @param damage Quantidade de dano
     */
    public void logDamage(Player damaged, Player damager, double damage) {
        // Registrar todos os danos para estatísticas
        String damagedName = damaged.getName();
        String damagerName = damager.getName();
        
        // Atualizar dano total
        int damageInt = (int) damage;
        totalDamage += damageInt;
        
        // Atualizar dano causado pelo atacante
        if (playerDamageDealt.containsKey(damagerName)) {
            int dealt = playerDamageDealt.get(damagerName);
            playerDamageDealt.put(damagerName, dealt + damageInt);
        }
        
        // Atualizar dano recebido pelo alvo
        if (playerDamageReceived.containsKey(damagedName)) {
            int received = playerDamageReceived.get(damagedName);
            playerDamageReceived.put(damagedName, received + damageInt);
        }
        
        // Debug para verificar o registro de dano
        System.out.println("[PrimeLeagueX1] Registrando dano: " + damagerName + " causou " + damageInt + 
                           " de dano a " + damagedName + " (Total acumulado: " + totalDamage + ")");
        
        // Registrar apenas danos significativos (acima de 3 corações) como eventos
        if (damage < 6) {
            return;
        }
        
        Location loc = damaged.getLocation();
        events.add(new DuelEvent(EventType.SIGNIFICANT_DAMAGE, damagedName, damagerName, loc, null, 0, damageInt));
    }
    
    /**
     * Salva o log do duelo em um arquivo
     * 
     * @param folder Pasta para salvar o arquivo
     * @return true se salvou com sucesso
     */
    public boolean saveToFile(File folder) {
        try {
            if (!folder.exists()) {
                boolean created = folder.mkdirs();
                if (!created) {
                    System.out.println("[PrimeLeagueX1] Falha ao criar diretório de logs: " + folder.getAbsolutePath());
                    return false;
                }
            }
            
            // Verificar se o diretório é gravável
            if (!folder.canWrite()) {
                System.out.println("[PrimeLeagueX1] O diretório de logs não é gravável: " + folder.getAbsolutePath());
                return false;
            }
            
            File logFile = new File(folder, duelId.toString() + ".duellog");
            System.out.println("[PrimeLeagueX1] Tentando salvar log do duelo em: " + logFile.getAbsolutePath());
            System.out.println("[PrimeLeagueX1] UUID do duelo usado para salvar arquivo: " + duelId.toString());
            
            try (FileWriter writer = new FileWriter(logFile)) {
                // Cabeçalho
                writer.write("Duelo: " + duelId.toString() + "\n");
                writer.write("Data: " + duelDate + "\n");
                writer.write("Tipo: " + duelType + "\n");
                writer.write("Duração: " + formatDuration(endTime - startTime) + "\n\n");
                
                // Equipes
                writer.write("Equipe 1:\n");
                for (String player : team1) {
                    writer.write("- " + player + "\n");
                }
                
                writer.write("\nEquipe 2:\n");
                for (String player : team2) {
                    writer.write("- " + player + "\n");
                }
                
                // Estatísticas
                writer.write("\nEstatísticas:\n");
                writer.write("Dano Total: " + totalDamage + "\n");
                
                // Estatísticas por jogador
                writer.write("\nDano Causado:\n");
                for (Map.Entry<String, Integer> entry : playerDamageDealt.entrySet()) {
                    writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
                }
                
                writer.write("\nDano Recebido:\n");
                for (Map.Entry<String, Integer> entry : playerDamageReceived.entrySet()) {
                    writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
                }
                
                writer.write("\nEliminações:\n");
                boolean hasKills = false;
                for (Map.Entry<String, Integer> entry : playerKills.entrySet()) {
                    if (entry.getValue() > 0) {
                        writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
                        hasKills = true;
                    }
                }
                
                if (!hasKills) {
                    writer.write("Nenhuma eliminação registrada.\n");
                }
                
                // Calcular eventos significativos
                int significativeDamages = 0;
                for (DuelEvent event : events) {
                    if (event.getType() == EventType.SIGNIFICANT_DAMAGE) {
                        significativeDamages++;
                    }
                }
                
                // Informações de métricas extras
                writer.write("\nMétricas:\n");
                writer.write("Total de eventos: " + events.size() + "\n");
                writer.write("Eliminações totais: " + getTotalKills() + "\n");
                writer.write("Dano total: " + totalDamage + "\n");
                writer.write("Danos significativos: " + significativeDamages + "\n");
                
                // Eventos
                writer.write("\nEventos:\n");
                long duelStartTime = events.isEmpty() ? startTime : startTime;
                
                for (DuelEvent event : events) {
                    writer.write(formatEvent(event, duelStartTime) + "\n");
                }
                
                writer.flush();
                System.out.println("[PrimeLeagueX1] Log do duelo salvo com sucesso: " + logFile.getAbsolutePath());
                return true;
            } catch (IOException e) {
                System.out.println("[PrimeLeagueX1] Erro ao salvar log do duelo: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            System.out.println("[PrimeLeagueX1] Exceção ao salvar log do duelo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Formata a duração em formato legível
     * 
     * @param duration Duração em milissegundos
     * @return String formatada
     */
    private String formatDuration(long duration) {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    /**
     * Formata um evento para o arquivo
     * 
     * @param event Evento a formatar
     * @param duelStartTime Timestamp de início do duelo
     * @return String formatada
     */
    private String formatEvent(DuelEvent event, long duelStartTime) {
        long eventTime = event.getTimestamp() - duelStartTime;
        long seconds = eventTime / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        String timeString = String.format("[%02d:%02d]", minutes, seconds);
        
        switch (event.getType()) {
            case DUEL_START:
                return timeString + " Duelo iniciado";
                
            case DUEL_END:
                int winnerTeam = event.getWinnerTeamNumber();
                String winnerPlayer = event.getWinnerPlayerName();
                
                if (winnerTeam == 0) {
                    return timeString + " Duelo terminou. Resultado: Empate";
                } else if (winnerTeam == 1) {
                    // Se temos o nome do jogador específico e é duelo 1v1, usar o nome
                    if (winnerPlayer != null && team1.size() == 1) {
                        return timeString + " Duelo terminou. Vencedor: " + winnerPlayer;
                    } else {
                        // Caso contrário, usar nome da equipe
                        return timeString + " Duelo terminou. Vencedor: " + 
                               (team1.size() == 1 ? team1.get(0) : "Equipe 1");
                    }
                } else {
                    // Se temos o nome do jogador específico e é duelo 1v1, usar o nome
                    if (winnerPlayer != null && team2.size() == 1) {
                        return timeString + " Duelo terminou. Vencedor: " + winnerPlayer;
                    } else {
                        // Caso contrário, usar nome da equipe
                        return timeString + " Duelo terminou. Vencedor: " + 
                               (team2.size() == 1 ? team2.get(0) : "Equipe 2");
                    }
                }
                
            case PLAYER_KILL:
                String killed = event.getPlayer1();
                String killer = event.getPlayer2();
                if (killer != null) {
                    return timeString + " " + killed + " foi eliminado por " + killer;
                } else {
                    return timeString + " " + killed + " morreu";
                }
                
            case SIGNIFICANT_DAMAGE:
                String damaged = event.getPlayer1();
                String damager = event.getPlayer2();
                // Mostrar a quantidade de dano, se disponível
                String damageAmount = event.getDamageAmount() > 0 ? " (" + event.getDamageAmount() + " de dano)" : "";
                return timeString + " " + damaged + " recebeu dano significativo de " + damager + damageAmount;
                
            default:
                return timeString + " Evento desconhecido";
        }
    }
    
    /**
     * Retorna o total de eliminações no duelo
     */
    private int getTotalKills() {
        int total = 0;
        for (Integer kills : playerKills.values()) {
            total += kills;
        }
        return total;
    }
    
    /**
     * Representa um evento durante o duelo
     */
    private class DuelEvent {
        private EventType type;
        private String player1; // Jogador principal do evento (eliminado, danificado, etc)
        private String player2; // Jogador secundário do evento (matador, danificador, etc)
        private Location location; // Local do evento
        private List<String> team; // Equipe relacionada ao evento (vencedores, etc)
        private long timestamp;
        private int winnerTeamNumber; // 0 = empate, 1 = equipe 1, 2 = equipe 2
        private int damageAmount; // Quantidade de dano para eventos de dano
        private String winnerPlayerName; // Nome específico do jogador vencedor (para 1v1)
        
        public DuelEvent(EventType type, String player1, String player2, Location location, List<String> team) {
            this(type, player1, player2, location, team, 0, 0, null);
        }
        
        public DuelEvent(EventType type, String player1, String player2, Location location, List<String> team, int winnerTeamNumber) {
            this(type, player1, player2, location, team, winnerTeamNumber, 0, null);
        }
        
        public DuelEvent(EventType type, String player1, String player2, Location location, List<String> team, int winnerTeamNumber, int damageAmount) {
            this(type, player1, player2, location, team, winnerTeamNumber, damageAmount, null);
        }
        
        public DuelEvent(EventType type, String player1, String player2, Location location, List<String> team, int winnerTeamNumber, String winnerPlayerName) {
            this(type, player1, player2, location, team, winnerTeamNumber, 0, winnerPlayerName);
        }
        
        public DuelEvent(EventType type, String player1, String player2, Location location, List<String> team, int winnerTeamNumber, int damageAmount, String winnerPlayerName) {
            this.type = type;
            this.player1 = player1;
            this.player2 = player2;
            this.location = location;
            this.team = team;
            this.timestamp = System.currentTimeMillis();
            this.winnerTeamNumber = winnerTeamNumber;
            this.damageAmount = damageAmount;
            this.winnerPlayerName = winnerPlayerName;
        }
        
        public EventType getType() {
            return type;
        }
        
        public String getPlayer1() {
            return player1;
        }
        
        public String getPlayer2() {
            return player2;
        }
        
        public Location getLocation() {
            return location;
        }
        
        public List<String> getTeam() {
            return team;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public int getWinnerTeamNumber() {
            return winnerTeamNumber;
        }
        
        public int getDamageAmount() {
            return damageAmount;
        }
        
        public String getWinnerPlayerName() {
            return winnerPlayerName;
        }
    }
    
    /**
     * Tipos de eventos que podem ocorrer durante um duelo
     */
    private enum EventType {
        DUEL_START,
        DUEL_END,
        PLAYER_KILL,
        SIGNIFICANT_DAMAGE
    }
} 