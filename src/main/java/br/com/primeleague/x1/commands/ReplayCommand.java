package br.com.primeleague.x1.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import br.com.primeleague.x1.Main;
import br.com.primeleague.x1.utils.ColorUtils;

/**
 * Comando para gerenciar os logs de duelo
 */
public class ReplayCommand {
    
    private final Main plugin;
    
    /**
     * Construtor
     * 
     * @param plugin Instância do plugin
     */
    public ReplayCommand(Main plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Processa um comando de replay
     * 
     * @param sender Remetente do comando
     * @param args Argumentos do comando
     * @return true se o comando foi processado corretamente
     */
    public boolean onCommand(CommandSender sender, String[] args) {
        // Verificar se o sistema de logs está ativado
        if (!plugin.isDuelLoggingEnabled()) {
            sender.sendMessage(ColorUtils.colorize("&cO sistema de registro de duelos está desativado!"));
            return true;
        }
        
        // Verificar permissão
        if (!sender.hasPermission("primeleague.x1.replay")) {
            sender.sendMessage(ColorUtils.colorize(plugin.getMessageManager().getMessage("geral.sem-permissao")));
            return true;
        }
        
        // Processar subcomando
        if (args.length < 2) {
            // Mostrar ajuda
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[1].toLowerCase();
        
        switch (subCommand) {
            case "list":
                listAllLogs(sender);
                break;
                
            case "ver":
                if (args.length < 3) {
                    sender.sendMessage(ColorUtils.colorize("&cUso correto: /x1 replay ver <id>"));
                    return true;
                }
                viewLog(sender, args[2]);
                break;
                
            case "limpar":
                cleanupLogs(sender);
                break;
                
            case "top":
                String metrica = args.length > 2 ? args[2].toLowerCase() : "dano";
                showTopReplays(sender, metrica);
                break;
                
            case "highlight":
                if (args.length < 3) {
                    sender.sendMessage(ColorUtils.colorize("&cUso correto: /x1 replay highlight <id>"));
                    return true;
                }
                showHighlights(sender, args[2]);
                break;
                
            case "share":
            case "compartilhar":
                if (args.length < 3) {
                    sender.sendMessage(ColorUtils.colorize("&cUso correto: /x1 replay share <id>"));
                    return true;
                }
                shareReplay(sender, args[2]);
                break;
                
            default:
                showHelp(sender);
                break;
        }
        
        return true;
    }
    
    /**
     * Mostra mensagem de ajuda
     * 
     * @param sender Remetente do comando
     */
    private void showHelp(CommandSender sender) {
        String prefix = plugin.getMessageManager().getPrefix();
        
        sender.sendMessage(ColorUtils.colorize(prefix + "&eComandos de registro de duelos:"));
        sender.sendMessage(ColorUtils.colorize(prefix + "&7/x1 replay list &f- Lista os duelos registrados"));
        sender.sendMessage(ColorUtils.colorize(prefix + "&7/x1 replay ver <id> &f- Exibe detalhes de um duelo registrado"));
        sender.sendMessage(ColorUtils.colorize(prefix + "&7/x1 replay limpar &f- Remove logs antigos"));
        sender.sendMessage(ColorUtils.colorize(prefix + "&7/x1 replay top [dano|kills|tempo] &f- Mostra top replays por métrica"));
        sender.sendMessage(ColorUtils.colorize(prefix + "&7/x1 replay highlight <id> &f- Mostra melhores momentos de um replay"));
        sender.sendMessage(ColorUtils.colorize(prefix + "&7/x1 replay share <id> &f- Compartilha um replay no chat"));
    }
    
    /**
     * Lista todos os logs de duelo disponíveis
     * 
     * @param sender Remetente do comando
     */
    private void listAllLogs(CommandSender sender) {
        String prefix = plugin.getMessageManager().getPrefix();
        
        // Obter arquivos de log
        File[] logFiles = plugin.getDuelLogManager().getLogFiles();
        
        if (logFiles == null || logFiles.length == 0) {
            sender.sendMessage(ColorUtils.colorize(prefix + plugin.getMessageManager().getMessage("duelo-log.nenhum-log")));
            System.out.println("[PrimeLeagueX1] Nenhum replay encontrado na pasta: " + 
                plugin.getDuelLogManager().getLogsFolder().getAbsolutePath());
            return;
        }
        
        // Verificar se a pasta de logs existe e tem permissão de leitura
        File logsFolder = plugin.getDuelLogManager().getLogsFolder();
        if (!logsFolder.exists()) {
            sender.sendMessage(ColorUtils.colorize(prefix + "A pasta de replays não existe!"));
            System.out.println("[PrimeLeagueX1] Pasta de replays não existe: " + logsFolder.getAbsolutePath());
            return;
        }
        
        if (!logsFolder.canRead()) {
            sender.sendMessage(ColorUtils.colorize(prefix + "Sem permissão para ler a pasta de replays!"));
            System.out.println("[PrimeLeagueX1] Sem permissão para ler pasta de replays: " + logsFolder.getAbsolutePath());
            return;
        }
        
        System.out.println("[PrimeLeagueX1] Encontrados " + logFiles.length + " arquivos de replay na pasta: " + 
            logsFolder.getAbsolutePath());
        
        // Enviar header
        sender.sendMessage(ColorUtils.colorize("&a=== Replays de Duelos Disponíveis ==="));
        
        // Criar lista formatada com IDs simples e data
        for (File file : logFiles) {
            String uuid = file.getName().replace(".duellog", "");
            String simpleId = plugin.getDuelLogManager().getSimpleIdFromUUID(UUID.fromString(uuid));
            
            if (simpleId == null) {
                System.out.println("[PrimeLeagueX1] UUID sem ID simples associado: " + uuid);
                continue; // Pular este arquivo
            }
            
            // Extrair informações básicas do arquivo de log
            String data = "";
            String tipo = "";
            List<String> time1 = new ArrayList<>();
            List<String> time2 = new ArrayList<>();
            boolean readingTeam1 = false;
            boolean readingTeam2 = false;
            
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Data:")) {
                        data = line.substring(5).trim();
                    } else if (line.startsWith("Tipo:")) {
                        tipo = line.substring(5).trim();
                    } else if (line.equals("Equipe 1:")) {
                        readingTeam1 = true;
                        readingTeam2 = false;
                    } else if (line.equals("Equipe 2:")) {
                        readingTeam1 = false;
                        readingTeam2 = true;
                    } else if (line.equals("Eventos:")) {
                        break; // Não precisamos ler os eventos aqui
                    } else if (line.startsWith("- ") && readingTeam1) {
                        time1.add(line.substring(2));
                    } else if (line.startsWith("- ") && readingTeam2) {
                        time2.add(line.substring(2));
                    }
                }
            } catch (IOException e) {
                System.out.println("[PrimeLeagueX1] Erro ao ler arquivo de replay: " + file.getName() + " - " + e.getMessage());
                continue; // Pular este arquivo
            }
            
            // Formatar mensagem com detalhes básicos
            StringBuilder sb = new StringBuilder();
            sb.append("&e[").append(simpleId).append("] &7");
            sb.append(data).append(" - ");
            
            if ("SOLO".equals(tipo)) {
                if (time1.size() > 0 && time2.size() > 0) {
                    sb.append(time1.get(0)).append(" vs ").append(time2.get(0));
                } else {
                    sb.append("Duelo 1v1");
                }
            } else {
                sb.append("Duelo em Equipe (").append(time1.size()).append("v").append(time2.size()).append(")");
            }
            
            sender.sendMessage(ColorUtils.colorize(sb.toString()));
        }
        
        // Enviar footer com comando para ver detalhes
        sender.sendMessage(ColorUtils.colorize("&7Use &f/x1 replay <ID> &7para ver detalhes de um replay"));
    }
    
    /**
     * Exibe detalhes de um log de duelo
     * 
     * @param sender Remetente do comando
     * @param simpleId ID simplificado do log
     */
    private void viewLog(CommandSender sender, String simpleId) {
        String prefix = plugin.getMessageManager().getPrefix();
        
        // Obter arquivo de log a partir do ID simplificado
        File logFile = plugin.getDuelLogManager().getLogFileFromSimpleId(simpleId);
        
        if (logFile == null || !logFile.exists()) {
            sender.sendMessage(ColorUtils.colorize(prefix + plugin.getMessageManager().getMessage("duelo-log.detalhes-nao-encontrado")));
            return;
        }
        
        // Ler informações detalhadas
        String data = "";
        String tipo = "";
        String duracao = "";
        List<String> time1 = new ArrayList<>();
        List<String> time2 = new ArrayList<>();
        List<String> eventos = new ArrayList<>();
        String vencedor = "Empate";
        boolean readingTeam1 = false;
        boolean readingTeam2 = false;
        boolean readingEvents = false;
        boolean readingDanoRecebido = false;
        boolean readingDanoCausado = false;
        boolean readingEliminacoes = false;
        int totalEventos = 0;
        int kills = 0;
        int significantDamages = 0;
        int danoTotal = 0;
        Map<String, Integer> danoCausado = new HashMap<>();
        Map<String, Integer> danoRecebido = new HashMap<>();
        Map<String, Integer> eliminacoes = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Duelo:")) {
                    // nada a fazer
                } else if (line.startsWith("Data:")) {
                    data = line.substring(5).trim();
                } else if (line.startsWith("Tipo:")) {
                    tipo = line.substring(5).trim();
                } else if (line.startsWith("Duração:")) {
                    duracao = line.substring(9).trim();
                } else if (line.equals("Equipe 1:")) {
                    readingTeam1 = true;
                    readingTeam2 = false;
                    readingEvents = false;
                    readingDanoRecebido = false;
                    readingDanoCausado = false;
                    readingEliminacoes = false;
                } else if (line.equals("Equipe 2:")) {
                    readingTeam1 = false;
                    readingTeam2 = true;
                    readingEvents = false;
                    readingDanoRecebido = false;
                    readingDanoCausado = false;
                    readingEliminacoes = false;
                } else if (line.equals("Dano Causado:")) {
                    readingTeam1 = false;
                    readingTeam2 = false;
                    readingEvents = false;
                    readingDanoRecebido = false;
                    readingDanoCausado = true;
                    readingEliminacoes = false;
                } else if (line.equals("Dano Recebido:")) {
                    readingTeam1 = false;
                    readingTeam2 = false;
                    readingEvents = false;
                    readingDanoRecebido = true;
                    readingDanoCausado = false;
                    readingEliminacoes = false;
                } else if (line.equals("Eliminações:")) {
                    readingTeam1 = false;
                    readingTeam2 = false;
                    readingEvents = false;
                    readingDanoRecebido = false;
                    readingDanoCausado = false;
                    readingEliminacoes = true;
                } else if (line.equals("Eventos:")) {
                    readingTeam1 = false;
                    readingTeam2 = false;
                    readingEvents = true;
                    readingDanoRecebido = false;
                    readingDanoCausado = false;
                    readingEliminacoes = false;
                } else if (line.startsWith("Dano Total:")) {
                    try {
                        danoTotal = Integer.parseInt(line.substring(11).trim());
                    } catch (NumberFormatException e) {
                        // Ignorar se não for um número válido
                    }
                } else if (line.startsWith("- ") && readingTeam1) {
                    time1.add(line.substring(2));
                } else if (line.startsWith("- ") && readingTeam2) {
                    time2.add(line.substring(2));
                } else if (readingDanoCausado && line.contains(":")) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        String player = parts[0].trim();
                        try {
                            int dano = Integer.parseInt(parts[1].trim());
                            danoCausado.put(player, dano);
                        } catch (NumberFormatException e) {
                            // Ignorar se não for um número válido
                        }
                    }
                } else if (readingDanoRecebido && line.contains(":")) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        String player = parts[0].trim();
                        try {
                            int dano = Integer.parseInt(parts[1].trim());
                            danoRecebido.put(player, dano);
                        } catch (NumberFormatException e) {
                            // Ignorar se não for um número válido
                        }
                    }
                } else if (readingEliminacoes && line.contains(":")) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        String player = parts[0].trim();
                        try {
                            int kill = Integer.parseInt(parts[1].trim());
                            eliminacoes.put(player, kill);
                            kills += kill;
                        } catch (NumberFormatException e) {
                            // Ignorar se não for um número válido
                        }
                    }
                } else if (readingEvents) {
                    totalEventos++;
                    
                    // Contar tipos de eventos
                    if (line.contains("foi eliminado por")) {
                        // Já contabilizado acima pelos dados de estatísticas
                    } else if (line.contains("recebeu dano significativo")) {
                        significantDamages++;
                    } else if (line.contains("Duelo terminou. Vencedor:")) {
                        if (line.contains("Equipe 1")) {
                            vencedor = time1.size() == 1 ? time1.get(0) : "Equipe 1";
                        } else if (line.contains("Equipe 2")) {
                            vencedor = time2.size() == 1 ? time2.get(0) : "Equipe 2";
                        }
                    }
                    
                    // Guardar eventos importantes apenas (filtra eventos de menor importância)
                    if (line.contains("Duelo iniciado") || 
                        line.contains("Duelo terminou") || 
                        line.contains("foi eliminado por") ||
                        line.contains("recebeu dano significativo")) {
                    eventos.add(line);
                    }
                }
            }
        } catch (IOException e) {
            sender.sendMessage(ColorUtils.colorize("&cErro ao ler arquivo de log: " + e.getMessage()));
            return;
        }
        
        // Enviar detalhes formatados
        sender.sendMessage(ColorUtils.colorize("&a&l=== Detalhes do Duelo " + simpleId + " ==="));
        sender.sendMessage(ColorUtils.colorize("&7Data: &f" + data));
        sender.sendMessage(ColorUtils.colorize("&7Tipo: &f" + (tipo.equals("SOLO") ? "1v1" : "Equipe")));
        sender.sendMessage(ColorUtils.colorize("&7Duração: &f" + duracao));
        
        // Times
        if (time1.size() == 1 && time2.size() == 1) {
            // Duelo 1v1
            sender.sendMessage(ColorUtils.colorize("&7Jogadores: &a" + time1.get(0) + " &7vs &c" + time2.get(0)));
        } else {
            // Duelo em equipe
            sender.sendMessage(ColorUtils.colorize("&7Equipe 1 (" + time1.size() + " jogadores):"));
            for (String player : time1) {
                sender.sendMessage(ColorUtils.colorize("&a  - " + player));
            }
            
            sender.sendMessage(ColorUtils.colorize("&7Equipe 2 (" + time2.size() + " jogadores):"));
            for (String player : time2) {
                sender.sendMessage(ColorUtils.colorize("&c  - " + player));
            }
        }
        
        // Resultado
        if (vencedor.equals("Empate")) {
            sender.sendMessage(ColorUtils.colorize("&7Resultado: &eEmpate"));
        } else {
            boolean team1Win = time1.contains(vencedor) || vencedor.equals("Equipe 1");
            String vencedorFormatado = team1Win ? "&a" + vencedor : "&c" + vencedor;
            sender.sendMessage(ColorUtils.colorize("&7Resultado: &fVitória de " + vencedorFormatado));
        }
        
        // Métricas
        sender.sendMessage(ColorUtils.colorize("&6&l=== Métricas ==="));
        sender.sendMessage(ColorUtils.colorize("&7Total de eventos: &f" + totalEventos));
        sender.sendMessage(ColorUtils.colorize("&7Eliminações totais: &f" + kills));
        sender.sendMessage(ColorUtils.colorize("&7Dano total: &f" + danoTotal));
        sender.sendMessage(ColorUtils.colorize("&7Danos significativos: &f" + significantDamages));
        
        // Estatísticas dos Jogadores
        sender.sendMessage(ColorUtils.colorize("&6&l=== Estatísticas dos Jogadores ==="));
        
        // Eliminações por jogador
        List<Map.Entry<String, Integer>> topKills = new ArrayList<>(eliminacoes.entrySet());
        topKills.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        for (Map.Entry<String, Integer> entry : topKills) {
            if (entry.getValue() > 0) {
                boolean isTeam1 = time1.contains(entry.getKey());
                String playerColor = isTeam1 ? "&a" : "&c";
                sender.sendMessage(ColorUtils.colorize(playerColor + entry.getKey() + "&7: &f" + entry.getValue() + " eliminações"));
            }
        }
        
        // Dano por jogador
        sender.sendMessage(ColorUtils.colorize("&6&l=== Dano Causado ==="));
        List<Map.Entry<String, Integer>> topDamage = new ArrayList<>(danoCausado.entrySet());
        topDamage.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        for (Map.Entry<String, Integer> entry : topDamage) {
            if (entry.getValue() > 0) {
                boolean isTeam1 = time1.contains(entry.getKey());
                String playerColor = isTeam1 ? "&a" : "&c";
                sender.sendMessage(ColorUtils.colorize(playerColor + entry.getKey() + "&7: &f" + entry.getValue() + " de dano"));
            }
        }
        
        // Resumo dos eventos importantes (limitar a 5 eventos)
        sender.sendMessage(ColorUtils.colorize("&6&l=== Eventos Importantes ==="));
        
        // Garantir que temos o início e o fim
        List<String> eventosImportantes = new ArrayList<>();
        String inicio = "";
        String fim = "";
        
        for (String evento : eventos) {
            if (evento.contains("Duelo iniciado")) {
                inicio = evento;
            } else if (evento.contains("Duelo terminou")) {
                fim = evento;
            } else if (eventosImportantes.size() < 5) {
                eventosImportantes.add(evento);
            }
        }
        
        // Mostrar o evento de início
        if (!inicio.isEmpty()) {
            sender.sendMessage(ColorUtils.colorize("&f" + inicio));
        }
        
        // Mostrar até 3 eventos do meio
        for (String evento : eventosImportantes) {
            sender.sendMessage(ColorUtils.colorize("&f" + evento));
        }
        
        // Mostrar o evento de fim
        if (!fim.isEmpty()) {
            sender.sendMessage(ColorUtils.colorize("&f" + fim));
        }
        
        // Caminho relativo do arquivo
        String relativePath = "plugins/PrimeLeagueX1/duellogs/" + logFile.getName();
        sender.sendMessage(ColorUtils.colorize("&7Para mais detalhes, veja o arquivo completo em:"));
        sender.sendMessage(ColorUtils.colorize("&f" + relativePath));
    }
    
    /**
     * Remove logs antigos
     * 
     * @param sender Remetente do comando
     */
    private void cleanupLogs(CommandSender sender) {
        String prefix = plugin.getMessageManager().getPrefix();
        
        // Vamos fazer uma limpeza mais inteligente:
        // 1. Manter todos os replays dos últimos 7 dias
        // 2. Manter replays importantes (muitas eliminações/dano) mesmo antigos
        // 3. Remover os demais replays antigos
        
        File[] logFiles = plugin.getDuelLogManager().getLogFiles();
        if (logFiles == null || logFiles.length == 0) {
            sender.sendMessage(ColorUtils.colorize(prefix + "Nenhum replay para limpar."));
            return;
        }
        
        int totalReplays = logFiles.length;
        int removidos = 0;
        int preservados = 0;
        int ignorados = 0; // Recentes
        
        // Mapa para armazenar informações de cada replay
        class ReplayInfo {
            File file;
            long timestamp;
            int eliminacoes;
            int danoTotal;
            boolean importante;
            
            public ReplayInfo(File file, long timestamp, int eliminacoes, int danoTotal) {
                this.file = file;
                this.timestamp = timestamp;
                this.eliminacoes = eliminacoes;
                this.danoTotal = danoTotal;
                
                // Considerar importante se tiver mais de 3 eliminações ou mais de 150 de dano
                this.importante = (eliminacoes > 3 || danoTotal > 150);
            }
        }
        
        List<ReplayInfo> replaysInfo = new ArrayList<>();
        long agora = System.currentTimeMillis();
        long seteDiasEmMillis = 7 * 24 * 60 * 60 * 1000L; // 7 dias em milissegundos
        
        // Analisar cada arquivo de replay
        for (File file : logFiles) {
            try {
                // Verificar data de criação do arquivo
                long lastModified = file.lastModified();
                boolean recente = (agora - lastModified) < seteDiasEmMillis;
                
                if (recente) {
                    // Ignorar arquivos recentes (não serão apagados)
                    ignorados++;
                    continue;
                }
                
                // Para arquivos antigos, verificar se são importantes
                int eliminacoes = 0;
                int danoTotal = 0;
                boolean readingEvents = false;
                
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Contar eliminações
                        if (line.contains("foi eliminado por")) {
                            eliminacoes++;
                        } 
                        // Extrair dano total
                        else if (line.startsWith("Dano Total:")) {
                            try {
                                danoTotal = Integer.parseInt(line.substring(11).trim());
                            } catch (NumberFormatException e) {
                                // Ignorar erro
                            }
                        }
                    }
                } catch (IOException e) {
                    // Se não conseguir ler, considerar não importante
                    continue;
                }
                
                // Adicionar informações à lista
                replaysInfo.add(new ReplayInfo(file, lastModified, eliminacoes, danoTotal));
                
            } catch (Exception e) {
                // Ignorar erro e continuar
            }
        }
        
        // Processar os arquivos antigos
        for (ReplayInfo info : replaysInfo) {
            if (info.importante) {
                // Marcar como importante e preservar
                preservados++;
            } else {
                // Não é importante e não é recente - pode ser removido
                if (info.file.delete()) {
                    removidos++;
                }
            }
        }
        
        // Enviar mensagem resumindo a operação
        sender.sendMessage(ColorUtils.colorize(prefix + "&aOperação de limpeza concluída:"));
        sender.sendMessage(ColorUtils.colorize("&7Total de replays: &f" + totalReplays));
        sender.sendMessage(ColorUtils.colorize("&7Replays recentes mantidos: &f" + ignorados));
        sender.sendMessage(ColorUtils.colorize("&7Replays importantes preservados: &f" + preservados));
        sender.sendMessage(ColorUtils.colorize("&7Replays removidos: &f" + removidos));
    }
    
    /**
     * Mostra os replays mais interessantes com base em uma métrica
     * 
     * @param sender Remetente do comando
     * @param metrica Métrica a usar (dano, kills, tempo)
     */
    private void showTopReplays(CommandSender sender, String metrica) {
        File[] logFiles = plugin.getDuelLogManager().getLogFiles();
        
        if (logFiles == null || logFiles.length == 0) {
            sender.sendMessage(ColorUtils.colorize(plugin.getMessageManager().getPrefix() + "Nenhum replay encontrado!"));
            return;
        }
        
        // Classe para armazenar dados dos replays para ranking
        class ReplayRanking {
            String id;
            String data;
            String tipo;
            String players;
            int danoTotal;
            int kills;
            int duracao; // em segundos
            
            public ReplayRanking(String id, String data, String tipo, String players, int danoTotal, int kills, int duracao) {
                this.id = id;
                this.data = data;
                this.tipo = tipo;
                this.players = players;
                this.danoTotal = danoTotal;
                this.kills = kills;
                this.duracao = duracao;
            }
        }
        
        List<ReplayRanking> replays = new ArrayList<>();
        
        // Ler dados de todos os replays
        for (File file : logFiles) {
            try {
                String uuid = file.getName().replace(".duellog", "");
                String simpleId = plugin.getDuelLogManager().getSimpleIdFromUUID(UUID.fromString(uuid));
                
                if (simpleId == null) {
                    continue;
                }
                
                String data = "";
                String tipo = "";
                int danoTotal = 0;
                int kills = 0;
                int duracaoSegundos = 0;
                List<String> time1 = new ArrayList<>();
                List<String> time2 = new ArrayList<>();
                
                boolean readingTeam1 = false;
                boolean readingTeam2 = false;
                
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("Data:")) {
                            data = line.substring(5).trim();
                        } else if (line.startsWith("Tipo:")) {
                            tipo = line.substring(5).trim();
                        } else if (line.startsWith("Duração:")) {
                            String duracao = line.substring(9).trim();
                            // Converter duração (MM:SS) para segundos
                            String[] parts = duracao.split(":");
                            if (parts.length == 2) {
                                try {
                                    int minutos = Integer.parseInt(parts[0]);
                                    int segundos = Integer.parseInt(parts[1]);
                                    duracaoSegundos = minutos * 60 + segundos;
                                } catch (NumberFormatException e) {
                                    // Ignorar erro
                                }
                            }
                        } else if (line.equals("Equipe 1:")) {
                            readingTeam1 = true;
                            readingTeam2 = false;
                        } else if (line.equals("Equipe 2:")) {
                            readingTeam1 = false;
                            readingTeam2 = true;
                        } else if (line.startsWith("Dano Total:")) {
                            try {
                                danoTotal = Integer.parseInt(line.substring(11).trim());
                            } catch (NumberFormatException e) {
                                // Ignorar
                            }
                        } else if (line.startsWith("- ") && readingTeam1) {
                            time1.add(line.substring(2));
                        } else if (line.startsWith("- ") && readingTeam2) {
                            time2.add(line.substring(2));
                        } else if (line.contains("foi eliminado por")) {
                            kills++;
                        }
                    }
                } catch (IOException e) {
                    continue;
                }
                
                // Formar string de jogadores
                String players;
                if (time1.size() == 1 && time2.size() == 1) {
                    players = time1.get(0) + " vs " + time2.get(0);
                } else {
                    players = "Equipe " + time1.size() + "v" + time2.size();
                }
                
                replays.add(new ReplayRanking(simpleId, data, tipo, players, danoTotal, kills, duracaoSegundos));
                
            } catch (Exception e) {
                // Ignorar arquivos com problemas
                continue;
            }
        }
        
        // Ordenar pela métrica escolhida
        switch (metrica.toLowerCase()) {
            case "kills":
            case "eliminacoes":
                replays.sort((a, b) -> Integer.compare(b.kills, a.kills));
                sender.sendMessage(ColorUtils.colorize("&a&l=== Top Replays por Eliminações ==="));
                break;
                
            case "tempo":
            case "duration":
            case "duracao":
                replays.sort((a, b) -> Integer.compare(b.duracao, a.duracao));
                sender.sendMessage(ColorUtils.colorize("&a&l=== Top Replays por Duração ==="));
                break;
                
            default: // dano
                replays.sort((a, b) -> Integer.compare(b.danoTotal, a.danoTotal));
                sender.sendMessage(ColorUtils.colorize("&a&l=== Top Replays por Dano Total ==="));
                break;
        }
        
        // Mostrar top 5 replays
        int count = 0;
        for (ReplayRanking replay : replays) {
            if (count >= 5) break;
            
            String duracao = String.format("%02d:%02d", replay.duracao / 60, replay.duracao % 60);
            
            sender.sendMessage(ColorUtils.colorize("&e#" + (count + 1) + " &7[ID: &f" + replay.id + "&7] &f" + replay.players));
            
            switch (metrica.toLowerCase()) {
                case "kills":
                case "eliminacoes":
                    sender.sendMessage(ColorUtils.colorize("   &7Eliminações: &f" + replay.kills + 
                                                        " &7| Dano: &f" + replay.danoTotal +
                                                        " &7| Duração: &f" + duracao));
                    break;
                    
                case "tempo":
                case "duration":
                case "duracao":
                    sender.sendMessage(ColorUtils.colorize("   &7Duração: &f" + duracao + 
                                                        " &7| Dano: &f" + replay.danoTotal +
                                                        " &7| Eliminações: &f" + replay.kills));
                    break;
                    
                default: // dano
                    sender.sendMessage(ColorUtils.colorize("   &7Dano: &f" + replay.danoTotal + 
                                                        " &7| Eliminações: &f" + replay.kills +
                                                        " &7| Duração: &f" + duracao));
                    break;
            }
            
            count++;
        }
        
        sender.sendMessage(ColorUtils.colorize("&7Use &f/x1 replay ver <ID> &7para ver detalhes de um replay"));
    }

    /**
     * Mostra os momentos mais importantes de um replay específico
     * 
     * @param sender Remetente do comando
     * @param simpleId ID simplificado do log
     */
    private void showHighlights(CommandSender sender, String simpleId) {
        String prefix = plugin.getMessageManager().getPrefix();
        
        // Obter arquivo de log a partir do ID simplificado
        File logFile = plugin.getDuelLogManager().getLogFileFromSimpleId(simpleId);
        
        if (logFile == null || !logFile.exists()) {
            sender.sendMessage(ColorUtils.colorize(prefix + plugin.getMessageManager().getMessage("duelo-log.detalhes-nao-encontrado")));
            return;
        }
        
        // Ler informações detalhadas
        String data = "";
        String duracao = "";
        List<String> time1 = new ArrayList<>();
        List<String> time2 = new ArrayList<>();
        List<String> eventos = new ArrayList<>();
        Map<String, Integer> killsByPlayer = new HashMap<>();
        Map<String, List<String>> playerHighlights = new HashMap<>();
        boolean readingTeam1 = false;
        boolean readingTeam2 = false;
        boolean readingEvents = false;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Data:")) {
                    data = line.substring(5).trim();
                } else if (line.startsWith("Duração:")) {
                    duracao = line.substring(9).trim();
                } else if (line.equals("Equipe 1:")) {
                    readingTeam1 = true;
                    readingTeam2 = false;
                    readingEvents = false;
                } else if (line.equals("Equipe 2:")) {
                    readingTeam1 = false;
                    readingTeam2 = true;
                    readingEvents = false;
                } else if (line.equals("Eventos:")) {
                    readingTeam1 = false;
                    readingTeam2 = false;
                    readingEvents = true;
                } else if (line.startsWith("- ") && readingTeam1) {
                    String playerName = line.substring(2);
                    time1.add(playerName);
                    playerHighlights.put(playerName, new ArrayList<>());
                    killsByPlayer.put(playerName, 0);
                } else if (line.startsWith("- ") && readingTeam2) {
                    String playerName = line.substring(2);
                    time2.add(playerName);
                    playerHighlights.put(playerName, new ArrayList<>());
                    killsByPlayer.put(playerName, 0);
                } else if (readingEvents) {
                    // Filtrar eventos significativos
                    if (line.contains("foi eliminado por")) {
                        eventos.add(line);
                        
                        // Registrar quem fez a eliminação
                        for (String player : playerHighlights.keySet()) {
                            if (line.contains(player + " eliminou") || line.contains("eliminado por " + player)) {
                                playerHighlights.get(player).add(line);
                                killsByPlayer.put(player, killsByPlayer.getOrDefault(player, 0) + 1);
                                break;
                            }
                        }
                    } else if (line.contains("recebeu dano significativo")) {
                        // Registrar danos significativos para os jogadores envolvidos
                        for (String player : playerHighlights.keySet()) {
                            if (line.contains(player + " causou") || line.contains(player + " recebeu")) {
                                playerHighlights.get(player).add(line);
                                break;
                            }
                        }
                        eventos.add(line);
                    } else if (line.contains("Duelo iniciado") || line.contains("Duelo terminou")) {
                        eventos.add(line);
                    }
                }
            }
        } catch (IOException e) {
            sender.sendMessage(ColorUtils.colorize("&cErro ao ler arquivo de log: " + e.getMessage()));
            return;
        }
        
        // Determinar o vencedor
        String vencedor = determinarVencedor(eventos, time1, time2);
        int equipeVencedora = getEquipeVencedora(vencedor, time1, time2);
        
        // Encontrar o jogador com mais destaques (MVP do duelo)
        String mvpPlayer = "";
        int maxKills = 0;
        for (Map.Entry<String, Integer> entry : killsByPlayer.entrySet()) {
            if (entry.getValue() > maxKills) {
                maxKills = entry.getValue();
                mvpPlayer = entry.getKey();
            }
        }
        
        // Enviar header
        sender.sendMessage(ColorUtils.colorize("&a&l=== Highlights do Duelo " + simpleId + " ==="));
        sender.sendMessage(ColorUtils.colorize("&7Data: &f" + data + " &7- Duração: &f" + duracao));
        
        // Jogadores
        if (time1.size() == 1 && time2.size() == 1) {
            // Duelo 1v1
            sender.sendMessage(ColorUtils.colorize("&7Jogadores: &a" + time1.get(0) + " &7vs &c" + time2.get(0)));
        } else {
            sender.sendMessage(ColorUtils.colorize("&7Equipes: &a" + time1.size() + " &7vs &c" + time2.size() + " jogadores"));
        }
        
        // Resultado com cores
        if (vencedor.equals("Empate")) {
            sender.sendMessage(ColorUtils.colorize("&7Resultado: &eEmpate"));
        } else {
            boolean vencedorTime1 = (equipeVencedora == 1);
            String vencedorFormatado = vencedorTime1 ? "&a" + vencedor : "&c" + vencedor;
            sender.sendMessage(ColorUtils.colorize("&7Resultado: &fVitória de " + vencedorFormatado));
        }
        
        // MVP do duelo (jogador com mais eliminações)
        if (!mvpPlayer.isEmpty() && maxKills > 0) {
            boolean isTeam1 = time1.contains(mvpPlayer);
            String playerColor = isTeam1 ? "&a" : "&c";
            sender.sendMessage(ColorUtils.colorize("&6MVP: " + playerColor + mvpPlayer + " &7com &f" + maxKills + " eliminações"));
        }
        
        // Momentos importantes - Ordenados por importância
        sender.sendMessage(ColorUtils.colorize("&e&l=== Momentos Importantes ==="));
        
        // Primeiro o início e o fim
        String inicio = "";
        String fim = "";
        for (String evento : eventos) {
            if (evento.contains("Duelo iniciado")) {
                inicio = evento;
            } else if (evento.contains("Duelo terminou")) {
                fim = evento;
            }
        }
        
        if (!inicio.isEmpty()) {
            sender.sendMessage(ColorUtils.colorize("&7" + inicio));
        } else {
            // Garantir que sempre temos o evento de início mesmo que falte no log
            sender.sendMessage(ColorUtils.colorize("&7[00:00] Duelo iniciado"));
        }
        
        // Separar eliminações e danos significativos
        List<String> eliminacoes = new ArrayList<>();
        List<String> danosSig = new ArrayList<>();
        
        for (String evento : eventos) {
            if (evento.contains("foi eliminado por")) {
                eliminacoes.add(evento);
            } else if (evento.contains("recebeu dano significativo")) {
                danosSig.add(evento);
            }
        }
        
        // Mostrar todas as eliminações (geralmente são poucos eventos)
        if (!eliminacoes.isEmpty()) {
            sender.sendMessage(ColorUtils.colorize("&e&l--- Eliminações ---"));
            for (String eliminacao : eliminacoes) {
                sender.sendMessage(ColorUtils.colorize("&f" + eliminacao));
            }
        }
        
        // Mostrar até 5 danos significativos
        if (!danosSig.isEmpty()) {
            sender.sendMessage(ColorUtils.colorize("&e&l--- Danos Significativos ---"));
            int count = 0;
            for (String dano : danosSig) {
                if (count >= 5) break;
                sender.sendMessage(ColorUtils.colorize("&f" + dano));
                count++;
            }
            
            if (danosSig.size() > 5) {
                sender.sendMessage(ColorUtils.colorize("&7...e mais " + (danosSig.size() - 5) + " eventos de dano."));
            }
        }
        
        // Final do duelo
        if (!fim.isEmpty()) {
            sender.sendMessage(ColorUtils.colorize("&7" + fim));
        } else {
            // Garantir que sempre temos o evento de fim
            if (vencedor.equals("Empate")) {
                sender.sendMessage(ColorUtils.colorize("&7[" + duracao + "] Duelo terminou. Resultado: Empate"));
            } else {
                boolean vencedorTime1 = (equipeVencedora == 1);
                String vencedorFormatado = vencedorTime1 ? "&a" + vencedor : "&c" + vencedor;
                sender.sendMessage(ColorUtils.colorize("&7[" + duracao + "] Duelo terminou. Vencedor: " + vencedorFormatado));
            }
        }
        
        // Se houver um MVP com mais de uma eliminação, mostrar seus highlights
        if (!mvpPlayer.isEmpty() && maxKills > 1 && playerHighlights.containsKey(mvpPlayer)) {
            List<String> mvpHighlights = playerHighlights.get(mvpPlayer);
            if (!mvpHighlights.isEmpty()) {
                boolean isTeam1 = time1.contains(mvpPlayer);
                String playerColor = isTeam1 ? "&a" : "&c";
                
                sender.sendMessage(ColorUtils.colorize("&6&l=== Highlights de " + playerColor + mvpPlayer + " &6&l==="));
                for (String highlight : mvpHighlights) {
                    sender.sendMessage(ColorUtils.colorize("&f" + highlight));
                }
            }
        }
        
        // Como ver mais detalhes
        sender.sendMessage(ColorUtils.colorize("&7Para ver detalhes completos: &f/x1 replay ver " + simpleId));
    }

    /**
     * Compartilha um replay no chat com texto clicável
     * 
     * @param sender Remetente do comando
     * @param simpleId ID simplificado do log
     */
    private void shareReplay(CommandSender sender, String simpleId) {
        String prefix = plugin.getMessageManager().getPrefix();
        
        // Verificar se o sender é um player
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtils.colorize("&cApenas jogadores podem compartilhar replays!"));
            return;
        }
        
        Player player = (Player) sender;
        
        // Obter arquivo de log a partir do ID simplificado
        File logFile = plugin.getDuelLogManager().getLogFileFromSimpleId(simpleId);
        
        if (logFile == null || !logFile.exists()) {
            sender.sendMessage(ColorUtils.colorize(prefix + plugin.getMessageManager().getMessage("duelo-log.detalhes-nao-encontrado")));
            return;
        }
        
        // Ler informações básicas do replay
        String data = "";
        String tipo = "";
        String duracao = "";
        int danoTotal = 0;
        int eliminacoes = 0;
        List<String> time1 = new ArrayList<>();
        List<String> time2 = new ArrayList<>();
        List<String> eventos = new ArrayList<>();
        boolean readingTeam1 = false;
        boolean readingTeam2 = false;
        boolean readingEvents = false;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Data:")) {
                    data = line.substring(5).trim();
                } else if (line.startsWith("Tipo:")) {
                    tipo = line.substring(5).trim();
                } else if (line.startsWith("Duração:")) {
                    duracao = line.substring(9).trim();
                } else if (line.equals("Equipe 1:")) {
                    readingTeam1 = true;
                    readingTeam2 = false;
                    readingEvents = false;
                } else if (line.equals("Equipe 2:")) {
                    readingTeam1 = false;
                    readingTeam2 = true;
                    readingEvents = false;
                } else if (line.equals("Eventos:")) {
                    readingTeam1 = false;
                    readingTeam2 = false;
                    readingEvents = true;
                } else if (line.startsWith("- ") && readingTeam1) {
                    time1.add(line.substring(2));
                } else if (line.startsWith("- ") && readingTeam2) {
                    time2.add(line.substring(2));
                } else if (line.startsWith("Dano Total:")) {
                    try {
                        danoTotal = Integer.parseInt(line.substring(11).trim());
                    } catch (NumberFormatException e) {
                        // Ignorar se não for um número
                    }
                } else if (line.contains("foi eliminado por")) {
                    eliminacoes++;
                } else if (readingEvents) {
                    eventos.add(line);
                }
                
                // Se já temos todas as informações básicas, podemos parar
                if (!data.isEmpty() && !tipo.isEmpty() && !duracao.isEmpty() && 
                    !time1.isEmpty() && !time2.isEmpty() && readingEvents) {
                    if (eventos.size() >= 2) { // Temos pelo menos início e fim
                        break;
                    }
                }
            }
        } catch (IOException e) {
            sender.sendMessage(ColorUtils.colorize("&cErro ao ler arquivo de log: " + e.getMessage()));
            return;
        }
        
        // Determinar o vencedor
        String vencedor = determinarVencedor(eventos, time1, time2);
        int equipeVencedora = getEquipeVencedora(vencedor, time1, time2);
        
        // Determinar o título do replay
        String replayTitle;
        if (time1.size() == 1 && time2.size() == 1) {
            replayTitle = time1.get(0) + " vs " + time2.get(0);
        } else {
            replayTitle = "Duelo em Equipe " + time1.size() + "v" + time2.size();
        }
        
        // Determinar resultado
        String resultadoTexto;
        if (vencedor.equals("Empate")) {
            resultadoTexto = "&eEmpate";
        } else {
            boolean vencedorTime1 = (equipeVencedora == 1);
            resultadoTexto = vencedorTime1 ? "&aVitória de " + vencedor : "&cVitória de " + vencedor;
        }
        
        // Construir mensagem para o jogador compartilhando
        String mensagemJogador = ColorUtils.colorize(prefix + "&aReplay compartilhado com sucesso!");
        player.sendMessage(mensagemJogador);
        
        // Construir mensagem de compartilhamento
        String mensagemHeader = ColorUtils.colorize("&8&l[&6&lREPLAY&8&l] &f" + player.getName() + " &7compartilhou um replay:");
        String mensagemInfo = ColorUtils.colorize("&e" + replayTitle + " &7(" + duracao + ") - " + resultadoTexto);
        String mensagemStats = ColorUtils.colorize("&7Dano Total: &f" + danoTotal + " &7| Eliminações: &f" + eliminacoes);
        String mensagemBotoes = ColorUtils.colorize("&7Use os comandos: &a/x1 replay ver " + simpleId + " &7ou &6/x1 replay highlight " + simpleId);
        
        // Broadcast a mensagem para todos os jogadores
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            online.sendMessage(mensagemHeader);
            online.sendMessage(mensagemInfo);
            online.sendMessage(mensagemStats);
            online.sendMessage(mensagemBotoes);
        }
    }

    /**
     * Determina quem foi o vencedor do duelo com base nos eventos do replay
     * 
     * @param eventos Lista de eventos do duelo
     * @param time1 Lista de jogadores da equipe 1
     * @param time2 Lista de jogadores da equipe 2
     * @return Nome do vencedor ou "Empate" se não houver vencedor
     */
    private String determinarVencedor(List<String> eventos, List<String> time1, List<String> time2) {
        // Primeiro verificar o evento de "Duelo terminou" que geralmente tem o resultado
        for (String evento : eventos) {
            if (evento.contains("Duelo terminou")) {
                if (evento.contains("Vencedor: Equipe 1")) {
                    return time1.size() == 1 ? time1.get(0) : "Equipe 1";
                } else if (evento.contains("Vencedor: Equipe 2")) {
                    return time2.size() == 1 ? time2.get(0) : "Equipe 2";
                } else if (evento.contains("Resultado: Empate")) {
                    return "Empate";
                } else if (evento.contains("Vencedor:")) {
                    // Verificar se o vencedor é um jogador específico
                    for (String player : time1) {
                        if (evento.contains("Vencedor: " + player)) {
                            return player;
                        }
                    }
                    for (String player : time2) {
                        if (evento.contains("Vencedor: " + player)) {
                            return player;
                        }
                    }
                }
            }
        }
        
        // Se não encontrou o vencedor explícito, tentar inferir pelas eliminações
        Map<String, Integer> eliminacoesPorJogador = new HashMap<>();
        Map<String, Integer> eliminacoesPorEquipe = new HashMap<>();
        eliminacoesPorEquipe.put("Equipe 1", 0);
        eliminacoesPorEquipe.put("Equipe 2", 0);
        
        for (String evento : eventos) {
            if (evento.contains("foi eliminado por")) {
                for (String player : time1) {
                    if (evento.contains("eliminado por " + player)) {
                        eliminacoesPorJogador.put(player, eliminacoesPorJogador.getOrDefault(player, 0) + 1);
                        eliminacoesPorEquipe.put("Equipe 1", eliminacoesPorEquipe.get("Equipe 1") + 1);
                        break;
                    }
                }
                for (String player : time2) {
                    if (evento.contains("eliminado por " + player)) {
                        eliminacoesPorJogador.put(player, eliminacoesPorJogador.getOrDefault(player, 0) + 1);
                        eliminacoesPorEquipe.put("Equipe 2", eliminacoesPorEquipe.get("Equipe 2") + 1);
                        break;
                    }
                }
            }
        }
        
        // Verificar se alguma equipe tem mais eliminações
        if (eliminacoesPorEquipe.get("Equipe 1") > eliminacoesPorEquipe.get("Equipe 2")) {
            // Encontrar jogador com mais eliminações na equipe 1
            String melhorJogador = "";
            int maxElims = 0;
            for (String player : time1) {
                int elims = eliminacoesPorJogador.getOrDefault(player, 0);
                if (elims > maxElims) {
                    maxElims = elims;
                    melhorJogador = player;
                }
            }
            return !melhorJogador.isEmpty() ? melhorJogador : (time1.size() == 1 ? time1.get(0) : "Equipe 1");
        } else if (eliminacoesPorEquipe.get("Equipe 2") > eliminacoesPorEquipe.get("Equipe 1")) {
            // Encontrar jogador com mais eliminações na equipe 2
            String melhorJogador = "";
            int maxElims = 0;
            for (String player : time2) {
                int elims = eliminacoesPorJogador.getOrDefault(player, 0);
                if (elims > maxElims) {
                    maxElims = elims;
                    melhorJogador = player;
                }
            }
            return !melhorJogador.isEmpty() ? melhorJogador : (time2.size() == 1 ? time2.get(0) : "Equipe 2");
        }
        
        // Se tudo falhar, retornar empate
        return "Empate";
    }
    
    /**
     * Determina qual equipe venceu baseado no nome do vencedor
     * 
     * @param vencedor Nome do vencedor ou "Empate"
     * @param time1 Lista de jogadores da equipe 1
     * @param time2 Lista de jogadores da equipe 2
     * @return 1 para equipe 1, 2 para equipe 2, 0 para empate
     */
    private int getEquipeVencedora(String vencedor, List<String> time1, List<String> time2) {
        if (vencedor.equals("Empate")) {
            return 0;
        } else if (vencedor.equals("Equipe 1") || time1.contains(vencedor)) {
            return 1;
        } else if (vencedor.equals("Equipe 2") || time2.contains(vencedor)) {
            return 2;
        }
        return 0; // Empate por padrão se não conseguir determinar
    }
} 