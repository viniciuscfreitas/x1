package br.com.primeleague.x1.rival;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import br.com.primeleague.x1.Main;
import br.com.primeleague.x1.utils.ColorUtils;
import br.com.primeleague.x1.utils.EffectsUtils;

/**
 * Gerenciador de rivalidades entre jogadores
 */
public class RivalManager {
    
    private final Main plugin;
    private final Map<String, RivalData> rivalries;
    private File rivalFile;
    private FileConfiguration rivalConfig;
    
    /**
     * Construtor da classe
     *
     * @param plugin Instância do plugin principal
     */
    public RivalManager(Main plugin) {
        this.plugin = plugin;
        this.rivalries = new HashMap<String, RivalData>();
        loadData();
    }
    
    /**
     * Carrega os dados de rivalidades do arquivo
     */
    private void loadData() {
        try {
            // Criar pasta do plugin se não existir
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            
            // Criar arquivo de rivalidades
            rivalFile = new File(plugin.getDataFolder(), "rivalidades.yml");
            if (!rivalFile.exists()) {
                rivalFile.createNewFile();
            }
            
            // Carregar configuração
            rivalConfig = YamlConfiguration.loadConfiguration(rivalFile);
            
            // Carregar rivalidades
            ConfigurationSection rivalsSection = rivalConfig.getConfigurationSection("rivalidades");
            if (rivalsSection != null) {
                for (String key : rivalsSection.getKeys(false)) {
                    try {
                        ConfigurationSection rivalSection = rivalsSection.getConfigurationSection(key);
                        if (rivalSection != null) {
                            String[] players = key.split("_");
                            if (players.length == 2) {
                                RivalData rival = new RivalData(players[0], players[1]);
                                
                                // Carregar vitórias
                                rival.addVictory(rivalSection.getString("vitorias_" + players[0], "0"));
                                rival.addVictory(rivalSection.getString("vitorias_" + players[1], "0"));
                                
                                // Carregar últimos duelos
                                List<String> ultimosDuelos = rivalSection.getStringList("ultimos_duelos");
                                for (String duelo : ultimosDuelos) {
                                    if (duelo.startsWith("vencedor: ")) {
                                        rival.addVictory(duelo.substring(10));
                                    }
                                }
                                
                                rivalries.put(key, rival);
                            }
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Erro ao carregar rivalidade " + key + ": " + e.getMessage());
                    }
                }
            }
            
            plugin.getLogger().info("Rivalidades carregadas com sucesso. Total: " + rivalries.size());
            
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao carregar arquivo de rivalidades: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Salva os dados de rivalidades no arquivo
     */
    public void saveData() {
        try {
            // Limpar configuração anterior
            rivalConfig = new YamlConfiguration();
            
            // Criar seção de rivalidades
            ConfigurationSection rivalsSection = rivalConfig.createSection("rivalidades");
            
            // Flag para verificar se houve mudanças
            boolean hasChanges = false;
            
            // Salvar cada rivalidade
            for (Map.Entry<String, RivalData> entry : rivalries.entrySet()) {
                String key = entry.getKey();
                RivalData rival = entry.getValue();
                
                ConfigurationSection rivalSection = rivalsSection.createSection(key);
                
                // Salvar vitórias
                String vitorias1 = rivalSection.getString("vitorias_" + rival.getPlayer1(), "0");
                String vitorias2 = rivalSection.getString("vitorias_" + rival.getPlayer2(), "0");
                
                if (!vitorias1.equals(String.valueOf(rival.getPlayer1Victories()))) {
                    rivalSection.set("vitorias_" + rival.getPlayer1(), rival.getPlayer1Victories());
                    hasChanges = true;
                }
                
                if (!vitorias2.equals(String.valueOf(rival.getPlayer2Victories()))) {
                    rivalSection.set("vitorias_" + rival.getPlayer2(), rival.getPlayer2Victories());
                    hasChanges = true;
                }
                
                // Salvar últimos duelos
                List<String> ultimosDuelos = rivalSection.getStringList("ultimos_duelos");
                List<String> novosDuelos = new ArrayList<>();
                for (String vencedor : rival.getLastDuels()) {
                    novosDuelos.add("vencedor: " + vencedor);
                }
                
                if (!ultimosDuelos.equals(novosDuelos)) {
                    rivalSection.set("ultimos_duelos", novosDuelos);
                    hasChanges = true;
                }
            }
            
            // Salvar apenas se houver mudanças
            if (hasChanges) {
                rivalConfig.save(rivalFile);
                plugin.getLogger().info("Dados de rivalidades salvos com sucesso. Total: " + rivalries.size());
            }
            
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Erro ao salvar arquivo de rivalidades", e);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Erro desconhecido ao salvar rivalidades", e);
        }
    }
    
    /**
     * Atualiza a relação entre dois jogadores após um duelo
     *
     * @param player1 Nome do primeiro jogador
     * @param player2 Nome do segundo jogador
     * @param winner Nome do jogador vencedor
     * @return true se a atualização ativou uma rivalidade
     */
    public boolean updateRivalry(String player1, String player2, String winner) {
        // Garantir que a ordem dos jogadores seja consistente
        String key;
        if (player1.compareTo(player2) <= 0) {
            key = player1 + "_" + player2;
        } else {
            key = player2 + "_" + player1;
        }
        
        // Obter ou criar rivalidade
        RivalData rival = rivalries.get(key);
        if (rival == null) {
            rival = new RivalData(player1, player2);
            rivalries.put(key, rival);
        }
        
        // Verificar se havia rivalidade antes da atualização
        boolean hadRivalry = isRivalry(player1, player2);
        
        // Atualizar vitórias
        rival.addVictory(winner);
        
        // Salvar dados
        saveData();
        
        // Verificar se agora existe uma rivalidade
        boolean hasRivalry = isRivalry(player1, player2);
        
        // Retornar true se a rivalidade foi ativada agora
        return !hadRivalry && hasRivalry;
    }
    
    /**
     * Verifica se existe uma rivalidade entre dois jogadores
     *
     * @param player1 Nome do primeiro jogador
     * @param player2 Nome do segundo jogador
     * @return true se existe uma rivalidade
     */
    public boolean isRivalry(String player1, String player2) {
        // Garantir que a ordem dos jogadores seja consistente
        String key;
        if (player1.compareTo(player2) <= 0) {
            key = player1 + "_" + player2;
        } else {
            key = player2 + "_" + player1;
        }
        
        // Verificar se existe rivalidade
        RivalData rival = rivalries.get(key);
        if (rival != null) {
            return rival.isRivalry(getMinDuelsForRivalry());
        }
        
        return false;
    }
    
    /**
     * Obtém os dados de rivalidade entre dois jogadores
     *
     * @param player1 Nome do primeiro jogador
     * @param player2 Nome do segundo jogador
     * @return Dados da rivalidade ou null se não existir
     */
    public RivalData getRivalData(String player1, String player2) {
        // Garantir que a ordem dos jogadores seja consistente
        String key;
        if (player1.compareTo(player2) <= 0) {
            key = player1 + "_" + player2;
        } else {
            key = player2 + "_" + player1;
        }
        
        return rivalries.get(key);
    }
    
    /**
     * Obtém todas as rivalidades de um jogador
     *
     * @param playerName Nome do jogador
     * @return Lista de dados de rivalidade
     */
    public List<RivalData> getPlayerRivalries(String playerName) {
        List<RivalData> result = new ArrayList<RivalData>();
        
        for (RivalData rival : rivalries.values()) {
            if (rival.hasPlayer(playerName) && rival.isRivalry(getMinDuelsForRivalry())) {
                result.add(rival);
            }
        }
        
        return result;
    }
    
    /**
     * Obtém o número mínimo de duelos para considerar uma rivalidade
     *
     * @return Número mínimo de duelos
     */
    public int getMinDuelsForRivalry() {
        return plugin.getConfigManager().getConfig().getInt("rivalidade.duelos_para_ativar", 5);
    }
    
    /**
     * Exibe uma mensagem de rivalidade para os dois jogadores
     * 
     * @param player1 Primeiro jogador
     * @param player2 Segundo jogador
     * @param messageKey Chave da mensagem no arquivo de mensagens
     * @param placeholders Placeholders adicionais
     */
    public void showRivalMessage(Player player1, Player player2, String messageKey, Object... placeholders) {
        if (player1 != null && player1.isOnline()) {
            plugin.getMessageManager().sendMessage(player1, messageKey, placeholders);
        }
        
        if (player2 != null && player2.isOnline()) {
            plugin.getMessageManager().sendMessage(player2, messageKey, placeholders);
        }
    }
    
    /**
     * Envia uma mensagem de rivalidade para dois jogadores
     * 
     * @param p1 Primeiro jogador
     * @param p2 Segundo jogador
     * @param message Mensagem a ser enviada
     */
    public void sendRivalMessage(Player p1, Player p2, String message) {
        if (p1 != null && p1.isOnline()) p1.sendMessage(message);
        if (p2 != null && p2.isOnline()) p2.sendMessage(message);
    }
    
    /**
     * Exibe uma mensagem de placar de rivalidade para um jogador
     * 
     * @param player Jogador para exibir a mensagem
     * @param opponent Nome do oponente
     */
    public void sendRivalScoreMessage(Player player, String opponent) {
        RivalData rival = getRivalData(player.getName(), opponent);
        if (rival != null && rival.isRivalry(getMinDuelsForRivalry())) {
            int p1victories = rival.getVictories(player.getName());
            int p2victories = rival.getVictories(opponent);
            
            String message = plugin.getMessageManager().getMessage("rival.placar");
            message = message.replace("{vitorias1}", String.valueOf(p1victories))
                             .replace("{vitorias2}", String.valueOf(p2victories));
            
            player.sendMessage(ColorUtils.colorize(message));
        }
    }
    
    /**
     * Exibe título e mensagem de rivalidade após um duelo
     * 
     * @param player1 Primeiro jogador
     * @param player2 Segundo jogador
     * @param winner Nome do jogador vencedor
     */
    public void showRivalDuelEndMessage(Player player1, Player player2, String winner) {
        RivalData rival = getRivalData(player1.getName(), player2.getName());
        if (rival == null || !rival.isRivalry(getMinDuelsForRivalry())) {
            return;
        }
        
        // Verificar se o duelo anterior teve o mesmo vencedor
        String lastWinner = rival.getLastDuelWinner();
        String messageKey;
        
        // O jogador acabou de vencer após ter perdido?
        if (lastWinner != null && !lastWinner.equals(winner)) {
            messageKey = "revanche";
        } else {
            messageKey = "mantida";
        }
        
        // Verificar se o título está habilitado
        boolean showTitle = plugin.getConfigManager().getConfig().getBoolean("rivalidade.titulo_fim", true);
        
        // Exibir título
        if (showTitle) {
            String titleText = plugin.getMessageManager().getMessage("rival." + messageKey);
            titleText = titleText.replace("{vencedor}", winner);
            
            if (player1 != null && player1.isOnline()) {
                EffectsUtils.sendTitle(player1, ChatColor.GOLD + "Rivalidade", 
                        ColorUtils.colorize(titleText), 10, 60, 10);
            }
            
            if (player2 != null && player2.isOnline()) {
                EffectsUtils.sendTitle(player2, ChatColor.GOLD + "Rivalidade", 
                        ColorUtils.colorize(titleText), 10, 60, 10);
            }
        }
        
        // Exibir mensagem no chat
        if (player1 != null && player1.isOnline() && player2 != null && player2.isOnline()) {
            showRivalMessage(player1, player2, messageKey, "{vencedor}", winner);
            
            // Exibir placar atual
            String player1Name = player1.getName();
            String player2Name = player2.getName();
            int p1victories = rival.getVictories(player1Name);
            int p2victories = rival.getVictories(player2Name);
            
            String placarMessage = plugin.getMessageManager().getMessage("rival.placar");
            placarMessage = placarMessage.replace("{vitorias1}", String.valueOf(p1victories))
                                        .replace("{vitorias2}", String.valueOf(p2victories));
            
            player1.sendMessage(ColorUtils.colorize(placarMessage));
            player2.sendMessage(ColorUtils.colorize(placarMessage));
        }
    }
    
    /**
     * Verifica se deve fazer broadcast quando dois rivais se enfrentam
     * 
     * @return true se deve fazer broadcast
     */
    public boolean shouldBroadcastRivals() {
        return plugin.getConfigManager().getConfig().getBoolean("rivalidade.broadcast", false);
    }
    
    /**
     * Anuncia um duelo entre rivais para todo o servidor
     * 
     * @param player1 Nome do primeiro jogador
     * @param player2 Nome do segundo jogador
     */
    public void broadcastRivalDuel(String player1, String player2) {
        if (!plugin.getConfigManager().getConfig().getBoolean("rivalidade.anunciar_duelos", true)) {
            return;
        }
        
        // Verificar se existe uma rivalidade
        if (isRivalry(player1, player2)) {
            RivalData rivalData = getRivalData(player1, player2);
            
            if (rivalData != null) {
                // Obter número de vitórias de cada jogador
                int vitorias1 = rivalData.getVictories(player1);
                int vitorias2 = rivalData.getVictories(player2);
                
                // Anunciar para o servidor
                String message = plugin.getMessageManager().getMessage("rival.broadcast")
                    .replace("{player1}", player1)
                    .replace("{player2}", player2)
                    .replace("{vitorias1}", String.valueOf(vitorias1))
                    .replace("{vitorias2}", String.valueOf(vitorias2));
                
                plugin.getServer().broadcastMessage(ColorUtils.colorize(message));
                
                // Tocar som para jogadores online
                Player p1 = Bukkit.getPlayerExact(player1);
                Player p2 = Bukkit.getPlayerExact(player2);
                
                if (p1 != null && p1.isOnline()) {
                    EffectsUtils.playSoundSpecial(p1);
                }
                
                if (p2 != null && p2.isOnline()) {
                    EffectsUtils.playSoundSpecial(p2);
                }
            }
        }
    }
} 