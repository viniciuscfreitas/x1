package br.com.primeleague.x1;

import br.com.primeleague.x1.commands.X1Command;
import br.com.primeleague.x1.gui.GUIManager;
import br.com.primeleague.x1.listeners.DuelListener;
import br.com.primeleague.x1.listeners.GUIListener;
import br.com.primeleague.x1.listeners.PlayerListener;
import br.com.primeleague.x1.managers.ArenaManager;
import br.com.primeleague.x1.managers.ConfigManager;
import br.com.primeleague.x1.managers.DuelManager;
import br.com.primeleague.x1.managers.EconomyManager;
import br.com.primeleague.x1.managers.MessageManager;
import br.com.primeleague.x1.managers.StatsManager;
import br.com.primeleague.x1.managers.TeamManager;
import br.com.primeleague.x1.storage.FileStorage;
import br.com.primeleague.x1.utils.ColorUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Classe principal do plugin PrimeLeagueX1
 * Sistema de duelos PvP (X1, 2v2, 3v3) completo baseado em GUI
 * 
 * @author PrimeLeague
 * @version 1.0.0
 */
public class Main extends JavaPlugin {
    
    private static Main instance;
    
    // Managers
    private ConfigManager configManager;
    private MessageManager messageManager;
    private DuelManager duelManager;
    private ArenaManager arenaManager;
    private StatsManager statsManager;
    private TeamManager teamManager;
    private EconomyManager economyManager;
    private GUIManager guiManager;
    
    // Storage
    private FileStorage fileStorage;
    
    @Override
    public void onEnable() {
        // Definir instância
        instance = this;
        
        // Inicializar managers
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        fileStorage = new FileStorage(this);
        arenaManager = new ArenaManager(this);
        statsManager = new StatsManager(this);
        teamManager = new TeamManager(this);
        economyManager = new EconomyManager(this);
        duelManager = new DuelManager(this);
        guiManager = new GUIManager(this);
        
        // Registrar comandos
        registerCommands();
        
        // Registrar listeners
        registerListeners();
        
        // Mensagem de ativação
        Bukkit.getConsoleSender().sendMessage(ColorUtils.colorize(messageManager.getPrefix() + messageManager.getMessage("geral.plugin-ativado")));
    }
    
    @Override
    public void onDisable() {
        // Cancelar todos os duelos ativos
        duelManager.cancelAllDuels();
        
        // Salvar dados
        fileStorage.saveAll();
        
        // Mensagem de desativação
        Bukkit.getConsoleSender().sendMessage(ColorUtils.colorize(messageManager.getPrefix() + messageManager.getMessage("geral.plugin-desativado")));
        
        // Limpar instância
        instance = null;
    }
    
    /**
     * Registra os comandos do plugin
     */
    private void registerCommands() {
        PluginCommand x1Command = getCommand("x1");
        if (x1Command != null) {
            x1Command.setExecutor(new X1Command(this));
        }
    }
    
    /**
     * Registra os listeners do plugin
     */
    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(new DuelListener(this), this);
        pm.registerEvents(new GUIListener(this), this);
    }
    
    /**
     * Obtém a instância do plugin
     * 
     * @return Instância do plugin
     */
    public static Main getInstance() {
        return instance;
    }
    
    /**
     * Obtém o gerenciador de configurações
     * 
     * @return Gerenciador de configurações
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Obtém o gerenciador de mensagens
     * 
     * @return Gerenciador de mensagens
     */
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    /**
     * Obtém o gerenciador de duelos
     * 
     * @return Gerenciador de duelos
     */
    public DuelManager getDuelManager() {
        return duelManager;
    }
    
    /**
     * Obtém o gerenciador de arenas
     * 
     * @return Gerenciador de arenas
     */
    public ArenaManager getArenaManager() {
        return arenaManager;
    }
    
    /**
     * Obtém o gerenciador de estatísticas
     * 
     * @return Gerenciador de estatísticas
     */
    public StatsManager getStatsManager() {
        return statsManager;
    }
    
    /**
     * Obtém o gerenciador de equipes
     * 
     * @return Gerenciador de equipes
     */
    public TeamManager getTeamManager() {
        return teamManager;
    }
    
    /**
     * Obtém o gerenciador de economia
     * 
     * @return Gerenciador de economia
     */
    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    
    /**
     * Obtém o gerenciador de GUIs
     * 
     * @return Gerenciador de GUIs
     */
    public GUIManager getGUIManager() {
        return guiManager;
    }
    
    /**
     * Obtém o armazenamento de arquivos
     * 
     * @return Armazenamento de arquivos
     */
    public FileStorage getFileStorage() {
        return fileStorage;
    }
} 