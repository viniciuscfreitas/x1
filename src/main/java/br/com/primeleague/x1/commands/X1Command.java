package br.com.primeleague.x1.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import br.com.primeleague.x1.Main;
import br.com.primeleague.x1.managers.MessageManager;

/**
 * Comando principal do plugin
 */
public class X1Command implements CommandExecutor {

    private final Main plugin;
    private final MessageManager messageManager;
    
    /**
     * Construtor
     * 
     * @param plugin Instância do plugin
     */
    public X1Command(Main plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Verificar permissão básica
        if (!player.hasPermission("primeleaguex1.use")) {
            messageManager.sendMessage(player, "geral.sem-permissao");
            return true;
        }
        
        // Sem argumentos - Abre o menu principal
        if (args.length == 0) {
            plugin.getGUIManager().openMainMenu(player);
            return true;
        }
        
        // Subcomandos
        String subCmd = args[0].toLowerCase();
        
        switch (subCmd) {
            case "admin":
                return handleAdminCommand(player, args);
                
            case "desafiar":
            case "challenge":
                return handleChallengeCommand(player, args);
                
            case "aceitar":
            case "accept":
                return handleAcceptCommand(player, args);
                
            case "recusar":
            case "reject":
                return handleRejectCommand(player, args);
                
            case "cancelar":
            case "cancel":
                return handleCancelCommand(player, args);
                
            case "stats":
            case "estatisticas":
                return handleStatsCommand(player, args);
                
            case "top":
            case "ranking":
                return handleTopCommand(player, args);
                
            case "equipe":
            case "team":
                return handleTeamCommand(player, args);
                
            case "assistir":
            case "spectate":
                return handleSpectateCommand(player, args);
                
            case "ajuda":
            case "help":
                return handleHelpCommand(player, args);
                
            default:
                messageManager.sendMessage(player, "geral.comando-inexistente");
                return true;
        }
    }
    
    /**
     * Processa comandos administrativos
     */
    private boolean handleAdminCommand(Player player, String[] args) {
        if (!player.hasPermission("primeleaguex1.admin")) {
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
                plugin.getConfigManager().reloadConfigs();
                messageManager.loadMessages();
                messageManager.sendMessage(player, "geral.plugin-ativado");
                break;
                
            default:
                messageManager.sendMessage(player, "geral.comando-inexistente");
                break;
        }
        
        return true;
    }
    
    /**
     * Processa comando para desafiar outro jogador
     */
    private boolean handleChallengeCommand(Player player, String[] args) {
        if (args.length < 2) {
            messageManager.sendMessage(player, "duelo.erro-uso-desafio");
            return true;
        }
        
        String targetName = args[1];
        Player target = Bukkit.getPlayerExact(targetName);
        
        if (target == null || !target.isOnline()) {
            messageManager.sendMessage(player, "geral.jogador-offline");
            return true;
        }
        
        if (player.getName().equals(target.getName())) {
            messageManager.sendMessage(player, "geral.voce-mesmo");
            return true;
        }
        
        // Verificar se o jogador já está em duelo
        if (plugin.getDuelManager().isInDuel(player.getName())) {
            messageManager.sendMessage(player, "duelo.duelo-em-andamento");
            return true;
        }
        
        // Verificar se o alvo já está em duelo
        if (plugin.getDuelManager().isInDuel(target.getName())) {
            messageManager.sendMessage(player, "duelo.oponente-em-duelo");
            return true;
        }
        
        // Se o comando foi chamado diretamente, abre a GUI de desafio
        plugin.getGUIManager().openChallengeConfirmationMenu(player, target.getName());
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
            messageManager.sendMessage(player, "duelo.sem-desafio-jogador", "%jogador%", challenger.getName());
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
            messageManager.sendMessage(player, "duelo.sem-desafio-jogador", "%jogador%", challenger.getName());
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
            messageManager.sendMessage(player, "duelo.desafio-cancelado", "%jogador%", challenged);
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
     * Processa comandos de equipe
     */
    private boolean handleTeamCommand(Player player, String[] args) {
        if (args.length < 2) {
            plugin.getGUIManager().openTeamMenu(player);
            return true;
        }
        
        String teamCmd = args[1].toLowerCase();
        
        switch (teamCmd) {
            case "criar":
            case "create":
                if (plugin.getTeamManager().isInTeam(player.getName())) {
                    messageManager.sendMessage(player, "equipes.ja-em-equipe");
                    return true;
                }
                
                plugin.getTeamManager().createTeam(player.getName());
                messageManager.sendMessage(player, "equipes.equipe-formada");
                break;
                
            case "convidar":
            case "invite":
                if (args.length < 3) {
                    messageManager.sendMessage(player, "equipes.erro-uso-convite");
                    return true;
                }
                
                if (!plugin.getTeamManager().isTeamLeader(player.getName())) {
                    messageManager.sendMessage(player, "equipes.nao-lider");
                    return true;
                }
                
                String targetName = args[2];
                Player target = Bukkit.getPlayerExact(targetName);
                
                if (target == null || !target.isOnline()) {
                    messageManager.sendMessage(player, "geral.jogador-offline");
                    return true;
                }
                
                if (plugin.getTeamManager().isInTeam(target.getName())) {
                    messageManager.sendMessage(player, "equipes.jogador-em-equipe", "%jogador%", target.getName());
                    return true;
                }
                
                plugin.getGUIManager().openTeamAddMenu(player);
                break;
                
            case "sair":
            case "leave":
                if (!plugin.getTeamManager().isInTeam(player.getName())) {
                    messageManager.sendMessage(player, "equipes.sem-equipe");
                    return true;
                }
                
                plugin.getTeamManager().removeFromTeam(player.getName());
                messageManager.sendMessage(player, "equipes.saiu-equipe");
                break;
                
            case "info":
                if (!plugin.getTeamManager().isInTeam(player.getName())) {
                    messageManager.sendMessage(player, "equipes.sem-equipe");
                    return true;
                }
                
                // Mostra informações da equipe
                String leader = plugin.getTeamManager().getTeamLeader(player.getName());
                java.util.List<String> members = plugin.getTeamManager().getTeam(player.getName());
                
                messageManager.sendMessage(player, "equipes.info-titulo");
                messageManager.sendMessage(player, "equipes.info-lider", "%jogador%", leader);
                
                for (String member : members) {
                    if (!member.equals(leader)) {
                        messageManager.sendMessage(player, "equipes.info-membro", "%jogador%", member);
                    }
                }
                
                break;
                
            default:
                messageManager.sendMessage(player, "geral.comando-inexistente");
                break;
        }
        
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
        player.sendMessage("§6===== §eAjuda: PrimeLeague X1 §6=====");
        player.sendMessage("§e/x1 §7- Abre o menu principal");
        player.sendMessage("§e/x1 desafiar <jogador> §7- Desafia um jogador para duelo");
        player.sendMessage("§e/x1 aceitar [jogador] §7- Aceita um desafio");
        player.sendMessage("§e/x1 recusar [jogador] §7- Recusa um desafio");
        player.sendMessage("§e/x1 cancelar §7- Cancela um desafio enviado");
        player.sendMessage("§e/x1 stats [jogador] §7- Mostra estatísticas");
        player.sendMessage("§e/x1 top §7- Mostra o ranking de jogadores");
        player.sendMessage("§e/x1 equipe §7- Gerencia sua equipe");
        player.sendMessage("§e/x1 assistir §7- Assiste duelos na arena");
        
        if (player.hasPermission("primeleaguex1.admin")) {
            player.sendMessage("§6===== §eComandos administrativos §6=====");
            player.sendMessage("§e/x1 admin §7- Abre o menu administrativo");
            player.sendMessage("§e/x1 admin setpos1 §7- Define a posição 1 da arena");
            player.sendMessage("§e/x1 admin setpos2 §7- Define a posição 2 da arena");
            player.sendMessage("§e/x1 admin setspectator §7- Define local de espectadores");
            player.sendMessage("§e/x1 admin reload §7- Recarrega a configuração");
        }
        
        return true;
    }
} 