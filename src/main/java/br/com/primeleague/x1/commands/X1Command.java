package br.com.primeleague.x1.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import br.com.primeleague.x1.Main;
import br.com.primeleague.x1.managers.MessageManager;
import br.com.primeleague.x1.enums.DuelType;
import br.com.primeleague.x1.commands.ReplayCommand;
import br.com.primeleague.x1.rival.RivalData;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.io.File;
import java.util.Map;
import java.util.HashMap;

/**
 * Comando principal do plugin
 */
public class X1Command implements CommandExecutor, TabCompleter {

    private final Main plugin;
    private final MessageManager messageManager;
    private final ReplayCommand replayCommand;
    
    /**
     * Construtor
     * 
     * @param plugin Instância do plugin
     */
    public X1Command(Main plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        this.replayCommand = new ReplayCommand(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Este comando só pode ser usado por jogadores!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Abrir o menu principal
            plugin.getGUIManager().openMainMenu(player);
            return true;
        }
        
        // Verificar subcomandos
        if (args.length > 0) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "team":
                case "equipe":
                    return handleTeamCommand(player, args);
                    
                case "admin":
                    return handleAdminCommand(player, args);
                    
                case "stats":
                case "estatisticas":
                    return handleStatsCommand(player, args);
                    
                case "replay":
                    return replayCommand.onCommand(sender, args);
                    
                case "desafiar":
                case "challenge":
                    return handleChallengeCommand(player, args);
                    
                case "aceitar":
                case "accept":
                    return handleAcceptCommand(player, args);
                    
                case "rejeitar":
                case "decline":
                case "recusar":
                    return handleRejectCommand(player, args);
                    
                case "cancelar":
                case "cancel":
                    return handleCancelCommand(player, args);
                    
                case "top":
                case "ranking":
                    return handleTopCommand(player, args);
                    
                case "spectate":
                case "spec":
                case "assistir":
                case "espectador":
                    return handleSpectateCommand(player, args);
                    
                case "help":
                case "ajuda":
                    return handleHelpCommand(player, args);
                    
                case "rival":
                    return handleRivalCommand(player, args);
                    
                case "reload":
                    return handleReloadCommand(player, args);
                    
                default:
                    messageManager.sendMessage(player, "geral.comando-inexistente");
                    return true;
            }
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> commands = new ArrayList<>();
            commands.add("desafiar");
            commands.add("aceitar");
            commands.add("rejeitar");
            commands.add("kits");
            commands.add("status");
            commands.add("stats");
            commands.add("sair");
            commands.add("help");
            commands.add("ajuda");
            commands.add("rival");
            
            if (sender.hasPermission("primeleague.x1.team")) {
                commands.add("team");
            }
            
            if (sender.hasPermission("primeleague.x1.admin")) {
                commands.add("admin");
            }
            
            if (sender.hasPermission("primeleague.x1.replay")) {
                commands.add("replay");
            }
            
            for (String s : commands) {
                if (s.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(s);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("rival")) {
                // Sugerir jogadores online para verificar rivalidade
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.getName().equals(sender.getName()) && 
                            p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(p.getName());
                    }
                }
            } else if (args[0].equalsIgnoreCase("team")) {
                List<String> teamCommands = new ArrayList<>();
                teamCommands.add("criar");
                teamCommands.add("convidar");
                teamCommands.add("aceitar");
                teamCommands.add("sair");
                teamCommands.add("listar");
                teamCommands.add("info");
                teamCommands.add("desafiar");
                
                for (String s : teamCommands) {
                    if (s.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(s);
                    }
                }
            } else if (args[0].equalsIgnoreCase("admin")) {
                List<String> adminCommands = new ArrayList<>();
                adminCommands.add("reload");
                adminCommands.add("setpos1");
                adminCommands.add("setpos2");
                adminCommands.add("setspectator");
                adminCommands.add("resetarena");
                
                for (String s : adminCommands) {
                    if (s.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(s);
                    }
                }
            } else if (args[0].equalsIgnoreCase("replay")) {
                List<String> replayCommands = new ArrayList<>();
                replayCommands.add("list");
                replayCommands.add("ver");
                replayCommands.add("limpar");
                replayCommands.add("top");
                replayCommands.add("highlight");
                replayCommands.add("share");
                
                for (String s : replayCommands) {
                    if (s.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(s);
                    }
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("replay") && args[1].equalsIgnoreCase("top")) {
                List<String> metricasCommands = new ArrayList<>();
                metricasCommands.add("dano");
                metricasCommands.add("kills");
                metricasCommands.add("tempo");
                
                for (String s : metricasCommands) {
                    if (s.toLowerCase().startsWith(args[2].toLowerCase())) {
                        completions.add(s);
                    }
                }
            }
        }
        
        Collections.sort(completions);
        return completions;
    }
    
    /**
     * Processa comandos administrativos
     */
    private boolean handleAdminCommand(Player player, String[] args) {
        if (!player.hasPermission("primeleague.x1.admin")) {
            messageManager.sendMessage(player, "geral.sem-permissao");
            return true;
        }
        
        if (args.length == 1) {
            plugin.getGUIManager().openAdminMenu(player);
            return true;
        }
        
        String adminCmd = args[1].toLowerCase();
        
        switch (adminCmd) {
            case "setpos1":
                plugin.getArenaManager().setPos1(player.getLocation());
                messageManager.sendMessage(player, "arena.pos1-definida");
                break;
                
            case "setpos2":
                plugin.getArenaManager().setPos2(player.getLocation());
                messageManager.sendMessage(player, "arena.pos2-definida");
                break;
                
            case "setspectator":
                plugin.getArenaManager().setSpectatorLocation(player.getLocation());
                messageManager.sendMessage(player, "arena.espectador-definido");
                break;
                
            case "reload":
                return handleReloadCommand(player, args);
                
            default:
                messageManager.sendMessage(player, "geral.comando-inexistente");
                break;
        }
        
        return true;
    }
    
    /**
     * Processa comando para desafiar um jogador
     */
    private boolean handleChallengeCommand(Player player, String[] args) {
        if (args.length < 2) {
            messageManager.sendMessage(player, "duelo.erro-uso-desafio");
            return true;
        }
        
        String targetName = args[1];
        Player target = Bukkit.getPlayerExact(targetName);
        
        // Verificar se o jogador está online
        if (target == null || !target.isOnline()) {
            messageManager.sendMessage(player, "geral.jogador-offline");
            return true;
        }
        
        // Verificar se o jogador tentou desafiar a si mesmo
        if (player.getName().equals(targetName)) {
            messageManager.sendMessage(player, "duelo.desafiar-proprio");
            return true;
        }
        
        // Verificar se o jogador já está em duelo
        if (plugin.getDuelManager().isInDuel(player.getName())) {
            messageManager.sendMessage(player, "duelo.ja-em-duelo");
            return true;
        }
        
        // Verificar se o alvo já está em duelo
        if (plugin.getDuelManager().isInDuel(targetName)) {
            messageManager.sendMessage(player, "duelo.alvo-em-duelo");
            return true;
        }
        
        // Definir um tipo de duelo padrão (ARENA) para uso com o comando
        DuelType duelType = DuelType.ARENA;
        
        // Se um tipo específico foi fornecido
        if (args.length >= 3) {
            String typeArg = args[2].toUpperCase();
            try {
                // Tenta converter a string para um tipo de duelo
                if (typeArg.equals("ARENA_KIT") || typeArg.equals("ARENA-KIT")) {
                    duelType = DuelType.ARENA_KIT;
                } else if (typeArg.equals("LOCAL")) {
                    duelType = DuelType.LOCAL;
                } else if (typeArg.equals("LOCAL_KIT") || typeArg.equals("LOCAL-KIT")) {
                    duelType = DuelType.LOCAL_KIT;
                }
            } catch (Exception e) {
                // Se falhar, mantém o tipo padrão
                System.out.println("[PrimeLeagueX1] Erro ao converter tipo de duelo: " + e.getMessage());
            }
        }
        
        // Registrar o tipo selecionado para usar quando o desafio for aceito
        plugin.getGUIManager().setSelectedDuelType(player, duelType);
        System.out.println("[PrimeLeagueX1] Jogador " + player.getName() + " desafiou " + targetName + 
                " com o tipo " + duelType.name() + " (usesKit: " + duelType.usesKit() + ")");
        
        // Enviar o desafio
        plugin.getDuelManager().sendChallenge(player, target, duelType, 0.0);
        
        return true;
    }
    
    /**
     * Processa comando para aceitar um desafio
     */
    private boolean handleAcceptCommand(Player player, String[] args) {
        if (plugin.getDuelManager().isInDuel(player.getName())) {
            messageManager.sendMessage(player, "duelo.duelo-em-andamento");
            return true;
        }
        
        if (args.length < 2) {
            // Aceitar o primeiro desafio disponível
            if (!plugin.getDuelManager().hasChallenge(player.getName())) {
                messageManager.sendMessage(player, "duelo.sem-desafio");
                return true;
            }
            
            // Encontrar e aceitar o desafio
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (plugin.getDuelManager().isChallengedBy(player.getName(), online.getName())) {
                    plugin.getDuelManager().acceptChallenge(online.getName(), player);
                    return true;
                }
            }
            
            messageManager.sendMessage(player, "duelo.sem-desafio");
            return true;
        }
        
        // Aceitar desafio de um jogador específico
        String challengerName = args[1];
        Player challenger = Bukkit.getPlayerExact(challengerName);
        
        if (challenger == null || !challenger.isOnline()) {
            messageManager.sendMessage(player, "geral.jogador-offline");
            return true;
        }
        
        if (!plugin.getDuelManager().isChallengedBy(player.getName(), challenger.getName())) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("jogador", challenger.getName());
            messageManager.sendMessage(player, "duelo.sem-desafio-jogador", placeholders);
            return true;
        }
        
        plugin.getDuelManager().acceptChallenge(challenger.getName(), player);
        return true;
    }
    
    /**
     * Processa comando para recusar um desafio
     */
    private boolean handleRejectCommand(Player player, String[] args) {
        if (args.length < 2) {
            // Recusar o primeiro desafio disponível
            if (!plugin.getDuelManager().hasChallenge(player.getName())) {
                messageManager.sendMessage(player, "duelo.sem-desafio");
                return true;
            }
            
            // Encontrar e recusar o desafio
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (plugin.getDuelManager().isChallengedBy(player.getName(), online.getName())) {
                    plugin.getDuelManager().rejectChallenge(online.getName(), player);
                    return true;
                }
            }
            
            messageManager.sendMessage(player, "duelo.sem-desafio");
            return true;
        }
        
        // Recusar desafio de um jogador específico
        String challengerName = args[1];
        Player challenger = Bukkit.getPlayerExact(challengerName);
        
        if (challenger == null || !challenger.isOnline()) {
            messageManager.sendMessage(player, "geral.jogador-offline");
            return true;
        }
        
        if (!plugin.getDuelManager().isChallengedBy(player.getName(), challenger.getName())) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("jogador", challenger.getName());
            messageManager.sendMessage(player, "duelo.sem-desafio-jogador", placeholders);
            return true;
        }
        
        plugin.getDuelManager().rejectChallenge(challenger.getName(), player);
        return true;
    }
    
    /**
     * Processa comando para cancelar um desafio enviado
     */
    private boolean handleCancelCommand(Player player, String[] args) {
        String challenged = plugin.getDuelManager().getChallenged(player.getName());
        
        if (challenged == null) {
            messageManager.sendMessage(player, "duelo.sem-desafio-enviado");
            return true;
        }
        
        Player target = Bukkit.getPlayerExact(challenged);
        
        if (target != null && target.isOnline()) {
            plugin.getDuelManager().rejectChallenge(player.getName(), target);
        } else {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("jogador", challenged);
            messageManager.sendMessage(player, "duelo.desafio-cancelado", placeholders);
        }
        
        return true;
    }
    
    /**
     * Processa comando para ver estatísticas
     */
    private boolean handleStatsCommand(Player player, String[] args) {
        if (args.length < 2) {
            // Ver estatísticas próprias
            plugin.getStatsManager().showStats(player);
            return true;
        }
        
        // Ver estatísticas de outro jogador
        if (!player.hasPermission("primeleaguex1.stats.others")) {
            messageManager.sendMessage(player, "geral.sem-permissao");
            return true;
        }
        
        String targetName = args[1];
        plugin.getGUIManager().openStatsMenu(player);
        
        return true;
    }
    
    /**
     * Processa comando para ver o ranking
     */
    private boolean handleTopCommand(Player player, String[] args) {
        plugin.getGUIManager().openRankingMenu(player);
        return true;
    }
    
    /**
     * Processa comando para assistir duelos
     */
    private boolean handleSpectateCommand(Player player, String[] args) {
        if (!player.hasPermission("primeleaguex1.spectate")) {
            messageManager.sendMessage(player, "geral.sem-permissao");
            return true;
        }
        
        // Verificar se a arena está configurada
        if (!plugin.getArenaManager().isArenaConfigured()) {
            messageManager.sendMessage(player, "arena.nao-configurada");
            return true;
        }
        
        // Teleportar para o local de espectador
        Location spectatorLoc = plugin.getArenaManager().getSpectatorLocation();
        player.teleport(spectatorLoc);
        messageManager.sendMessage(player, "duelo.modo-espectador");
        
        return true;
    }
    
    /**
     * Processa comando de ajuda
     */
    private boolean handleHelpCommand(Player player, String[] args) {
        player.sendMessage(messageManager.getMessageWithPrefix("prefixo") + "§e/x1 desafiar <jogador> §7- Desafiar um jogador para duelo");
        player.sendMessage(messageManager.getMessageWithPrefix("prefixo") + "§e/x1 aceitar §7- Aceitar um desafio de duelo");
        player.sendMessage(messageManager.getMessageWithPrefix("prefixo") + "§e/x1 rejeitar §7- Rejeitar um desafio de duelo");
        player.sendMessage(messageManager.getMessageWithPrefix("prefixo") + "§e/x1 cancelar §7- Cancelar um desafio enviado");
        player.sendMessage(messageManager.getMessageWithPrefix("prefixo") + "§e/x1 team §7- Comandos de equipe");
        player.sendMessage(messageManager.getMessageWithPrefix("prefixo") + "§e/x1 stats §7- Ver suas estatísticas");
        player.sendMessage(messageManager.getMessageWithPrefix("prefixo") + "§e/x1 top §7- Ver o ranking de duelos");
        player.sendMessage(messageManager.getMessageWithPrefix("prefixo") + "§e/x1 rival §7- Ver suas rivalidades");
        
        if (player.hasPermission("primeleague.x1.admin")) {
            player.sendMessage(messageManager.getMessageWithPrefix("prefixo") + "§e/x1 admin §7- Comandos administrativos");
        }
        
        if (player.hasPermission("primeleague.x1.replay")) {
            player.sendMessage(messageManager.getMessageWithPrefix("prefixo") + "§e/x1 replay §7- Sistema de replay de duelos");
        }
        
        return true;
    }
    
    /**
     * Processa comando para gerenciar equipes
     */
    private boolean handleTeamCommand(Player player, String[] args) {
        // Implementação básica para o comando de equipe
        if (!player.hasPermission("primeleague.x1.team")) {
            messageManager.sendMessage(player, "geral.sem-permissao");
            return true;
        }
        
        if (args.length < 2) {
            // Mostrar ajuda do comando de equipe
            player.sendMessage(messageManager.getMessageWithPrefix("prefixo") + "§e/x1 team criar <nome> §7- Criar uma equipe");
            player.sendMessage(messageManager.getMessageWithPrefix("prefixo") + "§e/x1 team convidar <jogador> §7- Convidar um jogador para a equipe");
            player.sendMessage(messageManager.getMessageWithPrefix("prefixo") + "§e/x1 team aceitar §7- Aceitar um convite de equipe");
            player.sendMessage(messageManager.getMessageWithPrefix("prefixo") + "§e/x1 team sair §7- Sair da equipe atual");
            player.sendMessage(messageManager.getMessageWithPrefix("prefixo") + "§e/x1 team listar §7- Listar todas as equipes");
            player.sendMessage(messageManager.getMessageWithPrefix("prefixo") + "§e/x1 team info <equipe> §7- Ver informações de uma equipe");
            player.sendMessage(messageManager.getMessageWithPrefix("prefixo") + "§e/x1 team desafiar <equipe> §7- Desafiar outra equipe para duelo");
            return true;
        }
        
        // Implementação básica dos subcomandos de equipe
        String subCommand = args[1].toLowerCase();
        
        switch (subCommand) {
            case "criar":
                // Lógica para criar equipe
                return true;
                
            case "convidar":
                // Lógica para convidar jogador
                return true;
                
            case "aceitar":
                // Lógica para aceitar convite
                return true;
                
            case "sair":
                // Lógica para sair da equipe
                return true;
                
            case "listar":
                // Lógica para listar equipes
                return true;
                
            case "info":
                // Lógica para mostrar info da equipe
                return true;
                
            case "desafiar":
                // Lógica para desafiar outra equipe
                return true;
                
            default:
                messageManager.sendMessage(player, "geral.comando-inexistente");
                return true;
        }
    }

    /**
     * Processa o comando de rivalidade
     * @param player Jogador que executou o comando
     * @param args Argumentos do comando
     * @return true se o comando foi processado
     */
    private boolean handleRivalCommand(Player player, String[] args) {
        // Verificar se o sistema de rivalidades está ativado
        if (plugin.getConfigManager().getConfig().getBoolean("rivalidade.ativar", true)) {
            String playerName = player.getName();
            
            // Caso o comando seja apenas /x1 rival, mostrar lista de rivais
            if (args.length == 1) {
                // Obter lista de rivalidades do jogador
                List<RivalData> rivalidades = plugin.getRivalManager().getPlayerRivalries(playerName);
                
                if (rivalidades.isEmpty()) {
                    messageManager.sendMessage(player, "rival.sem-rivais");
                    return true;
                }
                
                // Enviar cabeçalho
                messageManager.sendMessage(player, "rival.lista-cabecalho");
                
                // Listar cada rivalidade
                for (RivalData rival : rivalidades) {
                    String rivalName = rival.getPlayer1().equals(playerName) ? 
                            rival.getPlayer2() : rival.getPlayer1();
                            
                    int suasVitorias = rival.getVictories(playerName);
                    int vitoriasRival = rival.getVictories(rivalName);
                    int totalDuelos = rival.getTotalDuels();
                    
                    // Enviar informação sobre esta rivalidade
                    messageManager.sendMessage(player, "rival.lista-rival", 
                            "%rival%", rivalName,
                            "%suas_vitorias%", String.valueOf(suasVitorias),
                            "%vitorias_rival%", String.valueOf(vitoriasRival),
                            "%total%", String.valueOf(totalDuelos));
                }
                
                return true;
            } 
            // Caso o comando seja /x1 rival <jogador>, mostrar detalhes da rivalidade
            else if (args.length == 2) {
                String targetName = args[1];
                
                // Verificar se o jogador existe
                if (plugin.getStatsManager().playerExists(targetName)) {
                    // Verificar se existe uma rivalidade
                    if (plugin.getRivalManager().isRivalry(playerName, targetName)) {
                        // Obter dados da rivalidade
                        RivalData rivalData = plugin.getRivalManager().getRivalData(playerName, targetName);
                        
                        if (rivalData != null) {
                            int suasVitorias = rivalData.getVictories(playerName);
                            int vitoriasRival = rivalData.getVictories(targetName);
                            int totalDuelos = rivalData.getTotalDuels();
                            
                            // Enviar cabeçalho
                            messageManager.sendMessage(player, "rival.detalhes-cabecalho", 
                                    "%rival%", targetName);
                            
                            // Enviar estatísticas
                            messageManager.sendMessage(player, "rival.detalhes-vitorias", 
                                    "%suas_vitorias%", String.valueOf(suasVitorias));
                            
                            messageManager.sendMessage(player, "rival.detalhes-derrotas", 
                                    "%derrotas%", String.valueOf(vitoriasRival));
                            
                            messageManager.sendMessage(player, "rival.detalhes-total", 
                                    "%total%", String.valueOf(totalDuelos));
                            
                            // Mostrar últimos duelos, se houver
                            List<String> ultimosDuelos = rivalData.getLastDuels();
                            
                            if (!ultimosDuelos.isEmpty()) {
                                messageManager.sendMessage(player, "rival.detalhes-ultimos", 
                                        "%numero%", String.valueOf(ultimosDuelos.size()));
                                
                                for (String vencedor : ultimosDuelos) {
                                    String resultado;
                                    
                                    if (vencedor.equals(playerName)) {
                                        resultado = "§a✓ Vitória para você";
                                    } else if (vencedor.equals(targetName)) {
                                        resultado = "§c✗ Vitória para " + targetName;
                                    } else {
                                        resultado = "§e- Empate";
                                    }
                                    
                                    messageManager.sendMessage(player, "rival.detalhes-ultimo-item", 
                                            "%resultado%", resultado);
                                }
                            } else {
                                messageManager.sendMessage(player, "rival.historico-vazio");
                            }
                            
                            // Sugerir desafiar novamente
                            messageManager.sendMessage(player, "rival.desafiar-rival", 
                                    "%rival%", targetName);
                        }
                    } else {
                        messageManager.sendMessage(player, "rival.sem-rivalidade", 
                                "%jogador%", targetName);
                    }
                } else {
                    messageManager.sendMessage(player, "rival.jogador-nao-existe", 
                            "%jogador%", targetName);
                }
                
                return true;
            } else {
                messageManager.sendMessage(player, "rival.comando-uso");
                return true;
            }
        } else {
            player.sendMessage("§cO sistema de rivalidades está desativado neste servidor.");
            return true;
        }
    }

    /**
     * Processa o comando de recarregamento
     * @param player Jogador que executou o comando
     * @param args Argumentos do comando
     * @return true se o comando foi processado
     */
    private boolean handleReloadCommand(Player player, String[] args) {
        try {
            player.sendMessage("§eRecarregando configurações e mensagens...");
            
            // Recarregar configurações
            plugin.getConfigManager().reloadConfigs();
            
            // Recarregar mensagens
            messageManager.reload();
            
            // Verificar se as mensagens básicas foram carregadas corretamente
            if (messageManager.hasMessage("prefixo") && messageManager.hasMessage("geral.plugin-ativado")) {
                messageManager.sendMessage(player, "geral.reload-completo");
            } else {
                player.sendMessage("§cAlgumas mensagens essenciais não foram carregadas corretamente!");
                player.sendMessage("§cO plugin pode não funcionar corretamente. Verifique o arquivo messages.yml.");
            }
            
            // Verificar se o arquivo messages.yml está em bom estado
            File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
            if (br.com.primeleague.x1.utils.FileUtils.containsProblematicUnicodeCharacters(messagesFile)) {
                player.sendMessage("§cO arquivo messages.yml contém caracteres Unicode problemáticos!");
                player.sendMessage("§eO plugin tentará corrigir automaticamente esses problemas...");
                
                if (br.com.primeleague.x1.utils.FileUtils.removeUnicodeCharacters(messagesFile)) {
                    player.sendMessage("§aCaracteres problemáticos removidos. Recarregando mensagens...");
                    messageManager.reload();
                } else {
                    player.sendMessage("§cNão foi possível remover os caracteres problemáticos.");
                    player.sendMessage("§cVocê precisará editar manualmente o arquivo messages.yml.");
                }
            }
            
            System.out.println("[PrimeLeagueX1] Plugin recarregado por " + player.getName());
        } catch (Exception e) {
            player.sendMessage("§cErro ao recarregar o plugin: " + e.getMessage());
            e.printStackTrace();
            System.out.println("[PrimeLeagueX1] Erro ao recarregar o plugin: " + e.getMessage());
        }
        return true;
    }
} 