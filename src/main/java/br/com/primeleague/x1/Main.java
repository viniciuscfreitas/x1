package br.com.primeleague.x1;

import br.com.primeleague.x1.commands.X1Command;
import br.com.primeleague.x1.gui.GUIManager;
import br.com.primeleague.x1.integration.WorldGuardHook;
import br.com.primeleague.x1.lang.MessageValidator;
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
import br.com.primeleague.x1.managers.TeamTagManager;
import br.com.primeleague.x1.replay.DuelLogManager;
import br.com.primeleague.x1.storage.FileStorage;
import br.com.primeleague.x1.utils.ColorUtils;
import br.com.primeleague.x1.utils.NametagManager;
import br.com.primeleague.x1.listeners.ChatListener;
import br.com.primeleague.x1.managers.X1ScoreboardManager;
import br.com.primeleague.x1.rival.RivalManager;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

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
    private TeamTagManager teamTagManager;
    private DuelLogManager duelLogManager;
    private RivalManager rivalManager;
    
    // Hooks de integração
    private WorldGuardHook worldGuardHook;
    
    // Utilitários
    private NametagManager nametagManager;
    
    // Storage
    private FileStorage fileStorage;
    
    @Override
    public void onEnable() {
        // Definir instância
        instance = this;
        
        // Verificar compatibilidade com servidor
        checkServerCompatibility();
        
        // Criar pasta de dados se não existir
        try {
            if (!getDataFolder().exists()) {
                System.out.println("[PrimeLeagueX1] Criando pasta de dados...");
                if (getDataFolder().mkdirs()) {
                    System.out.println("[PrimeLeagueX1] Pasta de dados criada com sucesso!");
                } else {
                    System.out.println("[PrimeLeagueX1] ERRO: Não foi possível criar a pasta de dados!");
                }
            }
            
            // Verificar permissões da pasta de dados
            if (!getDataFolder().canWrite()) {
                System.out.println("[PrimeLeagueX1] ERRO: A pasta de dados não tem permissão de escrita!");
            } else {
                System.out.println("[PrimeLeagueX1] Pasta de dados está com permissões corretas.");
            }
        } catch (Exception e) {
            System.out.println("[PrimeLeagueX1] Erro ao verificar pasta de dados: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            // Verificar e atualizar arquivos YAML de recursos
            System.out.println("[PrimeLeagueX1] Verificando arquivos de recursos...");
            File configFile = new File(getDataFolder(), "config.yml");
            File messagesFile = new File(getDataFolder(), "messages.yml");
            
            // Verificar e atualizar config.yml se necessário
            br.com.primeleague.x1.utils.FileUtils.saveResourceIfDifferent(this, "config.yml", configFile, false);
            
            // Verificar e atualizar messages.yml se necessário
            br.com.primeleague.x1.utils.FileUtils.saveResourceIfDifferent(this, "messages.yml", messagesFile, false);
            
            // Inicializar managers
            System.out.println("[PrimeLeagueX1] Inicializando ConfigManager...");
            configManager = new ConfigManager(this);
            
            System.out.println("[PrimeLeagueX1] Inicializando MessageManager...");
            messageManager = new MessageManager(this);
            
            // Validar todas as mensagens usando o sistema de validação
            MessageValidator.validarMensagensEssenciais(messageManager);
            
            // Verificar se as mensagens foram carregadas corretamente
            if (messageManager.hasMessage("prefixo") && messageManager.hasMessage("geral.plugin-ativado")) {
                System.out.println("[PrimeLeagueX1] Mensagens básicas carregadas com sucesso!");
            } else {
                System.out.println("[PrimeLeagueX1] AVISO: Algumas mensagens básicas não foram encontradas!");
                System.out.println("[PrimeLeagueX1] O plugin pode não funcionar corretamente. Verifique o arquivo messages.yml.");
            }
            
            System.out.println("[PrimeLeagueX1] Inicializando outros componentes...");
            fileStorage = new FileStorage(this);
            arenaManager = new ArenaManager(this);
            statsManager = new StatsManager(this);
            teamManager = new TeamManager(this);
            economyManager = new EconomyManager(this);
            
            // Inicializar hooks e utilitários
            worldGuardHook = new WorldGuardHook(this);
            nametagManager = new NametagManager(this);
            
            // Inicializar utilitários
            br.com.primeleague.x1.utils.EffectsUtils.initialize(this);
            
            // Inicializar managers que dependem dos hooks
            duelManager = new DuelManager(this);
            guiManager = new GUIManager(this);
            teamTagManager = new TeamTagManager(this);
            
            // Inicializar gerenciador de logs de duelo
            if (configManager.getConfig().getBoolean("registro-duelos.ativar", true)) {
                duelLogManager = new DuelLogManager(this);
                getLogger().info("Sistema de registro de duelos ativado");
            } else {
                getLogger().info("Sistema de registro de duelos desativado");
            }
            
            // Inicializar gerenciador de rivalidades
            rivalManager = new RivalManager(this);
            getLogger().info("Sistema de rivalidades entre jogadores ativado");
            
            // Inicializar gerenciador de scoreboards
            X1ScoreboardManager.initialize(this);
            
            // Registrar comandos
            registerCommands();
            
            // Registrar listeners
            registerListeners();
            
            // Mensagem de ativação
            String activationMessage = messageManager.getMessageWithPrefix("geral.plugin-ativado");
            Bukkit.getConsoleSender().sendMessage(ColorUtils.colorize(activationMessage));
            
        } catch (Exception e) {
            getLogger().severe("Erro ao inicializar o plugin: " + e.getMessage());
            e.printStackTrace();
            getLogger().severe("O plugin pode não funcionar corretamente devido a um erro de inicialização.");
        }
    }
    
    @Override
    public void onDisable() {
        // Cancelar todos os duelos ativos
        if (duelManager != null) {
            duelManager.cancelAllDuels();
        }
        
        // Salvar dados
        System.out.println("[PrimeLeagueX1] Salvando estatísticas e histórico de duelos...");
        fileStorage.saveAll();
        
        // Finalizar logs de duelos pendentes
        if (duelLogManager != null) {
            System.out.println("[PrimeLeagueX1] Finalizando logs de duelos pendentes...");
            duelLogManager.finishAllLogs();
        }
        
        // Salvar dados de rivalidades
        if (rivalManager != null) {
            System.out.println("[PrimeLeagueX1] Salvando dados de rivalidades...");
            rivalManager.saveData();
        }
        
        // Limpar jogadores com PvP ativado
        if (worldGuardHook != null) {
            worldGuardHook.clearPvPPlayers();
        }
        
        // Limpar nametags
        if (nametagManager != null) {
            nametagManager.clearAllTags();
        }
        
        // Desabilitar scoreboards
        X1ScoreboardManager.desabilitar();
        
        // Mensagem de desativação
        Bukkit.getConsoleSender().sendMessage(ColorUtils.colorize(messageManager.getPrefix() + messageManager.getMessage("geral.plugin-desativado")));
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
        pm.registerEvents(new ChatListener(this), this);
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
     * Obtém o gerenciador de GUI
     * 
     * @return Gerenciador de GUI
     */
    public GUIManager getGUIManager() {
        return guiManager;
    }
    
    /**
     * Obtém o hook do WorldGuard
     * 
     * @return Hook do WorldGuard
     */
    public WorldGuardHook getWorldGuardHook() {
        return worldGuardHook;
    }
    
    /**
     * Obtém o gerenciador de nametags
     * 
     * @return Gerenciador de nametags
     */
    public NametagManager getNametagManager() {
        return nametagManager;
    }
    
    /**
     * Obtém o armazenamento de arquivos
     * 
     * @return Armazenamento de arquivos
     */
    public FileStorage getFileStorage() {
        return fileStorage;
    }
    
    /**
     * Obtém o gerenciador de tags de equipe
     * 
     * @return Gerenciador de tags de equipe
     */
    public TeamTagManager getTeamTagManager() {
        return teamTagManager;
    }
    
    /**
     * Obtém o gerenciador de logs de duelo
     * 
     * @return Gerenciador de logs de duelo
     */
    public DuelLogManager getDuelLogManager() {
        return duelLogManager;
    }
    
    /**
     * Obtém o gerenciador de rivalidades
     * 
     * @return Gerenciador de rivalidades
     */
    public RivalManager getRivalManager() {
        return rivalManager;
    }
    
    /**
     * Verifica se o sistema de registro de duelos está ativado
     * 
     * @return true se o sistema está ativado
     */
    public boolean isDuelLoggingEnabled() {
        return duelLogManager != null && configManager.getConfig().getBoolean("registro-duelos.ativar", true);
    }
    
    /**
     * Verifica a compatibilidade do servidor com o plugin
     * Garante que o plugin funcione corretamente no ambiente especificado
     */
    private void checkServerCompatibility() {
        try {
            // Verificar versão do servidor
            String version = Bukkit.getBukkitVersion();
            getLogger().info("Detectada versão do servidor: " + version);
            
            if (!version.startsWith("1.5.2")) {
                getLogger().warning("Este plugin foi projetado para Bukkit/Spigot 1.5.2!");
                getLogger().warning("Versão atual detectada: " + version);
                getLogger().warning("Podem ocorrer erros de compatibilidade!");
            }
            
            // Verificar existência das APIs necessárias
            try {
                Class.forName("org.bukkit.event.entity.EntityDamageByEntityEvent");
                Class.forName("org.bukkit.inventory.ItemStack");
            } catch (ClassNotFoundException e) {
                getLogger().severe("API Bukkit necessária não encontrada: " + e.getMessage());
                getLogger().severe("O plugin pode não funcionar corretamente!");
            }
        } catch (Exception e) {
            getLogger().severe("Erro ao verificar compatibilidade: " + e.getMessage());
        }
    }
} 