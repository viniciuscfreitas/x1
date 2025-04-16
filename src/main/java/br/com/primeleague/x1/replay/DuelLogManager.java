package br.com.primeleague.x1.replay;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import br.com.primeleague.x1.Main;
import br.com.primeleague.x1.models.Duel;

/**
 * Gerenciador de logs de duelo para registro de eventos
 */
public class DuelLogManager {
    
    private final Main plugin;
    private final Map<UUID, DuelLog> activeLogs;
    private final File logsFolder;
    private Map<String, String> idMapping; // Mapeia IDs simples para UUIDs
    private int nextId = 1; // Próximo ID numérico a ser atribuído
    private final File idMappingFile;
    
    /**
     * Construtor para o gerenciador de logs
     * 
     * @param plugin Instância do plugin
     */
    public DuelLogManager(Main plugin) {
        this.plugin = plugin;
        this.activeLogs = new HashMap<>();
        this.idMapping = new HashMap<>();
        
        // Criar pasta para logs
        this.logsFolder = new File(plugin.getDataFolder(), "duellogs");
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }
        
        // Arquivo para armazenar o mapeamento de IDs
        this.idMappingFile = new File(plugin.getDataFolder(), "duellog_ids.properties");
        
        // Carregar mapeamento de IDs existente
        loadIdMapping();
    }
    
    /**
     * Carrega o mapeamento de IDs do arquivo
     */
    private void loadIdMapping() {
        if (idMappingFile.exists()) {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(idMappingFile)) {
                props.load(fis);
                
                // Encontrar o maior ID para definir o próximo
                for (Object key : props.keySet()) {
                    String idStr = (String) key;
                    try {
                        int id = Integer.parseInt(idStr);
                        if (id >= nextId) {
                            nextId = id + 1;
                        }
                    } catch (NumberFormatException e) {
                        // Ignorar entrada inválida
                    }
                }
                
                // Carregar todos os mapeamentos
                for (Object key : props.keySet()) {
                    String idStr = (String) key;
                    String uuidStr = props.getProperty(idStr);
                    idMapping.put(idStr, uuidStr);
                }
                
                plugin.getLogger().info("Carregados " + idMapping.size() + " IDs de duelos");
            } catch (IOException e) {
                plugin.getLogger().warning("Erro ao carregar mapeamento de IDs: " + e.getMessage());
            }
        }
    }
    
    /**
     * Salva o mapeamento de IDs para o arquivo
     */
    private void saveIdMapping() {
        Properties props = new Properties();
        
        for (Map.Entry<String, String> entry : idMapping.entrySet()) {
            props.setProperty(entry.getKey(), entry.getValue());
        }
        
        try (FileOutputStream fos = new FileOutputStream(idMappingFile)) {
            props.store(fos, "Mapeamento de IDs simples para UUIDs de duelos");
        } catch (IOException e) {
            plugin.getLogger().warning("Erro ao salvar mapeamento de IDs: " + e.getMessage());
        }
    }
    
    /**
     * Gera um novo ID simplificado para um UUID
     * 
     * @param uuid UUID para mapear
     * @return ID simplificado
     */
    private String generateSimpleId(UUID uuid) {
        String simpleId = String.valueOf(nextId++);
        idMapping.put(simpleId, uuid.toString());
        saveIdMapping();
        return simpleId;
    }
    
    /**
     * Converte um ID simples para o UUID correspondente
     * 
     * @param simpleId ID simplificado
     * @return UUID correspondente ou null se não encontrado
     */
    public UUID getUUIDFromSimpleId(String simpleId) {
        String uuidStr = idMapping.get(simpleId);
        if (uuidStr != null) {
            try {
                return UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("UUID inválido no mapeamento: " + uuidStr);
            }
        }
        return null;
    }
    
    /**
     * Obtém o ID simplificado para um UUID
     * 
     * @param uuid UUID para procurar
     * @return ID simplificado ou null se não encontrado
     */
    public String getSimpleIdFromUUID(UUID uuid) {
        String uuidStr = uuid.toString();
        for (Map.Entry<String, String> entry : idMapping.entrySet()) {
            if (entry.getValue().equals(uuidStr)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * Inicia o registro de um duelo
     * 
     * @param duel Duelo para registrar
     * @return ID do log criado
     */
    public UUID startLogging(Duel duel) {
        try {
            // Gerar UUID para o log
            UUID logId = UUID.randomUUID();
            System.out.println("[PrimeLeagueX1] Gerando UUID para novo log: " + logId.toString());
            
            // Criar novo log com o UUID gerado
            DuelLog log = new DuelLog(duel, logId);
            
            // Registrar evento de início
            log.logDuelStart();
            
            // Armazenar log ativo
            activeLogs.put(logId, log);
            
            // Gerar e armazenar ID simplificado
            String simpleId = generateSimpleId(logId);
            
            plugin.getLogger().info("Iniciado registro de duelo com ID: " + simpleId + " (UUID: " + logId + ")");
            return logId;
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao iniciar registro de duelo: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Finaliza o registro de um duelo
     * 
     * @param logId ID do log
     * @param winnerTeam Lista de jogadores vencedores
     * @return true se o registro foi finalizado com sucesso
     */
    public boolean stopLogging(UUID logId, List<String> winnerTeam) {
        try {
            System.out.println("[PrimeLeagueX1] Tentando finalizar o log de duelo: " + logId);
            
            DuelLog log = activeLogs.get(logId);
            if (log == null) {
                plugin.getLogger().warning("Tentativa de finalizar um log inexistente: " + logId);
                return false;
            }
            
            // Registrar evento de finalização
            log.logDuelEnd(winnerTeam);
            
            // Obter ID simplificado
            String simpleId = getSimpleIdFromUUID(logId);
            if (simpleId == null) {
                simpleId = generateSimpleId(logId);
            }
            
            System.out.println("[PrimeLeagueX1] Salvando arquivo de replay para o duelo ID: " + simpleId);
            System.out.println("[PrimeLeagueX1] UUID do logId: " + logId.toString());
            
            // Salvar arquivo de log (usando o UUID como nome do arquivo)
            boolean success = log.saveToFile(logsFolder);
            
            if (success) {
                plugin.getLogger().info("Registro de duelo salvo com sucesso. ID: " + simpleId + " (UUID: " + logId + ")");
            } else {
                plugin.getLogger().warning("Falha ao salvar o arquivo de registro do duelo. ID: " + simpleId + " (UUID: " + logId + ")");
            }
            
            // Remover dos logs ativos
            activeLogs.remove(logId);
            
            return success;
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao finalizar registro de duelo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Registra eliminação em um duelo
     * 
     * @param logId ID do log
     * @param killed Jogador eliminado
     * @param killer Jogador que eliminou
     */
    public void logKill(UUID logId, Player killed, Player killer) {
        try {
            DuelLog log = activeLogs.get(logId);
            if (log != null) {
                log.logKill(killed, killer);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao registrar eliminação: " + e.getMessage());
        }
    }
    
    /**
     * Registra dano significativo em um duelo
     * 
     * @param logId ID do log
     * @param damaged Jogador que recebeu dano
     * @param damager Jogador que causou dano
     * @param damage Quantidade de dano
     */
    public void logDamage(UUID logId, Player damaged, Player damager, double damage) {
        try {
            DuelLog log = activeLogs.get(logId);
            if (log != null) {
                log.logDamage(damaged, damager, damage);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao registrar dano: " + e.getMessage());
        }
    }
    
    /**
     * Verifica se existe um log ativo para o ID especificado
     * 
     * @param logId ID do log
     * @return true se existe log ativo
     */
    public boolean hasActiveLog(UUID logId) {
        return activeLogs.containsKey(logId);
    }
    
    /**
     * Lista os arquivos de log disponíveis
     * 
     * @return Array de arquivos de log
     */
    public File[] getLogFiles() {
        return logsFolder.listFiles(file -> file.isFile() && file.getName().endsWith(".duellog"));
    }
    
    /**
     * Obtém o arquivo de log para um ID simplificado
     * 
     * @param simpleId ID simplificado
     * @return Arquivo de log ou null se não encontrado
     */
    public File getLogFileFromSimpleId(String simpleId) {
        UUID uuid = getUUIDFromSimpleId(simpleId);
        if (uuid != null) {
            File logFile = new File(logsFolder, uuid.toString() + ".duellog");
            if (logFile.exists()) {
                return logFile;
            }
        }
        return null;
    }
    
    /**
     * Limpa logs antigos (mais de 30 dias)
     * 
     * @return Número de logs removidos
     */
    public int cleanupOldLogs() {
        int count = 0;
        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24L * 60L * 60L * 1000L);
        
        File[] logFiles = logsFolder.listFiles();
        if (logFiles != null) {
            for (File file : logFiles) {
                if (file.lastModified() < thirtyDaysAgo) {
                    // Remover do mapeamento de IDs
                    String uuidStr = file.getName().replace(".duellog", "");
                    String simpleId = null;
                    
                    for (Map.Entry<String, String> entry : idMapping.entrySet()) {
                        if (entry.getValue().equals(uuidStr)) {
                            simpleId = entry.getKey();
                            break;
                        }
                    }
                    
                    if (simpleId != null) {
                        idMapping.remove(simpleId);
                    }
                    
                    if (file.delete()) {
                        count++;
                    }
                }
            }
            
            // Salvar mapeamento atualizado
            if (count > 0) {
                saveIdMapping();
            }
        }
        
        return count;
    }
    
    /**
     * Finaliza todos os logs ativos
     * Chamado quando o plugin é desativado para garantir que todos os logs sejam salvos
     */
    public void finishAllLogs() {
        for (UUID logId : new ArrayList<>(activeLogs.keySet())) {
            try {
                plugin.getLogger().info("Finalizando log não concluído: " + logId);
                // Finalizar como empate (sem vencedor)
                boolean saved = stopLogging(logId, null);
                if (saved) {
                    plugin.getLogger().info("Log finalizado com sucesso: " + logId);
                } else {
                    plugin.getLogger().warning("Falha ao finalizar log: " + logId);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Erro ao finalizar log " + logId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Obtém a pasta onde os logs são armazenados
     * 
     * @return Pasta de logs
     */
    public File getLogsFolder() {
        return logsFolder;
    }
} 